package lab4EV3Localization;
import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;
import lejos.hardware.lcd.*;
//Ready for Open Source
/**
 * Main class for lab 4, handles user-initiated events  assuming the robot starts in the lower left quadrant
 * After localization, the robot should be at (0, 0) facing 0 degrees
 * 
 * Monday February 6, 2017
 * 2:41pm
 * 
 * @author thomaschristinck
 * @author alexmasciotra
 */


public class Lab4 {
	// Static Resources:
	// Left motor, Right motors connected to outputs A and D
	// Ultrasonic sensor port, color sensor ports connected to input S1 and S2
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S1");		
	
	public static void main(String[] args) {		
		//Setup ultrasonic sensor. Suppress warnings because we don't bother closing the resource
		@SuppressWarnings("resource")							    
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			
		float[] usData = new float[usValue.sampleSize()];				
		int option = 0;
		
		//Initialize odometer and navigator
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
		Navigation nav = new Navigation(odo);
		
		//Initialize light and ultrasonic localizers
		USLocalizer us = new USLocalizer(odo, usValue, usData, USLocalizer.LocalizationType.RISING_EDGE);
		LightLocalizer lsl = new LightLocalizer(odo, nav);
		
		//Display
		LocalizationDisplay LocalizationDisplay = new LocalizationDisplay(odo);

		do {
			//Clear display
			LCD.clear();
			
			//Ask user which part of lab to execute; part 1 or part 2
			LCD.drawString("Part 1 | Part 2  ", 0, 4);
			option = Button.waitForAnyPress();
		} while (option != Button.ID_LEFT && option != Button.ID_RIGHT);		
	
		
		if (option == Button.ID_LEFT) {	
			LocalizationDisplay.start();	
			us.doLocalization();
			//Button.waitForAnyPress();
			//lsl.doLocalization();
			
		}
		else if (option == Button.ID_RIGHT){
			LocalizationDisplay.start();	
			lsl.doLocalization();
		}
	while (Button.waitForAnyPress() != Button.ID_ESCAPE);
	System.exit(0);
	}
}


