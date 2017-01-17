package lab1EV3WallFollower;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	
	private final int FILTER_OUT = 20;
	private final int motorLow, motorHigh;
	private final int maxSpeed = 400;
	private final int minSpeed = 125;
	private final double scalingFactor = .9;
	
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;
	private int error;
	
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
					   int bandCenter, int bandwidth, int motorHigh, int motorLow) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		leftMotor.setSpeed(motorHigh);					// Initialize motor rolling forward
		rightMotor.setSpeed(motorHigh);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
	}
	
	@Override
	public void processUSData(int distance) {

		// Rudimentary filter - toss out invalid samples corresponding to null
		// signal.
		// (N.b. this was not included in the Bang-bang controller, but easily
		// could have).
		//
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// Bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
		} else {
			// Distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
		}
		error = bandCenter - distance;													//Positive if closer than bandCenter, negative if farther
		int lowSpeed = (int) (motorLow/(scalingFactor*(Math.abs(error))));
		int highSpeed = (int) (motorHigh*(scalingFactor*(Math.abs(error))));
		//If error is within the allowed bandwidth of 0, the robot is a good distance from the wall
		//and will drive straight (both wheels at high speed).
		if ((0 <= error && error <= bandwidth) || (0 > error && error >= (-1 * bandwidth))){
			leftMotor.setSpeed(motorHigh);												// Set new speed
			rightMotor.setSpeed(motorHigh);
			leftMotor.forward();
			rightMotor.forward();
		} else if (error > bandwidth){
		//If error is greater than the bandwidth, robot is too close to wall and speed should 
		//be adjusted according to magnitude of error. The maximum possible speed is capped at 
		//"maxSpeed". Minimum possible speed is capped at "minSpeed".
			leftMotor.setSpeed(Math.max(highSpeed, maxSpeed));							// Set new speed
			rightMotor.setSpeed(Math.min(lowSpeed, minSpeed));
			leftMotor.forward();
			rightMotor.forward();
		} else {
		//If error is less than the (negative) bandwidth, robot is too far from wall and speed 
		//should be adjusted according to magnitude of error. The maximum possible speed is capped at 
		//"maxSpeed". Minimum possible speed is capped at "minSpeed".
			leftMotor.setSpeed(Math.max(lowSpeed+50, minSpeed+50));							// Set new speed
			rightMotor.setSpeed(Math.min(highSpeed, maxSpeed));
			leftMotor.forward();
			rightMotor.forward();
		}		
	}
	
	@Override
	public int readUSDistance() {
		return this.distance;
	}

}