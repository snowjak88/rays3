package org.snowjak.rays3.film;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays3.Global;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.sample.Sampler;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Simple additive Film implementation. Adds up radiances per image location,
 * and exports to an image-file.
 * 
 * @author snowjak88
 */
public class SimpleImageFilm implements Film {

	private final AtomicInteger							samplesAdded;
	private final BlockingQueue<Pair<int[], Spectrum>>	resultsQueue;

	private final double[][][]							film;
	private final boolean[][]							filmUpdated;

	public SimpleImageFilm(int imageWidth, int imageHeight, Sampler sampler) {

		this.samplesAdded = new AtomicInteger(0);
		this.resultsQueue = new LinkedBlockingQueue<>();

		this.film = new double[imageWidth][imageHeight][3];
		this.filmUpdated = new boolean[imageWidth][imageHeight];

		for (int i = 0; i < imageWidth; i++)
			for (int j = 0; j < imageHeight; j++) {
				filmUpdated[i][j] = false;

				for (int k = 0; k < 3; k++)
					film[i][j][k] = 0d;
			}

		Global.EXECUTOR
				.submit(new FilmUpdaterRunnable(resultsQueue, film, filmUpdated, sampler.totalSamples(), samplesAdded));

	}

	private static class FilmUpdaterRunnable implements Runnable {

		private final BlockingQueue<Pair<int[], Spectrum>>	resultsQueue;
		private final double[][][]							film;
		private final boolean[][]							filmUpdated;
		private final int									samplesExpected;
		private final AtomicInteger							samplesAdded;

		public FilmUpdaterRunnable(BlockingQueue<Pair<int[], Spectrum>> resultsQueue, double[][][] film,
				boolean[][] filmUpdated, int samplesExpected, AtomicInteger samplesAdded) {

			this.resultsQueue = resultsQueue;
			this.film = film;
			this.filmUpdated = filmUpdated;
			this.samplesExpected = samplesExpected;
			this.samplesAdded = samplesAdded;
		}

		@Override
		public void run() {

			Pair<int[], Spectrum> result;
			while (samplesAdded.get() < samplesExpected) {
				try {
					result = resultsQueue.take();

					final int filmX = result.getFirst()[0];
					final int filmY = result.getFirst()[1];

					filmUpdated[filmX][filmY] = true;

					final RGB rgb = result.getSecond().toRGB();
					film[filmX][filmY][0] += rgb.getRed();
					film[filmX][filmY][1] += rgb.getGreen();
					film[filmX][filmY][2] += rgb.getBlue();

					samplesAdded.incrementAndGet();

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void addSample(Sample sample, Spectrum radiance) {

		final int filmX = Film.convertContinuousToDiscrete(sample.getImageX());
		final int filmY = Film.convertContinuousToDiscrete(sample.getImageY());

		try {
			resultsQueue.put(new Pair<>(new int[] { filmX, filmY }, radiance));

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Write the current contents of this Film to a file as a PNG image.
	 * 
	 * @param imageFile
	 * @throws IOException
	 *             if any exception occurred during file-writing
	 */
	public void writeImage(final File imageFile) {

		for (int i = 0; i < filmUpdated.length; i++)
			for (int j = 0; j < filmUpdated[i].length; j++)
				if (filmUpdated[i][j] == false)
					System.err.println("Film location [" + i + "][" + j + "] was never updated!");

		BufferedImage image = new BufferedImage(film.length, film[0].length, BufferedImage.TYPE_INT_RGB);

		for (int u = 0; u < film.length; u++)
			for (int v = 0; v < film[0].length; v++)
				image.setRGB(u, film[0].length - v - 1, packRGB(film[u][v]));

		try {
			ImageIO.write(image, "png", imageFile);

		} catch (IOException e) {
			System.err.println("Exception encountered while saving to the image-file \"" + imageFile.getAbsolutePath()
					+ "\": " + e.getMessage());
			e.printStackTrace(System.err);
		}

	}

	private static int packRGB(double[] rgb) {

		final double r = FastMath.max(FastMath.min(rgb[0], 1d), 0d);
		final double g = FastMath.max(FastMath.min(rgb[1], 1d), 0d);
		final double b = FastMath.max(FastMath.min(rgb[2], 1d), 0d);
		return ( (int) ( r * 255d ) ) << 16 | ( (int) ( g * 255d ) ) << 8 | ( (int) ( b * 255d ) );
	}

	@Override
	public int countSamplesAdded() {

		return samplesAdded.get();
	}

}
