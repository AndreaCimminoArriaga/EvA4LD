package tdg.link_discovery.connector.sparql.learner.attributes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Sets;

import tdg.link_discovery.connector.sparql.engine.sample_reader.NTSampleReader;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.attribute_selector.SPARQLAttributeSelector;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.IARQStringSimilarity;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.LevenshteinSimilarity;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.IARQUnaryTransformation;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.LowercaseTransformation;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.TokenizeTransformation;
import tdg.link_discovery.framework.algorithm.sample.Sample;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.learner.attributes.AbstractAttributeSelector;
import tdg.link_discovery.middleware.objects.Tuple;

public class LevenshteinFilter extends AbstractAttributeSelector {


	private IARQStringSimilarity similarity;
	private Double threshold;
	private IARQUnaryTransformation lowercase;
	private IARQUnaryTransformation tokenize;
	
	public LevenshteinFilter(){
		super(null,"levenshtein filter 0.99");
		this.similarity = new LevenshteinSimilarity();
		this.threshold = 0.99;
		lowercase = new LowercaseTransformation();
		tokenize = new TokenizeTransformation();
	}
	
	public LevenshteinFilter(IEnvironment environment){
		super(environment,"levenshtein filter 0.99");
		this.similarity = new LevenshteinSimilarity();
		this.threshold = 0.99;
		lowercase = new LowercaseTransformation();
		tokenize = new TokenizeTransformation();
	}
	
	
	@Override
	public Set<Tuple<String,String>> getAttributesToCompare() {
		Set<Tuple<String,String>> suitableAttributesToCompare = Sets.newHashSet();
		
		if(!this.positiveExamples.isEmpty()) {
			//suitableAttributesToCompare = readFromExamplesFromModels();
			suitableAttributesToCompare = readFromExamplesFromEnvironments();
		}else {
			suitableAttributesToCompare = readFromEnvironmentExamplesFile();
		}
		//suitableAttributesToCompare.forEach(attrs -> System.out.println(attrs));
		return suitableAttributesToCompare;
	}

	private Set<Tuple<String, String>> readFromExamplesFromEnvironments() {
		
		// Read examples
		List<String> sourceSample = this.positiveExamples.stream().map(sample-> sample.getFirstElement()).collect(Collectors.toList());
		List<String> targetSample = this.positiveExamples.stream().map(sample-> sample.getSecondElement()).collect(Collectors.toList());
	
		// for each example load all the attr and its values
		Set<Tuple<String, String>> sourceAttributes = loadAttributesFromExamples(environment.getSourceDatasetFile(),sourceSample);
		Set<Tuple<String, String>> targetAttributes = loadAttributesFromExamples(environment.getTargetDatasetFile(),targetSample);
		
		// cartesian products
		@SuppressWarnings("unchecked")
		Set<List<Tuple<String, String>>> attributes = Sets.cartesianProduct(sourceAttributes, targetAttributes);
		// Get suitable attributes and remove repeated
		Set<Tuple<String, String>> suitableAttributes = attributes.stream().map(pair -> keepTuple(pair))
				.filter(pair -> (pair.getFirstElement() != null && pair.getSecondElement() != null))
				.collect(Collectors.toSet());
		return suitableAttributes;
	}
	
	@Deprecated
	private Set<Tuple<String,String>> readFromExamplesFromModels() {
		Set<String> positiveSourceSamples = this.positiveExamples.stream().map(tuple -> tuple.getFirstElement()).collect(Collectors.toSet());
		Set<String> positiveTargetSamples = this.positiveExamples.stream().map(tuple -> tuple.getSecondElement()).collect(Collectors.toSet());
		Set<Tuple<String,String>> sourceAttributes = Sets.newHashSet();
		Set<Tuple<String,String>> targetAttributes = Sets.newHashSet();
		for(List<String> modelTuple:this.models) {
			sourceAttributes.addAll(readFromModels(modelTuple.get(0), positiveSourceSamples));
			targetAttributes.addAll(readFromModels(modelTuple.get(1), positiveTargetSamples));
		}
		// cartesian products
		@SuppressWarnings("unchecked")
		Set<List<Tuple<String,String>>> attributes = Sets.cartesianProduct(sourceAttributes, targetAttributes);
		// Get suitable attributes and remove repeated
		Set<Tuple<String,String>> suitableAttributes = attributes.stream().map(pair -> keepTuple(pair)).filter(pair-> (pair.getFirstElement()!= null && pair.getSecondElement()!=null)).collect(Collectors.toSet());
		return suitableAttributes;
	}
	
