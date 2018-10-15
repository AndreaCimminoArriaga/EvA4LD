package tdg.link_discovery.middleware.framework.algorithm.replacement;

import java.util.Map;

import org.moeaframework.core.Population;

import tdg.link_discovery.framework.algorithm.replacement.AbstractReplacement;
import tdg.link_discovery.middleware.utils.Utils;

public class RandomReplacement extends AbstractReplacement{

	public RandomReplacement(){
		super("RandomReplacement");
	}

	@Override
	public Population combine(Population population, Population offspring) {
		Population resultantPopulation = new Population();
		Population mixedPopulation = new Population();
		mixedPopulation.addAll(population);
		mixedPopulation.addAll(offspring);
		
		for(int index = 0; index < population.size(); index++){
			Integer randomNumber = Utils.getRandomInteger(mixedPopulation.size()-1, 0);
			resultantPopulation.add(mixedPopulation.get(randomNumber));
		}
		
		
		return resultantPopulation;
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
