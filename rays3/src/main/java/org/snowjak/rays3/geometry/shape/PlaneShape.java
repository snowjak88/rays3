package org.snowjak.rays3.geometry.shape;

import java.lang.Double;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.Global;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.transform.Transform;

/**
 * Represents an unbounded plane, locally positioned so that its surface passes
 * through (0,0,0) and its normal points along {@link Vector#J}.
 * 
 * @author snowjak88
 */
public class PlaneShape extends AbstractShape {

	/**
	 * Create a new PlaneShape with the default orientation (see
	 * {@link PlaneShape} class description).
	 */
	public PlaneShape() {
		this(Collections.emptyList());
	}

	/**
	 * Create a new PlaneShape with the given set of transformations.
	 * 
	 * @param worldToLocal
	 */
	public PlaneShape(List<Transform> worldToLocal) {
		super(worldToLocal);
	}

	@Override
	public boolean isInteracting(Ray ray) {

		return isLocalInteracting(worldToLocal(ray));
	}

	@Override
	public boolean isLocalInteracting(Ray ray) {

		// The only way this Ray will fail to interact with this Plane anywhere
		// is if its origin y-coordinate and direction y-coordinate are of the
		// same sign (i.e., it is pointing away from the plane), or else if its
		// direction y-coordinate is 0 (i.e., it is pointing parallel to the
		// plane).
		//
		return !( Global.isNear(FastMath.signum(ray.getOrigin().getY()), ray.getDirection().getY()) )
				&& !( Global.isNear(ray.getDirection().getY(), 0d) );
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

		final double t = -ray.getOrigin().getY() / ray.getDirection().getY();

		if (t < Global.DOUBLE_TOLERANCE || Double.isNaN(t) || Global.isNear(t, 0d))
			return null;

		Ray intersectingRay = new Ray(ray.getOrigin(), ray.getDirection(), ray.getDepth(), t, t, t, ray.getWeight());
		Point intersectionPoint = intersectingRay.getPointAlong();
		Normal normal = new Normal(Vector.J);
		Point2D surfaceParam = getParamFromLocalSurface(intersectionPoint);

		return new Interaction(intersectionPoint, intersectingRay, normal, surfaceParam, null);
	}

	@Override
	public Point sampleSurfacePoint() {

		final double x = ( Global.RND.nextDouble() - 0.5 ) * Double.MAX_VALUE;
		final double y = 0d;
		final double z = ( Global.RND.nextDouble() - 0.5 ) * Double.MAX_VALUE;
		
		return localToWorld(new Point(x, y, z));
	}

	@Override
	public Point sampleSurfacePoint(Point facing) {

		return sampleSurfacePoint();
	}

	@Override
	public SurfaceDescriptor getSurfaceNearestTo(Point point) {

		Point localPoint = worldToLocal(point);

		Point surfacePoint = new Point(localPoint.getX(), 0d, localPoint.getZ());

		return new SurfaceDescriptor(localToWorld(surfacePoint), localToWorld(new Normal(Vector.J)),
				getParamFromLocalSurface(surfacePoint));
	}

	@Override
	public Point getLocalSurfaceFromParam(Point2D param) {

		return new Point(param.getX(), 0d, param.getY());
	}

	@Override
	public Point2D getParamFromLocalSurface(Point surface) {

		return new Point2D(surface.getX(), surface.getZ());
	}

}
