package verimag.flata.automata;

import java.util.*;


/**
 * Skeletal abstract class of a graph.
 * It introduces the topology of a graph:
 * 	<ul>
 * 		<li>graph is oriented</li>
 * 		<li>each node (see {@link BaseNode}) is able to provide a <code>Collection</code> of 
 * 			its	incoming and outcoming edges</li>
 * 		<li>each arc (see {@link BaseArc}) keeps its weight and and is able to provide 
 * 			its origin and destination nodes</li>
 * 	</ul>
 *
 * <p>
 * Also, the class implements some graph algorithms (e.g. Tarjan's algorithm).
 */
public abstract class BaseGraph {

	public abstract Collection<? extends BaseNode> nodes();
	public abstract Collection<? extends BaseArc> arcs();
	public abstract Collection<? extends BaseNode> initials();

	// fields used in Tarjan's algorithm
	private int tarjan_INIT = -1;
	private int index;
	private int sccCnt;
	private Deque<BaseNode> S;
	
	// set of strongly connected components
	protected List<List<BaseNode>> sccs;
	
	public int sccCount() { return sccs.size(); }
	
	/**
	 * Tarjan's algorithm for computation of strongly connected components of a subgraph
	 * reachable from the specified node. 
	 */
	public void tarjan(BaseNode v) {

		v.t_index = index;						// Set the depth index for v
		v.t_lowlink = index;
		index = index + 1;
		
		// Push v on the stack and add a flag that v is on the stack
		S.addFirst(v);
		v.t_onStack = true;
		
		for (Object t : v.outgoing()) {		// Consider successors of v
			BaseNode v2 = ((BaseArc)t).to();
			
//			// skip SCCs found reachable from initial states that have been processed
//			if (v2.scc!=null)
//				continue;
			
			if (v2.t_index == tarjan_INIT) {	// Was successor v' visited? 
				tarjan(v2);						// Recurse
				v.t_lowlink = Math.min(v.t_lowlink, v2.t_lowlink);
			} else if (v2.t_onStack) {			// Is v' on the stack?
				v.t_lowlink = Math.min(v.t_lowlink, v2.t_index);
			}
		}
		if (v.t_lowlink == v.t_index) {			// Is v the root of an SCC?
			
//			if (v.scc==null) { 					// a SCC has has not been found before for node v
				
				List<BaseNode> scc = new LinkedList<BaseNode>();
				
				BaseNode v2;
				do {
					// remove from stack
					v2 = S.removeFirst();
					v2.t_onStack = false;
					
					// give v2 a reference to the SCC and add v2 to the SCC
					v2.scc = scc;
					v2.t_sccInx = sccCnt;
					scc.add(v2);
		
				} while (v2 != v);
				
				sccs.add(scc);
				sccCnt++;
//			} else {
//				throw new RuntimeException("internal error");
//			}
		}
	}

	/**
	 * reset fields which are used in the Tarjan's algorithm
	 */
	private void tarjanReset() {
		for (BaseNode s : nodes()) {
			s.tarjanReset(tarjan_INIT);
		}
	}
	/**
	 * resets references of the specified nodes to their strongly connected components
	 */
	private void resetSccs() {
		for (BaseNode s : nodes()) {
			s.resetSccs();
		}
	}
	
