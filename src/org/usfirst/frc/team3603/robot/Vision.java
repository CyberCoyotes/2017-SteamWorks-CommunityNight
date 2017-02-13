package org.usfirst.frc.team3603.robot;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class Vision {
	NetworkTable table;
	public static final double xRes = 480;
	
	public Vision() {
		table = NetworkTable.getTable("GRIP/cyberContours");
	}
	
	public double getRawTargetXpos() {
		double xPos;
		double[] defaultValue = new double[0];
		
		double[] targetX = table.getNumberArray("centerX", defaultValue);
		double[] areas = table.getNumberArray("area", defaultValue);
		if(targetX.length != areas.length) {
			
		}
		if(targetX.length==0) {
			xPos = xRes/2;
		} else xPos = targetX[0];
		return xPos;
	}
}
