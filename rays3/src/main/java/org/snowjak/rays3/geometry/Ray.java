package org.snowjak.rays3.geometry;

/**
 * Represents a direction + origin in 3-space.
 * 
 * @author snowjak88
 */
public class Ray {

	private final Point		origin;
	private final Vector	direction;
	private final double	currT;
	private final double	minT, maxT;
	private final int		depth;

	/**
	 * Initialize a new Ray:
	 * <ul>
	 * <li>At: <code>( 0, 0, 0 )</code></li>
	 * <li>Toward: <code>( 0, 0, 0 )</code></li>
	 * <li>Depth: <code>0</code></li>
	 * <li>Curr-T: {@link Double#POSITIVE_INFINITY}</li>
	 * <li>Min-T: {@link Double#POSITIVE_INFINITY}</li>
	 * <li>Max-T: {@link Double#NEGATIVE_INFINITY}</li>
	 * </ul>
	 */
	public Ray() {
		this.origin = new Point();
		this.direction = new Vector();
		this.currT = Double.POSITIVE_INFINITY;
		this.minT = Double.POSITIVE_INFINITY;
		this.maxT = Double.NEGATIVE_INFINITY;
		this.depth = 0;
	}

	/**
	 * Initialize a new Ray with the given origin and direction.
	 * <ul>
	 * <li>Depth: <code>0</code></li>
	 * <li>Curr-T: {@link Double#POSITIVE_INFINITY}</li>
	 * <li>Min-T: {@link Double#POSITIVE_INFINITY}</li>
	 * <li>Max-T: {@link Double#NEGATIVE_INFINITY}</li>
	 * </ul>
	 * 
	 * @param origin
	 * @param direction
	 */
	public Ray(Point origin, Vector direction) {
		this(origin, direction, 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	/**
	 * Initialize a new Ray with the given origin, direction, and curr-T.
	 * <ul>
	 * <li>Depth: <code>0</code></li>
	 * <li>Min-T: {@link Double#POSITIVE_INFINITY}</li>
	 * <li>Max-T: {@link Double#NEGATIVE_INFINITY}</li>
	 * </ul>
	 * 
	 * @param origin
	 * @param direction
	 * @param currT
	 */
	public Ray(Point origin, Vector direction, double currT) {
		this(origin, direction, 0, currT);
	}

	/**
	 * Initialize a new Ray with the given origin and direction, taking another
	 * Ray as its "parent".
	 * <ul>
	 * <li>Depth: <code>parent.depth + 1</code></li>
	 * <li>Curr-T: {@link Double#POSITIVE_INFINITY}</li>
	 * <li>Min-T: {@link Double#POSITIVE_INFINITY}</li>
	 * <li>Max-T: {@link Double#NEGATIVE_INFINITY}</li>
	 * </ul>
	 * 
	 * @param origin
	 * @param direction
	 * @param parent
	 */
	public Ray(Point origin, Vector direction, Ray parent) {
		this(origin, direction, parent.depth + 1);
	}

	/**
	 * Initialize a new Ray with the given origin, direction, and
	 * <code>t</code>, taking another Ray as its "parent".
	 * <ul>
	 * <li>Depth: <code>parent.depth + 1</code></li>
	 * <li>Min-T: {@link Double#POSITIVE_INFINITY}</li>
	 * <li>Max-T: {@link Double#NEGATIVE_INFINITY}</li>
	 * </ul>
	 * 
	 * @param origin
	 * @param direction
	 * @param currT
	 * @param parent
	 */
	public Ray(Point origin, Vector direction, double currT, Ray parent) {
		this(origin, direction, parent.depth + 1, currT);
	}

	/**
	 * Initialize a new Ray with the given origin, direction, and depth.
	 * <ul>
	 * <li>Curr-T: {@link Double#POSITIVE_INFINITY}</li>
	 * <li>Min-T: {@link Double#POSITIVE_INFINITY}</li>
	 * <li>Max-T: {@link Double#NEGATIVE_INFINITY}</li>
	 * </ul>
	 * 
	 * @param origin
	 * @param direction
	 * @param parent
	 */
	public Ray(Point origin, Vector direction, int depth) {
		this(origin, direction, depth, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	/**
	 * Initialize a new Ray with the given origin, direction, depth, and curr-T.
	 * <ul>
	 * <li>Min-T: {@link Double#POSITIVE_INFINITY}</li>
	 * <li>Max-T: {@link Double#NEGATIVE_INFINITY}</li>
	 * </ul>
	 * 
	 * @param origin
	 * @param direction
	 * @param depth
	 * @param currT
	 */
	public Ray(Point origin, Vector direction, int depth, double currT) {
		this(origin, direction, depth, currT, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	/**
	 * Initialize a new Ray with the given origin, direction, depth, curr-T,
	 * min-T, and max-T.
	 * 
	 * @param origin
	 * @param direction
	 * @param depth
	 * @param currT
	 * @param minT
	 * @param maxT
	 */
	public Ray(Point origin, Vector direction, int depth, double currT, double minT, double maxT) {
		this.origin = origin;
		this.direction = direction.normalize();
		this.depth = depth;
		this.currT = currT;
		this.minT = minT;
		this.maxT = maxT;
	}

	/**
	 * Given a value <code>t</code>, calculate the resulting {@link Point} along
	 * this Ray.
	 * 
	 * @param t
	 * @return
	 */
	public Point getPointAlong(double t) {

		return origin.add(new Point(direction.multiply(t)));
	}

	/**
	 * Using {@link #getCurrT()}, calculate the resulting {@link Point} along
	 * this Ray.
	 * 
	 * @return
	 * @see {@link #getPointAlong(double)}, {@link #getCurrT()}
	 */
	public Point getPointAlong() {

		return getPointAlong(currT);
	}

	/**
	 * Get this Ray's origin-point.
	 * 
	 * @return
	 */
	public Point getOrigin() {

		return origin;
	}

	/**
	 * Get this Ray's direction.
	 * 
	 * @return
	 */
	public Vector getDirection() {

		return direction;
	}

	/**
	 * Get this Ray's "depth" -- a number used to indicate how many degrees of
	 * recursion this Ray is down the ray-tracing tree.
	 * 
	 * @return
	 */
	public int getDepth() {

		return depth;
	}

	/**
	 * Get this Ray's "curr-T" -- a value indicating where the "current"
	 * intersection-point lies along this Ray. Useful for reporting on
	 * interactions with objects with "hard" surface-interfaces.
	 * 
	 * @return
	 */
	public double getCurrT() {

		return currT;
	}

	/**
	 * Get this Ray's min-T parameter -- a value which can be used to indicate
	 * the closest-intersected point along this Ray. Useful for reporting on
	 * interactions with volumes.
	 * 
	 * @return
	 */
	public double getMinT() {

		return minT;
	}

	/**
	 * Get this Ray's max-T parameter -- a value which can be used to indicate
	 * the furthest-intersected point along this Ray. Useful for reporting on
	 * interactions with volumes.
	 * 
	 * @return
	 */
	public double getMaxT() {

		return maxT;
	}

	@Override
	public String toString() {

		return "Ray [origin=" + origin.toString() + ", direction=" + direction.toString() + ", currT="
				+ Double.toString(currT) + ", minT=" + Double.toString(minT) + ", maxT=" + Double.toString(maxT)
				+ ", depth=" + depth + "]";
	}
}
