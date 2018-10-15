package tdg.link_discovery.connector.sparql.engine.evaluator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.tdb.TDBFactory;

import com.google.common.collect.Sets;

import tdg.link_discovery.connector.sparql.engine.evaluator.linker.Linker;
import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.framework.engine.evaluator.AbstractEvaluator;
import tdg.link_discovery.framework.engine.evaluator.linker.ILinker;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.objects.ConfusionMatrix;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.FrameworkUtils;
import tdg.link_discovery.middleware.utils.StreamUtils;

public class ExaustiveEvaluator extends AbstractEvaluator{

	private  ILinker linker;
	private Set<Tuple<String, String>> goldLinks;
	private Integer cartesianProductSize;
	
	
	
	public ExaustiveEvaluator(IEnvironment environment) {
		super(environment);
		List<String> goldLinksStr = FrameworkUtils.readGoldLinks(environment.getGoldStandardFile());
		goldLinks = Sets.newHashSet();
		goldLinksStr.forEach(link -> goldLinks.add(new Tuple<String,String>(
										link.split("><http://www.w3.org/2002/07/owl#sameAs><")[0].replace("<", "").trim(),
										link.split("><http://www.w3.org/2002/07/owl#sameAs><")[1].replace(">.", "").trim())
		));
		linker = new Linker();
		linker.setDatasetSource(environment.getSourceDatasetFile());
		linker.setDatasetTarget(environment.getTargetDatasetFile());
		
		int sourceSize = querySizeOfInstances(environment.getSourceDatasetFile(), environment.getSourceRestrictions());
		int targetSize = querySizeOfInstances(environment.getTargetDatasetFile(), environment.getTargetRestrictions());
	
		cartesianProductSize = sourceSize*targetSize;
	}
	


	@Override
	public ConfusionMatrix evaluate(Object object) {
		@SuppressWarnings("unchecked")
		Tuple<String,String> queries = (Tuple<String, String>) object;
		linker.linkDatasets(queries, environment.getLinksOutput());		
		ConfusionMatrix matrix = getMetrics(linker.getInstancesLinked(), goldLinks, null);
		SPARQLFactory.usedVariables.clear(); // cleans the cache of used variables in the generated queries to free memory
		
		return matrix;
	}

	@Override
	public void apply(Object object) {
		Tuple<String,String> queries = (Tuple<String, String>) object;
		linker.linkDatasets(queries, environment.getLinksOutput());
		SPARQLFactory.usedVariables.clear(); // cleans the cache of used variables in the generated queries to free memory
	}

	@Override
	public ConfusionMatrix getMetrics(Set<Tuple<String, String>> instancesLinked, Set<Tuple<String, String>> positive, Set<Tuple<String, String>> negative) {
		ConfusionMatrix metrics = new ConfusionMatrix();
		Integer truePositives = 0;
		Integer falsePositives = 0;

		for(Tuple<String,String> irisLinked: instancesLinked){
			if(positive.contains(irisLinked)) {
				truePositives++;
			}else {
				falsePositives++;
			}
		}
		
		//
		Integer falseNegatives = positive.size() - truePositives;
		Integer trueNegatives = (cartesianProductSize - positive.size()) - falsePositives;
		
		metrics.setTruePositives(truePositives);
		metrics.setFalsePositives(falsePositives);
		metrics.setTrueNegatives(trueNegatives);
		metrics.setFalseNegatives(falseNegatives);
		
		return metrics;
	}
	
	
	
	private int querySizeOfInstances(String datasetFile, List<String> restrictions) {
		Integer datasetSize = 0;
		Dataset datasetSource = TDBFactory.createDataset(datasetFile);
		datasetSource.begin(ReadWrite.READ);
	
		
		// Prepare query to be executed
		StringBuffer queryString = new StringBuffer("SELECT (count(distinct ?s) as ?c) {\n");
		restrictions.stream().forEach(restriction -> queryString.append("\t?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <").append(restriction).append("> .\n"));
		queryString.append("}");
		try {
			// Execute query
			Query query = QueryFactory.create(queryString.toString());
			QueryExecution qexec = QueryExecutionFactory.create(query, datasetSource);
			ResultSet results = qexec.execSelect();
			// Retrieve and process results in parallel
			String result = results.next().get("?c").toString();
			if(result.contains("^^"))
				result = result.substring(0, result.indexOf("^"));
			if(result.contains("\""))
				result = result.replace("\"","");
			
			datasetSize = Integer.parseInt(result);
			qexec.close();
		} catch (Exception e) {
			System.out.println("Error with: "+queryString);
			e.printStackTrace();
		}
		datasetSource.end();
		
		return datasetSize;
	}
	

}
