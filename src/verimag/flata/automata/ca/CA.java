package verimag.flata.automata.ca;

import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import verimag.flata.View;
import verimag.flata.automata.*;
import verimag.flata.common.*;
import verimag.flata.presburger.*;

/**
 * class representing counter automata (CAs).
 * 
 */
public class CA extends BaseGraph {
	
	public static boolean HULL4ACCELERATION = false;
	
	public static int caInx = 0;
	private static boolean debug = false;
	// type of used collections - use for debugging only
	private static final boolean sortedCollections = false;
	
	public static final int fFINAL = 0x01;
	public static final int fINIT = 0x02;
	public static final int fERROR = 0x04;
	
	public static boolean TERM = false;
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	// name of the CA
	private String name = null;
	
	private Set<CAState> states;
	private Set<CAState> initialStates;
	private Set<CAState> finalStates;
	private Set<CAState> errorStates;

	private Map<Integer, CATransition> transitions;
	
	private VariablePool vp;

	// backup 
	private Set<CAState> bu_initialStates;
	private Set<CAState> bu_finalStates;
	//private Set<CAState> bu_states;
	private Map<Integer, CATransition> bu_transitions;
	
	
	// reduction / refinement fields
	private Collection<CATransition> origTransitions = new LinkedList<CATransition>();
	private Collection<CAState> origStates = new LinkedList<CAState>();
	private Map<CAState,CAState> msplit_in = new HashMap<CAState,CAState>();
	private Map<CAState,CAState> msplit_out = new HashMap<CAState,CAState>();
	

	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	public VariablePool varPool() { return vp; }
	
	public String name() { return name;	}
	public void name(String aName) { name = aName; }
	
	public Set<CAState> states() { return states; }

	public Collection<CATransition> transitions() {
		return transitions.values();
	}

	public Set<CAState> initialStates() { return initialStates;	}
	public Set<CAState> finalStates() {	return finalStates;	}
	public Set<CAState> errorStates() {	return errorStates;	}
	
