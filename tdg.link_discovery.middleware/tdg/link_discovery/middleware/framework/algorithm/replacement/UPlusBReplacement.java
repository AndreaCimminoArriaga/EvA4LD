package tdg.link_discovery.middleware.framework.algorithm.replacement;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

import tdg.link_discovery.framework.algorithm.replacement.AbstractReplacement;
import tdg.link_discovery.middleware.moea.comparators.SolutionComparator;

public class UPlusBReplacement extends AbstractReplacement{

	public UPlusBReplacement(){
		super("U + B");
	}
	
	@Override
	public Population combine(Population population, Population offspring) {
		Population mixedPopulation = new Population();
		
		
		List<Solution> populationL = Lists.newArrayList(population);
		List<String> populationString = populationL.stream().map(sol -> sol.getVariable(0).toString()).collect(Collectors.toList());
		List<Solution> offspringL = Lists.newArrayList(offspring);

		offspringL.stream()
					.filter(children -> !populationString.contains(children.getVariable(0).toString()))
					.forEach(children -> mixedPopulation.add(children));
		//System.out.println("*"+mixedPopulation.size());
		// Fusion population
		mixedPopulation.addAll(population);
		//System.out.println("*"+mixedPopulation.size());
		// Sort by score and retain only population.size candidates
		mixedPopulation.truncate(population.size(), new SolutionComparator());
		
		return mixedPopulation;
	}
	
	@Override
	public Map<String, Double> getInputDefaultParameters() {
		return null;
	}

	@Override
	public Boolean hasInputParameters() {
		return false;
	}

	@Override
	public void setInputParameters(String parameterName, Object value) {
		// Empty method
	}


}
