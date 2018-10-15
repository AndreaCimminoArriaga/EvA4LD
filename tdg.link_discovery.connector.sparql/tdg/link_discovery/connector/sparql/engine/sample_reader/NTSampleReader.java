package tdg.link_discovery.connector.sparql.engine.sample_reader;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.jena.ext.com.google.common.collect.Sets;

import tdg.link_discovery.framework.algorithm.sample.ISampleReader;
import tdg.link_discovery.framework.algorithm.sample.Sample;

public class NTSampleReader implements ISampleReader<String> {

	@Override
	public Collection<Sample<String>> readSamplesFromFile(String file) {
		Set<Sample<String>> samples =  Sets.newHashSet();
		try {
			LineIterator it = FileUtils.lineIterator(new File(file), "UTF-8");
		    while (it.hasNext()) {
		       String line = it.nextLine();
		       if(!line.isEmpty()){
		    	  Sample<String> sample = processLine(line);
		    	  samples.add(sample);
		       }
		    }
		    LineIterator.closeQuietly(it);
		}catch(Exception e){
			e.printStackTrace();
		}
		return samples;
	}

	private Sample<String> processLine(String line) {
		Sample<String> newSample = new Sample<String>();
		String[] iris = new String[2];
		line = line.replaceAll(">\\s*\\.\\s*", "");
		if(line.contains("http://www.w3.org/2002/07/owl#sameAs")){
			//Positive sample
			newSample.setIsPositive(true);
			iris = retrieveIris(line, "http://www.w3.org/2002/07/owl#sameAs");
		}else if(line.contains("owl:sameAs")){
			//Positive sample
			newSample.setIsPositive(true);
			iris = retrieveIris(line, "owl:sameAs");
		}else if(line.contains("owl:differentFrom")){
			//Negative sample
			newSample.setIsPositive(false);
			iris = retrieveIris(line, "owl:differentFrom");
		}else if(line.contains("http://www.w3.org/2002/07/owl#differentFrom")){
			//Negative sample
			newSample.setIsPositive(false);
			iris = retrieveIris(line, "http://www.w3.org/2002/07/owl#differentFrom");
		}else{
			newSample.setIsPositive(null);
			try{
				throw new Exception(line);
			}catch(Exception e){
				System.out.println("ERROR with provided sample: "+line);
				e.printStackTrace();
			}
		}
		newSample.setElement1(iris[0]);
		newSample.setElement2(iris[1]);
		return newSample;
	}
	
	private String[] retrieveIris(String line, String splitToken){
		String[] values = line.split(splitToken);
		values[0] = values[0].replaceAll("[<>\\s]*", "");
		values[1] = values[1].replaceAll("[<>\\s]*", "");
		return values;
	}

}
