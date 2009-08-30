package org.gorb.gcode;


import java.io.InputStream;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class PlayerTest
{
	Player player = new Player();
	
	FakeSender fakeSender = new FakeSender();
	{
		fakeSender.setListener(player);
		player.setSender(fakeSender);
	}

	@Test
	public void testOneLine() throws Exception {
		player.playOneLine("G20\n");
	}
	@Test
	public void testTwoLines() throws Exception {
		player.playOneLine("G20\n");
		player.playOneLine("G91\n");
	}
	
	@Test
	public void testFile() throws Exception {
		InputStream inputStream = new ClassPathResource("org/gorb/gcode/testLines.txt").getInputStream();
		player.playStream(inputStream);
	}
}
