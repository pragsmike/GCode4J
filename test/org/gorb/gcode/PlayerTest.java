package org.gorb.gcode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class PlayerTest
{
	Player player = new Player();
	
	class FakeSender extends Sender
	{
		boolean busy;
		int i = 0;
		String[] expected = {"G20\n", "G91\n"};
		
		@Override
		public void send(String string) {
			if (busy) 
				fail("Called send before ok reported");
			assertEquals(expected[i++], string);
			busy = true;
			reportOk();
		}

		Thread t;
		private void reportOk() {
			t = new Thread() {
				@Override
				public void run() {
					try { Thread.sleep(1);	} catch (InterruptedException e) {	}
					busy = false;
					senderListener.ok();
				}
			};
			t.start();
		}
	}
	@Test
	public void testOneLine() throws Exception {
		FakeSender fakeSender = new FakeSender();
		fakeSender.setListener(player);
		player.setSender(fakeSender);
		player.playOneLine("G20\n");
	}
	@Test
	public void testTwoLines() throws Exception {
		FakeSender fakeSender = new FakeSender();
		fakeSender.setListener(player);
		player.setSender(fakeSender);
		player.playOneLine("G20\n");
		player.playOneLine("G91\n");
	}
	
	@Test
	public void testFile() throws Exception {
		FakeSender fakeSender = new FakeSender();
		fakeSender.setListener(player);
		player.setSender(fakeSender);

		InputStream inputStream = new ClassPathResource("org/gorb/gcode/testLines.txt").getInputStream();
		player.playStream(inputStream);
	}
}
