package tdg.link_discovery.connector.sparql.engine.evaluator.linker;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.engine.evaluator.linker.ILinker;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.Utils;

public class SequentialLinker implements ILinker{

	protected String datasetSource, datasetTarget;
	protected String sourceQuery, targetQuery;
	
	protected Set<Tuple<String,String>> instances;
	
	protected Boolean linkindDatasets;
	protected String outputFile;
	protected Integer chunk;
	
	//protected Multimap<Tuple<String,String>,Double> instancesLinked;
	protected Set<Tuple<String,String>> instancesLinkedSet;
	public SequentialLinker(){
		super();
		datasetSource="";
		datasetTarget ="";
		sourceQuery="";
		targetQuery="";
		instances = Sets.newConcurrentHashSet();
		//instancesLinked = ArrayListMultimap.create();
		linkindDatasets = false;
		outputFile = "";
		chunk = SPARQLFactory.LINKER_RESULTS_SAVING_CHUNK;
	}
	
	@Override
	public Set<Tuple<String,String>> getInstancesLinked(){
		return this.instancesLinkedSet;
	}
	
	/*
	 * Link two datasets
	 */
	
	@Override
	public void linkDatasets(Tuple<String, String> queries, String outputFile) {
		linkindDatasets = true;
		this.outputFile = outputFile;
		
		instancesLinkedSet = Sets.newConcurrentHashSet();
		if(nonEmptyQueries(queries))
			linkInstances(null, null, queries);
		saveResults();
		System.out.println("Found " + instancesLinkedSet.size()+ " between datasets");
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
	 * Link only instances
	 */
	
	@Override
	public void linkInstances(Tuple<String,String> queries){
		linkindDatasets = false;
		outputFile = "";
		//instancesLinked = ArrayListMultimap.create();
		instancesLinkedSet = Sets.newConcurrentHashSet();
		if(nonEmptyQueries(queries)){
			instances.stream().forEach(pair-> linkPairOfInstances(pair,queries));
		}
	}
	
	private boolean linkPairOfInstances(Tuple<String,String> instancesToLink, Tuple<String,String> queries){
		//System.out.println("Thread: "+Thread.currentThread().getId());
		String sourceInstance = instancesToLink.getFirstElement();
		String targetInstance = instancesToLink.getSecondElement();
		linkInstances(sourceInstance, targetInstance, queries);
	
		return true;
	}
	
	
	private void linkInstances(String sourceInstance, String targetInstance, Tuple<String,String> queries) {
		Dataset dataset = TDBFactory.createDataset(datasetSource);
		dataset.begin(ReadWrite.READ);
		
		String [] vars = SPARQLFactory.getMainVariable(queries.getFirstElement());
		List<String> variables = Lists.newArrayList();
		String mainVariable = vars[0];
		variables.addAll(Arrays.asList(vars));
		variables.remove(mainVariable);
		
		String queryString = queries.getFirstElement();
		// if sourceInstance !=null means we are linking examples, hece, embed the example iri in the query
		if(sourceInstance != null){
			queryString = queryString.replace("DISTINCT "+mainVariable, "DISTINCT ");
			queryString = queryString.replace(mainVariable, SPARQLFactory.fixIRIS(sourceInstance));
		}// Other wise don't embed anything.
			 
		queryString = queryString.replace("DISTINCT", ""); // Not removing the DISTINCT throws error in some datasets due to the data structure and incompleteness
		//queryString = queryString.concat("  "+mainVariable);
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
			ResultSet results = qexec.execSelect();
			
			while(results!= null && results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				Multimap<String, String> literals = ArrayListMultimap.create();
				
				if(soln!=null){
					// In case sourceInstance is null, hence we are linking the datasets. Retrieve the current instance iri
					if(sourceInstance==null && soln.contains(mainVariable))
						sourceInstance = soln.getResource(mainVariable).toString();
					// Retrieving literal values
					for(String variable:variables){
						if(soln.contains(variable)){
							String literal = soln.get(variable).toString();
							if(!literals.containsEntry(variable, literal))
								literals.put(variable, literal);
						}
					}
				
					// Query second dataset
					if(sourceInstance!=null){
						if(literals.isEmpty()) 
							throw new Exception("Empty attribute values retrieved in query source: "+queryString);
						queryDatasetTarget(sourceInstance, targetInstance, literals, queries);	
					}
					sourceInstance = null; // this line is mandatory to correctly retrieve the next source iri
				}
			}
			qexec.close();
		} catch (Exception e) {
			System.out.println("Error with: "+queryString);
			e.printStackTrace();
		}
		dataset.end();
	}




