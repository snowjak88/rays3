package org.snowjak.rays3.light;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.light.Light.FalloffType;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.transform.TranslationTransform;

public class LightTest {

	@Test
	public void testGetPower() {

		Light light = new Light(new RGBSpectrum(RGB.GREEN), Collections.emptyList()) {

			@Override
			public Vector sampleLightVector(Point towards) {

				// don't care about this for this test
				return null;
			}

			@Override
			public double probabilitySampleVector(Point towards, Vector sampledVector) {

				// don't care about this for this test
				return 0;
			}
		};

		Spectrum power = light.getPower();

		assertEquals("Power-Red was not as expected!", 0d, power.toRGB().getRed(), 0.00001);
		assertEquals("Power-Green was not as expected!", 12.5663706d, power.toRGB().getGreen(), 0.00001);
		assertEquals("Power-Blue was not as expected!", 0d, power.toRGB().getBlue(), 0.00001);
	}

	@Test
	public void testGetRadianceAt() {

		Light light = new Light(new RGBSpectrum(RGB.WHITE), Arrays.asList(new TranslationTransform(3d, 1d, 3d))) {

			@Override
			public Vector sampleLightVector(Point towards) {

				// don't care about this for this test
				return null;
			}

			@Override
			public double probabilitySampleVector(Point towards, Vector sampledVector) {

				// don't care about this for this test
				return 0;
			}
		};

		final double distanceSq = new Vector(3d, 1d, 3d).getMagnitudeSquared();
		final double expectedFalloff = 1d / distanceSq;
		final double expectedCosine = new Vector(0, 1, 0).dotProduct(new Vector(3, 1, 3).normalize());

		Spectrum radiance = light.getRadianceAt(new Vector(0, 0, 0).subtract(new Vector(3, 1, 3)), new Normal(0, 1, 0));

		assertEquals(1d * expectedFalloff * expectedCosine, radiance.toRGB().getRed(), 0.00001);
	}

	@Test
	public void testGetFalloff() {

		assertEquals(1d, FalloffType.CONSTANT.calculate(5d), 0.00001);
		assertEquals(1d / 5d, FalloffType.LINEAR.calculate(5d), 0.00001);
		assertEquals(1d / 25d, FalloffType.QUADRATIC.calculate(5d), 0.00001);
	}

}
