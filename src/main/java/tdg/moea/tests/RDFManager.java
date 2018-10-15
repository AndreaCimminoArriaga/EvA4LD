package tdg.moea.tests;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;

public class RDFManager {

	public static void main(String[] args) {
		
		
		
		//for(String iri:irisRestaurant()){
		Integer old = GlobalCounter;
		String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
							 "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"+
							 "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
							 "PREFIX agg:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.>\n"+
							 "PREFIX set:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.sets.>\n"+
							 "PREFIX str:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.>\n"+
				 
				 //"PREFIX lvont:<http://lexvo.org/ontology#>\n"+
				 "#Query\n"+ //
				 "SELECT DISTINCT ?w ( group_concat(?c;separator=\"'@,@'\") AS ?contextLiterals) (count(distinct ?p) as ?sourceSize)  WHERE {\n"+
				 "<http://localhost:2020/nsf/pis/yanglhlt.utdallas.edu> <http://schema.org/name>  ?w .\n"+
				 "<http://localhost:2020/nsf/pis/yanglhlt.utdallas.edu> <http://schema.org/leads> ?z .\n"+
				 " ?z <http://schema.org/supports> ?p . \n"+
				 " ?p <http://schema.org/title> ?x .\n"+
		
				 "BIND (concat(\"?x='@\",?x,\"@'\") as ?c).\n"+
				 "} group by ?w ";
				 
		System.out.println("------");
		
		
		execQuery(queryString, "./tdb-data/restaurants1");
		/*if(GlobalCounter> old)
			System.out.println(iri);

		}System.out.println("Counter: "+GlobalCounter);
		 */
		/*	
		 * 
		 * <http://www.aktors.org/ontology/portal#Journal> )
		( ?t = <http://www.aktors.org/ontology/portal#Conference-Proceedings-Reference>
		 * 
		 * 
		 * 
		 * RESTAURANT1
		 	( ?t = <http://www.okkam.org/ontology_restaurant1.owl#Address> )
			 	( ?t = <http://www.okkam.org/ontology_restaurant1.owl#is_in_city> )
				( ?t = <http://www.okkam.org/ontology_restaurant1.owl#street> )
				( ?t = rdf:type )
			( ?t = <http://www.okkam.org/ontology_restaurant1.owl#City> )
				( ?t = rdf:type )
				( ?t = <http://www.okkam.org/ontology_restaurant1.owl#name> )
			( ?t = <http://www.okkam.org/ontology_restaurant1.owl#Restaurant> )
				( ?t = <http://www.okkam.org/ontology_restaurant1.owl#name> )
				( ?t = <http://www.okkam.org/ontology_restaurant1.owl#has_address> )
				( ?t = <http://www.okkam.org/ontology_restaurant1.owl#category> )
				( ?t = <http://www.okkam.org/ontology_restaurant1.owl#phone_number> )
		 */
		/* RESTAURANT 2
		 * 
		 	( ?t = <http://www.okkam.org/ontology_restaurant2.owl#Restaurant> )
			 	( ?t = <http://www.okkam.org/ontology_restaurant2.owl#has_address> )
				( ?t = <http://www.okkam.org/ontology_restaurant2.owl#has_category> )
				( ?t = <http://www.okkam.org/ontology_restaurant2.owl#phone_number> )
				( ?t = <http://www.okkam.org/ontology_restaurant2.owl#name> )
				( ?t = rdf:type )
			( ?t = <http://www.okkam.org/ontology_restaurant2.owl#Address> )
				( ?t = rdf:type )
				( ?t = <http://www.okkam.org/ontology_restaurant2.owl#city> )
				( ?t = <http://www.okkam.org/ontology_restaurant2.owl#street> )
			( ?t = <http://www.okkam.org/ontology_restaurant2.owl#Category> )
				( ?t = <http://www.okkam.org/ontology_restaurant2.owl#name> )
				( ?t = rdf:type )
		 * 
		 */
	
	}
	public static Integer GlobalCounter = 0;
	public static Set<String> execQuery(String queryString, String repositoryName) {
		Set<String> finalResults = new HashSet<String>();
		Dataset dataset = TDBFactory.createDataset(repositoryName);
		dataset.begin(ReadWrite.READ);
		TDB.sync(dataset) ;
		int counter = 0;
		Set<String> types = new HashSet<String>();
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
			ResultSet results = qexec.execSelect();
				while(results.hasNext()){
					if(results!=null){
						QuerySolution soln = results.nextSolution();
						if(soln != null){
							GlobalCounter++;
							System.out.println(soln.toString());
							counter++;	
						}
					}
				}
				qexec.close();
				
				
		}catch(Exception e){
				System.out.println("Failed executing in "+repositoryName+" the query:\n"+queryString);
				e.printStackTrace();			
		} 
		dataset.end();
		
		
		for(String type:types){
			System.out.println(type);
		}
		System.out.println(counter);
		return finalResults;
	}
	
	private static void writeFile(String content, String file) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file, true));
			bw.write(content);
			bw.newLine();
			bw.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (bw != null)
				try {
					bw.close();
				} catch (IOException ioe2) {
				}
		}
	}

	public static Set<String> pTP = new HashSet<String>();
	
	
	public static Set<String> irisRestaurant(){
		Set<String> set = new HashSet<String>();
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant0>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant1>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant2>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant3>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant4>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant5>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant6>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant7>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant8>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant9>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant10>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant11>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant12>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant13>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant14>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant15>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant16>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant17>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant18>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant19>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant20>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant21>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant22>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant23>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant24>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant25>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant26>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant27>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant28>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant29>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant30>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant31>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant32>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant33>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant34>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant35>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant36>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant37>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant38>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant39>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant40>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant41>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant42>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant43>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant44>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant45>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant46>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant47>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant48>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant49>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant50>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant51>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant52>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant53>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant54>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant55>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant56>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant57>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant58>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant59>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant60>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant61>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant62>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant63>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant64>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant65>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant66>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant67>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant68>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant69>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant70>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant71>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant72>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant73>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant74>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant75>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant76>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant77>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant78>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant79>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant80>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant81>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant82>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant83>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant84>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant85>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant86>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant87>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant88>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant89>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant90>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant91>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant92>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant93>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant94>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant95>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant96>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant97>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant98>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant99>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant100>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant101>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant102>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant103>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant104>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant105>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant106>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant107>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant108>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant109>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant110>");
		set.add("<http://www.okkam.org/oaie/restaurant2-Restaurant111>");

		return set;
	}
	
}
