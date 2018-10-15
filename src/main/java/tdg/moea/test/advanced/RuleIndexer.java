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
import com.mysql.jdbc.PreparedStatement;

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

public class RuleIndexer {

	private static List<Tuple<String,String>> positiveSamples;
	private static List<Tuple<String,String>> negativeSamples;
	private static Map<String,List<Double>> positiveScores, negativeScores;
	private static Model datasetSource, datasetTarget;
	
	public static void main(String[] args) throws Exception {
		
	
		// working with specific learning file
		System.out.println("Generating new index...");
		// Connecting to DB to retrieve rules
        Connection connect = DriverManager.getConnection("jdbc:mysql://localhost:8889/genetic_experiments?user=root&password=root");
        Statement statement = connect.createStatement();
      
	        ResultSet resultSet = statement.executeQuery("SELECT DISTINCT evaluation_one.F_e, evaluation_one.rule FROM evaluation_one  ORDER BY evaluation_one.F_e DESC\n" + 
	        		"\n" + 
	        		"");
	        while (resultSet.next()) {
	        		String rule = resultSet.getString("rule");
	        		Double fMeasure = resultSet.getDouble("F_e");
	        		List<String> metricNames = extractMetricNames(rule);
	        		List<String> aggregateNames = extractAggregationsNames(rule);
	        		Integer index = ((aggregateNames.size()*aggregateNames.size())+metricNames.size());
//	        		System.out.println(rule);
//	        		System.out.println("\tF:"+fMeasure);
//	        		System.out.println("\tAgg:"+aggregateNames.size());
//	        		System.out.println("\tStr:"+metricNames.size());
//	        		System.out.println("\t\tIndex: "+index);
	        		updateRow(connect, rule, index);
	        		
	        }
	        resultSet.close();
        
       
        statement.close();
        connect.close();
	}
		
	private static void updateRow(Connection connect, String rule, Integer index) {
		try{
			// create the java mysql update preparedstatement
		    String query = "update evaluation_one set score1 = ? where rule = ?";
		    java.sql.PreparedStatement preparedStmt = connect.prepareStatement(query);
		   
		    preparedStmt.setLong(1, index);
		    preparedStmt.setString(2, rule);
		    
		    // execute the java preparedstatement
		    preparedStmt.executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	private static List<String> extractAggregationsNames(String rule) {
		List<String> attributeNames = new ArrayList<String>();
		Pattern pattern = Pattern.compile("agg:[^\\(]+");
	        Matcher matcher = pattern.matcher(rule);
	        while (matcher.find()) {
	    
	        	attributeNames.add(matcher.group());
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


	
	
	
	
	private static Model getTDBModel(String datasetDirectrory) {
		  // Make a TDB-backed dataset
		  String directory = datasetDirectrory ;
		  Dataset dataset = TDBFactory.createDataset(directory) ;
	
		  dataset.begin(ReadWrite.READ) ;
		  // Get model inside the transaction
		  Model model = dataset.getDefaultModel() ;
		  
		  return model;
	}
	
	

}
