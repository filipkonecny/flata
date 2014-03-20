package verimag.flata.automata.g;

import java.util.*;

import verimag.flata.automata.BaseArc;
import verimag.flata.automata.BaseGraph;
import verimag.flata.automata.BaseNode;

public class GGraph extends BaseGraph {

	
	private Map<Integer,GNode> m = new HashMap<Integer,GNode>();
	
	private GNode give(Integer val) {
		GNode ret = m.get(val);
		if (ret == null) {
			ret = new GNode(val);
			m.put(val,ret);
		}
		return ret;
	}
	
	public void addNode(Integer aVal) {
		give(aVal);
	}
	public void addNode_newInst(Integer aVal) {
		GNode ret = new GNode(aVal);
		m.put(aVal,ret);
	}
	public void addEge(Integer aFrom, Integer aTo) {
		GNode from = give(aFrom);
		GNode to = give(aTo);
		GArc a = new GArc(from,to);
		from.addOutgoing_noCheck(a);
		to.addIncoming_noCheck(a);
	}
	
	@Override
	public Collection<? extends BaseNode> nodes() {
		return m.values();
	}

	@Override
	public Collection<? extends BaseArc> arcs() {
		Set<GArc> ret = new HashSet<GArc>();
		for (GNode n : m.values()) {
			ret.addAll(n.outgoing());
		}
		return ret;
	}

	@Override
	public Collection<? extends BaseNode> initials() {
		return this.nodes();
	}
	
	public List<List<Integer>> origComponents() {
		List<List<Integer>> ret = new LinkedList<List<Integer>>();
		for (List<BaseNode> l : this.findSccs()){
			List<Integer> lret = new LinkedList<Integer>();
			ret.add(lret);
			for (BaseNode n : l) {
				lret.add(((GNode)n).id());
			}
		}
		return ret;
	}

}
