package tdg.link_discovery.middleware.framework.environment;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.objects.Tuple;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class Environments {

	
	public static IEnvironment parseFromFile(String filePath) throws Exception{
		// Read lines
		List<String> lines = readFileLines(filePath);
		if(lines==null)
			throw new Exception("File "+filePath+" not found!");
		// Retrieve mandatory keywords contained in file
		List<String> keywords = lines.stream().map(line -> extractKeywork(line)).collect(Collectors.toList());
		// Retrieve mandatory keywords of an Environment
		List<String> mandatoryKeywords = Environment.getEnvironmentKeywords()
											.entrySet().stream()
												.filter(tuple -> tuple.getValue()==true)
												.map(tuple -> tuple.getKey())
												.collect(Collectors.toList());
		// Check file contains all mandatory keywords
		for(String mandatoryKeyword:mandatoryKeywords){
			if(!keywords.contains(mandatoryKeyword)){
				throw new Exception(Environments.class.getCanonicalName()+" : Missing "+mandatoryKeyword+" mandatory keyword in "+filePath);
			}
		}
		// Transform file into Multimap arguments
		Multimap<String,String> arguments = transformIntoMultimap(lines);
		IEnvironment newEnvironment = new Environment(arguments);
		return newEnvironment;
	}
	
	
	
	private static List<String> readFileLines(String filePath) {
		List<String> lines = null;
		try {
			
			File file = Paths.get(filePath).toFile();
			lines = FileUtils.readLines(file, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lines;
	}
	
	private static String extractKeywork(String line){
		String keyworkd = null;
		if(line.contains(":=")){
			String[] lineSplitted = line.split(":=");
			if(lineSplitted.length!=0)
				keyworkd = lineSplitted[0].trim();
		}
		return keyworkd;
	}
	
	private static Multimap<String,String> transformIntoMultimap(List<String> lines) throws Exception{
		Multimap<String,String> arguments = ArrayListMultimap.create();
		
		for(String line:lines){
			if(!line.startsWith("#") && !line.isEmpty()){ // in case is not a comment
				Tuple<String, String> argument = extractArgument(line);
				arguments.put(argument.getFirstElement(), argument.getSecondElement());
			}
		}
		
		return arguments;
	}
	

	private static Tuple<String,String> extractArgument(String line) throws Exception{
		Tuple<String,String> argument = null;
		if(line.contains(":=")){
			String[] lineSplitted = line.split(":=");
			if(lineSplitted.length==2){
				argument = new Tuple<String,String>(lineSplitted[0].trim(), lineSplitted[1].trim());
			}else{
				throwLineException(line);
			}
		}else{
			if(!line.startsWith("#") && !line.isEmpty())
				throwLineException(line);
		}
		return argument;
	}



	private static void throwLineException(String line) throws Exception {
		throw new Exception(Environments.class.getCanonicalName()+" : Error parsing the line \""+line+"\"");
	}
}
