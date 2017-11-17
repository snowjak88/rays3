package org.snowjak.rays3.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.film.Film;
import org.snowjak.rays3.geometry.Point2D;

public class StratifiedSamplerTest {

	private StratifiedSampler	sampler;
	private short[][]			film;

	@Before
	public void setUp() throws Exception {

		sampler = new StratifiedSampler(640, 480, 3);
		film = new short[640][480];
	}

	public void testPixelSampleCount() {

		assertEquals(4, sampler.getSamplesPerPixel());
	}

	@Test
	public void testGetNextSample() {

		for (int i = sampler.getMinFilmX(); i <= sampler.getMaxFilmX(); i++)
			for (int j = sampler.getMinFilmY(); j <= sampler.getMaxFilmY(); j++) {
				film[i][j] = 0;
			}

		Sample sample = sampler.getNextSample();
		assertNotNull("First sample was null!", sample);

		while (sample != null) {

			final double imageX = sample.getImageX();
			final double imageY = sample.getImageY();
			final int filmX = Film.convertContinuousToDiscrete(imageX);
			final int filmY = Film.convertContinuousToDiscrete(imageY);

			assertTrue("Film-X (" + filmX + ") out of bounds!", ( filmX >= 0 ) && ( filmX < film.length ));
			assertTrue("Film-Y (" + filmY + ") out of bounds!", ( filmY >= 0 ) && ( filmY < film[0].length ));

			film[filmX][filmY]++;

			sample = sampler.getNextSample();
		}

		for (int i = sampler.getMinFilmX(); i <= sampler.getMaxFilmX(); i++)
			for (int j = sampler.getMinFilmY(); j <= sampler.getMaxFilmY(); j++) {
				assertEquals("Film-location at [" + i + "," + j + "] not visited expected number of times!",
						sampler.getSamplesPerPixel(), film[i][j]);
			}
	}

	@Test
	public void testIsSampleAcceptable() {

		assertTrue("Expect this to always be true!", sampler.isSampleAcceptable(null, null));
	}

	@Test
	public void testGenerate1DStratum() {

		double[] stratum = StratifiedSampler.generate1DStratum(8);

		assertEquals("Stratum length not as expected!", 8, stratum.length);

		for (int i = 0; i < 8; i++) {

			final double value = stratum[i];
			final double minValue = (double) i / 8d, maxValue = (double) i / 8d + 1d;
			assertTrue(
					"Stratum element (" + Double.toString(value) + ") is not within expected bounds ("
							+ Double.toString(minValue) + ", " + Double.toString(maxValue) + ").",
					( value >= minValue ) && ( value <= maxValue ));
		}
	}

	@Test
	public void testGenerate2DStratum() {

		Point2D[][] stratum = StratifiedSampler.generate2DStratum(8);

		assertEquals("Stratum length not as expected!", 8, stratum.length);
		assertEquals("Stratum[0] length not as expected!", 8, stratum[0].length);

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {

				final Point2D value = stratum[i][j];
				final double minValueX = (double) i / 8d, maxValueX = (double) i / 8d + 1d;
				final double minValueY = (double) j / 8d, maxValueY = (double) j / 8d + 1d;

				assertTrue(
						"Stratum element [" + i + "," + j + "].X = (" + Double.toString(value.getX())
								+ ") is not within expected bounds (" + Double.toString(minValueX) + ", "
								+ Double.toString(maxValueY) + ").",
						( value.getX() >= minValueX ) && ( value.getX() <= maxValueX ));
				assertTrue(
						"Stratum element [" + i + "," + j + "].Y = (" + Double.toString(value.getY())
								+ ") is not within expected bounds (" + Double.toString(minValueY) + ", "
								+ Double.toString(maxValueY) + ").",
						( value.getY() >= minValueY ) && ( value.getY() <= maxValueY ));
			}
		}
	}

}
