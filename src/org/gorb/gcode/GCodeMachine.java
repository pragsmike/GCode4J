package org.gorb.gcode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class GCodeMachine implements SenderListener
{
	private GCodeMachineListener listener;
	private Sender sender;
	private String fileContents;
	private String fileName;
	
	void execImmediate(String gcode) {
		listener.log("Immediate: " + gcode);
	}
	void jog(String direction) {
		System.out.println(direction);
	}
	Iterator<String> linesIterator;
	private boolean aborted;
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
		if (aborted) {
			listener.abortedPlaying(fileName);
			synchronized (this) { notifyAll(); }
			return;
		}
		if (!linesIterator.hasNext()) {
			listener.finishedPlaying(fileName);
			synchronized (this) { notifyAll(); }
			return;
		}
		String line = linesIterator.next();
		sender.send(line + "\n");
		listener.sentLine(line);
	}
	
	@Override
	public void ok() {
		nextLine();
	}
	@Override
	public void status(String string) {
		nextLine();
	}

	void abort() {
		aborted = true;
	}
	void openFile(File f) throws IOException {
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
}
