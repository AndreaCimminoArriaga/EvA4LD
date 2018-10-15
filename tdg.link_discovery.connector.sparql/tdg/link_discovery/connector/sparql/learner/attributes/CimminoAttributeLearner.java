package tdg.link_discovery.connector.sparql.learner.attributes;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import tdg.link_discovery.connector.sparql.engine.sample_reader.NTSampleReader;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.attribute_selector.SPARQLAttributeSelector;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.CosineSimilarity;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.LevenshteinSimilarity;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.StripUriPrefixTransformation;
import tdg.link_discovery.framework.algorithm.sample.Sample;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.learner.attributes.AbstractAttributeSelector;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.objects.comparators.DoubleNaturalComparator;

public class CimminoAttributeLearner extends AbstractAttributeSelector{

	private Integer kmeansIterations;
	private Integer kSize;
	
	public CimminoAttributeLearner(){
		super(null,"cimminoAttributeLearner");
		kmeansIterations = 1000;
		kSize=2;
	}
	
	public CimminoAttributeLearner(IEnvironment environment){
		super(environment,"cimminoAttributeLearner");
		kmeansIterations = 1000;
		kSize=2;
	}
	
	public Integer getKmeansIterations() {
		return kmeansIterations;
	}

	public void setKmeansIterations(Integer kmeansIterations) {
		this.kmeansIterations = kmeansIterations;
	}

	@Override
	public Set<Tuple<String, String>> getAttributesToCompare() {
		Set<Tuple<String,String>> suitableAttributesToCompare = Sets.newHashSet();
		kSize=2;
		/*if(!this.positiveExamples.isEmpty()) {
			suitableAttributesToCompare = readFromExamplesFromModels();
		}else {*/
			suitableAttributesToCompare = readFromEnvironmentExamplesFile();
		//}
		return suitableAttributesToCompare;
	}
	
	
	

	private Set<Tuple<String, String>> readFromExamplesFromModels() {
		// Read attributes from models
		Set<String> positiveSourceSamples = this.positiveExamples.stream().map(tuple -> tuple.getFirstElement()).collect(Collectors.toSet());
		Set<String> positiveTargetSamples = this.positiveExamples.stream().map(tuple -> tuple.getSecondElement()).collect(Collectors.toSet());
		Set<Tuple<String,String>> sourceAttributes = Sets.newHashSet();
		Set<Tuple<String,String>> targetAttributes = Sets.newHashSet();
		for(List<String> modelTuple:this.models) {
			sourceAttributes.addAll(readFromModels(modelTuple.get(0), positiveSourceSamples));
			targetAttributes.addAll(readFromModels(modelTuple.get(1), positiveTargetSamples));
		}
		
		
		// Group by attribute labels
		Multimap<String, String> sourceAttributesGrouped = toMultimap(sourceAttributes);
		Multimap<String, String> targetAttributesGrouped = toMultimap(targetAttributes);
		
		// Rank by uniqness and maximal coverage, as lower the score as uniq the attribute is
		Map<String,Double> uniqMaximalCoverageSourceAttributes = getUniqMaximalCoverageAttributes(sourceAttributesGrouped);
		Map<String,Double> uniqMaximalCoverageTargetAttributes = getUniqMaximalCoverageAttributes(targetAttributesGrouped);
		System.out.println(uniqMaximalCoverageSourceAttributes);
		System.out.println(uniqMaximalCoverageTargetAttributes);
		// Clusterize and select suitable attributes
		Map<String,Double> suitableSourceAttributes = clusterValues(uniqMaximalCoverageSourceAttributes);
		Map<String,Double> suitableTargetAttributes = clusterValues(uniqMaximalCoverageTargetAttributes);
		
		
		// Rank by semantic distance of labels
		Set<Tuple<String,String>> suitableAttributes = pairWiseLabels(suitableSourceAttributes, suitableTargetAttributes);
		return suitableAttributes;
	
	}
	
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
		
		// Group by attribute labels
		Multimap<String, String> sourceAttributesGrouped = toMultimap(sourceAttributes);
		Multimap<String, String> targetAttributesGrouped = toMultimap(targetAttributes);
		
