package org.snowjak.rays3.integrator;

import java.util.Optional;
import java.util.stream.IntStream;

import org.snowjak.rays3.World;
import org.snowjak.rays3.bxdf.BSDF;
import org.snowjak.rays3.bxdf.BSDF.Property;
import org.snowjak.rays3.bxdf.BSDF.ReflectType;
import org.snowjak.rays3.bxdf.FresnelApproximation;
import org.snowjak.rays3.camera.Camera;
import org.snowjak.rays3.film.Film;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.light.Light;
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
public class SimpleWhittedIntegrator extends AbstractIntegrator {

	/**
	 * Construct a new {@link SimpleWhittedIntegrator}.
	 * 
	 * @param camera
	 * @param film
	 * @param sampler
	 */
	public SimpleWhittedIntegrator(Camera camera, Film film, Sampler sampler, int maxRayDepth) {
		super(camera, film, sampler, maxRayDepth);
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
				relativeInteraction = new Interaction(interaction, true);

			} else {
				// Nope -- the eye-vector is on the same side as the normal.
				relativeInteraction = interaction;
			}

			//
			//
			//
			final Point point = relativeInteraction.getPoint();
			final Vector w_e = relativeInteraction.getW_e();
			final Normal n = relativeInteraction.getNormal();
			final BSDF bsdf = relativeInteraction.getBdsf();

			//
			//
			// Allocate the Fresnel approximation.
			final FresnelApproximation fresnel = relativeInteraction.getFresnel();

			//
			//
			// Calculate the total light radiance present at the given
			// point.
			final Spectrum totalLightRadiance = world.getLights().stream().map(l -> {

				return IntStream.range(0, sample.getSampler().getSamplesPerPixel()).mapToObj(i -> {

					final Vector sampledLightVector = l.sampleLightVector(point, sample);
					final double sampledLightProb = l.probabilitySampleVector(point, sampledLightVector, sample);

					final Spectrum radianceFromLight = l.getRadianceAt(sampledLightVector, n).multiply(
							sampledLightProb / sample.getSampler().getSamplesPerPixel());

					if (!radianceFromLight.isBlack()) {

						if (Light.isVisibleFrom(world, point, Light.getLightSurfacePoint(point, sampledLightVector)))
							return radianceFromLight;
					}
					return RGBSpectrum.BLACK;

				}).reduce(RGBSpectrum.BLACK, (s1, s2) -> s1.add(s2));

			}).reduce(RGBSpectrum.BLACK, (s1, s2) -> s1.add(s2));

			//
			//
			// Determine the total radiance due to diffuse reflection.
			final Spectrum diffuseRadiance;
			if (bsdf.hasProperty(Property.REFLECT_DIFFUSE)) {

				final Spectrum surfaceColoration = bsdf.getReflectiveColoration(interaction, sample.getWavelength(),
						sample.getT());

				diffuseRadiance = totalLightRadiance.multiply(surfaceColoration).multiply(fresnel.getReflectance());
			} else {
				diffuseRadiance = RGBSpectrum.BLACK;
			}

			//
			//
			// Determine the total radiance due to specular reflection.
			final Spectrum specularRadiance;
			if (bsdf.hasProperty(Property.REFLECT_SPECULAR)) {

				if (ray.getDepth() >= getMaxRayDepth()) {
					specularRadiance = RGBSpectrum.BLACK;
				} else {
					final Vector specularVector = bsdf.sampleReflectionVector(point, w_e, n, sample,
							ReflectType.SPECULAR);
					final Ray specularRay = new Ray(point, specularVector, ray);

					final Spectrum specularTint;
					if (bsdf.hasProperty(Property.DIALECTRIC))
						specularTint = bsdf.getReflectiveColoration(interaction, sample.getWavelength(), sample.getT());
					else
						specularTint = RGBSpectrum.WHITE;

					specularRadiance = followRay(specularRay, world, sample)
							.multiply(fresnel.getReflectance())
								.multiply(specularTint);

				}

			} else {
				specularRadiance = RGBSpectrum.BLACK;
			}

			//
			//
			// Determine the total radiance due to transmission.
			final Spectrum transmitRadiance;
			if (bsdf.hasProperty(Property.TRANSMIT)) {

				if (ray.getDepth() >= getMaxRayDepth()) {
					transmitRadiance = RGBSpectrum.BLACK;
				} else {

					if (!fresnel.isTotalInternalReflection()) {

						final Vector transmitVector = fresnel.getTransmittedDirection();
						final Ray transmitRay = new Ray(point, transmitVector, ray);
						transmitRadiance = followRay(transmitRay, world, sample).multiply(fresnel.getTransmittance());

					} else {
						transmitRadiance = RGBSpectrum.BLACK;
					}
				}
			} else {
				transmitRadiance = RGBSpectrum.BLACK;
			}

			//
			//
			// Determine the total emissive radiance.
			final Spectrum emissiveRadiance = bsdf.getEmissiveRadiance(interaction, sample.getWavelength(),
					sample.getT());

			//
			// And compile total radiance.
			return emissiveRadiance.add(diffuseRadiance).add(specularRadiance).add(transmitRadiance);

		} else {
			return RGBSpectrum.BLACK;
		}
	}

}
