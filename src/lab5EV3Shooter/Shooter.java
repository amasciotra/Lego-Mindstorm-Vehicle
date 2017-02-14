package lab5EV3Shooter;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Shooter class uses the US sensor to turn robot to selected target. Then shoots a ball at the target.
 * 
 * Tuesday February 14, 2017
 * 9:45am
 * 
 * @author thomaschristinck
 * @author alexmasciotra
 */

public class Shooter {
	//Relevant speeds; note when shooting straight ahead launch speed won't need to be as fast as the "skew speed"
	public static final int ROTATION_SPEED = 80;
	public static final int SLOWDOWN_SPEED = 500;
	public static final int SKEW_SHOOTING_SPEED = 2000;
	public static final int STRAIGHT_SHOOTING_SPEED = 1500;
	public static final int STRAIGHT_ACCEL = 800;
	public static final int SKEW_ACCEL = 900;
	//Angle the robot is facing (off of 90 degree) when aiming at left or right target
	public static final int TARGET_ANGLE = 18;
	public static final int BUFFER = 5;
	//Angle shooting arm rotates through to shoot
	public static final int SHOOTING_ANGLE = -95;
	public static int targetNumber;
	
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor, shooterMotor;
		
	//Initializer
	public Shooter(EV3LargeRegulatedMotor shooterMotor, Odometer odometer) {
		this.odometer = odometer;
		this.shooterMotor = shooterMotor;
		leftMotor = odometer.getLeftMotor();
		rightMotor = odometer.getRightMotor();
	}
	
	public void shootLeft() {
		targetNumber = 1;
		//Sets motor speeds and accelerations
		leftMotor.setSpeed(ROTATION_SPEED); 
		rightMotor.setSpeed(ROTATION_SPEED);
		shooterMotor.setSpeed(SKEW_SHOOTING_SPEED);
		shooterMotor.setAcceleration(SKEW_ACCEL);
		//Check if robot is already directed to target
		if (odometer.getTheta() > 90 + BUFFER) {
			//Already facing target
		} else if (odometer.getTheta() < 90 - BUFFER) {
			//Facing far right
			rotate(-2 * TARGET_ANGLE);
		} else{
			rotate(-TARGET_ANGLE);
		}
		//Now shoot
		shooterMotor.rotate(SHOOTING_ANGLE, false);
		//Return to resting position
		shooterMotor.setSpeed(ROTATION_SPEED);
		shooterMotor.rotate(-SHOOTING_ANGLE, false);
		stopMotors();
	}
	
	public void shootRight() {
		targetNumber = 3;
		//Sets motor speeds and accelerations
		leftMotor.setSpeed(ROTATION_SPEED); 
		rightMotor.setSpeed(ROTATION_SPEED);
		shooterMotor.setSpeed(SKEW_SHOOTING_SPEED);
		shooterMotor.setAcceleration(SKEW_ACCEL);
		//Check if robot is already directed to target
		if (odometer.getTheta() < 90 - BUFFER) {
			//Already facing target
		} else if (odometer.getTheta() > 90 + BUFFER) {
			//Facing left, turn thought to face right target
			rotate(2 * TARGET_ANGLE);
		} else{
			rotate(TARGET_ANGLE);
		}
		//Now shoot
		shooterMotor.rotate(SHOOTING_ANGLE, false);
		//Return to resting position
		shooterMotor.setSpeed(ROTATION_SPEED);
		shooterMotor.rotate(-SHOOTING_ANGLE, false);
		stopMotors();
	}
	
	public void shootStraight() {
		targetNumber = 2;
		//Sets motor speeds and accelerations
		leftMotor.setSpeed(ROTATION_SPEED); 
		rightMotor.setSpeed(ROTATION_SPEED);
		shooterMotor.setSpeed(STRAIGHT_SHOOTING_SPEED);
		shooterMotor.setAcceleration(STRAIGHT_ACCEL);
		//Check if robot is already directed to target
		if (90 - BUFFER < odometer.getTheta() && 90 + BUFFER > odometer.getTheta()) {
			//Already facing target
		} else if (odometer.getTheta() > 90 + BUFFER) {
			//Facing left, turn thought to face straight target
			rotate(TARGET_ANGLE);
		} else{
			rotate(-TARGET_ANGLE);
		}
		//Now shoot
		shooterMotor.rotate(SHOOTING_ANGLE, false);
		//Return to resting position
		shooterMotor.setSpeed(ROTATION_SPEED);
		shooterMotor.rotate(-SHOOTING_ANGLE, false);
		stopMotors();
	}
	
	public int getTargetNumber(){
		return targetNumber;
	}
	
	private void stopMotors(){
		leftMotor.stop(true);
		rightMotor.stop(true);
	}
	
	private void rotate(double theta) {
		leftMotor.rotate(convertAngle(odometer.getRadius(), odometer.getWidth(), theta), true);
		rightMotor.rotate(-convertAngle(odometer.getRadius(), odometer.getWidth(), theta), false);
	}
	
	private  int convertDistance(double radius, double distance)
	{ 															 
		return (int) ((180.0 * distance) / (Math.PI * radius)); 
	} 
	      
	private  int convertAngle(double radius, double width, double angle) 
	{ 
		return convertDistance(radius, Math.PI * width * angle / 360.0); 
	}

}