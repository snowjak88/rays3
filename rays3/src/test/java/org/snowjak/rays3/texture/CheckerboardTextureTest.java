package org.snowjak.rays3.texture;

import static org.junit.Assert.*;

import org.junit.Test;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.RGBSpectrum;

public class CheckerboardTextureTest {

	@Test
	public void testDetermineTextureIndex() {

		final CheckerboardTexture texture = new CheckerboardTexture(new ConstantTexture(new RGBSpectrum(RGB.BLUE)),
				new ConstantTexture(new RGBSpectrum(RGB.GREEN)));

		assertEquals("Checkerboard for (0.3, -0.2) was not as expected!", 0,
				texture.determineTextureIndex(new Point2D(0.3, -0.2)));
		assertEquals("Checkerboard for (0.6, 0.1) was not as expected!", 1,
				texture.determineTextureIndex(new Point2D(0.6, 0.1)));
		assertEquals("Checkerboard for (1.6, 1.7) was not as expected!", 0,
				texture.determineTextureIndex(new Point2D(1.6, 1.7)));
		assertEquals("Checkerboard for (1.7, 0.6) was not as expected!", 1,
				texture.determineTextureIndex(new Point2D(1.7, 0.6)));
	}

}
