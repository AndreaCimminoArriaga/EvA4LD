package tdg.link_discovery.framework.algorithm.individual.initializer;

import tdg.link_discovery.framework.algorithm.individual.ISpecification;
import tdg.link_discovery.framework.objects.FrameworkObject;

public interface ISpecificationInitializer extends FrameworkObject{

	/* Attributes from source dataset must be enclosed between FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER
	 * Attributes from target dataset must be enclosed between FrameworkConfiguration.LINK_SPECIFICATION_TAGET_ATTR_DELIMITER 
	 * For instance, the attributes 'dblp:name' or 'http://.*#name' must be stored as: 
	 * 		FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER+'dblp:name'+FrameworkConfiguration.LINK_SPECIFICATION_TAGET_ATTR_DELIMITER
	 * 		FrameworkConfiguration.LINK_SPECIFICATION_SOURCE_ATTR_DELIMITER+'http://.*#name'+FrameworkConfiguration.LINK_SPECIFICATION_TAGET_ATTR_DELIMITER
	 */
	public  ISpecification<?> createLinkSpecification();
	
	// Implement methods below to enclose the attributes as previously explained
	public String encloseSourceAttribute(String sourceAttribute);
	public String encloseTargetAttribute(String targetAttribute);
	
}
