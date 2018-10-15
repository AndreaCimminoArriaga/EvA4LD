package tdg.link_discovery.middleware.moea.genetics.fitness_function;

import java.util.Map;

import tdg.link_discovery.framework.algorithm.fitness.AbstractFitness;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.utils.Utils;

public class FMeasureFitness extends AbstractFitness{
	
	public FMeasureFitness(){
		super("FMeasureFitness");
		this.name = "F-MeasureFitness";
	}
	
	@Override
	public Double evaluateSolutionResults(ConfusionMatrix confusionMatrix, Object[] otherArguments) {
		Double recall = confusionMatrix.getRecall();
		Double precision = confusionMatrix.getPrecision();
		//System.out.println("\tprecision: "+precision+"     recall: "+recall);
		//System.out.println("\tFitness: "+confusionMatrix.getFMeasure());
		return Utils.roundDecimal(confusionMatrix.getFMeasure(), 2);
	}

	
	@Override
	public Boolean hasInputParameters() {
		return false;
	}

	@Override
	public Map<String, Double> getInputDefaultParameters() {
		return null;
	}

	@Override
	public void setInputParameters(String parameterName, Object value) {
		// empty method
	}

	
}
