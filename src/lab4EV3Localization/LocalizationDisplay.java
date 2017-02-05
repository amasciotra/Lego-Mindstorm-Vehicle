package lab4EV3Localization;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

public class LocalizationDisplay extends Thread {
	private static final long DISPLAY_PERIOD = 250;
	private Odometer odo;
	private TextLCD LCD = LocalEV3.get().getTextLCD();
	
	public LocalizationDisplay(Odometer odo) {
		this.odo = odo;
	}
	
	public void run() { 
		long displayStart, displayEnd;
		double[] position = new double[3];

		// clear the display once
		LCD.clear();

		while (true) {
			displayStart = System.currentTimeMillis();

			// Clear the lines for displaying odometry information
			LCD.drawString("X:              ", 0, 0);
			LCD.drawString("Y:              ", 0, 1);
			LCD.drawString("T:              ", 0, 2);

			// Get the odometry information
			odo.getPosition(position);

			// Display odometry information
			for (int i = 0; i < 3; i++) {
				LCD.drawString(formattedDoubleToString(position[i], 2), 3, i);
			}

			// Throttle the OdometryDisplay
			displayEnd = System.currentTimeMillis();
			if (displayEnd - displayStart < DISPLAY_PERIOD) {
				try {
					Thread.sleep(DISPLAY_PERIOD - (displayEnd - displayStart));
				} catch (InterruptedException e) {
					// There is nothing to be done here because it is not
					// expected that OdometryDisplay will be interrupted
					// by another thread
				}
			}
		}
	}
	
	private static String formattedDoubleToString(double x, int places) {
		String result = "";
		String stack = "";
		long t;
		
		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";
		
		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long)x;
			if (t < 0)
				t = -t;
			
			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}
			
			result += stack;
		}
		
		// Put the decimal, if needed
		if (places > 0) {
			result += ".";
		
			// Put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long)x);
			}
		}
		
		return result;
	}

}