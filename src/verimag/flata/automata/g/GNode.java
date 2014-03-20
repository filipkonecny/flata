package verimag.flata.automata.g;

import java.util.*;

import verimag.flata.automata.BaseArc;
import verimag.flata.automata.BaseNode;

public class GNode extends BaseNode {

	private Integer id;
	
	private Set<GArc> in = new HashSet<GArc>();
	private Set<GArc> out = new HashSet<GArc>();
	
	public GNode(Integer aVal) { id = aVal; }
	
	public Integer id() { return id; }
	
	@Override
	public Collection<GArc> incoming() {
		return in;
	}

	@Override
	public Collection<GArc> outgoing() {
		return out;
	}
	
	@Override
	protected boolean addIncoming_internal(BaseArc aArc) {
		boolean b = in.contains(aArc);
		in.add((GArc)aArc);
		return b;
	}

	@Override
	protected boolean removeIncoming_internal(BaseArc aArc) {
		return in.remove(aArc);
	}

	@Override
	protected boolean addOutgoing_internal(BaseArc aArc) {
		boolean b = out.contains(aArc);
		out.add((GArc)aArc);
		return b;
	}

	@Override
	protected boolean removeOutgoing_internal(BaseArc aArc) {
		return out.remove(aArc);
	}

	public void addIncoming_noCheck(BaseArc aArc) {
		in.add((GArc)aArc);
	}
	public void addOutgoing_noCheck(BaseArc aArc) {
		out.add((GArc)aArc);
	}
}
