package org.snowjak.rays3.texture.mapping;

import org.snowjak.rays3.geometry.Point2D;

/**
 * Models how 2D object-surface-parameterization coordinates are mapped to 2D
 * texture-space coordinates.
 * 
 * @author snowjak88
 */
public interface TextureMapping {

	public Point2D map(Point2D point);
}
