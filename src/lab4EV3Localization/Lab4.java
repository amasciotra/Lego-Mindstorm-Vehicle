
import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;
import lejos.hardware.lcd.*;

public class Lab4 {

	
	//public static final double TRACK = 18.54;
	//public static final double WHEEL_RADIUS = 2.07;
	
	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// Ultrasonic sensor port connected to input S1
	// Color sensor port connected to input S2
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Port usPort = LocalEV3.get().getPort("S1");		
	private static final Port colorPort = LocalEV3.get().getPort("S2");		
	
	public static void main(String[] args) {		
		//Setup ultrasonic sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
		
		//Setup color sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("Red");			// colorValue provides samples from this instance
		float[] colorData = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned
				
		int option = 0;
		
		//Odometer odo = new Odometer(leftMotor, rightMotor, WHEEL_RADIUS, TRACK);
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
		USLocalizer us = new USLocalizer(odo, usValue, usData, USLocalizer.LocalizationType.FALLING_EDGE);
		//Navigation nav = new Navigation(leftMotor, rightMotor, odo, WHEEL_RADIUS, TRACK);
		Navigation nav = new Navigation(odo);
		LocalizationDisplay LocalizationDisplay = new LocalizationDisplay(odo);

		do {
			//Clear display
			LCD.clear();
			
			//Ask user which part of lab to execute; part 1 or part 2
			LCD.drawString("Part 1 | Part 2  ", 0, 4);
			option = Button.waitForAnyPress();
		} while (option != Button.ID_LEFT && option != Button.ID_RIGHT);
		LocalizationDisplay.start();													
		//option = Button.waitForAnyPress();
		
		
		switch(option) {
		case Button.ID_LEFT:								
			us.doLocalization();
			break;						
		}		
		
		option = Button.waitForAnyPress();
		
		switch(option)
		{
		case Button.ID_RIGHT:
			LightLocalizer lsl = new LightLocalizer(odo, colorValue, colorData, nav);
			lsl.doLocalization();
			break;
			}
		
		}
	}

