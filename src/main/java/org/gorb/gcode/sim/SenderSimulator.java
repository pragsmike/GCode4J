package org.gorb.gcode.sim;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.gorb.gcode.impl.Sender;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.timer.TimerTaskExecutor;

public class SenderSimulator extends Sender 
{
	private Random random = new Random(1);
	Timer timer = new Timer();
	TaskExecutor executor = new TimerTaskExecutor();
	
	class Responder extends TimerTask
	{
		private final String	command;

		public Responder(String command) {
			this.command = command;
		}

		@Override
		public void run() { 
			try {
				respond(command);
			} catch (Throwable t) {}
		};
	}
	
	@Override
	public void send(final String string) {
		System.out.print("R " + string);
		senderListener.status("Steppers enabled");
		Responder responder = new Responder(string);

		if (executor instanceof TimerTaskExecutor) {
			((TimerTaskExecutor) executor).setDelay(random.nextInt(2000));
			((TimerTaskExecutor) executor).setTimer(timer);
		}
		executor.execute(responder);
	}
	private void respond(String string) throws InterruptedException {
		senderListener.ok();
		Runnable r = new Runnable() {
			public void run() {
				senderListener.status("Steppers disabled");
			}
		};
		if (executor instanceof TimerTaskExecutor) {
			((TimerTaskExecutor) executor).setDelay(500);
		}
		executor.execute(r);
	}
	
	@Override
	public void close() {
		System.out.println("Sender simulator closed");
		try {timer.cancel();} catch (Exception e) {}
	}
}
