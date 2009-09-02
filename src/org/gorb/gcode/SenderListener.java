package org.gorb.gcode;

public interface SenderListener {
	public void ok();

	public void status(String string);

	public void coldStarted();
}