package tdg.moea.test.advanced;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.tdb.TDBFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Avg;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Max;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Min;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.*;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.environment.Environments;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;

public class SampleSetAnalyzer {

	private static List<Tuple<String,String>> positiveSamples;
	private static List<Tuple<String,String>> negativeSamples;
	private static Map<String,List<Double>> positiveScores, negativeScores;
	private static Model datasetSource, datasetTarget;
	
	public static void main(String[] args) throws Exception {
		String scenario = "restaurants";// args[0];
		String sourceDatasetDir = "/Users/cimmino/Desktop/tdb-data/restaurants1";//args[1];//
		String targetDatasetDir = "/Users/cimmino/Desktop/tdb-data/restaurants2";// args[2];//
		Boolean showStats = true;
		Boolean showSuspiciusNegativeSamples = false;

		datasetSource = getTDBModel(sourceDatasetDir);
		datasetTarget = getTDBModel(targetDatasetDir);
		
		// working with specific learning file
		System.out.println("\"metric\",\"source_attribute\",\"target_attribute\",\"positive_samples_covered(%)\",\"negative_samples_covered(%)\",\"avgPositveScores\",\"maxPositiveScores\",\"minPositiveScores\",\"avgNegativeScores\",\"maxNegativeScores\",\"minNegativeScores\",\"F_MEASURE\",\"DIFFERENCE_INDEX\",\"rule\",\"learning_file\"");
		// Connecting to DB to retrieve rules
        Connection connect = DriverManager.getConnection("jdbc:mysql://localhost:8889/genetic_experiments?user=root&password=root");
        Statement statement = connect.createStatement();
      
	        ResultSet resultSet = statement.executeQuery("SELECT distinct sub_prov.rule, cast((sub_ev.F_e) as  decimal(16,3)) AS F_E, sub_prov.path, sub_prov.learning_file\n" + 
	        		"FROM ( SELECT * FROM evaluation_one WHERE evaluation_one.scenario = \""+scenario+"\") sub_ev \n" + 
	        		"   INNER JOIN (SELECT * FROM rules_provenance_one WHERE rules_provenance_one.scenario = \""+scenario+"\") sub_prov \n" + 
	        		"   ON sub_ev.rule = sub_prov.rule AND  sub_ev.algorithm = sub_prov.algorithm AND sub_ev.setup = sub_prov.setup \n"
	        		+ "LIMIT 1"
	        		//+" WHERE cast((sub_ev.F_e) as  decimal(16,3)) >= "+lowerBound+" AND cast((sub_ev.F_e) as  decimal(16,3)) < "+upperBound+" LIMIT 1"
	        		
	        		+ "");
	        while (resultSet.next()) {
	        		String rule = resultSet.getString("rule");
	        		Double fMeasure = resultSet.getDouble("F_e");
	        		String file =  resultSet.getString("path")+"/"+resultSet.getString("learning_file");
	        		//System.out.println(fMeasure+" "+rule);
	        		//printLearningFileStatistics(file, showStats, showSuspiciusNegativeSamples, rule, fMeasure);	
	        }
	        resultSet.close();
        
       
        statement.close();
        connect.close();
	}
	
	
	
	
	private static void printLearningFileStatistics(String file, Boolean showStats, Boolean showSuspiciusNegativeSamples, String rule, Double fMeasure) {
		positiveSamples = new ArrayList<Tuple<String,String>>();
		negativeSamples = new ArrayList<Tuple<String,String>>();
		readSampleSet(file);
		List<String> metricNames = extractMetricNames(rule);
		Set<Tuple<String,String>> attributesUsed = extractAttributeNames(rule);
		// Apply metrics
		Avg avg = new Avg();
		Max max = new Max();
		Min min = new Min();
		Multimap<Double, String> highestV =  ArrayListMultimap.create();
		if(showStats ) { //TODO: REMOVE THIS TO GO BACK TO NORMAL metricNames.size()<2
			for(Tuple<String,String> attribute: attributesUsed) {
				positiveScores = applyMetrics(positiveSamples, attribute, false, metricNames);
				negativeScores = applyMetrics(negativeSamples, attribute, showSuspiciusNegativeSamples, metricNames); // put this true to find negative examples that are likely positives
				for(String metric: negativeScores.keySet()) {
					List<Double> negativeScoresMetric = negativeScores.get(metric);
					List<Double> positiveScoresMetric = positiveScores.get(metric);
					Double averageNegative = Utils.roundDecimal(avg.applyAggregation(negativeScores.get(metric)),3);
					Double maximNegative = Utils.roundDecimal(max.applyAggregation(negativeScores.get(metric)),3);
					Double minimNegative = Utils.roundDecimal(min.applyAggregation(negativeScores.get(metric)),3);
					Double averagePositive = Utils.roundDecimal(avg.applyAggregation(positiveScores.get(metric)),3);
					Double maximPositive = Utils.roundDecimal(max.applyAggregation(positiveScores.get(metric)),3);
					Double minimPositive = Utils.roundDecimal(min.applyAggregation(positiveScores.get(metric)),3);
					
					//
			        SummaryStatistics statsN = new SummaryStatistics();
					negativeScoresMetric.forEach(score -> statsN.addValue(score) );
					SummaryStatistics statsP = new SummaryStatistics();
					positiveScoresMetric.forEach(score -> statsP.addValue(score) );
					Double cP = calcMeanCI(statsP, 0.95);
					Double cN = calcMeanCI(statsN, 0.95);
					Double index = 0.0;
					//
					transformCounter(fMeasure+"-"+rule.hashCode(), metric,"P",positiveScoresMetric);
					transformCounter(fMeasure+"-"+rule.hashCode(), metric,"N",negativeScoresMetric);
					//String line = ""+fMeasure+";"+(cP-cN);
					//System.out.println(index);
					//String line = "\""+metric+"\",\""+attribute.getFirstElement()+"\",\""+attribute.getSecondElement()+"\",\""+(positiveScores.get(metric).size()/positiveSamples.size())+"\",\""+(negativeScores.get(metric).size()/negativeSamples.size())+"\",\""+averagePositive+"\",\""+maximPositive+"\",\""+minimPositive+"\",\""+averageNegative+"\",\""+maximNegative+"\",\""+minimNegative+"\",\""+fMeasure+"\",\""+index.toString()+"\",\""+rule+"\",\""+file+"\"";
					// PRINT this line var here to have previous version csv's
					//System.out.println("\tF:"+fMeasure+", cP:"+cP+" cN:"+cN+" -> "+(cP-cN)
					//		+ "\n\t\t\t=> "+line);
					//System.out.println("\tP: "+positiveScoresMetric);
					//System.out.println("\tN: "+negativeScoresMetric);
					String line="";						
					highestV.put(index, line);
					//System.out.println();
					
				}
			}
			
			List<Double> indexes = new ArrayList<Double>();
			
			indexes.addAll(highestV.keySet());
			Double maxIndex =  min.applyAggregation(indexes);
			//highestV.get(maxIndex).forEach(value -> System.out.println(value.toString().replace("[", "").replace("]","")));
			
			//System.out.println("-----------");
		}
	}
	
