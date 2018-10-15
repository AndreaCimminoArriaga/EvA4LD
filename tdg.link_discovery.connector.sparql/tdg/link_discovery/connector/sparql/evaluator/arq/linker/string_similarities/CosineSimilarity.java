package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;

import info.debatty.java.stringsimilarity.Cosine;


public class CosineSimilarity extends AbstractARQStringSimilarity{

	private Cosine cosine;
	
	public CosineSimilarity() {
		super("CosineSimilarity");
		cosine = new Cosine();
	}

	

	@Override
	public Double compareStrings(String element1, String element2) {
		return cosine.similarity(element1, element2);
	}



}
