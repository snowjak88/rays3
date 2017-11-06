package org.snowjak.rays3.intersect;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.geometry.Point;
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
	 * Given a collection of {@link Point}s (assumed to be expressed in global
	 * coordinates), compute the AABB that encompasses them all.
	 * 
	 * @param points
	 */
	public AABB(Collection<Point> points) {
		double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

		for (Point p : points) {
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
	 * Given the {@link List} of {@link Transform}s (assumed to be in
	 * local-to-world order), transform this AABB into its local-to-world
	 * equivalent.
	 * 
	 * @param transforms
	 * @return
	 */
	public AABB transform(List<Transform> transforms) {

		Collection<Point> corners = getCorners().stream().map(p -> {
			for (Transform t : transforms)
				p = t.localToWorld(p);
			return p;
		}).collect(Collectors.toCollection(LinkedList::new));

		return new AABB(corners);
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

	public Point getMinExtent() {

		return minExtent;
	}

	public Point getMaxExtent() {

		return maxExtent;
	}
}
