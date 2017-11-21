package org.snowjak.rays3.bxdf;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
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
public class LambertianBDRF extends BDSF {

	private final Texture	texture;
	private final Texture	emissive;

	/**
	 * Construct a new Lambertian-style BDRF.
	 * 
	 * @param texture
	 * @param indexOfRefraction
	 */
	public LambertianBDRF(Texture texture, double indexOfRefraction) {
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
	public LambertianBDRF(Texture texture, Texture emissive, double indexOfRefraction) {
		super(indexOfRefraction);

		this.texture = texture;
		this.emissive = emissive;
	}

	@Override
	public Spectrum getReflectableRadiance(Interaction interaction, Vector w_r, Spectrum lambda, double t) {

		Spectrum irradiance = texture.evaluate(interaction);

		// For a simple Lambertian surface, we don't worry about the angle of
		// interaction or any such thing. We simply evaluate the surface-color
		// and return it.

		// Finally, ensure that we're selecting only the desired
		// energy-wavelengths (if that parameter is supplied).
		if (lambda != null)
			irradiance = irradiance.multiply(lambda);

		return irradiance;
	}

	@Override
	public Spectrum getEmissiveRadiance(Interaction interaction, Spectrum lambda, double t) {

		Spectrum radiance;

		if (emissive == null)
			radiance = new RGBSpectrum();
		else
			radiance = emissive.evaluate(interaction);

		if (lambda != null)
			radiance = radiance.multiply(lambda);

		return radiance;
	}

	@Override
	public boolean hasEmissiveRadiance() {

		return ( emissive != null );
	}

	@Override
	public Vector sampleReflectionVector(Point x, Vector w_e, Normal n, Sample sample) {

		//
		// For a perfectly-diffuse BDRF, reflection vectors may be selected
		// from the hemisphere centered on the surface-point, with its zenith at
		// the surface-normal.
		//

		//
		// Let's construct a coordinate system about n -- call it Nv, Pv, Qv.
		final Vector nv = n.asVector().normalize();
		final Vector pv = nv.orthogonal().normalize();
		final Vector qv = nv.crossProduct(pv).normalize();

		//
		// Now compile a distribution of candidate reflection-vectors, and
		// select one of them.
		//
		List<Pair<Vector, Double>> reflectionVectors = new LinkedList<>();
		for (int c = 0; c < sample.getSampler().getSamplesPerPixel(); c++) {

			//
			// Select 3 factors, one for each of the new basis-vectors.
			// i --> N, j --> P, k --> Q
			//
			// Note that i is not scaled onto the interval [-1,1]. We want to
			// select
			// from the hemisphere pointing along N -- and so we only want to go
			// along N.
			//
			Supplier<Double> reflectionISampler = sample.getAdditionalSingleSampleSupplier("Lambert-reflect");
			Supplier<Point2D> reflectionJKSampler = sample.getAdditionalTwinSample("Lambert-reflect");

			final double i = reflectionISampler.get();
			final Point2D jk = reflectionJKSampler.get();

			double j = ( 2d * jk.getX() ) - 1d;
			double k = ( 2d * jk.getY() ) - 1d;
			//
			// Assemble the new reflection vector.
			//
			// @formatter:off
			Vector reflection = nv.multiply(i)
								.add(pv.multiply(j))
								.add(qv.multiply(k))
								.normalize();
			// @formatter:on
			//
			reflectionVectors.add(new Pair<>(reflection, reflection.dotProduct(nv)));
		}
		//
		//
		EnumeratedDistribution<Vector> reflectionDistribution = new EnumeratedDistribution<>(reflectionVectors);

		return reflectionDistribution.sample();
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
