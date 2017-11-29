package org.snowjak.rays3.transform;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.geometry.shape.AbstractShape;
import org.snowjak.rays3.geometry.shape.PlaneShape;
import org.snowjak.rays3.geometry.shape.SphereShape;
import org.snowjak.rays3.intersect.Interaction;

public class MultipleTransformTest {

	private List<Transform> transforms;

	@Before
	public void setUp() {

		transforms = Arrays.asList(new TranslationTransform(3, 0, 0), new RotationTransform(Vector.K, 90d));
	}

	@Test
	public void testRotationTranslation() {

		final AbstractShape shape = new SphereShape(0.5, transforms);

		final Point worldPoint = new Point(1, 0, 0);
		final Point localPoint = shape.worldToLocal(worldPoint);

		assertEquals("World-Local-Point-X not as expected!", 0, localPoint.getX(), 0.00001);
		assertEquals("World-Local-Point-Y not as expected!", 2, localPoint.getY(), 0.00001);
		assertEquals("World-Local-Point-Z not as expected!", 0, localPoint.getZ(), 0.00001);

		final Vector worldVector = new Vector(1, 1, 0);
		final Vector localVector = shape.localToWorld(worldVector);

		assertEquals("World-Local-Vector-X not as expected!", -1d, localVector.getX(), 0.00001);
		assertEquals("World-Local-Vector-Y not as expected!", 1d, localVector.getY(), 0.00001);
		assertEquals("World-Local-Vector-Z not as expected!", 0, localVector.getZ(), 0.00001);

	}

	@Test
	public void testTransformPlaneIntersection() {

		final AbstractShape plane = new PlaneShape(transforms);

		final Ray ray = new Ray(new Point(-1, 0, 0), new Vector(1, 0, 0));

		assertTrue("Ray should interact with plane!", plane.isInteracting(ray));

		final Interaction interaction = plane.getIntersection(ray);
		final Point point = interaction.getPoint();
		final Normal normal = interaction.getNormal();

		assertEquals("Interaction-point-X not as expected!", 3, point.getX(), 0.00001);
		assertEquals("Interaction-point-Y not as expected!", 0, point.getY(), 0.00001);
		assertEquals("Interaction-point-Z not as expected!", 0, point.getZ(), 0.00001);

		assertEquals("Interaction-normal-X not as expected!", -1, normal.getX(), 0.00001);
		assertEquals("Interaction-normal-Y not as expected!", 0, normal.getY(), 0.00001);
		assertEquals("Interaction-normal-Z not as expected!", 0, normal.getZ(), 0.00001);
	}
	
	@Test
	public void testInverseTransforms() {
		
		final Ray ray = new Ray(new Point(-1,0,0),new Vector(1,0,0));
		final AbstractShape shape = new PlaneShape(transforms);
		
		final Ray transformedRay = shape.worldToLocal(ray);
		final Ray inverseRay = shape.localToWorld(transformedRay);
		
		assertEquals("Inverse-transformed-origin-X not as original!", ray.getOrigin().getX(), inverseRay.getOrigin().getX(), 0.00001);
		assertEquals("Inverse-transformed-origin-Y not as original!", ray.getOrigin().getY(), inverseRay.getOrigin().getY(), 0.00001);
		assertEquals("Inverse-transformed-origin-Z not as original!", ray.getOrigin().getZ(), inverseRay.getOrigin().getZ(), 0.00001);
		
		assertEquals("Inverse-transformed-direction-X not as original!", ray.getDirection().getX(), inverseRay.getDirection().getX(), 0.00001);
		assertEquals("Inverse-transformed-direction-Y not as original!", ray.getDirection().getY(), inverseRay.getDirection().getY(), 0.00001);
		assertEquals("Inverse-transformed-direction-Z not as original!", ray.getDirection().getZ(), inverseRay.getDirection().getZ(), 0.00001);
		
	}
}
