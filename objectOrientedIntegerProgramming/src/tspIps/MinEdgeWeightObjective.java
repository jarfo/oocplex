package tspIps;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.UndirectedGraph;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;

public class MinEdgeWeightObjective<V,E> {
	
	private IloLinearNumExpr objective;
	private Transformer<E,Double> edgeWeights;
	private UndirectedGraph<V,E> graph;
	private IloCplex cplex;
	private EdgeVariables<V,E> edgeVariables;
	
	public MinEdgeWeightObjective(UndirectedGraph<V,E> graph, Transformer<E,Double> edgeWeights, EdgeVariables<V,E> edgeVariables, IloCplex cplex ) throws IloException{
		this.graph = graph;
		this.edgeWeights = edgeWeights;
		this.cplex = cplex;
		objective = cplex.linearNumExpr();
		for(E edge: graph.getEdges()){			
			objective.addTerm(edgeVariables.getEdgeVars().get(edge), edgeWeights.transform(edge).doubleValue());
		}
		cplex.addMinimize(objective);
	}

	public IloLinearNumExpr getObjective() {
		return objective;
	}

	public Transformer<E, Double> getEdgeWeights() {
		return edgeWeights;
	}

	public UndirectedGraph<V, E> getGraph() {
		return graph;
	}

	public IloCplex getCplex() {
		return cplex;
	}

	public EdgeVariables<V, E> getEdgeVariables() {
		return edgeVariables;
	}
	
	
	
	

}
