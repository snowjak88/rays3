package org.snowjak.rays3.camera;

import org.snowjak.rays3.geometry.Ray;

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

	/**
	 * Translate the given coordinates on the image-plane (each considered in
	 * the range [0.0 - 1.0]) into a Ray in world-space. It is assumed that the
	 * camera's lens is sampled at (0.5,0.5) -- i.e., through the middle of its
	 * lens-system.
	 * 
	 * @param imageX
	 *            x-coordinate in the image-plane. Clamped to the interval [0.0,
	 *            1.0]
	 * @param imageY
	 *            y-coordinate in the image-plane. Clamped to the interval [0.0,
	 *            1.0]
	 * @return
	 */
	public Ray getRay(double imageX, double imageY) {

		return getRay(imageX, imageY, 0.5d, 0.5d);
	}

	/**
	 * Translate the given coordinates on the image-plane (each considered in
	 * the range [0.0 - 1.0] into a Ray in world-space. The Ray is modeled so
	 * that it passes through the camera's composite-lens at the specified
	 * <code>lens</code> location (clamped to the interval [0.0 - 1.0]).
	 * 
	 * @param imageX
	 *            x-coordinate in the image-plane. Clamped to the interval [0.0,
	 *            1.0]
	 * @param imageY
	 *            y-coordinate in the image-plane. Clamped to the interval [0.0,
	 *            1.0]
	 * @param lensX
	 *            x-coordinate in the lens-plane. Clamped to the interval [0.0,
	 *            1.0]
	 * @param lensY
	 *            y-coordinate in the lens-plane. Clamped to the interval [0.0,
	 *            1.0]
	 * @return
	 */
	public abstract Ray getRay(double imageX, double imageY, double lensX, double lensY);
}
