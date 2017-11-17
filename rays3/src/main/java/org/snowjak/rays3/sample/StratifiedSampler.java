package org.snowjak.rays3.sample;

import static org.apache.commons.math3.util.FastMath.ceil;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.function.Supplier;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Implements "stratified" sampling.
 * <p>
 * Each pixel is sampled <code>n</code> times, where
 * <code>n = i<sup>2</sup></code> for a positive integer <code>i</code>. Each
 * pixel's continuous coordinate-space is divided evenly into <code>n</code>
 * cells, constituting a <code>sqrt(n) x sqrt(n)</code> grid. Within each cell,
 * a point is selected as that sample's image-plane coordinate.
 * </p>
 * <p>
 * This technique is repeated when selecting sample-points on the lens.
 * </p>
 * 
 * @author snowjak88
 */
public class StratifiedSampler extends Sampler {

	private int					currFilmX, currFilmY, currPixelSample;

	private Supplier<Point2D>	imageSamples, lensSamples;
	private Supplier<Double>	timeSamples;
	private final int			gridSideSize;

	/**
	 * Create a new StratifiedSampler.
	 * <p>
	 * <strong>Note</strong> that <code>samplesPerPixel</code> must be a power
	 * of 2. If necessary, this number is <strong>rounded up</strong>.
	 * </p>
	 * 
	 * @param filmSizeX
	 * @param filmSizeY
	 * @param samplesPerPixel
	 */
	public StratifiedSampler(int filmSizeX, int filmSizeY, int samplesPerPixel) {
		super(0, 0, filmSizeX - 1, filmSizeY - 1, (int) pow(ceil(sqrt((double) samplesPerPixel)), 2));

		currFilmX = 0;
		currFilmY = 0;
		currPixelSample = -1;

		gridSideSize = (int) ceil(sqrt((double) samplesPerPixel));

		imageSamples = new Stratified2DSupplier(gridSideSize);
		lensSamples = new Stratified2DSupplier(gridSideSize);
		timeSamples = new Stratified1DSupplier(gridSideSize);
	}

	@Override
	public Sample getNextSample() {

		currPixelSample++;
		if (currPixelSample >= getSamplesPerPixel()) {

			currPixelSample = 0;
			currFilmY++;

			if (currFilmY > getMaxFilmY()) {
				currFilmY = getMinFilmY();
				currFilmX++;

				if (currFilmX > getMaxFilmX())
					return null;
			}
		}

		final Point2D imageSample = imageSamples.get();
		final Point2D lensSample = lensSamples.get();
		final double timeSample = timeSamples.get();

		return new Sample(this, imageSample.getX() + (double) currFilmX, imageSample.getY() + (double) currFilmY,
				lensSample.getX(), lensSample.getY(), timeSample, null, new Supplier<Supplier<Double>>() {

					@Override
					public Supplier<Double> get() {

						return new Stratified1DSupplier(gridSideSize);
					}

				}, new Supplier<Supplier<Point2D>>() {

					@Override
					public Supplier<Point2D> get() {

						return new Stratified2DSupplier(gridSideSize);
					}

				});
	}

	@Override
	public boolean isSampleAcceptable(Sample sample, Spectrum result) {

		// All samples are always considered to be acceptable -- i.e., we have
		// no criterion for rejecting Sample results.
		return true;
	}

	/**
	 * Supplies stratified <code>double</code> samples on the interval
	 * <code>[0..1]</code>, from smallest to largest. When the current set of
	 * stratified samples is completely scanned, this Supplier automatically
	 * compiles a new set of samples.
	 * 
	 * @author snowjak88
	 */
	public static class Stratified1DSupplier implements Supplier<Double> {

		private double[]	stratum;
		private int			currentElement;

		public Stratified1DSupplier(int length) {
			this.stratum = generate1DStratum(length);
			this.currentElement = -1;
		}

		@Override
		public Double get() {

			currentElement++;

			if (currentElement >= stratum.length) {
				stratum = generate1DStratum(stratum.length);
				currentElement = 0;
			}

			return stratum[currentElement];
		}

	}

	/**
	 * Supplies stratified {@link Point2D} samples on the interval
	 * <code>[(0,0), (1,1)]</code>, by columns first. When the current set of
	 * stratified samples is completely scanned, this Supplier automatically
	 * compiles a new set of samples.
	 * 
	 * @author snowjak88
	 */
	public static class Stratified2DSupplier implements Supplier<Point2D> {

		private Point2D[][]	stratum;
		private int			currentI, currentJ;

		public Stratified2DSupplier(int sideLength) {
			this.stratum = generate2DStratum(sideLength);
			this.currentI = 0;
			this.currentJ = -1;
		}

		@Override
		public Point2D get() {

			this.currentJ++;

			if (currentJ >= stratum[currentI].length) {
				currentJ = 0;
				currentI++;

				if (currentI >= stratum.length) {
					currentI = 0;
					currentJ = 0;
					stratum = generate2DStratum(stratum.length);
				}
			}

			return stratum[currentI][currentJ];
		}

	}

	public static double[] generate1DStratum(int length) {

		final double[] results = new double[length];

		final double sampleWidth = 1d / (double) length;
		for (int i = 0; i < length; i++) {

			final double jitter = Global.RND.nextDouble();
			results[i] = ( (double) i * sampleWidth ) + ( jitter * sampleWidth );

		}

		return results;
	}

	public static Point2D[][] generate2DStratum(int sideLength) {

		final Point2D[][] cells = new Point2D[sideLength][sideLength];
		final double cellLengthX = 1d / ( (double) sideLength );

		for (int i = 0; i < cells.length; i++) {

			final double cellLengthY = 1d / ( (double) cells[i].length );

			for (int j = 0; j < cells[0].length; j++) {

				final double jitterX = Global.RND.nextDouble();
				final double jitterY = Global.RND.nextDouble();

				final double x = ( (double) i * cellLengthX ) + ( jitterX * cellLengthX );
				final double y = ( (double) j * cellLengthY ) + ( jitterY * cellLengthY );

				cells[i][j] = new Point2D(x, y);
			}
		}

		return cells;
	}

}
