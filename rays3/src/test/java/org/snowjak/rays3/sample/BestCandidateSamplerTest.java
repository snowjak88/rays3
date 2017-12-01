package org.snowjak.rays3.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.film.Film;

public class BestCandidateSamplerTest {

	private BestCandidateSampler	sampler;
	private short[][]				film;

	@Before
	public void setUp() throws Exception {

		sampler = new BestCandidateSampler(0, 0, 15, 7, 3);
		film = new short[sampler.getFilmSizeX()][sampler.getFilmSizeY()];
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

		Sample sample = sampler.getNextSample().orElse(null);
		assertNotNull("First sample was null!", sample);

		while (sample != null) {

			final double imageX = sample.getImageX();
			final double imageY = sample.getImageY();
			final int filmX = Film.convertContinuousToDiscrete(imageX);
			final int filmY = Film.convertContinuousToDiscrete(imageY);

			assertTrue("Film-X (" + filmX + ") out of bounds!", ( filmX >= 0 ) && ( filmX < film.length ));
			assertTrue("Film-Y (" + filmY + ") out of bounds!", ( filmY >= 0 ) && ( filmY < film[0].length ));

			film[filmX][filmY]++;

			sample = sampler.getNextSample().orElse(null);
		}

		for (int i = sampler.getMinFilmX(); i <= sampler.getMaxFilmX(); i++)
			for (int j = sampler.getMinFilmY(); j <= sampler.getMaxFilmY(); j++) {
				assertEquals("Film-location at [" + i + "," + j + "] not visited expected number of times!",
						sampler.getSamplesPerPixel(), film[i][j]);
			}
	}

	@Test
	public void testGetNextSample_subSamplers() {

		for (int i = sampler.getMinFilmX(); i <= sampler.getMaxFilmX(); i++)
			for (int j = sampler.getMinFilmY(); j <= sampler.getMaxFilmY(); j++) {
				film[i][j] = 0;
			}

		assertTrue("Sampler must be divisible into sub-samplers!", sampler.hasSubSamplers());
		Collection<Sampler> subSamplers = sampler.getSubSamplers();

		for (Sampler subSampler : subSamplers) {

			Sample sample = subSampler.getNextSample().orElse(null);
			assertNotNull("First sample was null!", sample);

			while (sample != null) {

				final double imageX = sample.getImageX();
				final double imageY = sample.getImageY();
				final int filmX = Film.convertContinuousToDiscrete(imageX);
				final int filmY = Film.convertContinuousToDiscrete(imageY);

				assertTrue("Film-X (" + filmX + ") out of bounds!", ( filmX >= 0 ) && ( filmX < film.length ));
				assertTrue("Film-Y (" + filmY + ") out of bounds!", ( filmY >= 0 ) && ( filmY < film[0].length ));

				film[filmX][filmY]++;

				sample = subSampler.getNextSample().orElse(null);
			}
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

}
