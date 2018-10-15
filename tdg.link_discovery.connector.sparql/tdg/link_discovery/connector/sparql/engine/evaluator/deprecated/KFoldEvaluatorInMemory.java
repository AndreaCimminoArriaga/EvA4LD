package tdg.link_discovery.connector.sparql.engine.evaluator.deprecated;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.Model;

import tdg.link_discovery.connector.sparql.engine.evaluator.linker.deprecated.LinkerInMemoryModel;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.engine.evaluator.AbstractEvaluator;
import tdg.link_discovery.framework.engine.evaluator.linker.ILinker;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.FrameworkUtils;
import tdg.link_discovery.middleware.utils.Utils;

public class KFoldEvaluatorInMemory extends AbstractEvaluator{

	private List<Tuple<String,Model>> datasetsModels;
	private List<List<String>> tdbFoldDatasetsDirectory;
	
	private Integer kFoldSize;
	private Integer currentIndexValidationFold;
	private StringBuffer outputDirectory;
		
	private Set<Tuple<String,String>> positiveSamples, negativeSamples;
	private List<Set<Tuple<String,String>>> positiveSamplesMap, negativeSamplesMap;
	private Boolean samplesLoaded;
	
	private Boolean useNegatives;
	
	
	public KFoldEvaluatorInMemory(IEnvironment environment, Integer kFoldSize, String workingFolderName) {
		super(environment,null);
		this.environment = environment;
		this.kFoldSize = kFoldSize;
		this.currentIndexValidationFold= 0;
		
		datasetsModels = new CopyOnWriteArrayList<Tuple<String,Model>>();
		
		// Init output directory where dumps and TDBs datasets will be lcated
		this.outputDirectory = new StringBuffer();
		this.outputDirectory.append(FrameworkConfiguration.FRAMEWORK_WORKSPACE_DIRECTORY).append("/results/");
		this.outputDirectory.append(this.environment.getName()).append("/");
		this.outputDirectory.append(workingFolderName).append("/");
		// Init list of TDB datasets directory names	
		this.tdbFoldDatasetsDirectory = new CopyOnWriteArrayList<List<String>>();
		initTDBDatasetsDirectories();
	
		this.positiveSamples= new CopyOnWriteArraySet<Tuple<String,String>>();
		this.negativeSamples = new CopyOnWriteArraySet<Tuple<String,String>>();
		positiveSamplesMap = new CopyOnWriteArrayList<Set<Tuple<String,String>>>();
		negativeSamplesMap = new CopyOnWriteArrayList<Set<Tuple<String,String>>>();
		this.samplesLoaded = false;
	}
	
	public void initTDBDatasetsDirectories() {
		List<String> tdbKFoldSourceDirectory = initTDBFoldDatasetsDirectory(environment.getSourceDatasetFile());
		List<String> tdbKFoldTargetDirectory = initTDBFoldDatasetsDirectory(environment.getTargetDatasetFile());
		List<String> tdbKFoldReferenceLinksDirectory =initTDBFoldDatasetsDirectory(environment.getGoldStandardFile());
		for(int i=0; i<kFoldSize; i++) {
			List<String> folds = new CopyOnWriteArrayList<String>();
			folds.add(tdbKFoldSourceDirectory.get(i));
			folds.add(tdbKFoldTargetDirectory.get(i));
			folds.add(tdbKFoldReferenceLinksDirectory.get(i));	
			tdbFoldDatasetsDirectory.add(folds);
		}
	}
	
	private List<String> initTDBFoldDatasetsDirectory(String tdbDirectory){
		List<String> results = Lists.newArrayList();
		for(int i=1; i<=kFoldSize; i++) {
			String directory = (new StringBuffer(outputDirectory.toString())).append(getTDBName(tdbDirectory)).append("-kFold_").append(kFoldSize).append("_").append(i).toString();
			results.add(Utils.getAbsoluteSystemPath(directory));
		}
		return results;
	}
	
