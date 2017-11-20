package org.snowjak.rays3.sample;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * A Sampler is responsible for generating useful {@link Sample}s.
 * 
 * @author snowjak88
 */
public abstract class Sampler {

	private final int								minFilmX, minFilmY, maxFilmX, maxFilmY;
	private final int								samplesPerPixel;

	private final BlockingQueue<Optional<Sample>>	sampleQueue;

	public Sampler(int minFilmX, int minFilmY, int maxFilmX, int maxFilmY, int samplesPerPixel) {

		this.minFilmX = minFilmX;
		this.minFilmY = minFilmY;
		this.maxFilmX = maxFilmX;
		this.maxFilmY = maxFilmY;
		this.samplesPerPixel = samplesPerPixel;

		this.sampleQueue = new ArrayBlockingQueue<>(65535);

		Global.SCHEDULED_EXECUTOR.schedule(() -> Global.EXECUTOR.execute(new SamplePusher(this, sampleQueue)), 1000,
				TimeUnit.MILLISECONDS);

		// Global.EXECUTOR.execute(new SamplePusher(this, sampleQueue));

	}

	/**
	 * {@link Runnable} responsible for generating new Samples and pushing them
	 * into the provided {@link BlockingQueue}.
	 * <p>
	 * Note that each {@link Sample} is itself packaged within an
	 * {@link Optional}. This allows us to drop in an <em>empty</em> Optional
	 * (see {@link Optional#empty()}) to denote the end of the sampling regime.
	 * </p>
	 * 
	 * @author snowjak88
	 */
	private static class SamplePusher implements Runnable {

		private final Sampler							sampler;
		private final BlockingQueue<Optional<Sample>>	sampleQueue;

		public SamplePusher(Sampler sampler, BlockingQueue<Optional<Sample>> sampleQueue) {
			this.sampler = sampler;
			this.sampleQueue = sampleQueue;
		}

		@Override
		public void run() {

			Sample sample;
			try {
				while (( sample = sampler.generateNextSample() ) != null)
					sampleQueue.put(Optional.of(sample));

				sampleQueue.put(Optional.empty());

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Grab the next Sample, or <code>null</code> if no more Samples are left in
	 * this sampling-regime.
	 */
	public Sample getNextSample() {

		try {
			return sampleQueue.take().orElse(null);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null;
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
	 * @return the total count of {@link Sample}s this Sampler is expected to
	 *         create
	 */
	public int totalSamples() {

		return getFilmSizeX() * getFilmSizeY() * samplesPerPixel;
	}

	/**
	 * @return the total count of {@link Sample}s ready to be used (i.e., those
	 *         already generated and waiting to be picked up)
	 */
	public int samplesReady() {

		return sampleQueue.size();
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
