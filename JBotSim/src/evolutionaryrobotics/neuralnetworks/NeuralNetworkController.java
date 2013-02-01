package evolutionaryrobotics.neuralnetworks;

import java.util.LinkedList;

import controllers.FixedLenghtGenomeEvolvableController;

import simulation.Controller;
import simulation.Simulator;
import simulation.robot.Robot;

public class NeuralNetworkController extends Controller implements FixedLenghtGenomeEvolvableController {
	protected NeuralNetwork neuralNetwork;

	// Robot robot;

	public NeuralNetworkController(Simulator simulator, Robot robot,
			NeuralNetwork neuralNetwork) {
		super(simulator, robot);
		this.neuralNetwork = neuralNetwork;
		// this.robot = robot;
	}

	public boolean isAlive() {
		return true;
	}

	@Override
	public void begin() {
	}

	@Override
	public void controlStep(int time) {
		neuralNetwork.controlStep(time);
	}

	@Override
	public void end() {
	}

	public int getRequiredNumberOfWeights() {
		return neuralNetwork.getRequiredNumberOfWeights();
	}
	
	public NeuralNetwork getNeuralNetwork() {
		return neuralNetwork;
	}
	
	@Override
	public void reset() {
		super.reset();
		neuralNetwork.reset();
	}

	@Override
	public void setNNWeights(double[] weights) {
		neuralNetwork.setWeights(weights);
//		setWeights(weights);		
	}

	public static void setNNWeights(LinkedList<Robot> robots, double[] weights) {
		for (Robot r : robots) {
			if (r.getEvolvingController() instanceof NeuralNetworkController){
				NeuralNetworkController nnController = (NeuralNetworkController) r.getEvolvingController();
				if (nnController != null)
					nnController.setNNWeights(weights);
			} //TODO: Miguel: you have to get the BehaviorController to implement the FixedLengthGenomeEvolvableController so that 
			  //              weights can be set tranparently.
			else if (r.getEvolvingController() instanceof BehaviorController){
				BehaviorController bController       = (BehaviorController) r.getEvolvingController();
				NeuralNetworkController nnController = (NeuralNetworkController) bController.getEvolvingController();
				nnController.setNNWeights(weights);
			}
		}	
	}

}
