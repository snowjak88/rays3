package org.snowjak.rays3.light;

import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Represents a {@link Light} that is very, very far away -- for all practical
 * purposes, at an infinite distance.
 * 
 * @author snowjak88
 */
public class InfiniteLight extends Light {

	public InfiniteLight(Spectrum unitRadiance) {
		super(unitRadiance, FalloffType.CONSTANT);
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
