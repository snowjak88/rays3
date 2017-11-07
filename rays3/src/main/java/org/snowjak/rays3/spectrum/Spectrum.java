package org.snowjak.rays3.spectrum;

/**
 * Represents a measurement of radiant energy distributed across several
 * wavelengths.
 * 
 * @author snowjak88
 */
public interface Spectrum {

	/**
	 * Compute the result of adding this Spectrum's energy with another.
	 */
	public Spectrum add(Spectrum addend);

	/**
	 * Convert this Spectrum to a RGB-trio for subsequent display.
	 */
	public RGB toRGB();
}
