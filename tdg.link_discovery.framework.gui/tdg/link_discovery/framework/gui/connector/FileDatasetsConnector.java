package tdg.link_discovery.framework.gui.connector;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;

import tdg.link_discovery.framework.gui.desktop.GuiConfiguration;

public class FileDatasetsConnector implements IDatasetsConnector{

	@Override
	public List<String> getListOfStoredDatasets() {
		return readFilesFromDirectory(GuiConfiguration.DATA_DIRECTORY);
	}
	
	 private static List<String> readFilesFromDirectory(String directory){
			List<String> files = Lists.newArrayList();
			try{
				 File aDirectory = new File(directory);
				 String[] filesInDir = aDirectory.list();
				 for(int i=0; i<filesInDir.length; i++ ){
					 String newDir = directory.concat(filesInDir[i]);
					 String size =  FileUtils.byteCountToDisplaySize(FileUtils.sizeOf(new File(newDir)));
					 StringBuffer str = new StringBuffer();
					 str.append(filesInDir[i]).append(" (").append(size).append(")");
				     files.add(str.toString());
				     
				 }
			}catch(Exception e){
				e.printStackTrace();
			}
			return files;
		}

	@Override
	public void addDataset(String filePath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeDataset(String datasetName) {
		try{
			FileUtils.deleteDirectory(new File(GuiConfiguration.DATA_DIRECTORY+""+datasetName));
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}

	@Override
	public void importTDBDataset(String name, String datasetDirectory) {
		// TODO Auto-generated method stub
		
	}

}
