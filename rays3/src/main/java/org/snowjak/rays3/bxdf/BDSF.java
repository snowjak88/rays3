package org.snowjak.rays3.bxdf;

import static org.apache.commons.math3.util.FastMath.*;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.Texture;

/**
 * Represents a Bi-Directional Scattering Function.
 * 
 * @author snowjak88
 */
public abstract class BDSF {

	private final double indexOfRefraction;

	public BDSF(double indexOfRefraction) {

		this.indexOfRefraction = indexOfRefraction;
	}

	/**
	 * Sample the radiant energy reflectable from the given point. This yields a
	 * {@link Spectrum} giving the maximum "reflectable" (i.e., potential)
	 * radiance obtained by reflection from this point on the surface. This is
	 * usually heavily determined by a surface {@link Texture}. Usually, you
	 * would multiply the resulting Spectrum by another Spectrum obtained by
	 * ray-tracing the radiance actually incident on this point.
	 * <p>
	 * Specifically, calculates:
	 * 
	 * <pre>
	 * f<sub>r</sub> ( <strong>x</strong>, <strong>w</strong><sub>e</sub>, <strong>w</strong><sub>r</sub>, &#x03BB;, t )
	 * </pre>
	 * 
	 * where
	 * 
	 * <pre>
	 * f<sub>r</sub> := radiant energy from <strong>w</strong><sub>r</sub> that's reflected along <strong>w</strong><sub>e</sub>
	 * <strong>x</strong> := point of reflection on surface
	 * <strong>w</strong><sub>e</sub> := "eye" vector, from point toward the eye
	 * <strong>w</strong><sub>r</sub> := "reflected" vector, from point outbound
	 * &#x03BB; := Spectrum denoting the specific wavelength we want to sample
	 * t := specific time we want to sample
	 * </pre>
	 * 
	 * All of these quantities are either given as parameters directly, or else
	 * derived from the specified {@link Interaction}.
	 * </p>
	 * 
	 * @param interaction
	 *            description of the surface-interaction
	 * @param w_r
	 *            reflection vector
	 * @param lambda
	 *            wavelength(s) to sample (or <code>null</code> if no wavelength
	 *            in particular)
	 * @param t
	 *            time at moment of sample
	 * @return the radiance of the given Spectrum reflected back along the
	 *         eye-vector
	 */
	public abstract Spectrum getReflectableRadiance(Interaction interaction, Vector w_r, Spectrum lambda, double t);

	/**
	 * Sample the radiant energy emitted from the given point.
	 * <p>
	 * Specifically, calculates:
	 * 
	 * <pre>
	 * f<sub>e</sub> ( <strong>x</strong>, <strong>w</strong><sub>e</sub>, &#x03BB;, t )
	 * </pre>
	 * 
	 * where
	 * 
	 * <pre>
	 * f<sub>r</sub> := radiant energy from <strong>w</strong><sub>r</sub> that's reflected along <strong>w</strong><sub>e</sub>
	 * <strong>x</strong> := point of reflection on surface
	 * <strong>w</strong><sub>e</sub> := "eye" vector, from point toward the eye
	 * &#x03BB; := Spectrum denoting the specific wavelength we want to sample
	 * t := specific time we want to sample
	 * </pre>
	 * 
	 * All of these quantities are either given as parameters directly, or else
	 * derived from the specified {@link Interaction}.
	 * </p>
	 * </p>
	 * 
	 * @param interaction
	 *            description of the surface-interaction
	 * @param lambda
	 *            wavelength(s) to sample (or <code>null</code> if no wavelength
	 *            in particular)
	 * @param t
	 *            time at moment of sample
	 * @return the radiance of the given Spectrum emitted along the eye-vector
	 */
	public abstract Spectrum getEmissiveRadiance(Interaction interaction, Spectrum lambda, double t);

	/**
	 * Indicates that the given surface is capable of emitting radiance itself,
	 * and should be considered as a light-source.
	 * 
	 * @return <code>true</code> if the given BDSF can possibly emit radiance
	 */
	public abstract boolean hasEmissiveRadiance();

	/**
	 * Given an intersection point <code>x</code>, an eye-vector
	 * <code>w_e</code>, and a surface-normal <code>n</code>, create a
	 * reflection vector within this BDSF's bounds. Implementations may opt to
	 * use importance sampling when selecting these samples.
	 * 
	 * @param x
	 *            point of surface-interaction
	 * @param w_e
	 *            vector from the surface-point to the eye
	 * @param n
	 *            the surface-normal at the point
	 * @param sample
	 *            the {@link Sample} currently being processed
	 * @return a reflection vector
	 */
	public abstract Vector sampleReflectionVector(Point x, Vector w_e, Normal n, Sample sample);

	/**
	 * Given an intersection point <code>x</code>, an eye-vector
	 * <code>w_e</code>, a surface-normal <code>n</code>, and a
	 * reflection-vector <code>w_r</code>, compute the Probability Distribution
	 * Function value that this reflection-vector should have been chosen.
	 * 
	 * @param x
	 *            point of surface-interaction
	 * @param w_e
	 *            vector from the surface-point to the eye
	 * @param w_r
	 *            vector reflected from the surface-point
	 * @param n
	 *            the surface-normal at the point
	 * @return the probability that the given reflection-vector lies within this
	 *         BDSF
	 */
	public abstract double reflectionPDF(Point x, Vector w_e, Vector w_r, Normal n);

