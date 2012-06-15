package minCut;

import java.util.ArrayList;
import java.util.Collection;
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

public class KargerSteinWeightedRandomCut<V,E> {

	private UndirectedGraph<V,E> graph;
	private ContractionGraph<V,E> contractionGraph;
	private RandomData rand;
	private Transformer<E,Number> edgeWeights;
	private PriorityQueueBounded<Cut<E>> randomCuts;
	private double maxWeightRetainCut;
	private boolean halt;
	
	public KargerSteinWeightedRandomCut(UndirectedGraph<V,E> graph, 
			Transformer<E,Number> edgeWeights, RandomData rand, double maxWeightRetainCut, PriorityQueueBounded<Cut<E>> cuts){
		this.graph = graph;
		this.rand = rand;
		this.edgeWeights = edgeWeights;
		this.randomCuts = cuts;
		this.maxWeightRetainCut = maxWeightRetainCut;
		this.contractionGraph = new ContractionGraph<V,E>(graph);
		this.halt = false;
		addCuts(new ArrayList<E>(graph.getEdges()),true,0);
		
	}
	

	//initalMerges will be mutated.  orderOfRemainingEdges will not be mutated.
	private void addCuts(List<E> orderOfRemainingEdges, boolean shuffleRequired, int depth){
		
		//System.err.println("nodes: " + this.contractionGraph.getNumComponents() + " depth: " + depth);
		//if(depth>100){
		//	throw new RuntimeException();
		//}
		int graphUndoSize = this.contractionGraph.getUndoSize();
		int nodeUndoSize = this.contractionGraph.getNodeUndoSize();
		int edgeUndoSize = this.contractionGraph.getEdgeUndoSize();
		
		List<E> orderEdgesUsed = orderOfRemainingEdges;
		if(shuffleRequired){
			WeightedEdgeSet<V,E> weightedEdgeSet = new WeightedEdgeSet<V,E>(graph,edgeWeights,orderOfRemainingEdges,rand);
			orderEdgesUsed = weightedEdgeSet.getRandomPermutation();
		}
		int n = this.contractionGraph.getNumComponents();
		int stop = n > 15? (int)Math.ceil(n/Math.sqrt(2)) + 1 : 2;
		int edgeIndex = 0;
		int numMerges = 0;
		while(edgeIndex < orderEdgesUsed.size() && this.contractionGraph.getNumComponents() > stop){
			E nextEdge = orderEdgesUsed.get(edgeIndex++);
			if(nextEdge == null){
				throw new RuntimeException();
			}
			if(contractionGraph.contractGraph(nextEdge)){
				numMerges++;
			}			
		}
		if(edgeIndex == orderEdgesUsed.size() && contractionGraph.getNumComponents() > 2){
			throw new RuntimeException();
			/*List<Cut<E>> allCuts = contractionGraph.testAllCuts(edgeWeights, maxWeightRetainCut);
			for(Cut<E> cut: allCuts){
				this.randomCuts.add(cut);
			}*/
		}
		else if(contractionGraph.getNumComponents() <= 2){			
			Cut<E> edgesOverCut = contractionGraph.getCut(this.edgeWeights,this.maxWeightRetainCut);
			if(edgesOverCut != null){
				this.randomCuts.add(edgesOverCut);
				if(edgesOverCut.getWeight() < 1.5){
					halt = true;
					return;
				}
			}
		}		
		else{
			List<E> edgeOrderRec = orderEdgesUsed.subList(edgeIndex, orderEdgesUsed.size());
			addCuts( edgeOrderRec,false,depth+1);
			if(halt){
				return;
			}
			addCuts(edgeOrderRec,true,depth+1);
			if(halt){
				return;
			}
			
		}
		for(int i =0; i < numMerges; i++){
			contractionGraph.undo();
		}
		if(this.contractionGraph.getUndoSize() != graphUndoSize){
			throw new RuntimeException("Graph Undo size was " + this.contractionGraph.getUndoSize() + " but should have been " + graphUndoSize);
		}
		if(this.contractionGraph.getNodeUndoSize() != nodeUndoSize){
			throw new RuntimeException("Node Undo size was " + this.contractionGraph.getNodeUndoSize() + " but should have been " + nodeUndoSize);
		}
		if(this.contractionGraph.getEdgeUndoSize() != edgeUndoSize){
			throw new RuntimeException("Edge Undo size was " + this.contractionGraph.getEdgeUndoSize() + " but should have been " + edgeUndoSize);
		}
		
		
	}

}
