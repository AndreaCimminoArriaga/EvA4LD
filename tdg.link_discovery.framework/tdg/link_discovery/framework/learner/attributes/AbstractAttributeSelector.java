package tdg.link_discovery.framework.learner.attributes;

import java.util.List;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;

import com.google.common.collect.Lists;

import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.objects.Tuple;

public abstract class AbstractAttributeSelector implements IAttributeSelector  {


	protected IEnvironment environment;
	protected String name;
	protected Set<Tuple<String,String>> positiveExamples;
	protected Set<Tuple<String,String>> negativeEamples;
	protected List<List<String>> models;
	
	public AbstractAttributeSelector() {
		this.name = "unnamed attribute selector";
		positiveExamples = Sets.newHashSet();
		negativeEamples = Sets.newHashSet();
		models = Lists.newArrayList();
	}
	
	public AbstractAttributeSelector(IEnvironment environment) {
		super();
		this.environment = environment;
		this.name = "unnamed attribute selector";
		positiveExamples = Sets.newHashSet();
		negativeEamples = Sets.newHashSet();
	}

	public AbstractAttributeSelector(IEnvironment environment,String name) {
		super();
		this.environment = environment;
		this.name = name;
		positiveExamples = Sets.newHashSet();
		negativeEamples = Sets.newHashSet();
	}


	@Override
	public IEnvironment getEnvironment() {
		return environment;
	}

	@Override
	public void setEnvironment(IEnvironment environment) {
		this.environment = environment;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void setPositiveExamples(Set<Tuple<String, String>> positiveExamples) {
		this.positiveExamples = positiveExamples;
	}

	public void setNegativeExamples(Set<Tuple<String, String>> negativeEamples) {
		this.negativeEamples = negativeEamples;
	}
	
	public void  setModels(List<List<String>> modelsName) {
		this.models = modelsName;
	}
	
}
