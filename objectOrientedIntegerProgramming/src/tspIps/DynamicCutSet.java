package tspIps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import minCut.Cut;
import minCut.MinCutSolver;
import minCut.WeightedEdge.WeightedEdgeFactory;
import minCut.WeightedEdge.WeightedEdgeTransformer;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.TransformerUtils;
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
	private MinCutSolver<V,E> minCutSolver;
	
	
	
	public DynamicCutSet(UndirectedGraph<V, E> graph, IloCplex cplex, EdgeVariables<V,E> edgeVariables,MinCutSolver<V,E> minCutSolver) throws IloException {
		super();
		this.graph = graph;
		this.cplex = cplex;
		this.edgeVariables = edgeVariables;
		this.minCutSolver = minCutSolver;
	}
	
	
	
	
	
	
	
	/**
	 * 
	 * @return the lowest weight of any cut found
	 */
	public double addViolatedCut() throws IloException {
		final Map<E,Double> edgeWeights = new HashMap<E,Double>();
		for(E edge: edgeVariables.getEdgeVars().keySet()){
			edgeWeights.put(edge, cplex.getValue(edgeVariables.getEdgeVars().get(edge)));
		}
		Iterable<Cut<E>> cuts = this.minCutSolver.findCutsLessThan(graph, new Transformer<E,Number>(){
			@Override
			public Number transform(E arg0) {
				return edgeWeights.get(arg0);
			}}, 2);
		boolean foundCut = false;
		double minWeight = Double.MAX_VALUE;
		for(Cut<E> graphCutEdges: cuts){
			foundCut = true;
			IloLinearIntExpr cut = cplex.linearIntExpr();
			for(E edge: graphCutEdges.getEdges()){
				cut.addTerm(edgeVariables.getEdgeVars().get(edge), 1);
			}
			cplex.addGe(cut, 2);
			minWeight = Math.min(minWeight, graphCutEdges.getWeight());
		}
		return minWeight;			
	}
	
	

}
