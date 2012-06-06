package minCut;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.apache.commons.collections15.Transformer;

import tspIps.RandomInterface;
import tspIps.RandomJavaUtil;

import edu.uci.ics.jung.graph.UndirectedGraph;

public class KargerMinCut<V,E> implements MinCutSolver<V, E> {
	
	public KargerMinCut(ExecutorService exec, int numThreads, double numTrialsMultiplier, 
			boolean printOutput, boolean minOnly, RandomInterface[] randoms){
		this.exec = exec;
		this.numTrialsMultiplier = numTrialsMultiplier;
		this.numThreads = numThreads;
		this.printOutput = printOutput;
		this.minOnly = minOnly;
		this.randoms = randoms;
	}
	
	
	public KargerMinCut(ExecutorService exec, int numThreads, double numTrialsMultiplier, 
			boolean printOutput, boolean minOnly){
		this(exec,numThreads,numTrialsMultiplier,printOutput,minOnly, defaultRandoms(numThreads));
	}
	
	private static RandomInterface[] defaultRandoms(int numThreads){
		RandomInterface[] ans = new RandomInterface[numThreads];
		for(int i = 0; i < numThreads; i++){
			ans[i] = new RandomJavaUtil();
		}
		return ans;
	}
	
	
	private Executor exec;
	private double numTrialsMultiplier;
	private int numThreads;
	private boolean printOutput;
	private boolean minOnly;
	private RandomInterface[] randoms;

	@Override
	public List<Set<E>> findCutsLessThan(final UndirectedGraph<V, E> graph,
			final Transformer<E, Number> edgeWeights, double value) {
		List<Set<E>> ans = new ArrayList<Set<E>>();
		int n = graph.getVertexCount();
		int numTrials = (int)(numTrialsMultiplier*n*n*Math.log(n));
		List<Callable<List<Set<E>>>> tasks = new ArrayList<Callable<List<Set<E>>>>();

		final int trialsPerThread = (int)Math.ceil(numTrials/(double)numThreads);
		for(int i = 0; i < numThreads; i++){
			tasks.add(new Callable<List<Set<E>>>(){

				@Override
				public List<Set<E>> call() throws Exception {
					List<Set<E>> ans = new ArrayList<Set<E>>();
					for(int i = 0; i < trialsPerThread; i++){
						KargerRandomCut<V,E> kargerCut = new KargerRandomCut<V,E>(graph,randoms[i]);
						kargerCut.allContractions();
						Set<E> cut = kargerCut.getCut();
						double value = 0;
						for(E e: cut){
							value += edgeWeights.transform(e).doubleValue();
						}
					}
					return ans;
				}
				
			});
		}
		
		return null;
	}
	

}
