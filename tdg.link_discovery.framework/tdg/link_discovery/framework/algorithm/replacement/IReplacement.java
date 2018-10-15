package tdg.link_discovery.framework.algorithm.replacement;

import org.moeaframework.core.Population;

import tdg.link_discovery.framework.objects.FrameworkObject;

public interface IReplacement extends FrameworkObject{

	public Population combine(Population population, Population offspring);

	
}
