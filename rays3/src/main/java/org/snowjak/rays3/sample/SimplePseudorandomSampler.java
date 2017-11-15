package org.snowjak.rays3.sample;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Simple implementation of {@link Sampler} that relies on straight-up
 * pseudorandom number generators. As such, it is quite simple -- but there is
 * no way of weeding out inadequately-distributed samples.
 * 
 * @author snowjak88
 */
public class SimplePseudorandomSampler implements Sampler {

	private final int	minFilmX, minFilmY, maxFilmX, maxFilmY;
	private int			currFilmX, currFilmY;

	public SimplePseudorandomSampler(int filmSizeX, int filmSizeY) {

		this.minFilmX = 0;
		this.minFilmY = 0;
		this.maxFilmX = filmSizeX - 1;
		this.maxFilmY = filmSizeY - 1;

		/*
		 * this.minImagePlaneX = -( imagePlaneSizeX / 2d ); this.minImagePlaneY
		 * = -( imagePlaneSizeY / 2d ); this.maxImagePlaneX = +( imagePlaneSizeX
		 * / 2d ); this.maxImagePlaneY = +( imagePlaneSizeY / 2d );
		 */

		this.currFilmX = this.minFilmX;
		this.currFilmY = this.minFilmY - 1;
	}

	@Override
	public Sample getNextSample() {

		currFilmY++;
		if (currFilmY > maxFilmY) {
			currFilmY = minFilmY;
			currFilmX++;
		}

		if (currFilmX > maxFilmX)
			return null;

		final double cameraU = mapXToU(currFilmX, minFilmX, maxFilmX);
		final double cameraV = mapXToU(currFilmY, maxFilmY, minFilmY);

		return new Sample(this, (double) currFilmX, (double) currFilmY, cameraU, cameraV, Global.RND.nextDouble(),
				Global.RND.nextDouble());
	}

	private double mapXToU(int x, int minX, int maxX) {

		return ( (double) x - (double) minX ) / ( (double) maxX - (double) minX );
	}

	@Override
	public boolean isSampleAcceptable(Sample sample, Spectrum result) {

		return true;
	}

	public int getMinFilmX() {

		return minFilmX;
	}

	public int getMinFilmY() {

		return minFilmY;
	}

	public int getMaxFilmX() {

		return maxFilmX;
	}

	public int getMaxFilmY() {

		return maxFilmY;
	}

	public int getFilmSizeX() {

		return maxFilmX - minFilmX + 1;
	}

	public int getFilmSizeY() {

		return maxFilmY - minFilmY + 1;
	}

}
