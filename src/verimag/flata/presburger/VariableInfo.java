package verimag.flata.presburger;


public class VariableInfo {

	private Variable var;  // not-primed or primed
	private Variable counterpart; // primed or not-primed
	private Variable intermediate; // doubly-primed
	
	// inlining requires to remember the depth of inlined variables as well as pointers to variables from which they were renamed
	private int depth = 0;
	private VariableInfo origInfo = null;
	private VariableInfo prevInfo = null;
	
	public int depth() { return depth; }

	public Variable getVariable() { return var; }
	public Variable getCounterpart() { return counterpart; }
	public Variable getIntermediate() { return intermediate; }
	
	public Variable getUnprimed() { return var.isPrimed()? counterpart : var; }
	public Variable getPrimed() { return var.isPrimed()? var : counterpart; }
	
	private VariableInfo(Variable aVar, Variable aCounterpart, Variable aIntermediate) {
		super();
		var = aVar;
		counterpart = aCounterpart;
		intermediate = aIntermediate;
	}
	// rename (no depth increase)
	private VariableInfo(Variable aVar, Variable aCounterpart, Variable aIntermediate, VariableInfo aPrevInfo) {
		this(aVar, aCounterpart, aIntermediate);
		prevInfo = aPrevInfo;
		if (aPrevInfo.origInfo == null)
			origInfo = aPrevInfo;
		else
			origInfo = aPrevInfo.origInfo;
		depth = aPrevInfo.depth;
	}
	
	public String toString() {
		return "("+var+","+counterpart+","+intermediate+")";
	}
	
	// ------------------ create new from scratch ------------------------------- //
	public static VariableInfo createNew(String aName, VariablePool aVP) {
		Variable new_variable = Variable.createNew(aName, aVP);
		Variable counterpart = Variable.createCounterpart(new_variable);
		Variable intermediate = Variable.createIntermediate(new_variable);
		return new VariableInfo(new_variable, counterpart, intermediate);
	}
	public static VariableInfo createNewDual(VariableInfo vi) {
		return new VariableInfo(vi.counterpart, vi.var, vi.intermediate);
	}
	// ------------------ create new by renaming ------------------------------- //
	public static VariableInfo createRename(String newName, VariableInfo oldInfo, VariablePool aVP) {
		Variable newV = Variable.createNew(newName, aVP);
		Variable newVc = Variable.createCounterpart(newV);
		Variable newVi = Variable.createIntermediate(newV);
		
		return new VariableInfo(newV,newVc,newVi, oldInfo);
	}
	public static VariableInfo createRenameDual(VariableInfo vi, VariableInfo oldInfo) {
		return new VariableInfo(vi.counterpart,vi.var,vi.intermediate, oldInfo);
	}
	// ------------------ create new by inlining ------------------------------- //
	public static VariableInfo createInlineNew(VariableInfo oldInfo) {
		VariableInfo ret = new VariableInfo(oldInfo.var,oldInfo.counterpart,oldInfo.intermediate, oldInfo);
		ret.depth++;
		return ret;
	}
	public static VariableInfo createInlineDual(VariableInfo vi, VariableInfo oldInfo) {
		VariableInfo ret =  new VariableInfo(vi.counterpart,vi.var,vi.intermediate, oldInfo);
		ret.depth++;
		return ret;
	}
}
