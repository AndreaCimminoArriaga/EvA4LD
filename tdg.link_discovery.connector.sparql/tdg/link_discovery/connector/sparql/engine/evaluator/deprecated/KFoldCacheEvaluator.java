package tdg.link_discovery.connector.sparql.engine.evaluator.deprecated;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.tdb.TDBFactory;
import com.google.common.collect.Lists;

import tdg.link_discovery.connector.sparql.engine.evaluator.deprecated.KFoldCache;
import tdg.link_discovery.connector.sparql.engine.evaluator.linker.LinkerKFoldCacheTDB;
import tdg.link_discovery.connector.sparql.engine.evaluator.linker.deprecated.LinkerKFoldCache;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.engine.evaluator.AbstractEvaluator;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.FrameworkUtils;
import tdg.link_discovery.middleware.utils.Utils;

public class KFoldCacheEvaluator extends AbstractEvaluator{

	private List<KFoldCache> subDatasets;
	private Integer currentValidationSubDataset;
	private List<String> globalReferenceLinks;
	private ConfusionMatrix validationMatrix;
	
	public KFoldCacheEvaluator(IEnvironment environment, Integer kFoldSize, String workingFolderName, Integer referenceLinksSize) {
		super(environment,null);
		if (kFoldSize > referenceLinksSize)
			throw new IllegalArgumentException("'referenceLinksSize' parameter cannot be lower than 'kFoldSize'  (Clazz KFoldCacheEvaluator)");
			
		// create directory tree
		initFileSystem(workingFolderName);
		// init subdatasets
		initializeSubDatasets(kFoldSize);
		// retrieve positivesReferenceLinks
		Set<Tuple<String,String>> positiveReferenceLinks = retrieveReferenceLinks(referenceLinksSize, environment);
		// sparse positive reference links into subDatasets
		sparsePositiveReferenceLinks(positiveReferenceLinks);
		// generate negative links
		generateNegativeReferenceLinks();
		// load instances into caches
		loadInstancesIntoCaches();
		// persist caches into file
		// TODO: persist
	}
	

