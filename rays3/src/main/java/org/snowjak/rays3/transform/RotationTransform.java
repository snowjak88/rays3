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

	private RotationTransform	inverse		= null;

	private Matrix				matrixForm	= null, inverseTransposeMatrix = null;

	/**
	 * Construct a new RotationTransform, representing a rotation about the
	 * specified axis-vector by the specified number of degrees.
	 * 
	 * @param axis
	 * @param degreesOfRotation
	 */
	public RotationTransform(Vector axis, double degreesOfRotation) {
		this(axis, degreesOfRotation, null);
	}

	private RotationTransform(Vector axis, double degreesOfRotation, RotationTransform inverse) {

		axis = axis.normalize();

		final double radians = degreesOfRotation * FastMath.PI / 180d;
		final double l = axis.getX(), m = axis.getY(), n = axis.getZ();
		final double cos = FastMath.cos(radians), sin = FastMath.sin(radians);

		//@formatter:off
		this.matrixForm = new Matrix(new double[][] {	{ l * l * (1d - cos) + cos,     m * l * (1d - cos) - n * sin, n * l * (1d - cos) + m * sin, 0d },
														{ l * m * (1d - cos) + n * sin, m * m * (1d - cos) + cos,     n * m * (1d- cos) - l * sin,  0d },
														{ l * n * (1d - cos) - m * sin, m * n * (1d - cos) + l * sin, n * n * (1d - cos) + cos,     0d },
														{ 0d,                           0d,                           0d,                           1d } });
		//@formatter:on

		this.inverse = inverse;
	}

	private RotationTransform(Matrix matrixForm, RotationTransform inverse) {

		this.matrixForm = matrixForm;
		this.inverse = inverse;
	}

	@Override
	public Point transform(Point point) {

		double[] result = matrixForm.multiply(new double[] { point.getX(), point.getY(), point.getZ(), 1d });

		return new Point(result[0], result[1], result[2]);
	}

	@Override
	public Vector transform(Vector vector) {

		return new Vector(this.transform(new Point(vector)));
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
			this.inverse = new RotationTransform(matrixForm.transpose(), this);

		return this.inverse;
	}

	@Override
	public Matrix getMatrixForm() {

		return matrixForm;
	}

}
