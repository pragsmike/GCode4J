package org.gorb.gcode;

import static org.junit.Assert.*;

import org.junit.Test;


public class GCodeMachineSetterTest
{
	GCodeMachineSetter setter = new GCodeMachineSetter();
	
	@Test
	public void testSimulator() throws Exception {
		Sender sender = setter.getSender("simulator");
		assertTrue(sender instanceof SenderSimulator);
	}
	@Test
	public void testComPort() throws Exception {
		Sender sender = setter.getSender("COM11");
		assertTrue(sender instanceof Sender);
		assertFalse(sender instanceof SenderSimulator);
	}
	
	@Test
	public void testBuildMachine() throws Exception {
		GCodeMachine machine = setter.buildMachine();
		Sender sender = machine.getSender();
		assertTrue(sender instanceof SenderSimulator);
	}

	@Test
	public void testSetSerialPortSimulator() throws Exception {
		GCodeMachine machine = setter.buildMachine();
		setter.setSerialPort(machine, "simulator");
		Sender sender = machine.getSender();
		assertTrue(sender instanceof SenderSimulator);
	}
	@Test
	public void testSetSerialPortReal() throws Exception {
		GCodeMachine machine = setter.buildMachine();
		setter.setSerialPort(machine, "COM11");
		Sender sender = machine.getSender();
		assertFalse(sender instanceof SenderSimulator);
	}
	
	@Test
	public void testGetSerialPortNames() throws Exception {
		String[] names = setter.getSerialPortNames();
		
		assertEquals(1, names.length);
		assertEquals("COM1", names[0]);
	}
}
