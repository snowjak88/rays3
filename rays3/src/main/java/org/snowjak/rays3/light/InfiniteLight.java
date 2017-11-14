package org.snowjak.rays3.light;

import java.util.List;

import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.transform.Transform;

/**
 * Represents a {@link Light} that is very, very far away -- for all practical
 * purposes, at an infinite distance.
 * 
 * @author snowjak88
 */
public class InfiniteLight extends Light {

	public InfiniteLight(Spectrum unitRadiance, List<Transform> worldToLocal) {
		super(unitRadiance, FalloffType.CONSTANT, worldToLocal);
	}

	@Override
	public Vector sampleLightVector(Point towards) {

		final Vector lightLocation = new Vector(this.getObjectZero());
		return new Vector(towards).subtract(lightLocation);
	}

	@Override
	public double probabilitySampleVector(Point towards, Vector sampledVector) {

		return 1d;
	}

}
