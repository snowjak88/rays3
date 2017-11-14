package org.snowjak.rays3;

import java.util.Collection;
import java.util.LinkedList;

import org.snowjak.rays3.geometry.shape.Primitive;
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

	public Collection<Light> getLights() {

		return lights;
	}
}
