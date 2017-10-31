package org.snowjak.rays3.geometry;

import org.apache.commons.math3.linear.MatrixUtils;

/**
 * Represents a 4x4 matrix.
 * 
 * @author snowjak88
 */
public class Matrix {

	//
	// Computing the determinant is an expensive operation.
	// We will compute it only once, and cache it here.
	private double				determinant;
	private boolean				determinantSet	= false;

	//
	// Ditto for the matrix's transpose.
	private Matrix				transpose		= null;

	//
	// Ditto for the matrix's inverse.
	private Matrix				inverse			= null;

	//@formatter:off
	/**
	 * Initialize a new zero-matrix:
	 * <pre>
	 *   |  0  0  0  0  |
	 *   |  0  0  0  0  |
	 *   |  0  0  0  0  |
	 *   |  0  0  0  0  |
	 * </pre>
	 */
	public static final Matrix	ZERO	= new Matrix(new double[][]	{		{ 0d, 0d, 0d, 0d },
																			{ 0d, 0d, 0d, 0d },
																			{ 0d, 0d, 0d, 0d },
																			{ 0d, 0d, 0d, 0d } });
	/**
	 * Initialize a new 4x4-identity matrix:
	 * <pre>
	 *   |  1  0  0  0  |
	 *   |  0  1  0  0  |
	 *   |  0  0  1  0  |
	 *   |  0  0  0  1  |
	 * </pre>
	 */
	public static final Matrix	IDENTITY	= new Matrix(new double[][]	{	{ 1d, 0d, 0d, 0d },
																			{ 0d, 1d, 0d, 0d },
																			{ 0d, 0d, 1d, 0d },
																			{ 0d, 0d, 0d, 1d } });
	//@formatter:on

	private double[][]			values;

	/**
	 * Initialize a new 4x4 matrix with the given 4x4 array of values.
	 * 
	 * @param values
	 * @throws IllegalArgumentException
	 *             if the given array is not a 4x4 array
	 */
	public Matrix(double[][] values) {
		if (values.length != 4 || values[0].length != 4)
			throw new IllegalArgumentException("Expecting a 4x4 array to initialize this 4x4 matrix!");

		this.values = new double[4][4];
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				this.values[i][j] = values[i][j];
	}

	/**
	 * Compute the sum of this and another Matrix.
	 * 
	 * @param addend
	 * @return
	 */
	public Matrix add(Matrix addend) {

		double[][] newValues = new double[4][4];

		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				newValues[i][j] = this.values[i][j] + addend.values[i][j];

		return new Matrix(newValues);
	}

	/**
	 * Compute the difference of this and another Matrix.
	 * 
	 * @param subtrahend
	 * @return
	 */
	public Matrix subtract(Matrix subtrahend) {

		double[][] newValues = new double[4][4];

		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				newValues[i][j] = this.values[i][j] - subtrahend.values[i][j];

		return new Matrix(newValues);
	}

	/**
	 * Compute the product of this Matrix and a scalar value.
	 * 
	 * @param scalar
	 * @return
	 */
	public Matrix multiply(double scalar) {

		double[][] newValues = new double[4][4];

		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				newValues[i][j] = this.values[i][j] * scalar;

		return new Matrix(newValues);
	}

	/**
	 * Compute the matrix-product of this and another Matrix.
	 * 
	 * @param other
	 * @return
	 */
	public Matrix multiply(Matrix other) {

		double[][] newValues = new double[4][4];

		for (int nvI = 0; nvI < 4; nvI++)
			for (int nvJ = 0; nvJ < 4; nvJ++) {

				// Compute the [nvI][nvJ]th entry in the new matrix
				// as the dot product of the [nvI]th row of this matrix
				// and the [nvJ]th column of the other matrix.
				newValues[nvI][nvJ] = 0d;

				for (int i = 0; i < 4; i++)
					newValues[nvI][nvJ] += this.values[nvI][i] * other.values[i][nvJ];
			}

		return new Matrix(newValues);
	}

	/**
	 * Multiply this Matrix by a Vector (treating that as a column-vector with a
	 * 4th value of 1).
	 * 
	 * @param vector
	 * @return
	 */
	public Vector multiply(Vector vector) {

		double[] newVector = this.multiply(new double[] { vector.getX(), vector.getY(), vector.getZ(), 1d });
		return new Vector(newVector[0], newVector[1], newVector[2]);
	}

