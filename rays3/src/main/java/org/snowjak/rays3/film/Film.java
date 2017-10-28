package org.snowjak.rays3.film;

import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Represents a piece of film exposed to radiance to produce an image.
 * <p>
 * Film is responsible for one thing:
 * <ol>
 * <li>Recording incoming radiance {@link Spectrum} objects in its internal
 * representation</li>
 * </ol>
 * What that representation looks like is implementation-specific.
 * </p>
 * 
 * @author snowjak88
 */
public interface Film {

	/**
	 * Add the given {@link Spectrum} (obtained using the specified
	 * {@link Sample}) to this Film's internal representation.
	 * 
	 * @param sample
	 * @param radiance
	 */
	public void addSample(Sample sample, Spectrum radiance);
}
