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

public class SampleSetAnalyzerWEKA {

	private static List<Tuple<String,String>> positiveSamples;
	private static List<Tuple<String,String>> negativeSamples;
	private static Map<String,List<Double>> positiveScores, negativeScores;
	private static Model datasetSource, datasetTarget;
	
	public static void main(String[] args) throws Exception {
		String scenario = args[0]; // "retaurants"
		String sourceDatasetDir = args[1];//"/Users/cimmino/Desktop/tdb-data/restaurants1";
		String targetDatasetDir = args[2];//"/Users/cimmino/Desktop/tdb-data/restaurants2";// 
		Boolean showStats = true;
		Boolean showSuspiciusNegativeSamples = false;

		datasetSource = getTDBModel(sourceDatasetDir);
		datasetTarget = getTDBModel(targetDatasetDir);
		
		// working with specific learning file
		System.out.println("\"rule\",\"p0\",\"n0\",\"p1\",\"n1\",\"p2\",\"n2\",\"p3\",\"n3\",\"p4\",\"n4\",\"p5\",\"n5\",\"p6\",\"n6\",\"p7\",\"n7\",\"p8\",\"n8\",\"p9\",\"n9\",\"p10\",\"n10\",\"CosineSimilarity\",\"JaccardSimilarity\",\"JaroSimilarity\",\"JaroWinklerSimilarity\",\"JaroWinklerTFIDFSimilarity\",\"LevenshteinSimilarity\",\"OverlapSimilarity\",\"QGramsSimilarity\",\"SoftTFIDFSimilarity\",\"SubstringSimilarity\",\"TrigramsSimilarit\",\"F1\",\"CombinedMetrics\"");
		// Connecting to DB to retrieve rules
        Connection connect = DriverManager.getConnection("jdbc:mysql://localhost:8889/genetic_experiments?user=root&password=root");
        Statement statement = connect.createStatement();
      
	        ResultSet resultSet = statement.executeQuery("SELECT distinct sub_prov.rule, cast((sub_ev.F_e) as  decimal(16,3)) AS F_E, sub_prov.path, sub_prov.learning_file\n" + 
	        		"FROM ( SELECT * FROM evaluation_one WHERE evaluation_one.scenario = \""+scenario+"\") sub_ev \n" + 
	        		"   INNER JOIN (SELECT * FROM rules_provenance_one WHERE rules_provenance_one.scenario = \""+scenario+"\") sub_prov \n" + 
	        		"   ON sub_ev.rule = sub_prov.rule AND  sub_ev.algorithm = sub_prov.algorithm AND sub_ev.setup = sub_prov.setup \n"
	        		+ "");
	        while (resultSet.next()) {
	        		String rule = resultSet.getString("rule");
	        		Double fMeasure = resultSet.getDouble("F_e");
	        		String file =  resultSet.getString("path")+"/"+resultSet.getString("learning_file");
	        		
	        		printLearningFileStatistics(file, showStats, showSuspiciusNegativeSamples, rule, fMeasure);	
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

		if(showStats ) { //TODO: REMOVE THIS TO GO BACK TO NORMAL metricNames.size()<2
			for(Tuple<String,String> attribute: attributesUsed) {
				positiveScores = applyMetrics(positiveSamples, attribute, false, metricNames);
				negativeScores = applyMetrics(negativeSamples, attribute, showSuspiciusNegativeSamples, metricNames); // put this true to find negative examples that are likely positives
				for(String metric: negativeScores.keySet()) {
					List<Double> negativeScoresMetric = negativeScores.get(metric);
					List<Double> positiveScoresMetric = positiveScores.get(metric);
					
					Map<Double, Integer> positiveScoreCounter = transformCounter(fMeasure+"-"+rule.hashCode(), metric,"P", positiveScoresMetric);
					Map<Double, Integer> negativeScoreCounter = transformCounter(fMeasure+"-"+rule.hashCode(), metric,"N", negativeScoresMetric);	

					StringBuffer line = new StringBuffer();
					line.append("\"").append(rule).append("\",");
					line.append("\"").append(positiveScoreCounter.get(0.0)).append("\",");
					line.append("\"").append(negativeScoreCounter.get(0.0)).append("\",");
					line.append("\"").append(positiveScoreCounter.get(0.1)).append("\",");
					line.append("\"").append(negativeScoreCounter.get(0.1)).append("\",");
					line.append("\"").append(positiveScoreCounter.get(0.2)).append("\",");
					line.append("\"").append(negativeScoreCounter.get(0.2)).append("\",");
					line.append("\"").append(positiveScoreCounter.get(0.3)).append("\",");
					line.append("\"").append(negativeScoreCounter.get(0.3)).append("\",");
					line.append("\"").append(positiveScoreCounter.get(0.4)).append("\",");
					line.append("\"").append(negativeScoreCounter.get(0.4)).append("\",");
					line.append("\"").append(positiveScoreCounter.get(0.5)).append("\",");
					line.append("\"").append(negativeScoreCounter.get(0.5)).append("\",");
					line.append("\"").append(positiveScoreCounter.get(0.6)).append("\",");
					line.append("\"").append(negativeScoreCounter.get(0.6)).append("\",");
					line.append("\"").append(positiveScoreCounter.get(0.7)).append("\",");
					line.append("\"").append(negativeScoreCounter.get(0.7)).append("\",");
					line.append("\"").append(positiveScoreCounter.get(0.8)).append("\",");
					line.append("\"").append(negativeScoreCounter.get(0.8)).append("\",");
					line.append("\"").append(positiveScoreCounter.get(0.9)).append("\",");
					line.append("\"").append(negativeScoreCounter.get(0.9)).append("\",");
					line.append("\"").append(positiveScoreCounter.get(1.0)).append("\",");
					line.append("\"").append(negativeScoreCounter.get(1.0)).append("\",");
					if(metric.contains("CosineSimilarity")){
						line.append("\"1\",");
					}else{
						line.append("\"0\",");
					}

					if(metric.contains("JaccardSimilarity")){
						line.append("\"1\",");
					}else{
						line.append("\"0\",");
					}

					if(metric.contains("JaroSimilarity")){
						line.append("\"1\",");
					}else{
						line.append("\"0\",");
					}

					if(metric.contains("JaroWinklerSimilarity")){
						line.append("\"1\",");
					}else{
						line.append("\"0\",");
					}

					if(metric.contains("JaroWinklerTFIDFSimilarity")){
						line.append("\"1\",");
					}else{
						line.append("\"0\",");
					}

					if(metric.contains("LevenshteinSimilarity")){
						line.append("\"1\",");
					}else{
						line.append("\"0\",");
					}

					if(metric.contains("OverlapSimilarity")){
						line.append("\"1\",");
					}else{
						line.append("\"0\",");
					}

					if(metric.contains("QGramsSimilarity")){
						line.append("\"1\",");
					}else{
						line.append("\"0\",");
					}

					if(metric.contains("SoftTFIDFSimilarity")){
						line.append("\"1\",");
					}else{
						line.append("\"0\",");
					}

					if(metric.contains("SubstringSimilarity")){
						line.append("\"1\",");
					}else{
						line.append("\"0\",");
					}

					if(metric.contains("TrigramsSimilarit")){
						line.append("\"1\",");
					}else{
						line.append("\"0\",");
					}

					line.append("\"").append(fMeasure).append("\",");
					line.append("\"").append(positiveScores.keySet().size()-1).append("\"");
					System.out.println(line);
					
					
				}
			}
		}
	}
	
 
	 private static Map<Double, Integer> transformCounter(String rule, String metric, String clazz, List<Double> scores){
		 Map<Double, Integer> scoring = new HashMap<Double,Integer>();
	
		 for(double i=0.0; i<= 1.08; i+=0.1) {
			 Integer counter=  0;
			 i = Utils.roundDecimal(i, 2);
			 for(Double score:scores) {
				 if(score>=i && score < (i+0.1))
					 counter ++;
			 }
			 scoring.put(i, counter);
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
