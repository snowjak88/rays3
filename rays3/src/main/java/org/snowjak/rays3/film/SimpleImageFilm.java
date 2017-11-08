package org.snowjak.rays3.film;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.snowjak.rays3.Global;
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

	private double[][][] film;

	public SimpleImageFilm(int imageWidth, int imageHeight) {
		this.film = new double[imageWidth][imageHeight][];
	}

	@Override
	public void addSample(Sample sample, Spectrum radiance) {

		int imageU = (int) sample.getImageX();
		int imageV = (int) sample.getImageY();

		if (film[imageU][imageV] == null)
			film[imageU][imageV] = radiance.toRGB().getComponents();
		else {
			RGB rgb = radiance.toRGB();
			film[imageU][imageV][0] += rgb.getRed();
			film[imageU][imageV][1] += rgb.getGreen();
			film[imageU][imageV][2] += rgb.getBlue();
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

		Global.EXECUTOR.submit(new Runnable() {

			@Override
			public void run() {

				BufferedImage image = new BufferedImage(film.length, film[0].length, BufferedImage.TYPE_INT_RGB);

				for (int u = 0; u < film.length; u++)
					for (int v = 0; v < film[0].length; v++)
						image.setRGB(u, v, packRGB(film[u][v]));

				try {
					ImageIO.write(image, "png", imageFile);

				} catch (IOException e) {
					System.err.println("Exception encountered while saving to the image-file \""
							+ imageFile.getAbsolutePath() + "\": " + e.getMessage());
					e.printStackTrace(System.err);
				}
			}
		});
	}

	private static int packRGB(double[] rgb) {

		return (int) ( rgb[0] * 256 ) << 16 | (int) ( rgb[1] * 256 ) << 8 | (int) ( rgb[2] * 256 );
	}

}
