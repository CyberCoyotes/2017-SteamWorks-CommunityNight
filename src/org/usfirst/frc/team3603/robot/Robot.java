/****************************************
 * 
 *	2017 STEAMWORKS
 *	@author CyberCoyotes
 *
 ****************************************/
package org.usfirst.frc.team3603.robot;

import com.ctre.CANTalon;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.SerialPort.Port;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	//These are values used throughout the code
	//You may change the speed of the things marked double
	int step;
	static final Value out = DoubleSolenoid.Value.kForward;
	static final Value in = DoubleSolenoid.Value.kReverse;
	static final edu.wpi.first.wpilibj.Relay.Value on = Relay.Value.kForward;
	static final edu.wpi.first.wpilibj.Relay.Value off = Relay.Value.kOff;
	double shooterSpeed = -1;//This speed the shooter must go 
	double mixerSpeed = 0.5;//The speed the window motor must go
	
	//Auton Names
	DriverStation ds = DriverStation.getInstance();
	DriverStation.Alliance team;
	
	//Controllers
	Joystick joy1 = new Joystick(0);//Big Logitech joystick
	Joystick joy2 = new Joystick(1);//Afterglow XBOX controller
	GenericHID.RumbleType leftRumble = GenericHID.RumbleType.kLeftRumble;
	GenericHID.RumbleType rightRumble = GenericHID.RumbleType.kRightRumble;
	// Drive Talons
	CANTalon frontLeft = new CANTalon(1);
	CANTalon frontRight = new CANTalon(2);
    CANTalon backLeft = new CANTalon(3);
    CANTalon backRight = new CANTalon(4);
    RobotDrive mainDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
    
    // Shooter and ball feeder
    Victor shooter = new Victor(3);//Shooter motor - changed from 0 to 9
    Victor arm = new Victor(2);	// Gear picker arm
    Victor climb = new Victor(1);//Climbing motor
    Victor mixer = new Victor(0);//Window motor
    Relay spike = new Relay(0);	//Spotting light
    
    //Sensors
	ADXRS450_Gyro gyro = new ADXRS450_Gyro();					//Gyroscope
	//ADXL362 accel = new ADXL362(Range.k8G);					//Accelerometer
	Timer timer = new Timer();									//Timer
	MyEncoder fle = new MyEncoder(frontLeft);							//Front left encoder; Add more for other 3 encoders if working properly
	PressureSensor pres = new PressureSensor(0);//Analog pressure sensor
	Vision vision;//Vision control object
	AHRS navx = new AHRS(SerialPort.Port.kMXP); 
	
	//Solenoids
    DoubleSolenoid gearA = new DoubleSolenoid(1, 6);	//One side of the gear mechanism
    DoubleSolenoid gearB =new DoubleSolenoid(2, 5);		//Other side of gear mechanism
    DoubleSolenoid gear = new DoubleSolenoid(3, 4);		//The floor gear lifter piston
    Compressor compressor = new Compressor(0);			//Air compressor
    
    //Vision
    CameraServer camera = CameraServer.getInstance();	//SmartDashboard camera
    
    //Drive stuff-don't touch
    public double x;//X magnitude for teleop
	public double y;//Y magnitude for teleop
	public double rot;//Rotational magnitude for teleop
	public double sens; // Joystick sensitivity slider
	public double turnSensitivity;
	
	//Toggles
	int front = 0;				//Angle for the front- 0 is gear side, 180 is shooter side 
	boolean f = true;			//Front toggle boolean
	boolean tSpeed = false;
	boolean light = false;		//Spike toggle boolean
	boolean shoot = false;		//Shooter toggle boolean
	boolean reader = false;		//Decides whether the spotting light should be on or off by combining if the light should be on because of the light button, or if it should be on because the robot is shooting
	boolean grab = true; 		//True means closed/activated
	
	public void robotInit() {
		frontLeft.setInverted(true);	//Invert the left motors
		backLeft.setInverted(true);
		gyro.calibrate();				//Calibrate the gyroscope
		fle.reset();					//Calibrate encoder
		
		compressor.start();							//Start the compressor
		camera.startAutomaticCapture("cam0", 0);	//Start the camera
		climb.setInverted(true);
		vision = new Vision();
    }
    
	public void autonomousInit() {
		team = ds.getAlliance();
		vision = new Vision();
		timer.reset();
		timer.start();
		gyro.reset();
		fle.reset();
		frontLeft.setEncPosition(0);
		step = 1;
    }
    public void autonomousPeriodic() {
    	read();
    	switch(team) {//Decide which auton to use
    	case Blue:
    		SmartDashboard.putString("Alliance", "Red");
    		RedAuton();//Use when on the red team
    		break;
    	case Red:
    		SmartDashboard.putString("Alliance", "Blue");
    		BlueAuton();//Use when on the blue team
    		break;
    	case Invalid:
    		SmartDashboard.putString("Alliance", "Defaulting to Red: Something went wrong and I have no idea what it is because this is completely new to me and I have no idea what is going to happen.");
    		RedAuton();
    		break;
    	}
    }
    
	public void teleopPeriodic() {
		//If nothing is being read by a controller, stop.
		if(!joy1.getRawButton(1) && (joy1.getPOV() != -1 || joy1.getRawButton(2) || joy1.getRawButton(3) || joy1.getRawButton(4) || joy1.getRawButton(5) || joy1.getRawButton(6) || joy1.getRawButton(7) || joy1.getRawButton(8) || joy1.getRawButton(9) || joy1.getRawButton(10) ||  joy2.getRawButton(1) || joy2.getRawButton(2) || joy2.getRawButton(3) || joy2.getRawButton(4) || joy2.getRawButton(5) || joy2.getRawButton(6) || joy2.getRawButton(7) || joy2.getRawButton(8) || joy2.getRawButton(9) || joy2.getRawButton(10) || joy1.getRawAxis(0) >= 0.05 || joy1.getRawAxis(1) >= 0.05 || joy1.getRawAxis(2) >= 0.05 || joy2.getRawAxis(0) >= 0.05 || joy2.getRawAxis(1) >= 0.05 || joy2.getRawAxis(2) >= 0.05 || joy2.getRawAxis(3) >= 0.05 || joy2.getRawAxis(4) >= 0.05 || joy2.getRawAxis(5) >= 0.05 || joy2.getRawAxis(6) >= 0.05 || joy1.getRawAxis(0) <= -0.05 || joy1.getRawAxis(1) <= -0.05 || joy1.getRawAxis(2) <= -0.05 || joy2.getRawAxis(0) <= -0.05 || joy2.getRawAxis(1) <= -0.05 || joy2.getRawAxis(2) <= -0.05 || joy2.getRawAxis(3) <= -0.05 || joy2.getRawAxis(4) <= -0.05 || joy2.getRawAxis(5) <= -0.05 || joy2.getRawAxis(6) <= -0.05)) {
			/***********************
    		 *** DRIVER CONTROLS ***
    		 ***********************/
			
    		//Drop gear
			if(joy1.getRawButton(3)) {//Pressing and holding button three on the big joystick opens the gear hatch while not holding button three closes it
				gearA.set(in);	//Open gear pistons
				gearB.set(in);
			} else {
				gearA.set(out);	//Close gear pistons
				gearB.set(out);
			}
    		
    		//Toggle the light on/off with a boolean
    		if(joy1.getRawButton(12)) {//Use button five on the big joystick
    			light = (boolean) light ? false : true;//If the light toggle boolean is true, make it false. If the light toggle boolean is false, make it true.
    			while(joy1.getRawButton(12)) {}
    		}
    		if(light || shoot) {//If Jade turned the light on with the toggle button or if the robot is shooting, turn on the light
    			spike.set(on);
    			reader = true;
    		} else {
    			spike.set(off);
    			reader = false;
    		}
    		
			//Changing the front with a boolean
			if(joy1.getRawButton(4)) {
    			f = (boolean) f ? false : true;//If the toggle boolean is false, make it true. If the toggle boolean is true, make it false.
    			while(joy1.getRawButton(4)) {}
    		}
    		if(f) {
    			front = 180;//Set the front of the robot to 180 degrees
    		} else {
    			front = 0;//Set the front of the robot to zero degrees
    		}
    		
    		//Drive code
    		if(joy1.getRawButton(5)) {//Toggle if only the turning is half speed or not
    			tSpeed = (boolean) tSpeed ? false : true;
    			while(joy1.getRawButton(5)) {}
    		}
    		
    		if(tSpeed) {
    			turnSensitivity = 0.5;//If the toggle boolean is true, decrease the sensitivity
    		}
    		if(!tSpeed) {
    			turnSensitivity = 1;//If the toggle boolean is false, don't decrease the turn sensitivity
    		}
    		
    		sens = -joy1.getRawAxis(3)/2+0.5; // Joystick sensitivity slider
    		
    		if(joy1.getRawButton(2)) {//If she is pressing the half speed button, decrease the drive magnitudes by half
	    		x = Math.pow(joy1.getRawAxis(0), 3)/2*sens;
	    		y = Math.pow(joy1.getRawAxis(1), 3)/2*sens;
	    		rot = -Math.pow(joy1.getRawAxis(2), 3)/2*sens;
    		} else {//If she isn't, leave them
    			x = Math.pow(joy1.getRawAxis(0), 3)*sens;
	    		y = Math.pow(joy1.getRawAxis(1), 3)*sens;
	    		rot = -Math.pow(joy1.getRawAxis(2), 3)/2*sens;
    		}
    		if((x > 0.25 || x < -0.25 || y > 0.25 || y < -0.25) && joy1.getRawButton(2)) {//If the robot is driving at a rate above the theshold limit, decrease the turning speed
    			rot = -Math.pow(joy1.getRawAxis(2), 3)/4*sens;
    		} else if((x > 0.5 || x < -0.5 || y > 0.5 || y < -0.5) && !joy1.getRawButton(2)) {
    			rot = -Math.pow(joy1.getRawAxis(2), 3)/4*sens;
    		}
    		
    		if((Math.abs(x)>=0.1 || Math.abs(y)>=0.1 || Math.abs(rot)>=0.1) && joy1.getPOV() == -1) {
    			mainDrive.mecanumDrive_Cartesian(x, y, rot*turnSensitivity, front);//Use the magnitudes and the front integer to drive with
    		}
    		//    			mainDrive.mecanumDrive_Cartesian(x, y, rot, front);//Use the magnitudes and the front integer to drive with
    		/************************
    		 * MANIPULATOR CONTROLS *
    		 ************************/
    		if(joy2.getRawButton(1)) {
    			shooter.set(shooterSpeed);	//Turn on shooter motor
    			mixer.set(mixerSpeed);	//Turn the window motor
    			shoot = true;
    		} else {
    			shooter.set(0);				//Turn off shooter motor
    			mixer.set(0);			//Stop the window motor
    			shoot = false;
    		}
			
			//Gear placer pneumatic
			if(joy2.getRawButton(3)) {//Toggle the gear lifter mechanism between open and closed
				grab = (boolean) grab ? false : true;//If the toggle boolean is true, make it false. If the toggle boolean is false, make it true.
				while(joy2.getRawButton(3)) {}
			}
			if(grab) {
				gear.set(out);//Open the gear lifter
			}
			if(!grab) {
				gear.set(in);//Close the gear lifter
			}
			
			//Gear placer motor
			if(joy2.getRawButton(4)) {//While Duey presses the Y button, raise the gear lifter
				arm.set(-0.6);
			}
			if(joy2.getRawButton(2)) {//While Duey presses the B button, lower the gear lifter
				arm.set(0.6);
			}
			if(!joy2.getRawButton(2) && !joy2.getRawButton(4)) {
				arm.set(0);//Disable the gear lifter  
			}
    		
    		//Climbing code
			if(Math.abs(joy2.getRawAxis(5))>=0.05) {
				climb.set(-joy2.getRawAxis(5));
				joy2.setRumble(leftRumble, Math.abs(joy2.getRawAxis(5)));
    			joy2.setRumble(rightRumble, Math.abs(joy2.getRawAxis(5)));
			} else {
				climb.set(0);
				joy2.setRumble(leftRumble, 0);
    			joy2.setRumble(rightRumble, 0);
			}
    		
    		if (joy1.getPOV()!=1) {
	    		int pov = joy1.getPOV();
				double a = 1;
				if(joy1.getRawButton(2)) {//Half speeds
					a = 0.5;
				} else {
					a = 1;
				}
				if(pov == 0) {
					mainDrive.mecanumDrive_Cartesian(0, -0.5*a*sens, 0, front);
				}
				if(pov == 45) {
					mainDrive.mecanumDrive_Cartesian(0.5*a*sens, -0.5*a*sens, 0, front);
				}
				if(pov == 90) {
					mainDrive.mecanumDrive_Cartesian(0.5*a*sens, 0, 0, front);
				}
				if(pov == 135) {
					mainDrive.mecanumDrive_Cartesian(0.5*a*sens, 0.5*a*sens, 0, front);
				}
				if(pov == 180) {
					mainDrive.mecanumDrive_Cartesian(0, 0.5*a*sens, 0, front);
				}
				if(pov == 225) {
					mainDrive.mecanumDrive_Cartesian(-0.5*a*sens, 0.5*a*sens, 0, front);
				}
				if(pov == 270) {
					mainDrive.mecanumDrive_Cartesian(-0.5*a*sens, 0, 0, front);
				}
				if(pov == 315) {
					mainDrive.mecanumDrive_Cartesian(-0.5*a*sens, -0.5*a*sens, 0, front);
				}
    		}
			
		} else {
			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		}
		read();
    }
    public void testPeriodic() {
    }
    
    void read() {//Read from the sensors
    	SmartDashboard.putBoolean("Front", f);//Tell which side is the front
    	SmartDashboard.putBoolean("Light", reader);//Tell if the light is on
    	SmartDashboard.putNumber("Pressure Sensor", pres.getPres());//Give the pressure being read from the pressure sensor
    	SmartDashboard.putNumber("Distance travelled", fle.getDistance());//Give the total distance travelled
    	SmartDashboard.putNumber("Vision Testing Center X", vision.getCenterX());//Give where the position of the center of the gear hook is.
    	SmartDashboard.putNumber("Rotation Magnitude", rot);//Give the rotational magnitude
    	SmartDashboard.putNumber("Gyro Value", gyro.getAngle());//Give the angle being read from the gyroscope
    	SmartDashboard.putNumber("Sensitivity", sens);
    	
    	if(pres.getPres()<20) {//Tell if there is usable pressure in the pneumatics system
    		SmartDashboard.putBoolean("Usable pressure", false);
    	} else {
    		SmartDashboard.putBoolean("Usable pressure", true);
    	}
    }
    
	private void RedAuton() { //The turn left one
		//After the competition, an FTA guy said to not use while loops anywhere because it messes with whatever they do, so now auton is in progress of being written without while loops
		switch(step) {
		case 1://When auton is one its first step, drive forwards
			if(fle.getDistance()<72.5) {
				mainDrive.mecanumDrive_Cartesian(0, 0.4, 0, 0);	//Drive forwards
				read();//Read from sensors
				gearA.set(out);//Set the gear pistons
				gearB.set(out);
				gyro.reset();
			} else {
				step = 2;
			}
			break;
		case 2://When auton is on its second step, turn left
			if(gyro.getAngle() > -42.5) {
				mainDrive.mecanumDrive_Cartesian(0, 0, 0.4, 0);
				read();//Read from sensors
			} else {
				step = 3;
				timer.reset();
				timer.start();
			}
			break;
		case 3://When auton is on its third step, drive forwards
			if(timer.get()<=3) {
				mainDrive.mecanumDrive_Cartesian(0, 0.2, 0, 0);//Slowly drive forwards
			} else {
				step = 4;
				timer.reset();
				timer.start();
			}
			break;
		case 4://When auton is on its fourth step, open the gear chute
			if(timer.get()<= 0.5) {
				gearA.set(in);
				gearB.set(in);
				mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
			} else {
				step = 5;
				timer.reset();
				timer.start();
			}
			break;
		case 5://When auton is on its fifth step, back up
			if(timer.get() <= 1.5) {
				mainDrive.mecanumDrive_Cartesian(0, -0.35, 0, 0);
			} else {
				step = 6;
			}
			break;
		case 6://When auton is on its sixth step, stop and close the chute
			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
			gearA.set(out);
			gearB.set(out);
		}
	}
	
	/**
	 * This auton is exactly the same as the blue auton. The only difference is that it turns right instead of left
	 */
	private void BlueAuton() { //The turn right one
		//After the competition, an FTA guy said to not use while loops anywhere because it messes with whatever they do, so now auton is in progress of being written without while loops
		switch(step) {
		case 1://When auton is one its first step, drive forwards
			if(fle.getDistance()<72.5) {
				mainDrive.mecanumDrive_Cartesian(0, 0.4, 0, 0);	//Drive forwards
				read();//Read from sensors
				gearA.set(out);//Set the gear pistons
				gearB.set(out);
				gyro.reset();
			} else {
				step = 2;
			}
			break;
		case 2://When auton is on its second step, turn left
			if(gyro.getAngle() < 42.5) {
				mainDrive.mecanumDrive_Cartesian(0, 0, -0.4, 0);
				read();//Read from sensors
			} else {
				step = 3;
				timer.reset();
				timer.start();
			}
			break;
		case 3://When auton is on its third step, drive forwards
			if(timer.get()<=3) {
				mainDrive.mecanumDrive_Cartesian(0, 0.2, 0, 0);//Slowly drive forwards
			} else {
				step = 4;
				timer.reset();
				timer.start();
			}
			break;
		case 4://When auton is on its fourth step, open the gear chute
			if(timer.get()<= 0.5) {
				gearA.set(in);
				gearB.set(in);
				mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
			} else {
				step = 5;
				timer.reset();
				timer.start();
			}
			break;
		case 5://When auton is on its fifth step, back up
			if(timer.get() <= 1.5) {
				mainDrive.mecanumDrive_Cartesian(0, -0.35, 0, 0);
			} else {
				step = 6;
			}
			break;
		case 6://When auton is on its sixth step, stop and close the chute
			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
			gearA.set(out);
			gearB.set(out);
		}
	}
}