package tdg.link_discovery.connector.sparql.engine.evaluator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Sets;
import com.google.common.collect.Lists;

import tdg.link_discovery.connector.sparql.engine.evaluator.linker.LinkerKFoldCacheTDB;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.engine.evaluator.AbstractEvaluator;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.FrameworkUtils;
import tdg.link_discovery.middleware.utils.Utils;

public class KFoldEvaluator extends AbstractEvaluator{

	private List<KFoldEvaluatorCache> subDatasets;
	private Integer currentValidationSubDataset;
	private List<String> globalReferenceLinks;
	private ConfusionMatrix validationMatrix;
	public static int DEFAULT_THREAD_POOL_SIZE = 10;
	
	public KFoldEvaluator(IEnvironment environment, Integer kFoldSize, String workingFolderName, Integer referenceLinksSize) {
		super(environment,null);
		if (kFoldSize > referenceLinksSize)
			throw new IllegalArgumentException("'referenceLinksSize' parameter cannot be lower than 'kFoldSize'  (Clazz KFoldCacheEvaluator)");
		
		// init subdatasets
		initializeSubDatasets(kFoldSize);
		// retrieve positivesReferenceLinks
		Set<Tuple<String,String>> positiveReferenceLinks = retrieveReferenceLinks(referenceLinksSize, environment);
		// sparse positive reference links into subDatasets
		sparsePositiveReferenceLinks(positiveReferenceLinks);
		// generate negative links
		generateNegativeReferenceLinks();
		// persist caches into file
		String outputDirectory = initFileSystem(workingFolderName);
		persistCaches(outputDirectory);
		loggingStart(kFoldSize, referenceLinksSize,  outputDirectory);
	}
	
