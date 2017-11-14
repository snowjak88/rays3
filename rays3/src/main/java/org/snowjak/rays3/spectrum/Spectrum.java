package org.snowjak.rays3.spectrum;

/**
 * Represents a measurement of radiant energy distributed across several
 * wavelengths.
 * 
 * @author snowjak88
 */
public interface Spectrum {

	/**
	 * @return <code>true</code> if this Spectrum has 0 energy associated with
	 *         it.
	 */
	public boolean isBlack();

	/**
	 * Compute the result of adding this Spectrum's energy with another.
	 */
	public Spectrum add(Spectrum addend);

	/**
	 * Compute the result of multiplying this Spectrum's energy with another.
	 * (Usually used to model filtering or fractional selection of radiant
	 * energy.)
	 */
	public Spectrum multiply(Spectrum multiplicand);

	/**
	 * Compute the result of multiplying this Spectrum's energy by a scalar
	 * factor -- essentially scaling this Spectrum's energy linearly.
	 * 
	 * @param scalar
	 * @return
	 */
	public Spectrum multiply(double scalar);

	/**
	 * Convert this Spectrum to a RGB-trio for subsequent display.
	 */
	public RGB toRGB();
}
