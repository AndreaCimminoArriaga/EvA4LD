package tdg.link_discovery.middleware.framework.learner;

import java.util.Set;

import com.google.common.collect.Sets;

import tdg.link_discovery.connector.sparql.learner.functions.StringMetricPruner;
import tdg.link_discovery.framework.environment.IEnvironment;
import tdg.link_discovery.framework.learner.AbstractLearner;
import tdg.link_discovery.framework.learner.functions.FunctionsFactory;
import tdg.link_discovery.middleware.framework.algorithm.setup.Setup;
import tdg.link_discovery.middleware.framework.configuration.FrameworkConfiguration;
import tdg.link_discovery.middleware.objects.Tuple;

public class GenericLearner extends AbstractLearner{

	
	public GenericLearner(IEnvironment configuration, Setup algorithmSetup){
		super(configuration, algorithmSetup);
	}
		
	
	private void logging() {
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "--- Start learner");
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "Space Reduction:");
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t metrics reduction: "+FrameworkConfiguration.REDUCE_SEARCH_SPACE_METRICS);
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t metrics that will be used:");
		FunctionsFactory.similarityFunctions.forEach(metric -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t metric: "+metric));
		
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t transformations block:" +FrameworkConfiguration.APPLY_STRING_TRANSFORMATIONS_BLOCK);
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t attributes selected automatically:" +(this.attributeSelector!=null));
		if((this.attributeSelector!=null))
			FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t attributes selector:" +this.attributeSelector.getClass().getSimpleName());
		FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t Attributes that will be used:");
		FunctionsFactory.suitableAttributes.forEach(attr -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t attr: "+attr));
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "--- Start learner");
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "Space Reduction:");
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t metrics reduction: "+FrameworkConfiguration.REDUCE_SEARCH_SPACE_METRICS);
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t\t metrics that will be used:");
		FunctionsFactory.similarityFunctions.forEach(metric -> FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t\t metric: "+metric));
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t transformations block:" +FrameworkConfiguration.APPLY_STRING_TRANSFORMATIONS_BLOCK);
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t attributes selected automatically:" +(this.attributeSelector!=null));
		if((this.attributeSelector!=null))
			FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t\t attributes selector:" +this.attributeSelector.getClass().getSimpleName());
		FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t Attributes that will be used:");
		FunctionsFactory.suitableAttributes.forEach(attr -> FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t\t attr: "+attr));	
	}
	
	
	@Override
	public void learnSpecifications() {
		Set<Tuple<String, String>> attributesToCompare = Sets.newHashSet();
	
		if(this.attributeSelector!=null){
			attributesToCompare = this.attributeSelector.getAttributesToCompare();
			if(!attributesToCompare.isEmpty()) {
				this.cleanSuitableAttributes(); // remove this if you want to combine manual attributes with automatic learned
			
			
			}else {
				System.out.println("WARNING: Attribute learner "+this.attributeSelector.getName()+" did not output any attributes to compare");
				FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t Attribute learner "+this.attributeSelector.getName()+" did not output any attributes to compare");
				FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t Attribute learner "+"+this.attributeSelector.getName()+"+" did not output any attributes to compare");
			}
			attributesToCompare.stream().forEach(tuple-> addSuitableAttributes(tuple.getFirstElement().trim(), tuple.getSecondElement().trim())); 
		}
		logging(); // does not show the metrics reduction
		try{
			if(!linkEnvironment.getSuitableAttributes().isEmpty() || existAttributesToCompare()){
				if(FrameworkConfiguration.REDUCE_SEARCH_SPACE_METRICS){
					StringMetricPruner strP = new StringMetricPruner();
					strP.pruneAvailableStringMetrics(linkEnvironment);
					FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t metrics that will be used afeter the reduction:");
					FunctionsFactory.similarityFunctions.forEach(metric -> FrameworkConfiguration.traceLog.addLogLine(this.getClass().getSimpleName(), "\t\t metric: "+metric));
					FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t metrics that will be used afeter the reduction:");
					FunctionsFactory.similarityFunctions.forEach(metric -> FrameworkConfiguration.resultsLog.addLogLine(this.getClass().getSimpleName(), "\t\t metric: "+metric));
					
					if(FunctionsFactory.similarityFunctions.isEmpty())
						this.initLearnerFunctions();
				}
				
				algorithm.learnSpecifications(); 
				
				
				//saveBestLearnedSpecifications();
				// TODO: UNCOMMENT LINE BELOW TO SAVE STATISTICS
				//this.algorithm.saveAlgorithmSatistics(linkEnvironment);
			}else{
				throw new Exception("Genertic learner has empty attributes to compare");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void initLearnerFunctions() {
		// empty
	}

	@Override
	public void initSuitableAttributes() {
		// empty
	}

	
	
}
