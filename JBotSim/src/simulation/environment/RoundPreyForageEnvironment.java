package simulation.environment;

import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import controllers.RandomRobotController;

public class RoundPreyForageEnvironment extends Environment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	private double nestLimit;
	private double forageLimit;
	private double forbiddenArea;
	private int numberOfPreys;
	private Nest nest;
	private int numberOfFoodSuccessfullyForaged = 0;
	private Random random;
	private Arguments arguments;

	public RoundPreyForageEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

		nestLimit = arguments.getArgumentIsDefined("nestlimit") ? arguments
				.getArgumentAsDouble("nestlimit") : .5;
		forageLimit = arguments.getArgumentIsDefined("foragelimit") ? arguments
				.getArgumentAsDouble("foragelimit") : 2.0;
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments
				.getArgumentAsDouble("forbiddenarea") : 5.0;

		this.arguments = arguments;
		this.random = simulator.getRandom();

		if (arguments.getArgumentIsDefined("densityofpreys")) {
			double densityoffood = arguments
					.getArgumentAsDouble("densityofpreys");
			numberOfPreys = (int) (densityoffood * Math.PI * forageLimit
					* forageLimit + .5);
		} else {
			numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments
					.getArgumentAsInt("numberofpreys") : 20;
		}

	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		for (int i = 0; i < numberOfPreys; i++) {
			addPrey(new Prey(simulator, "Prey " + i, newRandomPosition(), 0,
					PREY_MASS, PREY_RADIUS));
		}
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);
	}

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble() * (forageLimit - nestLimit)
				+ nestLimit;
		double angle = random.nextDouble() * 2 * Math.PI;
		return new Vector2d(radius * Math.cos(angle), radius * Math.sin(angle));
	}

	@Override
	public void update(double time) {
		
		Vector2d robot = new Vector2d();
		Prey prey = null;
		double distance = 0.002;
		
		for (Prey p : getPrey()) {
			prey = p;
		}
		
		for (Robot r : getRobots()) {
			robot.set(r.getPosition());
			double distanceToPrey = robot.distanceTo(prey.getPosition());
			if (distanceToPrey - r.getRadius() - prey.getRadius() < distance)
				prey.teleportTo(newRandomPosition());
		}
			
	}

	public int getNumberOfFoodSuccessfullyForaged() {
		return numberOfFoodSuccessfullyForaged;
	}

	public double getNestRadius() {
		return nestLimit;
	}

	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}

}