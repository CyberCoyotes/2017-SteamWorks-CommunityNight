/****************************************
 * 
 *	THOMAS 2
 *	@author CyberCoyotes
 *
 ****************************************/
package org.usfirst.frc.team3603.robot;

import edu.wpi.first.wpilibj.ADXL362;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.DoubleSolenoid;
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
	//Auton code
	final String defaultAuto = "Default";
	final String redAuton = "redAuton";
	final String blueAuton = "blueAuton";
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	
	//Controllers
	Joystick joy1 = new Joystick(0);
	Joystick joy2 = new Joystick(1);
	
	/** Replace with Talons **/
    Victor backLeft = new Victor(1);
    Victor backRight = new Victor(2);
    Victor frontLeft = new Victor(3);
    Victor frontRight = new Victor(4);
    Victor shooter = new Victor(5);//
    RobotDrive mainDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
    
    //Sensors
    ADXRS450_Gyro gyro = new ADXRS450_Gyro();
    ADXL362 accel = new ADXL362(Range.k8G);
    Timer timer = new Timer();
    Timer a = new Timer();
    Encoder enc = new Encoder(0, 1, true, Encoder.EncodingType.k4X);
    
    DoubleSolenoid blocker = new DoubleSolenoid(0, 1);//
    
    //Drive stuff
    public double x;
	public double y;
	public double rot;
	boolean speedUp = true;
    
    //Vision stuff
    Vision2017 vision = new Vision2017(0);
    final static int visAll = 20;
    
	public void robotInit() {
		//Invert the motors because they are wired backwards
    	frontRight.setInverted(true);
    	backRight.setInverted(true);
    	
    	gyro.calibrate();
    	gyro.reset();
    	
    	//Auton stuff
    	chooser.addDefault("Default Auto", defaultAuto);
		chooser.addObject("Red Autonomous Code", redAuton);
		chooser.addObject("Blue Autonomous Code", blueAuton);
		SmartDashboard.putData("Auton choices", chooser);
		
		//Encoder stuff
		double Encoder_Distance = 5.0/12.0*Math.PI/270.0; //nope
    	enc.setDistancePerPulse(Encoder_Distance);
    	enc.setSamplesToAverage(7);
    }
	public void autonomousInit() {
		autoSelected = chooser.getSelected();//Auton
    }
    public void autonomousPeriodic() {
    	//Select which auton
    	timer.start();
    	switch (autoSelected) {
		case defaultAuto:
			DefaultAuton();
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
		timer.reset();
    	while(isOperatorControl() && isEnabled()) {
    		//If nothing is being read by a controller, stop.
    		if(joy1.getRawButton(1) || joy1.getRawButton(2) || joy1.getRawButton(3) || joy1.getRawButton(4) || joy1.getRawButton(5) || joy1.getRawButton(6) || joy1.getRawButton(7) || joy1.getRawButton(8) || joy1.getRawButton(9) || joy1.getRawButton(10) ||  joy2.getRawButton(1) || joy2.getRawButton(2) || joy2.getRawButton(3) || joy2.getRawButton(4) || joy2.getRawButton(5) || joy2.getRawButton(6) || joy2.getRawButton(7) || joy2.getRawButton(8) || joy2.getRawButton(9) || joy2.getRawButton(10) || joy1.getRawAxis(0) >= 0.05 || joy1.getRawAxis(1) >= 0.05 || joy1.getRawAxis(2) >= 0.05 || joy1.getRawAxis(3) >= 0.05 || joy1.getRawAxis(4) >= 0.05 || joy1.getRawAxis(5) >= 0.05 || joy1.getRawAxis(6) >= 0.05 || joy2.getRawAxis(0) >= 0.05 || joy2.getRawAxis(1) >= 0.05 || joy2.getRawAxis(2) >= 0.05 || joy2.getRawAxis(3) >= 0.05 || joy2.getRawAxis(4) >= 0.05 || joy2.getRawAxis(5) >= 0.05 || joy2.getRawAxis(6) >= 0.05 || joy1.getRawAxis(0) <= -0.05 || joy1.getRawAxis(1) <= -0.05 || joy1.getRawAxis(2) <= -0.05 || joy1.getRawAxis(3) <= -0.05 || joy1.getRawAxis(4) <= -0.05 || joy1.getRawAxis(5) <= -0.05 || joy1.getRawAxis(6) <= -0.05 || joy2.getRawAxis(0) <= -0.05 || joy2.getRawAxis(1) <= -0.05 || joy2.getRawAxis(2) <= -0.05 || joy2.getRawAxis(3) <= -0.05 || joy2.getRawAxis(4) <= -0.05 || joy2.getRawAxis(5) <= -0.05 || joy2.getRawAxis(6) <= -0.05) {
    			/***********************
	    		 *** DRIVER CONTROLS ***
	    		 ***********************/
    			//The center of the camera image
	    		final double CENTER_IMAGE = vision.GetCameraWidth()/2;
	    		
	    		blocker.set(DoubleSolenoid.Value.kForward);
	    		
	    		a.reset();
	    		while(joy1.getRawButton(6)) {
	    			timer.reset();
	    			if(speedUp == true) {
		    			while(joy1.getRawButton(6) && a.get() <= 2.0){
		    				shooter.set(1);
		    			}
		    			speedUp = false;
	    			}
	    				
	    			while(joy1.getRawButton(6) && timer.get() <= 0.5){
	    				blocker.set(DoubleSolenoid.Value.kReverse);
	    				shooter.set(1);
	    			}
	    			while(joy1.getRawButton(6) && timer.get() <= 1){
	    				blocker.set(DoubleSolenoid.Value.kForward);
	    				shooter.set(1);
	    			}
	    		}
	    		if (! joy1.getRawButton(6)){
	    			blocker.set(DoubleSolenoid.Value.kForward);
	    			shooter.set(0);
	    			speedUp = true;
	    		}
	    			//
	    		
	    		
	    		//Pressing button 2 gives you half speeds
	    		if(joy1.getRawButton(2)) {
		    		x = Math.pow(joy1.getRawAxis(0), 3)/2;
		    		y = Math.pow(joy1.getRawAxis(1), 3)/2;
		    		rot = Math.pow(joy1.getTwist(), 3)/2;
	    		} else {
	    			x = Math.pow(joy1.getRawAxis(0), 3);
		    		y = Math.pow(joy1.getRawAxis(1), 3);
		    		rot = Math.pow(joy1.getTwist(), 3)*3/4;
	    		}
	    		
	    		//Drive w/ joystick
	    		if(Math.abs(x)>=0.1 || Math.abs(y)>=0.1 || Math.abs(rot)>=0.1 && joy1.getRawButton(2)) {
	    			mainDrive.mecanumDrive_Cartesian(x, y, rot, 0);
	    		} else if(Math.abs(x)>=0.1 || Math.abs(y)>=0.1 || Math.abs(rot)>=0.1) {
	    			mainDrive.mecanumDrive_Cartesian(x, y, rot, 0);
	    		} else {
	    			
	    		}
	    		
	    		//Drive dependent on the POV
	    		while(joy1.getPOV()!=-1 && !joy1.getRawButton(1)) {
	    			int pov = joy1.getPOV();
	    			switch(pov) {
	    			case -1:
	    				break;
	    			case 0:
	    				mainDrive.mecanumDrive_Cartesian(0, -.75, 0, 0);
	    				break;
	    			case 45:
	    				mainDrive.mecanumDrive_Cartesian(-.75, -.75, 0, 0);
	    				break;
	    			case 90:
	    				mainDrive.mecanumDrive_Cartesian(-.75, 0, 0, 0);
	    				break;
	    			case 135:
	    				mainDrive.mecanumDrive_Cartesian(-.75, .75, 0, 0);
	    				break;
	    			case 180:
	    				mainDrive.mecanumDrive_Cartesian(0, .75, 0, 0);
	    				break;
	    			case 225:
	    				mainDrive.mecanumDrive_Cartesian(.75, .75, 0, 0);
	    				break;
	    			case 270:
	    				mainDrive.mecanumDrive_Cartesian(.75, 0, 0, 0);
	    				break;
	    			case 315:
	    				mainDrive.mecanumDrive_Cartesian(.75, -.75, 0, 0);
	    				break;
	    			}
	    			read();
	    		}
	    		
	    		//Brake
	    		while(joy1.getRawButton(1)) {
	    			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
	    			read();
	    		}
	    		
	    		//Reset sensors
	    		if(joy1.getRawButton(4)) {
	    			gyro.reset();
	    			enc.reset();
	    		}
	    		
	    		//Pressing button 3 gives you gear targeting
	    		boolean gear = false;
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
    			//Stop
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
    	//Read all sensors
    	SmartDashboard.putNumber("Center X", vision.centerGear());
		SmartDashboard.putNumber("Center Y", vision.GetContour1CenterY());
		SmartDashboard.putNumber("Gear", vision.centerGear());
		SmartDashboard.putNumber("Gyro angle", gyro.getAngle());
		SmartDashboard.putNumber("Encoder", enc.getDistance());
    }
    
    //Drive straight
    private void DefaultAuton() {
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
    		//Auton code
    	}
    }
    
    private void BlueAuton() {
    	timer.reset();
    	gyro.reset();
    	while(isAutonomous() && isEnabled()) {
    		//Auton code
    	}
    }
    
}