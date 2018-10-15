package tdg.link_discovery.framework.algorithm.fitness;

public abstract class AbstractFitness implements IFitness{

	protected String name;
	
	public AbstractFitness(){
		this.name = "unnamed fitness";
	}
	
	public AbstractFitness(String name){
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
