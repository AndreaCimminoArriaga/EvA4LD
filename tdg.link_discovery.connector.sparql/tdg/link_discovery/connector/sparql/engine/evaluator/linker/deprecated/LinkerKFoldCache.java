package tdg.link_discovery.connector.sparql.engine.evaluator.linker.deprecated;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import tdg.link_discovery.connector.sparql.engine.evaluator.deprecated.KFoldCache;
import tdg.link_discovery.connector.sparql.engine.evaluator.deprecated.SparqlCacheInstance;
import tdg.link_discovery.framework.engine.evaluator.linker.ILinker;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.framework.learner.executor.LinkSpecificationExecutor;
import tdg.link_discovery.middleware.objects.Tuple;

public class LinkerKFoldCache implements ILinker{

	private KFoldCache kFoldCache;
	private String outputFile;
	
	
	private Set<Tuple<String,String>> instancesLinked;
	private int DEFAULT_THREAD_POOL_SIZE = 150;
	
	
	
	public LinkerKFoldCache(KFoldCache kFoldCache) {
		this.kFoldCache = kFoldCache;
		this.outputFile = "";
		instancesLinked = new CopyOnWriteArraySet<Tuple<String,String>>();
	}

	

	@Override
	public void linkDatasets(Tuple<String, String> queries, String outputFile) {
		String expression = queries.getFirstElement();
		this.outputFile = outputFile;
		link(expression);
		saveResults();
	}
	
	private void saveResults() {
		if(!outputFile.isEmpty()) {
			File output = new File(outputFile);
			if(output.exists())
				output.delete();
			try {
				FileUtils.writeLines(output, this.instancesLinked);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	@Override
	public void linkInstances(Tuple<String, String> queries) {
		String expression = queries.getFirstElement();
		link(expression);
		System.out.println("*Linked: "+this.instancesLinked.size());
	}

	private void link(String expression) {
		Integer threadPool = kFoldCache.getPositiveReferenceLinks().size()+kFoldCache.getNegativeReferenceLinks().size();
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(threadPool, DEFAULT_THREAD_POOL_SIZE));
		List<Callable<Tuple<String,String>>> tasks = Lists.newArrayList();
		// We aim to link each positive and negative reference link
		Set<Tuple<String,String>> instances = retrieveReferenceLinksfromKFoldCacheToLink();
		for(Tuple<String,String> instance : instances) {
			// For each reference link submit a task to link them
			Callable<Tuple<String,String>> task = () -> {
				return linkReferenceLink(expression, instance.getFirstElement(), instance.getSecondElement());
			};
			tasks.add(task);
		}
		// Invoke tasks
		try {
			List<Future<Tuple<String,String>>> futures = executor.invokeAll(tasks);
			for(Future<Tuple<String, String>> future: futures) {
				try {
					Tuple<String,String> link = future.get();
					if(link!=null)
						this.instancesLinked.add(link);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Shutdown executor
		executor.shutdown();
	}
	
	private Set<Tuple<String,String>> retrieveReferenceLinksfromKFoldCacheToLink() {
		Set<Tuple<String,String>> instances = Sets.newHashSet();
		instances.addAll(this.kFoldCache.getPositiveReferenceLinks());
		instances.addAll(this.kFoldCache.getNegativeReferenceLinks());
		return instances;
	}

	
	private Tuple<String, String> linkReferenceLink(String expression, String firstElement, String secondElement) {
		Tuple<String, String> link = null;
		// Retrieve instances
		SparqlCacheInstance sourceReferenceLink = this.kFoldCache.getSourceCache().getInstance(firstElement);
		SparqlCacheInstance targetReferenceLink = this.kFoldCache.getTargetCache().getInstance(secondElement);
		// Replace in the expression the instance attribute values
		String newExpression = replaceExpressionWithAttributes(expression,sourceReferenceLink, true);
		newExpression = replaceExpressionWithAttributes(newExpression,targetReferenceLink, false);
	
		// Execute link specification
		LinkSpecificationExecutor executor = new LinkSpecificationExecutor(newExpression);
		Double score = executor.evaluateStringSpecification(newExpression);
		//System.out.println(firstElement+" <=> "+secondElement+" ::> "+score);
		if(score > 0)
			link = new Tuple<String,String>(firstElement,secondElement);
		
		return link;
	}
	
	private String replaceExpressionWithAttributes(String expression, SparqlCacheInstance instance, Boolean source) {
		// Prepare regex pattern
		String pattern = "";
		String token = FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER;
		if (!source)
			token = FrameworkConfiguration.LINK_SPECIFICATION_TARGET_ATTR_DELIMITER;
		StringBuffer regexStr = new StringBuffer();
		regexStr.append("[^").append(token).append("]+");
		pattern = wrapString(regexStr.toString(), token);
		// Create a Pattern object
		Pattern regex = Pattern.compile(pattern);
		// Now create matcher object.
		Matcher matcher = regex.matcher(expression);
		String newExpression = expression;
		while (matcher.find()) {
			// replace attribute labels in expression with related attributes
			String attributeLabel = matcher.group().replace(token, "");
			
			if(instance.containsAttribute(attributeLabel)) {
				String attributeValue = instance.getAttribute(attributeLabel);
				newExpression = newExpression.replace(wrapString(attributeLabel, token), wrapString(attributeValue,FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER));
			}
		}
		
		return newExpression;
	}
	
	// Adds a prefix and a suffix to a given string
	private String wrapString(String value, String separator) {
		StringBuffer newValue = new StringBuffer();
		newValue.append(separator);
		newValue.append(value);
		newValue.append(separator);
		return newValue.toString();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public Set<Tuple<String, String>> getInstancesLinked() {
		return instancesLinked;
	}
	
	public void setDefaultThreadPoolSize(Integer threadPoolSize) {
		DEFAULT_THREAD_POOL_SIZE = threadPoolSize;
	}
	
	
	/*
	 * Deprecated methods
	 */

	@Deprecated
	@Override
	public void setInstances(Set<Tuple<String, String>> instances) {
		//
	}
	
	@Deprecated
	@Override
	public void setDatasetSource(String datasetSource) {
		// TODO Auto-generated method stub
		
	}
	@Deprecated
	@Override
	public void setDatasetTarget(String datasetTarget) {
		// TODO Auto-generated method stub
		
	}
	@Deprecated
	@Override
	public void setDatasetSource(Model datasetSource) {
		// TODO Auto-generated method stub
		
	}

	@Deprecated
	@Override
	public void setDatasetTarget(Model datasetTarget) {
		// TODO Auto-generated method stub
		
	}

}
