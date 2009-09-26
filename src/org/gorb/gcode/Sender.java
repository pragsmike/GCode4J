/**
 * 
 */
package org.gorb.gcode;

import processing.serial.Serial;
import processing.serial.SerialListener;

public class Sender implements SerialListener
{
	private Serial outSerial;
	protected SenderListener	senderListener;


	public void close() {
		outSerial.stop();
	}

	@Override
	public void serialEvent(Serial s) {
		int c = s.read();
		
		onChar(c);
	}

	StringBuffer buf = new StringBuffer();
	
	void onChar(int c) {
		if (c == '\n' || c == '\r') {
			if (buf.length() == 0)
				return;
			if ("start".equals(buf.toString())) {
				senderListener.coldStarted();
			} else if ("ok".equals(buf.toString())) {
				senderListener.ok();
			} else {
				senderListener.status(buf.toString());
			}
			buf.setLength(0);
			return;
		}
		buf.append((char)c);
	}

	public void send(String string) {
		outSerial.write(string);
	}

	public void setOutSerial(Serial serial) {
		this.outSerial = serial;
	}

	public Serial getOutSerial() {
		return outSerial;
	}

	public void setListener(SenderListener senderListener) {
		this.senderListener = senderListener;
	}
}