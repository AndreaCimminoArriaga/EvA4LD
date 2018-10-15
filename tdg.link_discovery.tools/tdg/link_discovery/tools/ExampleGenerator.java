package tdg.link_discovery.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Lists;

import tdg.link_discovery.middleware.utils.Utils;

public class ExampleGenerator {

	private final String sameAs = "<http://www.w3.org/2002/07/owl#sameAs>";
	private final String differentFrom = "<http://www.w3.org/2002/07/owl#differentFrom>";
	private List<String> exampleSameAsIRIs, examplesDifferentIRIs;
	private List<String> goldStdIRIs;
	private Integer examplesSize;
	
	
	
	public ExampleGenerator(String goldStdFile, Double percentage){
		goldStdIRIs = Lists.newArrayList();
		exampleSameAsIRIs = Lists.newArrayList();
		examplesDifferentIRIs =  Lists.newArrayList();

		
		try {
			Stream<String> stream = Files.lines(Paths.get(goldStdFile));
	        stream.forEach(line ->goldStdIRIs.add(line));
	        stream.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		this.examplesSize = (int) Math.floor(this.goldStdIRIs.size()*percentage);
		
		initiNegativeExamples();
		initiPositiveExamples();
		
		System.out.println("done!");
	}
	
	private void initiPositiveExamples(){
		while(exampleSameAsIRIs.size()<examplesSize){
			Integer randomIndexLine1 = Utils.getRandomInteger(examplesSize-1, 0);
			String randomLine1 = this.goldStdIRIs.get(randomIndexLine1);
			if(!exampleSameAsIRIs.contains(randomLine1))
				exampleSameAsIRIs.add(randomLine1);
		}
		
	}
	
	private void initiNegativeExamples(){
		while(examplesDifferentIRIs.size()<examplesSize){
			Boolean validExample = false;
			while(!validExample){
				Integer randomIndexLine1 = Utils.getRandomInteger(examplesSize-1, 0);
				Integer randomIndexLine2 = Utils.getRandomInteger(examplesSize-1, 0);
				String randomLine1 = this.goldStdIRIs.get(randomIndexLine1);
				String randomLine2 = this.goldStdIRIs.get(randomIndexLine2);
				if(!randomLine1.equals(randomLine2)){
					String[] iris1 = randomLine1.split(sameAs);
					String[] iris2 = randomLine2.split(sameAs);
					StringBuffer str = new StringBuffer();
					str.append(iris1[0].trim()).append(" ").append(differentFrom).append(" ").append(iris2[1].trim());
					if(!goldStdIRIs.contains(str.toString())){
						examplesDifferentIRIs.add(str.toString());
						validExample = true;
					}
				}
			}
		}
	}


	
	public void writeExamplesIntoFile(String outputFile){
		List<String> examples = Lists.newArrayList();
		this.exampleSameAsIRIs.forEach(entry -> examples.add(generateExampleLine(entry,true)));
		this.examplesDifferentIRIs.forEach(entry -> examples.add(generateExampleLine(entry,false)));
		
		try {
			Files.write(Paths.get(outputFile), examples);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String generateExampleLine(String line,Boolean positiveExample){
		if(!positiveExample)
			line = line.replace(sameAs, differentFrom);
		return line;
	}
	
}
