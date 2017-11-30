package org.snowjak.rays3.bxdf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Supplier;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.ConstantTexture;
import org.snowjak.rays3.texture.Texture;

/**
 * Defines a "perfect mirror" {@link BSDF}.
 * 
 * @author snowjak88
 */
public class PerfectSpecularBRDF extends BSDF {

	private final Texture	texture;
	private final Texture	emissive;

	/**
	 * Create a new PerfectSpecularBRDF with no tinting applied to reflected
	 * light.
	 */
	public PerfectSpecularBRDF() {
		this(new ConstantTexture(RGBSpectrum.WHITE));
	}

	/**
	 * Create a new PerfectSpecularBRDF, specifying a {@link Texture} for
	 * reflection-tinting.
	 * 
	 * @param texture
	 *            {@link Texture} defining this mirror's "reflectivity-fraction"
	 *            at various wavelengths
	 */
	public PerfectSpecularBRDF(Texture texture) {
		this(texture, null);
	}

	/**
	 * Create a new PerfectSpecularBRDF, specifying {@link Texture}s for both
	 * reflection-tinting and outright emission.
	 * 
	 * @param texture
	 *            {@link Texture} defining this mirror's "reflectivity-fraction"
	 *            at various wavelengths
	 * @param emissive
	 *            <code>null</code> if no emission should take place
	 */
	public PerfectSpecularBRDF(Texture texture, Texture emissive) {
		super(new HashSet<>(Arrays.asList(Property.REFLECT_SPECULAR)));

		this.texture = texture;
		this.emissive = emissive;
	}

	@Override
	public Spectrum sampleL_e(Interaction interaction, Sample sample, Supplier<Point2D> sampleSupplier) {

		if (emissive == null)
			return RGBSpectrum.BLACK;

		if (sample.getWavelength() == null)
			return emissive.evaluate(interaction);

		return emissive.evaluate(interaction).multiply(sample.getWavelength());
	}

	@Override
	public Vector sampleW_i(Interaction interaction, Sample sample, Supplier<Point2D> sampleSupplier) {

		//
		// A perfect mirror reflects only perfectly. No other directions are
		// possible.
		//
		return BSDF.getPerfectSpecularReflectionVector(interaction.getW_e(), interaction.getNormal());
	}

	@Override
	public double pdfW_i(Interaction interaction, Sample sample, Supplier<Point2D> sampleSupplier, Vector w_i) {

		//
		// A perfect mirror will only every reflect perfectly. Therefore, all
		// directions that are not perfect reflections are of probability 0.
		//
		return ( isPerfectReflection(interaction, sample, sampleSupplier, w_i) ) ? 1d : 0d;
	}

	@Override
	public Spectrum f_r(Interaction interaction, Sample sample, Supplier<Point2D> sampleSupplier, Vector w_i) {

		//
		//
		if (isPerfectReflection(interaction, sample, sampleSupplier, w_i))
			return texture.evaluate(interaction);
		else
			return RGBSpectrum.BLACK;
	}

	private boolean isPerfectReflection(Interaction interaction, Sample sample, Supplier<Point2D> sampleSupplier,
			Vector w_i) {

		final Vector perfectReflection = sampleW_i(interaction, sample, sampleSupplier).normalize();
		final Vector givenReflection = w_i.normalize();

		return Global.isNear(perfectReflection.getX(), givenReflection.getX())
				&& Global.isNear(perfectReflection.getY(), givenReflection.getY())
				&& Global.isNear(perfectReflection.getZ(), givenReflection.getZ());
	}

	@Override
	public boolean isEmissive() {

		return false;
	}

	@Override
	public Spectrum getTotalEmissivePower() {

		return RGBSpectrum.BLACK;
	}

}
