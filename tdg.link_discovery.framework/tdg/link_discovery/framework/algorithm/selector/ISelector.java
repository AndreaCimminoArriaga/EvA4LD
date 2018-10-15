package tdg.link_discovery.framework.algorithm.selector;

import java.util.List;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.objects.FrameworkObject;

public interface ISelector extends FrameworkObject{

	public <T extends Object> List<ISpecification<T>> select(List<ISpecification<?>> specifications);
	
	@Override
	public String getName();
	@Override
	public void setName(String name);
	public Integer getArity();
	public void setArity(Integer arity);
	
	
}
