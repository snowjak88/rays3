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

	private int	minImageX, minImageY, maxImageX, maxImageY;
	private int	currImageX, currImageY;

	public SimplePseudorandomSampler(int minImageX, int minImageY, int maxImageX, int maxImageY) {

		this.minImageX = minImageX;
		this.minImageY = minImageY;
		this.maxImageX = maxImageX;
		this.maxImageY = maxImageY;

		this.currImageX = this.minImageX;
		this.currImageY = this.minImageY - 1;
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

		return new Sample(this, (double) currImageX, (double) currImageY, Global.RND.nextDouble(),
				Global.RND.nextDouble());
	}

	@Override
	public boolean isSampleAcceptable(Sample sample, Spectrum result) {

		return true;
	}

}
