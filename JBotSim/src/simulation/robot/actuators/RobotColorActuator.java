package simulation.robot.actuators;

import java.awt.Color;

import simulation.Simulator;
import simulation.robot.Robot;

public class RobotColorActuator extends Actuator {	

	Color   color = Color.BLACK;
	
	public RobotColorActuator(Simulator simulator, int id) {
		super(simulator, id);
	}

	public void turnRed(){
		color = Color.RED;
	}

	public void turnGreen(){
		color = Color.GREEN;
	}

	public void turnBlack(){
		color = Color.BLACK;
	}

	public void turn(Color color) {
		this.color = color;
	}
		
	@Override
	public void apply(Robot robot) {
		robot.setBodyColor(color);
	}

	@Override
	public String toString() {
		return "RobotColorActuator [color=" + color + "]";
	}

	public void turnBlue() {
		color = Color.BLUE;		
	}

	public void turnLightGrey() {
		color = Color.LIGHT_GRAY;		
		
	}

	public void turnDarkGrey() {
		color = Color.DARK_GRAY;				
	}

	public void turnYellow() {
		color = Color.YELLOW;				
	}
}