	/**
	 * finds strongly connected components (SCCs) of all nodes which are reachable from the 
	 * specified set of 'initial' nodes. The method returns the set of SCCs. Moreover,
	 * it sets a reference to the corresponding CSS for each node.
	 * 
	 * <p>
	 * The method is a modification of Tarjan's algorithm. When necessary, it runs
	 * Tarjan's algorithm several times in order to get SCCs
	 * reachable from the specified set of 'initial' nodes.
	 */
	public List<List<BaseNode>> findSccs() {
		sccs = new LinkedList<List<BaseNode>>();
		
		Collection<? extends BaseNode> initialsCopy = new HashSet<BaseNode>(initials());
		
		resetSccs();
		
		tarjanReset();
		index = 0;								// DFS node number counter
		sccCnt = 0;
		S = new ArrayDeque<BaseNode>();		// An empty stack of nodes
		
		while (!initialsCopy.isEmpty()) {
			
			Iterator<? extends BaseNode> iter = initialsCopy.iterator();
			tarjan(iter.next());					// Start a DFS at the start node
			iter.remove();
			
			while (iter.hasNext()) {				// remove initial states which have been processed
				BaseNode s = iter.next();
				if (s.t_index != -1)
					iter.remove();
			}
		}
		
		return sccs;
	}
	
	
	// TODO check
	// returns initial nodes of the DAG
	public List<DAGNode> sccGraph(Collection<? extends BaseNode> initialNodes) {
		
		findSccs();

		DAGNode[] nodes = new DAGNode[sccCnt];
		//int i = 0;
		for (List<BaseNode> scc : sccs) {
			int i = scc.get(0).t_sccInx;
			nodes[i] = new DAGNode(i);

			for (BaseNode n : scc) {
				nodes[i].addNode(n);
				if (n.t_index == n.t_lowlink)
					nodes[i].tarjanEntry(n);
			}
			//i++;
		}

		for (BaseNode n : this.nodes()) {
			
			DAGNode from = nodes[n.t_sccInx];
			for (BaseArc a : n.outgoing()) {
				
				DAGNode to = nodes[a.to().t_sccInx];
				
				if (!from.equals(to)) {
					
					DAGArc arc = new DAGArc(from, to);
					
					// add DAG arc
					from.addOutgoing(arc);
					to.addIncoming(arc);
					
					// add information about base arc
					from.addOutArc(a);
					to.addInArc(a);
					
				} else {
					
					// add information about base arc
					from.addInterArc(a);
				}
			}
		}

		List<DAGNode> ret = new LinkedList<DAGNode>();
		for (BaseNode n : initialNodes) {
			DAGNode dn = nodes[n.t_sccInx];
			if (!ret.contains(dn))
				ret.add(dn);
		}
		
		return ret;
	}

	
	
	/**
	 * for each node <tt>n</tt> from the specified set, the method computes the 
	 * number of <tt>n</tt>'s outgoing edges which are contained in the <tt>n</tt>'s SCC
	 * 
	 * <p>
	 * Note: This method expects that SCCs are computed (i.e. that a method 
	 * {@link BaseGraph#findSccs()} has been invoked previously)
	 * 
	 * @param aNodes a set of nodes for which counting is done
	 */
	public static void countSccOutEdges(Collection<? extends BaseNode> aNodes) {
		Iterator<? extends BaseNode> iter = aNodes.iterator();
		while (iter.hasNext()) {
			BaseNode v = (BaseNode) iter.next();
			v.sccOutEdges = 0;
			for (Object t : v.outgoing())
				if (v.scc.contains(((BaseArc)t).to()))
					v.sccOutEdges++;
		}
	}
	
	public boolean allSCCareTrivial() {
		for (List<BaseNode> scc : this.findSccs()) {
			if (scc.size() > 1)
				return false;
			if (scc.size() == 1 && scc.get(0).loops() > 0)
				return false;
		}
		return true;
	}

	
	/**
	 * works in the same manner as {@link #countSccOutEdges(Collection)}, with one difference - 
	 * it performs counting for all graph nodes
	 */
	public void countSccOutEdges() {
		countSccOutEdges(nodes());
	}
	
