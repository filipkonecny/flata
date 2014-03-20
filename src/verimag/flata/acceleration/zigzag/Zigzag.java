package verimag.flata.acceleration.zigzag;

import java.util.*;

public class Zigzag {
	public static int count = 0;
	
	public int node_size;

	public int weight;
		
	private Vector<ZigzagEdge> edges;
	
	private Vector<Integer> dest[]; 
	
	public Zigzag(int size) {
		node_size = size;
		weight = 0;
		edges = new Vector<ZigzagEdge>();		
		dest = (Vector<Integer>[]) new Vector[node_size];		
		count ++;
	}
	
	public Zigzag(int node_size, ZigzagMatrix forw, ZigzagMatrix back) {
		this(node_size);
		
		initEdges(forw, back);
		initialize();
		
		count ++;
	}
	
	public Zigzag(int node_size, Vector<ZigzagEdge> edgeVect) {
		this(node_size);
		
		edges = new Vector<ZigzagEdge>(edgeVect);
		
		for (int i = 0; i < edgeVect.size(); i ++) {
			ZigzagEdge e = edgeVect.elementAt(i);
			weight += e.getLabel();
		}
		
		initialize();
	}
	
	private void initEdges(ZigzagMatrix forw, ZigzagMatrix back) {
		for (int i = 0; i < node_size; i ++)
			for (int j = 0; j < node_size; j ++) {
				addForward(forw, i, j);
				addBackward(back, i, j);
			}
	}
			
	public void addForward(ZigzagMatrix m, int row, int col) {
		String e = m.eles(row, col);
		
		if (!e.equals("inf")) {
			int v = new Integer(e).intValue();			
			weight += v;			
			edges.addElement(new ZigzagEdge(row, col, v, true));
		}
	}
	
	public void addBackward(ZigzagMatrix m, int row, int col) {
		String e = m.eles(row, col);
		
		if (!e.equals("inf")) {
			int v = new Integer(e).intValue();
			weight += v; 
			edges.addElement(new ZigzagEdge(row, col, v, false));
		}
	}
	
	public Vector<ZigzagEdge> getEdges() { return edges; }
	
	public int getNodeSize() { return node_size; }
		
	private void initialize() {			
		int u_in[] = new int[node_size];
		int p_in[] = new int[node_size];
		int u_out[] = new int[node_size];
		int p_out[] = new int[node_size];
		
		for (int i = 0; i < node_size; i ++)
			dest[i] = new Vector<Integer>();
		
		for (int i = 0; i < edges.size(); i ++) {
			ZigzagEdge e = edges.elementAt(i);
			int from = e.getFrom();
			int to = e.getTo();
			
			if (e.isForward()) {
				u_out[from] ++;
				p_in[to] ++;
			} else {
				u_in[to] ++;
				p_out[from] ++;
			}
		}
		
		for (int i = 0; i < node_size; i ++) {		
			if (p_in[i] > 0 && p_out[i] > 0) 
				dest[i].add(new Integer(Node.rightleft));
			else if (p_in[i] > 0 && p_out[i] == 0)
				dest[i].add(new Integer(Node.right));
			else if (p_in[i] == 0 && p_out[i] > 0)
				dest[i].add(new Integer(Node.left));
			else {
				dest[i].add(new Integer(Node.bottom));
				dest[i].add(new Integer(Node.leftright));
			}
		}
	}		
			
	private void append(Integer i, Vector<Integer> s) {
		s.add(i);
	}
	
	private void remove(Vector<Integer> s) {
		s.removeElementAt(s.size() - 1);
	}
	
	private void recGetDestinations(int level, Vector<Integer> currentSet, 
									Vector<Node> result, Graph graph) {
				
		if (level == node_size) {
			Node n = graph.findOrAdd(currentSet); 
			result.add(n);
			return;
		}
				
		for (int i = 0; i < dest[level].size(); i ++) {
			switch (dest[level].elementAt(i).intValue()) {
			case Node.bottom : 
				append(dest[level].elementAt(i), currentSet);
				recGetDestinations(level + 1, currentSet, result, graph);
				remove(currentSet);
				break;

			default : 
				if ( graph.reached(level) ) {
					append(dest[level].elementAt(i), currentSet);
					recGetDestinations(level + 1, currentSet, result, graph);
					remove(currentSet);
				}
			}
		}
	}
	
	/*
	 * Returns the set of nodes that are 
	 * out-compatible with this zigzag. 
	 */
	public Vector<Node> getSuccessorNodes(Graph graph) {
		Vector<Node> result = new Vector<Node>();
		recGetDestinations(0, new Vector<Integer>(), result, graph);		
		return result;
	}
	
	/*
	 * Returns the number of pairs of variables
	 * that may be dependent in the transitive
	 * closure of the zigzag -- the smaller the better
	 */
	public int accelerationFactor() {
		FoldedZigzag fz = new FoldedZigzag(node_size, edges);		
		fz.transitiveClosure();
		return fz.countOppositePairs();
	}
	
	public String toString() {
		String s = "{ ";
		for (int i = 0; i < edges.size(); i ++) {
			s += edges.elementAt(i).toString();
			if (i < edges.size() - 1)
				s += ", ";
		}
		s += " <" + weight + "> }";
		return s;
	}
}
