package org.snowjak.rays3;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.shape.AbstractShape;

/**
 * Describes the world as a whole.
 * 
 * @author snowjak88
 */
public class World {

	private Collection<AbstractShape> shapes = new LinkedList<>();

	public Collection<AbstractShape> getShapes() {

		return shapes;
	}

	/**
	 * Filter the list of shapes currently in the world, returning only those
	 * that are not obviously missed by the given Ray. (i.e., the returned
	 * shapes may not actually interact with the Ray, but a quick test reveals
	 * that they <em>might</em>)
	 * 
	 * @param ray
	 * @return
	 */
	public Collection<AbstractShape> getInteractable(Ray ray) {

		return shapes
				.parallelStream()
					.filter(s -> s.isInteracting(ray))
					.collect(Collectors.toCollection(LinkedList::new));
	}
}
