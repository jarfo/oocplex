package tspIps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ilog.concert.IloConversion;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;
import ilog.cplex.IloCplex.UnknownObjectException;

import org.apache.commons.collections15.Transformer;

import util.CplexUtil;

import edu.uci.ics.jung.graph.UndirectedGraph;

public class TspDynamicCutSet<V,E> extends AbstractTspFormulationEdgeBased<V, E> {
	
private DynamicCutSet<V,E> dynamicCutSet;
	
	public TspDynamicCutSet(UndirectedGraph<V, E> graph,
			Transformer<E, Double> edgeWeights) throws IloException{
		this(graph,edgeWeights,new IloCplex());
		cplex.setParam(IloCplex.IntParam.Threads, 4);
		
	}

	public TspDynamicCutSet(UndirectedGraph<V, E> graph,
			Transformer<E, Double> edgeWeights, IloCplex cplex)
			throws IloException {
		super(graph, edgeWeights, cplex);
		this.dynamicCutSet = new DynamicCutSet<V,E>(graph,cplex,edgeVariables);
	}
	
	@Override
	public void solve() throws IloException{
		boolean isATour = false;
		while(!isATour){
		Set<IloConversion> lpRelaxation = new HashSet<IloConversion>();
		for(IloIntVar edgeVar : this.edgeVariables.getEdgeVars().inverse().keySet()){
			IloConversion conversion = cplex.conversion(edgeVar, IloNumVarType.Float);
			cplex.add(conversion);
			lpRelaxation.add(conversion);
		}
		boolean foundCut = true;
		while(foundCut){
			System.out.println("Beginning LP");
			cplex.solve();
			System.out.println("LP value: " + cplex.getObjValue());
			foundCut = this.dynamicCutSet.addViolatedCut();
			
		}
		for(IloConversion conversion: lpRelaxation){
			cplex.remove(conversion);
		}
		System.out.println("Beginning LP");
		cplex.solve();
		System.out.println("IP value: " + cplex.getObjValue());
		List<List<E>> tours = this.extractTours();
		
		isATour = tours.size() ==1;
		if(!isATour){
			System.out.println(tours.size() + " sub tours");
			for(List<E> tour: tours){
				List<E> cutEdges = getAllEdgesLeavingTour(tour);
				IloLinearIntExpr cut = cplex.linearIntExpr();
				for(E edge: cutEdges){
					cut.addTerm(this.edgeVariables.getEdgeVars().get(edge), 1);
				}
				cplex.addGe(cut,2);
			}
		}
		}
		
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
	
	private List<E> getAllEdgesLeavingTour(List<E> tour){
		List<E> ans = new ArrayList<E>();
		Set<V> inTour = new HashSet<V>();
		for(E edge: tour){
			inTour.addAll(graph.getEndpoints(edge));
		}
		for(V vertex: inTour){
			for(E edge: graph.getIncidentEdges(vertex)){
				if(!inTour.contains(graph.getOpposite(vertex, edge))){
					ans.add(edge);
				}
			}
		}
		return ans;
	}
	
	/*private boolean currentSolutionIsInteger() throws IloException{
		for(IloIntVar edgeVar : this.edgeVariables.getEdgeVars().inverse().keySet()){
			double value = cplex.getValue(edgeVar);
			if(Math.abs(1-value) < CplexUtil.epsilon ){
				
			}
			else if(Math.abs(value) < CplexUtil.epsilon){
				
			}
			else{
				return false;
			}
		}
		return true;
	}*/

}
