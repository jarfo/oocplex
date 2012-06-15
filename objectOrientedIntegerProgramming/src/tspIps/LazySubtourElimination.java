package tspIps;

import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.CplexUtil;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class LazySubtourElimination<V,E> {
	
	private UndirectedGraph<V,E> graph;
	private IloCplex cplex;
	private EdgeVariables<V,E> edgeVariables;
	private boolean printOutput;
	private SubtourEliminationCallback callback;
	
	private void print(String s){
		if(this.printOutput){
			System.out.println(s);
		}
	}
	
	public LazySubtourElimination(UndirectedGraph<V, E> graph, IloCplex cplex, EdgeVariables<V,E> edgeVariables, boolean printOutput) throws IloException {
		super();
		this.graph = graph;
		this.cplex = cplex;
		this.edgeVariables = edgeVariables;
		this.callback = new SubtourEliminationCallback();
		cplex.use(callback);
	}
	
	SubtourEliminationCallback getCallback(){
		return this.callback;
	}
	
	public class SubtourEliminationCallback extends IloCplex.LazyConstraintCallback {
		
		private boolean on;
		
		public SubtourEliminationCallback(){
			this.on = true;
		}
		
		public void setOn(boolean value){
			this.on = value;
		}

		@Override
		protected void main() throws IloException {
			if(on){
			Set<E> edgesUsed = new HashSet<E>();
			for(E edge: edgeVariables.getEdgeVars().keySet() ){
				if(CplexUtil.doubleToBoolean(this.getValue(edgeVariables.getEdgeVars().get(edge)))){
					edgesUsed.add(edge);
				}
			}
			List<List<E>> loops = LoopExtractor.subTours(graph,edgesUsed);
			if(loops.size()>1){
				for(List<E> loop: loops){
					IloLinearIntExpr loopVars = cplex.linearIntExpr();
					for(E e: loop){
						loopVars.addTerm(edgeVariables.getEdgeVars().get(e), 1);
					}
					this.add(cplex.le(loopVars, loop.size()-1));
				}
				print("Added " + loops.size() + " lazy constraints");
			}
		}
		}
	}



	
	

}
