package org.snowjak.rays3;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.shape.Primitive;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.light.Light;

/**
 * Describes the world as a whole.
 * 
 * @author snowjak88
 */
public class World {

	private Collection<Primitive>	primitives	= new LinkedList<>();
	private Collection<Light>		lights		= new LinkedList<>();

	public Collection<Primitive> getPrimitives() {

		return primitives;
	}

	public Collection<Primitive> getEmissives() {

		return primitives
				.stream()
					.filter(p -> p.getBdsf().isEmissive())
					.collect(Collectors.toCollection(LinkedList::new));
	}

	public Collection<Light> getLights() {

		return lights;
	}

	/**
	 * Search for the closest interacting {@link Primitive} in this World that
	 * the given {@link Ray} interacts with (that is not behind the Ray).
	 * <p>
	 * <strong>Note</strong> that this method executes on the same thread as the
	 * caller.
	 * </p>
	 * 
	 * @param ray
	 * @return
	 */
	public Optional<Interaction> getClosestInteraction(Ray ray) {

		return getPrimitives()
				.stream()
					.filter(p -> p.isInteracting(ray))
					.map(p -> p.getIntersection(ray))
					.filter(p -> p != null)
					.filter(p -> p.getInteractingRay().getCurrT() > 0d)
					.sorted((i1, i2) -> Double.compare(i1.getInteractingRay().getCurrT(),
							i2.getInteractingRay().getCurrT()))
					.findFirst();
	}
}
