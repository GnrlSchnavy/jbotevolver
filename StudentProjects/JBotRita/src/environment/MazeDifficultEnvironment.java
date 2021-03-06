package environment;

import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class MazeDifficultEnvironment extends Environment {
	private Vector2d nestPosition = new Vector2d(0, 0);
	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;

	private static final double MAX_X_LIMIT_FOR_PREY = 1.6;
	private static final double MIN_X_LIMIT_FOR_PREY = 1.4;

	@ArgumentsAnnotation(name = "nestlimit", defaultValue = "0.5")
	private double nestLimit;

	@ArgumentsAnnotation(name = "foragelimit", defaultValue = "2.0")
	private double forageLimit;

	@ArgumentsAnnotation(name = "forbiddenarea", defaultValue = "5.0")
	private double forbiddenArea;

	@ArgumentsAnnotation(name = "numberofpreys", defaultValue = "20")
	private static final int NUMBER_OF_PREYS = 3;

	private Nest nest;
	private int numberOfFoodSuccessfullyForaged = 0;
	private Random random;

	private Simulator simulator;
	private boolean foodInCenter = false;
	private boolean change_PreyInitialDistance;

	private Prey prey;

	public MazeDifficultEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.simulator = simulator;

		nestLimit = arguments.getArgumentIsDefined("nestlimit") ? arguments
				.getArgumentAsDouble("nestlimit") : .5;
		forageLimit = arguments.getArgumentIsDefined("foragelimit") ? arguments
				.getArgumentAsDouble("foragelimit") : 2.0;
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments
				.getArgumentAsDouble("forbiddenarea") : 5.0;

		this.random = simulator.getRandom();

	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		for (int i = 0; i < NUMBER_OF_PREYS; i++)
			addPrey(new Prey(simulator, "Prey " + 0, newRandomPosition(), 0,
					PREY_MASS, PREY_RADIUS));

		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);

