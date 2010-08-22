package org.gorb.gcode.impl;

import java.util.ArrayList;
import java.util.List;

import org.gorb.gcode.GCodeMachineListener;
import org.gorb.gcode.SenderListener;



public class GCodeMachine implements SenderListener
{
	private List<GCodeMachineListener>	listeners = new ArrayList<GCodeMachineListener>();
	private Sender					sender;
	private Jogger					jogger;

	private boolean					busy;
	boolean 						positionAbsolute = true;

	void execImmediate(String line) {
		send(line);
	}
	void jog(String distance, String direction) {
		boolean wasAbsolute = positionAbsolute;
		if (wasAbsolute) {
			execImmediate("G91");
		}
		execImmediate(jogger.jog(distance, direction));
		if (wasAbsolute) {
			execImmediate("G90");
		}
	}
	void jog(String direction) {
		jog("0.001", direction);
	}
	
	void send(String line) {
		if (line.trim().startsWith("G90")) {
			positionAbsolute = true;
		} else if (line.trim().startsWith("G91")) {
			positionAbsolute = false;
		}
		for (GCodeMachineListener listener : listeners) {
			listener.sentLine(line);
		}
		sender.send(line + "\n");
		busy(true);
	}
	private void busy(boolean busy) {
		this.setBusy(busy);
		for (GCodeMachineListener listener : listeners) {
			listener.busy(busy);
		}
	}

	public void status(String string) {
		for (GCodeMachineListener listener : listeners) {
			listener.receivedLine(string);
		}
	}
	
	public void coldStarted() {
		for (GCodeMachineListener listener : listeners) {
			listener.sentLine("Controller has started");
		}
		execImmediate("G20\nG91\n");
	}
	
	public void ok() {
		busy(false);
		for (GCodeMachineListener listener : listeners) {
			listener.receivedLine("ok");
		}
	}
	
	public void shutdown() {
		sender.close();
	}
	
	public void addListener(GCodeMachineListener listener) {
		synchronized (listeners) { listeners.add(listener); }
	}
	public void setSender(Sender sender) {
		this.sender = sender;
	}
	public Sender getSender() {
		return sender;
	}
	public void setJogger(Jogger jogger) {
		this.jogger = jogger;
	}
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	public boolean isBusy() {
		return busy;
	}
	public boolean isPositionAbsolute() {
		return positionAbsolute;
	}
}
