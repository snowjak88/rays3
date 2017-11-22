package org.snowjak.rays3.bxdf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays3.Global;
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
	 * Construct a new Lambertian-style BDRF which allows transmittance.
	 * 
	 * @param texture
	 * @param indexOfRefraction
	 */
	public LambertianBDRF(Texture texture, double indexOfRefraction) {
		this(texture, null, indexOfRefraction, true);
	}

	/**
	 * Construct a new Lambertian-style BDRF.
	 * 
	 * @param texture
	 * @param indexOfRefraction
	 * @param allowsTransmittance
	 */
	public LambertianBDRF(Texture texture, double indexOfRefraction, boolean allowsTransmittance) {
		this(texture, null, indexOfRefraction, allowsTransmittance);
	}

	/**
	 * Construct a new Lambertian-style BDRF.
	 * 
	 * @param texture
	 * @param emissive
	 *            <code>null</code> if this BDRF does not emit any radiance
	 * @param indexOfRefraction
	 * @param allowsTransmittance
	 */
	public LambertianBDRF(Texture texture, Texture emissive, double indexOfRefraction, boolean allowsTransmittance) {
		super(new HashSet<>(allowsTransmittance
				? Arrays.asList(Property.REFLECT_DIFFUSE, Property.REFLECT_SPECULAR, Property.TRANSMIT)
				: Arrays.asList(Property.REFLECT_DIFFUSE, Property.REFLECT_SPECULAR)), indexOfRefraction);

		this.texture = texture;
		this.emissive = emissive;
	}

	@Override
	public Spectrum getReflectiveColoration(Interaction interaction, Spectrum lambda, double t) {

		Spectrum irradiance = texture.evaluate(interaction);

		// Ensure that we're selecting only the desired
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
	public Vector sampleReflectionVector(Point x, Vector w_e, Normal n, Sample sample, ReflectType reflectType) {

		switch (reflectType) {
		case DIFFUSE:
			return sampleDiffuseReflectionVector(x, w_e, n, sample);
		case SPECULAR:
			return BDSF.getPerfectSpecularReflectionVector(w_e, n);
		default:
			return null;
		}
	}

	private Vector sampleDiffuseReflectionVector(Point x, Vector w_e, Normal n, Sample sample) {

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
			Supplier<Double> reflectionISampler = sample.getAdditionalSingleSampleSupplier("Lambert-diffuse-reflect");
			Supplier<Point2D> reflectionJKSampler = sample.getAdditionalTwinSample("Lambert-diffuse-reflect");

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
	public double reflectionPDF(Point x, Vector w_e, Vector w_r, Normal n, ReflectType reflectType) {

		switch (reflectType) {
		case DIFFUSE:
			return reflectionPDF_diffuse(x, w_e, w_r, n);
		case SPECULAR:
			return reflectionPDF_specular(x, w_e, w_r, n);
		default:
			return 0d;
		}
	}

	private double reflectionPDF_diffuse(Point x, Vector w_e, Vector w_r, Normal n) {

		//
		// For a Lambertian (perfectly-diffuse) BDRF, the probability of a
		// given diffuse reflection-vector being chosen is directly proportional
		// to the cosine of the angle between it and the surface-normal.
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

	private double reflectionPDF_specular(Point x, Vector w_e, Vector w_r, Normal n) {

		//
		// For a Lambertian BDRF, the probability of a given specular
		// reflection-vector being selected is 1 if that reflection-vector is
		// perfectly specular, and 0 otherwise.
		//
		final Vector perfectSpecular = BDSF.getPerfectSpecularReflectionVector(w_e, n).normalize();
		final Vector w_rn = w_r.normalize();

		if (Global.isNear(w_rn.getX(), perfectSpecular.getX()) && Global.isNear(w_rn.getY(), perfectSpecular.getY())
				&& Global.isNear(w_rn.getZ(), perfectSpecular.getZ()))
			return 1d;
		else
			return 0d;
	}

	public Texture getTexture() {

		return texture;
	}

	public Texture getEmissive() {

		return emissive;
	}

}
