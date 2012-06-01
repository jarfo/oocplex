package tspIps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;

import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class DynamicCutSet<V,E> {
	
	private UndirectedGraph<V,E> graph;
	private IloCplex cplex;
	private EdgeVariables<V,E> edgeVariables;
	
	private static int rescaling = 10000;
	
	
	public DynamicCutSet(UndirectedGraph<V, E> graph, IloCplex cplex, EdgeVariables<V,E> edgeVariables) throws IloException {
		super();
		this.graph = graph;
		this.cplex = cplex;
		this.edgeVariables = edgeVariables;
		cplex.use(new DynamicCutSetCallBackUser());
		cplex.use(new DynamicCutSetCallBack());
	}
	
	private static class WeightedEdge{
		private double weight;
		
		public WeightedEdge(double weight){
			this.weight = weight;
		}
		
		public double getWeight(){
			return this.weight;
		}
		
		
	}
	
	public static class WeightedEdgeFactory implements Factory<WeightedEdge>{
		
		public static final WeightedEdgeFactory instance = new WeightedEdgeFactory();
		
		private WeightedEdgeFactory(){};

		@Override
		public WeightedEdge create() {
			return new WeightedEdge(0);
		}
		
	}
	
	private static class WeightedEdgeTransformer implements Transformer<WeightedEdge,Number>{
		
		public static final WeightedEdgeTransformer instance = new WeightedEdgeTransformer();
		
		private WeightedEdgeTransformer(){}

		@Override
		public Double transform(WeightedEdge arg0) {
			return arg0.getWeight()*rescaling;
		}
		
	}
	
	private class DynamicCutSetCallBack extends IloCplex.LazyConstraintCallback{
		
		

		@Override
		protected void main() throws IloException {
			
			Map<E,Double> edgeWeights = new HashMap<E,Double>();
			for(E edge: edgeVariables.getEdgeVars().keySet()){
				edgeWeights.put(edge, this.getValue(edgeVariables.getEdgeVars().get(edge)));
			}
			DirectedGraph<V,WeightedEdge> diGraph = new DirectedSparseGraph<V,WeightedEdge>();
			for(V vertex: graph.getVertices()){
				diGraph.addVertex(vertex);
			}
			
			for(E oldEdge: graph.getEdges()){
				Pair<V> nodes = graph.getEndpoints(oldEdge);
				double weight = edgeWeights.get(oldEdge).doubleValue();
				diGraph.addEdge(new WeightedEdge(weight), nodes.getFirst(), nodes.getSecond());
				diGraph.addEdge(new WeightedEdge(weight), nodes.getSecond(), nodes.getFirst());
			}
			for(V source: graph.getVertices()){
				for(V sink: graph.getVertices()){
					if(source != sink){
						HashMap<WeightedEdge,Number> ans = new HashMap<WeightedEdge,Number>();
						EdmondsKarpMaxFlow<V,WeightedEdge> maxFlow = new EdmondsKarpMaxFlow<V,WeightedEdge>(diGraph, source, sink, 
								WeightedEdgeTransformer.instance, ans,	WeightedEdgeFactory.instance);
						maxFlow.evaluate();
						if(maxFlow.getMaxFlow() < 2*rescaling){
							Set<WeightedEdge> diGraphCutEdges = maxFlow.getMinCutEdges();
							Set<E> graphCutEdges = new HashSet<E>();
							for(WeightedEdge diGraphEdge: diGraphCutEdges){
								V home = diGraph.getSource(diGraphEdge);
								V target = diGraph.getDest(diGraphEdge);
								graphCutEdges.add(graph.findEdge(home, target));
							}
							IloLinearIntExpr cut = cplex.linearIntExpr();
							for(E edge: graphCutEdges){
								cut.addTerm(edgeVariables.getEdgeVars().get(edge), 1);
							}
							this.add(cplex.ge(cut, 2));
						}
					}
				}
			}
			
		}
		
	}
	
	private class DynamicCutSetCallBackUser extends IloCplex.UserCutCallback{
		
		

		@Override
		protected void main() throws IloException {
			
			Map<E,Double> edgeWeights = new HashMap<E,Double>();
			for(E edge: edgeVariables.getEdgeVars().keySet()){
				edgeWeights.put(edge, this.getValue(edgeVariables.getEdgeVars().get(edge)));
			}
			DirectedGraph<V,WeightedEdge> diGraph = new DirectedSparseGraph<V,WeightedEdge>();
			for(V vertex: graph.getVertices()){
				diGraph.addVertex(vertex);
			}
			
			for(E oldEdge: graph.getEdges()){
				Pair<V> nodes = graph.getEndpoints(oldEdge);
				double weight = edgeWeights.get(oldEdge).doubleValue();
				diGraph.addEdge(new WeightedEdge(weight), nodes.getFirst(), nodes.getSecond());
				diGraph.addEdge(new WeightedEdge(weight), nodes.getSecond(), nodes.getFirst());
			}
			for(V source: graph.getVertices()){
				for(V sink: graph.getVertices()){
					if(source != sink){
						HashMap<WeightedEdge,Number> ans = new HashMap<WeightedEdge,Number>();
						EdmondsKarpMaxFlow<V,WeightedEdge> maxFlow = new EdmondsKarpMaxFlow<V,WeightedEdge>(diGraph, source, sink, 
								WeightedEdgeTransformer.instance, ans,	WeightedEdgeFactory.instance);
						maxFlow.evaluate();
						if(maxFlow.getMaxFlow() < 2*rescaling){
							Set<WeightedEdge> diGraphCutEdges = maxFlow.getMinCutEdges();
							Set<E> graphCutEdges = new HashSet<E>();
							for(WeightedEdge diGraphEdge: diGraphCutEdges){
								V home = diGraph.getSource(diGraphEdge);
								V target = diGraph.getDest(diGraphEdge);
								graphCutEdges.add(graph.findEdge(home, target));
							}
							IloLinearIntExpr cut = cplex.linearIntExpr();
							for(E edge: graphCutEdges){
								cut.addTerm(edgeVariables.getEdgeVars().get(edge), 1);
							}
							this.add(cplex.ge(cut, 2));
						}
					}
				}
			}
			
		}
		
	}

}
