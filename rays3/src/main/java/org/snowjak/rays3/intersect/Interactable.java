package org.snowjak.rays3.intersect;

import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.transform.Transformable;

/**
 * Denotes that an object can be interacted with (receiving {@link Ray}s and
 * producing {@link Interaction}s).
 * 
 * @author snowjak88
 */
public interface Interactable extends Transformable {

	/**
	 * Given a {@link Ray} (expressed in global coordinates), determine whether
	 * the Ray comes close to intersecting this object. (Implementations will
	 * probably use an {@link AABB} to quickly cull Rays that completely miss.)
	 * 
	 * @param ray
	 * @return
	 */
	public boolean isInteracting(Ray ray);

	/**
	 * Given a {@link Ray} (expressed in local coordinates), determine whether
	 * the Ray intersects this object or not. (Implementations will probably use
	 * an {@link AABB} to quickly cull Rays that completely miss.)
	 * 
	 * @param ray
	 * @return
	 */
	public boolean isLocalInteracting(Ray ray);

	/**
	 * Given a {@link Ray} (expressed in global coordinates), compute the
	 * {@link Interaction} describing the point at which that ray intersects the
	 * object, or <code>null</code> if no such point exists.
	 * 
	 * @param ray
	 * @return
	 */
	public Interaction getIntersection(Ray ray);

	/**
	 * Given a {@link Ray} (expressed in object-local coordinates), compute the
	 * {@link Interaction} describing the point at which that ray intersects the
	 * object, or <code>null</code> if no such point exists.
	 * 
	 * @param ray
	 * @return
	 */
	public Interaction getLocalIntersection(Ray ray);
}
