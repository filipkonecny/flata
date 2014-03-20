/**
 * 
 */
package verimag.flata.automata.ca;

import java.util.*;


@SuppressWarnings("incomplete-switch")
public class ReduceInfo {
	
	public static int _redCnt = 0;
	private int redCnt = _redCnt;
	
	//private int lastRefBorder = -1;
	
	protected ReduceOp op; // operation
	public ReduceOp op() { return op; }
	
	protected List<ReduceInfo> pred = new LinkedList<ReduceInfo>();
	protected List<ReduceInfo> succ = new LinkedList<ReduceInfo>();
	
	public boolean onePred() { return pred.size() == 1; }
	public ReduceInfo firstPred() { return pred.get(0); }
	
	// list of subsumed transitions; list is copied for 
	// hull, abstract, and reconnect operations
	protected List<ReduceInfo> subsumed = new LinkedList<ReduceInfo>();
	protected List<ReduceInfo> subsumedBy = new LinkedList<ReduceInfo>();
	//protected ReduceInfo subsumedBy = null;
	
	protected List<CENode> ceNodes = new LinkedList<CENode>();
	public void addCENode(CENode n) { ceNodes.add(n); }
	
	private ShortcutNode shortcut_up = null;
	private ShortcutNode shortcut_down = null;
	
	public void shortcut_up(ShortcutNode p) { shortcut_up = p; }
	public void shortcut_down(ShortcutNode p) { shortcut_down = p; }
	
	public ShortcutNode shortcut_up() { return shortcut_up; }
	public ShortcutNode shortcut_down() { return shortcut_down; }
	
	private CAState r_label; // reduction label
	
	//protected boolean meta = false;
	protected boolean useful = false;
	
	public void setUseful() { useful = true; }
	
	// setting of successors is done block-wise
	
	protected CATransition t;
	public CATransition t() { return t; }
	
	// connection infrastructure used to create doubly-linked lists for:
	//   -- transitions with same origin and same destination 
	//   -- transitions created after multi-loop transformation
//	private ReduceInfo next = null;
//	private ReduceInfo prev = null;

	public String toString() {
		return "("+op+", red-cnt: "+redCnt+", t-id: "+t.id()+", "+t.toString()+")";
	}
	
	protected ReduceInfo(CATransition aT, ReduceOp aOp) {
		t = aT;
		op = aOp;
	}
	private ReduceInfo(ReduceInfo other) {
		op = other.op;
		r_label = other.r_label;
		pred.addAll(other.pred);
		succ.addAll(other.succ);
	}
	public ReduceInfo copy() {
		return new ReduceInfo(this);
	}
	
	public void addSubsumed(ReduceInfo r) {
		
		if (!subsumedBy.isEmpty()) {
			throw new RuntimeException();
		}
		
		subsumed.add(r);
//		if (r.subsumedBy != null) {
//			System.err.println("------------");
//			//System.err.println(r);
//			//System.err.println(r.subsumedBy);
//			System.err.println(r.op);
//			System.err.println("has successors: "+!r.succ.isEmpty());
//			System.err.println("inverse binding: "+r.subsumedBy.subsumed.contains(r));
//			//System.err.println(r.subsumedBy.subsumed);
//			throw new RuntimeException("internal error: multiple subsumption of a transition");
//		}
		
		//r.subsumedBy = this;
		r.subsumedBy.add(this);
		
//		if (!r.succ.isEmpty())
//			throw new RuntimeException();
		
		//subsumed.addAll(r.subsumed); // subsumption transitivity
		//for (ReduceInfo ri : r.subsumed)
		//	r.subsumedBy.add(this);
	}
	public List<ReduceInfo> subsumed() {
		return subsumed;
	}
//	public void copySubsumed(ReduceInfo from) {
//		subsumed.addAll(from.subsumed);
//		for (ReduceInfo ri : from.subsumed)
//			ri.subsumedBy.add(this);
//	}
	
	public static ReduceInfo leaf(CATransition leaf) {
		ReduceInfo ret = new ReduceInfo(leaf,ReduceOp.LEAF);
		
		ret.r_label = null;
		
		return ret;
	}
	
