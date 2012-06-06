package minCut;

import org.apache.commons.math3.distribution.AbstractIntegerDistribution;

public class DiscreteDistribution extends AbstractIntegerDistribution {
	
	private double[] probabilities;
	private double[] cummulativeProbabilities;

	@Override
	public double probability(int x) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double cumulativeProbability(int x) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNumericalMean() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getNumericalVariance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSupportLowerBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSupportUpperBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSupportConnected() {
		// TODO Auto-generated method stub
		return false;
	}

}
