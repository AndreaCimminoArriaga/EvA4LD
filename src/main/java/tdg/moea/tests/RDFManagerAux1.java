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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;

import tdg.link_discovery.middleware.utils.StreamUtils;

public class RDFManagerAux1 {

	private static Boolean isContained = true;
	
	public static void main(String[] args) {
				
	
		Integer old = GlobalCounter;
		String queryString = "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
							 "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"+
							 "PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
							 "PREFIX agg:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.aggregates.>\n"+
							 "PREFIX set:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.sets.>\n"+
							 "PREFIX str:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.>\n"+
							 "PREFIX str:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities.>\n"+
							 "PREFIX trn:<java:tdg.link_discovery.connector.sparql.evaluator.arq.linker.transformations.>\n"+
				 //"PREFIX lvont:<http://lexvo.org/ontology#>\n"+
				 "#Query\n"+
				"SELECT   ?yW74  {\n" + 
				"<http://www.okkam.org/oaie/restaurant1-Restaurant63> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.okkam.org/ontology_restaurant1.owl#Restaurant> .\n" + 
				"<http://www.okkam.org/oaie/restaurant1-Restaurant63> <http://www.okkam.org/ontology_restaurant1.owl#has_address> ?Ao43 .\n" + 
				"\n" + 
				"	?Ao43 rdf:type <http://www.okkam.org/ontology_restaurant1.owl#Address> .\n" + 
				"\n" + 
				"	?Ao43 <http://www.okkam.org/ontology_restaurant1.owl#street> ?yW74 .\n" + 
				" }";
				 
		//System.out.println("------");
		long startTime = System.nanoTime();
		execQuery(queryString, "./tdb-data/restaurants1");
		long stopTime = System.nanoTime();
	    long elapsedTime = (stopTime - startTime)/1000000;
	    //System.out.println("Query took: "+elapsedTime+" ms");
		
	}
	
	
	public static Integer GlobalCounter = 0;
	public static Set<String> execQuery(String queryString, String repositoryName) {
		Set<String> finalResults = new HashSet<String>();
		Dataset dataset = TDBFactory.createDataset(repositoryName);
		dataset.begin(ReadWrite.READ);
	
		int counter = 0;
		Set<String> types = new HashSet<String>();
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
			ResultSet results = qexec.execSelect();
				while(results.hasNext()){
					if(results!=null){
						QuerySolution soln = results.nextSolution();
						//System.out.println(">>>>"+soln);
						if(soln != null){
							GlobalCounter++;
							System.out.println(soln);
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
		//System.out.println(counter);
		return finalResults;
	}
	
	public static Set<String> execQueryModel(String queryString, String repositoryName) {
		Set<String> finalResults = new HashSet<String>();
		Model model = RDFDataMgr.loadModel("/Users/andrea/Desktop/workbench/eclipse-workspace/EvA4LD/experiments/results/restaurants/2-fold_datasets-00_02_55/restaurants2-kFold_2_1.nt");
		
		int counter = 0;
		Set<String> types = new HashSet<String>();
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
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
		model.close();
		
		
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

set.add("http://www.bbc.co.uk/programmes/b00gdbmg#programme");
set.add("http://www.bbc.co.uk/programmes/b014k2z0#programme");
set.add("http://www.bbc.co.uk/programmes/b008zlhb#programme");
set.add("http://www.bbc.co.uk/programmes/b007ch9x#programme");
set.add("http://www.bbc.co.uk/programmes/b0074mwj#programme");
set.add("http://www.bbc.co.uk/programmes/b00x9b28#programme");
set.add("http://www.bbc.co.uk/programmes/b007vsr6#programme");
set.add("http://www.bbc.co.uk/programmes/b0125kjj#programme");
set.add("http://www.bbc.co.uk/programmes/b00kz7ym#programme");
set.add("http://www.bbc.co.uk/programmes/b00874b0#programme");
set.add("http://www.bbc.co.uk/programmes/b008flgw#programme");
set.add("http://www.bbc.co.uk/programmes/b007ch31#programme");
set.add("http://www.bbc.co.uk/programmes/b0078rfx#programme");
set.add("http://www.bbc.co.uk/programmes/b00kzcd0#programme");
set.add("http://www.bbc.co.uk/programmes/b007chbs#programme");
set.add("http://www.bbc.co.uk/programmes/b00gbjh8#programme");
set.add("http://www.bbc.co.uk/programmes/b00cw7yy#programme");
set.add("http://www.bbc.co.uk/programmes/b0074qmw#programme");
set.add("http://www.bbc.co.uk/programmes/b00949hz#programme");
set.add("http://www.bbc.co.uk/programmes/b00d4l69#programme");
set.add("http://www.bbc.co.uk/programmes/b0078f62#programme");
set.add("http://www.bbc.co.uk/programmes/b007898r#programme");
set.add("http://www.bbc.co.uk/programmes/b007c949#programme");
set.add("http://www.bbc.co.uk/programmes/b00bx1qs#programme");
set.add("http://www.bbc.co.uk/programmes/b0074dz8#programme");
set.add("http://www.bbc.co.uk/programmes/b009kfsn#programme");
set.add("http://www.bbc.co.uk/programmes/b012rfpw#programme");
set.add("http://www.bbc.co.uk/programmes/b0084z3v#programme");
set.add("http://www.bbc.co.uk/programmes/b00shkht#programme");
set.add("http://www.bbc.co.uk/programmes/b0077r2z#programme");
set.add("http://www.bbc.co.uk/programmes/b00djlxn#programme");
set.add("http://www.bbc.co.uk/programmes/b014kjs5#programme");
set.add("http://www.bbc.co.uk/programmes/b00g50yz#programme");
set.add("http://www.bbc.co.uk/programmes/b00qh059#programme");
set.add("http://www.bbc.co.uk/programmes/b00wmwwc#programme");
set.add("http://www.bbc.co.uk/programmes/b00pk65y#programme");
set.add("http://www.bbc.co.uk/programmes/b0078skw#programme");
set.add("http://www.bbc.co.uk/programmes/b00t0z3b#programme");
set.add("http://www.bbc.co.uk/programmes/b0079393#programme");
set.add("http://www.bbc.co.uk/programmes/b00g7t8s#programme");
set.add("http://www.bbc.co.uk/programmes/b0078zvm#programme");
set.add("http://www.bbc.co.uk/programmes/b007v5gt#programme");
set.add("http://www.bbc.co.uk/programmes/b007ch7t#programme");
set.add("http://www.bbc.co.uk/programmes/b00ftbp4#programme");
set.add("http://www.bbc.co.uk/programmes/b00gd22n#programme");
set.add("http://www.bbc.co.uk/programmes/b007bzz9#programme");
set.add("http://www.bbc.co.uk/programmes/b007cf12#programme");
set.add("http://www.bbc.co.uk/programmes/b0077xm6#programme");
set.add("http://www.bbc.co.uk/programmes/b010y7c2#programme");
set.add("http://www.bbc.co.uk/programmes/b00pn43z#programme");
set.add("http://www.bbc.co.uk/programmes/b00x9b26#programme");
set.add("http://www.bbc.co.uk/programmes/b00l9spb#programme");
set.add("http://www.bbc.co.uk/programmes/b007c7w6#programme");
set.add("http://www.bbc.co.uk/programmes/b0195pjz#programme");
set.add("http://www.bbc.co.uk/programmes/b0074g7n#programme");
set.add("http://www.bbc.co.uk/programmes/b00dtjjp#programme");
set.add("http://www.bbc.co.uk/programmes/b00kyyrb#programme");
set.add("http://www.bbc.co.uk/programmes/b008qt09#programme");
set.add("http://www.bbc.co.uk/programmes/b0094hh8#programme");
set.add("http://www.bbc.co.uk/programmes/b0078gjh#programme");
set.add("http://www.bbc.co.uk/programmes/b00xhf50#programme");
set.add("http://www.bbc.co.uk/programmes/b00g258y#programme");
set.add("http://www.bbc.co.uk/programmes/b007yr94#programme");
set.add("http://www.bbc.co.uk/programmes/b00wyhwz#programme");
set.add("http://www.bbc.co.uk/programmes/b0074fpl#programme");
set.add("http://www.bbc.co.uk/programmes/b007bxd2#programme");
set.add("http://www.bbc.co.uk/programmes/b007lrtv#programme");
set.add("http://www.bbc.co.uk/programmes/b007yskp#programme");
set.add("http://www.bbc.co.uk/programmes/b0078ygf#programme");
set.add("http://www.bbc.co.uk/programmes/b008mhl0#programme");
set.add("http://www.bbc.co.uk/programmes/b008m7w1#programme");
set.add("http://www.bbc.co.uk/programmes/b00786nd#programme");
set.add("http://www.bbc.co.uk/programmes/b00wyhwl#programme");
set.add("http://www.bbc.co.uk/programmes/b00796tx#programme");
set.add("http://www.bbc.co.uk/programmes/b00kyjfk#programme");
set.add("http://www.bbc.co.uk/programmes/b007xf3v#programme");
set.add("http://www.bbc.co.uk/programmes/b008lyr3#programme");
set.add("http://www.bbc.co.uk/programmes/b008xzrf#programme");
set.add("http://www.bbc.co.uk/programmes/b0074dlw#programme");
set.add("http://www.bbc.co.uk/programmes/b00hp2f5#programme");
set.add("http://www.bbc.co.uk/programmes/b00749zg#programme");
set.add("http://www.bbc.co.uk/programmes/b014r5wd#programme");
set.add("http://www.bbc.co.uk/programmes/b007cyts#programme");
set.add("http://www.bbc.co.uk/programmes/b016xjwc#programme");
set.add("http://www.bbc.co.uk/programmes/b00rfr4n#programme");
set.add("http://www.bbc.co.uk/programmes/b013j3cr#programme");
set.add("http://www.bbc.co.uk/programmes/b00wyj5h#programme");
set.add("http://www.bbc.co.uk/programmes/b00790kb#programme");
set.add("http://www.bbc.co.uk/programmes/b008p8qr#programme");
set.add("http://www.bbc.co.uk/programmes/b008m8mv#programme");
set.add("http://www.bbc.co.uk/programmes/b018l6wz#programme");
set.add("http://www.bbc.co.uk/programmes/b00cfl8z#programme");
set.add("http://www.bbc.co.uk/programmes/b00thv6f#programme");
set.add("http://www.bbc.co.uk/programmes/b007c3ym#programme");
set.add("http://www.bbc.co.uk/programmes/b0078l0v#programme");
set.add("http://www.bbc.co.uk/programmes/b007cjq4#programme");
set.add("http://www.bbc.co.uk/programmes/b00pk698#programme");
set.add("http://www.bbc.co.uk/programmes/b007cfrx#programme");
set.add("http://www.bbc.co.uk/programmes/b00dwn6b#programme");
set.add("http://www.bbc.co.uk/programmes/b00s2y91#programme");
set.add("http://www.bbc.co.uk/programmes/b0074b9j#programme");
set.add("http://www.bbc.co.uk/programmes/b00nspb1#programme");
set.add("http://www.bbc.co.uk/programmes/b007c52y#programme");
set.add("http://www.bbc.co.uk/programmes/b00gd3rt#programme");
set.add("http://www.bbc.co.uk/programmes/b00c6s7w#programme");
set.add("http://www.bbc.co.uk/programmes/b00mf3ct#programme");
set.add("http://www.bbc.co.uk/programmes/b00pk5cx#programme");
set.add("http://www.bbc.co.uk/programmes/b00dqcnd#programme");
set.add("http://www.bbc.co.uk/programmes/b00hd0vj#programme");
set.add("http://www.bbc.co.uk/programmes/b0074fyl#programme");
set.add("http://www.bbc.co.uk/programmes/b00t187j#programme");
set.add("http://www.bbc.co.uk/programmes/b012brhb#programme");
set.add("http://www.bbc.co.uk/programmes/b00yk32c#programme");
set.add("http://www.bbc.co.uk/programmes/b0074r49#programme");
set.add("http://www.bbc.co.uk/programmes/b00b5lf2#programme");
set.add("http://www.bbc.co.uk/programmes/b007cb41#programme");
set.add("http://www.bbc.co.uk/programmes/b007cdzn#programme");
set.add("http://www.bbc.co.uk/programmes/b0078lkb#programme");
set.add("http://www.bbc.co.uk/programmes/b00c3k72#programme");
set.add("http://www.bbc.co.uk/programmes/b0074d4f#programme");
set.add("http://www.bbc.co.uk/programmes/b0078f1w#programme");
set.add("http://www.bbc.co.uk/programmes/b008s17v#programme");
set.add("http://www.bbc.co.uk/programmes/b00wyhwj#programme");
set.add("http://www.bbc.co.uk/programmes/b00pm33z#programme");
set.add("http://www.bbc.co.uk/programmes/b009k7kh#programme");
set.add("http://www.bbc.co.uk/programmes/b0078tnd#programme");
set.add("http://www.bbc.co.uk/programmes/b007b6kt#programme");
set.add("http://www.bbc.co.uk/programmes/b008pr0x#programme");
set.add("http://www.bbc.co.uk/programmes/b008m1vr#programme");
set.add("http://www.bbc.co.uk/programmes/b00hhtg5#programme");
set.add("http://www.bbc.co.uk/programmes/b014gs78#programme");
set.add("http://www.bbc.co.uk/programmes/b007cky9#programme");
set.add("http://www.bbc.co.uk/programmes/b00gd270#programme");
set.add("http://www.bbc.co.uk/programmes/b00ky5j7#programme");
set.add("http://www.bbc.co.uk/programmes/b0078fd3#programme");
set.add("http://www.bbc.co.uk/programmes/b007ck9n#programme");
set.add("http://www.bbc.co.uk/programmes/b00dqgxl#programme");
set.add("http://www.bbc.co.uk/programmes/b016ltyg#programme");
set.add("http://www.bbc.co.uk/programmes/b007q85c#programme");
set.add("http://www.bbc.co.uk/programmes/b007qxmh#programme");
set.add("http://www.bbc.co.uk/programmes/b008kbvr#programme");
set.add("http://www.bbc.co.uk/programmes/b00784p6#programme");
set.add("http://www.bbc.co.uk/programmes/b00zf9c4#programme");
set.add("http://www.bbc.co.uk/programmes/b00pk5ns#programme");
set.add("http://www.bbc.co.uk/programmes/b008m44c#programme");
set.add("http://www.bbc.co.uk/programmes/b0146fz0#programme");
set.add("http://www.bbc.co.uk/programmes/b00k7sm3#programme");
set.add("http://www.bbc.co.uk/programmes/b00bv3f9#programme");
set.add("http://www.bbc.co.uk/programmes/b00gd77z#programme");
set.add("http://www.bbc.co.uk/programmes/b0078g75#programme");
set.add("http://www.bbc.co.uk/programmes/b007c31d#programme");
set.add("http://www.bbc.co.uk/programmes/b00x1znv#programme");
set.add("http://www.bbc.co.uk/programmes/b013nc0p#programme");
set.add("http://www.bbc.co.uk/programmes/b0074r4z#programme");
set.add("http://www.bbc.co.uk/programmes/b013nhjy#programme");
set.add("http://www.bbc.co.uk/programmes/b007bxkg#programme");
set.add("http://www.bbc.co.uk/programmes/b0078t0v#programme");
set.add("http://www.bbc.co.uk/programmes/b00l7qkn#programme");
set.add("http://www.bbc.co.uk/programmes/b012x5pv#programme");
set.add("http://www.bbc.co.uk/programmes/b00vdh5s#programme");
set.add("http://www.bbc.co.uk/programmes/b00tml99#programme");
set.add("http://www.bbc.co.uk/programmes/b00v1ymx#programme");
set.add("http://www.bbc.co.uk/programmes/b00794mr#programme");
set.add("http://www.bbc.co.uk/programmes/b007cj8r#programme");
set.add("http://www.bbc.co.uk/programmes/b007888l#programme");
set.add("http://www.bbc.co.uk/programmes/b008cx3x#programme");
set.add("http://www.bbc.co.uk/programmes/b00kz16j#programme");
set.add("http://www.bbc.co.uk/programmes/b007c3j9#programme");
set.add("http://www.bbc.co.uk/programmes/b00gd7mk#programme");
set.add("http://www.bbc.co.uk/programmes/b0077qg6#programme");
set.add("http://www.bbc.co.uk/programmes/b00ggdw5#programme");
set.add("http://www.bbc.co.uk/programmes/b00b92p4#programme");
set.add("http://www.bbc.co.uk/programmes/b009v6hm#programme");
set.add("http://www.bbc.co.uk/programmes/b007ch93#programme");
set.add("http://www.bbc.co.uk/programmes/b00tz49l#programme");
set.add("http://www.bbc.co.uk/programmes/b00wlrtc#programme");
set.add("http://www.bbc.co.uk/programmes/b00t61gx#programme");
set.add("http://www.bbc.co.uk/programmes/b00bwslq#programme");
set.add("http://www.bbc.co.uk/programmes/b00796hv#programme");
set.add("http://www.bbc.co.uk/programmes/b00kmwdk#programme");
set.add("http://www.bbc.co.uk/programmes/b012xx3s#programme");
set.add("http://www.bbc.co.uk/programmes/b007946f#programme");
set.add("http://www.bbc.co.uk/programmes/b00gn345#programme");
set.add("http://www.bbc.co.uk/programmes/b00s56f1#programme");
set.add("http://www.bbc.co.uk/programmes/b0078sxs#programme");
set.add("http://www.bbc.co.uk/programmes/b0079397#programme");
set.add("http://www.bbc.co.uk/programmes/b007cflp#programme");
set.add("http://www.bbc.co.uk/programmes/b00m5qgl#programme");
set.add("http://www.bbc.co.uk/programmes/b0074q93#programme");
set.add("http://www.bbc.co.uk/programmes/b00tnf0d#programme");
set.add("http://www.bbc.co.uk/programmes/b0074csy#programme");
set.add("http://www.bbc.co.uk/programmes/b00lv88n#programme");
set.add("http://www.bbc.co.uk/programmes/b009ysky#programme");
set.add("http://www.bbc.co.uk/programmes/b00lv80c#programme");
set.add("http://www.bbc.co.uk/programmes/b00pn5b9#programme");
set.add("http://www.bbc.co.uk/programmes/b00cjvl3#programme");
set.add("http://www.bbc.co.uk/programmes/b0077kc6#programme");
set.add("http://www.bbc.co.uk/programmes/b0120dxf#programme");
set.add("http://www.bbc.co.uk/programmes/b007chrl#programme");
set.add("http://www.bbc.co.uk/programmes/b0078h9g#programme");
set.add("http://www.bbc.co.uk/programmes/b007c1m5#programme");
set.add("http://www.bbc.co.uk/programmes/b0074c6v#programme");
set.add("http://www.bbc.co.uk/programmes/b00mjsdl#programme");
set.add("http://www.bbc.co.uk/programmes/b00lfl2r#programme");
set.add("http://www.bbc.co.uk/programmes/b00rf176#programme");
set.add("http://www.bbc.co.uk/programmes/b0089hhz#programme");
set.add("http://www.bbc.co.uk/programmes/b00x9b4x#programme");
set.add("http://www.bbc.co.uk/programmes/b0078ddk#programme");
set.add("http://www.bbc.co.uk/programmes/b00kz6jc#programme");
set.add("http://www.bbc.co.uk/programmes/b0077nkz#programme");
set.add("http://www.bbc.co.uk/programmes/b0074fgs#programme");
set.add("http://www.bbc.co.uk/programmes/b00747rr#programme");
set.add("http://www.bbc.co.uk/programmes/b00phdqk#programme");
set.add("http://www.bbc.co.uk/programmes/b0078dp9#programme");
set.add("http://www.bbc.co.uk/programmes/b00kmnf9#programme");
set.add("http://www.bbc.co.uk/programmes/b007bsrq#programme");
set.add("http://www.bbc.co.uk/programmes/b0078nfw#programme");
set.add("http://www.bbc.co.uk/programmes/b00gfycy#programme");
set.add("http://www.bbc.co.uk/programmes/b0078734#programme");
set.add("http://www.bbc.co.uk/programmes/b00796sj#programme");
set.add("http://www.bbc.co.uk/programmes/b007c962#programme");
set.add("http://www.bbc.co.uk/programmes/b00s89ch#programme");
set.add("http://www.bbc.co.uk/programmes/b0078lb4#programme");
set.add("http://www.bbc.co.uk/programmes/b0078qh1#programme");
set.add("http://www.bbc.co.uk/programmes/b00kxmpc#programme");
set.add("http://www.bbc.co.uk/programmes/b00t6xzz#programme");
set.add("http://www.bbc.co.uk/programmes/b007ckvk#programme");
set.add("http://www.bbc.co.uk/programmes/b0078qn2#programme");
set.add("http://www.bbc.co.uk/programmes/b00rqqqp#programme");
set.add("http://www.bbc.co.uk/programmes/b007795q#programme");
set.add("http://www.bbc.co.uk/programmes/b0074t0l#programme");
set.add("http://www.bbc.co.uk/programmes/b007xmr0#programme");
set.add("http://www.bbc.co.uk/programmes/b00dhlkm#programme");
set.add("http://www.bbc.co.uk/programmes/b00cfzc2#programme");
set.add("http://www.bbc.co.uk/programmes/b01850zb#programme");
set.add("http://www.bbc.co.uk/programmes/b0078clp#programme");
set.add("http://www.bbc.co.uk/programmes/b0074924#programme");
set.add("http://www.bbc.co.uk/programmes/b00hw3s2#programme");
set.add("http://www.bbc.co.uk/programmes/b00jpxrt#programme");
set.add("http://www.bbc.co.uk/programmes/b00kwyqt#programme");
set.add("http://www.bbc.co.uk/programmes/b007ck00#programme");
set.add("http://www.bbc.co.uk/programmes/b00ckf2m#programme");
set.add("http://www.bbc.co.uk/programmes/b012zsds#programme");
set.add("http://www.bbc.co.uk/programmes/b007cl0s#programme");
set.add("http://www.bbc.co.uk/programmes/b0074sgt#programme");
set.add("http://www.bbc.co.uk/programmes/b00yc3cw#programme");
set.add("http://www.bbc.co.uk/programmes/b00t25z9#programme");
set.add("http://www.bbc.co.uk/programmes/b00cr5zk#programme");
set.add("http://www.bbc.co.uk/programmes/b008n8rh#programme");
set.add("http://www.bbc.co.uk/programmes/b007cf19#programme");
set.add("http://www.bbc.co.uk/programmes/b00g8grc#programme");
set.add("http://www.bbc.co.uk/programmes/b007zc1b#programme");
set.add("http://www.bbc.co.uk/programmes/b0079290#programme");
set.add("http://www.bbc.co.uk/programmes/b0160176#programme");
set.add("http://www.bbc.co.uk/programmes/b00792zq#programme");
set.add("http://www.bbc.co.uk/programmes/b0077zzm#programme");
set.add("http://www.bbc.co.uk/programmes/b00ly591#programme");
set.add("http://www.bbc.co.uk/programmes/b00g7rgz#programme");
set.add("http://www.bbc.co.uk/programmes/b010vyk7#programme");
set.add("http://www.bbc.co.uk/programmes/b00792zl#programme");
set.add("http://www.bbc.co.uk/programmes/b007cjn3#programme");
set.add("http://www.bbc.co.uk/programmes/b00h433n#programme");
set.add("http://www.bbc.co.uk/programmes/b00g3692#programme");
set.add("http://www.bbc.co.uk/programmes/b0135jw3#programme");
set.add("http://www.bbc.co.uk/programmes/b0078szx#programme");
set.add("http://www.bbc.co.uk/programmes/b00788fx#programme");
set.add("http://www.bbc.co.uk/programmes/b00tc29d#programme");
set.add("http://www.bbc.co.uk/programmes/b00gbcrr#programme");
set.add("http://www.bbc.co.uk/programmes/b0079l4m#programme");
set.add("http://www.bbc.co.uk/programmes/b00tc323#programme");
set.add("http://www.bbc.co.uk/programmes/b0195psg#programme");
set.add("http://www.bbc.co.uk/programmes/b0092pwm#programme");
set.add("http://www.bbc.co.uk/programmes/b00dc22q#programme");
set.add("http://www.bbc.co.uk/programmes/b008n8ky#programme");
set.add("http://www.bbc.co.uk/programmes/b0074b94#programme");
set.add("http://www.bbc.co.uk/programmes/b0078f3f#programme");
set.add("http://www.bbc.co.uk/programmes/b008p8t7#programme");
set.add("http://www.bbc.co.uk/programmes/b0078xn6#programme");
set.add("http://www.bbc.co.uk/programmes/b00qmf1w#programme");
set.add("http://www.bbc.co.uk/programmes/b00pk5w7#programme");
set.add("http://www.bbc.co.uk/programmes/b007zzkz#programme");
set.add("http://www.bbc.co.uk/programmes/b00jz0jl#programme");
set.add("http://www.bbc.co.uk/programmes/b0074fzg#programme");
set.add("http://www.bbc.co.uk/programmes/b008m3rh#programme");
set.add("http://www.bbc.co.uk/programmes/b007cfvb#programme");
set.add("http://www.bbc.co.uk/programmes/b0074g3y#programme");
set.add("http://www.bbc.co.uk/programmes/b0077hr3#programme");
set.add("http://www.bbc.co.uk/programmes/b0078yc7#programme");
set.add("http://www.bbc.co.uk/programmes/b0078m03#programme");
set.add("http://www.bbc.co.uk/programmes/b00q9y32#programme");
set.add("http://www.bbc.co.uk/programmes/b007c19y#programme");
set.add("http://www.bbc.co.uk/programmes/b007y1k7#programme");
set.add("http://www.bbc.co.uk/programmes/b007cjr2#programme");
set.add("http://www.bbc.co.uk/programmes/b007cbtn#programme");
set.add("http://www.bbc.co.uk/programmes/b00pk778#programme");
set.add("http://www.bbc.co.uk/programmes/b00783dj#programme");
set.add("http://www.bbc.co.uk/programmes/b0107zhp#programme");
set.add("http://www.bbc.co.uk/programmes/b00792qz#programme");
set.add("http://www.bbc.co.uk/programmes/b0077hp3#programme");
set.add("http://www.bbc.co.uk/programmes/b00kj2r4#programme");
set.add("http://www.bbc.co.uk/programmes/b007870c#programme");
set.add("http://www.bbc.co.uk/programmes/b00vl3k1#programme");
set.add("http://www.bbc.co.uk/programmes/b007cf0r#programme");
set.add("http://www.bbc.co.uk/programmes/b00780nq#programme");
set.add("http://www.bbc.co.uk/programmes/b00790y0#programme");
set.add("http://www.bbc.co.uk/programmes/b0078hj5#programme");
set.add("http://www.bbc.co.uk/programmes/b007bd4y#programme");
set.add("http://www.bbc.co.uk/programmes/b00wyhwn#programme");
set.add("http://www.bbc.co.uk/programmes/b00wyqzs#programme");
set.add("http://www.bbc.co.uk/programmes/b007c94s#programme");
set.add("http://www.bbc.co.uk/programmes/b0078xh1#programme");
set.add("http://www.bbc.co.uk/programmes/b0074r4k#programme");
set.add("http://www.bbc.co.uk/programmes/b00pq8c6#programme");
set.add("http://www.bbc.co.uk/programmes/b0074fln#programme");
set.add("http://www.bbc.co.uk/programmes/b00lrpfr#programme");
set.add("http://www.bbc.co.uk/programmes/b00748db#programme");
set.add("http://www.bbc.co.uk/programmes/b00tc876#programme");
set.add("http://www.bbc.co.uk/programmes/b00zmc6h#programme");
set.add("http://www.bbc.co.uk/programmes/b0074cp1#programme");
set.add("http://www.bbc.co.uk/programmes/b007cg2g#programme");
set.add("http://www.bbc.co.uk/programmes/b00qgwc9#programme");
set.add("http://www.bbc.co.uk/programmes/b0078cl4#programme");
set.add("http://www.bbc.co.uk/programmes/b00794k1#programme");
set.add("http://www.bbc.co.uk/programmes/b00b93t3#programme");
set.add("http://www.bbc.co.uk/programmes/b018ntb1#programme");
set.add("http://www.bbc.co.uk/programmes/b00mf33b#programme");
set.add("http://www.bbc.co.uk/programmes/b00ttbjz#programme");
set.add("http://www.bbc.co.uk/programmes/b007mb28#programme");
set.add("http://www.bbc.co.uk/programmes/b007btk6#programme");
set.add("http://www.bbc.co.uk/programmes/b00gd3sx#programme");
set.add("http://www.bbc.co.uk/programmes/b007chvd#programme");
set.add("http://www.bbc.co.uk/programmes/b00794k5#programme");
set.add("http://www.bbc.co.uk/programmes/b00cv87q#programme");
set.add("http://www.bbc.co.uk/programmes/b0074rk1#programme");
set.add("http://www.bbc.co.uk/programmes/b00glr88#programme");
set.add("http://www.bbc.co.uk/programmes/b007c19t#programme");
set.add("http://www.bbc.co.uk/programmes/b00snc7q#programme");
set.add("http://www.bbc.co.uk/programmes/b00kzwxd#programme");
set.add("http://www.bbc.co.uk/programmes/b008wf7j#programme");
set.add("http://www.bbc.co.uk/programmes/b007bgf8#programme");
set.add("http://www.bbc.co.uk/programmes/b00cgtr4#programme");
set.add("http://www.bbc.co.uk/programmes/b017ct1d#programme");
set.add("http://www.bbc.co.uk/programmes/b00vdh11#programme");
set.add("http://www.bbc.co.uk/programmes/b00796tw#programme");
set.add("http://www.bbc.co.uk/programmes/b00kz6t4#programme");
set.add("http://www.bbc.co.uk/programmes/b00mk518#programme");
set.add("http://www.bbc.co.uk/programmes/b00p3214#programme");
set.add("http://www.bbc.co.uk/programmes/b00dcq8h#programme");
set.add("http://www.bbc.co.uk/programmes/b0074f12#programme");
set.add("http://www.bbc.co.uk/programmes/b00kz667#programme");
set.add("http://www.bbc.co.uk/programmes/b0074cdj#programme");
set.add("http://www.bbc.co.uk/programmes/b00c06n2#programme");
set.add("http://www.bbc.co.uk/programmes/b00cqzrm#programme");
set.add("http://www.bbc.co.uk/programmes/b007bgt8#programme");
set.add("http://www.bbc.co.uk/programmes/b0164hn5#programme");
set.add("http://www.bbc.co.uk/programmes/b013yrxb#programme");
set.add("http://www.bbc.co.uk/programmes/b007w893#programme");
set.add("http://www.bbc.co.uk/programmes/b007cjmj#programme");
set.add("http://www.bbc.co.uk/programmes/b007xh7w#programme");
set.add("http://www.bbc.co.uk/programmes/b009rgf3#programme");
set.add("http://www.bbc.co.uk/programmes/b007bxmt#programme");
set.add("http://www.bbc.co.uk/programmes/b0074tp4#programme");
set.add("http://www.bbc.co.uk/programmes/b008m8qk#programme");
set.add("http://www.bbc.co.uk/programmes/b0129cn2#programme");
set.add("http://www.bbc.co.uk/programmes/b00jc6r8#programme");
set.add("http://www.bbc.co.uk/programmes/b007ch3d#programme");
set.add("http://www.bbc.co.uk/programmes/b0116h74#programme");
set.add("http://www.bbc.co.uk/programmes/b00sl2z9#programme");
set.add("http://www.bbc.co.uk/programmes/b00lrhtd#programme");
set.add("http://www.bbc.co.uk/programmes/b0078f75#programme");
set.add("http://www.bbc.co.uk/programmes/b0078942#programme");
set.add("http://www.bbc.co.uk/programmes/b0074r43#programme");
set.add("http://www.bbc.co.uk/programmes/b013nh77#programme");
set.add("http://www.bbc.co.uk/programmes/b00qpm4h#programme");
set.add("http://www.bbc.co.uk/programmes/b00791q5#programme");
set.add("http://www.bbc.co.uk/programmes/b013l0x7#programme");
set.add("http://www.bbc.co.uk/programmes/b012rhpy#programme");
set.add("http://www.bbc.co.uk/programmes/b00wfkmf#programme");
set.add("http://www.bbc.co.uk/programmes/b007cl6v#programme");
set.add("http://www.bbc.co.uk/programmes/b0074t6w#programme");
set.add("http://www.bbc.co.uk/programmes/b0078zrm#programme");
set.add("http://www.bbc.co.uk/programmes/b00kz74z#programme");
set.add("http://www.bbc.co.uk/programmes/b00mq59l#programme");
set.add("http://www.bbc.co.uk/programmes/b008ly3k#programme");
set.add("http://www.bbc.co.uk/programmes/b00psq2d#programme");
set.add("http://www.bbc.co.uk/programmes/b0078ghz#programme");
set.add("http://www.bbc.co.uk/programmes/b017ndqj#programme");
set.add("http://www.bbc.co.uk/programmes/b007cwx8#programme");
set.add("http://www.bbc.co.uk/programmes/b00gd22l#programme");
set.add("http://www.bbc.co.uk/programmes/b00781cx#programme");
set.add("http://www.bbc.co.uk/programmes/b009yxv9#programme");
set.add("http://www.bbc.co.uk/programmes/b007w8ks#programme");
set.add("http://www.bbc.co.uk/programmes/b0094cxh#programme");
set.add("http://www.bbc.co.uk/programmes/b008v5kc#programme");
set.add("http://www.bbc.co.uk/programmes/b008mfc1#programme");
set.add("http://www.bbc.co.uk/programmes/b00l1hz1#programme");
set.add("http://www.bbc.co.uk/programmes/b00gbjg9#programme");
set.add("http://www.bbc.co.uk/programmes/b0074b1p#programme");
set.add("http://www.bbc.co.uk/programmes/b0074csf#programme");
set.add("http://www.bbc.co.uk/programmes/b00785v4#programme");
set.add("http://www.bbc.co.uk/programmes/b00b9cy4#programme");
set.add("http://www.bbc.co.uk/programmes/b007b8yl#programme");
set.add("http://www.bbc.co.uk/programmes/b00vhz6x#programme");
set.add("http://www.bbc.co.uk/programmes/b0074g8t#programme");
set.add("http://www.bbc.co.uk/programmes/b007cgk9#programme");
set.add("http://www.bbc.co.uk/programmes/b00ctzgj#programme");
set.add("http://www.bbc.co.uk/programmes/b007z9yg#programme");
set.add("http://www.bbc.co.uk/programmes/b007893r#programme");
set.add("http://www.bbc.co.uk/programmes/b0078t56#programme");
set.add("http://www.bbc.co.uk/programmes/b00h4hm9#programme");
set.add("http://www.bbc.co.uk/programmes/b0074rk9#programme");
set.add("http://www.bbc.co.uk/programmes/b00jytgk#programme");
set.add("http://www.bbc.co.uk/programmes/b007cljy#programme");
set.add("http://www.bbc.co.uk/programmes/b007z9x3#programme");
set.add("http://www.bbc.co.uk/programmes/b008m2jy#programme");
set.add("http://www.bbc.co.uk/programmes/b010748g#programme");
set.add("http://www.bbc.co.uk/programmes/b00792mr#programme");
set.add("http://www.bbc.co.uk/programmes/b00x941j#programme");
set.add("http://www.bbc.co.uk/programmes/b007c5bv#programme");
set.add("http://www.bbc.co.uk/programmes/b00l1lwm#programme");
set.add("http://www.bbc.co.uk/programmes/b00x4j5l#programme");
set.add("http://www.bbc.co.uk/programmes/b007byjj#programme");
set.add("http://www.bbc.co.uk/programmes/b008mh2l#programme");
set.add("http://www.bbc.co.uk/programmes/b00kyqnd#programme");
set.add("http://www.bbc.co.uk/programmes/b00pccvs#programme");
set.add("http://www.bbc.co.uk/programmes/b00d1fhb#programme");
set.add("http://www.bbc.co.uk/programmes/b00bx1t8#programme");
set.add("http://www.bbc.co.uk/programmes/b0077h1j#programme");
set.add("http://www.bbc.co.uk/programmes/b0129bp7#programme");
set.add("http://www.bbc.co.uk/programmes/b008m49f#programme");
set.add("http://www.bbc.co.uk/programmes/b00zmdg4#programme");
set.add("http://www.bbc.co.uk/programmes/b00wyfqk#programme");
set.add("http://www.bbc.co.uk/programmes/b00pk64q#programme");
set.add("http://www.bbc.co.uk/programmes/b00vtwg3#programme");
set.add("http://www.bbc.co.uk/programmes/b0074stb#programme");
set.add("http://www.bbc.co.uk/programmes/b018vcn2#programme");
set.add("http://www.bbc.co.uk/programmes/b007c970#programme");
set.add("http://www.bbc.co.uk/programmes/b00g87m3#programme");
set.add("http://www.bbc.co.uk/programmes/b016n2nx#programme");
set.add("http://www.bbc.co.uk/programmes/b00pnr5n#programme");
set.add("http://www.bbc.co.uk/programmes/b0078ynb#programme");
set.add("http://www.bbc.co.uk/programmes/b0078cfy#programme");
set.add("http://www.bbc.co.uk/programmes/b007793t#programme");
set.add("http://www.bbc.co.uk/programmes/b00gm6fx#programme");
set.add("http://www.bbc.co.uk/programmes/b00tq6lg#programme");
set.add("http://www.bbc.co.uk/programmes/b007cf01#programme");
set.add("http://www.bbc.co.uk/programmes/b0078pp3#programme");
set.add("http://www.bbc.co.uk/programmes/b018ntb3#programme");
set.add("http://www.bbc.co.uk/programmes/b007q86z#programme");
set.add("http://www.bbc.co.uk/programmes/b00dj7gc#programme");
set.add("http://www.bbc.co.uk/programmes/b00gd3w3#programme");
set.add("http://www.bbc.co.uk/programmes/b007ccg9#programme");
set.add("http://www.bbc.co.uk/programmes/b00795lx#programme");
set.add("http://www.bbc.co.uk/programmes/b00zs7v1#programme");
set.add("http://www.bbc.co.uk/programmes/b00gd41l#programme");
set.add("http://www.bbc.co.uk/programmes/b00pmbyl#programme");
set.add("http://www.bbc.co.uk/programmes/b00mvbwk#programme");
set.add("http://www.bbc.co.uk/programmes/b00qmfss#programme");
set.add("http://www.bbc.co.uk/programmes/b007bh7w#programme");
set.add("http://www.bbc.co.uk/programmes/b00plcmt#programme");
set.add("http://www.bbc.co.uk/programmes/b0078k9q#programme");
set.add("http://www.bbc.co.uk/programmes/b00xf5fn#programme");
set.add("http://www.bbc.co.uk/programmes/b00kyp1d#programme");
set.add("http://www.bbc.co.uk/programmes/b00749h7#programme");
set.add("http://www.bbc.co.uk/programmes/b0121088#programme");
set.add("http://www.bbc.co.uk/programmes/b008kkb8#programme");
set.add("http://www.bbc.co.uk/programmes/b0078lxg#programme");
set.add("http://www.bbc.co.uk/programmes/b016xhpb#programme");
set.add("http://www.bbc.co.uk/programmes/b00vhlw3#programme");
set.add("http://www.bbc.co.uk/programmes/b00bcchb#programme");
set.add("http://www.bbc.co.uk/programmes/b00790s3#programme");
set.add("http://www.bbc.co.uk/programmes/b00gd1rn#programme");
set.add("http://www.bbc.co.uk/programmes/b017042x#programme");
set.add("http://www.bbc.co.uk/programmes/b007bxcb#programme");
set.add("http://www.bbc.co.uk/programmes/b00l0f0t#programme");
set.add("http://www.bbc.co.uk/programmes/b00gd5cg#programme");
set.add("http://www.bbc.co.uk/programmes/b0074c4c#programme");
set.add("http://www.bbc.co.uk/programmes/b007c14w#programme");
set.add("http://www.bbc.co.uk/programmes/b00796xn#programme");
set.add("http://www.bbc.co.uk/programmes/b00x1yqr#programme");
set.add("http://www.bbc.co.uk/programmes/b00kz73y#programme");
set.add("http://www.bbc.co.uk/programmes/b013dvb6#programme");
set.add("http://www.bbc.co.uk/programmes/b0078nhb#programme");
set.add("http://www.bbc.co.uk/programmes/b0078wvx#programme");
set.add("http://www.bbc.co.uk/programmes/b00w4dwb#programme");
set.add("http://www.bbc.co.uk/programmes/b00pk7gn#programme");
set.add("http://www.bbc.co.uk/programmes/b007cdm3#programme");
set.add("http://www.bbc.co.uk/programmes/b00dqcsp#programme");
set.add("http://www.bbc.co.uk/programmes/b014grqc#programme");
set.add("http://www.bbc.co.uk/programmes/b00c4wpz#programme");
set.add("http://www.bbc.co.uk/programmes/b00tgdkn#programme");
set.add("http://www.bbc.co.uk/programmes/b008pbvj#programme");
set.add("http://www.bbc.co.uk/programmes/b00kxbts#programme");
set.add("http://www.bbc.co.uk/programmes/b00cqzjr#programme");
set.add("http://www.bbc.co.uk/programmes/b007cb5v#programme");
set.add("http://www.bbc.co.uk/programmes/b0078r6w#programme");
set.add("http://www.bbc.co.uk/programmes/b0078pw1#programme");
set.add("http://www.bbc.co.uk/programmes/b007ckfq#programme");
set.add("http://www.bbc.co.uk/programmes/b008y3d8#programme");
set.add("http://www.bbc.co.uk/programmes/b007cg9q#programme");
set.add("http://www.bbc.co.uk/programmes/b0078b9s#programme");
set.add("http://www.bbc.co.uk/programmes/b00825n9#programme");
set.add("http://www.bbc.co.uk/programmes/b00f60kf#programme");
set.add("http://www.bbc.co.uk/programmes/b0074st4#programme");
set.add("http://www.bbc.co.uk/programmes/b00l5wdn#programme");
set.add("http://www.bbc.co.uk/programmes/b00cfhgm#programme");
set.add("http://www.bbc.co.uk/programmes/b00pk76m#programme");
set.add("http://www.bbc.co.uk/programmes/b0074fsc#programme");
set.add("http://www.bbc.co.uk/programmes/b007905q#programme");
set.add("http://www.bbc.co.uk/programmes/b008s1d3#programme");
set.add("http://www.bbc.co.uk/programmes/b00749g3#programme");
set.add("http://www.bbc.co.uk/programmes/b00gbdsf#programme");
set.add("http://www.bbc.co.uk/programmes/b007cg0f#programme");
set.add("http://www.bbc.co.uk/programmes/b00d7jr4#programme");
set.add("http://www.bbc.co.uk/programmes/b00cv87n#programme");
set.add("http://www.bbc.co.uk/programmes/b00fpx78#programme");
set.add("http://www.bbc.co.uk/programmes/b00kyxmp#programme");
set.add("http://www.bbc.co.uk/programmes/b0077kgs#programme");
set.add("http://www.bbc.co.uk/programmes/b00b5hr9#programme");
set.add("http://www.bbc.co.uk/programmes/b00789db#programme");
set.add("http://www.bbc.co.uk/programmes/b013y0yw#programme");
set.add("http://www.bbc.co.uk/programmes/b00k2fh3#programme");
set.add("http://www.bbc.co.uk/programmes/b00wnstq#programme");
set.add("http://www.bbc.co.uk/programmes/b007494k#programme");
set.add("http://www.bbc.co.uk/programmes/b007821x#programme");
set.add("http://www.bbc.co.uk/programmes/b007b8zp#programme");
set.add("http://www.bbc.co.uk/programmes/b00ggpzy#programme");
set.add("http://www.bbc.co.uk/programmes/b00pk64v#programme");
set.add("http://www.bbc.co.uk/programmes/b00cyctp#programme");
set.add("http://www.bbc.co.uk/programmes/b0147p84#programme");
set.add("http://www.bbc.co.uk/programmes/b00jz1y7#programme");
set.add("http://www.bbc.co.uk/programmes/b00thdg6#programme");
set.add("http://www.bbc.co.uk/programmes/b0078zt6#programme");
set.add("http://www.bbc.co.uk/programmes/b007w9tf#programme");
set.add("http://www.bbc.co.uk/programmes/b0092r61#programme");
set.add("http://www.bbc.co.uk/programmes/b007896l#programme");
set.add("http://www.bbc.co.uk/programmes/b00d1g78#programme");
set.add("http://www.bbc.co.uk/programmes/b0074rrs#programme");
set.add("http://www.bbc.co.uk/programmes/b007bw1q#programme");
set.add("http://www.bbc.co.uk/programmes/b00796rx#programme");
set.add("http://www.bbc.co.uk/programmes/b00gbf13#programme");
set.add("http://www.bbc.co.uk/programmes/b00j275m#programme");
set.add("http://www.bbc.co.uk/programmes/b00tj5m7#programme");
set.add("http://www.bbc.co.uk/programmes/b00k7pvx#programme");
set.add("http://www.bbc.co.uk/programmes/b0074q9m#programme");
set.add("http://www.bbc.co.uk/programmes/b007c94f#programme");
set.add("http://www.bbc.co.uk/programmes/b007cf7f#programme");
set.add("http://www.bbc.co.uk/programmes/b00817r6#programme");
set.add("http://www.bbc.co.uk/programmes/b00lzk14#programme");
set.add("http://www.bbc.co.uk/programmes/b00wyj5y#programme");
set.add("http://www.bbc.co.uk/programmes/b00g8hbw#programme");
set.add("http://www.bbc.co.uk/programmes/b00tqh7w#programme");
set.add("http://www.bbc.co.uk/programmes/b0074slx#programme");
set.add("http://www.bbc.co.uk/programmes/b009r2f1#programme");
set.add("http://www.bbc.co.uk/programmes/b0074fy5#programme");
set.add("http://www.bbc.co.uk/programmes/b00lvgd0#programme");
set.add("http://www.bbc.co.uk/programmes/b00l02jx#programme");
set.add("http://www.bbc.co.uk/programmes/b00jnj0n#programme");
set.add("http://www.bbc.co.uk/programmes/b00m9n8g#programme");
set.add("http://www.bbc.co.uk/programmes/b0179vzm#programme");
set.add("http://www.bbc.co.uk/programmes/b0078j33#programme");
set.add("http://www.bbc.co.uk/programmes/b00ghqr8#programme");
set.add("http://www.bbc.co.uk/programmes/b00wyg0b#programme");
set.add("http://www.bbc.co.uk/programmes/b007wv7r#programme");
set.add("http://www.bbc.co.uk/programmes/b00ksjdc#programme");
set.add("http://www.bbc.co.uk/programmes/b00tkxhk#programme");
set.add("http://www.bbc.co.uk/programmes/b00d1j56#programme");
set.add("http://www.bbc.co.uk/programmes/b007c1bq#programme");
set.add("http://www.bbc.co.uk/programmes/b00m5ph6#programme");
set.add("http://www.bbc.co.uk/programmes/b00gl7sj#programme");
set.add("http://www.bbc.co.uk/programmes/b00ythc3#programme");
set.add("http://www.bbc.co.uk/programmes/b0078hf6#programme");
set.add("http://www.bbc.co.uk/programmes/b00gbjg7#programme");
set.add("http://www.bbc.co.uk/programmes/b00l9mt3#programme");
set.add("http://www.bbc.co.uk/programmes/b0078vzp#programme");
set.add("http://www.bbc.co.uk/programmes/b00t3t4h#programme");
set.add("http://www.bbc.co.uk/programmes/b00kx6g9#programme");
set.add("http://www.bbc.co.uk/programmes/b008499b#programme");
set.add("http://www.bbc.co.uk/programmes/b012fzzq#programme");
set.add("http://www.bbc.co.uk/programmes/b00794nd#programme");
set.add("http://www.bbc.co.uk/programmes/b0078xmc#programme");
set.add("http://www.bbc.co.uk/programmes/b008m213#programme");
set.add("http://www.bbc.co.uk/programmes/b00779d3#programme");
set.add("http://www.bbc.co.uk/programmes/b00tjp64#programme");
set.add("http://www.bbc.co.uk/programmes/b007vyhh#programme");
set.add("http://www.bbc.co.uk/programmes/b008m2f9#programme");
set.add("http://www.bbc.co.uk/programmes/b00ysp6d#programme");
set.add("http://www.bbc.co.uk/programmes/b018nw19#programme");
set.add("http://www.bbc.co.uk/programmes/b007c9bn#programme");
set.add("http://www.bbc.co.uk/programmes/b00gmlwg#programme");
set.add("http://www.bbc.co.uk/programmes/b00kyl91#programme");
set.add("http://www.bbc.co.uk/programmes/b00cyjjj#programme");
set.add("http://www.bbc.co.uk/programmes/b00sbk03#programme");
set.add("http://www.bbc.co.uk/programmes/b00kygwh#programme");
set.add("http://www.bbc.co.uk/programmes/b00crbjw#programme");
set.add("http://www.bbc.co.uk/programmes/b007c2w0#programme");
set.add("http://www.bbc.co.uk/programmes/b007b6ks#programme");
set.add("http://www.bbc.co.uk/programmes/b008m440#programme");
set.add("http://www.bbc.co.uk/programmes/b00bwstf#programme");
set.add("http://www.bbc.co.uk/programmes/b00jsz9d#programme");
set.add("http://www.bbc.co.uk/programmes/b007c4vs#programme");
set.add("http://www.bbc.co.uk/programmes/b00xhdwf#programme");
set.add("http://www.bbc.co.uk/programmes/b00zp6cd#programme");
set.add("http://www.bbc.co.uk/programmes/b00g7s6r#programme");
set.add("http://www.bbc.co.uk/programmes/b0078mdf#programme");
set.add("http://www.bbc.co.uk/programmes/b007c7w5#programme");
set.add("http://www.bbc.co.uk/programmes/b00pk69b#programme");
set.add("http://www.bbc.co.uk/programmes/b00wqfcs#programme");
set.add("http://www.bbc.co.uk/programmes/b00tj4lh#programme");
set.add("http://www.bbc.co.uk/programmes/b0078ln8#programme");
set.add("http://www.bbc.co.uk/programmes/b007ck4j#programme");
set.add("http://www.bbc.co.uk/programmes/b0074rrk#programme");
set.add("http://www.bbc.co.uk/programmes/b00tf20n#programme");
set.add("http://www.bbc.co.uk/programmes/b00h9xq2#programme");
set.add("http://www.bbc.co.uk/programmes/b00mvdnc#programme");
set.add("http://www.bbc.co.uk/programmes/b00791vy#programme");
set.add("http://www.bbc.co.uk/programmes/b00nvbr5#programme");
set.add("http://www.bbc.co.uk/programmes/b00ckf2p#programme");
set.add("http://www.bbc.co.uk/programmes/b00dj872#programme");
set.add("http://www.bbc.co.uk/programmes/b0074sc5#programme");
set.add("http://www.bbc.co.uk/programmes/b00z2wtd#programme");
set.add("http://www.bbc.co.uk/programmes/b007lrf7#programme");
set.add("http://www.bbc.co.uk/programmes/b00jt5cj#programme");
set.add("http://www.bbc.co.uk/programmes/b014kd3p#programme");
set.add("http://www.bbc.co.uk/programmes/b008lyfn#programme");
set.add("http://www.bbc.co.uk/programmes/b00gbdrn#programme");
set.add("http://www.bbc.co.uk/programmes/b008mfb8#programme");
set.add("http://www.bbc.co.uk/programmes/b0120dvc#programme");
set.add("http://www.bbc.co.uk/programmes/b00p8485#programme");
set.add("http://www.bbc.co.uk/programmes/b0078rzh#programme");
set.add("http://www.bbc.co.uk/programmes/b016fr22#programme");
set.add("http://www.bbc.co.uk/programmes/b00ty79c#programme");
set.add("http://www.bbc.co.uk/programmes/b00wyfxt#programme");
set.add("http://www.bbc.co.uk/programmes/b007chgc#programme");
set.add("http://www.bbc.co.uk/programmes/b00874dd#programme");
set.add("http://www.bbc.co.uk/programmes/b00tlnf1#programme");
set.add("http://www.bbc.co.uk/programmes/b007mcmd#programme");
set.add("http://www.bbc.co.uk/programmes/b00bx026#programme");
set.add("http://www.bbc.co.uk/programmes/b00vd7n0#programme");
set.add("http://www.bbc.co.uk/programmes/b007cf05#programme");
set.add("http://www.bbc.co.uk/programmes/b007922t#programme");
set.add("http://www.bbc.co.uk/programmes/b0074cnn#programme");
set.add("http://www.bbc.co.uk/programmes/b007qz4v#programme");
set.add("http://www.bbc.co.uk/programmes/b007c1q4#programme");
set.add("http://www.bbc.co.uk/programmes/b00wdq8v#programme");
set.add("http://www.bbc.co.uk/programmes/b00tf1mv#programme");
set.add("http://www.bbc.co.uk/programmes/b00jjgbw#programme");
set.add("http://www.bbc.co.uk/programmes/b0078ftz#programme");
set.add("http://www.bbc.co.uk/programmes/b0074fv9#programme");
set.add("http://www.bbc.co.uk/programmes/b008m3hc#programme");
set.add("http://www.bbc.co.uk/programmes/b00k2drg#programme");
set.add("http://www.bbc.co.uk/programmes/b007bgk3#programme");
set.add("http://www.bbc.co.uk/programmes/b00qsy83#programme");
set.add("http://www.bbc.co.uk/programmes/b0078npz#programme");
set.add("http://www.bbc.co.uk/programmes/b00sqljd#programme");
set.add("http://www.bbc.co.uk/programmes/b00g27m6#programme");
set.add("http://www.bbc.co.uk/programmes/b00rjrwz#programme");
set.add("http://www.bbc.co.uk/programmes/b00796tk#programme");
set.add("http://www.bbc.co.uk/programmes/b00hms22#programme");
set.add("http://www.bbc.co.uk/programmes/b00yc3b9#programme");
set.add("http://www.bbc.co.uk/programmes/b007bg1k#programme");
set.add("http://www.bbc.co.uk/programmes/b008njlr#programme");
set.add("http://www.bbc.co.uk/programmes/b00rf10p#programme");
set.add("http://www.bbc.co.uk/programmes/b012g01s#programme");
set.add("http://www.bbc.co.uk/programmes/b00b2swv#programme");
set.add("http://www.bbc.co.uk/programmes/b0078q8z#programme");
set.add("http://www.bbc.co.uk/programmes/b007c6gl#programme");
set.add("http://www.bbc.co.uk/programmes/b007926s#programme");
set.add("http://www.bbc.co.uk/programmes/b00kzcfy#programme");
set.add("http://www.bbc.co.uk/programmes/b00rfp72#programme");
set.add("http://www.bbc.co.uk/programmes/b0077yxs#programme");
set.add("http://www.bbc.co.uk/programmes/b0088qs0#programme");
set.add("http://www.bbc.co.uk/programmes/b00949jg#programme");
set.add("http://www.bbc.co.uk/programmes/b007bzmc#programme");
set.add("http://www.bbc.co.uk/programmes/b0078dzj#programme");
set.add("http://www.bbc.co.uk/programmes/b00hhgwj#programme");
set.add("http://www.bbc.co.uk/programmes/b0077szm#programme");
set.add("http://www.bbc.co.uk/programmes/b0078ns6#programme");
set.add("http://www.bbc.co.uk/programmes/b007c5bb#programme");
set.add("http://www.bbc.co.uk/programmes/b00g226h#programme");
set.add("http://www.bbc.co.uk/programmes/b00xhfcb#programme");
set.add("http://www.bbc.co.uk/programmes/b009gl57#programme");
set.add("http://www.bbc.co.uk/programmes/b00lf6z7#programme");
set.add("http://www.bbc.co.uk/programmes/b00c6spv#programme");
set.add("http://www.bbc.co.uk/programmes/b00wyh2k#programme");
set.add("http://www.bbc.co.uk/programmes/b0078rh5#programme");
set.add("http://www.bbc.co.uk/programmes/b00747pc#programme");
set.add("http://www.bbc.co.uk/programmes/b008m8nc#programme");
set.add("http://www.bbc.co.uk/programmes/b008cyh5#programme");
set.add("http://www.bbc.co.uk/programmes/b00796sx#programme");
set.add("http://www.bbc.co.uk/programmes/b00pl6tj#programme");

		return set;
	}
	
}