	 private static double calcMeanCI(SummaryStatistics stats, double level) {
	        try {
	            // Create T Distribution with N-1 degrees of freedom
	            TDistribution tDist = new TDistribution(stats.getN() - 1);
	            // Calculate critical value
	            double critVal = tDist.inverseCumulativeProbability(1.0 - (1 - level) / 2);
	            // Calculate confidence interval
	            return critVal * stats.getStandardDeviation() / Math.sqrt(stats.getN());
	        } catch (MathIllegalArgumentException e) {
	            return Double.NaN;
	        }
	    }
	 
	 private static Map<Double, Integer> transformCounter(String rule, String metric, String clazz, List<Double> scores){
		 Map<Double, Integer> scoring = new HashMap<Double,Integer>();
		 for(double i=0.0; i< 1.01; i+=0.01) {
			 Integer counter=  0;
			 i = Utils.roundDecimal(i, 2);
			 for(Double score:scores) {
				 if(score.equals(i))
					 counter ++;
			 }
			 scoring.put(i, counter);
			 System.out.println(rule+";"+metric+";"+clazz+";"+i+";"+counter);
		 }
		 return scoring;
	 }
	
	private static Set<Tuple<String,String>> extractAttributeNames(String rule) {
		Set<Tuple<String,String>> attributeNames = new HashSet<Tuple<String,String>>();
		Pattern pattern = Pattern.compile("\\(http[^\\,]+(\\),trn:TokenizeTransformation\\()?(,)?http[^\\,]+");
	        Matcher matcher = pattern.matcher(rule);
	        while (matcher.find()) {
	            String[] attributes = matcher.group().replace("(", "").split(",");
	            try {
	            String source = attributes[0].replace(")", "").trim();
	            String target = attributes[1].replace(")", "").trim();
	            attributeNames.add(new Tuple<String,String>(source,target));
	            }catch( Exception e) {
	            		e.printStackTrace();
	            }
	        }

		
		return attributeNames;
	}
	
