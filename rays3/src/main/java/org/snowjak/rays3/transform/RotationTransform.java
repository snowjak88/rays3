package org.snowjak.rays3.transform;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.geometry.Matrix;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;

/**
 * Represent a rotating Transform in 3-space -- specifically, a measure of
 * degrees of rotation about an arbitrary axis-vector.
 * 
 * @author snowjak88
 */
public class RotationTransform implements Transform {

	private Matrix	worldToLocal	= null, worldToLocal_inverseTranspose = null;
	private Matrix	localToWorld	= null, localToWorld_inverseTranspose = null;

	/**
	 * Construct a new RotationTransform, representing a rotation about the
	 * specified axis-vector by the specified number of degrees.
	 * 
	 * @param axis
	 * @param degreesOfRotation
	 */

	public RotationTransform(Vector axis, double degreesOfRotation) {

		axis = axis.normalize();

		final double radians = degreesOfRotation * FastMath.PI / 180d;
		final double ax = axis.getX(), ay = axis.getY(), az = axis.getZ();
		final double ax2 = FastMath.pow(ax, 2), ay2 = FastMath.pow(ay, 2), az2 = FastMath.pow(az, 2);
		final double cos = FastMath.cos(radians), sin = FastMath.sin(radians);

		//@formatter:off
		this.worldToLocal = new Matrix(new double[][] {	{ cos + ax2*(1d - cos),     ax*ay*(1d - cos) - az*sin, ax*az*(1d - cos) + ay*sin, 0d },
														{ ay*ax*(1 - cos) + az*sin, cos + ay2*(1d - cos),      ay*az*(1d - cos) - ax*sin, 0d },
														{ az*ax*(1 - cos) - ay*sin, az*ay*(1d - cos) + ax*sin, cos + az2*(1d - cos),      0d },
														{ 0d,                       0d,                        0d,                        1d } });
		//@formatter:on
		this.localToWorld = this.worldToLocal.transpose();
	}

	@Override
	public Point worldToLocal(Point point) {

		return new Point(apply(worldToLocal, point.getX(), point.getY(), point.getZ(), 1d));
	}

	@Override
	public Point localToWorld(Point point) {

		return new Point(apply(localToWorld, point.getX(), point.getY(), point.getZ(), 1d));
	}

	@Override
	public Vector worldToLocal(Vector vector) {

		return new Vector(apply(worldToLocal, vector.getX(), vector.getY(), vector.getZ(), 1d));
	}

	@Override
	public Vector localToWorld(Vector vector) {

		return new Vector(apply(localToWorld, vector.getX(), vector.getY(), vector.getZ(), 1d));
	}

	@Override
	public Ray worldToLocal(Ray ray) {

		return new Ray(worldToLocal(ray.getOrigin()), worldToLocal(ray.getDirection()));
	}

	@Override
	public Ray localToWorld(Ray ray) {

		return new Ray(localToWorld(ray.getOrigin()), localToWorld(ray.getDirection()));
	}

	@Override
	public Normal worldToLocal(Normal normal) {

		if (worldToLocal_inverseTranspose == null)
			worldToLocal_inverseTranspose = worldToLocal.inverse().transpose();

		return new Normal(apply(worldToLocal_inverseTranspose, normal.getX(), normal.getY(), normal.getZ(), 1d));
	}

	@Override
	public Normal localToWorld(Normal normal) {

		if (localToWorld_inverseTranspose == null)
			localToWorld_inverseTranspose = localToWorld.inverse().transpose();

		return new Normal(apply(localToWorld_inverseTranspose, normal.getX(), normal.getY(), normal.getZ(), 1d));
	}

	private double[] apply(Matrix matrix, double... coordinates) {

		return matrix.multiply(coordinates);
	}

	@Override
	public Matrix getWorldToLocal() {

		return worldToLocal;
	}

	@Override
	public Matrix getLocalToWorld() {

		return localToWorld;
	}

}
