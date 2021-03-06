package org.snowjak.rays3.transform;

import org.snowjak.rays3.geometry.Matrix;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;

/**
 * Represents a translating Transform in 3-space.
 * 
 * @author snowjak88
 */
public class TranslationTransform implements Transform {

	private Matrix worldToLocal, localToWorld;

	/**
	 * Create a new TranslationTransform, with the specified
	 * <strong>world-to-local</strong> translation terms.
	 * 
	 * @param dx
	 * @param dy
	 * @param dz
	 */
	public TranslationTransform(double dx, double dy, double dz) {

		//@formatter:off
		this.worldToLocal = new Matrix(new double[][] {	{ 1d, 0d, 0d, -dx },
														{ 0d, 1d, 0d, -dy },
														{ 0d, 0d, 1d, -dz },
														{ 0d, 0d, 0d,  1d } });
		this.localToWorld = new Matrix(new double[][] {	{ 1d, 0d, 0d, +dx },
														{ 0d, 1d, 0d, +dy },
														{ 0d, 0d, 1d, +dz },
														{ 0d, 0d, 0d,  1d } });
		//@formatter:on
	}

	@Override
	public Point worldToLocal(Point point) {

		return new Point(apply(worldToLocal, point.getX(), point.getY(), point.getZ(), 1d));
	}

	@Override
	public Point localToWorld(Point point) {

		return new Point(apply(localToWorld, point.getX(), point.getY(), point.getZ(), 1d));
	}

	/**
	 * As a rule, Vectors are considered to be unaffected by translations.
	 */
	@Override
	public Vector worldToLocal(Vector vector) {

		return vector;
	}

	/**
	 * As a rule, Vectors are considered to be unaffected by translations.
	 */
	@Override
	public Vector localToWorld(Vector vector) {

		return vector;
	}

	@Override
	public Ray worldToLocal(Ray ray) {

		return new Ray(worldToLocal(ray.getOrigin()), worldToLocal(ray.getDirection()), ray.getDepth(), ray.getCurrT(),
				ray.getMinT(), ray.getMaxT(), ray.getWeight());
	}

	@Override
	public Ray localToWorld(Ray ray) {

		return new Ray(localToWorld(ray.getOrigin()), localToWorld(ray.getDirection()), ray.getDepth(), ray.getCurrT(),
				ray.getMinT(), ray.getMaxT(), ray.getWeight());
	}

	/**
	 * As a rule, Normals are considered to be unaffected by translations.
	 */
	@Override
	public Normal worldToLocal(Normal normal) {

		return normal;
	}

	/**
	 * As a rule, Normals are considered to be unaffected by translations.
	 */
	@Override
	public Normal localToWorld(Normal normal) {

		return normal;
	}

	@Override
	public Interaction worldToLocal(Interaction interaction) {

		return new Interaction(worldToLocal(interaction.getPoint()), worldToLocal(interaction.getInteractingRay()),
				worldToLocal(interaction.getNormal()), interaction.getParam(), interaction.getPrimitive());
	}

	@Override
	public Interaction localToWorld(Interaction interaction) {

		return new Interaction(localToWorld(interaction.getPoint()), localToWorld(interaction.getInteractingRay()),
				localToWorld(interaction.getNormal()), interaction.getParam(), interaction.getPrimitive());
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