	private String initFileSystem(String workingFolderName) {
		// Init output directory where dumps and TDBs datasets will be lcated
		StringBuffer outputDirectory = new StringBuffer();
		outputDirectory.append(FrameworkConfiguration.FRAMEWORK_WORKSPACE_DIRECTORY).append("/results/");
		outputDirectory.append(this.environment.getName()).append("/");
		outputDirectory.append(workingFolderName).append("/");
		StringBuffer incrementalDirectory = new StringBuffer();
		try {
			String directory = outputDirectory.toString();
			String[] subDirectories = null;
			Boolean isMicrosoft = false;
			if(directory.contains("/")) {
				subDirectories= directory.split("/");
			}else {
				subDirectories = directory.replace("\\","/").split("/");
				isMicrosoft = true;
			}
			Integer subDirectoryCounter = 0;
			
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t Creating tree of directories");
			while(subDirectoryCounter < subDirectories.length) {
				if(!isMicrosoft) {
					incrementalDirectory.append(subDirectories[subDirectoryCounter]).append("/");
				}else {
					incrementalDirectory.append(subDirectories[subDirectoryCounter]).append("\\");
				}
				Boolean newFile = new File(Utils.getAbsoluteSystemPath(incrementalDirectory.toString())).mkdirs();
				FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t "+incrementalDirectory+" = "+newFile);
				subDirectoryCounter++;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		FrameworkConfiguration.traceLog.setCacheLinesSize(1);
		FrameworkConfiguration.resultsLog.setCacheLinesSize(1);
		return incrementalDirectory.toString();
	}
	
	private void loggingStart(Integer kfoldSize, Integer referenceLinksSize, String workingDirectory) {
		
		// Logging stuff
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "--- Starting kfolder evaluator");
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Folding configuration:");
		// Logging stuff
				
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(),
				"\t balanced reference links: true");
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(),
				"\t number of positive links per subDataset: " + this.subDatasets.get(0).getPositiveReferenceLinks().size());
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(),
				"\t number of negative links per subDataset: " + this.subDatasets.get(0).getNegativeReferenceLinks().size());
		
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Training datasets:");
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t"+this.subDatasets.stream().map(subD -> subD.isTrainingFold).collect(Collectors.toList()));
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Validation dataset index:");
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t"+this.currentValidationSubDataset);
	}


	public Boolean changeValidationDataset() {
		Boolean correctlyChanged = true;
		this.subDatasets.get(this.currentValidationSubDataset).setIsTrainingFold(true);
		this.currentValidationSubDataset--;
		if(this.currentValidationSubDataset<0) {
			correctlyChanged = false;
		}else {
			this.subDatasets.get(this.currentValidationSubDataset).setIsTrainingFold(false);
		}
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Changing validation dataset: "+ correctlyChanged);
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "New training datasets:");
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t"+this.subDatasets.stream().map(subD -> subD.isTrainingFold).collect(Collectors.toList()));
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "New validation dataset index:");
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t"+this.currentValidationSubDataset);
		return correctlyChanged;
	}
	
	

	private void initializeSubDatasets(Integer kFoldSize) {
		subDatasets = Lists.newCopyOnWriteArrayList();
		// Create kFoldSize sub-datasets
		for(int i=0;i<kFoldSize; i++) {
			KFoldEvaluatorCache subDataset = new KFoldEvaluatorCache();
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
			// do not add repeated links
			if(!positiveReferenceLinks.contains(link))
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
		for(KFoldEvaluatorCache subDatasetIndex:this.subDatasets) {
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
		for(KFoldEvaluatorCache currentCache:this.subDatasets) {
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
	 * Persisting methods
	 */

	private void persistCaches(String outputDirectory) {
		Integer threadPool = this.subDatasets.size();
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(threadPool, DEFAULT_THREAD_POOL_SIZE ));
		// We retrieve for each cache the writting function of its reference links
		List<Callable<Integer>> tasks = Lists.newArrayList();
		for(KFoldEvaluatorCache cache: this.subDatasets) {
			Callable<Integer> task = () -> {
				return writeCache(cache, outputDirectory, subDatasets.indexOf(cache));
			};
			tasks.add(task);
		}
		// Invoke tasks
		try {
			List<Future<Integer>> futures = executor.invokeAll(tasks);
			for(Future<Integer> future: futures) {
				try {
					future.get();
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

	private int writeCache(KFoldEvaluatorCache cache, String directory, Integer index) {
		Set<Tuple<String,String>> positiveReferenceLinks = cache.getPositiveReferenceLinks();
		Set<Tuple<String,String>> negativeReferenceLinks = cache.getNegativeReferenceLinks();
		// Build the output directory
		StringBuffer outputFile = new StringBuffer(directory);
		if(directory.contains("/") && !directory.endsWith("/"))
			outputFile.append("/");
		if(directory.contains("\\") && !directory.endsWith("\\"))
			outputFile.append("\\");
		outputFile.append("subDatasetFolded_").append(index).append(".nt");
		// Build file content, i.e., the positive and the negative reference links
		StringBuffer content = new StringBuffer();
		positiveReferenceLinks.forEach(tuple -> content.append(fromTupleToStringLink(tuple, " <http://www.w3.org/2002/07/owl#sameAs> ")));
		negativeReferenceLinks.forEach(tuple -> content.append(fromTupleToStringLink(tuple, " <http://www.w3.org/2002/07/owl#differentFrom> ")));
		// Write content into output directory
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter(outputFile.toString()));
		    writer.write(content.toString());
		    if ( writer != null)
		        writer.close( );
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	private String fromTupleToStringLink(Tuple<String,String> tuple, String predicate) {
		StringBuffer content = new StringBuffer();
		content.append(SPARQLFactory.fixIRIS(tuple.getFirstElement()));
		content.append(predicate);
		content.append(SPARQLFactory.fixIRIS(tuple.getSecondElement())).append(".\n");
		return content.toString();
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
		this.subDatasets.stream().filter(sub -> this.subDatasets.indexOf(sub)!=currentValidationSubDataset).forEach(dataset -> trainingPositiveReferenceLinks.addAll(dataset.getPositiveReferenceLinks()));
		this.subDatasets.stream().filter(sub -> this.subDatasets.indexOf(sub)!=currentValidationSubDataset).forEach(dataset -> trainingNegativeReferenceLinks.addAll(dataset.getNegativeReferenceLinks()));
		ConfusionMatrix matrix = getMetrics(trainingLinks, trainingPositiveReferenceLinks, trainingNegativeReferenceLinks);
		
		return matrix;
	}
	
	
	private Set<Tuple<String, String>> linkTrainingDatasetsReferenceLinks(Tuple<String, String> specification) {
		Set<Tuple<String,String>> links = Sets.newHashSet();
		// Parallelize
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(this.subDatasets.size(), 50));
		List<Callable<Set<Tuple<String, String>>>> tasks = Lists.newArrayList();
		// we are going to link the instances in the training subDataset
		List<KFoldEvaluatorCache> trainingDatasets = this.subDatasets.stream()
				.filter(subDataset -> subDataset.getIsTrainingFold()).collect(Collectors.toList());
		for (KFoldEvaluatorCache dataset : trainingDatasets) {
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
		KFoldEvaluatorCache validation  = this.subDatasets.get(this.currentValidationSubDataset);
		Set<Tuple<String,String>> links = linkReferenceLinks(specification, validation);
		
		validationMatrix = getMetrics(links, validation.getPositiveReferenceLinks(), validation.getNegativeReferenceLinks());
		
		
	}
	

	private Set<Tuple<String,String>> linkReferenceLinks(Tuple<String,String> specification, KFoldEvaluatorCache dataset){
		LinkerKFoldCacheTDB linker = new LinkerKFoldCacheTDB();
		linker.setDatasetSource(dataset);
		linker.setDatasetSource(this.environment.getSourceDatasetFile());
		linker.setDatasetTarget(this.environment.getTargetDatasetFile());
		linker.linkInstances(specification);
		return linker.getInstancesLinked();
	}
	
	
	@Override
	public ConfusionMatrix getMetrics(Set<Tuple<String,String>> instancesLinked, Set<Tuple<String, String>> positive, Set<Tuple<String, String>> negative){
		ConfusionMatrix metrics = new ConfusionMatrix();
		Integer truePositives = 0;
		Integer falsePositives = 0;
		Integer trueNegatives = negative.size();
		
		Set<Tuple<String,String>> tp = new HashSet<Tuple<String,String>>();
		for(Tuple<String,String> irisLinked: instancesLinked){
			if(positive.contains(irisLinked)) {
				truePositives++;
				tp.add(irisLinked);
			}
			if(negative.contains(irisLinked)) {
				falsePositives++;	
				trueNegatives--;
			}
		}
		
		//
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

	/*
	 * Getters and setters
	 */
	
	public Set<Tuple<String,String>> getTrainingPositiveReferenceLinks(){
		Set<Tuple<String,String>> positiveReferenceLinks = Sets.newHashSet();
		this.subDatasets.stream().filter(subDataset -> subDataset.isTrainingFold).forEach(training -> positiveReferenceLinks.addAll(training.getPositiveReferenceLinks()));
		return positiveReferenceLinks;
	}
	
	public Set<Tuple<String,String>> getTrainingNegativeReferenceLinks(){
		Set<Tuple<String,String>> negativeReferenceLinks = Sets.newHashSet();
		this.subDatasets.stream().filter(subDataset -> subDataset.isTrainingFold).forEach(training -> negativeReferenceLinks.addAll(training.getNegativeReferenceLinks()));
		return negativeReferenceLinks;
	}
	
	public Set<Tuple<String,String>> getValidationPositiveReferenceLinks(){
		Set<Tuple<String,String>> positiveReferenceLinks = Sets.newHashSet();
		this.subDatasets.stream().filter(subDataset -> !subDataset.isTrainingFold).forEach(training -> positiveReferenceLinks.addAll(training.getPositiveReferenceLinks()));
		return positiveReferenceLinks;
	}
	
	public Set<Tuple<String,String>> getValidationNegativeReferenceLinks(){
		Set<Tuple<String,String>> negativeReferenceLinks = Sets.newHashSet();
		this.subDatasets.stream().filter(subDataset -> !subDataset.isTrainingFold).forEach(training -> negativeReferenceLinks.addAll(training.getNegativeReferenceLinks()));
		return negativeReferenceLinks;
	}
}
