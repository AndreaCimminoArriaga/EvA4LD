package tdg.link_discovery.framework.algorithm;

import java.util.List;

import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.algorithm.setup.IAlgorithmSetup;
import tdg.link_discovery.framework.engine.IEngine;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.framework.algorithm.statistics.AlgorithmStatistics;

public interface IAlgorithm {

	public void learnSpecifications();
	
	public String getName();

	public void setName(String name);

	public IEngine getEngine();

	public void setEngine(IEngine engine);

	public Setup getSetup();

	public void setSetup(Setup setup);

	public <T> IAlgorithmSetup getOperatorsSetup();

	public void setOperatorsSetup(IAlgorithmSetup operatorsSetup);

	public <T extends Object>List<ISpecification<T>> getLearnedSpecifications();
	
	public <T extends Object>List<ISpecification<T>> getBetterScoredSpecifications();
	
	public AlgorithmStatistics getAlgorithmStatistics();
	
	public void saveAlgorithmSatistics(IEnvironment environment);
	
	public Double getAlgorithmProgress();
}