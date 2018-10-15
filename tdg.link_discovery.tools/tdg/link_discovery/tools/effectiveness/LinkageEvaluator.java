package tdg.link_discovery.tools.effectiveness;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.jena.ext.com.google.common.collect.Sets;

import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.utils.FrameworkUtils;
import tdg.link_discovery.middleware.utils.Utils;


public class LinkageEvaluator {

	public void obtainCSV(IEnvironment environment, String specification, String algorithm, long executionTime){
		String outputFile = environment.getSpecificationOutput();
		String goldStdFile = environment.getGoldStandardFile();
		String linksFile = environment.getLinksOutput();
		List<String> goldLinks = FrameworkUtils.readGoldLinks(goldStdFile);
		generateCSVResults(environment.getSourceDatasetFile(), environment.getTargetDatasetFile(), algorithm, specification, linksFile, goldLinks, outputFile, executionTime);		
	}
	
	private void generateCSVResults(String scenarioSource, String scenarioTarget, String algorithm, String specification, String resultsFile, List<String> goldLinks, String outputFile, long executionTime){
		String metrics = getMetrics(goldLinks, resultsFile);
		String csvLine = prepareCsvLine(metrics, scenarioSource, scenarioTarget, algorithm, specification, executionTime);
		Utils.appendLineInCSV(outputFile, csvLine);	
	}
	
	private String prepareCsvLine(String metrics, String scenarioSource, String scenarioTarget, String algorithm, String specification, long executionTime) {
		StringBuilder line = new StringBuilder();
		line.append("\"").append(scenarioSource).append("\",\"").append(scenarioTarget).append("\",\"").append(algorithm).append("\",").append(executionTime).append(",\"").append(specification).append("\",").append(metrics).append("\n");
		return line.toString();
		
	}
	
	/*
	 * Old and probably removable
	 */
	
	
	@ Deprecated
	public void obtainCSV(String scenario, String specification, String algorithm, String goldStdFile, String linksFile){
		String outputFile = "./"+algorithm+"-"+scenario+".csv";
		List<String> goldLinks = FrameworkUtils.readGoldLinks(goldStdFile);
		generateCSVResults(scenario, algorithm, linksFile, goldLinks, outputFile);		
	}
	
	private void generateCSVResults(String scenario, String algorithm, String resultsFile, List<String> goldLinks, String outputFile){
		
		String metrics = getMetrics(goldLinks, resultsFile);
		String csvLine = prepareCsvLine(metrics, scenario, algorithm);
		Utils.appendLineInCSV(outputFile, csvLine);
		
		
	}
	
	private String prepareCsvLine(String metrics, String scenario,String algorithm) {
		StringBuilder line = new StringBuilder();
		line.append(scenario).append(",").append(algorithm).append(",").append(metrics).append("\n");
		return line.toString();
		
	}

	
	private static Integer truePositives,  falsePositives, totalLinks;
	private static Set<String> alreadyProcessed, positives;
	public static String getMetrics(List<String> goldLinks, String fileName) {
		
		alreadyProcessed = new CopyOnWriteArraySet<String>();
		positives = new CopyOnWriteArraySet<String>();
		truePositives = 0;
		falsePositives = 0;
		totalLinks = 0;
		System.out.println("Processing: "+fileName);
		try {
			LineIterator it = FileUtils.lineIterator(new File(fileName), "UTF-8");
			//StreamUtils.asStream(it).parallel().forEach(line -> processLine(line, goldLinks));
			while (it.hasNext()) {
				String line = it.nextLine();
				if (!line.isEmpty()){
					String link = line.replaceAll("\\s+", "");
					if(!alreadyProcessed.contains(link)){
						if(goldLinks.contains(link)){
							truePositives++;
							positives.add(link);
						}else{
							//System.out.println("False Positive: "+link);
							falsePositives++;
						}
						totalLinks++;	
						alreadyProcessed.add(link);
					}
				}	
			}
			LineIterator.closeQuietly(it);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ConfusionMatrix cf = new ConfusionMatrix();
		cf.setTruePositives(truePositives);
		cf.setFalsePositives(falsePositives);
		cf.setFalseNegatives((goldLinks.size()-truePositives));
		
		System.out.println(Sets.difference(Sets.newHashSet(goldLinks), positives));
		
		StringBuilder result = new StringBuilder();
		result.append("\"").append(truePositives).append("\",\"").append(falsePositives).append("\",\"").append((goldLinks.size()-truePositives));
		result.append("\",\"").append(Utils.roundDecimal(cf.getPrecision(), FrameworkConfiguration.DECIMAL_PRECISION)).append("\",\"");
		result.append(Utils.roundDecimal(cf.getRecall(), FrameworkConfiguration.DECIMAL_PRECISION)).append("\",\"");
		result.append(Utils.roundDecimal(cf.getFMeasure(), FrameworkConfiguration.DECIMAL_PRECISION)).append("\""); // Tp, Fp, Fn
		
		return result.toString();
	}
	
	private static void processLine(String line, List<String> goldLinks){
		if (!line.isEmpty()){
			String link = line.replaceAll("\\s+", "");
			if(!alreadyProcessed.contains(link)){
				if(goldLinks.stream().anyMatch(goldLink -> goldLink.equals(link))){
					truePositives++;
					positives.add(link);
				}else{
					falsePositives++;
				}
				totalLinks++;	
				alreadyProcessed.add(link);
			}
	}
	}
	
	

}
