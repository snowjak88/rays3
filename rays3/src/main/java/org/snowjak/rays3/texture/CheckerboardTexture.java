package org.snowjak.rays3.texture;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.shape.SurfaceDescriptor;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.mapping.LinearTextureMapping;
import org.snowjak.rays3.texture.mapping.TextureMapping;

/**
 * Defines a checkerboard {@link Texture} -- i.e., a division of texture-space
 * into squares, and the allocation of one {@link Texture} or another to each of
 * those areas.
 * <p>
 * Space is divided between the two constituent Textures by:
 * 
 * <pre>
 * i = | round(x + y) | % 2
 * </pre>
 * 
 * where <code>i = 0</code> = Texture #1, <code>i = 1</code> = Texture #2
 * </p>
 * 
 * @author snowjak88
 */
public class CheckerboardTexture extends Texture {

	private final Texture[] textures;

	/**
	 * Construct a new CheckerboardTexture using the two specified
	 * {@link Texture}s, and the default {@link LinearTextureMapping}.
	 * 
	 * @param texture1
	 * @param texture2
	 */
	public CheckerboardTexture(Texture texture1, Texture texture2) {
		this(texture1, texture2, new LinearTextureMapping());
	}

	/**
	 * Construct a new CheckerboardTexture using the two specified
	 * {@link Texture}s, and the given {@link TextureMapping}.
	 * 
	 * @param texture1
	 * @param texture2
	 * @param textureMapping
	 */
	public CheckerboardTexture(Texture texture1, Texture texture2, TextureMapping textureMapping) {

		super(textureMapping);
		this.textures = new Texture[] { texture1, texture2 };
	}

	@Override
	public Spectrum evaluate(SurfaceDescriptor surface) {

		Point2D mappedPoint = getTextureMapping().map(surface.getParam());
		SurfaceDescriptor mappedSurface = new SurfaceDescriptor(surface.getPoint(), surface.getNormal(), mappedPoint);

		return textures[determineTextureIndex(mappedPoint)].evaluate(mappedSurface);
	}

	/**
	 * For the given texture-coordinates, determine which of the this
	 * checkerboard's {@link Texture}s to use (0-based index).
	 * 
	 * @param textureParam
	 * @return
	 */
	public int determineTextureIndex(Point2D textureParam) {

		return ( FastMath.abs((int) FastMath.round(textureParam.getX()))
				+ FastMath.abs((int) FastMath.round(textureParam.getY())) ) % textures.length;
	}

}
