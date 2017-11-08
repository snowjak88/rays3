package org.snowjak.rays3.geometry;

import static org.junit.Assert.*;

import org.junit.Test;
import org.snowjak.rays3.Global;

public class VectorTest {

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