	/**
	 * Multiply this Matrix by a 1x4 column vector.
	 * <p>
	 * <strong>Note</strong> that this column-vector is normalized to
	 * homogeneous coordinates after multiplication:
	 * 
	 * <pre>
	 *    vect[0] /= vect[3]
	 *    vect[1] /= vect[3]
	 *    vect[2] /= vect[3]
	 *    vect[3] = 1
	 * </pre>
	 * </p>
	 * 
	 * @param columnVector
	 * @return
	 * @throws IllegalArgumentException
	 *             if the given column-vector is not 1x4
	 */
	public double[] multiply(double[] columnVector) {

		if (columnVector.length != 4)
			throw new IllegalArgumentException(
					"Given column-vector is not 1x4 as expected, but 1x" + Integer.toString(columnVector.length));

		double[] newVector = new double[] {
				values[0][0] * columnVector[0] + values[0][1] * columnVector[1] + values[0][2] * columnVector[2]
						+ values[0][3],
				values[1][0] * columnVector[0] + values[1][1] * columnVector[1] + values[1][2] * columnVector[2]
						+ values[1][3],
				values[2][0] * columnVector[0] + values[2][1] * columnVector[1] + values[2][2] * columnVector[2]
						+ values[2][3],
				values[3][0] * columnVector[0] + values[3][1] * columnVector[1] + values[3][2] * columnVector[2]
						+ values[3][3] };

		newVector[0] /= newVector[3];
		newVector[1] /= newVector[3];
		newVector[2] /= newVector[3];
		newVector[3] = 1d;

		return newVector;

	}

	/**
	 * Compute the transposition of this Matrix.
	 * 
	 * @return
	 */
	public Matrix transpose() {

		if (this.transpose == null) {

			double[][] newValues = new double[4][4];

			for (int i = 0; i < 4; i++)
				for (int j = 0; j < 4; j++)
					newValues[i][j] = this.values[j][i];

			this.transpose = new Matrix(newValues);
		}

		return this.transpose;

	}

	/**
	 * Compute the inverse of this Matrix.
	 * 
	 * @return
	 */
	public Matrix inverse() {

		if (this.inverse == null)
			this.inverse = new Matrix(MatrixUtils.inverse(MatrixUtils.createRealMatrix(this.values)).getData());

		return this.inverse;
	}

	/**
	 * Compute the trace of this Matrix.
	 * 
	 * @return
	 */
	public double trace() {

		return this.values[0][0] + this.values[1][1] + this.values[2][2] + this.values[3][3];
	}

	/**
	 * Compute the determinant of this Matrix.
	 * 
	 * @return
	 */
	public double determinant() {

		if (!determinantSet) {
			determinant = this.values[0][3] * this.values[1][2] * this.values[2][1] * this.values[3][0]
					- this.values[0][2] * this.values[1][3] * this.values[2][1] * this.values[3][0]
					- this.values[0][3] * this.values[1][1] * this.values[2][2] * this.values[3][0]
					+ this.values[0][1] * this.values[1][3] * this.values[2][2] * this.values[3][0]
					+ this.values[0][2] * this.values[1][1] * this.values[2][3] * this.values[3][0]
					- this.values[0][1] * this.values[1][2] * this.values[2][3] * this.values[3][0]
					- this.values[0][3] * this.values[1][2] * this.values[2][0] * this.values[3][1]
					+ this.values[0][2] * this.values[1][3] * this.values[2][0] * this.values[3][1]
					+ this.values[0][3] * this.values[1][0] * this.values[2][2] * this.values[3][1]
					- this.values[0][0] * this.values[1][3] * this.values[2][2] * this.values[3][1]
					- this.values[0][2] * this.values[1][0] * this.values[2][3] * this.values[3][1]
					+ this.values[0][0] * this.values[1][2] * this.values[2][3] * this.values[3][1]
					+ this.values[0][3] * this.values[1][1] * this.values[2][0] * this.values[3][2]
					- this.values[0][1] * this.values[1][3] * this.values[2][0] * this.values[3][2]
					- this.values[0][3] * this.values[1][0] * this.values[2][1] * this.values[3][2]
					+ this.values[0][0] * this.values[1][3] * this.values[2][1] * this.values[3][2]
					+ this.values[0][1] * this.values[1][0] * this.values[2][3] * this.values[3][2]
					- this.values[0][0] * this.values[1][1] * this.values[2][3] * this.values[3][2]
					- this.values[0][2] * this.values[1][1] * this.values[2][0] * this.values[3][3]
					+ this.values[0][1] * this.values[1][2] * this.values[2][0] * this.values[3][3]
					+ this.values[0][2] * this.values[1][0] * this.values[2][1] * this.values[3][3]
					- this.values[0][0] * this.values[1][2] * this.values[2][1] * this.values[3][3]
					- this.values[0][1] * this.values[1][0] * this.values[2][2] * this.values[3][3]
					+ this.values[0][0] * this.values[1][1] * this.values[2][2] * this.values[3][3];

			determinantSet = true;
		}

		return determinant;
	}

	/**
	 * Returns <code>true</code> if each value in this Matrix is
	 * +/-<code>tolerance</code> difference from the given array of values.
	 * 
	 * @param values
	 * @return
	 * @throws IllegalArgumentException
	 *             if the given array is not 4x4
	 */
	public boolean equals(double[][] values, double tolerance) {

		if (values.length != 4 || values[0].length != 4)
			throw new IllegalArgumentException("Expecting a 4x4 array of values to compare with this Matrix!");

		boolean foundMismatch = false;

		for (int i = 0; i < 4; i++)
			for (int j = 0; j < 4; j++)
				if (( this.values[i][j] - tolerance > values[i][j] )
						|| ( this.values[i][j] + tolerance < values[i][j] ))
					foundMismatch = true;

		return !foundMismatch;
	}

}