		// Rank by uniqness and maximal coverage, as lower the score as uniq the attribute is
		Map<String,Double> uniqMaximalCoverageSourceAttributes = getUniqMaximalCoverageAttributes(sourceAttributesGrouped);
		Map<String,Double> uniqMaximalCoverageTargetAttributes = getUniqMaximalCoverageAttributes(targetAttributesGrouped);
		
		// Clusterize and select suitable attributes
		Map<String,Double> suitableSourceAttributes = clusterValues(uniqMaximalCoverageSourceAttributes);
		Map<String,Double> suitableTargetAttributes = clusterValues(uniqMaximalCoverageTargetAttributes);
		
		// Pairwise by semantic distance of labels
		Set<Tuple<String,String>> suitableAttributes = pairWiseLabels(suitableSourceAttributes, suitableTargetAttributes);
		
		return suitableAttributes;
	}

	
	private Set<Tuple<String, String>> pairWiseLabels(Map<String, Double> sourceLabels, Map<String, Double> targetLabels) {
		Set<Tuple<String,String>> suitableAttributes = Sets.newHashSet();
		Set<String> targetUsed = Sets.newHashSet();
		
		// First link attributes with the highest label similarity score
		for(String sourceLabel:sourceLabels.keySet()) {
			Double maxScore = -1.0;
			Tuple<String,String> pair = new Tuple<String,String>("","");
			for(String targetLabel:targetLabels.keySet()) {
				Double score = compareLabels(sourceLabel,targetLabel);
				if(score > maxScore) {
					maxScore = score;
					pair= new Tuple<String,String>(sourceLabel,targetLabel);
				}
			}
			// Add current pair of suitable attributes
			if(!suitableAttributes.contains(pair) && !targetUsed.contains(pair.getSecondElement())){
				suitableAttributes.add(pair);
				targetUsed.add(pair.getSecondElement());
			}
			
		}
		// check if there's a target attribute not paired, in that case pair it with some
		List<String> nonMatchedLabels = targetLabels.keySet().stream().filter(label -> suitableAttributes.stream().noneMatch(tuple -> tuple.getSecondElement().equals(label))).collect(Collectors.toList());
		targetUsed.clear();
		if(!nonMatchedLabels.isEmpty()) {
			for(String targetLabel:nonMatchedLabels) {
				Double maxScore = 0.0;
				Tuple<String,String> pair = new Tuple<String,String>("","");
				for(String sourceLabel:sourceLabels.keySet()) {
					Double score = compareLabels(sourceLabel,targetLabel);
					if(score >= maxScore) {
						maxScore = score;
						pair= new Tuple<String,String>(sourceLabel,targetLabel);
					}
				}
				// Add current pair of suitable attributes
				if(!suitableAttributes.contains(pair) && !targetUsed.contains(targetLabel)){
					suitableAttributes.add(pair);
					targetUsed.add(targetLabel);
				}
			}
		}
		suitableAttributes.stream().forEach(tuple -> System.out.println(tuple));
		
		return suitableAttributes;
	}
	
	private double compareLabels(String sourceLabel, String targetLabel) {
		LevenshteinSimilarity lev = new LevenshteinSimilarity();
		StripUriPrefixTransformation transformation = new StripUriPrefixTransformation();
		String sourceLabelAux = transformation.applyUnaryTransformation(sourceLabel);
		String targetLabelAux = transformation.applyUnaryTransformation(targetLabel);
		
		return  lev.compareStrings(sourceLabelAux, targetLabelAux);
	}


	private Set<Tuple<String,String>> loadAttributesFromExamples(String dataset, List<String> samples){
		Set<Tuple<String,String>> attributes = Sets.newHashSet();
		SPARQLAttributeSelector attrSelector = new SPARQLAttributeSelector();
		for(String iri:samples){
			attributes.addAll(attrSelector.retrieveAttributesFromExample(dataset, iri));
		}
		return attributes;
	}
	
	private Multimap<String, String> toMultimap(Set<Tuple<String, String>> attributes){
		Multimap<String, String> attributesGrouped = ArrayListMultimap.create();
		attributes.stream().forEach(tuple -> attributesGrouped.put(tuple.getFirstElement(), tuple.getSecondElement()));
		return attributesGrouped;
	}
	
	private Map<String,Double> getUniqMaximalCoverageAttributes(Multimap<String, String> attributesGrouped) {
		Map<String, Double> uniqAttributes = Maps.newHashMap();
		Double maximalCoverage = maximalCoverage(attributesGrouped);
		for(String label:attributesGrouped.keySet()){
			List<Double> scores = applyMetrics(Lists.newArrayList(attributesGrouped.get(label)));
			System.out.println(label+" "+scores);
			if(!scores.isEmpty()){
				// Coverage of current attribute: every instance has to be indentified, so has higher the coverage
				// as likely is an attribute that universally identifies the instace
				Integer coverage = attributesGrouped.get(label).size();
				// Dissimilarity among attribute values: ass dissimilar the attribute is, as likely universally identifies the instace
				Double average = scores.stream().mapToDouble(d -> d).average().getAsDouble();
				
				// inverseCoveragePercentage: combines the average similarity (which aims to be 0 due to the uniqueness) and the inverse coverage, i.e.,
				// the percetaje of instances covered by the attributes which tends to 1 in the best case, the inverse is 1- coverage.
				Double inverseCoveragePercentage = 1-(coverage/maximalCoverage); 
				Double uniqMaximalCoverageScore = (average+inverseCoveragePercentage)*0.5;
				uniqAttributes.put(label, uniqMaximalCoverageScore);
				
			}			
		}
		return uniqAttributes;
	}

	private Double maximalCoverage(Multimap<String, String> attributesGrouped){
		Double maximalCoverage = 0.0;
		for(String label:attributesGrouped.keySet()){
			Integer coverage = attributesGrouped.get(label).size();
			if(coverage > maximalCoverage)
				maximalCoverage = coverage*1.0;
		}
		return maximalCoverage;
	}

	private List<Double> applyMetrics(List<String> values) {
		CosineSimilarity cosine = new CosineSimilarity();
		LevenshteinSimilarity levenshtein = new LevenshteinSimilarity();
		List<Double> scores = Lists.newArrayList();
		// Lowercase normalization:
		values = values.stream().map(value -> value.toLowerCase()).collect(Collectors.toList());
		for(int i=0; i< values.size(); i++){
			for(int j=0; j< values.size(); j++){
				if(j!=i){ // Avoids comparing one element with itself, but still compring it with other that are exactly the same
					Double score = (cosine.compareStrings(values.get(i), values.get(j)) + levenshtein.compareStrings(values.get(i), values.get(j)));
					scores.add(score);
				}
			}
		}
		return scores;
	}
	

	private Map<String,Double> clusterValues(Map<String, Double> uniqMaximalCoverageAttributes) {
		KMeansPlusPlusClusterer<ClusterablePoint> clusterer = new KMeansPlusPlusClusterer<ClusterablePoint>(kSize, kmeansIterations);
		
		List<ClusterablePoint> pointsToCluster = uniqMaximalCoverageAttributes.values().stream().map(v-> new ClusterablePoint(v)).collect(Collectors.toList());
		
		// Obtain Clusters
		List<CentroidCluster<ClusterablePoint>> clusters = clusterer.cluster(pointsToCluster);
		List<Double> points0 = clusters.get(0).getPoints().stream().map(elem -> elem.getValue()).collect(Collectors.toList()); 
		List<Double> points1 = clusters.get(1).getPoints().stream().map(elem -> elem.getValue()).collect(Collectors.toList()); 
		// Find cluster with minimal values, store its points in attributePoints
		Double min0 = points0.stream().min(new DoubleNaturalComparator()).get();
		Double min1 = points1.stream().min(new DoubleNaturalComparator()).get();

		List<Double> attributePoints = Lists.newArrayList();
		if(Math.min(min1, min0) == min0){
			points0.stream().forEach(value -> attributePoints.add(value));
		}else{
			points1.stream().forEach(value -> attributePoints.add(value));
		}
		// Keep attributes from input that where in the cluster
		Map<String, Double> prunedAttributes = uniqMaximalCoverageAttributes.entrySet().stream().filter(entry -> attributePoints.contains(entry.getValue())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		return prunedAttributes;
	}
	
	private static class ClusterablePoint implements Clusterable{
		public static Double id = 0.0;
		private Double value;
		
		public ClusterablePoint(Double value){
			id++;
			this.value = value;
		}
		
		@Override
		public double[] getPoint() {
			return new double[]{id,value};
		}

		@Override
		public String toString() {
			return "ClusterablePoint [value=" + value + "]";
		}
		
		public Double getValue(){
			return value;
		}
		
		
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
