package minCut;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;



import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class ContractionGraph<V, E> {
	
	private UndirectedGraph<V,E> inputGraph;
	private UndirectedGraph<V,E> contractedGraph;
	private UnionFindNoCompression<V> nodeUnionFind;
	private UnionFindNoCompression<E> edgeUnionFind;
	private Deque<Undo> stack;
	
	public ContractionGraph(UndirectedGraph<V,E> inputGraph){
		this.inputGraph = inputGraph;
		contractedGraph = new UndirectedSparseGraph<V,E>();
		for(V node: inputGraph.getVertices()){
			contractedGraph.addVertex(node);
		}
		for(E edge: inputGraph.getEdges()){
			Pair<V> endpoints = inputGraph.getEndpoints(edge);
			contractedGraph.addEdge(edge, endpoints.getFirst(), endpoints.getSecond());
		}
		nodeUnionFind = new UnionFindNoCompression<V>(new HashSet<V>(inputGraph.getVertices()));
		edgeUnionFind = new UnionFindNoCompression<E>(new HashSet<E>(inputGraph.getEdges()));
		stack = new ArrayDeque<Undo>();
	}
	
	public int getNumComponents(){
		return this.contractedGraph.getVertexCount();
	}
	
	
	
	public Cut<E> getCut(Transformer<E,Number> edgeWeight, double maxWeight){
		if(this.contractedGraph.getVertexCount() != 2){
			throw new RuntimeException();
		}
		Set<E> ans = new HashSet<E>();
		E lastEdge = this.contractedGraph.getEdges().iterator().next();
		double cutWeight = 0;
		for(E edge: this.edgeUnionFind.allChildren(lastEdge)){
			cutWeight+= edgeWeight.transform(edge).doubleValue();
			if(cutWeight >= maxWeight){
				return null;
			}
			ans.add(edge);
		}
		return new Cut<E>(ans,cutWeight);		
	}
	
	public List<Cut<E>> testAllCuts(Transformer<E,Number> edgeWeight, double maxWeight){
		List<Cut<E>> ans = new ArrayList<Cut<E>>();
		/*for(V vertex: this.contractedGraph.getVertices()){
			double cutWeight = 0;
			Set<E> edgesLeavingVertex = new HashSet<E>();
			for(V neighbor: this.contractedGraph.getNeighbors(vertex)){
				E edgeRep = this.contractedGraph.findEdge(vertex, neighbor);
				for(E edge: this.edgeUnionFind.allChildren(edgeRep)){
					cutWeight+= edgeWeight.transform(edge).doubleValue();
					if(cutWeight >= maxWeight){
						break;
					}
					edgesLeavingVertex.add(edge);
				}
				if(cutWeight >= maxWeight){
					break;
				}
			}
			if(cutWeight < maxWeight){
				ans.add(edgesLeavingVertex);
			}
			
			
			
		}
		*/
		
		return ans;		
	}
	
	int getUndoSize(){
		return this.stack.size();
	}
	
	int getNodeUndoSize(){
		return this.nodeUnionFind.getUndoSize();
	}
	
	int getEdgeUndoSize(){
		return this.edgeUnionFind.getUndoSize();
	}
	
	public boolean contractGraph(E edge){
		E repEdge = edgeUnionFind.find(edge);
		if(!contractedGraph.containsEdge(repEdge)){
			return false;
		}
		Pair<V> repEndpoints = contractedGraph.getEndpoints(repEdge);
		V firstEnd = repEndpoints.getFirst();
		V secondEnd = repEndpoints.getSecond();
		V winner = nodeUnionFind.union(firstEnd, secondEnd);
		V loser = firstEnd == winner? secondEnd: firstEnd;
		
		Undo undo = new Undo(loser,repEdge,winner);
		contractedGraph.removeEdge(repEdge);
		for(V neighbor: new ArrayList<V>(contractedGraph.getNeighbors(loser))){
			E neighborEdge = contractedGraph.findEdge(loser, neighbor);
			E nodeWinnerEdge = contractedGraph.findEdge(winner, neighbor);
			if(nodeWinnerEdge != null){				
				E winningEdge = edgeUnionFind.union(neighborEdge, nodeWinnerEdge);
				contractedGraph.removeEdge(neighborEdge);
				contractedGraph.removeEdge(nodeWinnerEdge);
				contractedGraph.addEdge(winningEdge, winner, neighbor);
				undo.addEdgeUnion(nodeWinnerEdge,neighborEdge,winningEdge);
			}
			else{
				contractedGraph.removeEdge(neighborEdge);
				contractedGraph.addEdge(neighborEdge, winner, neighbor);
				undo.addEdgeMovedToWinnerNoUnion(neighborEdge);
			}			
		}
		contractedGraph.removeVertex(loser);
		stack.addLast(undo);
		return true;
	}
	
	public void undo(){
		Undo undoInfo = this.stack.removeLast();
		nodeUnionFind.undo();
		for(int i = 0; i < undoInfo.getEdgesUnions(); i++){
			edgeUnionFind.undo();
		}
		contractedGraph.addVertex(undoInfo.getNodeDeleted());
		contractedGraph.addEdge(undoInfo.getEdgeDeleted(), undoInfo.getNodeDeleted(), undoInfo.getNodeWinner());
		for(E edge: undoInfo.getEdgesMovedToWinnerNoUnion()){
			V neighbor = contractedGraph.getOpposite(undoInfo.getNodeWinner(), edge);
			contractedGraph.removeEdge(edge);
			contractedGraph.addEdge(edge, neighbor, undoInfo.getNodeDeleted());
		}
		for(Undo.EdgeMergeUndoInfo edgeMergeInfo: undoInfo.getEdgeMergeUndoInfo()){
			V neighbor = contractedGraph.getOpposite(undoInfo.getNodeWinner(), edgeMergeInfo.getIsAttachedToWinner());
			contractedGraph.removeEdge(edgeMergeInfo.getIsAttachedToWinner());
			contractedGraph.addEdge(edgeMergeInfo.getWasAttachedToWinner(), undoInfo.getNodeWinner(), neighbor);
			contractedGraph.addEdge(edgeMergeInfo.getWasAttachedToLoser(), undoInfo.getNodeDeleted(), neighbor);
		}
	}
	
	
	
	private class Undo{
		private int edgesUnions;
		private V nodeDeleted;
		private E edgeDeleted;
		private V nodeWinner;
		private List<E> edgesMovedToWinnerNoUnion;
		private List<EdgeMergeUndoInfo> edgeMergeUndoInfo;

		public Undo(V nodeDeleted, E edgeDeleted,
				V nodeWinner) {
			super();
			this.edgesUnions = 0;
			this.nodeDeleted = nodeDeleted;
			this.edgeDeleted = edgeDeleted;
			this.nodeWinner = nodeWinner;
			this.edgesMovedToWinnerNoUnion = new ArrayList<E>();
			this.edgeMergeUndoInfo = new ArrayList<EdgeMergeUndoInfo>();
		}
		
		public void addEdgeUnion(E wasAttachedToWinner,E wasAttachedToLoser,E isAttachedToWinner ){
			this.edgesUnions++;
			this.edgeMergeUndoInfo.add(new EdgeMergeUndoInfo(wasAttachedToWinner,wasAttachedToLoser,isAttachedToWinner));
		}
		
		public void addEdgeMovedToWinnerNoUnion(E edge){
			this.edgesMovedToWinnerNoUnion.add(edge);
		}
		
		
		public int getEdgesUnions() {
			return edgesUnions;
		}

		public V getNodeDeleted() {
			return nodeDeleted;
		}

		public E getEdgeDeleted() {
			return edgeDeleted;
		}

		public V getNodeWinner() {
			return nodeWinner;
		}

		public List<E> getEdgesMovedToWinnerNoUnion() {
			return edgesMovedToWinnerNoUnion;
		}

		public List<EdgeMergeUndoInfo> getEdgeMergeUndoInfo() {
			return edgeMergeUndoInfo;
		}


		private class EdgeMergeUndoInfo{
			private E wasAttachedToWinner;
			private E wasAttachedToLoser;
			private E isAttachedToWinner;
			public EdgeMergeUndoInfo(E wasAttachedToWinner,
					E wasAttachedToLoser, E isAttachedToWinner) {
				super();
				this.wasAttachedToWinner = wasAttachedToWinner;
				this.wasAttachedToLoser = wasAttachedToLoser;
				this.isAttachedToWinner = isAttachedToWinner;
			}
			public E getWasAttachedToWinner() {
				return wasAttachedToWinner;
			}
			public E getWasAttachedToLoser() {
				return wasAttachedToLoser;
			}
			public E getIsAttachedToWinner() {
				return isAttachedToWinner;
			}
			
		}		
		
	}

}
