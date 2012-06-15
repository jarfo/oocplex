package minCut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.random.RandomData;



import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;

public class KargerRandomCut<V,E> {
	
	private UndirectedGraph<V,E> graph;
	
	private EdgeSet edgeSet;
	private Map<WeightedEdge,E> edgeMap;
	private UndirectedSparseMultigraph<ContractionNode<V>,WeightedEdge> multigraph;
	private Map<V,ContractionNode<V>> nodeMapping;
	private RandomData rand;
	
	public KargerRandomCut(UndirectedGraph<V,E> graph, RandomData rand){
		this.graph = graph;
		
		this.rand = rand;
		multigraph = new UndirectedSparseMultigraph<ContractionNode<V>,WeightedEdge>();
		nodeMapping = new HashMap<V,ContractionNode<V>>();
		for(V vertex : graph.getVertices()){
			ContractionNode<V> contraction = new ContractionNode<V>( vertex);			
			nodeMapping.put(vertex, contraction);
			multigraph.addVertex(contraction);
		}
		edgeMap = new HashMap<WeightedEdge,E>();
		for(E edge: graph.getEdges()){
			Pair<V> endpoints = graph.getEndpoints(edge);
			
			
			WeightedEdge weightedEdge = new WeightedEdge(1);
			edgeMap.put(weightedEdge, edge);
			multigraph.addEdge(weightedEdge, nodeMapping.get(endpoints.getFirst()), nodeMapping.get(endpoints.getSecond()));
			
		}
		edgeSet = new EdgeSet(multigraph.getEdges(),rand);
		
		
	}
	
	public void allContractions(){
		while(multigraph.getVertexCount() > 2){
			contract();
		}
	}
	
	public Set<E> getCut(){
		if(multigraph.getVertexCount() != 2){
			throw new RuntimeException();
		}
		Set<E> ans = new HashSet<E>();
		for(WeightedEdge edge: multigraph.getEdges()) {
			ans.add(this.edgeMap.get(edge));
		}
		return ans;
	}
	
	public void contract(){
		WeightedEdge contracted = edgeSet.getAndRemoveRandom();
		Pair<ContractionNode<V>> endPoints = multigraph.getEndpoints(contracted);
		ContractionNode<V> merged = new ContractionNode<V>(endPoints.getFirst(), endPoints.getSecond());
		Map<WeightedEdge,ContractionNode<V>> neighbors = new HashMap<WeightedEdge,ContractionNode<V>>() ;
		for(ContractionNode<V> node: endPoints){
			for(WeightedEdge e: multigraph.getIncidentEdges(node)){
				ContractionNode<V> opposite = multigraph.getOpposite(node, e);
				if(!endPoints.contains(opposite) ){
					neighbors.put(e,opposite);
				}
				else if(e != contracted){
					edgeSet.removeEdge(e);
				}
			}
			for(V underNode: node.getNodes()){
				nodeMapping.remove(underNode);
				nodeMapping.put(underNode, merged);
			}
			
		}
		this.multigraph.addVertex(merged);
		ContractionNode<V> first  = endPoints.getFirst();
		ContractionNode<V> second = endPoints.getSecond();
		this.multigraph.removeVertex(first);
		this.multigraph.removeVertex(second);
		for (WeightedEdge toAdd : neighbors.keySet()) {
			multigraph.addEdge(toAdd, merged,neighbors.get(toAdd));
			
		}
		
		
	}
	
	private static class EdgeSet{
		
		private Map<WeightedEdge,Integer> edgeIndex;
		private WeightedEdge[] edges;
		private boolean[] present;
		private RandomData rand;
		private int emptyCount;
		
		public EdgeSet(Collection<WeightedEdge> edges, RandomData rand){
			this.edges = new WeightedEdge[edges.size()];
			this.present = new boolean[edges.size()];
			Arrays.fill(present,true);
			this.edgeIndex = new HashMap<WeightedEdge,Integer>();
			this.rand = rand;
			int i = 0;
			for(WeightedEdge edge: edges){
				int index = i++;
				this.edges[index] = edge;
				this.edgeIndex.put(edge, index);
			}
			emptyCount = 0;
		}
		
		public WeightedEdge getAndRemoveRandom(){			
			WeightedEdge ans = null;
			while(ans == null){
				int randIndex = this.rand.nextInt(0,this.edges.length-1);
				ans = getAndRemove(randIndex);
			}
			return ans;
		}
		
		public void removeEdge(WeightedEdge edge){
			WeightedEdge removed = getAndRemove(this.edgeIndex.get(edge));
			if(removed == null || removed != edge){
				throw new RuntimeException();
			}
			
		}
		
		public WeightedEdge getAndRemove(int index){
			if(!this.present[index]){
				return null;
			}
			WeightedEdge ans = this.edges[index];
			this.present[index] = false;
			this.edgeIndex.remove(ans);
			emptyCount++;
			if(emptyCount >= (this.edges.length+1)/2){
				resize();
			}
			return ans;
			
		}
		
		private void resize(){
			int numLeft = edges.length - emptyCount;
			WeightedEdge[] newEdge = new WeightedEdge[numLeft];
			boolean[] newPresent = new boolean[numLeft];
			Arrays.fill(newPresent, true);
			Map<WeightedEdge,Integer> newEdgeMap = new HashMap<WeightedEdge,Integer>();
			int j = 0;
			for(int i = 0; i < edges.length; i++){
				if(this.present[i]){
					newEdge[j] = edges[i];
					newEdgeMap.put(newEdge[j], Integer.valueOf(j));
					j++;
				}
			}
			if(j != numLeft){
				throw new RuntimeException();
			}
			this.edges = newEdge;
			this.present = newPresent;
			this.edgeIndex = newEdgeMap;
			 
			emptyCount = 0;
		}
		
		
		
		
	}
	
	
	

}
