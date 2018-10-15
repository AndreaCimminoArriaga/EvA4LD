package tdg.link_discovery.middleware.moea.genetics.fitness_function;

import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Maps;

import tdg.link_discovery.framework.algorithm.fitness.AbstractFitness;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.utils.Utils;

public class PseudoFMeasureFitness extends AbstractFitness{

	private Double betta;
	
	public PseudoFMeasureFitness(){
		super("FMeasureFitness");
		betta = 1.0;
		this.name = "PseudoF-MeasureFitness";
	}
	
	@Override
	public Double evaluateSolutionResults(ConfusionMatrix confusionMatrix, Object[] otherArguments) {
		Double recall = confusionMatrix.getRecall();
		Double precision = confusionMatrix.getPrecision();
		Double score = (1+betta*betta)*precision*recall/(betta*betta*precision+recall);
		if(score.isNaN() || score.isInfinite())
			score = 0.0;
		return Utils.roundDecimal(score, FrameworkConfiguration.DECIMAL_PRECISION);
	}

	

	public void setBetta(Double betta){
		this.betta = betta;
	}


	@Override
	public Boolean hasInputParameters() {
		return true;
	}

	@Override
	public Map<String, Double> getInputDefaultParameters() {
		Map<String,Double> defaultParameters = Maps.newHashMap();
		defaultParameters.put("betta", 1.0);
		return defaultParameters;
	}

	@Override
	public void setInputParameters(String parameterName, Object value) {
		if(parameterName.equals("betta"))
			setBetta((Double) value);
		
	}

	
}
