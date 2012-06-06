package tspIps;

/**
 * 
 * @author ross
 *
 * This is not required to be thread safe.
 */
public interface RandomInterface {
	
	/**
	 * 
	 * @param max
	 * @return An integer in [0,max) uniformly at random
	 */
	public int nextInt(int max);
	

}
