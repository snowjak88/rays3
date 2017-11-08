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

	private final int maxRayDepth;

	public SimpleWhittedIntegrator(Camera camera, Film film, Sampler sampler, int maxRayDepth) {
		super(camera, film, sampler);

		this.maxRayDepth = maxRayDepth;
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

			if (ray.getDepth() >= maxRayDepth)
				return RGBSpectrum.BLACK;

			final Optional<Interaction> op_interaction = world
					.getInteractable(ray)
						.stream()
						.map(p -> p.getIntersection(ray))
						.sorted((i1, i2) -> Double.compare(i1.getInteractingRay().getCurrT(),
								i2.getInteractingRay().getCurrT()))
						.findFirst();

			if (op_interaction.isPresent()) {

				final Interaction interaction = op_interaction.get();
				final Point point = interaction.getPoint();
				final Vector w_e = interaction.getInteractingRay().getDirection().negate();
				final Normal n = interaction.getNormal();

				final double n1 = 1d;
				final double n2 = interaction.getBdsf().getIndexOfRefraction();

				//
				// Construct both the reflected and transmitted rays.
				//
				final Vector reflectedVector = interaction.getBdsf().sampleReflectionVector(point, w_e, n, sample);
				final Vector transmittedVector = BDSF.getTransmittedVector(point, w_e, n, n1, n2);

				// Calculate the dot-product of the reflection-vector and the
				// surface-normal.
				final double cos_w_r = reflectedVector.normalize().dotProduct(n.asVector().normalize());

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
				final Spectrum reflectedRadiance = followRay(reflectedRay);
				final Spectrum transmittedRadiance = followRay(transmittedRay);

				final FresnelResult fresnel = BDSF.calculateFresnel(point, w_e, n, n1, n2);

				//
				// Add together all incident radiances: emissive + ( reflective
				// * cos(angle of reflection) ) + transmitted
				return emissiveRadiance.add(reflectedRadiance.multiply(fresnel.getReflectance()).multiply(cos_w_r)).add(
						transmittedRadiance.multiply(fresnel.getTransmittance()));

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
