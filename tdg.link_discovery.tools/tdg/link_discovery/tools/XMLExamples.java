package tdg.link_discovery.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.jena.ext.com.google.common.collect.Lists;

import tdg.link_discovery.middleware.objects.Tuple;


public class XMLExamples {

	private static final String head="<rdf:RDF xmlns:align=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns=\"http://knowledgeweb.semanticweb.org/heterogeneity/alignment#\">\n\t<Alignment>\n";   
	private static final String tail= "\t</Alignment>\n</rdf:RDF>";
	
	private static final String instanceH = "\t\t<map>\n\t\t<Cell>\n\t\t\t<entity1 rdf:resource=\"";
	private static final String instanceM = "\"/>\n\t\t\t<entity2 rdf:resource=\"";
	private static final String instanceT = "\"/>\n\t\t\t<relation>=</relation>\n\t\t\t<measure rdf:datatype=\"http://www.w3.org/2001/XMLSchema#float\">0.0</measure>\n\t\t</Cell>\n\t\t</map>\n";
			

	public static void main(String[] args) {
		
		if(args.length==2){
			
			List<Tuple<String,String>>  lines = readLines(args[0]);
			writeLines(lines, args[1]);
			
		}else{
			System.out.println("provide the program with: nt_with_instances ouput_file");
		}
	}

	private static void writeLines(List<Tuple<String, String>> lines, String outputFile) {
		try {
			Files.write(Paths.get(outputFile), head.getBytes(), StandardOpenOption.CREATE_NEW);
			for(Tuple<String,String> instances:lines){
				StringBuilder str = new StringBuilder();
				str.append(instanceH).append(instances.getFirstElement()).append(instanceM).append(instances.getSecondElement()).append(instanceT);
				Files.write(Paths.get(outputFile), str.toString().getBytes(), StandardOpenOption.APPEND);
			}
			Files.write(Paths.get(outputFile), tail.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<Tuple<String,String>>  readLines(String fileName) {
		// Retrieve the lines
		List<Tuple<String,String>> lines = Lists.newArrayList();

		try {
			LineIterator it = FileUtils.lineIterator(new File(fileName),
					"UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				if (!line.isEmpty()){
					String[] values = line.replaceAll(">\\s*\\.", "").split("<http://www.w3.org/2002/07/owl#sameAs>");
					lines.add(new Tuple<String,String>(values[0].replaceAll("[<>\\s]*", ""), values[1].replaceAll("[<>\\s]*", "")));
				}
			}
			LineIterator.closeQuietly(it);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lines;
	}
	

}
