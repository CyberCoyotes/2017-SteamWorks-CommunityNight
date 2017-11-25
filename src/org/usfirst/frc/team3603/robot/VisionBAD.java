package org.usfirst.frc.team3603.robot;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTableKeyNotDefined;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.tables.TableKeyNotDefinedException;


@SuppressWarnings("deprecation")
public class VisionBAD {
	NetworkTable table;
	double[] defaultValue = {0.0, 0.0, 0.0, 0.0, 0.0};
	
	@SuppressWarnings("unused")
	public VisionBAD() {
		try {
			table = NetworkTable.getTable("GRIP/cyberVision");
			SmartDashboard.putBoolean("Vision", true);
			double[] test = table.getNumberArray("centerX");
			System.out.println("Vision is working in initial startup.");
		} catch (TableKeyNotDefinedException ex) {
			table = NetworkTable.getTable("CyberCoyotes/errorFallback");
			table.putNumberArray("centerX", defaultValue);
			table.putNumberArray("centerY", defaultValue);
			table.putNumberArray("area", defaultValue);
			table.putNumberArray("width", defaultValue);
			table.putNumberArray("height", defaultValue);
			SmartDashboard.putBoolean("Vision", false);
			System.out.println("Vision is not working in initial startup");
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
		if(x==null || x == defaultValue) {
			return 0;
		} else {
			return x.length;
		}
	}
	
	public double getCenterX() {
		double[] x = table.getNumberArray("centerX");
		if(x==null || x.length<2 || x == defaultValue) {
			return 0;
		} else {
			double x1 = x[0];
			double x2 = x[1];
			double center = (x1+x2)/2;
			center = center * 0.003125 -1;
			return -center;
		}
	}
	
	public double getAdjustmentSpeed() {
		double[] x = table.getNumberArray("centerX");
		double centX;
		if(x==null || x.length<2 || x == defaultValue) {
			centX = 0;
		} else {
			double x1 = x[0];
			double x2 = x[1];
			centX = (x1+x2)/2;
			centX = centX * 0.003125 -1;
		}
		if(centX>0.05) {
			centX = 0.2*Math.sqrt(centX-0.05)+0.05;
		} else if(centX<-0.05) {
			centX = -(0.2*Math.sqrt(centX-0.05)+0.05);
		} else {
			centX = 0;
		}
		return -centX;
	}
	
	public double getBasicAdjSpeed() {
		double[] x = table.getNumberArray("centerX");
		double centX;
		if(x==null || x.length<2 || x == defaultValue) {
			return 0;
		} else {
			double x1 = x[0];
			double x2 = x[1];
			centX = (x1+x2)/2;
			centX = centX * 0.003125 -1;
			if(centX>=0.05) {
				return -0.2;
			} else 
			if(centX<=0.05){
				return 0.2;
			} else {
				return 0;
			}
		}
	}
	
	@SuppressWarnings("unused")
	public void retryTableSetting() {
		try {
			table = NetworkTable.getTable("GRIP/cyberVision");
			SmartDashboard.putBoolean("Vision", true);
			double[] test = table.getNumberArray("centerX");
			System.out.println("Vision is working in retry.");
		} catch (NetworkTableKeyNotDefined ex) {
			table = NetworkTable.getTable("CyberCoyotes/errorFallback");
			table.putNumberArray("centerX", defaultValue);
			table.putNumberArray("centerY", defaultValue);
			table.putNumberArray("area", defaultValue);
			table.putNumberArray("width", defaultValue);
			table.putNumberArray("height", defaultValue);
			System.out.println("Vision is not working in retry.");
			SmartDashboard.putBoolean("Vision", false);
		}
	}
}
