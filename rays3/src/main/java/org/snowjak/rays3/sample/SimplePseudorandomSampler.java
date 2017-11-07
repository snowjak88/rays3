package org.snowjak.rays3.sample;

import java.util.Random;

import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Simple implementation of {@link Sampler} that relies on straight-up
 * pseudorandom number generators. As such, it is quite simple -- but there is
 * no way of weeding out inadequately-distributed samples.
 * 
 * @author snowjak88
 */
public class SimplePseudorandomSampler implements Sampler {

	private int					minImageX, minImageY, maxImageX, maxImageY;
	private int					currImageX, currImageY;

	private static final Random	RND	= new Random(System.currentTimeMillis());

	public SimplePseudorandomSampler(int minImageX, int minImageY, int maxImageX, int maxImageY) {

		this.minImageX = minImageX;
		this.minImageY = minImageY;
		this.maxImageX = maxImageX;
		this.maxImageY = maxImageY;

		this.currImageX = minImageX;
		this.currImageY = minImageY - 1;
	}

	@Override
	public Sample getNextSample() {

		if (currImageX > maxImageX)
			return null;

		currImageY++;
		if (currImageY > maxImageY) {
			currImageY = minImageY;
			currImageX++;
		}

		return new Sample(this, (double) currImageX, (double) currImageY, RND.nextDouble(), RND.nextDouble());
	}

	@Override
	public boolean isSampleAcceptable(Sample sample, Spectrum result) {

		return true;
	}

}