	private String getTDBName(String tdbDirectory) {
		String currentDirectory = "";
		if(tdbDirectory.contains("\\")) {
			tdbDirectory = tdbDirectory.replace("\\", "/");
			currentDirectory = tdbDirectory.substring(tdbDirectory.lastIndexOf("/")+1, tdbDirectory.length());
		}else if(tdbDirectory.contains("/")){
			currentDirectory = tdbDirectory.substring(tdbDirectory.lastIndexOf("/")+1, tdbDirectory.length());
		}
		if(currentDirectory.endsWith(".nt"))
			currentDirectory = currentDirectory.substring(0, currentDirectory.lastIndexOf(".nt"));
		
		
		return currentDirectory;
	}
	
	public void foldDatasets(Boolean useNegatives, Boolean balancedReferenceLinks, Boolean useNumberOfNegativeLinks, Integer numberOfNegativeLinks, Integer maxSampleSize) {
		this.useNegatives = useNegatives;
		KFoldDumperInMemory kFoldDumper = new KFoldDumperInMemory(environment, kFoldSize, tdbFoldDatasetsDirectory, outputDirectory.toString());
		kFoldDumper.createFoldInfrastructure(useNegatives, balancedReferenceLinks, useNumberOfNegativeLinks, numberOfNegativeLinks, maxSampleSize);
		loggingStart(useNegatives, balancedReferenceLinks, useNumberOfNegativeLinks, numberOfNegativeLinks);
		datasetsModels = kFoldDumper.getDatasetsModels();
		FrameworkConfiguration.traceLog.setCacheLinesSize(1);
		FrameworkConfiguration.resultsLog.setCacheLinesSize(1);
		// Load reference links
				if(!this.samplesLoaded)
					loadInChacheReferenceLinks();
				this.samplesLoaded= true;
	}
		
	public void foldDatasets(Boolean useNegatives, Boolean balancedReferenceLinks, Boolean useNumberOfNegativeLinks, Integer numberOfNegativeLinks) {
		this.useNegatives = useNegatives;
		KFoldDumperInMemory kFoldDumper = new KFoldDumperInMemory(environment, kFoldSize, tdbFoldDatasetsDirectory, outputDirectory.toString());
		kFoldDumper.createFoldInfrastructure(useNegatives, balancedReferenceLinks, useNumberOfNegativeLinks, numberOfNegativeLinks);
		loggingStart(useNegatives, balancedReferenceLinks, useNumberOfNegativeLinks, numberOfNegativeLinks);
		datasetsModels = kFoldDumper.getDatasetsModels();
		FrameworkConfiguration.traceLog.setCacheLinesSize(1);
		FrameworkConfiguration.resultsLog.setCacheLinesSize(1);
		// Load reference links
				if(!this.samplesLoaded)
					loadInChacheReferenceLinks();
				this.samplesLoaded= true;
	}
	
	public void foldDatasetsWithNoNegativeReferenceLinks() {
		this.useNegatives = false;
		KFoldDumperInMemory kFoldDumper = new KFoldDumperInMemory(environment, kFoldSize, tdbFoldDatasetsDirectory, outputDirectory.toString());
		kFoldDumper.createFoldInfrastructure(useNegatives, false, false, -1);
		loggingStart(useNegatives, false, false, -1);
		datasetsModels = kFoldDumper.getDatasetsModels();
		FrameworkConfiguration.traceLog.setCacheLinesSize(1);
		FrameworkConfiguration.resultsLog.setCacheLinesSize(1);
		// Load reference links
				if(!this.samplesLoaded)
					loadInChacheReferenceLinks();
				this.samplesLoaded= true;
	}
	
