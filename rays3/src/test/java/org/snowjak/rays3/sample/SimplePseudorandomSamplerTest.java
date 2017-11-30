package org.snowjak.rays3.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.film.Film;

public class SimplePseudorandomSamplerTest {

	private SimplePseudorandomSampler sampler;

	@Before
	public void setUp() throws Exception {

		sampler = new SimplePseudorandomSampler(128, 128, 2);
	}

	@Test
	public void testGetNextSample() {

		final short[][] filmLocations = new short[sampler.getFilmSizeX()][sampler.getFilmSizeY()];
		for (int r = 0; r < filmLocations.length; r++)
			for (int c = 0; c < filmLocations[0].length; c++)
				filmLocations[r][c] = 0;

		Sample sample = sampler.getNextSample().orElse(null);
		assertNotNull("Expected first sample to be other than null!", sample);

		while (sample != null) {

			final int filmX = Film.convertContinuousToDiscrete(sample.getImageX()),
					filmY = Film.convertContinuousToDiscrete(sample.getImageY());
			assertTrue("Film-X (" + filmX + ") is not within bounds [" + 0 + ", " + filmLocations.length + ")",
					( filmX >= 0 ) && ( filmX < filmLocations.length ));
			assertTrue("Film-Y (" + filmY + ") is not within bounds [" + 0 + ", " + filmLocations[0].length + ")",
					( filmY >= 0 ) && ( filmY < filmLocations[0].length ));

			filmLocations[filmX][filmY]++;

			assertTrue("Lens-U is out of bounds!", ( sample.getLensU() >= 0d ) && ( sample.getLensU() <= 1d ));
			assertTrue("Lens-V is out of bounds!", ( sample.getLensV() >= 0d ) && ( sample.getLensV() <= 1d ));

			sample = sampler.getNextSample().orElse(null);
		}

		for (int r = 0; r < filmLocations.length; r++)
			for (int c = 0; c < filmLocations[0].length; c++)
				assertEquals("Film-location [" + r + ", " + c + "] was not visited the expected number of times!", 2,
						filmLocations[r][c]);
	}

	@Test
	public void testIsSampleAcceptable() {

		assertTrue("Sample should be always acceptable for Simple Samplers", sampler.isSampleAcceptable(null, null));
	}

}
