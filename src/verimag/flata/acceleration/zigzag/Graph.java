package verimag.flata.acceleration.zigzag;

import java.util.*;

public class Graph {
	private int node_size;
	private int node_count;
	private int edge_count;
	
	private TreeDictionary nodeDict;
	private Vector<Node> nodeList;
	
	private SCCGraph scc_graph;
	
	public RootNode[] oddForwStart, oddForwEnd;	
	private boolean[] oddForwDone;
	
	public RootNode[] oddBackStart, oddBackEnd;
	private boolean[] oddBackDone;
	
	public RootNode[][] evenForwStart, evenBackEnd;
	private boolean[][] evenForwDone;
	
	public RootNode evenForwEnd, evenBackStart;
	private boolean[][] evenBackDone;
	
	private Zigzag zigzag; 
	private Stack<Node> stack;

	private boolean[][] reachable;
	private boolean[] minimals;
	private boolean[] maximals;
	private boolean[] reached;
	
	public static final int LOW_DEBUG = 1;
	public static final int MEDIUM_DEBUG = 2;
	public static final int HIGH_DEBUG = 3;
	public static final int EXTREME_DEBUG = 4;
	
	//public static int DEBUG = 0;
	public static int DEBUG = MEDIUM_DEBUG;
	
	public Graph(int size, ZigzagMatrix forw, ZigzagMatrix back) {
		node_size = size;
		node_count = 0;
		edge_count = 0;
		
		nodeDict = new TreeDictionary();
		nodeList = new Vector<Node>();
		stack = new Stack<Node>();
		
		scc_graph = new SCCGraph();
		
		zigzag = new Zigzag(size, forw, back);
		
		reachable = new boolean[node_size][node_size];
		minimals = new boolean[node_size];
		maximals = new boolean[node_size];
		reached = new boolean[node_size];
		
		initReachable(zigzag);
	}
	
	private boolean subsetReach(int i, int j) {
		for (int k = 0; k < node_size; k ++) {
			if (reachable[i][k] && !reachable[j][k])
				return false;
		}
		
		return true;
	}
	
	private void initReachable(Zigzag z) {
		Vector<ZigzagEdge> edges = z.getEdges();
	
		// initialize the reachability table
		for (int i = 0; i < node_size; i ++)
			reachable[i][i] = true;
	
		for (int i = 0; i < edges.size(); i ++) {
			ZigzagEdge e = edges.elementAt(i);
			reachable[e.getFrom()][e.getTo()] = true;
		}
		
		// compute transitive closure
		for (int k = 0; k < node_size; k ++)
			for (int i = 0; i < node_size; i ++)
				for (int j = 0; j < node_size; j ++)
					reachable[i][j] = reachable[i][j] | (reachable[i][k] & reachable[k][j]);
		
		// mark minimal and maximal elements
		for (int i = 0; i < node_size; i ++)
			minimals[i] = maximals[i] = true;
			
		for (int i = 0; i < node_size; i ++) {
			if (minimals[i]) {
				for (int j = 0; j < node_size; j ++) {
					if (i != j && minimals[j] && 
						subsetReach(j, i) && !subsetReach(i, j)) {
						minimals[i] = false;
						break;
					}
				}
			}
		}

		for (int i = 0; i < node_size; i ++) {
			if (maximals[i]) {
				for (int j = 0; j < node_size; j ++) {
					if (i != j && maximals[j] && 
						subsetReach(i, j) && !subsetReach(j, i)) {
						maximals[i] = false;
						break;
					}
				}
			}
		}	
		
		if (DEBUG >= MEDIUM_DEBUG) {
			System.out.println("minimals=" + Arrays.toString(minimals));
			System.out.println("maximals=" + Arrays.toString(maximals));
		}
	}
	
	public boolean reached(int node) { return reached[node]; }
	
	public void setSCCGraph(SCCGraph s) { scc_graph = s; }
		                                                      
	public Node findOrAdd(Vector<Integer> tuple) {
		Node node = nodeDict.findOrAdd(tuple, zigzag, this);
		nodeList.add(node);		
		return node; 
	}