	/**
	 * Computes a 'measure' of flatness of a specified strongly connected component (SCC).
	 * The measure used is a number of branches in the specified SCC.
	 * @param aScc a SCC for which the measure of flatness is computed
	 * @return the computed measure of flatness
	 */ 
	public static double flatnessMeasure(List<? extends BaseNode> aScc) {
		double branches = 0.0;
		Iterator<? extends BaseNode> iter = aScc.iterator();
		while (iter.hasNext()) {
			BaseNode n = (BaseNode)(iter.next());
			
			if (n.sccOutEdges<2)
				continue;
			
			branches += n.sccOutEdges;
		}
		return branches;
	}

	
	public static Set<BaseNode> reach(Collection<? extends BaseNode> roots) {
		Set<BaseNode> done = new HashSet<BaseNode>();
		Set<BaseNode> todo = new HashSet<BaseNode>();
		todo.addAll(roots);
		while (!todo.isEmpty()) {
			Iterator<BaseNode> i = todo.iterator();
			BaseNode n = i.next();
			i.remove();
			
			done.add(n);
			
			for (BaseArc a : n.outgoing()) {
				if (!done.contains(a.to())) {
					todo.add(a.to());
				}
			}
		}
		
		return done;
	}
	
	
	public static List<BaseNode> topologicalSortFW(BaseNode root) { return topologicalSort(root, false); }
	public static List<BaseNode> topologicalSortBW(BaseNode root) { return topologicalSort(root, true); }
	private static List<BaseNode> topologicalSort(BaseNode root, boolean backward) {
		Collection<BaseNode> aux = new LinkedList<BaseNode>();
		aux.add(root);
		return topologicalSort(aux, backward);
	}
	public static List<BaseNode> topologicalSortFW(Collection<? extends BaseNode> roots) { return topologicalSort(roots, false); }
	public static List<BaseNode> topologicalSortBW(Collection<? extends BaseNode> roots) { return topologicalSort(roots, true); }
	public static List<BaseNode> topologicalSort(Collection<? extends BaseNode> roots, boolean backward) {
		Set<BaseNode> reach = reach(roots);
		Map<BaseNode,Integer> m = new HashMap<BaseNode,Integer>();

		int i=0;
		for (BaseNode n : reach) {
			m.put(n, new Integer(i++));
		}
		
		int s = reach.size();
		boolean b[][] = new boolean[s][s];
		
		for (BaseNode n1 : reach) {
			int i1 = m.get(n1);
			for (BaseArc a : n1.outgoing()) {
				int i2 = m.get(a.to());
				b[i1][i2] = true;
			}
		}
		
		// check if there exists a topological sort
		{
			boolean[][] bb = Arrays.copyOf(b, s);
			for (int ii=0; ii<bb.length; ii++) 
				bb[ii] = Arrays.copyOf(b[ii], s);
			
			for (int i1=0; i1<s; i1++)
				for (int i2=0; i2<s; i2++)
					for (int i3=0; i3<s; i3++)
						if (!bb[i1][i2])
							bb[i1][i2] = bb[i1][i3] && bb[i3][i2];
			for (int i1=0; i1<s; i1++)
				if (bb[i1][i1]) {
					throw new RuntimeException("internal error: call for topological sort of a cyclic graph");
				}
		}
		
		List<BaseNode> l = new LinkedList<BaseNode>();
		List<BaseNode> todo = new LinkedList<BaseNode>();
		for (BaseNode n : roots) {
			if (n.incoming().size() == 0)
				todo.add(n);
		}
		
		while (!todo.isEmpty()) {
			BaseNode n = todo.remove(0);
			l.add(n);
			
			int i1 = m.get(n);
			for (BaseArc a : n.outgoing()) {
				BaseNode n2 = a.to();
				int i2 = m.get(n2);
				b[i1][i2] = false;
				boolean bb = true;
				for (int ii=0; ii<s; ii++) {
					if (b[ii][i2]) {
						bb = false;
						break;
					}
				}
				if (bb)
					todo.add(n2);
			}
		}
		
		if (backward) {
			
			for (int jj=1; jj<=s/2; jj++) {
				int inx1 = jj-1;
				int inx2 = s-jj;
				BaseNode aux = l.get(inx1);
				l.set(inx1, l.get(inx2));
				l.set(inx2, aux);
			}
		}
		
		return l;
	}

	/**
	 * Performs DFS traversal of a subgraph formed by nodes given as the parameter.
	 */
	public static List<BaseArc> dfs(Collection<BaseNode> initials, Collection<BaseNode> onlyVia) {
		Deque<BaseArc> todo = new LinkedList<BaseArc>();
		List<BaseArc> ret = new LinkedList<BaseArc>();
		Set<BaseArc> seen = new HashSet<BaseArc>();
		
		for (BaseNode n : initials) {
			if (onlyVia.contains(n)) {
				for (BaseArc a : n.outgoing()) {
					if (onlyVia.contains(a.to())) {
						if (!seen.contains(a)) {
							todo.push(a);
							seen.add(a);
						}
					}
				}
			}
		}
		
		while (!todo.isEmpty()) {
			BaseArc aa = todo.pop();
			ret.add(aa);
			
			for (BaseArc a : aa.to().outgoing()) {
				if (onlyVia.contains(a.to())) {
					if (!seen.contains(a)) {
						todo.push(a);
						seen.add(a);
					}
				}
			}
		}
		
		return ret;
	}
	
	public static List<BaseArc> arcSortViaDag(Collection<? extends BaseNode> initials, boolean backward) {
		
		List<BaseNode> topSort = BaseGraph.topologicalSort(initials, backward);
		
		List<BaseArc> arcSort = new LinkedList<BaseArc>();
		
		for (BaseNode n : topSort) {
			arcSort.addAll(n.atom_incoming());
			arcSort.addAll(n.atom_internal());
		}
		
		return arcSort;
	}
}
