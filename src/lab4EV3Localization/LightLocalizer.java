package lab4EV3Localization;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	public static int ROTATION_SPEED = 100;
	public static int ACCEL = 300;	
	public static int SENSOR_MIN = 15;/*this needs tweaking, do not know the range needed based on where our light sensor is*/
	public static int SENSOR_MAX = 25;/*this needs tweaking, do not know the range needed based on where our light sensor is*/
	public static int SENSOR_DIST = 15;/*this needs tweaking, do not know what it defines*/
	public static int TURN_ERROR = 5;
	
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private boolean isNavigating;
	private int angleCount;
	private double[] angles;
	private Navigation nav;
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData, Navigation nav) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.nav = nav;
		EV3LargeRegulatedMotor[] motors = odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
		this.isNavigating = false;
		angles = new double[4];
		angleCount = 0;
		
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);
		leftMotor.setAcceleration(ACCEL);
		rightMotor.setAcceleration(ACCEL);
	}
	
	public void doLocalization() {/*this part needs the work so its customizable to our robot*/
		forward();/*for some reason our robot sometimes goes backwards at the start, it needs to go forward to check the line or else reverse in the wall*/
		// the robot will move forward until it detects the next black line
		while(isNavigating)
		{
			if(getColorData() < SENSOR_MAX && getColorData() > SENSOR_MIN)
				stopMotors();
		}
		
		// the robot then moves back a set amount and rotates -90 degrees
		moveDistance(SENSOR_DIST + 5, false);
		rotate(-90);
		forward();
		
		//the robot will move forward until it detects the next black line
		while(isNavigating)
		{
			if(getColorData() < SENSOR_MAX && getColorData() > SENSOR_MIN)
				stopMotors();
		}
		
		// the robot then moves back a set amount
		moveDistance(SENSOR_DIST + 5, false);
		rotate();
		
		// the robot rotates until it crosses all 4 lines so that it can localize itself
		while(angleCount < 4)
		{
			if(getColorData() < SENSOR_MAX && getColorData() > SENSOR_MIN)
			{
				Sound.beep();
				angles[angleCount] = odo.getAng();
				angleCount++;
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		stopMotors();
		
		// calculates its current position
		double thetaX = angles[3] - angles[1];
		double thetaY = angles[2] - angles[0];
		
		double xPosition = -1*SENSOR_DIST*Math.cos((Math.PI/180.0)*(thetaX/2.0));
		double yPosition= -1*SENSOR_DIST*Math.cos((Math.PI/180.0)*(thetaY/2.0));
		
		// turns to 0 degrees
		nav.turnTo(0,true);
		
		// updates odometer to current location
		odo.setPosition(new double [] {xPosition, yPosition, 0}, new boolean [] {true, true, true});
		
		// navigates to (0,0) and turns to 0 degrees
		nav.travelTo(0, 0);
		nav.turnTo(0,true);
		// robot was consistently off so this is the error correction
		/*i commented out this part below*/
		/*nav.turnTo(360-TURN_ERROR,true);
		stopMotors();
		Sound.beepSequence();
		odo.setPosition(new double[] {0, 0, 0}, new boolean[] {true, true, true});	*/	
	}
	
	// method moves the robot a set distance either forward or backwards
	private void moveDistance(double distance, boolean isForward)
	{
		int tmp = 0;
		if(isForward)
			tmp = 1;
		else
			tmp = -1;
		leftMotor.rotate(tmp * convertDistance(odo.getRadius(), distance), true);
		rightMotor.rotate(tmp * convertDistance(odo.getRadius(), distance), false);
	}
	
	// method rotates robot by theta degrees
	private void rotate(double theta)
	{
		leftMotor.rotate(convertAngle(odo.getRadius(), odo.getWidth(), theta), true);
		rightMotor.rotate(-convertAngle(odo.getRadius(), odo.getWidth(), theta), false);
	}
	
	// method rotates robot counterclockwise
	private void rotate()
	{
		leftMotor.backward();
		rightMotor.forward();
	}
	
	// method moves the robot forwards and updates the isNavigating value
	private void forward()
	{
		
		leftMotor.forward();	
		rightMotor.forward();
		isNavigating = true;
	}
	
	// method stops motors and updates isNavigating value
	private void stopMotors()
	{
		leftMotor.stop(true);
		rightMotor.stop(false);
		isNavigating = false;
	}
	
	private  int convertDistance(double radius, double distance)
	{ 															 
		return (int) ((180.0 * distance) / (Math.PI * radius)); 
	} 
	      
	private  int convertAngle(double radius, double width, double angle) 
	{ 
		return convertDistance(radius, Math.PI * width * angle / 360.0); 
	}
	
	// method returns the value of the color sensor
	private float getColorData()
	{
		colorSensor.fetchSample(colorData, 0);
		return 100 * colorData[0];		
	}
}