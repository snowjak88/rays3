package org.snowjak.rays3.geometry;

import static org.junit.Assert.*;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import org.snowjak.rays3.Global;

public class VectorTest {

	@Test
	public void testNormalize() {

		final Vector vect = new Vector(-3, 2, 7).normalize();
		final double vect_length = FastMath
				.sqrt(FastMath.pow(vect.getX(), 2) + FastMath.pow(vect.getY(), 2) + FastMath.pow(vect.getZ(), 2));
		assertEquals(1d, vect_length, 0.00001);

	}

	@Test
	public void testDotProduct() {

		assertEquals(0d, Vector.I.dotProduct(Vector.J), Global.DOUBLE_TOLERANCE);
		assertEquals(-1d, Vector.I.dotProduct(Vector.I.negate()), Global.DOUBLE_TOLERANCE);
	}

	@Test
	public void testCrossProduct() {

		Vector cp = Vector.I.crossProduct(Vector.J);
		assertEquals(Vector.K.getX(), cp.getX(), Global.DOUBLE_TOLERANCE);
		assertEquals(Vector.K.getY(), cp.getY(), Global.DOUBLE_TOLERANCE);
		assertEquals(Vector.K.getZ(), cp.getZ(), Global.DOUBLE_TOLERANCE);
	}

	@Test
	public void testOrthogonal() {

		Vector basis = new Vector(-3d, 5d, 2d);
		Vector othogonal = basis.orthogonal();
		assertEquals(0d, basis.dotProduct(othogonal), 0.000000001);
	}

}
