package tdg.link_discovery.middleware.moea.algorithm.individual.initializer;

import org.moeaframework.core.Initialization;
import org.moeaframework.core.Solution;

import tdg.link_discovery.framework.algorithm.individual.initializer.ISpecificationInitializer;
import tdg.link_discovery.middleware.moea.algorithm.individual.LinkSpecification;

public class GenericInitializator implements Initialization {

	private ISpecificationInitializer initializer;
	private Integer populationSize, objectives, variables;

	
	public GenericInitializator(ISpecificationInitializer initializer, Integer populationSize, Integer objectives, Integer variables) {
		super();
		this.initializer = initializer;
		this.populationSize = populationSize;
		this.objectives = objectives;
		this.variables = variables;
		
	}

	@Override
	public Solution[] initialize() {
		Solution[] population = new Solution[populationSize];
		
		for(int index=0;index<populationSize;index++){
			Solution newSolution = new Solution(variables, objectives);
			LinkSpecification specification = (LinkSpecification) initializer.createLinkSpecification();
			newSolution.setVariable(0, specification);
			population[index] = newSolution;	
		}

		return population;
	}
	
	

}
