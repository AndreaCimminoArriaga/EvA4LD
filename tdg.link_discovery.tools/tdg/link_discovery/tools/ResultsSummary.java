package tdg.link_discovery.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.jena.ext.com.google.common.collect.Maps;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.utils.StreamUtils;
import tdg.link_discovery.middleware.utils.Utils;

public class ResultsSummary {

	private static List<String> files;
	private static List<String> csvLines;
	private static StringBuffer timingLines;
	public static void main(String[] args) throws IOException {
	
		FrameworkConfiguration.DECIMAL_PRECISION = 3;
		files = Lists.newArrayList();
		csvLines = Lists.newArrayList();
		timingLines = new StringBuffer();
		csvLines.add(csvHeather());
		recursiveNavigation(new File("/Users/andrea/Desktop/roundX/"));
		files.forEach(file -> retrieveSummary(file));
		
		//csvLines.forEach(line -> System.out.print(line+","));
		
		Files.write(timingLines.toString().getBytes(),new File("/Users/andrea/Desktop/iterations.csv"));
	}
	
	private static String csvHeather() {
		StringBuffer buff = new StringBuffer();
		buff.append("\"").append("kFolding").append("\",");
		buff.append("\"").append("Scenario").append("\",");
		buff.append("\"").append("Execution name").append("\",");
		buff.append("\"").append("Mean Training Time(min)").append("\",");
		buff.append("\"").append("Variance Training Time(min)").append("\",");
		buff.append("\"").append("Std var Training Time(min) ").append("\",");
		
		buff.append("\"").append("Mean Training Fitness").append("\",");
		buff.append("\"").append("Variance Training Fitness").append("\",");
		buff.append("\"").append("Std var Training Fitness").append("\",");
		
		buff.append("\"").append("Mean Validation Time(min)").append("\",");
		buff.append("\"").append("Variance Validation Time(min)").append("\",");
		buff.append("\"").append("Std var Validation Time(min)").append("\",");
		
		buff.append("\"").append("Mean Validation Precision").append("\",");
		buff.append("\"").append("Variance Validation Precision").append("\",");
		buff.append("\"").append("Std var Validation Precision").append("\",");
		
		buff.append("\"").append("Mean Validation Recall").append("\",");
		buff.append("\"").append("Variance Validation Recall").append("\",");
		buff.append("\"").append("Std var Validation Recall").append("\",");
		
		buff.append("\"").append("Mean Validation FMeasure").append("\",");
		buff.append("\"").append("Variance Validation FMeasure").append("\",");
		buff.append("\"").append("Std var Validation FMeasure").append("\",");
		
		return buff.toString();
	}
	
	
	private static void retrieveSummary(String file) {
		System.out.println(file);
		if(file.contains("resultsLog")) {
			String[] folders = file.substring(file.indexOf("results")+7, file.lastIndexOf("/")).split("/");
			String stats = retrieveLinesofInterest(file);
			StringBuffer accumulator = new StringBuffer();
			for(String folder:folders) {
				if(!folder.isEmpty() && !folder.equals("results"))
					accumulator.append("\"").append(folder).append("\",");
			}
			accumulator = new StringBuffer(accumulator.replace(accumulator.length()-1, accumulator.length(), ""));
			accumulator.append(",").append(stats);

			csvLines.add(accumulator.toString());
			
		}
	}

