package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.BeeEnvironment;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.sensors.PreySensor;
import simulation.util.Arguments;
import experiments.Experiment;

public class BeeEvaluationFunction extends EvaluationFunction{
	private double 	    fitness = 0;
	private Vector2d    nestPosition = new Vector2d(0, 0);
	private double		forbiddenArea;
	private double		foragingArea;
	private double		nearPreyReward;
	private double		movementPenalty;
	private double      preyDistanceRewardFactor;
	private int         dieIfNoPreyEncounteredLimit;
	private int         dieIfNoPreyEncounteredSteps;
	private boolean     terminatedPrematurelyBecauseNoPreyEncountered = false;
	private boolean     terminatedPrematurelyBecauseAllRobotsDied     = false;
	private double      withPreyReward;
	private double      preyForagedReward;
	private double      noPreyFoundFitness;
	private double		deadBeePenalty;

	private int step = 0;

	public BeeEvaluationFunction(Simulator simulator,Arguments arguments) {
		super(simulator);
		forbiddenArea               =  ((BeeEnvironment)(simulator.getEnvironment())).getForbiddenArea();
		foragingArea                =  ((BeeEnvironment)(simulator.getEnvironment())).getForageRadius();
		nearPreyReward              = (arguments.getArgumentIsDefined("nearpreyreward")) ? arguments.getArgumentAsDouble("nearpreyreward") : 0;
		withPreyReward              = (arguments.getArgumentIsDefined("withpreyreward")) ? arguments.getArgumentAsDouble("withpreyreward") : 0;
		movementPenalty             = (arguments.getArgumentIsDefined("movementpenalty")) ? arguments.getArgumentAsDouble("movementpenalty") : 0;
		preyDistanceRewardFactor    = (arguments.getArgumentIsDefined("preydistancerewardfactor")) ? arguments.getArgumentAsDouble("preydistancerewardfactor") : 0.001;
		dieIfNoPreyEncounteredLimit = (arguments.getArgumentAsIntOrSetDefault("dieifnopreyencounteredlimit", Integer.MAX_VALUE));
		preyForagedReward           = arguments.getArgumentAsDoubleOrSetDefault("preyforagedreward", 5.0);
		noPreyFoundFitness          = arguments.getArgumentAsDoubleOrSetDefault("nopreyfoundfitness", 0);
		deadBeePenalty				= arguments.getArgumentAsDoubleOrSetDefault("deadbeepenalty", 0);

		dieIfNoPreyEncounteredSteps = dieIfNoPreyEncounteredLimit;
	}

	//@Override
	public double getFitness() {
		if (terminatedPrematurelyBecauseNoPreyEncountered) {
			return noPreyFoundFitness; 
		} else {
			return fitness
				+ ((BeeEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged() * preyForagedReward
				- ((BeeEnvironment)(simulator.getEnvironment())).deadBees() * deadBeePenalty;
		}
	}

	//@Override
	public void step() {
		
		printStuff();
		
		int numberOfRobotsWithPrey            = 0;
		int numberOfRobotsBeyondForbidenLimit = 0;
		int numberOfRobotsBeyondForagingLimit = 0;
		int numberOfRobotsNearPrey            = 0;
		int numberOfRobotsFarFromPrey         = 0;
		int numberOfRobots                    = simulator.getEnvironment().getRobots().size();
		
		double collectiveSpeed                = 0;
		
		boolean allDead = true;

		for(Robot r : simulator.getEnvironment().getRobots()) {
			double distanceToNest = r.getPosition().distanceTo(nestPosition);
			
			if(r.isEnabled()) 
				allDead = false;

			if(distanceToNest > forbiddenArea){
				numberOfRobotsBeyondForbidenLimit++;
			} else 	if(distanceToNest > foragingArea){
				numberOfRobotsBeyondForagingLimit++;
			}

			if (r.isCarryingPrey()) {
				numberOfRobotsWithPrey++;
			} else {

				PreySensor preySensor = (PreySensor)r.getSensorByType("PreySensor");

				boolean nearPrey = false;

				for(int sensorNumber = 0 ; sensorNumber < preySensor.getNumberOfSensors() && !nearPrey; sensorNumber++){ 
					nearPrey = preySensor.getSensorReading(sensorNumber) > 0;
				}

				if(nearPrey)
					numberOfRobotsNearPrey++;
				else
					numberOfRobotsFarFromPrey++;
			}					 			
			collectiveSpeed += (Math.abs(r.getLeftWheelSpeed())+Math.abs(r.getRightWheelSpeed()))/2;			
		}
		
		if (allDead) {
			terminatedPrematurelyBecauseAllRobotsDied = true; 
			simulator.getExperiment().endExperiment();
//			System.out.println("all dead");
		}

		if (numberOfRobotsNearPrey == 0 && numberOfRobotsWithPrey == 0 && ((BeeEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged() == 0) {
			if (--dieIfNoPreyEncounteredSteps == 0) {
				terminatedPrematurelyBecauseNoPreyEncountered = true;
				simulator.getExperiment().endExperiment();
			} 	
		} else {
			dieIfNoPreyEncounteredSteps = dieIfNoPreyEncounteredLimit;
		}

		double nestRadius         = ((BeeEnvironment)(simulator.getEnvironment())).getNestRadius();
		double forageRadius       = ((BeeEnvironment)(simulator.getEnvironment())).getForageRadius();
		double preyDistanceReward = 0;
		int    preyCounted        = 0;
		for(Prey prey : simulator.getEnvironment().getPrey()){
			double length = prey.getPosition().length();

			if(length > nestRadius) {
				length             -= nestRadius;		
				preyDistanceReward += (forageRadius - length) / forageRadius;
				preyCounted++;
			}
		}

		if (preyCounted > 0)
			preyDistanceReward /= preyCounted;


		if (numberOfRobots > 0) {
			fitness += (double) (numberOfRobotsWithPrey * withPreyReward) / (double) numberOfRobots + 
						(numberOfRobotsBeyondForbidenLimit * -0.1) / (double) numberOfRobots + 
						(numberOfRobotsBeyondForagingLimit * -0.0001) / (double) numberOfRobots +
						(collectiveSpeed * -movementPenalty) / (double) numberOfRobots +
						(numberOfRobotsNearPrey * nearPreyReward) / (double) numberOfRobots +
						(numberOfRobotsWithPrey * withPreyReward) / (double) numberOfRobots + 
						preyDistanceReward * preyDistanceRewardFactor;
		}
	}
	
	private void printStuff(){
		/*
		step++;
		Robot r = simulator.getEnvironment().getRobots().get(0);
		
		double maxNest = 0;
		double current = 0;
		for(int i = 0 ; i < 8 ; i++){
			current = r.getSensorWithId(1).getSensorReading(i);
			if(current > maxNest) maxNest = current;
		}
		
		double maxPrey = 0;
		for(int i = 0 ; i < 8 ; i++){
			current = r.getSensorWithId(2).getSensorReading(i);
			if(current > maxPrey) maxPrey = current;
		}
		
		System.out.println(step +" "+(r.getBodyColorAsDoubles()[Robot.REDINDEX])+" "+r.isCarryingPrey() + " "+
				maxNest +" "+maxPrey);*/
	}
}
