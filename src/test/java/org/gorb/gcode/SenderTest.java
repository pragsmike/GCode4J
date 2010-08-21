package org.gorb.gcode;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;

import org.junit.Test;

import processing.serial.Serial;

public class SenderTest
{
	Serial serial = createMock(Serial.class);
	SenderListener senderListener = createMock(SenderListener.class);
	
	Sender sender = new Sender();
	{
		sender.setOutSerial(serial);
		sender.setListener(senderListener);
	}
	
	class SerialFaker {
		private Serial	serial;
		public SerialFaker(Serial mockSerial) {
			this.serial = mockSerial;
			
		}
		int count;
		void programMock(String s) {
			for (byte c : s.getBytes()) {
				expect(serial.read()).andReturn((int)c);
				count++;
			}
		}
		void send(Sender sender) {
			while(count-- > 0) {
				sender.serialEvent(serial);
			}
		}
	}
	SerialFaker serialTester = new SerialFaker(serial);
	
	@Test
	public void testSendOneOk() throws Exception {
		serialTester.programMock("ok\n");
		senderListener.ok();
		replay(serial, senderListener);
		serialTester.send(sender);
		verify(serial, senderListener);
	}
	@Test
	public void testSendTwoOk() throws Exception {
		serialTester.programMock("ok\nok\n");
		senderListener.ok();
		senderListener.ok();
		replay(serial, senderListener);
		serialTester.send(sender);
		verify(serial, senderListener);
	}
	@Test
	public void testSendTwoOkWithBlankLines() throws Exception {
		serialTester.programMock("ok\n\nok\n");
		senderListener.ok();
		senderListener.ok();
		replay(serial, senderListener);
		serialTester.send(sender);
		verify(serial, senderListener);
	}
	@Test
	public void testSendTwoOkWithStatusReports() throws Exception {
		serialTester.programMock("ok\n\nSteppers enabled\nok\n");
		senderListener.ok();
		senderListener.status("Steppers enabled");
		senderListener.ok();
		replay(serial, senderListener);
		serialTester.send(sender);
		verify(serial, senderListener);
	}
	
	@Test
	public void testSend() throws Exception {
		serial.write("G20\n");
		replay(serial);
		
		sender.send("G20\n");
		verify(serial);
	}
}
