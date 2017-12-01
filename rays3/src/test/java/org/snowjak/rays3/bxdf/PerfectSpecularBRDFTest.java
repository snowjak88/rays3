package org.snowjak.rays3.bxdf;

import static org.junit.Assert.*;

import org.junit.Test;
import org.snowjak.rays3.geometry.Normal;
import org.snowjak.rays3.geometry.Point;
import org.snowjak.rays3.geometry.Point2D;
import org.snowjak.rays3.geometry.Ray;
import org.snowjak.rays3.geometry.Vector;
import org.snowjak.rays3.intersect.Interaction;
import org.snowjak.rays3.sample.Sample;
import org.snowjak.rays3.sample.SimplePseudorandomSampler;
import org.snowjak.rays3.spectrum.RGB;
import org.snowjak.rays3.spectrum.RGBSpectrum;
import org.snowjak.rays3.spectrum.Spectrum;
import org.snowjak.rays3.texture.ConstantTexture;

public class PerfectSpecularBRDFTest {

	@Test
	public void testSampleW_i() {

		final BSDF bsdf = new PerfectSpecularBRDF();
		final Interaction interaction = new Interaction(new Point(0, 0, 0),
				new Ray(new Point(-1, 1, 0), new Vector(1, -1, 0)), new Normal(Vector.J), new Point2D(1, 1), null);
		final Sample sample = new Sample(new SimplePseudorandomSampler(0, 0, 31, 31, 1), 8.0, 8.0);

		final Vector w_i = bsdf.sampleW_i(interaction, sample, sample.getAdditionalTwinSample("test", 1)).normalize();
		final Vector expected = BSDF
				.getPerfectSpecularReflectionVector(interaction.getW_e(), interaction.getNormal())
					.normalize();

		assertEquals("Reflection-X not as expected!", expected.getX(), w_i.getX(), 0.00001);
		assertEquals("Reflection-Y not as expected!", expected.getY(), w_i.getY(), 0.00001);
		assertEquals("Reflection-Z not as expected!", expected.getZ(), w_i.getZ(), 0.00001);
	}

	@Test
	public void testPdfW_i() {

		final BSDF bsdf = new PerfectSpecularBRDF();
		final Interaction interaction = new Interaction(new Point(0, 0, 0),
				new Ray(new Point(-1, 1, 0), new Vector(1, -1, 0)), new Normal(Vector.J), new Point2D(1, 1), null);
		final Sample sample = new Sample(new SimplePseudorandomSampler(0, 0, 31, 31, 1), 8.0, 8.0);

		final Vector w_i_perfect = new Vector(1, 1, 0).normalize();
		final Vector w_i_imperfect = new Vector(1, 0.333, 0).normalize();

		assertEquals("Perfect reflection should have PDF = 1!", 1.0,
				bsdf.pdfW_i(interaction, sample, sample.getAdditionalTwinSample("test", 1), w_i_perfect), 0.00001);
		assertEquals("Imperfect reflection should have PDF = 0!", 0.0,
				bsdf.pdfW_i(interaction, sample, sample.getAdditionalTwinSample("test", 1), w_i_imperfect), 0.00001);
	}

	@Test
	public void testF_r() {

		final BSDF bsdf = new PerfectSpecularBRDF(new ConstantTexture(new RGBSpectrum(RGB.RED)));
		final Interaction interaction = new Interaction(new Point(0, 0, 0),
				new Ray(new Point(-1, 1, 0), new Vector(1, -1, 0)), new Normal(Vector.J), new Point2D(1, 1), null);
		final Sample sample = new Sample(new SimplePseudorandomSampler(0, 0, 31, 31, 1), 8.0, 8.0);

		final Vector w_i_perfect = new Vector(1, 1, 0).normalize();
		final Vector w_i_imperfect = new Vector(1, 0.333, 0).normalize();

		final Spectrum f_r_perfect = bsdf.f_r(interaction, sample, sample.getAdditionalTwinSample("test", 1),
				w_i_perfect);
		final Spectrum f_r_imperfect = bsdf.f_r(interaction, sample, sample.getAdditionalTwinSample("test", 1),
				w_i_imperfect);

		assertEquals("f_r for perfect reflection (RED) not as expected!", 1.0, f_r_perfect.toRGB().getRed(), 0.00001);
		assertEquals("f_r for perfect reflection (GREEN) not as expected!", 0.0, f_r_perfect.toRGB().getGreen(),
				0.00001);
		assertEquals("f_r for perfect reflection (BLUE) not as expected!", 0.0, f_r_perfect.toRGB().getBlue(), 0.00001);

		assertEquals("f_r for imperfect reflection (RED) not as expected!", 0.0, f_r_imperfect.toRGB().getRed(),
				0.00001);
		assertEquals("f_r for imperfect reflection (GREEN) not as expected!", 0.0, f_r_imperfect.toRGB().getGreen(),
				0.00001);
		assertEquals("f_r for imperfect reflection (BLUE) not as expected!", 0.0, f_r_imperfect.toRGB().getBlue(),
				0.00001);
	}

}
