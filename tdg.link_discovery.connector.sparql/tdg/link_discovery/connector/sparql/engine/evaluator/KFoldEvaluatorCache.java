package tdg.link_discovery.connector.sparql.engine.evaluator;

import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import tdg.link_discovery.middleware.objects.Tuple;

public class KFoldEvaluatorCache {

	
	
	public Set<Tuple<String,String>> positiveReferenceLinks, negativeReferenceLinks;
	public Boolean isTrainingFold;
	
	
	public KFoldEvaluatorCache() {
		positiveReferenceLinks = Sets.newConcurrentHashSet();
		negativeReferenceLinks = Sets.newConcurrentHashSet();
	}
	
	public Boolean getIsTrainingFold() {
		return isTrainingFold;
	}

	public void setIsTrainingFold(Boolean isTrainingFold) {
		this.isTrainingFold = isTrainingFold;
	}
	
	public Set<Tuple<String, String>> getPositiveReferenceLinks() {
		return positiveReferenceLinks;
	}

	public void setPositiveReferenceLinks(Set<Tuple<String, String>> positiveReferenceLinks) {
		this.positiveReferenceLinks = positiveReferenceLinks;
	}

	public Set<Tuple<String, String>> getNegativeReferenceLinks() {
		return negativeReferenceLinks;
	}

	public void setNegativeReferenceLinks(Set<Tuple<String, String>> negativeReferenceLinks) {
		this.negativeReferenceLinks = negativeReferenceLinks;
	}
	
	
	
}