	private static String retrieveLinesofInterest(String file) {
		StringBuffer stats = new StringBuffer();
		List<Double> valTimeMinAvg =Lists.newArrayList();
		List<Double> trnTimeMinAvg =Lists.newArrayList();

		List<Double> precisionAvg=Lists.newArrayList();
		List<Double> recallAvg=Lists.newArrayList();
		List<Double> fmeasureAvg=Lists.newArrayList();
		List<Double> fitnessAvg=Lists.newArrayList();
		StringBuffer tmpLine = new StringBuffer();
		Boolean unfinished = true;
		int counter = 1;
		tmpLine.append("\"").append(file.replace("/", "\",\"")).append("\",").append("\"genetic-execution-").append(counter).append("\",");
		Boolean goodPreviousLine = false;
		System.out.println(file);
		try {
			FileInputStream fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				if(file.contains("4k")) {
					System.out.println(strLine.contains(" elapseTimeEstimated(ms)")+" "+strLine);
				}
				// Capturing validation parameters
				if(strLine.contains("Validation executed in")) {
					String strTime = strLine.substring(strLine.indexOf("Validation executed in"), strLine.length()).replaceAll("[a-zA-Z]+", "").trim();
					Double time = (Integer.valueOf(strTime)/1000)/60.0;
					valTimeMinAvg.add(time);
				}
				if(strLine.contains("	 avrg(P): ")) {
					String strP = strLine.substring(strLine.indexOf("avrg(P):")+8, strLine.indexOf(",")).trim();
					Double p = Double.valueOf(strP);
					precisionAvg.add(p);
				}
				if(strLine.contains("	 avrg(R): ")) {
					String strP = strLine.substring(strLine.indexOf("avrg(R):")+8, strLine.indexOf(",")).trim();
					Double p = Double.valueOf(strP);
					recallAvg.add(p);
				}
				if(strLine.contains("	 avrg(F): ")) {
					String strP = strLine.substring(strLine.indexOf("avrg(F):")+8, strLine.indexOf(",")).trim();
					Double p = Double.valueOf(strP);
					fmeasureAvg.add(p);
				}
				if(strLine.contains(" elapseTimeEstimated(ms)")) {
					String time = strLine.substring(strLine.indexOf("in")+2, strLine.indexOf("ms")).trim();
					tmpLine.append("\"").append(time).append("\",");
					unfinished = true;
				}
				if(strLine.contains("Learning (training) execution time")) {
					String strTime =strLine.substring(strLine.indexOf("(ms):")+5, strLine.length());
					Double time = (Integer.valueOf(strTime.trim())/1000)/60.0;
					trnTimeMinAvg.add(time);
					unfinished = false;
					timingLines.append(tmpLine.substring(0, tmpLine.length()-1).toString().replaceAll("^\"\",", "")).append("\n");
					// new line starts
					counter++;
					tmpLine = new StringBuffer();
					tmpLine.append("\"").append(file.replace("/", "\",\"")).append("\",").append("\"genetic-execution-").append(counter).append("\",");
				}
				if(strLine.contains("Fitness population: ")) {
					goodPreviousLine = true;
				}
				if(strLine.contains("GenericMOEAGeneticAlgorithm - 	 avrg(Fitness):") && goodPreviousLine) {
					String strFit = strLine.substring(strLine.indexOf("avrg(Fitness):")+14, strLine.indexOf(",")).trim();
					Double fitness = Double.valueOf(strFit);
					fitnessAvg.add(fitness);
					goodPreviousLine = false;
				}
				
				
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		if(unfinished)
			timingLines.append(tmpLine.substring(0, tmpLine.length()-1).toString().replaceAll("^\"\",", "")).append(",\"Unifished algorithm\"").append("\n");
		stats = obtainStatsSummary(valTimeMinAvg, trnTimeMinAvg, precisionAvg, recallAvg, fmeasureAvg, fitnessAvg);
		return stats.toString();
	}


	private static StringBuffer obtainStatsSummary(List<Double> valTimeMinAvg, List<Double> trnTimeMinAvg, List<Double> precisionAvg, List<Double> recallAvg, List<Double> fmeasureAvg, List<Double> fitnessAvg) {
		StringBuffer summary = new StringBuffer();
		summary.append(getSummary(trnTimeMinAvg)).append(",");
		summary.append(getSummary(fitnessAvg)).append(",");
		summary.append(getSummary(valTimeMinAvg)).append(",");
		summary.append(getSummary(precisionAvg)).append(",");
		summary.append(getSummary(recallAvg)).append(",");
		summary.append(getSummary(fmeasureAvg));
		return summary;
	}

	private static String getSummary(List<Double> list) {
		Double listMean = Utils.getMean(list);
		Double listVar = Utils.getVariance(list);
		Double listStd = Utils.getStdDev(list);
		StringBuffer summary = new StringBuffer();
		summary.append("\"").append(listMean).append("\",\"").append(listVar).append("\",\"").append(listStd).append("\"");
		return summary.toString();
	}



	private static void recursiveNavigation(File newFile) {
		String file = newFile.getAbsolutePath();
		if(newFile.isDirectory()) {
			
			if(file.contains("/")) {
				
				StreamUtils.asStream(newFile.listFiles()).forEach(subFile -> recursiveNavigation(subFile));
			}else {
				StreamUtils.asStream(newFile.listFiles()).forEach(subFile -> recursiveNavigation(subFile));
			}
		}
		
		if(file.endsWith(".txt")) {
			files.add(file);
		}
	}
}
