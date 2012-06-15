package minCut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.jgrapht.alg.StoerWagnerMinimumCut;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.WeightedMultigraph;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class StoerWagnerMinimumCutWrapper<V, E> implements MinCutSolver<V, E> {
	
	private boolean printProgress;
	
	public StoerWagnerMinimumCutWrapper(boolean printProgress){
		this.printProgress = printProgress;
	} 
	
	private void print(String s){
		if(printProgress){
			System.out.println(s);
		}
	}

	@Override
	public List<Cut<E>> findCutsLessThan(UndirectedGraph<V, E> graph,
			Transformer<E, Number> edgeWeights, double value) {
		JungJGraphTAdapator<V,E> adaptor = new JungJGraphTAdapator<V,E>(graph,edgeWeights);
		print("beginning min cut ");
		StoerWagnerMinimumCut<V,DefaultWeightedEdge> minCut = new StoerWagnerMinimumCut<V,DefaultWeightedEdge>(adaptor.getJgraph());
		List<Cut<E>> ans = new ArrayList<Cut<E>>();
		if(minCut.minCutWeight()< value){
			ans.add(getEdgesExiting(minCut.minCut(),graph,edgeWeights));
		}
		print("ending min cut");
		return ans;
	}
	
	public static <U,F> Cut<F> getEdgesExiting(Set<U> nodes, UndirectedGraph<U,F> graph, Transformer<F,Number> edgeWeights){
		Set<F> ans = new HashSet<F>();
		double weight = 0;
		for(U node: nodes){
			for(U neighbor: graph.getNeighbors(node)){
				if(!nodes.contains(neighbor)){
					F edge = graph.findEdge(node, neighbor);
					ans.add(edge);
					weight+=edgeWeights.transform(edge).doubleValue();
				}
			}
		}
		return new Cut<F>(ans,weight);
		
	}
	
	private static class JungJGraphTAdapator<U,F>{
		
		private UndirectedGraph<U,F> graph;
		private Transformer<F,Number> edgeWeights;
		
		private WeightedMultigraph<U,DefaultWeightedEdge> jgraph;
		private ImmutableBiMap<DefaultWeightedEdge,F> edgeMap;
		
		public JungJGraphTAdapator(UndirectedGraph<U,F> graph, Transformer<F,Number> edgeWeights){
			this.graph = graph;
			this.edgeWeights = edgeWeights;
			this.jgraph = new WeightedMultigraph<U,DefaultWeightedEdge>(DefaultWeightedEdge.class);
			Builder<DefaultWeightedEdge,F> edgeMapBuilder = ImmutableBiMap.builder();
			for(U node: graph.getVertices()){
				jgraph.addVertex(node);
			}
			for(F edge: graph.getEdges()){
				Pair<U> endpoints = graph.getEndpoints(edge);
				U source = endpoints.getFirst();
				U dest = endpoints.getSecond();
				DefaultWeightedEdge jedge = jgraph.addEdge(source, dest);
				jgraph.setEdgeWeight(jedge, edgeWeights.transform(edge).doubleValue());
				edgeMapBuilder.put(jedge, edge);
			}
			edgeMap = edgeMapBuilder.build();
		}

		public UndirectedGraph<U, F> getGraph() {
			return graph;
		}

		public Transformer<F, Number> getEdgeWeights() {
			return edgeWeights;
		}

		public WeightedMultigraph<U, DefaultWeightedEdge> getJgraph() {
			return jgraph;
		}

		public ImmutableBiMap<DefaultWeightedEdge, F> getEdgeMap() {
			return edgeMap;
		}
		
		
		
	}
	
	
	

}
