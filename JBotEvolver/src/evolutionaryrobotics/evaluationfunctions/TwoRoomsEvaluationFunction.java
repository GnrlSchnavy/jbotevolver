package evolutionaryrobotics.evaluationfunctions;

import simulation.Simulator;
import simulation.environment.TwoRoomsEnvironment;
import simulation.util.Arguments;

public class TwoRoomsEvaluationFunction extends EvaluationFunction {
	
	private int picks = 0;
	private double fitness = 0;
	private double timeAlive = 0;
	private double maxNumberOfSteps = 500;
	boolean punishCollision = true;
	boolean allowCollision = false;
	boolean rewardStepsAlive = true;

	public TwoRoomsEvaluationFunction(Simulator simulator, Arguments arguments) {
		super(simulator);
		punishCollision = arguments.getArgumentAsIntOrSetDefault("punishcollision", 1) == 1;
		allowCollision = arguments.getArgumentAsIntOrSetDefault("allowcollision", 0) == 1;
		
		rewardStepsAlive = arguments.getArgumentAsIntOrSetDefault("rewardstepsalive", 1) == 1;
		maxNumberOfSteps = arguments.getArgumentAsDoubleOrSetDefault("maxnumberofsteps", maxNumberOfSteps);
	}

	@Override
	public double getFitness() {
		return fitness;
	}

	@Override
	public void step() {
		timeAlive++;
		picks = ((TwoRoomsEnvironment)simulator.getEnvironment()).getNumberOfPicks();
		
		fitness = picks;
		
		if(rewardStepsAlive)
			fitness+= (timeAlive/maxNumberOfSteps)*10;
		
		if(!allowCollision && simulator.getEnvironment().getRobots().get(0).isInvolvedInCollison()) {
			if(punishCollision)
				fitness /=  2;
			simulator.getExperiment().endExperiment();
		}
	}

}
