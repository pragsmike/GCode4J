package org.gorb.gcode;

import java.util.Random;

public class SenderSimulator extends Sender 
{
	private Random random = new Random(1);
	
	@Override
	public void send(final String string) {
		System.out.print("R " + string);
		new Thread() {
			public void run() { try { respond(string); } catch (Throwable t) {}};
		}.start();
	}
	private void respond(String string) throws InterruptedException {
		Thread.sleep(random.nextInt(2000));
		senderListener.ok();
	}
}