	private void queryDatasetTarget(String sourceInstance, String targetInstance, Multimap<String, String> literals, Tuple<String, String> queries) {
		Dataset dataset = TDBFactory.createDataset(datasetTarget);
		dataset.begin(ReadWrite.READ);
		
		String queryString = replaceVariablesWithLiterals(queries.getSecondElement(), literals);
		// Obtaining the variable that encodes the obtained score
		String scoreVariable = queryString.substring(queryString.indexOf("FILTER")+6, queryString.lastIndexOf("0"));
		scoreVariable = scoreVariable.substring(scoreVariable.indexOf("?"), scoreVariable.lastIndexOf(">")).trim();
		
		String mainVar = SPARQLFactory.getMainVariable(queryString)[0];
		if(targetInstance!=null){
			// Adding score variable to SELECT statement & replacing mainVar for the instance iri
			// The regex that follows the main var is in case we havae as main var ?hc and then a var ?hcX. Withouth the regex an error would occur
			// because the non-main var would be partialy replaced with the iri of the main var rather than a literañ
			queryString = queryString.replaceFirst("\\"+mainVar+"[^\\,0-9a-zA-Z]", scoreVariable);  
			queryString = queryString.replaceAll("\\"+mainVar+"[^\\,0-9a-zA-Z]", SPARQLFactory.fixIRIS(targetInstance)+" ");
		}else{
			// We are linking the datasets, hence we don't replace the main var for the score 
			StringBuilder str = new StringBuilder();
			str.append("\\").append(mainVar);
			queryString = queryString.replaceFirst(str.toString()+"[^\\,]", str.append(" ").append(scoreVariable).toString());
		}
		
		queryString = queryString.replace("DISTINCT", ""); // Not removing the DISTINCT throws error in some datasets due to the data structure and incompleteness
		
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
			ResultSet results = qexec.execSelect();
			
			while (results!= null && results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				
					
				if(soln.contains(scoreVariable)){
					Double score = soln.get(scoreVariable).asLiteral().getDouble();
					
					if(targetInstance==null && soln.contains(mainVar))
						targetInstance = soln.getResource(mainVar).toString();
					
					if(score != null && targetInstance!=null){
						Tuple<String,String> instancesToLink = new Tuple<String,String>(sourceInstance, targetInstance);
						//instancesLinked.put(instancesToLink, score);
						instancesLinkedSet.add(instancesToLink);
						targetInstance=null; // Set this to null in order to update the targetInstance variable in the next iteration
					}
				}
			}
			qexec.close();
		} catch (Exception e) {
			System.out.println("Failed executing in " + this.datasetTarget + " the query:\n" + queryString+" \n\n ORIGINAL QUERY"+queries.getSecondElement());
			System.out.println(literals+" \n\t"+mainVar+"\t"+scoreVariable);
			e.printStackTrace();
		}
		dataset.end();
	}
	

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
	

	
	private void saveResults() {
		instancesLinkedSet.stream().forEach(tuple -> Utils.appendLineInCSV(outputFile, generateOutputFileLine(tuple)));
	}
	
	private String generateOutputFileLine(Tuple<String,String> tuple){
		StringBuilder str = new StringBuilder();
		str.append(SPARQLFactory.fixIRIS(tuple.getFirstElement()));
		str.append(" <http://www.w3.org/2002/07/owl#sameAs> ");
		str.append(SPARQLFactory.fixIRIS(tuple.getSecondElement())).append(" .\n");
		return str.toString();
	}

	

	/*
	 * Getters & Setters
	 */
	@Override
	public void setDatasetSource(String datasetSource) {
		this.datasetSource = datasetSource;
	}

	@Override
	public void setDatasetTarget(String datasetTarget) {
		this.datasetTarget = datasetTarget;
	}

	@Override
	public void setInstances(Set<Tuple<String, String>> instances) {
		this.instances = instances;
	}

	@Override
	public void setDatasetSource(Model datasetSource) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDatasetTarget(Model datasetTarget) {
		// TODO Auto-generated method stub
		
	}


	
}
