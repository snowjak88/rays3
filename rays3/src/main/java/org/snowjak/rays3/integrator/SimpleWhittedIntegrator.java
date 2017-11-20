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
	private final AtomicInteger	samplesCurrentlyRenderingCount;

	private final int			maxRayDepth;

	private boolean				finishedGettingSamples;

	public SimpleWhittedIntegrator(Camera camera, Film film, Sampler sampler, int maxRayDepth) {
		super(camera, film, sampler);

		this.samplesSubmitted = new AtomicInteger(0);
		this.samplesCurrentlyRenderingCount = new AtomicInteger(0);
		this.maxRayDepth = maxRayDepth;
		this.finishedGettingSamples = false;
	}

	@Override
	public void render(World world) {

		Sample currentSample;

		while (( currentSample = getSampler().getNextSample() ) != null) {

			Global.EXECUTOR.execute(new RenderSampleTask(world, currentSample, getCamera(), getFilm(), maxRayDepth,
					samplesCurrentlyRenderingCount));

			samplesSubmitted.incrementAndGet();
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
		private final AtomicInteger	samplesCurrentlyRenderingCount;

		public RenderSampleTask(World world, Sample sample, Camera camera, Film film, int maxRayDepth,
				AtomicInteger samplesCurrentlyRenderingCount) {

			super();
			this.world = world;
			this.sample = sample;
			this.camera = camera;
			this.film = film;
			this.maxRayDepth = maxRayDepth;
			this.samplesCurrentlyRenderingCount = samplesCurrentlyRenderingCount;
		}

		@Override
		protected void compute() {

			this.samplesCurrentlyRenderingCount.incrementAndGet();

			//
			// Set up the initial ray to follow.
			final Ray ray = camera.getRay(sample);

			//
			// Follow the ray.
			//
			// (notice that the initial ray-follow, at least, is kept on this
			// same thread)
			final Spectrum spectrum = new FollowRayRecursiveTask(ray, world, maxRayDepth, sample).invoke();

			if (sample.getSampler().isSampleAcceptable(sample, spectrum))
				film.addSample(sample, spectrum);

			this.samplesCurrentlyRenderingCount.decrementAndGet();
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
				// Construct both the reflected and transmitted rays.
				//
				// final Vector reflectedVector =
				// interaction.getBdsf().sampleReflectionVector(point, w_e, n,
				// sample);
				final FresnelResult fresnel = BDSF.calculateFresnel(w_e, relativeNormal, n1, n2);

				final Vector reflectedVector = fresnel.getReflectedDirection();
				final Vector transmittedVector = fresnel.getTransmittedDirection();

				final Ray reflectedRay = new Ray(point, reflectedVector, ray);
				final Ray transmittedRay;
				if (fresnel.isTotalInternalReflection())
					transmittedRay = null;
				else
					transmittedRay = new Ray(point, transmittedVector, ray);

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

				final ForkJoinTask<Spectrum> incidentRadiance_transmission;
				if (transmittedRay != null)
					incidentRadiance_transmission = new FollowRayRecursiveTask(transmittedRay, world, maxRayDepth,
							sample).fork();
				else
					//
					// Given that this is a case of Total Internal Reflection,
					// we don't have a transmission-direction. As such, we can
					// treat the final "transmission" radiance as BLACK -- i.e.,
					// nothing.
					incidentRadiance_transmission = new ConstantResultTask(RGBSpectrum.BLACK).fork();

				//
				//
				Spectrum totalLightRadiance = new RGBSpectrum();
				for (Light l : world.getLights()) {
					final Vector sampledLightVector = l.sampleLightVector(point);
					final Spectrum radianceFromLight = l.getRadianceAt(sampledLightVector, relativeNormal);
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
				final Spectrum result = emissiveRadiance
						.add(surfaceIrradiance)
							.multiply(fresnel.getReflectance())
							.add(incidentRadiance_reflection.join().multiply(fresnel.getReflectance()).multiply(
									reflectedRay.getDirection().dotProduct(relativeNormal.asVector().normalize())))
							.add(incidentRadiance_transmission.join().multiply(fresnel
									.getTransmittance()));

				return result;

			} else {
				return RGBSpectrum.BLACK;
			}
		}

	}

	/**
	 * Used in place of a full-on {@link FollowRayRecursiveTask} when we already
	 * know the result -- e.g., when it's a case of Total Internal Reflection
	 * and we already know that no energy will be transmitted.
	 * 
	 * @author snowjak88
	 */
	private static class ConstantResultTask extends RecursiveTask<Spectrum> {

		private static final long	serialVersionUID	= 5147703859633559447L;
		private final Spectrum		result;

		public ConstantResultTask(Spectrum result) {

			super();
			this.result = result;
		}

		@Override
		protected Spectrum compute() {

			return result;
		}

	}

}
