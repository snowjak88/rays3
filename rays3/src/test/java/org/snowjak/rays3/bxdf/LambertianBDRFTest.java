package org.snowjak.rays3.bxdf;

import static org.junit.Assert.*;

import static org.apache.commons.math3.util.FastMath.*;

import org.junit.Before;
import org.junit.Test;
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

	private LambertianBDRF	bdrf;
	private Primitive		primitive;
	private Interaction		interaction;
	private Sample			sample;

	@Before
	public void setUp() throws Exception {

		final Texture texture = new ConstantTexture(new RGBSpectrum(RGB.RED));
		final Texture emissive = new ConstantTexture(new RGBSpectrum());
		this.bdrf = new LambertianBDRF(texture, emissive, 1.3);

		sample = new Sample(new SimplePseudorandomSampler(16, 16), 8, 8, 0.5, 0.5);
		primitive = new Primitive(new SphereShape(1.0), bdrf);
		interaction = new Interaction(new Point(0, 1, 0),
				new Ray(new Point(-1, 2, 0), new Vector(1, -1, 0).normalize()), new Normal(Vector.J),
				new Point2D(0.5, 0.5), primitive);
	}

	@Test
	public void testGetReflectedRadiance() {

		final Spectrum reflected = bdrf.getReflectedRadiance(interaction, new Vector(1, 1, 0).normalize(), null, 0.5);

		assertEquals("Reflected-Red was not as expected!", cos(45d * PI / 180d), reflected.toRGB().getRed(), 0.00001);
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
	public void testSampleReflectionVector() {

		for (int i = 0; i < 32; i++) {

			final Vector sampledReflection = bdrf.sampleReflectionVector(interaction.getPoint(),
					interaction.getInteractingRay().getDirection().negate(), interaction.getNormal(), sample);
			final double dotProduct = sampledReflection
					.normalize()
						.dotProduct(interaction.getNormal().asVector().normalize());

			assertTrue("Sampled reflection vector is not as expected!", ( dotProduct >= 0d ) && ( dotProduct <= 1d ));
		}
	}

	@Test
	public void testReflectionPDF() {

		for (int i = 0; i < 32; i++) {

			final Vector sampledReflection = bdrf.sampleReflectionVector(interaction.getPoint(),
					interaction.getInteractingRay().getDirection().negate(), interaction.getNormal(), sample);
			final double dotProduct = sampledReflection
					.normalize()
						.dotProduct(interaction.getNormal().asVector().normalize());

			assertEquals("Reflection PDF is not as expected!", dotProduct / 2d,
					bdrf.reflectionPDF(interaction.getPoint(), interaction.getInteractingRay().getDirection().negate(),
							sampledReflection, interaction.getNormal()),
					0.00001);
		}
	}

}
