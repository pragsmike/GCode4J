package org.gorb.gcode.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.gorb.gcode.PlayerListener;

public class Player
{
	private String					fileContents;
	private String					fileName;
	private File					openedFile;

	private boolean					paused;
	private boolean					aborted;
	Iterator<String>				linesIterator;
	private PlayerListener			listener;
	private final GCodeMachine 		gCodeMachine;
	

	public Player(GCodeMachine gCodeMachine) {
		this.gCodeMachine = gCodeMachine;
	}
	public boolean isFileOpen() {
		return fileContents != null;
	}
	public void openFile(File f) throws IOException {
		openedFile = f;
		fileName = f.getName();
		fileContents = FileUtils.readFileToString(f);
		fileContents = fileContents.replaceAll("\r", "");
		getListener().fileLoaded(fileName, fileContents);
	}
	public void reloadFile() throws IOException {
		openFile(openedFile);
	}

	void play() {
		if (!isFileOpen())
			throw new IllegalStateException("No file is loaded, can't start");
		getListener().startedPlaying(fileName);
		List<String> lines = Arrays.asList(fileContents.split("\n"));
		linesIterator = lines.iterator();
		aborted = false;
		nextLine();
	}
	public void nextLine() {
		if (linesIterator == null)
			return;
		if (aborted) {
			linesIterator = null;
			getListener().abortedPlaying(fileName);
			paused = false;
			synchronized (this) { notifyAll(); }
			return;
		}
		if (paused)
			return;
		if (!linesIterator.hasNext()) {
			linesIterator = null;
			getListener().finishedPlaying(fileName);
			synchronized (this) { notifyAll(); }
			return;
		}
		String line = linesIterator.next();
		gCodeMachine.send(line);
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
		getListener().pausedPlaying(fileName);
	}
	public void resume() {
		paused = false;
		getListener().resumedPlaying(fileName);
		nextLine();
	}
	public boolean isPaused() {
		return paused;
	}
	public void setListener(PlayerListener listener) {
		this.listener = listener;
	}
	public PlayerListener getListener() {
		return listener;
	}
}