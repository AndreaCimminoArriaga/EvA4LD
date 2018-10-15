package tdg.link_discovery.framework.algorithm.individual;

import java.util.ArrayList;
import java.util.List;





public abstract class AbstractSpecification<T> implements ISpecification<T> {

	protected T specificationRepresentation;
	protected List<String> sourceRestrictions;
	protected List<String> targetRestrictions;
	protected List<Double> scores;
	
	public AbstractSpecification(){
		specificationRepresentation = null;
		sourceRestrictions = new ArrayList<String>();
		targetRestrictions = new ArrayList<String>();
		scores = new ArrayList<Double>();
	}
	
	public AbstractSpecification(T specificationRepresentation){
		this.specificationRepresentation = specificationRepresentation;
	}
	
	/*
	 * Getterns & Setters
	 */
	
	@Override
	public void setSpecificationRepresentation(T specificationRepresentation){
		this.specificationRepresentation =  specificationRepresentation;
	}

	@Override
	public List<String> getSourceRestrictions() {
		return sourceRestrictions;
	}

	@Override
	public void setSourceRestrictions(List<String> sourceRestrictions) {
		this.sourceRestrictions = sourceRestrictions;
	}

	@Override
	public List<String> getTargetRestrictions() {
		return targetRestrictions;
	}

	@Override
	public void setTargetRestrictions(List<String> targetRestrictions) {
		this.targetRestrictions = targetRestrictions;
	}

	@Override
	public T getSpecificationRepresentation() {
		return specificationRepresentation;
	}
		
	@Override
	public List<Double> getSpecificationScores() {
		return scores;
	}
	
	@Override
	public Double getSpecificationScore() {
		return scores.get(0);
	}

	@Override
	public void setSpecificationScores(List<Double> scores) {
		this.scores = scores;
	}
	
	@Override
	public void setSpecificationScore(Double scores) {
		this.scores.add(scores);
	}
	
	
	/*
	 * HashC & Equals
	 */
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((specificationRepresentation == null) ? 0
						: specificationRepresentation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractSpecification<?> other = (AbstractSpecification<?>) obj;
		if (specificationRepresentation == null) {
			if (other.specificationRepresentation != null)
				return false;
		} else if (!specificationRepresentation
				.equals(other.specificationRepresentation))
			return false;
		return true;
	}
	
	
	/*
	 * To string
	 */
	
	@Override
	public String toString() {
		return specificationRepresentation.toString();
	}


	
	
	
	
	
	
}
