package verimag.flata.acceleration.zigzag;

import java.util.Vector;

public class FoldedZigzagEdge {
	private int node_size; 
	private int from;
	private int to;
	private int label;
	
	private Vector<Integer> path;
	
	public FoldedZigzagEdge(int n, int f, int t, int l, Vector<Integer> p) {
		node_size = n;
		from = f;
		to = t;
		label = l;
		path = p;
	}
	
	public int getFrom() { return from; }
	
	public int getTo() { return to; }
	
	public int getLabel() { return label; }
	
	public Vector<Integer> getPath() { return path; }
	
	public boolean validPath() {
		int[] visited = new int[node_size];
		
		for (int i = 0; i < node_size; i ++)
			visited[i] = 0;
		
		for (int i = 0; i < path.size(); i ++) {
			if (i > 0 && i < path.size() - 1 && 
				path.elementAt(i).intValue() == from)
				return false;
				
			visited[path.elementAt(i).intValue()] ++;
		}
		
		for (int i = 0; i < node_size; i ++) {
			if (i != from && visited[i] > 1)
				return false;
		}
		
		return true;
	}
	
	public String toString() { return from + "-(" + label + ")->" + to + " " + path; }
}
