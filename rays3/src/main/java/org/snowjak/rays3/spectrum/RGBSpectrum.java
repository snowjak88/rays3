package org.snowjak.rays3.spectrum;

/**
 * Represents a {@link Spectrum} using a simple RGB trio.
 * 
 * @author snowjak88
 */
public class RGBSpectrum implements Spectrum {

	private RGB rgb;

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
