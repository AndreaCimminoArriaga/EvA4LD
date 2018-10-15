package tdg.moea.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


import tdg.link_discovery.connector.sparql.engine.evaluator.KFoldEvaluatorManualSamples;
import tdg.link_discovery.connector.sparql.engine.evaluator.linker.LinkerKFoldCacheTDB;
import tdg.link_discovery.connector.sparql.engine.translator.SparqlTranslator;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Avg;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.learner.ILearner;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.framework.configuration.ConfigurationReader;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.framework.environment.Environments;
import tdg.link_discovery.middleware.framework.learner.builder.LearnerBuilder;
import tdg.link_discovery.middleware.log.Logger;
import tdg.link_discovery.middleware.moea.genetics.algorithms.GeneticProgrammingAlgorithm;
import tdg.link_discovery.middleware.moea.genetics.problem_statement.LinkSpecificationDiscovery;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;


public class TestSingletonManualSamples {

	public static void main(String[] args) throws Exception {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		FrameworkConfiguration.TERMINAL_LOG = true;
		FrameworkConfiguration.DECIMAL_PRECISION = 2;
		
		KFoldEvaluatorManualSamples.DEFAULT_THREAD_POOL_SIZE = 1;//Runtime.getRuntime().availableProcessors();	// given a kFoldSize, the number of referenceLinks that can be retrieved and stored in parallel
		LinkerKFoldCacheTDB.DEFAULT_THREAD_POOL_SIZE=90;//Runtime.getRuntime().availableProcessors(); // given a link rule, the number of instances that can be checked in parallel
		// the number of link specifications that can be evaluated in parallel and the number variations that can be executed in parallel
		GeneticProgrammingAlgorithm.DEFAULT_THREAD_POOL_SIZE_VARIATIONS =90;// Runtime.getRuntime().availableProcessors();
		
	
		String environmentFile = args[0].trim();//"./experiments/environments/persons1.cnf";
		String algorithm =  args[1].trim();//"./experiments/algorithms/moea-gen1.cnf";//args[1].trim();
		String setupNumberStr =  args[2].trim();//"48";//args[2].trim();
		String folds = "2";// args[3].trim();

		
		
		runOverEnvironment(environmentFile, algorithm, folds, "1", Integer.valueOf(setupNumberStr));
	
		
	}
	
