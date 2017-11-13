package org.snowjak.rays3.transform;

import java.util.Iterator;
import java.util.List;

import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;

/**
 * Indicates that an object is associated with one or more {@link Transform}s,
 * and can use those Transforms to convert points in its object-local
 * coordinate-space to world/global and vice versa.
 * 
 * @author snowjak88
 */
public interface Transformable {

	/**
	 * Get the Transforms currently affecting this Transformable, in the order
	 * such that an {@link Iterator} traversing the {@link List} will correctly
	 * give the Transformable's orientation in local coordinates.
	 */
	public List<Transform> getWorldToLocalTransforms();

	/**
	 * Get the Transforms currently affecting this Transformable, in the order
	 * such that an {@link Iterator} traversing the {@link List} will correctly
	 * give the Transformable's orientation in world coordinates.
	 */
	public List<Transform> getLocalToWorldTransforms();

	/**
	 * Add the given {@link Transform} to the end of the list of world-to-local
	 * Transforms (and implicitly to the beginning of the corresponding
	 * local-to-world list).
	 * 
	 * @param transform
	 */
	public void appendTransform(Transform transform);

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

	/**
	 * Transform the given Interaction from world- to this-object-local
	 * coordinates.
	 * 
	 * @param interaction
	 * @return
	 */
	public default Interaction worldToLocal(Interaction interaction) {

		Interaction working = interaction;
		for (Transform t : getWorldToLocalTransforms())
			working = t.worldToLocal(working);

		return working;
	}

	/**
	 * Transform the given Interaction from this-object-local to
	 * world-coordinates.
	 * 
	 * @param interaction
	 * @return
	 */
	public default Interaction localToWorld(Interaction interaction) {

		Interaction working = interaction;
		for (Transform t : getLocalToWorldTransforms())
			working = t.localToWorld(working);

		return working;
	}

	/**
	 * Compute the world-coordinates for the center (<code>{0,0,0}</code>) of
	 * this object's coordinate system.
	 * 
	 * @return
	 */
	public default Point getObjectZero() {

		return localToWorld(new Point(0, 0, 0));
	}
}
