package minCut;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;



import edu.uci.ics.jung.graph.UndirectedGraph;

public class KargerMinCut<V,E> implements MinCutSolver<V, E> {
	
	public KargerMinCut(ExecutorService exec, int numThreads, double numTrialsMultiplier, 
			boolean printOutput, boolean minOnly, RandomData[] randoms){
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
	
	private static RandomData[] defaultRandoms(int numThreads){
		RandomData[] ans = new RandomData[numThreads];
		for(int i = 0; i < numThreads; i++){
			ans[i] = new RandomDataImpl();
		}
		return ans;
	}
	
	
	private ExecutorService exec;
	private double numTrialsMultiplier;
	private int numThreads;
	private boolean printOutput;
	private boolean minOnly;
	private RandomData[] randoms;

	@Override
	public List<Cut<E>> findCutsLessThan(final UndirectedGraph<V, E> graph,
			final Transformer<E, Number> edgeWeights, final double value) {
		List<Cut<E>> retVal = new ArrayList<Cut<E>>();
		int n = graph.getVertexCount();
		int numTrials = (int)(numTrialsMultiplier*n*Math.log(n));//(int)(numTrialsMultiplier*n*n*Math.log(n));
		List<Callable<List<Cut<E>>>> tasks = new ArrayList<Callable<List<Cut<E>>>>();

		final int trialsPerThread = (int)Math.ceil(numTrials/(double)numThreads);
		
		for(int i = 0; i < numThreads; i++){
			final RandomData rand = randoms[i];
			tasks.add(new Callable<List<Cut<E>>>(){

				@Override
				public List<Cut<E>> call() throws Exception {
					List<Cut<E>> ans = new ArrayList<Cut<E>>();
					for(int j = 0; j < trialsPerThread; j++){
						KargerWeightedRandomCut<V,E> kargerCut = new KargerWeightedRandomCut<V,E>(graph,edgeWeights, rand);
						if(j %50 == 0){
							System.err.println("Completed " + (j+1) + " trials of  " + trialsPerThread);
						}
						Set<E> cut = kargerCut.getCut();
						double cutValue = 0;
						for(E e: cut){
							cutValue += edgeWeights.transform(e).doubleValue();
						}
						if(cutValue < value){
							ans.add(new Cut<E>(cut,cutValue));
						}
					}
					return ans;
				}
				
			});
		}
		try {
			List<Future<List<Cut<E>>>> taskVals = exec.invokeAll(tasks);
			Set<Cut<E>> cuts = new HashSet<Cut<E>>();
			for(Future<List<Cut<E>>> future: taskVals){
				cuts.addAll(future.get());
			}
			for(Cut<E> cut: cuts){
				retVal.add(cut);
			}
			return retVal;
			
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		

	}
	

}
