package org.gorb.gcode;

import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;

import org.easymock.classextension.EasyMock;
import org.junit.Test;

import processing.serial.PortableSerial;
import processing.serial.Serial;
import processing.serial.SerialListener;

public class PortableSerialTest
{
	public static class Listener implements SerialListener {
		Serial heard;
		int readC;
		public void serialEvent(Serial s) {
			heard = s;
			readC = s.read();
		}
	}
	@Test
	public void testSerial() throws Exception {
		Listener l = new Listener();
		Serial portableSerial = new PortableSerial(l, "com3:", 9600);
		
		SerialPort sp = EasyMock.createMock(SerialPort.class);
		SerialPortEvent se = new SerialPortEvent(sp, SerialPortEvent.DATA_AVAILABLE, false, false);
		portableSerial.input = new ByteArrayInputStream("a".getBytes());
		portableSerial.serialEvent(se);

		assertEquals(portableSerial, l.heard);
		assertEquals('a', l.readC);
	}
}
