package org.snowjak.rays3.bxdf;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import org.snowjak.rays3.bxdf.BDSF.FresnelResult;
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
		Vector reflectVector = BDSF.getPerfectSpecularReflectionVector(intersectPoint, w_e, n);

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

	@Test
	public void testCalculateFresnel() {

		Point eyePoint = new Point(3d, 3d, 3d);
		Point intersectionPoint = new Point(0d, 0d, 3d);
		Vector w_e = new Vector(eyePoint).subtract(new Vector(intersectionPoint));
		Vector w_r = new Vector(-3d, 3d, 3d).subtract(new Vector(intersectionPoint));
		Normal n = new Normal(0d, 1d, 0d);

		double expectedReflectance = 0.113d;
		double expectedTransmittance = 1d - expectedReflectance;

		FresnelResult result = BDSF.calculateFresnel(intersectionPoint, w_e, w_r, n, 1d, 2d);

		assertEquals("Reflectance is not as expected!", expectedReflectance, result.getReflectance(), 0.0001);
		assertEquals("Transmittance is not as expected!", expectedTransmittance, result.getTransmittance(), 0.0001);

	}

}
