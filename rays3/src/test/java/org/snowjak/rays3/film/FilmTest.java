package org.snowjak.rays3.film;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.Spectrum;

public class FilmTest {

	private Film film;

	@Before
	public void setUp() {

		film = new Film() {

			@Override
			public void addSample(Sample sample, Spectrum radiance) {

				// We don't care about this method for this test.
			}
		};
	}

	@Test
	public void testConvertContinuousToDiscrete() {

		assertEquals(1, film.convertContinuousToDiscrete(1.5d));
		assertEquals(5, film.convertContinuousToDiscrete(5.0d));
		assertEquals(8, film.convertContinuousToDiscrete(8.999d));
	}

	@Test
	public void testConvertDiscreteToContinuous() {

		assertEquals(1.5d, film.convertDiscreteToContinuous(1), 0.00001);
		assertEquals(5.5d, film.convertDiscreteToContinuous(5), 0.00001);
		assertEquals(8.5d, film.convertDiscreteToContinuous(8), 0.00001);
	}

}
