package org.snowjak.rays3.geometry.shape;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interactable;
import org.snowjak.rays3.intersect.bound.BoundingSphere;
import org.snowjak.rays3.transform.Transform;

/**
 * Defines a 3-d shape procedurally.
 * 
 * @author snowjak88
 */
public abstract class AbstractShape implements Interactable {

	private LinkedList<Transform> worldToLocal = new LinkedList<>(), localToWorld = new LinkedList<>();

	/**
	 * Construct a new AbstractShape, initializing its internal list of
	 * Transforms.
	 * 
	 * @param worldToLocal
	 */
	public AbstractShape(List<Transform> worldToLocal) {
		worldToLocal.stream().forEach(t -> this.appendTransform(t));
	}

	/**
	 * Select a point (expressed in <strong>global</strong> coordinates) from
	 * the surface of this shape.
	 * 
	 * @return
	 */
	public abstract Point sampleSurfacePoint(Supplier<Point2D> sampleSupplier);

	/**
	 * Given another point (expressed in <strong>global</strong> coordinates),
	 * select a point on the surface of this shape such that the selected point
	 * "faces toward" the other point.
	 * 
	 * @param facing
	 * @return
	 */
	public abstract Point sampleSurfacePoint(Supplier<Point2D> sampleSupplier, Point facing);

	/**
	 * Given a Point (expressed in global coordinates) which this AbstractShape
	 * is viewed from, compute the solid angle which this AbstractShape subtends
	 * as seen from that Point.
	 * 
	 * @param viewedFrom
	 * @return
	 * @see #computeSolidAngle_sphere(Point, BoundingSphere)
	 */
	public abstract double computeSolidAngle(Point viewedFrom);

	/**
	 * Given a Point (expressed in global coordinates) which this AbstractShape
	 * is viewed from, compute the solid angle which this AbstractShape subtends
	 * as seen from that Point -- <em>assuming</em> that this AbstractShape
	 * looks like a sphere.
	 * 
	 * @param viewedFrom
	 * @param sphereRadius
	 * @return
	 */
	protected double computeSolidAngle_sphere(Point viewedFrom, double sphereRadius) {

		final double d = new Vector(getObjectZero(), viewedFrom).getMagnitude();
		final double r = sphereRadius;

		return 2d * PI * ( 1d - sqrt(d * d - r * r) / d );
	}

	/**
	 * Given a Point (expressed in global coordinates), calculate the
	 * SurfaceDescriptor of the point on the surface nearest to that given
	 * point.
	 * 
	 * @param point
	 * @return
	 */
	public abstract SurfaceDescriptor getSurfaceNearestTo(Point point);

	/**
	 * Given a pair of surface parameters, compute a resulting 3-D point on the
	 * surface.
	 * 
	 * @param param
	 * @return
	 */
	public abstract Point getLocalSurfaceFromParam(Point2D param);

	/**
	 * Given a 3-D point (in object-local coordinates) on this surface, compute
	 * the equivalent 2-D surface-parameters.
	 * 
	 * @param surface
	 * @return
	 */
	public abstract Point2D getParamFromLocalSurface(Point surface);

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
}
