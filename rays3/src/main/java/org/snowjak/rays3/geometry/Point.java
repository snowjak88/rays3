package org.snowjak.rays3.geometry;

import org.snowjak.rays3.Global;

/**
 * Represents a set of coordinates in 3-space.
 * 
 * @author snowjak88
 */
public class Point {

	private double x, y, z;

	public Point() {
		x = 0d;
		y = 0d;
		z = 0d;
	}

	public Point(Vector toConvert) {
		this(toConvert.getX(), toConvert.getY(), toConvert.getZ());
	}

	/**
	 * Create a new Point from an array of 4 double values.
	 * 
	 * @param coordinates
	 * @throws IllegalArgumentException
	 *             if the array of values is not of length 4
	 */
	public Point(double[] coordinates) {
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

	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point add(Point addend) {

		return new Point(this.x + addend.x, this.y + addend.y, this.z + addend.z);
	}

	public Point subtract(Point subtrahend) {

		return new Point(this.x - subtrahend.x, this.y - subtrahend.y, this.z - subtrahend.z);
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

}
