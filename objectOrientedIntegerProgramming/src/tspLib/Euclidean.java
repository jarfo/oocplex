package tspLib;

public class Euclidean implements Metric {
	
	public static final Euclidean instance = new Euclidean();
	
	private Euclidean(){}

	@Override
	public int distance(GeoNode n1, GeoNode n2) {
		return (int)Math.round(Math.sqrt(Math.pow(n1.getX() - n2.getX(), 2) + Math.pow(n1.getY() - n2.getY(), 2)));
	}

}
