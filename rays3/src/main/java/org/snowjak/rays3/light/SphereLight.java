package org.snowjak.rays3.light;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.List;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.transform.Transform;

/**
 * Models a {@link Light} shaped like a sphere.
 * 
 * @author snowjak88
 */
public class SphereLight extends Light {

	private final double radius;

	/**
	 * Create a new {@link SphereLight} with the default radius of 0.5.
	 * 
	 * @param unitRadiance
	 * @param worldToLocal
	 */
	public SphereLight(Spectrum unitRadiance, List<Transform> worldToLocal) {
		this(unitRadiance, worldToLocal, 0.5);
	}

	/**
	 * Create a new {@link SphereLight} with the given radius.
	 * 
	 * @param unitRadiance
	 * @param worldToLocal
	 * @param radius
	 */
	public SphereLight(Spectrum unitRadiance, List<Transform> worldToLocal, double radius) {
		super(unitRadiance, worldToLocal);
		this.radius = radius;
	}

	@Override
	public Vector sampleLightVector(Point towards, Sample sample) {

		//
		// We want to sample a point on the hemisphere nearest to the given
		// point.
		//
		// Accordingly:
		// 1) construct a coordinate system I,J,K, where J points from the light
		// toward the point
		// 2) select a pair of surface angles, phi and theta, and convert them
		// to (x,y,z) surface coordinates on a sphere
		// 3) construct a Vector pointing toward (xI,yJ,zK)
		// 4) find the Point along the Vector, distance of "radius"
		// 5) trace a Vector from that Point to the "towards" Point
		//
		final Vector towardsV_local = new Vector(worldToLocal(towards));

		final Vector J = towardsV_local.normalize();
		final Vector I = J.orthogonal();
		final Vector K = I.crossProduct(J);
		//
		//
		//
		final double sin2_theta = Global.RND.nextDouble();
		final double cos2_theta = 1d - sin2_theta;
		final double sin_theta = sqrt(sin2_theta);
		final double cos_theta = sqrt(cos2_theta);

		final double orientation = Global.RND.nextDouble() * 2d * PI;
		//
		//
		//
		final double x = sin_theta * cos(orientation);
		final double y = cos_theta;
		final double z = sin_theta * sin(orientation);

		final Vector samplePoint_local = I.multiply(x).add(J.multiply(y)).add(K.multiply(z)).multiply(radius);
		final Point samplePoint = localToWorld(new Point(samplePoint_local));

		return new Vector(towards).subtract(new Vector(samplePoint));
	}

	@Override
	public double probabilitySampleVector(Point towards, Vector sampledVector, Sample sample) {

		return 1d / ( 2d * PI );
	}

}
