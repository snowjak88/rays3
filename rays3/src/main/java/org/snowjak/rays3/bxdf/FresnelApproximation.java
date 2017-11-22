package org.snowjak.rays3.bxdf;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.sqrt;

import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Vector;

/**
 * Calculates Schlick's approximation to Fresnel's equations for reflection and
 * transmittance.
 * 
 * @author snowjak88
 */
public class FresnelApproximation {

	private final Vector	w_e;
	private final Normal	n;
	private final double	n1, n2;

	private Double			reflectance					= null;
	private Vector			reflectedDirection			= null, transmittedDirection = null;

	private Boolean			isTotalInternalReflection	= null;

	public FresnelApproximation(Vector w_e, Normal n, double leavingIndexOfRefraction,
			double enteringIndexOfRefraction) {

		//
		// Source:
		// http://graphics.stanford.edu/courses/cs148-10-summer/docs/2006--degreve--reflection_refraction.pdf
		//
		this.w_e = w_e;
		this.n = n;
		this.n1 = leavingIndexOfRefraction;
		this.n2 = enteringIndexOfRefraction;
	}

	/**
	 * For a given eye-vector and surface-normal, compute the resulting
	 * reflected vector (assuming perfect specular reflection).
	 * 
	 * @param w_e
	 * @param n
	 * @return
	 */
	public Vector getReflectedDirection() {

		if (reflectedDirection != null)
			return reflectedDirection;

		final Vector nv = n.asVector().normalize();
		final Vector i = w_e.normalize();

		final double cos_i = nv.dotProduct(i);
		reflectedDirection = i.negate().add(nv.multiply(2d * cos_i)).normalize();

		return reflectedDirection;
	}

	/**
	 * For a given eye-vector, surface-normal, and pair of
	 * indices-of-refraction, compute the resulting transmitted vector (or
	 * <code>null</code> if this is a case of Total Internal Reflection).
	 * 
	 * @return
	 */
	public Vector getTransmittedDirection() {

		if (transmittedDirection != null)
			return transmittedDirection;

		final Vector normalv = n.asVector().normalize();
		final Vector i = w_e.normalize();

		final double n0 = n1 / n2;
		final double cos_i = normalv.dotProduct(i);
		final double sin2_t = n0 * n0 * ( 1d - cos_i * cos_i );

		if (sin2_t > 1d) {
			// This is a case of Total Internal Reflection -- so nothing is
			// transmitted!
			this.isTotalInternalReflection = true;
			return null;
		} else
			this.isTotalInternalReflection = false;

		final double cos_t = sqrt(1d - sin2_t);
		transmittedDirection = i.negate().multiply(n0).add(normalv.multiply(n0 * cos_i - cos_t)).normalize();

		return transmittedDirection;
	}

	public double getReflectance() {

		if (reflectance != null)
			return reflectance;

		final Vector normalv = n.asVector().normalize();
		final Vector i = w_e.normalize();

		final double n0 = n1 / n2;
		final double cos_i = normalv.dotProduct(i);
		final double sin2_t = n0 * n0 * ( 1d - cos_i * cos_i );

		if (sin2_t > 1d) {
			// This is a case of Total Internal Reflection.
			this.isTotalInternalReflection = true;
			return 1d;
		} else
			this.isTotalInternalReflection = false;

		final double cos_t = sqrt(1d - sin2_t);
		final double r0_rth = ( n1 * cos_i - n2 * cos_t ) / ( n1 * cos_i + n2 * cos_t );
		final double r_par = ( n2 * cos_i - n1 * cos_t ) / ( n2 * cos_i + n1 * cos_t );
		reflectance = min(max(( ( r0_rth * r0_rth + r_par * r_par ) / 2d ), 0d), 1d);

		return reflectance;
	}

	public double getTransmittance() {

		return 1d - getReflectance();
	}

	public boolean isTotalInternalReflection() {

		if (isTotalInternalReflection != null)
			return isTotalInternalReflection;

		final Vector normalv = n.asVector().normalize();
		final Vector i = w_e.normalize();

		final double n0 = n1 / n2;
		final double cos_i = normalv.dotProduct(i);
		final double sin2_t = n0 * n0 * ( 1d - cos_i * cos_i );

		if (sin2_t > 1d) {
			// This is a case of Total Internal Reflection.
			isTotalInternalReflection = true;
		} else
			isTotalInternalReflection = false;

		return isTotalInternalReflection;
	}
}