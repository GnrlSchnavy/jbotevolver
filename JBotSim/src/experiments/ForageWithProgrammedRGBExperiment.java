package experiments;

import java.util.LinkedList;
import java.util.Vector;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.SimRandom;
import controllers.ProgrammedRGBAntiForagerForSocialInfluenceExperiment;
import controllers.ProgrammedRGBForagerForSocialInfluenceExperiment;
import controllers.ProgrammedRGBNeutralForagerForSocialInfluenceExperiment;

public class ForageWithProgrammedRGBExperiment extends Experiment {

	private int numberOfProgrammedRobots;
	private int numberOfEnemyRobots;
	private int numberOfNeutralRobots;

	public ForageWithProgrammedRGBExperiment(Simulator simulator,
			Arguments experimentArguments, Arguments environmentArguments,
			Arguments robotsArguments, Arguments controllersArguments) {
		super(simulator, experimentArguments, environmentArguments,
				robotsArguments, controllersArguments);

	}

	@Override
	public LinkedList<Robot> createRobots() {
		robots = super.createRobots();

		// double speedOfPreProgrammedRobot = Robot.MAXIMUMSPEED;
		double speedOfPreProgrammedRobot = 1;

		if (experimentArguments != null) {
			if (experimentArguments
					.getArgumentIsDefined("numberofprogrammedrobots")) {
				numberOfProgrammedRobots = experimentArguments
						.getArgumentAsInt("numberofprogrammedrobots");
			}
			if (experimentArguments.getArgumentIsDefined("numberofenemyrobots")) {
				numberOfEnemyRobots = experimentArguments
						.getArgumentAsInt("numberofenemyrobots");
			}
			if (experimentArguments
					.getArgumentIsDefined("numberofneutralrobots")) {
				numberOfNeutralRobots = experimentArguments
						.getArgumentAsInt("numberofneutralrobots");
			}
			if (experimentArguments
					.getArgumentIsDefined("speedofpreprogrammedrobots")) {
				speedOfPreProgrammedRobot = experimentArguments
						.getArgumentAsDouble("speedofpreprogrammedrobots");
				// speedOfPreProgrammedRobot *=
				// experimentArguments.getArgumentAsDouble("speedofpreprogrammedrobots");
			}

		}

		for (int i = 0; i < numberOfProgrammedRobots; i++)
			robots.add(createOnePreProgrmmedRobot(speedOfPreProgrammedRobot,
					robotArguments, controllerArguments));

		for (int i = 0; i < numberOfEnemyRobots; i++)
			robots.add(createOneEnemyRobot(robotArguments, controllerArguments));

		for (int i = 0; i < numberOfNeutralRobots; i++)
			robots.add(createOneNeutralRobot(robotArguments,
					controllerArguments));

		return robots;
	}

	private Robot createOnePreProgrmmedRobot( double speed,
			Arguments robotArguments, Arguments controllerArguments) {
		Robot robot = robotFactory.getRobot(robotArguments, this);
		// robot.setController(new ProgrammedForager(robot,
		// controllerArguments));
		robot.setController(new ProgrammedRGBForagerForSocialInfluenceExperiment(
				simulator,robot, controllerArguments));
		return robot;
	}

	private Robot createOneEnemyRobot( Arguments robotArguments,
			Arguments controllerArguments) {
		Robot robot = robotFactory.getRobot(robotArguments, this);
		robot.setController(new ProgrammedRGBAntiForagerForSocialInfluenceExperiment(
				simulator,robot, controllerArguments));
		return robot;

	}

	private Robot createOneNeutralRobot( Arguments robotArguments,
			Arguments controllerArguments) {
		Robot robot = robotFactory.getRobot(robotArguments, this);
		robot.setController(new ProgrammedRGBNeutralForagerForSocialInfluenceExperiment(
				simulator,robot, controllerArguments));
		return robot;

	}

	@Override
	protected void placeRobotsUsingPlacement() {
		if (robotArguments.getArgumentAsString("placement").equalsIgnoreCase(
				"nest")) {

			// System.out.println("Placing robots in a rectangle...");
			for (Robot r : robots) {

				boolean tooCloseToSomeOtherRobot = false;
				double maxRadius = ((RoundForageEnvironment) environment)
						.getNestRadius();
				do {
					tooCloseToSomeOtherRobot = false;
					double angle = simulator.getRandom().nextDouble() * 2 * Math.PI;
					double radius = simulator.getRandom().nextDouble() * maxRadius;
					double positionX = Math.cos(angle) * radius;
					double positionY = Math.sin(angle) * radius;

					r.setPosition(new Vector2d(positionX, positionY));
					Robot closestRobot = getRobotClosestTo(r);
					if (closestRobot != null) {
						if (r.getPosition().distanceTo(
								closestRobot.getPosition()) < 0.05)
							tooCloseToSomeOtherRobot = true;
					}
					maxRadius += .05;
				} while (tooCloseToSomeOtherRobot);
				r.teleportTo(r.getPosition());
			}
		} else {
			super.placeRobotsUsingPlacement();
		}
	}

}
