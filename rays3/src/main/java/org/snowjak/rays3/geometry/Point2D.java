package org.snowjak.rays3.geometry;

/**
 * Defines a 2-dimensional coordinate-pair.
 * 
 * @author snowjak88
 */
public class Point2D {

	private final double x, y;

	/**
	 * Create a new 2-d Point at (0,0)
	 */
	public Point2D() {
		this(0d, 0d);
	}

	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {

		return x;
	}

	public double getY() {

		return y;
	}

	@Override
	public String toString() {

		return "Point2D [" + Double.toString(x) + ", " + Double.toString(y) + "]";
	}

}
