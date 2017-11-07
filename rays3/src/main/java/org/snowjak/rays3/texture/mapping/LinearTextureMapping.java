package org.snowjak.rays3.texture.mapping;

import org.snowjak.rays3.geometry.Point2D;

/**
 * Represents a linear texture-mapping -- <code>(u,v)</code> are simply scaled
 * linearly to cover the texture's entirety.
 * 
 * @author snowjak88
 */
public class LinearTextureMapping implements TextureMapping {

	private double textureMinU, textureMinV, textureMaxU, textureMaxV;

	/**
	 * Create a new PlanarTextureMapping. The minimum and maximum
	 * texture-coordinates are assumed to lie within <code>(0,0)-(1,1)</code>.
	 */
	public LinearTextureMapping() {
		this(0d, 0d, 1d, 1d);
	}

	/**
	 * Create a new PlanarTextureMapping, specifying the maximum
	 * texture-coordinates (and assuming minimum coordinates are
	 * <code>(0,0)</code>).
	 * 
	 * @param textureMaxU
	 * @param textureMaxV
	 */
	public LinearTextureMapping(double textureMaxU, double textureMaxV) {
		this(0d, 0d, textureMaxU, textureMaxV);
	}

	/**
	 * Create a new PlanarTextureMapping, specifying the minimum and maximum
	 * texture-coordinates.
	 * 
	 * @param textureMinU
	 * @param textureMinV
	 * @param textureMaxU
	 * @param textureMaxV
	 */
	public LinearTextureMapping(double textureMinU, double textureMinV, double textureMaxU, double textureMaxV) {
		this.textureMinU = textureMinU;
		this.textureMinV = textureMinV;
		this.textureMaxU = textureMaxU;
		this.textureMaxV = textureMaxV;
	}

	@Override
	public Point2D map(Point2D point) {

		return new Point2D(scale(point.getX(), 0d, 1d, textureMinU, textureMaxU),
				scale(point.getY(), 0d, 1d, textureMinV, textureMaxV));
	}

	/**
	 * Given <code>x</code> on the interval [<code>fromMin</code>,
	 * <code>fromMax</code>], translate it to the interval [<code>toMin</code>,
	 * <code>toMax</code>].
	 * 
	 * @param x
	 * @param fromMin
	 * @param fromMax
	 * @param toMin
	 * @param toMax
	 * @return
	 */
	private double scale(double x, double fromMin, double fromMax, double toMin, double toMax) {

		return ( ( x - fromMin ) / ( fromMax - fromMin ) ) * ( toMax - toMin ) + toMin;
	}

}
