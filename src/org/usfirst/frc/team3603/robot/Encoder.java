package org.usfirst.frc.team3603.robot;

import com.ctre.CANTalon;

public class Encoder {
	CANTalon talon;
	
	public Encoder(int pin) {
		talon = new CANTalon(pin);
	}
	
	public double getRate() {
		return talon.getEncVelocity()/4096*8*Math.PI;
	}
	
	public double getEncPos() {
		return talon.getEncPosition();
	}
	
	public void callibrate() {
		talon.setEncPosition(0);
	}
	public double getDistance() {
		double x = -(talon.getEncPosition()/4096)*8*Math.PI;
		return x;
	}
	public void invert(boolean in) {
		talon.setInverted(in);
	}
}
