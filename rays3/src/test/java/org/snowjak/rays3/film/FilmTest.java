package org.snowjak.rays3.film;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FilmTest {

	@Test
	public void testConvertContinuousToDiscrete() {

		assertEquals(1, Film.convertContinuousToDiscrete(1.5d));
		assertEquals(5, Film.convertContinuousToDiscrete(5.0d));
		assertEquals(8, Film.convertContinuousToDiscrete(8.999d));
	}

	@Test
	public void testConvertDiscreteToContinuous() {

		assertEquals(1.5d, Film.convertDiscreteToContinuous(1), 0.00001);
		assertEquals(5.5d, Film.convertDiscreteToContinuous(5), 0.00001);
		assertEquals(8.5d, Film.convertDiscreteToContinuous(8), 0.00001);
	}

}
