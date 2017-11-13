package org.snowjak.rays3.spectrum;

import static org.junit.Assert.*;

import org.junit.Test;

public class RGBTest {

	@Test
	public void testFromHSL() {

		final RGB cyan = RGB.fromHSL(180d, 1d, 0.5);
		assertEquals("Expected HSL(Cyan)-Red not as expected!", 0d, cyan.getRed(), 0.00001);
		assertEquals("Expected HSL(Cyan)-Green not as expected!", 1d, cyan.getGreen(), 0.00001);
		assertEquals("Expected HSL(Cyan)-Blue not as expected!", 1d, cyan.getBlue(), 0.00001);

		final RGB orange = RGB.fromHSL(30d, 1d, 0.5);
		assertEquals("Expected HSL(Orange)-Red not as expected!", 1d, orange.getRed(), 0.00001);
		assertEquals("Expected HSL(Orange)-Green not as expected!", 0.5d, orange.getGreen(), 0.00001);
		assertEquals("Expected HSL(Orange)-Blue not as expected!", 0d, orange.getBlue(), 0.00001);

		final RGB purple = RGB.fromHSL(270d, 0.5d, 0.5);
		assertEquals("Expected HSL(Purple)-Red not as expected!", 0.5d, purple.getRed(), 0.00001);
		assertEquals("Expected HSL(Purple)-Green not as expected!", 0.25d, purple.getGreen(), 0.00001);
		assertEquals("Expected HSL(Purple)-Blue not as expected!", 0.75d, purple.getBlue(), 0.00001);
	}

}
