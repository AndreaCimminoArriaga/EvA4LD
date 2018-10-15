package tdg.moea.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.moeaframework.analysis.sensitivity.SampleReader;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import tdg.link_discovery.connector.sparql.engine.evaluator.KFoldEvaluator;
import tdg.link_discovery.connector.sparql.engine.evaluator.deprecated.KFoldCache;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.attribute_selector.SPARQLAttributeSelector;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.framework.environment.Environments;
import tdg.link_discovery.middleware.log.Logger;
import tdg.link_discovery.middleware.objects.Tuple;

public class TestChache {

	public static void main(String[] args) throws Exception {
		
		FrameworkConfiguration.traceLog = new Logger("traceLog.txt", 1);
		FrameworkConfiguration.resultsLog = new Logger("resultsLog.txt", 1);
		
		IEnvironment environment = Environments.parseFromFile("./experiments/environments/abt-buy.cnf");
		KFoldEvaluator evaluator = new KFoldEvaluator(environment, 2, "./keras_kfolding", 1600);
		
		Set<Tuple<String,String>> positiveLinks = evaluator.getTrainingPositiveReferenceLinks();
		Set<Tuple<String,String>> negativeLinks = evaluator.getTrainingNegativeReferenceLinks();
		positiveLinks.addAll(evaluator.getValidationPositiveReferenceLinks());
		negativeLinks.addAll(evaluator.getValidationNegativeReferenceLinks());
		
		SPARQLAttributeSelector reader = new SPARQLAttributeSelector();
		
		File input = new File("input.dat");
		File output = new File("output.dat");
		for(Tuple<String,String> tuple:negativeLinks) {
			Multimap<String, String> sourceAttributesMap = ArrayListMultimap.create();
			Multimap<String, String> targetAttributesMap = ArrayListMultimap.create();
			String sourceIri = tuple.getFirstElement();
			String targetIri = tuple.getSecondElement();
			Set<Tuple<String,String>> sourceAttributes = reader.retrieveAttributesFromExample(environment.getSourceDatasetFile(), sourceIri);
			Set<Tuple<String,String>> targetAttributes = reader.retrieveAttributesFromExample(environment.getTargetDatasetFile(), targetIri);
			sourceAttributes.forEach(attr -> sourceAttributesMap.put(attr.getFirstElement(), attr.getSecondElement()));
			targetAttributes.forEach(attr -> targetAttributesMap.put(attr.getFirstElement(), attr.getSecondElement()));
			List<String> sourceLine = Lists.newArrayList();
			List<String> targetLine = Lists.newArrayList();
			if( sourceAttributesMap.containsKey("http://schema.org/name")) {
				sourceLine.add("\""+sourceAttributesMap.get("http://schema.org/name").iterator().next()+"\"");
			}else {
				sourceLine.add("\"\"");
			}
			if( sourceAttributesMap.containsKey("http://schema.org/description")) {
				sourceLine.add("\""+sourceAttributesMap.get("http://schema.org/name").iterator().next()+"\"");
			}else {
				sourceLine.add("\"\"");
			}
			if( targetAttributesMap.containsKey("http://www.tdg-seville.info/schema/deviceName")) {
				targetLine.add("\""+targetAttributesMap.get("http://www.tdg-seville.info/schema/deviceName").iterator().next()+"\"");
			}else {
				targetLine.add("\"\"");
			}
			if( targetAttributesMap.containsKey("http://www.tdg-seville.info/schema/details")) {
				targetLine.add("\""+targetAttributesMap.get("http://www.tdg-seville.info/schema/details").iterator().next()+"\"");
			}else {
				targetLine.add("\"\"");
			}
			List<String> lines = Lists.newArrayList();
			lines.add("["+sourceLine+", "+targetLine+"]");
			FileUtils.writeLines(input, lines, true);
			lines.clear();
			lines.add("0");
			FileUtils.writeLines(output, lines, true);
		
		}
		
		
	}
	
	
	

	
}
