package org.snowjak.rays3.light;

import static org.apache.commons.math3.util.FastMath.sqrt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;
import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.sample.StratifiedSampler;
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

		final Sample sample = new Sample(new StratifiedSampler(16, 16, 4), 0, 0);
		final Point towardsPoint = new Point(0, 0, 0);
		final Vector lightZero = new Vector(light.getObjectZero());

		for (int i = 0; i < 64; i++) {

			final Vector sampleV = light.sampleLightVector(towardsPoint, sample);
			final Vector sampleReverse = new Vector(towardsPoint).subtract(sampleV);
			final Vector sampleLocal = sampleReverse.subtract(lightZero);

			assertEquals("Light sampled point ( " + Double.toString(sampleLocal.getX()) + ", "
					+ Double.toString(sampleLocal.getY()) + ", " + Double.toString(sampleLocal.getZ())
					+ ") is not on its surface!", 0.5, sampleLocal.getMagnitude(), 0.00001);

			assertTrue(
					"Light sampled point is not on hemisphere toward target point (distance = "
							+ Double.toString(sampleV.getMagnitude()) + ")!",
					( sampleV.getMagnitude() >= 2.5 ) && ( sampleV.getMagnitude() <= sqrt(0.5 * 0.5 + 3.0 * 3.0) ));
		}
	}

	@Test
	public void testProbabilitySampleVector() {

		assertEquals("Probability for spherical light should always be 1 / (2 * PI)!", 1d / ( 2d * FastMath.PI ),
				light.probabilitySampleVector(null, null, null), 0.00001);
	}

}
