package org.snowjak.rays3.sample;

import java.util.function.Supplier;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Represents a single sample from the world.
 * <p>
 * A Sample is composed of many different "sub-samples", which can include
 * dimensions such as:
 * <ul>
 * <li>Location on the image-plane (x,y)</li>
 * <li>Location on the camera-lens (u,v)</li>
 * <li>Time relative to camera-shutter open/close (t)</li>
 * <li>Spectrum to sample (nm)</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 */
public class Sample {

	private Sampler								sampler;

	private final double						imageX, imageY;
	private final double						imageU, imageV;
	private final double						lensU, lensV;
	private final double						t;
	private final Spectrum						wavelength;

	private final Supplier<Supplier<Double>>	singleSampleSupplier;
	private final Supplier<Supplier<Point2D>>	twinSampleSupplier;

	/**
	 * Construct a new Sample.
	 * <p>
	 * The lens-location to be sampled, ( <code>lensU</code>,
	 * <code>lensV</code>), is given a default value of (0.5, 0.5).
	 * </p>
	 * <p>
	 * The time of exposure, <code>t</code>, is given a default value of 0.5
	 * (i.e., at the camera shutter's halfway, wide-open point).
	 * </p>
	 * <p>
	 * The wavelength to be sampled is given a default value of
	 * <code>null</code> (i.e., no particular wavelength).
	 * </p>
	 * <p>
	 * For the embedded {@link Supplier}s, default implementations are provided
	 * (relying on Java's built-in psuedo-random number generation).
	 * </p>
	 * 
	 * @param sampler
	 * @param imageX
	 * @param imageY
	 * @param imageU
	 * @param imageV
	 */
	public Sample(Sampler sampler, double imageX, double imageY, double imageU, double imageV) {
		this(sampler, imageX, imageY, imageU, imageV, 0.5, 0.5);
	}

	/**
	 * Construct a new Sample.
	 * <p>
	 * The time of exposure, <code>t</code>, is given a default value of 0.5
	 * (i.e., at the camera shutter's halfway, wide-open point).
	 * </p>
	 * <p>
	 * The wavelength to be sampled is given a default value of
	 * <code>null</code> (i.e., no particular wavelength).
	 * </p>
	 * <p>
	 * For the embedded {@link Supplier}s, default implementations are provided
	 * (relying on Java's built-in psuedo-random number generation).
	 * </p>
	 * 
	 * @param sampler
	 * @param imageU
	 * @param imageV
	 * @param lensU
	 * @param lensV
	 */
	public Sample(Sampler sampler, double imageX, double imageY, double imageU, double imageV, double lensU,
			double lensV) {
		this(sampler, imageX, imageY, imageU, imageV, lensU, lensV, 0.5d, null);
	}

	/**
	 * Construct a new Sample.
	 * <p>
	 * For the embedded {@link Supplier}s, default implementations are provided
	 * (relying on Java's built-in psuedo-random number generation).
	 * </p>
	 * 
	 * @param sampler
	 * @param imageX
	 * @param imageY
	 * @param imageU
	 * @param imageV
	 * @param lensU
	 * @param lensV
	 * @param t
	 * @param wavelength
	 */
	public Sample(Sampler sampler, double imageX, double imageY, double imageU, double imageV, double lensU,
			double lensV, double t, Spectrum wavelength) {
		this(sampler, imageX, imageY, imageU, imageV, lensU, lensV, t, wavelength, new Supplier<Supplier<Double>>() {

			@Override
			public Supplier<Double> get() {

				return new Supplier<Double>() {

					@Override
					public Double get() {

						return Global.RND.nextDouble();
					}

				};
			}

		}, new Supplier<Supplier<Point2D>>() {

			@Override
			public Supplier<Point2D> get() {

				return new Supplier<Point2D>() {

					@Override
					public Point2D get() {

						return new Point2D(Global.RND.nextDouble(), Global.RND.nextDouble());
					}

				};
			}

		});
	}

	/**
	 * Construct a new Sample.
	 * 
	 * @param sampler
	 * @param imageX
	 * @param imageY
	 * @param imageU
	 * @param imageV
	 * @param lensU
	 * @param lensV
	 * @param t
	 * @param wavelength
	 * @param singleSampleSupplier
	 * @param twinSampleSupplier
	 */
	public Sample(Sampler sampler, double imageX, double imageY, double imageU, double imageV, double lensU,
			double lensV, double t, Spectrum wavelength, Supplier<Supplier<Double>> singleSampleSupplier,
			Supplier<Supplier<Point2D>> twinSampleSupplier) {

		this.sampler = sampler;
		this.imageX = imageX;
		this.imageY = imageY;
		this.imageU = imageU;
		this.imageV = imageV;
		this.lensU = lensU;
		this.lensV = lensV;
		this.t = t;
		this.wavelength = wavelength;
		this.singleSampleSupplier = singleSampleSupplier;
		this.twinSampleSupplier = twinSampleSupplier;
	}

	/**
	 * @return the {@link Sampler} which generated this Sample
	 */
	public Sampler getSampler() {

		return sampler;
	}

	/**
	 * @return the image X coordinate this sample corresponds to
	 */
	public double getImageX() {

		return imageX;
	}

	/**
	 * @return the image Y coordinate this sample corresponds to
	 */
	public double getImageY() {

		return imageY;
	}

	/**
	 * @return the image U coordinate to be sampled
	 */
	public double getImageU() {

		return imageU;
	}

	/**
	 * @return the image V coordinate to be sampled
	 */
	public double getImageV() {

		return imageV;
	}

	/**
	 * @return the lens U coordinate to be sampled
	 */
	public double getLensU() {

		return lensU;
	}

	/**
	 * @return the lens V coordinate to be sampled
	 */
	public double getLensV() {

		return lensV;
	}

	/**
	 * @return the camera-shutter time to be sampled
	 */
	public double getT() {

		return t;
	}

	/**
	 * @return the wavelength to be sampled, or <code>null</code> if no
	 *         wavelength in particular
	 */
	public Spectrum getWavelength() {

		return wavelength;
	}

	/**
	 * @return a {@link Supplier} of additional single (i.e.,
	 *         <code>double</code>) samples
	 */
	public Supplier<Double> getAdditionalSingleSampleSupplier() {

		return singleSampleSupplier.get();
	}

	/**
	 * @return a {@link Supplier} of additional twin (i.e., {@link Point2D})
	 *         samples
	 */
	public Supplier<Point2D> getAdditionalTwinSample() {

		return twinSampleSupplier.get();
	}

}
