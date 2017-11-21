package org.snowjak.rays3.light;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.snowjak.rays3.World;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.geometry.shape.PlaneShape;
import org.snowjak.rays3.geometry.shape.Primitive;
import org.snowjak.rays3.geometry.shape.SphereShape;
import org.snowjak.rays3.light.Light.FalloffType;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.transform.TranslationTransform;

public class LightTest {

	@Test
	public void testGetPower() {

		final Light light = new Light(new RGBSpectrum(RGB.GREEN), Collections.emptyList()) {

			@Override
			public Vector sampleLightVector(Point towards, Sample sample) {

				// don't care about this for this test
				return null;
			}

			@Override
			public double probabilitySampleVector(Point towards, Vector sampledVector, Sample sample) {

				// don't care about this for this test
				return 0;
			}
		};

		final Spectrum power = light.getPower();

		assertEquals("Power-Red was not as expected!", 0d, power.toRGB().getRed(), 0.00001);
		assertEquals("Power-Green was not as expected!", 12.5663706d, power.toRGB().getGreen(), 0.00001);
		assertEquals("Power-Blue was not as expected!", 0d, power.toRGB().getBlue(), 0.00001);
	}

	@Test
	public void testGetRadianceAt() {

		final Light light = new Light(new RGBSpectrum(RGB.WHITE), Arrays.asList(new TranslationTransform(3d, 1d, 3d))) {

			@Override
			public Vector sampleLightVector(Point towards, Sample sample) {

				// don't care about this for this test
				return null;
			}

			@Override
			public double probabilitySampleVector(Point towards, Vector sampledVector, Sample sample) {

				// don't care about this for this test
				return 0;
			}
		};

		final double distanceSq = new Vector(3d, 1d, 3d).getMagnitudeSquared();
		final double expectedFalloff = 1d / distanceSq;
		final double expectedCosine = new Vector(0, 1, 0).dotProduct(new Vector(3, 1, 3).normalize());

		final Spectrum radiance = light.getRadianceAt(new Vector(0, 0, 0).subtract(new Vector(3, 1, 3)),
				new Normal(0, 1, 0));

		assertEquals(1d * expectedFalloff * expectedCosine, radiance.toRGB().getRed(), 0.00001);
	}

	@Test
	public void testGetFalloff() {

		assertEquals(1d, FalloffType.CONSTANT.calculate(5d), 0.00001);
		assertEquals(1d / 5d, FalloffType.LINEAR.calculate(5d), 0.00001);
		assertEquals(1d / 25d, FalloffType.QUADRATIC.calculate(5d), 0.00001);
	}

	@Test
	public void testIsVisibleFrom() {

		final World world = new World();

		world.getPrimitives().add(
				new Primitive(new SphereShape(0.5, Arrays.asList(new TranslationTransform(2, 2, 0))), null));
		world.getPrimitives().add(new Primitive(new PlaneShape(), null));

		assertFalse("Expected non-visibility is really visible!",
				Light.isVisibleFrom(world, new Point(2, 0, 0), new Point(2, 4, 0)));
		assertFalse("Expected non-visibility is really visible!",
				Light.isVisibleFrom(world, new Point(2, 1, 0), new Point(2, 4, 0)));
		assertFalse("Expected non-visibility is really visible!",
				Light.isVisibleFrom(world, new Point(2, 2, 0), new Point(2, 4, 0)));
		assertTrue("Expected visibility is really not visible!",
				Light.isVisibleFrom(world, new Point(-2, 4, 0), new Point(2, 4, 0)));
	}

	@Test
	public void testGetLightSurfacePoint() {

		final Point from = new Point(0, 3, 0);
		final Point lightPoint = new Point(6, 7, 2);
		final Vector sampleVector = new Vector(from).subtract(new Vector(lightPoint));

		final Point calculatedLightPoint = Light.getLightSurfacePoint(from, sampleVector);

		assertEquals("Light-point-X not as expected!", lightPoint.getX(), calculatedLightPoint.getX(), 0.00001);
		assertEquals("Light-point-Y not as expected!", lightPoint.getY(), calculatedLightPoint.getY(), 0.00001);
		assertEquals("Light-point-Z not as expected!", lightPoint.getZ(), calculatedLightPoint.getZ(), 0.00001);
	}

}
