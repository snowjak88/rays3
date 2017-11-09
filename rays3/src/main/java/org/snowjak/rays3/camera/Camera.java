package org.snowjak.rays3.camera;

import org.snowjak.rays3.geometry.Matrix;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.sample.Sample;

/**
 * Represents a model for a camera and its associated system of lenses.
 * <p>
 * A Camera implementation is responsible for translating between locations in
 * the image-plane and rays in world-space. If appropriate, the Camera may also
 * accept specified locations on its lens-disc (where all its constituent lenses
 * are modeled as a single composite lens).
 * </p>
 * 
 * @author snowjak88
 */
public abstract class Camera {

	private final Matrix	cameraTwist, cameraTranslate;
	private final double	imagePlaneSizeX, imagePlaneSizeY;

	/**
	 * Construct a new Camera located at the given <code>eyePoint</code>,
	 * looking at <code>lookAt</code>, with the camera's "up" vector oriented
	 * along <code>up</code>.
	 * <p>
	 * The Camera's image-plane is considered to be centered at (0,0,0) (in
	 * camera coordinates), with maximum extents at:
	 * 
	 * <pre>
	 * [ -imagePlaneSizeX / 2, -imagePlaneSizeY / 2 ] , [ +imagePlaneSizeX / 2, +imagePlaneSizeY / 2 ]
	 * </pre>
	 * </p>
	 * 
	 * @param imagePlaneSizeX
	 * @param imagePlaneSizeY
	 * @param eyePoint
	 * @param lookAt
	 * @param up
	 */
	public Camera(double imagePlaneSizeX, double imagePlaneSizeY, Point eyePoint, Point lookAt, Vector up) {

		this.imagePlaneSizeX = imagePlaneSizeX;
		this.imagePlaneSizeY = imagePlaneSizeY;

		final Vector eyeVect = new Vector(eyePoint), lookAtVect = new Vector(lookAt);

		final Vector cameraZAxis = lookAtVect.subtract(eyeVect).normalize();
		final Vector cameraXAxis = up.crossProduct(cameraZAxis).normalize();
		final Vector cameraYAxis = cameraZAxis.crossProduct(cameraXAxis).normalize();

		//@formatter:off
		cameraTwist =
				new Matrix(new double[][] {	{ cameraXAxis.getX(), cameraYAxis.getX(), cameraZAxis.getX(), 0d },
											{ cameraXAxis.getY(), cameraYAxis.getY(), cameraZAxis.getY(), 0d },
											{ cameraXAxis.getZ(), cameraYAxis.getZ(), cameraZAxis.getZ(), 0d },
											{                 0d,                 0d,                 0d, 1d } });
		cameraTranslate =
				new Matrix(new double[][] {	{ 1d, 0d, 0d, +eyePoint.getX() },
											{ 0d, 1d, 0d, +eyePoint.getY() },
											{ 0d, 0d, 1d, +eyePoint.getZ() },
											{ 0d, 0d, 0d,               1d } });
		//@formatter:on
	}

	/**
	 * Translate the given {@link Sample} into a Ray in world-space.
	 * 
	 * @param sample
	 * @return
	 */
	public Ray getRay(Sample sample) {

		return getRay(sample.getImageU(), sample.getImageV(), sample.getLensU(), sample.getLensV());
	}

	/**
	 * Translate the given coordinates on the image-plane (each considered in
	 * the range [0.0 - 1.0]) into a Ray in world-space. It is assumed that the
	 * camera's lens is sampled at (0.5,0.5) -- i.e., through the middle of its
	 * lens-system.
	 * 
	 * @param imageU
	 *            x-coordinate in the image-plane. Clamped to the interval [0.0,
	 *            1.0]
	 * @param imageV
	 *            y-coordinate in the image-plane. Clamped to the interval [0.0,
	 *            1.0]
	 */
	public Ray getRay(double imageU, double imageV) {

		return getRay(imageU, imageV, 0.5d, 0.5d);
	}

	/**
	 * Translate the given coordinates on the image-plane (each considered in
	 * the range [0.0 - 1.0] into a Ray in world-space. The Ray is modeled so
	 * that it passes through the camera's composite-lens at the specified
	 * <code>lens</code> location (clamped to the interval [0.0 - 1.0]).
	 * 
	 * @param imageU
	 *            x-coordinate in the image-plane. Clamped to the interval [0.0,
	 *            1.0]
	 * @param imageV
	 *            y-coordinate in the image-plane. Clamped to the interval [0.0,
	 *            1.0]
	 * @param lensU
	 *            x-coordinate in the lens-plane. Clamped to the interval [0.0,
	 *            1.0]
	 * @param lensV
	 *            y-coordinate in the lens-plane. Clamped to the interval [0.0,
	 *            1.0]
	 * @return
	 */
	public abstract Ray getRay(double imageU, double imageV, double lensU, double lensV);

	/**
	 * Transform the given {@link Ray} (in camera-coordinates) into a Ray in
	 * world coordinates.
	 */
	protected Ray cameraToWorld(Ray ray) {

		Point origin = ray.getOrigin();
		Vector direction = ray.getDirection();

		origin = cameraTranslate.multiply(cameraTwist).multiply(origin);
		direction = cameraTwist.multiply(direction);

		return new Ray(origin, direction);
	}

	public double getImagePlaneSizeX() {

		return imagePlaneSizeX;
	}

	public double getImagePlaneSizeY() {

		return imagePlaneSizeY;
	}

}
