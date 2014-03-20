package verimag.flata.acceleration.zigzag;

import java.util.*;

public class SCCNode {
	private Vector<Node> nodes;
		
	private Vector<Node> entries;
	private Vector<Node> exits;
	
	private int id;
	
	static final double PRECISION = 0.0001;
	
	public SCCNode(Vector<Node> scc) {
		nodes = scc;	
		
		entries = new Vector<Node>();
		exits = new Vector<Node>();
	}
	
	public int size() { return nodes.size(); }
	
	public void initialize(int n) {
		id = n;
		for (int i = 0; i < nodes.size(); i ++) {
			Node node = nodes.elementAt(i);		
			node.setSCCNode(this);	
		}
	}
	
	public void initExits() {
		for (int i = 0; i < nodes.size(); i ++)
			nodes.elementAt(i).setSCCExit();			
	}
		
	public void addNode(Node node) { nodes.add(node); } 
	
	public Vector<Node> getNodes() { return nodes; }
	
	public boolean contains(Node n) { return nodes.contains(n); } 
		
	public void addEntry(Node node) { 
		if ( !entries.contains(node) )
			entries.add(node);
	}
		
	public Vector<Node> getEntries() { return entries; }
	
	public void addExit(Node node) {
		if ( !exits.contains(node) )
			exits.add(node); 			
	}
	
	public Vector<Node> getExits() { return exits; }
		
	public int getId() { return id; }
	
	public void setId(int i) { id = i; }
			
	public boolean isEqualTo(SCCNode n) {
		for (int i = 0; i < nodes.size(); i ++)
			if ( !n.contains(nodes.elementAt(i)) )
				return false;
		
		for (int i = 0; i < n.getNodes().size(); i ++)
			if ( !contains(n.getNodes().elementAt(i)) )
				return false;
		
		return true;
	}
		
	private void addSummaryEdge(Node src, Node dest, SLSet X) {
		SCCSummaryEdge e = new SCCSummaryEdge(src, dest, X.copy());
		src.addOutSummaryEdge(e);
		dest.addInSummaryEdge(e);
	}
		
	private boolean propagateSummary(Node entry) {
		boolean summarized = false;

		for (int i = 0; i < entry.outSummaryEdges().size(); i ++) {
			SCCSummaryEdge e = entry.outSummaryEdges().elementAt(i);
			
			if (Graph.DEBUG >= Graph.HIGH_DEBUG)
				System.out.println("propagate summary " + e);
			
			e.getExit().m_X.union( e.getExitSet() );			
			summarized = true;
		}					

		return summarized;
	}
	
	private void addSummary(Node entry) {
		for (int i = 0; i < nodes.size(); i ++) {
			Node node = nodes.elementAt(i);
			addSummaryEdge(entry, node, node.m_X);
			
			if (Graph.DEBUG >= Graph.HIGH_DEBUG)
				System.out.println("add summary edge " + entry + " ==" + node.m_X + "==> " + node);
			
			node.restoreSet();
		}
		
		propagateSummary(entry);
	}
	
	public void iterate(Node entry) {			
						
		if (Graph.DEBUG >= Graph.MEDIUM_DEBUG) {
			System.out.println("iterating: " + id + " size=" + nodes.size() + 
								" entry=" + entry + " " + entry.m_X + " ... ");
		}
		
		// initialize entry node
		
		entry.joinInterPredecessors();
		
		// if the entry has at least one outgoing summary edge, 
		// then it has been summarized, in which case propagate 
		// the summary and return
		
		if (propagateSummary(entry)) {
			if (Graph.DEBUG >= Graph.MEDIUM_DEBUG) {
				System.out.println("summarized");
			}			
			return;
		}
		
		for (int i = 0; i < nodes.size(); i ++) {
			Node node = nodes.elementAt(i);
			node.saveSet(node == entry);
		}	
						
		// initialize worklist
		LinkedList<Node> workList = new LinkedList<Node>();	
		
		for (int i = 0; i < entry.outEdges().size(); i ++) {
			Node d = entry.outEdges().elementAt(i).getDestination();
			
			if ( !d.is(Node.EVALUATE) && contains(d) ) {
				d.setFlag(Node.EVALUATE);
				workList.addLast(d);
			}
		}
		
		int iter_no = 0;
		
		// main loop
		while ( !workList.isEmpty() ) {
			Node node = workList.removeFirst();
			
			node.resetFlag(Node.EVALUATE);
			
			iter_no ++;
			
			if ( evaluate(entry, node) ) {
				for (int i = 0; i < node.outEdges().size(); i ++) {
					Node d = node.outEdges().elementAt(i).getDestination();
					
					if ( !d.is(Node.EVALUATE) && contains(d) ) {
						d.setFlag(Node.EVALUATE);
						workList.addLast(d);
					}
				}
			}
		}

		addSummary(entry);
				
		if (Graph.DEBUG >= Graph.MEDIUM_DEBUG)
			System.out.println(iter_no + " iterations");
	}

