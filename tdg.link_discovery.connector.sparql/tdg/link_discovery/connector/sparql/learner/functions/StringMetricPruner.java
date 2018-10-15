package tdg.link_discovery.connector.sparql.learner.functions;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.jena.ext.com.google.common.collect.Lists;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import tdg.link_discovery.connector.sparql.engine.sample_reader.NTSampleReader;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.attribute_selector.SPARQLAttributeSelector;
import tdg.link_discovery.framework.algorithm.sample.Sample;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.learner.functions.FunctionsFactory;
import tdg.link_discovery.framework.learner.functions.StringMetricFunction;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.objects.comparators.DoubleNaturalComparator;
import tdg.link_discovery.middleware.utils.Utils;

public class StringMetricPruner {

	private Integer kClusterIterations;
	
	
	public StringMetricPruner(){
		this.kClusterIterations = 10000;
	}
	
	public Integer getkClusterIterations() {
		return kClusterIterations;
	}

	public void pruneAvailableStringMetrics(IEnvironment environment){
		// Read examples
		NTSampleReader reader = new NTSampleReader();
		Collection<Sample<String>> samples = reader.readSamplesFromFile(environment.getExamplesFile());
		// Obtain tuples of positive and attribute values: (0, valueSource, valuetarget), i.e., positive, or (1, valueSource, valuetarget), i.e., negative
		Multimap<Integer,Tuple<String,String>> atributeTuples = loadSuitableAttributesFromExamples(environment, samples);
		
		// Obtain tuple (0/1, stringMetricName, score) that can be clustered later
		List<ClusterablePoint> positivePoints = Lists.newArrayList();
		List<ClusterablePoint> negativePoints = Lists.newArrayList();
		// Store points by average scoring
		for(StringMetricFunction metric: FunctionsFactory.similarityFunctions){
			List<Double> positiveScores = atributeTuples.get(0).stream().map(tuple -> metric.compareStrings(tuple.getFirstElement(), tuple.getSecondElement())).collect(Collectors.toList());
			List<Double> negativeScores = atributeTuples.get(1).stream().map(tuple -> metric.compareStrings(tuple.getFirstElement(), tuple.getSecondElement())).collect(Collectors.toList());
			positivePoints.add(new ClusterablePoint(metric, Utils.roundDecimal(positiveScores.stream().mapToDouble(d -> d).average().getAsDouble(),4)));
			negativePoints.add(new ClusterablePoint(metric, Utils.roundDecimal(negativeScores.stream().mapToDouble(d -> d).average().getAsDouble(),4)));

		}
		// Cluster Points
		KMeansPlusPlusClusterer<ClusterablePoint> clusterer = new KMeansPlusPlusClusterer<ClusterablePoint>(2, kClusterIterations);
		List<CentroidCluster<ClusterablePoint>> clusters = clusterer.cluster(positivePoints);
		CentroidCluster<ClusterablePoint> maxPositiveCluster = getMaximumScoreClusters(clusters);
		List<CentroidCluster<ClusterablePoint>> clusters2 = clusterer.cluster(negativePoints);
		CentroidCluster<ClusterablePoint> maxNegativeCluster = getMaximumScoreClusters(clusters2);
		
		// get metrics that better score obtain positive examples and negative examples
		List<StringMetricFunction> positiveMetrics = maxPositiveCluster.getPoints().stream().map(points -> points.getMetric()).collect(Collectors.toList());
		List<StringMetricFunction> negativeMetrics = maxNegativeCluster.getPoints().stream().map(points -> points.getMetric()).collect(Collectors.toList());
		List<StringMetricFunction> positiveMetricsAux = maxPositiveCluster.getPoints().stream().map(points -> points.getMetric()).collect(Collectors.toList());
		// if the setminus is non-empty then: positiveMetrics setminus negativeMetrics; else positiveMetrics
		positiveMetricsAux.removeAll(negativeMetrics);
		if(positiveMetricsAux.size()>0){
			positiveMetrics.removeAll(negativeMetrics);
		}
				
		// set new metrics
		FunctionsFactory.similarityFunctions = Lists.newArrayList();
		FunctionsFactory.similarityFunctions = positiveMetrics;
	}
	
