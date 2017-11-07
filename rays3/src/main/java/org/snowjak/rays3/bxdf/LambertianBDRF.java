package org.snowjak.rays3.bxdf;

import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.Texture;

/**
 * Implements a Lambertian BDRF, modeling a perfectly-diffuse surface.
 * 
 * @author snowjak88
 */
public class LambertianBDRF extends BDSF {

	private Texture texture;

	public LambertianBDRF(Texture texture) {
		super();

		this.texture = texture;
	}

	@Override
	public Spectrum getReflectedRadiance(Point x, Vector w_e, Vector w_r, Spectrum lambda, double t) {

		// TODO Auto-generated method stub
		return null;
	}

}
