package tspIps;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.UndirectedGraph;

public class TspDynamicCutSet<V,E> extends AbstractTspFormulationEdgeBased<V, E> {
	
private DynamicCutSet<V,E> dynamicCutSet;
	
	public TspDynamicCutSet(UndirectedGraph<V, E> graph,
			Transformer<E, Double> edgeWeights) throws IloException{
		this(graph,edgeWeights,new IloCplex());
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		
	}

	public TspDynamicCutSet(UndirectedGraph<V, E> graph,
			Transformer<E, Double> edgeWeights, IloCplex cplex)
			throws IloException {
		super(graph, edgeWeights, cplex);
		this.dynamicCutSet = new DynamicCutSet<V,E>(graph,cplex,edgeVariables);
	}

}
