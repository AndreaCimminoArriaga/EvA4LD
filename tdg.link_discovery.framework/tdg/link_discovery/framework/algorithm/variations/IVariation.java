package tdg.link_discovery.framework.algorithm.variations;

import java.util.List;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.objects.FrameworkObject;

public interface IVariation<T> extends FrameworkObject{

	public List<ISpecification<T>> crossParents( List<ISpecification<T>> parents);

	public Integer getInputArity();

	public void setInputArity(Integer inputArity);

	public Integer getOuputArity();

	public void setOuputArity(Integer ouputArity);

	public Double getProbability();

	public void setProbability(Double probability);

	@Override
	public boolean equals(Object obj);
	

}