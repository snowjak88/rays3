package org.snowjak.rays3;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.bxdf.LambertianBDRF;
import org.snowjak.rays3.camera.Camera;
import org.snowjak.rays3.camera.PinholeCamera;
import org.snowjak.rays3.film.SimpleImageFilm;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.geometry.shape.PlaneShape;
import org.snowjak.rays3.geometry.shape.Primitive;
import org.snowjak.rays3.geometry.shape.SphereShape;
import org.snowjak.rays3.integrator.SimpleWhittedIntegrator;
import org.snowjak.rays3.light.Light;
import org.snowjak.rays3.light.PointLight;
import org.snowjak.rays3.sample.Sampler;
import org.snowjak.rays3.sample.StratifiedSampler;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.texture.CheckerboardTexture;
import org.snowjak.rays3.texture.ConstantTexture;
import org.snowjak.rays3.transform.TranslationTransform;

public class Main {

	public static void main(String[] args) {

		World world = new World();

		for (double x = -5d; x <= 5d; x += 1d) {
			for (double z = -5d; z <= 5d; z += 1d) {
				final double hue = FastMath.atan2(z, x) * 180d / FastMath.PI + 180d;
				final double saturation = FastMath.sqrt(( x * x ) + ( z * z )) / FastMath.sqrt(5 * 5 + 5 * 5);

				Primitive sphere = new Primitive(
						new SphereShape(0.4, Arrays.asList(new TranslationTransform(x, 0d, z))), new LambertianBDRF(
								new ConstantTexture(new RGBSpectrum(RGB.fromHSL(hue, saturation, 0.5d))), 2d));
				world.getPrimitives().add(sphere);
			}
		}

		Primitive plane = new Primitive(new PlaneShape(Arrays.asList(new TranslationTransform(0d, -3d, 0d))),
				new LambertianBDRF(new CheckerboardTexture(new ConstantTexture(new RGBSpectrum(RGB.RED)),
						new ConstantTexture(new RGBSpectrum(RGB.WHITE).multiply(0.05))), 1000d));
		world.getPrimitives().add(plane);

		Light light = new PointLight(new RGBSpectrum(RGB.WHITE.multiply(64d)),
				Arrays.asList(new TranslationTransform(0d, 5d, 0d)));

		world.getLights().add(light);

		Sampler sampler = new StratifiedSampler(800, 600, 1);

		Camera camera = new PinholeCamera(800, 600, 4d, 3d, new Point(0, 1.5, -8), new Point(0, 0, 0), Vector.J, 5d);

		SimpleImageFilm film = new SimpleImageFilm(800, 600, sampler);

		SimpleWhittedIntegrator integrator = new SimpleWhittedIntegrator(camera, film, sampler, 4);

		Global.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
				() -> System.out.println(String.format("[%TT] (%,12d) --> [%,12d] --> {%,12d} --> (%,12d)", new Date(),
						sampler.totalSamples(), Global.EXECUTOR.getQueuedSubmissionCount(),
						integrator.countSamplesCurrentlyRendering(), film.countSamplesAdded())),
				1, 1, TimeUnit.SECONDS);
		Global.SCHEDULED_EXECUTOR.scheduleAtFixedRate(
				() -> System.out
						.println("[  TIME  ] ( TOT SAMPLE ) --> [ RENDR WAIT ] --> { RENDR ACTV } --> ( RESULT SAV )"),
				0, 60, TimeUnit.SECONDS);

		integrator.render(world);

		while (film.countSamplesAdded() < sampler.totalSamples()) {
			// Do nothing.
		}

		//
		// Wait a bit for everything to empty out ...
		//
		Global.SCHEDULED_EXECUTOR.schedule(() -> {
			//
			// Remember to shut down the global executors!
			System.out.println("Shutting down worker threads ...");
			// Global.EXECUTOR.shutdown();
			Global.SCHEDULED_EXECUTOR.shutdown();

			System.out.println("Writing image to file ...");
			film.writeImage(new File("render.png"));

			System.out.println("Done!");
		}, 3, TimeUnit.SECONDS);
	}

}
