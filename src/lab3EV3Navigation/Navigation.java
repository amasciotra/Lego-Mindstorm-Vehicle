package lab3EV3Navigation;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation extends Thread {
	//Final variables
	//private static double WHEEL_RADIUS;
	//private static double TRACK;
	private static final double BUFFER = 8;
	private static final int FORWARD_SPEED = 225;
	private static final int ROTATE_SPEED = 125;
	private static int demo;
	private UltrasonicPoller usPoller;
	private double destX;
	private double destY;
	private final Object lock;
	private double radius;
	private double width;	
	
	


	private Odometer odometer;
    OdometryDisplay display;
    BangBangController bangbang;  
    boolean isNavigating;
    boolean isAvoiding;
    int usDistance;
    
   
    //Variables to hold current and final x and y values
    double x = 0; 
	double y = 0;
	double dX = 0;
	double dY = 0;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
 
	final static TextLCD screen = LocalEV3.get().getTextLCD();   
	
	public Navigation(EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor, Odometer odometer, UltrasonicPoller usPoller, BangBangController bangbang,double radius, double width){
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.usPoller = usPoller;
		this.bangbang = bangbang;
		this.radius = radius;
		this.width = width;
				
		lock = new Object();
		destX=0;
		destY=0;
		
		
		// Reset motors
		for(EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }){
			motor.stop();
			motor.setAcceleration(1500); 
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
   			
   			while(isAvoiding){
   				//First way point defined in lab
   				travelTo(0, 60);
   			}
   			while(isAvoiding){
   				//Second way point defined in lab
   				travelTo(60, 0);
   			}
    	}
    }
    
   /* do{ alternate option since robot isnt moving for part 2
			travelTo(0,60);
		}while (isAvoiding==true);
			do{
				travelTo(60,0);
			}while(isAvoiding==true);
		}*/
 
private void setDest(double x, double y){
	this.destX = x;
	this.destY = y;
}

void travelTo(double x, double y) {
	setDest(x, y);
	
	double oldX = odometer.getX();
	double oldY = odometer.getY();
	double oldTheta = odometer.getTheta();
	
	double destTheta = arctan((y - oldY),(x - oldX));
	synchronized(lock){
		double dTheta = (((Math.PI / 2) - destTheta - oldTheta));
		dTheta = angleWrap(dTheta);
		turnTo((dTheta));
	}
	leftMotor.setSpeed(FORWARD_SPEED);
	rightMotor.setSpeed(FORWARD_SPEED);
	
	leftMotor.forward();
	rightMotor.forward();
	
	while(isNavigating()){
		//Do nothing
	}
	//something needs to be added here to put the motors in motion for part 2
	leftMotor.stop(true);
	rightMotor.stop(true);
	if(isAvoiding == true){
		/*
		 * LEAVE HERE FOR NOW
		 */
		bangbang.turnON();
		try {
			//Give the robot specified time to avoid obstacle
			Thread.sleep(8500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		bangbang.turnOFF();
		isAvoiding = false; 
	}
}
	
	private void turnTo(double theta) {
		isNavigating = true;
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		//leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, theta), true);
		//rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, theta), false);
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
		//Check if obstacle is present
		if(usPoller.getDistance() < 10 && demo == 2){
			isAvoiding = true;
			return false;
		}
		
		//if destination reached, stop moving, no obstacle need to be crossed
		if(Math.abs(odometer.getX() - destX) <= BUFFER && Math.abs(odometer.getY() - destY) <= BUFFER ){
			Sound.beep();//just putting this here temporary to know when we hit the location
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
			//return angle - 0.5 * Math.PI;
	}
	
	private  int convertAngle(double radius, double width, double angle) { 
		return (int)((angle*width*90)/(Math.PI*radius));
		//return convertDistance(radius, Math.PI * width * angle / 360.0); 
	}
	private  int convertDistance(double radius, double distance) { 															 
		return (int) ((180.0 * distance) / (Math.PI * radius)); 
	} 
	      
	
} 