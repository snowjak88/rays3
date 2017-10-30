package org.snowjak.rays3.transform;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Quarternion;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;

/**
 * Represent a rotating Transform in 3-space -- specifically, a measure of
 * degrees of rotation about an arbitrary axis-vector.
 * <p>
 * Internally, this implementation uses quarternions to model rotations.
 * </p>
 * 
 * @author snowjak88
 */
public class RotationTransform implements Transform {

	private Quarternion			quarternion, reciprocal;
	private RotationTransform	inverse	= null;

	/**
	 * Construct a new RotationTransform, representing a rotation about the
	 * specified axis-vector by the specified number of degrees.
	 * 
	 * @param axis
	 * @param degreesOfRotation
	 */
	public RotationTransform(Vector axis, double degreesOfRotation) {

		final double radians = degreesOfRotation * FastMath.PI / 180d;
		axis = axis.normalize();

		//@formatter:off
		this.quarternion = new Quarternion(
					(FastMath.cos(radians / 2d)),
					(axis.getX() * FastMath.sin(radians / 2d)),
					(axis.getY() * FastMath.sin(radians / 2d)),
					(axis.getZ() * FastMath.sin(radians / 2d))
				);
		//@formatter:on

		this.quarternion = this.quarternion.normalize();
		this.reciprocal = this.quarternion.reciprocal();
	}

	public RotationTransform(Quarternion quarternion) {
		this.quarternion = quarternion.normalize();
		this.reciprocal = this.quarternion.reciprocal();
	}

	private RotationTransform(Quarternion quarternion, RotationTransform inverse) {

		this.quarternion = quarternion.normalize();
		this.reciprocal = this.quarternion.reciprocal();
		this.inverse = inverse;
	}

	@Override
	public Point transform(Point point) {

		return this.apply(point);
	}

	@Override
	public Vector transform(Vector vector) {

		return new Vector(this.transform(new Point(vector)));
	}

	@Override
	public Ray transform(Ray ray) {

		return new Ray(this.transform(ray.getOrigin()), this.transform(ray.getDirection()));
	}

	private Point apply(Point point) {

		Quarternion pointQuart = new Quarternion(0d, point.getX(), point.getY(), point.getZ());

		pointQuart = quarternion.multiply(pointQuart).multiply(reciprocal);

		return new Point(pointQuart.getB(), pointQuart.getC(), pointQuart.getD());
	}

	@Override
	public Transform getInverse() {

		if (this.inverse == null)
			this.inverse = new RotationTransform(quarternion.reciprocal(), this);

		return this.inverse;
	}

}
