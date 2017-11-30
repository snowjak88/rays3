package org.snowjak.rays3;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
import org.snowjak.rays3.integrator.MonteCarloImportanceIntegrator;
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
		final int imageSliceSpanX = ( imageSizeX - 1 ) / 2;

		final int x0 = 0, x1 = 1 * ( imageSliceSpanX ) - 1, x2 = ( imageSizeX - 1 );
		final int y0 = 0, y1 = ( imageSizeY - 1 );

		final Sampler sampler1 = new BestCandidateSampler(x0, y0, x1, y1, 4);
		final Sampler sampler2 = new BestCandidateSampler(x1 + 1, y0, x2, y1, 4);

		final Camera camera = new PinholeCamera(imageSizeX, imageSizeY, 4d, 3d, new Point(0, 1, -5), new Point(0, 0, 0),
				Vector.J, 5d);

		final SimpleImageFilm film = new SimpleImageFilm(imageSizeX, imageSizeY, false);

		final AbstractIntegrator integrator1 = new MonteCarloImportanceIntegrator(camera, film, sampler1, 4, 4);
		final AbstractIntegrator integrator2 = new MonteCarloImportanceIntegrator(camera, film, sampler2, 4, 4);

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
						( sampler1.totalSamples() + sampler2.totalSamples() ),
						( integrator1.countSamplesWaitingToRender() + integrator2.countSamplesWaitingToRender() ),
						( integrator1.countSamplesCurrentlyRendering() + integrator2.countSamplesCurrentlyRendering() ),
						film.countSamplesAdded())),
				1, 10, TimeUnit.SECONDS);
		Global.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
				() -> System.out
						.println("[  TIME  ] ( TOT SAMPLE ) --> [ RENDR WAIT ] --> { ACTV } --> ( RESULT SAV )"),
				0, 60, TimeUnit.SECONDS);

		integrator1.render(world);
		integrator2.render(world);

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
			if (integrator1.isFinishedRenderingSamples() && integrator2.isFinishedRenderingSamples())
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
			// We need not explicitly shut down the batch-oriented EXECUTOR, a
			// ForkJoinPool; those threads hosted by a ForkJoinPool are, as per
			// the documentation, daemon threads, and are automatically stopped
			// when the main thread exits.
			//
			// So we need only shut down the SCHEDULED_EXECUTOR.
			Global.SCHEDULED_EXECUTOR.shutdown();

			System.out.println("Writing image to file ...");
			film.writeImage(new File("render.png"));

			System.out.println("Done!");
		}, 3, TimeUnit.SECONDS);
	}

}
