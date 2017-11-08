package org.snowjak.rays3.intersect;

import org.snowjak.rays3.bxdf.BDSF;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.shape.Primitive;
import org.snowjak.rays3.geometry.shape.SurfaceDescriptor;

public class Interaction extends SurfaceDescriptor {

	private final Primitive	primitive;
	private final Ray		interactingRay;

	public Interaction(Point point, Ray interactingRay, Normal normal, Point2D param, Primitive primitive) {
		super(point, normal, param);
		this.interactingRay = interactingRay;
		this.primitive = primitive;
	}

	/**
	 * @return the {@link Ray}, originating with the eye-point, which does the
	 *         interacting
	 */
	public Ray getInteractingRay() {

		return interactingRay;
	}

	/**
	 * @return the {@link Primitive} being interacted with
	 */
	public Primitive getPrimitive() {

		return primitive;
	}

	/**
	 * @return the {@link BDSF} associated with the interacting point on the
	 *         object
	 */
	public BDSF getBdsf() {

		return primitive.getBdsf();
	}
}
