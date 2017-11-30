package org.snowjak.rays3.bxdf;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

	private LambertianBRDF	bdrf;
	private Primitive		primitive;
	private Interaction		interaction;
	private Sample			sample;

	@Before
	public void setUp() throws Exception {

		final Texture texture = new ConstantTexture(new RGBSpectrum(RGB.RED));
		final Spectrum emissive = RGBSpectrum.BLACK;
		this.bdrf = new LambertianBRDF(texture, emissive, 1.3);

		sample = new Sample(new SimplePseudorandomSampler(16, 16, 1), 8.5, 8.5, 0.5, 0.5);
		primitive = new Primitive(new SphereShape(1.0), bdrf);
		interaction = new Interaction(new Point(0, 1, 0),
				new Ray(new Point(-1, 2, 0), new Vector(1, -1, 0).normalize()), new Normal(Vector.J),
				new Point2D(0.5, 0.5), primitive);
	}

	@Test
	public void testF_r() {

		final Spectrum reflected = bdrf.f_r(interaction, sample, sample.getAdditionalTwinSample("test", 1), Vector.J);

		assertEquals("Reflected-Red was not as expected!", 1d, reflected.toRGB().getRed(), 0.00001);
		assertEquals("Reflected-Green was not as expected!", 0d, reflected.toRGB().getGreen(), 0.00001);
		assertEquals("Reflected-Blue was not as expected!", 0d, reflected.toRGB().getBlue(), 0.00001);
	}

	@Test
	public void testsampleL_e() {

		Spectrum emissive = bdrf.sampleL_e(interaction, sample, sample.getAdditionalTwinSample("test", 1));

		assertEquals("Emissive-Red was not as expected!", 0d, emissive.toRGB().getRed(), 0.00001);
		assertEquals("Emissive-Green was not as expected!", 0d, emissive.toRGB().getGreen(), 0.00001);
		assertEquals("Emissive-Blue was not as expected!", 0d, emissive.toRGB().getBlue(), 0.00001);
	}

	@Test
	public void testSampleW_i() {

		for (int i = 0; i < 32; i++) {

			final Vector sampledReflection = bdrf.sampleW_i(interaction, sample,
					sample.getAdditionalTwinSample("test", 1));
			final double dotProduct = sampledReflection
					.normalize()
						.dotProduct(interaction.getNormal().asVector().normalize());

			assertTrue("Sampled reflection vector is not as expected!", ( dotProduct >= 0d ) && ( dotProduct <= 1d ));
		}
	}

	@Test
	public void testPdfW_i() {

		assertEquals("PDF of w_i should always be 1 / (2 * pi)", 1d / ( 2d * PI ),
				bdrf.pdfW_i(interaction, sample, sample.getAdditionalTwinSample("test", 1), null), 0.00001);
	}

}
