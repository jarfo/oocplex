package tspIps;

import java.util.HashSet;
import java.util.Set;

import util.CplexUtil;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.cplex.IloCplex;

import com.google.common.collect.ImmutableBiMap;

import edu.uci.ics.jung.graph.UndirectedGraph;

public class EdgeVariables<V,E> {
	
	private ImmutableBiMap<E,IloIntVar> edgeVars;
	
	public EdgeVariables(UndirectedGraph<V,E> graph, IloCplex cplex) throws IloException{
		edgeVars = CplexUtil.makeBinaryVariables(cplex, graph.getEdges());
	}

	public ImmutableBiMap<E, IloIntVar> getEdgeVars() {
		return edgeVars;
	}
	
	

}
