package tspLib;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.UndirectedGraph;
import tspIps.TspIpInstance;

public class TspLibInstance<V extends IntNode> extends
		TspIpInstance<V, WeightedEdge> {
	
	private String name;

	public TspLibInstance(UndirectedGraph<V, WeightedEdge> graph,
			Transformer<WeightedEdge, Double> edgeWeights, String name) {
		super(graph, edgeWeights);
		this.name = name;
	}

}
