/****************************************
 * 
 *	2017 STEAMWORKS
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
	int step;
	static final Value out = DoubleSolenoid.Value.kForward;
	static final Value in = DoubleSolenoid.Value.kReverse;
	static final edu.wpi.first.wpilibj.Relay.Value on = Relay.Value.kForward;
	static final edu.wpi.first.wpilibj.Relay.Value off = Relay.Value.kOff;
	double shooterSpeed = 0.9;//This speed the shooter must go 
	double climbSpeed = -0.5;//The speed the climber must go
	double mixerSpeed = 0.5;//The speed the window motor must go
	Thread thread;
	//Auton Names
	final String defaultAuto = "Default";//For a standard auton
	final String redAuton = "redAuton";	//For auton on the red team
	final String blueAuton = "blueAuton";//For auton on the blue team
	final String straight = "straightAuton";//For a center gear peg auton
	String autoSelected;//Thing needed for selecting autons
	SendableChooser<String> chooser = new SendableChooser<>();//Thing needed for selecting autons
	
	//Controllers
	Joystick joy1 = new Joystick(0);//Big Logitech joystick
	Joystick joy2 = new Joystick(1);//Afterglow XBOX controller
	
	// Drive Talons
	CANTalon frontLeft = new CANTalon(1);
	CANTalon frontRight = new CANTalon(2);
    CANTalon backLeft = new CANTalon(3);
    CANTalon backRight = new CANTalon(4);
    RobotDrive mainDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
    
    // Shooter and ball feeder
    Victor shooter = new Victor(0);//Shooter motor
    Victor arm = new Victor(1);	//Gear picker arm
    Victor climb = new Victor(2);//Climbing motor
    Victor mixer = new Victor(3);//Window motor
    Relay spike = new Relay(0);	//Spotting light
    
    //Sensors
	ADXRS450_Gyro gyro = new ADXRS450_Gyro();					//Gyroscope
	//ADXL362 accel = new ADXL362(Range.k8G);					//Accelerometer
	Timer timer = new Timer();									//Timer
	MyEncoder fle = new MyEncoder(1);							//Front left encoder; Add more for other 3 encoders if working properly
	PressureSensor pres = new PressureSensor(0);//Analog pressure sensor
	Vision vision = new Vision();//Vision control object
	
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
	
	//Toggles
	int front = 0;				//Angle for the front- 0 is gear side, 180 is shooter side
	boolean tlf = false;		//Toggle lock front boolean
	boolean f = true;			//Front toggle boolean
	boolean light = false;		//Spike toggle boolean
	boolean tll = false;		//Toggle lock light boolean
	boolean shoot = false;		//Shooter toggle boolean
	boolean reader = false;		//Decides whether the spotting light should be on or off by combining if the light should be on because of the light button, or if it should be on because the robot is shooting
	public boolean done = false;//Autonomous boolean
	//boolean armBool = true; 	//True means up--Not used due to motor change
	boolean grab = true; 		//True means closed/activated
	boolean tlg = false;		//Toggle lock gear lifter pneumatic boolean
	//double angle = 0;			//Used in switching which side of the robot is which--Not used because of motor change 
	
	//Y makes the thing go up -TOGGLE -default up
	//X opens and closes the thingy -TOGGLE -default closed
	//Button 3 does gear stuff
	
	public void robotInit() {
		frontLeft.setInverted(true);	//Invert the left motors
		backLeft.setInverted(true);
		gyro.calibrate();				//Calibrate the gyroscope
		fle.reset();					//Calibrate encoder
		
		//Adds Auton selectors to SmartDashboard
    	chooser.addObject("Default Auto", defaultAuto);//Needed for selecting autons		
		chooser.addDefault("Red Autonomous Code", redAuton);//Needed for selecting autons
		chooser.addObject("Blue Autonomous Code", blueAuton);//Needed for selecting autons
		chooser.addObject("Middle gear autonomous code", straight);//Needed for selecting autons
		SmartDashboard.putData("Autons choices", chooser);//Needed for selecting autons
		
		compressor.start();							//Start the compressor
		camera.startAutomaticCapture("cam0", 0);	//Start the camera
    }
    
	public void autonomousInit() {
		autoSelected = chooser.getSelected();		//Select the auton
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
    	switch(autoSelected) {//Decide which auton to use
    	case defaultAuto:
    		DefaultAuto();//Only cross the center line
    		break;
    	case redAuton:
    		RedAuton();//Use when on the red team
    		break;
    	case blueAuton:
    		BlueAuton();//Use when on the blue team
    		break;
    	case straight:
    		straightGear();//Use the middle gear peg
    	}
    	
    }
    
	public void teleopPeriodic() {
		//If nothing is being read by a controller, stop.
		if(!joy1.getRawButton(1) && (joy1.getPOV() == -1 || joy1.getRawButton(2) || joy1.getRawButton(3) || joy1.getRawButton(4) || joy1.getRawButton(5) || joy1.getRawButton(6) || joy1.getRawButton(7) || joy1.getRawButton(8) || joy1.getRawButton(9) || joy1.getRawButton(10) ||  joy2.getRawButton(1) || joy2.getRawButton(2) || joy2.getRawButton(3) || joy2.getRawButton(4) || joy2.getRawButton(5) || joy2.getRawButton(6) || joy2.getRawButton(7) || joy2.getRawButton(8) || joy2.getRawButton(9) || joy2.getRawButton(10) || joy1.getRawAxis(0) >= 0.05 || joy1.getRawAxis(1) >= 0.05 || joy1.getRawAxis(2) >= 0.05 || joy1.getRawAxis(3) >= 0.05 || joy1.getRawAxis(4) >= 0.05 || joy1.getRawAxis(5) >= 0.05 || joy1.getRawAxis(6) >= 0.05 || joy2.getRawAxis(0) >= 0.05 || joy2.getRawAxis(1) >= 0.05 || joy2.getRawAxis(2) >= 0.05 || joy2.getRawAxis(3) >= 0.05 || joy2.getRawAxis(4) >= 0.05 || joy2.getRawAxis(5) >= 0.05 || joy2.getRawAxis(6) >= 0.05 || joy1.getRawAxis(0) <= -0.05 || joy1.getRawAxis(1) <= -0.05 || joy1.getRawAxis(2) <= -0.05 || joy1.getRawAxis(3) <= -0.05 || joy1.getRawAxis(4) <= -0.05 || joy1.getRawAxis(5) <= -0.05 || joy1.getRawAxis(6) <= -0.05 || joy2.getRawAxis(0) <= -0.05 || joy2.getRawAxis(1) <= -0.05 || joy2.getRawAxis(2) <= -0.05 || joy2.getRawAxis(3) <= -0.05 || joy2.getRawAxis(4) <= -0.05 || joy2.getRawAxis(5) <= -0.05 || joy2.getRawAxis(6) <= -0.05)) {
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
    		if(joy1.getRawButton(5) && !tll) {//Use button five on the big joystick
    			light = (boolean) light ? false : true;//If the light toggle boolean is true, make it false. If the light toggle boolean is false, make it true.
    			tll = true;
    		} else if (joy1.getRawButton(5) && tll) {
    			tll = true;
    		} else if (!joy1.getRawButton(5) && tll) {
    			tll = false;
    		}
    		if(light || shoot) {//If Jade turned the light on with the toggle button or if the robot is shooting, turn on the light
    			spike.set(on);
    			reader = true;
    		} else {
    			spike.set(off);
    			reader = false;
    		}
    		
			//Changing the front with a boolean
			if(joy1.getRawButton(4) && !tlf) {
    			f = (boolean) f ? false : true;//If the toggle boolean is false, make it true. If the toggle boolean is true, make it false.
    			tlf = true;
    		} else if (joy1.getRawButton(4) && tlf) {
    			tlf = true;
    		} else if (!joy1.getRawButton(4) && tlf) {
    			tlf = false;
    		}
    		if(f) {
    			front = 180;//Set the front of the robot to 180 degrees
    		} else {
    			front = 0;//Set the front of the robot to zero degrees
    		}
    		
    		//Climbing code
    		if(joy1.getRawButton(7)) {	//press and hold button 6 to climb
				climb.set(climbSpeed);
			} 
    		if(joy1.getRawButton(8)) {
				climb.set(-climbSpeed);
			}
    		if(!joy1.getRawButton(6) && !joy1.getRawButton(8)) {
    			climb.set(0);
    		}
    		
    		//Gear adjustment code
    		if(joy1.getRawButton(12)) {//While button 12 is being pressed, adjust the angle with vision
    			mainDrive.mecanumDrive_Cartesian(0, 0, vision.getAdjustmentSpeed(), 0);
    		}
    		sens = joy1.getRawAxis(2)/2+0.5; // Joystick sensitivity slider
    		//Drive code
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
    		
    		if((Math.abs(x)>=0.1 || Math.abs(y)>=0.1 || Math.abs(rot)>=0.1)) {
    			mainDrive.mecanumDrive_Cartesian(x, y, rot, front);//Use the magnitudes and the front integer to drive with
    		}
    		
    		/************************
    		 * MANIPULATOR CONTROLS *
    		 ************************/
    		if(joy2.getRawButton(1)) {
    			shooter.set(shooterSpeed);	//Turn on shooter motor
    			mixer.set(mixerSpeed);	//Turn the window motor
    		} else {
    			shooter.set(0);				//Turn off shooter motor
    			mixer.set(0);			//Stop the window motor
    		}
			
			//Gear placer pneumatic
			if(joy2.getRawButton(3) && !tlg) {//Toggle the gear lifter mechanism between open and closed
				grab = (boolean) grab ? false : true;//If the toggle boolean is true, make it false. If the toggle boolean is false, make it true.
				tlg = true;
			} else if (joy2.getRawButton(3) && tlg) {
				tlg = true;
    		} else if (!joy2.getRawButton(3) && tlg) {
    			tlg = false;
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
    		
		} else if (joy1.getPOV()!=-1 && !joy1.getRawButton(2)){
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
			if(pov == 0) {
				mainDrive.mecanumDrive_Cartesian(0, 0.5*a, 0, front);
			}
			if(pov == 180) {
				mainDrive.mecanumDrive_Cartesian(0, -0.5*a, 0, front);
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
    	SmartDashboard.putNumber("Speed", fle.getRate());//Give the rate of the robot
    	SmartDashboard.putNumber("Vision Testing Center X", vision.getCenterX());//Give where the position of the center of the gear hook is.
    	SmartDashboard.putNumber("X Magnitude", x);//Give the X magnitude
    	SmartDashboard.putNumber("Y Magnitude", y);//Give the Y magnitude
    	SmartDashboard.putNumber("Rotation Magnitude", rot);//Give the rotational magnitude
    	SmartDashboard.putNumber("Gyro Value", gyro.getAngle());//Give the angle being read from the gyroscope
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
			if(fle.getDistance()<75) {
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
			if(timer.get()<= 1) {
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
			if(timer.get() <= 1) {
				mainDrive.mecanumDrive_Cartesian(0, -0.3, 0, 0);
			} else {
				step = 6;
			}
			break;
		case 6://When auton is on its sixth step, stop and close the chute
			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
			gearA.set(out);
			gearB.set(out);
		}
		/**
		while(fle.getDistance()<75 && timer.get() <= 15 && !done) {//While the distance travelled is less than 64 inches and the time is less than 15 seconds, drive forwards
			mainDrive.mecanumDrive_Cartesian(0, 0.4, 0, 0);	//Drive forwards
			read();//Read from sensors
			gearA.set(out);//Set the gear pistons
			gearB.set(out);
			gyro.reset();
		}
		//Turn -45 degrees
		while(gyro.getAngle() > -42.5 && timer.get() <= 15 && !done) {//While the gyro angle is greater than -45 and the timer is less than 15 seconds, turn left
			mainDrive.mecanumDrive_Cartesian(0, 0, 0.4, 0);
			read();//Read from sensors
		}
		
		
		while(timer.get() <= 15 && !done && (timer.get()-time0<=3)) {//While the difference between the initial travel distance and the current travel distance is less than 40 inches, move forwards
			mainDrive.mecanumDrive_Cartesian(0, 0.2, 0, 0);//Slowly drive forwards
		}
		
		
		/**
		//This whole area needs to be fixed. Once the robot has made it to the peg, it is supposed to open the hatch and drive backwards for two feet. However, it reads that it has instantly travelled more than two feet, which makes it skip the entire loop. This is problematic because the robot will neither back up nor will it open the hatch. Right now, it is set up to test and record data to be able to see where the problem might be. IT MUST BE FIXED BEFORE COMPETITION
		fle.reset();//Reset the encoder
		distance = fle.getDistance();//Record the current distance travelled
		while(timer.get() <= 15) {//THIS LOOP IS ONLY FOR TESTING PURPOSES
			System.out.println("distance number: " + distance + " Encoder distance: " + fle.getDistance());//Print the initial distance travelled and the current distance travelled to the consol
			while(Math.abs(fle.getDistance()-distance)<=24 && timer.get() <= 15) {//While the absolute value of the difference between the current distance travelled and the initial distance travelled are less than twenty-four inches, back up and open the hatch
				mainDrive.mecanumDrive_Cartesian(0, -0.4, 0, 0);//Back up
				gearA.set(in);//Open the gear hatch
				gearB.set(in);
				read();//Read from sensors
				System.out.println("distance number: " + distance + " Encoder distance: " + fle.getDistance());//Print the initial distance travelled and the current distance travelled to the consol
			}
		}
		**
		double time1 = timer.get();
		while((timer.get()-time1)<= 2 && !done) {
			gearA.set(in);
			gearB.set(in);
			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		}
		double time2 = timer.get();
		while((timer.get()-time2) <= 1 && !done) {
			mainDrive.mecanumDrive_Cartesian(0, -0.3, 0, 0);
			shooter.set(0);
			mixer.set(0);
		}
		while(gyro.getAngle() >-90 && timer.get() <=15) {
			mainDrive.mecanumDrive_Cartesian(0, 0, 0.4, 0);
		}
		double time3 = timer.get();
		while(timer.get()-time3<=3) {
			mainDrive.mecanumDrive_Cartesian(0.35, -0.35, 0, 0);
		}
		
		
		gearA.set(out);//Close gears after it has completely backed up
		gearB.set(out);
		mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);//Stop the drive motors just in case
		
		done = true;//Use a safetly boolean to confirm that auton has been completed so that it won't randomly try to run through it a second time. It may not be necessary, but it certainly does not hurt anything to have it.
		**/
	}
	
	
	
	
	
	
	
	/**
	 * This auton is exactly the same as the blue auton. The only difference is that it turns right instead of left
	 */
	private void BlueAuton() { //The turn right one
		//After the competition, an FTA guy said to not use while loops anywhere because it messes with whatever they do, so now auton is in progress of being written without while loops
		switch(step) {
		case 1://When auton is one its first step, drive forwards
			if(fle.getDistance()<75) {
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
			if(timer.get()<= 1) {
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
			if(timer.get() <= 1) {
				mainDrive.mecanumDrive_Cartesian(0, -0.3, 0, 0);
			} else {
				step = 6;
			}
			break;
		case 6://When auton is on its sixth step, stop and close the chute
			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
			gearA.set(out);
			gearB.set(out);
		}
		/**
		//Drive forwards 64 inches
		fle.reset();
		frontLeft.setEncPosition(0);
		while(fle.getDistance()<75 && timer.get() <= 15 && !done) {
			mainDrive.mecanumDrive_Cartesian(0, 0.4, 0, 0);	//Drive forwards
			read();//Read from sensors
			gearA.set(out);//Set the gear pistons
			gearB.set(out);
			gyro.reset();
		}
		//Turn 45 degrees
		while(gyro.getAngle() < 42.5 && timer.get() <= 15 && !done) {
			mainDrive.mecanumDrive_Cartesian(0, 0, -0.4, 0);
			read();
		}
		
		double time0 = timer.get();
		while(timer.get() <= 15 && !done && (timer.get()-time0<=3)) {//While the difference between the initial travel distance and the current travel distance is less than 40 inches, move forwards
			mainDrive.mecanumDrive_Cartesian(0, 0.2, 0, 0);//Slowly drive forwards
		}
		
		/**
		//This whole area needs to be fixed. Once the robot has made it to the peg, it is supposed to open the hatch and drive backwards for two feet. However, it reads that it has instantly travelled more than two feet, which makes it skip the entire loop. This is problematic because the robot will neither back up nor will it open the hatch. Right now, it is set up to test and record data to be able to see where the problem might be. IT MUST BE FIXED BEFORE COMPETITION
		fle.reset();//Reset the encoder
		distance = fle.getDistance();//Record the current distance travelled
		while(timer.get() <= 15) {//THIS LOOP IS ONLY FOR TESTING PURPOSES
			System.out.println("distance number: " + distance + " Encoder distance: " + fle.getDistance());//Print the initial distance travelled and the current distance travelled to the consol
			while(Math.abs(fle.getDistance()-distance)<=24 && timer.get() <= 15) {//While the absolute value of the difference between the current distance travelled and the initial distance travelled are less than twenty-four inches, back up and open the hatch
				mainDrive.mecanumDrive_Cartesian(0, -0.4, 0, 0);//Back up
				gearA.set(in);//Open the gear hatch
				gearB.set(in);
				read();//Read from sensors
				System.out.println("distance number: " + distance + " Encoder distance: " + fle.getDistance());//Print the initial distance travelled and the current distance travelled to the consol
			}
		}
		**
		double time1 = timer.get();
		while((timer.get()-time1)<= 2 && !done) {
			gearA.set(in);
			gearB.set(in);
			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		}
		double time2 = timer.get();
		while((timer.get()-time2) <= 1 && !done) {
			mainDrive.mecanumDrive_Cartesian(0, -0.3, 0, 0);
			shooter.set(0);
			mixer.set(0);
		}
		gearA.set(out);//Close gears
		gearB.set(out);
		mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		
		done = true;
		**/
	}
	
	
	
	
	
	
	
	
	private void straightGear() {
		switch(step) {
		case 1:
			if(fle.getDistance()<=60) {
				mainDrive.mecanumDrive_Cartesian(0, 0.2, 0, 0);
				read();
				gearA.set(out);
				gearB.set(out);
			} else {
				step = 2;
				timer.reset();
				timer.start();
			}
			break;
		case 2:
			if(timer.get() <= 1) {
				gearA.set(in);
				gearB.set(in);
			} else {
				step = 3;
				timer.reset();
				timer.start();
			}
			break;
		case 3:
			if(timer.get() <= 1) {
				mainDrive.mecanumDrive_Cartesian(0, -0.3, 0, 0);
				read();
			} else {
				step = 4;
			}
			break;
		case 4:
			gearA.set(out);
			gearB.set(out);
			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
			break;
		}
		
		/**
		fle.reset();
		while(fle.getDistance()<=60 && timer.get() <= 8 && !done) {
			mainDrive.mecanumDrive_Cartesian(0, 0.2, 0, 0);
			read();
			gearA.set(out);
			gearB.set(out);
		}
		while(timer.get() <= 9 && !done) {
			gearA.set(in);
			gearB.set(in);
		}
		while(timer.get() <= 13 && !done) {
			mainDrive.mecanumDrive_Cartesian(0, -0.3, 0, 0);
			read();
		}
		fle.reset();
		gearA.set(out);
		gearB.set(out);
		mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		done = true;
		**/
	}
	
	
	
	
	
	private void DefaultAuto() {
		if(fle.getDistance() < 100) {
			mainDrive.mecanumDrive_Cartesian(0, 0.4, 0, 0);
			read();
			gearA.set(out);
			gearB.set(out);
		} else {
			mainDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
		}
	}
}
