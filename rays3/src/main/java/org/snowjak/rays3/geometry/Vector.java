package org.snowjak.rays3.geometry;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.Global;

/**
 * Represents a direction + magnitude in 3-space.
 * 
 * @author snowjak88
 */
public class Vector {

	public static final Vector	ZERO		= new Vector(0d, 0d, 0d, 0d, 0d);
	public static final Vector	I			= new Vector(1d, 0d, 0d, 1d, 1d), J = new Vector(0d, 1d, 0d, 1d, 1d),
			K = new Vector(0d, 0d, 1d, 1d, 1d);

	private double				x, y, z;
	private double				magnitude	= -1d, magnitudeSquared = -1d;

	public Vector() {
		this(0d, 0d, 0d, 0d, 0d);
	}

	public Vector(Point pointTowards) {
		this(pointTowards.getX(), pointTowards.getY(), pointTowards.getZ());
	}

	public Vector(Normal convertFrom) {
		this(convertFrom.getX(), convertFrom.getY(), convertFrom.getZ());
	}

	/**
	 * Create a new Vector from an array of 4 double values.
	 * 
	 * @param coordinates
	 * @throws IllegalArgumentException
	 *             if the array of values is not of length 4
	 */
	public Vector(double[] coordinates) {
		if (coordinates.length != 4)
			throw new IllegalArgumentException("Expecting the array of coordinates to be of length 4!");

		if (Global.isNear(coordinates[3], 1d)) {
			this.x = coordinates[0];
			this.y = coordinates[1];
			this.z = coordinates[2];
		} else {
			this.x = coordinates[0] / coordinates[3];
			this.y = coordinates[1] / coordinates[3];
			this.z = coordinates[2] / coordinates[3];
		}
	}

	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	private Vector(double x, double y, double z, double magnitude, double magnitudeSquared) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.magnitude = magnitude;
		this.magnitudeSquared = magnitudeSquared;
	}

	public Vector normalize() {

		return new Vector(x / getMagnitude(), y / getMagnitude(), z / getMagnitude());
	}

	public Vector negate() {

		return new Vector(-x, -y, -z);
	}

	public Vector add(Vector addend) {

		return new Vector(x + addend.x, y + addend.y, z + addend.z);
	}

	public Vector subtract(Vector subtrahend) {

		return new Vector(x - subtrahend.x, y - subtrahend.y, z - subtrahend.z);
	}

	public Vector multiply(double scalar) {

		return new Vector(x * scalar, y * scalar, z * scalar);
	}

	public Vector divide(double scalar) {

		return multiply(1d / scalar);
	}

	public double dotProduct(Vector other) {

		return x * other.x + y * other.y + z * other.z;
	}

	public Vector crossProduct(Vector other) {

		return new Vector(this.y * other.z - this.z * other.y, this.z * other.x - this.x * other.z,
				this.x * other.y - this.y * other.x);
	}

	public double getX() {

		return x;
	}

	public double getY() {

		return y;
	}

	public double getZ() {

		return z;
	}

	public double getMagnitude() {

		if (magnitude < 0d)
			magnitude = FastMath.sqrt(getMagnitudeSquared());
		return magnitude;
	}

	public double getMagnitudeSquared() {

		if (magnitudeSquared < 0d)
			magnitudeSquared = FastMath.pow(x, 2) + FastMath.pow(y, 2) + FastMath.pow(z, 2);
		return magnitudeSquared;
	}

}
