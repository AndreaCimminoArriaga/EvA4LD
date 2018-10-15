package tdg.link_discovery.middleware.moea.genetics.fitness_function;

import java.util.Map;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.algorithm.fitness.AbstractFitness;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.middleware.utils.Utils;

public class FmeasureGenlink  extends AbstractFitness{

	public FmeasureGenlink(){
		super("GenlinkFitness");
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Double evaluateSolutionResults(ConfusionMatrix confusionMatrix, Object[] otherArguments) {
		Double mcc = confusionMatrix.getMcc();
		
		ISpecification<Tree> specification = (ISpecification<Tree>) otherArguments[0];
		// Retrieving number of operations:  function nodes - attribute nodes
		Map<String, Integer> statistics = specification.getSpecificationRepresentation().getStatistics();
		Integer operations =  statistics.get(SPARQLFactory.prefixJenaFunctionsAggregations) + statistics.get(SPARQLFactory.prefixJenaFunctionsStrings) + statistics.get(SPARQLFactory.prefixJenaFunctionsTransformations);
		// Calculating score
		Double finalScore = mcc - (0.01*operations);
		if(confusionMatrix.getFMeasure()==1.0) {
			finalScore = 1.0;
		}else if(finalScore < 0) {
			finalScore = 0.0;
		}
		//System.out.println("\t############################### >	"+(confusionMatrix.getFMeasure())+" -  "+(finalScore)+"    -> "+(mcc));
		
		return Utils.roundDecimal(finalScore, FrameworkConfiguration.DECIMAL_PRECISION);
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
