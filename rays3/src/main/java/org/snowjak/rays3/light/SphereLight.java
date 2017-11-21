package org.snowjak.rays3.light;

import java.util.List;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
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
	public Vector sampleLightVector(Point towards) {

		//
		// We want to sample a point on the hemisphere nearest to the given
		// point.
		//
		// Accordingly:
		// 1) construct a coordinate system I,J,K, where J points from the light
		// toward the point
		// 2) select a trio of factors x,y,z, where x,z are both in (-1,1) and y
		// in (0,1).
		// 3) construct a Vector pointing toward (xI,yJ,zK)
		// 4) find the Point along the Vector, distance of "radius"
		// 5) trace a Vector from that Point to the "towards" Point
		//
		final Vector towardsV_local = new Vector(worldToLocal(towards));

		final Vector J = towardsV_local.normalize();
		final Vector I = J.orthogonal();
		final Vector K = I.crossProduct(J);

		final double x = 2d * Global.RND.nextDouble() - 1d;
		final double z = 2d * Global.RND.nextDouble() - 1d;
		final double y = Global.RND.nextDouble();

		final Point samplePoint_local = new Point(
				I.multiply(x).add(J.multiply(y)).add(K.multiply(z)).normalize().multiply(radius));
		final Point samplePoint = localToWorld(samplePoint_local);

		return new Vector(towards).subtract(new Vector(samplePoint));
	}

	@Override
	public double probabilitySampleVector(Point towards, Vector sampledVector) {

		return 1d;
	}

}