	public static ReduceInfo identity(CATransition t) {
		ReduceInfo ret = new ReduceInfo(t,ReduceOp.IDENTITY);
		
		ret.r_label = null;
		
		return ret;
	}
	
//	private void add(ReduceInfo o) {
//		if (o.op.isHull()) {
//			pred.addAll(o.pred);
//			for (ReduceInfo oo : o.pred) {
//				if (oo.succ.size() != 1) // a transition may contribute to only one hull
//					throw new RuntimeException("internal error");
//				oo.succ.clear();
//				oo.succ.add(this);
//			}
//		} else {
//			pred.add(o);
//			//o.succ.clear();
//			o.succ.add(this);
//		}
//	}
	
	public static ReduceInfo subset(CATransition tSubset, ReduceInfo aSubsetOf) {
		ReduceInfo ret = new ReduceInfo(tSubset,ReduceOp.SUBSET);
		ret.pred.add(aSubsetOf);
		aSubsetOf.succ.add(ret);
		return ret;
	}
	public static ReduceInfo subset(CATransition tSubset, List<ReduceInfo> aSubsetOf) {
		ReduceInfo ret = new ReduceInfo(tSubset,ReduceOp.SUBSET);
		ret.pred.addAll(aSubsetOf);
		// note: successors are not set !!
		return ret;
	}
	
	public static ReduceInfo hull(CATransition thull, ReduceInfo o1, ReduceInfo o2) {
		ReduceInfo ret = new ReduceInfo(thull,ReduceOp.HULL);
		
		ret.t = thull;
		
//		ret.add(o1);
//		ret.add(o2);
		o1.succ.add(ret);
		o2.succ.add(ret);
		ret.pred.add(o1);
		ret.pred.add(o2);
		
		// TODO: ret.r_label = alevel;
		
		//ret.copySubsumed(o1);
		//ret.copySubsumed(o2);
		
//		// replace two items of the old linkage by a new one
//		ret.next = o1.next;
//		ret.prev = o1.prev;
//		if (o1.prev != null)
//			o1.prev.next = ret;
//		if (o1.next != null)
//			o1.next.prev = ret;
//		
//		ReduceInfo p1 = ret, p2 = o2;
//		while (p1.next != null)
//			p1 = p1.next;
//		while (p2.prev != null)
//			p2 = p2.prev;
//		
//		p1.next = p2;
//		p2.prev = p1;
		
		return ret;
	}
	public static ReduceInfo abstr(CATransition tabstr, ReduceInfo o, ReduceOp rop) {
		ReduceInfo ret = new ReduceInfo(tabstr, rop);
		
		ret.pred.add(o);
		// TODO: ret.r_label = alevel;
		
		//ret.copySubsumed(o);
		o.succ.add(ret);
		
		return ret;
	}
	public static ReduceInfo closure(CATransition tclosure, ReduceInfo oloop, CAState alevel) {
		ReduceInfo ret = new ReduceInfo(tclosure, ReduceOp.CLOSURE);
		
		ret.pred.add(oloop);
		ret.r_label = alevel;
		oloop.succ.add(ret);
		
		return ret;
	}
	
	public static void linkNewHull(ReduceInfo someold, ReduceInfo onew) {
		
//		// reach the end of the list
//		ReduceInfo p = someold;
//		while (p.next != null)
//			p = p.next;
//		
//		// link a new element to the list
//		p.next = onew;
//		onew.prev = p;
	}
	public static ReduceInfo compose(CATransition t, ReduceInfo o1, ReduceInfo o2, CAState alevel) {
		ReduceInfo ret = new ReduceInfo(t, ReduceOp.COMPOSE);
		
		ret.pred.add(o1);
		ret.pred.add(o2);
		ret.r_label = alevel;
		o1.succ.add(ret);
		o2.succ.add(ret);
		
		if (!o1.t.from().equals(ret.t.from())
				|| !o2.t.to().equals(ret.t.to())) {
			throw new RuntimeException();
		}
		
		return ret;
	}

