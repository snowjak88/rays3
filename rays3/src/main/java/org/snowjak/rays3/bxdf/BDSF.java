package org.snowjak.rays3.bxdf;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Represents a Bi-Directional Scattering Function.
 * 
 * @author snowjak88
 */
public abstract class BDSF {

	/**
	 * Determine the fraction of radiance reflected from the given point.
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
	 * f<sub>r</sub> := fraction of incident light from <strong>w</strong><sub>r</sub> that's reflected along <strong>w</strong><sub>e</sub>
	 * <strong>x</strong> := point of reflection on surface
	 * <strong>w</strong><sub>e</sub> := "eye" vector, from point toward the eye
	 * <strong>w</strong><sub>r</sub> := "reflected" vector, from point outbound
	 * &#x03BB; := Spectrum denoting the specific wavelength we want to sample
	 * t := specific time we want to sample
	 * </pre>
	 * </p>
	 * 
	 * @param x
	 *            point of reflection on the shape
	 * @param w_e
	 *            vector from <strong>x</strong> toward the eye
	 * @param w_r
	 *            vector from <strong>x</strong>, reflected away
	 * @param lambda
	 *            wavelength at moment of sample
	 * @param t
	 *            time at moment of sample
	 * @return a fraction of the radiance of the given Spectrum reflected back
	 *         along the eye-vector
	 */
	public abstract double getReflectedRadiance(Point x, Vector w_e, Vector w_r, Spectrum lambda, double t);

	/**
	 * Determine the vector of reflection from the given point.
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
	public static Vector getReflectedVector(Point x, Vector w_e, Normal n) {

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

		double nv_factor = r * c - FastMath.sqrt(1d - FastMath.pow(r, 2) * ( 1d - FastMath.pow(c, 2) ));
		return ( l.multiply(r) ).add(nv.multiply(nv_factor)).normalize();
	}
}