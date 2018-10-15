package tdg.link_discovery.framework.gui.connector;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;

import tdg.link_discovery.framework.algorithm.fitness.IFitness;
import tdg.link_discovery.framework.algorithm.individual.initializer.ISpecificationInitializer;
import tdg.link_discovery.framework.algorithm.replacement.IReplacement;
import tdg.link_discovery.framework.algorithm.selector.ISelector;
import tdg.link_discovery.framework.algorithm.variations.IVariation;
import tdg.link_discovery.framework.engine.IEngine;
import tdg.link_discovery.framework.gui.desktop.GuiConfiguration;
import tdg.link_discovery.framework.learner.attributes.IAttributeSelector;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.utils.ClassesUtils;
import tdg.link_discovery.middleware.utils.ObjectUtils;
import tdg.link_discovery.middleware.utils.StreamUtils;

public class FileAlgorithmsConnector implements IAlgorithmsConnector {

	@Override
	public List<String> getListOfStoredAlgorithmsNames() {
		return readFilesFromDirectory(GuiConfiguration.ALGORTIHMS_DIRECTORY);
	}

	private List<String> readFilesFromDirectory(String directory) {
		List<String> files = Lists.newArrayList();
		try {
			File aDirectory = new File(directory);
			String[] filesInDir = aDirectory.list();
			for (int i = 0; i < filesInDir.length; i++) {
				if (!filesInDir[i].contains("setup.cnf")) {
					String algorithmName = filesInDir[i].substring(0, filesInDir[i].lastIndexOf("."));
					files.add(algorithmName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return files;
	}


	@Override
	public void importAlgorithmFromFile(String algorithmFile) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public Boolean addAlgorithm(Map<String, String> algorithmGeneticOperators) {
		String name = algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_NAME_INFILE_TOKEN);
		String selectionClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_SELECTOR_INFILE_TOKEN));
		String initializerClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_INITIALIZATION_INFILE_TOKEN));
		String replacementClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_REPLACEEMT_INFILE_TOKEN));
		String fitnessClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_FITNESS_INFILE_TOKEN));
		String crossoverClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_CROSSOVER_INFILE_TOKEN));
		String mutationClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_MUTATION_INFILE_TOKEN));
		String engine = ClassesUtils.findClassPackageByName("SparqlEngine");
		String attributeSelector = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_ATTRIBUTE_LEARNER_INFILE_TOKEN));
		// load aggregates
		List<String> aggregates = loadAggregates(algorithmGeneticOperators);
		// load string metrics
		List<String> metrics = loadMetrics(algorithmGeneticOperators);
		// load string treansformations
		List<String> transformations = loadTransformations(algorithmGeneticOperators);
		// write into file
		List<String> lines =saveGAFeaturesIntoFile(name, selectionClazzPath, initializerClazzPath, replacementClazzPath, fitnessClazzPath, crossoverClazzPath, mutationClazzPath, aggregates, metrics, transformations, attributeSelector, engine);
		Boolean correctlyWritten = createFile(GuiConfiguration.ALGORTIHMS_DIRECTORY+""+name+".cnf", lines);
		correctlyWritten &= createSetupFile(algorithmGeneticOperators, name);
		return correctlyWritten;
	}
	
	private Boolean createFile(String name, List<String> lines) {
		File newFile =new File(name);
		Boolean correctlyWritten = false;
		try{
			FileUtils.writeLines(newFile, lines);
			correctlyWritten = true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return correctlyWritten;
	}

	private List<String> loadAggregates(Map<String, String> algorithmGeneticOperators){
		List<String> aggregates = Lists.newArrayList("# "+FrameworkConfiguration.ALGORITHM_AGGREGATES_INFILE_TOKEN+" := empty");
		if(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_AGGREGATES_INFILE_TOKEN)!=null){
			aggregates.clear();
			aggregates = StreamUtils.asStream(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_AGGREGATES_INFILE_TOKEN).split(",")).map(elem -> ClassesUtils.findClassPackageByName(elem)).collect(Collectors.toList());
		}
		return aggregates;
	}
	
	private List<String> loadMetrics(Map<String, String> algorithmGeneticOperators){
		List<String> metrics = Lists.newArrayList("# "+FrameworkConfiguration.ALGORITHM_STRING_METRICS_INFILE_TOKEN+" := empty");
		if(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_STRING_METRICS_INFILE_TOKEN)!=null){
			metrics.clear();
			metrics = StreamUtils.asStream(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_STRING_METRICS_INFILE_TOKEN).split(",")).map(elem -> ClassesUtils.findClassPackageByName(elem)).collect(Collectors.toList());
		}
		return metrics;
	}

	private List<String> loadTransformations(Map<String, String> algorithmGeneticOperators){
		List<String> transformations = Lists.newArrayList("# "+FrameworkConfiguration.ALGORITHM_TRANSFORMATIONS_INFILE_TOKEN+" := empty");
		if(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_TRANSFORMATIONS_INFILE_TOKEN)!=null){
			transformations.clear();
			transformations = StreamUtils.asStream(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_TRANSFORMATIONS_INFILE_TOKEN).split(",")).map(elem -> ClassesUtils.findClassPackageByName(elem)).collect(Collectors.toList());
		}
		return transformations;
	}

	private Boolean createSetupFile(Map<String, String> algorithmGeneticOperators, String name) {
		List<String> lines = Lists.newArrayList();
		lines.add("max_iterations := 100");
		lines.add("population_size := 20");
		lines.add("objectives_num := 1");
		lines.add("variables_num := 1");
		lines.add("generations_rate := 50");
		if(isValidOperator(algorithmGeneticOperators, FrameworkConfiguration.ALGORITHM_SELECTOR_INFILE_TOKEN))
			lines.addAll(retrieveSelectorParameters(algorithmGeneticOperators));
		if(isValidOperator(algorithmGeneticOperators, FrameworkConfiguration.ALGORITHM_INITIALIZATION_INFILE_TOKEN))
			lines.addAll(retrieveInitializerParameters(algorithmGeneticOperators));
		if(isValidOperator(algorithmGeneticOperators, FrameworkConfiguration.ALGORITHM_REPLACEEMT_INFILE_TOKEN))
			lines.addAll(retrieveReplacementParameters(algorithmGeneticOperators));
		if(isValidOperator(algorithmGeneticOperators, FrameworkConfiguration.ALGORITHM_FITNESS_INFILE_TOKEN))
			lines.addAll(retrieveFitnessParameters(algorithmGeneticOperators));
		if(isValidOperator(algorithmGeneticOperators, FrameworkConfiguration.ALGORITHM_CROSSOVER_INFILE_TOKEN))
			lines.addAll(retrieveVariationParameters(algorithmGeneticOperators, true));
		if(isValidOperator(algorithmGeneticOperators, FrameworkConfiguration.ALGORITHM_MUTATION_INFILE_TOKEN))
			lines.addAll(retrieveVariationParameters(algorithmGeneticOperators, false));
		if(isValidOperator(algorithmGeneticOperators, FrameworkConfiguration.ALGORITHM_ATTRIBUTE_LEARNER_INFILE_TOKEN))
			lines.addAll(retrieveAttributeSelectorParameters(algorithmGeneticOperators));
				
		return createFile(GuiConfiguration.ALGORTIHMS_DIRECTORY+""+name+"_setup.cnf", lines);
	}
	
	private Boolean isValidOperator(Map<String, String> algorithmGeneticOperators, String operatorKey){
		Boolean valid = false;
		if(algorithmGeneticOperators.containsKey(operatorKey))
			if(algorithmGeneticOperators.get(operatorKey) !=null)
				if(!algorithmGeneticOperators.get(operatorKey).isEmpty())
					valid = true;
		return valid;
	}
	
	private List<String> retrieveSelectorParameters(Map<String, String> algorithmGeneticOperators){
		List<String> lines = Lists.newArrayList();
		String selectionClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_SELECTOR_INFILE_TOKEN));
		ISelector selector = (ISelector) ObjectUtils.createObject(selectionClazzPath, null, null);
		if(selector.hasInputParameters())
			selector.getInputDefaultParameters().entrySet().stream().forEach(tuple -> lines.add(tuple.getKey()+" := "+tuple.getValue()));
		return lines;
	}

	private List<String> retrieveInitializerParameters(Map<String, String> algorithmGeneticOperators){
		List<String> lines = Lists.newArrayList();
		String initializerClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_INITIALIZATION_INFILE_TOKEN));
		ISpecificationInitializer initializer = (ISpecificationInitializer) ObjectUtils.createObject(initializerClazzPath, null, null);
		if(initializer.hasInputParameters())
			initializer.getInputDefaultParameters().entrySet().stream().forEach(tuple -> lines.add(tuple.getKey()+" := "+tuple.getValue()));
		return lines;
	}

	private List<String> retrieveReplacementParameters(Map<String, String> algorithmGeneticOperators){
		List<String> lines = Lists.newArrayList();
		String replacementClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_REPLACEEMT_INFILE_TOKEN));
		IReplacement replacement = (IReplacement) ObjectUtils.createObject(replacementClazzPath, null, null);
		if(replacement.hasInputParameters())
			replacement.getInputDefaultParameters().entrySet().stream().forEach(tuple -> lines.add(tuple.getKey()+" := "+tuple.getValue()));
		return lines;
	}

	private List<String> retrieveFitnessParameters(Map<String, String> algorithmGeneticOperators){
		List<String> lines = Lists.newArrayList();
		String fitnessClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_FITNESS_INFILE_TOKEN));
		IFitness fitness = (IFitness) ObjectUtils.createObject(fitnessClazzPath, null, null);
		if(fitness.hasInputParameters())
			fitness.getInputDefaultParameters().entrySet().stream().forEach(tuple -> lines.add(tuple.getKey()+" := "+tuple.getValue()));
		return lines;
	}
	
	private List<String> retrieveVariationParameters(Map<String, String> algorithmGeneticOperators, Boolean isCrossover){
		List<String> lines = Lists.newArrayList();
		String variationClazzPath = "";
		if(isCrossover){
			variationClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_CROSSOVER_INFILE_TOKEN));
		}else{
			variationClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_MUTATION_INFILE_TOKEN));
		}
		IVariation<?> variation = (IVariation<?>) ObjectUtils.createObject(variationClazzPath, null, null);
		if(variation.hasInputParameters())
			variation.getInputDefaultParameters().entrySet().stream().forEach(tuple -> lines.add(tuple.getKey()+" := "+tuple.getValue()));
		
		return lines;
	}
	
	private List<String> retrieveEngineParameters(Map<String, String> algorithmGeneticOperators){
		List<String> lines = Lists.newArrayList();
		String engineClazzPath = ClassesUtils.findClassPackageByName("SparqlEngine");
		IEngine engine = (IEngine) ObjectUtils.createObject(engineClazzPath, null, null);
		if(engine.hasInputParameters())
			engine.getInputDefaultParameters().entrySet().stream().forEach(tuple -> lines.add(tuple.getKey()+" := "+tuple.getValue()));
		return lines;
	}
	
	private List<String> retrieveAttributeSelectorParameters(Map<String, String> algorithmGeneticOperators){
		List<String> lines = Lists.newArrayList();
		String attributeSelectorClazzPath = ClassesUtils.findClassPackageByName(algorithmGeneticOperators.get(FrameworkConfiguration.ALGORITHM_ATTRIBUTE_LEARNER_INFILE_TOKEN));
		IAttributeSelector attrSelector = (IAttributeSelector) ObjectUtils.createObject(attributeSelectorClazzPath, null, null);
		if(attrSelector.hasInputParameters())
			attrSelector.getInputDefaultParameters().entrySet().stream().forEach(tuple -> lines.add(tuple.getKey()+" := "+tuple.getValue()));
		return lines;
	}
	
	
	private List<String> saveGAFeaturesIntoFile(String name, String selector, String initializer, String replacement, String fitness, String crossover, String mutation, List<String> aggregates, List<String> metrics, List<String> transformations, String attributeSelector, String engine){
		List<String> features = Lists.newArrayList();
		features.add("# -----");
		features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_NAME_INFILE_TOKEN, name));
		features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_SELECTOR_INFILE_TOKEN, selector));
		features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_INITIALIZATION_INFILE_TOKEN, initializer));
		features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_REPLACEEMT_INFILE_TOKEN, replacement));
		features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_FITNESS_INFILE_TOKEN, fitness));
		features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_CROSSOVER_INFILE_TOKEN, crossover));
		features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_MUTATION_INFILE_TOKEN, mutation));
		features.add("# -----");
		features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_ENVINE_INFILE_TOKEN, engine));
		features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_ATTRIBUTE_LEARNER_INFILE_TOKEN, attributeSelector));
		features.add("# -----");
		aggregates.forEach(elem -> features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_AGGREGATES_INFILE_TOKEN, elem)));
		metrics.forEach(elem -> features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_STRING_METRICS_INFILE_TOKEN, elem)));
		transformations.forEach(elem -> features.add(generateInFileLine(FrameworkConfiguration.ALGORITHM_TRANSFORMATIONS_INFILE_TOKEN, elem)));
		return features;
	}
	
	private String generateInFileLine(String prefix, String value){
		StringBuffer str = new StringBuffer();
		if(value!=null){
			str.append(prefix).append(" := ").append(value);
		}else{
			str.append("#").append(prefix).append(" := empty");
		}
		return str.toString();
	}


	@Override
	public void removeAlgorithm(String algorithmName) {
		try{
			File toRemove1 = new File(GuiConfiguration.ALGORTIHMS_DIRECTORY+""+algorithmName+".cnf");
			File toRemove2 = new  File(GuiConfiguration.ALGORTIHMS_DIRECTORY+""+algorithmName+"_setup.cnf");
			toRemove1.delete();
			toRemove2.delete();
		} catch (Exception e){
			e.printStackTrace();
		}
	}



	@Override
	public Set<String> getIntializersNameList() {
		return GuiConfiguration.initializersClasses;
	}

	@Override
	public Set<String> getSelectorsNameList() {
		return GuiConfiguration.selectorsClasses;
	}

	@Override
	public Set<String> getReplacementsNameList() {
		return GuiConfiguration.replacementsClasses;
	}

	@Override
	public Set<String> getCrossoversNameList() {
		return GuiConfiguration.crossoversClasses;
	}

	@Override
	public Set<String> getMutationsNameList() {
		return GuiConfiguration.mutationsClasses;
	}
	
	@Override
	public Set<String> getFitnessNameList() {
		return GuiConfiguration.fitnessClasses;
	}

	@Override
	public Set<String> getAggregatesNameList() {
		return GuiConfiguration.aggregatesClasses;
	}

	@Override
	public Set<String> getAttributeLearnerNameList() {
		return GuiConfiguration.attributeLearnerClasses;
	}

	@Override
	public Set<String> getMetricsNameList() {
		return GuiConfiguration.stringMetricsClasses;
	}

	@Override
	public Set<String> getTransformationsNameList() {
		return GuiConfiguration.transfromationsClasses;
	}

}
