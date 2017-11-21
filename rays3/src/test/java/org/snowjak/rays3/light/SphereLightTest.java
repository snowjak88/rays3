package org.snowjak.rays3.light;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.transform.TranslationTransform;

public class SphereLightTest {

	private Light light;

	@Before
	public void setUp() throws Exception {

		this.light = new SphereLight(new RGBSpectrum(RGB.WHITE), Arrays.asList(new TranslationTransform(0, 3, 0)), 0.5);
	}

	@Test
	public void testSampleLightVector() {

		final Point towardsPoint = new Point(2, 0, 0);
		final Vector lightZero = new Vector(light.getObjectZero());

		for (int i = 0; i < 64; i++) {

			final Vector sample = light.sampleLightVector(towardsPoint);
			final Vector sampleReverse = new Vector(towardsPoint).subtract(sample);
			final Vector sampleLocal = sampleReverse.subtract(lightZero);

			assertEquals("Light sampled point ( " + Double.toString(sampleLocal.getX()) + ", "
					+ Double.toString(sampleLocal.getY()) + ", " + Double.toString(sampleLocal.getZ())
					+ ") is not on its surface!", 0.5, sampleLocal.getMagnitude(), 0.00001);

			assertTrue("Light sampled point is not on hemisphere toward target point!",
					sampleLocal.dotProduct(new Vector(towardsPoint).subtract(lightZero)) >= 0d);
		}
	}

	@Test
	public void testProbabilitySampleVector() {

		assertEquals("Probability for spherical light should always be 1!", 1d,
				light.probabilitySampleVector(null, null), 0.00001);
	}

}
