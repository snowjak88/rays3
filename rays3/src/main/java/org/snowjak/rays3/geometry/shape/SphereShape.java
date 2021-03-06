package org.snowjak.rays3.geometry.shape;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.acos;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.Global;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.intersect.bound.AABB;
import org.snowjak.rays3.transform.Transform;

/**
 * Implements a sphere.
 * 
 * @author snowjak88
 */
public class SphereShape extends AbstractShape {

	/**
	 * Represents an AABB in *global* space.
	 */
	private AABB	aabb;
	private double	r;

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
		super(worldToLocal);
		this.r = r;

		this.aabb = new AABB(Arrays.asList(new Point(-r, -r, -r), new Point(+r, +r, +r)), getLocalToWorldTransforms());
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

		Interaction localInteraction = getLocalIntersection(worldToLocal(ray));
		if (localInteraction == null)
			return null;
		return localToWorld(localInteraction);
	}

	@Override
	public Interaction getLocalIntersection(Ray ray) {

		Double t = getLocalIntersectionT(ray, false);

		if (t == null)
			return null;

		Ray newRay = new Ray(ray.getOrigin(), ray.getDirection(), ray.getDepth(), t, t, t, ray.getWeight());
		Point point = newRay.getPointAlong();
		Normal normalAt = new Normal(new Vector(point).normalize());

		// Compute the surface parameterization in terms of theta and phi.
		Point2D param = getParamFromLocalSurface(point);

		return new Interaction(point, newRay, normalAt, param, null);
	}

	/**
	 * For a given Ray, calculate the smallest value <code>t</code> that defines
	 * its intersection-point along that ray with this sphere -- or
	 * <code>null</code> if no such intersection exists.
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

		double d2 = l.dotProduct(l) - ( t_ca * t_ca );
		double r2 = ( r * r );
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

		if (t0 < Global.DOUBLE_TOLERANCE && t1 < Global.DOUBLE_TOLERANCE)
			return null;

		if (t0 < Global.DOUBLE_TOLERANCE)
			return t1;
		else if (t1 < Global.DOUBLE_TOLERANCE)
			return t0;
		else if (t0 < t1)
			return t0;
		else
			return t1;
	}

	@Override
	public Point sampleSurfacePoint(Supplier<Point2D> sampleSupplier) {

		//
		//
		//
		final Point2D sphericalPoint = sampleSupplier.get();

		final double theta = sphericalPoint.getX() * 2d * PI;
		final double phi = acos(2d * sphericalPoint.getY() - 1d);
		//
		final double x = r * cos(theta) * sin(phi);
		final double y = r * cos(phi);
		final double z = r * sin(theta) * sin(phi);
		//
		Vector localVector = Vector.I.multiply(x).add(Vector.J.multiply(y)).add(Vector.K.multiply(z));

		return localToWorld(new Point(localVector));
	}

	@Override
	public Point sampleSurfacePoint(Supplier<Point2D> sampleSupplier, Point facing) {

		final Vector towardsV_local = new Vector(worldToLocal(facing));

		final Vector J = towardsV_local.normalize();
		final Vector I = J.orthogonal();
		final Vector K = I.crossProduct(J);
		//
		//
		//
		final Point2D sphericalPoint = sampleSupplier.get();

		final double sin2_theta = sphericalPoint.getX();
		final double cos2_theta = 1d - sin2_theta;
		final double sin_theta = sqrt(sin2_theta);
		final double cos_theta = sqrt(cos2_theta);

		final double orientation = sphericalPoint.getY() * 2d * PI;
		//
		final double x = sin_theta * cos(orientation);
		final double y = cos_theta;
		final double z = sin_theta * sin(orientation);
		//
		final Vector samplePoint_local = I.multiply(x).add(J.multiply(y)).add(K.multiply(z)).multiply(r);
		return localToWorld(new Point(samplePoint_local));
	}

	@Override
	public double computeSolidAngle(Point viewedFrom) {

		return computeSolidAngle_sphere(viewedFrom, r);
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

		Ray newRay = new Ray(localRay.getOrigin(), localRay.getDirection(), localRay.getDepth(), t, t, t);
		Point newPoint = newRay.getPointAlong();
		Normal normalAt = new Normal(new Vector(newPoint).normalize());
		Point2D param = getParamFromLocalSurface(newPoint);

		return new SurfaceDescriptor(localToWorld(newPoint), localToWorld(normalAt), param);
	}

	@Override
	public Point2D getParamFromLocalSurface(Point point) {

		// Compute the surface parameterization in terms of theta and .
		// Theta = acos ( z / r ) [normalized to [0,1] ]
		// Phi = atan ( y / x ) [normalized to [0,1] ]
		return new Point2D(FastMath.acos(point.getZ() / r) / ( FastMath.PI ),
				( FastMath.atan2(point.getY(), point.getX()) + FastMath.PI ) / ( 2d * FastMath.PI ));
	}

	@Override
	public Point getLocalSurfaceFromParam(Point2D param) {

		final double theta = param.getX() * FastMath.PI;
		final double phi = param.getY() * ( 2d * FastMath.PI ) - FastMath.PI;

		return new Point(( this.r * FastMath.sin(theta) * FastMath.cos(phi) ),
				( this.r * FastMath.sin(theta) * FastMath.sin(phi) ), ( this.r * FastMath.cos(theta) ));
	}

}
