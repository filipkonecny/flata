package verimag.flata.acceleration.zigzag; 

import java.util.*;

public class Node {
	public static int count = 0;
	
	private TreeDictionary leaf;
	
	// tuple elements
	public static final int bottom = 0;
	public static final int right = 1;
	public static final int left = 2;
	public static final int rightleft = 3;
	public static final int leftright = 4;
	
	// used flags
	public static final int STACKED = 1;
	public static final int REACHED = 2;
	public static final int WORKED = 4;
	public static final int VISITED = 8;
	public static final int CRITICAL = 16;
	public static final int EVALUATE = 32;
	public static final int SCC_EVALUATE = 64;
	
	protected int node_size;	
	
	private boolean visited;
	
	private Vector<Edge> m_inEdges;
	private Vector<Edge> m_outEdges;

	protected SCCNode scc_node;

	private Vector<SCCSummaryEdge> s_inEdges;
	private Vector<SCCSummaryEdge> s_outEdges;
	
	// private Vector<OldSLSet> m_lengthWeight; // length and weight of a path

	public int m_mark;

	public int m_dfs;

	public int m_lowlink;

	private Edge m_policy;
	private Vector<Node> s_policy;
	
	public double m_htta;

	public double m_distance;

	public Point m_cycle;

	public SLSet m_X;
	private SLSet copy_X;
	
	public Node(int size) {		
		node_size = size;
		
		visited = false;
		
		m_inEdges = new Vector<Edge>();
		m_outEdges = new Vector<Edge>();
		
		s_inEdges = new Vector<SCCSummaryEdge>();
		s_outEdges = new Vector<SCCSummaryEdge>();
		
		s_policy = new Vector<Node>();
		
		m_X = new GSLSet();
		
		count ++;
	}
	
	public Node(int size, TreeDictionary l) {
		this(size);
		leaf = l;
	}
	
	public void saveSet(boolean init) {
		
		if (Graph.DEBUG >= Graph.HIGH_DEBUG)
			System.out.println("saving " + this + " " + m_X + " ...");
			
		copy_X = m_X.copy();
		m_X.clear();
		
		if (init)
			m_X.addPoint(new Point(0,0));
	}
	
	public void restoreSet() {
		m_X = copy_X;
		copy_X = null;
	}
	
	public void setSCCEntry() {
		boolean added = false;
		
		for (int i = 0; i < m_inEdges.size(); i ++) {
			Node src = m_inEdges.elementAt(i).getSource();
			
			if ( !scc_node.contains(src) ) {
				src.getSCCNode().addExit(src);
				
				if (!added) {
					scc_node.addEntry(this);				
					added = true;
				}
			}
		}
	}
	
	public void setSCCExit() {
		for (int i = 0; i < m_outEdges.size(); i ++) {
			Node dest = m_outEdges.elementAt(i).getDestination();
	
			if ( !scc_node.contains(dest) ) {
				dest.getSCCNode().addEntry(dest);
				scc_node.addExit(this);
			}		
		}
	}
	
	public void addInSummaryEdge(SCCSummaryEdge e) { s_inEdges.add(e); }
	
	public void addOutSummaryEdge(SCCSummaryEdge e) { s_outEdges.add(e); }
	
	public Vector<SCCSummaryEdge> inSummaryEdges() { return s_inEdges; } 
	
	public Vector<SCCSummaryEdge> outSummaryEdges() { return s_outEdges; }
	
	public boolean isVisited() { return visited; }
	
	public void setVisited() { visited = true; }
		
