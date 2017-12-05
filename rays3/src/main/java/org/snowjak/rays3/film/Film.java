package org.snowjak.rays3.film;

import java.io.File;
import java.io.IOException;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Represents a piece of film exposed to radiance to produce an image.
 * <p>
 * Film is responsible for one thing:
 * <ol>
 * <li>Recording incoming radiance {@link Spectrum} objects in its internal
 * representation</li>
 * </ol>
 * What that representation looks like is implementation-specific.
 * </p>
 * 
 * @author snowjak88
 */
public interface Film {

	/**
	 * Add the given {@link Spectrum} (obtained using the specified
	 * {@link Sample}) to this Film's internal representation.
	 * 
	 * @param sample
	 * @param radiance
	 */
	public void addSample(Sample sample, Spectrum radiance);

	public int countSamplesAdded();

	/**
	 * Given a "continuous" (i.e., decimal) image-coordinate, convert it to a
	 * discrete image-coordinate.
	 * 
	 * @param continuousCoordinate
	 * @return
	 */
	public static int convertContinuousToDiscrete(double continuousCoordinate) {

		return (int) FastMath.floor(continuousCoordinate);
	}

	/**
	 * Given a discrete image-coordinate, convert it to a "continuous" (i.e.,
	 * decimal) image-coordinate.
	 * 
	 * @param discreteCoordinate
	 * @return
	 */
	public static double convertDiscreteToContinuous(int discreteCoordinate) {

		return ( (double) discreteCoordinate ) + 0.5d;
	}

	public int getWidth();

	public int getHeight();

	/**
	 * Write the current contents of this Film to a file as an image.
	 * 
	 * @param imageFile
	 * @throws IOException
	 *             if any exception occurred during file-writing
	 */
	public void writeImage(final File imageFile, ImageFormat format);

	public enum ImageFormat {
		PNG("png"), JPEG("jpeg"), BMP("bmp"), GIF("gif"), WBMP("wbmp");

		private String formatName;

		ImageFormat(String formatName) {
			this.formatName = formatName;
		}

		public String getFormatName() {

			return formatName;
		}
	}
}
