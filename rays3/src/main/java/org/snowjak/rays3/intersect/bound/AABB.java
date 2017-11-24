package org.snowjak.rays3.intersect.bound;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.transform.Transform;

/**
 * An <strong>A</strong>xis-<strong>A</strong>ligned <strong>B</strong>ounding
 * <strong>B</strong>ox is an "acceleration structure" used to help speed up the
 * rejection of Shapes when performing Ray-intersection testing. Unlike a
 * more-general "bounding box", an AABB is always axis-aligned (i.e., it can be
 * represented by only 2 points in global coordinates).
 * 
 * @author snowjak88
 */
public class AABB {

	private Point minExtent, maxExtent;

	/**
	 * Given an existing {@link BoundingSphere}, construct an AABB that will
	 * fully-enclose the same volume bounded by this sphere.
	 * 
	 * @param fromSphere
	 */
	public AABB(BoundingSphere fromSphere) {
		this(Arrays.asList(
				new Point(fromSphere.getCenter().getX() - fromSphere.getRadius(),
						fromSphere.getCenter().getY() - fromSphere.getRadius(),
						fromSphere.getCenter().getZ() - fromSphere.getRadius()),
				new Point(fromSphere.getCenter().getX() + fromSphere.getRadius(),
						fromSphere.getCenter().getY() + fromSphere.getRadius(),
						fromSphere.getCenter().getZ() + fromSphere.getRadius())));
	}

	/**
	 * Given an existing AABB (assumed to be given in object-local coordinates),
	 * and a {@link List} of {@link Transform}s (assumed to give the proper
	 * order for local-to-world transformation), compute the corresponding AABB
	 * in global coordinates.
	 * 
	 * @param copyOf
	 * @param localToWorld
	 */
	public AABB(AABB copyOf, List<Transform> localToWorld) {
		this(copyOf.getCorners(), localToWorld);
	}

	/**
	 * Given a collection of {@link Point}s (assumed to be expressed in
	 * object-local coordinates), and a {@link List} of {@link Transform}s
	 * (assumed to give the proper order for local-to-world transformation),
	 * compute the AABB in global coordinates that encompasses these Points.
	 * 
	 * @param localPoints
	 * @param localToWorld
	 */
	public AABB(Collection<Point> localPoints, List<Transform> localToWorld) {

		this(localPoints.stream().map(p -> {
			for (Transform t : localToWorld)
				p = t.localToWorld(p);
			return p;
		}).collect(Collectors.toCollection(LinkedList::new)));
	}

	/**
	 * Given a collection of {@link Point}s (assumed to be expressed in global
	 * coordinates), compute the AABB that encompasses them all.
	 * 
	 * @param globalPoints
	 */
	public AABB(Collection<Point> globalPoints) {
		double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

		for (Point p : globalPoints) {
			minX = FastMath.min(minX, p.getX());
			minY = FastMath.min(minY, p.getY());
			minZ = FastMath.min(minZ, p.getZ());

			maxX = FastMath.max(maxX, p.getX());
			maxY = FastMath.max(maxY, p.getY());
			maxZ = FastMath.max(maxZ, p.getZ());
		}

		this.minExtent = new Point(minX, minY, minZ);
		this.maxExtent = new Point(maxX, maxY, maxZ);
	}

	/**
	 * Create a new AxisAlignedBoundingBox, using the given extents (assumed to
	 * be expressed in global coordinates).
	 * 
	 * @param minExtent
	 * @param maxExtent
	 */
	public AABB(Point minExtent, Point maxExtent) {
		this.minExtent = minExtent;
		this.maxExtent = maxExtent;
	}

	/**
	 * Given a set of {@link AABB}s, compute the AABB that encompasses them all.
	 * 
	 * @param boundingBoxes
	 * @return
	 */
	public static AABB union(Collection<AABB> boundingBoxes) {

		return new AABB(boundingBoxes.stream().collect(LinkedList::new, (l, aabb) -> {
			l.add(aabb.minExtent);
			l.add(aabb.maxExtent);
		}, LinkedList::addAll));
	}

	/**
	 * @return a {@link Collection} containing all 8 corners of this AABB, in no
	 *         particular order.
	 */
	public Collection<Point> getCorners() {

		return Arrays.asList(minExtent, maxExtent, new Point(minExtent.getX(), minExtent.getY(), maxExtent.getZ()),
				new Point(minExtent.getX(), maxExtent.getY(), minExtent.getZ()),
				new Point(minExtent.getX(), maxExtent.getY(), maxExtent.getZ()),
				new Point(maxExtent.getX(), minExtent.getY(), minExtent.getZ()),
				new Point(maxExtent.getX(), minExtent.getY(), maxExtent.getZ()),
				new Point(maxExtent.getX(), maxExtent.getY(), minExtent.getZ()));
	}

	/**
	 * Given a {@link Ray} (expressed in global coordinates), determine if that
	 * Ray intersects this AABB.
	 * 
	 * @param ray
	 * @return
	 */
	public boolean isIntersecting(Ray ray) {

		double temp;

		double tmin = ( minExtent.getX() - ray.getOrigin().getX() ) / ray.getDirection().getX();
		double tmax = ( maxExtent.getX() - ray.getOrigin().getX() ) / ray.getDirection().getX();

		if (tmin > tmax) {
			temp = tmin;
			tmin = tmax;
			tmax = temp;
		}

		double tymin = ( minExtent.getY() - ray.getOrigin().getY() ) / ray.getDirection().getY();
		double tymax = ( maxExtent.getY() - ray.getOrigin().getY() ) / ray.getDirection().getY();

		if (tymin > tymax) {
			temp = tymin;
			tymin = tymax;
			tymax = temp;
		}

		if (( tmin > tymax ) || ( tymin > tmax ))
			return false;

		if (tymin > tmin)
			tmin = tymin;

		if (tymax < tmax)
			tmax = tymax;

		double tzmin = ( minExtent.getZ() - ray.getOrigin().getZ() ) / ray.getDirection().getZ();
		double tzmax = ( maxExtent.getZ() - ray.getOrigin().getZ() ) / ray.getDirection().getZ();

		if (tzmin > tzmax) {
			temp = tzmin;
			tzmin = tzmax;
			tzmax = temp;
		}

		if (( tmin > tzmax ) || ( tzmin > tmax ))
			return false;

		// Commented out because non-effective.
		/*
		 * if (tzmin > tmin) tmin = tzmin;
		 * 
		 * if (tzmax < tmax) tmax = tzmax;
		 */

		return true;
	}

	public Point getMinExtent() {

		return minExtent;
	}

	public Point getMaxExtent() {

		return maxExtent;
	}
}
