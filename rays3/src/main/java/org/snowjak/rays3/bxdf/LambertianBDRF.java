package org.snowjak.rays3.bxdf;

import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.Texture;

/**
 * Implements a Lambertian BDRF, modeling a perfectly-diffuse surface.
 * 
 * @author snowjak88
 */
public class LambertianBDRF extends BDSF {

	private Texture	texture;
	private Texture	emissive;

	public LambertianBDRF(Texture texture, Texture emissive) {
		super();

		this.texture = texture;
		this.emissive = emissive;
	}

	@Override
	public Spectrum getReflectedRadiance(Interaction interaction, Spectrum lambda, double t) {

		Spectrum irradiance = texture.evaluate(interaction);

		// Reflected energy is proportional to the cosine of the angle between
		// the two vectors (the eye and the reflected vectors).
		Vector w_e = interaction.getInteractingRay().getDirection().negate();
		Vector w_r = getReflectedVector(interaction.getPoint(), w_e, interaction.getNormal());

		double cosTheta = w_e.dotProduct(w_r);
		irradiance = irradiance.multiply(cosTheta);

		// Finally, ensure that we're selecting only the desired
		// energy-wavelengths (if that parameter is supplied).
		if (lambda != null)
			irradiance = irradiance.multiply(lambda);

		return irradiance;
	}

	@Override
	public Spectrum getEmissiveRadiance(Interaction interaction, Spectrum lambda, double t) {

		Spectrum radiance = emissive.evaluate(interaction);

		if (lambda != null)
			radiance = radiance.multiply(lambda);

		return radiance;
	}

}
