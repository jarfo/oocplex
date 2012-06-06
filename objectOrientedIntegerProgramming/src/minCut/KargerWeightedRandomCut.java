package minCut;

import java.util.Map;

import org.apache.commons.math3.random.RandomData;


import tspIps.RandomInterface;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;

public class KargerWeightedRandomCut<V,E> {
	
private UndirectedGraph<V,E> graph;
	
	//private EdgeSet edgeSet;
	private Map<WeightedEdge,E> edgeMap;
	//private UndirectedSparseMultigraph<ContractionNode<V>,WeightedEdge> multigraph;
	//private Map<V,ContractionNode<V>> nodeMapping;
	private RandomData rand;

}