	public void foldDatasetsWithFullNegativeReferenceLinks() {
		this.useNegatives = true;
		KFoldDumperInMemory kFoldDumper = new KFoldDumperInMemory(environment, kFoldSize, tdbFoldDatasetsDirectory, outputDirectory.toString());
		kFoldDumper.createFoldInfrastructure(useNegatives, false, false, -1);
		loggingStart(useNegatives, false, false, -1);
		datasetsModels = kFoldDumper.getDatasetsModels();
		FrameworkConfiguration.traceLog.setCacheLinesSize(1);
		FrameworkConfiguration.resultsLog.setCacheLinesSize(1);
		// Load reference links
				if(!this.samplesLoaded)
					loadInChacheReferenceLinks();
				this.samplesLoaded= true;
	}
	
	public void foldDatasetsWithBalancedReferenceLinks() {
		this.useNegatives = true;
		KFoldDumperInMemory kFoldDumper = new KFoldDumperInMemory(environment, kFoldSize, tdbFoldDatasetsDirectory, outputDirectory.toString());
		kFoldDumper.createFoldInfrastructure(useNegatives, true, false, -1);
		loggingStart(useNegatives, true, false, -1);
		datasetsModels = kFoldDumper.getDatasetsModels();
		FrameworkConfiguration.traceLog.setCacheLinesSize(1);
		FrameworkConfiguration.resultsLog.setCacheLinesSize(1);
		// Load reference links
				if(!this.samplesLoaded)
					loadInChacheReferenceLinks();
				this.samplesLoaded= true;
	}
	
	public void foldDatasetsWithSpecificNumberOfReferenceLinks(Integer numberOfNegativeLinks) {
		this.useNegatives = true;
		KFoldDumperInMemory kFoldDumper = new KFoldDumperInMemory(environment, kFoldSize, tdbFoldDatasetsDirectory, outputDirectory.toString());
		kFoldDumper.createFoldInfrastructure(useNegatives, false, true, numberOfNegativeLinks);
		loggingStart(useNegatives, false, true, numberOfNegativeLinks);
		datasetsModels = kFoldDumper.getDatasetsModels();
		FrameworkConfiguration.traceLog.setCacheLinesSize(1);
		FrameworkConfiguration.resultsLog.setCacheLinesSize(1);
		// Load reference links
				if(!this.samplesLoaded)
					loadInChacheReferenceLinks();
				this.samplesLoaded= true;
	}
	