	public ReduceInfo summary_pred() { return pred.get(0); }
	public static ReduceInfo inline_rename(CATransition t, ReduceInfo o) {
		ReduceInfo ret = new ReduceInfo(t, ReduceOp.INLINE_RENAME);
		ret.pred.add(o);
		o.succ.add(ret);
		return ret;
	}
	public static ReduceInfo inline_plug(CATransition t, ReduceInfo o) {
		ReduceInfo ret = new ReduceInfo(t, ReduceOp.INLINE_PLUG);
		ret.pred.add(o);
		o.succ.add(ret);
		return ret;
	}
	public static ReduceInfo inline_call(CATransition t, ReduceInfo o) {
		ReduceInfo ret = new ReduceInfo(t, ReduceOp.INLINE_CALL);
		ret.pred.add(o);
		o.succ.add(ret);
		return ret;
	}
	public static ReduceInfo inline_return(CATransition t, ReduceInfo o) {
		ReduceInfo ret = new ReduceInfo(t, ReduceOp.INLINE_RETURN);
		ret.pred.add(o);
		o.succ.add(ret);
		return ret;
	}
	// note: summaries don't have the label minimized nor partitions
	public static ReduceInfo summary(CATransition t, ReduceInfo o) {
		ReduceInfo ret = new ReduceInfo(t, ReduceOp.SUMMARY);
		ret.pred.add(o);
		o.succ.add(ret);
		return ret;
	}
	public static ReduceInfo project(CATransition t, ReduceInfo o) {
		ReduceInfo ret = new ReduceInfo(t, ReduceOp.PROJECTED);
		ret.pred.add(o);
		o.succ.add(ret);
		return ret;
	}
	
	public ReduceInfo plugged_summary_calling() { return pred.get(0); }
	public ReduceInfo plugged_summary_called() { return pred.get(1); }
	public static ReduceInfo plugged_summary(CATransition t, ReduceInfo plugging, ReduceInfo plugged) {
		ReduceInfo ret = new ReduceInfo(t, ReduceOp.PLUGGED_SUMMARY);
		ret.pred.add(plugging); // plugging is first
		ret.pred.add(plugged); // plugged is second
		plugging.succ.add(ret);
		plugged.succ.add(ret);
		return ret;
	}
	public static ReduceInfo reconnect(CATransition t, ReduceInfo o) {
		ReduceInfo ret = new ReduceInfo(t, ReduceOp.RECONNECT);
		
		ret.pred.add(o);
		// TODO: ret.r_label = alevel;
		
		//ret.copySubsumed(o);
		o.succ.add(ret);
		
		return ret;
	}

	private void traverse_base(StringBuffer ret, String ss) {
		int i = 1;
		for (ReduceInfo ri : this.pred) {
			if (i != 1)
				ret.append(ss);
			ret.append(ri.traverse());
			i ++;
		}
	}
	public StringBuffer traverse() {
		StringBuffer ret = new StringBuffer();
		switch (this.op) {
		case LEAF:
			ret.append(this.t.name());
			break;
		case COMPOSE:
			traverse_base(ret, ".");
			break;
		case CLOSURE:
			ret.append("(");
			ret.append(this.pred.get(0).traverse());
			ret.append(")*");
			break;
		case ABSTROCT:
		case ABSTRLIN:
			ret.append("@(");
			ret.append(this.pred.get(0).traverse());
			ret.append(")");
			break;
		case HULL:
			traverse_base(ret, "U");
			break;
		case RECONNECT:
			return this.pred.get(0).traverse();
		}
		return ret;
	}
	
	public int depth_base() {
		int max = -1;
		for (ReduceInfo ri : this.pred) {
			int v = ri.depth();
			if (max == -1 || v > max)
				max = v;
		}
		return max;
	}
	public int depth() {
		switch (this.op) {
		case LEAF:
			return 0;
		case COMPOSE:
			return depth_base() + 1;
		case CLOSURE:
			return this.pred.get(0).depth() + 1;
		case ABSTROCT:
		case ABSTRLIN:
			return this.pred.get(0).depth() + 1;
		case HULL:
			return depth_base() + 1;
		case RECONNECT:
			return this.pred.get(0).depth() + 1;
		}
		throw new RuntimeException();
	}
	
