package minCut;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;



public class WeightedEdge{
	
	private double weight;

	public WeightedEdge(double weight){
		this.weight = weight;
	}
	
	public double getWeight(){
		return this.weight;
	}
	
	public static class WeightedEdgeFactory implements Factory<WeightedEdge>{
		
		public static final WeightedEdgeFactory instance = new WeightedEdgeFactory();
		
		private WeightedEdgeFactory(){};

		@Override
		public WeightedEdge create() {
			return new WeightedEdge(0);
		}		
	}
	public static class WeightedEdgeTransformer implements Transformer<WeightedEdge,Number>{
		
		public static final WeightedEdgeTransformer instance = new WeightedEdgeTransformer(1);
		
		private double rescale;
		
		public WeightedEdgeTransformer(double rescale){
			this.rescale = rescale;
		}

		@Override
		public Double transform(WeightedEdge arg0) {
			return arg0.getWeight()*rescale;
		}
		
	}
	
	
}