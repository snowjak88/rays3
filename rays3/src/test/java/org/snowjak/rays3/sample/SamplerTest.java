package org.snowjak.rays3.sample;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;
import org.snowjak.rays3.spectrum.Spectrum;

public class SamplerTest {

	@Test
	public void testMapXToU() {

		assertEquals("5 between 0 and 10 not as expected!", 0.5, Sampler.mapXToU(5, 0, 10), 0.00001);
		assertEquals("3 between 0 and 5 not as expected!", 0.6, Sampler.mapXToU(3, 0, 5), 0.00001);
		assertEquals("2 between 1 and 3 not as expected!", 0.5, Sampler.mapXToU(2, 1, 3), 0.00001);
	}

	@Test
	public void testRecursivelySubdivide() {

		Sampler sampler = new SamplerImpl(0, 0, 16, 16, 1);

		Collection<Sampler> subSamplers = sampler.recursivelySubdivide(4);

		assertEquals("Recursively-subdivided Sampler didn't return expected number of sub-Samplers!", 16,
				subSamplers.size());
	}

	private static class SamplerImpl extends Sampler {

		public SamplerImpl(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY, int samplesPerPixel) {
			super(minFilmX, minFilmY, maxFilmX, maxFilmY, samplesPerPixel);
		}

		@Override
		protected Sampler splitSubSampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY) {

			return new SamplerImpl(minFilmX, minFilmY, maxFilmX, maxFilmY, getSamplesPerPixel());
		}

		@Override
		protected Sample generateNextSample() {

			// We don't care about this method for the purposes of these tests.
			return null;
		}

		@Override
		public boolean isSampleAcceptable(Sample sample, Spectrum result) {

			// We don't care about this method for the purposes of these tests.
			return false;
		}

	}

}
