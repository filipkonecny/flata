package verimag.flata.automata;

import java.util.*;

public class DAGNode extends BaseNode {
	
	int id = -1;
	
//	private static int id_gen = 1; 
//	public void createID() {
//		this.id = id_gen ++;
//	}
	
	public DAGNode(int aId) {
		id = aId;
	}

	List<DAGArc> incoming = new LinkedList<DAGArc>();
	List<DAGArc> outgoing = new LinkedList<DAGArc>();
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////// implementation of abstract methods //////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected boolean addIncoming_internal(BaseArc aArc) {
		if (incoming.contains((DAGArc)aArc))
			return false;
		else
			return incoming.add((DAGArc)aArc);
	}
	@Override
	protected boolean addOutgoing_internal(BaseArc aArc) {
		if (outgoing.contains((DAGArc)aArc))
			return false;
		else
			return outgoing.add((DAGArc)aArc);
	}
	@Override
	public Collection<DAGArc> incoming() {
		return incoming;
	}
	@Override
	public Collection<DAGArc> outgoing() {
		return outgoing;
	}
	@Override
	protected boolean removeIncoming_internal(BaseArc aArc) {
		return incoming.remove((DAGArc)aArc);
	}
	@Override
	protected boolean removeOutgoing_internal(BaseArc aArc) {
		return outgoing.remove((DAGArc)aArc);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	/////////   data of the corresponding "atomic" graph     ///////////////////////
	////////////////////////////////////////////////////////////////////////////////	
	
	
	// !!! only in_states and out_states are checked for duplicities in collection
	
	// nodes of the SCC, border nodes (in, out) 
	List<BaseNode> states = new LinkedList<BaseNode>();
	List<BaseNode> states_in = new LinkedList<BaseNode>();
	List<BaseNode> states_out = new LinkedList<BaseNode>();
	
	// border arcs (in, out), internal arcs of the SCC
	List<BaseArc> atom_arcs_in = new LinkedList<BaseArc>();
	List<BaseArc> atom_arcs_out = new LinkedList<BaseArc>();
	List<BaseArc> atom_arcs_internal = new LinkedList<BaseArc>();
	
	// the node via which Tarjan algorithm entered this SCC
	List<BaseNode> tarjanEntry = new LinkedList<BaseNode>();
	// sets Tarjan entry node
	public void tarjanEntry(BaseNode n) {
		tarjanEntry.clear(); tarjanEntry.add(n);
	}
	
	public List<BaseNode> states() { return states; }
	public List<BaseNode> inStates() { return states_in; }
	public List<BaseNode> outStates() { return states_out; }
	
	public boolean containsInState(BaseNode s) { return states_in.contains(s); }
	public boolean containsOutState(BaseNode s) { return states_out.contains(s); }
	
	////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////  overrides  /////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	public Collection<? extends BaseArc> atom_incoming() { return atom_arcs_in; }
	public Collection<? extends BaseArc> atom_outgoing() { return atom_arcs_out; }
	public Collection<? extends BaseArc> atom_internal() {
		return BaseGraph.dfs(tarjanEntry, states);
	}
		
	// adds a node to this SCC
	public void addNode(BaseNode s) { 
		states.add(s);
	}
	// is a node present in this SCC ?
	public boolean containsNode(BaseNode s) {
		return states.contains(s);
	}
	// adds a border (in) node
	private void addInNode(BaseNode s) { 
		if (!states_in.contains(s))
			states_in.add(s); 
	}
	// adds a border (out) node
	private void addOutNode(BaseNode s) { 
		if (!states_out.contains(s))
			states_out.add(s); 
	}
	
	public void addInArc(BaseArc a) {
		atom_arcs_in.add(a); addInNode(a.to());
	}
	public void addOutArc(BaseArc a) {
		atom_arcs_out.add(a); addOutNode(a.from());
	}
	public void addInterArc(BaseArc a) {
		atom_arcs_internal.add(a);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////	
	
	
	public String toString() {
		String pred = "", succ = "";
		for (DAGArc a : incoming)
			pred += a.from().id+",";
		for (DAGArc a : outgoing)
			succ += a.to().id+",";
		return "<SCC "+id+": "+states+", pred:"+pred+" succ: "+succ+">";
	}
	
	public void copyBaseData(DAGNode other) {
		states.addAll(other.states);
		states_in.addAll(other.states_in);
		states_out.addAll(other.states_out);
		atom_arcs_in.addAll(other.atom_arcs_in);
		atom_arcs_out.addAll(other.atom_arcs_out);
		atom_arcs_internal.addAll(other.atom_arcs_internal);
	}
	private static DAGNode get_create_in_map(Map<DAGNode, DAGNode> map, DAGNode n) {
		DAGNode c_n;
		if (map.containsKey(n))
			c_n = map.get(n);
		else {
			c_n = new DAGNode(n.id);
			c_n.copyBaseData(n);
			map.put(n, c_n);
		}
		return c_n;
	}
	public static Map<DAGNode,DAGNode> copy(DAGNode[] init) {
		Map<DAGNode,DAGNode> map = new HashMap<DAGNode,DAGNode>();
		Deque<DAGNode> visited = new LinkedList<DAGNode>();
		Deque<DAGNode> todo = new LinkedList<DAGNode>();
		
		for (DAGNode n : init) {
			todo.add(n);
		}
		
		while (!todo.isEmpty()) {
			DAGNode n = todo.removeFirst();
			
			if (visited.contains(n))
				throw new RuntimeException();
			
			visited.add(n);
			
			DAGNode c_n = get_create_in_map(map,n);
			
			for (DAGArc a : n.outgoing) {
				DAGNode to = a.to();
				DAGNode c_to = get_create_in_map(map,to);
				DAGArc c_a = new DAGArc(c_n, c_to);
				
				c_n.addOutgoing(c_a);
				c_to.addIncoming(c_a);
				
				if (!visited.contains(to) && !todo.contains(to))
					todo.add(to);
			}
		}
		
		return map;
	}
	
}
