package org.snowjak.rays3.transform;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;

public class ScaleTransformTest {

	private Transform transform;

	@Before
	public void setUp() throws Exception {

		transform = new ScaleTransform(2d, 2d, 2d);
	}

	@Test
	public void testWorldToLocalPoint() {

		Point point = new Point(1, 2, 3);
		Point transformed = transform.worldToLocal(point);

		assertEquals("Transformed X is not as expected!", 1d / 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y is not as expected!", 2d / 2d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z is not as expected!", 3d / 2d, transformed.getZ(), 0.00001);
	}

	@Test
	public void testLocalToWorldPoint() {

		Point point = new Point(1, 2, 3);
		Point transformed = transform.localToWorld(point);

		assertEquals("Transformed X is not as expected!", 1d * 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y is not as expected!", 2d * 2d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z is not as expected!", 3d * 2d, transformed.getZ(), 0.00001);
	}

	@Test
	public void testWorldToLocalVector() {

		Vector vector = new Vector(1, 2, 3);
		Vector transformed = transform.worldToLocal(vector);

		assertEquals("Transformed X is not as expected!", 1d / 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y is not as expected!", 2d / 2d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z is not as expected!", 3d / 2d, transformed.getZ(), 0.00001);
	}

	@Test
	public void testLocalToWorldVector() {

		Vector vector = new Vector(1, 2, 3);
		Vector transformed = transform.localToWorld(vector);

		assertEquals("Transformed X is not as expected!", 1d * 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y is not as expected!", 2d * 2d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z is not as expected!", 3d * 2d, transformed.getZ(), 0.00001);
	}

	@Test
	public void testWorldToLocalNormal() {

		Normal normal = new Normal(1, 2, 3);
		Normal transformed = transform.localToWorld(normal);

		assertEquals("Transformed X is not as expected!", 1d / 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y is not as expected!", 2d / 2d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z is not as expected!", 3d / 2d, transformed.getZ(), 0.00001);
	}

	@Test
	public void testLocalToWorldNormal() {

		Normal normal = new Normal(1, 2, 3);
		Normal transformed = transform.worldToLocal(normal);

		assertEquals("Transformed X is not as expected!", 1d * 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y is not as expected!", 2d * 2d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z is not as expected!", 3d * 2d, transformed.getZ(), 0.00001);
	}

}
