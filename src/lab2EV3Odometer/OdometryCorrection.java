/* 
 * OdometryCorrection.java
 */
package lab2EV3Odometer;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;


public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	//Declare odometer, initialize color sensor
	private static final Port csPort = LocalEV3.get().getPort("S1");	
	private EV3ColorSensor colorSensor = new EV3ColorSensor(csPort);
	private static SampleProvider sampleProvider;
	private Odometer odometer;
	
	//Boolean to determine if line is crossed
	private static boolean crossed;

	// Spacing of the tiles in centimeters
	private static final double TILE_LENGTH = 30.48;
 	
	// Max light value reading for grid lines
	private static final double LINE_LIGHT = 0.18;
	// Constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
	}
	
	private static float[] getSample(){
		//Set up array to collect samples
		int sampleSize = 1;
		int offset = 0;
		float[] sample = new float[sampleSize];
		sampleProvider.fetchSample(sample, offset);
		return sample;
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;
		
		double xtempDistance = 0;
		double ytempDistance = 0;
		int count = 0;
		while (true) {
			correctionStart = System.currentTimeMillis();
			// Initialize boolean to not crossed
			crossed = false;
			// Set sensor light
			colorSensor.setFloodlight(true);
			//Operate sensor in reflection mode
			sampleProvider = colorSensor.getRedMode();
			// put your correction code here
			//Read sensor
			//int lightValue = lightSensor.getNormalizedLightValue();
			//Check if we are reading a line
			if(getSample()[0] < LINE_LIGHT && !crossed){
				//Play sound
				Sound.playNote(Sound.FLUTE, 400, 200);
				//Get theta
				double theta = odometer.getTheta();
				//Increment counter
				count++;
				//Ignore first line counted after each turn
				if(count == 1 || count == 4 || count == 7 || count == 10 || count > 12){
					ytempDistance = odometer.getY();
					xtempDistance = odometer.getX();
				}
				//Moving up - increment y by tile length for each tile crossed
				else if(count > 1 && count <= 3){
					ytempDistance += TILE_LENGTH;
					odometer.setY(ytempDistance);
				}
				else if(count > 7 && count <=9){
				//Moving down - decrement y by tile length for each tile crossed
					ytempDistance -= TILE_LENGTH;
					odometer.setY(ytempDistance);
				}
				//Moving right - increment x by tile length for each tile crossed
				else if(count > 4 && count <= 7){
					xtempDistance += TILE_LENGTH;
					odometer.setX(xtempDistance);
				}
				//Moving left - decrement x by tile length for each tile crossed
				else if(count > 10){
					xtempDistance -= TILE_LENGTH;
					odometer.setX(xtempDistance);
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
