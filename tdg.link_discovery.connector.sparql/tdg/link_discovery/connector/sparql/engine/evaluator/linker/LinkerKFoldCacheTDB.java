package tdg.link_discovery.connector.sparql.engine.evaluator.linker;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import tdg.link_discovery.connector.sparql.engine.evaluator.KFoldEvaluatorCache;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.engine.evaluator.linker.ILinker;
import tdg.link_discovery.middleware.objects.Tuple;


public class LinkerKFoldCacheTDB implements ILinker {

	protected String datasetSourceName, datasetTargetName;
	protected String sourceQuery, targetQuery;
	
	private KFoldEvaluatorCache kFoldCache;
	
	private String outputFile;
	
	
	private Set<Tuple<String,String>> instancesLinked;
	public static int DEFAULT_THREAD_POOL_SIZE = 50;
	
	public LinkerKFoldCacheTDB(KFoldEvaluatorCache kFoldCache){
		super();
		datasetSourceName="";
		datasetTargetName ="";
		sourceQuery="";
		targetQuery="";
		this.kFoldCache = kFoldCache;
		this.outputFile = "";
		instancesLinked = new CopyOnWriteArraySet<Tuple<String,String>>();
	}
	
	
	

	public LinkerKFoldCacheTDB() {
		// TODO Auto-generated constructor stub
	}




	private Boolean nonEmptyQuery(String query){
		Pattern pattern = Pattern.compile("\\{\\s*\\}");
        Matcher matcher = pattern.matcher(query);
		return  !matcher.find();
	}
	
	private Boolean nonEmptyQueries(Tuple<String,String> queries){
		return nonEmptyQuery(queries.getFirstElement()) && nonEmptyQuery(queries.getSecondElement());
	}
	
	/*
	 * Link two datasets
	 */

