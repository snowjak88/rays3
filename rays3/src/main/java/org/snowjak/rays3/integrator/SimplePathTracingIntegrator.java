package org.snowjak.rays3.integrator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

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
 * A simple Whitted-style integrator will render a {@link World} using the
 * following rules:
 * <ul>
 * <li>Each found Interaction will generate two Rays -- one for reflection, one
 * for transmittance -- if the allowable Ray-depth has not yet been
 * exceeded</li>
 * <li>If the interacting Ray has reached the maximum-allowed Ray-depth, no
 * further Rays will be generated</li>
 * </ul>
 * 
 * @author snowjak88
 */
public class SimplePathTracingIntegrator extends AbstractIntegrator {

	/**
	 * Construct a new {@link SimplePathTracingIntegrator}.
	 * 
	 * @param camera
	 * @param film
	 * @param sampler
	 */
	public SimplePathTracingIntegrator(Camera camera, Film film, Sampler sampler, int maxRayDepth) {
		this(camera, film, Arrays.asList(sampler), maxRayDepth);
	}

	/**
	 * Construct a new {@link SimplePathTracingIntegrator}.
	 * 
	 * @param camera
	 * @param film
	 * @param samplers
	 */
	public SimplePathTracingIntegrator(Camera camera, Film film, Collection<Sampler> samplers, int maxRayDepth) {
		super(camera, film, samplers, maxRayDepth);
	}

	@Override
	public Spectrum followRay(Ray ray, World world, Sample sample) {

		final Optional<Interaction> op_interaction = world.getClosestInteraction(ray);

		if (op_interaction.isPresent()) {

			final Interaction interaction = op_interaction.get();

			//
			// If the interacting ray is on the opposite side of the surface
			// from its normal, then swap the two indices of refraction.
			final Interaction relativeInteraction;
			if (interaction.getW_e().normalize().dotProduct(interaction.getNormal().asVector().normalize()) < 0d) {
				relativeInteraction = new Interaction(interaction, interaction.getNormal().negate());

			} else {
				// Nope -- the eye-vector is on the same side as the normal.
				relativeInteraction = interaction;
			}

			//
			//
			//
			final Point point = relativeInteraction.getPoint();
			final BSDF bsdf = relativeInteraction.getBdsf();

			//
			//
			// With naive path-tracing, we will simply return the BSDF's
			// emissive color, plus the BSDF's reflectance function multiplied
			// by any incoming radiance (if the ray is not too deep yet) both
			// from known light-sources and from the BSDF's sampled domain.
			//

			//
			//
			//
			final Spectrum incidentRadiance;

			// Estimate the total incident radiance due to direct
			// illumination.
			final Spectrum directRadiance = world.getEmissives().stream().map(p -> {
				final Point emissiveSurfacePoint = p
						.sampleSurfacePoint(sample.getAdditionalTwinSample("sample-emissive-surface", 1), point);
				final Vector toEmissiveVector = new Vector(point, emissiveSurfacePoint);

				if (toEmissiveVector.normalize().dotProduct(interaction.getNormal().asVector().normalize()) <= 0d)
					return RGBSpectrum.BLACK;

				final Ray toEmissiveSurfaceRay = new Ray(point, toEmissiveVector);
				final Optional<Interaction> op_emissiveInteraction = world.getClosestInteraction(toEmissiveSurfaceRay);
				if (!op_emissiveInteraction.isPresent())
					return RGBSpectrum.BLACK;

				final Interaction emissiveInteraction = op_emissiveInteraction.get();
				final double emissiveDistance = emissiveInteraction.getInteractingRay().getCurrT();

				if (emissiveInteraction.getPrimitive() != p)
					return RGBSpectrum.BLACK;

				final Spectrum emissiveRadiance = p.getBsdf().sampleL_e(emissiveInteraction, sample,
						sample.getAdditionalTwinSample("sample-emisive-L_e", 1));
				return emissiveRadiance
						.multiply(bsdf.f_r(relativeInteraction, sample,
								sample.getAdditionalTwinSample("sample-emissive-f_r", 1), toEmissiveVector))
							.multiply(bsdf.cos_i(relativeInteraction, toEmissiveVector))
							.multiply(1d / ( emissiveDistance * emissiveDistance ));
			}).reduce(RGBSpectrum.BLACK, Spectrum::add);

			//
			//
			//

			final Spectrum indirectRadiance;
			if (ray.getDepth() < getMaxRayDepth()) {

				// Estimate the total incident radiance due to indirect
				// illumination.

				final Vector reflectedDirection = bsdf.sampleW_i(relativeInteraction, sample,
						sample.getAdditionalTwinSample("sample-indirect-W_i", 1));
				final Ray reflectedRay = new Ray(point, reflectedDirection, ray);

				indirectRadiance = followRay(reflectedRay, world, sample)
						.multiply(bsdf.f_r(relativeInteraction, sample,
								sample.getAdditionalTwinSample("sample-indirect-f_r", 1), reflectedDirection))
							.multiply(bsdf.cos_i(relativeInteraction, reflectedDirection));

			} else {
				indirectRadiance = RGBSpectrum.BLACK;
			}

			if (ray.getDepth() < getMaxRayDepth())
				incidentRadiance = directRadiance.add(indirectRadiance).multiply(1d / 2d);
			else
				incidentRadiance = directRadiance;

			//
			//
			final Spectrum result = bsdf
					.sampleL_e(relativeInteraction, sample, sample.getAdditionalTwinSample("sample-L_e", 1))
						.add(incidentRadiance);
			return result;

		} else {
			return RGBSpectrum.BLACK;
		}
	}

}
