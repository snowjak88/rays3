package org.snowjak.rays3.film;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import org.apache.commons.math3.util.FastMath;
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
public class SimpleImageFilm implements StatisticsFilm {

	private final AtomicInteger		samplesAdded;
	private final Lock				filmLock;

	private final double[][][]		filmRGB;
	private final List<Double>[][]	filmAmplitude;
	private final int[][]			filmCounts;

	@SuppressWarnings("unchecked")
	public SimpleImageFilm(int imageWidth, int imageHeight, Sampler sampler) {

		this.samplesAdded = new AtomicInteger(0);
		this.filmLock = new ReentrantLock();

		this.filmRGB = new double[imageWidth][imageHeight][3];
		this.filmAmplitude = new ArrayList[imageWidth][imageHeight];
		this.filmCounts = new int[imageWidth][imageHeight];

		for (int i = 0; i < imageWidth; i++)
			for (int j = 0; j < imageHeight; j++) {

				filmCounts[i][j] = 0;
				filmAmplitude[i][j] = null;

				for (int k = 0; k < 3; k++)
					filmRGB[i][j][k] = 0d;
			}

	}

	@Override
	public void addSample(Sample sample, Spectrum radiance) {

		final int filmX = Film.convertContinuousToDiscrete(sample.getImageX());
		final int filmY = Film.convertContinuousToDiscrete(sample.getImageY());

		filmLock.lock();

		if (filmAmplitude[filmX][filmY] == null)
			filmAmplitude[filmX][filmY] = new ArrayList<Double>(sample.getSampler().getSamplesPerPixel());

		filmAmplitude[filmX][filmY].add(radiance.getAmplitude());
		filmCounts[filmX][filmY]++;

		final RGB rgb = radiance.toRGB();
		filmRGB[filmX][filmY][0] += rgb.getRed();
		filmRGB[filmX][filmY][1] += rgb.getGreen();
		filmRGB[filmX][filmY][2] += rgb.getBlue();

		samplesAdded.incrementAndGet();

		filmLock.unlock();

	}

	@Override
	public int countSamplesAdded() {

		return samplesAdded.get();
	}

	@Override
	public double getVariance(double imageX, double imageY) {

		final int filmX = Film.convertContinuousToDiscrete(imageX);
		final int filmY = Film.convertContinuousToDiscrete(imageY);

		final List<Double> amplitudes = filmAmplitude[filmX][filmY];

		filmLock.lock();

		double result = 0d;
		for (int i = 0; i < amplitudes.size() - 1; i++)
			for (int j = i + 1; j < amplitudes.size(); j++) {
				result += FastMath.pow(( amplitudes.get(i) - amplitudes.get(j) ), 2);
			}

		filmLock.unlock();

		return result / ( (double) amplitudes.size() * (double) amplitudes.size() );
	}

	@Override
	public int getCountAt(double imageX, double imageY) {

		final int filmX = Film.convertContinuousToDiscrete(imageX);
		final int filmY = Film.convertContinuousToDiscrete(imageY);

		return filmCounts[filmX][filmY];
	}

	/**
	 * Write the current contents of this Film to a file as a PNG image.
	 * 
	 * @param imageFile
	 * @throws IOException
	 *             if any exception occurred during file-writing
	 */
	public void writeImage(final File imageFile) {

		BufferedImage image = new BufferedImage(filmRGB.length, filmRGB[0].length, BufferedImage.TYPE_INT_RGB);

		for (int u = 0; u < filmRGB.length; u++)
			for (int v = 0; v < filmRGB[0].length; v++)
				image.setRGB(u, filmRGB[0].length - v - 1, packRGB(filmRGB[u][v]));

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

}
