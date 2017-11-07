package org.snowjak.rays3.geometry.shape;

import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;

/**
 * Describes a surface at a specific point.
 * <p>
 * <strong>Note</strong>: Unless explicitly stated, all Points, Vectors, etc.
 * are assumed to be given in terms of global coordinates.
 * 
 * @author snowjak88
 */
public class SurfaceDescriptor {

	private Point	point;
	private Normal	normal;
	private Point2D	param;

	public SurfaceDescriptor(Point point, Normal normal, Point2D param) {
		this.point = point;
		this.normal = normal;
	}

	/**
	 * @return the point on the surface
	 */
	public Point getPoint() {

		return point;
	}

	/**
	 * @return the surface-normal at the point
	 */
	public Normal getNormal() {

		return normal;
	}

	/**
	 * @return the parameterization <code>(u,v)</code> of the surface
	 */
	public Point2D getParam() {

		return param;
	}

}
