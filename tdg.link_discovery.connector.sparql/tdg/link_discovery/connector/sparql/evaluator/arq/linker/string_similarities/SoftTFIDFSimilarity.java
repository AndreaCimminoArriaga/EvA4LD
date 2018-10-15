package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;

import com.wcohen.ss.SoftTFIDF;

public class SoftTFIDFSimilarity extends AbstractARQStringSimilarity{

	private  SoftTFIDF softTFIDF;
	
	public SoftTFIDFSimilarity() {
		super("SoftTFIDFSimilarity");
		softTFIDF = new SoftTFIDF();
	}

	@Override
	public Double compareStrings(String element1, String element2) {	
		
		Double score= 0.0;
		try {
			score = (double) softTFIDF.score(element1, element2);
		}catch (Exception e) {
			System.out.println("Error with strings:  "+element1+"     "+element2+" in SoftTFIDFSimilarity [non blocking just returning 0]" );
		}
		
		return score;
	}

}
