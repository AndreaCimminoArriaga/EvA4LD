package tdg.link_discovery.framework.algorithm.fitness;

import tdg.link_discovery.framework.objects.FrameworkObject;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;


public interface IFitness extends FrameworkObject{
	
	public Double evaluateSolutionResults(ConfusionMatrix confusionMatrix, Object[] otherArguments); 
	
}
