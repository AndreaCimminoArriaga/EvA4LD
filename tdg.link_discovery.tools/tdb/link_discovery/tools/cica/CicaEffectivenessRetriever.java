package tdb.link_discovery.tools.cica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import tdg.link_discovery.middleware.objects.comparators.DoubleNaturalComparator;
import tdg.link_discovery.middleware.utils.StreamUtils;
import tdg.link_discovery.middleware.utils.Utils;

public class CicaEffectivenessRetriever {

	private static List<String> files;
	private static List<String> csvLines;
	private static List<String> iterations;
	
	private static Boolean variance = false;
	private static Boolean std = true;
	

	public static void main(String[] args) throws IOException {
		String folder = "/Users/cimmino/Desktop/carvalho_1";
		// Retrieve list of logs
		files = Lists.newArrayList();
		csvLines = Lists.newArrayList();
		csvLines.add(csvHeather());
		System.out.println(csvHeather());
		recursiveNavigation(new File(folder));
		for(String file:files) {
			retrieveSummary(file);
			
		}
		
		//csvLines.stream().forEach(line -> System.out.println(line));
		//FileUtils.writeLines(new File("./results.csv"), csvLines);
	}

	
	private static void retrieveSummary(String file) {
			String algorithm = extractAlgorithm(file);
			String[] folders = null;
			
			if(file.contains("/"))
				folders = file.substring(file.indexOf("results")+7, file.lastIndexOf("/")).split("/");
			if(file.contains("\\"))
				folders =  file.substring(file.indexOf("results")+7, file.lastIndexOf("\\")).split("\\");
			
			String stats = retrieveLinesofInterest(file);
			StringBuffer accumulator = new StringBuffer(algorithm+",");
			for(String folder:folders) {
				if(!folder.isEmpty() && !folder.equals("results") && !folder.equals("experiments") && !folder.contains("fold_datasets")  && !accumulator.toString().contains(folder) &&  !isNotAPartialAlgorithm(folder))
					accumulator.append("\"").append(folder).append("\",");
			}
			
			accumulator = new StringBuffer(accumulator.replace(accumulator.length()-1, accumulator.length(), ""));
			accumulator.append(",").append(stats);
			System.out.println(accumulator.toString());
			csvLines.add(accumulator.toString());
			
		
	}

