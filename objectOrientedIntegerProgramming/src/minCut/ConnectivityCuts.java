package minCut;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class ConnectivityCuts {
	
	public static <V,E> Set<Cut<E>> makeCuts(UndirectedGraph<V,E> graph, Transformer<E,Number> edgeWeights){
		Set<Cut<E>> ans = new HashSet<Cut<E>>();
		UndirectedGraph<V,E> subgraph = new UndirectedSparseGraph<V,E>();
		for(V vertex: graph.getVertices()){
			subgraph.addVertex(vertex);
		}
		for(E edge: graph.getEdges()){
			if(edgeWeights.transform(edge).doubleValue()>0){
				Pair<V> endpoints = graph.getEndpoints(edge);
				subgraph.addEdge(edge, endpoints.getFirst(), endpoints.getSecond());
			}
		}
		WeakComponentClusterer<V,E> clusterer = new WeakComponentClusterer<V,E>();
		//System.err.println("start transform");
		Set<Set<V>> clusters = clusterer.transform(subgraph);
		//System.err.println("done transform");
		if(clusters.size()>1){
		for(Set<V> cluster: clusters){
			Set<E> outEdges = new HashSet<E>();
			for(V node: cluster){
				for(V neighbor: graph.getNeighbors(node)){
					if(!cluster.contains(neighbor)){
						outEdges.add(graph.findEdge(node, neighbor));
					}
				}
			}
			ans.add(new Cut<E>(outEdges,0));
		}
		}
		return ans;
	}

}
