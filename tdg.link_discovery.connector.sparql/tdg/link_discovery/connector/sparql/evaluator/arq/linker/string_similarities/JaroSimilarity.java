package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;

import com.wcohen.ss.Jaro;


public class JaroSimilarity extends AbstractARQStringSimilarity{

	private  Jaro jaro;
	
	public JaroSimilarity() {
		super("JaroSimilarity");
		jaro = new Jaro();
	}

	@Override
	public Double compareStrings(String element1, String element2) {	
		return (double) jaro.score(element1, element2);
	}



}
