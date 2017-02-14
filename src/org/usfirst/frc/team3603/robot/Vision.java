package org.usfirst.frc.team3603.robot;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class Vision {
	NetworkTable table;
	public static final double xRes = 480;
	double[] defaultValue = {0};
	
	public Vision() {
		table = NetworkTable.getTable("GRIP/cyberContours");
	}
	
	@SuppressWarnings("deprecation")
	public int getNum() {
		double[] x = table.getNumberArray("centerX");
		int y = x.length;
		return y;
	}
	
	@SuppressWarnings("deprecation")
	public double getRawCenterX() {
		double[] x = table.getNumberArray("centerX");
		if(x==null || x.length<2) {
			return -2;
		} else {
			double x1 = x[0];
			double x2 = x[1];
			double center = (x1+x2)/2;
			return -center;
		}
	}
	
	@SuppressWarnings("deprecation")
	public double getCenterX() {
		double[] x = table.getNumberArray("centerX");
		if(x==null || x.length<2) {
			return -2;
		} else {
			double x1 = x[0];
			double x2 = x[1];
			double center = (x1+x2)/2;
			center = center * 0.003125 -1;
			return -center;
		}
	}
}
