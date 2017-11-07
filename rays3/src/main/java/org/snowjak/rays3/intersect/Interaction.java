package org.snowjak.rays3.intersect;

import org.snowjak.rays3.bxdf.BDSF;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.shape.SurfaceDescriptor;

public class Interaction extends SurfaceDescriptor {

	private Ray		interactingRay;
	private BDSF	bdsf;

	public Interaction(Point point, Ray interactingRay, Normal normal, BDSF bdsf) {
		super(point, normal);
		this.interactingRay = interactingRay;
		this.bdsf = bdsf;
	}

	/**
	 * @return the {@link Ray}, originating with the eye-point, which does the
	 *         interacting
	 */
	public Ray interactingRay() {

		return interactingRay;
	}

	/**
	 * @return the {@link BDSF} associated with the interacting point on the
	 *         object
	 */
	public BDSF bdsf() {

		return bdsf;
	}
}
