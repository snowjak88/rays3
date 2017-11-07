package org.snowjak.rays3.transform;

import org.snowjak.rays3.geometry.Matrix;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;

/**
 * Represents a scaling Transform in 3-space.
 * 
 * @author snowjak88
 */
public class ScaleTransform implements Transform {

	private Matrix	worldToLocal	= null, worldToLocal_inverseTranspose = null;
	private Matrix	localToWorld	= null, localToWorld_inverseTranspose = null;

	/**
	 * Create a new ScaleTransform, with the specified
	 * <strong>world-to-local</strong> scaling terms.
	 * 
	 * @param sx
	 * @param sy
	 * @param sz
	 */
	public ScaleTransform(double sx, double sy, double sz) {

		//@formatter:off
		this.worldToLocal = new Matrix(new double[][] {	{ sx,    0d,    0d,    0d },
														{ 0d,    sy,    0d,    0d },
														{ 0d,    0d,    sz,    0d },
														{ 0d,    0d,    0d,    1d } });
		this.localToWorld = new Matrix(new double[][] {	{ 1d/sx, 0d,    0d,    0d },
														{ 0d,    1d/sy, 0d,    0d },
														{ 0d,    0d,    1d/sz, 0d },
														{ 0d,    0d,    0d,    1d } });
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

	@Override
	public Interaction worldToLocal(Interaction interaction) {

		return new Interaction(worldToLocal(interaction.getPoint()), worldToLocal(interaction.getInteractingRay()),
				worldToLocal(interaction.getNormal()), interaction.getBdsf());
	}

	@Override
	public Interaction localToWorld(Interaction interaction) {

		return new Interaction(localToWorld(interaction.getPoint()), localToWorld(interaction.getInteractingRay()),
				localToWorld(interaction.getNormal()), interaction.getBdsf());
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
