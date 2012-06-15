package minCut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import edu.uci.ics.jung.graph.UndirectedGraph;

public class KargerSteinMinCut<V,E> implements MinCutSolver<V, E> {
	
	private ExecutorService exec;
	private double numTrialsMultiplier;
	private int numThreads;
	private boolean printOutput;
	private final int maxNumCuts;
	private RandomData[] randoms;
	
	public KargerSteinMinCut(ExecutorService exec, int numThreads, double numTrialsMultiplier, 
			boolean printOutput, int maxNumCuts, RandomData[] randoms){
		this.exec = exec;
		this.numTrialsMultiplier = numTrialsMultiplier;
		this.numThreads = numThreads;
		this.printOutput = printOutput;
		this.maxNumCuts = maxNumCuts;
		this.randoms = randoms;
	}
	
	
	public KargerSteinMinCut(ExecutorService exec, int numThreads, double numTrialsMultiplier, 
			boolean printOutput, int maxNumCuts){
		this(exec,numThreads,numTrialsMultiplier,printOutput,maxNumCuts, defaultRandoms(numThreads));
	}
	
	private static RandomData[] defaultRandoms(int numThreads){
		RandomData[] ans = new RandomData[numThreads];
		for(int i = 0; i < numThreads; i++){
			ans[i] = new RandomDataImpl();
		}
		return ans;
	}
	
	private void print(String s){
		if(this.printOutput){
			System.out.println(s);
		}
	}
	
	

	@Override
	public Iterable<Cut<E>> findCutsLessThan(final UndirectedGraph<V, E> graph,
			final Transformer<E, Number> edgeWeights, final double value) {
		print("Testing connectivity...");
		Set<Cut<E>> connectivityCuts = ConnectivityCuts.makeCuts(graph, edgeWeights);
		if(connectivityCuts.size()>0){
			print("added " + connectivityCuts.size() + " cuts for connectivity");
			return connectivityCuts;
		}
		PriorityQueueBounded<Cut<E>> retVal = new PriorityQueueBounded<Cut<E>>(this.maxNumCuts,Collections.reverseOrder(Cut.<E>makeCutComparator()));
		int n = graph.getVertexCount();
		int numTrials = (int)(numTrialsMultiplier*Math.log(n));//Math.pow(Math.log(n),2));
		List<Callable<PriorityQueueBounded<Cut<E>>>> tasks = new ArrayList<Callable<PriorityQueueBounded<Cut<E>>>>();

		final int trialsPerThread = (int)Math.ceil(numTrials/(double)numThreads);
		
		for(int i = 0; i < numThreads; i++){
			final int threadId = i;
			final RandomData rand = randoms[i];
			tasks.add(new Callable<PriorityQueueBounded<Cut<E>>>(){

				@Override
				public PriorityQueueBounded<Cut<E>> call() throws Exception {
					PriorityQueueBounded<Cut<E>> ans = new PriorityQueueBounded<Cut<E>>(maxNumCuts,Collections.reverseOrder(Cut.<E>makeCutComparator()));
					for(int j = 0; j < trialsPerThread; j++){
						KargerSteinWeightedRandomCut<V,E> kargerCut = new KargerSteinWeightedRandomCut<V,E>(graph,edgeWeights, rand,value,ans);
						if(threadId ==0 ){
							print("Thread " + threadId + " completed " + j + " trials of  " + trialsPerThread);
						}											
					}
					return ans;
				}
				
			});
		}
		try {
			List<Future<PriorityQueueBounded<Cut<E>>>> taskVals = exec.invokeAll(tasks);
			for(Future<PriorityQueueBounded<Cut<E>>> future: taskVals){
				for(Cut<E> cut: future.get()){
					retVal.add(cut);
				}
			}
			return retVal;
			
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		

	}
	

}