	public boolean checkNoCycles(Deque<ReduceInfo> l) {
		if (l.contains(this))
			return false;
		l.addLast(this);
		for (ReduceInfo ri : this.pred) {
			if (!ri.checkNoCycles(l))
				return false;
		}
		l.removeLast();
		return true;
	}
	public boolean checkNoCycles() {
		Deque<ReduceInfo> l = new LinkedList<ReduceInfo>();
		return checkNoCycles(l);
	}
	
	public boolean checkNoCycles1() {
		Set<ReduceInfo> v = new HashSet<ReduceInfo>();
		List<ReduceInfo> l = new LinkedList<ReduceInfo>();
		
		l.add(this);
		while (!l.isEmpty()) {
			ReduceInfo ri = l.remove(0);
			if (v.contains(ri)) {
				return false;
				//throw new RuntimeException("internal error: cycles in a graph");
			}
			v.add(ri);
			l.addAll(ri.pred);
		}
		
		return true;
	}
	
//	public Set<ReduceInfo> reachStar() {
//		return reachStar(new HashSet<CAState>(), new HashSet<CATransition>());
//	}
	
	//public Set<ReduceInfo> reachStar(Set<CAState> starStates, Set<CATransition> endTransitions) {
	public static Set<ReduceInfo> reachStar(Collection<ReduceInfo> startFrom, Set<CAState> starStates, Set<CATransition> endTransitions) {
		Set<ReduceInfo> reach = new HashSet<ReduceInfo>();
		List<ReduceInfo> todo = new LinkedList<ReduceInfo>(startFrom);
		
		while (!todo.isEmpty()) {
			ReduceInfo ri = todo.remove(0);
			
			if (!reach.contains(ri)) {
				reach.add(ri);
				
				if (ri.succ.isEmpty()) {
					endTransitions.add(ri.t);
				} else {
					if (ri.shortcut_up == null) {
						todo.addAll(ri.succ);
					} else {
						todo.addAll(ri.shortcut_up.out);
						
						if (!ri.t.from().equals(ri.t.to()))
							throw new RuntimeException("internal error: refinement");
						
						starStates.add(ri.t.from());
					}
				}
			}
		}
		
		return reach;
	}
	
	// predecessors via "operand-relation" not in reach
	// cuts the operand-relation both-way
	private static void pred_base(Set<ReduceInfo> predborder, Set<ReduceInfo> reach, ReduceInfo r, Collection<ReduceInfo> preds, boolean cut, boolean subsumeRel) {
		Iterator<ReduceInfo> iter = preds.iterator();
		
		while (iter.hasNext()) {
			ReduceInfo pred = iter.next();
			if (!reach.contains(pred)) { // filter only border nodes
				// add to border
				predborder.add(pred);
				
				// cut
				// only border nodes must be removed from the "operand-relation"
				if (cut) {
					
					if (subsumeRel)
						pred.subsumedBy.remove(r);
					else
						pred.succ.remove(r);
					
					iter.remove();
				}
				// cutting shortcuts is never necessary:
				//   spurious hull tsp may be accelerated immediately after, but tsp won't be used (it will be refined)
				//   other transitions on the border cannot be accelerated immediately after, because then the border would be their acceleration (contradiction)
			}
		}
	}

	public static Set<ReduceInfo> pred(Set<ReduceInfo> reach) {
		return predAndCut(reach, false);
	}
	public static Set<ReduceInfo> predAndCut(Set<ReduceInfo> reach) {
		return predAndCut(reach, true);
	}
	
	// parameter -- reach via "operand-relation" (NOT "subsume-relation")
	private static Set<ReduceInfo> predAndCut(Set<ReduceInfo> reach, boolean cut) {
		Set<ReduceInfo> v = new HashSet<ReduceInfo>();
		
		for (ReduceInfo r : reach) {
			
			if (r.shortcut_down == null) {
				pred_base(v, reach, r, r.pred, cut, false);
			} else {
				for (ReduceInfo closure : r.shortcut_down.closures) {
					pred_base(v, reach, closure, closure.pred, cut, false);
				}
				if (cut) {
					for (ReduceInfo loop : r.shortcut_down.in) {
						loop.shortcut_up = null;
					}
				}
			}
			
			pred_base(v, reach, r, r.subsumed, cut, true);
			
			// any reachable node must be removed from the "subsume-relation"
			if (cut) {
				
				// reset 'subsumedBy' pointers pointing to r
				for (ReduceInfo subs : r.subsumed) {
					subs.subsumedBy.remove(r);
				}
				r.subsumed.clear();
				
				// reset 'subsumed' pointers pointing to r
				for (ReduceInfo subs : r.subsumedBy) {
					subs.subsumed.remove(r);
				}
				r.subsumedBy.clear();
			}
		}
		
		return v;
	}
	public static Set<CAState> splittedStates(Set<ReduceInfo> p) {
		Set<CAState> splitted = new HashSet<CAState>();
		
		for (ReduceInfo r : p) {
			if (r.op.equals(ReduceOp.CLOSURE)) {
				if (!r.t.from().equals(r.t.to()))
					throw new RuntimeException("internal error: invalid closure node");
				splitted.add(r.t.from());
			}
		}
		
		return splitted;
	}
	
