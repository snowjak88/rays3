package org.snowjak.rays3.camera;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.sample.Sampler;
import org.snowjak.rays3.sample.StratifiedSampler;

public class PinholeCameraTest {

	private Camera camera;

	@Before
	public void setUp() throws Exception {

		camera = new PinholeCamera(100, 100, 4d, 4d, new Point(0, 0, -5), new Point(0, 0, 0), Vector.J, 8);
	}

	@Test
	public void testGetRayDoubleDoubleDoubleDouble_center() {

		Ray ray = camera.getRay(50d, 50d, 0.5d, 0.5d);

		assertEquals("Ray origin-X not as expected!", 0d, ray.getOrigin().getX(), 0.00001);
		assertEquals("Ray origin-Y not as expected!", 0d, ray.getOrigin().getY(), 0.00001);
		assertEquals("Ray origin-Z not as expected!", -5d, ray.getOrigin().getZ(), 0.00001);

		assertEquals("Ray direction-X not as expected!", 0d, ray.getDirection().getX(), 0.00001);
		assertEquals("Ray direction-Y not as expected!", 0d, ray.getDirection().getY(), 0.00001);
		assertEquals("Ray direction-Z not as expected!", 1d, ray.getDirection().getZ(), 0.00001);
	}

	@Test
	public void testGetRayDoubleDoubleDoubleDouble_edge() {

		Ray ray = camera.getRay(100d, 50d, 0.5d, 0.5d);

		assertEquals("Ray origin-X not as expected!", 2d, ray.getOrigin().getX(), 0.00001);
		assertEquals("Ray origin-Y not as expected!", 0d, ray.getOrigin().getY(), 0.00001);
		assertEquals("Ray origin-Z not as expected!", -5d, ray.getOrigin().getZ(), 0.00001);

		assertEquals("Ray direction-X not as expected!", 0.242536d, ray.getDirection().getX(), 0.00001);
		assertEquals("Ray direction-Y not as expected!", 0d, ray.getDirection().getY(), 0.00001);
		assertEquals("Ray direction-Z not as expected!", 0.970143d, ray.getDirection().getZ(), 0.00001);
	}

	@Test
	public void testGetRay_stratifiedSampler() {

		Sampler sampler = new StratifiedSampler(0, 0, (int) camera.getFilmSizeX() - 1, (int) camera.getFilmSizeY() - 1,
				1);

		Optional<Sample> op_sample;

		while (( op_sample = sampler.getNextSample() ).isPresent()) {

			Sample sample = op_sample.get();

			Ray ray = camera.getRay(sample);

			final double expectedCameraX_min = ( sample.getImageX() - ( (double) sampler.getFilmSizeX() / 2d ) - 0.5d )
					* ( camera.getImagePlaneSizeX() / camera.getFilmSizeX() );
			final double expectedCameraY_min = ( sample.getImageY() - ( (double) sampler.getFilmSizeY() / 2d ) - 0.5d )
					* ( camera.getImagePlaneSizeY() / camera.getFilmSizeY() );

			final double expectedCameraX_max = ( sample.getImageX() - ( (double) sampler.getFilmSizeX() / 2d ) + 0.5d )
					* ( camera.getImagePlaneSizeX() / camera.getFilmSizeX() );
			final double expectedCameraY_max = ( sample.getImageY() - ( (double) sampler.getFilmSizeY() / 2d ) + 0.5d )
					* ( camera.getImagePlaneSizeY() / camera.getFilmSizeY() );

			assertTrue(
					"Ray origin-X (" + Double.toString(ray.getOrigin().getX()) + ") not within expected bounds ("
							+ Double.toString(expectedCameraX_min) + ", " + Double.toString(expectedCameraX_max) + ")!",
					( ray.getOrigin().getX() >= expectedCameraX_min ) && ( ray
							.getOrigin()
								.getX() <= expectedCameraX_max ));
			assertTrue(
					"Ray origin-Y (" + Double.toString(ray.getOrigin().getY()) + ") not within expected bounds ("
							+ Double.toString(expectedCameraY_min) + ", " + Double.toString(expectedCameraY_max) + ")!",
					( ray.getOrigin().getY() >= expectedCameraY_min ) && ( ray
							.getOrigin()
								.getY() <= expectedCameraY_max ));

		}

	}

}
