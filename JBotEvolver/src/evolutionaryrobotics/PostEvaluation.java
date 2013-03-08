package evolutionaryrobotics;

import java.io.File;
import taskexecutor.TaskExecutor;
import taskexecutor.results.PostEvaluationResult;
import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.PostEvaluationTask;

public class PostEvaluation {
	
	private int startTrial = 0;
	private int maxTrial = 0;
	private int samples = 100;
	private int fitnesssamples = 1;
	private double targetfitness = 0;
	private String dir = "";
	
	private TaskExecutor taskExecutor;
	private String args ="";
	
	public PostEvaluation(String[] args) {
		
		for(String s : args) {
			String[] a = s.split("=");
			if(a[0].equals("dir")) dir = a[1];
			if(a[0].equals("samples")) samples = Integer.parseInt(a[1]);
			if(a[0].equals("fitnesssamples")) fitnesssamples = Integer.parseInt(a[1]);
			if(a[0].equals("targetfitness")) targetfitness = Double.parseDouble(a[1]);
		}
		
		if(!dir.endsWith("/"))
			dir+="/";
		
		File subFolder;
		int currentFolder = 0;
		do {
			currentFolder++;
			subFolder = new File(dir+currentFolder);
		}while(subFolder.exists());
		
		startTrial = 1;
		maxTrial = --currentFolder;
	}
	
	public double[][] runPostEval() {

		double[][] result = new double[maxTrial][fitnesssamples];
		
		try {
		
			String file = dir+startTrial+"/_showbest_current.conf";
			JBotEvolver jBotEvolver = new JBotEvolver(new String[]{file});
			
			if (jBotEvolver.getArguments().get("--executor") != null) {
				taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
				taskExecutor.prepareArguments(jBotEvolver.getArguments());
				taskExecutor.start();
			}
		
			for(int i = startTrial ; i <= maxTrial ; i++) {
				
				file = dir+i+"/_showbest_current.conf";
				jBotEvolver = new JBotEvolver(new String[]{file});
				
				taskExecutor.prepareArguments(jBotEvolver.getArguments());
				
				for(int fitnesssample = 0 ; fitnesssample < fitnesssamples ; fitnesssample++) {
					JBotEvolver newJBot = new JBotEvolver(jBotEvolver.getArgumentsCopy(),jBotEvolver.getRandomSeed());
					PostEvaluationTask t = new PostEvaluationTask(i,newJBot,fitnesssample,newJBot.getPopulation().getBestChromosome(),samples,targetfitness);
					taskExecutor.addTask(t);
				}
			}
			
			for(int i = startTrial ; i <= maxTrial ; i++) {
				for(int fitnesssample = 0 ; fitnesssample < fitnesssamples ; fitnesssample++) {
					PostEvaluationResult sfr = (PostEvaluationResult)taskExecutor.getResult();
					result[sfr.getRun()-1][sfr.getFitnesssample()] = sfr.getFitness();
				}
			}
				
			taskExecutor.stopTasks();
			
		} catch(Exception e) {e.printStackTrace();}
		
		return result;
	}
}