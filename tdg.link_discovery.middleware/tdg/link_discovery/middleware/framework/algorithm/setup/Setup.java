package tdg.link_discovery.middleware.framework.algorithm.setup;

import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Maps;

import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;

public class Setup {

	private Map<String, Number> configuration;
	
	public Setup(){
		this.configuration = Maps.newHashMap();
		this.configuration.put(FrameworkConfiguration.SETUP_MAX_ITERATION_INFILE_TOKEN, null);
		this.configuration.put(FrameworkConfiguration.SETUP_POPULATION_SIZE_INFILE_TOKEN, null);
		this.configuration.put(FrameworkConfiguration.SETUP_CROSSOVER_RATE_INFILE_TOKEN, 0.0);
		this.configuration.put(FrameworkConfiguration.SETUP_MUTATION_RATE_INFILE_TOKEN, 0.0);
		this.configuration.put(FrameworkConfiguration.SETUP_NUM_ALGORITHM_OBJECTIVES_INFILE_TOKEN, 1);
		this.configuration.put(FrameworkConfiguration.SETUP_NUM_AGLORITHM_VARIABLES_INFILE_TOKEN, 1);
		this.configuration.put(FrameworkConfiguration.SETUP_PARENTS_SELECTION_RATE_INFILE_TOKEN, null);
		this.configuration.put(FrameworkConfiguration.SETUP_GENERATIONS_RATE_INFILE_TOKEN, null);
	}
	
	public Setup(Number max_iterations, Number population_size, Number crossoverRate, Number mutationRate){
		this.configuration = Maps.newHashMap();
		addMaxIterations(max_iterations);
		addCrossoverRate(crossoverRate);
		addMutationRate(mutationRate);
		addPopulationSize(population_size);
	}
	
	public Map<String, Number> getSetup() {
		return configuration;
	}
	
	public void addOtherParameter(String parameter, Number value){
		if(!configuration.containsKey(parameter)){
			configuration.put(parameter, value);
		}
	}
	
	public void addMaxIterations(Number value){
		configuration.put(FrameworkConfiguration.SETUP_MAX_ITERATION_INFILE_TOKEN, value);
	}
	
	public void addPopulationSize(Number value){
		configuration.put(FrameworkConfiguration.SETUP_POPULATION_SIZE_INFILE_TOKEN, value);	
	}
	
	public void addCrossoverRate(Number value){
		configuration.put(FrameworkConfiguration.SETUP_CROSSOVER_RATE_INFILE_TOKEN, value);
	}
	
	public void addMutationRate(Number value){
		configuration.put(FrameworkConfiguration.SETUP_MUTATION_RATE_INFILE_TOKEN, value);
	}
	
	public void addObjectives(Number value){
		configuration.put(FrameworkConfiguration.SETUP_NUM_ALGORITHM_OBJECTIVES_INFILE_TOKEN, value);
	}
	
	public void addParentsSelectionSize(Number value){
		configuration.put(FrameworkConfiguration.SETUP_PARENTS_SELECTION_RATE_INFILE_TOKEN, value);
	}
	
	public void addGenerationsRate(Number value){
		configuration.put(FrameworkConfiguration.SETUP_GENERATIONS_RATE_INFILE_TOKEN, value);
	}
	
	public void addVariables(Number value){
		configuration.put(FrameworkConfiguration.SETUP_NUM_AGLORITHM_VARIABLES_INFILE_TOKEN, value);
	}
	
	public Integer getMaxIterations(){
		return  configuration.get(FrameworkConfiguration.SETUP_MAX_ITERATION_INFILE_TOKEN).intValue();
	}
	
	public Integer getPopulationSize(){
		return  configuration.get(FrameworkConfiguration.SETUP_POPULATION_SIZE_INFILE_TOKEN).intValue();	
	}
	
	public Double getCrossoverRate(){
		return  configuration.get(FrameworkConfiguration.SETUP_CROSSOVER_RATE_INFILE_TOKEN).doubleValue();
	}
	
	public Double getMutationRate(){
		return  configuration.get(FrameworkConfiguration.SETUP_MUTATION_RATE_INFILE_TOKEN).doubleValue();
	}
	
	public Integer getObjectives(){
		return configuration.get(FrameworkConfiguration.SETUP_NUM_ALGORITHM_OBJECTIVES_INFILE_TOKEN).intValue();
	}
	
	public Integer getParentsSelectionSize(){
		return configuration.get(FrameworkConfiguration.SETUP_PARENTS_SELECTION_RATE_INFILE_TOKEN).intValue();
	}
	
	public Integer getGenerationsRate(){
		return configuration.get(FrameworkConfiguration.SETUP_GENERATIONS_RATE_INFILE_TOKEN).intValue();
	}
	
	public Integer getVariables(){
		return configuration.get(FrameworkConfiguration.SETUP_NUM_AGLORITHM_VARIABLES_INFILE_TOKEN).intValue();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		this.configuration.entrySet().stream().forEach(pair -> str.append(pair.getKey()).append(" := ").append(pair.getValue()).append("\n"));
		return str.toString();
	}
	
	
	
}
