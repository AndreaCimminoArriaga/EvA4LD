package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;

import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import uk.ac.shef.wit.simmetrics.tokenisers.TokeniserQGram3;


public class QGramsSimilarity extends AbstractARQStringSimilarity{

	public QGramsSimilarity() {
		super("QGramsSimilarity");
		
	}

	@Override
	public Double compareStrings(String element1, String element2) {	
		return getSimilarity(element1, element2);
	}

	// Limes implementation of QGramsSimilarity
	private double getSimilarity(String x, String y) {
		TokeniserQGram3 t = new TokeniserQGram3();
		Set<String> xTokens = Sets.newHashSet(t.tokenizeToArrayList(x));
		Set<String> yTokens = Sets.newHashSet(t.tokenizeToArrayList(y));
		return getSimilarity(xTokens, yTokens);
	}

	private double getSimilarity(Set<String> sourceSet, Set<String> targetSet) {
		double sourceSetSize = sourceSet.size();
		double targetSetSize = targetSet.size();
		// create a copy of X
		Set<String> targetSetCopy = Sets.newHashSet();
		for (String s : sourceSet) {
			targetSetCopy.add(s);
		}
		targetSetCopy.retainAll(targetSet); // Intersecting tokens between source and target sets
		double z = targetSetCopy.size();
		return z / (sourceSetSize + targetSetSize - z);
	}
	
}
