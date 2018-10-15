package tdg.link_discovery.framework.algorithm.individual;

import java.util.List;

public interface ISpecification<T> {

	public void setSpecificationRepresentation(T specificationRepresentation);
	public T getSpecificationRepresentation();
	public List<Double> getSpecificationScores();
	public Double getSpecificationScore();
	public void setSpecificationScores(List<Double> score);
	public void setSpecificationScore(Double score);
	public List<String> getSourceRestrictions();
	public List<String> getTargetRestrictions();
	public void setSourceRestrictions(List<String> sourceRestrictions);
	public void setTargetRestrictions(List<String> targetRestrictions);
}