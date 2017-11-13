package org.snowjak.rays3.geometry.shape;

import java.awt.Shape;
import java.util.List;

import org.snowjak.rays3.bxdf.BDSF;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.intersect.Interactable;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.transform.Transform;
import org.snowjak.rays3.transform.Transformable;

/**
 * A Primitive encompasses both a {@link Shape} and a {@link BDSF}. While a
 * Shape merely defines geometry, a Primitive gives that geometry color etc.
 * 
 * @author snowjak88
 */
public class Primitive implements Interactable, Transformable {

	private final AbstractShape	shape;
	private final BDSF			bdsf;

	public Primitive(AbstractShape shape, BDSF bdsf) {
		this.shape = shape;
		this.bdsf = bdsf;
	}

	public AbstractShape getShape() {

		return shape;
	}

	public BDSF getBdsf() {

		return bdsf;
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

		return new Interaction(interaction.getPoint(), interaction.getInteractingRay(), interaction.getNormal(),
				interaction.getParam(), this);
	}

	@Override
	public Interaction getLocalIntersection(Ray ray) {

		Interaction interaction = shape.getLocalIntersection(ray);

		if (interaction == null)
			return null;

		return new Interaction(interaction.getPoint(), interaction.getInteractingRay(), interaction.getNormal(),
				interaction.getParam(), this);
	}

}