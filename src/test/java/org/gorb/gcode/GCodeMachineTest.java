package org.gorb.gcode;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class GCodeMachineTest 
{
	GCodeMachine machine = new GCodeMachine();
	GCodeMachineListener listener = createMock(GCodeMachineListener.class);
	FakeSender fakeSender = new FakeSender();
	{
		machine.setListener(listener);
		machine.setSender(fakeSender);
	}

	private void waitForMachine() throws InterruptedException {
		synchronized(machine) { machine.wait(10); }
	} 


	@Test
	public void testOpenFile() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		replay(listener);
		
		assertFalse(machine.isFileOpen());
		machine.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		assertTrue(machine.isFileOpen());
		
		verify(listener);
	}

	@Test
	public void testReloadFile() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		replay(listener);
		
		machine.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		machine.reloadFile();
		assertTrue(machine.isFileOpen());
		
		verify(listener);
	}
	
	@Test
	public void testPlayNoFileLoaded() throws Exception {
		replay(listener);

		try {
			machine.play();
			fail("Should have thrown");
		} catch (IllegalStateException e) {
			assertEquals("No file is loaded, can't start", e.getMessage());
		}
		
		verify(listener);
	}
	@Test
	public void testPlayAndFinish() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		listener.busy(true);
		listener.sentLine("G20");
		listener.receivedLine("ok");
		listener.busy(false);
		listener.busy(true);
		listener.sentLine("G91");
		listener.receivedLine("ok");
		listener.busy(false);
		listener.finishedPlaying("testLines.txt");
		replay(listener);
		
		machine.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		machine.play();
		
		waitForMachine();
		assertFalse(machine.isBusy());
		assertFalse(machine.isPlaying());
		verify(listener);
	}
	@Test
	public void testPlayAndAbort() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		listener.busy(true);
		listener.sentLine("G20");
		listener.receivedLine("ok");
		listener.busy(false);
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				machine.abort();
				return null;
			}
		});
		listener.abortedPlaying("testLines.txt");
		replay(listener);
		
		machine.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		machine.play();
		
		waitForMachine();
		assertFalse(machine.isPlaying());
		verify(listener);
	} 

	int countOfLogCalls = 0;
	
	@Test
	public void testPlayAndAbortThenPlayAndFinish() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		listener.busy(true);
		listener.sentLine("G20");
		listener.receivedLine("ok");
		listener.busy(false);
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				if (countOfLogCalls++ == 0)
					machine.abort();
				return null;
			}
		});
		listener.abortedPlaying("testLines.txt");
		listener.startedPlaying("testLines.txt");
		listener.busy(true);
		listener.sentLine("G20");
		listener.receivedLine("ok");
		listener.busy(false);
		listener.busy(true);
		listener.sentLine("G91");
		listener.receivedLine("ok");
		listener.busy(false);
		listener.finishedPlaying("testLines.txt");
		replay(listener);
		
		machine.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		machine.play();
		
		waitForMachine();
		assertFalse(machine.isPlaying());
		
		fakeSender.reset();
		machine.play();
		waitForMachine();
		assertFalse(machine.isPlaying());

		verify(listener);
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
	public void testStartThenPauseThenResume() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		listener.busy(true);
		listener.sentLine("G20");
		listener.receivedLine("ok");
		listener.busy(false);
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				if (countOfLogCalls++ == 0)
					machine.pause();
				return null;
			}
		});
		listener.pausedPlaying("testLines.txt");
		listener.resumedPlaying("testLines.txt");
		listener.busy(true);
		listener.sentLine("G91");
		listener.receivedLine("ok");
		listener.busy(false);
		listener.finishedPlaying("testLines.txt");
		replay(listener);
		
		machine.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		machine.play();
		
		waitForMachine();
		assertTrue(machine.isPaused());
		assertTrue(machine.isPlaying());
		
		machine.resume();
		assertFalse(machine.isPaused());
		assertTrue(machine.isPlaying());
		
		waitForMachine();
		assertFalse(machine.isPlaying());
		assertFalse(machine.isPaused());

		verify(listener);
		
	}
	@Test
	public void testStartThenPauseThenAbort() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		listener.busy(true);
		listener.sentLine("G20");
		listener.receivedLine("ok");
		listener.busy(false);
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				if (countOfLogCalls++ == 0)
					machine.pause();
				return null;
			}
		});
		listener.pausedPlaying("testLines.txt");
		listener.abortedPlaying("testLines.txt");
		replay(listener);
		
		machine.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		machine.play();
		
		waitForMachine();
		assertTrue(machine.isPaused());
		assertTrue(machine.isPlaying());
		
		machine.abort();
		assertFalse(machine.isPaused());
		assertFalse(machine.isPlaying());
		
		waitForMachine();
		assertFalse(machine.isPlaying());
		assertFalse(machine.isPaused());

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

		sender.setListener(machine);	// TODO shouldn't MachineSetter do this?
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
		
		sender.setListener(machine);
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
