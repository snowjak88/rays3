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

/**
 * Represents a Bi-Directional Scattering Function.
 * 
 * @author snowjak88
 */
public abstract class BSDF {

	private final double		indexOfRefraction;
	private final Set<Property>	properties;

	/**
	 * Create a new {@link BSDF} with the given list of {@link Property} and the
	 * default index-of-refraction of 1.0
	 * 
	 * @param properties
	 */
	public BSDF(Set<Property> properties) {
		this(properties, 1.0);
	}

	/**
	 * Create a new {@link BSDF} with the given list of {@link Property} and
	 * index-of-refraction.
	 * 
	 * @param properties
	 * @param indexOfRefraction
	 */
	public BSDF(Set<Property> properties, double indexOfRefraction) {

		this.properties = new HashSet<>(properties);
		this.indexOfRefraction = indexOfRefraction;
	}

	/**
	 * @return <code>true</code> if this {@link BSDF} is configured with the
	 *         given {@link Property}
	 */
	public boolean hasProperty(Property property) {

		return this.properties.contains(property);
	}

	/**
	 * Sample the radiant energy emitted from the given interaction.
	 * 
	 * @param interaction
	 * @param sample
	 * @return
	 */
	public abstract Spectrum sampleL_e(Interaction interaction, Sample sample);

	/**
	 * Sample an incident vector from the given interaction.
	 * 
	 * @param interaction
	 * @param sample
	 * @return
	 */
	public abstract Vector sampleW_i(Interaction interaction, Sample sample);

	/**
	 * Given an inbound vector <code>w<sub>i</sub></code>, what is the
	 * probability that this BSDF would have chosen that vector?
	 * 
	 * @param interaction
	 * @param sample
	 * @param w_i
	 * @return
	 */
	public abstract double pdfW_i(Interaction interaction, Sample sample, Vector w_i);

	/**
	 * Compute the fraction of energy (for each wavelength) that's reflected
	 * from the given vector <code>w<sub>o</sub></code>.
	 * 
	 * @param interaction
	 * @param sample
	 * @param w_o
	 * @return
	 */
	public abstract Spectrum f_r(Interaction interaction, Sample sample, Vector w_o);

	/**
	 * Compute the cosine term for each BSDF interaction -- i.e., the
	 * dot-product of the outbound direction <code>w<sub>o</sub></code> and the
	 * surface-normal <code>n</code>
	 * 
	 * @param interaction
	 * @param w_o
	 * @return
	 */
	public double cos_i(Interaction interaction, Vector w_o) {

		return interaction.getNormal().asVector().normalize().dotProduct(w_o.normalize());
	}

	/**
	 * @return this BSDF's index-of-refraction at the given point
	 */
	public double getIndexOfRefraction() {

		return indexOfRefraction;
	}

	/**
	 * Enumeration describing the various properties a {@link BSDF} instance can
	 * have (and which will affect how the active {@link AbstractIntegrator}
	 * will treat it).
	 * 
	 * @author snowjak88
	 */
	public enum Property {
		/**
		 * Indicates that the given {@link BSDF} should be sampled for specular
		 * reflection.
		 */
		REFLECT_SPECULAR,
		/**
		 * Indicates that the given {@link BSDF} should be sampled for diffuse
		 * reflection.
		 */
		REFLECT_DIFFUSE,
		/**
		 * Indicates that the given {@link BSDF} should be sampled for
		 * transmittance.
		 */
		TRANSMIT,
		/**
		 * Indicates that the given {@link BSDF} behaves like a dialetric
		 * material -- i.e., specularly-reflected light is tinted
		 */
		DIALECTRIC,
		/**
		 * Indicates that the given {@link BSDF} should be sampled for glossy
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
