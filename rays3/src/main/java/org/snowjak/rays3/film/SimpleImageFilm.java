package org.snowjak.rays3.film;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Simple additive Film implementation. Adds up radiances per image location,
 * and exports to an image-file.
 * 
 * @author snowjak88
 */
public class SimpleImageFilm implements Film {

	private final AtomicInteger	samplesAdded;
	private final Lock			filmLock;

	private final double[][][]	filmRGB;

	public SimpleImageFilm(int imageWidth, int imageHeight) {

		this.samplesAdded = new AtomicInteger(0);
		this.filmLock = new ReentrantLock();

		this.filmRGB = new double[imageWidth][imageHeight][3];

		for (int i = 0; i < imageWidth; i++)
			for (int j = 0; j < imageHeight; j++)
				for (int k = 0; k < 3; k++)
					filmRGB[i][j][k] = 0d;

	}

	@Override
	public void addSample(Sample sample, Spectrum radiance) {

		final int filmX = Film.convertContinuousToDiscrete(sample.getImageX());
		final int filmY = Film.convertContinuousToDiscrete(sample.getImageY());

		this.addSample(filmX, filmY, sample.getSampler().getSamplesPerPixel(), radiance);

		samplesAdded.incrementAndGet();

	}

	protected void addSample(int filmX, int filmY, int samplesPerPixel, Spectrum radiance) {

		filmLock.lock();

		final RGB rgb = radiance.toRGB();
		filmRGB[filmX][filmY][0] += rgb.getRed();
		filmRGB[filmX][filmY][1] += rgb.getGreen();
		filmRGB[filmX][filmY][2] += rgb.getBlue();

		filmLock.unlock();
	}

	@Override
	public int countSamplesAdded() {

		return samplesAdded.get();
	}

	@Override
	public void writeImage(final File imageFile, ImageFormat format) {

		BufferedImage image = new BufferedImage(filmRGB.length, filmRGB[0].length, BufferedImage.TYPE_INT_RGB);

		for (int u = 0; u < filmRGB.length; u++)
			for (int v = 0; v < filmRGB[0].length; v++)
				image.setRGB(u, filmRGB[0].length - v - 1, packRGB(filmRGB[u][v]));

		try {
			ImageIO.write(image, format.getFormatName(), imageFile);

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
	public int getWidth() {

		return filmRGB.length;
	}

	@Override
	public int getHeight() {

		return filmRGB[0].length;
	}

}
