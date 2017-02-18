package org.usfirst.frc.team3603.robot;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTableKeyNotDefined;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


@SuppressWarnings("deprecation")
public class Vision {
	NetworkTable table;
	double[] defaultValue = {0.0, 0.0, 0.0, 0.0, 0.0};
	
	@SuppressWarnings("unused")
	public Vision() {
		try {
			table = NetworkTable.getTable("GRIP/cyberVision");
			SmartDashboard.putBoolean("Vision", true);
			double[] test = table.getNumberArray("centerX");
		} catch (NetworkTableKeyNotDefined ex) {
			table = NetworkTable.getTable("CyberCoyotes/errorFallback");
			table.putNumberArray("centerX", defaultValue);
			table.putNumberArray("centerY", defaultValue);
			table.putNumberArray("area", defaultValue);
			table.putNumberArray("width", defaultValue);
			table.putNumberArray("height", defaultValue);
			SmartDashboard.putBoolean("Vision", false);
		}
	}
	
	public double getRawCenterX() {
		double[] x = table.getNumberArray("centerX");
		if(x==null || x.length<2) {
			return 0;
		} else {
			double x1 = x[0];
			double x2 = x[1];
			double center = (x1+x2)/2;
			return center;
		}
	}
	
	public double getNumContours() {
		double[] x = table.getNumberArray("centerX");
		if(x==null) {
			return 0;
		} else {
			return x.length;
		}
	}
	
	public double getCenterX() {
		double[] x = table.getNumberArray("centerX");
		if(x==null || x.length<2) {
			return 0;
		} else {
			double x1 = x[0];
			double x2 = x[1];
			double center = (x1+x2)/2;
			center = center * 0.003125 -1;
			return -center;
		}
	}
	
	public double getContourInfo(String type, int num) {
		double[] x = table.getNumberArray("centerX");
		double[] y = {0.0};
		if(x==null) {
		} else {
			switch(type) {
			case "centerX":
				y = table.getNumberArray("centerX");
				break;
			case "centerY":
				y = table.getNumberArray("centerY");
				break;
			case "height":
				y = table.getNumberArray("height");
				break;
			case "width":
				y = table.getNumberArray("width");
				break;
			case "area":
				y = table.getNumberArray("area");
				break;
			}
		}
		return y[num];
	}
	
	@SuppressWarnings("unused")
	public boolean retryTableSetting() {
		try {
			table = NetworkTable.getTable("GRIP/cyberVision");
			SmartDashboard.putBoolean("Vision", true);
			double[] test = table.getNumberArray("centerX");
			return true;
		} catch (NetworkTableKeyNotDefined ex) {
			table = NetworkTable.getTable("CyberCoyotes/errorFallback");
			table.putNumberArray("centerX", defaultValue);
			table.putNumberArray("centerY", defaultValue);
			table.putNumberArray("area", defaultValue);
			table.putNumberArray("width", defaultValue);
			table.putNumberArray("height", defaultValue);
			SmartDashboard.putBoolean("Vision", false);
			return false;
		}
	}
}