	@Deprecated
	private Set<Tuple<String, String>> readFromModels(String model, Collection<String> samples) {
		Set<Tuple<String,String>> attributes = Sets.newHashSet();
		SPARQLAttributeSelector attrSelector = new SPARQLAttributeSelector();
		samples.stream().forEach(sample -> attributes.addAll(attrSelector.retrieveAttributesFromExampleInModel(model, sample)));
		return attributes;
	}

	private Set<Tuple<String,String>> readFromEnvironmentExamplesFile() {
		NTSampleReader reader = new NTSampleReader();
		// Read examples
		Collection<Sample<String>> samples = reader.readSamplesFromFile(environment.getExamplesFile());
		List<String> sourceSample = samples.stream().filter(sample -> sample.getIsPositive()).map(sample-> sample.getElement1()).collect(Collectors.toList());
		List<String> targetSample = samples.stream().filter(sample -> sample.getIsPositive()).map(sample-> sample.getElement2()).collect(Collectors.toList());
		
		// for each example load all the attr and its values
		Set<Tuple<String,String>> sourceAttributes = loadAttributesFromExamples(environment.getSourceDatasetFile(), sourceSample);
		Set<Tuple<String,String>> targetAttributes = loadAttributesFromExamples(environment.getTargetDatasetFile(), targetSample);
	
		// cartesian products
		@SuppressWarnings("unchecked")
		Set<List<Tuple<String,String>>> attributes = Sets.cartesianProduct(sourceAttributes, targetAttributes);
		// Get suitable attributes and remove repeated
		Set<Tuple<String,String>> suitableAttributes = attributes.stream().map(pair -> keepTuple(pair)).filter(pair-> (pair.getFirstElement()!= null && pair.getSecondElement()!=null)).collect(Collectors.toSet());
		return suitableAttributes;
	}

	private Tuple<String,String> keepTuple(List<Tuple<String,String>> attributePair){
		// Retrieve attribute values
		String value1 = attributePair.get(0).getSecondElement();
		String value2 = attributePair.get(1).getSecondElement();
		// Apply transformations
		value1 = tokenize.applyUnaryTransformation(lowercase.applyUnaryTransformation(value1));
		value2 = tokenize.applyUnaryTransformation(lowercase.applyUnaryTransformation(value2));
		// Apply score to find suitable attributes
		Tuple<String,String> suitableAttr = new Tuple<String,String>(null, null);
		
		if( similarity.similarity(value1, value2, threshold)>0){
			// Initialize suitable attributes with current attr labels
			suitableAttr.setFirstElement(attributePair.get(0).getFirstElement());
			suitableAttr.setSecondElement(attributePair.get(1).getFirstElement());
		}
	
		return suitableAttr;
	}
	
	private Set<Tuple<String,String>> loadAttributesFromExamples(String dataset, List<String> samples){
		Set<Tuple<String,String>> attributes = Sets.newHashSet();
		SPARQLAttributeSelector attrSelector = new SPARQLAttributeSelector();
		for(String iri:samples){
			attributes.addAll(attrSelector.retrieveAttributesFromExample(dataset, iri));
		}
		return attributes;
	}




	@Override
	public Map<String, Double> getInputDefaultParameters() {
		return null;
	}

	@Override
	public Boolean hasInputParameters() {
		return false;
	}


	@Override
	public void setInputParameters(String parameterName, Object value) {
		// empty
	}


	
	
	
}
