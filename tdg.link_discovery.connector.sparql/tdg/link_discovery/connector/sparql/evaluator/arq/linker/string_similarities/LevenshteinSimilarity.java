package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;


import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;



public class LevenshteinSimilarity extends AbstractARQStringSimilarity{

	private Levenshtein lev;

	public LevenshteinSimilarity() {
		super("LevenshteinSimilarity");
		lev = new Levenshtein();
	}


	@Override
	public Double compareStrings(String element1, String element2) {
		return (double) lev.getSimilarity(element1, element2);
	}




	
	

	
}
