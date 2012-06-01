package tspIps;

import util.CplexUtil;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;

import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class DegreeConstraints<V,E> {
	
	private EdgeVariables<V,E> edgeVariables;
	private UndirectedGraph<V,E> graph;
	private IloCplex cplex;
	private ImmutableBiMap<V,IloLinearIntExpr> degree;
	private ImmutableBiMap<V,IloRange> degreeConstraint;
	private int degreeEquals;
	
	public DegreeConstraints(EdgeVariables<V, E> edgeVariables,
			UndirectedGraph<V, E> graph, IloCplex cplex, int degreeEquals) throws IloException {
		super();
		this.edgeVariables = edgeVariables;
		this.graph = graph;
		this.cplex = cplex;
		this.degreeEquals = degreeEquals;
		degree = CplexUtil.makeLinearIntExpr(cplex, graph.getVertices());
		for(E edge: graph.getEdges()){
			Pair<V> endpoints = graph.getEndpoints(edge);
			for(V vertex: endpoints){
				degree.get(vertex).addTerm(edgeVariables.getEdgeVars().get(edge), 1);
			}			
		}
		Builder<V,IloRange> builder = ImmutableBiMap.builder();
		for(V vertex: degree.keySet()){			
			builder.put(vertex, cplex.addEq(degree.get(vertex), degreeEquals));
		}
		degreeConstraint = builder.build();
	}
	
	public DegreeConstraints(UndirectedGraph<V, E> graph, IloCplex cplex, int degreeEquals) throws IloException  {
		this(new EdgeVariables<V,E>(graph,cplex),graph,cplex,degreeEquals);
	}
	
	

}
