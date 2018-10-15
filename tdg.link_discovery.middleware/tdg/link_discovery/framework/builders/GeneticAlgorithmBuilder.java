package tdg.link_discovery.framework.builders;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import tdg.link_discovery.framework.algorithm.IAlgorithm;
import tdg.link_discovery.framework.algorithm.fitness.IFitness;
import tdg.link_discovery.framework.algorithm.individual.initializer.ISpecificationInitializer;
import tdg.link_discovery.framework.algorithm.replacement.IReplacement;
import tdg.link_discovery.framework.algorithm.selector.ISelector;
import tdg.link_discovery.framework.algorithm.setup.AlgorithmSetup;
import tdg.link_discovery.framework.algorithm.setup.IAlgorithmSetup;
import tdg.link_discovery.framework.algorithm.variations.IVariation;
import tdg.link_discovery.framework.engine.IEngine;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.learner.attributes.IAttributeSelector;
import tdg.link_discovery.framework.learner.functions.AggregateFunction;
import tdg.link_discovery.framework.learner.functions.StringMetricFunction;
import tdg.link_discovery.framework.learner.functions.TransformationFunction;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.moea.genetics.algorithms.GenericMOEAGeneticAlgorithm;
import tdg.link_discovery.middleware.utils.ObjectUtils;
import tdg.link_discovery.middleware.utils.StreamUtils;

