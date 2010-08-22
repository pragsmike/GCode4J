/**
 * 
 */
package org.gorb.gcode.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


class FakeSender extends Sender
{
	boolean busy;
	int i = 0;
	String[] expected = {"G20\n", "G91\n"};
	
	public void reset() { i = 0;}
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