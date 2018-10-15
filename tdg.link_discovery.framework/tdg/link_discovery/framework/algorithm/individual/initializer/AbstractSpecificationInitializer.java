package tdg.link_discovery.framework.algorithm.individual.initializer;

public abstract class AbstractSpecificationInitializer implements ISpecificationInitializer{

	protected String name;

	public AbstractSpecificationInitializer(){
		this.name = "unnamed specification initializer";
	}
	
	public AbstractSpecificationInitializer(String name){
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	
	
	
}
