package lab4EV3Localization;


/*
 * File: Navigation.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * 
 * Movement control class (turnTo, travelTo, flt, localize)
 */
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation {
	final static int FAST = 200, SLOW = 100, ACCELERATION = 4000;
	final static double DEG_ERR = 3.0, CM_ERR = 1.0;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	public Navigation(Odometer odo) {
		this.odometer = odo;

		EV3LargeRegulatedMotor[] motors = this.odometer.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 */
	public void travelTo(double x, double y) {
		double minAng;
		while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
			this.turnTo(minAng, false);
			this.setSpeeds(FAST, FAST);
		}
		this.setSpeeds(0, 0);
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();

		while (Math.abs(error) > DEG_ERR) {

			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
	}
	
	/*
	 * Go foward a set distance in cm
	 */
	public void goForward(double distance) {
		this.travelTo(Math.cos(Math.toRadians(this.odometer.getAng())) * distance, Math.cos(Math.toRadians(this.odometer.getAng())) * distance);

	}
}



//import lab3EV3Navigation.OdometryDisplay;
/*import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
*//**
 * Navigation class for lab 3. Provides functionality for demos 1 and 2.
 * 
 * 
 * @author thomaschristinck
 * @author alexmasciotra
 *//*

public class Navigation extends Thread {
	//Final variables

	private static final double BUFFER = 1.0;
	private static final int FORWARD_SPEED = 225;
	private static final int ROTATE_SPEED = 125;
	private static int demo;
	private double destX;
	private double destY;
	private final Object lock;
	private double radius;
	private double width;	
	private Odometer odometer;
    OdometryDisplay display; 
    boolean isNavigating;
    boolean isAvoiding;
    int usDistance;
    
   
    //Variables to hold current and final x and y values
    double x = 0; 
	double y = 0;
	double dX = 0;
	double dY = 0;
	
	//Declare motors
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
 
	final static TextLCD screen = LocalEV3.get().getTextLCD();   
	
	public Navigation(EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor, Odometer odometer, double radius, double width){
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.radius = radius;
		this.width = width;
				
		lock = new Object();
		destX = 0;
		destY = 0;
		
		// Reset motors
		for(EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }){
			motor.stop();
			motor.setAcceleration(3000); 
		}
	}

        
    public void run(){ 
    	try {
    		//Wait before next navigation
			Thread.sleep(2000); 
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	if(demo == 1){
    		//Demo Part 1
    		//Way points defined in lab
    		travelTo(60,30);
			travelTo(30,30);
			travelTo(30,60);
			travelTo(60,0);
   		} else if (demo == 2) {
   			do{
   				//First way-point defined
				travelTo(0,60);
			}while(isAvoiding);
			do
			{
				//Second way-point defined
				travelTo(60,0);
			}while(isAvoiding);
   		}
    }
    
    //Set destination to a specified x and y
private void setDest(double x, double y){
	this.destX = x;
	this.destY = y;
}

void travelTo(double x, double y) {
	setDest(x, y);
	
	//Get current x, y and theta
	double oldX = odometer.getX();
	double oldY = odometer.getY();
	double oldTheta = odometer.getTheta();
	
	double destTheta = arctan((y - oldY),(x - oldX));
	synchronized(lock){
		//Find theta to travel to
		double dTheta = (((Math.PI / 2) - destTheta - oldTheta));
		dTheta = angleWrap(dTheta);
		turnTo((dTheta));
	}
	//Head in dTheta direction
	leftMotor.setSpeed(FORWARD_SPEED);
	rightMotor.setSpeed(FORWARD_SPEED);
	leftMotor.forward();
	rightMotor.forward();
	
	while(isNavigating()){
		//Do nothing
	}
	if (demo == 2){
		//If Part 2, turn to second way-point after brick has been avoided
		do{
			//Second way-point defined
			travelTo(60,0);
		}while(isAvoiding);
	}
	leftMotor.stop(true);
	rightMotor.stop(true);
}
	
	public void turnTo(double theta) {
		isNavigating = true;
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(convertAngle(radius, width, theta), true);
		rightMotor.rotate(-convertAngle(radius, width, theta), false);
	}

	

	double distanceLeft(double x, double y) { 
		// Calculates the distance left to travel
		double distanceleft;
		distanceleft = Math.sqrt(Math.pow((x - odometer.getX()), 2) + Math.pow((y - odometer.getY()), 2)); 
		return distanceleft;
	}
	
	//Returns arc-tangent within appropriate range(-pi, pi)
	private double arctan(double y,double x){
		double result = Math.atan(y/x);
		if(x<0){
			if(y>0)
				result += Math.PI;
			else
				result -= Math.PI;
		}
		return result;
	}
	
	public void setDemo(int demoNumber){
		demo = demoNumber;
	}
	
	private boolean isNavigating(){
		//if destination reached, stop moving, no obstacle need to be crossed
		if(Math.abs(odometer.getX() - destX) <= BUFFER && Math.abs(odometer.getY() - destY) <= BUFFER ){
			isAvoiding = false;
			return false;	
		}
		else
			//Otherwise keep navigating
			return true;	
	}
	
	//Methods from previous labs
	private double angleWrap(double angle){
		if (angle > Math.PI)
			return (angle - 2 * Math.PI);
		else if(angle < -Math.PI)
			return angle + 2 * Math.PI;
		else 
			return angle;
	}
	
	private  int convertAngle(double radius, double width, double angle) { 
		return (int)((angle*width*90)/(Math.PI*radius)); 
	}
} */