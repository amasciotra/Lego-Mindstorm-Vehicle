/*
 * Odometer.java
 */
package lab2EV3Odometer;


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
	private static final double WHEEL_RADIUS = 2.07;
	private static final double WHEELBASE_WIDTH = 18.50;//real 18.6
	private static final double TWO_PI = 2 * Math.PI;
	
	// lock object for mutual exclusion
	private Object lock;

	// Default constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		lock = new Object();
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		
		 // Reset motor tacho counts
	    leftMotor.resetTachoCount();
	    rightMotor.resetTachoCount();
	    // Set starting position
	    x = 0;
	    y = 0;
	    theta =  Math.PI / 2;
	    
		while (true) {
			updateStart = System.currentTimeMillis();
			
			
			//Find the current phi and rho by converting tacho count of each 
			//motor to radians (rpm to rads)
			phi = Math.toRadians(leftMotor.getTachoCount());
			rho = Math.toRadians(rightMotor.getTachoCount());
			//Compute the difference from previous values
			double deltaPhi = phi - oldPhi;
			double deltaRho = rho - oldRho;
			oldPhi = phi;
			oldRho = rho;
			//Scale delta angle by the wheel radius
			double deltaPhiRadius = WHEEL_RADIUS * deltaPhi;
			double deltaRhoRadius = WHEEL_RADIUS * deltaRho;
			//Compute average delta 
			double deltaAvg = deltaRhoRadius + deltaPhiRadius / 2;
			//Compute delta theta (robot position)
			double deltaTheta = (deltaPhiRadius - deltaRhoRadius) / WHEELBASE_WIDTH;
			//Find delta x and delta y (y is forward, x is right)
			double deltaX = deltaAvg * Math.cos(theta + (deltaTheta / 2));
			double deltaY = deltaAvg * Math.sin(theta + (deltaTheta / 2));
			
			synchronized (lock) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here!
				 * Only update the values of x, y, and theta in this block. 
				 * Do not perform complex math
				 * 
				 */
				
				//Update x, y and theta
				x += deltaX;
				y += deltaY;
				theta += deltaTheta;
				theta = theta % TWO_PI;
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// Accessors
	public void getPosition(double[] position, boolean[] update) {
		// Ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
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
			result = theta;
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