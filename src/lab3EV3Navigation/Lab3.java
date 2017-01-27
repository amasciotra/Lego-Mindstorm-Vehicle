package lab3EV3Navigation;

/*
 * TO DO:
 *
 * 1. Main thing is the angle situation in Navigation. Making the angles make sense and work out
 * is going to be the hardest part, especially because our odometer angles don't really make sense
 * 2. The turnTo() method could be found in other people's code on GitHub, the only problem is that 
 * our theta won't necessarily be the same as theirs. I think ours is like this from the starting point
 * 
 * 				90*
 * 				|
 *				| 
 * 		 0*---------- 360*
 * 				|
 * 				|
 * 				270*
 * 
 * Test
 */
import lab1EV3WallFollower.BangBangController;
import lab1EV3WallFollower.UltrasonicPoller;
import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.*;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.*;
import lejos.robotics.*;

public class Lab3 {
	//Final variables for bang-bang control
	private static final int BAND_CENTER = 30;
	private static final int BAND_WIDTH= 30;	
	private static final int MOTOR_LOW = 100;
	private static final int MOTOR_HIGH = 300;
	
	public static void main(String[] args) {
		int buttonChoice;

		//initializes both motors
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D")); 
		
		//initializes sensor and prepares it to read the distance
		@SuppressWarnings("resource")					
		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S2"));		
		SampleProvider usDistance = usSensor.getMode("Distance");
		float[] usData = new float[usDistance.sampleSize()];
		
		//Initialize bang-bang controller
		BangBangController bangbang = new BangBangController(leftMotor, rightMotor, 
				BAND_CENTER, BAND_WIDTH, MOTOR_LOW, MOTOR_HIGH);
		
		final TextLCD t = LocalEV3.get().getTextLCD();
		Navigation nav = null;
		do {
			//Clear display
			t.clear();
			
			//Ask user which part of lab to execute; part 1 or part 2
			LCD.drawString("Part 2 | Part 1  ", 0, 4);
			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			nav = new Navigation(leftMotor, rightMotor, bangbang, true);
			UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData, bangbang);;
			nav.start();
			usPoller.start();
			
		} else {
			nav = new Navigation(leftMotor, rightMotor, bangbang, false);
			nav.start();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
