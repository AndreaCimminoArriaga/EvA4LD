package tdg.moea.test.advanced;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tdg.link_discovery.connector.sparql.engine.evaluator.ExaustiveEvaluator;
import tdg.link_discovery.connector.sparql.engine.translator.SparqlTranslator;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.engine.evaluator.IEvaluator;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.framework.environment.Environments;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.objects.Tuple;

public class LinkerFromFile {


	
	public static void main(String[] args) {
		String file = args[0].trim();
		IEnvironment environment = null;
		IEvaluator evaluator = null;
		SparqlTranslator translator = new SparqlTranslator();
	
		try {
			FileInputStream fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			Boolean header = false;
			
			while ((strLine = br.readLine()) != null)   {
				if(header && !strLine.isEmpty()) {
					
					// load csv relevant elements:
					String[] elements = strLine.replaceFirst("\"", "").replaceAll("\"\\s*$", "").split("\",\"");
					
					String environmentName = elements[1]+".cnf";
					String rule = retrieveRule(elements[5]);
					
					String ruleHash = String.valueOf(rule.toString().hashCode())+""+SPARQLFactory.generateFreshVar().replace("?", "");
					
					// init objects to link only once per environment
					if(environment==null) {
						environment  = Environments.parseFromFile("experiments/environments/"+environmentName);
						evaluator = new ExaustiveEvaluator(environment);
					}
					if(environment!=null && !environment.getName().equals(elements[1].trim())) {
						environment  = Environments.parseFromFile("experiments/environments/"+environmentName);
						evaluator = new ExaustiveEvaluator(environment);
					}
					
					
					
					// Execute rule
					Tuple<String,String> linkRule = translator.translate(rule, environment);				
					try {	
						long startTime = System.nanoTime();
						evaluator.getEnvironment().setLinksOutput("./links/"+elements[1]+"_"+ruleHash+".nt");
						ConfusionMatrix matrix = evaluator.evaluate(linkRule);

						long stopTime = System.nanoTime();
					    long elapsedTime = (stopTime - startTime)/1000000;
					    // save resuls
					    storeCsvLine(strLine, rule, matrix, String.valueOf(elapsedTime), file);
					   
					}catch(Exception e) {
						e.printStackTrace();
						System.out.println("Repeat execution for rule: "+rule);
					}
					
					
		
				}else {
			
					printHeader(strLine, file);
				}
				header = true;
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	

	private static String retrieveRule(String rawRule) {
		String rule = rawRule.trim();
		Pattern p = Pattern.compile("http:[^,\\)]+");
		Matcher matcher = p.matcher(rule);
		int start = 0;
		Boolean even = true;
		int pointer = 0;
		StringBuffer finalRule = new StringBuffer();
		String lastToAppend = "";
		while (matcher.find(start)) {
			String value = matcher.group();
			String newValue = "";
			if(even) {
				newValue = FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER+value+FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER;
				even = false;
			}else {
				newValue =  FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER+value+FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER;
				even = true;
			}
			rule = rule.substring(pointer);
			pointer = 0;
			
			int breakpoint = rule.indexOf(value);
			lastToAppend = rule.substring(breakpoint+value.length(), rule.length());
			finalRule.append(rule.substring(pointer, breakpoint)).append(newValue);
			pointer = breakpoint+value.length();
			
			start = matcher.start() + 1;
		}
		finalRule.append(lastToAppend);
		return finalRule.toString();
	}
	
	private static void storeCsvLine(String originalLine, String rule, ConfusionMatrix matrix, String elapseTime, String file) {
		String outputFile = file.replace(".csv", "_processed.csv");
		StringBuffer line = new StringBuffer(originalLine);
		line.append(",");
		line.append("\"").append(rule).append("\",");
		line.append("\"").append(elapseTime).append("\",");
		line.append("\"").append(matrix.getTruePositives()).append("\",");
		line.append("\"").append(matrix.getTrueNegatives()).append("\",");
		line.append("\"").append(matrix.getFalsePositives()).append("\",");
		line.append("\"").append(matrix.getFalseNegatives()).append("\",");
		line.append("\"").append(matrix.getPrecision()).append("\",");
		line.append("\"").append(matrix.getRecall()).append("\",");
		line.append("\"").append(matrix.getFMeasure()).append("\"");
		line.append("\n");
		// write
		try {
			BufferedWriter outcome = new BufferedWriter(new FileWriter(outputFile, true));
			outcome.append(line.toString());
			outcome.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void printHeader(String strLine, String file) {
		// prepare header
		StringBuffer header =new StringBuffer(strLine);
		header.append(",");
		header.append("\"").append("rule_applied").append("\",");
		header.append("\"").append("time").append("\",");
		header.append("\"").append("tp").append("\",");
		header.append("\"").append("tn").append("\",");
		header.append("\"").append("fp").append("\",");
		header.append("\"").append("fn").append("\",");
		header.append("\"").append("P").append("\",");
		header.append("\"").append("R").append("\",");
		header.append("\"").append("F").append("\"");
		header.append("\n");
		// write
		String outputFile = file.replace(".csv", "_processed.csv");
		try {
			BufferedWriter outcome = new BufferedWriter(new FileWriter(outputFile, true));
			outcome.append(header.toString());
			outcome.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
}
