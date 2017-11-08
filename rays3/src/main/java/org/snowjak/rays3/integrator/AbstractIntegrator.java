package org.snowjak.rays3.integrator;

import org.snowjak.rays3.World;
import org.snowjak.rays3.camera.Camera;
import org.snowjak.rays3.film.Film;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.sample.Sampler;
import org.snowjak.rays3.spectrum.Spectrum;

/**
 * An integrator is responsible for rendering a World:
 * <ul>
 * <li>Translating a {@link Sample} into a {@link Spectrum}</li>
 * <li>Recording that Spectrum on the specified {@link Film}</li>
 * </ul>
 * 
 * @author snowjak88
 */
public abstract class AbstractIntegrator {

	private Camera	camera;
	private Film	film;
	private Sampler	sampler;

	/**
	 * Construct a new Integrator.
	 * 
	 * @param camera
	 * @param film
	 * @param sampler
	 */
	public AbstractIntegrator(Camera camera, Film film, Sampler sampler) {

		this.camera = camera;
		this.film = film;
		this.sampler = sampler;
	}

	public abstract void render(World world);

	public Camera getCamera() {

		return camera;
	}

	public Film getFilm() {

		return film;
	}

	public Sampler getSampler() {

		return sampler;
	}
}
