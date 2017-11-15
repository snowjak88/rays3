package org.snowjak.rays3;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.math3.util.FastMath;
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
import org.snowjak.rays3.light.Light;
import org.snowjak.rays3.light.PointLight;
import org.snowjak.rays3.sample.Sampler;
import org.snowjak.rays3.sample.SimplePseudorandomSampler;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.RGBSpectrum;
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
						new SphereShape(0.5, Arrays.asList(new TranslationTransform(x, 0d, z))), new LambertianBDRF(
								new ConstantTexture(new RGBSpectrum(RGB.fromHSL(hue, saturation, 0.5d))), 100d));
				world.getPrimitives().add(sphere);
			}
		}

		Light light = new PointLight(new RGBSpectrum(RGB.WHITE.multiply(10d)),
				Arrays.asList(new TranslationTransform(0d, 5d, 0d)));

		world.getLights().add(light);

		Camera camera = new PinholeCamera(3.2d, 2.4d, new Point(0, 1.5, -3), new Point(0, 0, 0), Vector.J, 1d);

		SimpleImageFilm film = new SimpleImageFilm(800, 600);

		Sampler sampler = new SimplePseudorandomSampler(800, 600, 1);

		AbstractIntegrator integrator = new SimpleWhittedIntegrator(camera, film, sampler, 4);
		integrator.render(world);

		while (!integrator.isFinishedGettingSamples()) {
			// Do nothing.
		}
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
