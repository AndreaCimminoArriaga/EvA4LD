package tdb.link_discovery.tools.cica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.collect.Lists;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.Avg;
import tdg.link_discovery.middleware.utils.StreamUtils;
import tdg.link_discovery.middleware.utils.Utils;

public class CicaProgressRetriever {
	private static List<String> files;
	private static String fileToSearch;
	
	public static void main(String[] args) {
		String mainDir = "./";//args[0]; //directory from where to start looking for the file
		fileToSearch = "resultado_tails.txt";//args[1]; // file that contains potential errors
		files = Lists.newArrayList();
		recursiveNavigation(new File(mainDir));
		readProgressFile();
		
		
	}

	private static void readProgressFile() {
		for(String file:files) {
			String setup = "";
			Integer folderCounter = 0;
			List<Double> timing = Lists.newArrayList();
			Boolean hasDoneOne = false;
			try {
				if( (new File(file).exists()) ){
					FileInputStream fstream = new FileInputStream(file);
					BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
					String line;
					while ((line = br.readLine()) != null)   {
						String strLine = line;
						if(strLine.startsWith("execution")) {
							setup = "";
							folderCounter = 0;
							setup = strLine;
						}else if(strLine.contains("Starting genetic algorithm")) {
							folderCounter++;
						}else if(strLine.contains("Execution finished:")) {
							String strTime = strLine.replace("Execution finished: ", "").replace("(ms)", "").trim();
							Integer time = Integer.valueOf(strTime)/1000; 
							Double exactTime = time/3600.0;
							timing.add(exactTime);
							hasDoneOne = true;
						}
					}
					br.close();
				}else {
					System.out.println(file+" ... not created yet");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		
			if(hasDoneOne) {
				System.out.println(file+":\n\t-"+setup+"\n\t-Current fold: "+folderCounter+"\n\t+Average time 10-folds,i.e., 1 execution (h): "+getMean(timing)+"  (var="+getVariance(timing)+")"+"  (std="+getStdDev(timing)+")");
			}else {
				System.out.println(file+":\n\t-"+setup+"\n\t-Current fold: "+folderCounter+"\n\t+Average time 10-folds,i.e., 1 execution (h): Nan (has not finish an execution yet)");
			}
		}
	}
	
	private static Double getMean(List<Double> numbers) {
		 Avg avg = new Avg();
		 return Utils.roundDecimal(avg.applyAggregation(numbers), 7);
	}
	
	private static double getVariance( List<Double> numbers){
		   double mean = getMean(numbers);
		   double temp = 0;
		   for(double a :numbers)
		       temp += (a-mean)*(a-mean);
		   return Utils.roundDecimal(temp/(numbers.size()-1), 7);
		}
		
		private static double getStdDev(List<Double> numbers){
		   return Utils.roundDecimal(Math.sqrt(getVariance(numbers)), 7);
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
		
		if(file.endsWith(fileToSearch)) {
			files.add(file);
		}
	}
}
