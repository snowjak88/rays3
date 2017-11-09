package org.snowjak.rays3.transform;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;

public class TranslationTransformTest {

	private Transform transform;

	@Before
	public void setUp() {

		transform = new TranslationTransform(+1, +2, +3);
	}

	@Test
	public void worldToLocalPoint() {

		Point point = new Point(5, 5, 5);
		Point transformed = transform.worldToLocal(point);

		assertEquals("Transformed X not as expected!", 4, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 3, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 2, transformed.getZ(), 0.00001);
	}

	@Test
	public void localToWorldPoint() {

		Point point = new Point(5, 5, 5);
		Point transformed = transform.localToWorld(point);

		assertEquals("Transformed X not as expected!", 6, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 7, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 8, transformed.getZ(), 0.00001);
	}

	@Test
	public void worldToLocalVector() {

		Vector vector = new Vector(5, 5, 5);
		Vector transformed = transform.worldToLocal(vector);

		assertEquals("Transformed X not as expected!", 5, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 5, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 5, transformed.getZ(), 0.00001);
	}

	@Test
	public void localToWorldVector() {

		Vector vector = new Vector(5, 5, 5);
		Vector transformed = transform.localToWorld(vector);

		assertEquals("Transformed X not as expected!", 5, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 5, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 5, transformed.getZ(), 0.00001);
	}

	@Test
	public void worldToLocalNormal() {

		Normal normal = new Normal(5, 5, 5);
		Normal transformed = transform.worldToLocal(normal);

		assertEquals("Transformed X not as expected!", 5, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 5, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 5, transformed.getZ(), 0.00001);
	}

	@Test
	public void localToWorldNormal() {

		Normal normal = new Normal(5, 5, 5);
		Normal transformed = transform.localToWorld(normal);

		assertEquals("Transformed X not as expected!", 5, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 5, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 5, transformed.getZ(), 0.00001);
	}

}
