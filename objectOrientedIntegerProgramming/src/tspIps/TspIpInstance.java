package tspIps;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.UndirectedGraph;

public class TspIpInstance<V,E> {
	
	private UndirectedGraph<V,E> graph;
	private Transformer<E,Double> edgeWeights;
	public TspIpInstance(UndirectedGraph<V, E> graph,
			Transformer<E, Double> edgeWeights) {
		super();
		this.graph = graph;
		this.edgeWeights = edgeWeights;
	}
	public UndirectedGraph<V, E> getGraph() {
		return graph;
	}
	public Transformer<E, Double> getEdgeWeights() {
		return edgeWeights;
	}
	
	

}
