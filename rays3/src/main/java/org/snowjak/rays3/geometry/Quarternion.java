package org.snowjak.rays3.geometry;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.transform.RotationTransform;

/**
 * Represents a quarternion.
 * <p>
 * A quarternion is an extension of complex-numbers from a 2-dimensional (<em>a
 * + b<strong>i</strong></em>) to a 4-dimensional (<em>a + b<strong>i</strong> +
 * c<strong>j</strong> + d<strong>k</strong></em>) field. They have many
 * applications, including (for us) the ability to efficiently represent spatial
 * rotations.
 * </p>
 * 
 * @author snowjak88
 * @see RotationTransform
 */
public class Quarternion {

	private final double	a, b, c, d;
	private double			norm	= -1d;

	public Quarternion() {
		this(0d, 0d, 0d, 0d);
	}

	public Quarternion(double a, double b, double c, double d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	/**
	 * Compute the sum of this and another Quarternion.
	 * 
	 * @param addend
	 * @return
	 */
	public Quarternion add(Quarternion addend) {

		//@formatter:off
		return new Quarternion(
					(this.a + addend.a),
					(this.b + addend.b),
					(this.c + addend.c),
					(this.d + addend.d)
				);
		//@formatter:on
	}

	/**
	 * Compute the difference of this and another Quarternion.
	 * 
	 * @param subtrahend
	 * @return
	 */
	public Quarternion subtract(Quarternion subtrahend) {

		//@formatter:off
		return new Quarternion(
					(this.a - subtrahend.a),
					(this.b - subtrahend.b),
					(this.c - subtrahend.c),
					(this.d - subtrahend.d)
				);
		//@formatter:on
	}

	/**
	 * Compute the Hamiltonian product of this and another Quarternion.
	 * 
	 * @param other
	 * @return
	 */
	public Quarternion multiply(Quarternion other) {

		//@formatter:off
		return new Quarternion(
					(this.a * other.a - this.b * other.b - this.c * other.c - this.d * other.d),
					(this.a * other.b + this.b * other.a + this.c * other.d - this.d * other.c),
					(this.a * other.c - this.b * other.d + this.c * other.a + this.d * other.b),
					(this.a * other.d + this.b * other.c - this.c * other.b + this.d * other.a)
				);
		//@formatter:on
	}

	/**
	 * Compute the scalar product of this Quarternion and a scalar value.
	 * 
	 * @param scalar
	 * @return
	 */
	public Quarternion multiply(double scalar) {

		//@formatter:off
		return new Quarternion(
					(this.a * scalar),
					(this.b * scalar),
					(this.c * scalar),
					(this.d * scalar)
				);
		//@formatter:on
	}

	/**
	 * Compute this Quarternion's conjugate.
	 * 
	 * @return
	 */
	public Quarternion conjugate() {

		//@formatter:off
		return new Quarternion(
					(this.a),
					(-this.b),
					(-this.c),
					(-this.d)
				);
		//@formatter:on
	}

	/**
	 * Compute this Quarternion's norm.
	 * 
	 * @return
	 */
	public double norm() {

		if (this.norm < 0d)
			this.norm = FastMath.sqrt(FastMath.pow(this.a, 2) + FastMath.pow(this.b, 2) + FastMath.pow(this.c, 2)
					+ FastMath.pow(this.d, 2));

		return this.norm;
	}

	/**
	 * Normalize this Quarternion -- i.e., convert it into a unit quarternion.
	 * 
	 * @return
	 */
	public Quarternion normalize() {

		return this.multiply(1d / this.norm());
	}

	/**
	 * Compute this Quarternion's reciprocal.
	 * 
	 * @return
	 */
	public Quarternion reciprocal() {

		return this.conjugate().multiply(FastMath.pow(this.norm(), 2));
	}

	/**
	 * Return a List containing all 4 elements of this Quarternion in the order
	 * <code>{ a, b, c, d }</code>.
	 * 
	 * @return
	 */
	public List<Double> toList() {

		return Arrays.asList(a, b, c, d);
	}

	public double getA() {

		return a;
	}

	public double getB() {

		return b;
	}

	public double getC() {

		return c;
	}

	public double getD() {

		return d;
	}
}
