package org.snowjak.rays3.sample;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.snowjak.rays3.spectrum.Spectrum;

/**
 * A Sampler is responsible for generating useful {@link Sample}s.
 * 
 * @author snowjak88
 */
public abstract class Sampler {

	private final int	minFilmX, minFilmY, maxFilmX, maxFilmY;
	private final int	samplesPerPixel;

	private final Lock	samplerLock;

	private boolean		noMoreSamples;

	public Sampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY, int samplesPerPixel) {

		this.minFilmX = minFilmX;
		this.minFilmY = minFilmY;
		this.maxFilmX = maxFilmX;
		this.maxFilmY = maxFilmY;
		this.samplesPerPixel = samplesPerPixel;

		this.samplerLock = new ReentrantLock();

		this.noMoreSamples = false;
	}

	/**
	 * @return <code>true</code> if this Sampler may be subdivided into
	 *         "sub-Samplers", splitting this Sampler's domain between them
	 */
	public boolean hasSubSamplers() {

		if (getFilmSizeX() > getFilmSizeY())
			return ( getMinFilmX() + 1 < getMaxFilmX() );

		else
			return ( getMinFilmY() + 1 < getMaxFilmY() );

	}

	/**
	 * Subdivide this Sampler into two sub-Samplers which together cover this
	 * Sampler's domain, or <code>this</code> if this Sampler cannot be so
	 * divided.
	 * 
	 * @return
	 */
	public Collection<Sampler> getSubSamplers() {

		if (!hasSubSamplers())
			return Arrays.asList(this);

		if (getFilmSizeX() > getFilmSizeY()) {

			final int midX = ( getMaxFilmX() - getMinFilmX() ) / 2 + getMinFilmX();
			return Arrays.asList(splitSubSampler(getMinFilmX(), getMinFilmY(), midX, getMaxFilmY()),
					splitSubSampler(midX + 1, getMinFilmY(), getMaxFilmX(), getMaxFilmY()));

		} else {

			final int midY = ( getMaxFilmY() - getMinFilmY() ) / 2 + getMinFilmY();
			return Arrays.asList(splitSubSampler(getMinFilmX(), getMinFilmY(), getMaxFilmX(), midY),
					splitSubSampler(getMinFilmX(), midY + 1, getMaxFilmX(), getMaxFilmY()));

		}
	}

	/**
	 * Split off a new sub-Sampler off of this Sampler, using the given
	 * film-extents as the sub-Sampler's new domain.
	 * 
	 * @param minFilmX
	 * @param minFilmY
	 * @param maxFilmX
	 * @param maxFilmY
	 * @return
	 */
	protected abstract Sampler splitSubSampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY);

	/**
	 * Grab the next Sample, or an empty {@link Optional} if no more Samples are
	 * left in this sampling-regime.
	 */
	public Optional<Sample> getNextSample() {

		final Optional<Sample> result;
		samplerLock.lock();

		result = Optional.ofNullable(generateNextSample());

		if (!result.isPresent())
			noMoreSamples = true;

		samplerLock.unlock();

		return result;
	}

	/**
	 * Implement your Sampler here. This method will only be called from its own
	 * separate thread, so it will be safe to refer to {@link Sampler} state
	 * from here (regardless of how many rendering threads it is feeding).
	 * 
	 * @return the next {@link Sample} in this sampling-regime, or
	 *         <code>null</code> if no more Samples left.
	 */
	protected abstract Sample generateNextSample();

	/**
	 * @return <code>true</code> if this Sampler has generated all the
	 *         {@link Sample}s in its domain
	 */
	public boolean isNoMoreSamples() {

		return noMoreSamples;
	}

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

	public static double mapXToU(double x, double minX, double maxX) {

		return ( x - minX ) / ( maxX - minX );
	}

}
