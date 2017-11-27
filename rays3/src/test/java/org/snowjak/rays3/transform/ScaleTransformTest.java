package org.snowjak.rays3.transform;

import static org.junit.Assert.*;
import static org.apache.commons.math3.util.FastMath.*;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
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

	@Test
	public void testLocalToWorldRay() {

		final Transform transform2 = new ScaleTransform(1d / 3d, -1d / 2d, 1d / 5d);

		final Ray ray = new Ray(new Point(1, 2, 3), new Vector(-2, 4, -6).normalize(), 1, 2d, 2d, 2d, 0.5);
		final Ray transformed1 = transform.localToWorld(ray);

		assertEquals("#1-Transformed origin-X is not as expected!", 1d * 2d, transformed1.getOrigin().getX(), 0.00001);
		assertEquals("#1-Transformed origin-Y is not as expected!", 2d * 2d, transformed1.getOrigin().getY(), 0.00001);
		assertEquals("#1-Transformed origin-Z is not as expected!", 3d * 2d, transformed1.getOrigin().getZ(), 0.00001);

		assertEquals("#1-Transformed direction-X is not as expected!", -1d / sqrt(14d),
				transformed1.getDirection().getX(), 0.00001);
		assertEquals("#1-Transformed direction-Y is not as expected!", sqrt(2d / 7d),
				transformed1.getDirection().getY(), 0.00001);
		assertEquals("#1-Transformed direction-Z is not as expected!", -3d / sqrt(14d),
				transformed1.getDirection().getZ(), 0.00001);

		assertEquals("#1-Depth is not as expected!", 1, transformed1.getDepth());
		assertEquals("#1-Transformed currT is not as expected!", 4d, transformed1.getCurrT(), 0.00001);
		assertEquals("#1-Transformed minT is not as expected!", 4d, transformed1.getMinT(), 0.00001);
		assertEquals("#1-Transformed maxT is not as expected!", 4d, transformed1.getMaxT(), 0.00001);
		assertEquals("#1-Transformed weight not as expected!", 0.5, transformed1.getWeight(), 0.00001);

		final Ray transformed2 = transform2.localToWorld(ray);

		assertEquals("#2-Transformed origin-X is not as expected!", 1d / 3d, transformed2.getOrigin().getX(), 0.00001);
		assertEquals("#2-Transformed origin-Y is not as expected!", -1, transformed2.getOrigin().getY(), 0.00001);
		assertEquals("#2-Transformed origin-Z is not as expected!", 3d / 5d, transformed2.getOrigin().getZ(), 0.00001);

		assertEquals("#2-Transformed direction-X is not as expected!", -0.274825, transformed2.getDirection().getX(),
				0.00001);
		assertEquals("#2-Transformed direction-Y is not as expected!", -0.824475, transformed2.getDirection().getY(),
				0.00001);
		assertEquals("#2-Transformed direction-Z is not as expected!", -0.494685, transformed2.getDirection().getZ(),
				0.00001);

		assertEquals("#2-Depth is not as expected!", 1, transformed2.getDepth());
		assertEquals("#2-Transformed currT is not as expected!", 0.648319, transformed2.getCurrT(), 0.00001);
		assertEquals("#2-Transformed minT is not as expected!", 0.648319, transformed2.getMinT(), 0.00001);
		assertEquals("#2-Transformed maxT is not as expected!", 0.648319, transformed2.getMaxT(), 0.00001);
		assertEquals("#2-Transformed weight not as expected!", 0.5, transformed2.getWeight(), 0.00001);
	}

	@Test
	public void testWorldToLocalRay() {

		final Transform transform2 = new ScaleTransform(1d / 3d, -1d / 2d, 1d / 5d);

		final Ray ray = new Ray(new Point(1, 2, 3), new Vector(-2, 4, -6), 1, 2d, 2d, 2d, 0.5);
		final Ray transformed1 = transform.worldToLocal(ray);

		assertEquals("Transformed X is not as expected!", 1d / 2d, transformed1.getOrigin().getX(), 0.00001);
		assertEquals("Transformed Y is not as expected!", 2d / 2d, transformed1.getOrigin().getY(), 0.00001);
		assertEquals("Transformed Z is not as expected!", 3d / 2d, transformed1.getOrigin().getZ(), 0.00001);

		assertEquals("#1-Transformed direction-X is not as expected!", -1d / sqrt(14d),
				transformed1.getDirection().getX(), 0.00001);
		assertEquals("#1-Transformed direction-Y is not as expected!", sqrt(2d / 7d),
				transformed1.getDirection().getY(), 0.00001);
		assertEquals("#1-Transformed direction-Z is not as expected!", -3d / sqrt(14d),
				transformed1.getDirection().getZ(), 0.00001);

		assertEquals("#1-Depth is not as expected!", 1, transformed1.getDepth());
		assertEquals("#1-Transformed currT is not as expected!", 2d / 2d, transformed1.getCurrT(), 0.00001);
		assertEquals("#1-Transformed minT is not as expected!", 2d / 2d, transformed1.getMinT(), 0.00001);
		assertEquals("#1-Transformed maxT is not as expected!", 2d / 2d, transformed1.getMaxT(), 0.00001);
		assertEquals("#1-Transformed weight not as expected!", 0.5, transformed1.getWeight(), 0.00001);

		final Ray transformed2 = transform2.worldToLocal(ray);

		assertEquals("#2-Transformed origin-X is not as expected!", 1d / ( 1d / 3d ), transformed2.getOrigin().getX(),
				0.00001);
		assertEquals("#2-Transformed origin-Y is not as expected!", 2d / ( -1d / 2d ), transformed2.getOrigin().getY(),
				0.00001);
		assertEquals("#2-Transformed origin-Z is not as expected!", 3d / ( 1d / 5d ), transformed2.getOrigin().getZ(),
				0.00001);

		assertEquals("#2-Transformed direction-X is not as expected!", -0.189737, transformed2.getDirection().getX(),
				0.00001);
		assertEquals("#2-Transformed direction-Y is not as expected!", -0.252982, transformed2.getDirection().getY(),
				0.00001);
		assertEquals("#2-Transformed direction-Z is not as expected!", -0.948683, transformed2.getDirection().getZ(),
				0.00001);

		assertEquals("#2-Depth is not as expected!", 1, transformed2.getDepth());
		assertEquals("#2-Transformed currT is not as expected!", 8.45154, transformed2.getCurrT(), 0.00001);
		assertEquals("#2-Transformed minT is not as expected!", 8.45154, transformed2.getMinT(), 0.00001);
		assertEquals("#2-Transformed maxT is not as expected!", 8.45154, transformed2.getMaxT(), 0.00001);
		assertEquals("#2-Transformed weight not as expected!", 0.5, transformed2.getWeight(), 0.00001);
	}

}
