package org.snowjak.rays3.transform;

import org.snowjak.rays3.geometry.Matrix;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;

/**
 * Represents a scaling Transform in 3-space.
 * 
 * @author snowjak88
 */
public class ScaleTransform implements Transform {

	private double			sx, sy, sz;
	private ScaleTransform	inverse		= null;

	private Matrix			matrixForm	= null, inverseTransposeMatrix = null;

	public ScaleTransform(double sx, double sy, double sz) {
		this(sx, sy, sz, null);
	}

	private ScaleTransform(double sx, double sy, double sz, ScaleTransform inverse) {
		this.sx = sx;
		this.sy = sy;
		this.sz = sz;
		this.inverse = inverse;

		//@formatter:off
		this.matrixForm = new Matrix(new double[][] {	{ sx, 0d, 0d, 0d },
														{ 0d, sy, 0d, 0d },
														{ 0d, 0d, sz, 0d },
														{ 0d, 0d, 0d, 1d} });
		//@formatter:on
	}

	@Override
	public Point transform(Point point) {

		return new Point(point.getX() * sx, point.getY() * sy, point.getZ() * sz);
	}

	@Override
	public Vector transform(Vector vector) {

		return new Vector(vector.getX() * sx, vector.getY() * sy, vector.getZ() * sz);
	}

	@Override
	public Ray transform(Ray ray) {

		return new Ray(this.transform(ray.getOrigin()), this.transform(ray.getDirection()));
	}

	@Override
	public Normal transform(Normal normal) {

		if (inverseTransposeMatrix == null)
			inverseTransposeMatrix = matrixForm.inverse().transpose();

		double[] transformedResult = inverseTransposeMatrix
				.multiply(new double[] { normal.getX(), normal.getY(), normal.getZ(), 1d });

		return new Normal(transformedResult[0], transformedResult[1], transformedResult[2]);
	}

	@Override
	public Transform getInverse() {

		if (this.inverse == null)
			this.inverse = new ScaleTransform(1d / sx, 1d / sy, 1d / sz, this);

		return this.inverse;
	}

	@Override
	public Matrix getMatrixForm() {

		return matrixForm;
	}

}
