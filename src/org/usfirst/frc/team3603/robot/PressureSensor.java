package org.usfirst.frc.team3603.robot;

import edu.wpi.first.wpilibj.AnalogInput;

public class PressureSensor {
	AnalogInput analog;
	
	public PressureSensor(int pin) {
		analog = new AnalogInput(pin);
	}
	
	public int getPres() {
		int adjusted = (int) (49.775*analog.getVoltage()-24.864);
		if(adjusted > 0) {
			return adjusted;
		} else {
			return 0;
		}
	}
}
