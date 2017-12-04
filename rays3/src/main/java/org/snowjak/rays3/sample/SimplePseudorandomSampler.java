package org.snowjak.rays3.sample;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.film.Film;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Simple implementation of {@link Sampler} that relies on straight-up
 * pseudorandom number generators. As such, it is quite simple -- but there is
 * no way of weeding out inadequately-distributed samples.
 * 
 * @author snowjak88
 */
public class SimplePseudorandomSampler extends Sampler {

	private int	currFilmX, currFilmY;
	private int	currSamplePerPixel;

	public SimplePseudorandomSampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY, int samplesPerPixel) {
		this(minFilmX, minFilmY, maxFilmX, maxFilmY, samplesPerPixel, 0);
	}

	public SimplePseudorandomSampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY, int samplesPerPixel,
			int pregenerateBufferSize) {

		super(minFilmX, minFilmY, maxFilmX, maxFilmY, samplesPerPixel, pregenerateBufferSize);

		this.currFilmX = this.getMinFilmX();
		this.currFilmY = this.getMinFilmY();
		this.currSamplePerPixel = -1;
	}

	@Override
	protected Sampler splitSubSampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY,
			int pregenerateBufferSize) {

		return new SimplePseudorandomSampler(minFilmX, minFilmY, maxFilmX, maxFilmY, getSamplesPerPixel(),
				pregenerateBufferSize);
	}

	@Override
	protected Sample generateNextSample() {

		currSamplePerPixel++;

		if (currSamplePerPixel >= getSamplesPerPixel()) {
			currSamplePerPixel = 0;

			currFilmY++;
			if (currFilmY > getMaxFilmY()) {
				currFilmY = getMinFilmY();
				currFilmX++;
			}

			if (currFilmX > getMaxFilmX())
				return null;
		}

		final double currImageX = Film.convertDiscreteToContinuous(currFilmX),
				currImageY = Film.convertDiscreteToContinuous(currFilmY);

		final double imageXScatter = Global.RND.nextDouble() - 0.5d, imageYScatter = Global.RND.nextDouble() - 0.5d;

		final double imageX_scattered = currImageX + imageXScatter, imageY_scattered = currImageY + imageYScatter;

		return new Sample(this, imageX_scattered, imageY_scattered, Global.RND.nextDouble(), Global.RND.nextDouble());
	}

	@Override
	public boolean isSampleAcceptable(Sample sample, Spectrum result) {

		return true;
	}

}
