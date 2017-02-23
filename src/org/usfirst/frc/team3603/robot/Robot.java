/****************************************
 * 
 *	STEAMWORKS
 *	@author CyberCoyotes
 *
 ****************************************/
package org.usfirst.frc.team3603.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
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
    Victor arm = new Victor(1);//Gear picker arm
    Victor climb = new Victor(2);//Climbing motor
    Relay spike = new Relay(0);//Spoting light
    
    //Sensors
	ADXRS450_Gyro gyro = new ADXRS450_Gyro();//Gyroscope
	//ADXL362 accel = new ADXL362(Range.k8G);//Accelerometer
	Timer timer = new Timer();//Timer
	MyEncoder fle = new MyEncoder(1);//Front left encoder
	Encoder enc = new Encoder(7, 6, true, EncodingType.k4X);
	PressureSensor pres = new PressureSensor(0);
	Vision vision = new Vision();
	
	//Solenoids
    DoubleSolenoid blocker = new DoubleSolenoid(7, 0);//Shooter solenoid
    DoubleSolenoid gearA = new DoubleSolenoid(1, 6);//One side of the gear mechanism
    DoubleSolenoid gearB =new DoubleSolenoid(2, 5);//Other side of gear mechanism
    DoubleSolenoid gear = new DoubleSolenoid(3, 4);
    Compressor compressor = new Compressor(0);//Air compressor
    
    //Vision
    CameraServer camera = CameraServer.getInstance();//Smartdashboard camera
    
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
	boolean armBool = true; //True means up
	boolean grab = true; //True means closed/activated
	double angle = 0;
	
	//Y makes the thing go up -TOGGLE -default up
	//X opens and closes the thingy -TOGGLE -default closed
	//Button 3 does gear stuff
	
	public void robotInit() {
		frontLeft.setInverted(true);//Invert the left motors
		backLeft.setInverted(true);
		gyro.calibrate();//Callibrate the gyroscope
		fle.reset();//Callibrate encoder
		enc.setDistancePerPulse(1.0/(497.0*360.0));
		enc.setSamplesToAverage(7);
		
    	chooser.addDefault("Default Auto", defaultAuto);//Add the autons to the smart dashboard
		chooser.addObject("Red Autonomous Code", redAuton);
		chooser.addObject("Blue Autonomous Code", blueAuton);
		chooser.addObject("Middle gear autonomous code", straight);
		SmartDashboard.putData("Autons choices", chooser);
		
		compressor.start();//Start the compressor
		camera.startAutomaticCapture("cam0", 0);//Start the camera
    }
    
	public void autonomousInit() {
		autoSelected = chooser.getSelected();//Select the auton
    }
    public void autonomousPeriodic() {
    	timer.reset();
    	timer.start();
    	if(isAutonomous() && isEnabled() && timer.get() <= 15 && !done) {
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
	    		while(joy1.getRawButton(2)) {
	    			mainDrive.mecanumDrive_Cartesian(0, 0, 0, front);
	    			read();//Contunue reading from sensors
	    		}
	    		
	    		//Drop gear
    			if(joy1.getRawButton(3)) {
    				gearA.set(in);//Open gear pistons
    				gearB.set(in);
    			} else {
    				gearA.set(out);//Close gear pistons
    				gearB.set(out);
    			}
	    		
	    		//Toggle the light on/off with a boolean
	    		if(joy1.getRawButton(5)) {
	    			light = (boolean) light ? false : true;
	    			while(joy1.getRawButton(5)) {}
	    		}
	    		if(light || shoot) {
	    			spike.set(on);
	    			reader = true;
	    		} else {
	    			spike.set(off);
	    			reader = false;
	    		}
	    		
    			//Changing the front with a boolean
    			if(joy1.getRawButton(4)) {
	    			f = (boolean) f ? false : true;
	    			while(joy1.getRawButton(4)) {}
	    		}
	    		if(f) {
	    			front = 180;
	    		} else {
	    			front = 0;
	    		}
	    		
	    		//Climbing code
	    		if(joy1.getRawButton(6)) {//press button 6
    				climb.set(climbSpeed);
    			} else {
    				climb.set(0);
    			}
	    		
	    		//Gear adjustment code
	    		if(joy1.getRawButton(12)) {
	    			mainDrive.mecanumDrive_Cartesian(0, 0, vision.getAdjustmentSpeed(), 0);
	    		}
	    		
	    		//Drive code
	    		if(joy1.getRawButton(1)) {
		    		x = Math.pow(joy1.getRawAxis(0), 3)/2;
		    		y = Math.pow(joy1.getRawAxis(1), 3)/2;
		    		rot = -Math.pow(joy1.getRawAxis(2), 3)/2;
	    		} else {
	    			x = Math.pow(joy1.getRawAxis(0), 3);
		    		y = Math.pow(joy1.getRawAxis(1), 3);
		    		rot = -Math.pow(joy1.getRawAxis(2), 3)/2;
	    		}
	    		if((x > 0.25 || x < -0.25 || y > 0.25 || y < -0.25) && joy1.getRawButton(1)) {
	    			rot = -Math.pow(joy1.getRawAxis(2), 3)/4;
	    		} else if((x > 0.5 || x < -0.5 || y > 0.5 || y < -0.5) && !joy1.getRawButton(1)) {
	    			rot = -Math.pow(joy1.getRawAxis(2), 3)/4;
	    		}
	    		if((Math.abs(x)>=0.1 || Math.abs(y)>=0.1 || Math.abs(rot)>=0.1)) {
	    			mainDrive.mecanumDrive_Cartesian(x, y, rot, front);
	    		}
	    		
	    		//POV side-to-side
	    		while(joy1.getPOV()!=-1 && !joy1.getRawButton(2)) {
	    			int pov = joy1.getPOV();
	    			double a = 1;
	    			if(joy1.getRawButton(1)) {//Half speeds
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
	    		if(joy2.getRawButton(1)) {
	    			shooter.set(shooterSpeed);//Turn on motor
	    			blocker.set(in);//unblock
	    		} else {
	    			shooter.set(0);//Turn off motor
	    			blocker.set(out);//continue blocking
	    		}
    			
    			//Gear placer pneumatic
    			if(joy2.getRawButton(3)) {
    				grab = (boolean) grab ? false : true;
    				while(joy2.getRawButton(3)) {}
    			}
    			if(grab) {
    				gear.set(out);
    			}
    			if(!grab) {
    				gear.set(in);
    			}
    			
    			/*
    			if(joy2.getRawAxis(5) > 0.1 || joy2.getRawAxis(5) < -0.1) {
    				arm.set(joy2.getRawAxis(5));
    				angle = enc.getDistance();
    			} else {
    				if(enc.getDistance() > angle+10) {
    					arm.set(-0.2);
    				}
    				if(enc.getDistance() < angle-10) {
    					arm.set(0.2);
    				}
    			}
    			if(joy2.getRawButton(5)) {
    				angle = enc.getDistance();
    			}
    			*/
    			
    			if(joy2.getRawButton(4)) {
    				arm.set(0.3);
    			}
    			if(joy2.getRawButton(2)) {
    				arm.set(-0.3);
    			}
    			if(!joy2.getRawButton(2) && !joy2.getRawButton(4)) {
    				arm.set(0);
    			}
	    		
    		} else {
    			//Stop driving if nothing is being read from the controllers
    			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
    		}
    		read();
    	}
    	while(!isEnabled()) {
    		enc.reset();
    		arm.set(0);
    	}
    }
    public void testPeriodic() {
    }
    
    void read() {//Read from the sensors
    	SmartDashboard.putBoolean("Front", f);//Tell which side is front
    	SmartDashboard.putBoolean("Light", reader);//Tell if the light is on
    	SmartDashboard.putNumber("Pressure Sensor", pres.getPres());
    	SmartDashboard.putNumber("Distance travelled", fle.getDistance());
    	SmartDashboard.putNumber("Speed", fle.getRate());
    	SmartDashboard.putNumber("Arm Angle", enc.get());
    	SmartDashboard.putNumber("Vision Testing Center X", vision.getAdjustmentSpeed());
    	SmartDashboard.putNumber("X Magnitude", x);
    	SmartDashboard.putNumber("Y Magnitude", y);
    	SmartDashboard.putNumber("Rotation Magnitude", rot);
    	if(pres.getPres()<20) {
    		SmartDashboard.putBoolean("Usable pressure", false);
    	} else {
    		SmartDashboard.putBoolean("Usable pressure", true);
    	}
    }
    
	private void BlueAuton() { //The turn left one
		//Drive forwards 93 inches
		while(fle.getDistance()<90 && timer.get() <= 15) {
			mainDrive.mecanumDrive_Cartesian(0, 0.5, 0, gyro.getAngle());//Drive forwards
			read();//Read from sensors
			gearA.set(out);//Set the gear pistons
			gearB.set(out);
		}
		//Turn -60 degrees
		while(gyro.getAngle() > -60 && timer.get() <= 15) {
			mainDrive.mecanumDrive_Cartesian(0, 0, -0.4, 0);
			read();
		}
		//Drive while locked on to the gear targets
		while(timer.get()<=15 && (vision.getAdjustmentSpeed() > 0.05 || vision.getAdjustmentSpeed() < -0.05)) {
			mainDrive.mecanumDrive_Cartesian(0, 0, vision.getAdjustmentSpeed(), 0);
			read();
		}
		fle.reset();
		while(timer.get() <= 15 && fle.getDistance() < 12) {
			mainDrive.mecanumDrive_Cartesian(0, 0.3, 0, 0);
		}
		double time = timer.get();//Take 0.2 seconds to open gear
		while(timer.get()-time<0.2 && timer.get() <=15) {
			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
			gearA.set(in);
			gearB.set(in);
			read();
		}
		fle.reset();//Drive backwards
		while(fle.getDistance()>=12 && timer.get() <= 15) {
			mainDrive.mecanumDrive_Cartesian(0, -0.3, 0, 0);
			read();
		}
		gearA.set(out);//Close gears
		gearB.set(out);
		mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		done = true;
	}
	
	
	
	
	
	
	
	
	private void RedAuton() { //The turn right one
		//Drive forwards 93 inches
				while(fle.getDistance()<90 && timer.get() <= 15) {
					mainDrive.mecanumDrive_Cartesian(0, 0.9, 0, gyro.getAngle());//Drive forwards
					read();//Read from sensors
					gearA.set(out);//Set the gear pistons
					gearB.set(out);
				}
				//Turn 60 degrees
				while(gyro.getAngle() < 60 && timer.get() <= 15) {
					mainDrive.mecanumDrive_Cartesian(0, 0, 0.5, 0);
					read();
				}
				//Drive while locked on to the gear targets
				while(timer.get()<=15 && (vision.getAdjustmentSpeed() >0.05 || vision.getAdjustmentSpeed() < -0.05)) {
					mainDrive.mecanumDrive_Cartesian(0, 0, vision.getAdjustmentSpeed(), 0);
					read();
				}
				fle.reset();
				while(timer.get() <= 15 && fle.getDistance() < 8) {
					mainDrive.mecanumDrive_Cartesian(0, 0.5, 0, 0);
				}
				double time = timer.get();//Take 0.2 seconds to open gear
				while(timer.get()-time<0.2 && timer.get() <=15) {
					mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
					gearA.set(in);
					gearB.set(in);
					read();
				}
				fle.reset();//Drive backwards
				while(fle.getDistance()>=8 && timer.get() <= 15) {
					mainDrive.mecanumDrive_Cartesian(0, -0.4, 0, 0);
					read();
				}
				gearA.set(out);//Close gears
				gearB.set(out);
				mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
				done = true;
	}
	
	
	
	
	
	
	
	
	private void straightGear() {
		fle.reset();
		while(fle.getDistance()<=70 && timer.get() <= 15 && !done) {
			mainDrive.mecanumDrive_Cartesian(0, 0.4, 0, gyro.getAngle());
			read();
			gearA.set(out);
			gearB.set(out);
		}
		fle.reset();
		while(timer.get() <= 15 && !done) {
			if(vision.getAdjustmentSpeed()!=-2) {
				mainDrive.mecanumDrive_Cartesian(0, 0, vision.getAdjustmentSpeed(), 0);
			} else {
				mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
			}
			read();
		}
		double time = timer.get();
		while(timer.get()-time<0.2 && timer.get() <=15 && !done) {
			gearA.set(in);
			gearB.set(in);
		}
		fle.reset();
		while(fle.getDistance()>=-8 && timer.get() <= 15 && !done) {
			mainDrive.mecanumDrive_Cartesian(0, -0.4, 0, 0);
			read();
		}
		fle.reset();
		gearA.set(out);
		gearB.set(out);
		mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		done = true;
	}
	
	
	
	
	
	
	
	
	private void DefaultAuto() {
		while(timer.get() <= 15 && fle.getDistance() < 100) {
			mainDrive.mecanumDrive_Cartesian(0, 0.75, 0, gyro.getAngle());
			read();
			gearA.set(out);
			gearB.set(out);
		}
		mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		done = true;
	}
}

