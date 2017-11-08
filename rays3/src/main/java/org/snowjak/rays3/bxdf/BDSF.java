package org.snowjak.rays3.bxdf;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.Spectrum;

import static org.apache.commons.math3.util.FastMath.*;

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
	 * Sample the radiant energy reflectable from the given point.
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
	public abstract Spectrum getReflectedRadiance(Interaction interaction, Vector w_r, Spectrum lambda, double t);

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
	 * Determine the vector of perfect specular reflection from the given point.
	 * 
	 * @param x
	 *            point of intersection on the shape
	 * @param w_e
	 *            vector from <strong>x</strong> toward the eye
	 * @param n
	 *            surface normal at <strong>x</strong>
	 * @return the vector pointing along the path of light reflected from the
	 *         surface
	 */
	public static Vector getPerfectSpecularReflectionVector(Point x, Vector w_e, Normal n) {

		/*
		 * Vector nv = n.asVector().normalize(); w_e = w_e.normalize();
		 * 
		 * return nv.multiply(2d * ( w_e.dotProduct(nv) )).subtract(w_e);
		 */
		Vector nv = n.asVector().normalize();
		Vector l = w_e.normalize().negate();
		double c = nv.negate().dotProduct(l);

		return l.add(nv.multiply(2d).multiply(c));
	}

	/**
	 * Determine the vector of transmittance (i.e., refraction) through the
	 * given point using Snell's law.
	 * 
	 * @param x
	 *            point of intersection on the shape
	 * @param w_e
	 *            vector from <strong>x</strong> toward the eye
	 * @param n
	 *            surface normal at <strong>x</strong>
	 * @param leavingIndexOfRefraction
	 *            index-of-refraction of the material light is coming from
	 *            (along <strong>w</strong><sub>e</sub> toward
	 *            <strong>x</strong>)
	 * @param enteringIndexOfRefraction
	 *            index-of-refraction of the material light is entering (along
	 *            <strong>w</strong><sub>e</sub> toward <strong>x</strong>)
	 * @return the vector pointing along the path of light transmitted through
	 *         the surface
	 */
	public static Vector getTransmittedVector(Point x, Vector w_e, Normal n, double leavingIndexOfRefraction,
			double enteringIndexOfRefraction) {

		Vector nv = n.asVector().normalize();
		Vector l = w_e.normalize().negate();
		double r = leavingIndexOfRefraction / enteringIndexOfRefraction;
		double c = nv.negate().dotProduct(l);

		double nv_factor = r * c - sqrt(1d - FastMath.pow(r, 2) * ( 1d - pow(c, 2) ));
		return ( l.multiply(r) ).add(nv.multiply(nv_factor)).normalize();
	}

	/**
	 * Calculate a {@link FresnelResult}, giving the relative fractions of
	 * reflectance and transmittance that go into contributing to the total
	 * incident light.
	 * <p>
	 * <strong>Note</strong> that this method uses Schlick's approximation to
	 * Fresnel's equations, and so does not take the polarization of the
	 * incident light into account.
	 * </p>
	 * 
	 * @param x
	 *            point of interaction on the shape
	 * @param w_e
	 *            vector from <strong>x</strong> toward the eye
	 * @param n
	 *            surface-normal at <strong>x</strong>
	 * @param leavingIndexOfRefraction
	 *            index-of-refraction of the material light is coming from
	 *            (along <strong>w</strong><sub>e</sub> toward
	 *            <strong>x</strong>)
	 * @param enteringIndexOfRefraction
	 *            index-of-refraction of the material light is entering (along
	 *            <strong>w</strong><sub>e</sub> toward <strong>x</strong>)
	 * @return a FresnelResult
	 */
	public static FresnelResult calculateFresnel(Point x, Vector w_e, Normal n, double leavingIndexOfRefraction,
			double enteringIndexOfRefraction) {

		final double n1 = leavingIndexOfRefraction, n2 = enteringIndexOfRefraction;
		final double cos_theta_i = w_e.normalize().dotProduct(n.asVector().normalize());
		double sin2_theta_t = pow(n1 / n2, 2d) * ( 1d - pow(cos_theta_i, 2d) );

		double reflectance = 1d, transmittance = 0d;

		if (sin2_theta_t <= 1d) {
			//
			// This is NOT a case of Total Internal Reflection
			//
			final double cos_theta_t = sqrt(1d - sin2_theta_t);
			final double r_normal = pow(
					( n1 * cos_theta_i - n2 * cos_theta_t ) / ( n1 * cos_theta_i + n2 * cos_theta_t ), 2d);
			final double r_tangent = pow(
					( n2 * cos_theta_i - n1 * cos_theta_t ) / ( n2 * cos_theta_i + n1 * cos_theta_t ), 2d);

			reflectance = ( r_normal + r_tangent ) / 2d;
			transmittance = 1d - reflectance;
		} else {
			//
			// This is a case of Total Internal Reflection.
			// As such, reflectance = 1.0, transmittance = 0.0
			//
		}

		return new FresnelResult(reflectance, transmittance);
	}

	/**
	 * @return this BDSF's index-of-refraction at the given point
	 */
	public double getIndexOfRefraction() {

		return indexOfRefraction;
	}

	/**
	 * Simple holder-class for Fresnel computation results -- i.e., which
	 * fractions of the incident energy stem from:
	 * <ul>
	 * <li>reflectance</li>
	 * <li>transmittance</li>
	 * </ul>
	 * 
	 * @author snowjak88
	 */
	public static class FresnelResult {

		private double reflectance, transmittance;

		public FresnelResult(double reflectance, double transmittance) {

			this.reflectance = reflectance;
			this.transmittance = transmittance;
		}

		public double getReflectance() {

			return reflectance;
		}

		public double getTransmittance() {

			return transmittance;
		}
	}
}
