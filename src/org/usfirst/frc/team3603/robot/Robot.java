/****************************************
 * 
 *	STEAMWORKS
 *	@author CyberCoyotes
 *
 ****************************************/
package org.usfirst.frc.team3603.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	//These are values used throughout the code
	//You may change the speed of the things marked double
	static final Value out = DoubleSolenoid.Value.kForward;
	static final Value in = DoubleSolenoid.Value.kReverse;
	static final edu.wpi.first.wpilibj.Relay.Value on = Relay.Value.kForward;
	static final edu.wpi.first.wpilibj.Relay.Value off = Relay.Value.kOff;
	double shooterSpeed = 0.9;
	double climbSpeed = -0.5;//This MUST be negative
	
	//Auton code
	final String defaultAuto = "Default";//For a standard auton
	final String redAuton = "redAuton";//For auton on the red team
	final String blueAuton = "blueAuton";//For auton on the blue team
	final String straight = "straightAuton";
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	
	//Controllers
	Joystick joy1 = new Joystick(0);//Big joystick
	Joystick joy2 = new Joystick(1);//Afterglow xbox controller
	
	// Drive Talons
	CANTalon frontLeft = new CANTalon(1);
	CANTalon frontRight = new CANTalon(2);
    CANTalon backLeft = new CANTalon(3);
    CANTalon backRight = new CANTalon(4);
    RobotDrive mainDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
    
    // Shooter and ball feeder
    Victor shooter = new Victor(0);//Shooter motor
    Victor climb = new Victor(2);//Climbing motor
    Relay spike = new Relay(0);//Spoting light
    
    //Sensors
	ADXRS450_Gyro gyro = new ADXRS450_Gyro();//Gyroscope
	//ADXL362 accel = new ADXL362(Range.k8G);//Accelerometer
	Timer timer = new Timer();//Timer
	Timer s = new Timer();//Special timer -don't touch
	Encoder fle = new Encoder(1);
	PressureSensor pres = new PressureSensor(0);
	Vision vision = new Vision();
	
	//Solenoids
    DoubleSolenoid blocker = new DoubleSolenoid(7, 0);//Shooter solenoid
    DoubleSolenoid gearA = new DoubleSolenoid(1, 6);//One side of the gear mechanism
    DoubleSolenoid gearB =new DoubleSolenoid(2, 5);//Other side of gear mechanism
    Compressor compressor = new Compressor(0);//Air compressor
    
    //Vision
    //CameraServer camera = CameraServer.getInstance();//Smartdashboard camera
    
    //Drive stuff-don't touch
    public double x;
	public double y;
	public double rot;
	
	//Toggles
	int front = 0;//Angle for the front- 0 is gear side, 180 is shooter side
	boolean f = true;//Front toggle boolean
	boolean light = false;//Spike toggle boolean
	boolean shoot = false;//Shooter toggle boolean
	boolean reader = false;
	public boolean done = false;
	
	public void robotInit() {
		frontLeft.setInverted(true);//Invert the left motors
		backLeft.setInverted(true);
		gyro.calibrate();//Callibrate the gyroscope
		fle.callibrate();//Callibrate encoder
		
    	chooser.addDefault("Default Auto", defaultAuto);//Add the autons to the smart dashboard
		chooser.addObject("Red Autonomous Code", redAuton);
		chooser.addObject("Blue Autonomous Code", blueAuton);
		chooser.addObject("Middle gear autonomous code", straight);
		SmartDashboard.putData("Autons choices", chooser);
		
		compressor.start();//Start the compressor
		//camera.startAutomaticCapture("cam0", 0);//Start the camera
		s.start();//Special timer
    }
    
	public void autonomousInit() {
		autoSelected = chooser.getSelected();//Select the auton
    }
    public void autonomousPeriodic() {
    	timer.reset();
    	timer.start();
    	if(isAutonomous() && isEnabled() && timer.get() <= 15 && done==false) {
	    	switch(autoSelected) {
	    	case defaultAuto:
	    		DefaultAuto();
	    		break;
	    	case redAuton:
	    		RedAuton();
	    		break;
	    	case blueAuton:
	    		BlueAuton();
	    		break;
	    	case straight:
	    		straightGear();
	    	}
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
    			//Brake/
	    		while(joy1.getRawButton(1)) {
	    			mainDrive.mecanumDrive_Cartesian(0, 0, 0, front);
	    			read();//Contunue reading from sensors
	    		}
	    		
	    		
	    		//Toggle the light on/off with a boolean
	    		if(joy1.getRawButton(3) && !light) {
	    			light = true;
	    			while(joy1.getRawButton(3)) {}
	    		}
	    		if(joy1.getRawButton(3) && light) {
	    			light = false;
	    			while(joy1.getRawButton(3)) {}
	    		}
	    		if(light || shoot) {
	    			spike.set(on);
	    			reader = true;
	    		} else {
	    			spike.set(off);
	    			reader = false;
	    		}
	    		
    			//Changing the front with a boolean
    			if(joy1.getRawButton(4) && !f) {
	    			f = true;
	    			while(joy1.getRawButton(4)) {}
	    		}
	    		if(joy1.getRawButton(4) && f) {
	    			f = false;
	    			while(joy1.getRawButton(4)) {}
	    		}
	    		if(f) {
	    			front = 180;
	    		} else {
	    			front = 0;
	    		}
	    		
	    		//Climbing code
	    		if(joy1.getRawButton(7)) {//press button 7
    				climb.set(climbSpeed);
    			} else {
    				climb.set(0);
    			}
	    		
	    		//Pressing button 2 gives you half speeds
	    		x = Math.pow(joy1.getRawAxis(0), 3);
	    		y = Math.pow(joy1.getRawAxis(1), 3);
	    		rot = -Math.pow(joy1.getRawAxis(2), 3)/2;
	    		
	    		//Drive w/ joystick
	    		if((Math.abs(x)>=0.1 || Math.abs(y)>=0.1 || Math.abs(rot)>=0.1) && joy1.getRawButton(2)) {
	    			mainDrive.mecanumDrive_Cartesian(x/2, y/2, rot/2, front);
	    		} else if(Math.abs(x)>=0.1 || Math.abs(y)>=0.1 || Math.abs(rot)>=0.1) {
	    			mainDrive.mecanumDrive_Cartesian(x, y, rot, front);
	    		}
	    		
	    		//POV side-to-side
	    		while(joy1.getPOV()!=-1 && !joy1.getRawButton(1)) {
	    			int pov = joy1.getPOV();
	    			double a = 1;
	    			if(joy1.getRawButton(2)) {//Half speeds
	    				a = 0.5;
	    			} else {
	    				a = 1;
	    			}
	    			if(pov >= 45 && pov <= 135) {
	    				mainDrive.mecanumDrive_Cartesian(0.5*a, 0, 0, front);
	    			} 
	    			if(pov >= 225 && pov <= 305) {
	    				mainDrive.mecanumDrive_Cartesian(-0.5*a, 0, 0, front);
	    			}
	    			read();
	    		}
	    		
	    		/************************
	    		 * MANIPULATOR CONTROLS *
	    		 ************************/
	    		/*
	    		//Shooter code
	    		if(joy2.getRawButton(1) && !shoot) {
	    			shoot = true;
	    			while(joy2.getRawButton(1)) {}
	    		}
	    		if(joy2.getRawButton(1) && shoot) {
	    			shoot = false;
	    			while(joy2.getRawButton(1)) {}
	    		}
	    		if(shoot) {
	    			if(s.get() > 6) {
	    				s.reset();
	    				s.start();
	    			} else if(s.get() <= 2) {
	    				shooter.set(shooterSpeed);
	    				blocker.set(out);
	    			} else if(s.get() > 2 && s.get() <= 4) {
	    				shooter.set(shooterSpeed);
	    				blocker.set(in);
	    			} else if(s.get() > 4 && s.get() <= 6) {
	    				shooter.set(shooterSpeed);
	    				blocker.set(out);
	    			}
	    		} else {
	    			shooter.set(0);
	    			blocker.set(out);
	    		}
	    		*/
	    		
	    		if(joy2.getRawButton(1)) {
	    			shooter.set(shooterSpeed);//Turn on motor
	    			blocker.set(in);//unblock
	    		} else {
	    			shooter.set(0);//Turn off motor
	    			blocker.set(out);//continue blocking
	    		}
	    		
	    		//Drop gear
    			if(joy2.getRawButton(2)) {
    				gearA.set(in);//Open gear pistons
    				gearB.set(in);
    			} else {
    				gearA.set(out);//Close gear pistons
    				gearB.set(out);
    			}
    			
    			if(joy1.getRawButton(12) && vision.getCenterX()!=-2) {
    				mainDrive.mecanumDrive_Cartesian(0, 0.3, vision.getCenterX()/2, 0);
    			}
	    		
    		} else {
    			//Stop driving if nothing is being read from the controllers
    			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
    		}
    		read();
    	}
    }
    public void testPeriodic() {
    }
    
    void read() {//Read from the sensors
    	SmartDashboard.putBoolean("Front", f);//Tell which side is front
    	SmartDashboard.putString(" ", "Green is for gear side");
    	SmartDashboard.putString("  ", "Red is for shooter side");
    	SmartDashboard.putBoolean("Shooter", shoot);//Tell if shooter is on
    	SmartDashboard.putBoolean("Light", reader);//Tell if the light is on
    	SmartDashboard.putNumber("Pressure Sensor", pres.getPres());
    	SmartDashboard.putNumber("Length", fle.getDistance());
    	SmartDashboard.putNumber("Visiony", vision.getRawCenterX());
    	if(pres.getPres()<20) {
    		SmartDashboard.putBoolean("Usable pressure", false);
    	} else {
    		SmartDashboard.putBoolean("Usable pressure", true);
    	}
    }

    /**
     * The instances of [timer.get() <= 15] are for safety
     */
    
	private void BlueAuton() {
		//Drive forwards 93 inches
		while(fle.getDistance()<93 && timer.get() <= 15) {
			mainDrive.mecanumDrive_Cartesian(0, 0.9, 0, gyro.getAngle());//Drive forwards
			read();//Read from sensors
			gearA.set(out);//Set the gear pistons
			gearB.set(out);
		}
		//Turn -60 degrees
		while(gyro.getAngle() > -60 && timer.get() <= 15) {
			mainDrive.mecanumDrive_Cartesian(0, 0, -0.75, 0);
			read();
		}
		fle.callibrate();//Reset encoder
		//Drive while locked on to the gear targets
		while(fle.getDistance()<=8 && timer.get() <= 15) {
			if(vision.getCenterX()!=-2) {
				mainDrive.mecanumDrive_Cartesian(0, 0.4, vision.getCenterX(), 0);
				read();
			} else {
				mainDrive.mecanumDrive_Cartesian(0, 0.4, 0, 0);
			}
		}
		double time = timer.get();//Take 0.2 seconds to open gear
		while(timer.get()-time<0.2 && timer.get() <=15) {
			gearA.set(in);
			gearB.set(in);
		}
		fle.callibrate();//Drive backwards
		while(fle.getDistance()>=8 && timer.get() <= 15) {
			mainDrive.mecanumDrive_Cartesian(0, -4, 0, 0);
			read();
		}
		gearA.set(out);//Close gears
		gearB.set(out);
		mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		done = true;
	}
	
	
	
	
	
	
	
	
	private void RedAuton() {
		while(fle.getDistance()<93 && timer.get() <= 15) {
			mainDrive.mecanumDrive_Cartesian(0, 0.9, 0, gyro.getAngle());
			read();
			gearA.set(out);
			gearB.set(out);
		}
		while(gyro.getAngle() < 60 && timer.get() <= 15) {
			mainDrive.mecanumDrive_Cartesian(0, 0, 0.75, 0);
			read();
		}
		fle.callibrate();
		while(fle.getDistance()<=8 && timer.get() <= 15) {
			if(vision.getCenterX()!=-2) {
				mainDrive.mecanumDrive_Cartesian(0, 0.4, vision.getCenterX(), 0);
				read();
			} else {
				mainDrive.mecanumDrive_Cartesian(0, 0.4, 0, 0);
			}
		}
		double time = timer.get();
		while(timer.get()-time<0.2 && timer.get() <=15) {
			gearA.set(in);
			gearB.set(in);
		}
		fle.callibrate();
		while(fle.getDistance()>=8 && timer.get() <= 15) {
			mainDrive.mecanumDrive_Cartesian(0, -4, 0, 0);
			read();
		}
		gearA.set(out);
		gearB.set(out);
		mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		done = true;
	}
	
	
	
	
	
	
	
	
	private void straightGear() {
		fle.callibrate();
		while(fle.getDistance()<=70 && timer.get() <= 15 && !done) {
			mainDrive.mecanumDrive_Cartesian(0, 0.4, 0, gyro.getAngle());
			read();
			gearA.set(out);
			gearB.set(out);
		}
		fle.callibrate();
		while(timer.get() <= 15 && !done) {
			if(vision.getCenterX()!=-2) {
				mainDrive.mecanumDrive_Cartesian(0, 0, vision.getCenterX()/1.5, 0);
				read();
			} else {
				mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
			}
			timer.reset();
		}//
		double time = timer.get();
		while(timer.get()-time<0.2 && timer.get() <=15 && !done) {
			gearA.set(in);
			gearB.set(in);
		}
		fle.callibrate();
		while(fle.getDistance()>=-8 && timer.get() <= 15 && !done) {
			mainDrive.mecanumDrive_Cartesian(0, -0.4, 0, 0);
			read();
		}
		fle.callibrate();
		gearA.set(out);
		gearB.set(out);
		mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		done = true;
	}
	
	
	
	
	
	
	
	
	private void DefaultAuto() {
		while(timer.get() <= 15 && fle.getDistance() < 100) {
			mainDrive.mecanumDrive_Cartesian(0, 0.5, 0, gyro.getAngle());
			read();
			gearA.set(out);
			gearB.set(out);
		}
		mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		done = true;
	}
}

