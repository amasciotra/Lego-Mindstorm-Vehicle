package lab4EV3Localization;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

/**
 * Light Localizer class uses the light sensor to localize the robot, assuming the robot starts in the lower left quadrant
 * (i.e. in the correct location after running USLocalizer). After localization, the robot should travel to (0, 0, 0).
 * 
 * Sunday February 5, 2017
 * 2:30pm
 * 
 * @author thomaschristinck
 * @author alexmasciotra
 */
public class LightLocalizer {
	public static int ROTATION_SPEED = 80;
	public static int FORWARD_SPEED = 90;
	public static int ACCEL = 300;	
	public static int SENSOR_DIST = 2;/*this needs tweaking, do not know what it defines*/
	public static int TURN_ERROR = 5;
	private static double THRESHOLD = 0.17;//If beeping too soon - buffer is low
	
	//Declare odometer, initialize color sensor
	private static final Port colorPort = LocalEV3.get().getPort("S2");	
	private EV3ColorSensor colorSensor = new EV3ColorSensor(colorPort);
	private static SampleProvider sampleProvider;
	private Odometer odo;	
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private boolean isNavigating;
	private int sensorAve = 0;
	private Navigation nav;
	
	public LightLocalizer(Odometer odo, Navigation nav) {
		this.odo = odo;
		this.nav = nav;
		EV3LargeRegulatedMotor[] motors = odo.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];
		this.isNavigating = false;
	}
	
	public void doLocalization() {
		//Angle array will store angles of each black line sensed
		double [] angle = new double [4]; 
		//Operate sensor in reflection mode
		sampleProvider = colorSensor.getRedMode();
		
		//Calibrate the sensor - basically read the sensor and see what value we get
	    calibrateSensorAverage();
	    
	    //Motor speeds
	    leftMotor.setSpeed(FORWARD_SPEED); 
		rightMotor.setSpeed(FORWARD_SPEED);
	    
	    //First we need to get to the position listed in tutorial	
		// The robot will move forward until it detects the next black line
	    forward();	
		while(isNavigating)
		{
			if(blackLineDetected())
				stopMotors();
		}
		
		// The robot then moves back a set amount and rotates 90 degrees
		moveDistance(SENSOR_DIST, true);
		rotate(-90);
		forward();
		
		// The robot will move forward until it detects the next black line (along x-axis)
	    forward();

		while(isNavigating)
		{
			if(blackLineDetected())
				stopMotors();
		}
		// The robot then moves forward a set amount
		moveDistance(SENSOR_DIST, true);
		rotate();
				
		
	    // Rotate and clock the 4 grid lines
        leftMotor.setSpeed(ROTATION_SPEED);
        rightMotor.setSpeed(ROTATION_SPEED);
        
        int counter = 0;
        while(counter < 4)
		{
        	if (blackLineDetected()){
  				angle[counter] = odo.getTheta();
  				counter++;
  				try {
  					//Sleep to avoid counting the same line twice
  					Thread.sleep(400);
  				} catch (InterruptedException e) {}
  			}
		}
		
		stopMotors();
		
		// Calculates its current position
		double thetaX = angle[2] - angle[0];
		double thetaY = angle[3] - angle[1];
		
		double xPosition = -1*SENSOR_DIST*Math.sin(Math.toRadians(thetaX/2));
		double yPosition= -1*SENSOR_DIST*Math.sin(Math.toRadians(thetaY/2));
		
		//Correct theta, then add to current theta
		double newTheta = 168 - angle[0]; 
		System.out.println("\n\n\n\n New theta: " + (int)newTheta + "\n odometer: " + (int)odo.getTheta());
		newTheta += odo.getTheta();
		newTheta = Odometer.fixDegAngle(newTheta);
		
		// Updates odometer to current location
		odo.setPosition(new double [] {xPosition, yPosition, newTheta}, new boolean [] {true, true, true});
		
		// Turns to 0 degrees
		//nav.turnTo(0,true);
		
		// navigates to (0,0) and turns to 0 degrees
		nav.travelTo(0, 0);
		nav.turnTo(0,true);
	}
	
	// Method moves the robot a set distance either forward or backwards
	private void moveDistance(double distance, boolean isForward){
		int tmp = 0;
		if(isForward)
			tmp = 1;
		else
			tmp = -1;
		leftMotor.rotate(tmp * convertDistance(odo.getRadius(), distance), true);
		rightMotor.rotate(tmp * convertDistance(odo.getRadius(), distance), false);
	}
	
	// Method rotates robot by theta degrees
	private void rotate(double theta){
		//theta += TURN_ERROR;
		leftMotor.rotate(convertAngle(odo.getRadius(), odo.getWidth(), theta), true);
		rightMotor.rotate(-convertAngle(odo.getRadius(), odo.getWidth(), theta), false);
	}
	
	// Method rotates robot counterclockwise
	private void rotate(){
		leftMotor.backward();
		rightMotor.forward();
	}
	
	// method moves the robot forwards and updates the isNavigating value
	private void forward(){
		leftMotor.forward();	
		rightMotor.forward();
		isNavigating = true;
	}
	
	// method stops motors and updates isNavigating value
	private void stopMotors(){
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
	
	//Calibrates sensor (baseline is average of 4 tile readings)
  	private int calibrateSensorAverage(){
  		int sensorValue = 0;
  		for(int i = 0;i < 4; i++){
  			sensorValue += 100 * getColorData();;
  		}
  		sensorValue = sensorValue / 4;
  		this.sensorAve = sensorValue;
  		return sensorValue;	
  	}
  	
	// Helper method to detect the black line
  	private boolean blackLineDetected() {
  		float lineCheck = getColorData();
  		//float deltaCheck = Math.abs(sensorAve - lineCheck);
  		//Black line is detected if the color is below the tile's color by a threshold
  		boolean isHit = (lineCheck < THRESHOLD);
  		if (isHit)
  			Sound.beep();	
  		return isHit; 
  	}

  	// Method returns the value of the color sensor
 	private float getColorData(){
 		//Set up array to collect samples
 		int sampleSize = 1;
 		int offset = 0;
 		float[] sample = new float[sampleSize];
		sampleProvider.fetchSample(sample, offset);
 		return sample[0];
 	}
 	
}