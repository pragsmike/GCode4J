package org.gorb.gcode.edit;

import static org.junit.Assert.*;

import java.awt.Rectangle;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class EditorTest
{
	Editor editor = new Editor();
	
	@SuppressWarnings("unchecked")
	@Test
	public void testIt() throws Exception {
		List<String> lines = IOUtils.readLines(new ClassPathResource("emmy.nc").getInputStream());
		assertEquals(135, lines.size());

		Rectangle rect = new Rectangle(0,0,1,1);

		List<String> filteredLines = editor.gcodesWithinRect(lines, rect);
		
		assertEquals(104, filteredLines.size());
	}

	@Test
	public void testParse() throws Exception {
		assertXY("G1 X0 Y0", 0., 0.);
		assertXY("G1 X0. Y0.", 0., 0.);
		assertXY("G1 X1 Y1", 1., 1.);
		assertXY("G1 X1. Y1.", 1., 1.);
		assertXY("G1 X1.1 Y1.1", 1.1, 1.1);
		assertXY("G1 X1.1Y1.1", 1.1, 1.1);
		assertXY("G1X1.1Y1.1", 1.1, 1.1);
	}

	private void assertXY(String gcode, double expectedX, double expectedY) {
		editor.parse(gcode);
		assertEquals(expectedX, editor.x, 1e-5);
		assertEquals(expectedY, editor.y, 1e-5);
	}
	
	@Test
	public void testGcodeLine() throws Exception {
		assertTrue(editor.inBoundingBox("G1 X0 Y0", new Rectangle(0,0,1,1)));
		assertFalse(editor.inBoundingBox("G1 X1 Y1", new Rectangle(0,0,1,1)));
		assertFalse(editor.inBoundingBox("G1 X10 Y10", new Rectangle(0,0,1,1)));

	}
}
