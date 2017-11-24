package org.snowjak.rays3.bxdf;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.Arrays;
import java.util.HashSet;

import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.Texture;

/**
 * Implements a Lambertian BDRF, modeling a perfectly-diffuse surface.
 * 
 * @author snowjak88
 */
public class LambertianBRDF extends BSDF {

	private final Texture	texture;
	private final Texture	emissive;

	/**
	 * Construct a new Lambertian-style BDRF which allows transmittance.
	 * 
	 * @param texture
	 * @param indexOfRefraction
	 */
	public LambertianBRDF(Texture texture, double indexOfRefraction) {
		this(texture, null, indexOfRefraction);
	}

	/**
	 * Construct a new Lambertian-style BDRF.
	 * 
	 * @param texture
	 * @param emissive
	 *            <code>null</code> if this BDRF does not emit any radiance
	 * @param indexOfRefraction
	 */
	public LambertianBRDF(Texture texture, Texture emissive, double indexOfRefraction) {
		super(new HashSet<>(Arrays.asList(Property.REFLECT_DIFFUSE)), indexOfRefraction);

		this.texture = texture;
		this.emissive = emissive;
	}

	@Override
	public Spectrum sampleL_e(Interaction interaction, Sample sample) {

		if (this.emissive == null)
			return RGBSpectrum.BLACK;

		if (sample.getWavelength() == null)
			return this.emissive.evaluate(interaction);

		return this.emissive.evaluate(interaction).multiply(sample.getWavelength());
	}

	@Override
	public Vector sampleW_i(Interaction interaction, Sample sample) {

		//
		//
		// For a simple Lambertian BRDF, we can simply choose any direction in
		// the hemisphere centered around the surface normal.
		//
		final Point2D sampledPoint = sample.getAdditionalTwinSample("Lambert-W_o").get();

		final double sin2_theta = sampledPoint.getX(); // the uniform random
														 // number is equal to
														 // sin^2(theta)
		final double cos2_theta = 1d - sin2_theta; // cos^2(x) + sin^2(x) = 1
		final double sin_theta = sqrt(sin2_theta);
		final double cos_theta = sqrt(cos2_theta);

		final double orientation = sampledPoint.getY() * 2d * PI;
		//
		//
		//
		final double x = sin_theta * cos(orientation);
		final double y = cos_theta;
		final double z = sin_theta * sin(orientation);

		//
		//
		// Construct a coordinate system centered around the surface-normal.
		final Vector j = interaction.getNormal().asVector().normalize();
		final Vector i = j.orthogonal();
		final Vector k = i.crossProduct(j);
		//
		//
		// Convert the Cartesian coordinates to a Vector in the constructed
		// coordinate system.
		return i.multiply(x).add(j.multiply(y)).add(k.multiply(z)).normalize();
	}

	@Override
	public Spectrum f_r(Interaction interaction, Sample sample, Vector w_o) {

		//
		//
		final double cos_i = interaction.getNormal().asVector().normalize().dotProduct(w_o.normalize());
		if (cos_i < 0d)
			return RGBSpectrum.BLACK;

		return texture.evaluate(interaction).multiply(cos_i);
	}

	public Texture getTexture() {

		return texture;
	}

	public Texture getEmissive() {

		return emissive;
	}

}