	private static List<String> extractMetricNames(String rule) {
		List<String> metricNames = new ArrayList<String>();
		
		 Pattern pattern = Pattern.compile("str:[^\\(]+");
	        Matcher matcher = pattern.matcher(rule);
	        while (matcher.find()) {
	            metricNames.add(matcher.group());
	        }
		
		
		return metricNames;
	}


	
	
	private static Map<String, List<Double>> applyMetrics(List<Tuple<String, String>> iris,Tuple<String, String> attribute, Boolean flag, List<String> metricNames) {
		Map<String, List<Double>> scores = new HashMap<String, List<Double>>();
		for(IARQStringSimilarity metric: loadStringMetrics(metricNames)  ) {
			List<Double> specificScores = new ArrayList<Double>();
			for (Tuple<String, String> iriTuple : iris) {
				String source = iriTuple.getFirstElement();
				String target = iriTuple.getSecondElement();
				List<RDFNode> sourceValues = datasetSource.listObjectsOfProperty(ResourceFactory.createResource(source), ResourceFactory.createProperty(attribute.getFirstElement())).toList();
				List<RDFNode> targetValues = datasetTarget.listObjectsOfProperty(ResourceFactory.createResource(target),ResourceFactory.createProperty(attribute.getSecondElement())).toList();
				if (!sourceValues.isEmpty() && !targetValues.isEmpty()) {
					String sourceValue = sourceValues.get(0).asLiteral().toString();
					String targetValue = targetValues.get(0).asLiteral().toString();
					if(Utils.roundDecimal(metric.compareStrings(sourceValue, targetValue), 2) > 0.99 && flag)
						System.out.println(source+" "+target);
					specificScores.add(Utils.roundDecimal(metric.compareStrings(sourceValue, targetValue), 2));
					//System.out.println("\t\t**"+sourceValue+" "+targetValue+" "+Utils.roundDecimal(metric.compareStrings(sourceValue, targetValue), 3));
				}
			}
			
			scores.put(metric.getName(), specificScores);
		}
		return scores;

	}
	

	
	
	
	
	
	
	private static void readSampleSet(String file){
		
		try {
			FileInputStream fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			Boolean header = false;
			
			while ((strLine = br.readLine()) != null)   {
				String[] iris = null;
				
				if(strLine.contains("http://www.w3.org/2002/07/owl#sameAs")) {
					iris = strLine.split("<http://www.w3.org/2002/07/owl#sameAs>");
					String source = iris[0].replace("<", "").replace(">", "").trim();
					String target = iris[1].replace("<", "").replaceAll(">\\s*\\.", "").trim();
					positiveSamples.add(new Tuple<String,String>(source,target));
				}else {
					iris = strLine.split("<http://www.w3.org/2002/07/owl#differentFrom>");
					String source = iris[0].replace("<", "").replace(">", "").trim();
					String target = iris[1].replace("<", "").replaceAll(">\\s*\\.", "").trim();
					negativeSamples.add(new Tuple<String,String>(source,target));
				}
			
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static Model getTDBModel(String datasetDirectrory) {
		  // Make a TDB-backed dataset
		  String directory = datasetDirectrory ;
		  Dataset dataset = TDBFactory.createDataset(directory) ;
	
		  dataset.begin(ReadWrite.READ) ;
		  // Get model inside the transaction
		  Model model = dataset.getDefaultModel() ;
		  
		  return model;
	}
	
	private static List<IARQStringSimilarity> loadStringMetrics(List<String> metricsNames) {
		List<IARQStringSimilarity> metrics = new ArrayList<IARQStringSimilarity>();
		List<IARQStringSimilarity> metricsToRemove = new ArrayList<IARQStringSimilarity>();
		metrics.add(new CosineSimilarity());
		metrics.add(new JaccardSimilarity());
		metrics.add(new JaroSimilarity());
		metrics.add(new JaroWinklerSimilarity());
		metrics.add(new JaroWinklerTFIDFSimilarity());
		metrics.add(new LevenshteinSimilarity());
		metrics.add(new OverlapSimilarity());
		metrics.add(new QGramsSimilarity());
		metrics.add(new SoftTFIDFSimilarity());
		metrics.add(new SubstringSimilarity());
		metrics.add(new TrigramsSimilarity());
		
		for(IARQStringSimilarity metric:metrics) {
			if(!metricsNames.contains(metric.getName()))
				metricsToRemove.add(metric);
		}
		metrics.removeAll(metricsToRemove);
		return metrics;
	}
	

}