	public Set<ReduceInfo> reach(boolean fw) {
		Collection<ReduceInfo> tmp = new LinkedList<ReduceInfo>();
		return reach(fw,tmp);
	}
	public static Set<ReduceInfo> reach(boolean fw, Collection<ReduceInfo> init) {
		Set<ReduceInfo> v = new HashSet<ReduceInfo>();
		Set<ReduceInfo> l = new HashSet<ReduceInfo>();
		
		l.addAll(init);
		while (!l.isEmpty()) {
			Iterator<ReduceInfo> it = l.iterator();
			ReduceInfo ri = it.next();
			it.remove();
			
			if (v.add(ri)) {
				v.add(ri);
				l.addAll((fw)? ri.succ : ri.pred);
			}
		}
		
		return v;
	}
	
	public static Set<ReduceInfo> baseOperands(Set<ReduceInfo> v) {
		
		Set<ReduceInfo> ret = new HashSet<ReduceInfo>();
		
		for (ReduceInfo r : v) {
			switch (r.op()) {
			case SUPERNODECL:
			case COMPOSE:
			case CLOSURE:
			case HULL:
				// add all unvisited operands
				for (ReduceInfo r2 : r.pred) {
					if (!v.contains(r2)) {
						ret.add(r2);
					}
				}
				// add all unvisited subsumed
				for (ReduceInfo r2 : r.subsumed) {
					if (!v.contains(r2)) {
						ret.add(r2);
					}
				}
				break;
			}
		}
		
		return ret;
	}
	
