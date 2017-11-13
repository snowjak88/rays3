package org.snowjak.rays3.geometry.shape;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.intersect.Interactable;
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
