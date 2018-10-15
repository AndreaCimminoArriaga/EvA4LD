package tdb.link_discovery.tools.cica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import tdg.link_discovery.middleware.utils.FilesUtils;
import tdg.link_discovery.middleware.utils.StreamUtils;

public class CicaRulesProvenanceRetrieverFromList {

	public static List<String> files  = new ArrayList<>();
	
	public static void main(String[] args) {
		String rulesFile = "/Users/cimmino/Desktop/training-scenarios/training_restaurantsZ.csv";
		String baseDirectoryToSearch = "/Volumes/Samsung_T5/GPLinkDiscovery/experimentation/1-environments/cica/";

		StringBuilder header = new StringBuilder();
		header.append("algorithm;scenario;setup;rule;fitness;P;R;F;file;training_file;examples;validation_file");
		System.out.println(header);
		try {
			FileReader reader = new FileReader(rulesFile);
			BufferedReader bufferedReader = new BufferedReader(reader);

			String line;
			Boolean firstLine = true;

			while ((line = bufferedReader.readLine()) != null) {
				if(firstLine) {
					firstLine = false;
				}else {
					
					//if( line.contains("setup2") && line.contains("agg:Min(agg:Min(str:JaroSimilarity(http://schema.org/phone,http://schema.org/phone,0.51),0.71),agg:Avg(agg:Min(agg:Min(agg:Avg(agg:Min(agg:Mult(str:CosineSimilarity(http://schema.org/category,http://schema.org/category,0.35),0.28),str:SoftTFIDFSimilarity(http://schema.org/name,http://schema.org/category,0.32)),agg:Mult(str:SoftTFIDFSimilarity(http://schema.org/category,http://schema.org/category,0.32),0.71)),agg:Mult(str:LevenshteinSimilarity(http://schema.org/city,http://schema.org/city,0.04),0.47)),agg:Mult(str:CosineSimilarity(http://schema.org/category,http://schema.org/category,0.6),0.71)),str:JaroWinklerSimilarity(http://schema.org/city,http://schema.org/city,0.05)),agg:Mult(str:CosineSimilarity(http://schema.org/city,http://schema.org/city,0.6),0.98))")) {
						String[] lineFields = line.split(";");
						String folder = retrieveFolder(lineFields[2]);
						String directoryToSearch = baseDirectoryToSearch+lineFields[1]+"/"+lineFields[0]+"_"+folder+"/experiments/results/"+lineFields[1]+"/"+lineFields[2];
						String rule = lineFields[5];
						
						List<String> dataRuleFound = findSuitableInformation(rule, directoryToSearch, lineFields[10], lineFields[11], lineFields[12]);
						 if(dataRuleFound.isEmpty()) { 
							 System.out.println("----------------------------------");
							 System.out.println("NOT FOUND RULE :"+ rule);
							 System.out.println("DIRECTORY      :"+ directoryToSearch);
							 System.out.println("----------------------------------");
						 }
						for(String dataRule:dataRuleFound)
							System.out.println(lineFields[0]+";"+lineFields[1]+";"+lineFields[2]+";"+dataRule);
						
						//if(!rule.equals("agg:Mult(str:JaroWinklerTFIDFSimilarity(http://www.okkam.org/ontology_restaurant1.owl#name,http://www.okkam.org/ontology_restaurant2.owl#name,0.1),0.62)"))
				
					//}
						
					
				}
				
				
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static List<String> findSuitableInformation(String rule, String directoryToSearch, String P, String R, String F){
		List<String> linesFound = new ArrayList<>();
		files.clear();
		recursiveNavigation(new File(directoryToSearch));
		for(String file:files) {
			if(file.endsWith("/traceLog.txt")) {
				String found = readFile(file, rule.replace(" ", ""), P, R,F);
				if(!found.isEmpty()) {
					linesFound.add(found.trim());
				}
			}
		}
		return linesFound;
	}
	
	private static String readFile(String file, String rule, String P, String R, String F) {
		StringBuilder str = new StringBuilder();
		StringBuilder strFitness = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file)); 
		    String line;
		    Boolean ruleFound = false;
		    Boolean validationFound= false;
		    String trainingFile = "subDatasetFolded_";
		    String validationFile = "subDatasetFolded_";
		    String fitnessRule = null;
		    while ((line = br.readLine()) != null) {
		    	  if(line.contains("clazz:KFoldEvaluator - 	0")) {
		    		  trainingFile = "subDatasetFolded_0.nt";
		    		  validationFile = "subDatasetFolded_1.nt";
		    	  }
		    	  if(line.contains("clazz:KFoldEvaluator - 	1")) {
		    		  trainingFile = "subDatasetFolded_1.nt";
		    		  validationFile = "subDatasetFolded_0.nt";
		    	  }
		    	  
		    	  
		    	  if(line.contains("Validation of resultant rules:")) {
		    		  validationFound = true;
		    	  }else if(line.contains("Validation executed in")) {
		    		  validationFound = false;
		    		  ruleFound = false;
		    		  fitnessRule = null;
		    	  }
		    	  if(line.contains("fitness") && line.contains(", rule") && line.contains(rule)) {
		    		  fitnessRule = line.replaceAll(".*fitness: ", "").replaceAll(", rule.*", "").replace(" ", "");
		    		  
		    	  }
		    	  if(validationFound && line.contains("rule:")) {
		    		  String ruleInLine = line.replaceAll(".*rule: ", "").replace(" ","");
		    		  if(rule.equals(ruleInLine)) {
		    			  ruleFound = true;
		    		  }
		    	  }
		  
				if (fitnessRule!=null && validationFound && ruleFound && line.contains("confusionMatrix")) {
					String auxline = line.replaceAll("^.+ P=", "").replace("R=", ";").replace("F=", ";").replace(" ","");
					Boolean sameMetrics = sameMetrics(P, R, F, auxline);
					
					//if(sameMetrics) {
						try {
							Double fitness = 1- Double.valueOf(fitnessRule);
							str.append(rule).append(";").append(fitness);
							str.append(";").append(auxline);
							str.append(";").append(file).append(";");
							str.append(trainingFile).append(";");
							String examples = FileUtils.readFileToString(new File(file.replace("traceLog.txt", trainingFile)));
							examples = examples.replaceAll("\n", "#").substring(0, examples.length()-1) ;
							str.append( examples ).append(";");
							str.append(validationFile);
						} catch (Exception e) {
							System.out.println("+RULE: " + rule);
							System.out.println("+FILE: " + file);
							System.out.println("+Fitness: " + strFitness);
							e.printStackTrace();
						}
	
						
					//}
					ruleFound = false;
					validationFound = false;
					strFitness = new StringBuilder();
				}
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str.toString();
	}
	
	private static Boolean sameMetrics(String P, String R, String F, String auxline) {
		P = fixNumber(P);
		R = fixNumber(R);
		F = fixNumber(F);
		
		String[] metrics = auxline.split(";");
		String P_found = fixNumber(metrics[0]);
		String R_found = fixNumber(metrics[1]);
		String F_found = fixNumber(metrics[2]);
		
		/*System.out.println(">P_found:"+P_found+" -----<P: "+P);
		System.out.println(">R_found:"+R_found+" -----<R: "+R);
		System.out.println(">F_found:"+F_found+" -----<F: "+F);*/
		return P_found.contains(P) && R_found.contains(R) && F_found.contains(F);
	}
	
	private static String fixNumber(String value) {
		String newValue = value;
		if(value.length()>4)
			newValue = value.substring(0, 4);
		return newValue;
	}
	
	private static String retrieveFolder(String setup) {
		Integer folderNumber = 0;
		Integer setupNumber = Integer.valueOf(setup.replace("setup", ""));
		if(setupNumber>15 && setupNumber<29) {
			folderNumber = 1;
		}else if(setupNumber>28 && setupNumber<39) {
			folderNumber = 2;
		}else if(setupNumber>38) {
			folderNumber = 3;
		}
		return String.valueOf(folderNumber);
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