	public void cut() {
		
		for (ReduceInfo ri : pred) {
			ri.succ.remove(this);
		}
		pred.clear();
		
		for (ReduceInfo ri : subsumed) {
			ri.subsumedBy.remove(this);
		}
		subsumed.clear();
		
		for (ReduceInfo ri : subsumedBy) {
			ri.subsumed.remove(this);
		}
		subsumedBy.clear();
	}
	
	
	private static Integer getID(Map<ReduceInfo, Integer> map, ReduceInfo ri, Collection<ReduceInfo> red, Collection<ReduceInfo> blue, StringBuffer sb) {
		if (map.containsKey(ri))
			return map.get(ri);
		else {
			Integer i = nextID++;
			map.put(ri, i);
			//String s = (ri.op==ReduceOp.LEAF)? "\\n"+ri.t.from()+"->"+ri.t.to() : "";
			String s = ","+ri.t.id()+"\\n"+ri.t.from()+"->"+ri.t.to();
			String m;
			if (red != null && red.contains(ri))
				m = ", style=filled, fillcolor=red, color=red";
			else if (blue != null && blue.contains(ri))
				m = ", style=filled, fillcolor=yellow, color=yellow";
			else if (ri.op.isLeaf())
				m = ", style=filled, fillcolor=green, color=green";
			else
				m = "";
			sb.append("s"+i+" [label = \""+ri.op + s + "\\n"+(ri.t.calls()?ri.t().call().toString():ri.t.rel().toString())+"\""+m+"];\n");
			return i;
		}
	}
	private static int nextID=1;
	public static StringBuffer toDot(Collection<CATransition> col, Collection<ReduceInfo> red, Collection<ReduceInfo> blue) {
		StringBuffer sb = new StringBuffer();

		int subgraphID = 1;
		
		sb.append("digraph " + "thistory" + " {\n");

		Map<ReduceInfo, Integer> map = new HashMap<ReduceInfo, Integer>();
		nextID=1;
		
		List<ReduceInfo> todo1 = new LinkedList<ReduceInfo>();
		List<ReduceInfo> todo2 = new LinkedList<ReduceInfo>();
		
		for (CATransition t : col)
			todo1.add(t.reduce_info());
		Set<ReduceInfo> processed = new HashSet<ReduceInfo>();
		
		List<ReduceInfo> todo = todo1;
		ShortcutNode sn = null;
		while (!todo1.isEmpty() || !todo2.isEmpty()) {
			
			ReduceInfo ri;
			
			if (!todo2.isEmpty()) {
				
				todo = todo2;
				//sn = null; // sn remains same until the whole closure subgraph is processed
			
			} else {
				
				ri = todo1.get(0);
				
				if (ri.shortcut_up != null) {
					
					sb.append("subgraph cluster"+(subgraphID++)+" {\n");
					todo = todo2;
					sn = ri.shortcut_up;
					
//					// move all nodes with the same shortcut node to todo2
//					Iterator<ReduceInfo> iter = todo1.iterator();
//					while (iter.hasNext()) {
//						ReduceInfo tmp = iter.next();
//						if (tmp.shortcut_up == sn) {
//							iter.remove();
//							todo2.add(tmp);
//						}
//					}
					
					String sin = "";
					for (ReduceInfo i : sn.in) {
						sin = sin + "s"+getID(map, i, red, blue, sb)+" ";
						todo1.remove(i);
						todo2.add(i);
					}
					sb.append("{rank=same; "+sin+" }\n");
					
					String sout = "";
					for (ReduceInfo i : sn.out) {
						sout = sout + "s"+getID(map, i, red, blue, sb)+" ";
						todo1.remove(i);
						todo2.add(i);
					}
						
					sb.append("{rank=same; "+sout+" }\n");
				
				} else {
				
					todo = todo1;
					sn = null;
				
				}
				
			}
			
			ri = todo.remove(0);			
			
			processed.add(ri);
			Integer idFrom = getID(map, ri, red, blue, sb);
			for (ReduceInfo ri2 : ri.succ) {
				//List<ReduceInfo> todoRelev;
				//if (sn != null && !todo2.contains(ri2) && (ri2.shortcut_up == null || ri2.shortcut_up == sn))
				if (!processed.contains(ri2)) {
					//if (sn != null && !todo2.contains(ri2) && !ri2.op.isReconnect())
					if (ri2.useful) {
						if (!todo.contains(ri2))
							todo.add(ri2);
					} else if (!todo1.contains(ri2))
						todo1.add(ri2);
				}
				
				Integer idTo = getID(map, ri2, red, blue, sb);
				
				String m = (red != null && blue != null && red.contains(ri) && red.contains(ri2))? "color=red" : "";
				sb.append("s"+idFrom + " -> " + "s"+idTo
						+ " [ "+m+" ];\n");
					
			}
			for (ReduceInfo ri2 : ri.subsumed) {
				//if (sn != null && !todo2.contains(ri2) && (ri2.shortcut_up == null || ri2.shortcut_up == sn))
				if (!processed.contains(ri2)) {
					//if (sn != null && !todo2.contains(ri2) &&  !ri2.op.isReconnect())
					if (ri2.useful) {
						if (!todo.contains(ri2))
							todo.add(ri2);
					} else if (!todo1.contains(ri2))
						todo1.add(ri2);
				}
				
				Integer idTo = getID(map, ri2, red, blue, sb);
				sb.append("s"+idTo + " -> " + "s"+idFrom
						+ " [ style=dashed ];\n");
			}
			
//			if (ri.op.isHull()) {
//				String stmp = "";
//				for (ReduceInfo i : ri.pred)
//					stmp += "s"+getID(map, i, red, blue, sb)+" ";
//				sb.append("{rank=same; "+stmp+" }\n");
//			}
			
			// todo == todo2 should evaluate same as sn != null
			if (sn != null && todo.isEmpty()) { // processing closure in the priority mode and the last node of the subgraph has been just processed
				sb.append("}\n");
			}
			
		}
		sb.append("}\n");
		return sb;		
	}
	
}