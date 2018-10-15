package tdg.link_discovery.framework.algorithm.sample;

public class Sample<T> {

	protected T element1, element2;
	protected Boolean isPositive;
	
	public Sample() {
		// Empty
	}
	
	public Sample(T element1, T element2, Boolean isPositive) {
		this.element1 = element1;
		this.element2 = element2;
		this.isPositive = isPositive;
	}
	
	/*
	 * Getters and Setters
	 */
	
	public T getElement1() {
		return element1;
	}
	public void setElement1(T element1) {
		this.element1 = element1;
	}
	public T getElement2() {
		return element2;
	}
	public void setElement2(T element2) {
		this.element2 = element2;
	}
	public Boolean getIsPositive() {
		return isPositive;
	}
	public void setIsPositive(Boolean isPositive) {
		this.isPositive = isPositive;
	}
	
	/*
	 * toString
	 */
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("(").append(element1).append(", ").append(element2).append(", ");
		if(isPositive){
			str.append("positive)");
		}else{
			str.append("negative)");
		}
		return str.toString();
	}

	/*
	 * HashC & Equals
	 */
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((element1 == null) ? 0 : element1.hashCode());
		result = prime * result
				+ ((element2 == null) ? 0 : element2.hashCode());
		result = prime * result
				+ ((isPositive == null) ? 0 : isPositive.hashCode());
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sample<T> other = (Sample<T>) obj;
		if (element1 == null) {
			if (other.element1 != null)
				return false;
		} else if (!element1.equals(other.element1))
			return false;
		if (element2 == null) {
			if (other.element2 != null)
				return false;
		} else if (!element2.equals(other.element2))
			return false;
		if (isPositive == null) {
			if (other.isPositive != null)
				return false;
		} else if (!isPositive.equals(other.isPositive))
			return false;
		return true;
	}

	
	
	
	
}
