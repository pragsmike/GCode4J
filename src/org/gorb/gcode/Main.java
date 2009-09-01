package org.gorb.gcode;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import processing.serial.PortableSerial;
import processing.serial.Serial;

public class Main
{
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		String fileName = "gcode.txt";

		// Print a list of the serial ports, for debugging purposes:
		for (String n : Serial.list()) {
		  System.out.println(n);
		}
		String portName = "COM11";
		int baud = 9600;

		Sender sender = new Sender();
		Serial serial = new PortableSerial(sender, portName, baud);
		sender.setOutSerial(serial);
		
		Player player = new Player();
		player.setSender(sender);
		sender.setListener(player);
		
		List<String> lines = FileUtils.readLines(new File(fileName));
		for (String line : lines) {
			player.playOneLine(line.replaceAll("\r", "") + "\n");
//			System.in.read();
		}

	}
}
