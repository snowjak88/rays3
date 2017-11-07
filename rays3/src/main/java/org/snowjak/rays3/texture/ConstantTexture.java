package org.snowjak.rays3.texture;

import org.snowjak.rays3.geometry.shape.SurfaceDescriptor;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.mapping.LinearTextureMapping;

/**
 * A "constant" texture is simply one color, all the time.
 * 
 * @author snowjak88
 */
public class ConstantTexture extends Texture {

	private Spectrum constant;

	public ConstantTexture(Spectrum constant) {
		super(new LinearTextureMapping());

		this.constant = constant;
	}

	@Override
	public Spectrum evaluate(SurfaceDescriptor surface) {

		return constant;
	}

}