	@Override
	public void linkDatasets(Tuple<String, String> queries, String outputFile) {
		this.outputFile = outputFile;
		instancesLinked = new CopyOnWriteArraySet<Tuple<String,String>>();
		link(queries);
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

	
	/*
	 * Link only instances
	 */
	
	@Override
	public void linkInstances(Tuple<String,String> queries){
		instancesLinked = new CopyOnWriteArraySet<Tuple<String,String>>();
		if(nonEmptyQueries(queries)){
			link(queries);
		}
	}
	
	private void link(Tuple<String,String> queries) {
		Integer threadPool = kFoldCache.getPositiveReferenceLinks().size()+kFoldCache.getNegativeReferenceLinks().size();
		ExecutorService executor = Executors.newFixedThreadPool(Math.min(threadPool, DEFAULT_THREAD_POOL_SIZE));
		List<Callable<Tuple<String,String>>> tasks = Lists.newArrayList();
		// We aim to link each positive and negative reference link
		Set<Tuple<String,String>> instances = retrieveReferenceLinksfromKFoldCacheToLink();
		for(Tuple<String,String> instance : instances) {
			// For each reference link submit a task to link them
			Callable<Tuple<String,String>> task = () -> {
				return linkReferenceLinksFromDatasets(instance.getFirstElement(),instance.getSecondElement(), queries);
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
					// TODO
					System.exit(-1);
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

	

	
	private Tuple<String,String> linkReferenceLinksFromDatasets(String sourceInstance, String targetInstance, Tuple<String,String> queries) {
		Dataset datasetSource = TDBFactory.createDataset(datasetSourceName);
		datasetSource.begin(ReadWrite.READ);
		Tuple<String,String> link = null;
		// Retrieving variables
		Tuple<String, List<String>> retrievedVariables = retrieveVariablesFromSourceQuery(queries.getFirstElement());
		String mainVariable = retrievedVariables.getFirstElement();
		List<String> variables = retrievedVariables.getSecondElement(); // Does not contain the main var
		// Prepare query to be executed
		String queryString = formatSourceQuery(queries.getFirstElement(), mainVariable, sourceInstance);
			 
		try {
			// Execute query
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, datasetSource);
			ResultSet results = qexec.execSelect();
			
			// Retrieve and process results in parallel
			while(results.hasNext()) {
				QuerySolution soln = results.next();
				if (soln != null) {
					// Retrieving literal values
					Multimap<String, String> literals = retrieveSourceLiteralsFromQuerySolution(soln, mainVariable, variables);
					link = queryDatasetTarget(sourceInstance, targetInstance, literals,queries);
				}
			}
			
			qexec.close();
		} catch (Exception e) {
			System.out.println("Error with: "+queryString);
			e.printStackTrace();
		}
		datasetSource.end();
		return link;
	}
	
		

	private Tuple<String,String> queryDatasetTarget(String sourceInstance, String targetInstance, Multimap<String, String> literals, Tuple<String, String> queries) {
		Tuple<String,String> link = null;
		Dataset datasetTarget = TDBFactory.createDataset(datasetTargetName);
		datasetTarget.begin(ReadWrite.READ);
		
		String queryString = replaceVariablesWithLiterals(queries.getSecondElement(), literals);
		// Obtaining relevant variables from query: main var and score var
		String scoreVariable = getTargetScoreVariable(queryString);
		String mainVariable = SPARQLFactory.getMainVariable(queryString)[0];
		// Formatting query
		queryString = formatTargetQuery(queryString, mainVariable,scoreVariable, targetInstance);
		try {
		
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, datasetTarget);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()) {
				QuerySolution soln = results.next();
				
				if (soln.contains(scoreVariable)) {
					Double score = soln.get(scoreVariable).asLiteral().getDouble();
					if (score != null && score>0.0) 
						link = new Tuple<String, String>( sourceInstance, targetInstance);
				}
			}
			qexec.close();
		} catch (Exception e) {
			System.out.println("Failed executing target query in " + this.datasetTargetName + " the query:\n" + queryString+" \n\n ORIGINAL QUERY"+queries.getSecondElement());
			System.out.println(literals);
			System.out.println("main: "+mainVariable);
			System.out.println("score: "+scoreVariable);
			System.out.println("target: "+targetInstance);
			//e.printStackTrace();
		}
		datasetTarget.end();
		return link;
	}
		
	/*
	 * Source query auxiliar functions
	 */
	
	private Tuple<String, List<String>> retrieveVariablesFromSourceQuery(String query){
		String [] vars = SPARQLFactory.getMainVariable(query);
		List<String> variables = Lists.newArrayList();
		String mainVariable = vars[0];
		variables.addAll(Arrays.asList(vars));
		variables.remove(mainVariable);
		return new Tuple<String,List<String>>(mainVariable, variables);
	}

	private String	formatSourceQuery(String query, String mainVariable, String sourceInstance){
		String queryString = query;
		// if sourceInstance !=null means we are linking examples, hece, embed the example iri in the query
		if(sourceInstance != null){
			queryString = queryString.replace("DISTINCT "+mainVariable, "DISTINCT ");
			queryString = queryString.replace(mainVariable, SPARQLFactory.fixIRIS(sourceInstance));
		}// Other wise don't embed anything.
		queryString = queryString.replace("DISTINCT", ""); // Not removing the DISTINCT throws error in some datasets due to the data structure and incompleteness
		return queryString;
	}
	
	private Multimap<String, String> retrieveSourceLiteralsFromQuerySolution(QuerySolution soln, String mainVariable, List<String> variables){
		Multimap<String, String> literals = ArrayListMultimap.create();
		variables.stream().forEach(variable ->{
			if (soln.contains(variable)) {
				String literal = soln.get(variable).toString();
				if (!literals.containsEntry(variable, literal))
					literals.put(variable, literal);
			}
		});
		return literals;
	}
	
	/*
	 * Target query auxiliar functions
	 */
	
	private String getTargetScoreVariable(String targetQuery){
		String scoreVariable = targetQuery.substring(targetQuery.indexOf("FILTER")+6, targetQuery.lastIndexOf("0"));
		scoreVariable = scoreVariable.substring(scoreVariable.indexOf("?"), scoreVariable.lastIndexOf(">")).trim();
		return scoreVariable;
	}
	
	private String formatTargetQuery(String targetQuery, String mainVar, String scoreVariable, String targetInstance){
		String queryString = targetQuery;
		if(targetInstance!=null){
			// Adding score variable to SELECT statement & replacing mainVar for the instance iri
			// The regex that follows the main var is in case we havae as main var ?hc and then a var ?hcX. Withouth the regex an error would occur
			// because the non-main var would be partialy replaced with the iri of the main var rather than a literañ
			queryString = queryString.replaceFirst("\\"+mainVar+"[^\\,0-9a-zA-Z]", scoreVariable);  
			queryString = queryString.replaceAll("\\"+mainVar+"[^\\,0-9a-zA-Z]", SPARQLFactory.fixIRIS(targetInstance)+" ");
			
		}
		queryString = queryString.replace("DISTINCT", ""); // Not removing the DISTINCT throws error in some datasets due to the data structure and incompleteness
		return queryString;
	}
	
	/*
	 * General auxiliar functions
	 */
	
	private String replaceVariablesWithLiterals(String queryString, Multimap<String, String> literals) {
		String query = queryString;
		for(String var:literals.keySet()){
			for(String literal:literals.get(var)){
				String literalFixed = SPARQLFactory.fixLiterals(literal);
				StringBuffer literalToReplace = new StringBuffer();
				literalToReplace.append("\"").append(literalFixed).append("\"");
				query = query.replace(var,literalToReplace.toString());
			
			}
		}
		return query;
	}
	

	/*
	 * Getters & Setters
	 */

	@Override
	public Set<Tuple<String,String>> getInstancesLinked(){
		return this.instancesLinked;
	}
	
	@Override
	public void setDatasetSource(String datasetSource) {
		this.datasetSourceName = datasetSource;
	}


	@Override
	public void setDatasetTarget(String datasetTarget) {
		this.datasetTargetName = datasetTarget;
	}
	

	public void setDatasetSource(KFoldEvaluatorCache dataset) {
		this.kFoldCache = dataset;
	}

	/*
	 * Deprecated
	 */



	@Deprecated
	@Override
	public void setDatasetSource(Model datasetSource) {
		//empty
	}
	@Deprecated
	@Override
	public void setDatasetTarget(Model datasetTarget) {
		//empty
	}

	@Deprecated
	@Override
	public void setInstances(Set<Tuple<String, String>> instances) {
		//empty
	}




	
}
