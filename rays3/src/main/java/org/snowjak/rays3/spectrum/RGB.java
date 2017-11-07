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

	private double			red, green, blue;

	public RGB() {
		this(0d, 0d, 0d);
	}

	public RGB(double red, double green, double blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public RGB add(RGB addend) {

		return new RGB(this.red + addend.red, this.green + addend.green, this.blue + addend.blue);
	}

	public RGB subtract(RGB subtrahend) {

		return new RGB(this.red - subtrahend.red, this.green - subtrahend.green, this.blue - subtrahend.blue);
	}

	public RGB multiply(double multiplicand) {

		return new RGB(this.red * multiplicand, this.green * multiplicand, this.blue * multiplicand);
	}

	public RGB multiply(RGB multiplicand) {

		return new RGB(this.red * multiplicand.red, this.green * multiplicand.green, this.blue * multiplicand.blue);
	}

	public RGB divide(double divisor) {

		return new RGB(this.red / divisor, this.green / divisor, this.blue / divisor);
	}

	public RGB divide(RGB divisor) {

		return new RGB(this.red / divisor.red, this.green / divisor.green, this.blue / divisor.blue);
	}

	/**
	 * @return a new RGB trio with each component clamped to <code>[0,1]</code>
	 */
	public RGB clamp() {

		return new RGB(clampFraction(red), clampFraction(green), clampFraction(blue));
	}

	public double getRed() {

		return red;
	}

	public double getGreen() {

		return green;
	}

	public double getBlue() {

		return blue;
	}

	private double clampFraction(double v) {

		return FastMath.max(FastMath.min(v, 1d), 0d);
	}

}
