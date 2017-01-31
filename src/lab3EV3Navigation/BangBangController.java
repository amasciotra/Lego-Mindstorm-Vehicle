package lab3EV3Navigation;

import lejos.hardware.motor.*;

/**
 * Bangbang controller from Lab 1; used to navigate around the obstacle.
 * 
 * @author thomaschristinck
 * @author alexmasciotra
 */


public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private int distance;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int filterControl;
	private int FILTER_OUT = 20;
	//ON specifies whether the bangbang controller is active or not
	private boolean ON = false;

	public BangBangController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}

	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		if (ON){
			// Rudimentary filter - toss out invalid samples corresponding to null
			// signal.
			// (N.b. this was not included in the Bang-bang controller, but easily
			// could have).
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
			if(distance > (bandCenter + bandwidth)){
				leftMotor.setSpeed(motorLow);					// Too far, set new speed
				rightMotor.setSpeed(motorHigh);
				leftMotor.forward();
				rightMotor.forward();
			} else if (distance > 4 * bandwidth && distance < (bandCenter - bandwidth)){
				leftMotor.setSpeed(motorHigh);					// Too close, set new speed
				rightMotor.setSpeed(motorLow);
				leftMotor.forward();
				rightMotor.forward();
			} else if (distance <= 7.5 * bandwidth){
				leftMotor.setSpeed(2 * motorHigh);				// Way too close, set new speed
				rightMotor.setSpeed(2 * motorHigh);
				leftMotor.forward();
				rightMotor.backward();
			} else {
				//Continue forward at normal speed if within bandwidth of the band center
				leftMotor.setSpeed(motorHigh);					// Set new speed
				rightMotor.setSpeed(motorHigh);
				leftMotor.forward();
				rightMotor.forward();
			}
		}
	}

	public void turnON()
	{
		ON = true;
	}
	
	public void turnOFF()
	{
		ON = false;
	}
	
	@Override
	public int readUSDistance() {
		return this.distance;
	}
}
