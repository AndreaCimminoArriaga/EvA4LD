package tdg.link_discovery.middleware.objects;

public class Tuple<T,O> {
	
	protected T firstElement;
	protected O secondElement;
	
	public Tuple(T t, O o){
		this.firstElement = t;
		this.secondElement = o;
	}
	
	public Tuple(){
		this.firstElement = null;
		this.secondElement = null;
	}

	public T getFirstElement() {
		return firstElement;
	}

	public void setFirstElement(T firstElement) {
		this.firstElement = firstElement;
	}

	public O getSecondElement() {
		return secondElement;
	}

	public void setSecondElement(O secondElement) {
		this.secondElement = secondElement;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((firstElement == null) ? 0 : firstElement.hashCode());
		result = prime * result
				+ ((secondElement == null) ? 0 : secondElement.hashCode());
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
		Tuple<T,O> other = (Tuple<T,O>) obj;
		if (firstElement == null) {
			if (other.firstElement != null)
				return false;
		} else if (!firstElement.equals(other.firstElement))
			return false;
		if (secondElement == null) {
			if (other.secondElement != null)
				return false;
		} else if (!secondElement.equals(other.secondElement))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("('").append(firstElement).append("', '").append(secondElement).append("')");
		return str.toString();
	}
	
	
}
