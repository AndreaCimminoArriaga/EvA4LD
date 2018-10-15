package tdg.link_discovery.framework.algorithm;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;

import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.algorithm.setup.IAlgorithmSetup;
import tdg.link_discovery.framework.engine.IEngine;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.framework.algorithm.statistics.AlgorithmStatistics;
import tdg.link_discovery.middleware.utils.Utils;

public abstract class AbstractAlgorithm implements IAlgorithm {
	// General attrs
	protected String name;
	protected IEngine engine;
	protected List<ISpecification<?>> results;
	protected List<ISpecification<?>> bestResults;
	protected AlgorithmStatistics algorithmStatistics;
	// Setup attrs
	protected Setup setup;
	protected IAlgorithmSetup operatorsSetup;
	// Execution attrs
	protected Double algorithmProgress;
	

	
	public AbstractAlgorithm(String name, IEngine engine, Setup setup, IAlgorithmSetup operatorsSetup) {
		super();
		this.name = name;
		this.engine = engine;
		this.setup = setup;
		this.operatorsSetup = operatorsSetup;
		this.results = Lists.newArrayList();
		this.bestResults = Lists.newArrayList();
		this.algorithmStatistics = new AlgorithmStatistics();
		this.algorithmProgress=0.0;
	}

	public AbstractAlgorithm() {
		// Empty
	}

	/*
	 * Getters & setters
	 */

	/* (non-Javadoc)
	 * @see tdg.link_discovery.framework.genetics.algorithms.IAlgorithm#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see tdg.link_discovery.framework.genetics.algorithms.IAlgorithm#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see tdg.link_discovery.framework.genetics.algorithms.IAlgorithm#getEngine()
	 */
	@Override
	public IEngine getEngine() {
		return engine;
	}

	/* (non-Javadoc)
	 * @see tdg.link_discovery.framework.genetics.algorithms.IAlgorithm#setEngine(tdg.link_discovery.framework.data.enginers.IEngine)
	 */
	@Override
	public void setEngine(IEngine engine) {
		this.engine = engine;
	}

	/* (non-Javadoc)
	 * @see tdg.link_discovery.framework.genetics.algorithms.IAlgorithm#getSetup()
	 */
	@Override
	public Setup getSetup() {
		return setup;
	}

	/* (non-Javadoc)
	 * @see tdg.link_discovery.framework.genetics.algorithms.IAlgorithm#setSetup(tdg.link_discovery.framework.genetics.algorithms.Setup)
	 */
	@Override
	public void setSetup(Setup setup) {
		this.setup = setup;
	}

	/* (non-Javadoc)
	 * @see tdg.link_discovery.framework.genetics.algorithms.IAlgorithm#getOperatorsSetup()
	 */
	@Override
	public IAlgorithmSetup getOperatorsSetup() {
		return operatorsSetup;
	}

	/* (non-Javadoc)
	 * @see tdg.link_discovery.framework.genetics.algorithms.IAlgorithm#setOperatorsSetup(tdg.link_discovery.framework.operators.IOperatorsSetup)
	 */
	@Override
	public void setOperatorsSetup(IAlgorithmSetup operatorsSetup) {
		this.operatorsSetup = operatorsSetup;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public List<ISpecification<?>> getLearnedSpecifications(){
		return this.results;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ISpecification<?>> getBetterScoredSpecifications() {
		return bestResults;
	}
	
	@Override
	public AlgorithmStatistics getAlgorithmStatistics() {
		return this.algorithmStatistics;
	}
	
	
	@Override
	public void saveAlgorithmSatistics(IEnvironment environment){
		if(!environment.getAlgorithmStatisticsFile().isEmpty()){
			String datasetSource = environment.getSourceDatasetFile();
			String datasetTarget = environment.getTargetDatasetFile();
			String algorithmName = this.name;
			StringBuffer fileName = new StringBuffer();
			fileName.append("-").append(LocalDateTime.now().toString()).append("_").append(algorithmName).append(".json");
			String fileDir = environment.getAlgorithmStatisticsFile().replace(".csv", fileName.toString().replace(":","."));
			Utils.appendLineInCSV(fileDir, this.algorithmStatistics.toJSON(datasetSource, datasetTarget, algorithmName));
		}
	}
	
	@Override
	public Double getAlgorithmProgress(){
		return this.algorithmProgress;
	}
	
	/*
	 * Others
	 */
	
	
	@Override
	public String toString() {
		return "Algorithm [name=" + name + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractAlgorithm other = (AbstractAlgorithm) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
