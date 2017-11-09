package org.snowjak.rays3.transform;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;

public class RotationTransformTest {

	private Transform	transformAboutK;
	private Transform	transformAboutJ;

	@Before
	public void setUp() throws Exception {

		transformAboutK = new RotationTransform(Vector.K, 90d);
		transformAboutJ = new RotationTransform(Vector.J, -90d);
	}

	@Test
	public void testWorldToLocalPoint() {

		Point point = new Point(1, 2, 3);
		Point transformed = transformAboutK.worldToLocal(point);

		assertEquals("Transformed X not as expected!", 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", -1d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 3d, transformed.getZ(), 0.00001);
	}

	@Test
	public void testLocalToWorldPoint() {

		Point point = new Point(1, 2, 3);
		Point transformed = transformAboutJ.localToWorld(point);

		assertEquals("Transformed X not as expected!", -3d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 2d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 1d, transformed.getZ(), 0.00001);
	}

	@Test
	public void testWorldToLocalVector() {

		Vector vector = new Vector(1, 2, 3);
		Vector transformed = transformAboutK.worldToLocal(vector);

		assertEquals("Transformed X not as expected!", 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", -1d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 3d, transformed.getZ(), 0.00001);
	}

	@Test
	public void testLocalToWorldVector() {

		Vector vector = new Vector(1, 2, 3);
		Vector transformed = transformAboutK.localToWorld(vector);

		assertEquals("Transformed X not as expected!", -2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 1d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 3d, transformed.getZ(), 0.00001);
	}

	@Test
	public void testWorldToLocalNormal() {

		Normal normal = new Normal(1, 2, 3);
		Normal transformed = transformAboutK.worldToLocal(normal);

		assertEquals("Transformed X not as expected!", 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", -1d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 3d, transformed.getZ(), 0.00001);
	}

	@Test
	public void testLocalToWorldNormal() {

		Normal normal = new Normal(1, 2, 3);
		Normal transformed = transformAboutK.localToWorld(normal);

		assertEquals("Transformed X not as expected!", -2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 1d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 3d, transformed.getZ(), 0.00001);
	}

}
