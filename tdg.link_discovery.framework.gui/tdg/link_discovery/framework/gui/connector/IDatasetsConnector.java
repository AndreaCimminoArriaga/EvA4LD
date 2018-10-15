package tdg.link_discovery.framework.gui.connector;

import java.util.List;

public interface IDatasetsConnector {

	
	// Datasets
	public List<String> getListOfStoredDatasets();
	public void addDataset(String filePath);
	public void removeDataset(String datasetName);
	public void importTDBDataset(String name, String datasetDirectory);
	
	
	
}
