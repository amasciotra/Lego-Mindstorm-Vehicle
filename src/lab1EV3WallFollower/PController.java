package lab1EV3WallFollower;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 200, FILTER_OUT = 20;
	private final int motorTurnFast = 300;
	private final int motorTurnSlow = 100;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;
	
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
					   int bandCenter, int bandwidth) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
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
			if (distance > (bandCenter + bandwidth)){
				leftMotor.setSpeed(motorTurnSlow);					// Set new speed
				rightMotor.setSpeed(motorTurnFast);
				leftMotor.forward();
				rightMotor.forward();
			} else if (distance < (bandCenter - bandwidth)){
				leftMotor.setSpeed(motorTurnFast);					// Set new speed
				rightMotor.setSpeed(motorTurnSlow);
				leftMotor.forward();
				rightMotor.forward();
			} else {
				//Continue forward at normal speed if within bandwidth of the band center
				leftMotor.setSpeed(motorStraight);					// Set new speed
				rightMotor.setSpeed(motorStraight);
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
