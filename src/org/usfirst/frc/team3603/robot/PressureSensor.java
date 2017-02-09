package org.usfirst.frc.team3603.robot;

import edu.wpi.first.wpilibj.AnalogInput;

public class PressureSensor {
	AnalogInput analog;
	
	public PressureSensor(int pin) {
		analog = new AnalogInput(pin);
	}
	
	public double getPres() {
		double adjusted = 49.775*analog.getVoltage()-24.864;
		return adjusted;
	}
}
