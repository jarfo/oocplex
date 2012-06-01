package tspLib;

import org.apache.commons.collections15.Transformer;

public class WeightedEdgeTransformer implements Transformer<WeightedEdge,Double> {
	
	public static WeightedEdgeTransformer instance = new WeightedEdgeTransformer();
	
	private WeightedEdgeTransformer(){}

	@Override
	public Double transform(WeightedEdge arg0) {
		return Double.valueOf(arg0.getDistance());
	}

}
