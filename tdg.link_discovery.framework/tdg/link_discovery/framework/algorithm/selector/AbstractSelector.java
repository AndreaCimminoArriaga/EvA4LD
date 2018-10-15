package tdg.link_discovery.framework.algorithm.selector;

public abstract class AbstractSelector implements ISelector {

	protected String name;
	protected Integer arity;

	
	public AbstractSelector(){
		this.name = "empty selector";
		this.arity = -1;		
	}
	
	public AbstractSelector(String name, Integer arity){
		this.name = name;
		this.arity = arity;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Integer getArity() {
		return arity;
	}

	@Override
	public void setArity(Integer arity) {
		this.arity = arity;
	}
	
	
	
}