	/*
	 * DFA-style join operation on semilinear sets -- intra SCC 
	 */
	public void joinIntraPredecessors() {
		Vector<Node> summaryEntries = new Vector<Node>();
		boolean[] summarized = new boolean [ s_inEdges.size() ];
		
		for (int i = 0; i < s_inEdges.size(); i ++)
			summaryEntries.add( s_inEdges.elementAt(i).getEntry() );
		
		for (int i = 0; i < m_inEdges.size(); i ++) {
			Edge e = m_inEdges.elementAt(i);
			Node s = e.getSource();		
			
			if (s.getSCCNode() != null && 
				scc_node.getId() == s.getSCCNode().getId()) {
				
				int index = summaryEntries.indexOf(s);
			
				if (index > -1) {
					if ( !summarized[index] ) {
						
						if (Graph.DEBUG >= Graph.HIGH_DEBUG)
							System.out.println("join intra " + 
												s_inEdges.elementAt(index) +   
												" (summarized)");						
						
						m_X.union( s_inEdges.elementAt(index).getExitSet() );
						summarized[index] = true;
					}
				} else {
					SLSet S = s.m_X.copy();
					
					if (Graph.DEBUG >= Graph.HIGH_DEBUG)
						System.out.println("join intra " + e);
					
					S.translate( new Point(e.getLength(), e.getWeight()) );
					m_X.union(S);
				}
			}
		}
		
		for (int i = 0; i < summaryEntries.size(); i ++) {
			if ( !summarized[i] ) {
	
				if (Graph.DEBUG >= Graph.HIGH_DEBUG)
					System.out.println("join intra " + s_inEdges.elementAt(i) + " (summarized)");						
				
				m_X.union( s_inEdges.elementAt(i).getExitSet() );
			}
		}
	}
	
	/*
	 * DFA-style join operation on semilinear sets -- inter SCC
	 */
	public void joinInterPredecessors() {
		for (int i = 0; i < m_inEdges.size(); i ++) {
			Edge e = m_inEdges.elementAt(i);
			Node s = e.getSource();
			
			if (s.getSCCNode() != null && 
				scc_node.getId() != s.getSCCNode().getId()) {
				
				if (Graph.DEBUG >= Graph.HIGH_DEBUG)
					System.out.println("join inter " + e);
				
				SLSet S = s.m_X.copy();
				S.translate( new Point(e.getLength(), e.getWeight()) );
				m_X.union(S);						
			}
		}
	}
	
	public void addCriticalCycle() {
		if ( is( Node.CRITICAL ) && m_cycle != null )
			m_X.addGenerator( m_cycle );
	}
		
	/*
	 * Returns the set of subsets of the given zigzag 
	 * that are out-compatible with the node
	 */
	public Vector<Zigzag> getOutZigzags(Graph graph) { return leaf.outZigzags(graph); }
			
	public Vector<Integer> getTuple() { return leaf.getTuple(); } 
	
	public int getSize() { return node_size; }
	
	public void addOutEdge(Edge edge) { m_outEdges.add(edge); }
	
	public void removeOutEdge(Edge edge) { m_outEdges.remove(edge); }
	
	public void addInEdge(Edge edge) { m_inEdges.add(edge); }

	public void removeInEdge(Edge edge) { m_inEdges.remove(edge); }
	
	public Vector<Edge> outEdges() { return m_outEdges; }

	public Vector<Edge> inEdges() { return m_inEdges; }

	public void setSCCNode(SCCNode node) { scc_node = node; }
	
	public SCCNode getSCCNode() { return scc_node; }
	
	public int dfs() { return m_dfs; }

	public int lowlink() { return m_lowlink; }

	public void setFlag(int flag) { m_mark = m_mark | flag; }

	public void resetFlag(int flag) { m_mark = m_mark & (~flag); }

	public boolean is(int flag) { return (m_mark & flag) != 0; }

	public void setPolicy(Edge e) { 
		if (m_policy != null)
			m_policy.getDestination().remPolicySource(this);
		
		m_policy = e;
		
		if (e != null)
			e.getDestination().addPolicySource(this);
	}
	
	public Edge getPolicy() { return m_policy; }

	public void addPolicySource(Node n) { s_policy.add(n); }
	
	public void remPolicySource(Node n) { s_policy.remove(n); }
	
	public Vector<Node> getPolicySources() { return s_policy; }
	
	public double htta() { return m_htta; }

	public double distance() { return m_distance; }

	public Point cycle() { return m_cycle; }

	public String toString() {
		String s = "[";
		String[] t = {"bot", "r", "l", "rl", "lr"};
		int size = getSize();
		Vector<Integer> tuple = getTuple();
		
		for (int i = 0; i < size; i ++) {
			s += t[tuple.elementAt(i).intValue()];
			if (i < size - 1)
				s += ",";
		}
		
		s += "]";
		return s;
	}
}
