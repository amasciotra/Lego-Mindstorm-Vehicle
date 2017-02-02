package lab4EV3Localization;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static int ROTATION_SPEED = 100;
	public static int ACCEL = 300;
	public static int MAX_DIST = 50;
	public static int WALL_DIST = 30;
	public static int WALL_ERROR = 3;
	public static int FILTER_OUT = 3;
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
		EV3LargeRegulatedMotor[] motors = odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
		filterControl = 0;
		prevDistance = 50;
	}
	
	public void doLocalization() {
		double angleA, angleB;
		
		leftMotor.setSpeed(ROTATION_SPEED); //sets motor speeds and accelerations
		rightMotor.setSpeed(ROTATION_SPEED);
		leftMotor.setAcceleration(ACCEL);
		rightMotor.setAcceleration(ACCEL);
		
		if (locType == LocalizationType.FALLING_EDGE) {
			// robot will rotate clockwise until there is no wall in front of it
			while(getFilteredData() < WALL_DIST + WALL_ERROR) 
			{
				rotateClockwise();
			}
			// robot will continue to rotate clockwise when it sees no wall until it sees another wall in front of it
			while(getFilteredData() > WALL_DIST)
			{
				rotateClockwise();
			}
			// angle from odometer is stored and motors are stopped
			angleA = odo.getTheta();
			stopMotors();
			// robot will rotate counterclockwise until there is no wall in front of it
			while(getFilteredData() < WALL_DIST + WALL_ERROR)
			{
				rotateCClockwise();
			}
			// robot will continue to rotate counterclockwise until it sees another wall in front of it
			while(getFilteredData() > WALL_DIST)
			{
				rotateCClockwise();
			}
			// angle from odometer is stored and motors are stopped
			angleB = odo.getTheta();
			stopMotors();		
			
			// this deals with angleA being greater than 360 degrees
			if(angleA > angleB)
				angleA -= 360;
			
			// avgAngle is 45 degrees away from "trueNorth" which is the zero degrees the robot needs to turn to
			avgAngle = (angleA + angleB)/2.0;
			trueNorth =  angleB - avgAngle + 45;			
			
			// rotate to trueNorth and add TURN_ERROR which was calculated from the trials we ran
			rotate(trueNorth + TURN_ERROR);
			
			odo.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
		} else {
			// robot will rotate counterclockwise
			while(getFilteredData() > WALL_DIST - WALL_ERROR)
			{
				rotateCClockwise();
			}
			// robot will continue to rotate counterclockwise until there is no wall in front of it
			while(getFilteredData() < WALL_DIST)
			{
				rotateCClockwise();
			}
			// the angle from the odometer is stored and the motors are stopped
			angleA = odo.getTheta();
			stopMotors();			
			
			// robot will rotate clockwise 
			while(getFilteredData() > WALL_DIST - WALL_ERROR)
			{
				rotateClockwise();
			}
			// robot will continue to rotate clockwise until there is no wall in front of it
			while(getFilteredData() < WALL_DIST)
			{
				rotateClockwise();
			}
			// angle from odometer is stored
			angleB = odo.getTheta();
			stopMotors();
			
			// this deals with angleA being greater than 360 degrees
			if(angleA > angleB)
				angleA -= 360;
			
			// avgAngle is 45 degrees away from "trueNorth" which is the zero degrees the robot needs to turn to
			avgAngle = (angleA + angleB)/2.0;
			trueNorth = angleB - avgAngle + 45;
			
			// rotate to trueNorth and add TURN_ERROR which was calculated from the trials we ran
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
	
	// Method returns the value of the Ultrasonic Sensor. The largest distance we want is 50cm so any distance thats greater than 50 is capped at 50cm.
	// 
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