package lab3EV3Navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation extends Thread {
	//Final variables
	private static double WHEEL_RADIUS;
	private static double TRACK;
	private static final double BUFFER = 0.5;
	private static final int FORWARD_SPEED = 200;
	private static final int ROTATE_SPEED = 150;
	private static int demo;
	private UltrasonicPoller usPoller;
	
	//Way points defined in lab (1 correspond to part one, 2 corresonds to part 2)
	private double[] x1 = {60.0, 30.0, 30.0, 60.0}; 
	private double[] y1 = {30.0, 30.0, 60.0, 0.0}; 
	private double[] x2 = {0.0, 60.0}; 
	private double[] y2 = {60.0, 0.0}; 
	
	Odometer odometer;
    OdometryDisplay display;
    BangBangController bangbang;  
    boolean isNavigating;
    boolean isAvoiding;
    int usDistance;
    
   
    //Variables to hold current and final x and y values
    double x = 0 ; 
	double y = 0 ;
	double dX = 0;
	double dY = 0;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
 
	final static TextLCD screen = LocalEV3.get().getTextLCD();   
	
	public Navigation(EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor, Odometer odometer, UltrasonicPoller usPoller, BangBangController bangbang){
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.usPoller = usPoller;
		this.bangbang = bangbang;
	}

        
    public void run(){  
    	if(demo == 1){
    		//Demo Part 1
    		for (int i = 0; i < 4; i++) {
    			isNavigating = true;
    			//Set isNavigating to false once arrived at destination (inside TravelTo)
    			while(isNavigating){
    				travelTo(x1[i], y1[i]);
    			}
   			}
    	} else if (demo == 2) {
    		for (int i=0; i<2; i++) {	
    			isNavigating = true;
    			if (i == 1) 
    				isAvoiding = true;
			while (isNavigating) {
					travelTo(x2[i],y2[i]);
				}
			}
    	}
    } 
  
void travelTo(double x, double y) {
	if(isAvoiding) {
		// Starts part 2 of demo
		if (bangbang.readUSDistance() <= 10) {
			//To close to obstacle. Turn on bang-bang for a specified amount of time
			while (isAvoiding) {
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
			while (distanceLeft(x, y) > 1) { 
				// Robot will adjust position until the distance left is less than 1
				if (Math.abs(x - odometer.getX()) < BUFFER){  
					//Since our theta is reversed, if y is less than the y stored in the odometer, turn to 90 (90
					//on a cartesian grid)
					if (y < odometer.getY()){ 
						turnTo(90); 
					}
					else{ 
						// Turns the robot to 270 if y is greater than the y stored in the odometer (360 on cartesian 
						// grid)
						turnTo(270);
					}
				}
				if (Math.abs(y - odometer.getY()) < BUFFER){        
				    // Tur								
					if (x < odometer.getX()){ 
						//Turns robot to 0 if x is less than x stored in odometer (360 on cartesian grid)
						turnTo(0); 
					}
					else {
						// Turns the robot to 360 if x is greater than the x stored in the odometer (0 on cartesian
						//grid)
						turnTo(360); 
					}
				}
				else { 
					//If the x and y errors are greater than BUFFER then the robot will try to correct its position
					//TO DO: Comment and clean up the following code
					if (y > odometer.getY()) {
						turnTo(Math.toDegrees(Math.atan((x - odometer.getX())/(y - odometer.getY()))));		
					}
					else if (x < odometer.getX()){
						turnTo((-1)*Math.toDegrees(Math.atan((y - odometer.getY())/(x - odometer.getX()))) - 90);
					}
					else{
						turnTo((-1)*Math.toDegrees(Math.atan((y - odometer.getY())/(x - odometer.getX()))) + 90);
					}
				}	
				//Head in adjusted direction
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.setSpeed(FORWARD_SPEED);
				leftMotor.forward();
				rightMotor.forward();
	
				//Sleep 100 ms 
				try {
					Thread.sleep(100); 
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
	
	private void turnTo(double orientation) {
		//TO DO: Turn to the absolute angle specified
		isNavigating=true;
		double degressWanted = (orientation * 180) / Math.PI;
		double currentTheta = (odometer.getTheta());
		double theta = degressWanted - currentTheta;
		
		if(theta > Math.PI){ 
			theta = theta - 2*Math.PI;
		}
		else if (theta < -Math.PI){
			theta = theta + 2*Math.PI;
		}
		//error 1
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
	}	
	
	double distanceLeft(double x, double y) { 
		// Calculates the distance left to travel
		double distanceleft;
		distanceleft = Math.sqrt(Math.pow((x - odometer.getX()), 2) + Math.pow((y - odometer.getY()), 2)); 
		return distanceleft;
	}

	public static boolean isNavigating() { 
		// Returns true if the robot is still correcting its position
		return isNavigating();
	}
	
	public void setDemo(int demoNumber){
		demo = demoNumber;
	}
	
	//Methods from previous labs
	
	private  int convertDistance(double radius, double distance) { 															 
		return (int) ((180.0 * distance) / (Math.PI * radius)); 
	} 
	      
	private  int convertAngle(double radius, double width, double angle) { 
		return convertDistance(radius, Math.PI * width * angle / 360.0); 
	}
} 