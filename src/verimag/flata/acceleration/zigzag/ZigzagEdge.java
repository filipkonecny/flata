package verimag.flata.acceleration.zigzag;

public class ZigzagEdge {
	public static int count = 0;
	
	private int from;
	private int to;
	private int label;
	private boolean dir;
	
	public ZigzagEdge(int f, int t, int l, boolean d) {
		from = f;
		to = t;
		label = l;
		dir = d;
		
		count ++;
	}
	
	public int getFrom() { return from; }
	
	public int getTo() { return to; }
	
	public int getLabel() { return label; }
	
	public boolean isForward() { return dir; }
	
	public boolean isBackward() { return !dir; }
	
	public String toString() { 
		return dir ? from + "-(" + label + ")->" + to : to + "<-(" + label + ")-" + from; 
	}
}
