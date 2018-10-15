package tdg.link_discovery.framework.algorithm.setup;

import java.util.List;

import tdg.link_discovery.framework.algorithm.fitness.IFitness;
import tdg.link_discovery.framework.algorithm.individual.initializer.ISpecificationInitializer;
import tdg.link_discovery.framework.algorithm.replacement.IReplacement;
import tdg.link_discovery.framework.algorithm.selector.ISelector;
import tdg.link_discovery.framework.algorithm.variations.IVariation;

public class  AlgorithmSetup implements IAlgorithmSetup {

	// Evoluationary setup
	protected ISelector selector;
	protected IReplacement replacement;
	protected IVariation<?> crossover;
	protected IVariation<?> mutation;
	protected List<IFitness> fitness;
	protected ISpecificationInitializer specificationInitializer;
	
	
	public AlgorithmSetup(ISelector selector, IReplacement replacement,
			IVariation<?> crossover, IVariation<?> mutation, List<IFitness> fitness,
			ISpecificationInitializer specificationInitializer) {
		super();
		this.selector = selector;
		this.replacement = replacement;
		this.crossover = crossover;
		this.mutation = mutation;
		this.fitness = fitness;
		this.specificationInitializer = specificationInitializer;
	}


	@Override
	public ISelector getSelector() {
		return selector;
	}


	@Override
	public void setSelector(ISelector selector) {
		this.selector = selector;
	}


	@Override
	public IReplacement getReplacement() {
		return replacement;
	}


	@Override
	public void setReplacement(IReplacement replacement) {
		this.replacement = replacement;
	}


	@SuppressWarnings("unchecked")
	@Override
	public IVariation<?> getCrossover() {
		return crossover;
	}


	@Override
	public <T extends Object> void setCrossover(IVariation<T> crossover) {
		this.crossover = crossover;
	}


	@SuppressWarnings("unchecked")
	@Override
	public IVariation<?> getMutation() {
		return mutation;
	}


	@Override
	public <T extends Object> void setMutation( IVariation<T> mutation) {
		this.mutation = mutation;
	}


	@Override
	public List<IFitness> getFitness() {
		return fitness;
	}


	@Override
	public void setFitness(List<IFitness> fitness) {
		this.fitness = fitness;
	}


	@Override
	public ISpecificationInitializer getSpecificationInitializer() {
		return specificationInitializer;
	}

	
	@Override
	public void setSpecificationInitializer(
			ISpecificationInitializer specificationInitializer) {
		this.specificationInitializer = specificationInitializer;
	}


	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.selector.getName()).append("\n");
		str.append(this.crossover.getName()).append("\n");
		str.append(this.mutation.getName()).append("\n");
		str.append(this.replacement.getName()).append("\n");
		str.append(this.specificationInitializer.getName()).append("\n");
		this.fitness.stream().forEach(fitF -> str.append(fitF.getName()).append(","));
		str.append("\n");
		return str.toString();
	}



	
}
