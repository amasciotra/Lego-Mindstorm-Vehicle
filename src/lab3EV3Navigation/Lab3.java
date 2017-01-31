package lab3EV3Navigation;

/**
 * Main class for lab 3. Primary function is allowing the user to select which
 * part of the demo to run (part 1 or part 2). The demo field in the Navigation class
 * is then correspondingly set to 1 or 2.
 * 
 * 
 * @author thomaschristinck
 * @author alexmasciotra
 */

import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.*;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.*;
import lejos.robotics.*;

public class Lab3 {
	//Final variables for bang-bang control`
	private static final int BAND_CENTER = 20;
	private static final int BAND_WIDTH= 2;	
	private static final int MOTOR_LOW = 75;
	private static final int MOTOR_HIGH = 200;
	public static final double TRACK = 18.54;//18.54
	public static final double WHEEL_RADIUS = 2.07;//2.07

	public static void main(String[] args) {
		int buttonChoice;
		

		//Initialize motors
		final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		final EV3LargeRegulatedMotor usMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
		//Initialize odometer
		Odometer odometer = new Odometer(leftMotor, rightMotor, WHEEL_RADIUS, TRACK);
		//initializes sensor and prepares it to read the distance
		@SuppressWarnings("resource")					
		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));		
		SampleProvider usDistance = usSensor.getMode("Distance");
		//Initialize bangbang controller
		BangBangController bangbang = new BangBangController(leftMotor, rightMotor, BAND_CENTER, BAND_WIDTH, MOTOR_LOW, MOTOR_HIGH);
		float[] usData = new float[usDistance.sampleSize()];
		//UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData, bangbang);
		UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData, bangbang);
		Navigation nav = new Navigation(leftMotor, rightMotor, usMotor, odometer, usPoller, bangbang, WHEEL_RADIUS, TRACK);
		
		final TextLCD t = LocalEV3.get().getTextLCD();
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, t);
		
		do {
			//Clear display
			t.clear();
			
			//Ask user which part of lab to execute; part 1 or part 2
			LCD.drawString("Part 1 | Part 2  ", 0, 4);
			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
		odometer.start();
		odometryDisplay.start();
			
		if (buttonChoice == Button.ID_LEFT) {
			//Start part 1 of demo
			nav.setDemo(1);
			nav.start();
			
		} else {
			//Start part 2 of demo
			nav.setDemo(2);
			usPoller.start();
			nav.start();
		}
			
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
	
	

