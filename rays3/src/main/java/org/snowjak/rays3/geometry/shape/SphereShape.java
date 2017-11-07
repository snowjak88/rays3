package org.snowjak.rays3.geometry.shape;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.AABB;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.transform.Transform;

/**
 * Implements a sphere.
 * 
 * @author snowjak88
 */
public class SphereShape implements Shape {

	/**
	 * Represents an AABB in *global* space.
	 */
	private AABB					aabb;
	private double					r;
	private LinkedList<Transform>	worldToLocal	= new LinkedList<>(), localToWorld = new LinkedList<>();

	/**
	 * Initialize a new sphere, with a radius of 0.5, centered at the origin.
	 */
	public SphereShape() {
		this(0.5d);
	}

	public SphereShape(double r) {
		this(r, Collections.emptyList());
	}

	public SphereShape(double r, List<Transform> worldToLocal) {
		this.r = r;
		worldToLocal.stream().forEach(t -> this.appendTransform(t));

		this.aabb = new AABB(Arrays.asList(new Point(0d, -r, 0d), new Point(0d, +r, 0d)), getLocalToWorldTransforms());
	}

	@Override
	public boolean isInteracting(Ray ray) {

		return aabb.isIntersecting(ray);
	}

	@Override
	public boolean isLocalInteracting(Ray ray) {

		return aabb.isIntersecting(localToWorld(ray));
	}

	@Override
	public Interaction getIntersection(Ray ray) {

		return localToWorld(getLocalIntersection(worldToLocal(ray)));
	}

	@Override
	public Interaction getLocalIntersection(Ray ray) {

		double t = getLocalIntersectionT(ray, false);

		Ray newRay = new Ray(ray.getOrigin(), ray.getDirection(), t);
		Point point = newRay.getPointAlong();
		Normal normalAt = new Normal(new Vector(point).normalize());

		// Compute the surface parameterization in terms of phi and theta.
		// Phi = atan ( z / x ) [normalized to [0,1] ]
		// Theta = acos ( y / r ) [normalized to [0,1] ]
		Point2D param = computeSurfaceParameterization(point);

		return new Interaction(point, newRay, normalAt, param, null);
	}

	/**
	 * For a given Ray, calculate the smallest value <code>t</code> that defines
	 * its intersection-point along that ray with this sphere.
	 * <p>
	 * <strong>Note</strong> that this works only in object-local coordinates!
	 * </p>
	 * 
	 * @param ray
	 * @param includeBehindRay
	 *            <code>true</code> if we should consider intersections behind
	 *            the Ray, or only those in front of it
	 * @return
	 */
	private Double getLocalIntersectionT(Ray ray, boolean includeBehindRay) {

		Vector l = new Vector(ray.getOrigin()).negate();
		double t_ca = l.dotProduct(ray.getDirection());

		if (t_ca < 0d)
			return null;

		double d2 = l.dotProduct(l) - FastMath.pow(t_ca, 2);
		double r2 = FastMath.pow(r, 2);
		if (d2 > r2)
			return null;

		double t_hc = FastMath.sqrt(r2 - d2);

		double t0 = t_ca - t_hc;
		double t1 = t_ca + t_hc;

		if (includeBehindRay) {
			if (FastMath.abs(t0) < FastMath.abs(t1))
				return t0;
			else
				return t1;
		}

		if (t0 < 0 && t1 < 0)
			return null;

		if (t0 < 0 && t1 >= 0)
			return t1;
		else if (t0 > 0 && t1 < 0)
			return t0;
		else if (t0 < t1)
			return t1;
		else
			return t0;
	}

	private Point2D computeSurfaceParameterization(Point point) {

		// Compute the surface parameterization in terms of phi and theta.
		// Phi = atan ( z / x ) [normalized to [0,1] ]
		// Theta = acos ( y / r ) [normalized to [0,1] ]
		return new Point2D(( FastMath.atan2(point.getZ(), point.getX()) + FastMath.PI ) / ( 2d * FastMath.PI ),
				FastMath.acos(point.getY() / r) / ( FastMath.PI ));
	}

	@Override
	public List<Transform> getWorldToLocalTransforms() {

		return Collections.unmodifiableList(worldToLocal);
	}

	@Override
	public List<Transform> getLocalToWorldTransforms() {

		return Collections.unmodifiableList(localToWorld);
	}

	@Override
	public void appendTransform(Transform transform) {

		worldToLocal.addLast(transform);
		localToWorld.addFirst(transform);
	}

	@Override
	public SurfaceDescriptor getSurfaceNearestTo(Point point) {

		//
		// For a sphere, this can be done quite simply.
		// Transform everything to the local coordinate-system (where the sphere
		// is centered on (0,0,0)).
		// Then trace a Ray from the Point in question to (0,0,0), and find its
		// intersection with the sphere.
		//
		Point localPoint = worldToLocal(point);
		Ray localRay = new Ray(localPoint, new Vector(localPoint).negate());

		double t = getLocalIntersectionT(localRay, true);

		Ray newRay = new Ray(localRay.getOrigin(), localRay.getDirection(), t);
		Point newPoint = newRay.getPointAlong();
		Normal normalAt = new Normal(new Vector(newPoint).normalize());
		Point2D param = computeSurfaceParameterization(newPoint);

		return new SurfaceDescriptor(localToWorld(newPoint), localToWorld(normalAt), param);
	}

}
