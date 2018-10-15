package tdg.moea.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;



public class SetSPARQLTest {

	public static void main(String[] args) {
		
			/*Set<String> finalResults = new HashSet<String>();
			Dataset dataset = TDBFactory.createDataset("./tdb-data/restaurants1");
			dataset.begin(ReadWrite.READ);
			TDB.sync(dataset) ;
			
			Model model = dataset.getDefaultModel();
			
	        // setup the initial queue
	        final Queue<List<Resource>> queue = new LinkedList<>();
	        final List<Resource> thingPath = new ArrayList<>();
	        thingPath.add( OWL.Thing );
	        queue.offer( thingPath );

	        // Get the paths, and display them
	        final List<List<Resource>> paths = BFS( model, queue, 4 );
	        for ( List<Resource> path : paths ) {
	            System.out.println( path );
	        }
		*/
	}
	
	  

	
	 public static List<List<Resource>> BFS( final Model model, final Queue<List<Resource>> queue, final int depth ) {
	        final List<List<Resource>> results = new ArrayList<>();
	        while ( !queue.isEmpty() ) {
	            final List<Resource> path = queue.poll();
	            results.add( path );
	            if ( path.size() < depth ) {
	                final Resource last = path.get( path.size() - 1 );
	                final StmtIterator stmt = model.listStatements( null, RDFS.subClassOf, last );
	                while ( stmt.hasNext() ) {
	                    final List<Resource> extPath = new ArrayList<>( path );
	                    extPath.add( stmt.next().getSubject().asResource() );
	                    queue.offer( extPath );
	                }
	            }
	        }
	        return results;
	    }

}
