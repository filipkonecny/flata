package verimag.flata.automata.cg;


import java.util.*;

import verimag.flata.automata.*;
import verimag.flata.automata.ca.*;
import verimag.flata.presburger.*;

public class CGNode extends BaseNode {
	
	private Set<CGArc> called = new HashSet<CGArc>();
	private Set<CGArc> calling = new HashSet<CGArc>();
	
	private CA procedure;
	private boolean defined = false;
	private String name;
	
	private Summary summary;
	
	public void summary(Summary aSummary) { summary = aSummary; }
	public Summary summary() { return summary; }
	
	public boolean defined() { return defined; }
	public void setDefined(VariablePool vp) {
		procedure = new CA(name, vp);
		
		defined = true;
	}
	
	public String name() { return procedure.name(); }
	public CA procedure() { return procedure; }
	public String toString() { return name(); }
	//public CGNode(CA aProc) { procedure = aProc; }
	public CGNode(String aName) {
		procedure = null;
		name = aName;
		//procedure = new CA(aName);
	}
	public CGNode(CA aCA) {
		procedure = aCA;
		name = aCA.name();
		defined = true;
	}
	
	@Override
	protected boolean addIncoming_internal(BaseArc aArc) { return calling.add((CGArc)aArc); }
	@Override
	protected boolean addOutgoing_internal(BaseArc aArc) { return called.add((CGArc)aArc); }
	
	@Override
	public Collection<? extends BaseArc> incoming() { return calling; }
	@Override
	public Collection<? extends BaseArc> outgoing() { return called; }

	@Override
	protected boolean removeIncoming_internal(BaseArc aArc)  { return calling.remove((CGArc)aArc); }
	@Override
	protected boolean removeOutgoing_internal(BaseArc aArc)  { return called.remove((CGArc)aArc); }

}
