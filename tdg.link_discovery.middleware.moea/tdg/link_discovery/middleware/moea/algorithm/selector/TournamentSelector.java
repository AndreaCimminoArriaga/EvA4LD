package tdg.link_discovery.middleware.moea.algorithm.selector;

import java.util.List;
import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.moeaframework.core.Population;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.operator.TournamentSelection;

import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.algorithm.selector.AbstractSelector;


public class TournamentSelector  extends AbstractSelector  implements Selection{

	private TournamentSelection selector;
	
	
	public TournamentSelector(){
		super("TournamentSelection", 5);
		this.selector = new TournamentSelection(5);
	}
	
	public TournamentSelector(Integer size){
		super("TournamentSelection", size);
		this.selector = new TournamentSelection(size);
	}
	
	
	@Override
	public <T> List<ISpecification<T>> select( List<ISpecification<?>> specifications) {
		//empty -> already implemented by moea
		return null;
	}

	@Override
	public void setArity(Integer arity) {
		this.arity = arity;
		this.selector = new TournamentSelection(arity);
	}

	@Override
	public Solution[] select(int arity, Population population) {
		return selector.select(arity, population);
	}
		
	@Override
	public Map<String, Double> getInputDefaultParameters() {
		Map<String,Double> parameters = Maps.newHashMap();
		parameters.put("tournament_size", 5.0);
		return parameters;
	}

	@Override
	public Boolean hasInputParameters() {
		return true;
	}

	@Override
	public void setInputParameters(String parameterName, Object value) {
		if(parameterName.equals("tournament_size"))
			setArity((Integer) value);
		
	}
	

}
