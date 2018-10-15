package tdg.link_discovery.connector.sparql.engine.evaluator.deprecated;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.FrameworkUtils;
import tdg.link_discovery.middleware.utils.StreamUtils;
import tdg.link_discovery.middleware.utils.Utils;

public class KFoldDumper {

	private IEnvironment environment;
	private Integer kFoldSize;
	
	private List<List<String>> tdbFoldDatasetsDirectory;
	private Boolean includeNegativeLinks, balancedReferenceLinks, useNumberOfNegatives;
	private Integer numberOfNegatives;
	
	private StringBuffer outputDirectory;
	
	public KFoldDumper(IEnvironment environment, Integer kFoldSize, List<List<String>> tdbDatasetsDirectory, String outputDirectory){
		// init parameters
		this.environment = environment;
		this.kFoldSize = kFoldSize;
		
		this.outputDirectory = new StringBuffer(outputDirectory);
		// Init tdb directories in concurrent list
		tdbFoldDatasetsDirectory = new CopyOnWriteArrayList<List<String>>();
		tdbFoldDatasetsDirectory.addAll(tdbDatasetsDirectory);
	
		
	}
	public void createFoldInfrastructure(Boolean includeNegativeLinks, Boolean balancedReferenceLinks, Boolean useNumberOfNegatives, Integer numberOfNegatives) {
		this.includeNegativeLinks = includeNegativeLinks;
		this.balancedReferenceLinks = balancedReferenceLinks;
		this.useNumberOfNegatives = useNumberOfNegatives;
		this.numberOfNegatives = numberOfNegatives;
		// Create k-fold TDB datasets for source and target datasets plus a reference links
		initFileSystem();
		createSubDatasetsPartitions();
	}

	
	
