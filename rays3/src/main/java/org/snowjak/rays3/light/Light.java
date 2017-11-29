package org.snowjak.rays3.light;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleFunction;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.Global;
import org.snowjak.rays3.World;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.transform.Transform;
import org.snowjak.rays3.transform.Transformable;

/**
 * Represents a source of radiance.
 * 
 * @author snowjak88
 */
public abstract class Light implements Transformable {

	private FalloffType					falloffType;
	private Spectrum					unitRadiance;

	private final LinkedList<Transform>	worldToLocal	= new LinkedList<>();
	private final LinkedList<Transform>	localToWorld	= new LinkedList<>();

	/**
	 * Construct a new Light of the given radiance per unit-solid-angle, and the
	 * default fall-off rate of {@link FalloffType#QUADRATIC}.
	 * 
	 * @param unitRadiance
	 */
	public Light(Spectrum unitRadiance, List<Transform> worldToLocal) {
		this(unitRadiance, FalloffType.QUADRATIC, worldToLocal);
	}

	/**
	 * Construct a new Light of the given radiance per unit-solid-angle and
	 * falloff-type.
	 * 
	 * @param unitRadiance
	 * @param falloffType
	 */
	public Light(Spectrum unitRadiance, FalloffType falloffType, List<Transform> worldToLocal) {
		this.unitRadiance = unitRadiance;
		this.falloffType = falloffType;

		for (Transform t : worldToLocal)
			this.appendTransform(t);
	}

	/**
	 * Returns this Light's radiance per unit-solid-angle.
	 */
	public Spectrum getUnitRadiance() {

		return unitRadiance;
	}

	/**
	 * Returns the total power (i.e., sum of emitted energy) emitted by this
	 * Light in every possible direction.
	 * <p>
	 * By default, this means "every direction on the unit sphere", or 4&pi; *
	 * L, where L is radiant intensity per unit-solid-angle.
	 * </p>
	 */
	public Spectrum getPower() {

		return unitRadiance.multiply(4d * FastMath.PI);
	}

	/**
	 * Construct a {@link Vector} <em>from</em> a sampled point on the surface
	 * of this Light, <em>towards</em> the indicated {@link Point}
	 * <code>towards</code>. All coordinates expressed in the global/world
	 * frame.
	 * <p>
	 * <strong>Note</strong> that this Vector should <strong>not</strong> be
	 * normalized. Instead, its magnitude will indicate the distance between the
	 * Light surface and the Point.
	 * </p>
	 * 
	 * @param towards
	 * @param sample
	 * @return
	 */
	public abstract Vector sampleLightVector(Point towards, Sample sample);

	/**
	 * Given a {@link Point} <code>towards</code> and a {@link Vector} we
	 * sampled as pointing from this Light toward that Point, what is the
	 * probability that we would have chosen that Vector?
	 * 
	 * @param towards
	 * @param sampledVector
	 * @param sample
	 * @return
	 */
	public abstract double probabilitySampleVector(Point towards, Vector sampledVector, Sample sample);

	/**
	 * Given a surface-point with a corresponding surface-normal <code>n</code>,
	 * and a sampled Vector <code>sampleLightVector</code> (obtained from
	 * {@link #sampleLightVector(Point)}), determine the total radiant energy
	 * available to that Point (measured in W/m^2/sr).
	 * <p>
	 * This method will factor in the total falloff due to distance.
	 * </p>
	 * 
	 * @param sampleLightVector
	 * @return
	 * @see #sampleLightVector(Point)
	 * @see FalloffType#calculate(double)
	 */
	public Spectrum getRadianceAt(Vector sampleLightVector) {

		if (getUnitRadiance().isBlack())
			return getUnitRadiance();

		final double falloffFraction = getFalloff().calculate(sampleLightVector.getMagnitude());

		return getUnitRadiance().multiply(falloffFraction);
	}

	/**
	 * Checks to see if the given point on the surface of a Light is visible
	 * from the given Point -- or, more properly, if any {@link Interaction}s
	 * can be detected when tracing a Ray from <code>pointFrom</code> to
	 * <code>lightSurfacePoint</code>.
	 * 
	 * @param world
	 * @param pointFrom
	 * @param lightSurfacePoint
	 * @return
	 */
	public static boolean isVisibleFrom(World world, Point pointFrom, Point lightSurfacePoint) {

		final Vector toLight = new Vector(lightSurfacePoint).subtract(new Vector(pointFrom));
		final Ray ray = new Ray(pointFrom, toLight.normalize());

		return world
				.getPrimitives()
					.stream()
					.filter(p -> p.isInteracting(ray))
					.map(p -> p.getIntersection(ray))
					.filter(i -> i != null)
					.filter(i -> i.getInteractingRay().getCurrT() > 0d
							&& !Global.isNear(i.getInteractingRay().getCurrT(), 0d))
					.allMatch(i -> i.getInteractingRay().getCurrT() > toLight.getMagnitude());
	}

	/**
	 * Given a {@link Point}, and a {@link Vector} from the surface of this
	 * Light to that point (see {@link #sampleLightVector(Point)}), calculate
	 * the corresponding {@link Point} on this Light's surface.
	 * 
	 * @param from
	 * @param sampleLightVector
	 * @return
	 */
	public static Point getLightSurfacePoint(Point from, Vector sampleLightVector) {

		return new Point(sampleLightVector.negate().add(new Vector(from)));
	}

	/**
	 * @return the {@link FalloffType} employed by this Light.
	 */
	public FalloffType getFalloff() {

		return this.falloffType;
	}

	@Override
	public List<Transform> getWorldToLocalTransforms() {

		return Collections.unmodifiableList(worldToLocal);
	}

	@Override
	public List<Transform> getLocalToWorldTransforms() {

		return Collections.unmodifiableList(localToWorld);
	}

	@Override
	public void appendTransform(Transform transform) {

		worldToLocal.addLast(transform);
		localToWorld.addFirst(transform);
	}

	/**
	 * Encapsulates logic for computing a {@link Light}'s fall-off fraction at
	 * different distances.
	 * 
	 * @author snowjak88
	 */
	public enum FalloffType {
		/**
		 * "Constant" fall-off -- i.e., no fall-off at all. Light is of the same
		 * intensity at all distances. Not very realistic, except for
		 * immensely-powerful light-sources at great distances (e.g., the Sun).
		 */
		CONSTANT(d -> 1.0),
		/**
		 * Linear fall-off, equal to
		 * 
		 * <pre>
		 *                1
		 * falloff = ----------
		 *            distance
		 * </pre>
		 */
		LINEAR(d -> 1.0 / d),
		/**
		 * Quadratic (i.e., realistic) fall-off, equal to
		 * 
		 * <pre>
		 *                  1
		 * falloff = --------------
		 *            distance ^ 2
		 * </pre>
		 */
		QUADRATIC(d -> 1.0 / ( d * d ));

		private DoubleFunction<Double> implementation;

		FalloffType(DoubleFunction<Double> implementation) {
			this.implementation = implementation;
		}

		/**
		 * Calculate the fall-off fraction at the specified distance.
		 * 
		 * @param distance
		 * @return
		 */
		public double calculate(double distance) {

			return implementation.apply(distance);
		}
	}
}
