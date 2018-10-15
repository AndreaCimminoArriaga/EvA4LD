package tdg.link_discovery.middleware.moea.genetics.algorithms;

import java.util.List;
import java.util.stream.Collectors;

import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import com.google.common.collect.Lists;
import tdg.link_discovery.framework.algorithm.replacement.IReplacement;
import tdg.link_discovery.framework.algorithm.setup.IAlgorithmSetup;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.moea.algorithm.individual.initializer.GenericInitializator;

public class GeneticProgrammingAlgorithmCopy extends AbstractEvolutionaryAlgorithm{

	protected String name;
	protected Variation mutation, variation;
	protected Selection selection;
	protected IReplacement replace;
	protected Population currentPopulation;
	private Boolean firstIteration;
	protected Integer selectorArity;
	
	public GeneticProgrammingAlgorithmCopy(String name, Problem problem, IAlgorithmSetup operatorsSetup, GenericInitializator initializator) {
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
	

	
	@Override
	public void iterate() {
		    
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t --- Iteration details");
			if(!firstIteration){
				currentPopulation = getPopulation();
				firstIteration = true;
			}
			
	        Population offspring = new Population();
	        int populationSize = population.size();
	        //System.out.println("Parents");
	        //rankedLs(currentPopulation);
	        // Generating offspring
	        FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t current_population: ");
	     	Lists.newArrayList(currentPopulation).stream().forEach(rule -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t fitness: "+rule.getObjective(0)+"  rule: "+rule.getVariable(0).toString().replaceAll("∂", "").replaceAll("ß", "")));

	        while (offspring.size() < populationSize) {
		        	// Selecting parents
		        	Solution[] parents = selection.select(selectorArity, currentPopulation);
		        	 FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t parents_selected: ");
		        	 Lists.newArrayList(parents).stream().forEach(rule -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t\t fitness: "+rule.getObjective(0)+"  rule: "+rule.getVariable(0).toString().replaceAll("∂", "").replaceAll("ß", "")));
		        	// Crossing
		        	Solution[] childred = variation.evolve(parents.clone());
		        	FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t crossed_children: ");
		        	Lists.newArrayList(childred).stream().forEach(rule -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t\t rule: "+rule.getVariable(0).toString().replaceAll("∂", "").replaceAll("ß", "")));

			    //Mutating
		        Solution[] newChildren = mutation.evolve(childred);
		        offspring.addAll(newChildren);
		        FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t muted_children: ");
		        Lists.newArrayList(newChildren).stream().forEach(rule -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t\t\t\t rule: "+rule.getVariable(0).toString().replaceAll("∂", "").replaceAll("ß", "")));

	        }
	        
	        // Evaluation
	        long startTime = System.nanoTime();
	        evaluateAll(offspring);
	        long stopTime = System.nanoTime();
		    long elapsedTime = (stopTime - startTime)/1000000;
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
