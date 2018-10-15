package tdg.link_discovery.connector.sparql.evaluator.arq.linker.attribute_selector;

import java.io.FileInputStream;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDBFactory;

import tdg.link_discovery.connector.sparql.evaluator.arq.linker.factory.SPARQLFactory;
import tdg.link_discovery.middleware.objects.Tuple;

public class SPARQLAttributeSelector {

	private StringBuilder query;
	private final String mainVariable = "?x";
	private final String attrVariable = "?a";
	private final String valueVariable = "?v";
	
	public SPARQLAttributeSelector(){
		query = new StringBuilder();
		query.append("SELECT ").append(attrVariable).append(" ").append(valueVariable).append(" {\n");
		query.append("\t").append(mainVariable).append(" ").append(attrVariable).append(" ").append(valueVariable).append(" .\n");
		query.append("}");
	}
	
	public Set<Tuple<String,String>> retrieveAttributesFromExample(String datasetName, String iri){
		Dataset dataset = TDBFactory.createDataset(datasetName);
		dataset.begin(ReadWrite.READ);
		Set<Tuple<String,String>> attributes = Sets.newHashSet();
		// Replace main var with the sample iri
		String queryString = query.toString().replace(mainVariable, SPARQLFactory.fixIRIS(iri));
		
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
			ResultSet results = qexec.execSelect();
		
			while(results!= null && results.hasNext()) {
				QuerySolution soln = results.nextSolution();
		
				if(soln!=null){
					if(soln.contains(attrVariable) && soln.contains(valueVariable)){
						String label = soln.get(attrVariable).toString();
						String value = soln.get(valueVariable).toString();
						attributes.add(new Tuple<String,String>(label, value));
					}
					
				}
			}
			qexec.close();
		} catch (Exception e) {
			System.out.println("Error with: "+queryString);
			e.printStackTrace();
		}
		dataset.end();
	
		return attributes;
	}
	
	public Set<Tuple<String,String>> retrieveAttributesFromExampleInModel(String datasetName, String iri){
		Set<Tuple<String,String>> attributes = Sets.newHashSet();
		String queryString = "";
		try {
			Model model = RDFDataMgr.loadModel(datasetName+".nt") ;
			if(iri.startsWith("\"") && ( iri.endsWith("\"") || iri.endsWith("\"."))) {
				int index = iri.length()-1;
				if(iri.endsWith("\".")) {
					index = index - 2;
				}
				iri = iri.replaceFirst("\"", "").substring(0, index);
			}
			// Replace main var with the sample iri
			queryString= query.toString().replace(mainVariable, SPARQLFactory.fixIRIS(iri));
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
			ResultSet results = qexec.execSelect();
		
			while(results!= null && results.hasNext()) {
				QuerySolution soln = results.nextSolution();
		
				if(soln!=null){
					if(soln.contains(attrVariable) && soln.contains(valueVariable)){
						String label = soln.get(attrVariable).toString();
						String value = soln.get(valueVariable).toString();
						attributes.add(new Tuple<String,String>(label, value));
					}
					
				}
			}
			qexec.close();
			model.close();
		} catch (Exception e) {
			System.out.println("error in query: "+queryString);
			e.printStackTrace();
		}
		return attributes;
	}
}
