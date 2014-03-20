package verimag.flata.presburger;

import java.io.StringWriter;
import java.util.*;

import verimag.flata.common.CR;
import verimag.flata.common.IndentedWriter;

public class VariablePool {
	
	private String name;

	// all visible variables (local + global)
	private Map<String, VariableInfo> variables = new HashMap<String, VariableInfo>();
	
	private Set<Variable> primedVariables = new HashSet<Variable>();
	private Set<Variable> unprimedVariables = new HashSet<Variable>();
	private Variable[] primedVariables_arr, unprimedVariables_arr;
	
	private Variable[] globalVariables, localVariables; // unprimed
	private Variable[] globalVariablesPr, localVariablesPr; // primed
	
	private Variable[] portIn = new Variable[0]; // unprimed
	private Variable[] portOut = new Variable[0]; // primed
	
	public Variable[] portIn() { return portIn; }
	public Variable[] portOut() { return portOut; }
	
	public Variable[] localUnp() { return localVariables; }
	public Variable[] globalUnp() { return globalVariables; }
	public Variable[] allUnp() { return unprimedVariables_arr; }
	public Collection<Variable> allUnpCol() { return unprimedVariables; }
	public Collection<Variable> allPrimedCol() { return primedVariables; }
	
	public int portInSize() { return portIn.length; }
	public int portOutSize() { return portOut.length; }
	
	public boolean DECLARE_LOCALS = true;
	
	
	public boolean isDeclared(String aName) {
		return variables.containsKey(aName);
	}
	
