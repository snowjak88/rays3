package org.snowjak.rays3.sample;

import org.snowjak.rays3.spectrum.Spectrum;

/**
 * A Sampler is responsible for generating useful {@link Sample}s.
 * 
 * @author snowjak88
 */
public interface Sampler {

	/**
	 * Grab the next Sample, or <code>null</code> if no more Samples are left in
	 * this sampling-regime.
	 */
	public Sample getNextSample();

	/**
	 * Given a Sample and the Spectrum that resulted from using it, is that
	 * Sample+Spectrum acceptable?
	 */
	public boolean isSampleAcceptable(Sample sample, Spectrum result);
}