public class GeneticAlgorithmBuilder implements IBuilder<IAlgorithm>{

	
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
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public IAlgorithm buildObject(Object buildSpecifications) {
		Map<String,String> geneticOperatorsSpecifications = (Map<String,String>) buildSpecifications;
		// Retrieve classes names from map
		String algorithmName = geneticOperatorsSpecifications.get(FrameworkConfiguration.ALGORITHM_NAME_INFILE_TOKEN);
		String selectorName = geneticOperatorsSpecifications
				.get(FrameworkConfiguration.ALGORITHM_SELECTOR_INFILE_TOKEN);
		String crossoverName = geneticOperatorsSpecifications
				.get(FrameworkConfiguration.ALGORITHM_CROSSOVER_INFILE_TOKEN);
		String mutationName = geneticOperatorsSpecifications
				.get(FrameworkConfiguration.ALGORITHM_MUTATION_INFILE_TOKEN);
		String replacementName = geneticOperatorsSpecifications
				.get(FrameworkConfiguration.ALGORITHM_REPLACEEMT_INFILE_TOKEN);
		String initializerName = geneticOperatorsSpecifications
				.get(FrameworkConfiguration.ALGORITHM_INITIALIZATION_INFILE_TOKEN);
		String fitnessName = geneticOperatorsSpecifications
				.get(FrameworkConfiguration.ALGORITHM_FITNESS_INFILE_TOKEN );
		String attributeLearnerName = geneticOperatorsSpecifications
				.get(FrameworkConfiguration.ALGORITHM_ATTRIBUTE_LEARNER_INFILE_TOKEN);
		String engineName = geneticOperatorsSpecifications.get(FrameworkConfiguration.ALGORITHM_ENVINE_INFILE_TOKEN);
		String stringMetricsNameList = geneticOperatorsSpecifications
				.get(FrameworkConfiguration.ALGORITHM_STRING_METRICS_INFILE_TOKEN);
		String aggregatesNameList = geneticOperatorsSpecifications
				.get( FrameworkConfiguration.ALGORITHM_AGGREGATES_INFILE_TOKEN);
		String transformationNameList = geneticOperatorsSpecifications
				.get(FrameworkConfiguration.ALGORITHM_TRANSFORMATIONS_INFILE_TOKEN);
		
		// Loading algorithm operators
		loadName(algorithmName);
		loadSelector(selectorName);
		loadCrossover(crossoverName);
		loadMutation(mutationName);
		loadReplacementPolicy(replacementName);
		loadChromosomeInitializer(initializerName);
		loadFitness(fitnessName);
		loadAttributeSelector(attributeLearnerName);
		loadEngine(engineName);
		loadStringMetrics(stringMetricsNameList);
		loadAggregates(aggregatesNameList);
		loadTransformations(transformationNameList);

		return generateGenericAlgorithm();
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
	
	
	
	/*
	 * Loaders
	 */
	
	
	private void loadName(String algorithmName){
		if (algorithmName != null)
				this.algorithmName = algorithmName;
	}
	
	private void loadSelector(String selectorName){
		if (selectorName != null)
			this.selector = (ISelector) ObjectUtils.createObject(selectorName,
					new Class<?>[] { Integer.class },
					new Object[] { setup.getParentsSelectionSize() });
	}
	
	private void loadCrossover(String crossoverName){
		if (crossoverName != null)
			this.crossover = (IVariation<?>) ObjectUtils.createObject(
					crossoverName, new Class<?>[] { Double.class },
					new Object[] { setup.getCrossoverRate() });
	}
	
	private void loadMutation(String mutationName){
		if (mutationName != null)
			this.mutation = (IVariation<?>) ObjectUtils.createObject(
					mutationName, new Class<?>[] { Double.class },
					new Object[] { setup.getMutationRate() });
		
	}
	
	
	private void loadReplacementPolicy(String replacementName){
		if (replacementName != null)
			this.replacement = (IReplacement) ObjectUtils.createObject(
					replacementName, new Class<?>[] {}, new Object[] {});
	}
	
	private void loadChromosomeInitializer(String initializerName){
		if (initializerName != null)
			this.initializer = (ISpecificationInitializer) ObjectUtils
					.createObject(initializerName, new Class<?>[] {},
							new Object[] {});
	}
	
	
	private void loadFitness(String fitnessName){
		if (fitnessName != null)
			this.fitness = (IFitness) ObjectUtils.createObject(fitnessName,
					new Class<?>[] {}, new Object[] {});
	}
	
	private void loadAttributeSelector(String attributeLearnerName){
		if (attributeLearnerName != null) {
			this.attributeSelector = (IAttributeSelector) ObjectUtils
					.createObject(attributeLearnerName,
							new Class<?>[] { IEnvironment.class },
							new Object[] { environment });
		} else {
			this.attributeSelector = null;
		}
	}
	
	
	private void loadEngine(String engineName){
		if (engineName != null)
			this.engine = (IEngine) ObjectUtils.createObject(engineName,
					new Class<?>[] { IEnvironment.class },
					new Object[] { environment });
	}
	
	private void loadStringMetrics(String stringMetricsNameList){
		if (stringMetricsNameList != null)
			this.stringMetricFunctions = StreamUtils
					.asStream(
							stringMetricsNameList.replaceAll("\\s", "").split(
									","))
					.map(metricName -> (StringMetricFunction) ObjectUtils
							.createObject(metricName, new Class<?>[] {},
									new Object[] {}))
					.collect(Collectors.toList());
	}
	
	private void loadAggregates(String aggregatesNameList){
		if (aggregatesNameList != null)
			this.aggregateFunctions = StreamUtils
					.asStream(
							aggregatesNameList.replaceAll("\\s", "").split(","))
					.map(aggregateName -> (AggregateFunction) ObjectUtils
							.createObject(aggregateName, new Class<?>[] {},
									new Object[] {}))
					.collect(Collectors.toList());
	}
	
	
	private void loadTransformations(String transformationNameList){
		if (transformationNameList != null)
			this.transformationFunctions = StreamUtils
					.asStream(
							transformationNameList.replaceAll("\\s", "").split(
									","))
					.map(transformationName -> (TransformationFunction) ObjectUtils
							.createObject(transformationName,
									new Class<?>[] {}, new Object[] {}))
					.collect(Collectors.toList());
		
	}
	
	
	

}