	private void initFileSystem(String workingFolderName) {
		try {
			String directory = workingFolderName;
			String[] subDirectories = null;
			Boolean isMicrosoft = false;
			if(directory.contains("/")) {
				subDirectories= directory.split("/");
			}else {
				subDirectories = directory.replace("\\","/").split("/");
				isMicrosoft = true;
			}
			Integer subDirectoryCounter = 0;
			StringBuffer incrementalDirectory = new StringBuffer();
			//FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t Creating tree of directories");
			while(subDirectoryCounter < subDirectories.length-1) {
				if(!isMicrosoft) {
					incrementalDirectory.append(subDirectories[subDirectoryCounter]).append("/");
				}else {
					incrementalDirectory.append(subDirectories[subDirectoryCounter]).append("\\");
				}
				Boolean newFile = new File(Utils.getAbsoluteSystemPath(incrementalDirectory.toString())).mkdirs();
				//FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t "+incrementalDirectory+" = "+newFile);
				subDirectoryCounter++;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeSubDatasets(Integer kFoldSize) {
		subDatasets = Lists.newCopyOnWriteArrayList();
		// Create kFoldSize sub-datasets
		for(int i=0;i<kFoldSize; i++) {
			KFoldCache subDataset = new KFoldCache();
			subDataset.setIsTrainingFold(true);
			subDatasets.add(subDataset);
		}
		// Set validation dataset
		currentValidationSubDataset = subDatasets.size()-1;
		subDatasets.get(currentValidationSubDataset).setIsTrainingFold(false);
	}

	public Set<Tuple<String,String>> retrieveReferenceLinks(Integer referenceLinksSize, IEnvironment environment) {
		globalReferenceLinks = FrameworkUtils.readGoldLinks(environment.getGoldStandardFile());
		Set<Tuple<String,String>> positiveReferenceLinks = Sets.newHashSet();
		// Select a random gold link
		while(positiveReferenceLinks.size() < referenceLinksSize ) {
			Integer randomElement = Utils.getRandomInteger(globalReferenceLinks.size()-1, 0);
			String linkStr = globalReferenceLinks.get(randomElement);
			// adapt gold link to tuple format
			Tuple<String,String> link = transformLinkStrIntoTuple(linkStr);
			positiveReferenceLinks.add(link);
			// In case the referenceLinksSize is higher than the goldStd size break the loop
			// For instance referenceLinksSize=1600 but referenceLinks=120
			if(positiveReferenceLinks.size() == globalReferenceLinks.size())
				break;
		}
		return positiveReferenceLinks;
	}
	
	
	private void sparsePositiveReferenceLinks(Set<Tuple<String, String>> positiveReferenceLinks) {
		Integer referenceLinkPerFold = Math.round(positiveReferenceLinks.size()/subDatasets.size());
		for(KFoldCache subDatasetIndex:this.subDatasets) {
			Set<Tuple<String,String>> subPositiveReferenceLinks = Sets.newHashSet();
			
			while(subPositiveReferenceLinks.size()<referenceLinkPerFold && positiveReferenceLinks.size()>0) {
				@SuppressWarnings("unchecked")
				Tuple<String,String> link = (Tuple<String, String>) Utils.getRandomElement(positiveReferenceLinks);
				positiveReferenceLinks.remove(link);
				subPositiveReferenceLinks.add(link);
			}
			subDatasetIndex.setPositiveReferenceLinks(subPositiveReferenceLinks);
			
		}
	}
		
	private void generateNegativeReferenceLinks() {
		for(KFoldCache currentCache:this.subDatasets) {
			Set<Tuple<String,String>> negativeReferenceLinks = generateNegativeLinksFromPositiveLinks(currentCache.getPositiveReferenceLinks());
			// it may happen that some of the previous positive links where not enough to generate the negatives,
			// in this case we rely on the globalReferenceLinks to generate new negative links
			while(negativeReferenceLinks.size() < currentCache.getPositiveReferenceLinks().size()) {
				Integer randomElement1 = Utils.getRandomInteger(globalReferenceLinks.size()-1, 0);
				String linkStr1 = globalReferenceLinks.get(randomElement1);
				Integer randomElement2 = Utils.getRandomInteger(globalReferenceLinks.size()-1, 0);
				String linkStr2 = globalReferenceLinks.get(randomElement2);
				Tuple<String,String> tuple1 = transformLinkStrIntoTuple(linkStr1);
				Tuple<String,String> tuple2 = transformLinkStrIntoTuple(linkStr2);
				Tuple<String,String> negative1 = new Tuple<String,String>(tuple1.getFirstElement(), tuple2.getSecondElement());
				Tuple<String,String> negative2 = new Tuple<String,String>(tuple2.getFirstElement(), tuple1.getSecondElement());
				if(!globalReferenceLinks.contains(transformIntoStrLink(negative1.getFirstElement(), negative1.getFirstElement())))
						negativeReferenceLinks.add(negative1);
				if(!globalReferenceLinks.contains(transformIntoStrLink(negative1.getFirstElement(), negative1.getFirstElement())) && negativeReferenceLinks.size() < currentCache.getPositiveReferenceLinks().size())
						negativeReferenceLinks.add(negative2);
			}
		
			currentCache.setNegativeReferenceLinks(negativeReferenceLinks);
		}
	}

	private Set<Tuple<String,String>> generateNegativeLinksFromPositiveLinks(Set<Tuple<String,String>> positivereferenceLinks){
		Set<Tuple<String,String>> negativeReferenceLinks = Sets.newHashSet();
		Set<String> sourceIRIs = positivereferenceLinks.stream().map(tuple -> tuple.getFirstElement()).collect(Collectors.toSet());
		Set<String> targetIRIs = positivereferenceLinks.stream().map(tuple -> tuple.getSecondElement()).collect(Collectors.toSet());
		@SuppressWarnings("unchecked")
		Set<List<String>> mixedIRIs = Sets.cartesianProduct(sourceIRIs, targetIRIs); 
		for(List<String> negativeLinks:mixedIRIs ) {
			String linkStr = transformIntoStrLink(negativeLinks.get(0), negativeLinks.get(1));
			if(!this.globalReferenceLinks.contains(linkStr))
				negativeReferenceLinks.add(transformLinkStrIntoTuple(linkStr));
			if(negativeReferenceLinks.size()==positivereferenceLinks.size())
				break;
		}
		return negativeReferenceLinks;
	}
	
	
	private void loadInstancesIntoCaches() {
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(this.subDatasets.size(), 50));
		List<Callable<Integer>> futures = Lists.newArrayList();
		for(KFoldCache subDataset:this.subDatasets) {
			
			Callable<Integer> task = () -> {
				fillCacheWithInstances(subDataset, this.environment);
				return 1;
			};
			futures.add(task);
			
		}
		// Invoke tasks
		try {
			executor.invokeAll(futures).stream().forEach(future -> {
				try {
					future.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Shutdown executor
		executor.shutdown();
		
	}
	
	
	private void fillCacheWithInstances(KFoldCache subDataset, IEnvironment environment) {
		Set<String> sourceIRIs = Sets.newHashSet();
		Set<String> targetIRIs = Sets.newHashSet();

		// Retrieve source and target IRIs from cache reference links
		Set<Tuple<String,String>> positiveInstances = subDataset.getPositiveReferenceLinks();
		Set<Tuple<String,String>> negativeInstances = subDataset.getNegativeReferenceLinks();
		positiveInstances.forEach(iris -> sourceIRIs.add(iris.getFirstElement()));
		negativeInstances.forEach(iris -> sourceIRIs.add(iris.getFirstElement()));
		positiveInstances.forEach(iris -> targetIRIs.add(iris.getSecondElement()));
		negativeInstances.forEach(iris -> targetIRIs.add(iris.getSecondElement()));

		// Retrieve instances from IRIs
		List<SparqlCacheInstance> sourceInstances = fillCacheWithFromTDBInstances(sourceIRIs, environment.getSourceDatasetFile());
		List<SparqlCacheInstance> targetInstances = fillCacheWithFromTDBInstances(targetIRIs, environment.getTargetDatasetFile());
		// Fill cache with retrieved instances 
		subDataset.getSourceCache().addInstances(sourceInstances);
		subDataset.getTargetCache().addInstances(targetInstances);

	}

	private List<SparqlCacheInstance> fillCacheWithFromTDBInstances(Set<String> iris,  String datasetStr) {
		List<SparqlCacheInstance> instances = Lists.newArrayList();
		// Parallelizer objects
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(iris.size(), 100));
		List<Callable<SparqlCacheInstance>> futures = Lists.newArrayList();
	
		// for each Iri we are going to retrieve its attributes
		for (String iri : iris) {
			Callable<SparqlCacheInstance> task = () -> {
				return retrieveInstanceFromDataset(datasetStr, iri);
			};
			futures.add(task);
		}
		// Invoke tasks
		try {
			executor.invokeAll(futures).stream().forEach(future -> {
				try {
					instances.add(future.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Shutdown executor
		executor.shutdown();
		
		
		return instances;
	}
	
	private SparqlCacheInstance retrieveInstanceFromDataset(String datasetStr, String instanceIRI) {
		// opening database
		Dataset dataset = TDBFactory.createDataset(datasetStr);
		dataset.begin(ReadWrite.READ);
		// Adapt query to specific instance
		String queryStr = "SELECT ?p ?o {  ?s ?p ?o. }";
		queryStr = queryStr.replace("?s", SPARQLFactory.fixIRIS(instanceIRI));
		SparqlCacheInstance instance = new SparqlCacheInstance();
		instance.setId(instanceIRI);
		try {
			// Execute query
			Query query = QueryFactory.create(queryStr);
			QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
			ResultSet results = qexec.execSelect();
			// Retrieve and process results in parallel
			while(results.hasNext()) {
				QuerySolution solution = results.next();
				if (solution != null && solution.contains("?p") && solution.contains("?o")) {
					instance.addAttribute(solution.get("?p").toString(), solution.get("o").toString());
				}
			}
			qexec.close();
		} catch (Exception e) {
			System.out.println("Error with: "+queryStr);
			e.printStackTrace();
		}
		dataset.end();
		return instance;
	}

	private String transformIntoStrLink(String iriSource, String iriTarget) {
		StringBuffer link = new StringBuffer();
		link.append("<").append(iriSource).append("><http://www.w3.org/2002/07/owl#sameAs><").append(iriTarget).append(">.");
		return link.toString();
	}
	
	private Tuple<String,String> transformLinkStrIntoTuple(String linkStr) {
		String[] links = linkStr.split("<http://www.w3.org/2002/07/owl#sameAs>");
		String linkSource = links[0].replaceAll("[<>]+", "").trim();
		if(linkSource.endsWith("."))
			linkSource = linkSource.substring(0, linkSource.lastIndexOf("."));
		String linkTarget = links[1].replaceAll("[<>]+", "").trim();
		if(linkTarget.endsWith("."))
			linkTarget = linkTarget.substring(0, linkTarget.lastIndexOf("."));
		Tuple<String,String> link = new Tuple<String,String>(linkSource, linkTarget);
		return link;
	}
	
	
	
	
	
	
	
	
	
	/*
	 * 
	 */
	
	@Override
	public ConfusionMatrix evaluate(Object object) {
		@SuppressWarnings("unchecked")
		Tuple<String,String> specification = (Tuple<String, String>) object;
		Set<Tuple<String, String>> trainingLinks =  linkTrainingDatasetsReferenceLinks(specification);
		Set<Tuple<String, String>> trainingPositiveReferenceLinks = Sets.newHashSet();
		Set<Tuple<String, String>> trainingNegativeReferenceLinks = Sets.newHashSet();	
		this.subDatasets.stream().forEach(dataset -> trainingPositiveReferenceLinks.addAll(dataset.getPositiveReferenceLinks()));
		this.subDatasets.stream().forEach(dataset -> trainingNegativeReferenceLinks.addAll(dataset.getNegativeReferenceLinks()));
		ConfusionMatrix matrix = getMetrics(trainingLinks, trainingPositiveReferenceLinks, trainingNegativeReferenceLinks);
		return matrix;
	}
	
	
	private Set<Tuple<String, String>> linkTrainingDatasetsReferenceLinks(Tuple<String, String> specification) {
		Set<Tuple<String,String>> links = Sets.newHashSet();
		// Parallelize
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(this.subDatasets.size(), 50));
		List<Callable<Set<Tuple<String, String>>>> tasks = Lists.newArrayList();
		// we are going to link the instances in the training subDataset
		List<KFoldCache> trainingDatasets = this.subDatasets.stream()
				.filter(subDataset -> subDataset.getIsTrainingFold()).collect(Collectors.toList());
		for (KFoldCache dataset : trainingDatasets) {
			// For each training dataset we link it
			Callable<Set<Tuple<String, String>>> task = () -> {
				return linkReferenceLinks(specification, dataset);
			};
			tasks.add(task);
		}

		// Invoke tasks
		try {
			List<Future<Set<Tuple<String, String>>>> futures = executor.invokeAll(tasks);
			for (Future<Set<Tuple<String, String>>> future : futures) {
				try {
					Set<Tuple<String, String>> link = future.get();
					if (link != null)
						links.addAll(link);
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
		return links;
	}

	@Override
	public void apply(Object object) {
		@SuppressWarnings("unchecked")
		Tuple<String,String> specification = (Tuple<String, String>) object;
		KFoldCache validation  = this.subDatasets.get(this.currentValidationSubDataset);
		Set<Tuple<String,String>> links = linkReferenceLinks(specification, validation);
		validationMatrix = getMetrics(links, validation.getPositiveReferenceLinks(), validation.getNegativeReferenceLinks());

	}
	
	private Set<Tuple<String,String>> linkReferenceLinks(Tuple<String,String> specification, KFoldCache dataset){
		LinkerKFoldCache linker = new LinkerKFoldCache(dataset);
		linker.linkInstances(specification);
		return linker.getInstancesLinked();
	}
	
	
	@Override
	public ConfusionMatrix getMetrics(Set<Tuple<String,String>> instancesLinked, Set<Tuple<String, String>> positive, Set<Tuple<String, String>> negative){
		ConfusionMatrix metrics = new ConfusionMatrix();
		Integer truePositives = 0;
		Integer falsePositives = 0;
		Integer trueNegatives = negative.size();
		for(Tuple<String,String> irisLinked: instancesLinked){
			if(positive.contains(irisLinked))
				truePositives++;
			if(negative.contains(irisLinked)) {
				falsePositives++;	
				trueNegatives--;
			}
		}
		
		Integer falseNegatives = positive.size()- truePositives;

		metrics.setTruePositives(truePositives);
		metrics.setFalsePositives(falsePositives);
		metrics.setTrueNegatives(trueNegatives);
		metrics.setFalseNegatives(falseNegatives);
		return metrics;
	}

	public ConfusionMatrix getValidationMatrix() {
		return validationMatrix;
	}

	
	
}
