package org.snowjak.rays3.geometry.shape;

import java.awt.Shape;
import java.util.List;
import java.util.function.Supplier;

import org.snowjak.rays3.bxdf.BSDF;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.intersect.Interactable;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.transform.Transform;
import org.snowjak.rays3.transform.Transformable;

/**
 * A Primitive encompasses both a {@link Shape} and a {@link BSDF}. While a
 * Shape merely defines geometry, a Primitive gives that geometry color etc.
 * 
 * @author snowjak88
 */
public class Primitive implements Interactable, Transformable {

	private final AbstractShape	shape;
	private final BSDF			bsdf;

	public Primitive(AbstractShape shape, BSDF bsdf) {

		this.shape = shape;
		this.bsdf = bsdf;
	}

	public AbstractShape getShape() {

		return shape;
	}

	public BSDF getBsdf() {

		return bsdf;
	}

	@Override
	public List<Transform> getWorldToLocalTransforms() {

		return shape.getWorldToLocalTransforms();
	}

	@Override
	public List<Transform> getLocalToWorldTransforms() {

		return shape.getLocalToWorldTransforms();
	}

	@Override
	public void appendTransform(Transform transform) {

		shape.appendTransform(transform);
	}

	@Override
	public boolean isInteracting(Ray ray) {

		return shape.isInteracting(ray);
	}

	@Override
	public boolean isLocalInteracting(Ray ray) {

		return shape.isLocalInteracting(ray);
	}

	@Override
	public Interaction getIntersection(Ray ray) {

		Interaction interaction = shape.getIntersection(ray);

		if (interaction == null)
			return null;

		return new Interaction(interaction, this);
	}

	@Override
	public Interaction getLocalIntersection(Ray ray) {

		Interaction interaction = shape.getLocalIntersection(ray);

		if (interaction == null)
			return null;

		return new Interaction(interaction.getPoint(), interaction.getInteractingRay(), interaction.getNormal(),
				interaction.getParam(), this);
	}

	/**
	 * @see AbstractShape#sampleSurfacePoint(Sample)
	 */
	public Point sampleSurfacePoint(Supplier<Point2D> sampleSupplier) {

		return shape.sampleSurfacePoint(sampleSupplier);
	}

	/**
	 * @see AbstractShape#sampleSurfacePoint(Sample, Point)
	 */
	public Point sampleSurfacePoint(Supplier<Point2D> sampleSupplier, Point facing) {

		return shape.sampleSurfacePoint(sampleSupplier, facing);
	}

	/**
	 * @see AbstractShape#computeSolidAngle(Point)
	 */
	public double computeSolidAngle(Point viewedFrom) {

		return shape.computeSolidAngle(viewedFrom);
	}

}
