package org.snowjak.rays3.texture;

import org.snowjak.rays3.geometry.shape.SurfaceDescriptor;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.mapping.TextureMapping;

/**
 * Represents a texture.
 * 
 * @author snowjak88
 */
public abstract class Texture {

	private TextureMapping textureMapping;

	/**
	 * Construct this Texture using the specified {@link TextureMapping} to
	 * translate to/from surface-parameterization coordinates.
	 * 
	 * @param textureMapping
	 */
	public Texture(TextureMapping textureMapping) {
		this.textureMapping = textureMapping;
	}

	/**
	 * Given the specified {@link SurfaceDescriptor}, compute the resulting
	 * coloration of this texture.
	 * 
	 * @param surface
	 * @return
	 */
	public abstract Spectrum evaluate(SurfaceDescriptor surface);

	/**
	 * @return this texture's assigned {@link TextureMapping}
	 */
	public TextureMapping getTextureMapping() {

		return textureMapping;
	}
}
