package org.snowjak.rays3.sample;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.ceil;
import static org.apache.commons.math3.util.FastMath.floor;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.spectrum.Spectrum;

public class BestCandidateSampler extends Sampler {

	private final int						samplesPerSide;
	private int								samplesGenerated;
	private int								currentPixelSample, currentImageX, currentImageY;

	private final Map<String, Double>[][]	samples;

	private static final String				SAMPLE_IMAGE_X	= "IMAGE_X", SAMPLE_IMAGE_Y = "IMAGE_Y",
			SAMPLE_LENS_U = "LENS_U", SAMPLE_LENS_V = "LENS_V", SAMPLE_TIME = "TIME";

	@SuppressWarnings("unchecked")
	public BestCandidateSampler(int filmSizeX, int filmSizeY, int samplesPerPixel) {
		super(0, 0, filmSizeX - 1, filmSizeY - 1, samplesPerPixel);

		this.samplesPerSide = (int) ceil(sqrt(samplesPerPixel * samplesPerPixel));
		this.samples = new HashMap[samplesPerSide][samplesPerSide];

		for (int i = 0; i < samplesPerSide; i++)
			for (int j = 0; j < samplesPerSide; j++)
				this.samples[i][j] = new HashMap<>();

		this.samplesGenerated = 0;
		this.currentImageX = getMinFilmX();
		this.currentImageY = getMinFilmY();
		this.currentPixelSample = -1;
	}

	@Override
	protected Sample generateNextSample() {

		currentPixelSample++;
		if (currentPixelSample >= getSamplesPerPixel()) {

			for (int i = 0; i < samplesPerSide; i++)
				for (int j = 0; j < samplesPerSide; j++)
					this.samples[i][j].clear();

			currentPixelSample = 0;
			currentImageY++;

			if (currentImageY > getMaxFilmY()) {
				currentImageY = getMinFilmY();
				currentImageX++;

				if (currentImageX > getMaxFilmX())
					return null;
			}
		}

		final int samples_i, samples_j;

		if (samplesGenerated == 0) {

			//
			// The very first sample is selected at random.
			//
			double imageX = Global.RND.nextDouble();
			double imageY = Global.RND.nextDouble();
			double lensU = Global.RND.nextDouble();
			double lensV = Global.RND.nextDouble();
			double t = Global.RND.nextDouble();

			samples_i = (int) floor(imageX / (double) samplesPerSide);
			samples_j = (int) floor(imageY / (double) samplesPerSide);

			samples[samples_i][samples_j].put(SAMPLE_IMAGE_X, imageX);
			samples[samples_i][samples_j].put(SAMPLE_IMAGE_Y, imageY);
			samples[samples_i][samples_j].put(SAMPLE_LENS_U, lensU);
			samples[samples_i][samples_j].put(SAMPLE_LENS_V, lensV);
			samples[samples_i][samples_j].put(SAMPLE_TIME, t);

		} else {

			//
			// Subsequent samples are selected by dart-throwing successive
			// dimensions and selecting the best (i.e., most-distant) candidate.
			//

			final Point2D imageXY = throwImageDart();

			samples_i = (int) floor(imageXY.getX() / (double) samplesPerSide);
			samples_j = (int) floor(imageXY.getY() / (double) samplesPerSide);

			final double lensU = throwDart(samples_i, samples_j, SAMPLE_LENS_U);
			final double lensV = throwDart(samples_i, samples_j, SAMPLE_LENS_V);
			final double t = throwDart(samples_i, samples_j, SAMPLE_TIME);

			samples[samples_i][samples_j].put(SAMPLE_IMAGE_X, imageXY.getX());
			samples[samples_i][samples_j].put(SAMPLE_IMAGE_Y, imageXY.getY());
			samples[samples_i][samples_j].put(SAMPLE_LENS_U, lensU);
			samples[samples_i][samples_j].put(SAMPLE_LENS_V, lensV);
			samples[samples_i][samples_j].put(SAMPLE_TIME, t);

		}

		samplesGenerated++;

		return createSample(samples_i, samples_j);
	}

	private Point2D throwImageDart() {

		double bestDartX = 0d, bestDartY = 0d;
		double bestDartDistanceSq = -1d;

		for (int i = 0; i < samplesGenerated; i++) {

			final double dartX = Global.RND.nextDouble(), dartY = Global.RND.nextDouble();

			double dartDistanceSq = Double.MAX_VALUE;
			for (int si = 0; si < samplesPerSide; si++)
				for (int sj = 0; sj < samplesPerSide; sj++)
					if (samples[si][sj].containsKey(SAMPLE_IMAGE_X) && samples[si][sj].containsKey(SAMPLE_IMAGE_Y)) {
						final double currDistanceSq = pow(samples[si][sj].get(SAMPLE_IMAGE_X) - dartX, 2)
								+ pow(samples[si][sj].get(SAMPLE_IMAGE_Y) - dartY, 2);
						if (currDistanceSq < dartDistanceSq)
							dartDistanceSq = currDistanceSq;
					}

			if (dartDistanceSq == Double.MAX_VALUE)
				dartDistanceSq = 0d;

			if (dartDistanceSq > bestDartDistanceSq) {
				bestDartDistanceSq = dartDistanceSq;
				bestDartX = dartX;
				bestDartY = dartY;
			}

		}

		return new Point2D(bestDartX, bestDartY);
	}

