package verimag.flata.automata;

import java.util.*;

/**
 * Skeletal abstract class of a node of a graph. It is an element of topology introduced by 
 * {@link BaseGraph}.
 *  
 *  It also defines some other members used in algorithms of the <tt>GraphAlg</tt> class.
 *  It implements final methods 
 *  {@link #addIncoming(BaseArc)},
 *  {@link #removeIncoming(BaseArc)},
 *  {@link #addOutgoing(BaseArc)},
 *  {@link #removeOutgoing(BaseArc)}.
 *  Classes which extend this class must implement their code for addition and removal of 
 *  incoming and outcoming transitions for following abstract methods:
 *  {@link #addIncoming_internal(BaseArc)},
 *  {@link #removeIncoming_internal(BaseArc)},
 *  {@link #addOutgoing_internal(BaseArc)},
 *  {@link #removeOutgoing_internal(BaseArc)}.
 *  
 * <p>
 * <b>Important note:</b> 
 * Method {@link #scc()} and {@link #sccOutEdges()} return only values of corresponding members.
 * They do not compute anything. Therefore, previous invocations of {@link BaseGraph#findSccs()},
 * respectively of {@link BaseGraph#findSccs()} and {@link BaseGraph#countSccOutEdges()}
 * are expected.
 */
public abstract class BaseNode {
	
	////////////////////////////////////////////////////////////////////////////////
	//////////////////////////  abstract methods  //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	public abstract Collection<? extends BaseArc> incoming();
	public abstract Collection<? extends BaseArc> outgoing();
	
	protected abstract boolean addIncoming_internal(BaseArc aArc);
	protected abstract boolean removeIncoming_internal(BaseArc aArc);
	protected abstract boolean addOutgoing_internal(BaseArc aArc);
	protected abstract boolean removeOutgoing_internal(BaseArc aArc);
	
	// Subclasses of BaseGraph may represent more abstract structure, such as graphs
	// of graphs (e.g. graphs of strongly connected components). Following method refer to 
	// the atomic edges of graphs (e.g. atomic arcs of SCC graphs). These abstract 
	// structures may override these methods.
	
	public Collection<? extends BaseArc> atom_incoming() { return incoming(); }
	public Collection<? extends BaseArc> atom_outgoing() { return outgoing(); }
	// empty collection by default
	public Collection<? extends BaseArc> atom_internal() { return new LinkedList<BaseArc>(); }
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
//	// number of self-loops
//	private int loops = 0;
	
	public final boolean addIncoming(BaseArc aArc) {		
		boolean b = addIncoming_internal(aArc);
		
//		// common operations
//		if (aArc.from()==this)
//			loops++;
		
		return b;
	}
	public final boolean removeIncoming(BaseArc aArc) {
		boolean b = removeIncoming_internal(aArc);
		
//		// common operations
//		if (aArc.from()==this)
//			loops--;
		
		return b;
	}
	public final boolean addOutgoing(BaseArc aArc) {
		return addOutgoing_internal(aArc);
		
		// common operations
	}
	public final boolean removeOutgoing(BaseArc aArc) {
		return removeOutgoing_internal(aArc);
		
		// common operations
	}

		
	/**
	 * @return a number of self-loops
	 */
	public int loops() {
		int x = 0;
		for (BaseArc a : this.outgoing()) {
			if (a.from().equals(a.to())) {
				x++;
			}
		}
		return x;
	}
	public Set<BaseArc> getLoops() {
		Set<BaseArc> loops = new HashSet<BaseArc>();
		for (BaseArc arc : this.outgoing())
			if (arc.to()==this)
				loops.add(arc);
		return loops;
	}
	public Set<BaseNode> getNeighbours() {
		Set<BaseNode> neighbours = new HashSet<BaseNode>();
		for (BaseArc arc : this.outgoing()) {
			BaseNode n =arc.to();
			if (!n.equals(this))
				neighbours.add(n);
		}
		for (BaseArc arc : this.incoming()) {
			BaseNode n =arc.from();
			if (!n.equals(this))
				neighbours.add(n);
		}

		return neighbours;
	}
	
	public static int elemCycles(BaseNode n1, BaseNode n2) {
		int x1 = 0;
		for (BaseArc a : n1.outgoing())
			if (a.to().equals(n2)) {
				x1++;
			}
		if (x1==0)
			return x1;
		
		int x2 = 0;
		for (BaseArc a : n2.outgoing())
			if (a.to().equals(n1)) {
				x2++;
			}
		return x1*x2;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////  data used in Tarjan algorithm  ///////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	protected int t_index;
	protected int t_lowlink;
	protected boolean t_onStack;
	protected int t_sccInx = -1;
	protected void tarjanReset(int tarjan_INIT) {
		t_index = tarjan_INIT;
		t_lowlink = tarjan_INIT;
		t_onStack = false;
		t_sccInx = -1;
	}
	protected void resetSccs() {
		scc=null;
	}
	
	
	
	// reference to corresponding strongly connected component
	protected List<BaseNode> scc = null;
	// number of outgoing edges in the node's SCC subgraph
	protected int sccOutEdges;
	/**
	 * gives its <em>strongly connected component</em>
	 */
	public Set<BaseNode> scc() { return new HashSet<BaseNode>(scc); }
	/**
	 * gives a number of edges coming from this node which are contained in this node's SCC.
	 */
	public int sccOutEdges() { return sccOutEdges; }

}
