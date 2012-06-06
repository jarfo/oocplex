package minCut;

import java.util.HashSet;
import java.util.Set;



class ContractionNode<V>{
	private Set<V> nodes;
	
	public ContractionNode(V baseNode){
		this.nodes = new HashSet<V>();
		this.nodes.add(baseNode);
	}
	
	public ContractionNode(ContractionNode<V> first, ContractionNode<V> second){
		this.nodes = new HashSet<V>();
		nodes.addAll(first.nodes);
		nodes.addAll(second.nodes);
	}
	
	public Set<V> getNodes(){
		return this.nodes;
	}
}