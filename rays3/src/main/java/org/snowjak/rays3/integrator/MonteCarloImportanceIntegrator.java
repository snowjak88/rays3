package org.snowjak.rays3.integrator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.apache.commons.math3.util.FastMath.*;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.World;
import org.snowjak.rays3.bxdf.BSDF;
import org.snowjak.rays3.camera.Camera;
import org.snowjak.rays3.film.Film;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.geometry.shape.Primitive;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.sample.Sampler;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Performs Monte-Carlo integration using importance sampling.
 * 
 * @author snowjak88
 */
public class MonteCarloImportanceIntegrator extends AbstractIntegrator {

	private final int samplesPerInteraction;

	/**
	 * Construct a new {@link MonteCarloImportanceIntegrator}.
	 * 
	 * @param camera
	 * @param film
	 * @param sampler
	 * @param maxRayDepth
	 * @param samplesPerInteraction
	 */
	public MonteCarloImportanceIntegrator(Camera camera, Film film, Sampler sampler, int maxRayDepth,
			int samplesPerInteraction) {
		this(camera, film, Arrays.asList(sampler), maxRayDepth, samplesPerInteraction);
	}

	/**
	 * Construct a new {@link MonteCarloImportanceIntegrator}.
	 * 
	 * @param camera
	 * @param film
	 * @param samplers
	 * @param maxRayDepth
	 * @param samplesPerInteraction
	 */
	public MonteCarloImportanceIntegrator(Camera camera, Film film, Collection<Sampler> samplers, int maxRayDepth,
			int samplesPerInteraction) {

		super(camera, film, samplers, maxRayDepth);
		this.samplesPerInteraction = samplesPerInteraction;
	}

