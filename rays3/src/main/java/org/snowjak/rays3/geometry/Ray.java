package org.snowjak.rays3.geometry;

/**
 * Represents a direction + origin in 3-space.
 * 
 * @author snowjak88
 */
public class Ray {

	private Point	origin;
	private Vector	direction;

	public Ray() {
		this.origin = new Point();
		this.direction = new Vector();
	}

	public Ray(Point origin, Vector direction) {
		this.origin = origin;
		this.direction = direction.normalize();
	}

	public Point getPointAlong(double t) {

		return origin.add(new Point(direction.multiply(t)));
	}

	public Point getOrigin() {

		return origin;
	}

	public Vector getDirection() {

		return direction;
	}
}
