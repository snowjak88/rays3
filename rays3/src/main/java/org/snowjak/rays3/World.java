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
					.filter(p -> p.getBsdf().isEmissive())
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

	/**
	 * Search for the closest {@link Interaction} between the given {@link Ray}
	 * and the given {@link Primitive}.
	 * <p>
	 * If <code>primitive == null</code>, this method simply chains to
	 * {@link #getClosestInteraction(Ray)}.
	 * </p>
	 * 
	 * @param ray
	 * @param primitive
	 * @return
	 */
	public Optional<Interaction> getClosestInteraction(Ray ray, Primitive primitive) {

		if (primitive == null)
			return getClosestInteraction(ray);

		if (!primitive.isInteracting(ray))
			return Optional.empty();

		return Optional.ofNullable(primitive.getIntersection(ray));
	}
}
