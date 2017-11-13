package org.snowjak.rays3;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ThreadPoolExecutor;

import org.snowjak.rays3.bxdf.LambertianBDRF;
import org.snowjak.rays3.camera.Camera;
import org.snowjak.rays3.camera.PinholeCamera;
import org.snowjak.rays3.film.SimpleImageFilm;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.geometry.shape.Primitive;
import org.snowjak.rays3.geometry.shape.SphereShape;
import org.snowjak.rays3.integrator.AbstractIntegrator;
import org.snowjak.rays3.integrator.SimpleWhittedIntegrator;
import org.snowjak.rays3.sample.Sampler;
import org.snowjak.rays3.sample.SimplePseudorandomSampler;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.texture.ConstantTexture;
import org.snowjak.rays3.transform.TranslationTransform;

public class Main {

	public static void main(String[] args) {

		World world = new World();

		Primitive sphere1 = new Primitive(new SphereShape(1d, Arrays.asList(new TranslationTransform(-2d, 0d, 0d))),
				new LambertianBDRF(new ConstantTexture(new RGBSpectrum(RGB.RED)),
						new ConstantTexture(new RGBSpectrum()), 1.1d));
		Primitive sphere2 = new Primitive(new SphereShape(1d, Arrays.asList(new TranslationTransform(+2d, 0d, 0d))),
				new LambertianBDRF(new ConstantTexture(new RGBSpectrum(RGB.BLUE)),
						new ConstantTexture(new RGBSpectrum()), 1.1d));

		Primitive sphere3 = new Primitive(new SphereShape(1d, Arrays.asList(new TranslationTransform(-2d, 0d, +2d))),
				new LambertianBDRF(new ConstantTexture(new RGBSpectrum(RGB.GREEN)),
						new ConstantTexture(new RGBSpectrum()), 100d));
		Primitive sphere4 = new Primitive(new SphereShape(1d, Arrays.asList(new TranslationTransform(+2d, 0d, +2d))),
				new LambertianBDRF(new ConstantTexture(new RGBSpectrum(RGB.GREEN)),
						new ConstantTexture(new RGBSpectrum()), 100d));

		world.getPrimitives().add(sphere1);
		world.getPrimitives().add(sphere2);
		world.getPrimitives().add(sphere3);
		world.getPrimitives().add(sphere4);

		Camera camera = new PinholeCamera(3.2d, 2.4d, new Point(0, 0, -3), new Point(0, 0, 0), Vector.J, 1d);

		SimpleImageFilm film = new SimpleImageFilm(800, 600);

		Sampler sampler = new SimplePseudorandomSampler(800, 600);

		AbstractIntegrator integrator = new SimpleWhittedIntegrator(camera, film, sampler, 4);
		integrator.render(world);

		while (( (ThreadPoolExecutor) Global.EXECUTOR ).getActiveCount() > 0) {
			// Do nothing.
		}

		System.out.println("Writing image to file ...");
		film.writeImage(new File("render.png"));

		//
		// Remember to shut down the global executor!
		Global.EXECUTOR.shutdown();
	}

}
