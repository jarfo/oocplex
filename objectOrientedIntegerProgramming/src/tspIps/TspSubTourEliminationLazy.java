package tspIps;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.UndirectedGraph;

public class TspSubTourEliminationLazy<V, E> extends
		AbstractTspFormulationEdgeBased<V, E> {
	
	private LazySubtourElimination<V,E> lazySubtourElimination;
	
	public TspSubTourEliminationLazy(UndirectedGraph<V, E> graph,
			Transformer<E, Double> edgeWeights) throws IloException{
		this(graph,edgeWeights,new IloCplex());
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		
	}

	public TspSubTourEliminationLazy(UndirectedGraph<V, E> graph,
			Transformer<E, Double> edgeWeights, IloCplex cplex)
			throws IloException {
		super(graph, edgeWeights, cplex);
		this.lazySubtourElimination = new LazySubtourElimination<V,E>(graph,cplex,edgeVariables);
	}

}