	public Vector<Node> getNodes() { return nodeList; }
			
	public void addEdge(Node src, Zigzag z, Node dest) {
		// if edge exists, keep the minimum weight
		for (int i = 0; i < src.outEdges().size(); i ++) {
			
			if (Thread.interrupted()) {
				throw new RuntimeException(" --- interrupted");
			}
			
			Edge e = src.outEdges().elementAt(i);
			
			if (e.getDestination() == dest) {
				if (z != null && e.getWeight() > z.weight)
					e.setZigzag(z);
				return;
			}
		}
		
		Edge edge = new Edge(src,dest,z); 
		
		src.addOutEdge(edge);
		dest.addInEdge(edge);
		
		edge_count ++;
	}
	
	public void removeNode(Node node) {
		for (int i = 0; i < node.inEdges().size(); i ++) {
			Edge e = node.inEdges().elementAt(i);
			node.removeInEdge(e);
			e.getSource().removeOutEdge(e);
		}
	
		for (int i = 0; i < node.outEdges().size(); i ++) {
			Edge e = node.outEdges().elementAt(i);
			node.removeOutEdge(e);
			e.getDestination().removeInEdge(e);
		}
		
		nodeList.remove(node);
	}
	
	public Zigzag getZigzag() { return zigzag; }
	
	public int getNodeSize() { return node_size; }
	
	public int getNodeCount() { return node_count; }
	
	public int getEdgeCount() { return edge_count; }
		
	public void incNodeCount() { node_count ++; }
		
	/*
	 * On-the-fly graph generation
	 */
	public void depthFirstGenerate(Node node) {
		if (node.isVisited())
			return;
	
		stack.push(node);
		
		while (!stack.empty()) {
			
			node = stack.pop();
			
			if (node.isVisited())
				continue;
				
			node.setVisited();
			
			connectStartRoots(node);
			connectEndRoots(node);
			
			Vector<Zigzag> outZigzags = node.getOutZigzags(this);
				
			// System.out.println("\t\t" + node + " : " + outZigzags);
			
			for (int i = 0; i < outZigzags.size(); i ++) {
				Zigzag z = outZigzags.elementAt(i);
				
				Vector<Node> succNodes = z.getSuccessorNodes(this);
				
				// System.out.println("\t\t" + z + " : " + succNodes);
				
				for (int j = 0; j < succNodes.size(); j ++) {	
					Node next = succNodes.elementAt(j);
					
					addEdge(node, z, next);
						
					if (!next.isVisited())
						stack.push(next);
						
					if (DEBUG >= MEDIUM_DEBUG)
						System.out.println(node + " ==" + z + "==> " + next + " (" + next.isVisited() + ")");
				}				
			}
		}
	}
	
	private void connectStartRoots(Node node) {
		Vector<Integer> tuple = node.getTuple();
		
		if (evenBackStart.isTerminal(tuple)) {			
			if (DEBUG >= MEDIUM_DEBUG)
				System.out.println(evenBackStart + " ==" + null + "==> " + node);
			
			addEdge(evenBackStart, null, node);
		}

		for (int i = 0; i < node_size; i ++) {
			
			if (oddForwStart[i].isTerminal(tuple)) {			
				if (DEBUG >= MEDIUM_DEBUG)
					System.out.println(oddForwStart[i] + " ==" + null + "==> " + node);
				
				addEdge(oddForwStart[i], null, node);
				return;				
			}

			if (oddBackStart[i].isTerminal(tuple)) {				
				if (DEBUG >= MEDIUM_DEBUG)
					System.out.println(oddBackStart[i] + " ==" + null + "==> " + node);
				
				addEdge(oddBackStart[i], null, node);
				return;
			}

			for (int j = 0; j < node_size; j ++) {
				if (evenForwStart[i][j].isTerminal(tuple)) {
					if (DEBUG >= MEDIUM_DEBUG)
						System.out.println(evenForwStart[i][j] + " ==" + null + "==> " + node);
					
					addEdge(evenForwStart[i][j], null, node);
					return;
				}				
			}
		}
	}
	
