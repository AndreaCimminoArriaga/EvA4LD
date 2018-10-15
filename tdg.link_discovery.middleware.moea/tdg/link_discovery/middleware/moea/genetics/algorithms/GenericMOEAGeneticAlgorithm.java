package tdg.link_discovery.middleware.moea.genetics.algorithms;


import java.util.List;
import java.util.stream.Collectors;

import org.moeaframework.core.Solution;

import com.google.common.collect.Lists;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Avg;
import tdg.link_discovery.framework.algorithm.AbstractAlgorithm;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.engine.IEngine;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.framework.algorithm.statistics.AlgorithmStatistics;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.moea.algorithm.individual.LinkSpecification;
import tdg.link_discovery.middleware.moea.algorithm.individual.initializer.GenericInitializator;
import tdg.link_discovery.middleware.moea.genetics.problem_statement.LinkSpecificationDiscovery;
import tdg.link_discovery.middleware.objects.comparators.DoubleNaturalComparator;
import tdg.link_discovery.middleware.utils.Utils;

public class GenericMOEAGeneticAlgorithm extends AbstractAlgorithm{
	
	
	private Boolean elapseTimeShowed;
	
	public GenericMOEAGeneticAlgorithm(){
		super();
	}
	
	public GenericMOEAGeneticAlgorithm(IEngine engine, Setup setup){
		super("moea-generic-empty-algorithm", engine, setup, null);
	}

