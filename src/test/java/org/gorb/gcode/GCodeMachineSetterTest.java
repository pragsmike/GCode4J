package org.gorb.gcode;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.gorb.gcode.impl.GCodeMachine;
import org.gorb.gcode.impl.Player;
import org.gorb.gcode.impl.Sender;
import org.gorb.gcode.sim.SenderSimulator;
import org.junit.Ignore;
import org.junit.Test;


public class GCodeMachineSetterTest
{
	GCodeMachineSetter setter = new GCodeMachineSetter();
	
	GCodeMachineListener listener = createMock(GCodeMachineListener.class);
	PlayerListener playerListener = createMock(PlayerListener.class);
	
	@Test
	public void testBuildMachine() throws Exception {
		replay(listener);
		GCodeMachine machine = setter.buildMachine(listener);
		Sender sender = machine.getSender();
		assertTrue(sender instanceof SenderSimulator);
		verify(listener);
	}
	@Test
	public void testBuildPlayer() throws Exception {
		GCodeMachine machine = createMock(GCodeMachine.class);
		machine.addListener((GCodeMachineListener) anyObject());
		replay(machine, listener);
		Player player = setter.buildPlayer(machine, playerListener);
		assertFalse(player.isFileOpen());
		verify(machine, listener);
	}


	@Test
	public void testGetSenderSimulator() throws Exception {
		Sender sender = setter.getSender("simulator");
		assertTrue(sender instanceof SenderSimulator);
	}
	
	@Test
	public void testSetSerialPortSimulator() throws Exception {
		GCodeMachine machine = setter.buildMachine(null);
		setter.setSerialPort(machine, "simulator");
		Sender sender = machine.getSender();
		assertTrue(sender instanceof SenderSimulator);
	}

	// These next three tests access the machine's serial ports
	// and might not work on all machines.
	@Test
	public void testSetSerialPortReal() throws Exception {
		GCodeMachine machine = setter.buildMachine(null);
		setter.setSerialPort(machine, "COM11");
		Sender sender = machine.getSender();
		assertFalse(sender instanceof SenderSimulator);
	}
	
	@Test
	public void testSenderComPort() throws Exception {
		Sender sender = setter.getSender("COM11");
		assertTrue(sender instanceof Sender);
		assertFalse(sender instanceof SenderSimulator);
	}
	@Test
	@Ignore
	public void testGetSerialPortNames() throws Exception {
		String[] names = setter.getSerialPortNames();
		
		assertTrue(names.length > 0);
		assertTrue(names[0].startsWith("COM"));
	}
}
