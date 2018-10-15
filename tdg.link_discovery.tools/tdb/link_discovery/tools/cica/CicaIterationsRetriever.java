package tdb.link_discovery.tools.cica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.objects.comparators.DoubleNaturalComparator;
import tdg.link_discovery.middleware.utils.StreamUtils;
import tdg.link_discovery.middleware.utils.Utils;

public class CicaIterationsRetriever {

	private static List<String> files;
	private static List<String> iterations;
	
	private static Boolean variance = false;
	private static Boolean std = true;
	

	public static void main(String[] args) throws IOException {
		String folder = args[0].trim();//"/Volumes/Samsung_T5/GPLinkDiscovery/experimentation/environments/cica/cora";
		// Retrieve list of logs
		files = Lists.newArrayList();
		iterations = Lists.newArrayList();
		System.out.println(csvHeather());
		recursiveNavigation(new File(folder));
		for(String file:files) {
			retrieveSummary(file);
		}
	}

	
	private static void retrieveSummary(String file) {
			String algorithm = extractAlgorithm(file);
			String[] folders = null;
			
			if(file.contains("/"))
				folders = file.substring(file.indexOf("results")+7, file.lastIndexOf("/")).split("/");
			if(file.contains("\\"))
				folders =  file.substring(file.indexOf("results")+7, file.lastIndexOf("\\")).split("\\");
			
			StringBuffer accumulator = new StringBuffer(algorithm+",");
			for(String folder:folders) {
				if(!folder.isEmpty() && !folder.equals("results") && !folder.equals("experiments") && !folder.contains("fold_datasets")  && !accumulator.toString().contains(folder) &&  !isNotAPartialAlgorithm(folder))
					accumulator.append("\"").append(folder).append("\",");
			}
			accumulator = new StringBuffer(accumulator.replace(accumulator.length()-1, accumulator.length(), ""));
	
			String stats = retrieveLinesofInterest(file, accumulator.toString());
			System.out.println(stats.toString());
			
		
	}

	private static Boolean isNotAPartialAlgorithm(String file) {
		if(file.startsWith("genlink")) {
			return true;
		}
		if(file.startsWith("carvalho")) {
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
		buff.append("\"").append("Total iterations").append("\",");
		buff.append("\"").append("iterations").append("\",");
		buff.append("\"").append("rule").append("\",");
		buff.append("\"").append("tp").append("\",");
		buff.append("\"").append("tn").append("\",");
		buff.append("\"").append("fp").append("\",");
		buff.append("\"").append("fn").append("\",");
		buff.append("\"").append("P").append("\",");
		buff.append("\"").append("R").append("\",");
		buff.append("\"").append("F").append("\",");
		buff.append("\"").append("Fitness").append("\"");
		return buff.toString();
	}

	private static String retrieveLinesofInterest(String file, String accumulator) {
		
		List<String> rulesLines = Lists.newArrayList();
		List<String> bestRules = Lists.newArrayList();
		Double maxScore = 0.0;
		Boolean startRecording = false;
		String tmpRuleLine = "";
		StringBuffer stats = new StringBuffer();
		StringBuffer preamble = new StringBuffer();
		Map<String,String> rules = new HashMap<String,String>();
		try {
			FileInputStream fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				if(strLine.contains("clazz:Main - 	 max_iterations: ")) {
					String max_it_string = strLine.substring(strLine.indexOf("max_iterations: ")+16, strLine.length()).trim();
					preamble = new StringBuffer();
					preamble.append(accumulator).append(",\"").append(max_it_string).append("\",");
				}
				if(strLine.contains("iteration") && (strLine.contains("STOP") || strLine.contains("elapseTimeEstimated(ms): 0.0"))) {
					String iteration = strLine.replaceAll("iteration:?", "#");
					if(iteration.contains("STOP")) {
						iteration = iteration.substring(iteration.indexOf("#")+1, iteration.lastIndexOf("-")).trim();
					}else if(iteration.contains("executed")) {
						iteration = iteration.substring(iteration.indexOf("#")+1, iteration.indexOf("executed")).trim();
					}
					stats.append(preamble.toString()).append("\"").append(iteration).append("\"\n");
				}
				//**********
				if(strLine.contains("clazz:GenericMOEAGeneticAlgorithm - 	 fitness:") && !startRecording) {
					String line = strLine.replaceAll("^.+clazz:GenericMOEAGeneticAlgorithm - 	 fitness:", "");
					String rule = line.substring(line.indexOf(",")+1).replace("rule:", "").trim();
					String fitness = line.substring(0, line.indexOf(",")).trim();
					// Good fitness is 0, but we are going to normalize it such as 1 is good and 0 is bad
					Double aux = 1- Double.valueOf(fitness);
					fitness = aux.toString();
					rules.put(rule, fitness);
				}
				 if(strLine.contains("Validation of resultant rules:")) {
					startRecording = true;
				}
				if(startRecording && strLine.contains("rule:")){
					tmpRuleLine = strLine.substring(strLine.indexOf("rule:")+5, strLine.length()).trim();
				}
						
				if(startRecording &&	strLine.contains("results:")) {
					String confMatrix = "\""+strLine.substring(strLine.indexOf("[")+1, strLine.indexOf("]"))
												.replace("truePositives=", "")
												.replace("trueNegatives=", "")
												.replace("falsePositives=", "")
												.replace("falseNegatives=", "")
												.replaceAll(",", "\",\"")
												.replaceAll(" ", "")+"\",";
					String p = "\""+strLine.substring(strLine.indexOf("P=")+2, strLine.indexOf("R=")).trim()+"\",";
					String r = "\""+strLine.substring(strLine.indexOf("R=")+2, strLine.indexOf("F=")).trim()+"\",";
					String f = "\""+strLine.substring(strLine.indexOf("F=")+2, strLine.length()).trim()+"\"";
					Double tmpScore = Double.valueOf(strLine.substring(strLine.indexOf("F=")+2, strLine.length()));
					StringBuffer line = new StringBuffer();
					line.append("\"").append(tmpRuleLine).append("\",").append(confMatrix).append(p).append(r).append(f);
				
					if(tmpScore > maxScore) {
						rulesLines.clear();
						rulesLines.add(line.toString()); // add the line
						maxScore = tmpScore;		
						
					}else if (tmpScore.equals(maxScore)) {
						rulesLines.add(line.toString()); // add the line
					}
					
					
					tmpRuleLine = "";
				}			
				if(strLine.contains("Validation executed")) {
					startRecording = false;
					bestRules.addAll(rulesLines);
					rulesLines.clear();
					maxScore = -0.5;
					
		
				}
				
				//**********
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		String lastRule  = "";
	
		lastRule = ","+bestRules.get(1);
		String fitness1 = rules.get(bestRules.get(0).substring(1, bestRules.get(0).indexOf("\",\"")).trim());
		String fitness2 = rules.get(lastRule.substring(2, lastRule.indexOf("\",\"")).trim());
		String statsString = stats.toString().replaceFirst("\n", ","+bestRules.get(0)+",\""+fitness1+"\"\n").replaceAll("\"$", "\""+lastRule+",\""+fitness2+"\"").trim();
		//statsString = statsString.substring(0, statsString.length()-1)+","+bestRules.get(1);
		//System.out.println(statsString.substring(0, statsString.length()-2));
		return statsString;
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
		
		if(file.endsWith("/traceLog.txt")) {
			files.add(file);
		}
	}
	
	
}
