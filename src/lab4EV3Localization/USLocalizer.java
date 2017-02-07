package lab4EV3Localization;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * US Localizer class uses the US sensor to turn robot in the theta = 0 direction, assuming the robot starts in the lower left quadrant
 * After localization, the robot should be facing 0 degrees
 * 
 * Monday February 6, 2017
 * 2:41pm
 * 
 * @author thomaschristinck
 * @author alexmasciotra
 */

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int ROTATION_SPEED = 100;
	public static int ACCEL = 300;
	public static int MAX_DIST = 50;
	public static int WALL_DIST = 30;
	public static int WALL_ERROR = 3;
	public static int FILTER_OUT = 5;
	//Play with turn error so that odometer is perfectly at 0 degrees
	public static int TURN_ERROR = 8;
	
	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int filterControl;	
	private float prevDistance;
	private double angleA, angleB, avgAngle, trueNorth;
		
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.filterControl = 0;
		EV3LargeRegulatedMotor leftMotor = odo.getLeftMotor();
		EV3LargeRegulatedMotor rightMotor = odo.getRightMotor();
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		filterControl = 0;
		prevDistance = 50;
	}
	
	public void doLocalization() {
		double angleA, angleB;
		
		//Sets motor speeds and accelerations
		leftMotor.setSpeed(ROTATION_SPEED); 
		rightMotor.setSpeed(ROTATION_SPEED);
		leftMotor.setAcceleration(ACCEL);
		rightMotor.setAcceleration(ACCEL);
		
		if (locType == LocalizationType.FALLING_EDGE) {
			// Robot rotates clockwise until it no longer sees wall
			while(getFilteredData() < WALL_DIST + WALL_ERROR) 
			{
				rotateClockwise();
			}
			// Robot continues rotating until it sees another wall
			while(getFilteredData() > WALL_DIST)
			{
				rotateClockwise();
			}
			//When wall is found, stop motors and latch the angle (angle A)
			angleA = odo.getTheta();
			stopMotors();
			
			// Robot will rotate counterclockwise until there is no wall in front of it
			while(getFilteredData() < WALL_DIST + WALL_ERROR)
			{
				rotateCClockwise();
			}
			// Robot will continue to rotate until it sees another wall
			while(getFilteredData() > WALL_DIST)
			{
				rotateCClockwise();
			}
			// Again, when wall is found, stop motors and latch the angle
			angleB = odo.getTheta();
			stopMotors();		
			
			// This deals with angleA being greater than 360 degrees
			if(angleA > angleB)
				angleA -= 360;
			
			// The average angle of the two is 45 degrees away from "trueNorth" which is the zero degrees the robot needs to turn to
			avgAngle = (angleA + angleB) / 2.0;
			trueNorth =  angleB - avgAngle + 45;			
			
			// Rotate to trueNorth and add TURN_ERROR which was calculated from the trials we ran
			rotate(trueNorth + TURN_ERROR);
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
		} else {
			// Rising edge - robot will rotate counterclockwise
			while(getFilteredData() > WALL_DIST - WALL_ERROR)
			{
				rotateCClockwise();
			}
			// Robot will continue to rotate counterclockwise until there is no wall in front of it
			while(getFilteredData() < WALL_DIST)
			{
				rotateCClockwise();
			}
			// Without the wall in front of it, stop motors and latch the angle (angle A)
			//angleA = odo.getTheta();
			angleA = odo.getTheta();
			stopMotors();			
			
			// Robot will now rotate clockwise 
			while(getFilteredData() > WALL_DIST - WALL_ERROR)
			{
				rotateClockwise();
			}
			// Robot will continue to rotate clockwise until there is no wall in front of it
			while(getFilteredData() < WALL_DIST)
			{
				rotateClockwise();
			}
			// The angle from odometer is stored, stop motors and latch the angle
			angleB = odo.getTheta();
			stopMotors();
			
			// This deals with angleA being greater than 360 degrees
			if(angleA > angleB)
				angleA -= 360;
			
			// The average angle is 45 degrees away from "trueNorth" which is the zero degrees the robot needs to turn to
			avgAngle = (angleA + angleB)/2.0;
			trueNorth = angleB - avgAngle + 45;
			
			// Rotate to trueNorth and add TURN_ERROR which was calculated from the trials we ran
			rotate(trueNorth - TURN_ERROR);
			
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
		}
	}
	
	
	public double[] getAngles()
	{
		double[] angles = new double[4];
		angles[0] = angleA;
		angles[1] = angleB;
		angles[2] = avgAngle;
		angles[3] = trueNorth;
		return angles;
	}
	private void rotateClockwise()
	{
		leftMotor.forward();
		rightMotor.backward();
	}
	
	private void rotateCClockwise()
	{
		leftMotor.backward();
		rightMotor.forward();
	}
	
	private void stopMotors()
	{
		leftMotor.stop(true);
		rightMotor.stop(false);
	}
	
	private void rotate(double theta)
	{
		leftMotor.rotate(convertAngle(odo.getRadius(), odo.getWidth(), theta), true);
		rightMotor.rotate(-convertAngle(odo.getRadius(), odo.getWidth(), theta), false);
	}
	
	private  int convertDistance(double radius, double distance)
	{ 															 
		return (int) ((180.0 * distance) / (Math.PI * radius)); 
	} 
	      
	private  int convertAngle(double radius, double width, double angle) 
	{ 
		return convertDistance(radius, Math.PI * width * angle / 360.0); 
	}
	
	// Method returns the value of the Ultrasonic Sensor. The largest distance we want is 50 cm so any distance 
	// greater than 50 is capped at 50 cm.
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = (int)(100.0 * usData[0]);
		float result = 0;
		if(distance > MAX_DIST && filterControl < FILTER_OUT)
		{
			filterControl++;
			result = prevDistance;
		}
		else if(distance > MAX_DIST)
		{
			result = MAX_DIST;
		}
		else
		{
			filterControl = 0;
			result = distance;
		}
		prevDistance = distance;
		return result;
	}

}