package tdg.link_discovery.middleware.moea.genetics.variation.mutations;

import java.util.List;
import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import tdg.link_discovery.connector.sparql.algorithm.initializer.GenLinkCreator;
import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.algorithm.variations.AbstractVariation;
import tdg.link_discovery.middleware.moea.algorithm.individual.LinkSpecification;
import tdg.link_discovery.middleware.moea.genetics.variation.crossovers.GenLinkCrossover;
import tdg.link_discovery.middleware.objects.Tree;
import tdg.link_discovery.middleware.utils.Utils;

public class GenLinkMutation extends AbstractVariation<Tree> implements Variation  {

	protected Integer arity;
	protected GenLinkCreator lsGenerator;
	

	public GenLinkMutation(){
		super(1, 1, 0.5, "genlink.mutation");
		this.arity = 1;
		lsGenerator = new GenLinkCreator();
	}
	
	public GenLinkMutation(Double probability){
		super(1, 1, probability, "genlink.mutation");
		this.arity = this.inputArity;
		lsGenerator = new GenLinkCreator();
	}
	
	@Override
	public int getArity() {
		return arity;
	}
	

	@Override
	public Solution[] evolve(Solution[] parents) {
		Solution[] result = parents.clone();
		Double randomProbability = Utils.getRandomInteger(100, 1)/100.0;
		if( randomProbability<=this.probability){
				Solution s1 =  parents[0].copy();
				// Obtain a random solution to cross with parents[0]
				LinkSpecification ls1 = (LinkSpecification) s1.getVariable(0);
				List<ISpecification<Tree>> genitors = Lists.newArrayList();
				genitors.add(ls1);
				// Cross the trees 
				List<ISpecification<Tree>> offspring = crossParents(genitors);
				// return solutions
				LinkSpecification evolvedChildren = (LinkSpecification) offspring.get(0);
				s1.setVariable(0, evolvedChildren);
				s1.setObjective(0, 0.0);
				result[0] = s1;
				result[1] = parents[1].copy();
		}
		return result;
	}

	@Override
	public List<ISpecification<Tree>> crossParents(List<ISpecification<Tree>> parents) {
		// Selecting parents to cross
		LinkSpecification ls1 = (LinkSpecification) parents.get(0);
		LinkSpecification ls2 = (LinkSpecification) lsGenerator.createLinkSpecification();
		// Crossing
		List<ISpecification<Tree>> newParents = Lists.newArrayList();
		newParents.add(ls1);
		newParents.add(ls2);
		GenLinkCrossover genCross = new GenLinkCrossover(1.0);
		List<ISpecification<Tree>> offspring = genCross.crossParents(newParents);
		
		return offspring;
	}
	
	


	
	@Override
	public Boolean hasInputParameters() {
		return true;
	}

	@Override
	public Map<String, Double> getInputDefaultParameters() {
		Map<String,Double> defaultParameters = Maps.newHashMap();
		defaultParameters.put("mutation_rate", 0.5);
		defaultParameters.put("crossover_input_arity", 2.0);
		defaultParameters.put("crossover_output_arity", 1.0);
		return defaultParameters;
	}

	@Override
	public void setInputParameters(String parameterName, Object value) {
		if(parameterName.equals("mutation_rate"))
			setProbability((Double) value);
		if(parameterName.equals("input_arity"))
			setInputArity((Integer) value);
		if(parameterName.equals("output_arity"))
			setOuputArity((Integer) value);
		
	}

}
