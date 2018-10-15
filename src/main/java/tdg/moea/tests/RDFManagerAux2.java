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

public class RDFManagerAux2 {

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
				 "#Query\n"+
				 "SELECT  (count(distinct ?z) as ?iris) (group_concat(?scoring1;separator=\",\") as ?s1) (group_concat(?scoring2;separator=\",\") as ?s2) (group_concat(?scoring3;separator=\",\") as ?s3) (group_concat(?scoring4;separator=\",\") as ?s4) (group_concat(?scoring5;separator=\",\") as ?s5) (group_concat(?scoring6;separator=\",\") as ?s6) (group_concat(?scoring7;separator=\",\") as ?s7) (group_concat(?scoring8;separator=\",\") as ?s8) (group_concat(?scoring9;separator=\",\") as ?s9) (group_concat(?scoring10;separator=\",\") as ?s10)  (group_concat(?scoring10;separator=\",\") as ?s11) (set:SimpsonMetric(concat(?s1,\"@\",?s2,\"@\",?s3,\"@\",?s4,\"@\",?s5,\"@\",?s6,\"@\",?s7,\"@\",?s8,\"@\",?s9,\"@\",?s10,\"@\",?s11), 11, ?iris, 0.01) as ?score)  WHERE {\n"+
				 
					 "<http://dblp.org/pers/l/Liu:Yang> <http://dblp.org/rdf/schema-2015-01-26#primaryFullPersonName> ?w .\n"+
					 "?z <http://dblp.org/rdf/schema-2015-01-26#authoredBy> <http://dblp.org/pers/l/Liu:Yang> . \n"+
					 "?z <http://dblp.org/rdf/schema-2015-01-26#title> ?x .\n"+
				 
					"BIND ( str:LevenshteinSimilarity(?x,\"Exploring a Corpus-based Approach for Detecting Language Impairment in Children.\",0.4) as ?scoring1 ) .\n"+
					"BIND ( str:LevenshteinSimilarity(?x,\"Automatic Generation of Index of Productive Syntax for Child Language Transcripts.\",0.4) as ?scoring2 ) .\n"+
					"BIND ( str:LevenshteinSimilarity(?x,\"Automatic generation of the index of productive syntax\\nfor child language transcripts\",0.4) as ?scoring3 ) .\n"+
					"BIND ( str:LevenshteinSimilarity(?x,\"Exploring Correlation between ROUGE and Human Evaluation on Meeting Summaries\",0.4) as ?scoring4 ) .\n"+
					"BIND ( str:LevenshteinSimilarity(?x,\"Improving Supervised Learning for Meeting Summarization Using Sampling and Regression\",0.4) as ?scoring5 ) .\n"+
					"BIND ( str:LevenshteinSimilarity(?x,\"A Supervised Framework for Keyword Extraction from Meeting Transcripts\",0.4) as ?scoring6 ) .\n"+
					"BIND ( str:LevenshteinSimilarity(?x,\"Using N-best Lists and Confusion Networks for Meeting Summarization\",0.4) as ?scoring7 ) .\n"+
					"BIND ( str:LevenshteinSimilarity(?x,\"Improving Supervised Learning for Meeting Summarization Using Sampling and Regression\",0.4) as ?scoring8 ) .\n"+
					"BIND ( str:LevenshteinSimilarity(?x,\"Exploring Correlation between ROUGE and Human Evaluation on Meeting Summaries\",0.4) as ?scoring9 ) .\n"+
					"BIND ( str:LevenshteinSimilarity(?x,\"Towards Abstractive Speech Summarization: Exploring Unsupervised and Supervised Approaches for Spoken Utterance Compression\",0.4) as ?scoring10 ) .\n"+
					"BIND ( str:LevenshteinSimilarity(?x,\"Linear discourse segmentation of multi-party meetings based on local and global information\",0.4) as ?scoring11 ) .\n"+
					
					//"BIND ( (IF(?scoring1>0 || ?scoring2>0 || ?scoring3>0 || ?scoring4>0 || ?scoring5>0 || ?scoring6>0 || ?scoring7>0 || ?scoring8>0 || ?scoring9>0 || ?scoring10>0 || ?scoring11>0, ?z, \"nan\" )) AS ?links). \n"+		
				 "} ";
				 
		System.out.println("------");
		
		long startTime = System.nanoTime();
		execQuery(queryString, "./tdb-data/dblp_big");
		long stopTime = System.nanoTime();
	    long elapsedTime = (stopTime - startTime)/1000000;
	    System.out.println("Query took: "+elapsedTime+" ms");
	    
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
