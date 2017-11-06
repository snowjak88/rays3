package org.snowjak.rays3.intersect;

import org.snowjak.rays3.bxdf.BDSF;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;

public class Interaction {

	private Point	point;
	private Ray		interactingRay;
	private Normal	normal;
	private BDSF	bdsf;

	public Interaction(Point point, Ray interactingRay, Normal normal, BDSF bdsf) {
		super();
		this.point = point;
		this.interactingRay = interactingRay;
		this.normal = normal;
		this.bdsf = bdsf;
	}

	/**
	 * @return the {@link Point} of interaction
	 */
	public Point point() {

		return point;
	}

	/**
	 * @return the {@link Ray}, originating with the eye-point, which does the
	 *         interacting
	 */
	public Ray interactingRay() {

		return interactingRay;
	}

	/**
	 * @return the {@link Normal} at the point of interaction
	 */
	public Normal normal() {

		return normal;
	}

	/**
	 * @return the {@link BDSF} associated with the interacting point on the
	 *         object
	 */
	public BDSF bdsf() {

		return bdsf;
	}
}
