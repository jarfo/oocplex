package tspIps;
import static org.junit.Assert.*;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import minCut.EdmondsKarpMinCutSolver;
import minCut.MinCutSolver;

import org.apache.commons.collections15.Transformer;
import org.junit.Test;

import util.CplexUtil;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;


public class TspSubTourEliminationTest {
	
	private static double tolerance = .00001;
	
	private static enum Node{
		a,b,c,d,e,f;
		
		private static enum Edge{
			ab(a,b,1),bc(b,c,1),ac(a,c,1),
			bd(b,d,10),ce(c,e,10),de(d,e,1),
			df(d,f,1),ef(e,f,1);
			
			private Node first;
			private Node second;
			private double weight;
			
			private Edge(Node first, Node second, double weight){
				this.weight = weight;
				this.first = first;
				this.second = second;
			}

			public Node getFirst() {
				return first;
			}

			public Node getSecond() {
				return second;
			}

			public double getWeight() {
				return weight;
			}
		}
		
		public static UndirectedGraph<Node,Edge> makeGraph(){
			UndirectedGraph<Node,Edge> ans = new UndirectedSparseGraph<Node,Edge>();
			for(Node node: Node.values()){
				ans.addVertex(node);
			}
			for(Edge edge: Node.Edge.values()){
				ans.addEdge(edge, edge.getFirst(),edge.getSecond());
			}			
			return ans;
		}
		
		public static Transformer<Edge,Double> makeEdgeWeights(){
			return new Transformer<Edge,Double>(){

				@Override
				public Double transform(Edge edge) {
					return Double.valueOf(edge.getWeight());
				}};
		}
	}
	
	
	@Test
	public void testDegreeConstraints(){
		UndirectedGraph<Node,Node.Edge> graph = Node.makeGraph();
		Transformer<Node.Edge,Double> edgeWeights = Node.makeEdgeWeights();
		try {
			IloCplex cplex = new IloCplex();
			EdgeVariables<Node,Node.Edge> vars = new EdgeVariables<Node,Node.Edge>(graph,cplex);
			DegreeConstraints<Node,Node.Edge> degreeConstraints = new DegreeConstraints<Node,Node.Edge>(vars,graph,cplex,2);
			MinEdgeWeightObjective<Node, Node.Edge> objective = new MinEdgeWeightObjective<Node,Node.Edge>(graph,edgeWeights,vars,cplex);
			cplex.solve();
			assertEquals(6,cplex.getObjValue(),tolerance);
			Set<Node.Edge> edgesUsed = new HashSet<Node.Edge>();
			for(Node.Edge edge: vars.getEdgeVars().keySet() ){
				if(CplexUtil.doubleToBoolean(cplex.getValue(vars.getEdgeVars().get(edge)))){
					edgesUsed.add(edge);
				}
			}
			List<List<Node.Edge>> tours = LoopExtractor.subTours(graph, edgesUsed);
			assertEquals(2,tours.size());
			assertEquals(3,tours.get(0).size());
			assertEquals(3,tours.get(1).size());
			Set<EnumSet<Node.Edge>> toursAsSet = new HashSet<EnumSet<Node.Edge>>();
			for(List<Node.Edge> tour: tours){
				toursAsSet.add(EnumSet.copyOf(tour));
			}
			Set<EnumSet<Node.Edge>> expected = new HashSet<EnumSet<Node.Edge>>();
			expected.add(EnumSet.of(Node.Edge.ab,Node.Edge.bc, Node.Edge.ac));
			expected.add(EnumSet.of(Node.Edge.de,Node.Edge.ef, Node.Edge.df));
			assertEquals(expected,toursAsSet);
			cplex.end();
			
		} catch (IloException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testSubTourLazy() {
		UndirectedGraph<Node,Node.Edge> graph = Node.makeGraph();
		Transformer<Node.Edge,Double> edgeWeights = Node.makeEdgeWeights();
		try {
			TspSubTourEliminationLazy<Node,Node.Edge> tsp = new TspSubTourEliminationLazy<Node,Node.Edge>(graph,edgeWeights,false);
			tsp.solve();
			assertEquals(24,tsp.getOptimalCost(),tolerance);
			List<Node.Edge> optimalTour = tsp.getOptimalTour();
			assertEquals(6,optimalTour.size());
			EnumSet<Node.Edge> tourAsSet = EnumSet.copyOf(optimalTour);
			EnumSet<Node.Edge> expected = EnumSet.of(Node.Edge.ab, Node.Edge.bd, Node.Edge.df, Node.Edge.ef, Node.Edge.ce, Node.Edge.ac);
			assertEquals(expected,tourAsSet);
			tsp.cleanUp();
		} catch (IloException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	@Test
	public void testDynamicCutSet() {
		UndirectedGraph<Node,Node.Edge> graph = Node.makeGraph();
		Transformer<Node.Edge,Double> edgeWeights = Node.makeEdgeWeights();
		MinCutSolver<Node,Node.Edge> solver = new EdmondsKarpMinCutSolver<Node,Node.Edge>(false,false);
		try {
			TspDynamicCutSet<Node,Node.Edge> tsp = new TspDynamicCutSet<Node,Node.Edge>(graph,edgeWeights,solver,1.9);
			tsp.solve();
			assertEquals(24,tsp.getOptimalCost(),tolerance);
			List<Node.Edge> optimalTour = tsp.getOptimalTour();
			assertEquals(6,optimalTour.size());
			EnumSet<Node.Edge> tourAsSet = EnumSet.copyOf(optimalTour);
			EnumSet<Node.Edge> expected = EnumSet.of(Node.Edge.ab, Node.Edge.bd, Node.Edge.df, Node.Edge.ef, Node.Edge.ce, Node.Edge.ac);
			assertEquals(expected,tourAsSet);
			tsp.cleanUp();
		} catch (IloException e) {
			throw new RuntimeException(e);
		}
		
	}

}
