package org.snowjak.rays3.transform;

import org.snowjak.rays3.geometry.Matrix;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;

/**
 * Represents a single transformation in 3-space.
 * 
 * @author snowjak88
 */
public interface Transform {

	/**
	 * Transform the given Point into local coordinates.
	 * 
	 * @param point
	 * @return
	 */
	public Point worldToLocal(Point point);

	/**
	 * Transform the given Point into world coordinates.
	 * 
	 * @param point
	 * @return
	 */
	public Point localToWorld(Point point);

	/**
	 * Transform the given Vector into local coordinates.
	 * 
	 * @param vector
	 * @return the transformed Vector
	 */
	public Vector worldToLocal(Vector vector);

	/**
	 * Transform the given Vector into world coordinates.
	 * 
	 * @param vector
	 * @return the transformed Vector
	 */
	public Vector localToWorld(Vector vector);

	/**
	 * Transform the given Ray into local coordinates.
	 * 
	 * @param ray
	 * @return the transformed Ray
	 */
	public Ray worldToLocal(Ray ray);

	/**
	 * Transform the given Ray into world coordinates.
	 * 
	 * @param ray
	 * @return the transformed Ray
	 */
	public Ray localToWorld(Ray ray);

	/**
	 * Transform the given Normal into local coordinates.
	 * 
	 * @param normal
	 * @return the transformed Normal
	 */
	public Normal worldToLocal(Normal normal);

	/**
	 * Transform the given Normal into world coordinates.
	 * 
	 * @param normal
	 * @return the transformed Normal
	 */
	public Normal localToWorld(Normal normal);

	/**
	 * Transform the given Interaction into local coordinates.
	 * 
	 * @param interaction
	 * @return the transformed Interaction
	 */
	public Interaction worldToLocal(Interaction interaction);

	/**
	 * Transform the given Interaction into world coordinates.
	 * 
	 * @param interaction
	 * @return the transformed Interaction
	 */
	public Interaction localToWorld(Interaction interaction);

	/**
	 * Return the Matrix implementing the world-to-local form of this Transform.
	 * 
	 * @return
	 */
	public Matrix getWorldToLocal();

	/**
	 * Return the Matrix implementing the local-to-world form of this Transform.
	 * 
	 * @return
	 */
	public Matrix getLocalToWorld();

}
