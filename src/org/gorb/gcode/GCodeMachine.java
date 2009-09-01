package org.gorb.gcode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class GCodeMachine implements SenderListener
{
	private GCodeMachineListener	listener;
	private Sender					sender;
	private Jogger					jogger;

	private String					fileContents;
	private String					fileName;
	Iterator<String>				linesIterator;
	private boolean					aborted;
	private boolean					paused;
	private boolean					busy;

	void execImmediate(String line) {
		send(line);
	}
	void jog(String direction) {
		execImmediate(jogger.jog("1", direction));
	}
	
	void start() {
		if (fileContents == null)
			throw new IllegalStateException("No file is loaded, can't start");
		listener.startedPlaying(fileName);
		List<String> lines = Arrays.asList(fileContents.split("\n"));
		linesIterator = lines.iterator();
		aborted = false;
		nextLine();
	}

	private void nextLine() {
		if (linesIterator == null)
			return;
		if (aborted) {
			listener.abortedPlaying(fileName);
			linesIterator = null;
			paused = false;
			synchronized (this) { notifyAll(); }
			return;
		}
		if (paused)
			return;
		if (!linesIterator.hasNext()) {
			listener.finishedPlaying(fileName);
			linesIterator = null;
			synchronized (this) { notifyAll(); }
			return;
		}
		String line = linesIterator.next();
		send(line);
	}
	
	private void send(String line) {
		sender.send(line + "\n");
		listener.sentLine(line);
		busy(true);
	}
	private void busy(boolean busy) {
		this.setBusy(busy);
		listener.busy(busy);
	}

	@Override
	public void ok() {
		busy(false);
		nextLine();
	}
	@Override
	public void status(String string) {
		nextLine();
	}
	public boolean isPlaying() {
		return linesIterator != null;
	}

	public void abort() {
		aborted = true;
		if (paused) 
			nextLine();
	}
	public void pause() {
		paused = true;
		listener.pausedPlaying(fileName);
	}
	public void resume() {
		paused = false;
		listener.resumedPlaying(fileName);
		nextLine();
	}
	public boolean isPaused() {
		return paused;
	}

	public void openFile(File f) throws IOException {
		fileName = f.getName();
		fileContents = FileUtils.readFileToString(f);
		fileContents = fileContents.replaceAll("\r", "");
		listener.fileLoaded(fileName, fileContents);
	}
	
	
	public void setListener(GCodeMachineListener listener) {
		this.listener = listener;
	}
	public GCodeMachineListener getListener() {
		return listener;
	}
	public void setSender(Sender sender) {
		this.sender = sender;
		if (sender != null)
			sender.setListener(this);
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
}