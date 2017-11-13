package org.snowjak.rays3.geometry.shape;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.transform.RotationTransform;
import org.snowjak.rays3.transform.TranslationTransform;

public class AbstractShapeTest {

	private AbstractShape shape;

	@Before
	public void setUp() {

		shape = new AbstractShape(Collections.emptyList()) {

			@Override
			public boolean isLocalInteracting(Ray ray) {

				// We don't care about this method for the purposes of this
				// test.
				return false;
			}

			@Override
			public boolean isInteracting(Ray ray) {

				// We don't care about this method for the purposes of this
				// test.
				return false;
			}

			@Override
			public Interaction getLocalIntersection(Ray ray) {

				// We don't care about this method for the purposes of this
				// test.
				return null;
			}

			@Override
			public Interaction getIntersection(Ray ray) {

				// We don't care about this method for the purposes of this
				// test.
				return null;
			}

			@Override
			public SurfaceDescriptor getSurfaceNearestTo(Point point) {

				// We don't care about this method for the purposes of this
				// test.
				return null;
			}

			@Override
			public Point getLocalSurfaceFromParam(Point2D param) {

				// We don't care about this method for the purposes of this
				// test.
				return null;
			}

			@Override
			public Point2D getParamFromLocalSurface(Point surface) {

				// We don't care about this method for the purposes of this
				// test.
				return null;
			}
		};
	}

	@Test
	public void testAppendTransform() {

		shape.appendTransform(new TranslationTransform(3d, 0d, 0d));
		shape.appendTransform(new RotationTransform(Vector.J, 90d));

		//
		// Therefore, a point located at (1,0,0) relative to the object should
		// be located at (3,0,1) relative to the world-origin.
		//
		Point relativePoint = new Point(1, 0, 0);
		Point absolutePoint = shape.localToWorld(relativePoint);

		assertEquals("Absolute X is not as expected!", 3d, absolutePoint.getX(), 0.00001);
		assertEquals("Absolute Y is not as expected!", 0d, absolutePoint.getY(), 0.00001);
		assertEquals("Absolute Z is not as expected!", -1d, absolutePoint.getZ(), 0.00001);
	}

}
