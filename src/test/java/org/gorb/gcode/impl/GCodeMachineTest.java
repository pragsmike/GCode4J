package org.gorb.gcode.impl;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.gorb.gcode.GCodeMachineListener;
import org.gorb.gcode.impl.GCodeMachine;
import org.junit.Test;

public class GCodeMachineTest 
{
	GCodeMachine machine = new GCodeMachine();
	GCodeMachineListener listener = createMock(GCodeMachineListener.class);
	FakeSender fakeSender = new FakeSender();
	{
		machine.addListener(listener);
		machine.setSender(fakeSender);
		fakeSender.setListener(machine);
	}

	private void waitForMachine() throws InterruptedException {
		synchronized(machine) { machine.wait(10); }
	} 


	@Test
	public void testExecImmediate() throws Exception {
		listener.busy(true);
		listener.sentLine("G20");
		listener.receivedLine("ok");
		listener.busy(false);
		replay(listener);
		
		machine.execImmediate("G20");
		
		waitForMachine();
		verify(listener);
	}
	

	@Test
	public void testJogInAbsoluteModeSendsG91andG90() throws Exception {
		checkJog(true);
	}
	@Test
	public void testJogInRelativeModeSendsNoG91orG90() throws Exception {
		checkJog(false);
	}


	private void checkJog(boolean useAbsoluteMode) {
		Jogger jogger = createMock(Jogger.class);
		Sender sender = createMock(Sender.class);

		if (useAbsoluteMode) {
			sender.send("G91\n");
		}
		expect(jogger.jog("0.001", "n")).andReturn("a");
		sender.send("a\n");
		if (useAbsoluteMode) {
			sender.send("G90\n");
		}
		replay(jogger, sender);

		machine.positionAbsolute = useAbsoluteMode;
		machine.setJogger(jogger);
		machine.setSender(sender);
		machine.jog("n");
		verify(jogger, sender);
	}
	
	@Test
	public void testPositionModeInitiallyAbsolute() throws Exception {
		assertTrue(machine.isPositionAbsolute());
	}
	
	@Test
	public void testTracksAbsPositionMode() throws Exception {
		Sender sender = createMock(Sender.class);
		
		sender.send("G90\n");
		sender.send("G91\n");
		sender.send("G90\n");
		replay(sender);
		machine.setSender(sender);
		
		machine.send("G90");
		assertTrue(machine.isPositionAbsolute());
		
		machine.send("G91");
		assertFalse(machine.isPositionAbsolute());
		
		machine.send("G90");
		assertTrue(machine.isPositionAbsolute());
		
		verify(sender);
	}
	

}
