package verimag.flata.automata.cg;

import java.util.*;
import verimag.flata.automata.*;
import verimag.flata.automata.ca.*;

public class CGArc extends BaseArc {

	private CGNode calling;
	private CGNode called;
	
	private List<Call> calls = new LinkedList<Call>(); // list of calls
	
	public CGArc(CGNode aCalling, CGNode aCalled) {
		calling = aCalling;
		called = aCalled;
		
		calling.addOutgoing(this);
		called.addIncoming(this);
	}
	
	public List<Call> calls() { return calls; }
	public void addCall(Call aCall) {
		calls.add(aCall);
	}
	
	@Override
	public BaseNode from() { return calling; }
	@Override
	public BaseNode to() { return called; }

}
