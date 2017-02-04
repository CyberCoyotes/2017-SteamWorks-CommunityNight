/****************************************
 * 
 *	STEAMWORKS
 *	@author CyberCoyotes
 *
 ****************************************/
package org.usfirst.frc.team3603.robot;

import com.ctre.CANTalon;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	static final Value out = DoubleSolenoid.Value.kForward;
	static final Value in = DoubleSolenoid.Value.kReverse;
	static final edu.wpi.first.wpilibj.Relay.Value on = Relay.Value.kForward;
	static final edu.wpi.first.wpilibj.Relay.Value off = Relay.Value.kOff;
	double intakeSpeed = 0.6;
	double shooterSpeed = 0.9;
	double climbSpeed = -0.5;
	double slowModeTurnSpeed = 0.25;//Change this number to change turing speed for slow mode
	double regularModeTurnSpeed = 0.5;//Change this number to change turing speed for regular mode
	
	//Auton code
	final String defaultAuto = "Default";
	final String redAuton = "redAuton";
	final String blueAuton = "blueAuton";
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	
	//Controllers
	Joystick joy1 = new Joystick(0);
	Joystick joy2 = new Joystick(1);
	
	// Drive Talons
	CANTalon frontLeft = new CANTalon(1);//
	CANTalon frontRight = new CANTalon(2);//
    CANTalon backLeft = new CANTalon(3);//
    CANTalon backRight = new CANTalon(4);//
    RobotDrive mainDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
    
    // Shooter and ball feeder
    Victor shooter = new Victor(0);
    Victor intake = new Victor(1);//
    Victor climb = new Victor(2);
    Relay spike = new Relay(0);
    
    //Sensors
	//ADXRS450_Gyro gyro = new ADXRS450_Gyro();
	//ADXL362 accel = new ADXL362(Range.k8G);
	Timer timer = new Timer();
	Timer a = new Timer();
    
	//Solenoids
    DoubleSolenoid blocker = new DoubleSolenoid(7, 0);//
    DoubleSolenoid gearA = new DoubleSolenoid(1, 6);//
    DoubleSolenoid gearB =new DoubleSolenoid(2, 5);//
    
    Compressor compressor = new Compressor(0);
    CameraServer camera = CameraServer.getInstance();
    
    //Drive stuff
    public double x;
	public double y;
	public double rot;
	boolean speedUp = true;
	boolean vac = false;
	
	int front = 0;
	boolean f = true;
	
	boolean light = false;
	
	boolean shoot = false;
	
    //Vision stuff
    //Vision2017 vision = new Vision2017(0);
    
	public void robotInit() {
		frontLeft.setInverted(true);
		backLeft.setInverted(true);
		
    	chooser.addDefault("Default Auto", defaultAuto);
		chooser.addObject("Red Autonomous Code", redAuton);
		chooser.addObject("Blue Autonomous Code", blueAuton);
		SmartDashboard.putData("Auton choices", chooser);
		compressor.start();
		camera.startAutomaticCapture("cam0", 0);
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
    			//Brake
	    		while(joy1.getRawButton(1)) {
	    			mainDrive.mecanumDrive_Cartesian(0, 0, 0, front);
	    			read();
	    		}
	    		
	    		//Light code
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
	    		} else if (!light && !shoot){
	    			spike.set(off);
	    		}
	    		
    			//Changing the front
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
	    		if(joy1.getRawButton(7)) {
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
	    			if(joy1.getRawButton(2)) {
	    				a = 0.5;
	    			} else {
	    				a = 1;
	    			}
	    			if(pov >= 45 && pov <= 135) {
	    				mainDrive.mecanumDrive_Cartesian(-0.5*a, 0, 0, front);
	    			} 
	    			if(pov >= 225 && pov <= 305) {
	    				mainDrive.mecanumDrive_Cartesian(0.5*a, 0, 0, front);
	    			}
	    			read();
	    		}
	    		
	    		/************************
	    		 * MANIPULATOR CONTROLS *
	    		 ************************/
	    		//
	    		//Shooter code
	    		if(joy2.getRawButton(3) && !shoot) {
	    			shoot = true;
	    			while(joy2.getRawButton(3)) {}
	    		}
	    		if(joy2.getRawButton(3) && shoot) {
	    			shoot = false;
	    			while(joy2.getRawButton(3)) {}
	    		}
	    		if(shoot) {
	    			spike.set(on);
	    			shooter.set(shooterSpeed);
	    			blocker.set(in);
	    		} else {
	    			shooter.set(0);
	    			blocker.set(out);
	    		}
	    		
	    		//Ball picker system
	    		if(joy2.getRawButton(2) && !vac) {
	    			vac = true;
	    			while(joy2.getRawButton(2)) {}
	    		}
	    		if(joy2.getRawButton(2) && vac) {
	    			vac = false;
	    			while(joy2.getRawButton(2)) {}
	    		}
	    		if(vac) {
	    			intake.set(intakeSpeed);
	    		} else {
	    			intake.set(0);
	    		}
	    		
	    		//Drop gear
    			if(joy2.getRawButton(1)) {
    				gearA.set(in);
    				gearB.set(in);
    			} else {
    				gearA.set(out);
    				gearB.set(out);
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
    	double aSpeed = (frontRight.getSpeed() + frontLeft.getSpeed() + backRight.getSpeed() + backLeft.getSpeed())/4;
    	SmartDashboard.putBoolean("Intake red=off green=on", vac);
    	SmartDashboard.putNumber("Speed", aSpeed);
    }
    
    
    //Drive straight
     private void DefaultAuton() {
    	timer.reset();
    	//gyro.reset();
    	while(isAutonomous() && isEnabled() && timer.get() < 6.0) {
    		//mainDrive.mecanumDrive_Cartesian(0, -0.2, 0.2, gyro.getAngle());
    		read();
    	}
	}

    private void RedAuton() {
    	timer.reset();
    	//gyro.reset();
    	while(isAutonomous() && isEnabled()) {
    		//Auton code
    	}
    }
    
    private void BlueAuton() {
    	timer.reset();
    	//gyro.reset();
    	while(isAutonomous() && isEnabled()) {
    		//Auton code
    	}
    }
}
