package tdg.link_discovery.framework.environment;

import java.util.List;

import tdg.link_discovery.middleware.objects.Tuple;


public interface IEnvironment {

	public String getName();
	
	public void setName(String name);
	
	public  String getSourceDatasetFile();

	public  void setSourceDatasetFile(String sourceDatasetFile);

	public  String getTargetDatasetFile();

	public  void setTargetDatasetFile(String targetDatasetFile);

	public  String getExamplesFile();

	public  void setExamplesFile(String examplesFile);

	public  List<String> getSourceRestrictions();

	public  void setSourceRestrictions(List<String> sourceRestrictions);

	public  List<String> getTargetRestrictions();

	public  void setTargetRestrictions(List<String> targetRestrictions);

	public  String getSpecificationOutput();

	public  void setSpecificationOutput(String specificationOutput);

	public  String getLinksOutput();

	public  void setLinksOutput(String linksOutput);

	public  List<Tuple<String, String>> getSuitableAttributes();

	public  void setSuitableAttributes(List<Tuple<String, String>> suitableAttributes);

	public String getGoldStandardFile();
	
	public void setGoldStandardFile(String goldStandard);
	
	public String getAlgorithmStatisticsFile();
	
	public void setAlgorithmStatisticsFile(String file);
	

}