package verimag.flata.automata.g;

import verimag.flata.automata.BaseArc;
import verimag.flata.automata.BaseNode;

public class GArc extends BaseArc {

	private GNode from, to;
	
	public GArc(GNode aFrom, GNode aTo) {
		from = aFrom;
		to = aTo;
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof GArc))
			return false;
		GArc o = (GArc) other;
		return from.equals(o.from) && to.equals(o.to());
	}
	public int hashCode() {
		return from.hashCode() + to.hashCode();
	}
	
	@Override
	public BaseNode from() {
		return from;
	}

	@Override
	public BaseNode to() {
		return to;
	}

}
