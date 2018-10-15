package tdg.link_discovery.middleware.utils;

import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.tdb.TDBFactory;

public class FrameworkUtils {

	
	public static List<String> readGoldLinks(String fileName) {
		List<String> goldLinks = Lists.newArrayList();
		try {
			LineIterator it = FileUtils.lineIterator(new File(fileName),"UTF-8");
			StreamUtils.asStream(it).forEach(line -> {
				if (!line.isEmpty() && line.contains(" ")) {
					String value = line.replaceAll("\\s+", "");
					goldLinks.add(value);
				}
			});
			LineIterator.closeQuietly(it);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return goldLinks;
	}

	public static List<String> readSameAsLinks(String fileName) {
		List<String> positiveLinks = Lists.newArrayList();
		try {
			LineIterator it = FileUtils.lineIterator(new File(fileName),"UTF-8");
			StreamUtils.asStream(it).forEach(line -> {
				if (!line.isEmpty() && line.contains(" ") && (line.contains("owl:sameAs") || line.contains("http://www.w3.org/2002/07/owl#sameAs") )) {
					String value = line.replaceAll("\\s+", "");
					positiveLinks.add(value);
				}
			});
			LineIterator.closeQuietly(it);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return positiveLinks;
	}

	
	public static List<String> readDifferentFromLinks(String fileName) {
		List<String> negativeLinks = Lists.newArrayList();
		try {
			LineIterator it = FileUtils.lineIterator(new File(fileName),"UTF-8");
			StreamUtils.asStream(it).forEach(line -> {
				if (!line.isEmpty() && line.contains(" ") && (line.contains("owl:differentFrom") || line.contains("http://www.w3.org/2002/07/owl#differentFrom") )) {
					String value = line.replaceAll("\\s+", "");
					negativeLinks.add(value);
				}
			});
			LineIterator.closeQuietly(it);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return negativeLinks;
	}
	
	public static Boolean createEmptyNamedModelForDataset(String datasetTDBDirectory, String modelName) {
		Boolean correctlyCreated = false;
		Dataset dataset = TDBFactory.createDataset(datasetTDBDirectory);
		if(!dataset.containsNamedModel(modelName)) {
			Model model = ModelFactory.createDefaultModel();
			Statement statement = FrameworkUtils.createJenaStatement("http://www.tdg-seville.info/acimmino/Home", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Author");
			model.add(statement);
			dataset.addNamedModel(modelName.trim(), model);
	        correctlyCreated = dataset.containsNamedModel(modelName);
		}
		dataset.close();
		return correctlyCreated;
	}
	
	public static Boolean createNamedModelForDataset(String datasetTDBDirectory, String modelName, Model model) {
		Boolean correctlyCreated = false;
		Dataset dataset = TDBFactory.createDataset(datasetTDBDirectory);
		if(!dataset.containsNamedModel(modelName)) {
			dataset.addNamedModel(modelName.trim(), model);
	        correctlyCreated = dataset.containsNamedModel(modelName);
		}
		dataset.close();
		return correctlyCreated;
	}
	
	public static Statement createJenaStatement(String subjectStr, String predicateStr, String objectStr) {
		Resource subject = ResourceFactory.createResource(subjectStr);
		Property predicate = ResourceFactory.createProperty(predicateStr);
		Resource object = ResourceFactory.createResource(objectStr);
		Statement newReferenceLink =  ResourceFactory.createStatement(subject, predicate, object);
		return newReferenceLink;
	}


}