	/**
	 * Calculate a {@link FresnelResult}, giving the relative fractions of
	 * reflectance and transmittance that go into contributing to the total
	 * incident light.
	 * <p>
	 * <strong>Note</strong> that this method merely calls the
	 * {@link FresnelResult} constructor.
	 * </p>
	 * 
	 * @param w_e
	 *            vector from the surface toward the eye
	 * @param n
	 *            surface-normal at the point of interaction
	 * @param leavingIndexOfRefraction
	 *            index-of-refraction of the material light is coming from
	 *            (along <strong>w</strong><sub>e</sub> toward the surface)
	 * @param enteringIndexOfRefraction
	 *            index-of-refraction of the material light is entering (along
	 *            <strong>w</strong><sub>e</sub> toward the surface)
	 * @return a FresnelResult
	 */
	public static FresnelResult calculateFresnel(Vector w_e, Normal n, double leavingIndexOfRefraction,
			double enteringIndexOfRefraction) {

		return new FresnelResult(w_e, n, leavingIndexOfRefraction, enteringIndexOfRefraction);
	}

	/**
	 * @return this BDSF's index-of-refraction at the given point
	 */
	public double getIndexOfRefraction() {

		return indexOfRefraction;
	}

	/**
	 * Calculates and stores Schlick's approximation for reflection and
	 * transmittance.
	 * 
	 * @author snowjak88
	 */
	public static class FresnelResult {

		private final double	reflectance, transmittance;
		private final Vector	reflectedDirection, transmittedDirection;

		public FresnelResult(Vector w_e, Normal n, double leavingIndexOfRefraction, double enteringIndexOfRefraction) {

			//
			// Source:
			// http://graphics.stanford.edu/courses/cs148-10-summer/docs/2006--degreve--reflection_refraction.pdf
			//
			this.reflectance = calculateReflectance(w_e, n, leavingIndexOfRefraction, enteringIndexOfRefraction);
			this.transmittance = 1d - this.reflectance;

			this.reflectedDirection = getW_r(w_e, n);
			this.transmittedDirection = getW_t(w_e, n, leavingIndexOfRefraction, enteringIndexOfRefraction);
		}

		/**
		 * For a given eye-vector and surface-normal, compute the resulting
		 * reflected vector (assuming perfect specular reflection).
		 * 
		 * @param w_e
		 * @param n
		 * @return
		 */
		private Vector getW_r(Vector w_e, Normal n) {

			final Vector nv = n.asVector().normalize();
			final Vector i = w_e.normalize();

			final double cos_i = nv.dotProduct(i);
			return i.negate().add(nv.multiply(2d * cos_i)).normalize();
		}

		/**
		 * For a given eye-vector, surface-normal, and pair of
		 * indices-of-refraction, compute the resulting transmitted vector (or
		 * <code>null</code> if this is a case of Total Internal Reflection).
		 * 
		 * @param w_e
		 * @param normal
		 * @param n1
		 * @param n2
		 * @return
		 */
		private Vector getW_t(Vector w_e, Normal normal, double n1, double n2) {

			final Vector normalv = normal.asVector().normalize();
			final Vector i = w_e.normalize();

			final double n = n1 / n2;
			final double cos_i = normalv.dotProduct(i);
			final double sin2_t = n * n * ( 1d - cos_i * cos_i );

			if (sin2_t > 1d)
				// This is a case of Total Internal Reflection -- so nothing is
				// transmitted!
				return null;

			final double cos_t = sqrt(1d - sin2_t);
			return i.negate().multiply(n).add(normalv.multiply(n * cos_i - cos_t)).normalize();
		}

		private double calculateReflectance(Vector w_e, Normal normal, double n1, double n2) {

			final Vector normalv = normal.asVector().normalize();
			final Vector i = w_e.normalize();

			/*
			 * final double r0 = pow(( n1 - n2 ) / ( n1 + n2 ), 2); final double
			 * cos_x;
			 * 
			 * if (n1 > n2) { final double n = n1 / n2; final double cos_i =
			 * normalv.dotProduct(i); final double sin2_t = n * n * ( 1d - cos_i
			 * * cos_i ); if (sin2_t > 1d) // A case of Total Internal
			 * Reflection return 1d;
			 * 
			 * cos_x = sqrt(1d - sin2_t);
			 * 
			 * } else {
			 * 
			 * cos_x = normalv.dotProduct(i); }
			 * 
			 * final double x = 1d - cos_x;
			 * 
			 * return r0 + ( 1d - r0 ) * pow(x, 5);
			 */

			final double n = n1 / n2;
			final double cos_i = normalv.dotProduct(i);
			final double sin2_t = n * n * ( 1d - cos_i * cos_i );

			if (sin2_t > 1d)
				// This is a case of Total Internal Reflection.
				return 1d;

			final double cos_t = sqrt(1d - sin2_t);
			final double r0_rth = ( n1 * cos_i - n2 * cos_t ) / ( n1 * cos_i + n2 * cos_t );
			final double r_par = ( n2 * cos_i - n1 * cos_t ) / ( n2 * cos_i + n1 * cos_t );
			return min(max(( ( r0_rth * r0_rth + r_par * r_par ) / 2d ), 0d), 1d);

		}

		public Vector getReflectedDirection() {

			return reflectedDirection;
		}

		public Vector getTransmittedDirection() {

			return transmittedDirection;
		}

		public double getReflectance() {

			return reflectance;
		}

		public double getTransmittance() {

			return transmittance;
		}

		public boolean isTotalInternalReflection() {

			return ( transmittedDirection == null );
		}
	}
}
