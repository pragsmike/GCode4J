/**
 * 
 */
package org.gorb.gcode;

import processing.serial.PortableSerial;
import processing.serial.Serial;

public class GCodeMachineSetter
{
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

	public GCodeMachine buildMachine() {
		GCodeMachine machine = new GCodeMachine();
		machine.setSender(new SenderSimulator());
		machine.setJogger(new Jogger());
		return machine;
	}

	public void setSerialPort(GCodeMachine machine, String serialPortName) {
		Sender sender = new GCodeMachineSetter().getSender(serialPortName);
		machine.setSender(sender);
	}
}