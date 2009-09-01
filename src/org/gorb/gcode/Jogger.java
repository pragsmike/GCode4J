package org.gorb.gcode;

public class Jogger
{
	public String jog(String distance, String direction) {
		String s = "";
		String in = direction.toLowerCase();
		if (in.contains("w"))
			s = " X-" + distance;
		if (in.contains("e"))
			s = " X" + distance;
		if (in.contains("s"))
			s += " Y-" + distance;
		if (in.contains("n"))
			s +=  " Y" + distance;
		return "G1" + s;
	}
}