	private static Boolean isNotAPartialAlgorithm(String file) {
		if(file.startsWith("genlink")) {
			return true;
		}
		if(file.startsWith("carvalho")) {
			return true;
		}
		if(file.startsWith("carvalho_ext")) {
			return true;
		}
		if(file.startsWith("eagle")) {
			return true;
		}
		if(file.startsWith("gen1")) {
			return true;
		}
		if(file.startsWith("gen2")) {
			return true;
		}
		if(file.startsWith("gen3")) {
			return true;
		}
		return false;
	}

	
	private static String extractAlgorithm(String file) {
		if(file.contains("genlink")) {
			return "\"genlink\"";
		}
		if(file.contains("carvalho")) {
			return "\"carvalho\"";
		}
		if(file.contains("carvalho_ext")) {
			return "\"carvalho_ext\"";
		}
		if(file.contains("eagle")) {
			return "\"eagle\"";
		}
		if(file.contains("gen1")) {
			return "\"gen1\"";
		}
		if(file.contains("gen2")) {
			return "\"gen2\"";
		}
		if(file.contains("gen3")) {
			return "\"gen3\"";
		}
		return null;
	}

	
	private static String csvHeather() {
		StringBuffer buff = new StringBuffer();
		buff.append("\"").append("algorithm").append("\",");
		buff.append("\"").append("scenario").append("\",");
		buff.append("\"").append("setup").append("\",");
		buff.append("\"").append("Avg Training Time(s)").append("\",");
		if(variance)
			buff.append("\"").append("Variance Training Time(s)").append("\",");
		if(std)
			buff.append("\"").append("Std var Training Time(s) ").append("\",");
		
		buff.append("\"").append("Avg Iterations").append("\",");
		if(variance)
			buff.append("\"").append("Variance Iterations").append("\",");
		if(std)
			buff.append("\"").append("Std var Iterations").append("\",");
		
		buff.append("\"").append("Avg Training Fitness").append("\",");
		if(variance)
			buff.append("\"").append("Variance Training Fitness").append("\",");
		if(std)
			buff.append("\"").append("Std var Training Fitness").append("\",");
		
		buff.append("\"").append("Avg Training BestFitness").append("\",");
		if(variance)	
			buff.append("\"").append("Variance Training BestFitness").append("\",");
		if(std)
			buff.append("\"").append("Std var Training BestFitness").append("\",");
		
		buff.append("\"").append("Avg Validation Time(s)").append("\",");
		if(variance)
			buff.append("\"").append("Variance Validation Time(s)").append("\",");
		if(std)
			buff.append("\"").append("Std var Validation Time(s)").append("\",");
		
		buff.append("\"").append("Avg Validation Precision").append("\",");
		if(variance)
			buff.append("\"").append("Variance Validation Precision").append("\",");
		if(std)
			buff.append("\"").append("Std var Validation Precision").append("\",");
		
		buff.append("\"").append("Avg Validation Recall").append("\",");
		if(variance)
			buff.append("\"").append("Variance Validation Recall").append("\",");
		if(std)
			buff.append("\"").append("Std var Validation Recall").append("\",");
		
		buff.append("\"").append("Avg Validation FMeasure").append("\",");
		if(variance)
			buff.append("\"").append("Variance Validation FMeasure").append("\",");
		if(std)
			buff.append("\"").append("Std var Validation FMeasure").append("\",");
		
		buff.append("\"").append("Avg Validation BestFMeasure").append("\",");
		if(variance)
			buff.append("\"").append("Variance Validation BestFMeasure").append("\",");
		if(std)
			buff.append("\"").append("Std var Validation BestFMeasure").append("\",");
		
		return buff.toString();
	}

