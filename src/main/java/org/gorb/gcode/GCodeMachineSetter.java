package org.gorb.gcode;

import java.io.File;
import java.io.IOException;

import processing.serial.PortableSerial;
import processing.serial.Serial;

public class GCodeMachineSetter
{
	private static final String	DEFAULT_SerialPortName	= "simulator";	;
	private static final String	DEFAULT_INITIAL_FILE	= "gcode.txt";
	File initialFile = new File(DEFAULT_INITIAL_FILE);

	public GCodeMachine buildMachine(GCodeMachineListener listener) {
		GCodeMachine machine = new GCodeMachine();
		machine.setListener(listener);
		machine.setJogger(new Jogger());
		setSerialPort(machine, DEFAULT_SerialPortName);

		return machine;
	}

	public void startMachine(GCodeMachine machine) throws IOException {
		if (!machine.isFileOpen()) {
			machine.openFile(initialFile);
		}
	}

	public void setSerialPort(GCodeMachine machine, String serialPortName) {
		if (machine.getSender() != null) {
			machine.getSender().close();
		}
		Sender sender = getSender(serialPortName);
		machine.setSender(sender);
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