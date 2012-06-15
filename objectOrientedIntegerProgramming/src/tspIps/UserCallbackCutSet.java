package tspIps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import util.CplexUtil;

import minCut.Cut;
import minCut.MinCutSolver;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.DoubleParam;
import ilog.cplex.IloCplex.IntParam;
import ilog.cplex.IloCplex.LongParam;

import edu.uci.ics.jung.graph.UndirectedGraph;

public class UserCallbackCutSet<V,E> {

	private UndirectedGraph<V,E> graph;
	private IloCplex cplex;
	private EdgeVariables<V,E> edgeVariables;
	private MinCutSolver<V,E> minCutSolver;
	private double cutVal;
	private boolean haltUser;
	
	public UserCallbackCutSet(UndirectedGraph<V, E> graph, IloCplex cplex, EdgeVariables<V,E> edgeVariables,
			MinCutSolver<V,E> minCutSolver) throws IloException {
		this(graph,cplex,edgeVariables,minCutSolver,2);
	}
	
	public UserCallbackCutSet(UndirectedGraph<V, E> graph, IloCplex cplex, EdgeVariables<V,E> edgeVariables,MinCutSolver<V,E> minCutSolver, double cutVal) throws IloException {
		super();
		this.graph = graph;
		this.cplex = cplex;
		this.edgeVariables = edgeVariables;
		this.minCutSolver = minCutSolver;
		this.cutVal = cutVal;
		haltUser = false;
		cplex.use(new CutSetCallback());
		//cplex.setParam(DoubleParam.CutsFactor, 1);
		//cplex.setParam(IntParam.PreslvNd, -1);
		//cplex.setParam(IntParam.RelaxPreInd, 0);
		//cplex.setParam(LongParam.CutPass, -1);
	}
	
	private class CutSetCallback extends IloCplex.UserCutCallback {
		
		public CutSetCallback(){}

		@Override
		protected void main() throws IloException {
			//System.err.println("attempting cut");
			if(!this.isAfterCutLoop()){
				return;
			}
			//System.err.println("checking haltUser");
			if(haltUser){
				return;
			}
			//System.err.println("checking integer");
			final Map<E,Double> edgeWeights = new HashMap<E,Double>(edgeVariables.getEdgeVars().size()*2);
			boolean isIntegral = true;
			IloIntVar[] edgeVars = edgeVariables.getEdgeVars().values().toArray(new IloIntVar[]{});
			double[] edgeVarVals = this.getValues(edgeVars);
			for(int i = 0; i < edgeVars.length; i++){
				edgeWeights.put(edgeVariables.getEdgeVars().inverse().get(edgeVars[i]), Double.valueOf(edgeVarVals[i]));
				isIntegral &= CplexUtil.isBinaryIntegral(edgeVarVals[i]);
			}
			
			
			if(isIntegral){
				System.err.println("Found integral in user callback");
				return;
			}
			//System.err.println("not integer, proceeding");
			
			Iterable<Cut<E>> cuts = minCutSolver.findCutsLessThan(graph, new Transformer<E,Number>(){
				@Override
				public Number transform(E arg0) {
					return edgeWeights.get(arg0);
				}}, 2);
			double bestCut = Double.MAX_VALUE;
			//int count = 0;
			for(Cut<E> graphCutEdges: cuts){
				//count++;
				IloLinearIntExpr cut = cplex.linearIntExpr();
				double cutVal = graphCutEdges.getWeight();
				bestCut = Math.min(bestCut, cutVal);
				for(E edge: graphCutEdges.getEdges()){
					cut.addTerm(edgeVariables.getEdgeVars().get(edge), 1);
				}
				this.add(cplex.ge(cut, 2));
			}
			//System.err.println("added " + count + " cuts");
			if(bestCut > cutVal){
				haltUser = true;
				//cplex.setParam(LongParam.CutPass, 0);
				//cplex.setParam(DoubleParam.CutsFactor, 4);
				//cplex.setParam(IntParam.PreslvNd, 0);
				//cplex.setParam(IntParam.RelaxPreInd, -1);
				System.err.println("halting user cuts");
				
			}
		}
		
		
	}
	
	private static <EE> double getCutValue(Set<EE> cut, Map<EE,Double> edgeValue){
		double ans = 0;
		for(EE edge: cut){
			ans += edgeValue.get(edge).doubleValue();
		}
		return ans;
	}
		
}
