package org.snowjak.rays3.geometry.shape;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.transform.TranslationTransform;

public class PlaneShapeTest {

	@Test
	public void testGetSurfaceNearestTo() {

		final PlaneShape plane = new PlaneShape(Arrays.asList(new TranslationTransform(0, -3, 0)));

		final Point nearbyPoint = new Point(3, 5, 2);
		final SurfaceDescriptor surfaceDescriptor = plane.getSurfaceNearestTo(nearbyPoint);
		final Point surfacePoint = surfaceDescriptor.getPoint();
		final Normal surfaceNormal = surfaceDescriptor.getNormal();
		final Point2D surfaceParam = surfaceDescriptor.getParam();

		assertEquals("Nearby surface point-X not as expected!", 3, surfacePoint.getX(), 0.00001);
		assertEquals("Nearby surface point-Y not as expected!", -3, surfacePoint.getY(), 0.00001);
		assertEquals("Nearby surface point-Z not as expected!", 2, surfacePoint.getZ(), 0.00001);

		assertEquals("Nearby surface normal-X not as expected!", 0, surfaceNormal.getX(), 0.00001);
		assertEquals("Nearby surface normal-Y not as expected!", 1, surfaceNormal.getY(), 0.00001);
		assertEquals("Nearby surface normal-Z not as expected!", 0, surfaceNormal.getZ(), 0.00001);

		assertEquals("Nearby surface param-X not as expected!", 3, surfaceParam.getX(), 0.00001);
		assertEquals("Nearby surface param-Y not as expected!", 2, surfaceParam.getY(), 0.00001);
	}

	@Test
	public void testGetLocalSurfaceFromParam() {

		PlaneShape plane = new PlaneShape(Collections.emptyList());

		final Point2D surfaceParam = new Point2D(4, -7);
		final Point surfacePoint = plane.getLocalSurfaceFromParam(surfaceParam);

		assertEquals("Surface point-X not as expected!", 4, surfacePoint.getX(), 0.00001);
		assertEquals("Surface point-Y not as expected!", 0, surfacePoint.getY(), 0.00001);
		assertEquals("Surface point-Z not as expected!", -7, surfacePoint.getZ(), 0.00001);
	}

	@Test
	public void testGetParamFromLocalSurface() {

		PlaneShape plane = new PlaneShape(Collections.emptyList());

		final Point surfacePoint = new Point(5, 0, 2);
		final Point2D surfaceParam = plane.getParamFromLocalSurface(surfacePoint);

		assertEquals("Surface param-X not as expected!", 5, surfaceParam.getX(), 0.00001);
		assertEquals("Surface param-Y not as expected!", 2, surfaceParam.getY(), 0.00001);
	}

	@Test
	public void testIsLocalInteracting() {

		final PlaneShape plane = new PlaneShape(Arrays.asList(new TranslationTransform(0, -3, 0)));

		Ray localRay = new Ray(new Point(3, 3, 0), new Vector(-1, -1, 0).normalize());
		assertTrue(plane.isLocalInteracting(localRay));
	}

	@Test
	public void testGetLocalIntersection() {

		final PlaneShape plane = new PlaneShape(Arrays.asList(new TranslationTransform(0, -3, 0)));

		Ray localRay = new Ray(new Point(3, 3, 2), new Vector(-1, -1, 0).normalize());
		Interaction interaction = plane.getLocalIntersection(localRay);

		Point surfacePoint = interaction.getPoint();
		Normal surfaceNormal = interaction.getNormal();
		Point2D surfaceParam = interaction.getParam();

		assertEquals("Nearby surface point-X not as expected!", 0, surfacePoint.getX(), 0.00001);
		assertEquals("Nearby surface point-Y not as expected!", 0, surfacePoint.getY(), 0.00001);
		assertEquals("Nearby surface point-Z not as expected!", 2, surfacePoint.getZ(), 0.00001);

		assertEquals("Nearby surface normal-X not as expected!", 0, surfaceNormal.getX(), 0.00001);
		assertEquals("Nearby surface normal-Y not as expected!", 1, surfaceNormal.getY(), 0.00001);
		assertEquals("Nearby surface normal-Z not as expected!", 0, surfaceNormal.getZ(), 0.00001);

		assertEquals("Nearby surface param-X not as expected!", 0, surfaceParam.getX(), 0.00001);
		assertEquals("Nearby surface param-Y not as expected!", 2, surfaceParam.getY(), 0.00001);
	}

}
