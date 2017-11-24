package org.snowjak.rays3.bxdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays3.bxdf.BSDF.ReflectType;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.geometry.shape.Primitive;
import org.snowjak.rays3.geometry.shape.SphereShape;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.sample.SimplePseudorandomSampler;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.ConstantTexture;
import org.snowjak.rays3.texture.Texture;

public class LambertianBDRFTest {

	private LambertianBRDF	bdrf;
	private Primitive		primitive;
	private Interaction		interaction;
	private Sample			sample;

	@Before
	public void setUp() throws Exception {

		final Texture texture = new ConstantTexture(new RGBSpectrum(RGB.RED));
		final Texture emissive = new ConstantTexture(new RGBSpectrum());
		this.bdrf = new LambertianBRDF(texture, emissive, 1.3, false);

		sample = new Sample(new SimplePseudorandomSampler(16, 16, 1), 8.5, 8.5, 0.5, 0.5);
		primitive = new Primitive(new SphereShape(1.0), bdrf);
		interaction = new Interaction(new Point(0, 1, 0),
				new Ray(new Point(-1, 2, 0), new Vector(1, -1, 0).normalize()), new Normal(Vector.J),
				new Point2D(0.5, 0.5), primitive);
	}

	@Test
	public void testGetReflectedRadiance() {

		final Spectrum reflected = bdrf.getReflectiveColoration(interaction, null, 0.5);

		assertEquals("Reflected-Red was not as expected!", 1d, reflected.toRGB().getRed(), 0.00001);
		assertEquals("Reflected-Green was not as expected!", 0d, reflected.toRGB().getGreen(), 0.00001);
		assertEquals("Reflected-Blue was not as expected!", 0d, reflected.toRGB().getBlue(), 0.00001);
	}

	@Test
	public void testGetEmissiveRadiance() {

		Spectrum emissive = bdrf.getEmissiveRadiance(interaction, null, 0.5);

		assertEquals("Emissive-Red was not as expected!", 0d, emissive.toRGB().getRed(), 0.00001);
		assertEquals("Emissive-Green was not as expected!", 0d, emissive.toRGB().getGreen(), 0.00001);
		assertEquals("Emissive-Blue was not as expected!", 0d, emissive.toRGB().getBlue(), 0.00001);
	}

	@Test
	public void testSampleReflectionVector_diffuse() {

		for (int i = 0; i < 32; i++) {

			final Vector sampledReflection = bdrf.sampleReflectionVector(interaction.getPoint(),
					interaction.getW_e(), interaction.getNormal(), sample,
					ReflectType.DIFFUSE);
			final double dotProduct = sampledReflection
					.normalize()
						.dotProduct(interaction.getNormal().asVector().normalize());

			assertTrue("Sampled reflection vector is not as expected!", ( dotProduct >= 0d ) && ( dotProduct <= 1d ));
		}
	}

	@Test
	public void testSampleReflectionVector_specular() {

		final Vector sampledReflection = bdrf.sampleReflectionVector(interaction.getPoint(),
				interaction.getW_e(), interaction.getNormal(), sample,
				ReflectType.SPECULAR);
		final Vector expectedReflection = new Vector(1, 1, 0).normalize();

		assertEquals("Specular reflection-X is not as expected!", expectedReflection.getX(), sampledReflection.getX(),
				0.00001);
		assertEquals("Specular reflection-Y is not as expected!", expectedReflection.getY(), sampledReflection.getY(),
				0.00001);
		assertEquals("Specular reflection-Z is not as expected!", expectedReflection.getZ(), sampledReflection.getZ(),
				0.00001);

	}

	@Test
	public void testReflectionPDF_diffuse() {

		for (int i = 0; i < 32; i++) {

			final Vector sampledReflection = bdrf.sampleReflectionVector(interaction.getPoint(),
					interaction.getInteractingRay().getDirection().negate(), interaction.getNormal(), sample,
					ReflectType.DIFFUSE);
			final double dotProduct = sampledReflection
					.normalize()
						.dotProduct(interaction.getNormal().asVector().normalize());

			assertEquals("Reflection PDF is not as expected!", dotProduct / 2d,
					bdrf.reflectionPDF(interaction.getPoint(), interaction.getW_e(),
							sampledReflection, interaction.getNormal(), ReflectType.DIFFUSE),
					0.00001);
		}
	}

	@Test
	public void testReflectionPDF_specular() {

		final Vector sampledReflection_specular = bdrf.sampleReflectionVector(interaction.getPoint(),
				interaction.getInteractingRay().getDirection().negate(), interaction.getNormal(), sample,
				ReflectType.SPECULAR);

		assertEquals("Reflection PDF is not as expected!", 1d,
				bdrf.reflectionPDF(interaction.getPoint(), interaction.getW_e(),
						sampledReflection_specular, interaction.getNormal(), ReflectType.SPECULAR),
				0.00001);

		final Vector sampledReflection_other = interaction.getW_e();

		assertEquals("Reflection PDF is not as expected!", 0d,
				bdrf.reflectionPDF(interaction.getPoint(), interaction.getW_e(),
						sampledReflection_other, interaction.getNormal(), ReflectType.SPECULAR),
				0.00001);
	}

}
