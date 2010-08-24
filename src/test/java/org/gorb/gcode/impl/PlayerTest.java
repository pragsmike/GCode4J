package org.gorb.gcode.impl;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.gorb.gcode.PlayerListener;
import org.gorb.gcode.impl.GCodeMachine;
import org.gorb.gcode.impl.Player;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class PlayerTest 
{
	GCodeMachine			machine		= createMock(GCodeMachine.class);
	Player					player		= new Player(machine);
	PlayerListener			listener	= createMock(PlayerListener.class);
	{
		player.setListener((listener));
	}


	@Test
	public void testOpenFile() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		replay(listener, machine);
		
		assertFalse(player.isFileOpen());
		player.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		assertTrue(player.isFileOpen());
		
		verify(listener, machine);
	}

	@Test
	public void testReloadFile() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		replay(listener, machine);
		
		player.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		player.reloadFile();
		assertTrue(player.isFileOpen());
		
		verify(listener, machine);
	}
	
	@Test
	public void testPlayNoFileLoaded() throws Exception {
		replay(listener, machine);

		try {
			player.play();
			fail("Should have thrown");
		} catch (IllegalStateException e) {
			assertEquals("No file is loaded, can't start", e.getMessage());
		}
		
		verify(listener, machine);
	}
	@Test
	public void testPlayAndFinish() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		machine.send("G20");
		machine.send("G91");
		listener.finishedPlaying("testLines.txt");
		replay(listener, machine);
		
		player.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		player.play();
		
		player.nextLine();
		player.nextLine();
		assertFalse(player.isPlaying());
		verify(listener, machine);
	}
	@Test
	public void testPlayAndAbort() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		machine.send("G20");
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				player.abort();
				return null;
			}
		});
		listener.abortedPlaying("testLines.txt");
		replay(listener, machine);
		
		player.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		player.play();
		
		player.nextLine();
		player.nextLine();
		assertFalse(player.isPlaying());
		verify(listener, machine);
	} 

	int countOfLogCalls = 0;
	
	@Test
	public void testPlayAndAbortThenPlayAndFinish() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		machine.send("G20");
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				if (countOfLogCalls++ == 0)
					player.abort();
				return null;
			}
		});
		listener.abortedPlaying("testLines.txt");
		listener.startedPlaying("testLines.txt");
		machine.send("G20");
		machine.send("G91");
		listener.finishedPlaying("testLines.txt");
		replay(listener, machine);
		
		player.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		player.play();
		
		player.nextLine();
		player.nextLine();
		assertFalse(player.isPlaying());
		
		player.play();
		player.nextLine();
		player.nextLine();
		assertFalse(player.isPlaying());

		verify(listener, machine);
	}
	
	@Test
	public void testStartThenPauseThenResume() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		machine.send("G20");
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				if (countOfLogCalls++ == 0)
					player.pause();
				return null;
			}
		});
		listener.pausedPlaying("testLines.txt");
		listener.resumedPlaying("testLines.txt");
		machine.send("G91");
		listener.finishedPlaying("testLines.txt");
		replay(listener, machine);
		
		player.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		player.play();
		
		assertTrue(player.isPaused());
		assertTrue(player.isPlaying());
		player.nextLine();
		player.nextLine();
		
		player.resume();
		assertFalse(player.isPaused());
		assertTrue(player.isPlaying());
		
		player.nextLine();
		player.nextLine();
		assertFalse(player.isPlaying());
		assertFalse(player.isPaused());

		verify(listener, machine);
		
	}
	@Test
	public void testStartThenPauseThenAbort() throws Exception {
		listener.fileLoaded("testLines.txt", "G20\nG91\n");
		listener.startedPlaying("testLines.txt");
		machine.send("G20");
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
			public Object answer() throws Throwable {
				if (countOfLogCalls++ == 0)
					player.pause();
				return null;
			}
		});
		listener.pausedPlaying("testLines.txt");
		listener.abortedPlaying("testLines.txt");
		replay(listener, machine);
		
		player.openFile(new ClassPathResource("org/gorb/gcode/testLines.txt").getFile());
		player.play();

		
		player.nextLine();
		player.nextLine();
		assertTrue(player.isPaused());
		assertTrue(player.isPlaying());
		
		player.abort();
		assertFalse(player.isPaused());
		assertFalse(player.isPlaying());
		
		player.nextLine();
		player.nextLine();
		assertFalse(player.isPlaying());
		assertFalse(player.isPaused());

		verify(listener, machine);
		
	}


}
