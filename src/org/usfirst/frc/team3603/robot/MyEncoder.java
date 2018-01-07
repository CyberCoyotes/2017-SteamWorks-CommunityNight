/****************************************
 * 
 *	Encoder Code for ...
 *	@author Connor
 *
 ****************************************/

package org.usfirst.frc.team3603.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class MyEncoder {
	WPI_TalonSRX talon;
	
	public MyEncoder(int pin) {
		talon = new WPI_TalonSRX(pin);
	}
	
	public MyEncoder(WPI_TalonSRX inputTalon) {
		talon = inputTalon;
	}
	
	public double getRate() {
		return 0;
	}
	
	public double getEncPos() {
		return 0;
	}
	
	public void reset() {
	}
	public double getDistance() {
		double x = 0;
		return x;
	}
	public void invert(boolean in) {
		talon.setInverted(in);
	}
}
