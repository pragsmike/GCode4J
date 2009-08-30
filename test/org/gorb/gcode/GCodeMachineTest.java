package org.gorb.gcode;

import static org.junit.Assert.*;
import static org.easymock.classextension.EasyMock.*;

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

	@Test
	public void testOpenFile() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		replay(listener);
		
		machine.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		
		verify(listener);
	}
	
	@Test
	public void testStartNoFileLoaded() throws Exception {
		replay(listener);

		try {
			machine.start();
			fail("Should have thrown");
		} catch (IllegalStateException e) {
			assertEquals("No file is loaded, can't start", e.getMessage());
		}
		
		verify(listener);
	}
	@Test
	public void testStartAndFinish() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		listener.sentLine("G20");
		listener.sentLine("G91");
		listener.finishedPlaying("testLines.txt");
		replay(listener);
		
		machine.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		machine.start();
		
		synchronized(machine) { machine.wait(10); }
		verify(listener);
	} 

	@Test
	public void testStartAndAbort() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		listener.sentLine("G20");
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				machine.abort();
				return null;
			}
		});
		listener.abortedPlaying("testLines.txt");
		replay(listener);
		
		machine.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		machine.start();
		
		synchronized(machine) { machine.wait(10); }
		verify(listener);
	} 

	int countOfLogCalls = 0;
	@Test
	public void testStartAndAbortThenStartAndFinish() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		listener.sentLine("G20");
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				if (countOfLogCalls++ == 0)
					machine.abort();
				return null;
			}
		});
		listener.abortedPlaying("testLines.txt");
		listener.startedPlaying("testLines.txt");
		listener.sentLine("G20");
		listener.sentLine("G91");
		listener.finishedPlaying("testLines.txt");
		replay(listener);
		
		machine.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		machine.start();
		
		synchronized(machine) { machine.wait(10); }
		
		fakeSender.reset();
		machine.start();
		synchronized(machine) { machine.wait(10); }

		verify(listener);
	} 
}
