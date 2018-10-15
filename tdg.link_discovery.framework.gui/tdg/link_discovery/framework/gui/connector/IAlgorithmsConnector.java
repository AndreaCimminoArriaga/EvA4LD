package tdg.link_discovery.framework.gui.connector;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAlgorithmsConnector {

	
	public List<String> getListOfStoredAlgorithmsNames();
	public Boolean addAlgorithm(Map<String, String> algorithmGeneticOperators);
	public void removeAlgorithm(String algorithmName);
	public void importAlgorithmFromFile(String algorithmFile);
	
	public Set<String> getIntializersNameList();
	public Set<String> getSelectorsNameList();
	public Set<String> getReplacementsNameList();
	public Set<String> getCrossoversNameList();
	public Set<String> getMutationsNameList();
	public Set<String> getFitnessNameList();
	public Set<String> getAggregatesNameList();
	public Set<String> getAttributeLearnerNameList();
	public Set<String> getMetricsNameList();
	public Set<String> getTransformationsNameList();

	
	
}
