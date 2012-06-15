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

import minCut.MinCutSolver;

import org.apache.commons.collections15.Transformer;

import util.CplexUtil;

import edu.uci.ics.jung.graph.UndirectedGraph;

public class TspDynamicCutSet<V,E> extends AbstractTspFormulationEdgeBased<V, E> {
	
	private DynamicCutSet<V,E> dynamicCutSet;
	private MinCutSolver<V,E> minCutSolver;
	private double cutToStopCuttingPlanes;
	private LazySubtourElimination<V,E> subtourElimination;
	
	public TspDynamicCutSet(UndirectedGraph<V, E> graph,
			Transformer<E, Double> edgeWeights, MinCutSolver<V,E> minCutSolver, double cutToStopCuttingPlanes) throws IloException{
		this(graph,edgeWeights,minCutSolver,cutToStopCuttingPlanes,new IloCplex());
		
		
	}

	public TspDynamicCutSet(UndirectedGraph<V, E> graph,
			Transformer<E, Double> edgeWeights,MinCutSolver<V,E> minCutSolver,double cutToStopCuttingPlanes, IloCplex cplex)
			throws IloException {
		super(graph, edgeWeights, cplex);
		cplex.setParam(IloCplex.IntParam.Threads, 8);
		this.minCutSolver = minCutSolver;
		this.dynamicCutSet = new DynamicCutSet<V,E>(graph,cplex,edgeVariables,minCutSolver);
		this.cutToStopCuttingPlanes = cutToStopCuttingPlanes;
		subtourElimination = new LazySubtourElimination<V,E>(graph,cplex,edgeVariables,true);
		subtourElimination.getCallback().setOn(false);
	}
	
	@Override
	public void solve() throws IloException{
		//boolean isATour = false;
		boolean cuttingPlaneMode = true;
		//while(!isATour && cuttingPlaneMode){
			Set<IloConversion> lpRelaxation = new HashSet<IloConversion>();
			for(IloIntVar edgeVar : this.edgeVariables.getEdgeVars().inverse().keySet()){
				IloConversion conversion = cplex.conversion(edgeVar, IloNumVarType.Float);
				cplex.add(conversion);
				lpRelaxation.add(conversion);
			}
			//boolean foundCut = true;
			while(cuttingPlaneMode){
				System.out.println("Beginning LP");
				cplex.solve();
				System.out.println("LP value: " + cplex.getObjValue());
				double bestCut = this.dynamicCutSet.addViolatedCut();
				//if(bestCut == Double.MAX_VALUE){
				//	foundCut = false;
				//}
				if(bestCut> this.cutToStopCuttingPlanes){
					cuttingPlaneMode = false;
				}

			}
			for(IloConversion conversion: lpRelaxation){
				cplex.remove(conversion);
			}
			subtourElimination.getCallback().setOn(true);
			System.out.println("Beginning IP");
			cplex.solve();
			System.out.println("IP value: " + cplex.getObjValue());
			List<List<E>> tours = this.extractTours();

			/*isATour = tours.size() ==1;
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
			}*/
		
		
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
