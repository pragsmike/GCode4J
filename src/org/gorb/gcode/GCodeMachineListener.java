package org.gorb.gcode;

public interface GCodeMachineListener 
{
	void log(String msg);
	void sentLine(String msg);
	void receivedLine(String msg);

	void busy(boolean busy);
	
	void fileLoaded(String fileName, String fileContents);
	void startedPlaying(String fileName);
	void finishedPlaying(String string);
	void abortedPlaying(String string);
	void pausedPlaying(String string);
	void resumedPlaying(String string);
}
