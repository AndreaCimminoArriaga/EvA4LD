package tdg.link_discovery.framework.learner.functions;

public interface StringMetricFunction extends Function{
	// Specialization interface, implement with string metrics
	public Double compareStrings(String element1, String element2);
}
