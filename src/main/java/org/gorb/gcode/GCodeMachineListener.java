package org.gorb.gcode;

public interface GCodeMachineListener 
{
	void log(String msg);
	void sentLine(String msg);
	void receivedLine(String msg);

	void busy(boolean busy);
}
