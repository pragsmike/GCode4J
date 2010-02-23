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
	private File	openedFile;

	void execImmediate(String line) {
		send(line);
	}
	void jog(String distance, String direction) {
		execImmediate(jogger.jog(distance, direction));
	}
	void jog(String direction) {
		jog("0.001", direction);
	}
	
	void play() {
		if (!isFileOpen())
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
			linesIterator = null;
			listener.abortedPlaying(fileName);
			paused = false;
			synchronized (this) { notifyAll(); }
			return;
		}
		if (paused)
			return;
		if (!linesIterator.hasNext()) {
			linesIterator = null;
			listener.finishedPlaying(fileName);
			synchronized (this) { notifyAll(); }
			return;
		}
		String line = linesIterator.next();
		send(line);
	}
	
	private void send(String line) {
		if (line.trim().startsWith("M06")) {
//			listener.pausedForChangeTool(line.substring(3).trim());
//			pause();
//			return;
		}
		listener.sentLine(line);
		sender.send(line + "\n");
		busy(true);
	}
	private void busy(boolean busy) {
		this.setBusy(busy);
		listener.busy(busy);
	}

	@Override
	public void ok() {
		busy(false);
		listener.receivedLine("ok");
		nextLine();
	}
	@Override
	public void status(String string) {
		listener.receivedLine(string);
	}
	
	@Override
	public void coldStarted() {
		listener.sentLine("Controller has started");
		execImmediate("G20\nG91\n");
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

	public boolean isFileOpen() {
		return fileContents != null;
	}
	public void openFile(File f) throws IOException {
		openedFile = f;
		fileName = f.getName();
		fileContents = FileUtils.readFileToString(f);
		fileContents = fileContents.replaceAll("\r", "");
		listener.fileLoaded(fileName, fileContents);
	}
	public void reloadFile() throws IOException {
		openFile(openedFile);
	}
	
	public void shutdown() {
		sender.close();
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
