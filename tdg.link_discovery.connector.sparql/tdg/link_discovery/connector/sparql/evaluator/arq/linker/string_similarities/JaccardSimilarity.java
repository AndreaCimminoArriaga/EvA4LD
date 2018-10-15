package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;


public class JaccardSimilarity extends AbstractARQStringSimilarity{

	private uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity jaccard;
	
	public JaccardSimilarity() {
		super("JaccardSimilarity");
		jaccard = new uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity();
	}

	@Override
	public Double compareStrings(String element1, String element2) {	
		return (double) jaccard.getSimilarity(element1, element2);
	}


	
	
}
