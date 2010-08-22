package org.gorb.gcode;

import java.io.File;

import org.gorb.gcode.impl.GCodeMachine;
import org.gorb.gcode.impl.Jogger;
import org.gorb.gcode.impl.Player;
import org.gorb.gcode.impl.Sender;
import org.gorb.gcode.sim.SenderSimulator;


import processing.serial.PortableSerial;
import processing.serial.Serial;

public class GCodeMachineSetter
{
	private static final String	DEFAULT_SerialPortName	= "simulator";
	private static final String	DEFAULT_INITIAL_FILE	= "gcode.txt";
	File initialFile = new File(DEFAULT_INITIAL_FILE);

	public GCodeMachine buildMachine(GCodeMachineListener listener) {
		GCodeMachine machine = new GCodeMachine();
		machine.addListener(listener);
		machine.setJogger(new Jogger());
		setSerialPort(machine, DEFAULT_SerialPortName);

		return machine;
	}
	public Player buildPlayer(GCodeMachine machine, PlayerListener listener) {
		Player player = new Player(machine);
		player.setListener(listener);
		machine.addListener(new PlayerLink(player));
		return player;
	}
	
	static class PlayerLink implements GCodeMachineListener {
		Player player;
		public PlayerLink(Player player) {
			this.player = player;
		}
		public void receivedLine(String msg) {
			if ("ok".equals(msg))
				player.nextLine();
		}
		public void busy(boolean busy) {		}
		public void log(String msg) {}
		public void sentLine(String msg) {}
	}

	public void setSerialPort(GCodeMachine machine, String serialPortName) {
		if (machine.getSender() != null) {
			machine.getSender().close();
		}
		Sender sender = getSender(serialPortName);
		machine.setSender(sender);
		sender.setListener(machine);
	}
	
	public String[] getSerialPortNames() {
		return Serial.list();
	}

	public Sender getSender(String serialPortName) {
		if (serialPortName.toLowerCase().equals("simulator"))
			return new SenderSimulator();
		String portName = serialPortName.toUpperCase();
		int baud = 9600;

		Sender sender = new Sender();
		Serial serial = new PortableSerial(sender, portName, baud);
		sender.setOutSerial(serial);
		return sender;
	}

}