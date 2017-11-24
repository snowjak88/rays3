package org.snowjak.rays3.intersect.bound;

import static org.apache.commons.math3.util.FastMath.*;

import java.util.Collection;

import org.snowjak.rays3.geometry.Point;

/**
 * A BoundingSphere is another bounding volume, like an {@link AABB}. However, a
 * BoundingSphere has two important differences:
 * <ol>
 * <li>It usually does not fit nearly as well</li>
 * <li>It is easier to compute its overall size relative to a viewer</li>
 * </ol>
 * 
 * @author snowjak88
 */
public class BoundingSphere {

	private final double	radius;
	private final Point		center;

	public BoundingSphere(Collection<Point> points) {
		// @formatter:off
		this(	new Point(points.stream().map(p -> p.getX()).min(Double::compare).orElse(0d),
				          points.stream().map(p -> p.getY()).min(Double::compare).orElse(0d),
				          points.stream().map(p -> p.getZ()).min(Double::compare).orElse(0d)),
				new Point(points.stream().map(p -> p.getX()).max(Double::compare).orElse(0d),
				          points.stream().map(p -> p.getY()).max(Double::compare).orElse(0d),
				          points.stream().map(p -> p.getZ()).max(Double::compare).orElse(0d)));
		// @formatter:on
	}

	public BoundingSphere(AABB aabb) {
		this(aabb.getMinExtent(), aabb.getMaxExtent());
	}

	public BoundingSphere(Point minExtent, Point maxExtent) {
		// @formatter:off
		this(new Point( ( minExtent.getX() + maxExtent.getX() ) / 2d,
					    ( minExtent.getY() + maxExtent.getY() ) / 2d,
					    ( minExtent.getZ() + maxExtent.getZ() ) / 2d),
				sqrt(pow(maxExtent.getX() - minExtent.getX(), 2) +
					 pow(maxExtent.getY() - minExtent.getY(), 2) +
					 pow(maxExtent.getZ() - minExtent.getZ() ,2)) / 2d);
		// @formatter:on
	}

	public BoundingSphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}

	public double getRadius() {

		return radius;
	}

	public Point getCenter() {

		return center;
	}

}
