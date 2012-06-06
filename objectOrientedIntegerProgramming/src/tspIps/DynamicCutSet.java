package tspIps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import minCut.WeightedEdge.WeightedEdgeFactory;
import minCut.WeightedEdge.WeightedEdgeTransformer;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;


import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class DynamicCutSet<V,E> {
	
	private UndirectedGraph<V,E> graph;
	private IloCplex cplex;
	private EdgeVariables<V,E> edgeVariables;
	
	
	
	
	public DynamicCutSet(UndirectedGraph<V, E> graph, IloCplex cplex, EdgeVariables<V,E> edgeVariables) throws IloException {
		super();
		this.graph = graph;
		this.cplex = cplex;
		this.edgeVariables = edgeVariables;		
	}
	
	
	
	
	
	
	
	/**
	 * 
	 * @return true if found a violated cut
	 */
	public boolean addViolatedCut() throws IloException {
		Map<E,Double> edgeWeights = new HashMap<E,Double>();
		for(E edge: edgeVariables.getEdgeVars().keySet()){
			edgeWeights.put(edge, cplex.getValue(edgeVariables.getEdgeVars().get(edge)));
		}
		List<Set<E>> cuts = null;
		for(Set<E> graphCutEdges: cuts){
		IloLinearIntExpr cut = cplex.linearIntExpr();
		for(E edge: graphCutEdges){
			cut.addTerm(edgeVariables.getEdgeVars().get(edge), 1);
		}
		cplex.addGe(cut, 2);
		}
		if(cuts.size() > 0){
			return true;
		}
		else{
			return false;
		}
			
	}
	
	

}
