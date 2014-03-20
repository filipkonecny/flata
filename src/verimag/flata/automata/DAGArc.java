package verimag.flata.automata;

public class DAGArc extends BaseArc {

	private DAGNode from;
	private DAGNode to;
	
	@Override
	public DAGNode from() { return from; }
	@Override
	public DAGNode to() { return to; }
	
	public DAGArc(DAGNode aFrom, DAGNode aTo) {
		from  = aFrom; to = aTo;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof BaseArc))
			return false;
		DAGArc other = (DAGArc)o;
		return from.equals(other.from) && to.equals(other.to);
	}
	
	public int hashCode() {
		return from.hashCode() + to.hashCode();
	}
	
}
