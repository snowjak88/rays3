package org.snowjak.rays3.bxdf;

import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.Texture;

/**
 * Implements a Lambertian BDRF, modeling a perfectly-diffuse surface.
 * 
 * @author snowjak88
 */
public class LambertianBDRF extends BDSF {

	private final Texture	texture;
	private final Texture	emissive;

	public LambertianBDRF(Texture texture, Texture emissive, double indexOfRefraction) {
		super(indexOfRefraction);

		this.texture = texture;
		this.emissive = emissive;
	}

	@Override
	public Spectrum getReflectedRadiance(Interaction interaction, Vector w_r, Spectrum lambda, double t) {

		Spectrum irradiance = texture.evaluate(interaction);

		// Reflected energy is proportional to the cosine of the angle between
		// the two vectors (the eye and the reflected vectors).
		Vector w_e = interaction.getInteractingRay().getDirection().negate();

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

	@Override
	public Vector sampleReflectionVector(Point x, Vector w_e, Normal n, Sample sample) {

		//
		// For a perfectly-diffuse BDRF, reflection vectors may be selected
		// from the hemisphere centered on the surface-point, with its zenith at
		// the surface-normal.
		//

		//
		// Let's construct a coordinate system about n -- call it N, P, Q.
		Vector p = n.asVector().orthogonal();
		Vector q = n.asVector().crossProduct(p);

		//
		// Select 3 factors, one for each of the new basis-vectors.
		// i --> N, j --> P, k --> Q
		//
		// Note that i is not scaled onto the interval [-1,1]. We want to select
		// from the hemisphere pointing along N -- and so we only want to go
		// along N.
		//
		double i = sample.getAdditionalSingleSample();
		double j = ( 2d * sample.getAdditionalSingleSample() ) - 1d;
		double k = ( 2d * sample.getAdditionalSingleSample() ) - 1d;
		//
		// Assemble the new reflection vector.
		//
		// @formatter:off
		Vector reflection = n.asVector().multiply(i)
							.add(p.multiply(j))
							.add(q.multiply(k))
							.normalize();
		// @formatter:on

		return reflection;
	}

	@Override
	public double reflectionPDF(Point x, Vector w_e, Vector w_r, Normal n) {

		//
		// For a Lambertian (perfectly-diffuse) BDRF, the probability of a
		// reflection-vector being chosen
		// is directly proportional to the cosine of the angle between it and
		// the surface-normal.
		//
		// Of course, if the dot-product is negative, the probability of
		// this reflection-vector arising is 0.
		//
		double dotProduct = w_r.normalize().dotProduct(n.asVector().normalize());
		if (dotProduct < 0d)
			return 0;
		//
		// We cannot simply return the dot-product of w_r and n, however.
		// Remember that the integral of the PDF across its whole range must
		// equal 1.
		// We therefore need to normalize the particular dot-product by dividing
		// it by that integral.
		//
		// And according to Wolfram Alpha, the integral of cos(x),
		// for x = -pi/2..+pi/2,
		// is 2.
		//
		return dotProduct / 2d;
	}

	public Texture getTexture() {

		return texture;
	}

	public Texture getEmissive() {

		return emissive;
	}

}
