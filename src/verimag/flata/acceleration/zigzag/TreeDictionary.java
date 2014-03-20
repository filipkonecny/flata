package verimag.flata.acceleration.zigzag;

import java.util.Vector;

public class TreeDictionary {
	public static int count = 0;
	
	// bottom, left, right, leftright, righleft
	// -1 is undefined
	private int id; 
	
	// 0 ... node_size - 1
	// -1 is undefined
	private int level;
	
	TreeDictionary parent;
	TreeDictionary[] children;
	
	Node entry;
	
	Vector<ZigzagEdge> inEdges;
	Vector<ZigzagEdge> outEdges;
	
	public TreeDictionary() { this(-1, -1, null); }
	
	public TreeDictionary(int i, int l, TreeDictionary p) {
		id = i;
		level = l;
		
		parent = p;
		children = new TreeDictionary[5];
		
		entry = null;
		
		inEdges = new Vector<ZigzagEdge>();
		outEdges = new Vector<ZigzagEdge>();
		
		count ++;
	}
		
	public int getLevel() { return level; }
	
	private void recGetTuple(Vector<Integer> tuple) {
		if (level == -1)
			return;
		
		parent.recGetTuple(tuple);
		tuple.add(new Integer(id));
	}
	
	public Vector<Integer> getTuple() {
		Vector<Integer> tuple = new Vector<Integer>();
		recGetTuple(tuple);
		return tuple;
	}
	
	public Node findOrAdd(Vector<Integer> tuple, Zigzag z, Graph g) {
		return recFindOrAdd(tuple, 0, z, g);
	}
	
	private Node recFindOrAdd(Vector<Integer> tuple, int pos, Zigzag z, Graph g) {
		
		if (Thread.interrupted())
			throw new RuntimeException(" --- interrupted");
		
		if (level == tuple.size() - 1) {
			if (entry == null) {
				g.incNodeCount();
				entry = new Node(g.getNodeSize(), this);
			}
			return entry;
		}
		
		int next = tuple.elementAt(pos).intValue();
		TreeDictionary nextNode = children[next];
		
		if (nextNode == null) {
			nextNode = new TreeDictionary(next, pos, this);
			nextNode.setOutEdges(z);
			children[next] = nextNode;
		}
		
		return nextNode.recFindOrAdd(tuple, pos + 1, z, g);
	}
	
	public void setOutEdges(Zigzag z) {
		if (inEdges.size() > 0 || outEdges.size() > 0)
			return;
		
		Vector<ZigzagEdge> edges = z.getEdges();
				
		for (int i = 0; i < edges.size(); i ++) {
			ZigzagEdge e = edges.elementAt(i);

			if (e.isForward() && e.getFrom() == level) {
				if (id == Node.right || id == Node.leftright)
					outEdges.add(e);
			} else if (e.isBackward() && e.getTo() == level) {
				if (id == Node.left || id == Node.leftright)
					inEdges.add(e);
			}
		}
	}
	
	public Vector<ZigzagEdge> getInEdges() { return inEdges; }
	public Vector<ZigzagEdge> getOutEdges() { return outEdges; }
	
	private boolean compatibleEdge(Vector<ZigzagEdge> set, ZigzagEdge el) {
		for (int i = 0; i < set.size(); i ++) {
			ZigzagEdge edge = set.elementAt(i);
			if (!((edge.isForward() ^ el.isForward()) ||
				(edge.getFrom() != el.getFrom() && edge.getTo() != el.getTo())))
				return false;
		}		
		return true;
	}
	
	private void append(Vector<ZigzagEdge> set, ZigzagEdge el) {		
		set.add(el);
	}
		
	private void remove(Vector<ZigzagEdge> set) {
		set.removeElementAt(set.size() - 1);
	}
	
	private void recOutZigzags(int size, Vector<ZigzagEdge> currentSet, 
								Vector<Zigzag> result, Graph graph) {		
		if (level == -1) {
			// sanity check
			if (!currentSet.isEmpty())
				result.add(new Zigzag(size, currentSet));
			return;
		}
		
		switch (id) {
		case Node.bottom : 
		case Node.rightleft :
			parent.recOutZigzags(size, currentSet, result, graph);
			break;
			
		case Node.left :
			for (int i = 0; i < inEdges.size(); i ++) {
				ZigzagEdge e = inEdges.elementAt(i);	
				
				if (graph.reached(e.getFrom()) && compatibleEdge(currentSet, e)) {
					append(currentSet, inEdges.elementAt(i));
					parent.recOutZigzags(size, currentSet, result, graph);
					remove(currentSet);
				}
			}
			
			break;
			
		case Node.right :
			for (int i = 0; i < outEdges.size(); i ++) {
				ZigzagEdge e = outEdges.elementAt(i);
				
				if (graph.reached(e.getTo()) && compatibleEdge(currentSet, e))	{
					append(currentSet, outEdges.elementAt(i));
					parent.recOutZigzags(size, currentSet, result, graph);
					remove(currentSet);
				}
			}
			
			break;
			
		case Node.leftright :
			for (int i = 0; i < inEdges.size(); i ++) {
				ZigzagEdge in_e = inEdges.elementAt(i);
				
				if (graph.reached(in_e.getFrom()) && compatibleEdge(currentSet, in_e)) {
					for (int j = 0; j < outEdges.size(); j ++) {
						ZigzagEdge out_e = outEdges.elementAt(j);
						
						if (graph.reached(out_e.getTo()) && compatibleEdge(currentSet, out_e)) {
							append(currentSet, inEdges.elementAt(i));
							append(currentSet, outEdges.elementAt(j));
							parent.recOutZigzags(size, currentSet, result, graph);
							remove(currentSet);
							remove(currentSet);
						}
					}
				}
			}

			break;
		}
	}
	
	public Vector<Zigzag> outZigzags(Graph graph) {
		Vector<Zigzag> result = new Vector<Zigzag>();
		recOutZigzags(entry.getSize(), new Vector<ZigzagEdge>(), result, graph);
		return result;
	}
}

