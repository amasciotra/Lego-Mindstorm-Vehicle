package lab5EV3Shooter;

import lejos.hardware.ev3.LocalEV3;

import lejos.hardware.lcd.TextLCD;
//Ready for Open Source
/**
 * The Shooter Display class displays relevant information regarding the robot's position provided 
 * by the odometer, as well as the target selected by the user. Currently not in use.
 * 
 * Friday February 10, 2017
 * 12:50pm
 * 
 * @author thomaschristinck
 * @author alexmasciotra
 */
public class ShooterDisplay extends Thread {
	private static final long DISPLAY_PERIOD = 500;
	private Odometer odometer;
	private Shooter shooter;
	private TextLCD LCD = LocalEV3.get().getTextLCD();

	// constructor
	public ShooterDisplay(Odometer odometer, Shooter shooter) {
		this.odometer = odometer;
		this.shooter = shooter;
	}

	// run method (required for Thread)
	public void run() { 
		long displayStart, displayEnd;

		// Clear the display once
		LCD.clear();

		while (true) {
			displayStart = System.currentTimeMillis();

			// Clear the lines for displaying odometry information
			LCD.drawString("Theta:          ", 0, 0);
			LCD.drawString("Target:         ", 0, 1);
		

			// Get the odometry information
			LCD.drawString(formattedDoubleToString(odometer.getTheta(), 2), 7, 0);
			//Get target information
			LCD.drawString(Integer.toString(shooter.getTargetNumber()), 7, 1);
		
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
