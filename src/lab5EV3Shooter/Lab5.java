package lab5EV3Shooter;

import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.lcd.*;
//Ready for Open Source
/**
 * Main class for lab 5, handles user-initiated events, switches between methods in Shooter class.
 * 
 * Friday February 10, 2017
 * 12:41pm
 * 
 * @author thomaschristinck
 * @author alexmasciotra
 */


public class Lab5 {
	// Static Resources:
	// Left motor, Right motors connected to outputs A and D
	// Ultrasonic sensor port, color sensor ports connected to input S1 and S2
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor shooterMotorL = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));	
	private static final EV3LargeRegulatedMotor shooterMotorR = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	
	public static void main(String[] args) {		
		//Initialize odometer and shooter
		Odometer odometer = new Odometer(leftMotor, rightMotor, 30, true);
		Shooter shooter = new Shooter(shooterMotorL, shooterMotorR, odometer);
		
		//Display
		int option = 0;

		do{
			LCD.clear();
			LCD.drawString("Target 1 '<' ", 0, 1);
			LCD.drawString("Target 2 '^' ", 0, 2);
			LCD.drawString("Target 3 '>' ", 0, 3);
			option = Button.waitForAnyPress();
			LCD.clear();
			switch(option) {
			/*case Button.ID_LEFT:	
				shooter.shootLeft();
				break;
			case Button.ID_RIGHT:	
				shooter.shootRight();
				break;*/
			case Button.ID_UP:
				shooter.shootStraight();
				break;
			default:
				System.out.println("Error - invalid button");			
				System.exit(-1);
				break;
			}
		} while(Button.waitForAnyPress() != Button.ID_ESCAPE);
	System.exit(0);
	}
}


