package org.gorb.gcode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.easymock.classextension.EasyMock.*;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;


public class GCodeMachineSetterTest
{
	GCodeMachineSetter setter = new GCodeMachineSetter();
	
	GCodeMachineListener listener = createMock(GCodeMachineListener.class);
	
	@Test
	public void testBuildMachine() throws Exception {
		replay(listener);
		GCodeMachine machine = setter.buildMachine(listener);
		Sender sender = machine.getSender();
		assertTrue(sender instanceof SenderSimulator);
		assertFalse(machine.isFileOpen());
		verify(listener);
	}

	@Test
	public void testStartOpensInitialFile() throws Exception {
		listener.fileLoaded(eq("gcode.txt"), (String) anyObject());
		replay(listener);
		setter.initialFile = new ClassPathResource("gcode.txt").getFile();
		GCodeMachine machine = setter.buildMachine(listener);
		setter.startMachine(machine);
		assertTrue(machine.isFileOpen());
		verify(listener);
	}
	@Test
	public void testStartDoesNothingOnSubsequentCalls() throws Exception {
		listener.fileLoaded(eq("gcode.txt"), (String) anyObject());
		replay(listener);
		setter.initialFile = new ClassPathResource("gcode.txt").getFile();
		GCodeMachine machine = setter.buildMachine(listener);
		setter.startMachine(machine);
		setter.startMachine(machine);
		verify(listener);
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
