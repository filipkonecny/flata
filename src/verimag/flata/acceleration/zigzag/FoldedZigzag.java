package verimag.flata.acceleration.zigzag;

import java.util.Vector;

public class FoldedZigzag {
	private int node_size; 
	private Vector<FoldedZigzagEdge> edges;
	
	public FoldedZigzag(int size, Vector<ZigzagEdge> edgeVec) {
		node_size = size;
		edges = new Vector<FoldedZigzagEdge>();	
		
		for (int i = 0; i < edgeVec.size(); i ++) 
			addEdge(edgeVec.elementAt(i));
	}
	
	public boolean addEdge(ZigzagEdge e) {
		Vector<Integer> path = new Vector<Integer>();
		
		path.add(new Integer(e.getFrom()));
		path.add(new Integer(e.getTo()));
		
		return addEdge(e.getFrom(), e.getTo(), e.getLabel(), path);
	}
	
	public boolean addEdge(int from, int to, int label, Vector<Integer> path) {
		FoldedZigzagEdge edge = new FoldedZigzagEdge(node_size, from, to, label, path);
				
		if (!edge.validPath())
			return false;

		for (int i = 0; i < edges.size(); i ++) {
			FoldedZigzagEdge e = edges.elementAt(i);
			if (e.getPath().equals(path))
				return false;
		}
		
		edges.add(edge);
		return true;
	}
	
	public Vector<FoldedZigzagEdge> getEdges(int from, int to) {
		Vector<FoldedZigzagEdge> result = new Vector<FoldedZigzagEdge>();

		for (int i = 0; i < edges.size(); i ++) {
			FoldedZigzagEdge e = edges.elementAt(i);
			if (e.getFrom() == from && e.getTo() == to)
				result.add(e);
		}

		return result;
	}
		
	public void transitiveClosure() {
		boolean change = true;
		
		while (change) {
			change = false;
			
			for (int i = 0; i < node_size; i ++)
				for (int j = 0; j < node_size; j ++) 
					for (int k = 0; k < node_size; k ++) {
						Vector<FoldedZigzagEdge> eik = getEdges(i, k);
						Vector<FoldedZigzagEdge> ekj = getEdges(k, j);
					
						System.out.println("i=" + i + " j=" + j + " k=" + k);
					
						for (int n = 0; n < eik.size(); n ++)
							for (int m = 0; m < ekj.size(); m ++) {
								FoldedZigzagEdge e1 = eik.elementAt(n);
								FoldedZigzagEdge e2 = ekj.elementAt(m);
								
								Vector<Integer> p1 = e1.getPath();
								Vector<Integer> p2 = e2.getPath();
								Vector<Integer> p = (Vector<Integer>) p1.clone();
								
								for (int x = 1; x < p2.size(); x ++)
									p.add(p2.elementAt(x));
								
								System.out.println("adding edge " + e1 + " + " + e2);
							
								change |= addEdge(e1.getFrom(), e2.getTo(),
										  e1.getLabel() + e2.getLabel(), p);
								
								System.out.println(edges);
							}
					}
		}		
	}
	
	public int countOppositePairs() {
		int result = 0;
		
		for (int i = 0; i < node_size; i ++) 
			for (int j = i + 1; j < node_size; j ++) {
				Vector<FoldedZigzagEdge> ei = getEdges(i, i);
				Vector<FoldedZigzagEdge> ej = getEdges(j, j);
				
				for (int n = 0; n < ei.size(); n ++)
					for (int m = 0; m < ej.size(); m ++) {
						FoldedZigzagEdge e1 = ei.elementAt(n);
						FoldedZigzagEdge e2 = ej.elementAt(m);
						
						if (e1.getLabel() * e2.getLabel() < 0)
							result ++;
					}
			}
		
		return result;
	}
	
	public String toString() { return edges.toString(); }
	
	public static void main(String[] args) {
		Vector<ZigzagEdge> ev = new Vector<ZigzagEdge>();
		
		ev.add(new ZigzagEdge(0,1,-1,true));
		ev.add(new ZigzagEdge(0,0,0,true));
		ev.add(new ZigzagEdge(1,2,-2,true));
		ev.add(new ZigzagEdge(1,1,1,true));
		ev.add(new ZigzagEdge(2,0,-3,true));
		ev.add(new ZigzagEdge(2,2,2,true));
		
		FoldedZigzag z = new FoldedZigzag(3, ev);
		
		System.out.println(z);
		
		z.transitiveClosure();
		
		System.out.println(z);
		System.out.println(z.countOppositePairs());
	}
}
