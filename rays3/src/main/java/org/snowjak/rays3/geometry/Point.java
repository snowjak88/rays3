package org.snowjak.rays3.geometry;

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
