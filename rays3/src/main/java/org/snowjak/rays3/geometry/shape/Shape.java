package org.snowjak.rays3.geometry.shape;

import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.intersect.Interactable;

/**
 * Defines a 3-d shape procedurally.
 * 
 * @author snowjak88
 */
public interface Shape extends Interactable {

	/**
	 * Given a Point (expressed in global coordinates), calculate the
	 * SurfaceDescriptor of the point on the surface nearest to that given
	 * point.
	 * 
	 * @param point
	 * @return
	 */
	public SurfaceDescriptor getSurfaceNearestTo(Point point);
}