	private double throwDart(int samples_i, int samples_j, String dartName) {

		double bestDart = 0d;
		double bestDartDistance = -1d;

		for (int i = 0; i < samplesGenerated; i++) {

			final double dart = Global.RND.nextDouble();
			double dartDistance = Double.MAX_VALUE;

			for (int si = max(samples_i - 1, 0); si < min(samples_i + 1, samplesPerSide); si++)
				for (int sj = max(samples_j - 1, 0); sj < min(samples_j + 1, samplesPerSide); sj++)
					if (samples[si][sj].containsKey(dartName)) {
						final double currDistance = abs(samples[si][sj].get(dartName) - dart);
						if (currDistance < dartDistance)
							dartDistance = currDistance;
					}

			if (dartDistance == Double.MAX_VALUE)
				dartDistance = 0d;

			if (bestDartDistance < dartDistance) {
				bestDartDistance = dartDistance;
				bestDart = dart;
			}

		}

		return bestDart;
	}

	private Sample createSample(int samples_i, int samples_j) {

		return new Sample(this, samples[samples_i][samples_j].get(SAMPLE_IMAGE_X) + (double) currentImageX,
				samples[samples_i][samples_j].get(SAMPLE_IMAGE_Y) + (double) currentImageY,
				samples[samples_i][samples_j].get(SAMPLE_LENS_U), samples[samples_i][samples_j].get(SAMPLE_LENS_V),
				samples[samples_i][samples_j].get(SAMPLE_TIME), null, new Function<Integer, Supplier<Double>>() {

					@Override
					public Supplier<Double> apply(Integer period) {

						return new DartBoard1D(period);
					}

				}, new Function<Integer, Supplier<Point2D>>() {

					@Override
					public Supplier<Point2D> apply(Integer period) {

						return new DartBoard2D(period);
					}

				});
	}

	@Override
	public void reset() {

		for (int i = 0; i < samplesPerSide; i++)
			for (int j = 0; j < samplesPerSide; j++)
				this.samples[i][j].clear();

		this.currentImageX = 0;
		this.currentImageY = 0;
		this.currentPixelSample = -1;
	}

	@Override
	public boolean isSampleAcceptable(Sample sample, Spectrum result) {

		return true;
	}

	private static class DartBoard1D implements Supplier<Double> {

		private final List<Double>	board;
		private final int			period;

		public DartBoard1D(int period) {
			this.board = new LinkedList<>();
			this.period = period;
		}

		@Override
		public Double get() {

			if (this.board.size() == this.period)
				board.clear();

			double bestDistance = -1d;
			double bestDart = 0d;

			for (int i = 0; i < board.size(); i++) {

				final double dart = Global.RND.nextDouble();
				double dartDistance = Double.MAX_VALUE;

				for (Double d : board) {
					final double currDistance = abs(d - dart);
					if (currDistance < dartDistance) {
						dartDistance = currDistance;
					}
				}

				if (dartDistance == Double.MAX_VALUE)
					dartDistance = 0d;

				if (bestDistance < dartDistance) {
					bestDistance = dartDistance;
					bestDart = dart;
				}
			}

			board.add(bestDart);

			return bestDart;
		}
	}

	private static class DartBoard2D implements Supplier<Point2D> {

		private final List<Point2D>	board;
		private final int			period;

		public DartBoard2D(int period) {
			this.board = new LinkedList<>();
			this.period = period;
		}

		@Override
		public Point2D get() {

			if (this.board.size() == this.period)
				board.clear();

			double bestDistanceSq = -1d;
			double bestDartX = 0d, bestDartY = 0d;

			for (int i = 0; i < board.size(); i++) {

				final double dartX = Global.RND.nextDouble(), dartY = Global.RND.nextDouble();
				double dartDistanceSq = Double.MAX_VALUE;

				for (Point2D p : board) {
					final double currDistanceSq = pow(p.getX() - dartX, 2) + pow(p.getY() - dartY, 2);
					if (currDistanceSq < dartDistanceSq) {
						dartDistanceSq = currDistanceSq;
					}
				}

				if (dartDistanceSq == Double.MAX_VALUE)
					dartDistanceSq = 0d;

				if (bestDistanceSq < dartDistanceSq) {
					bestDistanceSq = dartDistanceSq;
					bestDartX = dartX;
					bestDartY = dartY;
				}
			}

			final Point2D resultDart = new Point2D(bestDartX, bestDartY);
			board.add(resultDart);

			return resultDart;
		}
	}

}
