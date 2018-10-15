package tdg.link_discovery.middleware.framework.algorithm.statistics;

import java.util.Collection;
import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import tdg.link_discovery.framework.algorithm.setup.IAlgorithmSetup;

public class AlgorithmStatistics {

	protected Integer maximumIterations;
	protected Integer executedIterations;
	protected List<Double> averageFitness;
	protected IAlgorithmSetup algorithmSetup;
	protected Multimap<Integer,Double> fitnessEvolution;
	protected long trainingTime; // in milliseconds
	
	public AlgorithmStatistics(){
		maximumIterations = 0;
		executedIterations = 0;
		averageFitness = Lists.newArrayList();
		fitnessEvolution = ArrayListMultimap.create();
		trainingTime=0;
	}
	
	public Integer getMaximumIterations() {
		return maximumIterations;
	}
	public void setMaximumIterations(Integer maximumIterations) {
		this.maximumIterations = maximumIterations;
	}
	public Integer getExecutedIterations() {
		return executedIterations;
	}
	public void setExecutedIterations(Integer executedIterations) {
		this.executedIterations = executedIterations;
	}
	
	public List<Double> getAverageFitness() {
		return averageFitness;
	}
	public void setAverageFitness(List<Double> averageFitness) {
		this.averageFitness = averageFitness;
	}
	
	
	public IAlgorithmSetup getAlgorithmSetup() {
		return algorithmSetup;
	}

	public void setAlgorithmSetup(IAlgorithmSetup algorithmSetup) {
		this.algorithmSetup = algorithmSetup;
	}

	public void addAverageFitness(Double fitness){
		this.averageFitness.add(fitness);
	}
	public void addFitness(List<Double> fitness){
		Double sum = fitness.stream().mapToDouble(s -> Double.valueOf(s)).sum();
		Double avg = sum/fitness.size();
		if(avg.isNaN())
			avg=-1.0;
		this.averageFitness.add(avg);
	}

	public Multimap<Integer, Double> getFitnessEvolution() {
		return fitnessEvolution;
	}

	public void setFitnessEvolution(Multimap<Integer, Double> fitnessEvolution) {
		this.fitnessEvolution = fitnessEvolution;
	}
	
	public void addFitnessEvolution(Integer iteration, Collection<Double> fitnessScores){
		fitnessScores.stream().forEach(score -> this.fitnessEvolution.put(iteration, score));
	}

	public long getTrainingTime() {
		return trainingTime;
	}

	public void setTrainingTime(long trainingTime) {
		this.trainingTime = trainingTime;
	}
	
	public String toJSON(String datasetSource, String datasetTarget, String algorithmName){
		StringBuffer str = new StringBuffer();
		str.append("{\n");
		str.append("\"source_dataset\" : \"").append(datasetSource).append("\",\n");
		str.append("\"target_dataset\" : \"").append(datasetSource).append("\",\n");
		str.append("\"algorithm\" : \"").append(algorithmName).append("\",\n");
		str.append("\"genetic_operators\" : ").append(jsonGeneticOperators()).append(",\n");
		
		str.append("\"max_iterations\" : ").append(getMaximumIterations()).append(",\n");
		str.append("\"executed_iterations\" : ").append(getExecutedIterations()).append(",\n");
		str.append("\"average_fitness_evolution\" : ").append(getAverageFitness().toString()).append(",\n");
		str.append("\"fitness_evolution\" : [");
		for(Integer key:this.fitnessEvolution.keySet())
			str.append("{\"iteration\":").append(key).append(",\n \"fitness\":").append(this.fitnessEvolution.get(key).toString()).append("},");
		str.replace(str.lastIndexOf(","), str.lastIndexOf(",")+1, "");
		str.append("]\n");
		 
		str.append("\n}");
		return str.toString();
	}
	
	private String jsonGeneticOperators(){
		StringBuffer str = new StringBuffer();
		str.append("{");
		str.append("\"initializer\":");
		str.append("\"").append(getAlgorithmSetup().getSpecificationInitializer().getName()).append("\",");
		str.append("\"selector\":");
		str.append("\"").append(getAlgorithmSetup().getSelector().getName()).append("\",");
		str.append("\"crossover\":");
		str.append("\"").append(getAlgorithmSetup().getCrossover().getName()).append("\",");
		str.append("\"mutation\":");
		str.append("\"").append(getAlgorithmSetup().getMutation().getName()).append("\",");
		str.append("\"replacement\":");
		str.append("\"").append(getAlgorithmSetup().getReplacement().getName()).append("\",");
		str.append("\"fitness\":");
		str.append("\"").append(getAlgorithmSetup().getFitness().get(0).getName()).append("\"");
		str.append("}");
		return str.toString();
	}
	

}
