package org.snowjak.rays3.integrator;

import java.util.Optional;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.World;
import org.snowjak.rays3.bxdf.BDSF;
import org.snowjak.rays3.bxdf.BDSF.Property;
import org.snowjak.rays3.bxdf.BDSF.ReflectType;
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

	private final int			maxRayDepth;
	private boolean				finishedGettingSamples;

	private final AtomicInteger	samplesWaitingToRender;
	private final AtomicInteger	samplesCurrentlyRenderingCount;

	/**
	 * Construct a new {@link SimpleWhittedIntegrator}.
	 * 
	 * @param camera
	 * @param film
	 * @param sampler
	 * @param maxRayDepth
	 */
	public SimpleWhittedIntegrator(Camera camera, Film film, Sampler sampler, int maxRayDepth) {
		super(camera, film, sampler);

		this.maxRayDepth = maxRayDepth;
		this.finishedGettingSamples = false;
		this.samplesWaitingToRender = new AtomicInteger(0);
		this.samplesCurrentlyRenderingCount = new AtomicInteger(0);
	}

	@Override
	public void render(World world) {

		Sample currentSample;

		while (( currentSample = getSampler().getNextSample() ) != null) {

			samplesWaitingToRender.incrementAndGet();

			Global.RENDER_EXECUTOR.execute(new RenderSampleTask(world, currentSample, getCamera(), getFilm(), maxRayDepth,
					samplesWaitingToRender, samplesCurrentlyRenderingCount));
		}

		this.finishedGettingSamples = true;
	}

	@Override
	public boolean isFinishedGettingSamples() {

		return finishedGettingSamples;
	}

	@Override
	public boolean isFinishedRenderingSamples() {

		return samplesCurrentlyRenderingCount.get() == 0;
	}

	@Override
	public int countSamplesWaitingToRender() {

		return samplesWaitingToRender.get();
	}

	@Override
	public int countSamplesCurrentlyRendering() {

		return samplesCurrentlyRenderingCount.get();
	}

	public static class RenderSampleTask extends RecursiveAction {

		/**
		 * 
		 */
		private static final long	serialVersionUID	= 3322955501411696398L;
		private final World			world;
		private final Sample		sample;
		private final Camera		camera;
		private final Film			film;
		private final int			maxRayDepth;
		private final AtomicInteger	samplesWaitingToRender;
		private final AtomicInteger	samplesCurrentlyRenderingCount;

		public RenderSampleTask(World world, Sample sample, Camera camera, Film film, int maxRayDepth,
				AtomicInteger samplesWaitingToRender, AtomicInteger samplesCurrentlyRenderingCount) {

			super();
			this.world = world;
			this.sample = sample;
			this.camera = camera;
			this.film = film;
			this.maxRayDepth = maxRayDepth;
			this.samplesWaitingToRender = samplesWaitingToRender;
			this.samplesCurrentlyRenderingCount = samplesCurrentlyRenderingCount;
		}

		@Override
		protected void compute() {

			this.samplesWaitingToRender.decrementAndGet();
			this.samplesCurrentlyRenderingCount.incrementAndGet();

			//
			// Set up the initial ray to follow.
			final Ray ray = camera.getRay(sample);

			//
			// Follow the ray.
			//
			// (notice that the initial ray-follow, at least, is kept on this
			// same thread)
			final Spectrum spectrum = new FollowRayRecursiveTask(ray, world, maxRayDepth, sample)
					.invoke()
						.multiply(1d / sample.getSampler().getSamplesPerPixel());

			this.samplesCurrentlyRenderingCount.decrementAndGet();

			if (sample.getSampler().isSampleAcceptable(sample, spectrum))
				film.addSample(sample, spectrum);
		}

	}

	public static class FollowRayRecursiveTask extends RecursiveTask<Spectrum> {

		/**
		 * 
		 */
		private static final long	serialVersionUID	= 6129032173615008877L;
		private final Ray			ray;
		private final World			world;
		private final int			maxRayDepth;
		private final Sample		sample;

		public FollowRayRecursiveTask(Ray ray, World world, int maxRayDepth, Sample sample) {

			super();
			this.ray = ray;
			this.world = world;
			this.maxRayDepth = maxRayDepth;
			this.sample = sample;
		}

		@Override
		protected Spectrum compute() {

			final Optional<Interaction> op_interaction = world
					.getClosestInteraction(ray);

			if (op_interaction.isPresent()) {

				final Interaction interaction = op_interaction.get();
				final Point point = interaction.getPoint();
				final Vector w_e = interaction.getInteractingRay().getDirection().negate();
				final Normal n = interaction.getNormal();
				final BDSF bdsf = interaction.getBdsf();

				final double n1;
				final double n2;
				final Normal relativeNormal;

				//
				// If the interacting ray is on the opposite side of the surface
				// from its normal, then swap the two indices of refraction.
				if (w_e.normalize().dotProduct(n.asVector().normalize()) < 0d) {
					n1 = interaction.getBdsf().getIndexOfRefraction();
					n2 = 1d;
					relativeNormal = n.negate();

				} else {
					// Nope -- the eye-vector is on the same side as the normal.
					n1 = 1d;
					n2 = interaction.getBdsf().getIndexOfRefraction();
					relativeNormal = n;
				}

				//
				//
				// Allocate the Fresnel approximation.
				final FresnelApproximation fresnel = new FresnelApproximation(w_e, relativeNormal, n1, n2);

				//
				//
				// Calculate the total light radiance present at the given
				// point.
				final Spectrum totalLightRadiance = world.getLights().stream().map(l -> {

					return IntStream.range(0, sample.getSampler().getSamplesPerPixel()).mapToObj(i -> {

						final Vector sampledLightVector = l.sampleLightVector(point, sample);
						final double sampledLightProb = l.probabilitySampleVector(point, sampledLightVector, sample);

						final Spectrum radianceFromLight = l.getRadianceAt(sampledLightVector, relativeNormal).multiply(
								sampledLightProb / sample.getSampler().getSamplesPerPixel());

						if (!radianceFromLight.isBlack()) {

							if (Light.isVisibleFrom(world, point,
									Light.getLightSurfacePoint(point, sampledLightVector)))
								return radianceFromLight;
						}
						return RGBSpectrum.BLACK;

					}).reduce(RGBSpectrum.BLACK, (s1, s2) -> s1.add(s2));

				}).reduce(RGBSpectrum.BLACK, (s1, s2) -> s1.add(s2));

				//
				//
				// Determine the total radiance due to diffuse reflection.
				final Spectrum diffuseRadiance;
				if (bdsf.hasProperty(Property.REFLECT_DIFFUSE)) {

					final Spectrum surfaceColoration = bdsf.getReflectiveColoration(interaction, sample.getWavelength(),
							sample.getT());

					diffuseRadiance = totalLightRadiance.multiply(surfaceColoration).multiply(fresnel.getReflectance());
				} else {
					diffuseRadiance = RGBSpectrum.BLACK;
				}

				//
				//
				// Determine the total radiance due to specular reflection.
				final Spectrum specularRadiance;
				if (bdsf.hasProperty(Property.REFLECT_SPECULAR)) {

					if (ray.getDepth() >= this.maxRayDepth)
						specularRadiance = RGBSpectrum.BLACK;

					else {
						final Vector specularVector = bdsf.sampleReflectionVector(point, w_e, relativeNormal, sample,
								ReflectType.SPECULAR);
						final Ray specularRay = new Ray(point, specularVector, ray);

						final Spectrum specularTint;
						if (bdsf.hasProperty(Property.DIALECTRIC))
							specularTint = bdsf.getReflectiveColoration(interaction, sample.getWavelength(),
									sample.getT());
						else
							specularTint = RGBSpectrum.WHITE;

						specularRadiance = new FollowRayRecursiveTask(specularRay, world, maxRayDepth, sample)
								.invoke()
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
				if (bdsf.hasProperty(Property.TRANSMIT)) {

					if (ray.getDepth() >= this.maxRayDepth)
						transmitRadiance = RGBSpectrum.BLACK;

					else {

						if (!fresnel.isTotalInternalReflection()) {

							final Vector transmitVector = fresnel.getTransmittedDirection();
							final Ray transmitRay = new Ray(point, transmitVector, ray);
							transmitRadiance = new FollowRayRecursiveTask(transmitRay, world, maxRayDepth, sample)
									.invoke()
										.multiply(fresnel.getTransmittance());

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
				final Spectrum emissiveRadiance = bdsf.getEmissiveRadiance(interaction, sample.getWavelength(),
						sample.getT());

				//
				// And compile total radiance.
				return emissiveRadiance.add(diffuseRadiance).add(specularRadiance).add(transmitRadiance);

			} else {
				return RGBSpectrum.BLACK;
			}
		}

	}

}
