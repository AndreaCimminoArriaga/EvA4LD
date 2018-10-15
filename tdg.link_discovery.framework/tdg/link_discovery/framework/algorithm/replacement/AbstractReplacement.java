package tdg.link_discovery.framework.algorithm.replacement;

public abstract class AbstractReplacement implements IReplacement {

	protected String name;

	
	public AbstractReplacement(){
		this.name = "unnamed replacement";
	}
	
	public AbstractReplacement(String name){
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
