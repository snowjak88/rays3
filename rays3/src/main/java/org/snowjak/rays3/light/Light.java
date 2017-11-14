package org.snowjak.rays3.light;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleFunction;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.spectrum.RGBSpectrum;
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
	public Light(Spectrum unitRadiance) {
		this(unitRadiance, FalloffType.QUADRATIC);
	}

	/**
	 * Construct a new Light of the given radiance per unit-solid-angle and
	 * falloff-type.
	 * 
	 * @param unitRadiance
	 * @param falloffType
	 */
	public Light(Spectrum unitRadiance, FalloffType falloffType) {
		this.unitRadiance = unitRadiance;
		this.falloffType = falloffType;
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
	 * @return
	 */
	public abstract Vector sampleLightVector(Point towards);

	/**
	 * Given a {@link Point} <code>towards</code> and a {@link Vector} we
	 * sampled as pointing from this Light toward that Point, what is the
	 * probability that we would have chosen that Vector?
	 * 
	 * @param towards
	 * @param sampledVector
	 * @return
	 */
	public abstract double probabilitySampleVector(Point towards, Vector sampledVector);

	/**
	 * Given a surface-point with a corresponding surface-normal <code>n</code>,
	 * and a sampled Vector <code>sampleLightVector</code> (obtained from
	 * {@link #sampleLightVector(Point)}), determine the total radiant energy
	 * available to that Point.
	 * <p>
	 * This method will calculate the total radiant energy using Lambert's
	 * cosine law, and factor in falloff due to distance.
	 * </p>
	 * 
	 * @param sampleLightVector
	 * @param n
	 * @return
	 * @see #sampleLightVector(Point)
	 * @see FalloffType#calculate(double)
	 */
	public Spectrum getRadianceAt(Vector sampleLightVector, Normal n) {

		if (getUnitRadiance().isBlack())
			return getUnitRadiance();

		final double cosTheta = n.asVector().dotProduct(sampleLightVector.normalize().negate());

		if (cosTheta < 0d)
			return new RGBSpectrum();

		return getUnitRadiance().multiply(cosTheta).multiply(getFalloff().calculate(sampleLightVector.getMagnitude()));
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
