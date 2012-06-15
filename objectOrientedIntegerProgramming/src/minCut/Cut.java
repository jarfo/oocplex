package minCut;

import java.util.Comparator;
import java.util.Set;

public class Cut<E> {
	
	private Set<E> edges;
	
	private double weight;
	
	public Cut(Set<E> edges, double weight){
		this.edges = edges;
		this.weight = weight;
	}

	public Set<E> getEdges() {
		return edges;
	}

	public double getWeight() {
		return weight;
	}

	@Override
	public int hashCode() {
		return edges.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cut other = (Cut) obj;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		return true;
	}
	
	
	public static <F> Comparator<Cut<F>> makeCutComparator(){
		return new Comparator<Cut<F>>(){

			@Override
			public int compare(Cut<F> arg0, Cut<F> arg1) {
				return Double.compare(arg0.getWeight(), arg1.getWeight());
			}
			
		};
	}

}
