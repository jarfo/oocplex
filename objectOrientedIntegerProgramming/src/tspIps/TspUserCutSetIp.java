package tspIps;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.UndirectedGraph;
import minCut.MinCutSolver;

public class TspUserCutSetIp<V, E> extends AbstractTspFormulationEdgeBased<V, E> {
	
	
	private MinCutSolver<V,E> minCutSolver;
	private LazySubtourElimination<V,E> lazySubtourElimination;
	private UserCallbackCutSet<V,E> userCallbackCutSet;
	
	
	public TspUserCutSetIp(UndirectedGraph<V, E> graph,
			Transformer<E, Double> edgeWeights,MinCutSolver<V,E> minCutSolver, double cutVal, boolean printOutput)
			throws IloException {
		this(graph,edgeWeights,minCutSolver, cutVal,printOutput,new IloCplex());
	}
	
	
	public TspUserCutSetIp(UndirectedGraph<V, E> graph,
			Transformer<E, Double> edgeWeights,MinCutSolver<V,E> minCutSolver, double cutVal, boolean printOutput,IloCplex cplex)
			throws IloException {
		super(graph, edgeWeights, cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		this.minCutSolver = minCutSolver;
		this.lazySubtourElimination = new LazySubtourElimination<V,E>(graph,cplex,edgeVariables,printOutput);
		this.userCallbackCutSet = new UserCallbackCutSet<V,E>(graph, cplex, edgeVariables,minCutSolver, cutVal);
	}
}