	@Override
	public void learnSpecifications() {
		elapseTimeShowed = false;
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), " ----  Starting genetic programming algorithm");
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), " ---- Starting genetic programming algorithm");
		if(FrameworkConfiguration.TERMINAL_LOG)
			System.out.println("Starting genetic algorithm");
		this.algorithmStatistics = new AlgorithmStatistics();
		this.results = Lists.newArrayList();
		this.bestResults = Lists.newArrayList();
		
		Integer variables = setup.getVariables();
		Integer objectives = setup.getObjectives();
		Integer populationSize = setup.getPopulationSize();
		Integer max_iterations = setup.getMaxIterations();
		Integer generations = setup.getGenerationsRate();
		Boolean stopByGeneration = generations!=null;
		this.algorithmStatistics.setMaximumIterations(max_iterations);
		operatorsSetup.getSelector().setArity(setup.getParentsSelectionSize());
		//Problem statement
		LinkSpecificationDiscovery problem = new LinkSpecificationDiscovery(variables, objectives, engine, operatorsSetup.getSpecificationInitializer(), operatorsSetup.getFitness());
		// Algorithm creation
		GenericInitializator initializator = new GenericInitializator(operatorsSetup.getSpecificationInitializer(), populationSize, objectives, variables);
	
		GeneticProgrammingAlgorithm algorithm = new GeneticProgrammingAlgorithm(name, problem, this.operatorsSetup, initializator);
		// Algorithm Execution
		List<Double> lastFitnessScores = Lists.newArrayList();
		int nonImprovingScores = 0;
		int current_iteration = 0;
		Double max_iterations_double = Double.valueOf(max_iterations);
		long startTrainingTime = System.nanoTime();
		while (current_iteration < max_iterations) {
			long startTime = System.nanoTime();
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t iteration: "+current_iteration+"/"+max_iterations);
			//Execute iteration
			algorithm.step();
			current_iteration++;
			
			// Update statistics
			Double doubleIterations = Double.valueOf(current_iteration);
			this.algorithmProgress=Utils.roundDecimal(doubleIterations/max_iterations_double, 6)*100;
			if(FrameworkConfiguration.TERMINAL_LOG)
				System.out.println("Progress: "+this.algorithmProgress);
			List<Double> lastFitnessScoresTmp = algorithm.getLastScoring();
			//this.algorithmStatistics.addFitness(lastFitnessScoresTmp);
			//this.algorithmStatistics.addFitnessEvolution(current_iteration, lastFitnessScoresTmp);
			// Check stop criteria: stop condition or generations reached
			if(current_iteration == 1){
				lastFitnessScores = algorithm.getLastScoring();
			}else{
				if(lastFitnessScores.equals(lastFitnessScoresTmp)){
					nonImprovingScores++;
				}else{
					nonImprovingScores=0;
				}
				if( stopByGeneration && nonImprovingScores==generations){
					FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t iteration: "+current_iteration+" - STOP BY GENERATION WITHOUT IMPROVEMENT REACHED!");
					FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t iteration: "+current_iteration+" - STOP BY GENERATION WITHOUT IMPROVEMENT REACHED!");
					break;
				}else{
					lastFitnessScores = lastFitnessScoresTmp;
				}
			}
			if(lastFitnessScores.contains(0.0)) {
				FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t iteration: "+current_iteration+" - STOP BY MAX FITNESS REACHED!");
				FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t iteration: "+current_iteration+" - STOP BY MAX FITNESS REACHED!");
				break;
			}
				
			
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t avg(fitness): "+Utils.roundDecimal(getMean(lastFitnessScores), FrameworkConfiguration.DECIMAL_PRECISION)
																										   +", variance(fitness): "+Utils.roundDecimal(getVariance(lastFitnessScores), FrameworkConfiguration.DECIMAL_PRECISION)
																										   +", stdDev(fitness): "+Utils.roundDecimal(getStdDev(lastFitnessScores), FrameworkConfiguration.DECIMAL_PRECISION));
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t population fitness: "+lastFitnessScores);
			
			FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t iteration: "+current_iteration+"/"+max_iterations);
			FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t\t avg(fitness): "+Utils.roundDecimal(getMean(lastFitnessScores), FrameworkConfiguration.DECIMAL_PRECISION)
			   +", variance(fitness): "+Utils.roundDecimal(getVariance(lastFitnessScores), FrameworkConfiguration.DECIMAL_PRECISION)
			   +", stdDev(fitness): "+Utils.roundDecimal(getStdDev(lastFitnessScores), FrameworkConfiguration.DECIMAL_PRECISION));
			FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t\t population fitness: "+lastFitnessScores);
			
			long stopTime = System.nanoTime();
		    long elapsedTime = (stopTime - startTime)/1000000;
		    FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t iteration "+current_iteration+" executed in "+elapsedTime+" ms -> elapseTimeEstimated(ms): "+(elapsedTime*(max_iterations_double-current_iteration)));
		    FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t iteration "+current_iteration+" executed in "+elapsedTime+" ms -> elapseTimeEstimated(ms): "+(elapsedTime*(max_iterations_double-current_iteration)));
			if(current_iteration == 2) {
			    if(!elapseTimeShowed && FrameworkConfiguration.TERMINAL_LOG)
					System.out.println("\t elapseTimeEstimated(ms): "+(elapsedTime*(max_iterations_double-current_iteration)));
				elapseTimeShowed = true;
			}
		}
		
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Learning task finished (training)");
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "Learning task finished (training)");
		
		//Store training time
		long stopTraigingTime = System.nanoTime();
	    long elapsedTraningTime = (stopTraigingTime - startTrainingTime)/1000000;
	    FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Learning (training) execution time (ms): "+elapsedTraningTime);
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "Learning (training) execution time (ms): "+elapsedTraningTime);
		
		
		
	    
		// Store other statistics
		//this.algorithmStatistics.setTrainingTime(elapsedTraningTime);
		//this.algorithmStatistics.setExecutedIterations(current_iteration);
		//this.algorithmStatistics.setAlgorithmSetup(operatorsSetup);
		
		// Retrieving results
		List<Solution> rankedFinal = Lists.newArrayList(algorithm.getCurrentPopulation());
		this.results = rankedFinal.stream().map(sol -> (LinkSpecification) sol.getVariable(0)).collect(Collectors.toList());
		
		// Retrieving best results
		Double bestScore = rankedFinal.stream().map(sol -> sol.getObjective(0)).collect(Collectors.minBy(new DoubleNaturalComparator())).get();
		this.bestResults = rankedFinal.stream().filter(sol-> sol.getObjective(0)==bestScore).map(sol -> (LinkSpecification) sol.getVariable(0)).collect(Collectors.toList());
		logResults(rankedFinal, bestResults);
	
	}

	private void logResults(List<Solution> rankedFinal, List<ISpecification<?>> bestResults) {
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "--- Learning summary");
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Final population: ");
		rankedFinal.forEach(rule -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t fitness: "+rule.getObjective(0)+", rule: "+rule.getVariable(0).toString().replaceAll("∂", "").replaceAll("ß", "")));
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Best population: ");
		bestResults.forEach(rule -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t rule: "+rule.toString().replaceAll("∂", "").replaceAll("ß", "")));
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Fitness population: ");
		List<Double> scores=  rankedFinal.stream().map(rule -> rule.getObjective(0)).collect(Collectors.toList());
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t avrg(Fitness): "+getMean(scores)+", variance(Fitness): "+getVariance(scores)+", stdDev(Fitness): "+getStdDev(scores)+"  -> "+scores);

		
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "--- Execution summary");
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "Final population: ");
		rankedFinal.forEach(rule -> FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t fitness: "+rule.getObjective(0)+", rule: "+rule.getVariable(0).toString().replaceAll("∂", "").replaceAll("ß", "")));
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "Best population: ");
		bestResults.forEach(rule -> FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t rule: "+rule.toString().replaceAll("∂", "").replaceAll("ß", "")));
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "Fitness population: ");
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t avrg(Fitness): "+getMean(scores)+", variance(Fitness): "+getVariance(scores)+", stdDev(Fitness): "+getStdDev(scores));

	}
	
	

	private Double getMean(List<Double> numbers) {
		 Avg avg = new Avg();
		 return avg.applyAggregation(numbers);
	}

	private double getVariance( List<Double> numbers){
        double mean = getMean(numbers);
        double temp = 0;
        for(double a :numbers)
            temp += (a-mean)*(a-mean);
        return temp/(numbers.size()-1);
    }

	private double getStdDev(List<Double> numbers){
        return Math.sqrt(getVariance(numbers));
    }

	

	
}
