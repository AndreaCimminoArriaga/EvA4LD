package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;

import com.wcohen.ss.JaroWinklerTFIDF;

public class JaroWinklerTFIDFSimilarity extends AbstractARQStringSimilarity{

	private  JaroWinklerTFIDF jaroWinkler;
	
	public JaroWinklerTFIDFSimilarity() {
		super("JaroWinklerTFIDFSimilarity");
		jaroWinkler = new JaroWinklerTFIDF();
	}

	@Override
	public Double compareStrings(String element1, String element2) {	
		return (double) jaroWinkler.score(element1, element2);
	}

}
