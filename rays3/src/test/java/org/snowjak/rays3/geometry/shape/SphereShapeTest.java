package org.snowjak.rays3.geometry.shape;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.transform.RotationTransform;
import org.snowjak.rays3.transform.TranslationTransform;

public class SphereShapeTest {

	private SphereShape sphere;

	@Before
	public void setUp() throws Exception {

		sphere = new SphereShape(1d,
				Arrays.asList(new TranslationTransform(3d, 0d, 0d), new RotationTransform(Vector.J, 90d)));
	}

	@Test
	public void testGetIntersection() {

		Ray ray = new Ray(new Point(0, 0, 0), new Vector(1, 0, 0));

		Interaction hit = sphere.getIntersection(ray);

		assertNotNull("Expected hit was actually a miss!", hit);

		assertEquals("Hit point X not as expected", 2d, hit.getPoint().getX(), 0.00001);
		assertEquals("Hit point Y not as expected", 0d, hit.getPoint().getY(), 0.00001);
		assertEquals("Hit point Z not as expected", 0d, hit.getPoint().getZ(), 0.00001);

		assertEquals("Hit normal X not as expected", -1d, hit.getNormal().getX(), 0.00001);
		assertEquals("Hit normal Y not as expected", 0d, hit.getNormal().getY(), 0.00001);
		assertEquals("Hit normal Z not as expected", 0d, hit.getNormal().getZ(), 0.00001);

		assertEquals("Hit ray direction X not as expected", 1d, hit.getInteractingRay().getDirection().getX(), 0.00001);
		assertEquals("Hit ray direction Y not as expected", 0d, hit.getInteractingRay().getDirection().getY(), 0.00001);
		assertEquals("Hit ray direction Z not as expected", 0d, hit.getInteractingRay().getDirection().getZ(), 0.00001);
		assertEquals("Hit ray currT not as expected", 2d, hit.getInteractingRay().getCurrT(), 0.00001);
	}

	@Test
	public void testGetLocalIntersection() {

		Ray rayHit = new Ray(new Point(0, 0, -3), new Vector(0, 0, 1));
		Ray rayMiss = new Ray(new Point(0.9, 0.9, -3), new Vector(0, 0, 1));

		Interaction hit = sphere.getLocalIntersection(rayHit);

		assertNotNull("Expected hit was actually a miss!", hit);

		assertEquals("Hit point X not as expected", 0d, hit.getPoint().getX(), 0.00001);
		assertEquals("Hit point Y not as expected", 0d, hit.getPoint().getY(), 0.00001);
		assertEquals("Hit point Z not as expected", -1d, hit.getPoint().getZ(), 0.00001);

		assertEquals("Hit normal X not as expected", 0d, hit.getNormal().getX(), 0.00001);
		assertEquals("Hit normal Y not as expected", 0d, hit.getNormal().getY(), 0.00001);
		assertEquals("Hit normal Z not as expected", -1d, hit.getNormal().getZ(), 0.00001);

		assertEquals("Hit ray direction X not as expected", 0d, hit.getInteractingRay().getDirection().getX(), 0.00001);
		assertEquals("Hit ray direction Y not as expected", 0d, hit.getInteractingRay().getDirection().getY(), 0.00001);
		assertEquals("Hit ray direction Z not as expected", 1d, hit.getInteractingRay().getDirection().getZ(), 0.00001);
		assertEquals("Hit ray currT not as expected", 2d, hit.getInteractingRay().getCurrT(), 0.00001);

		Interaction miss = sphere.getLocalIntersection(rayMiss);

		assertNull("Expected miss was actually a hit!", miss);
	}

}
