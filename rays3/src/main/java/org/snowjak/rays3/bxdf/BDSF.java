package org.snowjak.rays3.bxdf;

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
	 * @return this BDSF's index-of-refraction at the given point
	 */
	public double getIndexOfRefraction() {

		return indexOfRefraction;
	}
}