//		// primeiro de lado do ninho horizontal
//		addStaticObject(new Wall(simulator, 0.4, 0.5, 0.3, 0.1));
//		addStaticObject(new Wall(simulator, -0.4, 0.5, 0.3, 0.1));
//		addStaticObject(new Wall(simulator, 0.4, -0.5, 0.3, 0.1));
//		addStaticObject(new Wall(simulator, -0.4, -0.5, 0.3, 0.1));
//
//		// //primeiro de lado do ninho vertical
//		addStaticObject(new Wall(simulator, 0.5, 0, 0.1, 1));
//		addStaticObject(new Wall(simulator, -0.5, 0, 0.1, 1));
//
//		// as || ao pe do ninho
//		addStaticObject(new Wall(simulator, 0.2, 1, 0.1, 1.1));
//		addStaticObject(new Wall(simulator, -0.2, 1, 0.1, 1.1));
//		addStaticObject(new Wall(simulator, 0.2, -1, 0.1, 1.1));
//		addStaticObject(new Wall(simulator, -0.2, -1, 0.1, 1.1));
//
//		// exterior ao ninho horizontal maior
//		addStaticObject(new Wall(simulator, 0, -1.8, 3.4, 0.1));
//		addStaticObject(new Wall(simulator, 0, 1.8, 3.4, 0.1));
//
//		// exterior ao ninho horizontal menorr
//		addStaticObject(new Wall(simulator, 0.8, -1.5, 1.1, 0.1));
//		addStaticObject(new Wall(simulator, 0.8, 1.5, 1.1, 0.1));
//		addStaticObject(new Wall(simulator, -0.8, -1.5, 1.1, 0.1));
//		addStaticObject(new Wall(simulator, -0.8, 1.5, 1.1, 0.1));
//
//		// exterior ao ninho vertical menores
//		addStaticObject(new Wall(simulator, 1.3, 0, 0.1, 3.1));
//		addStaticObject(new Wall(simulator, -1.3, 0, 0.1, 3.1));
//
//		// exterior ao ninho vertical maiores
//		addStaticObject(new Wall(simulator, 1.7, 0, 0.1, 3.7));
//		addStaticObject(new Wall(simulator, -1.7, 0, 0.1, 3.7));
//		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
//		addObject(nest);
//		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
//		addObject(nest);
//		
//		

		
		addStaticObject(new Wall(simulator, 0.4, 0.5, 0.3, 0.1));
		addStaticObject(new Wall(simulator, -0.4, 0.5, 0.3, 0.1));
		addStaticObject(new Wall(simulator, 0.4, -0.5, 0.3, 0.1));
		addStaticObject(new Wall(simulator, -0.4, -0.5, 0.3, 0.1));

		// //primeiro de lado do ninho vertical
		addStaticObject(new Wall(simulator, 0.5, 0, 0.1, 1));
		addStaticObject(new Wall(simulator, -0.5, 0, 0.1, 1));

		// as || ao pe do ninho
		addStaticObject(new Wall(simulator, 0.2, 1, 0.1, 1.1));
		addStaticObject(new Wall(simulator, -0.2, 1, 0.1, 1.1));
		addStaticObject(new Wall(simulator, 0.2, -1, 0.1, 1.1));
		addStaticObject(new Wall(simulator, -0.2, -1, 0.1, 1.1));

		// exterior ao ninho horizontal maior
		addStaticObject(new Wall(simulator, 0, 1.9, 3.5, 0.1));
		addStaticObject(new Wall(simulator, 0, -1.9, 3.5, 0.1));

		// exterior ao ninho horizontal menorr
		addStaticObject(new Wall(simulator, 0.8, -1.5, 1.1, 0.1));
		addStaticObject(new Wall(simulator, 0.8, 1.5, 1.1, 0.1));
		addStaticObject(new Wall(simulator, -0.8, -1.5, 1.1, 0.1));
		addStaticObject(new Wall(simulator, -0.8, 1.5, 1.1, 0.1));

		// exterior ao ninho vertical menores
		addStaticObject(new Wall(simulator, 1.3, 0, 0.1, 3.1));
		addStaticObject(new Wall(simulator, -1.3, 0, 0.1, 3.1));

		// exterior ao ninho vertical maiores
		addStaticObject(new Wall(simulator, 1.7, 0, 0.1, 3.8));
		addStaticObject(new Wall(simulator, -1.7, 0, 0.1, 3.8));
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);
		
		


	}

	private Vector2d newRandomPosition() {
		return ((int) (random.nextDouble() * 2) == 0) ? new Vector2d(
				random.nextDouble()
						* (MAX_X_LIMIT_FOR_PREY - MIN_X_LIMIT_FOR_PREY)
						+ MIN_X_LIMIT_FOR_PREY, random.nextDouble()
						* (3.5 - 0.2) - 1.5)
				: new Vector2d(random.nextDouble()
						* (-(MAX_X_LIMIT_FOR_PREY - MIN_X_LIMIT_FOR_PREY))
						- MIN_X_LIMIT_FOR_PREY, random.nextDouble()
						* (3.5 - 0.2) - 1.5);
	}

	@Override
	public void update(double time) {
		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			// double distance = nextPrey.getPosition().length();
			double distance = nextPrey.getPosition().distanceTo(nestPosition);
			if (nextPrey.isEnabled() && distance < 0.5) {
				// if(distance == 0){
				// System.out.println("ERRO--- zero");
				// }
				Vector2d position = newRandomPosition();
				nextPrey.teleportTo(position);
				numberOfFoodSuccessfullyForaged++;
				prey = nextPrey;
				change_PreyInitialDistance = true;
			}
		}

	}

	public int getNumberOfFoodSuccessfullyForaged() {
		// System.out.println("food foragin"+numberOfFoodSuccessfullyForaged);
		return numberOfFoodSuccessfullyForaged;
	}

	public boolean isToChange_PreyInitialDistance() {
		return change_PreyInitialDistance;
	}

	public Prey preyWithNewDistace() {

		return prey;
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
