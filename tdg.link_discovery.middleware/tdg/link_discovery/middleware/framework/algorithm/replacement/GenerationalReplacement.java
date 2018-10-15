package tdg.link_discovery.middleware.framework.algorithm.replacement;

import java.util.Map;

import org.moeaframework.core.Population;

import tdg.link_discovery.framework.algorithm.replacement.AbstractReplacement;

public class GenerationalReplacement extends AbstractReplacement{

	public GenerationalReplacement(){
		super("Generational");
	}
	
	@Override
	public Population combine(Population population, Population offspring) {
		return offspring;
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
