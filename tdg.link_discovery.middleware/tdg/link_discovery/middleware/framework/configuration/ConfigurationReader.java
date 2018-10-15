package tdg.link_discovery.middleware.framework.configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Maps;

import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.framework.environment.Environment;
import tdg.link_discovery.middleware.objects.Tuple;

public class ConfigurationReader {

	
	
	/*
	 * Environment reader methods
	 */
	
	public static IEnvironment readIEnvironmentFromFile(String file) {
		Map<String, Object> rawConfiguration = readRawEnvironmentFromFile(file);
		IEnvironment environment = environmentFromMap(rawConfiguration);
		return environment;
	}
	
	@SuppressWarnings({ "resource", "unchecked" })
	public static Map<String, Object> readRawEnvironmentFromFile(String file) {
		Map<String, Object> rawConfiguration = Maps.newHashMap();
		try{ 
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				if(!line.isEmpty()){
					if(!line.startsWith("#")){
						String[] arguments = line.trim().split(FrameworkConfiguration.SETUP_SEPARATOR_TOKEN);
						if(arguments.length==2){
							Tuple<String, Object> result = matchesAnyEnvironmentKeys(arguments[0].trim(), arguments[1].trim());
							if(result!=null){
								if(result.getFirstElement().equals(FrameworkConfiguration.ENVIRONMENT_SUITABLE_ATTRIBUTES_INFILE_TOKEN)){
									if(rawConfiguration.containsKey(result.getFirstElement())){
										List<Tuple<String,String>> old_attributes = (List<Tuple<String, String>>) rawConfiguration.get(result.getFirstElement());
										old_attributes.add((Tuple<String, String>)result.getSecondElement());
										rawConfiguration.put(result.getFirstElement(), old_attributes);
									}else{
										List<Tuple<String,String>> suitableAttr = Lists.newArrayList();
										Tuple<String,String> tuple = (Tuple<String, String>) result.getSecondElement();
										suitableAttr.add(tuple);
										rawConfiguration.put(result.getFirstElement(), suitableAttr);
									}
								}else{
									rawConfiguration.put(result.getFirstElement(), result.getSecondElement());
								}
								
								
							}else{
								throw new Exception("Error in setup file "+file+" with argument: "+line);
							}
						}else{
							throw new Exception("Error in setup file "+file+" with argument: "+line);
						}
					}
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rawConfiguration;
	}
	


	
	private static Tuple<String,Object> matchesAnyEnvironmentKeys(String token, String value){
		Tuple<String,Object> result = new Tuple<String,Object>();
		if(token.equals(FrameworkConfiguration.ENVIRONMENT_SOURCEDATASET_INFILE_TOKEN)){
			result = new Tuple<String,Object>(token,value);
		}else if(token.equals(FrameworkConfiguration.ENVIRONMENT_TARGETDATASET_INFILE_TOKEN)){
			result = new Tuple<String,Object>(token,value);
		}else if(token.equals(FrameworkConfiguration.ENVIRONMENT_EXAMPLESFILE_INFILE_TOKEN)){
			result = new Tuple<String,Object>(token,value);
		}else if(token.equals(FrameworkConfiguration.ENVIRONMENT_SPECIFICATIONS_OUTPUT_FILE_INFILE_TOKEN)){
			result = new Tuple<String,Object>(token,value);
		}else if(token.equals(FrameworkConfiguration.ENVIRONMENT_LINKS_OUTPUT_FILE_INFILE_TOKEN)){
			result = new Tuple<String,Object>(token,value);
		}else if(token.equals(FrameworkConfiguration.ENVIRONMENT_GOLD_STANDARD_INFILE_TOKEN)){
			result = new Tuple<String,Object>(token,value);
		}else if(token.equals(FrameworkConfiguration.ENVIRONMENT_ALGORITHM_STATISTICS_INFILE_TOKEN)){
			result = new Tuple<String,Object>(token,value);
		}else if(token.equals(FrameworkConfiguration.ENVIRONMENT_SOURCE_RESTRICTIONS_INFILE_TOKEN)){
			List<String> splitedRestrictions = separateStringList(value);
			result = new Tuple<String,Object>(token,splitedRestrictions);
		}else if(token.equals(FrameworkConfiguration.ENVIRONMENT_TARGET_RESTRICTIONS_INFILE_TOKEN)){
			List<String> splitedRestrictions = separateStringList(value);
			result = new Tuple<String,Object>(token,splitedRestrictions);
		}else if(token.equals(FrameworkConfiguration.ENVIRONMENT_SUITABLE_ATTRIBUTES_INFILE_TOKEN)){
			List<String> splitedRestrictions = separateStringList(value);
			result = new Tuple<String,Object>(token,new Tuple<String,String>(splitedRestrictions.get(0).trim(), splitedRestrictions.get(1).trim()));
		}else{
			result = null;
		}
		return result;
	}
	
	private static List<String> separateStringList(String stringList){
		String[] list = stringList.split(",");
		List<String> elements = Lists.newArrayList();
		elements.addAll(Arrays.asList(list));
		return elements;
	}
	
	
	@SuppressWarnings("unchecked")
	public static IEnvironment environmentFromMap(Map<String,Object> rawEnvironment){
		// Mandatory
		checkFileEnvironmentHasMandatoryElements(rawEnvironment);
		String sourceDataset = (String) rawEnvironment.get(FrameworkConfiguration.ENVIRONMENT_SOURCEDATASET_INFILE_TOKEN);
		String targetDataset = (String) rawEnvironment.get(FrameworkConfiguration.ENVIRONMENT_TARGETDATASET_INFILE_TOKEN);
		
		// Optional
		String specificationsOutput = "";
		String statisticsOutput = "";
		String linksOutput = "";
		String examplesFile = "";
		String goldStandard = "";
		List<String> sourceRestrictions = Lists.newArrayList();
		List<String> targetRestrictions = Lists.newArrayList();
		List<Tuple<String,String>> suitableAttributes =  Lists.newArrayList();
		
		if(rawEnvironment.containsKey(FrameworkConfiguration.ENVIRONMENT_SPECIFICATIONS_OUTPUT_FILE_INFILE_TOKEN))
			specificationsOutput = (String) rawEnvironment.get(FrameworkConfiguration.ENVIRONMENT_SPECIFICATIONS_OUTPUT_FILE_INFILE_TOKEN);
		if(rawEnvironment.containsKey(FrameworkConfiguration.ENVIRONMENT_ALGORITHM_STATISTICS_INFILE_TOKEN))
			statisticsOutput = (String) rawEnvironment.get(FrameworkConfiguration.ENVIRONMENT_ALGORITHM_STATISTICS_INFILE_TOKEN);
		if(rawEnvironment.containsKey(FrameworkConfiguration.ENVIRONMENT_LINKS_OUTPUT_FILE_INFILE_TOKEN))
			linksOutput = (String) rawEnvironment.get(FrameworkConfiguration.ENVIRONMENT_LINKS_OUTPUT_FILE_INFILE_TOKEN);
		if(rawEnvironment.containsKey(FrameworkConfiguration.ENVIRONMENT_EXAMPLESFILE_INFILE_TOKEN))
			examplesFile =(String) rawEnvironment.get(FrameworkConfiguration.ENVIRONMENT_EXAMPLESFILE_INFILE_TOKEN);
		if(rawEnvironment.containsKey(FrameworkConfiguration.ENVIRONMENT_GOLD_STANDARD_INFILE_TOKEN))
			goldStandard = (String) rawEnvironment.get(FrameworkConfiguration.ENVIRONMENT_GOLD_STANDARD_INFILE_TOKEN);
		if(rawEnvironment.containsKey(FrameworkConfiguration.ENVIRONMENT_SOURCE_RESTRICTIONS_INFILE_TOKEN))
			sourceRestrictions = (List<String>) rawEnvironment.get(FrameworkConfiguration.ENVIRONMENT_SOURCE_RESTRICTIONS_INFILE_TOKEN);
		if(rawEnvironment.containsKey(FrameworkConfiguration.ENVIRONMENT_TARGET_RESTRICTIONS_INFILE_TOKEN))
			targetRestrictions = (List<String>) rawEnvironment.get(FrameworkConfiguration.ENVIRONMENT_TARGET_RESTRICTIONS_INFILE_TOKEN);
		if(rawEnvironment.containsKey(FrameworkConfiguration.ENVIRONMENT_SUITABLE_ATTRIBUTES_INFILE_TOKEN))
			suitableAttributes = (List<Tuple<String,String>>) rawEnvironment.get(FrameworkConfiguration.ENVIRONMENT_SUITABLE_ATTRIBUTES_INFILE_TOKEN);
		
		// Generate environment object to return
		IEnvironment environment = new Environment("",sourceDataset, targetDataset, examplesFile, linksOutput, specificationsOutput);
		environment.setSourceRestrictions(sourceRestrictions);
		environment.setTargetRestrictions(targetRestrictions);
		environment.setSuitableAttributes(suitableAttributes);
		environment.setGoldStandardFile(goldStandard);
		environment.setAlgorithmStatisticsFile(statisticsOutput);
		return environment;
	}
	
	private static void checkFileEnvironmentHasMandatoryElements(Map<String,Object> rawEnvironment) {
		try{
			if(!rawEnvironment.containsKey(FrameworkConfiguration.ENVIRONMENT_SOURCEDATASET_INFILE_TOKEN))
				throw new Exception("Check setup file, source dataset is missing");
			if(!rawEnvironment.containsKey(FrameworkConfiguration.ENVIRONMENT_TARGETDATASET_INFILE_TOKEN))
				throw new Exception("Check setup file, target dataset is missing");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	/*
	 * Setup reader methods
	 */
	
	public static Setup readSetupFromFile(String file){
		Map<String, String> rawConfiguration = readRawSetupFromFile(file);
		Setup setup = setupFromMap(rawConfiguration);
		return setup;
	}
	
	@SuppressWarnings("resource")
	public static Map<String, String> readRawSetupFromFile(String file) {
		Map<String, String> rawConfiguration = Maps.newHashMap();
		try{ 
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = br.readLine()) != null) {
				if(!line.startsWith("#") && !line.isEmpty()){
					String[] arguments = line.trim().replaceAll("\\#.*", "").split(FrameworkConfiguration.SETUP_SEPARATOR_TOKEN);
					if(arguments.length==2){
						rawConfiguration.put(arguments[0].trim(), arguments[1].trim());
					}else{
						throw new Exception("Error in setup file "+file+" with argument: "+line);
					}
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rawConfiguration;
	}
	

	private static Setup setupFromMap(Map<String, String> rawConfiguration) {
		Setup setup = new Setup();
		try{
			for(Entry<String,String> entry:rawConfiguration.entrySet()){
				String token = entry.getKey();
				String value  = entry.getValue();
				Number number = NumberFormat.getInstance().parse(value);
				
				if(token.equals(FrameworkConfiguration.SETUP_MAX_ITERATION_INFILE_TOKEN)){
					setup.addMaxIterations(number);
				}else if(token.equals(FrameworkConfiguration.SETUP_POPULATION_SIZE_INFILE_TOKEN)){
					setup.addPopulationSize(number);
				}else if(token.equals(FrameworkConfiguration.SETUP_NUM_ALGORITHM_OBJECTIVES_INFILE_TOKEN)){
					setup.addObjectives(number);
				}else if(token.equals(FrameworkConfiguration.SETUP_NUM_AGLORITHM_VARIABLES_INFILE_TOKEN)){
					setup.addVariables(number);
				}else if(token.equals(FrameworkConfiguration.SETUP_CROSSOVER_RATE_INFILE_TOKEN)){
					setup.addCrossoverRate(number);
				}else if(token.equals(FrameworkConfiguration.SETUP_MUTATION_RATE_INFILE_TOKEN)){
					setup.addMutationRate(number);
				}else if(token.equals(FrameworkConfiguration.SETUP_PARENTS_SELECTION_RATE_INFILE_TOKEN)){
					setup.addParentsSelectionSize(number);
				}else if(token.equals(FrameworkConfiguration.SETUP_GENERATIONS_RATE_INFILE_TOKEN)){
					setup.addGenerationsRate(number);
				}else{
					setup.addOtherParameter(token, number);
				}
			}
				
		}catch(Exception e){
			e.printStackTrace();
			setup = null;
		}
		return setup;
	}


	
}
