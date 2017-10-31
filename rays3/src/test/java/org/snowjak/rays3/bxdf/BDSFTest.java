package org.snowjak.rays3.bxdf;

import static org.junit.Assert.*;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;

public class BDSFTest {

	@Test
	public void testGetReflectedVector() {

		Point intersectPoint = new Point(3d, 0d, 3d);
		Point eyePoint = new Point(0d, 3d, 3d);
		Vector w_e = new Vector(eyePoint).subtract(new Vector(intersectPoint));
		Normal n = new Normal(0d, 1d, 0d);

		Vector expectedReflectedVector = new Vector(1d, 1d, 0d).normalize();
		Vector reflectVector = BDSF.getReflectedVector(intersectPoint, w_e, n);

		assertEquals("Reflected vector (x) is not as expected!", expectedReflectedVector.getX(), reflectVector.getX(),
				0.0001);
		assertEquals("Reflected vector (y) is not as expected!", expectedReflectedVector.getY(), reflectVector.getY(),
				0.0001);
		assertEquals("Reflected vector (z) is not as expected!", expectedReflectedVector.getZ(), reflectVector.getZ(),
				0.0001);
	}

	@Test
	public void testGetTransmittedVector() {

		Point intersectPoint = new Point(3d, 0d, 3d);
		Point eyePoint = new Point(0d, 3d, 3d);
		Vector w_e = new Vector(eyePoint).subtract(new Vector(intersectPoint));
		Normal n = new Normal(0d, 1d, 0d);

		Vector expectedReflectedVector = new Vector(FastMath.cos(( 90d - 32.95 ) * FastMath.PI / 180d),
				-FastMath.sin(( 90d - 32.95 ) * FastMath.PI / 180d), 0d).normalize();
		Vector reflectVector = BDSF.getTransmittedVector(intersectPoint, w_e, n, 1d, 1.3d);

		assertEquals("Reflected vector (x) is not as expected!", expectedReflectedVector.getX(), reflectVector.getX(),
				0.0001);
		assertEquals("Reflected vector (y) is not as expected!", expectedReflectedVector.getY(), reflectVector.getY(),
				0.0001);
		assertEquals("Reflected vector (z) is not as expected!", expectedReflectedVector.getZ(), reflectVector.getZ(),
				0.0001);
	}

}
