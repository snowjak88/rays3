package org.snowjak.rays3.integrator;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

	private final int	maxRayDepth;

	private boolean		finishedGettingSamples;

	public SimpleWhittedIntegrator(Camera camera, Film film, Sampler sampler, int maxRayDepth) {
		super(camera, film, sampler);

		this.maxRayDepth = maxRayDepth;
		this.finishedGettingSamples = false;
	}

	@Override
	public void render(World world) {

		Sample currentSample = getSampler().getNextSample();

		while (currentSample != null) {

			Future<Spectrum> sampleResult = Global.EXECUTOR
					.submit(new SampleCallable(world, currentSample, getCamera(), maxRayDepth));

			Global.EXECUTOR.submit(new UpdateFilmTask(getFilm(), currentSample, sampleResult));

			currentSample = getSampler().getNextSample();
		}

		this.finishedGettingSamples = true;
	}

	@Override
	public boolean isFinishedGettingSamples() {

		return finishedGettingSamples;
	}

	public static class SampleCallable implements Callable<Spectrum> {

		private final World		world;
		private final Sample	sample;
		private final Camera	camera;
		private final int		maxRayDepth;

		public SampleCallable(World world, Sample sample, Camera camera, int maxRayDepth) {

			this.world = world;
			this.sample = sample;
			this.camera = camera;
			this.maxRayDepth = maxRayDepth;
		}

		@Override
		public Spectrum call() throws Exception {

			//
			// Set up the initial ray to follow.
			Ray ray = camera.getRay(sample);

			//
			// Follow the ray.
			return followRay(ray);
		}

		private Spectrum followRay(Ray ray) {

			final Optional<Interaction> op_interaction = world
					.getPrimitives()
						.stream()
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
				final Spectrum incidentRadiance_reflection = followRay(reflectedRay);
				final Spectrum incidentRadiance_transmission = followRay(transmittedRay);

				//
				//
				Spectrum totalLightRadiance = new RGBSpectrum();
				for (Light l : world.getLights()) {
					final Vector sampledVector = l.sampleLightVector(point);
					totalLightRadiance = totalLightRadiance.add(l.getRadianceAt(sampledVector, n));
				}

				//
				//
				final Spectrum surfaceIrradiance = interaction
						.getBdsf()
							.getReflectableRadiance(interaction, reflectedVector, null, sample.getT())
							.multiply(totalLightRadiance);

				//
				// Add together all incident radiances: emissive + ( reflective
				// * cos(angle of reflection) ) + transmitted
				return emissiveRadiance
						.add(surfaceIrradiance)
							.add(incidentRadiance_reflection.multiply(fresnel.getReflectance()))
							.add(incidentRadiance_transmission.multiply(fresnel.getTransmittance()));

			} else {
				return RGBSpectrum.BLACK;
			}
		}

	}

	public static class UpdateFilmTask implements Runnable {

		private final Film				film;
		private final Sample			sample;
		private final Future<Spectrum>	f_spectrum;

		public UpdateFilmTask(Film film, Sample sample, Future<Spectrum> spectrum) {

			this.film = film;
			this.sample = sample;
			this.f_spectrum = spectrum;
		}

		@Override
		public void run() {

			try {

				final Spectrum spectrum = f_spectrum.get();

				if (sample.getSampler().isSampleAcceptable(sample, spectrum)) {
					synchronized (film) {
						film.addSample(sample, spectrum);
					}
				}

			} catch (InterruptedException | ExecutionException e) {

				// TODO add some kind of logging eventually
				e.printStackTrace();
			}
		}

	}

}
