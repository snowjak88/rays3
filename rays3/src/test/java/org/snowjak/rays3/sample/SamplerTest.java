package org.snowjak.rays3.sample;

import static org.junit.Assert.*;

import org.junit.Test;

public class SamplerTest {

	@Test
	public void testMapXToU() {

		assertEquals("5 between 0 and 10 not as expected!", 0.5, Sampler.mapXToU(5, 0, 10), 0.00001);
		assertEquals("3 between 0 and 5 not as expected!", 0.6, Sampler.mapXToU(3, 0, 5), 0.00001);
		assertEquals("2 between 1 and 3 not as expected!", 0.5, Sampler.mapXToU(2, 1, 3), 0.00001);
	}

}
