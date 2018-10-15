package tdg.moea.tests;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Sets;

import tdg.link_discovery.connector.sparql.engine.evaluator.linker.Linker;
import tdg.link_discovery.framework.engine.evaluator.linker.ILinker;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.environment.Environments;
import tdg.link_discovery.middleware.objects.Tuple;
import tdg.link_discovery.middleware.utils.FrameworkUtils;

public class GenlinkExperimenter {


	public static void main(String[] args) {
		
		Tuple<String,String> queries = new Tuple<String,String>();
		queries.setFirstElement("#Prefixes\n" + 
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" + 
				"PREFIX agg:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.>\n" + 
				"PREFIX str:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.>\n" + 
				"PREFIX trn:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.>\n" + 
				"#Query\n" + 
				"SELECT DISTINCT ?I3 ?om293  {\n" + 
				"?I3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/PI> .\n" + 
				"?lRh628 <http://schema.org/leadedBy> ?I3 .\n" + 
				"?lRh628 <http://schema.org/supports> ?dq957 .\n" + 
				"\n" + 
				"	?dq957 rdf:type <http://schema.org/Paper> .\n" + 
				"\n" + 
				"	?dq957 <http://schema.org/title> ?om293 .\n" + 
				" }");
		queries.setSecondElement("#Prefixes\n" + 
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n" + 
				"PREFIX agg:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.>\n" + 
				"PREFIX str:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.>\n" + 
				"PREFIX trn:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.>\n" + 
				"#Query\n" + 
				"SELECT DISTINCT ?Q37  {\n" + 
				"?Q37 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Top> .\n" + 
				"?deG1 <http://dblp.org/rdf/schema-2015-01-26#authoredBy> ?Q37 .\n" + 
				"\n" + 
				"	?deG1 rdf:type <http://dblp.org/rdf/schema-2015-01-26#Publication> .\n" + 
				"\n" + 
				"	?deG1 <http://dblp.org/rdf/schema-2015-01-26#title> ?V46 .\n" + 
				" 	#Bind and Filter\n" + 
				"	BIND ( agg:Mult(str:JaroWinklerTFIDFSimilarity(?om293,?V46,0.8),0.62) AS ?Ml1 ) .\n" + 
				"	FILTER ( ?Ml1> 0 ) .\n" + 
				"}");
		
		ILinker linker= new Linker();
		linker.setDatasetSource("tdb-data/nsf_little");
		linker.setDatasetTarget("tdb-data/dblp_reloaded");
		linker.setInstances(getInstancesToLink());
		linker.linkInstances(queries);
		System.out.println("--------END-------------");
		System.out.println(linker.getInstancesLinked());
		
	}

	private static Set<Tuple<String, String>> getInstancesToLink() {
		Set<Tuple<String,String>> instancesToLink = Sets.newHashSet();
		List<Tuple<String,String>> instances = FrameworkUtils.readGoldLinks("mainLinks.nt")
				.stream()
				.map(line -> new Tuple<String,String>(
							line.substring(line.indexOf("<")+1, line.indexOf(">")),
							line.substring(line.lastIndexOf("<")+1, line.lastIndexOf(">"))
							)
						).collect(Collectors.toList());
		
		instancesToLink.addAll(instances);
		System.out.println(instancesToLink);
		return instancesToLink;
	}
	
	
	
}