	private boolean evaluate(Node entry, Node node) {
		
		if (Graph.DEBUG >= Graph.HIGH_DEBUG)
			System.out.println("evaluate " + node + " " + node.m_X + " ... ");
		
		// boolean changed = false;
		
		SLSet X = node.m_X.copy();
		// Vector<Node> summaryEntries = new Vector<Node>();
		
		// System.out.println("join intra ...");
		
		// node.joinIntraPredecessors(summaryEntries);
		node.joinIntraPredecessors();
		
		// System.out.println("add critical ...");
		
		node.addCriticalCycle();
		
		/*
		if ( !node.m_X.equals(X) ) {
			changed = true;
		} else if ( !summaryEntries.contains(entry) ) {
			
			System.out.println("add summary edge " + entry + " ==" + node.m_X + "==> " + node);
			
			addSummaryEdge(entry, node, node.m_X);
		}
		*/
		
		if (Graph.DEBUG >= Graph.HIGH_DEBUG)
			System.out.println(" --> " + node.m_X);
		
		// return changed;
		
		return !node.m_X.equals(X); 
	}
	
	private boolean acyclic() {		
		if (nodes.size() == 1) {
			Node n = nodes.elementAt(0);
			
			for (int k = 0; k < n.outEdges().size(); k ++) {
				if ( n.outEdges().elementAt(k).getDestination() == n )
					return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	public void howard() {		
		if (acyclic())
			return;
			
		mark(Node.WORKED); 
		unmark(Node.VISITED);
		
		initializePolicy();
		
		boolean improved = true;
		while (improved) {
			
			valueDetermination();
			unmark(Node.VISITED);	
			improved = policyImprovement();
		}
		
		markCritical();	
		unmark(Node.WORKED);
	}
	
	private void mark(int flag) {
		for (int i = 0; i < nodes.size(); i ++) {
			Node n = nodes.elementAt(i);
			n.setFlag(flag);
		}
	}

	private void unmark(int flag) {
		for (int i = 0; i < nodes.size(); i ++) {
			Node n = nodes.elementAt(i);
			n.resetFlag(flag);
		}
	}
	
	private void initializePolicy() {
		for (int i = 0; i < nodes.size(); i ++) {
			Node n = nodes.elementAt(i);
			n.setPolicy(null);
			
			for (int j = 0; j < n.outEdges().size(); j ++) {
				Edge oe = n.outEdges().elementAt(j);
				Node tn = oe.getDestination();
				
				if ( tn.is(Node.WORKED) ) {
					int weight_of_edge = oe.getWeight();
					if (n.getPolicy() == null || n.getPolicy().getWeight() > weight_of_edge)
						n.setPolicy(oe);
				}
			}
		}
	}

	private void valueDetermination() {
		for (int i = 0; i < nodes.size(); i ++) {
			Node n =  nodes.elementAt(i);
			
			if ( !n.is(Node.VISITED) ) {
				Node root = computePolicyCycle(n);
				double h = computeCycleMean(root);
				computeDistances(root, h);
			}
		}
	}
	
	private Node computePolicyCycle(Node source_node) {
		Node current_node = source_node;
		
		while ( !current_node.is(Node.VISITED) ) {
			current_node.setFlag(Node.VISITED);
			current_node = current_node.getPolicy().getDestination();
		}
		
		return current_node;
	}

	private double computeCycleMean(Node root) {
		int weight = root.getPolicy().getWeight();
		int length = root.getPolicy().getLength();
		Node current = root.getPolicy().getDestination();
		
		while (current != root) {
			weight += current.getPolicy().getWeight();
			length += current.getPolicy().getLength();
			current = current.getPolicy().getDestination();
		}
		
		return ((double) weight) / ((double) length);
	}

	private void computeDistances(Node root, double h) {
		HashMap<Node, Node> reached = new HashMap<Node, Node>();
		LinkedList<Node> workList = new LinkedList<Node>();
		
		root.m_distance = 0.0;
		root.m_htta = h;
		root.setFlag(Node.VISITED);
		reached.put(root, root);
		
		Vector<Node> rootSources = root.getPolicySources();
		
		for (int i = 0; i < rootSources.size(); i ++) {
			Node n = rootSources.elementAt(i);
			
			if (n.is(Node.WORKED) && !reached.containsKey(n))
				workList.addLast(n);
		}
				
		while ( !workList.isEmpty() ) {
			Node target = workList.removeFirst();
						
			target.m_distance = target.getPolicy().getWeight() - h 
								+ target.getPolicy().getDestination().distance();
			
			target.m_htta = h;
			target.setFlag(Node.VISITED);
			reached.put(target, target);
			
			Vector<Node> targetSources = target.getPolicySources();
			for (int i = 0; i < targetSources.size(); i ++) {
				Node n = targetSources.elementAt(i);
				
				if (n.is(Node.WORKED) && !reached.containsKey(n))
					workList.addLast(n);
			}
		}
	}
	
	private boolean policyImprovement() {
		boolean improved = false;
		
		// first step, eqn 17
		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.elementAt(i);
			Edge min = null;
			
			for (int j = 0; j < n.outEdges().size(); j++) {
				Edge e = n.outEdges().get(j);
				if (e.getDestination().is(Node.WORKED)
					&& (min == null || min.getDestination().htta() > e.getDestination().htta()))
					min = e;
			}
			
			if (n.htta() > min.getDestination().htta() + PRECISION) {
				n.setPolicy(min);
				improved = true;
			}
		}
				
		if (improved)
			return improved;

		// second step, eqn 18
		for (int i = 0; i < nodes.size(); i ++) {
			Node n = nodes.elementAt(i);
			Edge min = null;
			
			for (int j = 0; j < n.outEdges().size(); j ++) {
				Edge e = n.outEdges().get(j);

				if (e.getDestination().is(Node.WORKED)
						&& (min == null || min.getWeight() - min.getDestination().htta()
								+ min.getDestination().distance() > e.getWeight()
								- e.getDestination().htta() + e.getDestination().distance()))
					min = e;
			}
			
			if (min.getWeight()-min.getDestination().htta()+min.getDestination().distance()+PRECISION < 
					n.distance()) {				
				n.setPolicy(min);
				improved = true;
			}
		}

		return improved;
	}
	
	private void markCritical() {		
		// mark critical nodes and critical edges
		for (int i = 0; i < nodes.size(); i ++) {
			Node n1 = nodes.elementAt(i);
			Vector<Edge> edges = n1.outEdges();
			
			for (int j = 0; j < edges.size(); j ++) {
				Edge e = edges.get(j);
				Node n2 = e.getDestination();
			
				if ( !n2.is(Node.WORKED) )
					continue;
				
				double d = n1.distance() - n2.distance() - e.getWeight() + n1.htta();
				
				if (-PRECISION < d && d < PRECISION) {
					n1.setFlag(Node.CRITICAL);
					n2.setFlag(Node.CRITICAL);
					e.setFlag(Edge.CRITICAL);
				}
			}
		}
		
		// find minimal cycles through critical nodes and edges
		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.elementAt(i);
			
			if ( n.is(Node.CRITICAL) ) 
				findCriticalCycle(n);
		}
	}

	private void findCriticalCycle(Node root) {
        HashMap<Node, Point> reached = new HashMap<Node, Point>();
		LinkedList<Node> pending = new LinkedList<Node>();

		// initialize
		pending.addLast(root);
		reached.put(root, new Point(0, 0));
		
		// find shortest cycle using BFS traversal
		while ( !pending.isEmpty() ) {
		      Node top = pending.removeFirst();
		      Point p_top = reached.get(top);
	      
		      // go through good successors
              for(int i = 0; i < top.outEdges().size(); i++) {
            	  Edge e = top.outEdges().get(i);
            	  Node succ = e.getDestination();
            	  
            	  if ( !succ.is(Node.WORKED) || !e.is(Edge.CRITICAL) || !succ.is(Node.CRITICAL) )
            		  continue;
            	  
            	  if ( succ == root ) {
            		  // here we find the cycle
            		  succ.m_cycle = new Point( p_top.getLength() + e.getLength(), 
            				  					p_top.getWeight() + e.getWeight() );
            		  break;
            	  }

            	  if ( !reached.containsKey(succ) ) {
            		  reached.put( succ, new Point( p_top.getLength() + e.getLength(), 
            				  						p_top.getWeight() + e.getWeight()) );
            		  pending.addLast( succ );
            	  }
              }
		}
	}
	
	public String toString() {
		String s = "scc " + id + " {";
		for (int i = 0; i < nodes.size(); i ++) {
			Node node = nodes.elementAt(i);
			
			if (entries.contains(node))
				s += ">";
			
			s += node; 
			
			if (exits.contains(node))
				s += ">";
		}
		
		s += "}" ;
		return s;
	}
}
