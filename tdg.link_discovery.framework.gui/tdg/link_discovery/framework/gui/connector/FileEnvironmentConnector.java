package tdg.link_discovery.framework.gui.connector;

import java.io.File;
import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;

import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.gui.desktop.GuiConfiguration;

public class FileEnvironmentConnector implements IEnvironmentsConnector {

	@Override
	public List<String> getListOfStoredEnvironments() {
		return readFilesFromDirectory(GuiConfiguration.ENVIRONMENTS_DIRECTORY);
	}
	

	
	private static List<String> readFilesFromDirectory(String directory) {
		List<String> files = Lists.newArrayList();
		try {
			File aDirectory = new File(directory);
			String[] filesInDir = aDirectory.list();
			for (int i = 0; i < filesInDir.length; i++) {
				Integer indexToCut = filesInDir[i].indexOf(".cnf");
				String environmentName = filesInDir[i].substring(0, indexToCut)
						.trim();
				files.add(environmentName);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return files;
	}

	@Override
	public void addEnvironment(IEnvironment algorithm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeEnvironemnt(IEnvironment algorithm) {
		// TODO Auto-generated method stub

	}


	@Override
	public void importEnvironmentFromFile(String environmentFile) {
		// TODO Auto-generated method stub

	}

}
