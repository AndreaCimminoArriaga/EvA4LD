package tdg.link_discovery.middleware.framework.environment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ext.com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.middleware.objects.Tuple;

public class Environment implements IEnvironment{
	
	private static Map<String, Boolean> environmentKeywords = initKeywords(); // key, mandatory key

	protected String name;
	protected String sourceDatasetFile;
	protected String targetDatasetFile;
	protected String examplesFile;
	protected String goldStandard;
	protected String specificationOutput;
	protected String linksOutput;
	protected String statisticsOutput;
	protected List<String> sourceRestrictions, targetRestrictions;
	protected List<Tuple<String,String>> suitableAttributes;
	
	@Deprecated
	public Environment(String name, String sourceDatasetFile, String targetDatasetFile, String examplesFile, String linksOutput, String specificationsOutput, List<String> sourceRestrictionsList, List<String> targetRestrictionsList) {
		super();
		this.name = name;
		this.sourceDatasetFile = sourceDatasetFile;
		this.targetDatasetFile = targetDatasetFile;
		this.examplesFile = examplesFile;
		this.linksOutput = linksOutput;
		this.specificationOutput = specificationsOutput;
		this.sourceRestrictions = sourceRestrictionsList;
		this.targetRestrictions = targetRestrictionsList;
		
	}
	@Deprecated
	public Environment(String name, String sourceDatasetFile, String targetDatasetFile, String examplesFile, String linksOutput, String specificationsOutput) {
		super();
		this.name = name;
		this.sourceDatasetFile = sourceDatasetFile;
		this.targetDatasetFile = targetDatasetFile;
		this.examplesFile = examplesFile;
		this.linksOutput = linksOutput;
		this.specificationOutput = specificationsOutput;
		this.sourceRestrictions = Lists.newArrayList();
		this.targetRestrictions = Lists.newArrayList();
		
	}
	
	public Environment(Multimap<String,String> arguments) throws Exception{
		 initEmptyAttributes();
	
		 for(String key:arguments.keySet()){
			 List<String> values = Lists.newArrayList(arguments.get(key));
			 if(key.equals("name"))
				 this.name = values.get(0);
			 if(key.equals("source_dataset"))
				 this.sourceDatasetFile = values.get(0);
			 if(key.equals("target_dataset"))
				this.targetDatasetFile = values.get(0);
			if(key.equals("examples_file"))
				this.examplesFile = values.get(0);
			if(key.equals("gold_standard"))
				this.goldStandard = values.get(0);
			if(key.equals("algorithm_statistics_file"))
				this.statisticsOutput = values.get(0);
			if(key.equals("specifications_output"))
				this.specificationOutput = values.get(0);
			if(key.equals("links_output"))
				this.linksOutput = values.get(0);
			if(key.equals("source_class_restrictions"))
				this.sourceRestrictions = values;
			if(key.equals("target_class_restrictions"))
				this.targetRestrictions = values;
			if(key.equals("suitable_attributes")){
				List<Tuple<String,String>> attributes = values.stream().map(line -> new Tuple<String,String>(line.split(",")[0].trim(),line.split(",")[1].trim())).collect(Collectors.toList());
				this.suitableAttributes = attributes;
			}	
		 }
		 checkMandatoryAttributesAreInitialized();
	}
	
	private static Map<String, Boolean> initKeywords() {
		Map<String,Boolean> environmentKeywordsTmp = Maps.newHashMap();
		environmentKeywordsTmp.put("name", true);
		environmentKeywordsTmp.put("source_dataset", true);
		environmentKeywordsTmp.put("target_dataset", true);
		environmentKeywordsTmp.put("examples_file", false);
		environmentKeywordsTmp.put("gold_standard", true);
		environmentKeywordsTmp.put("algorithm_statistics_file", true);
		environmentKeywordsTmp.put("specifications_output", true);
		environmentKeywordsTmp.put("links_output", true);
		environmentKeywordsTmp.put("source_class_restrictions", false);
		environmentKeywordsTmp.put("target_class_restrictions", false);
		environmentKeywordsTmp.put("suitable_attributes", false);
		return environmentKeywordsTmp;
	}
	
