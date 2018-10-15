package tdg.link_discovery.middleware.moea.genetics.algorithms;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import tdg.link_discovery.framework.algorithm.replacement.IReplacement;
import tdg.link_discovery.framework.algorithm.setup.IAlgorithmSetup;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.moea.algorithm.individual.initializer.GenericInitializator;


public class GeneticProgrammingAlgorithm extends AbstractEvolutionaryAlgorithm{

	protected String name;
	protected Variation mutation, variation;
	protected Selection selection;
	protected IReplacement replace;
	protected Population currentPopulation;
	private Boolean firstIteration;
	protected Integer selectorArity;
	public static int DEFAULT_THREAD_POOL_SIZE_VARIATIONS = 50;
	
	
	public GeneticProgrammingAlgorithm(String name, Problem problem, IAlgorithmSetup operatorsSetup, GenericInitializator initializator) {
		super(problem, new Population(), null, initializator);
		this.name = name;
		this.variation = (Variation) operatorsSetup.getCrossover();
		this.mutation = (Variation) operatorsSetup.getMutation();
	    this.selection = (Selection) operatorsSetup.getSelector();
	    this.selectorArity = operatorsSetup.getSelector().getArity();
	    this.replace = operatorsSetup.getReplacement();
	    currentPopulation = new Population();
	    firstIteration = false;
	   
	}
	
	
	
	// Re-implement this method to make it parallel
	/*@Override
	public void evaluateAll(Iterable<Solution> solutions) {
		solutions.forEach(solution -> evaluate(solution));
	}
		/*List<Callable<Integer>> tasks = Lists.newArrayList();
		for(Solution solution: solutions) {
	        	// Evaluate Rules
			Callable<Integer> task = () -> {
					evaluate(solution);
					return 1;
				};
			tasks.add(task);
		 }
		// execute tasks
		ExecutorService executor = Executors.newFixedThreadPool((Math.min(tasks.size(), DEFAULT_THREAD_POOL_SIZE )));
		try {
			List<Future<Integer>> futures = executor.invokeAll(tasks);
			for(Future<Integer> future: futures) {
				try {
					future.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Shutdown executor
		executor.shutdown();
	}*/
	
	@Override
	public void iterate() {
			
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t --- Iteration details");
			if(!firstIteration){
				currentPopulation = getPopulation();
				firstIteration = true;
			}
			
	        // Generating offspring
	        FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t current_population: ");
	     	Lists.newArrayList(currentPopulation).stream().forEach(rule -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t fitness: "+rule.getObjective(0)+"  rule: "+rule.getVariable(0).toString().replaceAll("∂", "").replaceAll("ß", "")));
	     	long startTime0 = System.nanoTime();
	     	Population offspring = new Population();
	     	while(offspring.size()< population.size()) 
	     		offspring.addAll(generateOffspringPopulation(population));	
	     	long stopTime0 = System.nanoTime();
			long elapsedTime0 = (stopTime0 - startTime0)/1000000;
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t Variations applied in: "+elapsedTime0+" (ms)");
			
	        // Evaluation
	        long startTime = System.nanoTime();
	        evaluateAll(offspring);
	        long stopTime = System.nanoTime();
		    long elapsedTime = (stopTime - startTime)/1000000;
		    //System.out.println("Time to evaluate the whole population: "+elapsedTime+" (ms)");
	        FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t ofsspring fitness calculated in "+elapsedTime+" (ms)");
	        
	        //System.out.println("\nOffspring");
	        //rankedLs(offspring);
	        
	        
	        // Replacement
	        FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t offsprings: ");
	        Lists.newArrayList(offspring).stream().forEach(rule -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t fitness: "+rule.getObjective(0)+"  rule: "+rule.getVariable(0).toString().replaceAll("∂", "").replaceAll("ß", "")));
	        
	        currentPopulation =  replace.combine(currentPopulation, offspring);
	     
	        //System.out.println("\nFinal");
	        //rankedLs(currentPopulation);
	        //System.out.println("\n-------------------");
	      
	        
	        
	    }
		

	
	private Population generateOffspringPopulation(Population population) {
		Population offspring =  new Population();

		// load parallelizable tasks
		List<Callable<Population>> tasks = Lists.newArrayList();
		Integer offspringCounter = 0; 
		while (offspringCounter != population.size()) {
	        	// Selecting parents
	        	Solution[] parents = selection.select(selectorArity, currentPopulation);
	        FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t parents_selected: ");
	        	Lists.newArrayList(parents).stream().forEach(rule -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t\t fitness: "+rule.getObjective(0)+"  rule: "+rule.getVariable(0).toString().replaceAll("∂", "").replaceAll("ß", "")));
	        	Callable<Population> task = () -> {
					return variationOperations(parents);
				};
			tasks.add(task);
			offspringCounter++;
		 }

		// execute tasks
		
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(tasks.size(), DEFAULT_THREAD_POOL_SIZE_VARIATIONS ));
		try {
			List<Future<Population>> futures = executor.invokeAll(tasks);
			for(Future<Population> future: futures) {
				try {
					Population subOffspring = future.get();
					for(Solution solution:subOffspring) {
						if(offspring.size()< population.size()) {
							offspring.add(solution);
							break;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Shutdown executor
		executor.shutdown();
		
		return offspring;
	}

	
	
	private Population variationOperations(Solution[] parents) {
		Population offspring = new Population();

		// Crossing
		Solution[] childred = variation.evolve(parents.clone());
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t crossed_children: ");
		Lists.newArrayList(childred).stream()
				.forEach(rule -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(),
						"\t\t\t\t\t rule: " + rule.getVariable(0).toString().replaceAll("∂", "").replaceAll("ß", "")));

		// Mutating
		Solution[] newChildren = mutation.evolve(childred);
		offspring.addAll(newChildren);
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t muted_children: ");
		Lists.newArrayList(newChildren).stream()
				.forEach(rule -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(),
						"\t\t\t\t\t rule: " + rule.getVariable(0).toString().replaceAll("∂", "").replaceAll("ß", "")));
		return offspring;

	}
	
	
	
	public void rankedLs(Population pop){
			List<Solution> p = Lists.newArrayList(pop);
			//p.stream().forEach(s -> System.out.println(s.getVariable(0)+" => "+s.getObjective(0)));
			p.stream().forEach(s -> System.out.print(s.getObjective(0)+", "));
			
			//System.out.println("\n");
		}
	 
		public List<Double> getLastScoring(){
			List<Solution> p = Lists.newArrayList(currentPopulation);
			List<Double> scores =p.stream().map(s -> s.getObjective(0)).collect(Collectors.toList());
			return scores;
		}
		
		public String getName() {
			return name;
		}
		
		
		public Population getCurrentPopulation(){
			return currentPopulation;
		}

	 
}
