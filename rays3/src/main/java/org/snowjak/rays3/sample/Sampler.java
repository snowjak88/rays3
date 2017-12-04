package org.snowjak.rays3.sample;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * A Sampler is responsible for generating useful {@link Sample}s.
 * 
 * @author snowjak88
 */
public abstract class Sampler {

	private final int					minFilmX, minFilmY, maxFilmX, maxFilmY;
	private final int					samplesPerPixel;

	private final Runnable				samplesPregenerator;
	private final CountDownLatch		samplesPregeneratedLatch;
	private final BlockingQueue<Sample>	samplesQueue;

	private final Lock					samplerLock;

	private final int					pregenerateBufferSize;
	private boolean						pregenerate;
	private AtomicBoolean				generatorStarted;
	private AtomicBoolean				generatorFinished;
	private boolean						noMoreSamples;

	/**
	 * Construct a new Sampler.
	 * 
	 * @param minFilmX
	 * @param minFilmY
	 * @param maxFilmX
	 * @param maxFilmY
	 * @param samplesPerPixel
	 */
	public Sampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY, int samplesPerPixel) {
		this(minFilmX, minFilmY, maxFilmX, maxFilmY, samplesPerPixel, 0);
	}

	/**
	 * Construct a new Sampler. If you want to pre-generate any {@link Sample}s
	 * on this Sampler's internal queue, provide a positive non-zero value for
	 * <code>pregenerateBuffer</code>.
	 * 
	 * @param minFilmX
	 * @param minFilmY
	 * @param maxFilmX
	 * @param maxFilmY
	 * @param samplesPerPixel
	 * @param pregenerateBufferSize
	 *            Must be <code>&gt; 0</code> if you want to pre-generate any
	 *            Samples
	 */
	public Sampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY, int samplesPerPixel,
			int pregenerateBufferSize) {

		this.minFilmX = minFilmX;
		this.minFilmY = minFilmY;
		this.maxFilmX = maxFilmX;
		this.maxFilmY = maxFilmY;
		this.samplesPerPixel = samplesPerPixel;

		this.samplerLock = new ReentrantLock();
		this.noMoreSamples = false;

		this.pregenerateBufferSize = min(max(pregenerateBufferSize, 0), totalSamples());

		this.pregenerate = ( this.pregenerateBufferSize > 0 );
		this.generatorStarted = new AtomicBoolean(false);
		this.generatorFinished = new AtomicBoolean(false);

		if (this.pregenerateBufferSize > 0)
			this.samplesQueue = new ArrayBlockingQueue<>(this.pregenerateBufferSize);
		else
			this.samplesQueue = null;

		this.samplesPregeneratedLatch = new CountDownLatch(this.pregenerateBufferSize);

		this.samplesPregenerator = () -> {

			generatorStarted.set(true);

			Sample currentSample;
			while (( currentSample = generateNextSample() ) != null) {
				try {
					samplesQueue.put(currentSample);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				samplesPregeneratedLatch.countDown();
			}

			this.generatorFinished.set(true);
		};
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
			return Arrays.asList(
					splitSubSampler(getMinFilmX(), getMinFilmY(), midX, getMaxFilmY(), getPregenerateBufferSize() / 2),
					splitSubSampler(midX + 1, getMinFilmY(), getMaxFilmX(), getMaxFilmY(),
							getPregenerateBufferSize() / 2));

		} else {

			final int midY = ( getMaxFilmY() - getMinFilmY() ) / 2 + getMinFilmY();
			return Arrays.asList(
					splitSubSampler(getMinFilmX(), getMinFilmY(), getMaxFilmX(), midY, getPregenerateBufferSize() / 2),
					splitSubSampler(getMinFilmX(), midY + 1, getMaxFilmX(), getMaxFilmY(),
							getPregenerateBufferSize() / 2));

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
	 * @param pregenerateBufferSize
	 * @return
	 */
	protected abstract Sampler splitSubSampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY,
			int pregenerateBufferSize);

	/**
	 * @return a count of those {@link Sample}s that have been pre-generated and
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

		Optional<Sample> result = Optional.empty();
		samplerLock.lock();

		//
		//
		// Are we pre-generating Samples for this Sampler?
		if (pregenerate) {

			//
			// If we haven't started the pre-generator sampler, start it now!
			if (!generatorStarted.get())
				pregenerateSamples();

			//
			// If the pre-generator finished and there are no more samples, then
			// this Sampler is finished!
			if (generatorFinished.get() && samplesQueue.isEmpty())
				result = Optional.empty();
			else
				try {
					result = Optional.of(samplesQueue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
					result = Optional.empty();
				}

		} else {

			//
			// We are *not* pre-generating anything.
			// So generate the next Sample in this Sampler's domain.
			result = Optional.ofNullable(generateNextSample());

		}

		//
		//
		//
		if (!result.isPresent())
			noMoreSamples = true;

		samplerLock.unlock();

		return result;
	}

	/**
	 * If this Sampler was configued to do any {@link Sample} pre-generation,
	 * generate those Samples now.
	 * <p>
	 * This method will block until pre-generation is complete.
	 * </p>
	 */
	public void pregenerateSamples() {

		Global.RENDER_EXECUTOR.submit(samplesPregenerator);

		try {
			samplesPregeneratedLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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

	public int getPregenerateBufferSize() {

		return pregenerateBufferSize;
	}

	public static double mapXToU(double x, double minX, double maxX) {

		return ( x - minX ) / ( maxX - minX );
	}

}
