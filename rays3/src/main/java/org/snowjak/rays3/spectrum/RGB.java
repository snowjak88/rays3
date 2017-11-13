package org.snowjak.rays3.spectrum;

import org.apache.commons.math3.util.FastMath;

/**
 * Simple holder for a trio of RGB values.
 * <p>
 * <strong>Note</strong> that these components are not clamped in any way --
 * they may take any value, positive or negative.
 * </p>
 * 
 * @author snowjak88
 */
public class RGB {

	/**
	 * <code>RGB(0,0,0)</code>
	 */
	public static final RGB	BLACK	= new RGB(0d, 0d, 0d);
	/**
	 * <code>RGB(1,0,0)</code>
	 */
	public static final RGB	RED		= new RGB(1d, 0d, 0d);
	/**
	 * <code>RGB(0,1,0)</code>
	 */
	public static final RGB	GREEN	= new RGB(0d, 1d, 0d);
	/**
	 * <code>RGB(0,0,1)</code>
	 */
	public static final RGB	BLUE	= new RGB(0d, 0d, 1d);
	/**
	 * <code>RGB(1,1,1)</code>
	 */
	public static final RGB	WHITE	= new RGB(1d, 1d, 1d);

	private double[]		rgb;

	/**
	 * Construct a new RGB trio from an HSL trio.
	 * 
	 * @param hue
	 *            hue-angle, given in <strong>degrees</strong>
	 * @param saturation
	 *            saturation-value, given in <code>[0,1]</code>
	 * @param lightness
	 *            lightness-value, given in <code>[0,1]</code>
	 * @return
	 */
	public static RGB fromHSL(double hue, double saturation, double lightness) {

		final double chroma = ( 1d - FastMath.abs(2d * lightness - 1) ) * saturation;

		final double h_prime = hue / 60d;

		final double x = chroma * ( 1d - FastMath.abs(( h_prime % 2 ) - 1) );

		final double r1, g1, b1;
		if (h_prime >= 0d && h_prime <= 1d) {
			r1 = chroma;
			g1 = x;
			b1 = 0d;
		} else if (h_prime >= 1d && h_prime <= 2d) {
			r1 = x;
			g1 = chroma;
			b1 = 0d;
		} else if (h_prime >= 2d && h_prime <= 3d) {
			r1 = 0d;
			g1 = chroma;
			b1 = x;
		} else if (h_prime >= 3d && h_prime <= 4d) {
			r1 = 0d;
			g1 = x;
			b1 = chroma;
		} else if (h_prime >= 4d && h_prime <= 5d) {
			r1 = x;
			g1 = 0d;
			b1 = chroma;
		} else if (h_prime >= 5d && h_prime <= 6d) {
			r1 = chroma;
			g1 = 0d;
			b1 = x;
		} else {
			r1 = 0d;
			g1 = 0d;
			b1 = 0d;
		}

		final double m = lightness - chroma / 2d;

		return new RGB(r1 + m, g1 + m, b1 + m);
	}

	public RGB() {
		this(0d, 0d, 0d);
	}

	public RGB(double red, double green, double blue) {
		this.rgb = new double[] { red, green, blue };
	}

	public RGB add(RGB addend) {

		return new RGB(this.rgb[0] + addend.rgb[0], this.rgb[1] + addend.rgb[1], this.rgb[2] + addend.rgb[2]);
	}

	public RGB subtract(RGB subtrahend) {

		return new RGB(this.rgb[0] - subtrahend.rgb[0], this.rgb[1] - subtrahend.rgb[1],
				this.rgb[2] - subtrahend.rgb[2]);
	}

	public RGB multiply(double multiplicand) {

		return new RGB(this.rgb[0] * multiplicand, this.rgb[1] * multiplicand, this.rgb[2] * multiplicand);
	}

	public RGB multiply(RGB multiplicand) {

		return new RGB(this.rgb[0] * multiplicand.rgb[0], this.rgb[1] * multiplicand.rgb[1],
				this.rgb[2] * multiplicand.rgb[2]);
	}

	public RGB divide(double divisor) {

		return new RGB(this.rgb[0] / divisor, this.rgb[1] / divisor, this.rgb[2] / divisor);
	}

	public RGB divide(RGB divisor) {

		return new RGB(this.rgb[0] / divisor.rgb[0], this.rgb[1] / divisor.rgb[1], this.rgb[2] / divisor.rgb[2]);
	}

	/**
	 * @return a new RGB trio with each component clamped to <code>[0,1]</code>
	 */
	public RGB clamp() {

		return new RGB(clampFraction(rgb[0]), clampFraction(rgb[1]), clampFraction(rgb[2]));
	}

	public double getRed() {

		return rgb[0];
	}

	public double getGreen() {

		return rgb[1];
	}

	public double getBlue() {

		return rgb[2];
	}

	/**
	 * <strong>Note</strong> that the <code>double</code> array returned here is
	 * the backing array of this RGB object. Modifying this array directly is
	 * considered to be unsafe, as it breaks the "value-object" paradigm.
	 * 
	 * @return an array of 3 <code>double</code>s:
	 *         <code>{ red, green, blue }</code>
	 */
	public double[] getComponents() {

		return rgb;
	}

	private double clampFraction(double v) {

		return FastMath.max(FastMath.min(v, 1d), 0d);
	}

}
