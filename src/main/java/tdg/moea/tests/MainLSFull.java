package tdg.moea.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Maps;


import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.framework.configuration.ConfigurationReader;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.framework.learner.builder.LearnerBuilder;
import tdg.link_discovery.middleware.framework.learner.executor.LearnerExecutor;

public class MainLSFull {

	public static void main(String[] args) {
		
		
		// Inputs
		String agorithmDescription = args[0];//"./experiments/algorithms/moea-genlink.cnf";
		String algorithmSetupFile = args[1];//"./experiments/algorithms/moea_genlink_setup.cnf";
		
		FrameworkConfiguration.REDUCE_SEARCH_SPACE_METRICS = Boolean.valueOf(args[2]);//true;
		FrameworkConfiguration.APPLY_STRING_TRANSFORMATIONS_BLOCK =  Boolean.valueOf(args[3]);//true;
		
		Map<String,Integer> environmentFiles = readEnvironmentFiles("./experiments_table.txt");
		
		for(String environmentFile:environmentFiles.keySet()){
			Integer executionsNumber = environmentFiles.get(environmentFile);
			// Setting up
			IEnvironment environment = ConfigurationReader.readIEnvironmentFromFile(environmentFile);		
			Setup setup = ConfigurationReader.readSetupFromFile(algorithmSetupFile);
			Map<String,String> description = ConfigurationReader.readRawSetupFromFile(agorithmDescription);
			// Build learner from input
			LearnerBuilder builder = new LearnerBuilder(description, setup, environment);
					
			// Execute experiments n times
			LearnerExecutor executor = new LearnerExecutor();
			executor.executeLearner(builder, environment, executionsNumber);	
		}
	}
	
	
	private static Map<String,Integer> readEnvironmentFiles(String file){
		Map<String,Integer> environmentFiles = Maps.newHashMap();
		try{
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			      if(!line.isEmpty() && !line.startsWith("#")){
			    	  String [] splittedLine = line.split(",");
			    	  String environmentFile = splittedLine[0].trim();
			    	  Integer executionsNumber = Integer.valueOf(splittedLine[1].trim());
			    	  environmentFiles.put(environmentFile, executionsNumber);
			      }
			    }
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		
		
		return environmentFiles;
	}

}