	private static String retrieveLinesofInterest(String file) {
		
		StringBuffer stats = new StringBuffer();
		List<Double> valTimeMinAvg =Lists.newArrayList();
		List<Double> trnTimeMinAvg =Lists.newArrayList();
		List<Double> bestFitness = Lists.newArrayList();
		List<Double> bestFMeasureVal = Lists.newArrayList();
		List<Double> precisionAvg=Lists.newArrayList();
		List<Double> recallAvg=Lists.newArrayList();
		List<Double> fmeasureAvg=Lists.newArrayList();
		List<Double> fitnessAvg=Lists.newArrayList();
		List<Double> iterations=Lists.newArrayList();
		StringBuffer tmpLine = new StringBuffer();
		Integer max_iterations = 0;
		Boolean unfinished = true;
		int counter = 1;
		tmpLine.append("\"").append(file.replace("/", "\",\"")).append("\",").append("\"genetic-execution-").append(counter).append("\",");
		Boolean goodPreviousLine = false;
		try {
			FileInputStream fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				
				if(file.contains("4k")) {
					System.out.println(strLine.contains(" elapseTimeEstimated(ms)")+" "+strLine);
				}
				if(strLine.contains("clazz:Main - 	 max_iterations: ")) {
					String max_it_string = strLine.substring(strLine.indexOf("max_iterations: ")+16, strLine.length()).trim();
					max_iterations = Integer.valueOf(max_it_string);
				}
				// Capturing validation parameters
				if(strLine.contains("Validation executed in")) {
					String strTime = strLine.substring(strLine.indexOf("Validation executed in"), strLine.length()).replaceAll("[a-zA-Z]+", "").trim();
					Double time = (Integer.valueOf(strTime)/1000.0);
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
					String[] bestFmeasureScore  = strLine.substring(strLine.indexOf("[")+1, strLine.indexOf("]")).trim().split(",");
					Double maxScore = 	StreamUtils.asStream(bestFmeasureScore).map(strNum -> Double.valueOf(strNum.trim())).max(new DoubleNaturalComparator()).get();		
					bestFMeasureVal.add(maxScore);
				}
				if(strLine.contains(" elapseTimeEstimated(ms)")) {
					String time = strLine.substring(strLine.indexOf("in")+2, strLine.indexOf("ms")).trim();
					tmpLine.append("\"").append(time).append("\",");
					unfinished = true;
				}
				if(strLine.contains("Learning (training) execution time")) {
					String strTime =strLine.substring(strLine.indexOf("(ms):")+5, strLine.length());
					Double time = (Integer.valueOf(strTime.trim())/1000.0);
					trnTimeMinAvg.add(time);
					unfinished = false;
					//timingLines.append(tmpLine.substring(0, tmpLine.length()-1).toString().replaceAll("^\"\",", "")).append("\n");
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
					fitnessAvg.add(1-fitness);
					String[] bestFitnessScore  = strLine.substring(strLine.indexOf("[")+1, strLine.indexOf("]")).trim().split(",");
					// Best fitness possible is 0, hence, we retrieve the minimum
					Double minScore = 	StreamUtils.asStream(bestFitnessScore).map(strNum -> Double.valueOf(strNum.trim())).min(new DoubleNaturalComparator()).get();		
					bestFitness.add(1-minScore);
					goodPreviousLine = false;
				}
				if(strLine.contains("iteration") && (strLine.contains("STOP") || strLine.contains("elapseTimeEstimated(ms): 0.0"))) {
					String iteration = strLine.replaceAll("iteration:?", "#");
					if(iteration.contains("STOP")) {
						iteration = iteration.substring(iteration.indexOf("#")+1, iteration.lastIndexOf("-")).trim();
					}else if(iteration.contains("executed")) {
						iteration = iteration.substring(iteration.indexOf("#")+1, iteration.indexOf("executed")).trim();
					}
					
					iterations.add(Double.valueOf(iteration)/max_iterations); // TODO: HERE
				}
				
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		//if(unfinished)
			//timingLines.append(tmpLine.substring(0, tmpLine.length()-1).toString().replaceAll("^\"\",", "")).append(",\"Unifished algorithm\"").append("\n");
		
		stats = obtainStatsSummary(valTimeMinAvg, trnTimeMinAvg, precisionAvg, recallAvg, fmeasureAvg, fitnessAvg, bestFitness, bestFMeasureVal, iterations, max_iterations);
		String statsString = stats.toString();
		return statsString;
	}


	private static StringBuffer obtainStatsSummary(List<Double> valTimeMinAvg, List<Double> trnTimeMinAvg, List<Double> precisionAvg, List<Double> recallAvg, List<Double> fmeasureAvg, List<Double> fitnessAvg, List<Double> bestFitness, List<Double> bestFMeasure, List<Double> iterations, Integer maxIterations) {
		StringBuffer summary = new StringBuffer();
		summary.append(getSummary(trnTimeMinAvg)).append(",");
		summary.append(getSummary(iterations)).append(",");
		summary.append(getSummary(fitnessAvg)).append(",");
		summary.append(getSummary(bestFitness)).append(",");
		
		summary.append(getSummary(valTimeMinAvg)).append(",");
		summary.append(getSummary(precisionAvg)).append(",");
		summary.append(getSummary(recallAvg)).append(",");
		summary.append(getSummary(fmeasureAvg)).append(",");
		summary.append(getSummary(bestFMeasure));
		
		return summary;
	}

	private static String getSummary(List<Double> list) {
		Double listMean =Utils.roundDecimal( Utils.getMean(list) , 3);
		Double listVar = Utils.roundDecimal( Utils.getVariance(list), 3);
		Double listStd = Utils.roundDecimal( Utils.getStdDev(list), 3);
		StringBuffer summary = new StringBuffer();
		summary.append("\"").append(listMean).append("\"");
		if(variance)
			summary.append(",\"").append(listVar).append("\"");
		if(std)
			summary.append(",\"").append(listStd).append("\"");
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
		
		if(file.endsWith("traceLog.txt")) {
			files.add(file);
		}
	}
	
	
}
