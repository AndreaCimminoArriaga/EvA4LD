package tdg.link_discovery.connector.sparql.evaluator.arq.linker.string_similarities;

import java.util.TreeSet;


public class TrigramsSimilarity extends AbstractARQStringSimilarity{

	//private NGram ngram;
	
	public TrigramsSimilarity() {
		super("TrigramsSimilarity");
		//ngram = new NGram(3);
	}

	@Override
	public Double compareStrings(String element1, String element2) {	
		//Double score = 1- ngram.distance(element1, element2);
		return getSimilarity(element1, element2);
	}

	// LIMES implementation of TrigramsSimilarity
	private double getSimilarity(Object object1, Object object2) {
        String p1 = "  " + object1 + "  ";
        String p2 = "  " + object2 + "  ";

        if (p1.length() == 4 && p2.length() == 4)
            return 1.0;
        if ((p1.length() == 4 && p2.length() > 4) || (p2.length() == 4 && p1.length() > 4))
            return 0.0;
        TreeSet<String> t1 = getTrigrams(p1);
        TreeSet<String> t2 = getTrigrams(p2);
        double counter = 0;
        for (String s : t1) {
            if (t2.contains(s))
                counter++;
        }
        return 2 * counter / (t1.size() + t2.size());
    }

    private static TreeSet<String> getTrigrams(String a) {
        TreeSet<String> result = new TreeSet<String>();
        String copy = a;

        for (int i = 2; i < copy.length(); i++) {
            result.add(copy.substring(i - 2, i));
        }
        return result;
    }
	
	
}
