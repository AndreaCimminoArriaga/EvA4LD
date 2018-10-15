package tdg.link_discovery.framework.algorithm.variations;

public abstract class AbstractVariation<T> implements IVariation<T> {

	protected Integer inputArity, ouputArity;
	protected Double probability;
	protected String name;
	
	public AbstractVariation(Integer inputArity, Integer ouputArity, Double probability, String name) {
		super();
		this.inputArity = inputArity;
		this.ouputArity = ouputArity;
		this.probability = probability;
		this.name = name;
	}

	@Override
	public Integer getInputArity() {
		return inputArity;
	}

	@Override
	public void setInputArity(Integer inputArity) {
		this.inputArity = inputArity;
	}

	@Override
	public Integer getOuputArity() {
		return ouputArity;
	}

	@Override
	public void setOuputArity(Integer ouputArity) {
		this.ouputArity = ouputArity;
	}

	@Override
	public Double getProbability() {
		return probability;
	}

	@Override
	public void setProbability(Double probability) {
		this.probability = probability;
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
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		AbstractVariation other = (AbstractVariation) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}



	
	
	

}
