package org.snowjak.rays3.transform;

import java.util.List;

import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;

/**
 * Indicates that an object is associated with one or more {@link Transform}s,
 * and can use those Transforms to convert points in its object-local
 * coordinate-space to world/global and vice versa.
 * 
 * @author snowjak88
 */
public interface Transformable {

	public List<Transform> getWorldToLocalTransforms();

	public List<Transform> getLocalToWorldTransforms();

	/**
	 * Transform the given Point from world- to this-object-local coordinates.
	 * 
	 * @param point
	 * @return
	 */
	public default Point worldToLocal(Point point) {

		Point working = point;
		for (Transform t : getWorldToLocalTransforms())
			working = t.worldToLocal(working);

		return working;
	}

	/**
	 * Transform the given Point from this-object-local to world-coordinates.
	 * 
	 * @param point
	 * @return
	 */
	public default Point localToWorld(Point point) {

		Point working = point;
		for (Transform t : getLocalToWorldTransforms())
			working = t.localToWorld(working);

		return working;
	}

	/**
	 * Transform the given Vector from world- to this-object-local coordinates.
	 * 
	 * @param vector
	 * @return
	 */
	public default Vector worldToLocal(Vector vector) {

		Vector working = vector;
		for (Transform t : getWorldToLocalTransforms())
			working = t.worldToLocal(working);

		return working;
	}

	/**
	 * Transform the given Vector from this-object-local to world-coordinates.
	 * 
	 * @param vector
	 * @return
	 */
	public default Vector localToWorld(Vector vector) {

		Vector working = vector;
		for (Transform t : getLocalToWorldTransforms())
			working = t.localToWorld(working);

		return working;
	}

	/**
	 * Transform the given Ray from world- to this-object-local coordinates.
	 * 
	 * @param ray
	 * @return
	 */
	public default Ray worldToLocal(Ray ray) {

		Ray working = ray;
		for (Transform t : getWorldToLocalTransforms())
			working = t.worldToLocal(working);

		return working;
	}

	/**
	 * Transform the given Ray from this-object-local to world-coordinates.
	 * 
	 * @param ray
	 * @return
	 */
	public default Ray localToWorld(Ray ray) {

		Ray working = ray;
		for (Transform t : getLocalToWorldTransforms())
			working = t.localToWorld(working);

		return working;
	}

	/**
	 * Transform the given Normal from world- to this-object-local coordinates.
	 * 
	 * @param normal
	 * @return
	 */
	public default Normal worldToLocal(Normal normal) {

		Normal working = normal;
		for (Transform t : getWorldToLocalTransforms())
			working = t.worldToLocal(working);

		return working;
	}

	/**
	 * Transform the given Normal from this-object-local to world-coordinates.
	 * 
	 * @param normal
	 * @return
	 */
	public default Normal localToWorld(Normal normal) {

		Normal working = normal;
		for (Transform t : getLocalToWorldTransforms())
			working = t.localToWorld(working);

		return working;
	}
}
