package tdg.moea.tests;

import java.net.URISyntaxException;
import java.util.Map;

import tdg.link_discovery.connector.sparql.engine.evaluator.deprecated.KFoldDumperInMemory;
import tdg.link_discovery.connector.sparql.engine.evaluator.deprecated.KFoldDumperEvaluator;
import tdg.link_discovery.connector.sparql.engine.evaluator.deprecated.KFoldEvaluatorInMemory;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.framework.configuration.ConfigurationReader;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.framework.environment.Environments;
import tdg.link_discovery.middleware.framework.learner.builder.LearnerBuilder;
import tdg.link_discovery.middleware.framework.learner.executor.LearnerExecutor;
import tdg.link_discovery.middleware.log.Logger;
import tdg.link_discovery.middleware.utils.Utils;

public class MainLSValidation {


	public static void main(String[] args) throws Exception, URISyntaxException {
		//org.apache.log4j.BasicConfigurator.configure();
		// Inputs
		String environmentFile =  "./experiments/environments/restaurantsZ.cnf";
		String algorithmSetupFile = "./experiments/algorithms/moea-genlink_setup.cnf";
		String agorithmDescription = "./experiments/algorithms/moea-genlink.cnf";
		Integer executionsNumber = 2;
		
		FrameworkConfiguration.REDUCE_SEARCH_SPACE_METRICS = false;
		FrameworkConfiguration.APPLY_STRING_TRANSFORMATIONS_BLOCK =  false;
		
		// Setting up
		IEnvironment environment = Environments.parseFromFile(environmentFile);
		FrameworkConfiguration.traceLog = new Logger(new StringBuffer("./experiments/").append("traceLog.txt").toString(), 1000000);		
		FrameworkConfiguration.resultsLog = new Logger(new StringBuffer("./experiments/").append("resultsLog.txt").toString(), 1000000);	
		
		
		long startTime = System.currentTimeMillis();
		String workingFolderName = 10+"-fold_datasets-"+Utils.getCurrentTime();
		KFoldEvaluatorInMemory evaluator = new KFoldEvaluatorInMemory(environment, 10, workingFolderName);		
		evaluator.foldDatasets(true, true, false, -1);
		
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("**"+elapsedTime);
		/*
		
		KFoldEvaluator evaluator = new KFoldEvaluator(environment, executionsNumber);
		Setup setup = ConfigurationReader.readSetupFromFile(algorithmSetupFile);
		Map<String,String> description = ConfigurationReader.readRawSetupFromFile(agorithmDescription);
		
		// Build learner from input
		LearnerBuilder builder = new LearnerBuilder(description, setup, environment);
		builder.getEngine().setEvaluator(evaluator);
		
		long startTime = System.currentTimeMillis();
		// Execute experiments n times
		LearnerExecutor executor = new LearnerExecutor();
		executor.executeLearner(builder, environment, executionsNumber);	

	 	long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("**"+elapsedTime);*/
	    
	}
	


}
