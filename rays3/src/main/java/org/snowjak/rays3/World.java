package org.snowjak.rays3;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.shape.Primitive;

/**
 * Describes the world as a whole.
 * 
 * @author snowjak88
 */
public class World {

	private Collection<Primitive> primitives = new LinkedList<>();

	public Collection<Primitive> getPrimitives() {

		return primitives;
	}

	/**
	 * Filter the list of {@link Primitive}s currently in the world, returning
	 * only those that are not obviously missed by the given Ray. (i.e., the
	 * returned primitives may not actually interact with the Ray, but a quick
	 * test reveals that they <em>might</em>)
	 * 
	 * @param ray
	 * @return
	 */
	public Collection<Primitive> getInteractable(Ray ray) {

		return primitives.stream().filter(p -> p.isInteracting(ray)).collect(Collectors.toCollection(LinkedList::new));
	}
}
