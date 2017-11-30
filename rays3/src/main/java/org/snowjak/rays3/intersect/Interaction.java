package org.snowjak.rays3.intersect;

import org.snowjak.rays3.bxdf.BSDF;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.geometry.shape.Primitive;
import org.snowjak.rays3.geometry.shape.SurfaceDescriptor;

public class Interaction extends SurfaceDescriptor {

	private final Primitive	primitive;
	private final Ray		interactingRay;

	private final Vector	w_e;

	/**
	 * Create a new {@link Interaction} by copying an existing Interaction and
	 * providing a new {@link Primitive} (updating the <em>entering</em>
	 * index-of-refraction, if provided, in the process).
	 * 
	 * @param toCopy
	 * @param primitive
	 */
	public Interaction(Interaction toCopy, Primitive primitive) {
		this(toCopy.getPoint(), toCopy.getInteractingRay(), toCopy.getNormal(), toCopy.getParam(), primitive);
	}

	/**
	 * Create a new {@link Interaction} by copying an existing Interaction and
	 * providing a new {@link Normal}.
	 * 
	 * @param toCopy
	 * @param normal
	 * @param flipIndicesOfRefraction
	 */
	public Interaction(Interaction toCopy, Normal normal) {
		this(toCopy.getPoint(), toCopy.getInteractingRay(), normal, toCopy.getParam(), toCopy.getPrimitive());
	}

	/**
	 * Create a new {@link Interaction}.
	 * 
	 * @param point
	 * @param interactingRay
	 * @param normal
	 * @param param
	 * @param primitive
	 */
	public Interaction(Point point, Ray interactingRay, Normal normal, Point2D param, Primitive primitive) {
		super(point, normal, param);
		this.interactingRay = interactingRay;
		this.primitive = primitive;
		this.w_e = interactingRay.getDirection().negate().normalize();
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
	 * @return the {@link BSDF} associated with the interacting point on the
	 *         object
	 */
	public BSDF getBdsf() {

		return primitive.getBsdf();
	}

	/**
	 * @return the eye-vector associated with this interaction.
	 */
	public Vector getW_e() {

		return w_e;
	}

}
