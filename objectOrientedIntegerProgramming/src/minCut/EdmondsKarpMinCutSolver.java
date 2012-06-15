package minCut;

import ilog.concert.IloLinearIntExpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import minCut.WeightedEdge.WeightedEdgeFactory;
import minCut.WeightedEdge.WeightedEdgeTransformer;

import org.apache.commons.collections15.Transformer;


import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class EdmondsKarpMinCutSolver<V, E> implements MinCutSolver<V, E> {
	
	private boolean printOutput;
	private static int rescaling = 10000;
	private boolean minOnly;
	
	public EdmondsKarpMinCutSolver(boolean minOnly,boolean printOutput){
		this.printOutput = printOutput;
		this.minOnly = minOnly;
	}
	
	private void print(String s){
		if(printOutput){
			System.out.println(s);
		}
	}

	@Override
	public List<Cut<E>> findCutsLessThan(UndirectedGraph<V, E> graph,
			Transformer<E, Number> edgeWeights, double value) {
		List<Cut<E>> ans = new ArrayList<Cut<E>>();
		
		print("Forming min cut");
		
		DirectedGraph<V,WeightedEdge> diGraph = new DirectedSparseGraph<V,WeightedEdge>();
		for(V vertex: graph.getVertices()){
			diGraph.addVertex(vertex);
		}
		
		for(E oldEdge: graph.getEdges()){
			Pair<V> nodes = graph.getEndpoints(oldEdge);
			double weight = edgeWeights.transform(oldEdge).doubleValue();
			diGraph.addEdge(new WeightedEdge(weight), nodes.getFirst(), nodes.getSecond());
			diGraph.addEdge(new WeightedEdge(weight), nodes.getSecond(), nodes.getFirst());
		}
		double bestCut = value*rescaling;
		if(graph.getVertexCount() > 0){
		V source = graph.getVertices().iterator().next();
		int subProblems = 0;
			for(V sink: graph.getVertices()){
				if(source != sink){
					print("next cut sub problem: " + subProblems++);
					HashMap<WeightedEdge,Number> flowValues = new HashMap<WeightedEdge,Number>();
					EdmondsKarpMaxFlow<V,WeightedEdge> maxFlow = 
							new EdmondsKarpMaxFlow<V,WeightedEdge>(diGraph, source, sink, 
							new WeightedEdgeTransformer(rescaling), flowValues,	WeightedEdgeFactory.instance);
					maxFlow.evaluate();
					boolean addCut = maxFlow.getMaxFlow() < (this.minOnly ?  bestCut : value*rescaling); 
					if(addCut){						
						Set<WeightedEdge> diGraphCutEdges = maxFlow.getMinCutEdges();
						Set<E> graphCutEdges = new HashSet<E>();
						double weight = 0;
						for(WeightedEdge diGraphEdge: diGraphCutEdges){
							V home = diGraph.getSource(diGraphEdge);
							V target = diGraph.getDest(diGraphEdge);
							E edge = graph.findEdge(home, target);
							graphCutEdges.add(edge);
							weight+= edgeWeights.transform(edge).doubleValue();
						}
						ans.add(new Cut<E>(graphCutEdges,weight));						
					}
				}
			}
		}
		print("Done with min cut");
		return ans;
	}

}