	private CentroidCluster<ClusterablePoint> getMaximumScoreClusters(List<CentroidCluster<ClusterablePoint>> clusters){
		CentroidCluster<ClusterablePoint> finalCluster = null;
		
		List<Double> points0 = clusters.get(0).getPoints().stream().map(elem -> elem.getScore()).collect(Collectors.toList());
		List<Double> points1 = clusters.get(1).getPoints().stream().map(elem -> elem.getScore()).collect(Collectors.toList());
		
		Double min0 = points0.stream().min(new DoubleNaturalComparator()).get();
		Double min1 = points1.stream().min(new DoubleNaturalComparator()).get();
		
		if(Math.max(min1, min0) == min0){
			finalCluster = clusters.get(0);
		}else{
			finalCluster = clusters.get(1);
		}
		return finalCluster;
	}
	

	
	private Multimap<Integer,Tuple<String,String>> loadSuitableAttributesFromExamples(IEnvironment environemnt, Collection<Sample<String>> samples){
		Multimap<Integer, Tuple<String,String>> attributes = ArrayListMultimap.create();
		
		SPARQLAttributeSelector attrSelector = new SPARQLAttributeSelector();
		
		for(Sample<String> sample:samples){
			Set<Tuple<String,String>> sourceAttributes = attrSelector.retrieveAttributesFromExample(environemnt.getSourceDatasetFile(), sample.getElement1());
			Set<Tuple<String,String>> targetAttributes = attrSelector.retrieveAttributesFromExample(environemnt.getTargetDatasetFile(), sample.getElement2());
			List<Tuple<String, String>> prunedAttributes = pruneAttributes(sourceAttributes, targetAttributes);
			if(sample.getIsPositive()){
				attributes.putAll(0, prunedAttributes);
			}else{
				attributes.putAll(1, prunedAttributes);
			}
			
		}
		return attributes;
	}
	
	private List<Tuple<String, String>> pruneAttributes(Set<Tuple<String,String>>  source, Set<Tuple<String,String>>  target){
		List<Tuple<String,String>> values = Lists.newArrayList();
		
		for(Tuple<String,String> labels:FunctionsFactory.suitableAttributes){
			String sourceLabel = labels.getFirstElement();
			String targetLabel = labels.getSecondElement();
			
			List<String> sourceValues = source.stream().filter(tuple -> tuple.getFirstElement().equals(sourceLabel)).map(tuple-> tuple.getSecondElement()).collect(Collectors.toList());
			List<String> targetValues = target.stream().filter(tuple -> tuple.getFirstElement().equals(targetLabel)).map(tuple-> tuple.getSecondElement()).collect(Collectors.toList());
			
			values.addAll(toListOfTuples(sourceValues, targetValues));
		}
	
		return values;
	}
	
	private List<Tuple<String,String>> toListOfTuples(List<String> source, List<String> target){
		List<Tuple<String,String>> attributesCombined = Lists.newArrayList();
		for(String sValue:source){
			for(String tValue:target){
				attributesCombined.add(new Tuple<String,String>(sValue,tValue));
			}
		}
		return attributesCombined;
	}
	
	
	private static class ClusterablePoint implements Clusterable{
		public static Double id = 0.0;
		private Double score;
		private StringMetricFunction metric;

		
		public ClusterablePoint(StringMetricFunction metric, Double score){
			id++;
			this.score = score;
			this.metric=metric;
	
		}
		
		@Override
		public double[] getPoint() {
			return new double[]{id,score};
		}

		@Override
		public String toString() {
			return "ClusterablePoint [name=" + metric+" = "+score + "]";
		}
		
		public Double getScore(){
			return score;
		}
		
		public StringMetricFunction getMetric(){
			return metric;
		}
		
		
	}
	
}
