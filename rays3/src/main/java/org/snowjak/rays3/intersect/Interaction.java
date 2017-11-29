package org.snowjak.rays3.intersect;

import org.snowjak.rays3.bxdf.BSDF;
import org.snowjak.rays3.bxdf.FresnelApproximation;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.geometry.shape.Primitive;
import org.snowjak.rays3.geometry.shape.SurfaceDescriptor;

public class Interaction extends SurfaceDescriptor {

	private final Primitive				primitive;
	private final Ray					interactingRay;

	private final Vector				w_e;

	private final double				n1, n2;
	private final FresnelApproximation	fresnel;

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
	 * flipping the existing {@link Normal} and, if necessary, exchanging the
	 * two indices of refraction.
	 * 
	 * @param toCopy
	 * @param normal
	 */
	public Interaction(Interaction toCopy, boolean flipIndicesOfRefraction) {
		this(toCopy, toCopy.getNormal().negate(), true);
	}

	/**
	 * Create a new {@link Interaction} by copying an existing Interaction and
	 * providing a new {@link Normal}, with the option to reverse the two
	 * indices of refraction.
	 * 
	 * @param toCopy
	 * @param normal
	 * @param flipIndicesOfRefraction
	 */
	public Interaction(Interaction toCopy, Normal normal, boolean flipIndicesOfRefraction) {
		this(toCopy.getPoint(), toCopy.getInteractingRay(), normal, toCopy.getParam(), toCopy.getPrimitive(),
				( flipIndicesOfRefraction ? toCopy.n2 : toCopy.n1 ),
				( flipIndicesOfRefraction ? toCopy.n1 : toCopy.n2 ));
	}

	/**
	 * Create a new {@link Interaction}.
	 * <p>
	 * Indices of refraction are given default values:
	 * <ul>
	 * <li>n1: 1.0</li>
	 * <li>n2: {@link Primitive#getBsdf()}.getIndexOfRefraction()
	 * <strong>or</strong> 1.0 if <code>primitive == null</code> or
	 * <code>primitive.bdsf == null</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @param point
	 * @param interactingRay
	 * @param normal
	 * @param param
	 * @param primitive
	 */
	public Interaction(Point point, Ray interactingRay, Normal normal, Point2D param, Primitive primitive) {
		this(point, interactingRay, normal, param, primitive, 1.0, ( primitive == null || primitive.getBsdf() == null
				? 1.0 : primitive.getBsdf().getIndexOfRefraction() ));
	}

	public Interaction(Point point, Ray interactingRay, Normal normal, Point2D param, Primitive primitive, double n1,
			double n2) {
		super(point, normal, param);
		this.interactingRay = interactingRay;
		this.primitive = primitive;
		this.w_e = interactingRay.getDirection().negate().normalize();
		this.n1 = n1;
		this.n2 = n2;
		this.fresnel = new FresnelApproximation(w_e, normal, n1, n2);
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
	 * @return the {@link FresnelApproximation} corresponding to this particular
	 *         surface-interaction
	 */
	public FresnelApproximation getFresnel() {

		return fresnel;
	}

	/**
	 * @return the eye-vector associated with this interaction.
	 */
	public Vector getW_e() {

		return w_e;
	}

	/**
	 * @return the index-of-refraction for the medium from which the eye is
	 *         looking out toward the surface
	 */
	public double getN1() {

		return n1;
	}

	/**
	 * @return
	 */
	public double getN2() {

		return n2;
	}
}
