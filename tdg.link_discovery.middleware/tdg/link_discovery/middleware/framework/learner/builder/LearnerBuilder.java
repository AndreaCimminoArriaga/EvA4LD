package tdg.link_discovery.middleware.framework.learner.builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import tdg.link_discovery.framework.algorithm.fitness.IFitness;
import tdg.link_discovery.framework.algorithm.individual.initializer.ISpecificationInitializer;
import tdg.link_discovery.framework.algorithm.replacement.IReplacement;
import tdg.link_discovery.framework.algorithm.selector.ISelector;
import tdg.link_discovery.framework.algorithm.setup.AlgorithmSetup;
import tdg.link_discovery.framework.algorithm.setup.IAlgorithmSetup;
import tdg.link_discovery.framework.algorithm.variations.IVariation;
import tdg.link_discovery.framework.engine.IEngine;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.learner.ILearner;
import tdg.link_discovery.framework.learner.attributes.IAttributeSelector;
import tdg.link_discovery.framework.learner.functions.AggregateFunction;
import tdg.link_discovery.framework.learner.functions.StringMetricFunction;
import tdg.link_discovery.framework.learner.functions.TransformationFunction;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.framework.learner.GenericLearner;
import tdg.link_discovery.middleware.moea.genetics.algorithms.GenericMOEAGeneticAlgorithm;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.ObjectUtils;
import tdg.link_discovery.middleware.utils.StreamUtils;

public class LearnerBuilder {

	protected String algorithmName = null;
	
	protected IEngine engine = null;
	
	protected IEnvironment environment = null;
	protected Setup setup = null;

	protected ISelector selector = null;
	protected IVariation<?> crossover = null;
	protected IVariation<?> mutation = null;
	protected IReplacement replacement = null;
	protected ISpecificationInitializer initializer = null;
	protected IFitness fitness = null;
	protected IAttributeSelector attributeSelector = null;

	protected List<StringMetricFunction> stringMetricFunctions;
	protected List<AggregateFunction> aggregateFunctions;
	protected List<TransformationFunction> transformationFunctions;

	public LearnerBuilder(Map<String,String> buildDescription, Setup setup, IEnvironment environment){
		this.environment = environment;
		this.setup = setup;
		this.stringMetricFunctions = Lists.newArrayList();
		this.aggregateFunctions = Lists.newArrayList();
		this.transformationFunctions = Lists.newArrayList();
		// Retrieve classes names from map
		String algorithmName = buildDescription.get("name");
		String selectorName = buildDescription.get("selector_class");
		String crossoverName = buildDescription.get("crossover_class");
		String mutationName = buildDescription.get("mutation_class");
		String replacementName = buildDescription.get("replacement_class");
		String initializerName = buildDescription.get("initializer_class");
		String fitnessName = buildDescription.get("fitness_class");
		String attributeLearnerName = buildDescription.get("attribute_learner_class");
		String engineName = buildDescription.get("engine_class");
		String stringMetricsNameList = buildDescription.get("string_metrics_classes");
		String aggregatesNameList = buildDescription.get("aggregate_classes");
		String transformationNameList = buildDescription.get("transformation_classes");
		//TODO: Calculate path to classes
		// Loading algorithm operators
		if(algorithmName!= null)
			this.algorithmName = algorithmName;
		if(selectorName!=null)
			this.selector = (ISelector) ObjectUtils.createObject(selectorName, new Class<?>[]{Integer.class}, new Object[]{setup.getParentsSelectionSize()});
		if(crossoverName!=null)
			this.crossover = (IVariation<?>) ObjectUtils.createObject(crossoverName, new Class<?>[]{Double.class}, new Object[]{setup.getCrossoverRate()});
		if(mutationName!=null)
			this.mutation = (IVariation<?>) ObjectUtils.createObject(mutationName, new Class<?>[]{Double.class}, new Object[]{setup.getMutationRate()});
		if(replacementName!=null)		
			this.replacement = (IReplacement) ObjectUtils.createObject(replacementName, new Class<?>[]{}, new Object[]{});
		if(initializerName!=null)
			this.initializer = (ISpecificationInitializer) ObjectUtils.createObject(initializerName, new Class<?>[]{}, new Object[]{});
		if(fitnessName!=null)		
			this.fitness = (IFitness) ObjectUtils.createObject(fitnessName, new Class<?>[]{}, new Object[]{});
		if(attributeLearnerName!=null){
			this.attributeSelector = (IAttributeSelector) ObjectUtils.createObject(attributeLearnerName, new Class<?>[]{IEnvironment.class}, new Object[]{environment});
		}else{
			this.attributeSelector = null;
		}
		if(engineName!=null)
			this.engine = (IEngine) ObjectUtils.createObject(engineName, new Class<?>[]{IEnvironment.class}, new Object[]{environment});
		if(stringMetricsNameList!=null)
			this.stringMetricFunctions = StreamUtils.asStream(stringMetricsNameList.replaceAll("\\s", "").split(","))
													.map(metricName -> (StringMetricFunction) ObjectUtils.createObject(metricName, new Class<?>[]{}, new Object[]{}))
								  				    .collect(Collectors.toList());
		if(aggregatesNameList!=null)
			this.aggregateFunctions = StreamUtils.asStream(aggregatesNameList.replaceAll("\\s", "").split(","))
													.map(aggregateName -> (AggregateFunction) ObjectUtils.createObject(aggregateName, new Class<?>[]{}, new Object[]{}))
								  				    .collect(Collectors.toList());
		if(transformationNameList!=null)
			this.transformationFunctions = StreamUtils.asStream(transformationNameList.replaceAll("\\s", "").split(","))
													.map(transformationName -> (TransformationFunction) ObjectUtils.createObject(transformationName, new Class<?>[]{}, new Object[]{}))
								  				    .collect(Collectors.toList());
	}
	
	
	
	
	public IAlgorithmSetup generateLearner(){
		IAlgorithmSetup genetics = new AlgorithmSetup(selector, replacement, crossover, mutation, Lists.newArrayList(fitness), initializer);
		return genetics;
	}
	
	public GenericMOEAGeneticAlgorithm generateGenericAlgorithm(){
		IAlgorithmSetup genetics = generateLearner();
		GenericMOEAGeneticAlgorithm genericAlgorithm = new GenericMOEAGeneticAlgorithm(engine, setup);
		genericAlgorithm.setOperatorsSetup(genetics);
		genericAlgorithm.setName(algorithmName);
		return genericAlgorithm;
	}
	
	public ILearner getLearner(){
		GenericMOEAGeneticAlgorithm genericAlgorithm = generateGenericAlgorithm();
		GenericLearner learner = new GenericLearner(this.environment, this.setup);
		learner.setAttributeSelector(attributeSelector);
		learner.setEngine(engine);
		learner.setConfiguration(environment);
		learner.setAlgorithm(genericAlgorithm);
		// Add suitable attributes to the learner
		List<Tuple<String,String>> attributes = environment.getSuitableAttributes();
		if(!attributes.isEmpty())
			attributes.stream().forEach(pair -> learner.addSuitableAttributes(pair.getFirstElement(), pair.getSecondElement()));
		// Add functions
		this.stringMetricFunctions.stream().forEach(metric -> learner.addLearnerStringMetricFunction(metric));
		this.aggregateFunctions.stream().forEach(aggregate -> learner.addLearnerAggregateFunction(aggregate));
		this.transformationFunctions.stream().forEach(transformation -> learner.addLearnerTransformationFunction(transformation));
		
		return learner;
	}
	
	public IEngine getEngine(){
		return this.engine;
	}
}
