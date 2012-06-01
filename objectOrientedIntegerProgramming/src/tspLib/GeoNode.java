package tspLib;

public class GeoNode extends IntNode {
	
	private double x;
	private double y;
	public GeoNode(int value, double x, double y) {
		super(value);
		this.x = x;
		this.y = y;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	
	
	
	

}