	private void loggingStart(Boolean useNegatives, Boolean balancedReferenceLinks, Boolean useNumberOfNegativeLinks,Integer numberOfNegativeLinks) {
		List<List<String>> tdbs = Lists.newArrayList();
		tdbs.addAll(tdbFoldDatasetsDirectory);
		tdbs.remove(this.tdbFoldDatasetsDirectory.get(currentIndexValidationFold));
		// Logging stuff
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "--- Starting kfolding");
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Folding configuration:");
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(),
				"\t use negative links:" + useNegatives);
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(),
				"\t balanced reference links:" + balancedReferenceLinks);
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(),
				"\t use number of negative links:" + useNumberOfNegativeLinks);
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(),
				"\t number of negative links:" + numberOfNegativeLinks);
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Training datasets:");
		tdbs.forEach(tdb -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t " + tdb));
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Validation datasets:");
		this.tdbFoldDatasetsDirectory.get(currentIndexValidationFold).forEach(
				tdb -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t " + tdb));
	}


	public Boolean changeValidationDataset() {
		Boolean correctlyChanged = false;
		if(currentIndexValidationFold+1<kFoldSize) {
			currentIndexValidationFold++;
			List<List<String>> tdbs = Lists.newArrayList();
			tdbs.addAll(tdbFoldDatasetsDirectory);
			tdbs.remove(this.tdbFoldDatasetsDirectory.get(currentIndexValidationFold));
			this.samplesLoaded = false;
			correctlyChanged=true;
			// logging stuff
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Changing validation and training datasets");
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "New training datasets:");
			tdbs.forEach(tdb -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t "+tdb));
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "New validation datasets:");
			this.tdbFoldDatasetsDirectory.get(currentIndexValidationFold).forEach(tdb -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t "+tdb));
			
		}
		return correctlyChanged;
	}
	
	
	

	private Set<Tuple<String,String>> instancesLinked = new CopyOnWriteArraySet<Tuple<String,String>>();
	@SuppressWarnings("unchecked")
	@Override
	public ConfusionMatrix evaluate(Object object) {
		Tuple<String, String> queries = (Tuple<String, String>) object;
		// Create concurrent data files
		instancesLinked = new CopyOnWriteArraySet<Tuple<String,String>>();
		// Load reference links
		if(!this.samplesLoaded)
			loadInChacheReferenceLinks();
		this.samplesLoaded= true;
		
		// link different folded datasets
		List<List<String>> trainingSets = new CopyOnWriteArrayList<List<String>>();
		trainingSets.addAll(this.tdbFoldDatasetsDirectory);
		trainingSets.remove(tdbFoldDatasetsDirectory.get(currentIndexValidationFold));
		trainingSets.parallelStream().forEach(tdbs -> linkSubDatasets(tdbs.get(0), tdbs.get(1), trainingSets.indexOf(tdbs), queries));
		
		ConfusionMatrix matrix = getMetrics(instancesLinked, positiveSamples, negativeSamples);
		
		//SPARQLFactory.usedVariables.clear(); // cleans the cache of used variables in the generated queries to free
												// memory
		return matrix;
	}
	
	private void linkSubDatasets(String sourceTDB, String targetTDB, Integer current_fold, Tuple<String,String> queries) {
		ILinker linker = new LinkerInMemoryModel();		
		linker.setDatasetSource(findModelByName(sourceTDB));
		linker.setDatasetTarget(findModelByName(targetTDB));
		linker.setDatasetSource(sourceTDB+".nt");
		linker.setDatasetTarget(targetTDB+".nt");
		linker.setInstances(Sets.union(positiveSamplesMap.get(current_fold), negativeSamplesMap.get(current_fold)));
		//linker.linkDatasets(queries,"");
		linker.linkInstances(queries);
		Set<Tuple<String, String>> linked = linker.getInstancesLinked();		
		instancesLinked.addAll(linked);	
	}
	
	
	private void loadInChacheReferenceLinks() {
		instancesLinked = new CopyOnWriteArraySet<Tuple<String,String>>();
		// Init with only current training datasets
		List<List<String>> trainingSets = new CopyOnWriteArrayList<List<String>>();
		trainingSets.addAll(this.tdbFoldDatasetsDirectory);
		trainingSets.remove(tdbFoldDatasetsDirectory.get(currentIndexValidationFold));
		trainingSets.stream().forEach(tdbs -> loadReferenceLinksFromDatasets(tdbs.get(2), trainingSets.indexOf(tdbs)));
	}


	private void loadReferenceLinksFromDatasets(String referenceLinksTDB, Integer current_fold) {
		Set<Tuple<String,String>> positiveReferenceLinks = readReferenceLinks(referenceLinksTDB, true);
		positiveSamples.addAll(positiveReferenceLinks);
		positiveSamplesMap.add(current_fold, positiveSamples);
		// Logging stuff
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Retrieving reference links from"+referenceLinksTDB);
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t positive links retrieved: "+positiveReferenceLinks.size());
		// ---
		if(useNegatives) {
				Set<Tuple<String,String>> negativeReferenceLinks = readReferenceLinks(referenceLinksTDB, false);
				negativeSamples.addAll(negativeReferenceLinks);
				FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t negative links retrieved: "+negativeReferenceLinks.size());
				negativeSamplesMap.add(current_fold, negativeSamples);
			}else {
				negativeSamplesMap.add(current_fold, Sets.newHashSet());
			}
		
	}

	private Set<Tuple<String, String>> readReferenceLinks(String referenceLinksTDB, Boolean readPositives) {
		Set<Tuple<String, String>> referenceLinks = Sets.newHashSet();
		String predicateD = "http://www.w3.org/2002/07/owl#differentFrom";
		String predicateS = "http://www.w3.org/2002/07/owl#sameAs";
		
		List<String> goldStd = FrameworkUtils.readGoldLinks(referenceLinksTDB+".nt");
		if(readPositives)
			referenceLinks = goldStd.stream().filter(line -> line.contains(predicateS)).map(line -> fromLineToTuple(line, predicateS)).collect(Collectors.toSet());
		if(!readPositives)
			referenceLinks = goldStd.stream().filter(line -> line.contains(predicateD)).map(line -> fromLineToTuple(line, predicateD)).collect(Collectors.toSet());
		
		return referenceLinks;
	}

	private Tuple<String,String> fromLineToTuple(String line, String predicate){
		String[] lineToSplit = line.replace("<", "").replace(">.", "").replace(">", "").split(predicate);
		return new Tuple<String,String>(lineToSplit[0],lineToSplit[1]);
	}


	private ConfusionMatrix validationMatrix = new ConfusionMatrix();
	

	@SuppressWarnings("unchecked")
	@Override
	public void apply(Object object) {
		Tuple<String, String> queries = (Tuple<String, String>) object;
		// Retrieve validation datasets
		List<String> tdbs = this.tdbFoldDatasetsDirectory.get(currentIndexValidationFold);
		String sourceTDB = tdbs.get(0);
		String targetTDB = tdbs.get(1);
		String referenceLinksTDB = tdbs.get(2);
		// Create linker
		ILinker linker = new LinkerInMemoryModel();		
		linker.setDatasetSource(findModelByName(sourceTDB));
		linker.setDatasetSource(sourceTDB+".nt");
		linker.setDatasetTarget(findModelByName(targetTDB));
		linker.setDatasetTarget(targetTDB+".nt");
		linker.linkDatasets(queries, environment.getLinksOutput());
		validationMatrix = getMetrics(linker.getInstancesLinked(), readReferenceLinks(referenceLinksTDB, true), readReferenceLinks(referenceLinksTDB, false));
		//SPARQLFactory.usedVariables.clear(); // cleans the cache of used variables in the generated queries to free
												// memory
	}
	
	private Model findModelByName(String name) {
		return this.datasetsModels.stream().filter(tuple -> tuple.getFirstElement().equals(name)).map(tuple -> tuple.getSecondElement()).collect(Collectors.toList()).get(0);
	}
	
	
	public ConfusionMatrix getValidationMatrix() {
		return validationMatrix;
	}

	public StringBuffer getOutputDirectory() {
		return outputDirectory;
	}
	
	

	@Override
	public ConfusionMatrix getMetrics(Set<Tuple<String,String>> instancesLinked, Set<Tuple<String, String>> positive, Set<Tuple<String, String>> negative){
		ConfusionMatrix metrics = new ConfusionMatrix();
		Integer truePositives = 0;
		Integer falsePositives = 0;
		//Integer trueNegatives = negative.size();
		for(Tuple<String,String> irisLinked: instancesLinked){
			if(positive.contains(irisLinked))
				truePositives++;
			if(negative.contains(irisLinked)) {
				falsePositives++;	
				//trueNegatives--;
			}
			if(!negative.isEmpty() && !positive.contains(irisLinked)) {
				falsePositives++;
			}
		}
		
		
		Integer falseNegatives = Math.abs(positive.size()- truePositives);

		Integer trueNegatives = (negative.size()) - falsePositives;
		if(negative.isEmpty() || trueNegatives<0)
			trueNegatives = -1;
		metrics.setTruePositives(truePositives);
		metrics.setFalsePositives(falsePositives);
		metrics.setTrueNegatives(trueNegatives);
		metrics.setFalseNegatives(falseNegatives);
		return metrics;
	}

	
	public List<List<String>> getTrainingModels() {
		List<List<String>> trainingSets = new CopyOnWriteArrayList<List<String>>();
		trainingSets.addAll(this.tdbFoldDatasetsDirectory);
		trainingSets.remove(tdbFoldDatasetsDirectory.get(currentIndexValidationFold));
		return trainingSets;
	}
	
	public Set<Tuple<String, String>> getPositiveInstances() {
		return positiveSamples;
	}
}
