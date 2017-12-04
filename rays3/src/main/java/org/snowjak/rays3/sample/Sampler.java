package org.snowjak.rays3.sample;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.snowjak.rays3.spectrum.Spectrum;

/**
 * A Sampler is responsible for generating useful {@link Sample}s.
 * 
 * @author snowjak88
 */
public abstract class Sampler {

	private final int					minFilmX, minFilmY, maxFilmX, maxFilmY;
	private final int					samplesPerPixel;

	private final BlockingQueue<Sample>	samplesQueue;

	private final Lock					samplerLock;

	private boolean						pregenerate;
	private boolean						noMoreSamples;

	public Sampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY, int samplesPerPixel) {

		this.minFilmX = minFilmX;
		this.minFilmY = minFilmY;
		this.maxFilmX = maxFilmX;
		this.maxFilmY = maxFilmY;
		this.samplesPerPixel = samplesPerPixel;

		this.samplesQueue = new LinkedBlockingQueue<>();

		this.samplerLock = new ReentrantLock();

		this.pregenerate = false;
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
	 * Recursively split this Sampler <code>n</code> times, giving you a grand
	 * total of (at most) 2<sup>n</sup> Sampler instances.
	 * 
	 * @param n
	 * @return
	 * @see #getSubSamplers()
	 */
	public Collection<Sampler> recursivelySubdivide(int n) {

		if (n <= 0 || !hasSubSamplers())
			return Arrays.asList(this);
		else {

			Collection<Sampler> result = new LinkedList<>();

			for (Sampler subSampler : getSubSamplers()) {
				result.addAll(subSampler.recursivelySubdivide(n - 1));
			}

			return result;

		}
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
	 * Pre-generates all {@link Sample}s within this Sampler's domain. Blocks
	 * until all Samples are generated and stored in this Sampler's internal
	 * queue.
	 * <p>
	 * <strong>Note</strong> that all pregenerated Samples will not be copied
	 * into any subdivided sub-Samplers you may subsequently split off.
	 * </p>
	 */
	public void pregenerateSamples() {

		this.pregenerate = true;

		try {

			Sample currentSample;
			while (( currentSample = generateNextSample() ) != null)
				samplesQueue.put(currentSample);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return a count of those {@link Sample}s that were pre-generated and
	 *         stored in this Sampler's internal queue
	 */
	public int countSamplesPregenerated() {

		return samplesQueue.size();
	}

	/**
	 * Grab the next Sample, or an empty {@link Optional} if no more Samples are
	 * left in this sampling-regime.
	 */
	public Optional<Sample> getNextSample() {

		final Optional<Sample> result;
		samplerLock.lock();

		if (this.pregenerate)
			if (samplesQueue.isEmpty())
				result = Optional.empty();
			else
				try {
					result = Optional.of(samplesQueue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
					return Optional.empty();
				}
		else
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
