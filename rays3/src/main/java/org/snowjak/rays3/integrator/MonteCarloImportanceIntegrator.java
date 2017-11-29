package org.snowjak.rays3.integrator;

import java.util.Optional;

import static org.apache.commons.math3.util.FastMath.*;
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
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Performs Monte-Carlo integration using importance sampling.
 * 
 * @author snowjak88
 */
public class MonteCarloImportanceIntegrator extends AbstractIntegrator {

	private final int samplesPerInteraction;

	public MonteCarloImportanceIntegrator(Camera camera, Film film, Sampler sampler, int maxRayDepth,
			int samplesPerInteraction) {

		super(camera, film, sampler, maxRayDepth);
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
			relativeInteraction = new Interaction(interaction, true);
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
		// First, sample all known emissives, to estimate direct illumination.
		for (Primitive p : world.getEmissives()) {

			for (int i = 0; i < samplesPerInteraction; i++) {

				//
				// Create an incident direction from this point toward the
				// emissive shape.
				//
				final Point emissiveSurfacePoint = p.sampleSurfacePoint(
						sample.getAdditionalTwinSample("sample-emissive-surface", samplesPerInteraction), point);
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

				final double sampleProb = bsdf.pdfW_i(relativeInteraction, sample, toEmissiveSurface)
						* ( p.getShape().computeSolidAngle(point) / ( 2d * PI ) );
				final double emissiveDistance = emissiveInteraction.getInteractingRay().getCurrT();

				final Spectrum radianceFromEmissive = p.getBsdf().sampleL_e(emissiveInteraction, sample).multiply(
						1d / ( emissiveDistance * emissiveDistance ));

				totalProb_direct += ( 1d / sampleProb );
				totalW_i_direct = totalW_i_direct.add(radianceFromEmissive
						.multiply(bsdf.f_r(relativeInteraction, sample, toEmissiveSurface))
							.multiply(cos_i)
							.multiply(1d / sampleProb));

			}

		}
		totalW_i_direct = totalW_i_direct.multiply(1d / totalProb_direct);

		//
		//
		// Sample a number of rays and scale them according to their respective
		// probabilities.

		if (ray.getDepth() < getMaxRayDepth()) {

			for (int i = 0; i < samplesPerInteraction; i++) {
				//
				// Sample an incident direction from the BSDF and compute its
				// PDF.
				final Vector sampledDirection = bsdf.sampleW_i(relativeInteraction, sample);
				final double sampledProb = bsdf.pdfW_i(relativeInteraction, sample, sampledDirection);

				final Ray sampledRay = new Ray(point, sampledDirection, ray);

				final Spectrum sampledW_i = followRay(sampledRay, world, sample)
						.multiply(bsdf.f_r(relativeInteraction, sample, sampledDirection))
							.multiply(bsdf.cos_i(relativeInteraction, sampledDirection));

				totalProb_indirect += ( 1d / sampledProb );
				totalW_i_indirect = totalW_i_indirect.add(sampledW_i.multiply(1d / sampledProb));

			}

		}
		totalW_i_indirect = totalW_i_indirect.multiply(1d / totalProb_indirect);

		//
		//
		//
		return bsdf.sampleL_e(relativeInteraction, sample).add(totalW_i_direct.multiply(1d / 2d)).add(
				totalW_i_indirect.multiply(1d / 2d));
	}

}
