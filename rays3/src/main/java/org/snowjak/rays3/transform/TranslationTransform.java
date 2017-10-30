package org.snowjak.rays3.transform;

import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;

/**
 * Represents a translating Transform in 3-space.
 * 
 * @author snowjak88
 */
public class TranslationTransform implements Transform {

	private double					dx, dy, dz;
	private TranslationTransform	inverse	= null;

	public TranslationTransform(double dx, double dy, double dz) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}

	/**
	 * Private constructor; used to explicitly initialize the "inverse" link.
	 * 
	 * @param dx
	 * @param dy
	 * @param dz
	 * @param inverse
	 */
	private TranslationTransform(double dx, double dy, double dz, TranslationTransform inverse) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		this.inverse = inverse;
	}

	@Override
	public Point transform(Point point) {

		return new Point(point.getX() + dx, point.getY() + dy, point.getZ() + dz);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note</strong>: a translation is considered to have <strong>no
	 * effect</strong> on a Vector.
	 * </p>
	 */
	@Override
	public Vector transform(Vector vector) {

		return vector;
	}

	@Override
	public Ray transform(Ray ray) {

		return new Ray(this.transform(ray.getOrigin()), ray.getDirection());
	}

	@Override
	public Transform getInverse() {

		if (inverse == null)
			inverse = new TranslationTransform(-dx, -dy, -dz, this);

		return inverse;
	}

}
