package org.snowjak.rays3.integrator;

import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.World;
import org.snowjak.rays3.bxdf.BDSF;
import org.snowjak.rays3.bxdf.BDSF.FresnelResult;
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

	private final AtomicInteger	samplesSubmitted;

	private final int			maxRayDepth;

	private boolean				finishedGettingSamples;

	public SimpleWhittedIntegrator(Camera camera, Film film, Sampler sampler, int maxRayDepth) {
		super(camera, film, sampler);

		this.samplesSubmitted = new AtomicInteger(0);
		this.maxRayDepth = maxRayDepth;
		this.finishedGettingSamples = false;
	}

	@Override
	public void render(World world) {

		Sample currentSample = getSampler().getNextSample();

		while (currentSample != null) {

			Global.EXECUTOR.submit(new RenderSampleTask(world, currentSample, getCamera(), getFilm(), maxRayDepth));

			samplesSubmitted.incrementAndGet();

			currentSample = getSampler().getNextSample();
		}

		this.finishedGettingSamples = true;
	}

	@Override
	public boolean isFinishedGettingSamples() {

		return finishedGettingSamples;
	}

	@Override
	public int countSamplesSubmitted() {

		return samplesSubmitted.get();
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

		public RenderSampleTask(World world, Sample sample, Camera camera, Film film, int maxRayDepth) {

			super();
			this.world = world;
			this.sample = sample;
			this.camera = camera;
			this.film = film;
			this.maxRayDepth = maxRayDepth;
		}

		@Override
		protected void compute() {

			//
			// Set up the initial ray to follow.
			final Ray ray = camera.getRay(sample);

			//
			// Follow the ray.
			//
			// (notice that the initial ray-follow, at least, is kept on this
			// same thread)
			final Spectrum spectrum = new FollowRayRecursiveTask(ray, world, maxRayDepth, sample)
					.fork()
						.join()
						.multiply(1d / (double) sample.getSampler().getSamplesPerPixel());

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
					.getPrimitives()
						.parallelStream()
						.filter(p -> p.isInteracting(ray))
						.map(p -> p.getIntersection(ray))
						.filter(p -> p != null)
						.sorted((i1, i2) -> Double.compare(i1.getInteractingRay().getCurrT(),
								i2.getInteractingRay().getCurrT()))
						.findFirst();

			if (op_interaction.isPresent()) {

				final Interaction interaction = op_interaction.get();
				final Point point = interaction.getPoint();
				final Vector w_e = interaction.getInteractingRay().getDirection().negate();
				final Normal n = interaction.getNormal();

				if (ray.getDepth() >= maxRayDepth)
					return interaction
							.getBdsf()
								.getReflectableRadiance(interaction, w_e, sample.getWavelength(), sample.getT())
								.add(interaction.getBdsf().getEmissiveRadiance(interaction, sample.getWavelength(),
										sample.getT()));

				final double n1 = 1d;
				final double n2 = interaction.getBdsf().getIndexOfRefraction();

				//
				// Construct both the reflected and transmitted rays.
				//
				// final Vector reflectedVector =
				// interaction.getBdsf().sampleReflectionVector(point, w_e, n,
				// sample);
				final Vector reflectedVector = BDSF.getPerfectSpecularReflectionVector(point, w_e, n);
				final Vector transmittedVector = BDSF.getTransmittedVector(point, w_e, n, n1, n2);

				final FresnelResult fresnel = BDSF.calculateFresnel(point, w_e, reflectedVector, n, n1, n2);

				final Ray reflectedRay = new Ray(point, reflectedVector, ray);
				final Ray transmittedRay = new Ray(point, transmittedVector, ray);

				//
				// Start compiling total radiance emanating from this point by
				// including emissive radiance.
				//
				final Spectrum emissiveRadiance = interaction.getBdsf().getEmissiveRadiance(interaction,
						sample.getWavelength(), sample.getT());

				//
				// Follow both the reflected and transmitted rays.
				//
				// Notice that we have only the Future objects here. We will
				// not try to access these Futures until we actually need them,
				// right at the end of this method (when we total up all
				// radiances).
				final ForkJoinTask<Spectrum> incidentRadiance_reflection = new FollowRayRecursiveTask(reflectedRay,
						world, maxRayDepth, sample).fork();
				final ForkJoinTask<Spectrum> incidentRadiance_transmission = new FollowRayRecursiveTask(transmittedRay,
						world, maxRayDepth, sample).fork();

				//
				//
				Spectrum totalLightRadiance = new RGBSpectrum();
				for (Light l : world.getLights()) {
					final Vector sampledLightVector = l.sampleLightVector(point);
					final Spectrum radianceFromLight = l.getRadianceAt(sampledLightVector, n);
					if (!radianceFromLight.isBlack()) {

						if (Light.isVisibleFrom(world, point, Light.getLightSurfacePoint(point, sampledLightVector)))

							totalLightRadiance = totalLightRadiance.add(radianceFromLight);
					}
				}

				//
				//
				final Spectrum surfaceIrradiance = interaction
						.getBdsf()
							.getReflectableRadiance(interaction, reflectedVector, null, sample.getT())
							.multiply(totalLightRadiance);

				//
				// Add together all incident radiances: emissive + (surface
				// irradiance)
				// + ( reflective * cos(angle of reflection) ) + transmitted
				return emissiveRadiance
						.add(surfaceIrradiance)
							.multiply(fresnel.getReflectance())
							.add(incidentRadiance_reflection.join().multiply(fresnel.getReflectance()).multiply(
									reflectedRay.getDirection().dotProduct(n.asVector().normalize())))
							.add(incidentRadiance_transmission.join().multiply(fresnel
									.getTransmittance()));

			} else {
				return RGBSpectrum.BLACK;
			}
		}

	}

}
