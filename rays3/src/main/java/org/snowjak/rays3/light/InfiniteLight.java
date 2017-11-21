package org.snowjak.rays3.light;

import java.util.List;

import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.transform.Transform;

/**
 * Represents a {@link Light} that is very, very far away -- for all practical
 * purposes, at an infinite distance.
 * 
 * @author snowjak88
 */
public class InfiniteLight extends Light {

	/**
	 * Construct a new InfiniteLight, oriented to point along -{@link Vector#J}.
	 * For purposes of strength, an InfiniteLight is always reckoned as being 1
	 * unit's distance away -- therefore, the assigned <code>unitRadiance</code>
	 * is <strong>not</strong> subject to falloff.
	 * 
	 * @param unitRadiance
	 * @param worldToLocal
	 */
	public InfiniteLight(Spectrum unitRadiance, List<Transform> worldToLocal) {
		super(unitRadiance, FalloffType.CONSTANT, worldToLocal);
	}

	@Override
	public Vector sampleLightVector(Point towards, Sample sample) {

		return localToWorld(Vector.J.negate()).normalize();
	}

	@Override
	public double probabilitySampleVector(Point towards, Vector sampledVector, Sample sample) {

		return 1d;
	}

}
