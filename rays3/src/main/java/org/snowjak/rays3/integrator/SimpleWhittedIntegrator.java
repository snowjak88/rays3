package org.snowjak.rays3.integrator;

import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.World;
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

			Global.EXECUTOR.execute(new RenderSampleTask(world, currentSample, getCamera(), getFilm(), maxRayDepth,
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
				// Start compiling total radiance emanating from this point by
				// including emissive radiance.
				//
				final Spectrum emissiveRadiance = interaction.getBdsf().getEmissiveRadiance(interaction,
						sample.getWavelength(), sample.getT());

				final FresnelApproximation fresnel = new FresnelApproximation(w_e, relativeNormal, n1, n2);
				final Vector reflectedVector = fresnel.getReflectedDirection();
				final Vector transmittedVector = fresnel.getTransmittedDirection();

				final Ray reflectedRay = new Ray(point, reflectedVector, ray);

				//
				//
				// Should we follow the transmission and reflection rays?
				// Or should we bypass them because we're at the maximum-allowed
				// ray-depth?
				//
				final ForkJoinTask<Spectrum> incidentRadiance_reflection;
				final ForkJoinTask<Spectrum> incidentRadiance_transmission;
				//
				if (ray.getDepth() >= maxRayDepth) {
					//
					// To bypass the reflection and transmission ray-following,
					// we can simply treat them as already solved, with final
					// radiance-values of BLACK.
					incidentRadiance_reflection = new ConstantResultTask(RGBSpectrum.BLACK);
					incidentRadiance_transmission = new ConstantResultTask(RGBSpectrum.BLACK);
				} else {
					//
					// Follow both the reflected and transmitted rays.
					//
					//
					// Construct both the reflected and transmitted rays.

					//
					incidentRadiance_reflection = new FollowRayRecursiveTask(reflectedRay, world, maxRayDepth, sample);
					//
					// Remember that transmission will only take place if this
					// is NOT a case of Total Internal Reflection.
					//
					if (!fresnel.isTotalInternalReflection()) {

						final Ray transmittedRay = new Ray(point, transmittedVector, ray);
						incidentRadiance_transmission = new FollowRayRecursiveTask(transmittedRay, world, maxRayDepth,
								sample);
					} else
						//
						// Given that this is a case of Total Internal
						// Reflection, we don't have a transmission-direction.
						// As such, we can treat the final "transmission"
						// radiance as BLACK -- i.e., nothing.
						incidentRadiance_transmission = new ConstantResultTask(RGBSpectrum.BLACK);
				}

				//
				//
				Spectrum totalLightRadiance = new RGBSpectrum();
				for (Light l : world.getLights()) {

					//
					//

					for (int i = 0; i < sample.getSampler().getSamplesPerPixel(); i++) {
						final Vector sampledLightVector = l.sampleLightVector(point, sample);
						final double sampledLightProb = l.probabilitySampleVector(point, sampledLightVector, sample);

						final Spectrum radianceFromLight = l.getRadianceAt(sampledLightVector, relativeNormal).multiply(
								sampledLightProb / sample.getSampler().getSamplesPerPixel());

						if (!radianceFromLight.isBlack()) {

							if (Light.isVisibleFrom(world, point,
									Light.getLightSurfacePoint(point, sampledLightVector)))

								totalLightRadiance = totalLightRadiance.add(radianceFromLight);
						}
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
							.add(incidentRadiance_reflection.invoke().multiply(fresnel.getReflectance()).multiply(
									reflectedRay.getDirection().dotProduct(relativeNormal.asVector().normalize())))
							.add(incidentRadiance_transmission.invoke().multiply(fresnel
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