	private void initEmptyAttributes() {
		this.name = "";
		this.sourceDatasetFile = "";
		this.targetDatasetFile = "";
		this.examplesFile = "";
		this.goldStandard = "";
		this.specificationOutput = "";
		this.linksOutput = "";
		this.statisticsOutput = "";
		this.sourceRestrictions = Lists.newArrayList();
		this.targetRestrictions = Lists.newArrayList();
		this.suitableAttributes = Lists.newArrayList();
	}
	
	private void checkMandatoryAttributesAreInitialized() throws Exception {
		Boolean badInitialization = this.name.isEmpty() || this.sourceDatasetFile.isEmpty()
				|| this.targetDatasetFile.isEmpty() || this.goldStandard.isEmpty() || this.statisticsOutput.isEmpty()
				|| this.linksOutput.isEmpty() || this.specificationOutput.isEmpty();
		
		if(badInitialization)
			throw new Exception(this.getClass().getCanonicalName()+": Missing mandatory argument");
		
	}
	
	/*
	 * Getters & Setters
	 */
	

	
	
	@Override
	public String getSourceDatasetFile() {
		return sourceDatasetFile;
	}


	@Override
	public void setSourceDatasetFile(String sourceDatasetFile) {
		this.sourceDatasetFile = sourceDatasetFile;
	}

	@Override
	public String getTargetDatasetFile() {
		return targetDatasetFile;
	}

	
	@Override
	public void setTargetDatasetFile(String targetDatasetFile) {
		this.targetDatasetFile = targetDatasetFile;
	}


	@Override
	public String getExamplesFile() {
		return examplesFile;
	}


	@Override
	public void setExamplesFile(String examplesFile) {
		this.examplesFile = examplesFile;
	}


	@Override
	public List<String> getSourceRestrictions() {
		return sourceRestrictions;
	}


	@Override
	public void setSourceRestrictions(List<String> sourceRestrictions) {
		this.sourceRestrictions = sourceRestrictions;
	}

	@Override
	public List<String> getTargetRestrictions() {
		return targetRestrictions;
	}

	@Override
	public void setTargetRestrictions(List<String> targetRestrictions) {
		this.targetRestrictions = targetRestrictions;
	}

	
	@Override
	public String getSpecificationOutput() {
		return specificationOutput;
	}


	@Override
	public void setSpecificationOutput(String specificationOutput) {
		this.specificationOutput = specificationOutput;
	}


	@Override
	public String getLinksOutput() {
		return linksOutput;
	}

	
	@Override
	public void setLinksOutput(String linksOutput) {
		this.linksOutput = linksOutput;
	}


	@Override
	public List<Tuple<String, String>> getSuitableAttributes() {
		return suitableAttributes;
	}


	@Override
	public void setSuitableAttributes(List<Tuple<String, String>> suitableAttributes) {
		this.suitableAttributes = suitableAttributes;
	}

	@Override
	public String getGoldStandardFile() {
		return goldStandard;
	}

	@Override
	public void setGoldStandardFile(String goldStandard) {
		this.goldStandard = goldStandard;
	}

	@Override
	public String getAlgorithmStatisticsFile() {
		return statisticsOutput;
	}

	@Override
	public void setAlgorithmStatisticsFile(String file) {
		this.statisticsOutput = file;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	

	public static Map<String, Boolean> getEnvironmentKeywords() {
		return environmentKeywords;
	}
	
	public static Boolean isValidKeyword(String keyword){
		return environmentKeywords.containsKey(keyword);
	}
	

	
	public static Boolean isValidMandatoryKeyword(String keyword){
		Boolean isValidMandatoryKeyWord = environmentKeywords.entrySet().stream().filter(tuple -> tuple.getValue()==true).anyMatch(tuple -> tuple.getKey().equals(keyword));
		return isValidMandatoryKeyWord;
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
		Environment other = (Environment) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
}
