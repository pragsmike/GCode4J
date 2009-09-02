package org.gorb.gcode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class Player  implements SenderListener
{
	private Sender	sender;
	String lastSent;

	public void setSender(Sender sender) {
		this.sender = sender;
	}

	public void playOneLine(String string) {
		System.out.print(string);
		sender.send(string);
		lastSent = string;
		synchronized (this) {
			try {
				wait(30000);
			} catch (InterruptedException e) {
			}
		}
	}
	@SuppressWarnings("unchecked")
	public void playStream(InputStream inputStream) throws IOException {
		List<String> lines = IOUtils.readLines(inputStream);
		for (String line : lines) {
			playOneLine(line.replaceAll("\r", "") + "\n");
		}
	}

	@Override
	public void ok() {
		System.out.println("ok!");
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public void status(String string) {
		System.out.println(string);
	}

	@Override
	public void coldStarted() {
		System.out.println("Controller has started");
	}
}