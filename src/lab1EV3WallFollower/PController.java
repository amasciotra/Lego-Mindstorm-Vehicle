package lab1EV3WallFollower;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 200, FILTER_OUT = 20;
	private final int motorLow, motorHigh;
	private final double scalingFactor = 0.3;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;
	private int deltaDistance;
	
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
					   int bandCenter, int bandwidth, int motorHigh, int motorLow) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		leftMotor.setSpeed(motorStraight);					// Initalize motor rolling forward
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
	}
	
	@Override
	public void processUSData(int distance) {

		// rudimentary filter - toss out invalid samples corresponding to null
		// signal.
		// (n.b. this was not included in the Bang-bang controller, but easily
		// could have).
		//
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
			deltaDistance = bandCenter - distance;
			if (deltaDistance < bandwidth || deltaDistance > (-1 * bandwidth)){
				leftMotor.setSpeed(motorHigh);												// Set new speed
				rightMotor.setSpeed(motorHigh);
				leftMotor.forward();
				rightMotor.forward();
			} else if (deltaDistance > bandwidth){
			//If deltaDistance is greater than the bandwidth, robot is too close to wall and speed should 
			//be adjusted according to magnitude of deltaDistance
				leftMotor.setSpeed((int) (motorHigh*(scalingFactor*deltaDistance)));		// Set new speed
				rightMotor.setSpeed((int) (motorLow/(scalingFactor*deltaDistance)));
				leftMotor.forward();
				rightMotor.forward();
			} else if (deltaDistance < (-1 * bandwidth)){
			//If deltaDistance is less than the (negative) bandwidth, robot is too far from wall and speed 
			//should be adjusted according to magnitude of deltaDistance
				rightMotor.setSpeed((int) (motorHigh*(scalingFactor*deltaDistance)));		// Set new speed
				leftMotor.setSpeed((int) (motorLow/(scalingFactor*deltaDistance)));
				leftMotor.forward();
				rightMotor.forward();
			}
			
		}

		// TODO: process a movement based on the us distance passed in (P style)
	}

	
	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
