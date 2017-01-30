package lab3EV3Navigation;

/**
 * Main class for lab 3. Primary function is allowing the user to select which
 * part of the demo to run (part 1 or part 2). An instance of the Navigation class
 * that corresponds to the selection is then initialized.
 * 
 * 
 * @author thomaschristinck
 * @author alexmasciotra
 */

/*ok, i added a turnTo, and tried to fix some things, still have 2 errors im unable to fix.with the code we had,

 * the robot was not turning at all just driving in a straight line for path 1. for path 2 the bangbang didnt really work
 * i saw its sharp turn once but after that it doesn't work.
 * i tried to focus more on the path 1 part of the code. 
 * i feel the way you wrote the travel to in the array x1 y1 it was not being read properly.
 * 
 * you can see all the changes i did on github, feel free to remove anything and keep what you think we need
 * not the most productive 5 hours ive had in this lab.
 * 
 * the next time i can come in is monday as well, willing to spend the night.
 * 
 */

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
	public static final double TRACK = 18.54;
	public static final double WHEEL_RADIUS = 2.07;
	private static final TextLCD TextLCD = null;
	
	
	public static void main(String[] args) {
		int buttonChoice;

		//initializes both motors
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D")); 
		Odometer odometer = new Odometer(leftMotor, rightMotor);
		
		//initializes sensor and prepares it to read the distance
		@SuppressWarnings("resource")					
		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));		
		SampleProvider usDistance = usSensor.getMode("Distance");
		//Initialize bangbang controller
		BangBangController bangbang = new BangBangController(leftMotor, rightMotor, BAND_CENTER, BAND_WIDTH, MOTOR_LOW, MOTOR_HIGH);
		
		UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, bangbang);
		Navigation nav = new Navigation(odometer, usPoller, bangbang);
		
		final TextLCD t = LocalEV3.get().getTextLCD();
		OdometryDisplay display = new OdometryDisplay(odometer, TextLCD);

		do {
			//Clear display
			t.clear();
			
			//Ask user which part of lab to execute; part 1 or part 2
			LCD.drawString("Part 2 | Part 1  ", 0, 4);
			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			//UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, bangbang);;
			
			nav.setDemo(1);
			nav.start();
			usPoller.start();
			
		} else {
			
			nav.setDemo(2);
			nav.start();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