	public boolean isInitial(CAState s) { return initialStates.contains(s); }
	public boolean isFinal(CAState s) { return finalStates.contains(s);	}
	public boolean isError(CAState s) { return errorStates.contains(s);	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	/////////////// IMPLEMENTATION OF ABSTRACT METHODS /////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	public Set<CAState> nodes() { return states(); }
	public Collection<CATransition> arcs() { return transitions(); }
	public Set<CAState> initials() { return initialStates(); }
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	public Variable[] portIn() { return vp.portIn(); }
	public Variable[] portOut() { return vp.portOut(); }
	
	public Variable[] localUnp() { return vp.localUnp(); }
	public Variable[] globalUnp() { return vp.globalUnp(); }
	
	public int portInSize() { return vp.portInSize(); }
	public int portOutSize() { return vp.portOutSize(); }
	
	public Variable giveVariable(String aName) { return vp.giveVariable(aName); }
	
	public Variable[] localUnpZeroDepth() { return vp.localUnpZeroDepth(); }
	
	public Substitution renameForCalling(Call call) {
		return vp.renameForCalling(call.arguments());
	}
	public Variable[] unassignedLocalsAsUnp(Call call) {
		return vp.unassignedLocalsAsUnp(call.argsOut());
	}
	
	public Set<String> variableNames() {
		return vp.variableNames();
	}
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	public void bu_copy() {
		bu_initialStates = new HashSet<CAState>(initialStates);
		bu_finalStates = new HashSet<CAState>(finalStates);
		//bu_states = new HashSet<CAState>(states);
		bu_transitions  = new HashMap<Integer, CATransition>(transitions);
	}
	private void bu() {
		bu_initialStates = new HashSet<CAState>(initialStates);
		bu_finalStates = new HashSet<CAState>(finalStates);
		//bu_states = new HashSet<CAState>(states);
		bu_transitions  = new HashMap<Integer, CATransition>(transitions);
	}
	private void bu_transitions(Collection<CATransition> col) {
		bu_transitions  = new HashMap<Integer, CATransition>();
		for (CATransition t : col) {
			bu_transitions.put(t.id(), t);
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	public void setOrigTrans(Collection<CATransition> col) {
		origTransitions.addAll(col);
	}
	public void setOrigStates() {
		this.origStates = new LinkedList<CAState>(this.states);
	}
	public Collection<CATransition> origTransitions() { return origTransitions; }
	
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	

	public CA() {
		this("a" + caInx, null);
	}

	public CA(String aName, VariablePool aVP) {
		caInx++;
		name = aName;
		if (CA.sortedCollections) {
			initialStates = new TreeSet<CAState>();
			finalStates = new TreeSet<CAState>();
			errorStates = new TreeSet<CAState>();
			states = new TreeSet<CAState>();
			transitions = new TreeMap<Integer, CATransition>();
			//existentialParameters = new TreeSet<Variable>();
		} else {
			initialStates = new HashSet<CAState>();
			finalStates = new HashSet<CAState>();
			errorStates = new HashSet<CAState>();
			states = new HashSet<CAState>();
			transitions = new HashMap<Integer, CATransition>();
			//existentialParameters = new HashSet<Variable>();
		}
		
		vp = aVP;
	}

	private int stateFlags(CAState s) {
		int flags = 0;
		if (isInitial(s))
			flags |= CA.fINIT;
		if (isFinal(s))
			flags |= CA.fFINAL;
		if (isError(s))
			flags |= CA.fERROR;
		return flags;
	}
	

	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////


	public boolean isEmpty() {
		return this.transitions.size() == 0;
	}
	
	// generation of labels of states
	private int nextStateGenerator = 1;

	public boolean isUsedTransName(String aName) {
		for (CATransition t : this.transitions.values()) {
			if (t.name() == null)
				continue;
			if (t.name().equals(aName))
				return true;
		}
		return false;
	}
	
	private boolean hasStateNamedAs(String name) {
		for (CAState s : states) {
			if (s.name().equals(name))
				return true;
		}
		return false;
		
	}
	
	// gives unique name with specified prefix
	public String giveNextStateLabelWithPrefix(String aPrefix) {
		if (!hasStateNamedAs(aPrefix))
			return aPrefix;
		while (hasStateNamedAs(aPrefix + nextStateGenerator))
			nextStateGenerator++;
		return aPrefix + nextStateGenerator++;
	}

	// generation of labels of transitions
	private int nextTransitionGenerator = 1;
	
	// gives unique label
	public String giveNextTransitionLabel() {
		String pref = "t";
		String s = pref + "_" + nextTransitionGenerator;
		while (isUsedTransName(s))
			nextTransitionGenerator ++;
		
		nextTransitionGenerator ++;
		return s;
	}

	// gives unique label with specified prefix
	public String giveNextTransitionLabelWithPrefix(String pref) {
		while (!isUsedTransName(pref))
			return pref;
		String s = pref + "_" + nextTransitionGenerator;
		while (isUsedTransName(s))
			nextTransitionGenerator ++;
		
		nextTransitionGenerator ++;
		return s;
	}

	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////  SUPPORT FOR HIERARCHICAL AUTOMATA   //////////////////////////
	////////////////////////////////////////////////////////////////////////////////


	public List<CATransition> calls() {
		List<CATransition> ret = new LinkedList<CATransition>();
		for (CATransition t : transitions.values())
			if (t.calls())
				ret.add(t);
		return ret;
	}

	
	private void copyStates(CA aCA) {
		copyStates(aCA, null);
	}
	private void copyStates(CA aCA, Rename r) {
		for (CAState s : aCA.states) {
			int flags = 0;
			if (aCA.isInitial(s))
				flags |= CA.fINIT;
			if (aCA.isFinal(s))
				flags |= CA.fFINAL;
			if (aCA.isError(s))
				flags |= CA.fERROR;
			String ss = r==null? s.name() : r.getNewNameFor(s.name());
			this.addState(ss, flags);
		}
	}

	
	public CAState giveSomeErrorState() {
		if (!errorStates().isEmpty()) {
			return errorStates().iterator().next();
		} else {
			String name = this.giveNextStateLabelWithPrefix("err");
			return getStateWithName(name, CA.fERROR);
		}
	}
	
	public void fillVarID(Rename r) {
		for (String s : this.variableNames()) {
			if (r.getNewNameFor(s) == null)
				r.put(s, s);
		}
	}
	
	
	public static CA copy(CA aCA) {
		
		// variables
		VariablePool vp_new = aCA.vp;
		
		CA ret = new CA(aCA.name, vp_new);
		
		// control states
		ret.copyStates(aCA);
		
		// transitions
		for (CATransition t : aCA.transitions()) {
			
			CAState cp_from = ret.getStateWithName(t.from().name());
			CAState cp_to = ret.getStateWithName(t.to().name());
			CompositeRel cp_rel = ((CompositeRel)t.label()).copy();
			//public CATransition(CAState aFrom, CAState aTo, Label aLabel, CA aCA) {
			CATransition tCopy = new CATransition(cp_from, cp_to,cp_rel, ret);
			
			ret.addTransition_base(tCopy);
		}
		
		// other members
		ret.nextStateGenerator = aCA.nextStateGenerator;
		ret.nextTransitionGenerator = aCA.nextTransitionGenerator;
		
		return ret;
	}
	// copy with renamed variables and control states
	public static CA rename(CA aCM, Rename aRV) {
		
		// variables
		VariablePool vp_new = VariablePool.rename(aCM.vp, aRV);
		
		CA ret = new CA(aCM.name, vp_new);
		
		// control states
		ret.copyStates(aCM);
		
		// transitions
		for (CATransition t : aCM.transitions()) {
			//CAState from = this.getStateWithName(aRS.getNewNameFor(t.from().name()));
			//CAState to = this.getStateWithName(aRS.getNewNameFor(t.to().name()));
			CATransition tCopy = CATransition.inline_rename(t, aRV, vp_new, /*from, to, t.name(),*/ ret);
			
			ret.addTransition_base(tCopy);
		}
		
		// other members
		ret.nextStateGenerator = aCM.nextStateGenerator;
		ret.nextTransitionGenerator = aCM.nextTransitionGenerator;
		
		return ret;
	}
	
	public void renameControlStates(String aPrefix) {
		List<CAState> aux = new LinkedList<CAState>(this.states);
		this.states.clear();
		for (CAState s : aux) {
			s.name(aPrefix+s.name());
			states.add(s);
		}
	}
	
	// declare all control states of the inlined automaton except for initial and final states
	private void inline_declareStates(CA ca, Rename r) {
		for (CAState s : ca.states) {
			if (!ca.isInitial(s) && !ca.isFinal(s)) {
				String ss = (r==null)? s.name() : r.getNewNameFor(s.name());
				for (CAState s2 : this.states)
					if (s2.name().equals(ss))
						throw new RuntimeException();
				
				this.getStateWithName(ss);
			}
		}
		for (CAState s : ca.errorStates()) {
			String ss = (r==null)? s.name() : r.getNewNameFor(s.name());
			this.setError(this.getStateWithName(ss));
		}
	}
	
	// assumption: initial states don't have incoming and final states don't have outgoing transitions
	public Collection<Call> inline(Collection<Call> calls, CA ca, RenameM[] renSa) {
		
		Collection<Call> callsNew = new LinkedList<Call>();
				
		// backup locals
		
		Variable[] local_old = vp.localUnp();
		
		// declare locals of the inlined automaton
		
		//declareVarLocalAdd(CA.toStringList(ca.localVariables));
		vp.inlinePool(ca.vp);
		
		// if there are 2+ calls, rename inlined states for each call by unique names
		
		int i=0;
		
		for (Call call : calls) {
			
			CATransition tCalling = call.getCallingPoint();
			
			this.removeTransition(tCalling);
						
			// declare control states of the inlined automaton
			
			RenameM renS = renSa[i];
			i++;
			
			this.inline_declareStates(ca, renS);
			
			// prepare call and return transitions
			
			Call c = tCalling.call();
			CompositeRel rCall = CompositeRel.createAssignment(Variable.counterpartArray(ca.vp.portIn()), c.argsIn().toArray(new LinearConstr[0]));
			CompositeRel rReturn = CompositeRel.createAssignment(c.argsOut().toArray(new Variable[0]), Variable.counterpartArray(ca.vp.portOut()));
			
			//add implAct (only on original locals)
			rCall.addImplicitActionsForSorted(local_old);
			rReturn.addImplicitActionsForSorted(local_old);
			
			// merge all the initial states into one (assumption: none of them has incoming edge)
			// merge all the final states into one (assumption: none of them has outgoing edge)
			CAState s0I = ca.initialStates().iterator().next();
			CAState s0F = ca.finalStates().iterator().next();
			String nameI = s0I.name();
			String nameF = s0F.name();
			if (renS != null) {
				nameI = renS.getNewNameFor(nameI);
				nameF = renS.getNewNameFor(nameF);
			}
			CAState sCaI = this.getStateWithName(nameI);
			CAState sCaF = this.getStateWithName(nameF);
			
			CATransition tCall = CATransition.inline_call(tCalling, tCalling.from(), sCaI, rCall, "call", this);
			CATransition tReturn = CATransition.inline_return(tCalling, sCaF, tCalling.to(), rReturn, "ret", this);
			
			this.addTransition(tCall);
			this.addTransition(tReturn);
			
			for (CATransition tt : ca.transitions()) {
				
				CATransition tInl;
				
				CAState fr, to;
				if (renS == null) {
					fr = ca.isInitial(tt.from())? sCaI : this.getStateWithName(tt.from().name());
					to = ca.isFinal(tt.to())? sCaF : this.getStateWithName(tt.to().name());
				} else {
					fr = ca.isInitial(tt.from())? sCaI : this.getStateWithName(renS.getNewNameFor(tt.from().name()));
					to = ca.isFinal(tt.to())? sCaF : this.getStateWithName(renS.getNewNameFor(tt.to().name()));
				}
				tInl = CATransition.inline_plug(tt, fr, to, tt.name(), this, local_old);
				
				this.addTransition(tInl);
				
				if (tInl.label().isCall()) {
					callsNew.add(tInl.call());
				}
			}
		}
		
		return callsNew;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////   REDUCTION ////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////


	public Set<CAState> nonInitFinErrStates() {
		Set<CAState> ret = new HashSet<CAState>(this.states());
		ret.removeAll(this.initialStates());
		ret.removeAll(this.finalStates());
		ret.removeAll(this.errorStates());
		return ret;
	}
	

	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////


	
	public void renameBFS_simple(boolean renStates) {

		Collection<CATransition> trans = new LinkedList<CATransition>();
		Collection<CAState> initS = new LinkedList<CAState>();
		Collection<CAState> finS = new LinkedList<CAState>();

		trans.addAll(this.transitions());
		initS.addAll(this.initialStates);
		finS.addAll(this.finalStates);

		Map<Integer, String> renS = new HashMap<Integer, String>();
		Map<Integer, String> renT = new HashMap<Integer, String>();

		int sCtr = 1;
		int tCtr = 1;
		
		for (CATransition t : this.transitions.values()) {
			if (renS.get(t.from().id()) == null) {
				renS.put(t.from().id(), "s" + sCtr++);
			}
			if (renS.get(t.to().id()) == null) {
				renS.put(t.to().id(), "s" + sCtr++);
			}
			renT.put(t.id(), "t" + tCtr++);
		}
		
		if (renStates)
			for (CAState s : states) {
				s.oldName(s.name());
				s.name(renS.get(s.id()));
			}
		
		for (CATransition t : transitions.values()) {
			t.oldName(t.name());
			t.name(renT.get(t.id()));
		}
	}
	// renames states and transitions (order given by BFS from initial states)
	public void renameBFS() {

		Collection<CATransition> trans = new LinkedList<CATransition>();
		Collection<CAState> initS = new LinkedList<CAState>();
		Collection<CAState> finS = new LinkedList<CAState>();

		trans.addAll(this.transitions());
		initS.addAll(this.initialStates());
		finS.addAll(this.finalStates());

		Map<Integer, String> renS = new HashMap<Integer, String>();
		Map<Integer, String> renT = new HashMap<Integer, String>();

		Set<CAState> visited = new HashSet<CAState>();
		List<CAState> bfsList = new LinkedList<CAState>();
		bfsList.addAll(initS);

		int sCtr = 1;
		int tCtr = 1;

		// get the rename-mapping
		while (!bfsList.isEmpty()) {
			CAState s = bfsList.remove(0);

			if (visited.contains(s))
				continue;

			visited.add(s);

			renS.put(s.id(), "s" + sCtr++);

			for (CATransition t : s.outgoing()) {
				if (!visited.contains(t.to().id()))
					bfsList.add(t.to());

				renT.put(t.id(), "t" + tCtr++);
			}
		}

		for (CAState s : states) {
			s.oldName(s.name());
			s.name(renS.get(s.id()));
		}
		
		for (CATransition t : transitions.values()) {
			t.oldName(t.name());
			t.name(renT.get(t.id()));
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	

	/**
	 * returns a <tt>CM_State</tt> object representing state with the specified
	 * name. If an object representing a state of that name has already been
	 * created, it is returned. Otherwise, a new <tt>CM_State</tt> object with
	 * the specified name is created. 
	 * 
	 * <p>
	 * The second parameter may contain flags informing if the state of the
	 * specified name should be initial and/or final. Therefore, the state
	 * object that is to be returned is marked according to the specified flags.
	 * 
	 * @param aName
	 *            a name of a state
	 * @param aFlags
	 *            flags of a state (initial, final)
	 * @return unique object representing a state with the specified name
	 */
	
	// adds a new state with unique ID even if a state with a same name exists
	// Therefore, two distinct invocations of this method with the same 
	// arguments will return distinct objects.
	public CAState addState(String aName, int aFlags) {
		CAState state = new CAState(aName);
		
		states.add(state);
		
		if (CR.isMaskedWith(aFlags, CA.fINIT))
			initialStates.add(state);
		if (CR.isMaskedWith(aFlags, CA.fFINAL))
			finalStates.add(state);
		if (CR.isMaskedWith(aFlags, CA.fERROR))
			errorStates.add(state);
		return state;
	}
	
	// adds a new state with unique ID only if a state with a same name doesn't exists
	// Therefore, two distinct invocations of this method with the same 
	// arguments will return identical objects.
	public CAState getStateWithName(String aName) {
		return getStateWithName(aName, 0x0);
	}
	// adds a new state with unique ID only if a state with a same name doesn't exists
	// Therefore, two distinct invocations of this method with the same 
	// arguments will return identical objects.
	public CAState getStateWithName(String aName, int aFlags) {
		CAState state = null;
		for (CAState s : states) { // TODO: change
			if (s.name().equals(aName)) {
				state = s;
			}
		}
		
		if (state == null) {
			state = new CAState(aName);
			states.add(state);
		}
		if (CR.isMaskedWith(aFlags, CA.fINIT))
			initialStates.add(state);
		if (CR.isMaskedWith(aFlags, CA.fFINAL))
			finalStates.add(state);
		if (CR.isMaskedWith(aFlags, CA.fERROR))
			errorStates.add(state);

		return state;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	
	// add implicit actions if required and add bindings ...
	public void postParsing(Collection<CATransition> aCol) {

//		if (CR.Parameters.isOnParameter(CR.Parameters.ABSTR_OCT)) {
//			aCol = CATransition.abstractOct(aCol);
//		}
		
		//if (LinearRel.INCLUSION_GLPK) {
			GLPKInclusion.initOpt(vp.allUnp());
		//}
		
		Variable[] vararr = vp.allUnp();
		Arrays.sort(vararr);
		
		if (CR.NO_POSTPARSING) {
			
			Iterator<CATransition> iter = aCol.iterator();
			while (iter.hasNext()) {
				
				CATransition t = iter.next();
				
				if (Parameters.isOnParameter(Parameters.IMPLACT)) {
					if (!t.calls())
						t.rel().addImplicitActionsForSorted(vararr);
				}

//				if (!t.rel().isDBM() || !t.isContradictory()) {
//					addTransitionOrig(t);
//				}
				if (t.rel().isDBRel() || t.isContradictory()) {
					iter.remove();
				}
			}

			addTransitions_strat(aCol);
			setOrigTrans(aCol);
			setOrigStates();
			
		} else {
			
			// add implicit actions
			// remove transition which have an error state as a source
			boolean ia = Parameters.isOnParameter(Parameters.IMPLACT);
			Iterator<CATransition> iter = aCol.iterator();
			while (iter.hasNext()) {
				CATransition t = iter.next();
				if (this.isError(t.from())) {
					iter.remove();
					continue;
				}
				if (!states.contains(t.from()) || !states.contains(t.to()))
					throw new RuntimeException("internal error: undeclared state +"+(states.contains(t.from())? t.to():t.from()));
				if (ia && !t.calls())
					t.rel().addImplicitActionsForSorted(vararr);
			}
			
			// add the transitions to the automaton
			removeContradictions(aCol);
			addTransitions_strat(aCol);
			
			nontermUnreachRemoval();
			
			// ensure that initial and final states are trivial SCCs
			if (TERM) {
				// perform elsewhere
				//transform4termination();
			} else {
				checkMarkedStates();
			}
			
			
			setOrigTrans(aCol);
			setOrigStates();
			
			bu();
			bu_transitions(aCol);
		}
	}
	private static void removeContradictions(Collection<CATransition> col) {
		Iterator<CATransition> iter = col.iterator();
		while (iter.hasNext()) {
			CATransition t = iter.next();
			if (t.calls())
				continue;
			if (t.contradictory())
				iter.remove();
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	
	public List<CATransition> incidentTrans(CAState from, CAState to) {
		List<CATransition> ret = new LinkedList<CATransition>(from.outgoing());
		
		Iterator<CATransition> iter = ret.iterator();
		while (iter.hasNext())
			if (!iter.next().to().equals(to))
				iter.remove();
		
		return ret;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	
	public void addTransition_base(CATransition aT) {
		
		transitions.put(aT.id(), aT);
		aT.from().addOutgoing(aT);
		aT.to().addIncoming(aT);
		states.add(aT.from());
		states.add(aT.to());
	}
	
	public boolean addTransitionOnlySat(CATransition aT) {
		if (aT.isContradictory())
			return false;
		return addTransition(aT);
	}
	public boolean addTransition(CATransition aT) {
		return addTransition(aT, getMergeType());
	}
	public void addTransitions_strat(Collection<CATransition> aCol) {
		int n = aCol.size();
		int step = n / 10;
		int i = 0;
		if (!CR.RELEASE && n>50) { System.out.print("Adding "+n+" transitions.");}
		for (CATransition t : aCol) {
			addTransition(t);
			if (!CR.RELEASE && i > 50 && i%step == 0) { System.out.print("("+(float)i/n+"%)"); }
			i++;	
		}
	}
	
	private static MergeType getMergeType() {
		if (Parameters.isOnParameter(Parameters.T_MERGE_PREC))
			return MergeType.ONLY_PREC;
		if (Parameters.isOnParameter(Parameters.T_MERGE_IMPRECISE))
			return MergeType.ONLY_IMPREC;
		return MergeType.NONE;
	}
	private enum MergeType {
		NONE, ONLY_PREC, ONLY_IMPREC;
		public boolean isOnlyPrec() { return this == ONLY_PREC; }
		public boolean isOnlyImprec() { return this == ONLY_IMPREC; }
		//public boolean isNone() { return this == NONE; }
	}
	
	
	
	
	private boolean addTransition(CATransition aT, MergeType mergeType) {
		
		// special-case branch
		if (Parameters.isOnParameter(Parameters.T_ALWAYSONE)) {
			Collection<CATransition> c = this.incidentTrans(aT.from(), aT.to());
			switch (c.size()) {
			case 0:
				
				break;
			case 1:
				CATransition t = c.iterator().next();
				
				aT = aT.hullOct(t);
				this.removeTransition(t);
				
				break;
			default:
				throw new RuntimeException("Internal error:");
			}

			addTransition_base(aT);
			
			return true;
		}
		
		// special-case branch
		if (CR.NO_POSTPARSING) {
			transitions.put(aT.id(), aT);
			aT.from().addOutgoing(aT);
			aT.to().addIncoming(aT);
			return true;
		}
		
		List<CATransition> incid = incidentTrans(aT.from(), aT.to());
		
		// filter by inclusion checks
		if (Parameters.isOnParameter(Parameters.T_FULLINCL) || 
				(Parameters.isOnParameter(Parameters.T_OCTINCL) && aT.rel().isOctagon())) {

//			List<CATransition> toRemove = CATransition.checkInclusion(incid, aT, false);
//			if (toRemove.isEmpty()) // nothing removed => aT was included
//				return false;
//			
//			for (CATransition t : toRemove)
//				this.removeTransition(t);
			
			Iterator<CATransition> iter = incid.iterator();
			while (iter.hasNext()) {
				
				CATransition t = iter.next();
				
				if (Parameters.isOnParameter(Parameters.T_OCTINCL) && !t.rel().isOctagon())
					continue;
				
				if (!t.from().equals(aT.from()) || !t.to().equals(aT.to()))
					throw new RuntimeException();

				// !!!! need to represent as merge
				// inclusion holds => merge condition holds
				if (t.rel().includes(aT.rel()).isTrue()) {
					if (Parameters.isOnParameter(Parameters.STAT_INCLTRANS)) {
						StringBuffer sb = new StringBuffer();
						sb.append("INCLUSION: "+ t);
						sb.append("includes " + aT + "\n");
						Parameters.logTransIncl(sb);
					}
					
					t.addSubsumed(aT);
					
					// merge
					// TODO !!! generates too many transitions
//					this.removeTransition(t);
//					CATransition ttt = t.mergeWithIncluded(aT);
//					addTransition_base(ttt, flag);
					
					return false;
					
				} else if (t.rel().isIncludedIn(aT.rel()).isTrue()) {
					this.removeTransition(t);
					iter.remove();
					
					aT.addSubsumed(t);
					
					// merge
//					aT = aT.mergeWithIncluded(t);
					
					continue;
				}
			}
		}
		
		// incid -- list if incident transitions currently present
		
		//if (aT.rel().isOctagon()) {
			if (mergeType.isOnlyPrec()) {
				this.merge(incid, aT);
			} else {
				
				//if (CR.Parameters.isOnParameter(CR.Parameters.T_MERGE_IMPRECISE)) {
				if (mergeType.isOnlyImprec()) {
					List<CATransition> c = this.incidentTrans(aT.from(), aT.to());
					
					if (c.size() > 0) {
						CATransition hull = null, oldhull = null;
						
						// choose cluster (according to some strategy)
						oldhull = c.get(0);
						hull = oldhull.hullOct(aT);
						
						if (oldhull != null) {
							
							this.removeTransition(oldhull);
							this.addTransition(hull);
							//this.transitions.remove(oldhull.id());
							//this.transitions.put(hull.id(), hull);
							
							return true;
						}
					}
				}
	
				addTransition_base(aT);
			}
		//}

		
		if (!mergeType.isOnlyPrec() && isInitial(aT.from()) && isError(aT.to()) && this.isMain) {
			//System.out.println("!! CE candidate !!");
			//CR.writeToFile("snapshot-i-f.dot", ReduceInfo.toDot(origTransitions, null, null).toString());
			
			ce_trace_time = System.currentTimeMillis();
			
			if (Parameters.isOnParameter(Parameters.CE_NO)) {
				throw new StopReduction(StopReduction.StopType.NOTRACE);
			}
			
			CENode root = CENode.buildTree(aT.reduce_info(), vp.globalUnp(), vp.localUnp());
				
			ce_nodes.add(root);
			
			if (!Parameters.isOnParameter(Parameters.CE_ALL)) {
				throw new StopReduction(StopReduction.StopType.TRACE);
			}
		}
		
		return true;
	}
	
	private long ce_trace_time;
	public long ceTraceTime() { return ce_trace_time; }
	
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	
	// roots of CE-trees found during analysis
	private Collection<CENode> ce_nodes = new LinkedList<CENode>();
	public Collection<CENode> ce_nodes() { return ce_nodes; }
	
	private void checkCE() {
		
		if (!isMain) // certainty that CE is real can be achieved only in the main procedure
			return;
		
		if (ce_nodes.isEmpty())
			return;
		
		if (!CENode.hasRealTrace(ce_nodes)) { // spurious
			throw new SpuriousCE();
		} else {
			if (!Parameters.isOnParameter(Parameters.CE_ALL)) {
				throw new StopReduction(StopReduction.StopType.TRACE);
			}
		}
	}
	
	private Set<ReduceInfo> reachAll() {
		List<ReduceInfo> todo = new LinkedList<ReduceInfo>();
		for (CATransition t : this.origTransitions) {
			todo.add(t.reduce_info());
		}
		for (CATransition t : this.transitions()) {
			todo.add(t.reduce_info());
		}
		
		Set<ReduceInfo> done = new HashSet<ReduceInfo>();
		
		while (!todo.isEmpty()) {
			ReduceInfo ri = todo.remove(0);
			
			if (done.contains(ri))
				continue;
			done.add(ri);
			
			todo.addAll(ri.succ);
			todo.addAll(ri.pred);
		}
		
		return done;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	// removes specified collection from this CA
	public void removeTransition(CATransition aTransition) {
		boolean b1 = aTransition.from().removeOutgoing(aTransition);
		boolean b2 = aTransition.to().removeIncoming(aTransition);

		if (!b1 || !b2)
			throw new RuntimeException("Transition couldn't be removed!");

		transitions.remove(aTransition.id());
	}

	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	// TODO: convert, remove, never used??
	// private static YicesAnswer isIncluded(int inx, CATransition[] tt, BitSet bs, Collection<String> vars) {

	// 	StringWriter sw = new StringWriter();
	// 	IndentedWriter iw = new IndentedWriter(sw);
		
	// 	iw.writeln("(reset)");
		
	// 	CR.yicesDefineVarNames(iw, vars);

	// 	iw.writeln("(assert");
	// 	iw.indentInc();
		
	// 	iw.writeln("(and");
	// 	iw.indentInc();
		
	// 	iw.writeln("(not (or");
	// 	iw.indentInc();
		
	// 	iw.writeln("false");
		
	// 	for (int i = 0; i < tt.length; i ++) {
	// 		if (bs.get(i) || i == inx)
	// 			continue;
			
	// 		tt[i].rel().toSBYicesAsConj(iw);
	// 	}
		
	// 	iw.indentDec(); // not or
	// 	iw.writeln("))");
		
	// 	tt[inx].rel().toSBYicesAsConj(iw);
		
	// 	iw.indentDec(); // and
	// 	iw.writeln(")");
		
	// 	iw.indentDec(); // assert
	// 	iw.writeln(")");
		
	// 	iw.writeln("(check)");
		
	// 	return CR.isSatisfiableYices(sw.getBuffer(), new StringBuffer());
	// }
	

	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	
	public static class MergeResult {
		ArrayList<CATransition> list;
		BitSet ismerged;
		int origsize;
		
		public MergeResult(Collection<CATransition> col, int aOrigSize) {
			list = new ArrayList<CATransition>(col);
			origsize = aOrigSize;
			ismerged = new BitSet(origsize);
		}
		public MergeResult(Collection<CATransition> col) {
			list = new ArrayList<CATransition>(col);
			origsize = list.size();
			ismerged = new BitSet(origsize); // init value = false
		}
		public int size() {
			return list.size();
		}
		
		public int merge(MergeResult mr, int s1, int e1) {
			return merge(mr, s1, e1, s1, e1);
		}
		public int merge(MergeResult mr, int s1, int e1, int s2, int e2) {
			
			boolean useSymetry = s1 == s2 && e1 == e2;
			
			int mcnt = 0;
			for (int i1 = s1; i1 < e1; i1 ++) {
				if (mr.ismerged.get(i1))
					continue;
				for (int i2 = (useSymetry)? i1+1 : s2; i2 < e2; i2 ++) {
					if (i1 == i2)
						continue;
					if (mr.ismerged.get(i2))
						continue;
					
					CATransition t1 = mr.list.get(i1);
					CATransition t2 = mr.list.get(i2);
					
					CATransition t = t1.merge(t2);
					if (t != null) {
						mr.list.add(t);
						mr.ismerged.set(i1);
						mr.ismerged.set(i2);
						mcnt ++;
						
						// log
						if (Parameters.isOnParameter(Parameters.STAT_MERGE)) {
							Parameters.log(Parameters.STAT_MERGE, new StringBuffer(
									"\nR1: "+t1.rel().toLinearRel().ordered().toSB()+
									"\nR2: "+t2.rel().toLinearRel().ordered().toSB()+
									"\nR : "+t.rel().toLinearRel().ordered().toSB()+"\n"));
							//CR.Parameters.log(CR.Parameters.STAT_MERGE, ca.printTransitionsOrderedViaIncidence());
						}
					}
				}
			}
			return mcnt;
		}
		
		public List<CATransition> getNew() {
			List<CATransition> ret = new LinkedList<CATransition>();
			
			int size = size();
			for (int i = ismerged.nextClearBit(origsize); i >= 0 && i < size; i = ismerged.nextClearBit(i+1)) {
				ret.add(list.get(i));
			}
			
			return ret;
		}
		public void carryOut(CA ca) {
			int size = size();

			// remove merged existing merged transitions
			for (int i = ismerged.nextSetBit(0); i >= 0 && i < origsize; i = ismerged.nextSetBit(i+1)) {
				ca.removeTransition(list.get(i));
			}
			// add non-existing transitions (results of merging)
			for (int i = ismerged.nextClearBit(origsize); i >= 0 && i < size; i = ismerged.nextClearBit(i+1)) {
				ca.addTransition(list.get(i), MergeType.NONE);
			}
		}
		public static void merge2(MergeResult mr, int oldsize, int newsize) {
			
			int cntnew = newsize - oldsize;
			
			while (cntnew > 0) {
				
				cntnew = 0;
				
				cntnew += mr.merge(mr, 0, oldsize, oldsize, newsize);
				cntnew += mr.merge(mr, oldsize, newsize);
				
				oldsize = newsize;
				newsize = oldsize + cntnew;
			}
		}
	}
	
	private void merge(Collection<CATransition> col, CATransition t) {
		
		MergeResult ret = new MergeResult(col);
		int oldsize = ret.list.size();
		
		ret.list.add(t);
		int newsize = oldsize + 1;
		MergeResult.merge2(ret, oldsize, newsize);
		
		ret.carryOut(this);
	}
	

	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	// removes states in the specified collection from this CA. All transitions
	// which lead to or originate in some of these states are removed.
	public void removeStates(Collection<CAState> aStates) {
		for (CAState s : aStates) {
			removeState(s);
		}
	}

	// removes a specified state in the specified collection from this CA. All transitions
	// which lead to or originate in some of these states are removed.
	public void removeState(CAState s) {
		Iterator<CATransition> iter;
		iter = (new LinkedList<CATransition>(s.incoming())).iterator();
		while (iter.hasNext()) {
			CATransition t = iter.next();
			removeTransition(t);
		}
		iter = (new LinkedList<CATransition>(s.outgoing())).iterator();
		while (iter.hasNext()) {
			CATransition t = iter.next();
			removeTransition(t);
		}
		this.states.remove(s);
		this.initialStates.remove(s);
		this.finalStates.remove(s);
		this.errorStates.remove(s);
	}

	// marks the specified state as initial
	public boolean setInitial(CAState aState) {
		return initialStates.add(aState);
	}
	// unmarks the specified state as initial
	public boolean unsetInitial(CAState aState) {
		return initialStates.remove(aState);
	}
	
	public boolean setError(CAState aState) {
		return errorStates.add(aState);
	}
	public boolean setFinal(CAState aState) {
		return finalStates.add(aState);
	}
	public boolean unsetFinal(CAState aState) {
		return finalStates.remove(aState);
	}
	public boolean unsetError(CAState aState) {
		return errorStates.remove(aState);
	}
	
	public Collection<CATransition> composeTransitions(CATransition aT1,
			CATransition aT2) {

		Collection<CATransition> comp = CATransition.compose_asCol(aT1, aT2);
		return comp;
	}

	public Set<CAState> reachableStates(Set<CAState> startFrom, boolean forward) {

		Set<CAState> reach = new HashSet<CAState>();
		Set<CAState> queue = new HashSet<CAState>(startFrom);

		while (!queue.isEmpty()) {
			Iterator<CAState> i = queue.iterator();
			CAState s = i.next();
			i.remove();
			reach.add(s);

			for (CATransition t : forward ? s.outgoing() : s.incoming()) {

				CAState s2 = forward ? t.to() : t.from();

				if (!reach.contains(s2) && !queue.contains(s2))
					queue.add(s2);
			}
		}

		return reach;
	}

	private Set<CAState> usefullStates() {
		Set<CAState> usefull;
		{
			Set<CAState> reach_fwd = this.reachableStates(this.initialStates, true);
			Set<CAState> aux = new HashSet<CAState>(this.finalStates);
			aux.addAll(this.errorStates);
			Set<CAState> reach_bwd = this.reachableStates(aux, false);

			usefull = reach_fwd;
			Iterator<CAState> iter = usefull.iterator();
			while (iter.hasNext()) {
				if (!reach_bwd.contains(iter.next()))
					iter.remove();
			}
		}
		return usefull;
	}
	/**
	 * removes unreachable states and transitions start or end in them from this
	 * CA.
	 */
	public void nontermUnreachRemoval() {

		Set<CAState> usefull = usefullStates();

		// elimination of useless states
		Set<CAState> useless = new HashSet<CAState>(states);
		useless.removeAll(usefull);
		Iterator<CAState> iter = useless.iterator();
		while (iter.hasNext()) {
			CAState s = iter.next();
			//this.printStatTrans();
			System.out.print("eliminating state "+s+"...  ");
			this.removeState(s);
			//System.out.println("done (useless); remains "+remain2elim()+" states");
			System.out.println("done (useless)");
		}
	}
	
	/**
	 * tries to eliminate the specified state. That state must not have a
	 * self-loop. It tries to compose every incoming transition with every
	 * outcoming transition (by using Fourier-Moetzkin or Cooper methods). 
	 * The method removes the state and all transitions leading to or 
	 * starting in that state; it also adds composed transitions to the CA.
	 * 
	 * @param aState
	 *            a state that is to be eliminated
	 */
	public void elimNonloopState(CAState aState) {

		if (aState.loops() != 0)
			throw new RuntimeException(
					"internal error: state should not contain a self-loop.");
		
		if (CR.DEBUG > CR.DEBUG_NO)
			System.out.println("composing " + aState.incoming().size()
					+ " incoming with " + aState.outgoing().size()
					+ " outcoming" + "[" + aState.incoming().size()
					* aState.outgoing().size() + " compositions]"
					+ "\n in.stat: " + CATransition.typeStat(aState.incoming())
					+ "\n " + "out.stat: "
					+ CATransition.typeStat(aState.outgoing()));
		
		Collection<CATransition> tin = aState.incoming();
		Collection<CATransition> tout = aState.outgoing();

		this.removeState(aState);

		Collection<CATransition> t_new = new LinkedList<CATransition>();
		
		CATransition.compose(tin, tout, t_new);
		
		addTransitions_strat(t_new);
	}


	public String printFlatStates() {
		String str = "  flatness of states: ";

		boolean isFlat = true;
		for (CAState s : states) {
			if (s.sccOutEdges() > 1) {
				isFlat = false;
				str += "!";
			}
			str += s.name() + " ";
		}
		str += "\n  System is " + ((isFlat) ? "" : "not ") + "flat.\n";

		return str;
	}

	private static class StateWithMetrics implements Comparable<StateWithMetrics> {

		// highest priority -- used for random strategies
		int rand = 0;

		CAState state;
		// int accelerationFactor; // for states with one loop
		int loops;

		// effects of elimination
		int genLoops;
		int genNonLoops;

		boolean scc_input_output = false;

		/*
		public int genLoops() {
			return genLoops;
		}
		public int genNonLoops() {
			return genNonLoops;
		}
		public int genTransitions() {
			return genLoops + genNonLoops;
		}
		*/
		public String toString() {
			return state + ": [" +
				"loops=" + loops + 
				",genLoops=" + genLoops + 
				",genNonLoops=" + genNonLoops + 
				"]\n";
		}

		public boolean equals(Object o) {
			if (!(o instanceof StateWithMetrics || o instanceof CAState))
				return false;

			if (o instanceof CAState)
				return state.equals((CAState) o);
			else
				return state.equals(((StateWithMetrics) o).state);
		}

		public int hashCode() {
			return state.hashCode();
		}

		public StateWithMetrics(CAState aState) {
			state = aState;

			update();
		}

		public void update() {

			loops = state.loops();

			int genTotal = (state.incoming().size() - loops)
					* (state.outgoing().size() - loops);

			genLoops = 0;
			for (BaseNode an : state.getNeighbours()) {
				genLoops += BaseNode.elemCycles(an, state);
			}

			genNonLoops = genTotal - genLoops;
		}

		public int compareTo(StateWithMetrics other) {

			int ret;
			if ((ret = new Integer(rand).compareTo(new Integer(other.rand))) != 0)
				return ret;
			else if (this.scc_input_output && !other.scc_input_output)
				return -1;
			else if (!this.scc_input_output && other.scc_input_output)
				return 1;
			else if (this.loops == other.loops) {
				switch (this.loops) {
				case 0:
					if (this.genLoops == other.genLoops)
						return new Integer(genNonLoops).compareTo(new Integer(
								other.genNonLoops));
					else
						return new Integer(genLoops).compareTo(new Integer(
								other.genLoops));
				default:
					return 0;
				}
			} else {
				return new Integer(this.loops).compareTo(new Integer(
						other.loops));
			}
		}

		public static int indexOf(List<StateWithMetrics> sml, CAState s) {
			int i = 0;
			for (StateWithMetrics sm : sml) {
				if (sm.state.equals(s))
					return i;
				i++;
			}
			return -1;
		}

		public static void update(List<StateWithMetrics> stateMetrics,
				Set<BaseNode> neighbours) {

			for (BaseNode an : neighbours) {
				CAState n = (CAState) an;

				int inx = StateWithMetrics.indexOf(stateMetrics, n);
				if (inx != -1) {
					StateWithMetrics sm = stateMetrics.get(inx);
					sm.update();
				}
			}
			Collections.sort(stateMetrics);
		}

		public static List<StateWithMetrics> init_rand(Collection<BaseNode> states, int max) {
			List<StateWithMetrics> stateMetrics = new ArrayList<StateWithMetrics>();
			Random rand = new java.util.Random();
			for (BaseNode n : states) {
				CAState s = (CAState)n;
				// if (ca.isInitial(s) || ca.isFinal(s))
				// continue;
				StateWithMetrics sm = new StateWithMetrics(s);
				sm.rand = rand.nextInt(max);
				stateMetrics.add(sm);
			}
			Collections.sort(stateMetrics);

			return stateMetrics;
		}
		
		public static List<StateWithMetrics> init(Collection<? extends BaseNode> states) {
			List<StateWithMetrics> stateMetrics = new ArrayList<StateWithMetrics>();
			for (BaseNode a : states) {
				CAState s = (CAState)a;
				//if (ca.isInitial(s) || ca.isFinal(s))
				//	continue;
				StateWithMetrics sm = new StateWithMetrics(s);
				stateMetrics.add(sm);
			}
			Collections.sort(stateMetrics);

			return stateMetrics;
		}

		static boolean preferInputOutpuNodes = false;

		public static List<StateWithMetrics> init(boolean forward, DAGNode dagNode) {

			List<StateWithMetrics> stateMetrics = init(dagNode.states());

			for (StateWithMetrics sm : stateMetrics) {
				CAState s = sm.state;
				if (preferInputOutpuNodes) {
					if (forward && dagNode.containsInState(s))
						sm.scc_input_output = true;
					else if (!forward && dagNode.containsOutState(s))
						sm.scc_input_output = true;
				}
			}
			Collections.sort(stateMetrics);

			return stateMetrics;
		}

		public static CAState bestOf(List<StateWithMetrics> stateMetrics,
				List<CAState> list) {
			for (StateWithMetrics sm : stateMetrics) {
				if (list.contains(sm.state))
					return sm.state;
			}
			System.out.println(stateMetrics);
			System.out.println(list);
			throw new RuntimeException();
		}
	}
	
	public static CATransition createIdentityTransition(CA aCA, CAState from, CAState to) {
		CompositeRel identity_rel = CompositeRel.createIdentityRelationForSorted(aCA.vp.allUnp());
		CATransition identity = new CATransition(from, to, identity_rel, CR.TRANS_ID_NAME, aCA);
		return identity;
	}
	public static CATransition createIdentityTransition(CA aCA, CAState s) {
		return createIdentityTransition(aCA, s, s);
	}
	
	private void hull4acceleration(CAState s) {
		List<CATransition> l = new LinkedList<CATransition>(s.incoming());
		for (CATransition t : l) {
			if (t.from() != t.to() || t.rel().isOctagon())
				continue;
			CATransition hull = t.hull(Relation.RelType.OCTAGON);
			this.removeTransition(t);
			this.addTransition(hull);
		}
	}
	
	// strategy: state q has the loop R
	// compute composed transitions of the form (Ri o R+ o Rj) and (Ri o Rj),
	// where i iterates over transitions leading to q
	// and j iterates over transitions coming from q
	private boolean elimAsRegExp(CAState s) {
		if (s.loops() != 1)
			throw new RuntimeException("State " + s + " cannot be eliminated ("
					+ s.loops() + " loops).");
		
		CATransition loop = (CATransition) s.getLoops().iterator().next();
		
		if (Parameters.isOnParameter(Parameters.ABSTR_OCT)) {
			loop = loop.abstractOct();
		}

//System.out.println("Accelerating:\n"+loop.rel());
		
		CATransition[] closure_transitions;
		closure_transitions = loop.closurePlus();
//		if (TERM) {
//			closure_transitions = loop.closurePlus();
//		} else {
//			closure_transitions = loop.closureStar();
//		}
		if (closure_transitions == null)
			return false;
		
		Collection<CATransition> l = new LinkedList<CATransition>(Arrays.asList(closure_transitions));
		
		if (!TERM) {
			CATransition t_ident = CA.createIdentityTransition(this, s);
			//ReduceInfo ri = t_ident.reduce_info();
	 		t_ident.setClosureIndentInfo(loop); // this is a bit unclean
			//t_ident.setIdentInfo(loop);
			l.add(t_ident);
		}
		
		this.replaceMultiLoop(s, l);
		
		CATransition.shortcutNode(loop,l);
		
		loop.reduce_info().setUseful();
		for (CATransition t : l) {
			t.reduce_info().setUseful();
		}
		
		return true;
	}
	

	public void reduce_nonloop_state() {

		List<StateWithMetrics> stateMetrics = StateWithMetrics.init(this
				.nonInitFinErrStates());

		while (!stateMetrics.isEmpty()) {
			
			//this.printStatTrans();
			
			StateWithMetrics smm = stateMetrics.remove(0);
			CAState s = smm.state;
			
			if (s.loops() != 0)
				return;

			Set<BaseNode> neighbours = s.getNeighbours();

			String info = "(" + "loops: 0"+", in:"+(s.incoming().size())+", out:"+(s.outgoing().size())+") ";
			if (!CR.NO_OUTPUT)
				System.out.print("eliminating state "+s+"...  "+info);
			
			elimNonloopState(s);
			
			if (debug) {
				this.bu_copy();
				View.view(this, "red-"+reduce_cnt+"-"+(reduce_cnt2++)+".dot");
			}
			
			checkCE();
			
			if (!CR.NO_OUTPUT)
				System.out.print("done");

			
			System.out.println(", remains "+remain2elim()+" states");
			
			StateWithMetrics.update(stateMetrics, neighbours);
		}
	}

	
	public boolean eliminateState(CAState s, boolean elim_forward, boolean tree_bfs, boolean print) {

		int loops = s.loops();
		
		if (Parameters.isOnParameter(Parameters.REDUCE_NO_MLOOP) && loops > 1)
			return false;
		
		//this.printStatTrans();
		boolean success;

		if (print && !CR.NO_OUTPUT)
			System.out.print("eliminating state "+s+"...  ");

		// set reduce label
		reduceLabelSet(s);
		
		String info = "(" + "loops: "+loops+", in:"+(s.incoming().size()-loops)+", out:"+(s.outgoing().size()-loops)+") ";
		System.out.print(info);
		
		if (HULL4ACCELERATION)
			hull4acceleration(s);
		
		info = "";
		if (loops == 0) {
			
			elimNonloopState(s);
			success = true;
		} else if (loops == 1) {
			
			success = elimAsRegExp(s);

		} else {
			//public static int reduceMultiLoopState(CA aCA, CAState s, boolean elim_forward, boolean tree_bfs, Collection<CATransition> pcomputed, boolean plus) {
			int depth;
			if (TERM) {
				depth = verimag.flata.automata.ca.MultiloopTransformation.reduceMultiLoopStatePlus(this, s, elim_forward, tree_bfs);
			} else {
				depth = verimag.flata.automata.ca.MultiloopTransformation.reduceMultiLoopState(this, s, elim_forward, tree_bfs);
				
			}
			success = (depth >=0);
			info = " (depth: "+depth+")";
//			
//			if (!success)
//				System.out.println(s.getLoops());

		}

		if (debug) {
			this.bu_copy();
			View.view(this, "red-"+reduce_cnt+"-"+(reduce_cnt2++)+".dot");
		}
		
		checkCE();
		
		if (print && !CR.NO_OUTPUT) {
			if (success)
				System.out.print("done"+info);
			else
				System.out.print("unsuccessful");
		}
		
		System.out.println(", remains "+remain2elim()+" states");
		return success;
	}

	private int remain2elim() {
		//return this.states.size() - this.initialStates.size() - this.finalStates.size();
		return this.states.size() - initialStates.size() - finalStates.size() - errorStates.size();
	}
	
	private boolean canReduceMin(CAState s) {
		if (isInitial(s) || isFinal(s) || isError(s) || s.loops() > 0 ) {
			return false;
		}
		List<CATransition> l = new LinkedList<CATransition>();
		l.addAll(s.incoming());
		l.addAll(s.outgoing());
		
		if (l.size() > 3) {
			return false;
		}
		
		for (CATransition t : l) {
			if (t.label().isCall()) {
				return false;
			}
		}
		
		return true;
	}
	public void reduce_min() {
		
		List<CAState> l = new LinkedList<CAState>(this.states);
		int cnt = this.states.size();
		int i=0;
		for (CAState s : l) {
			if (canReduceMin(s)) {
				elimNonloopState(s);
				i++;
			}
		}
		System.out.println("Automaton "+this.name+": "+i+"/"+cnt+" ("+(float)i/cnt+" %) states eliminated.");
		
	}
	public boolean reduce_global(DAGNode dagNode, boolean elim_forward, boolean tree_bfs) {

		List<StateWithMetrics> stateMetrics = StateWithMetrics.init(elim_forward, dagNode);

		boolean sth_eliminated = false;
		
		List<StateWithMetrics> unsuccessful = new LinkedList<StateWithMetrics>();

		while (!stateMetrics.isEmpty()) {
			StateWithMetrics smm = stateMetrics.remove(0);
			CAState s = smm.state;

			if (CR.DEBUG > CR.DEBUG_NO)
				printAdjacencyMatrix();

			Set<BaseNode> neighbours = s.getNeighbours();

			if (!eliminateState(s, elim_forward, tree_bfs, true)) {
				unsuccessful.add(smm);
			} else {
				sth_eliminated = true;
			}

			StateWithMetrics.update(stateMetrics, neighbours);
		}

		return sth_eliminated;
		
		//System.err.println("Number of unsuccessful states: " + unsuccessful.size());
	}

	private List<CAState> gen_scc_start(boolean forward, DAGNode dagNode) {
		List<CAState> ret = new LinkedList<CAState>();
		List<BaseNode> aux;
		if (forward)
			aux = dagNode.inStates();
		else
			aux = dagNode.outStates();
		
		for (BaseNode n : aux)
			ret.add((CAState)n);
		return ret;
	}

	private static void gen_todo_add(boolean elim_bfs,
			Deque<List<CAState>> searchTodo, List<CAState> newCand) {
		if (elim_bfs)
			searchTodo.addLast(newCand);
		else
			searchTodo.addFirst(newCand);
	}

	private static Set<CAState> gen_succ_pred(boolean forward, CAState s) {
		Set<CAState> set;
		if (forward)
			set = s.succ();
		else
			set = s.pred();

		set.remove(set);
		return set;
	}

	private static Set<CAState> gen_succ_pred_of_start(boolean forward, CA ca) {
		Set<CAState> set = new HashSet<CAState>();
		for (CAState s : (forward) ? ca.initialStates() : ca.finalStates()) {
			if (forward)
				set.addAll(s.succ());
			else
				set.addAll(s.pred());
		}
		return set;
	}

	private void todo_remove(Deque<List<CAState>> searchTodo, CAState s) {
		Iterator<List<CAState>> iter = searchTodo.iterator();
		while (iter.hasNext()) {
			List<CAState> list = iter.next();
			list.remove(s);
			if (list.isEmpty())
				iter.remove();
		}
	}

	public boolean reduce_systematic(DAGNode dagNode, boolean elim_forward,
			boolean elim_bfs, boolean tree_bfs) {

		boolean sth_eliminated = false;
		
		List<StateWithMetrics> stateMetrics = StateWithMetrics.init(
				elim_forward, dagNode);

		Deque<List<CAState>> searchTodo = new LinkedList<List<CAState>>();
		Deque<CAState> visited = new LinkedList<CAState>();

		List<CAState> scc_start = gen_scc_start(elim_forward, dagNode);
		gen_todo_add(elim_bfs, searchTodo, scc_start);

		while (!searchTodo.isEmpty()) {

			List<CAState> list = searchTodo.getFirst();
			CAState s = StateWithMetrics.bestOf(stateMetrics, list);

			list.remove(s);
			if (list.isEmpty())
				searchTodo.removeFirst();

			if (visited.contains(s))
				continue;

			visited.add(s);
			todo_remove(searchTodo, s);

			Set<BaseNode> neighbours = s.getNeighbours();

			Set<CAState> succ_pred_of_s = gen_succ_pred(elim_forward, s);

			if (!eliminateState(s, elim_forward, tree_bfs, true)) {
				System.err.println("state:" + s);
				System.exit(-2);
			} else {
				sth_eliminated = true;
			}

			Set<CAState> succ_pred_of_start = gen_succ_pred_of_start(
					elim_forward, this);

			succ_pred_of_s.retainAll(succ_pred_of_start);

			if (!succ_pred_of_s.isEmpty()) {
				List<CAState> newCandidates = new LinkedList<CAState>(
						succ_pred_of_s);

				gen_todo_add(elim_bfs, searchTodo, newCandidates);
			}

			StateWithMetrics.update(stateMetrics, neighbours);
		}

		return sth_eliminated;
	}

	private static CAState stateNoInOrOut(Collection<CAState> col, boolean in) {
		for (CAState s : col) {
			Collection<CATransition> c = (in)?s.incoming():s.outgoing();
			if (c.isEmpty()) {
				return s;
			}
		}
		return null;
 	}
	private void addIDbetween(CAState s1, CAState s2, boolean reverse) {
		if (s1.equals(s2))
			return;
	
		CompositeRel id = CompositeRel.createIdentityRelationForSorted(vp.allUnp());
		CATransition tnew;
		if (reverse)
			tnew = CATransition.identity(s2, s1, id, this);
		else
			tnew = CATransition.identity(s1, s2, id, this);
		addTransition_base(tnew);
	}
	
	// normalization: 
	//   -- only one initial, one final, and one error control state
	//   -- initial control state has no incoming edges
	//   -- final control state and error control state have no outgoing edges	
	private void checkMarkedStates() {
		for (CAState s : new LinkedList<CAState>(initialStates)) {
			CAState ss = s;
			if (!ss.incoming().isEmpty()) {
				String str = giveNextStateLabelWithPrefix("_"+s.name());
				ss = getStateWithName(str);
				addIDbetween(s, ss, true);
				this.unsetInitial(s);
				this.setInitial(ss);
			}
		}
		for (CAState s : new LinkedList<CAState>(finalStates)) {
			CAState ss = s;
			if (!ss.outgoing().isEmpty()) {
				String str = giveNextStateLabelWithPrefix("_"+s.name());
				ss = getStateWithName(str);
				addIDbetween(s, ss, false);
				this.unsetFinal(s);
				this.setFinal(ss);
			}
		}
		for (CAState s : new LinkedList<CAState>(errorStates)) {
			CAState ss = s;
			if (!ss.outgoing().isEmpty()) {
				String str = giveNextStateLabelWithPrefix("_"+s.name());
				ss = getStateWithName(str);
				addIDbetween(s, ss, false);
				this.unsetError(s);
				this.setError(ss);
			}
		}
	}

	
	public List<CompositeRel> relBetween(CAState from, CAState to) {
		List<CompositeRel> ret = new LinkedList<CompositeRel>();
		for (CATransition t : incidentTrans(from, to)) {
			ret.add((CompositeRel)t.label());
		}
		return ret;
	}
	public static class Trans4Term {
		public CAState oldInit;
		public Map<CAState,CAState> mI = new HashMap<CAState,CAState>();
		public Map<CAState,CAState> mF = new HashMap<CAState,CAState>();
		public List<CAState> nontriv_states; // states that are not on trivial SCCs
	}
	
	private void addTermSpecial(CAState s, Trans4Term tt) {
		CAState sI = this.addState("I$"+s.name(), CA.fINIT);
		CAState sF = this.addState("F$"+s.name(), CA.fFINAL);
					
		tt.mI.put(s, sI);
		tt.mF.put(s, sF);
		
		CATransition tI = CA.createIdentityTransition(this,sI,s);
		CATransition tF = CA.createIdentityTransition(this,s,sF);
		
		tI.TERM_FLAG = true;
		tF.TERM_FLAG = true;
		
		this.addTransition(tI);
		this.addTransition(tF);
	}
	
	//public Trans4Term trans4term;  
	public Trans4Term transform4termination() {
		assert(this.initialStates.size() == 1);
		
		Trans4Term trans4term = new Trans4Term();
		trans4term.oldInit = this.initialStates.iterator().next();
		trans4term.nontriv_states = new LinkedList<CAState>(this.states);

		// optimization: only control states from non-trivial SCC
		List<List<BaseNode>> sccs = this.findSccs();

		for (CAState s : trans4term.nontriv_states) {
			
			unsetInitial(s);
			unsetFinal(s);
			unsetError(s);
		}
		
		for (List<BaseNode> l : sccs) {
			if (l.size() == 1) {
				CAState aux = ((CAState)l.get(0));
				if (!isInitial(aux) && aux.loops() == 0) {
					trans4term.nontriv_states.remove(aux);
				}
			}
		}
		
		addTermSpecial(trans4term.oldInit,trans4term);
		for (CAState s : trans4term.nontriv_states) {
			
			addTermSpecial(s,trans4term);
		}
		
		return trans4term;
	}
	
	public Trans4Term transform4termination(CAState aFinal) {
		assert(this.initialStates.size() == 1);
		
		Trans4Term trans4term = new Trans4Term();
		trans4term.oldInit = this.initialStates.iterator().next();
		
		
		for (CAState s : this.states) {
			unsetInitial(s);
			unsetFinal(s);
			unsetError(s);
		}
		
		addTermSpecial(trans4term.oldInit,trans4term);
		if (!trans4term.oldInit.equals(aFinal)) {
			addTermSpecial(aFinal,trans4term);
		}
		
		return trans4term;
	}
	
	public static class ReachConfig {
		public Map<String,DisjRel> reach = new HashMap<String,DisjRel>();
	}
	public static ReachConfig reachableConfigurations4Term(CA aCA) {
		
		CA cp = CA.copy(aCA);
		
		//List<CAState> backup_states = new LinkedList<CAState>(cp.states);
		CAState backup_init = cp.initialStates.iterator().next();
		
		Trans4Term tt = cp.transform4reachableStates4Term();
		Summary sum = cp.reduce(true);
		
		ReachConfig ret = new ReachConfig();
		CAState from = tt.mI.get(backup_init);
		for (CAState s : tt.nontriv_states) {
			CAState to = tt.mF.get(s);
			DisjRel disj = new DisjRel();
			for (CATransition t : cp.incidentTrans(from,to)) {
				for (CompositeRel r : ((CompositeRel)t.label()).range()) {
					disj.addDisj(r);
				}
			}
			ret.reach.put(s.name(), disj);
		}
		
		return ret;
	}
	
	public void strengthenWithReach(ReachConfig rc) {
		
		List<CATransition> l = new LinkedList<CATransition>(this.transitions.values()); 
		
		for (CATransition t : l) {
			CAState to = t.to();
			DisjRel disj = rc.reach.get(to.name());
			if (disj.disjuncts().size() == 1) {
				t.rel(disj.disjuncts().get(0));
			} else {
				this.removeTransition(t);
				//for (CompositeRel )
			}
		}
		
	}
	
	public Trans4Term transform4reachableStates4Term() {
		assert(this.initialStates.size() == 1);
		
		Trans4Term trans4term = new Trans4Term();
		trans4term.oldInit = this.initialStates.iterator().next();
		trans4term.nontriv_states = new LinkedList<CAState>(this.states);
		
		for (CAState s : trans4term.nontriv_states) {

			unsetInitial(s);
			unsetFinal(s);
			unsetError(s);
		}
		
		// optimization: only control states from non-trivial SCC
		List<List<BaseNode>> sccs = this.findSccs();
		for (List<BaseNode> l : sccs) {
			if (l.size() == 1) {
				CAState aux = ((CAState)l.get(0));
				if (!isInitial(aux) && aux.loops() == 0) {
					trans4term.nontriv_states.remove(aux);
				}
			}
		}
		
		{//if (s == trans4term.oldInit) {
			CAState sI = this.addState("I$"+trans4term.oldInit.name(), CA.fINIT);
			trans4term.mI.put(trans4term.oldInit, sI);
			CATransition tI = CA.createIdentityTransition(this,sI,trans4term.oldInit);
			tI.TERM_FLAG = true;
			this.addTransition(tI);
		}
		
		
		for (CAState s : trans4term.nontriv_states) {
			
			CAState sF = this.addState("F$"+s.name(), CA.fFINAL);
			trans4term.mF.put(s, sF);
			CATransition tF = CA.createIdentityTransition(this,s,sF);
			tF.TERM_FLAG = true;
			this.addTransition(tF);
		}
		
		return trans4term;
	}

	private boolean checkNoInOut(Collection<CAState> col, boolean in) {
		
		for (CAState s : col) {
			Collection<CATransition> inout = in? s.incoming() : s.outgoing();
			if (inout.size() != 0) {
				return false;
			}
		}
		return true;
	}
	
	private void makeUniqueInitState() {
		if (!checkNoInOut(this.initialStates(), true)) {
			List<CAState> l = new LinkedList<CAState>(initialStates);
			CAState sI = this.addState("$INIT$", CA.fINIT);
			setInitial(sI);
			for (CAState s : l) {
				unsetInitial(s);
				CATransition t = CA.createIdentityTransition(this,sI,s);
				this.addTransition(t);
			}
			
		}
	}
	private void makeUniqueFinalState() {
		if (!checkNoInOut(this.finalStates(), false)) {
			List<CAState> l = new LinkedList<CAState>(finalStates);
			CAState sF = this.addState("$FINAL$", CA.fFINAL);
			setFinal(sF);
			for (CAState s : l) {
				unsetFinal(s);
				CATransition t = CA.createIdentityTransition(this,s,sF);
				this.addTransition(t);
			}
		}
	}
	private void makeUniqueErrorState() {
		if (!checkNoInOut(this.errorStates(), false)) {
			List<CAState> l = new LinkedList<CAState>(errorStates);
			CAState sE = this.addState("$ERROR$", CA.fERROR);
			setError(sE);
			for (CAState s : l) {
				unsetError(s);
				CATransition t = CA.createIdentityTransition(this,s,sE);
				this.addTransition(t);
			}
		}
	}
	
	// initial states with no incoming edges
	// final states with no outgoing edges
	// error states with no outgoing
	public void transform4reachability() {
		
		makeUniqueInitState();
		makeUniqueFinalState();
		makeUniqueErrorState();
	}
	
	public void setPrecondition(DisjRel pre) {
		
		if (pre.isTrue()) {
			return;
		}
		
		makeUniqueInitState();
		
		if (pre.contradictory()) {
			this.transitions.clear();
		}
		
		CAState s_init = this.initialStates.iterator().next();
		
		//CompositeRel identity_rel = CompositeRel.createIdentityRelationForSorted(this.vp.allUnp());
		
		List<CATransition> ts = new LinkedList<CATransition>(s_init.outgoing());
		for (CATransition t : ts) {
			this.removeTransition(t);
		}
		
		for (CompositeRel r : pre.disjuncts()) {
			
			for (CATransition t : ts) {
				CompositeRel[] rel = r.intersect(t.rel());
				CATransition t_new = new CATransition(s_init, t.to(), rel[0], CR.TRANS_ID_NAME, this);
				this.addTransition(t_new);
			}
		}
		
	}
	
	/*
	private static CAState[] permute_topological_sort(DAGNode[] top_sort) {
		int elim_nodes = 0;
		for (DAGNode dn : top_sort)
			elim_nodes += dn.states().size();
		CAState[] ret = new CAState[elim_nodes];
		
		int i=0;
		for (DAGNode dn : top_sort) {
			List<StateWithMetrics> sms = StateWithMetrics.init_rand(dn.states(),1000*dn.states().size());
			for (StateWithMetrics sm : sms)
				ret[i++] = sm.state;
		}
		
		return ret;
	}
	*/

	
	public String statTrans() {
		return CATransition.typeStat(this.transitions());
	}
	public void printStatTrans() {
		System.out.println(statTrans());
	}
	
	
	private CAState reduce_label = null;
	private void reduceLabelSet(CAState aLabel) { reduce_label = aLabel; }
	public CAState reduceLabel() { return reduce_label; }
	
	// boolean global, boolean elim_forward, boolean elim_bfs, boolean tree_bfs
	private int reduce_cnt = 0;
	private int reduce_cnt2;
	private void redCntInc() {
		++reduce_cnt;
		ReduceInfo._redCnt = reduce_cnt;
	}
	
	private Summary createReachResult() {
		
		if (initials().size()+finalStates().size()+errorStates().size() != states().size()) {
			
			return Summary.unsuccessful();
			
		} else {
			
			// create a summary (i.e. keep only global counters, unprimed inputs and primed outputs)
			List<Variable> elimFinal = vp.interprocInvisible(true);
			List<Variable> elimError = vp.interprocInvisible(false);
			
			// hack for termination
			if (TERM && this.isMain) {
				elimFinal.clear();
				elimError.clear();
			}
			
			
			boolean keepAll = isMain && Parameters.isOnParameter(Parameters.T_SUMMARY_WITH_LOCALS);
			
			if (!keepAll) {
				List<CATransition> aux = new LinkedList<CATransition>();
				for (CATransition t : new LinkedList<CATransition>(transitions())) {
					
					if (!isInitial(t.from()))
						throw new RuntimeException("internal error: claim of successful reduction while some state remains");
					
					if (isFinal(t.to())) {
						aux.addAll(t.summary(elimFinal));
					} else if (isError(t.to())) {
						aux.addAll(t.summary(elimError));
					} else {
						throw new RuntimeException("internal error: claim of successful reduction while some state remains");
					}
					
					this.removeTransition(t);
				}
				
				this.addTransitions_strat(aux);
			}
			List<CATransition> reachEnd = new LinkedList<CATransition>();
			List<CATransition> reachError = new LinkedList<CATransition>();
			
			for (CATransition t : transitions()) {
				
				List<CATransition> list = null;
				
				if (!isInitial(t.from()))
					throw new RuntimeException("internal error: claim of successful reduction while some state remains");
				
				if (isFinal(t.to())) {
					list = reachEnd;
				} else if (isError(t.to())) {
					list = reachError;
				} else {
					throw new RuntimeException("internal error: claim of successful reduction while some state remains");
				}
				
				list.add(t);
			}
			
			return Summary.successful(reachEnd, reachError);
		}
	}
	
	private boolean something2eliminate() {
		return initials().size()+finalStates().size()+errorStates().size() != states().size();
	}
	
	private boolean isMain = false;
	public Summary reduce(boolean aIsMain) {
		isMain = aIsMain;
		checkCE();
		return reduce();
	}
	private Summary reduce() {
		
		//System.out.println(this);
		this.bu_copy();
		redCntInc();
		reduce_cnt2 = 1;
		if (debug)
			View.view(this, "red-"+reduce_cnt+".dot");
		
		SCCStrategy scc_strat = Parameters.getSCCStrategy();
		DirStrategy dir_strat = Parameters.getDirStrategy();
		boolean topologyFW = dir_strat == DirStrategy.FW;
		boolean tree_bfs = true; // TODO
		
		System.out.println("Starting reduction "/*+reduce_cnt*/+" of the automaton '"+this.name+"'");
		
		nontermUnreachRemoval();
		
		// a heuristics for models without output variables
		//if (!TERM && vp.globalUnp().length == 0 && vp.portOutSize() == 0) {
		if (!TERM && Parameters.isOnParameter(Parameters.ACCELERATE_WITH_OUTGOING) && 
				vp.globalUnp().length == 0 && vp.portOutSize() == 0) {
			topologyFW = false;
			
			Collection<Variable> primed = this.vp.allPrimedCol();
			List<CATransition> aux_new = new LinkedList<CATransition>();
			for (CAState s : this.finalStates) {
				List<CATransition> aux = new LinkedList<CATransition>(s.incoming());
				for (CATransition t : aux) {
					this.removeTransition(t);
					aux_new.addAll(t.project(primed));
				}
			}
			for (CATransition t : aux_new) {
				this.addTransition(t);
			}
		}
		
		reduce_nonloop_state();
		
		if (!something2eliminate())
			return createReachResult();			
		
		nontermUnreachRemoval();
		
		if (!something2eliminate())
			return createReachResult();
		
		if (Parameters.isOnParameter(Parameters.REDUCE_NO_LOOP))
			return createReachResult();
		
		int cpi; // constant propagation level
		boolean progress;
		do {

		cpi = 0;

		if (Parameters.isOnParameter(Parameters.CONSTPROP)) {
			constantPropagation();
			System.out.println("Constant propagation done.");
		}

		int rem;
		do {
			cpi ++;
			progress = false;
			List<DAGNode> sccGraphInit = sccGraph(this.initialStates);
			// sort of SCCs without starting (trivial) SCCs and without ending (trivial) SCCs
			
			DAGNode[] topologicalSort = BaseGraph.topologicalSort(sccGraphInit, !topologyFW).toArray(new DAGNode[0]);
			// topologicalSort[0] -- initial component
			// topologicalSort[len-2],topologicalSort[len-1] -- final and error components (in any order)
			// note: error and final state don't have to present
			
			
			// hack for termination analysis
			if (TERM) {
				for (int i=0; i<topologicalSort.length; i ++) {
					CAState someState = (CAState)topologicalSort[i].states().iterator().next();
					if (!isFinal(someState) && !isInitial(someState)) {
						progress = reduce_global(topologicalSort[i], topologyFW, tree_bfs);
					}
				}
			} else {
				
				int endScc = 0;
				if (!this.finalStates().isEmpty())
					endScc++;
				if (!this.errorStates().isEmpty())
					endScc++;
				//int iS = topologyFW? 1 : endScc;
				//int iF = topologyFW? topologicalSort.length-endScc : topologicalSort.length-1;
				
				rem = states.size() - (finalStates.size() + errorStates.size() + initialStates.size());
				if (rem != 0) {
					if (scc_strat == SCCStrategy.GLOBAL) {
						for (int i=0; i<topologicalSort.length; i ++) {
							CAState some = (CAState)topologicalSort[i].states().iterator().next();
							if (!isFinal(some) && !isInitial(some) && !isFinal(some) && !isError(some)) {
								progress = progress || reduce_global(topologicalSort[i], topologyFW, tree_bfs);
							}
						}
					}
					
					if (!progress) {
						progress = makeMoreAccelerable();
						if (progress) {
							System.out.println("Splitting transitions.");
						}
					}
				}
				
//				if (scc_strat == SCCStrategy.GLOBAL) {
//					// skip initial, final, error components <1..len-endScc)
//					for (int i=iS; i<iF; i ++) {
//						sth_eliminated = reduce_global(topologicalSort[i], topologyFW, tree_bfs);
//					} 
//				} else if (scc_strat == SCCStrategy.BFS || scc_strat == SCCStrategy.DFS) {
//					boolean scc_bfs = scc_strat == SCCStrategy.BFS;
//					// skip initial, final, error components <1..len-endScc)
//					for (int i=iS; i<iF; i ++) {
//						sth_eliminated = reduce_systematic(topologicalSort[i], topologyFW, scc_bfs, tree_bfs);
//					} 
//				} else if (scc_strat == SCCStrategy.RANDOM) {
//					
//					CA copy = this;
//					
//					sccGraphInit = copy.sccGraph(copy.initialStates);
//					//topologicalSort = topological_sort(topologyFW, sccGraphInit);
//					topologicalSort = BaseGraph.topologicalSort(sccGraphInit, !topologyFW).toArray(new DAGNode[0]);
//					
//					iS = topologyFW? 1 : endScc;
//					iF = topologyFW? topologicalSort.length-endScc : topologicalSort.length-1;
//					
//					// skip initial, final, error components <1..len-3>
//					System.arraycopy(topologicalSort, iS, topologicalSort, 0, iF-iS);
//					CAState[] permutedSort = permute_topological_sort(topologicalSort);
//					
//					long start = System.currentTimeMillis();
//					for (CAState s : permutedSort) {
//						copy.eliminateState(s, topologyFW, tree_bfs, true);
//					}
//					System.out.println("#####\n"+Arrays.toString(permutedSort));
//					System.out.println(System.currentTimeMillis() - start);
//				}
			
			}
			
			nontermUnreachRemoval();
			
		} while (progress);
		
		
		} while (cpi>1 || progress);
		
		return createReachResult();
	}
	
	boolean makeMoreAccelerable() {
		boolean ret = false;
		List<CATransition> aux = new LinkedList<CATransition>(this.transitions());
		for (CATransition t : aux) {
			
			if (!t.from().equals(t.to())) {
				continue;
			}
			
			CATransition[] split = t.makeMoreAccelerable();
			if (split != null) {
				ret = true;
				this.removeTransition(t);
				for (CATransition tt : split) {
					this.addTransition(tt);
				}
			}
		}
		return ret;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////



	public boolean isTREXCompatible() {
		return isFASTCompatible();
	}
	public boolean isFASTCompatible() {
		int v_cnt = varPool().allUnp().length * 2;
		for (CATransition trans : transitions.values()) {
			if (!trans.isFASTCompatible(v_cnt))
				return false;
		}
		return true;
	}

	public boolean isARMCCompatible() {
		for (CATransition trans : transitions.values()) {
			if (!trans.isARMCCompatible())
				return false;
		}
		return true;
	}

	public boolean isFlat() {
		this.findSccs();
		CA.countSccOutEdges(this.states());
		for (CAState s : states)
			if (s.sccOutEdges() > 1)
				return false;

		return true;
	}

	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	
	private static class StatePair {
		public CAState ca1State;
		public CAState ca2State;

		public StatePair(CAState aCA1state, CAState aCA2state) {
			ca1State = aCA1state;
			ca2State = aCA2state;
		}
		
		// creates product state, adds it to given CA
		public CAState createProductState(CA aCA) {
			String s = ca1State.name().equals(ca2State.name())? ca1State.name() : ca1State.name() + ca2State.name();
			CAState product = new CAState(s);
			aCA.states.add(product);
			return product;
		}

		public String toString() {
			return "[" + ca1State + "," + ca2State + "]";
		}

		public boolean equals(Object aObject) {
			return (aObject instanceof StatePair
					&& ca1State.equals(((StatePair) aObject).ca1State) && ca2State
					.equals(((StatePair) aObject).ca2State));
		}

		public int hashCode() {
			return ca1State.hashCode() + ca2State.hashCode();
		}

		public boolean isFinal(CA aCA1, CA aCA2) {
			return aCA1.isFinal(this.ca1State) && aCA2.isFinal(this.ca2State);
		}
		public boolean isError(CA aCA1, CA aCA2) {
			return aCA1.isError(this.ca1State) && aCA2.isError(this.ca2State);
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	// assumption: useless states are removed
	// note: just over-approximation
	public boolean isEmptyOveapprox() {
		return this.transitions.isEmpty();
	}

	public static CA product(CA aCA1, CA aCA2) {
		
		if (aCA1.isEmptyOveapprox())
			return aCA1;
		else if (aCA2.isEmptyOveapprox())
			return aCA2;
		
		CA prodCA = new CA();
		
		
		// add missing variables to both automata
//		Set<String> vars = aCA1.variableNames();
//		vars.addAll(aCA2.variableNames());
//		prodCA.addVariables(vars);
		prodCA.vp = VariablePool.product(aCA1.vp, aCA2.vp);
		
		Map<StatePair, CAState> seen = new HashMap<StatePair, CAState>();
		Set<StatePair> processed = new HashSet<StatePair>();
		
		Queue<StatePair> enqueued = new LinkedList<StatePair>();
		
		for (CAState ca1Init : aCA1.initialStates) {
			for (CAState ca2Init : aCA2.initialStates) {
				
				StatePair prodInit = new StatePair(ca1Init,ca2Init);
				CAState prod = prodInit.createProductState(prodCA);
				prodCA.setInitial(prod);
				
				seen.put(prodInit, prod);
				enqueued.add(prodInit);
			}
		}
		
		while (!enqueued.isEmpty()) {
			
			StatePair pairFrom = enqueued.poll();
			
			if (processed.contains(pairFrom))
				continue;
			
			CAState ca1From = pairFrom.ca1State;
			CAState ca2From = pairFrom.ca2State;
			
			CAState prodFromState = seen.get(pairFrom);
			processed.add(pairFrom);
			
			for (CATransition ca1Trans : ca1From.outgoing()) {
				for (CATransition ca2Trans : ca2From.outgoing()) {
					
					StatePair pairTo = new StatePair(ca1Trans.to(), ca2Trans.to());
					
					CompositeRel[] prod_rel = ca1Trans.rel().intersect(ca2Trans.rel());
					
					if (prod_rel.length != 0) {
						
						CAState prodTo = seen.get(pairTo);
						if (prodTo == null) {
							prodTo = pairTo.createProductState(prodCA);
							seen.put(pairTo, prodTo);
							enqueued.add(pairTo);
						}
						
						if (pairTo.isFinal(aCA1, aCA2)) {
							prodCA.setFinal(prodTo);
						}
						if (pairTo.isError(aCA1, aCA2)) {
							prodCA.setError(prodTo);
						}
						
						CATransition prodTrans = new CATransition(prodFromState, prodTo, prod_rel[0], prodCA);
						
						prodCA.addTransition(prodTrans);
					}
				}
			}
		
		}
		
		prodCA.SILreduce();
		//prodCA.reduce();
		//prodCA.endLog();
		
		return prodCA;
	}
	
	public void SILreduce() {
		nontermUnreachRemoval();
		//loopUnrolling(1);
	}	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	private void removeLoops(CAState s) {
		Collection<CATransition> outcoming = new LinkedList<CATransition>(s.outgoing());
		for (CATransition t : outcoming) {
			if (t.to().equals(t.from()))
				this.removeTransition(t);
		}
	}
	private void splitStateMoveOutcoming(CAState s, CAState split_in, CAState split_out,
			Collection<CATransition> unrollings) {

		Collection<CATransition> loops = new LinkedList<CATransition>();
		Collection<CATransition> outs = new LinkedList<CATransition>();
		for (CATransition t : s.outgoing()) {
			if (t.to().equals(s)) {
				loops.add(t);
			} else {
				outs.add(t);
			}
		}
  
		for (CATransition t : outs) {
			removeTransition(t);
			addTransition_base(t.reconnect(split_out, t.to()));
		}
		for (CATransition t : loops) {
			removeTransition(t);
		}
		
		Collection<CATransition> unroll_move = new LinkedList<CATransition>();
		for (CATransition t : unrollings)
			unroll_move.add(t.reconnect(split_in, split_out));
		addTransitions_strat(unroll_move);
		
		Collection<CATransition> ins = new LinkedList<CATransition>(s.incoming());
		for (CATransition t : ins) {
			if (t.from().equals(t.to()))
				continue;
			
			this.removeTransition(t);
			//t.to(split_in);
			this.addTransition_base(t.reconnect(t.from(), split_in));
		}
		
		this.removeState(s);
	}

	public void replaceMultiLoop(CAState s, Collection<CATransition> unrollings) {
		replaceMultiLoop_WithSplitState(s, unrollings);	
	}
	
	public void replaceMultiLoop_WithSplitState(CAState s, Collection<CATransition> unrollings) {
		
		if (TERM) {
			List<CATransition> aux_in = new LinkedList<CATransition>();
			List<CATransition> aux_out = new LinkedList<CATransition>();
			List<CATransition> aux_new = new LinkedList<CATransition>();
			
			for (CATransition t : s.incoming()) {
				if (t.from() != t.to()) {
					aux_in.add(t);
				}
			}
			for (CATransition t : s.outgoing()) {
				if (t.from() != t.to()) {
					aux_out.add(t);
				}
			}
			
			CATransition.compose(aux_in, aux_out, aux_new);
			addTransitions_strat(aux_new);
		}
		
		String split_in_name = this.giveNextStateLabelWithPrefix(s.name());
		String split_out_name = this.giveNextStateLabelWithPrefix(s.name());
		int flags = stateFlags(s);
		CAState split_in = this.addState(split_in_name, flags);
		CAState split_out = this.addState(split_out_name, flags);
		
		msplit_in.put(s, split_in);
		msplit_out.put(s, split_out);
		
		CAState.bindSplit(s, split_in, split_out);
		
		splitStateMoveOutcoming(s, split_in, split_out, unrollings);
		
		elimNonloopState(split_in);
		elimNonloopState(split_out);
	}

	
	public void replaceMultiLoop_NoSplitState(CAState s, Collection<CATransition> closure_transitions) {
		
		removeLoops(s);
		
		// create a collection of new transitions
		Collection<CATransition> new_transitions = new LinkedList<CATransition>();

		for (CATransition tIn : s.incoming()) {

			// loop in number of iterations >=0

			for (CATransition tClosure : closure_transitions) {
				Collection<CATransition> tTmp_col = composeTransitions(tIn,tClosure);

				for (CATransition tTmp : tTmp_col) {

					for (CATransition tOut : s.outgoing()) {

						Collection<CATransition> tNew_col = composeTransitions(tTmp, tOut);
						new_transitions.addAll(tNew_col);
					}
				}
			}

			// loop in zero iterations
			for (CATransition tOut : s.outgoing()) {

				Collection<CATransition> tNew_col = composeTransitions(tIn,tOut);
				new_transitions.addAll(tNew_col);
			}
		}

		this.removeState(s);

		addTransitions_strat(new_transitions);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	

	public void printAdjacencyMatrix() {

		int size = this.states.size();
		CAState[] arr = new CAState[size];

		Map<CAState, Integer> m = new HashMap<CAState, Integer>();
		{
			int i = 0;
			for (CAState s : this.states) {
				m.put(s, new Integer(i));
				arr[i] = s;
				i++;
			}
		}

		int[][] adj = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = i; j < size; j++) {
				adj[i][j] = 0;
				adj[j][i] = 0;
			}
		}

		for (CATransition t : this.transitions.values()) {
			int r = m.get(t.from()).intValue();
			int c = m.get(t.to()).intValue();
			adj[r][c]++;
		}

		System.out.println("States: " + Arrays.toString(arr));
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				System.out.print("|" + adj[i][j]);
			}
			System.out.println("|");
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	/////////////////////////  CONSTANT PROPAGATION ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	public static class CPState {
		CAState s;
		boolean fw = false;
		boolean bw = false;
		
		public void setFW() { fw = true; }
		public void setBW() { bw = true; }
		
		public CPState(CAState aS) {
			s = aS;
		}
		
		public String toString() {
			return "["+s.toString()+",fw="+fw+",bw="+bw+"]";
		}
	}
	
	public static class CPWorklist {
		List<CPState> W = new LinkedList<CPState>();
		
		private CPState find(CAState s) {
			CPState ss = null;
			Iterator<CPState> iter = W.iterator();
			while (iter.hasNext()) {
				CPState sss = iter.next();
				if (sss.s.equals(s)) {
					ss = sss;
					break;
				}
			}
			
			if (ss == null) {
				ss = new CPState(s);
				W.add(ss);
			}
			
			return ss;
		}
		public void findSetFW(CAState s) {
			find(s).setFW();
		}
		public void findSetBW(CAState s) {
			find(s).setBW();
		}
		public boolean isEmpty() {
			return W.isEmpty();
		}
		public CPState removeFirst() {
			return W.remove(0);
		}
		
		public String toString() { return W.toString(); }
	}
	
	public static class CPFunc<T> {
		Map<T, ConstProps> map = new HashMap<T, ConstProps>();
		
		public boolean strictlyBigger(T t, ConstProps cps) {
			int sn = cps.size();
			ConstProps c = map.get(t);
			int so = (c == null)? 0 : c.size();
			return sn > so;
		}

		public ConstProps setMinus(ConstProps cps, T t) {
			ConstProps tmp = map.get(t);
			if (tmp == null)
				return new ConstProps(cps);
			else
				return cps.setMinus(tmp);
		}
		// returns set difference
		public ConstProps putIfSuperset(T t, ConstProps cps) {
			if (cps.isEmpty())
				return new ConstProps();
			
			if (map.containsKey(t)) {
				ConstProps ret;
				if (!(ret = setMinus(cps, t)).isEmpty()) {
					map.put(t, cps);
				}
				return ret;
			} else {
				map.put(t, cps);
				return new ConstProps(cps);
			}
		}
		
		public ConstProps get(T t) {
			return map.get(t);
		}
		
		public String toString() { return map.toString(); }
	}
	
	private ConstProps intersection(CPFunc<CATransition> func, Collection<CATransition> col) {
		
		if (col.size() == 1) {
			ConstProps ret = func.get(col.iterator().next());
			if (ret == null)
				return new ConstProps();
			else
				return new ConstProps(ret);
		}
		
		if (col.isEmpty())
			return new ConstProps();
		
		for (CATransition t : col)
			if (func.get(t) == null)
				return new ConstProps();
		
		Collection<ConstProp> intersect = new LinkedList<ConstProp>();
		ConstProps first = func.get(col.iterator().next());
		for (ConstProp cp : first.getAll()) {
			boolean all = true;
			for (CATransition t : col) {
				if (!func.get(t).contains(cp)) {
					all = false;
					break;
				}
			}
			if (all)
				intersect.add(new ConstProp(cp));
		}
		
		return new ConstProps(intersect);
	}
	

	private void propagate(ConstProps newProps, Collection<CATransition> col) {
		// rename
		newProps.switchPrimes();
		
		for (CATransition t : col) {
			// update transition
			updateTransition(t, newProps);
		}
	}

	private ConstProps tofromUnp(ConstProps cps, boolean fw) {
		if (fw)
			cps.switchPrimes();
		return cps;
	}
	
	private void processWorklist(
			CAState s, boolean fw, 
			CPFunc<CATransition> tFwGen, CPFunc<CATransition> tBwGen, 
			CPFunc<CAState> sFunc,
			CPWorklist worklist) {
		
		Collection<CATransition> col1 = (fw)? s.incoming() : s.outgoing();
		Collection<CATransition> col2 = (!fw)? s.incoming() : s.outgoing();
		CPFunc<CATransition> tFunc = (fw)? tFwGen : tBwGen;
		
		if (col1.isEmpty() || col2.isEmpty())
			return;
		
		ConstProps intersect = intersection(tFunc, col1);
		intersect = tofromUnp(intersect, fw);
		
		if (sFunc.strictlyBigger(s, intersect)) {
			ConstProps newProps = sFunc.putIfSuperset(s, intersect);
			newProps = tofromUnp(newProps, fw);
			
			if (newProps.isEmpty())
				throw new RuntimeException("internal error during constant propagation");

			propagate(newProps, col2);
		}
		
	}
	
	public void updateGenFunc(CATransition t, boolean fw, CPFunc<CATransition> tFunc, CPWorklist worklist) {
		ConstProps cps;
		if (fw)
			cps = t.rel().outConst();
		else
			cps = t.rel().inConst();
		if (!cps.isEmpty()) {
			if (!tFunc.putIfSuperset(t, cps).isEmpty()) {
				if (fw)
					worklist.findSetFW(t.to());
				else
					worklist.findSetBW(t.from());
			}
		}
	}
	public void updateGenFunc(Collection<CATransition> col, CPFunc<CATransition> tFwGen, CPFunc<CATransition> tBwGen, CPWorklist worklist) {

		for (CATransition t : col) {
			updateGenFunc(t, false, tBwGen, worklist);
			updateGenFunc(t, true, tFwGen, worklist);
		}
	}
	
	public void constantPropagation() {
		
		CPFunc<CATransition> tFwGen = new CPFunc<CATransition>();
		CPFunc<CATransition> tBwGen = new CPFunc<CATransition>();
		
		CPFunc<CAState> sFunc = new CPFunc<CAState>();
		
		CPWorklist worklist = new CPWorklist();
		
		updateGenFunc(transitions(), tFwGen, tBwGen, worklist);
		
		while (!worklist.isEmpty()) {
			
			CPState ss = worklist.removeFirst();
			
			if (ss.fw) {
				propageteLoops(ss.s, true, tFwGen, tBwGen, sFunc, worklist);
				processWorklist(ss.s, true, tFwGen, tBwGen, sFunc, worklist);
				updateGenFunc(ss.s.outgoing(), tFwGen, tBwGen, worklist);
			}
			if (ss.bw) {
				propageteLoops(ss.s, false, tFwGen, tBwGen, sFunc, worklist);
				processWorklist(ss.s, false, tFwGen, tBwGen, sFunc, worklist);
				updateGenFunc(ss.s.incoming(), tFwGen, tBwGen, worklist);
			}
		}
	}
	
	private Set<Variable> commonIdentVars(Collection<CATransition> col) {
		
		Set<Variable> ret = new HashSet<Variable>();
		
		Iterator<CATransition> iter = col.iterator();
		if (iter.hasNext())
			ret.addAll(iter.next().rel().identVars());
		while (iter.hasNext()) {
			ret.retainAll(iter.next().rel().identVars());
		}
		
		return ret;
	}
	
	private void propageteLoops(CAState s, boolean fw, 
			CPFunc<CATransition> tFwGen, CPFunc<CATransition> tBwGen, 
			CPFunc<CAState> sFunc,
			CPWorklist worklist) {
		
		if (isInitial(s) || isFinal(s))
			return;
		
		Collection<CATransition> col = (fw)? s.incoming() : s.outgoing();
		CPFunc<CATransition> tFunc = (fw)? tFwGen : tBwGen;
		
		Collection<CATransition> loops = new LinkedList<CATransition>();
		Collection<CATransition> nonloops = new LinkedList<CATransition>();
		
		for (CATransition t : col) {
			if (t.from().equals(t.to())) 
				loops.add(t);
			else
				nonloops.add(t);
		}		
				
		ConstProps intersect = intersection(tFunc, nonloops);
		intersect = tofromUnp(intersect, fw);
		
		// variable x is propagated only if all loops have x'=x as a constraint 
		intersect.keepOnly(commonIdentVars(loops));
		
		if (sFunc.strictlyBigger(s, intersect)) {
			ConstProps newProps = sFunc.setMinus(intersect, s);
			newProps = tofromUnp(newProps, fw);
			
			if (newProps.isEmpty())
				throw new RuntimeException("internal error during constant propagation");

			propagate(newProps, loops);
			
			updateGenFunc(loops, tFwGen, tBwGen, worklist);
		}
		
	}
	
	public void updateTransition(CATransition t, ConstProps cps) {
		t.update(cps);
		if (t.isContradictory())
			removeTransition(t);
	}

	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	
	public static void printTransitionsOrdered(StringBuffer sb, List<CATransition> l) {
		boolean tmp = DBRel.TOLIN_COMPACT;
		DBRel.TOLIN_COMPACT = false;
		
		LinearRel.LROrdered[] arr = new LinearRel.LROrdered[l.size()];
		int i = 0;
		for (CATransition t : l) {
			LinearRel lr = t.rel().toLinearRel();
			//sb.append(lr.toSBOrder() + "\n");
			arr[i++] = lr.ordered();
		}
		
		Arrays.sort(arr);
		for (LinearRel.LROrdered lro : arr)
			sb.append(lro.toSB() + "\n");
		
		DBRel.TOLIN_COMPACT = tmp;
	}
	public StringBuffer printTransitionsOrderedViaIncidence(Set<CAState> onlyStates) {
		StringBuffer sb = new StringBuffer();
		for (CAState from : onlyStates) {
			for (CAState to : onlyStates) {
				List<CATransition> l = incidentTrans(from, to);
				
				if (l.isEmpty())
					continue;
				
				sb.append("########### "+from+"->"+to+" ("+l.size()+") ############\n");
				printTransitionsOrdered(sb,l);
			}
		}
		
		return sb;
	}
	public StringBuffer printTransitionsOrderedViaIncidence() {
		return printTransitionsOrderedViaIncidence(this.states);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	// works properly only if all control states are reachable from the init
	public String toString() {
		return toString(false);
	}
	public String toString(boolean sortTransUniqueName) {

		Set<CAState> o_initialStates = initialStates;
		Set<CAState> o_finalStates = finalStates;
		Set<CAState> o_errorStates = errorStates;
		//Map<Integer, CATransition> o_transitions = transitions;

		if (!CA.sortedCollections) {
			o_initialStates = new TreeSet<CAState>(initialStates);
			o_finalStates = new TreeSet<CAState>(finalStates);
			//o_transitions = new TreeMap<Integer, CATransition>(transitions);
		}

		final String separator = ", ";

		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, 0);

		iw.writeln("automaton " + name + " {");

		iw.indentInc();

		vp.print(iw);
		
		iw.writeln("initial" + " {"
				+ CR.collectionToString(o_initialStates, separator) + "}");
		iw.writeln("final" + " {"
				+ CR.collectionToString(o_finalStates, separator) + "}");
		iw.writeln("error" + " {"
				+ CR.collectionToString(o_errorStates, separator) + "}");

		iw.writeln();
		// transitions
		if (!sortTransUniqueName) {
			
			List<DAGNode> dagInit = this.sccGraph(initialStates());
			List<BaseArc> tsorted = BaseGraph.arcSortViaDag(dagInit, false); // forward topological sort
			
			for (BaseArc a : tsorted) {
				CATransition t = (CATransition)a;
				iw.write(t.toSBuf(CR.eOUT_FLATA, iw.indentCnt()));
			}
//			for (CATransition t : transitions.values()) {
//				iw.write(t.toSBuf(CR.eOUT_FLATA, iw.indentCnt()));
//			}
		} else {
			Map<String, CATransition> o_transitions = new TreeMap<String, CATransition>();
			for (CATransition t : transitions.values())
				o_transitions.put(t.name(), t);
			for (CATransition t : o_transitions.values()) {
				iw.write(t.toSBuf(CR.eOUT_FLATA, iw.indentCnt()));
			}
		}
		
		iw.indentDec();

		iw.writeln("}\n");

		return iw.getWriter().toString();
	}
	
	private static StringBuffer fstRegion(Collection<CAState> states) {
		StringBuffer sb = new StringBuffer("{");
		boolean b = true;
		for (CAState s : states) {
			if (!b)
				sb.append(" || ");
			else {
				b = false;
			}
			sb.append("state = "+s);
		}
		sb.append("}");
		return sb;
	}
	
	public String toStringFAST_nostrategy() { return toStringFAST_ASPIC(false,true); }
	public String toStringFAST() { return toStringFAST_ASPIC(true,true); }
	public String toStringASPIC() { return toStringFAST_ASPIC(true,false); }
	private String toStringFAST_ASPIC(boolean strategy, boolean fast) {

		// ASPIC -- error region can be one control state only
		String aspic_errorstate = "uniqueErrState";
		
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, 0);

		iw.writeln("model " + this.name + " {");

		iw.indentInc();
		iw.writeln("var " + CR.collectionToString(vp.allUnpCol(), ", ")
				+ ";");
		String statesList = CR.collectionToString(states, ", ");
		if (!fast) {
			statesList += ", "+aspic_errorstate;
		}
		iw.writeln("states "
				+ statesList + ";\n");
		
		

		int t_counter = 1;
		for (CATransition t : transitions()) {
			if (t.name() == null) {
				t.name("tt_"+(t_counter++));
			}
			
			iw.write(t.toSBuf(CR.eOUT_FAST, iw.indentCnt()));
		}

		

		if (!fast) {
			//TODO
			for (CAState s : this.errorStates) {
				iw.writeln("transition t"+(t_counter++)+" := {");
				iw.writeln("  from := "+s+";");
				iw.writeln("  to := "+aspic_errorstate+";");
				iw.writeln("  guard := true;");
				iw.writeln("  action := ;");
				iw.writeln("};");
			}
		}
		
		iw.indentDec();

		iw.writeln("}");
		
		String aux = iw.getWriter().toString();
		String initStates = fstRegion(this.initialStates).toString();
		String errorStates = fstRegion(this.errorStates).toString();
		if (!fast) {
			aux = aux.replaceAll("init", "ii");
			aux = aux.replaceAll("bad", "bb");
			initStates = initStates.replaceAll("init", "ii");
			initStates = initStates.replaceAll("bad", "bb");
			errorStates = errorStates.replaceAll("init", "ii");
			errorStates = errorStates.replaceAll("bad", "bb");
		}
		
		sw = new StringWriter();
		iw = new IndentedWriter(sw, 0);
		
		if (strategy) {
			iw.writeln("strategy s1"+" {");
			
			if (fast) {
				iw.writeln("  setMaxState(0);");
				iw.writeln("  setMaxAcc(100);");
			}

			String regpref = fast? "r" : "";
			
			iw.writeln("  Region "+regpref+"init := " + initStates + ";");
			
			if (fast) {
				iw.writeln("  Region "+regpref+"bad := " + errorStates + ";");
			} else {
				//TODO
				iw.writeln("  Region "+regpref+"bad := {state = " + aspic_errorstate + "};");
			}
			
			if (fast) {
				iw.write  ("  Transitions ttt := {");
				Iterator<CATransition> iter = transitions().iterator();
				while (iter.hasNext()) {
					iw.write(iter.next().name());
					if (iter.hasNext())
						iw.write(",");
				}
				iw.writeln("};");
				iw.writeln("  Region rreach := post*("+regpref+"init, ttt);");
				iw.writeln("  Region rresult := rreach && "+regpref+"bad;");
				iw.writeln("  if (isEmpty(rresult)) ");
				iw.writeln("    then print(\" safety holds for all parameters \");");
				iw.writeln("    else print(\" unsafe \");");
				iw.writeln("  endif");
			}
			iw.writeln("}");
		}
		
		return aux+iw.getWriter().toString();
	}
	
	public String toStringTREX() {
		
		String initKEY="QQQQQQQQ";
		String initName="qqqq";
		CAState initState = new CAState(initName);
		
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, 0);

		iw.writeln("system " + this.name + ";");
		
		Iterator<Variable> iter = vp.allUnpCol().iterator();
		if (iter.hasNext())
			iw.writeln("var " + iter.next() + " : int;");
		while (iter.hasNext())
			iw.writeln("    " + iter.next() + " : int;");
		
		iw.writeln("process proc" + this.name + ";");
		
		iw.write("state "+initName+" : " + initKEY + "; ");
		for (CAState s : this.states)
			iw.write(s + "; ");
		iw.writeln();
		
		iw.writeln("transition");
		CATransition tmp = CA.createIdentityTransition(this, initState);
		for (CAState ss : this.initialStates) {
			tmp.to(ss);
			iw.write(tmp.toSBufTREX(iw.indentCnt()));
		}
		
		for (CATransition t : transitions()) {
			iw.write(t.toSBufTREX(iw.indentCnt()));
		}
		
		String aux = sw.getBuffer().toString();
		aux = aux.replaceAll("init", "iii");
		aux = aux.replaceAll(initKEY, "init");
		aux = aux.replaceAll("'=", ":=");
		
		return aux;
	}
	
	
	private StringBuffer toSBufARMC_renamed(StringBuffer transitions,
			Collection<String> initials, Collection<String> error,
			Collection<String> counters) {
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, 0);

		iw.writeln(":- multifile r/5, implicit_updates/0, var2names/2, preds/2, cube_size/1, start/1, refinement/1, error/1.");
		iw.writeln("refinement(inter).");
		iw.writeln("cube_size(1).");

		iw.writeln("");

		iw.writeln("%initial states");
		for (String str : initials) {
			iw.writeln("start(pc(" + str + ")).");
		}

		iw.writeln("%error states");
		for (String str : error) {
			iw.writeln("error(pc(" + str + ")).");
		}

		// Collection<String> counters =
		// CR.collectionToStringCollection(unprimedVariables);
		Collection<String> countersPairs = new ArrayList<String>();
		for (Variable var : vp.allUnp()) {
			countersPairs.add("(" + var.toString(Variable.ePRINT_p_armcPref)
					+ ",'" + var.toString(Variable.ePRINT_p_armcPref) + "')");
		}
		String dataStr = "data(" + CR.collectionToString(counters, ", ") + ")";
		String pDataStr = "p(_, " + dataStr + ")";
		String var2names = "var2names(" + pDataStr + ", ["
				+ CR.collectionToString(countersPairs, ", ") + "]).";

		iw.writeln();
		iw.writeln(var2names);
		iw.writeln("preds(" + pDataStr + ", []).");
		iw.writeln("cube_size(1).");
		iw.writeln("implicit_updates.");

		iw.writeln();
		iw.writeln(transitions);

		return sw.getBuffer();
	}

	public String toStringARMC() {

		Collection<String> counters = CR.collectionToStringCollectionUpperCase(vp.allUnpCol());
		Collection<String> countersp = new ArrayList<String>();
		for (String s : counters) {
			countersp.add(s + Variable.ARMCPRIME);
		}

		String dataStr = "data(" + CR.collectionToString(counters, ", ") + ")";
		String datapStr = "data(" + CR.collectionToString(countersp, ", ")
				+ ")";

		StringBuffer transitions = new StringBuffer();
		
		Map<String, CATransition> o_transitions = new TreeMap<String, CATransition>();
		int i = 0;
		for (CATransition t : this.transitions.values()) {
			i++;
			String tname = t.name();
			if (tname == null) {
				tname = "t"+(i++);
			}
			t.name(tname);
			o_transitions.put(tname, t);
		}
		for (CATransition t : o_transitions.values()) {
			transitions.append(t.toSBufARMC(1, dataStr, datapStr, t.name()));
		}
		
//		for (CATransition t : transitions()) {
//			transitions.append(t.toSBufARMC(1, dataStr, datapStr, t.name()));
//		}

		return toSBufARMC_renamed(transitions,
				CR.collectionToStringCollection(initials()),
				CR.collectionToStringCollection(errorStates()), counters)
				.toString();

	}

	private static void marking(IndentedWriter iw, String s, Collection<CAState> col) {
		if (col.isEmpty())
			return;
		StringBuffer sb = new StringBuffer(s+" ");
		Iterator<CAState> i = col.iterator();
		while (i.hasNext()) {
			sb.append(i.next().name());
			if (i.hasNext()) sb.append(",");
		}
		sb.append(";");
		iw.writeln(sb);
	}

	
	public void toStringNTS(IndentedWriter iw) {
		
		iw.writeln(this.name()+" {");
		iw.indentInc();
		
		// declaration
		if (vp.portIn().length > 0) {
			StringBuffer sb = new StringBuffer("in ");
			sb.append(VariablePool.asString(vp.portIn()));
			sb.append(" : int;");
			iw.writeln(sb);
		}
		if (vp.portOut().length > 0) {
			StringBuffer sb = new StringBuffer("out ");
			sb.append(VariablePool.asString(Variable.counterpartArray(vp.portOut())));
			sb.append(" : int;");
			iw.writeln(sb);
		}
		Variable[] aux = vp.localUnp_notInOut();
		if (aux.length > 0) {
			StringBuffer sb = VariablePool.asString(aux);
			sb.append(" : int;");
			iw.writeln(sb);
		}
		
		// state marking
		marking(iw,"initial",this.initialStates);
		marking(iw,"final",this.finalStates);
		marking(iw,"error",this.errorStates);
		
		// transitions
		for (CATransition t : this.transitions.values()) {
			StringBuffer sb = new StringBuffer();
			if (t.name() != null) sb.append(t.name()+": ");
			sb.append(t.from().name()+" -> "+t.to().name()+" { ");
			
			sb.append(t.label().toSB_NTS());

			sb.append(" } ");
			iw.writeln(sb);
		}
		
		iw.indentDec();
		iw.writeln("}");
	}
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////


	/*
	// used only for export to .dot file
	private class TransitionLabel {
		LinearRel constrs = new LinearRel();
		Set<CATransition> transitions = new HashSet<CATransition>();

		public boolean equals(Object other) {
			if (!(other instanceof TransitionLabel))
				return false;
			return ((TransitionLabel) other).constrs.equals(this.constrs);
		}

		public int hashCode() {
			return constrs.hashCode();
		}

		public TransitionLabel(CATransition aT) {
			LinearRel lr = aT.rel().toLinearRel();
			constrs.addAll(lr.guards());
			constrs.addAll(LinearRel.removeImplActions(lr.actions()));

			transitions.add(aT);
		}

		public String toString() {
			return "label:\n" + constrs + "\n\ntransitions:\n" + transitions;
		}
	}
	*/

	private boolean printDotTrans = true;
	public void setDotTrans() {printDotTrans=true;}
	public void unsetDotTrans() {printDotTrans=false;}
	
	public StringBuffer toDotLang(String aName) {
		return toDotLang_mark(aName, new LinkedList<CAState>(), new LinkedList<CATransition>());
	}
	public StringBuffer toDotLang(String aName, String info) {
		return toDotLang_mark(aName, new LinkedList<CAState>(), new LinkedList<CATransition>(), info);
	}
	public StringBuffer toDotLang_mark(String aName, Collection<CAState> mark_states, Collection<CATransition> mark_transitions) {
		return toDotLang_mark(aName, mark_states, mark_transitions, null);
	}
	public StringBuffer toDotLang_mark(String aName, Collection<CAState> mark_states, Collection<CATransition> mark_transitions, String info) {
		StringBuffer sb = new StringBuffer();

		sb.append("digraph " + aName + " {\n");

		// initial states
		sb.append("node [shape = diamond];");
		for (CAState s : this.bu_initialStates) {
			sb.append(" " + s.name());
			if (mark_states.contains(s))
				sb.append(" [color=red]");
		}
		sb.append(";\n");

		// final states
		sb.append("node [shape = doublecircle];");
		if (bu_finalStates.size() != 0) {
			for (CAState s : this.bu_finalStates) {
				sb.append(" " + s.name());
				if (mark_states.contains(s))
					sb.append(" [color=red]");
			}
			sb.append(";\n");
		}

		// info
		if (info != null) {
			String dd = "dd";
			//sb.append("node [shape = doublecircle]; " + dd);
			sb.append( dd + " -> " + dd
					+ " [ label = \"" + info + "\"];\n");
		}
		
		// setting for the rest
		sb.append("node [shape = circle];\n");
		for (CAState m : mark_states) {
			if (!bu_initialStates.contains(m) && !bu_finalStates.contains(m)) {
				sb.append(" " + m.name());
				sb.append(" [color=red]");
			}
		}

			for (CATransition t : this.bu_transitions.values()) {

				StringBuffer sbT = (printDotTrans)? t.toStringBufferDot() : new StringBuffer();
				String color = (mark_transitions.contains(t))? "color=red, " : "";
				sb.append(t.from().name() + " -> " + t.to().name()
						+ " [ "+color+"label = \"" + sbT + "\"];\n");
	
			}
		
//		Map<TransitionLabel, List<CATransition>> labels = new HashMap<TransitionLabel, List<CATransition>>();
//		{ // create a map from TransitionLabel to transition list
//
//			for (CATransition t : this.transitions.values()) {
//				TransitionLabel tl = new TransitionLabel(t);
//
//				List<CATransition> val = labels.get(tl);
//				if (val != null) {
//					val.add(t);
//				} else {
//					val = new ArrayList<CATransition>();
//					val.add(t);
//					labels.put(tl, val);
//				}
//			}
//		}
//		
//		StringBuffer commonLabels = new StringBuffer();
//		// labels (transitions)
//		for (java.util.Map.Entry<TransitionLabel, List<CATransition>> entry : labels
//				.entrySet()) {
//			List<CATransition> val = entry.getValue();
//
//			if (val.size() == 1) {
//				CATransition t = val.iterator().next();
//
//				StringBuffer sbT = t.toStringBufferDot();
//				sb.append(t.from().name() + " -> " + t.to().name()
//						+ " [ label = \"" + sbT + "\"];\n");
//			} else {
//				Iterator<CATransition> iter = val.iterator();
//				// CATransition tFirst = iter.next();
//
//				boolean first = true;
//				String firstName = "";
//				while (iter.hasNext()) {
//					CATransition t = iter.next();
//					if (first) {
//						first = false;
//						firstName = t.name();
//
//						// add dummy label
//						commonLabels.append(t.toStringBufferDot() + "\\n");
//					} else {
//
//					}
//					sb.append(t.from().name() + " -> " + t.to().name()
//							+ " [ label = \"" + firstName + ":" + t.name()
//							+ "\"];\n");
//				}
//
//			}
//		}
//		// create dummy node for the common label
//		// TODO
//		sb.append("dummy [color=white, label=\"" + commonLabels + "\"]\n");

		/*
		 * // transitions for (CATransition t : this.transitions.values()) {
		 * sb.append
		 * (t.from().name()+" -> "+t.to().name()+" [ label = "+t.name()+
		 * " ];\n"); }
		 */

		sb.append("}\n");

		return sb;
	}
	private StringBuffer replaceNonLatexSymbols(String aStr) {

		aStr = aStr.replaceAll("<=", "@leq ");
		aStr = aStr.replaceAll(">=", "@geq ");
		aStr = aStr.replaceAll(",", "#, #");
		aStr = aStr.replace('#', '$');
		aStr = aStr.replace('@', '\\');
		aStr = "$ " + aStr + " $";

		return new StringBuffer(aStr);
	}

	public StringBuffer toLatex(String aTitle) {
		StringBuffer sb = new StringBuffer();

		sb.append("\\documentclass{article}\n\n\\begin{document}\n\n");

		sb.append(aTitle);

		TreeSet<CATransition> tSorted = new TreeSet<CATransition>(
				this.transitions.values());

		for (CATransition t : tSorted) {
			// String sGrd = t.guards().toString();
			// String sAct = t.actions().toString();
			String strT = new String(t.toSBuf(CR.eOUT_FLATA, 0));

			strT = strT.replace('{', ' ');
			strT = strT.replace('}', ' ');

			sb.append("$" + t.name() + "$: \\newline\n");
			// sb.append(replaceNonLatexSymbols(sGrd)+"\\newline\n");
			// sb.append(replaceNonLatexSymbols(sAct)+"\\newline\n");
			sb.append(replaceNonLatexSymbols(strT) + "\\newline\n");
		}

		sb.append("\n\n\\end{document}");

		return sb;
	}

	public void exportView(String aDirPath, String aCAname, String aTitle) {

		String pathBase = aDirPath + "/" + aCAname;
		File fDot = new File(pathBase + ".dot");
		File fTex = new File(pathBase + ".tex");

		try {
			{
				FileWriter w = new java.io.FileWriter(fDot);
				w.append(this.toDotLang(aCAname));
				w.flush();
				w.close();
			}
			{
				FileWriter w = new java.io.FileWriter(fTex);
				// w.append();
				w.append(this.toLatex(aTitle));
				w.flush();
				w.close();
			}

		} catch (IOException e) {
			System.err.println("I/O problems");
			System.err.println(e.getMessage());
			System.exit(-1);
		}

	}

	public void encodeParameters(List<String> parameters) {
		Variable[] params_var = new Variable[parameters.size()];
		int i=0;
		for (String s : parameters) {
			params_var[i++] = vp.giveVariable(s);
		}
		Arrays.sort(params_var);
		
		for (CATransition t : this.transitions()) {
			if (t.label().isRelation()) {
				t.rel().addImplicitActionsForSorted(params_var);
			}
		}
	}

	
	
}
