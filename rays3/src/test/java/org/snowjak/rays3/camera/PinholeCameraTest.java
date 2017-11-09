package org.snowjak.rays3.camera;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;

public class PinholeCameraTest {

	private Camera camera;

	@Before
	public void setUp() throws Exception {

		camera = new PinholeCamera(4d, 4d, new Point(0, 0, -5), new Point(0, 0, 0), Vector.J, 8);
	}

	@Test
	public void testGetRayDoubleDoubleDoubleDouble_center() {

		Ray ray = camera.getRay(0.5d, 0.5d, 0.5d, 0.5d);

		assertEquals("Ray origin-X not as expected!", 0d, ray.getOrigin().getX(), 0.00001);
		assertEquals("Ray origin-Y not as expected!", 0d, ray.getOrigin().getY(), 0.00001);
		assertEquals("Ray origin-Z not as expected!", -5d, ray.getOrigin().getZ(), 0.00001);

		assertEquals("Ray direction-X not as expected!", 0d, ray.getDirection().getX(), 0.00001);
		assertEquals("Ray direction-Y not as expected!", 0d, ray.getDirection().getY(), 0.00001);
		assertEquals("Ray direction-Z not as expected!", 1d, ray.getDirection().getZ(), 0.00001);
	}

	@Test
	public void testGetRayDoubleDoubleDoubleDouble_edge() {

		Ray ray = camera.getRay(1d, 0.5d, 0.5d, 0.5d);

		assertEquals("Ray origin-X not as expected!", 2d, ray.getOrigin().getX(), 0.00001);
		assertEquals("Ray origin-Y not as expected!", 0d, ray.getOrigin().getY(), 0.00001);
		assertEquals("Ray origin-Z not as expected!", -5d, ray.getOrigin().getZ(), 0.00001);

		assertEquals("Ray direction-X not as expected!", 0.242536d, ray.getDirection().getX(), 0.00001);
		assertEquals("Ray direction-Y not as expected!", 0d, ray.getDirection().getY(), 0.00001);
		assertEquals("Ray direction-Z not as expected!", 0.970143d, ray.getDirection().getZ(), 0.00001);
	}

}