	@Override
	public Spectrum followRay(Ray ray, World world, Sample sample) {

		final Optional<Interaction> op_interaction = world.getClosestInteraction(ray);

		if (!op_interaction.isPresent())
			return RGBSpectrum.BLACK;

		final Interaction interaction = op_interaction.get();

		//
		//
		// Do we need to flip this interaction around?
		// If the surface normal is pointing "the wrong way", we should do so.
		final Interaction relativeInteraction;

		if (interaction.getNormal().asVector().normalize().dotProduct(interaction.getW_e()) < 0d)
			relativeInteraction = new Interaction(interaction, interaction.getNormal().negate());
		else
			relativeInteraction = interaction;

		//
		//
		//
		final Point point = relativeInteraction.getPoint();
		final BSDF bsdf = relativeInteraction.getBdsf();

		//
		//
		Spectrum totalW_i_direct = RGBSpectrum.BLACK;
		Spectrum totalW_i_indirect = RGBSpectrum.BLACK;

		double totalProb_direct = 0d;
		double totalProb_indirect = 0d;
		
		//
		//
		final int twinSampleGridPerInteraction = (int) ceil(sqrt(samplesPerInteraction));
		//
		//
		// First, sample all known emissives, to estimate direct illumination.
		for (Primitive p : world.getEmissives()) {

			for (int i = 0; i < samplesPerInteraction; i++) {

				//
				// Create an incident direction from this point toward the
				// emissive shape.
				//
				final Point emissiveSurfacePoint = p.sampleSurfacePoint(
						sample.getAdditionalTwinSample("sample-emissive-surface", twinSampleGridPerInteraction), point);
				final Vector toEmissiveSurface = new Vector(point, emissiveSurfacePoint);

				final double cos_i = bsdf.cos_i(relativeInteraction, toEmissiveSurface);
				if (cos_i <= 0d)
					continue;

				final Ray toEmissiveSurfaceRay = new Ray(point, toEmissiveSurface);
				final Optional<Interaction> op_emissiveInteraction = world.getClosestInteraction(toEmissiveSurfaceRay);
				if (!op_emissiveInteraction.isPresent())
					continue;

				final Interaction emissiveInteraction = op_emissiveInteraction.get();
				if (emissiveInteraction.getPrimitive() != p)
					continue;

				final double pdfW_i = bsdf.pdfW_i(relativeInteraction, sample,
						sample.getAdditionalTwinSample("sample-emissive-W_i", twinSampleGridPerInteraction), toEmissiveSurface)
						* ( p.getShape().computeSolidAngle(point) / ( 2d * PI ) );
				final double sampleProb = Global.isNear(pdfW_i, 0d) ? 0d : 1d / pdfW_i;

				final double emissiveDistance = emissiveInteraction.getInteractingRay().getCurrT();

				final Spectrum radianceFromEmissive = p
						.getBsdf()
							.sampleL_e(emissiveInteraction, sample,
									sample.getAdditionalTwinSample("sample-emissive-L_e", twinSampleGridPerInteraction))
							.multiply(1d / ( emissiveDistance * emissiveDistance ));

				totalProb_direct += sampleProb;
				totalW_i_direct = totalW_i_direct.add(radianceFromEmissive
						.multiply(bsdf.f_r(relativeInteraction, sample,
								sample.getAdditionalTwinSample("sample-emissive-f_r", twinSampleGridPerInteraction),
								toEmissiveSurface))
							.multiply(cos_i)
							.multiply(sampleProb));

			}

		}
		if (totalProb_direct == 0.0)
			totalW_i_direct = totalW_i_direct.multiply(0d);
		else
			totalW_i_direct = totalW_i_direct.multiply(1d / totalProb_direct);

		//
		//
		// Sample a number of rays and scale them according to their respective
		// probabilities.

		if (ray.getDepth() < getMaxRayDepth()) {

			for (int i = 0; i < samplesPerInteraction; i++) {
				//
				// Sample an incident direction from the BSDF and compute
				// its
				// PDF.
				final Vector sampledDirection = bsdf.sampleW_i(relativeInteraction, sample,
						sample.getAdditionalTwinSample("sample-indirect-W_i", twinSampleGridPerInteraction));
				final double pdfW_i = bsdf.pdfW_i(relativeInteraction, sample,
						sample.getAdditionalTwinSample("sample-indirect-W_i-prob", twinSampleGridPerInteraction),
						sampledDirection);
				final double sampledProb = Global.isNear(pdfW_i, 0d) ? 0d : 1d / pdfW_i;
				
				//
				// Perform Russian-roulette elimination on paths that will not contribute much to the total light estimate.
				//
				Spectrum indirectContribution =
									bsdf.f_r(relativeInteraction, sample, sample.getAdditionalTwinSample("sample-indirect-f_r", twinSampleGridPerInteraction), sampledDirection)
										.multiply(bsdf.cos_i(relativeInteraction, sampledDirection));
				final RGB indirectContrib_rgb = indirectContribution.toRGB();
				final double maxIndirectContribComponent = max(max(indirectContrib_rgb.getRed(), indirectContrib_rgb.getGreen()), indirectContrib_rgb.getBlue());
				
				final double russianRouletteProbability = Global.RND.nextDouble();
				if (russianRouletteProbability >= maxIndirectContribComponent)
					continue;
				else
					indirectContribution = indirectContribution.multiply(1d / maxIndirectContribComponent);
				//
				//
				//

				final Ray sampledRay = new Ray(point, sampledDirection, ray);

				final Spectrum sampledW_i = followRay(sampledRay, world, sample)
						.multiply(indirectContribution);

				totalProb_indirect += sampledProb;
				totalW_i_indirect = totalW_i_indirect.add(sampledW_i.multiply(sampledProb));

			}
			
			if (totalProb_indirect == 0.0)
				totalW_i_indirect = totalW_i_indirect.multiply(0d);
			else
				totalW_i_indirect = totalW_i_indirect.multiply(1d / totalProb_indirect);
		}

		//
		//
		//
		return bsdf
				.sampleL_e(relativeInteraction, sample, sample.getAdditionalTwinSample("sample-L_e", twinSampleGridPerInteraction))
					.add(totalW_i_direct.multiply(1d / 2d))
					.add(totalW_i_indirect.multiply(1d / 2d));
	}

}