	private void connectEndRoots(Node node) {
		Vector<Integer> tuple = node.getTuple();

		if (evenForwEnd.isTerminal(tuple)) {
			if (DEBUG >= MEDIUM_DEBUG)
				System.out.println(node + " ==" + null + "==> " + evenForwEnd);
			
			addEdge(node, null, evenForwEnd);	
			return;
		}
				
		for (int i = 0; i < node_size; i ++) {
			if (oddForwEnd[i].isTerminal(tuple)) {				
				if (DEBUG >= MEDIUM_DEBUG)
					System.out.println(node + " ==" + null + "==> " + oddForwEnd[i]);
				
				addEdge(node, null, oddForwEnd[i]);
				return;
			}

			if (oddBackEnd[i].isTerminal(tuple)) {				
				if (DEBUG >= MEDIUM_DEBUG)
					System.out.println(node + " ==" + null + "==> " + oddBackEnd[i]);
				
				addEdge(node, null, oddBackEnd[i]);
				return;
			}

			for (int j = 0; j < node_size; j ++) {
				if (evenBackEnd[i][j].isTerminal(tuple)) {
					
					if (DEBUG >= MEDIUM_DEBUG)
						System.out.println(node + " ==" + null + "==> " + evenBackEnd[i][j]);
					
					addEdge(node, null, evenBackEnd[i][j]);
					return;
				}				
			}		
		}
	}	
		
	public void initRootNodes() {
		oddForwStart = new RootNode[node_size];
		oddForwEnd = new RootNode[node_size];
		oddForwDone = new boolean[node_size];
		
		oddBackStart = new RootNode[node_size];
		oddBackEnd = new RootNode[node_size];
		oddBackDone = new boolean[node_size];
		
		evenForwStart = new RootNode[node_size][node_size];
		evenBackEnd = new RootNode[node_size][node_size];
		evenForwDone = new boolean[node_size][node_size];
		evenBackDone = new boolean[node_size][node_size];
		
		for (int i = 0; i < node_size; i ++) {
			oddForwStart[i] = new RootNode(i, -1, node_size);
			oddForwStart[i].initRootNode(true,true,true);
			
			oddForwEnd[i] = new RootNode(i, -1, node_size);
			oddForwEnd[i].initRootNode(true, true, false);
			
			oddBackStart[i] = new RootNode(i, -1, node_size);
			oddBackStart[i].initRootNode(true, false, true);
			
			oddBackEnd[i] = new RootNode(i, -1, node_size);
			oddBackEnd[i].initRootNode(true, false, false);
			
			for (int j = 0; j < node_size; j  ++) {
				evenForwStart[i][j] = new RootNode(i, j, node_size);
				evenForwStart[i][j].initRootNode(false, true, true);
				
				evenBackEnd[i][j] = new RootNode(i, j, node_size);
				evenBackEnd[i][j].initRootNode(false, false, false);				
			}
		}
		
		evenForwEnd = new RootNode(-1, -1, node_size);
		evenForwEnd.initRootNode(false, true, false);
		addEdge(evenForwEnd, new Zigzag(node_size), evenForwEnd);
		
		evenBackStart = new RootNode(-1, -1, node_size);
		evenBackStart.initRootNode(false, false, true);
		addEdge(evenBackStart, new Zigzag(node_size), evenBackStart);
	}
	
	public void buildSemilinearSets(Node root) { scc_graph.buildSemilinearSets(root); }
	
