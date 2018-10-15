package tdg.link_discovery.connector.sparql.engine.evaluator.deprecated;

import java.util.LinkedHashMap;
import java.util.Map;

public class SparqlCacheInstance {

	
	protected Map<String,String> attributes;
	protected String id;
	
	public SparqlCacheInstance() {
		attributes = new LinkedHashMap<String,String>();
		id=null;
	}
	
	public boolean containsAttribute(String attributeLabel) {
		return attributes.containsKey(attributeLabel);
	}
	
	public void addAttribute(String attributeLabel, String value) {
		attributes.put(attributeLabel, value);
	}
	
	public  String getAttribute(String attributeLabel) {
		return attributes.get(attributeLabel);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "SparqlCacheInstance [attributes=" + attributes + ", id=" + id + "]";
	}
	
	
	
}
