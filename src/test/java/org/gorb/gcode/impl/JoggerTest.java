package org.gorb.gcode.impl;

import static org.junit.Assert.*;

import org.gorb.gcode.impl.Jogger;
import org.junit.Test;

public class JoggerTest
{
	Jogger jogger = new Jogger();
	
	@Test
	public void testJog() throws Exception {
		assertEquals("G1 X1", jogger.jog("1", "e"));
		assertEquals("G1 X1", jogger.jog("1", "E"));
		assertEquals("G1 Y1", jogger.jog("1", "n"));
		assertEquals("G1 Y1", jogger.jog("1", "N"));
		assertEquals("G1 X-1", jogger.jog("1", "w"));
		assertEquals("G1 Y-1", jogger.jog("1", "S"));
		assertEquals("G1 X1 Y1", jogger.jog("1", "NE"));
		assertEquals("G1 X-1 Y-1", jogger.jog("1", "SW"));
		assertEquals("G1 X-1 Y1", jogger.jog("1", "NW"));
		assertEquals("G1 X1 Y-1", jogger.jog("1", "SE"));
		
		assertEquals("G1 Z1", jogger.jog("1", "Up"));
		assertEquals("G1 Z-1", jogger.jog("1", "Down"));


		assertEquals("G1 X-2 Y-2", jogger.jog("2", "SW"));
	}
}
