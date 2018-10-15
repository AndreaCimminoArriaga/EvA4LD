package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;


import com.wcohen.ss.JaroWinkler;

public class JaroWinklerSimilarity extends AbstractARQStringSimilarity{

	private  JaroWinkler jaroWinkler;
	
	public JaroWinklerSimilarity() {
		super("JaroWinklerSimilarity");
		jaroWinkler = new JaroWinkler();
	}

	@Override
	public Double compareStrings(String element1, String element2) {	
		return (double) jaroWinkler.score(element1, element2);
	}


}
