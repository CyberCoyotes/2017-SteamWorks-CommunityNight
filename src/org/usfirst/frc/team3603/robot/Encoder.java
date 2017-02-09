package org.usfirst.frc.team3603.robot;

import com.ctre.CANTalon;

public class Encoder {
	CANTalon talon;
	
	public Encoder(int pin) {
		talon = new CANTalon(pin);
	}
	
	public double getRate() {
		return talon.getSpeed();
	}
	
	public double getEncPos() {
		return talon.getEncPosition();
	}
	
	public void callibrate() {
		talon.setEncPosition(0);
	}
	
	
}
