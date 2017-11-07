package org.snowjak.rays3.camera;

import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;

/**
 * Implements a simple Pinhole Camera -- i.e., a camera with a lens of
 * 0-diameter.
 * 
 * @author snowjak88
 */
public class PinholeCamera extends Camera {

	private Vector focusPoint;

	public PinholeCamera(Point eyePoint, Point lookAt, Vector up, double focalLength) {
		super(eyePoint, lookAt, up);

		this.focusPoint = new Vector(0d, 0d, -focalLength);
	}

	@Override
	public Ray getRay(double imageX, double imageY, double lensU, double lensV) {

		//
		// We disregard lensU and lensV -- this is a pinhole camera, its lens is
		// assumed to be of a point size!
		//
		Vector origin = new Vector(imageX, imageY, 0d);
		Vector direction = origin.subtract(focusPoint).normalize();

		return cameraToWorld(new Ray(new Point(origin), direction));
	}

}
