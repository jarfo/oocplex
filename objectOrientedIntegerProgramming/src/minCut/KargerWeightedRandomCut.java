package minCut;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.random.RandomData;
import org.jgrapht.alg.util.UnionFind;



import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;

public class KargerWeightedRandomCut<V,E> {
	
	private UndirectedGraph<V,E> graph;
	private RandomData rand;
	private UnionFind<V> unionFind;
	private Transformer<E,Number> edgeWeights;
	private Set<E> edgesOverCut;
	
	
	public KargerWeightedRandomCut(UndirectedGraph<V,E> graph, Transformer<E,Number> edgeWeights, RandomData rand){
		this.graph = graph;
		this.rand = rand;
		this.edgeWeights = edgeWeights;
		this.unionFind = new UnionFind<V>(new HashSet<V>(graph.getVertices()));
		WeightedEdgeSet<V,E> weightedEdgeSet = new WeightedEdgeSet<V,E>(graph,edgeWeights,graph.getEdges(),rand);
		List<E> randomEdgeOrder = weightedEdgeSet.getRandomPermutation();
		int numComponents = graph.getVertexCount();
		int edgeIndex = 0;
		while(edgeIndex < randomEdgeOrder.size() && numComponents > 2){
			E nextEdge = randomEdgeOrder.get(edgeIndex++);
			if(nextEdge == null){
				throw new RuntimeException();
			}
			Pair<V> endpoints = graph.getEndpoints(nextEdge);
			
			V firstRep = unionFind.find(endpoints.getFirst());
			V secondRep = unionFind.find(endpoints.getSecond());
			if(firstRep != secondRep){
				numComponents--;
				unionFind.union(endpoints.getFirst(), endpoints.getSecond());
			}
		}
		edgesOverCut = new HashSet<E>();
		if(numComponents == 2){
			
			for(E edge: graph.getEdges()){
				Pair<V> endpoints = graph.getEndpoints(edge);
				V firstRep = unionFind.find(endpoints.getFirst());
				V secondRep = unionFind.find(endpoints.getSecond());
				if(firstRep != secondRep){
					edgesOverCut.add(edge);
				}
			}
		}
		else{
			V firstComponent = unionFind.find(graph.getVertices().iterator().next());
			for(E edge: graph.getEdges()){
				Pair<V> endpoints = graph.getEndpoints(edge);
				V firstRep = unionFind.find(endpoints.getFirst());
				V secondRep = unionFind.find(endpoints.getSecond());
				if((firstRep == firstComponent && secondRep != firstComponent) ||
						(firstRep != firstComponent && secondRep == firstComponent)	){
					edgesOverCut.add(edge);
				}
			}
		}
				
		
	}
	
	public Set<E> getCut(){
		return this.edgesOverCut;
	}
	
	
	
	
	
	
	
	

}