	private void initFileSystem() {
		try {
			String directory = this.tdbFoldDatasetsDirectory.get(0).get(0);
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
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t Creating tree of directories");
			while(subDirectoryCounter < subDirectories.length-1) {
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
	}
	

	
	// Create k-fold if not exists
	private void createSubDatasetsPartitions() {
		// Check TDB datasets already exist
		if(!kFoldAlreadyExists()) {
			// Retrieve all reference links, positive and negative
			Set<Tuple<String,String>> goldLinks = retrieveGoldStdLinks();
			Set<Tuple<String,String>> negativeLinks = Sets.newHashSet();
			if(includeNegativeLinks)
				negativeLinks = retrieveNegativeLinks(goldLinks);
		
			//Dispense links through the dumps, then release memory space
			splitLinksInKFoldModelsFromSet(goldLinks, true);
			goldLinks.clear();
			splitLinksInKFoldModelsFromSet(negativeLinks, false);	
			negativeLinks.clear();
			// Persist models and create reference links database
			loadDumpsIntoTDBDatasets();
		}
	}


	private boolean kFoldAlreadyExists() {
		File directory = new File(this.outputDirectory.toString());
		Boolean isCreated = false;
		if(directory.isDirectory()) {
			isCreated = directory.listFiles().length>0;
		}
		
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t k-folds already exists: "+isCreated);
		return isCreated;
	}


	private Set<Tuple<String,String>> retrieveGoldStdLinks(){
		String goldStandardFile = this.environment.getGoldStandardFile();
		Set<Tuple<String, String>> goldLinks = FrameworkUtils.readGoldLinks(goldStandardFile).stream()
				.parallel()
				.map(line -> new Tuple<String,String>(line.split("><http://www.w3.org/2002/07/owl#sameAs><")[0].replace("<",""),
													 line.split("><http://www.w3.org/2002/07/owl#sameAs><")[1].replace(">.","")))
				.collect(Collectors.toSet());
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t gold links retrieved: "+goldLinks.size());
		return goldLinks;
	}
	
	
	private Set<Tuple<String,String>> retrieveNegativeLinks(Set<Tuple<String,String>> goldLinks){
		List<String> sourceIris = retrieveIrisTosplit(this.environment.getSourceDatasetFile(), environment.getSourceRestrictions());
		List<String> targetIris = retrieveIrisTosplit(this.environment.getTargetDatasetFile(), environment.getTargetRestrictions());
		Set<Tuple<String,String>> irisLinked = Sets.newHashSet();
		
		if(balancedReferenceLinks && !useNumberOfNegatives)
			numberOfNegatives = goldLinks.size();
		if(!balancedReferenceLinks && !useNumberOfNegatives)
			numberOfNegatives = sourceIris.size()*targetIris.size();
		
		while(irisLinked.size()-1<= numberOfNegatives) {
			String iriS = sourceIris.get(Utils.getRandomInteger(sourceIris.size()-1, 0));
			String iriT = targetIris.get(Utils.getRandomInteger(targetIris.size()-1, 0));
			Tuple<String,String> newNegativeLink = new Tuple<String,String>(iriS, iriT);
			if(!goldLinks.contains(newNegativeLink))
				irisLinked.add(newNegativeLink);
		}
		
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t negative links generated: "+irisLinked.size());
		return irisLinked;
	}
	
		
	private List<String> retrieveIrisTosplit(String tdbDirectory, List<String> typeRestrictions) {
		List<String> iris = Lists.newArrayList();
		Dataset dataset =  TDBFactory.createDataset(Utils.getAbsoluteSystemPath((tdbDirectory)));
		dataset.begin(ReadWrite.READ);
		String iriVar =  "?iri";
		String sparql = generateTypesEnmbededQuery(iriVar, typeRestrictions);
		Query qry = QueryFactory.create(sparql);
		QueryExecution qe = QueryExecutionFactory.create(qry, dataset);
		ResultSet rs = qe.execSelect();
		try {
			StreamUtils.asStream(rs).forEach(result -> iris.add(result.get(iriVar).asResource().toString()));
		}catch(Exception e) {
			System.out.println("Error retrieving instances from "+tdbDirectory+" that hold retrictions: "+typeRestrictions);
			System.out.println("Error in query: \n"+sparql);
		}
		qe.close();
		dataset.commit();
		dataset.end();
		return iris;
	}

	private String generateTypesEnmbededQuery(String variable, List<String> typeRestrictions){
		 StringBuffer sparql = new StringBuffer();
		 sparql.append("SELECT DISTINCT ").append(variable).append(" WHERE { ");
		 for(String type:typeRestrictions)
			 sparql.append(variable).append(" <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <").append(type).append("> . ");
		 sparql.append("} ");
		 return sparql.toString();
	}
	
	private List<Tuple<String,String>> linksToSplit;
	private void splitLinksInKFoldModelsFromSet(Set<Tuple<String,String>> links, Boolean linksArePositive) {
		// Copy links set to a concurrent list
		linksToSplit = new CopyOnWriteArrayList<Tuple<String,String>>();
		linksToSplit.addAll(links);
		links.clear();
				
		// Dispense instances in each kfold dataset stored in the ouput files
		tdbFoldDatasetsDirectory.parallelStream().forEach(modelTriple -> fillCurrentFoldWithInstances( modelTriple.get(0), modelTriple.get(1), modelTriple.get(2), linksArePositive));
		linksToSplit.clear();
	}
	
	private void fillCurrentFoldWithInstances(String sourceModelName, String targetModelTarget, String referenceLinksModelName, Boolean linksArePositive) {
		Integer numberOfInstancesInFold = linksToSplit.size()/this.kFoldSize;	//TODO: WHAT APPENDS IF FOLD SIZE IS ODD
		Set<String> sourceIris = Sets.newHashSet();
		Set<String> targetIris = Sets.newHashSet();
		// TODO: instead of writting the iris one by one, write them in chunks in the file
		for( int instances_added=0; instances_added <numberOfInstancesInFold;instances_added++) {
			// Pick random pair of instances
			Tuple<String,String> randomTuple = null;
			while(randomTuple==null) {
				try {
					Integer randomIndex = Utils.getRandomInteger(linksToSplit.size()-1, 0);
					randomTuple = linksToSplit.get(randomIndex);
				}catch(Exception e) {
					System.out.println("Index stolen by other thread");
				}
			}
			if(!sourceIris.contains(randomTuple.getFirstElement())) {
				addTriplesToModel(this.environment.getSourceDatasetFile(), sourceModelName, randomTuple.getFirstElement());
				sourceIris.add(randomTuple.getFirstElement());
			}
			// Include instances in dumps with all their properties		
			if(!targetIris.contains(randomTuple.getSecondElement())) {
				addTriplesToModel(this.environment.getTargetDatasetFile(), targetModelTarget, randomTuple.getSecondElement());
				targetIris.add(randomTuple.getSecondElement());
			}
			
			addReferenceLinksInstancesInDumpFile(referenceLinksModelName, randomTuple, linksArePositive);
			
			// Removed added to avoid including it in another model
			linksToSplit.remove(randomTuple);
			if(linksToSplit.size()==0)
				break;
		}
		sourceIris.clear();
		targetIris.clear();
		
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t dispensed in current fold: "+numberOfInstancesInFold +" (is_positive="+linksArePositive+")");
	}
	
	private void addReferenceLinksInstancesInDumpFile(String referenceLinksModel, Tuple<String,String> iris, Boolean linksArePositive) {
		String predicateStr = "http://www.w3.org/2002/07/owl#differentFrom";
		if(linksArePositive)
			predicateStr = "http://www.w3.org/2002/07/owl#sameAs";	
		// Create an RDF resource to add
		String line = formatLineForDump(iris.getFirstElement(), predicateStr, iris.getSecondElement());
		addLineToDumpModel(getTDBReferenceLinksDirectory(), referenceLinksModel, line);
	}
		
	private String formatLineForDump(String subject, String predicate, String object) {
		StringBuffer line = new StringBuffer();
		line.append("<").append(subject).append("> ");
		line.append("<").append(predicate).append("> ");
		if(object.startsWith("http://")) {
			line.append("<").append(object).append("> .\n");
		}else {
			line.append("\"").append(object).append("\" .\n");
		}
		return line.toString();
	}
	
	private Boolean error = false;
	private void addLineToDumpModel(String tdbDirectory, String dumpDir, String line) {
		String dumpFile= Utils.getAbsoluteSystemPath(dumpDir)+".nt";
		File newFile = new File(dumpFile);
		
		try {
			if(!newFile.exists())
				newFile.createNewFile();
			Path file = Paths.get(Utils.getAbsoluteSystemPath((dumpFile)));
			Files.write(file, line.getBytes(), StandardOpenOption.APPEND);
		}catch (IOException e) {
			if(!error)
				System.out.println("File  '"+ dumpDir+"'     -> Not created");
			error = true;
		}
	}

	private void addTriplesToModel(String tdbDirectory, String modelName, String iri) {
		Dataset dataset =  TDBFactory.createDataset(Utils.getAbsoluteSystemPath(tdbDirectory));
		dataset.begin(ReadWrite.READ);
		String sparql = "SELECT DISTINCT ?p ?o WHERE { <"+iri+"> ?p ?o . } ";
		Query qry = QueryFactory.create(sparql);
		QueryExecution qe = QueryExecutionFactory.create(qry, dataset);
		ResultSet rs = qe.execSelect();
		StringBuffer newLine = new StringBuffer();
		StreamUtils.asStream(rs).forEach(result -> newLine.append(formatLineForDump(iri, result.get("?p").toString(),result.get("?o").toString())));
		qe.close(); 
		dataset.commit();
		dataset.end();
		addLineToDumpModel(tdbDirectory, modelName, newLine.toString());
	}
	
	//TODO: USE THIS TO REMOVE DUPLICATE LINES IN THE DUMP FILES BEFORE LOADING THEM IN THE DATASETS
	public void stripDuplicatesFromFile(String filename) {
		try {
		    BufferedReader reader = new BufferedReader(new FileReader(filename));
		    Set<String> lines = new HashSet<String>(10000); // maybe should be bigger
		    String line;
		    while ((line = reader.readLine()) != null) {
		        lines.add(line);
		    }
		    reader.close();
		    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		    for (String unique : lines) {
		        writer.write(unique);
		        writer.newLine();
		    }
		    writer.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadDumpsIntoTDBDatasets(){
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t loading dumps into TDB datasets");

		List<String> sourceDumps = tdbFoldDatasetsDirectory.stream().map(modelsName -> modelsName.get(0)).collect(Collectors.toList());
		List<String> targetDumps = tdbFoldDatasetsDirectory.stream().map(modelsName -> modelsName.get(1)).collect(Collectors.toList());
		List<String> referenceLinksDumps = tdbFoldDatasetsDirectory.stream().map(modelsName -> modelsName.get(2)).collect(Collectors.toList());
		
		//Presisting dumps into datasets
		sourceDumps.parallelStream().forEach(dumpName -> addDumpsToTDBDataset(dumpName));
		targetDumps.parallelStream().forEach(dumpName -> addDumpsToTDBDataset(dumpName));
		referenceLinksDumps.parallelStream().forEach(dumpName -> addDumpsToTDBDataset(dumpName));
		
	}
	
	private void addDumpsToTDBDataset(String dumpName) {
		try {
			
			Dataset dataset =  TDBFactory.createDataset(dumpName);
			dataset.begin(ReadWrite.WRITE);
			Model model = RDFDataMgr.loadModel(dumpName+".nt") ;
			dataset.getDefaultModel().add(model);
			model.close();
			dataset.commit();
			dataset.end();
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("Error loading dump '"+dumpName+".nt' into TDB "+dumpName);
		}
		
	}
	
	public String getTDBReferenceLinksDirectory() {
		String sourceTDB = this.environment.getSourceDatasetFile();
		String targetTDB = this.environment.getTargetDatasetFile();
		
		String sourceName = getTDBName(sourceTDB);
		String targetName = getTDBName(targetTDB);
		String tdbDirectory =  sourceTDB.substring(0,sourceTDB.lastIndexOf("/"));
		StringBuffer datasetName = new StringBuffer();
		datasetName.append(tdbDirectory).append("/").append(sourceName).append("-").append(targetName).append("-reference_links");
		return datasetName.toString();
	}
	
	private String getTDBName(String tdbDirectory) {
		return tdbDirectory.substring(tdbDirectory.lastIndexOf("/")+1, tdbDirectory.length());
	}
		
	public void removeAllFoldsFiles(String sourceTDBDirectory, String targetTDBDirectory) {
		// Removing source Models
		//TODO
		
		
		// Removing reference Models
		String sourceName = sourceTDBDirectory.substring(sourceTDBDirectory.lastIndexOf("/")+1, sourceTDBDirectory.length());
		String targetName = targetTDBDirectory.substring(targetTDBDirectory.lastIndexOf("/")+1, targetTDBDirectory.length());
		String tdbDirectory =  sourceTDBDirectory.substring(0,sourceTDBDirectory.lastIndexOf("/"));
		
		StringBuffer datasetName = new StringBuffer();
		datasetName.append(tdbDirectory).append("/").append(sourceName).append("-").append(targetName).append("-reference_links");
		File referenceLinksDataset = new File(datasetName.toString());
		if(referenceLinksDataset.exists())
			referenceLinksDataset.delete();
	}
	

}
