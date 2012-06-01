package main;

import ilog.concert.IloException;
import tspIps.AbstractTspFormulationEdgeBased;
import tspIps.TspDynamicCutSet;
import tspIps.TspSubTourEliminationLazy;
import tspLib.GeoNode;
import tspLib.IntNode;
import tspLib.TspLibInstance;
import tspLib.TspLibParser;
import tspLib.TspLibParser.UnsupportedFileTypeException;
import tspLib.WeightedEdge;

public class Main {
	
	public static void main(String[] args){
		//brd14051 is too big
		String[] problemNames = new String[]{"bier127","eil51","brd14051","ch130","ch150","d198", "d493"};
		String problemName = problemNames[1];
		try {
			TspLibInstance<GeoNode> instance = TspLibParser.parse(problemName);
			AbstractTspFormulationEdgeBased<GeoNode,WeightedEdge> tsp =
					new TspDynamicCutSet<GeoNode,WeightedEdge>(instance.getGraph(), instance.getEdgeWeights());
					//new TspSubTourEliminationLazy<GeoNode,WeightedEdge>(instance.getGraph(), instance.getEdgeWeights());
			tsp.solve();
			tsp.getOptimalTour();
			System.out.println(tsp.getOptimalCost());
			tsp.cleanUp();
			
		} catch (UnsupportedFileTypeException e) {
			throw new RuntimeException(e);
		} catch (IloException e) {
			throw new RuntimeException(e);
		}
	}

}
