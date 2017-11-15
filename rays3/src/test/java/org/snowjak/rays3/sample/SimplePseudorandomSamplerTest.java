package org.snowjak.rays3.sample;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SimplePseudorandomSamplerTest {

	private SimplePseudorandomSampler sampler;

	@Before
	public void setUp() throws Exception {

		sampler = new SimplePseudorandomSampler(128, 128);
	}

	@Test
	public void testGetNextSample() {

		boolean[][] filmLocations = new boolean[sampler.getFilmSizeX()][sampler.getFilmSizeY()];
		for (int r = 0; r < filmLocations.length; r++)
			for (int c = 0; c < filmLocations[0].length; c++)
				filmLocations[r][c] = false;

		Sample sample = sampler.getNextSample();
		assertNotNull("Expected first sample to be other than null!", sample);

		while (sample != null) {

			int filmX = new Double(sample.getFilmX()).intValue(), filmY = new Double(sample.getFilmY()).intValue();
			assertTrue("Film-X is not within bounds!", ( filmX >= 0 ) && ( filmX < filmLocations.length ));
			assertTrue("Film-Y is not within bounds!", ( filmY >= 0 ) && ( filmY < filmLocations[0].length ));

			filmLocations[filmX][filmY] = true;

			double filmXFrac = ( (double) filmX ) / ( (double) filmLocations.length );
			double filmYFrac = 1d - ( (double) filmY ) / ( (double) filmLocations[0].length );
			assertEquals("Image-plane-U is not as expected!", filmXFrac, sample.getImageU(),
					1d / ( (double) sampler.getFilmSizeX() ));
			assertEquals("Image-plane-V is not as expected!", filmYFrac, sample.getImageV(),
					1d / ( (double) sampler.getFilmSizeY() ));

			assertTrue("Lens-U is out of bounds!", ( sample.getLensU() >= 0d ) && ( sample.getLensU() <= 1d ));
			assertTrue("Lens-V is out of bounds!", ( sample.getLensV() >= 0d ) && ( sample.getLensV() <= 1d ));

			sample = sampler.getNextSample();
		}

		for (int r = 0; r < filmLocations.length; r++)
			for (int c = 0; c < filmLocations[0].length; c++)
				assertTrue("Film-location [" + r + ", " + c + "] was never visited!", filmLocations[r][c]);
	}

	@Test
	public void testIsSampleAcceptable() {

		assertTrue("Sample should be always acceptable for Simple Samplers", sampler.isSampleAcceptable(null, null));
	}

}
