package org.snowjak.rays3;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.snowjak.rays3.bxdf.BDSF;
import org.snowjak.rays3.geometry.shape.Primitive;

/**
 * Describes the world as a whole.
 * 
 * @author snowjak88
 */
public class World {

	private Collection<Primitive>	primitives	= new LinkedList<>();
	private Collection<Primitive>	lights		= null;

	public Collection<Primitive> getPrimitives() {

		synchronized (this) {
			this.lights = null;

			return primitives;
		}
	}

	/**
	 * Compute the list of those {@link Primitive}s that have configured their
	 * {@link BDSF}s to be "emissive" -- i.e., such that their
	 * <code>{@link BDSF#hasEmissiveRadiance()} == true</code>.
	 */
	public Collection<Primitive> getLights() {

		synchronized (this) {
			if (lights != null)
				return lights;

			lights = getPrimitives().stream().filter(p -> p.getBdsf().hasEmissiveRadiance()).collect(
					Collectors.toCollection(LinkedList::new));
			return lights;
		}
	}
}
