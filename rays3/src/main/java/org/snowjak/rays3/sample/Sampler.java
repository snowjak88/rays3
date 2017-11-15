package org.snowjak.rays3.sample;

import org.snowjak.rays3.spectrum.Spectrum;

/**
 * A Sampler is responsible for generating useful {@link Sample}s.
 * 
 * @author snowjak88
 */
public abstract class Sampler {

	private int	minFilmX, minFilmY, maxFilmX, maxFilmY;
	private int	samplesPerPixel;

	public Sampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY, int samplesPerPixel) {

		this.minFilmX = minFilmX;
		this.minFilmY = minFilmY;
		this.maxFilmX = maxFilmX;
		this.maxFilmY = maxFilmY;
		this.samplesPerPixel = samplesPerPixel;
	}

	/**
	 * Grab the next Sample, or <code>null</code> if no more Samples are left in
	 * this sampling-regime.
	 */
	public abstract Sample getNextSample();

	/**
	 * @return the total count of {@link Sample}s this Sampler is expected to
	 *         create
	 */
	public int totalSamples() {

		return getFilmSizeX() * getFilmSizeY() * samplesPerPixel;
	}

	/**
	 * Given a Sample and the Spectrum that resulted from using it, is that
	 * Sample+Spectrum acceptable?
	 */
	public abstract boolean isSampleAcceptable(Sample sample, Spectrum result);

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

	public int getSamplesPerPixel() {

		return samplesPerPixel;
	}

	public int getFilmSizeX() {

		return maxFilmX - minFilmX + 1;
	}

	public int getFilmSizeY() {

		return maxFilmY - minFilmY + 1;
	}

}
