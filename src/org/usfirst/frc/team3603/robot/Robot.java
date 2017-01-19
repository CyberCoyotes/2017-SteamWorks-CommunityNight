/****************************************
 * 
 *	THOMAS 2
 *	@author CyberCoyotes
 *
 ****************************************/
package org.usfirst.frc.team3603.robot;

import edu.wpi.first.wpilibj.ADXL362;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.interfaces.Accelerometer.Range;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	final String defaultAuto = "Default";
	final String redAuton = "redAuton";
	final String blueAuton = "blueAuton";
	final static int visAll = 20;
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	
	Joystick joy1 = new Joystick(0);
	Joystick joy2 = new Joystick(1);
	
    Victor backLeft = new Victor(1);//
    Victor backRight = new Victor(2);//
    Victor frontLeft = new Victor(3);//
    Victor frontRight = new Victor(4);//
    Victor shoot = new Victor(5);
    
    RobotDrive mainDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
    
    ADXRS450_Gyro gyro = new ADXRS450_Gyro();
    ADXL362 accel = new ADXL362(Range.k8G);
    Timer timer = new Timer();
    Encoder enc = new Encoder(0, 1, true, Encoder.EncodingType.k4X);
    
    int x = 0;
    
    Vision2017 vision = new Vision2017(0);
    
	public void robotInit() {
    	frontRight.setInverted(true);
    	backRight.setInverted(true);
    	gyro.calibrate();
    	gyro.reset();
    	chooser.addDefault("Default Auto", defaultAuto);
		chooser.addObject("Red Autonomous Code", redAuton);
		chooser.addObject("Blue Autonomous Code", blueAuton);
		SmartDashboard.putData("Auto choices", chooser);
    }
	public void autonomousInit() {
		autoSelected = chooser.getSelected();
    }
    public void autonomousPeriodic() {
    	timer.start();
    	switch (autoSelected) {
		case defaultAuto:
			DefaultAuto();
			break;

		case redAuton:
			RedAuton();
			break;
		
		case blueAuton:
			BlueAuton();
			break;
    	}
    }
    
	public void teleopPeriodic() {
    	while(isOperatorControl() && isEnabled()) {
    		if(joy1.getRawButton(1) || joy1.getRawButton(2) || joy1.getRawButton(3) || joy1.getRawButton(4) || joy1.getRawButton(5) || joy1.getRawButton(6) || joy1.getRawButton(7) || joy1.getRawButton(8) || joy1.getRawButton(9) || joy1.getRawButton(10) ||  joy2.getRawButton(1) || joy2.getRawButton(2) || joy2.getRawButton(3) || joy2.getRawButton(4) || joy2.getRawButton(5) || joy2.getRawButton(6) || joy2.getRawButton(7) || joy2.getRawButton(8) || joy2.getRawButton(9) || joy2.getRawButton(10) || joy1.getRawAxis(0) >= 0.05 || joy1.getRawAxis(1) >= 0.05 || joy1.getRawAxis(2) >= 0.05 || joy1.getRawAxis(3) >= 0.05 || joy1.getRawAxis(4) >= 0.05 || joy1.getRawAxis(5) >= 0.05 || joy1.getRawAxis(6) >= 0.05 || joy2.getRawAxis(0) >= 0.05 || joy2.getRawAxis(1) >= 0.05 || joy2.getRawAxis(2) >= 0.05 || joy2.getRawAxis(3) >= 0.05 || joy2.getRawAxis(4) >= 0.05 || joy2.getRawAxis(5) >= 0.05 || joy2.getRawAxis(6) >= 0.05 || joy1.getRawAxis(0) <= -0.05 || joy1.getRawAxis(1) <= -0.05 || joy1.getRawAxis(2) <= -0.05 || joy1.getRawAxis(3) <= -0.05 || joy1.getRawAxis(4) <= -0.05 || joy1.getRawAxis(5) <= -0.05 || joy1.getRawAxis(6) <= -0.05 || joy2.getRawAxis(0) <= -0.05 || joy2.getRawAxis(1) <= -0.05 || joy2.getRawAxis(2) <= -0.05 || joy2.getRawAxis(3) <= -0.05 || joy2.getRawAxis(4) <= -0.05 || joy2.getRawAxis(5) <= -0.05 || joy2.getRawAxis(6) <= -0.05) {
    			/***********************
	    		 *** DRIVER CONTROLS ***
	    		 ***********************/
    			
	    		final double CENTER_IMAGE = vision.GetCameraWidth()/2;
	    		double x = Math.pow(joy1.getRawAxis(0), 3);
	    		double y = Math.pow(joy1.getRawAxis(1), 3);
	    		double rot = Math.pow(joy1.getTwist(), 3)/2;
	    		
	    		if(Math.abs(x)>=0.1 || Math.abs(y)>=0.1 || Math.abs(rot)>=0.1 && joy1.getRawButton(2)) {
	    			mainDrive.mecanumDrive_Cartesian(x, y, rot, gyro.getAngle());
	    		} else if(Math.abs(x)>=0.1 || Math.abs(y)>=0.1 || Math.abs(rot)>=0.1) {
	    			mainDrive.mecanumDrive_Cartesian(x, y, rot, 0);
	    		} else {
	    			
	    		}
	    		while(joy1.getRawButton(1)) {
	    			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
	    			read();
	    		}
	    		boolean gear = false;
	    		
	    		if(joy1.getRawButton(4)) {
	    			gyro.reset();
	    		}
	    		
	    		while(joy1.getRawButton(3)) {
		    		while(joy1.getRawButton(3) && gear == false) {
		    			if(vision.centerGear()> CENTER_IMAGE + visAll) {
		    				mainDrive.mecanumDrive_Cartesian(0, 0, -.28, 0);
		    			} else if(vision.centerGear()< CENTER_IMAGE - visAll) {
		    				mainDrive.mecanumDrive_Cartesian(0, 0, .28, 0);
		    			} else if(vision.centerGear() > CENTER_IMAGE - visAll && vision.centerGear() < CENTER_IMAGE + visAll) {
		    				mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		    				gear = true;
		    			}
		    			read();
		    		}
		    		read();
	    		}
	    		
    		} else {
    			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
    		}
    		try {
    			read();
				Thread.sleep(25);
			} catch (InterruptedException e) {
			}
    	}
    }
    public void testPeriodic() {
    }
    
    void read() {
    	SmartDashboard.putNumber("Center X", vision.centerGear());
		SmartDashboard.putNumber("Center Y", vision.GetContour1CenterY());
		SmartDashboard.putNumber("Gyro angle", gyro.getAngle());
    }
    
    private void DefaultAuto() {
    	timer.reset();
    	gyro.reset();
    	while(isAutonomous() && isEnabled() && timer.get() < 6.0) {
    		mainDrive.mecanumDrive_Cartesian(0, -0.2, 0.2, gyro.getAngle());
    		read();
    	}
	}
    
    private void RedAuton() {
    	timer.reset();
    	gyro.reset();
    	
    	while(isAutonomous() && isEnabled()) {
    		boolean end = false;
    		while(timer.get() < 3 && end == false) {
    			if(vision.centerGear()> vision.GetCameraWidth() + visAll) {
    				mainDrive.mecanumDrive_Cartesian(0, 0, -.28, 0);
    			} else if(vision.centerGear()< vision.GetCameraWidth() - visAll) {
    				mainDrive.mecanumDrive_Cartesian(0, 0, .28, 0);
    			} else {
    				mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
    				end = true;
    			}
    			read();
    		}
    		mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
    		read();
    	}
    	
    }
    
    private void BlueAuton() {
    	while(isAutonomous() && isEnabled()) {
    		mainDrive.mecanumDrive_Cartesian(0, 0, -.3, 0);
    		read();
    	}
    }
    
}