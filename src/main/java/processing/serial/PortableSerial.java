package processing.serial;

import processing.core.PApplet;

public class PortableSerial extends Serial
{
	@SuppressWarnings("serial")
	static class PRApplet extends PApplet
	{
		private final SerialListener	listener;
		public PRApplet(SerialListener listener) {
			this.listener = listener;
			
		}
		@Override
		public void registerDispose(Object arg0) {
		}
		public void serialEvent(Serial s) {
			listener.serialEvent(s);
		}
	}
	public PortableSerial(SerialListener listener, String portName, int baud) {
		super(new PRApplet(listener), portName, baud);
	}
}
