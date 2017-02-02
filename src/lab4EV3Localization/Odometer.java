package lab4EV3Localization;

/**
 * Odometer code from Lab 2 with minor modifications; used for localization.
 * 
 * @author thomaschristinck
 * @author alexmasciotra
 */


import lejos.hardware.motor.*;

public class Odometer extends Thread {
	// Robot position (given by x, y and theta, from xy-axis)
	private double x, y, theta;
	//Relevant tachometer readings in rads
	private double phi, rho, oldPhi, oldRho;
	private int leftMotorTachoCount, rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	// Odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;
	private static final double TWO_PI = 2 * Math.PI;
	//Parameters entered when object is initialized
	private double radius;
	private double width;
	
	// Lock object for mutual exclusion
	private Object lock;

	// Default constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor, double radius, double width) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		this.radius = radius;
		this.width = width;
		lock = new Object();
	}

	//Theta correction(corrects negative angles)
	public double thetaCorrection(double radians){
		double omega = radians;
		if(omega < 0){
			omega = (TWO_PI) + omega;
		}
		else if(omega >= (TWO_PI)){
			omega = omega % (TWO_PI);
		}
		return omega;
	}
		
	// Run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		
		 // Reset motor tacho counts
	    leftMotor.resetTachoCount();
	    rightMotor.resetTachoCount();
	   
	    // Set starting position
	    x = 0;
	    y = 0;
	    theta = 0;
	    oldPhi = leftMotor.getTachoCount();
	    oldRho = rightMotor.getTachoCount();
	   
	    		
	     
		while (true) {
			updateStart = System.currentTimeMillis();
			//Find the current phi and rho 
			phi = (leftMotor.getTachoCount());
			rho = (rightMotor.getTachoCount());
			
			//Left and right displacements
			double leftDist = Math.PI * radius * (phi-oldPhi)/180;
			double rightDist= Math.PI * radius * (rho-oldRho)/180;
			oldPhi = phi;
			oldRho = rho;
		
		
			//Compute average delta for distance
			double deltaAvgDist = (leftDist + rightDist) / 2;
			//Compute average delta for theta
			double deltaTheta = (leftDist - rightDist) / width;
			
			synchronized (lock) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here!
				 * Only update the values of x, y, and theta in this block. 
				 * Do not perform complex math
				 * 
				 */
				//Update x, y, and theta by delta x, delta y, delta theta
				theta += deltaTheta;
				double deltaX = deltaAvgDist * Math.sin(theta + (deltaTheta / 2));
				double deltaY = deltaAvgDist * Math.cos(theta + (deltaTheta / 2));
				x += deltaX;
				y += deltaY;
			}

			// This ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// There is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// Accessors
	public void getPosition(double[] position) {
		// Ensure that the values don't change while the odometer is running
		synchronized (lock) {
			position[0] = x;
			position[1] = y;
			//Convert angle sent to odometer to degrees
			double degrees = (theta * 360 / TWO_PI);
			if(degrees > 180)
				degrees -= 360;
			if(degrees < -180)
				degrees += 360;
			position[2] = degrees;	
		}
	}
	
	public double getRadius()
	{
		return radius;
	}
	
	public double getWidth()
	{
		return width;
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result =  theta ;
		}

		return result;
	}

	// Mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
	
	public EV3LargeRegulatedMotor [] getMotors() {
		return new EV3LargeRegulatedMotor[] {this.leftMotor, this.rightMotor};
	}

	/**
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * @param leftMotorTachoCount the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;	
		}
	}

	/**
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * @param rightMotorTachoCount the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;	
		}
	}
}