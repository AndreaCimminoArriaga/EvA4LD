package tdg.link_discovery.framework.learner.attributes;

import java.util.List;
import java.util.Set;

import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.objects.FrameworkObject;
import tdg.link_discovery.middleware.objects.Tuple;

public interface IAttributeSelector extends FrameworkObject{

	public Set<Tuple<String,String>> getAttributesToCompare();

	public IEnvironment getEnvironment();

	public void setEnvironment(IEnvironment environment);

	public void  setPositiveExamples(Set<Tuple<String,String>> positiveExamples);
	public void  setNegativeExamples(Set<Tuple<String,String>> negativeExamples);
	public void  setModels(List<List<String>> modelsName);
}