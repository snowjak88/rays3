package org.snowjak.rays3.transform;

import org.snowjak.rays3.geometry.Matrix;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;

/**
 * Represents a single transformation in 3-space.
 * 
 * @author snowjak88
 */
public interface Transform {

	/**
	 * Transform the given Point.
	 * 
	 * @param point
	 * @return the transformed Point
	 */
	public Point transform(Point point);

	/**
	 * Transform the given Vector.
	 * 
	 * @param vector
	 * @return the transformed Vector
	 */
	public Vector transform(Vector vector);

	/**
	 * Transform the given Ray
	 * 
	 * @param ray
	 * @return the transformed Ray
	 */
	public Ray transform(Ray ray);

	/**
	 * Transform the given Normal
	 * 
	 * @param normal
	 * @return the transformed Normal
	 */
	public Normal transform(Normal normal);

	/**
	 * If possible, compute the inverse of this Transform.
	 * 
	 * @return this Transform's inverse, or <code>null</code> if no such inverse
	 *         is possible
	 */
	public Transform getInverse();

	/**
	 * Return the transformation-matrix that performs this Transform.
	 * 
	 * @return
	 */
	public Matrix getMatrixForm();
}