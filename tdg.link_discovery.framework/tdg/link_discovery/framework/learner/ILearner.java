package tdg.link_discovery.framework.learner;

import java.util.List;

import tdg.link_discovery.framework.algorithm.IAlgorithm;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.engine.IEngine;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.learner.attributes.IAttributeSelector;
import tdg.link_discovery.framework.learner.functions.AggregateFunction;
import tdg.link_discovery.framework.learner.functions.StringMetricFunction;
import tdg.link_discovery.framework.learner.functions.TransformationFunction;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;

public interface ILearner {

	// Learning methods
	public void learnSpecifications();
	public <T extends Object> List<ISpecification<T>> getLearnedSpecifications();
	public <T extends Object> List<ISpecification<T>> getBestLearnedSpecifications();
	
	//Evolutionary Algorithm
	public IAlgorithm getAlgorithm();
	public void setAlgorithm(IAlgorithm algorithm);
	public void setAlgorithmSetup(Setup file);
	public Setup getAlgorithmSetup();
	public Double getAlgorithmProgress();
	
	//Attribute selector
	public IAttributeSelector getAttributeSelector();
	public void setAttributeSelector(IAttributeSelector attributeSelector);
	
	// Learner configuration
	public void setConfiguration(IEnvironment configuration);
	public IEnvironment getConfiguration();
		
	// Initialize the functions that the learner will relying on:
	public void initLearnerFunctions(); // initialize here
	public void initSuitableAttributes(); // initialize here
	public void addLearnerAggregateFunction(AggregateFunction aggregate);	
	public void addLearnerStringMetricFunction(StringMetricFunction stringMetric);
	public void addLearnerTransformationFunction(TransformationFunction transformation);
	public void addSuitableAttributes(String attributeSource, String attributeTarget); // initialize here
	public Boolean existAttributesToCompare();
	public Boolean hasAttributeLearner();
	
	// Engine to access datasets
	public IEngine getEngine();
	public void setEngine(IEngine engine);
	
	//Storing data
	public void saveLearnedSpecifications();
	public void saveBestLearnedSpecifications();
	public void cleanSuitableAttributes();
	
}
