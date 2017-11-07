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

		double t;

		if (t0 < 0 && t1 < 0)
			return null;

		if (t0 < 0 && t1 >= 0)
			t = t1;
		else if (t0 > 0 && t1 < 0)
			t = t0;
		else if (t0 < t1)
			t = t1;
		else
			t = t0;

		Ray newRay = new Ray(ray.getOrigin(), ray.getDirection(), t);
		Point point = newRay.getPointAlong();
		Normal normalAt = new Normal(new Vector(point).normalize());

		// Compute the surface parameterization in terms of phi and theta.
		// Phi = atan ( z / x )
		// Theta = acos ( y / r )
		Point2D param = new Point2D(FastMath.atan2(point.getZ(), point.getX()), FastMath.acos(point.getY() / r));

		return new Interaction(point, newRay, normalAt, param, null);
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

		Point localPoint = worldToLocal(point);
		return localToWorld(getLocalIntersection(new Ray(localPoint, new Vector(localPoint).negate())));
	}

}