	private static void runOverEnvironment(String envorinmentDirectoy, String algorithmDirectory, String kFoldSizeStr, String execution, Integer index) throws Exception {
		String environmentFile =  Paths.get(envorinmentDirectoy).toString();//"./experiments/environments/restaurants.cnf";
		String algorithmSetupFile = Paths.get(algorithmDirectory.replace(".cnf", "_setup"+index+".cnf")).toString();//"./experiments/algorithms/moea-eagle_setup.cnf";
		String agorithmDescription = Paths.get(algorithmDirectory).toString();;
		Integer executionsNumber = Integer.valueOf(execution);
		Integer kFoldSize = Integer.valueOf(kFoldSizeStr);
	
		FrameworkConfiguration.REDUCE_SEARCH_SPACE_METRICS = false;
		FrameworkConfiguration.APPLY_STRING_TRANSFORMATIONS_BLOCK =  false;
		
		// Setting up
		IEnvironment environment = Environments.parseFromFile(environmentFile);
		Setup setup = ConfigurationReader.readSetupFromFile(algorithmSetupFile);
		Map<String,String> description = ConfigurationReader.readRawSetupFromFile(agorithmDescription);
		
		
		
		// Execute
		String workingFolderName = "/setup"+index+"/"+kFoldSize+"-fold_datasets-"+Utils.getCurrentTime();
		initFrameworkLog(workingFolderName, environment, setup, kFoldSize, executionsNumber,algorithmSetupFile, agorithmDescription, environmentFile); 
		FrameworkConfiguration.traceLog.addLogLine("Main", " ----  Starting execution: "+(execution)+"/"+executionsNumber);
		FrameworkConfiguration.resultsLog.addLogLine("Main", " ----  Starting execution: "+(execution)+"/"+executionsNumber);
		// Execute fold-times the algorithm
		long startTime = System.nanoTime();
		executeLearner(workingFolderName, environment, kFoldSize, setup, description);
		long stopTime = System.nanoTime();
	    long elapsedTime = (stopTime - startTime)/1000000;
	    //System.out.println("Execution "+execution+"/"+executionsNumber+" finished in "+elapsedTime);
	    System.out.println("Execution finished: "+elapsedTime+" (ms)");
	}
	
	
	
	
	
	
	private static void executeLearner(String workingFolderName, IEnvironment environment, Integer kFoldSize, Setup setup, Map<String,String> description) {
		Integer validationCounter =0;
		KFoldEvaluatorManualSamples evaluator = new KFoldEvaluatorManualSamples(environment, kFoldSize, workingFolderName);		
		
		
		while(validationCounter<=kFoldSize) {
			FrameworkConfiguration.traceLog.addLogLine("Main", "Start learning with fold: "+(1+validationCounter)+"/"+kFoldSize);
			FrameworkConfiguration.resultsLog.addLogLine("Main", "Start learning with fold: "+(1+validationCounter)+"/"+kFoldSize);
			// Create kFolder
			LearnerBuilder builder = new LearnerBuilder(description, setup, environment);
			builder.getEngine().setEvaluator(evaluator);
			// Build learner from input
			ILearner learner = builder.getLearner();
			learner.getEngine().setEvaluator(evaluator);
			//Init attribute learner
			if(learner.getAttributeSelector()!=null) {
				Set<Tuple<String,String>> positiveReferenceLinks = evaluator.getTrainingPositiveReferenceLinks();
				Set<Tuple<String,String>> negativeReferenceLinks = evaluator.getTrainingNegativeReferenceLinks();
				learner.getAttributeSelector().setPositiveExamples(positiveReferenceLinks);
				learner.getAttributeSelector().setNegativeExamples(negativeReferenceLinks);
			}
			
			learner.learnSpecifications();
			List<ISpecification<Tree>> specifications = learner.getLearnedSpecifications();
			Set<String> processed = Sets.newHashSet();
			FrameworkConfiguration.traceLog.addLogLine("Main", "Validation of resultant rules: ");
			FrameworkConfiguration.resultsLog.addLogLine("Main", "Validation of resultant rules: ");
			long startTime = System.nanoTime();
			List<Double> precision = Lists.newArrayList(); 
			List<Double> recall = Lists.newArrayList(); 
			List<Double> fMeasure = Lists.newArrayList(); 
			//TODO : SELECT SPECIFICATION WITH BEST F-MEASURE AND EXECUTE IT OVER THE HOWLE DATASETS
			for(ISpecification<Tree> specification:specifications) {
					SparqlTranslator translator = new SparqlTranslator();
					evaluator.apply(translator.translate(specification));
					ConfusionMatrix results = evaluator.getValidationMatrix();
					processed.add(specification.toString());
					// log stuff
					FrameworkConfiguration.traceLog.addLogLine("Main", "\t rule: "+specification.toString().replaceAll("∂", "").replaceAll("ß", ""));
					FrameworkConfiguration.traceLog.addLogLine("Main", "\t results: confusionMatrix="+results+" P="+results.getPrecision()+" R="+results.getRecall()+" F="+results.getFMeasure());
					FrameworkConfiguration.traceLog.addLogLine("Main", "\t .");
					FrameworkConfiguration.resultsLog.addLogLine("Main", "\t rule: "+specification.toString().replaceAll("∂", "").replaceAll("ß", ""));
					FrameworkConfiguration.resultsLog.addLogLine("Main", "\t results: confusionMatrix="+results+" P="+results.getPrecision()+" R="+results.getRecall()+" F="+results.getFMeasure());
					FrameworkConfiguration.resultsLog.addLogLine("Main", "\t .");
					precision.add(results.getPrecision());
					recall.add(results.getRecall());
					fMeasure.add(results.getFMeasure());
			}
			long stopTime = System.nanoTime();
		    long elapsedTime = (stopTime - startTime)/1000000;
		    FrameworkConfiguration.traceLog.addLogLine("Main", "Validation executed in "+elapsedTime+" ms");
		    FrameworkConfiguration.resultsLog.addLogLine("Main", "Validation executed in "+elapsedTime+" ms");
		    
			FrameworkConfiguration.traceLog.addLogLine("Main", "Validation summary of resultant rules:");
			FrameworkConfiguration.traceLog.addLogLine("Main", "\t avrg(P): "+getMean(precision)+", variance(P): "+getVariance(precision)+", stdDev(P): "+getStdDev(precision)+"  -> "+precision );
			FrameworkConfiguration.traceLog.addLogLine("Main", "\t avrg(R): "+getMean(recall)+", variance(R): "+getVariance(recall)+", stdDev(R): "+getStdDev(recall)+"  -> "+recall);
			FrameworkConfiguration.traceLog.addLogLine("Main", "\t avrg(F): "+getMean(fMeasure)+", variance(F): "+getVariance(fMeasure)+", stdDev(F): "+getStdDev(fMeasure)+"  -> "+fMeasure);

			
			FrameworkConfiguration.resultsLog.addLogLine("Main", "Validation summary of resultant rules:");
			FrameworkConfiguration.resultsLog.addLogLine("Main", "\t avrg(P): "+getMean(precision)+", variance(P): "+getVariance(precision)+", stdDev(P): "+getStdDev(precision));
			FrameworkConfiguration.resultsLog.addLogLine("Main", "\t avrg(R): "+getMean(recall)+", variance(R): "+getVariance(recall)+", stdDev(R): "+getStdDev(recall));
			FrameworkConfiguration.resultsLog.addLogLine("Main", "\t avrg(F): "+getMean(fMeasure)+", variance(F): "+getVariance(fMeasure)+", stdDev(F): "+getStdDev(fMeasure));
			
			Boolean chaginDatasets = evaluator.changeValidationDataset();
			if(chaginDatasets) {
				validationCounter++;
				LinkSpecificationDiscovery.solutionsCache.clear();
				//break; // add this if you do not want to change validation dataset 
			}else {
				System.out.println("breaking");
				break;
			}
			
			
		}
		
		
		// Save last log lines & copy experiments folder to backup directory
		String foldedDataDirectory = Utils.getAbsoluteSystemPath(workingFolderName);
		
		FrameworkConfiguration.traceLog.addLogLine("Main", "Saving results file in folder '"+foldedDataDirectory+"'");
		FrameworkConfiguration.resultsLog.addLogLine("Main", "Saving results file in folder '"+foldedDataDirectory+"'");
		FrameworkConfiguration.resultsLog.writeCurrentCachedLines();
		FrameworkConfiguration.traceLog.writeCurrentCachedLines();	
		
	}

	
	private static void initFrameworkLog(String workingFolderName, IEnvironment environment, Setup setup, Integer kFoldSize, Integer executionsNumber, String algorithmSetupFile, String agorithmDescription, String environmentFile) {
		StringBuffer logsDirectories = new StringBuffer();
		logsDirectories.append(FrameworkConfiguration.FRAMEWORK_WORKSPACE_DIRECTORY).append("/results/").append(environment.getName()).append("/").append(workingFolderName).append("/");
		logsDirectories = new StringBuffer(Utils.getAbsoluteSystemPath(logsDirectories.toString()));
		if(logsDirectories.toString().contains("\\") && !logsDirectories.toString().endsWith("\\")) {
			logsDirectories =  new StringBuffer(logsDirectories.toString()+"\\");
		}else if (logsDirectories.toString().contains("/") && !logsDirectories.toString().endsWith("/") ){
			logsDirectories =  new StringBuffer(logsDirectories.toString()+"/");
		}
		
		FrameworkConfiguration.traceLog = new Logger(new StringBuffer(logsDirectories).append("traceLog.txt").toString(), 10000);		
		FrameworkConfiguration.resultsLog = new Logger(new StringBuffer(logsDirectories).append("resultsLog.txt").toString(), 10000);	
		
		
		FrameworkConfiguration.traceLog.addLogLine("Main", " ----  Starting execution");
		FrameworkConfiguration.traceLog.addLogLine("Main", "KFoldValidation size:"+kFoldSize);
		FrameworkConfiguration.traceLog.addLogLine("Main", "Executions:"+executionsNumber);
		FrameworkConfiguration.traceLog.addLogLine("Main", "Environment: ");
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t file: "+environmentFile);
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t experiment_name: "+environment.getName());
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t source_dataset: "+environment.getSourceDatasetFile());
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t target_dataset: "+environment.getTargetDatasetFile());
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t source_restrictions: "+environment.getSourceRestrictions());
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t target_restrictions: "+environment.getTargetRestrictions());
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t suitable_attributes: ");
		environment.getSuitableAttributes().forEach(attr -> FrameworkConfiguration.traceLog.addLogLine("Main", "\t\t attr pair: "+attr));
		FrameworkConfiguration.traceLog.addLogLine("Main", "Setup: ");
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t file: "+algorithmSetupFile);
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t max_iterations: "+setup.getMaxIterations());
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t generations: "+setup.getGenerationsRate());
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t population_size: "+setup.getPopulationSize());
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t selector_arity: "+setup.getParentsSelectionSize());
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t crossover_rate: "+setup.getCrossoverRate());
		FrameworkConfiguration.traceLog.addLogLine("Main", "\t mutation_rate: "+setup.getMutationRate());
		FrameworkConfiguration.traceLog.addLogLine("Main", "Algorithm:");
		FrameworkConfiguration.traceLog.addLogLine("Main", "file: "+agorithmDescription);
		
		FrameworkConfiguration.resultsLog.addLogLine("Main", " ----  Starting execution");
		FrameworkConfiguration.resultsLog.addLogLine("Main", "KFoldValidation size:"+kFoldSize);
		FrameworkConfiguration.resultsLog.addLogLine("Main", "Executions:"+executionsNumber);
		FrameworkConfiguration.resultsLog.addLogLine("Main", "Environment: ");
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t file: "+environmentFile);
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t experiment_name: "+environment.getName());
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t source_dataset: "+environment.getSourceDatasetFile());
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t target_dataset: "+environment.getTargetDatasetFile());
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t source_restrictions: "+environment.getSourceRestrictions());
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t target_restrictions: "+environment.getTargetRestrictions());
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t suitable_attributes: ");
		environment.getSuitableAttributes().forEach(attr -> FrameworkConfiguration.traceLog.addLogLine("Main", "\t\t attr pair: "+attr));
		FrameworkConfiguration.resultsLog.addLogLine("Main", "Setup: ");
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t file: "+algorithmSetupFile);
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t max_iterations: "+setup.getMaxIterations());
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t generations: "+setup.getGenerationsRate());
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t population_size: "+setup.getPopulationSize());
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t selector_arity: "+setup.getParentsSelectionSize());
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t crossover_rate: "+setup.getCrossoverRate());
		FrameworkConfiguration.resultsLog.addLogLine("Main", "\t mutation_rate: "+setup.getMutationRate());
		FrameworkConfiguration.resultsLog.addLogLine("Main", "Algorithm:");
		FrameworkConfiguration.resultsLog.addLogLine("Main", "file: "+agorithmDescription);
	}
	
	
	private static Double getMean(List<Double> numbers) {
		 Avg avg = new Avg();
		 return Utils.roundDecimal(avg.applyAggregation(numbers), 7);
	}
	
	private static double getVariance( List<Double> numbers){
	   double mean = getMean(numbers);
	   double temp = 0;
	   for(double a :numbers)
	       temp += (a-mean)*(a-mean);
	   return Utils.roundDecimal(temp/(numbers.size()-1), 7);
	}
	
	private static double getStdDev(List<Double> numbers){
	   return Utils.roundDecimal(Math.sqrt(getVariance(numbers)), 7);
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