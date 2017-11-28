package org.snowjak.rays3.film;

import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * A StatisticsFilm is a {@link Film} extended with the ability to report on the
 * statistics of its gathered {@link Sample}s and {@link Spectrum}s.
 * 
 * @author snowjak88
 */
public interface StatisticsFilm extends Film {

	/**
	 * Calculate the variance of the samples gathered for the given image
	 * location.
	 * 
	 * @param imageX
	 * @param imageY
	 * @return
	 */
	public double getVariance(double imageX, double imageY);

	/**
	 * Return the count of samples gathered for the given image location.
	 * 
	 * @param imageX
	 * @param imageY
	 * @return
	 */
	public int getCountAt(double imageX, double imageY);
}
