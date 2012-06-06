package tspIps;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;
import ilog.cplex.IloCplex.UnknownObjectException;

import org.apache.commons.collections15.Transformer;

import util.CplexUtil;

import edu.uci.ics.jung.graph.UndirectedGraph;

public abstract class AbstractTspFormulationEdgeBased<V,E> {
	
	protected UndirectedGraph<V,E> graph;
	protected Transformer<E,Double> edgeWeights;
	protected EdgeVariables<V,E> edgeVariables;
	protected IloCplex cplex;
	protected DegreeConstraints<V,E> degreeConstraints;
	protected MinEdgeWeightObjective<V,E> objective;
	protected List<E> optimalTour;
	protected double optimalCost;
	

	
	protected AbstractTspFormulationEdgeBased(UndirectedGraph<V,E> graph, Transformer<E,Double> edgeWeights, IloCplex cplex) throws IloException{
		this.graph = graph;
		this.edgeWeights = edgeWeights;
		this.cplex = cplex;
		this.edgeVariables = new EdgeVariables<V,E>(graph,cplex);
		this.degreeConstraints = new DegreeConstraints<V,E>(edgeVariables,graph,cplex,2);
		this.objective = new MinEdgeWeightObjective<V,E>(graph,edgeWeights,edgeVariables,cplex);
				
	}
	
	public void solve() throws IloException{
		this.cplex.solve();
		if(cplex.getStatus() == Status.Feasible || cplex.getStatus() == Status.Optimal){
			this.optimalCost = cplex.getObjValue();
			this.optimalTour = extractTour();
			/*double value = 0;
			for(E edge: optimalTour){
				value += this.edgeWeights.transform(edge);
			}
			System.out.println("manually tallied:  " + value);*/
		}
		else{
			this.optimalCost = 0;
			this.optimalTour = null;
		}
		
	}
	
	public void cleanUp(){
		cplex.end();
	}
	
	
	
	public List<E> getOptimalTour() {
		return optimalTour;
	}

	public double getOptimalCost() {
		return optimalCost;
	}

	protected List<E> extractTour() throws IloException{
		List<List<E>> tours = extractTours();
		if(tours.size() != 1){
			throw new RuntimeException();
		}
		return tours.get(0);
	}
	
	protected List<List<E>> extractTours() throws IloException{
		Set<E> edgesUsed = new HashSet<E>();
		for(E edge: edgeVariables.getEdgeVars().keySet() ){
			if(CplexUtil.doubleToBoolean(cplex.getValue(edgeVariables.getEdgeVars().get(edge)))){
				edgesUsed.add(edge);
			}
		}
		List<List<E>> tours = LoopExtractor.subTours(graph, edgesUsed);
		return tours;
	}

}
