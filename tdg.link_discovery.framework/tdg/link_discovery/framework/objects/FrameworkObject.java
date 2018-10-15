package tdg.link_discovery.framework.objects;

import java.util.Map;

public interface FrameworkObject {

	public String getName();
	public void setName(String name); // this method makes no sense
	
	public Map<String,Double> getInputDefaultParameters(); // todo this may be something rather than Double, change it to ?
	public Boolean hasInputParameters();
	public void setInputParameters(String parameterName, Object value);
	
	// TODO: get parameters names, i.e., set with the paramers name
}