	public void oddForw(int from, OctagonalClosure closure) {
		if (!maximals[from])
			return;
			
		for (int i = 0; i < node_size; i ++)
			reached[i] = reachable[from][i];
			
		if (Graph.DEBUG >= Graph.LOW_DEBUG) 
			System.out.println("oddForw(" + from + "): generating zigzag automaton ...");
		
		Vector<Node> initSet = oddForwStart[from].getSuccessors(this);
		
		for (int i = 0; i < initSet.size(); i ++) {
			Node node = initSet.elementAt(i);
			depthFirstGenerate(node);
		}
		
		if (DEBUG >= MEDIUM_DEBUG)
			clearDeadStates();
		
		if (Graph.DEBUG >= Graph.LOW_DEBUG)
			System.out.println(getNodeCount() + " nodes " + getEdgeCount() + " edges");
		
		for (int i = 0; i < node_size; i ++) {
			if (!oddForwDone[i] && reached[i]) {
					
				if (Graph.DEBUG >= Graph.LOW_DEBUG)		
					System.out.println("oddForw(" + i + "): running minpath ...");

				buildSemilinearSets(oddForwStart[i]);
								
				for (int j = 0; j < node_size; j ++) {
					oddForwEnd[j].m_X.minimize();
						
					if (Graph.DEBUG >= Graph.LOW_DEBUG)
						System.out.println("oddForw(" + i + ", " + j + ") --> " + oddForwEnd[j].m_X);
				
					closure.setUnprimedPrimed(i, j, oddForwEnd[j].m_X);
				}
				
				clearSemilinearSets();

				oddForwDone[i] = true;
			}
		}
	}
	
	public void oddBack(int to, OctagonalClosure closure) {	
		if (!minimals[to])
			return;
		
		for (int i = 0; i < node_size; i ++)
			reached[i] = reachable[i][to];
					
		if (Graph.DEBUG >= Graph.LOW_DEBUG)
			System.out.println("oddBack(" + to + "): generating zigzag automaton ...");
		
		Vector<Node> initSet = oddBackStart[to].getSuccessors(this);
		
		for (int i = 0; i < initSet.size(); i ++) {
			Node node = initSet.elementAt(i);
			depthFirstGenerate(node);
		}
		
		if (DEBUG >= MEDIUM_DEBUG)
			clearDeadStates();
		
		if (Graph.DEBUG >= Graph.LOW_DEBUG)
			System.out.println(getNodeCount() + " nodes " + getEdgeCount() + " edges");

		for (int i = 0; i < node_size; i ++) {
			if (!oddBackDone[i] && reached[i]) {

				if (Graph.DEBUG >= Graph.LOW_DEBUG)		
					System.out.println("oddBack(" + i + "): running minpath ...");

				buildSemilinearSets(oddBackStart[i]);
				
				for (int j = 0; j < node_size; j ++) {
					oddBackEnd[j].m_X.minimize();
					
					if (Graph.DEBUG >= Graph.LOW_DEBUG)
						System.out.println("oddBack(" + i + ", " + j + ") --> " + oddBackEnd[j].m_X);

					closure.setPrimedUnprimed(j, i, oddBackEnd[j].m_X);
				}
				
				clearSemilinearSets();

				oddBackDone[i] = true;
			}			
		}		
	}
	
	public void evenForw(int from, int to, OctagonalClosure closure) {
		if (!maximals[from] && !minimals[to])
			return;
		
		for (int i = 0; i < node_size; i ++)
			reached[i] = reachable[from][i] && reachable[i][to];
		
		if (Graph.DEBUG >= Graph.LOW_DEBUG)
			System.out.println("evenForw(" + from + "," + to + "): generating zigzag automaton ...");
		
		Vector<Node> initSet = evenForwStart[from][to].getSuccessors(this);
		
		for (int i = 0; i < initSet.size(); i ++) {
			Node node = initSet.elementAt(i);
			depthFirstGenerate(node);
		}				
	
		if (DEBUG >= MEDIUM_DEBUG)
			clearDeadStates();
		
		if (Graph.DEBUG >= Graph.LOW_DEBUG)
			System.out.println(getNodeCount() + " nodes " + getEdgeCount() + " edges");

		for (int i = 0; i < node_size; i ++) {
			for (int j = 0; j < node_size; j ++) {
				if (i != j && !evenForwDone[i][j] && reached[i] && reached[j]) {
					
					if (Graph.DEBUG >= Graph.LOW_DEBUG)
						System.out.println("evenForw(" + i + "," + j + "): running minpath ...");

					buildSemilinearSets(evenForwStart[i][j]);
					
					evenForwEnd.m_X.minimize();
					
					if (Graph.DEBUG >= Graph.LOW_DEBUG)
						System.out.println("evenForw(" + i + ", " + j + ") --> " + evenForwEnd.m_X);

					closure.setUnprimedUnprimed(i, j, evenForwEnd.m_X);
					
					clearSemilinearSets();
					
					evenForwDone[i][j] = true;
				}
			}
		}
	}
	
