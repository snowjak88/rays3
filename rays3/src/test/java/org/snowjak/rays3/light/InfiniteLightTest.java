package org.snowjak.rays3.light;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.spectrum.RGBSpectrum;

public class InfiniteLightTest {

	@Test
	public void testSampleLightVector() {

		final Light light = new InfiniteLight(RGBSpectrum.WHITE, Collections.emptyList());

		final Point p1 = new Point(-3, 0, 3), p2 = new Point(5, 3, -7);

		final Vector ls1 = light.sampleLightVector(p1, null);
		final Vector ls2 = light.sampleLightVector(p2, null);

		assertEquals("Light's direction-X not as expected!", 0d, ls1.getX(), 0.00001);
		assertEquals("Light's direction-Y not as expected!", -1d, ls1.getY(), 0.00001);
		assertEquals("Light's direction-Z not as expected!", 0d, ls1.getZ(), 0.00001);

		assertEquals("Light's direction-X not as expected!", 0d, ls2.getX(), 0.00001);
		assertEquals("Light's direction-Y not as expected!", -1d, ls2.getY(), 0.00001);
		assertEquals("Light's direction-Z not as expected!", 0d, ls2.getZ(), 0.00001);
	}

	@Test
	public void testProbabilitySampleVector() {

		assertEquals("Infinite light probability should always be 1!", 1d,
				new InfiniteLight(null, Collections.emptyList()).probabilitySampleVector(null, null, null), 0.00001);
	}

}
