package tdg.link_discovery.framework.gui.connector;

import java.util.List;

import tdg.link_discovery.framework.environment.IEnvironment;

public interface IEnvironmentsConnector {

	
	public List<String> getListOfStoredEnvironments();
	public void addEnvironment(IEnvironment algorithm);
	public void removeEnvironemnt(IEnvironment algorithm);
	public void importEnvironmentFromFile(String environmentFile);
}
