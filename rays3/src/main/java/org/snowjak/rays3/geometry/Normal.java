package org.snowjak.rays3.geometry;

/**
 * A Normal is like a Vector in that it denotes direction, but it differs from a
 * Vector in two very crucial respects:
 * <ul>
 * <li>It is not usually normalized</li>
 * <li>It has special rules regarding the application of spatial Transforms</li>
 * </ul>
 * 
 * @author snowjak88
 */
public class Normal {

	private Vector direction;

	public Normal(double nx, double ny, double nz) {
		this.direction = new Vector(nx, ny, nz);
	}

	public Normal(Vector vector) {
		this.direction = vector;
	}

	public Normal negate() {

		return new Normal(direction.negate());
	}

	public double getX() {

		return direction.getX();
	}

	public double getY() {

		return direction.getY();
	}

	public double getZ() {

		return direction.getZ();
	}

	public Vector asVector() {

		return direction;
	}

}
