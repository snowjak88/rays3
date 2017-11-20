package org.snowjak.rays3.film;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays3.Global;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * Extracts information on each Sample and Spectrum received for subsequent
 * analysis.
 * 
 * @author snowjak88
 */
public class SamplesExtractFilm implements Film {

	private final BlockingQueue<Pair<Sample, Spectrum>>	results;
	private final AtomicInteger							samplesAdded;

	private FileWriter									fileWriter	= null;

	public SamplesExtractFilm(File extractFile) {

		results = new LinkedBlockingQueue<>();
		samplesAdded = new AtomicInteger(0);

		try {
			fileWriter = new FileWriter(extractFile);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (fileWriter != null)
			Global.EXECUTOR.submit(new ResultAppendingRunnable(fileWriter, results));
	}

	private static class ResultAppendingRunnable implements Runnable {

		private final FileWriter							fileWriter;
		private final BlockingQueue<Pair<Sample, Spectrum>>	results;
		private int samplesWritten;

		public ResultAppendingRunnable(FileWriter fileWriter, BlockingQueue<Pair<Sample, Spectrum>> results) {

			this.fileWriter = fileWriter;
			this.results = results;
			this.samplesWritten = 0;
		}

		@Override
		public void run() {

			Pair<Sample, Spectrum> result = null;
			do {
				try {
					result = results.take();

					samplesWritten++;
					
					fileWriter.write(String.format("%d\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\n",
							Film.convertContinuousToDiscrete(result.getFirst().getImageX()),
							Film.convertContinuousToDiscrete(result.getFirst().getImageY()),
							result.getFirst().getImageX(), result.getFirst().getImageY(), result.getFirst().getLensU(),
							result.getFirst().getLensV(), result.getFirst().getT(), result.getSecond().toRGB().getRed(),
							result.getSecond().toRGB().getGreen(), result.getSecond().toRGB().getBlue()));

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} while (samplesWritten < result.getFirst().getSampler().totalSamples());
		}

	}

	@Override
	public void addSample(Sample sample, Spectrum radiance) {

		try {
			results.put(new Pair<>(sample, radiance));

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		samplesAdded.incrementAndGet();
	}

	@Override
	public int countSamplesAdded() {

		return samplesAdded.get();
	}

}