package org.snowjak.rays3.integrator;

import java.util.Optional;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.World;
import org.snowjak.rays3.bxdf.BSDF;
import org.snowjak.rays3.camera.Camera;
import org.snowjak.rays3.film.Film;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
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

		ray = new Ray(ray, 1.0);

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
		// Sample a number of rays and scale them according to their respective
		// probabilities.
		Spectrum totalW_i = RGBSpectrum.BLACK;

		if (ray.getDepth() < getMaxRayDepth())
			for (int i = 0; i < samplesPerInteraction; i++) {

				final Vector sampledDirection = bsdf.sampleW_i(relativeInteraction, sample);
				final double sampledProb = bsdf.pdfW_i(relativeInteraction, sample, sampledDirection);
				final double cos_i = bsdf.cos_i(relativeInteraction, sampledDirection);

				if (Global.RND.nextDouble() >= cos_i) {

					final Ray sampledRay = new Ray(point, sampledDirection, ray, ray.getWeight() * cos_i);

					final Spectrum sampledW_i = followRay(sampledRay, world, sample)
							.multiply(bsdf.f_r(relativeInteraction, sample, sampledDirection))
								.multiply(cos_i);

					totalW_i = totalW_i.add(sampledW_i.multiply(1d / sampledProb).multiply(1d / cos_i));

				}

			}

		totalW_i = totalW_i.multiply(1d / (double) samplesPerInteraction);

		//
		//
		//
		return bsdf.sampleL_e(relativeInteraction, sample).add(totalW_i);
	}

}
