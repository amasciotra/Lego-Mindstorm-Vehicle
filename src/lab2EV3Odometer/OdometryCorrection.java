/* 
 * OdometryCorrection.java
 */
package lab2EV3Odometer;

import java.awt.List;
import java.util.ArrayList;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	//Declare odometer, initialize color sensor
	private static final Port usPort = LocalEV3.get().getPort("S1");
	NXTLightSensor lightSensor = new NXTLightSensor(usPort);	
	//private ColorSensor colorSensor = new ColorSensor(SensorPort(S1));
	private Odometer odometer;
	// The distance of the sensor from the wheel axle
	private static final double SENSOR_OFFSET = 3.5;
	//Boolean to determine if line is crossed
	private static boolean crossed;
	// Spacing of the tiles in centimeters
	private static final double TILE_LENGTH = 30.48;
	private static final double HALF_TILE_LENGTH = TILE_LENGTH/2;
	//Useful math constants
	private static final double TWO_PI = Math.PI * 2;
	private static final double ONE_QUARTER_PI = Math.PI / 4;
	// Max light value reading for grid lines
	private static final int LINE_LIGHT = 500;
	// Constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();
			// Initialize boolean to not crossed
			crossed = false;
			// Set sensor light
			lightSensor.setFloodlight(true);
			float[] sample = new float[1];
			int offset = 0;
			lightSensor.fetchSample(sample, offset);
			float lightValue = sample[0];

			// put your correction code here
			//Read sensor
			//int lightValue = lightSensor.getNormalizedLightValue();
			//Check if we are reading a line
			if(lightValue < LINE_LIGHT && !crossed){
				//Send theta to 0<=theta<=2pi
				double theta = odometer.getTheta();
				//Check which line direction was crossed using theta
				if(theta >= ONE_QUARTER_PI && theta < 3 * ONE_QUARTER_PI || theta >= 5 * ONE_QUARTER_PI && theta < 7 * ONE_QUARTER_PI) {
					Sound.playNote(Sound.FLUTE, 400, 200);
					// Crossed horizontal line
					double yError = Math.sin(theta) * SENSOR_OFFSET;
					// Error y accounts for sensor distance to axel
					double y = odometer.getY() + yError;
					// Send y to closest line
					y = Math.round((y + HALF_TILE_LENGTH) / TILE_LENGTH) * TILE_LENGTH - HALF_TILE_LENGTH;
					// Correct y, removing the error
					odometer.setY(y - yError / 2);
				} else {
					Sound.playNote(Sound.FLUTE, 1000, 200);
					// Crossed vertical line
					double xError = Math.cos(theta) * SENSOR_OFFSET;
					// Error x to account for sensor to axel
					double x = odometer.getX() + xError;
					// Send x to closest line
					x = Math.round((x + HALF_TILE_LENGTH) / TILE_LENGTH) * TILE_LENGTH - HALF_TILE_LENGTH;
					// Correct x, Removing the offset
					odometer.setX(x - xError / 2);
				}
				// Set line as crossed until line is no longer being read
				crossed = true;
			} else {
				// Line is not reading a line (finished crossing) 
				crossed = false;
			}
			
			//This ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}		
		}
	}
}