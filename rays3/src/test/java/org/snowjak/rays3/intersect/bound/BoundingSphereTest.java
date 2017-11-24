package org.snowjak.rays3.intersect.bound;

import static org.junit.Assert.*;
import static org.apache.commons.math3.util.FastMath.*;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.snowjak.rays3.geometry.Point;

public class BoundingSphereTest {

	@Test
	public void testBoundingSphereCollectionOfPoint() {

		final Collection<Point> points = Arrays.asList(new Point(-3, 2, 0), new Point(1, -2, -1), new Point(3, 3, -1),
				new Point(1, 0, -1));

		final BoundingSphere boundingSphere = new BoundingSphere(points);
		final Point sphereCenter = boundingSphere.getCenter();
		final double sphereRadius = boundingSphere.getRadius();

		assertEquals("Center-X is not as expected!", 0, sphereCenter.getX(), 0.00001);
		assertEquals("Center-Y is not as expected!", 0.5, sphereCenter.getY(), 0.00001);
		assertEquals("Center-Z is not as expected!", -0.5, sphereCenter.getZ(), 0.00001);

		assertEquals("Radius is not as expected!", sqrt(3 * 3 + 2.5 * 2.5 + .5 * .5), sphereRadius, 0.00001);
	}

	@Test
	public void testBoundingSphereAABB() {

		final AABB aabb = new AABB(new Point(-3, -2, -1), new Point(3, 3, 0));

		final BoundingSphere boundingSphere = new BoundingSphere(aabb);
		final Point sphereCenter = boundingSphere.getCenter();
		final double sphereRadius = boundingSphere.getRadius();

		assertEquals("Center-X is not as expected!", 0, sphereCenter.getX(), 0.00001);
		assertEquals("Center-Y is not as expected!", 0.5, sphereCenter.getY(), 0.00001);
		assertEquals("Center-Z is not as expected!", -0.5, sphereCenter.getZ(), 0.00001);

		assertEquals("Radius is not as expected!", sqrt(3 * 3 + 2.5 * 2.5 + .5 * .5), sphereRadius, 0.00001);
	}

}
