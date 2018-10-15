package tdg.link_discovery.tools.effectiveness;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;

import tdg.link_discovery.middleware.utils.FrameworkUtils;
import tdg.link_discovery.middleware.utils.StreamUtils;

public class LinkageEvaluatorMain {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		LinkageEvaluator evaluator = new LinkageEvaluator();
		String goldFile = "./experiments/gold-stds/restaurant1-restaurant2-gold.nt";
		String resultsDirectory = "./100-native";
		List<String> goldLinks = FrameworkUtils.readGoldLinks(goldFile);
		
		File[] files = new File(resultsDirectory).listFiles();
		List<String> linesToWrite = Lists.newArrayList();
		StreamUtils.asStream(files).forEach(file -> linesToWrite.add(LinkageEvaluator.getMetrics(goldLinks, file.getAbsolutePath())));
	
		linesToWrite.stream().forEach(line -> System.out.println(line));
	}

}
