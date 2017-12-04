package org.snowjak.rays3;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.snowjak.rays3.bxdf.LambertianBRDF;
import org.snowjak.rays3.bxdf.PerfectSpecularBRDF;
import org.snowjak.rays3.camera.Camera;
import org.snowjak.rays3.camera.PinholeCamera;
import org.snowjak.rays3.film.SimpleImageFilm;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.geometry.shape.PlaneShape;
import org.snowjak.rays3.geometry.shape.Primitive;
import org.snowjak.rays3.geometry.shape.SphereShape;
import org.snowjak.rays3.integrator.AbstractIntegrator;
import org.snowjak.rays3.integrator.SimplePathTracingIntegrator;
import org.snowjak.rays3.sample.BestCandidateSampler;
import org.snowjak.rays3.sample.Sampler;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.texture.CheckerboardTexture;
import org.snowjak.rays3.texture.ConstantTexture;
import org.snowjak.rays3.transform.RotationTransform;
import org.snowjak.rays3.transform.TranslationTransform;

public class Main {

	public static void main(String[] args) {

		final int imageSizeX = 400;
		final int imageSizeY = 300;

		//
		//
		//
		final Sampler sampler = new BestCandidateSampler(0, 0, imageSizeX - 1, imageSizeY - 1, 4);

		System.out.println(String.format("Pre-generating %,d samples ...", sampler.totalSamples()));
		Collection<Sampler> subSamplers = sampler.recursivelySubdivide(2);

		final CountDownLatch pregenerateComplete = new CountDownLatch(subSamplers.size());

		subSamplers.forEach(s -> Global.RENDER_EXECUTOR.submit(() -> {
			s.pregenerateSamples();
			pregenerateComplete.countDown();
		}));

		final Future<?> pregenerateMessage = Global.SCHEDULED_EXECUTOR
				.scheduleWithFixedDelay(
						() -> System.out.println(String.format("%,d samples pregenerated ...",
								subSamplers
										.stream()
											.collect(Collectors.summingInt(s -> s.countSamplesPregenerated())))),
						1, 1, TimeUnit.SECONDS);

		try {
			pregenerateComplete.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return;
		}

		pregenerateMessage.cancel(false);
		System.out.println("Done pre-generating samples!");
		//
		//
		//

		final Camera camera = new PinholeCamera(imageSizeX, imageSizeY, 4d, 3d, new Point(0, 1, -5), new Point(0, 0, 0),
				Vector.J, 5d);

		final SimpleImageFilm film = new SimpleImageFilm(imageSizeX, imageSizeY, false);

		final AbstractIntegrator integrator = new SimplePathTracingIntegrator(camera, film, subSamplers, 4);

		//
		//
		//
		final World world = new World();

		Primitive sphere = new Primitive(new SphereShape(1.0, Arrays.asList(new TranslationTransform(-1.5, 0, 0))),
				new PerfectSpecularBRDF());
		world.getPrimitives().add(sphere);

		sphere = new Primitive(new SphereShape(1.0, Arrays.asList(new TranslationTransform(+1.5, 0, 0))),
				new LambertianBRDF(new ConstantTexture(RGBSpectrum.WHITE)));
		world.getPrimitives().add(sphere);

		//

		Primitive plane = new Primitive(new PlaneShape(Arrays.asList(new TranslationTransform(0d, -1d, 0d))),
				new LambertianBRDF(new CheckerboardTexture(new ConstantTexture(RGBSpectrum.BLACK),
						new ConstantTexture(RGBSpectrum.WHITE))));
		world.getPrimitives().add(plane);

		plane = new Primitive(
				new PlaneShape(
						Arrays.asList(new TranslationTransform(0, 0d, 2.5d), new RotationTransform(Vector.I, -90d))),
				new LambertianBRDF(new ConstantTexture(RGBSpectrum.WHITE)));
		world.getPrimitives().add(plane);

		plane = new Primitive(
				new PlaneShape(
						Arrays.asList(new TranslationTransform(+3, 0, 0), new RotationTransform(Vector.K, +90d))),
				new LambertianBRDF(new ConstantTexture(new RGBSpectrum(RGB.RED))));
		world.getPrimitives().add(plane);

		plane = new Primitive(
				new PlaneShape(
						Arrays.asList(new TranslationTransform(-3, 0, 0), new RotationTransform(Vector.K, -90d))),
				new LambertianBRDF(new ConstantTexture(new RGBSpectrum(RGB.BLUE))));
		world.getPrimitives().add(plane);

		plane = new Primitive(
				new PlaneShape(
						Arrays.asList(new TranslationTransform(0d, +5.0d, 0d), new RotationTransform(Vector.I, 180d))),
				new LambertianBRDF(new ConstantTexture(RGBSpectrum.WHITE)));
		world.getPrimitives().add(plane);

		sphere = new Primitive(new SphereShape(0.5, Arrays.asList(new TranslationTransform(0, 5, 0))),
				new LambertianBRDF(new ConstantTexture(RGBSpectrum.WHITE), RGBSpectrum.WHITE.multiply(32d)));
		world.getPrimitives().add(sphere);

		//
		//
		//
		Global.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
				() -> System.out.println(String.format("[%TT] (%,12d) --> [%,12d] --> {%,6d} --> (%,12d)", new Date(),
						( sampler.totalSamples() ), ( integrator.countSamplesWaitingToRender() ),
						( integrator.countSamplesCurrentlyRendering() ), film.countSamplesAdded())),
				1, 10, TimeUnit.SECONDS);
		Global.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
				() -> System.out
						.println("[  TIME  ] ( TOT SAMPLE ) --> [ SAMPL WAIT ] --> { ACTV } --> ( RESULT SAV )"),
				0, 60, TimeUnit.SECONDS);

		integrator.render(world);

		//
		//
		// Use a CountDownLatch, coupled with a scheduled checker-thread, to
		// wait this main thread until rendering is complete.
		//
		final CountDownLatch awaitUntilDone = new CountDownLatch(1);
		//
		// This is the scheduled checker-thread. It will signal the
		// CountDownLatch when the given condition is reached.
		//
		Global.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(() -> {
			if (integrator.isFinishedRenderingSamples())
				awaitUntilDone.countDown();
		}, 1, 1, TimeUnit.SECONDS);

		//
		// And here we wait until that Latch is signaled.
		//
		try {
			awaitUntilDone.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//
		// Wait a bit for everything to empty out ...
		//
		Global.SCHEDULED_EXECUTOR.schedule(() -> {
			//
			// Remember to shut down the global executors!
			System.out.println("Shutting down worker threads ...");
			//
			Global.RENDER_EXECUTOR.shutdown();
			Global.SCHEDULED_EXECUTOR.shutdown();

			System.out.println("Writing image to file ...");
			film.writeImage(new File("render.png"));

			System.out.println("Done!");
		}, 3, TimeUnit.SECONDS);
	}

}
