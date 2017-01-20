/* 
 * OdometryCorrection.java
 */
package lab2EV3Odometer;

//import lejos.nxt.*;

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	//Declare odometer, initialize color sensor
	//private ColorSensor colorSensor = new ColorSensor(SensorPort(S1));
	private Odometer odometer;
	//Useful math constants
	private static final double TWO_PI = Math.PI * 2;
	private static final double ONE_QUARTER_PI = Math.PI / 4;
	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();

			// put your correction code here
			//Read sensor
			//int lightValue = lightSensor.getNormalizedLightValue();
			//Check if we are reading a line
			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
	
	private double wrapAngle(double rads) {
		return rads % TWO_PI;
	}
}