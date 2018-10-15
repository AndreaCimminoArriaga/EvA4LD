package tdg.link_discovery.framework.learner;

import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;

import tdg.link_discovery.framework.algorithm.IAlgorithm;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.engine.IEngine;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.learner.attributes.IAttributeSelector;
import tdg.link_discovery.framework.learner.functions.AggregateFunction;
import tdg.link_discovery.framework.learner.functions.FunctionsFactory;
import tdg.link_discovery.framework.learner.functions.StringMetricFunction;
import tdg.link_discovery.framework.learner.functions.TransformationFunction;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;


public abstract class AbstractLearner implements ILearner{

	protected IEnvironment linkEnvironment;
	protected IAlgorithm algorithm;
	protected Setup algorithmSetup;
	protected IAttributeSelector attributeSelector;
	protected IEngine engine;
	
	public AbstractLearner(IEnvironment configuration, Setup algorithmSetup) {
		super();
		initAttributes();
		this.linkEnvironment = configuration;
		this.algorithmSetup = algorithmSetup;
		FunctionsFactory.aggregateFunctions = Lists.newArrayList();
		FunctionsFactory.similarityFunctions = Lists.newArrayList();
		FunctionsFactory.suitableAttributes = Lists.newArrayList();
		FunctionsFactory.transformationsFunctions = Lists.newArrayList();
	}
	
	public AbstractLearner() {
		super();
		initAttributes();
	}
	
	private void initAttributes(){
		this.algorithm = null;
		this.attributeSelector = null;
	}
	
	// Learner algorithm methods
	
	@Override
	public IAlgorithm getAlgorithm() {
		return algorithm;
	}
	
	@Override
	public void setAlgorithm(IAlgorithm algorithm) {
		this.algorithm = algorithm;
	}
	
	@Override
	public void setAlgorithmSetup(Setup setup){
		algorithmSetup = setup;
	}
	
	@Override
	public Setup getAlgorithmSetup(){
		return this.algorithmSetup;
	}
	
	// Learner attribute selector methods
	
	@Override
	public IAttributeSelector getAttributeSelector() {
		return attributeSelector;
	}
	
	@Override
	public void setAttributeSelector(IAttributeSelector attributeSelector) {
		this.attributeSelector = attributeSelector;
		
	}
	
	
	@Override
	public IEnvironment getConfiguration() {
		return linkEnvironment;
	}
	
	@Override
	public void setConfiguration(IEnvironment configuration) {
		this.linkEnvironment = configuration;
	}
	

	@Override
	public Double getAlgorithmProgress() {
		if(this.algorithm == null)
			return null;
		return this.algorithm.getAlgorithmProgress();
	}

	@Override
	public Boolean hasAttributeLearner() {
		// TODO Auto-generated method stub
		return this.attributeSelector!=null;
	}

	
	// Learner functions methods
	
	@Override
	public void addLearnerAggregateFunction(AggregateFunction aggregate){
		FunctionsFactory.aggregateFunctions.add(aggregate);
	}
	
	@Override
	public void addLearnerStringMetricFunction(StringMetricFunction stringMetric){
		FunctionsFactory.similarityFunctions.add(stringMetric);
	}
	
	@Override
	public void addLearnerTransformationFunction(TransformationFunction transformation){
		FunctionsFactory.transformationsFunctions.add(transformation);
	}

	@Override
	public void cleanSuitableAttributes(){
		FunctionsFactory.suitableAttributes.clear();
	}
	
	@Override
	public void addSuitableAttributes(String attributeSource, String attributeTarget){
		FunctionsFactory.suitableAttributes.add(new Tuple<String,String>(attributeSource,attributeTarget));
	}
	
	@Override
	public Boolean existAttributesToCompare(){
		return !FunctionsFactory.suitableAttributes.isEmpty();
	}
	
	// Engine methods
	@Override
	public IEngine getEngine() {
		return engine;
	}
	
	@Override
	public void setEngine(IEngine engine) {
		this.engine = engine;
	}

	// Learning methods
	@Override
	public <T extends Object> List<ISpecification<T>> getLearnedSpecifications(){
		return this.algorithm.getLearnedSpecifications();
	}
	
	@Override
	public <T extends Object> List<ISpecification<T>> getBestLearnedSpecifications(){
		return this.algorithm.getBetterScoredSpecifications();
	}
	
	@Override
	public void saveLearnedSpecifications(){
		String file = linkEnvironment.getSpecificationOutput();
		if(file!=null && !file.isEmpty()){
			getLearnedSpecifications().stream().forEach(ls -> Utils.appendLineInCSV(file, prepareCSVLine(ls.toString())));
		}
	}
	
	
	@Override
	public void saveBestLearnedSpecifications(){
		String file = linkEnvironment.getSpecificationOutput();
		if(file!=null && !file.isEmpty()){
			getBestLearnedSpecifications().stream().forEach(ls -> Utils.appendLineInCSV(file, prepareCSVLine(ls.toString())));
		}
	}
	
	private String prepareCSVLine(String ls){
		StringBuffer line = new StringBuffer();
		line.append("\"").append(linkEnvironment.getSourceDatasetFile()).append("\",");
		line.append("\"").append(linkEnvironment.getTargetDatasetFile()).append("\",");
		line.append("\"").append(this.algorithm.getName()).append("\",");
		line.append("\"").append(ls.toString()).append("\"\n");
		return line.toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("\t--Learner\n");
		str.append("Algorithm name :=").append(this.algorithm.getName()).append("\n");
		if(this.attributeSelector!=null){
			str.append("Attr. Selector := ").append(this.attributeSelector.toString()).append("\n");
		}else{
			str.append("Attr. Selector := ").append("none").append("\n");
		}
		str.append("Engine := ").append(this.engine.getName()).append("\n\n");
		str.append("-->Genetic Operators\n").append(this.algorithm.getOperatorsSetup().toString()).append("\n");
		str.append("-->Setup\n").append(this.algorithmSetup.toString()).append("\n");
		str.append("\n");
		return str.toString();
	}
	
	//TODO: Create method similar toString that saves the full learner in a file so others can import it
	
	
}
