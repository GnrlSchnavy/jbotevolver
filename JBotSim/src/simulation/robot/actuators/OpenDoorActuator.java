package simulation.robot.actuators;

import java.util.LinkedList;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.TwoRoomsEnvironment;
import simulation.physicalobjects.Wall;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class OpenDoorActuator extends Actuator {
	
	protected TwoRoomsEnvironment env;
	protected double distanceToDoor = 0.2;
	protected boolean open = false;
	protected Simulator simulator;
	protected boolean openedDoor = false;

	public OpenDoorActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		this.simulator = simulator;
		args.getArgumentAsDoubleOrSetDefault("distancetodoor", distanceToDoor);
	}

	@Override
	public void apply(Robot robot) {
		openedDoor = false;
		if(open) {
			if(env == null)
				env = (TwoRoomsEnvironment)simulator.getEnvironment();
			
			LinkedList<Wall> buttons = env.getButtons();
			Robot rob = env.getRobots().get(0);
			Vector2d r = rob.getPosition(); 
			
			for(Wall b : buttons) {
				Vector2d bPos = b.getPosition();
				if(bPos.distanceTo(r) < distanceToDoor) {
					boolean doorAlreadyOpen = env.doorsOpen;
					env.openDoor(bPos.getX());
					if(!doorAlreadyOpen)
						openedDoor = true;
					break;
				}
			}
			
			((DifferentialDriveRobot) robot).setWheelSpeed(0, 0);
		}
		open = false;
	}

	public void open(boolean open) {
		this.open = open;		
	}
}