	public void evenBack(int from, int to, OctagonalClosure closure) {	
		if (!maximals[from] && !minimals[to])
			return;
		
		for (int i = 0; i < node_size; i ++)
			reached[i] = reachable[from][i] && reachable[i][to];

		if (Graph.DEBUG >= Graph.LOW_DEBUG)
			System.out.println("evenBack(): generating zigzag automaton ...");
		
		Vector<Node> initSet = evenBackStart.getSuccessors(this);
		
		for (int i = 0; i < initSet.size(); i ++) {
			Node node = initSet.elementAt(i);
			depthFirstGenerate(node);
		}						
	
		if (DEBUG >= MEDIUM_DEBUG)
			clearDeadStates();

		if (Graph.DEBUG >= Graph.LOW_DEBUG) 
			System.out.println("evenBack(): running minpath ...");

		buildSemilinearSets(evenBackStart);	

		for (int i = 0; i < node_size; i ++) {
			for (int j = 0; j < node_size; j ++) {
				if (i != j && !evenBackDone[i][j] && reached[i] && reached[j]) {		
					evenBackEnd[i][j].m_X.minimize();
					
					if (Graph.DEBUG >= Graph.LOW_DEBUG)
						System.out.println("evenBack(" + i + ", " + j + ") --> " + evenBackEnd[i][j].m_X);
					
					closure.setPrimedPrimed(i, j, evenBackEnd[i][j].m_X);
					
					evenBackDone[i][j] = true;
				}
			}
		}
		
		clearSemilinearSets();
	}	
	
	public void clearSemilinearSets() {
		evenForwEnd.m_X.clear();
		evenBackStart.m_X.clear();
		
		for (int i = 0; i < node_size; i ++) {			
			oddForwStart[i].m_X.clear();
			oddForwEnd[i].m_X.clear();
			oddForwDone[i] = false;
			
			oddBackStart[i].m_X.clear();
			oddBackEnd[i].m_X.clear();
			oddBackDone[i] = false;
			
			for (int j = 0; j < node_size; j ++) {
				evenForwStart[i][j].m_X.clear();
				evenBackEnd[i][j].m_X.clear();
				evenForwDone[i][j] = false;
			}
		}
		
		for (int i = 0; i < nodeList.size(); i ++)
			nodeList.elementAt(i).m_X.clear();
	}
	
	private void backwardsPrune(Node node, HashSet<Node> visited) {
		LinkedList<Node> worklist = new LinkedList<Node>();
		
		worklist.addLast(node);
		
		while ( !worklist.isEmpty() ) {
			Node next = worklist.removeFirst();

			visited.add(next);
			
			for (int i = 0; i < next.inEdges().size(); i ++) {
				Node source = next.inEdges().elementAt(i).getSource();
				if ( !visited.contains(source) )
					worklist.addLast(source);
			}
		}
	}

	public void clearDeadStates() {
		HashSet<Node> visited = new HashSet<Node>();
		
		backwardsPrune(evenForwEnd, visited); 
		
		for (int i = 0; i < node_size; i ++) {
			backwardsPrune(oddForwEnd[i], visited);
			backwardsPrune(oddBackEnd[i], visited);
			
			for (int j = 0; j < node_size; j ++) 
				backwardsPrune(evenBackEnd[i][j], visited);
		}
		
		Vector<Node> tobeRemoved = new Vector<Node>();
		
		for (int i = 0; i < nodeList.size(); i ++)
			if ( !visited.contains(nodeList.elementAt(i)) )
				tobeRemoved.add(nodeList.elementAt(i));
		
		for (int i = 0; i < tobeRemoved.size(); i ++)
			removeNode(tobeRemoved.elementAt(i));
	}	
}