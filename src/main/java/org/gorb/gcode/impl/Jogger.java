package org.gorb.gcode.impl;

public class Jogger
{
	public String jog(String distance, String direction) {
		String s = "";
		String in = direction.toLowerCase();
		if ("up".equals(in))
			s = " Z" + distance;
		else if ("down".equals(in))
			s = " Z-" + distance;
		else {
			if (in.contains("w"))
				s = " X-" + distance;
			if (in.contains("e"))
				s = " X" + distance;
			if (in.contains("s"))
				s += " Y-" + distance;
			if (in.contains("n"))
				s +=  " Y" + distance;
		}
		return "G1" + s;
	}
}