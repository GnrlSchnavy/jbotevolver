package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.GroupedPreyEnvironment;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.sensors.PreySensor;
import simulation.util.Arguments;

public class ColorNearPreyEvaluationFunction extends EvaluationFunction{
	private double 	    fitness;
	private Vector2d    nestPosition = new Vector2d(0, 0);
	private double		forbidenArea;
	private double		foragingArea;
	private double		redFarPenalty;
	private double		redNearBonus;
	private double		movementPenalty;
	private double      preyDistanceRewardFactor;

	public ColorNearPreyEvaluationFunction(Simulator simulator, Arguments arguments) {
		super(simulator);
		forbidenArea    =  ((GroupedPreyEnvironment)(simulator.getEnvironment())).getForbiddenArea();
		foragingArea    =  ((GroupedPreyEnvironment)(simulator.getEnvironment())).getForageRadius();
		redFarPenalty   = (arguments.getArgumentIsDefined("redfarpenalty")) ? arguments.getArgumentAsDouble("redfarpenalty") : 0;
		redNearBonus    = (arguments.getArgumentIsDefined("rednearbonus")) ? arguments.getArgumentAsDouble("rednearbonus") : 0;
		movementPenalty = (arguments.getArgumentIsDefined("movementpenalty")) ? arguments.getArgumentAsDouble("movementpenalty") : 0;
		preyDistanceRewardFactor = (arguments.getArgumentIsDefined("preydistancerewardfactor")) ? arguments.getArgumentAsDouble("preydistancerewardfactor") : 0.001;
	}

	//@Override
	public double getFitness() {
		return fitness + ((GroupedPreyEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged() * 1.0;
	}
	
	/*
	private boolean haveForaged(){
		return ((GroupedPreyEnvironment)(Environment.getInstance())).getNumberOfFoodSuccessfullyForaged() > 0;
	}*/

	//@Override
	public void step() {			
		int numberOfRobotsWithPrey = 0;
		int numberOfRobotsBeyondForbidenLimit = 0;
		int numberOfRobotsBeyondForagingLimit = 0;
		int numberOfRedRobotsNearPrey = 0;
		int numberOfRedRobotsFarFromPrey = 0;
		
		double collectiveSpeed = 0;

		for(Robot r : simulator.getEnvironment().getRobots()){
			double distanceToNest = r.getPosition().distanceTo(nestPosition);
		
			if(distanceToNest > forbidenArea){
				numberOfRobotsBeyondForbidenLimit++;
			} else 	if(distanceToNest > foragingArea){
				numberOfRobotsBeyondForagingLimit++;
			}
			
			if (r.isCarryingPrey()) {
				numberOfRobotsWithPrey++;
			}
			
			//Check which robots are lighting up red near the prey
			if(redNearBonus != 0 && r.getBodyColorAsDoubles()[Robot.REDINDEX] > 0){
				 PreySensor preySensor = (PreySensor)r.getSensorByType("PreySensor");
				 
				 boolean nearPrey = false;
				 
				 for(int sensorNumber = 0 ; sensorNumber < preySensor.getNumberOfSensors() && !nearPrey; sensorNumber++){ 
					 nearPrey = preySensor.getSensorReading(sensorNumber) > 0;
				 }
				 
				 if(nearPrey)
					 numberOfRedRobotsNearPrey++;
				 else
					 numberOfRedRobotsFarFromPrey++;
					 
			}
			
			collectiveSpeed+= (Math.abs(r.getLeftWheelSpeed())+Math.abs(r.getRightWheelSpeed()))/2;			
		}
		
		double nestRadius         = ((GroupedPreyEnvironment)(simulator.getEnvironment())).getNestRadius();
		double forageRadius       = ((GroupedPreyEnvironment)(simulator.getEnvironment())).getForageRadius();
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


		fitness += (double) numberOfRobotsWithPrey * 0.001 + 
			numberOfRobotsBeyondForbidenLimit * -0.1 + 
			numberOfRobotsBeyondForagingLimit * -0.0001 +
			collectiveSpeed * -movementPenalty +
			numberOfRedRobotsNearPrey * redNearBonus +
			numberOfRedRobotsFarFromPrey * -redFarPenalty +
			preyDistanceReward * preyDistanceRewardFactor;
	}
}
