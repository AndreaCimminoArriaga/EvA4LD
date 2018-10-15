package tdg.link_discovery.framework.algorithm.setup;

import java.util.List;

import tdg.link_discovery.framework.algorithm.fitness.IFitness;
import tdg.link_discovery.framework.algorithm.individual.initializer.ISpecificationInitializer;
import tdg.link_discovery.framework.algorithm.replacement.IReplacement;
import tdg.link_discovery.framework.algorithm.selector.ISelector;
import tdg.link_discovery.framework.algorithm.variations.IVariation;

public interface IAlgorithmSetup {

	public <T extends Object> ISelector getSelector();

	public <T extends Object> void setSelector(ISelector selector);

	public IReplacement getReplacement();

	public void setReplacement(IReplacement replacement);

	public <T extends Object> IVariation<T> getCrossover();

	public <T extends Object> void setCrossover(IVariation<T> crossover);

	public <T extends Object> IVariation<T> getMutation();

	public <T extends Object> void setMutation(IVariation<T> mutation);

	public List<IFitness> getFitness();

	public void setFitness(List<IFitness> fitness);

	public ISpecificationInitializer getSpecificationInitializer();

	public abstract void setSpecificationInitializer(
			ISpecificationInitializer specificationInitializer);


}