package org.snowjak.rays3.bxdf;

import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Represents a Bi-Directional Reflectance Function.
 * 
 * @author snowjak88
 */
public interface BDRF {

	/**
	 * Determine the radiance reflected from the given point.
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
	 *            point of reflection on the shape, in world coordinates
	 * @param w_e
	 *            vector from <strong>x</strong> toward the eye
	 * @param w_r
	 *            vector from <strong>x</strong>, reflected away
	 * @param lambda
	 *            wavelength at moment of sample
	 * @param t
	 *            time at moment of sample
	 * @return
	 */
	public double getRadiance(Point x, Vector w_e, Vector w_r, Spectrum lambda, double t);
}
