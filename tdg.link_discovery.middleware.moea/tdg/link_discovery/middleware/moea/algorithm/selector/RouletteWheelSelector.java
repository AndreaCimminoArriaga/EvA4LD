package tdg.link_discovery.middleware.moea.algorithm.selector;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.moeaframework.core.Population;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;

import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.algorithm.selector.AbstractSelector;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.utils.Utils;


public class RouletteWheelSelector extends AbstractSelector implements Selection {

	public RouletteWheelSelector(){
		super("RouletteWheelSelector", 2);
	}
	
	public RouletteWheelSelector(Integer arity){
		super("RouletteWheelSelector", arity);
	}
	
	@Override
	public <T> List<ISpecification<T>> select( List<ISpecification<?>> specifications) {
		// Empty: implementation not required using moea framework
		return null;
	}

	@Override
	public Solution[] select(int arity, Population population) {
		Double[] scores = new Double[population.size()];
		for(int i=0;i<population.size();i++){
			// we modify the scores since the roulette wheel considers scores of 1 good and 0 bad,
			// though, the moea framework consider such values in the oposite way, 1 bad, 0 good
			Double modifiedScore = 1 - population.get(i).getObjective(0);
			scores[i] = Utils.roundDecimal(modifiedScore, FrameworkConfiguration.DECIMAL_PRECISION);
		}
		// Selecting parents
		Solution[] parentsSelected = new Solution[this.arity];
		for(int i=0;i<this.arity;i++){
			int indexSelected = rouletteSelect(scores);
			parentsSelected[i]= population.get(indexSelected);
		}
		
		return parentsSelected;
	}
	
	private int rouletteSelect(Double[] weight) {
		// calculate the total weight
		double weight_sum = 0;
		for(int i=0; i<weight.length; i++) {
			weight_sum += weight[i];
		}
		// get a random value
		double value = randUniformPositive() * weight_sum;	
		// locate the random value based on the weights
		for(int i=0; i<weight.length; i++) {		
			value -= weight[i];		
			if(value <= 0) return i;
		}
		// when rounding errors occur, we return the last item's index 
		return weight.length - 1;
	}

	// Returns a uniformly distributed double value between 0.0 and 1.0
	private double randUniformPositive() {
		// easiest implementation
		return new Random().nextDouble();
	}


	@Override
	public Boolean hasInputParameters() {
		return true;
	}

	@Override
	public Map<String, Double> getInputDefaultParameters() {
		Map<String,Double> parameters = Maps.newHashMap();
		parameters.put("selector_arity", 2.0);
		return parameters;
	}

	@Override
	public void setInputParameters(String parameterName, Object value) {
		if(parameterName.equals("selector_arity"))
			this.arity = (Integer) value;
		
	}

	
	
	
}
