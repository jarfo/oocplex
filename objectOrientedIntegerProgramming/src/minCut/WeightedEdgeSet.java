package minCut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



import org.apache.commons.collections15.Transformer;
import org.apache.commons.math3.random.RandomData;

import edu.uci.ics.jung.graph.UndirectedGraph;

public class WeightedEdgeSet<V,E>{
	
	
	
	private double currentWeight;
	private List<EdgeEntry> currentEdgeEntries;
	private UndirectedGraph<V,E> graph;
	private Transformer<E,Number> edgeWeights;
	private RandomData rand;
	
	public WeightedEdgeSet(UndirectedGraph<V,E> graph, Transformer<E,Number> edgeWeights, 
			Collection<E> initialEdges, RandomData rand){
		this.graph = graph;
		this.rand = rand;
		this.edgeWeights = edgeWeights;
		currentEdgeEntries = new ArrayList<EdgeEntry>();
		currentWeight = 0;
		for(E edge:initialEdges){
			double weight = edgeWeights.transform(edge).doubleValue();
			if(weight > 0){
				currentWeight += weight;
				this.currentEdgeEntries.add(new EdgeEntry(edge,weight));
			}
		}
	}
	
	public List<E> getRandomPermutation(){
		List<E> ans = new ArrayList<E>();
		while(currentEdgeEntries.size() > 0){
			sampleAndRemoveUsed(ans,currentEdgeEntries.size()*2);
		}
		return ans;
	}
	
	private void sampleAndRemoveUsed(List<E> edgeListToAddTo, int numSamples){
		List<EdgeEntry> unused = new ArrayList<EdgeEntry>();
		RandomWeight<E>[] randomDataByIndex = new RandomWeight[numSamples];
		
		for(int i = 0; i < numSamples; i++){
			randomDataByIndex[i] = new RandomWeight<E>(rand.nextUniform(0, currentWeight, true));
		}
		RandomWeight<E>[] randomDataByWeight = Arrays.copyOf(randomDataByIndex,numSamples);
		Arrays.sort(randomDataByWeight, RandomWeight.weightComparator);
		
		double currentWeightEdges = 0;
		double weightRemaining =0;
		int weightIndex = 0;
		for(EdgeEntry edgeEntry: currentEdgeEntries){
			currentWeightEdges += edgeEntry.getWeight();
			while(weightIndex < numSamples && currentWeightEdges > randomDataByWeight[weightIndex].getWeight()){
				edgeEntry.setUsed();
				E edge = edgeEntry.getEdge();
				if(edge == null){
					throw new RuntimeException();
				}
				randomDataByWeight[weightIndex].setValue(edge);
				weightIndex++;
			}
			if(!edgeEntry.isUsed()){
				unused.add(edgeEntry);
				weightRemaining+=edgeEntry.getWeight();
			}
		}
		boolean error = false;
		String errorMsg = "";
		for(int i = 0; i < numSamples; i++){				
			if(randomDataByIndex[i].getValue() == null){
				error = true;
				errorMsg += " "+i;
			}
		}
		if(error){
			throw new RuntimeException(errorMsg);
		}
		Set<E> edgesAdded = new HashSet<E>();
		for(int i = 0; i < numSamples; i++){
			E nextEdge = randomDataByIndex[i].getValue();
			if(nextEdge == null){
				throw new RuntimeException();
			}
			if(!edgesAdded.contains(nextEdge)){
				edgeListToAddTo.add(nextEdge);
				edgesAdded.add(nextEdge);
			}
		}
		this.currentEdgeEntries = unused;
		this.currentWeight = weightRemaining;
	}
	
	private static class RandomWeight<T>{
		private double weight;
		private T value;
		
		public RandomWeight(double weight) {
			super();
			this.weight = weight;

		}
		public double getWeight() {
			return weight;
		}

		
		public void setValue(T value){
			this.value = value;
		}
		
		public T getValue(){
			return this.value;
		}
		
		public static Comparator<RandomWeight> weightComparator = new Comparator<RandomWeight>(){
		

			@Override
			public int compare(RandomWeight arg0, RandomWeight arg1) {
				return Double.compare(arg0.getWeight(), arg1.getWeight());
			}
			
		};
		
		
		
	}
	
	private class EdgeEntry{
		E edge;
		boolean used;
		double weight;
		public EdgeEntry(E edge, double weight) {
			super();
			this.edge = edge;
			this.weight = weight;
			this.used = false;
		}
		
		public void setUsed(){
			this.used = true;
		}

		public E getEdge() {
			return edge;
		}

		public boolean isUsed() {
			return used;
		}

		public double getWeight() {
			return weight;
		}
		
		
		
		
	}
}