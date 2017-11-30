package org.snowjak.rays3.integrator;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.snowjak.rays3.Global;
import org.snowjak.rays3.World;
import org.snowjak.rays3.camera.Camera;
import org.snowjak.rays3.film.Film;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.sample.Sampler;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * An integrator is responsible for rendering a World:
 * <ul>
 * <li>Translating a {@link Sample} into a {@link Spectrum}</li>
 * <li>Recording that Spectrum on the specified {@link Film}</li>
 * </ul>
 * <p>
 * All implementations will implement {@link #followRay(Ray, World, Sample)}.
 * </p>
 * <p>
 * When {@link #render(World)} is called, the AbstractIntegrator will begin
 * submitting {@link RenderSampleTask}s to the {@link Global#RENDER_EXECUTOR}
 * ForkJoinPool. Each RenderSampleTask will in turn call
 * {@link #followRay(Ray, World, Sample)} on the AbstractIntegrator instance.
 * </p>
 * 
 * @author snowjak88
 */
public abstract class AbstractIntegrator {

	/**
	 * The number of {@link RenderSampleTask}s that are allowed to be queued up
	 * and not started in the {@link Global#RENDER_EXECUTOR}.
	 */
	public final static int							MAX_WAITING_SAMPLES	= 8192;
	public final static int							PREGENERATE_SAMPLES	= 65536;

	private final Camera							camera;
	private final Film								film;
	private final Sampler							sampler;
	private final BlockingQueue<Optional<Sample>>	samplesQueue;

	private final int								maxRayDepth;
	private boolean									finishedGettingSamples;

	private final Semaphore							samplesWaitingToRender;
	private final AtomicInteger						samplesCurrentlyRenderingCount;

	/**
	 * Construct a new Integrator.
	 * 
	 * @param camera
	 * @param film
	 * @param sampler
	 */
	public AbstractIntegrator(Camera camera, Film film, Sampler sampler, int maxRayDepth) {

		this.camera = camera;
		this.film = film;
		this.sampler = sampler;
		this.samplesQueue = new LinkedBlockingQueue<Optional<Sample>>(PREGENERATE_SAMPLES);

		this.maxRayDepth = maxRayDepth;

		this.finishedGettingSamples = false;
		this.samplesWaitingToRender = new Semaphore(MAX_WAITING_SAMPLES);
		this.samplesCurrentlyRenderingCount = new AtomicInteger(0);
	}

	/**
	 * Start rendering the given world. This method will create
	 * {@link RenderSampleTask}s for all {@link Sample}s returned by the
	 * configured {@link Sampler}, rendering the results of
	 * {@link AbstractIntegrator#followRay(Ray, World, Sample)} to the
	 * configured {@link Film}.
	 * 
	 * @param world
	 */
	public void render(World world) {

		final CountDownLatch pregeneratedSamples = new CountDownLatch(1);
		Global.RENDER_EXECUTOR.submit(() -> {

			System.out.println("Pre-generating " + Integer.toString(PREGENERATE_SAMPLES) + " samples ...");

			boolean pregenerating = true;
			int samplesGenerated = 0;

			Optional<Sample> sample = null;
			do {
				sample = sampler.getNextSample();
				try {
					samplesQueue.put(sample);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (pregenerating) {
					samplesGenerated++;

					if (samplesGenerated >= PREGENERATE_SAMPLES && pregeneratedSamples.getCount() > 0) {
						System.out.println("Finished pre-generating!");
						pregeneratedSamples.countDown();
						pregenerating = false;
					}
				}

			} while (sample.isPresent());
		});

		Global.RENDER_EXECUTOR.submit(() -> {

			try {
				pregeneratedSamples.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("Commencing rendering ...");
			Optional<Sample> currentSample;

			try {

				while (( currentSample = samplesQueue.take() ).isPresent()) {

					samplesWaitingToRender.acquire();

					Global.RENDER_EXECUTOR.execute(new RenderSampleTask(this, world, currentSample.get(), getCamera(),
							getFilm(), samplesWaitingToRender, samplesCurrentlyRenderingCount));
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			this.finishedGettingSamples = true;

		});
	}

	/**
	 * Responsible for estimating the radiant energy obtained by backtracing
	 * along the given {@link Ray} -- i.e., converts a Ray into a
	 * {@link Spectrum}.
	 * <p>
	 * <strong>Note</strong> that this method is called by many threads at the
	 * same time. Ensure that your implementation is thread-safe!
	 * </p>
	 * <p>
	 * To follow sub-rays on the currently-executing thread, simply call this
	 * method recursively.
	 * </p>
	 * <p>
	 * To follow sub-rays on separate threads, use
	 * {@link FollowRayRecursiveTask#fork()} and <code>join()</code>.
	 * </p>
	 * 
	 * @param ray
	 * @param world
	 * @param sample
	 * @return
	 */
	public abstract Spectrum followRay(Ray ray, World world, Sample sample);

	/**
	 * @return <code>true</code> if the configured Sampler has no more
	 *         {@link Sample}s to pick up
	 */
	public boolean isFinishedGettingSamples() {

		return finishedGettingSamples;
	}

	/**
	 * @return <code>true</code> if this Integrator has finished rendering all
	 *         {@link Sample}s
	 */
	public boolean isFinishedRenderingSamples() {

		return finishedGettingSamples && countSamplesWaitingToRender() == 0 && countSamplesCurrentlyRendering() == 0;
	}

	/**
	 * Count the number of Samples which this Integrator has picked up from its
	 * {@link Sampler} and not yet begun processing.
	 * 
	 * @return
	 */
	public int countSamplesWaitingToRender() {

		return MAX_WAITING_SAMPLES - samplesWaitingToRender.availablePermits();
	}

	/**
	 * Count the number of {@link Sample}s currently being rendered by this
	 * Integrator.
	 * 
	 * @return
	 */
	public int countSamplesCurrentlyRendering() {

		return samplesCurrentlyRenderingCount.get();
	}

	public Camera getCamera() {

		return camera;
	}

	public Film getFilm() {

		return film;
	}

	public Sampler getSampler() {

		return sampler;
	}

	public int getMaxRayDepth() {

		return maxRayDepth;
	}

	/**
	 * {@link RecursiveTask} implementation that renders a single {@link Sample}
	 * to the given {@link Film}, using a {@link FollowRayRecursiveTask}.
	 * <p>
	 * Note that this will not execute the FollowRayRecursiveTask on a separate
	 * thread. Instead, you should execute multiple RenderSampleTasks on
	 * separate threads to achieve multithreading.
	 * </p>
	 * 
	 * @author snowjak88
	 */
	public static class RenderSampleTask extends RecursiveAction {

		private static final long			serialVersionUID	= 3322955501411696398L;

		private final AbstractIntegrator	integrator;
		private final World					world;
		private final Sample				sample;
		private final Camera				camera;
		private final Film					film;
		private final Semaphore				samplesWaitingToRender;
		private final AtomicInteger			samplesCurrentlyRenderingCount;

		public RenderSampleTask(AbstractIntegrator integrator, World world, Sample sample, Camera camera, Film film,
				Semaphore samplesWaitingToRender, AtomicInteger samplesCurrentlyRenderingCount) {

			super();
			this.integrator = integrator;
			this.world = world;
			this.sample = sample;
			this.camera = camera;
			this.film = film;
			this.samplesWaitingToRender = samplesWaitingToRender;
			this.samplesCurrentlyRenderingCount = samplesCurrentlyRenderingCount;
		}

		@Override
		protected void compute() {

			this.samplesWaitingToRender.release();
			this.samplesCurrentlyRenderingCount.incrementAndGet();

			try {

				//
				// Set up the initial ray to follow.
				final Ray ray = camera.getRay(sample);

				//
				// Follow the ray.
				//
				// (notice that the initial ray-follow, at least, is kept on
				// this
				// same thread)
				final Spectrum spectrum = new FollowRayRecursiveTask(integrator, ray, world, sample)
						.invoke()
							.multiply(1d / (double) sample.getSampler().getSamplesPerPixel());

				if (sample.getSampler().isSampleAcceptable(sample, spectrum))
					film.addSample(sample, spectrum);

			} catch (Throwable t) {
				t.printStackTrace();

			} finally {
				this.samplesCurrentlyRenderingCount.decrementAndGet();
			}
		}

	}

	/**
	 * {@link RecursiveTask} implementation that will simply call
	 * {@link AbstractIntegrator#followRay(Ray, World, Sample)} for any given
	 * AbstractIntegrator instance.
	 * <p>
	 * This class is provided for those occasions when you with to follow a ray
	 * on a separate thread -- e.g., through
	 * {@link FollowRayRecursiveTask#fork()}.
	 * </p>
	 * 
	 * @author snowjak88
	 */
	public static class FollowRayRecursiveTask extends RecursiveTask<Spectrum> {

		private static final long			serialVersionUID	= 891450880425940388L;

		private final AbstractIntegrator	integrator;
		private final Ray					ray;
		private final World					world;
		private final Sample				sample;

		/**
		 * Create a new {@link FollowRayRecursiveTask}.
		 * 
		 * @param integrator
		 * @param ray
		 * @param world
		 * @param sample
		 */
		public FollowRayRecursiveTask(AbstractIntegrator integrator, Ray ray, World world, Sample sample) {
			this.integrator = integrator;
			this.ray = ray;
			this.world = world;
			this.sample = sample;
		}

		@Override
		protected Spectrum compute() {

			return integrator.followRay(ray, world, sample);
		}
	}
}
