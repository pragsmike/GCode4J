package org.gorb.gcode;

public interface PlayerListener extends GCodeMachineListener
{
	void fileLoaded(String fileName, String fileContents);
	void startedPlaying(String fileName);
	void finishedPlaying(String fileName);
	void abortedPlaying(String fileName);
	void pausedPlaying(String fileName);
	void resumedPlaying(String fileName);
	
}