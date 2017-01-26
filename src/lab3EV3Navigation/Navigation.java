package lab3EV3Navigation;

import lab1EV3WallFollower.BangBangController;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation extends Thread {
	//Final variables
	private static final double WHEEL_RADIUS = 2.07;
	private static final double TRACK = 18.54;
	private static final double BUFFER = 0.5;
	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 150;
	
	//Way points defined in lab (1 correspond to part one, 2 corresonds to part 2)
	private double[] x1 = {60.0, 30.0, 30.0, 60.0}; 
	private double[] y1 = {30.0, 30.0, 60.0, 0.0}; 
	private double[] x2 = {0.0, 60.0}; 
	private double[] y2 = {60.0, 0.0}; 
	
	EV3LargeRegulatedMotor leftMotor, rightMotor;
	Odometer odometer;
    OdometryDisplay display;
    BangBangController bangbang;  
    boolean isNavigating, isAvoiding;
    int usDistance; 
   
    //Variables to hold current and final x and y values
    double x = 0 ; 
	double y = 0 ;
	double dX = 0;
	double dY = 0;
	 
	final static TextLCD screen = LocalEV3.get().getTextLCD();
	float[] sample = new float[1];        

    public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, BangBangController bangbang, boolean isAvoiding){   //constructor  	
    	this.rightMotor = rightMotor;
    	this.leftMotor = leftMotor;
    	this.bangbang = bangbang;
    	this.isAvoiding = isAvoiding;
    	this.odometer = new Odometer(leftMotor, rightMotor);
        OdometryDisplay display = new OdometryDisplay (odometer, screen);                         
        odometer.start(); 
        display.start(); 
    } 
        
    public void run(){  
    	
    	//Demo Part 1
    	if(!isAvoiding)
    	{
    		for (int i = 0; i < 4; i++) {
    			isNavigating = true;
    			while(isNavigating){
    				travelTo(x1[i], y1[i]);
    				//Set isNavigating to false once arrived at destination
    			}
    		}
    	}
    	else
    	{
    		for (int i=0; i<2; i++)
			{	
				isNavigating = true;
				if (i == 1)
				{
					isAvoiding = true;
				}
				while (isNavigating)  
				{
					travelTo(x2[i],y2[i]);
				}
			}
    	}
    } 
  
void travelTo(double x, double y) {
	if(isAvoiding)// Starts part 2 of demo
	{	
		if (bangbang.readUSDistance() <= 10) {
			//To close to obstacle. Turn on bangbang for a specified amount of time
			while (isAvoiding)
			{
			
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);

			//Rotate robot 90 degrees if wall encountered
			leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, 90.0), true);	
			rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, 90.0), false); 
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
	} else {
		//Part 1 of demo
		//Find distance to waypoint
		dX = x - odometer.getX();
		dY = y - odometer.getY();
		
		//If dX and dY are within a BUFFER range of x, y, then stop navigating
		if (Math.abs(dX) < BUFFER && Math.abs(dY) < BUFFER){
			isNavigating = false;
			leftMotor.stop();
			rightMotor.stop();
			System.out.println("Destination reached!");
		} else {
			//Otherwise destination has not been yet reached
			while (distanceLeftGo(x, y) > 1) { // will continuously try and correct position until the distance left is less than 1
				if (Math.abs(x - odometer.getX()) < BUFFER){      
				            								
					if (y < odometer.getY()) // turns robot to 180 if y is less than the y stored in the odometer
						turnTo(180); 
					else // turns the robot to 0 if y is greater than the y stored in the odometer
						turnTo(0); 
				}
				if (Math.abs(y - odometer.getY()) < BUFFER){        
				     										
					if (x < odometer.getX()) // turns the robot to -90 if x is less than the x stored in the odometer
						turnTo(-90); 
					else // turns the robot to 90 if x is greater than the x stored in the odometer
						turnTo(90); 
				}
				else { //if the x and y errors are greater than .2 than the robot will try to correct its position
				
					if (y > odometer.getY()) 
						turnTo(Math.toDegrees(Math.atan((x - odometer.getX())/(y - odometer.getY()))));					  
					else if (x < odometer.getX())
						turnTo((-1)*Math.toDegrees(Math.atan((y - odometer.getY())/(x - odometer.getX()))) - 90);
					else
						turnTo((-1)*Math.toDegrees(Math.atan((y - odometer.getY())/(x - odometer.getX()))) + 90);
				}			
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.setSpeed(FORWARD_SPEED);
				leftMotor.forward();
				rightMotor.forward();
			}
		}
	}
}
	
	private void turnTo(double theta) {
		//Turn to the absolute angle specified
	
	}	
	
	double distanceLeftGo(double x, double y) { // calculates the remaining distance the robot needed to travel to get to the absolute position
		double distanceleft;
		distanceleft = Math.sqrt(Math.pow((x - odometer.getX()), 2) + Math.pow((y - odometer.getY()), 2)); 
		return distanceleft;
	}

	public static boolean isNavigating() { // true if the robot is still correcting its position
		return isNavigating();
	}
	
	//Methods from previous labs
	private  int convertDistance(double radius, double distance)
	{ 															 
		return (int) ((180.0 * distance) / (Math.PI * radius)); 
	} 
	      
	private  int convertAngle(double radius, double width, double angle) 
	{ 
		return convertDistance(radius, Math.PI * width * angle / 360.0); 
	}
} 