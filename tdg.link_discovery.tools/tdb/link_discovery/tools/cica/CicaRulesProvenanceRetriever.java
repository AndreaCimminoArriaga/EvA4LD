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

public class CicaRulesProvenanceRetriever {

	private static List<String> files;

	public static void main(String[] args) throws IOException {
		String folder =  "/Volumes/Samsung_T5/GPLinkDiscovery/experimentation/1-environments/cica/persons1/carvalho_0/experiments/results/persons1/setup0/2-fold_datasets-09_59_13";//args[0];
		// Retrieve list of logs
		files = Lists.newArrayList();
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
			
			List<String> stats = retrieveLinesofInterest(file);
			StringBuffer accumulator = new StringBuffer(algorithm+",");
			for(String folder:folders) {
				if(!folder.isEmpty() && !folder.equals("results") && !folder.equals("experiments") && !folder.contains("fold_datasets")  && !accumulator.toString().contains(folder) &&  !isNotAPartialAlgorithm(folder))
					accumulator.append("\"").append(folder).append("\",");
			}
						
			accumulator = new StringBuffer(accumulator.replace(accumulator.length()-1, accumulator.length(), ""));
			for(String line:stats)
				System.out.println(accumulator.toString()+","+line);
			
			
		
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
		buff.append("\"").append("rule").append("\",");
		
		buff.append("\"").append("learning_file").append("\",");
		buff.append("\"").append("path").append("\"");
		
		return buff.toString();
	}


	private static List<String> retrieveLinesofInterest(String file) {
		
		List<String> rulesLines = Lists.newArrayList();
		List<String> rulesLinesFinal = Lists.newArrayList();
		Double maxScore = 0.0;
		Boolean startRecording = false;
		String tmpRuleLine = "";
		String trainingFolder = "";
		try {
			FileInputStream fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				if(strLine.contains("Validation of resultant rules:")) {
					startRecording = true;
					maxScore =0.0;
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
					line.append("\"").append(tmpRuleLine).append("\",\"").append(trainingFolder).append("\",\"").append(file.substring(0, file.lastIndexOf("/"))).append("\"");
				
					
					if(tmpScore > maxScore) {
						rulesLines.clear();
						rulesLines.add(line.toString()); // add the line
						maxScore = tmpScore;		
					}else if (tmpScore.equals(maxScore) || tmpScore == maxScore || (tmpScore - maxScore) ==0.0 ) {
						rulesLines.add(line.toString()); // add the line
					}
					
					
					
					tmpRuleLine = "";
				}	
				if(strLine.contains("Start learning with fold: ")) {
					Integer index = Integer.valueOf(strLine.substring(strLine.indexOf("with fold: ")+11, strLine.indexOf("/"))) -1 ;
					trainingFolder = "subDatasetFolded_"+index+".nt";
				}
				if(strLine.contains("Validation executed")) {
					startRecording = false;
					rulesLinesFinal.addAll(rulesLines);
					rulesLines.clear();
					
				}
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	
		
		return rulesLinesFinal;
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
