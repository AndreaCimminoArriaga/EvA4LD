package tdg.moea.tests;

import java.net.URISyntaxException;
import java.util.Map;

import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.framework.configuration.ConfigurationReader;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.framework.environment.Environments;
import tdg.link_discovery.middleware.framework.learner.builder.LearnerBuilder;
import tdg.link_discovery.middleware.framework.learner.executor.LearnerExecutor;
import tdg.link_discovery.middleware.log.Logger;

public class MainLS {


	public static void main(String[] args) throws Exception, URISyntaxException {
		//org.apache.log4j.BasicConfigurator.configure();
		// Inputs
		String environmentFile =  "./experiments/environments/restaurants.cnf";
		String algorithmSetupFile = "./experiments/algorithms/moea-genlink_setup.cnf";
		String agorithmDescription = "./experiments/algorithms/moea-genlink.cnf";
		Integer executionsNumber = 1;
		
		FrameworkConfiguration.REDUCE_SEARCH_SPACE_METRICS = false;
		FrameworkConfiguration.APPLY_STRING_TRANSFORMATIONS_BLOCK =  false;
		
		FrameworkConfiguration.traceLog = new Logger("",1);
		FrameworkConfiguration.resultsLog = new Logger("",1);
		// Setting up
		//IEnvironment environment = ConfigurationReader.readIEnvironmentFromFile(environmentFile);		// TODO: REMOVE ConfigurationReader CLASS
		IEnvironment environment = Environments.parseFromFile(environmentFile);
		
		Setup setup = ConfigurationReader.readSetupFromFile(algorithmSetupFile);
		Map<String,String> description = ConfigurationReader.readRawSetupFromFile(agorithmDescription);
		// Build learner from input
		LearnerBuilder builder = new LearnerBuilder(description, setup, environment);
		
		long startTime = System.currentTimeMillis();
		// Execute experiments n times
		LearnerExecutor executor = new LearnerExecutor();
		executor.executeLearner(builder, environment, executionsNumber);	

	 	long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println("**"+elapsedTime);
	    
		
		
		
	}
	


}
