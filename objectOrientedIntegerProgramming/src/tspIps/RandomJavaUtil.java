package tspIps;

import java.util.Random;

public class RandomJavaUtil implements RandomInterface {
	
	private Random random;
	
	public RandomJavaUtil(){
		this(new Random());
	}
	
	public RandomJavaUtil(long seed){
		this(new Random(seed));
	}
	
	public RandomJavaUtil(Random random){
		this.random = random;
	}

	@Override
	public int nextInt(int max) {
		return this.random.nextInt(max);
	}

}
