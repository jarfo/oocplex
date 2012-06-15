package main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import minCut.EdmondsKarpMinCutSolver;
import minCut.KargerMinCut;
import minCut.KargerSteinMinCut;
import minCut.MinCutSolver;
import minCut.StoerWagnerMinimumCutWrapper;
import ilog.concert.IloException;
import tspIps.AbstractTspFormulationEdgeBased;
import tspIps.TspDynamicCutSet;
import tspIps.TspSubTourEliminationLazy;
import tspIps.TspUserCutSetIp;
import tspLib.GeoNode;
import tspLib.IntNode;
import tspLib.TspLibInstance;
import tspLib.TspLibParser;
import tspLib.TspLibParser.UnsupportedFileTypeException;
import tspLib.WeightedEdge;

public class Main {
	
	public static void main(String[] args){
		//brd14051 is too big
		String[] problemNames = new String[]{"bier127","eil51","brd14051","ch130","ch150","d198", "d493","d1291","fl1400"};
		String problemName = problemNames[1];
		try {
			TspLibInstance<GeoNode> instance = TspLibParser.parse(problemName);
			int nThreads = 8;
			ExecutorService exec = Executors.newFixedThreadPool(nThreads);
			MinCutSolver<GeoNode,WeightedEdge> minCutSolver =
					//new StoerWagnerMinimumCutWrapper<GeoNode,WeightedEdge>(true);
					new KargerSteinMinCut<GeoNode,WeightedEdge>(exec,nThreads,2,true,100);
			//new EdmondsKarpMinCutSolver<GeoNode,WeightedEdge>(false,false);
			AbstractTspFormulationEdgeBased<GeoNode,WeightedEdge> tsp =
					//new TspDynamicCutSet<GeoNode,WeightedEdge>(instance.getGraph(), instance.getEdgeWeights(),minCutSolver,1.9);
					new TspUserCutSetIp<GeoNode,WeightedEdge>(instance.getGraph(), instance.getEdgeWeights(),minCutSolver,1.9,true);
					//new TspSubTourEliminationLazy<GeoNode,WeightedEdge>(instance.getGraph(), instance.getEdgeWeights());
			tsp.solve();
			tsp.getOptimalTour();
			System.out.println(tsp.getOptimalCost());
			tsp.cleanUp();
			exec.shutdown();
			
		} catch (UnsupportedFileTypeException e) {
			throw new RuntimeException(e);
		} catch (IloException e) {
			throw new RuntimeException(e);
		}
	}

}