	// -----------------------------printing methods---------------------------------- //
	public String toString() {
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, "  ");
		print(iw);
		return sw.toString();
	}
	public void print(IndentedWriter iw) {
		iw.writeln("variable-pool "+name+" {");
		iw.indentInc();
		iw.writeln("global {"+ CR.arr2sb(globalVariables) +"}");
		iw.writeln("local {"+ CR.arr2sb(localVariables) +"}");
		iw.writeln("in {"+ CR.arr2sb(portIn) +"}");
		iw.writeln("out {"+ CR.arr2sb(portOut) +"}");
		iw.indentDec();
		iw.writeln("}");
	}
	
	public Variable[] localUnp_notInOut() {
		Set<Variable> aux = new HashSet<Variable>();
		for (Variable v : localUnp()) {
			aux.add(v);
		}
		for (Variable v : portIn) {
			aux.remove(v);
		}
		for (Variable v : portOut) {
			aux.remove(v.getCounterpart());
		}
		return aux.toArray(new Variable[0]);
	}
	
	// -----------------------------get methods---------------------------------- //
	private VariableInfo getVariableInfo(String s) {
		return variables.get(s);
	}
	private Variable getVariable(String s) {
		return giveVariableInfo(s).getVariable();
		//return variables.get(s).getVariable();
	}
	public Variable getCounterpart(Variable v) {
		return variables.get(v.name).getCounterpart();
	}
	public Variable getIntermediate(Variable v) {
		return variables.get(v.name).getIntermediate();
	}
	public Variable getUnprimed(Variable v) { 
		if (v.isPrimed())
			return variables.get(v.name).getCounterpart();
		else
			return v;
	}
	public Variable getPrimed(Variable v) { 
		if (v.isPrimed())
			return v;
		else
			return variables.get(v.name).getCounterpart();
	}
	public Set<String> variableNames() {
		return new HashSet<String>(variables.keySet());
	}
	
	public Variable[] localUnpZeroDepth() {
		List<Variable> aux = new LinkedList<Variable>();
		for (Variable v : unprimedVariables_arr) {
			VariableInfo vi = this.getVariableInfo(v.name);
			if (vi.depth() == 0)
				aux.add(v);
		}
		return aux.toArray(Variable.azl);
	}
	
	
	public static VariablePool createEmptyPool() {
		return new VariablePool();
	}
	
	// ensure sharing of global variables
	// constructor public VariablePool(VariablePool other); // use other to retrieve globals
	
	private VariablePool() {}
	// creates a pool and declares globals in it
	public static VariablePool createGPool(List<String> aGlobals) {
		VariablePool vp = new VariablePool();
		vp.declareVarGlobal(aGlobals);
		vp.inferPandUnpArrays();
		return vp;
	}
	
	private void inferPandUnpArrays() {
		unprimedVariables_arr = unprimedVariables.toArray(Variable.azl);
		Arrays.sort(unprimedVariables_arr);
		
		primedVariables_arr = primedVariables.toArray(Variable.azl);
		Arrays.sort(primedVariables_arr);
	}
	// creates a pool, copies globals from the global pool, and declares locals in it 
	public static VariablePool createGLPool(VariablePool globalPool, List<String> aLocals, String aName) {
		VariablePool vp = new VariablePool();
		
		vp.declareGlobalsByCopy(globalPool);
		
		vp.name = aName;
		
		// still, need to declare locals
		vp.declareVarLocal(aLocals);
		
		// finally, infer arrays
		vp.inferPandUnpArrays();
		
		return vp;
	}
	public void inlinePool(VariablePool inlined) {
		// copy all locals, just increase depth (i.e. deep copy of VariableInfo)

		// fill the fields: variables, (un)primedVariables
		for (Variable v : inlined.localVariables) {
			VariableInfo viOld = inlined.variables.get(v.name());
			VariableInfo viOldD = inlined.variables.get(viOld.getCounterpart().name());
			
			VariableInfo vi = VariableInfo.createInlineNew(viOld);
			VariableInfo viD = VariableInfo.createInlineDual(vi, viOldD);
			
			this.variables.put(vi.getVariable().name(), vi);
			this.variables.put(viD.getVariable().name(), viD);
			
			this.unprimedVariables.add(vi.getVariable());
			this.primedVariables.add(vi.getCounterpart());
		}
		
		// extend localVariable(Pr)		
		int l = this.localVariables.length;
		int li = inlined.localVariables.length;
		
		this.localVariables = Arrays.copyOf(this.localVariables, l + li);
		System.arraycopy(inlined.localVariables, 0, this.localVariables, l, li);
		Arrays.sort(this.localVariables);
		
		this.localVariablesPr = this.deriveCounterpart(this.localVariables);
		
		// extend (un)primedVariables_arr
		l = unprimedVariables_arr.length;
		
		this.unprimedVariables_arr = Arrays.copyOf(this.unprimedVariables_arr, l + li);
		System.arraycopy(inlined.localVariables, 0, this.unprimedVariables_arr, l, li);
		Arrays.sort(this.unprimedVariables_arr);
		
		this.primedVariables_arr = Arrays.copyOf(this.primedVariables_arr, l + li);
		System.arraycopy(inlined.localVariablesPr, 0, this.primedVariables_arr, l, li);
		Arrays.sort(this.primedVariables_arr);
	}
	
	
	private static void copy(VariablePool vp, VariablePool vpOld, Variable[] arr, List<Variable> aux) {
		for (Variable v : arr) {
			if (vp.declareUndeclared(v.name, vpOld))
				aux.add(v);
		}
	}
	// product requires to take a union of all
	public static VariablePool product(VariablePool vp1, VariablePool vp2) {
		VariablePool vp = new VariablePool();
		
		// shallow copy for the first, then add missing from the second
		List<Variable> aux = new LinkedList<Variable>();
		VariablePool.copy(vp, vp1, vp1.localVariables, aux);
		VariablePool.copy(vp, vp2, vp2.localVariables, aux);
		vp.localVariables = aux.toArray(Variable.azl);
		vp.localVariablesPr = vp.deriveCounterpart(vp.localVariables);
		
		aux.clear();
		VariablePool.copy(vp, vp1, vp1.globalVariables, aux);
		VariablePool.copy(vp, vp2, vp2.globalVariables, aux);
		vp.globalVariables = aux.toArray(Variable.azl);
		vp.globalVariablesPr = vp.deriveCounterpart(vp.globalVariables);
		
		vp.inferPandUnpArrays();
		
		// TODO: do something about ports
		
		return vp;
	}
	
	// inlining: infer from one pool another pool (i.e. rename locals of depth 0, increase depth of all inlined local variables)
	// e.g. method: public void update4inline(VariablePool inlinedPool, RenameM ren);	
	
	// renaming -> propagate new pool to constructors of classes of the Presburger package
	
	private Variable[] deriveCounterpart(Variable[] from) {
		int l = from.length;
		Variable[] ret = new Variable[l];
		for (int i=0; i<l; i++) {
			ret[i] = variables.get(from[i].name()).getCounterpart();
		}
		return ret;
	}
	private Variable[] renameAndDeclare(VariablePool other, Rename ren, Variable[] arr) {
		int l = arr.length;
		Variable[] ret = new Variable[l];
		for (int i=0; i<l; i++) {
			Variable v = arr[i];
			ret[i] = this.declareVarCopyOrRename(v.name(), other, ren);
		}
		return ret;
	}
	private Variable[] renameDeclared(Rename ren, Variable[] arr) {
		int l = arr.length;
		Variable[] ret = new Variable[l];
		for (int i=0; i<l; i++) {
			Variable v = arr[i];
			String s = ren.getNewNameFor(v.name());
			if (s == null)
				s = v.name();
			ret[i] = this.getVariable(s);
		}
		return ret;
	}
	
	// renaming constructor -- rename specified local variables of depth 0
	// creates a pool where all variables will be renamed
	// (according to Rename argument -- it some name is unspecified for renaming, it is kept)
	public static VariablePool rename(VariablePool other, Rename ren) {
		VariablePool vp = new VariablePool();
		
		vp.globalVariables = vp.renameAndDeclare(other, ren, other.globalVariables);
		vp.localVariables = vp.renameAndDeclare(other, ren, other.localVariables);
		
		vp.portIn = vp.renameDeclared(ren, other.portIn);
		vp.portIn = vp.renameDeclared(ren, other.portIn);
		
		Arrays.sort(vp.globalVariables);
		Arrays.sort(vp.localVariables);
		
		vp.globalVariablesPr = vp.deriveCounterpart(vp.globalVariables);
		vp.localVariablesPr = vp.deriveCounterpart(vp.localVariables);
		
		return vp;
	}
	
	
	// -----------------------------declaration methods---------------------------------- //
	public void declarePortIn(List<String> l) {
		this.portIn = new Variable[l.size()];
		int i = 0;
		for (String s : l) {
			Variable v = this.giveVariable(s);
			if (v.isPrimed()) {
				System.err.println("Input parameters must be without prime");
				System.exit(-1);
			}
			if (Arrays.binarySearch(this.globalVariables, v) >= 0) {
				System.err.println("Declared procedure parameters must not be global.");
				System.exit(-1);
			}
			this.portIn[i] = v;
			i++;
		}
	}
	public void declarePortOut(List<String> l) {
		this.portOut = new Variable[l.size()];
		int i = 0;
		for (String s : l) {
			Variable v = this.giveVariable(s);
			if (!v.isPrimed()) {
				System.err.println("Output parameters must be specified with a prime");
				System.exit(-1);
			}
			
			Variable vunp = variables.get(v.name()).getCounterpart();
			
			if (Arrays.binarySearch(this.globalVariables, vunp) >= 0) {
				System.err.println("Declared procedure parameters must not be global.");
				System.exit(-1);
			}
			
			this.portOut[i] = v;
			i++;
		}
	}
	
	private static void checkUnprimed(Variable v) {
		if (v.isPrimed()) {
			System.err.println("Declaration of local and global counters has to be without primes. Use '"+v.getCounterpart()+"' instead of "+v+"");
			System.exit(-1);
		}
	}
	
	private void declareVarLocal(List<String> l) {
		localVariables = declareVar(l);
		localVariablesPr = new Variable[localVariables.length];
		localVariablesPr = deriveCounterpart(localVariables);
	}
	private void declareVarGlobal(List<String> l) {
		globalVariables = declareVar(l);
		globalVariablesPr = new Variable[globalVariables.length];
		globalVariablesPr = deriveCounterpart(globalVariables);
	}
	private Variable[] declareVar(List<String> l) {
		Variable[] vvv = new Variable[l.size()];
		int i=0;
		for (String s : l) {
			Variable v = declareVar(s);
			checkUnprimed(v);
			vvv[i] = v;
			i++;
		}
		Arrays.sort(vvv);
		return vvv;
	}
	
	// fills the fields: variable, (un)primedVariable
	private Variable declareVar(String aName) {

		if (variables.containsKey(aName)) {
			System.err.println("Multiple declaration of counter '"+aName+"'");
			System.exit(-1);
			return variables.get(aName).getVariable();
		} else {
			VariableInfo vi = VariableInfo.createNew(aName, this);
			VariableInfo vid = VariableInfo.createNewDual(vi);
			variables.put(aName, vi);
			variables.put(vid.getVariable().name(), vid);
			
			unprimedVariables.add(vi.getUnprimed());
			primedVariables.add(vi.getPrimed());
			
			return vi.getVariable();
		}
	}
	
	// fills the fields: variables, (un)primedVariables
	// declare for either unprimed or primed only (the other are declared automatically)
	private Variable declareVarCopyOrRename(String aName, VariablePool other, Rename ren) {
		
		String rName = ren.getNewNameFor(aName);
		
		if (variables.containsKey(rName)) {
			throw new RuntimeException("Multiple declaration of counter '"+aName+"'");
		} else {
			
			VariableInfo vi = other.getVariableInfo(aName);
			
			if (rName == null || rName.equals(aName)) { // shallow copy of VariableInfos
				
				
				String cName = vi.getCounterpart().name;
				VariableInfo vic = other.getVariableInfo(cName);
				
				variables.put(aName, vi);
				variables.put(cName, vic);
				
			} else {
				
				// set correctly the new variable, its counterpart and intermediate variable
				VariableInfo viOld = vi;
				VariableInfo viOldD = other.getVariableInfo(viOld.getCounterpart().name); // dual
				
				vi = VariableInfo.createRename(rName, viOld, this);
				VariableInfo viD = VariableInfo.createRenameDual(vi, viOldD); // dual
				
				variables.put(vi.getVariable().name(), vi);
				variables.put(viD.getVariable().name(), viD);
				
			}
			
			unprimedVariables.add(vi.getUnprimed());
			primedVariables.add(vi.getPrimed());
			
			return vi.getVariable();
		}
	}
	
	// fills the fields: variables, (un)primedVariables
	// declare for either unprimed or primed only (the other are declared automatically)
	private boolean declareUndeclared(String aName, VariablePool other) {
		
		if (variables.containsKey(aName)) {
			return false;
		} else {
			
			VariableInfo vi = other.getVariableInfo(aName);
			String cName = vi.getCounterpart().name;
			VariableInfo vic = other.getVariableInfo(cName);
			
			variables.put(aName, vi);
			variables.put(cName, vic);
			
			unprimedVariables.add(vi.getUnprimed());
			primedVariables.add(vi.getPrimed());
			
			return true;
		}
	}
	
	// assume that other VariablePool contains only globals and copy them
	// shallow copies of VariableInfo and Variable
	private void declareGlobalsByCopy(VariablePool other) {
		this.variables = new HashMap<String,VariableInfo>(other.variables);
		this.primedVariables = new HashSet<Variable>(other.primedVariables);
		this.unprimedVariables = new HashSet<Variable>(other.unprimedVariables);
		
		if (other.globalVariables != null) {
			this.globalVariables = Arrays.copyOf(other.globalVariables, other.globalVariables.length);
			this.globalVariablesPr = Arrays.copyOf(other.globalVariablesPr, other.globalVariablesPr.length);
		}
		
		// check
		for (VariableInfo vi : this.variables.values()) {
			Variable v = vi.getVariable();
			if (v.isPrimed()) {
				if (Arrays.binarySearch(this.globalVariablesPr, v) < 0)
					throw new RuntimeException("internal error: some declared variables are not globals");
			} else {
				if (Arrays.binarySearch(this.globalVariables, v) < 0)
					throw new RuntimeException("internal error: some declared variables are not globals");
			}
		}
	}
	
	
	public Variable giveVariable(String aName) {
		
		// assumption: global variables are declared immediately after creation of a CA
		
		if (!variables.containsKey(aName)) {
			if (DECLARE_LOCALS) {
				System.err.println("automaton '"+this.name+"': undeclared variable \"" + (VariablePool.createSpecial(aName)).getUnprimedName()+"\""); // TODO solve using exception
				System.exit(-1);
				return null;
			} else {
				Variable v = declareVar(aName);
				localVariables = Arrays.copyOf(localVariables, localVariables.length+1);
				localVariables[localVariables.length-1] = v;
				Arrays.sort(localVariables);
				
				localVariablesPr = deriveCounterpart(localVariables);
				inferPandUnpArrays();
				
				return v;
			}
		} else {
			return variables.get(aName).getVariable();
		}
	}
	
	// methods required by SIL
	public VariableInfo giveVariableInfo(String aName) {
		VariableInfo ret = this.variables.get(aName);
		
		if (ret == null) {
			this.giveVariable(aName);
			ret = this.variables.get(aName);
		}
		
		return ret;
	}
	public boolean containsVariable(String aName) {
		return this.variables.get(aName) != null;
	}
	
	
	
	// primed locals which are not output parameters of the call
	public Variable[] unassignedLocalsAsUnp(List<Variable> argsOut) { // TODO: don't use call in this package
		// problem: argsOut is not sorted		
		
		List<Variable> aux = new LinkedList<Variable>();
		for (Variable v : localVariables)
			aux.add(v);
		for (Variable v : argsOut)
			aux.remove(v.getCounterpart());
		return aux.toArray(new Variable[0]);
	}
	public Substitution renameForCalling(List<LinearConstr> args) { // TODO: don't use call in this package
		Substitution ret = new Substitution();
		for (int i=0; i<portIn.length; i++) {
			ret.put(portIn[i], args.get(i));
		}
		int l = portIn.length;
		for (int i=0; i<portOut.length; i++) {
			ret.put(portOut[i], args.get(i+l));
		}
		return ret;
	}
	private List<Variable> localNotInput() {
		List<Variable> aux = new LinkedList<Variable>();
		for (Variable v : localVariables)
			aux.add(v);
		for (Variable v : portIn)
			aux.remove(v);
		return aux;
	}
	private Set<Variable> localPrNotOutput() {
		Set<Variable> aux = new HashSet<Variable>();
		for (Variable v : localVariablesPr)
			aux.add(v);
		for (Variable v : portOut)
			aux.remove(v);
		return aux;
	}
	public List<Variable> interprocInvisible(boolean forFinalStates) {
		List<Variable> ret = new LinkedList<Variable>();

		// local unprimed which are not input
		
		ret.addAll(localNotInput());
		
		// problem: portIn is not sorted
		
		if (forFinalStates) {
			// local primed which are not output
			ret.addAll(localPrNotOutput());
		} else {
			for (Variable v : localVariablesPr)
				ret.add(v);
		}
		
		return ret;
	}

	// -------------------  static pool for variables which don't belong anywhere: ------------------------------ //
	// --------------   e.g.  $ for DBM, x+ and x- for octagons, $k for elimination etc. ) -----------------------//
	private static VariablePool special = new VariablePool();
	public static VariablePool getSpecialPool() { return special; }
	public static Variable createSpecial(String aName) {
		if (special.isDeclared(aName))
			return special.getVariable(aName);
		else {
			return special.declareVar(aName);
		}
	}
	public static VariablePool createEmptyPoolNoDeclar() {
		VariablePool ret = new VariablePool();
		ret.DECLARE_LOCALS = false;
		
		ret.primedVariables_arr = new Variable[0];
		ret.unprimedVariables_arr = new Variable[0];
		
		ret.globalVariables = new Variable[0];
		ret.localVariables = new Variable[0];
		ret.globalVariablesPr = new Variable[0];
		ret.localVariablesPr = new Variable[0];
		
		return ret;
	}
	public static StringBuffer asString(Variable[] vars) {
		StringBuffer sb = new StringBuffer();
		int n = vars.length;
		for (int i=0; i<n; i++) {
			sb.append(vars[i]);
			if (i<n-1) sb.append(",");
		}
		return sb;
	}
	
	
}
