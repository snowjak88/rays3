package org.snowjak.rays3.bxdf;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Supplier;

import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.ConstantTexture;
import org.snowjak.rays3.texture.Texture;

/**
 * Implements a Lambertian BDRF, modeling a perfectly-diffuse surface.
 * 
 * @author snowjak88
 */
public class LambertianBRDF extends BSDF {

	private final Texture	texture;
	private final Texture	emissive;
	private final Spectrum	totalEmissivePower;

	/**
	 * Construct a new Lambertian-style BDRF.
	 * 
	 * @param texture
	 */
	public LambertianBRDF(Texture texture) {
		this(texture, null);
	}

	/**
	 * Construct a new Lambertian-style BDRF. Total emissive power is assumed to
	 * equal <code>emissive * 4 * pi</code>, or <code>0</code> if
	 * <code>emissive == null</code> .
	 * 
	 * @param texture
	 * @param emissive
	 *            <code>null</code> if this BDRF does not emit any radiance
	 */
	public LambertianBRDF(Texture texture, Spectrum emissive) {
		this(texture, ( emissive == null ) ? null : new ConstantTexture(emissive),
				( emissive == null ) ? RGBSpectrum.BLACK : emissive.multiply(4d * PI));
	}

	/**
	 * Construct a new Lambertian-style BDRF.
	 * 
	 * @param texture
	 * @param emissive
	 *            <code>null</code> if this BDRF does not emit any radiance
	 * @param totalEmissivePower
	 *            {@link Spectrum} giving total radiant emissions, over all
	 *            directions, or {@link RGBSpectrum#BLACK} if no emissions at
	 *            all
	 */
	public LambertianBRDF(Texture texture, Texture emissive, Spectrum totalEmissivePower) {
		super(new HashSet<>(Arrays.asList(Property.REFLECT_DIFFUSE)));

		this.texture = texture;
		this.emissive = emissive;
		this.totalEmissivePower = ( totalEmissivePower == null ) ? RGBSpectrum.BLACK : totalEmissivePower;
	}

	@Override
	public Spectrum sampleL_e(Interaction interaction, Sample sample, Supplier<Point2D> sampleSupplier) {

		if (this.emissive == null)
			return RGBSpectrum.BLACK;

		if (sample.getWavelength() == null)
			return this.emissive.evaluate(interaction);

		return this.emissive.evaluate(interaction).multiply(sample.getWavelength());
	}

	@Override
	public Vector sampleW_i(Interaction interaction, Sample sample, Supplier<Point2D> sampleSupplier) {

		//
		//
		// For a simple Lambertian BRDF, we can simply choose any direction in
		// the hemisphere centered around the surface normal.
		//
		final Point2D sphericalPoint = sampleSupplier.get();

		final double sin2_theta = sphericalPoint.getX();
		final double cos2_theta = 1d - sin2_theta;
		final double sin_theta = sqrt(sin2_theta);
		final double cos_theta = sqrt(cos2_theta);

		final double orientation = sphericalPoint.getY() * 2d * PI;
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
	public double pdfW_i(Interaction interaction, Sample sample, Supplier<Point2D> sampleSupplier, Vector w_i) {

		//
		// A Lambertian BRDF samples directions from anywhere on the hemisphere.
		// Therefore the probability of choosing any 1 direction is equal to
		//
		// 1 / (integral (0 -> 2pi) 1 dx == 1 / 2pi
		//
		return 1d / ( 2d * PI );
	}

	@Override
	public Spectrum f_r(Interaction interaction, Sample sample, Supplier<Point2D> sampleSupplier, Vector w_i) {

		//
		//
		return texture.evaluate(interaction);
	}

	public Texture getTexture() {

		return texture;
	}

	public Texture getEmissive() {

		return emissive;
	}

	@Override
	public Spectrum getTotalEmissivePower() {

		return totalEmissivePower;
	}

	@Override
	public boolean isEmissive() {

		return ( emissive != null );
	}

}
