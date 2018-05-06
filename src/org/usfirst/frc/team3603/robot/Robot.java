/****************************************
 * 
 *	2017 STEAMWORKS
 *	@author CyberCoyotes
 *
 ****************************************/
package org.usfirst.frc.team3603.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.drive.MecanumDrive;

public class Robot extends IterativeRobot {
	static final Value out = DoubleSolenoid.Value.kForward;
	static final Value in = DoubleSolenoid.Value.kReverse;
	
	//Controllers
	Joystick joy1 = new Joystick(0);//Big Logitech joystick
	Joystick joy2 = new Joystick(1);//Afterglow XBOX controller
	GenericHID.RumbleType leftRumble = GenericHID.RumbleType.kLeftRumble;
	GenericHID.RumbleType rightRumble = GenericHID.RumbleType.kRightRumble;
	// Drive Talons
	WPI_TalonSRX frontLeft = new WPI_TalonSRX(1);
	WPI_TalonSRX frontRight = new WPI_TalonSRX(2);
	WPI_TalonSRX backLeft = new WPI_TalonSRX(3);
	WPI_TalonSRX backRight = new WPI_TalonSRX(4);
	MecanumDrive mainDrive = new MecanumDrive(frontLeft, backLeft, frontRight, backRight);
    
    // Shooter and ball feeder
    Victor arm = new Victor(2);	// Gear picker arm
    Victor climb = new Victor(1);//Climbing motor
    
	//Solenoids
    DoubleSolenoid gearA = new DoubleSolenoid(1, 6);	//One side of the gear mechanism
    DoubleSolenoid gearB =new DoubleSolenoid(2, 5);		//Other side of gear mechanism
    DoubleSolenoid gear = new DoubleSolenoid(3, 4);		//The floor gear lifter piston
    Compressor compressor = new Compressor();			//Air compressor
    
    //Vision
    CameraServer camera = CameraServer.getInstance();	//SmartDashboard camera
    
    //Drive stuff-don't touch
	public double sens; // Joystick sensitivity slider
	
	public void robotInit() {
		mainDrive.setSafetyEnabled(false);
		compressor.start();							//Start the compressor
		climb.setInverted(true);
    }
    
	public void autonomousInit() {
    }
    public void autonomousPeriodic() {
    }
    
	public void teleopPeriodic() {
		/***********************
    	 *** DRIVER CONTROLS ***
    	 ***********************/
		
		
		sens = -joy1.getRawAxis(3)/2+0.5; // Joystick sensitivity slider
		
		if(!joy1.getRawButton(1) && Math.abs(joy1.getRawAxis(0)) >= 0.15 || Math.abs(joy1.getRawAxis(1)) >= 0.15 || Math.abs(joy1.getRawAxis(2)) >= 0.15) {
			mainDrive.driveCartesian(joy1.getRawAxis(0) * sens, -joy1.getRawAxis(1) * sens, joy1.getRawAxis(2) * sens /2);
		} else {
			mainDrive.driveCartesian(0, 0, 0);
		}
		
		
		/************************
		 * MANIPULATOR CONTROLS *
		 ************************/

		//Drop gear
		if(joy2.getRawButton(5)) {//Pressing and holding button three on the big joystick opens the gear hatch while not holding button three closes it
			gearA.set(in);	//Open gear pistons
			gearB.set(in);
		} else {
			gearA.set(out);	//Close gear pistons
			gearB.set(out);
		}
		
		if(joy2.getRawButton(6)) {
			gear.set(out);
		} else {
			gear.set(in);
		}
		
		if(Math.abs(joy2.getRawAxis(5)) >= 0.2) {
			arm.set(joy2.getRawAxis(5));
		} else {
			arm.set(0);
		}
		
		//Climbing code
		if(Math.abs(joy2.getRawAxis(1))>=0.2) {
			climb.set(-Math.abs(joy2.getRawAxis(1)));
			joy2.setRumble(leftRumble, Math.abs(joy2.getRawAxis(5)));
			joy2.setRumble(rightRumble, Math.abs(joy2.getRawAxis(5)));
		} else {
			climb.set(0);
			joy2.setRumble(leftRumble, 0);
			joy2.setRumble(rightRumble, 0);
		}
    }
}