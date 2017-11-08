package org.snowjak.rays3.spectrum;

/**
 * Represents a {@link Spectrum} using a simple RGB trio.
 * 
 * @author snowjak88
 */
public class RGBSpectrum implements Spectrum {

	/**
	 * Represents a 0-energy Spectrum.
	 */
	public static final RGBSpectrum	BLACK	= new RGBSpectrum(RGB.BLACK);
	/**
	 * Represents a 1.0-energy Spectrum. (i.e., equivalent to {@link RGB#WHITE})
	 */
	public static final RGBSpectrum	WHITE	= new RGBSpectrum(RGB.WHITE);

	private RGB						rgb;

	public RGBSpectrum() {
		this(RGB.BLACK);
	}

	public RGBSpectrum(RGB rgb) {
		this.rgb = rgb;
	}

	@Override
	public Spectrum add(Spectrum addend) {

		return new RGBSpectrum(rgb.add(addend.toRGB()));
	}

	@Override
	public Spectrum multiply(Spectrum multiplicand) {

		return new RGBSpectrum(rgb.multiply(multiplicand.toRGB()));
	}

	@Override
	public Spectrum multiply(double scalar) {

		return new RGBSpectrum(rgb.multiply(scalar));
	}

	@Override
	public RGB toRGB() {

		return rgb;
	}

}
