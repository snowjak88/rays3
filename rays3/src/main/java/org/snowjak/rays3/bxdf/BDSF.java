package org.snowjak.rays3.bxdf;

import java.util.HashSet;
import java.util.Set;

import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.integrator.AbstractIntegrator;
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

	private final double		indexOfRefraction;
	private final Set<Property>	properties;

	/**
	 * Create a new {@link BDSF} with the given list of {@link Property} and the
	 * default index-of-refraction of 1.0
	 * 
	 * @param properties
	 */
	public BDSF(Set<Property> properties) {
		this(properties, 1.0);
	}

	/**
	 * Create a new {@link BDSF} with the given list of {@link Property} and
	 * index-of-refraction.
	 * 
	 * @param properties
	 * @param indexOfRefraction
	 */
	public BDSF(Set<Property> properties, double indexOfRefraction) {

		this.properties = new HashSet<>(properties);
		this.indexOfRefraction = indexOfRefraction;
	}

	/**
	 * @return <code>true</code> if this {@link BDSF} is configured with the
	 *         given {@link Property}
	 */
	public boolean hasProperty(Property property) {

		return this.properties.contains(property);
	}

	/**
	 * Sample the coloration available to potentially tint radiance reflected
	 * from the given {@link Interaction}. Usually, this is backed by a static
	 * {@link Texture}, but more complex implementations are certainly possible.
	 * 
	 * @param interaction
	 *            description of the surface-interaction
	 * @param lambda
	 *            wavelength(s) to sample (or <code>null</code> if no wavelength
	 *            in particular)
	 * @param t
	 *            time at moment of sample
	 * @return a {@link Spectrum} representing the coloration potentially
	 *         affecting reflected radiance from the surface
	 */
	public abstract Spectrum getReflectiveColoration(Interaction interaction, Spectrum lambda, double t);

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
	 * @param reflectType
	 *            the type of reflection being sampled for
	 * @return a reflection vector, or <code>null</code> if the given
	 *         ReflectType is inappropriate
	 */
	public abstract Vector sampleReflectionVector(Point x, Vector w_e, Normal n, Sample sample,
			ReflectType reflectType);

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
	public abstract double reflectionPDF(Point x, Vector w_e, Vector w_r, Normal n, ReflectType reflectType);

	/**
	 * @return this BDSF's index-of-refraction at the given point
	 */
	public double getIndexOfRefraction() {

		return indexOfRefraction;
	}

	/**
	 * Enumeration describing the various properties a {@link BDSF} instance can
	 * have (and which will affect how the active {@link AbstractIntegrator}
	 * will treat it).
	 * 
	 * @author snowjak88
	 */
	public enum Property {
		/**
		 * Indicates that the given {@link BDSF} should be sampled for specular
		 * reflection.
		 */
		REFLECT_SPECULAR,
		/**
		 * Indicates that the given {@link BDSF} should be sampled for diffuse
		 * reflection.
		 */
		REFLECT_DIFFUSE,
		/**
		 * Indicates that the given {@link BDSF} should be sampled for
		 * transmittance.
		 */
		TRANSMIT,
		/**
		 * Indicates that the given {@link BDSF} behaves like a dialetric
		 * material -- i.e., specularly-reflected light is tinted
		 */
		DIALECTRIC,
		/**
		 * Indicates that the given {@link BDSF} should be sampled for glossy
		 * reflection, when sampling for specular reflection
		 */
		GLOSSY
	}

	public enum ReflectType {
		/**
		 * Indicates specular reflection
		 */
		SPECULAR,
		/**
		 * Indicates diffuse reflection
		 */
		DIFFUSE
	}

	/**
	 * Given an surface-intersection point <code><strong>x</strong></code>, a
	 * Vector from <code>x</code> toward the eye-point
	 * <code><strong>w</strong><sub>e</sub></code>, and a surface-normal
	 * <code><strong>n</strong></code>, construct a Vector giving the direction
	 * of perfect specular reflection.
	 * 
	 * @param x
	 * @param w_e
	 * @param n
	 * @return
	 */
	public static Vector getPerfectSpecularReflectionVector(Point x, Vector w_e, Normal n) {

		return getPerfectSpecularReflectionVector(w_e, n);
	}

	/**
	 * Given an surface-intersection point <code><strong>x</strong></code>, a
	 * Vector from <code>x</code> toward the eye-point
	 * <code><strong>w</strong><sub>e</sub></code>, and a surface-normal
	 * <code><strong>n</strong></code>, construct a Vector giving the direction
	 * of perfect specular reflection.
	 * 
	 * @param w_e
	 * @param n
	 * @return
	 */
	public static Vector getPerfectSpecularReflectionVector(Vector w_e, Normal n) {

		final Vector i = w_e.normalize();
		final Vector nv = n.asVector().normalize();

		final double cos_i = nv.dotProduct(i);
		return i.negate().add(nv.multiply(2d * cos_i)).normalize();
	}
}
