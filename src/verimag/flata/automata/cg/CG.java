package verimag.flata.automata.cg;

import java.io.File;
import java.io.StringWriter;
import java.util.*;

import verimag.flata.automata.*;
import verimag.flata.automata.ca.*;
import verimag.flata.automata.ca.CA.Trans4Term;
import verimag.flata.common.CR;
import verimag.flata.common.IndentedWriter;
import verimag.flata.common.Parameters;
import verimag.flata.common.SpuriousCE;
import verimag.flata.common.StopReduction;
import verimag.flata.presburger.*;

// call graph
public class CG extends BaseGraph {

	private File inputFile;
	public void inputFile(File aF) { inputFile = aF; }
	public File inputFile() { return inputFile; }
	
	private static class CGNodePair {
		private String first, second;
		public CGNodePair(String aFirst, String aSecond) { first = aFirst; second = aSecond; }
		public int hashCode() {
			return first.hashCode() + second.hashCode();
		}
		public boolean equals(Object other) {
			if (!(other instanceof CGNodePair))
				return false;
			CGNodePair o = (CGNodePair)other;
			return first.equals(o.first) && second.equals(o.second);
		} 
	}
	
	private String name;
	public void name(String aName) { name = aName; }
	
	private CGNode main = null; // main procedure
	private Map<String,CGNode> procedures = new HashMap<String,CGNode>();
	private Map<CGNodePair,CGArc> calls = new HashMap<CGNodePair,CGArc>();

	public Collection<CGNode> procedures() { return procedures.values(); }
	
	public void setMain(CGNode aMain) {
		main = aMain;
	}
	public void addProc(CGNode aProc) {
		procedures.put(aProc.name(), aProc);
	}
	
	public String mainName() {
		return main.name();
	}
	public CGNode main() {
		return main;
	}
	public CA mainProc() {
		return main.procedure();
	}
	public boolean singleProc() {
		return procedures.size() == 1;
	}
	public boolean hasCalls() {
		return !calls.isEmpty();
	}
	
	
	private void output_(int statusCode, float time) {
		if (Parameters.isOnParameter(Parameters.OUT_STATUS)) {
			String f = Parameters.getParameter(Parameters.OUT_STATUS).arguments()[0];
			CR.writeToFile(f,""+statusCode+" "+time);
		}
		//Main.finalActions();
		//System.exit(0);
	}
	public static int EXIT_CORRECT = 1;
	public static int EXIT_BUG = 2;
	public static int EXIT_DONTKNOW = 3;
	private void outputCORRECT(float time) {
		output_(EXIT_CORRECT, time);
	}
	private void outputBUG(float time) {
		output_(EXIT_BUG, time);
	}
	private void outputDONTKNOW(float time) {
		output_(EXIT_DONTKNOW, time);
	}
	
	
	public String toString() {
		StringBuffer ret = new StringBuffer("call-graph {\n");
		
		ret.append("  automata {");
		Iterator<String> iter = procedures.keySet().iterator();
		while (iter.hasNext()) {
			ret.append(iter.next());
			if (iter.hasNext())
				ret.append(",");
		}
		ret.append("}\n");
		
		ret.append("  calls {");
		Iterator<CGNodePair> iter2 = calls.keySet().iterator();
		while (iter2.hasNext()) {
			CGNodePair p = iter2.next();
			ret.append(p.first+"-->"+p.second);
			if (iter2.hasNext())
				ret.append(",");
		}
		ret.append("}\n");
		
		ret.append("}\n");
		
		return ret.toString();
	}
	
	public CG() { }

	@Override
	public Collection<? extends BaseArc> arcs() { return calls.values(); }
	@Override
	public Collection<? extends BaseNode> initials() {
		Collection<CGNode> ret = new LinkedList<CGNode>();
		ret.add(main);
		return ret;
	}
	@Override
	public Collection<? extends BaseNode> nodes() { return procedures.values(); }
	
	
	public CGNode giveNode(String aName) {
		CGNode ret = procedures.get(aName);
		if (ret == null) {
			ret = new CGNode(aName);
			procedures.put(aName, ret);
		}
		return ret;
	}
	
	private CGArc giveArc(String aCalling, String aCalled) {
		CGNode calling = giveNode(aCalling);
		CGNode called = giveNode(aCalled);
		CGNodePair pair = new CGNodePair(aCalling, aCalled);
		CGArc ret = calls.get(pair);
		if (ret == null) {
			ret = new CGArc(calling, called);
			calls.put(pair, ret);
		}
		return ret;
	}
	
	public Call addCall(Call call) {
		CGArc arc = giveArc(call.calling().name(), call.called().name());
		arc.addCall(call);
		return call;
	}
	
	public Call addCall(String aCalling, String aCalled, List<LinearConstr> args) {
		Call call = new Call(caForCall(aCalling), caForCall(aCalled), args);
		CGArc arc = giveArc(aCalling, aCalled);
		arc.addCall(call);
		return call;
	}
	
	public List<Call> allCalls() {
		List<Call> ret = new LinkedList<Call>();
		
		for (CGArc a : calls.values())
			ret.addAll(a.calls());
		
		return ret;
	}
	
	public boolean isDefined(String aName) {
		CGNode n = procedures.get(aName);
		return n != null && n.defined();
	}
	public void checkProcedureDefinitions() {
	    for (CGNode n : procedures.values()) {
	        if (!n.defined()) {
	        	System.err.println("Automaton '"+n.name()+"' undefined.");
	        	System.exit(-1);
	        }
	    }
	}
	public void checkCallParameters() {
		Call.check(allCalls());
	}
	// definition of the main method is obligatory only if neither one procedure is named 'main' nor 
	// the callgraph doesn't have a unique root
	public void checkMain() {
		
		int m;
		CGNode mmm = procedures.get("main");
		
		if (mmm != null) {
			m = 1;
		} else {
			m = 0;
			for (CGNode n : procedures.values()) {
				if (n.incoming().size() == 0) {
					m++;
					if (m > 1)
						break;
					mmm = n;
				}
			}
			if (m == 1)
				System.out.println("The callgraph's root procedure '"+mmm.name()+"' chosen as the main procedure (to select a different procedure, name it 'main')");
		}
		
	    if (m != 1) {
	      System.err.println("Definition of the 'main' automaton is required.");
	      System.exit(-1);    
	    }
	    
	    main = mmm;
	}
	
	private void setDefined(String aName, VariablePool aVP) {
		CGNode proc = procedures.get(aName);
		if (proc.defined()) {
			System.err.println("Redefinition of automaton '"+aName+"'.");
			System.exit(-1);
		}
		proc.setDefined(aVP);
		for (BaseArc a : proc.incoming()) {
			CGArc aa = (CGArc)a;
			for (Call c : aa.calls()) {
				c.setCalled(proc.procedure());
			}
		}
	}
	public CA caForDefinition(String aName, VariablePool aVP) {
		CGNode cgnode = cgnodeForCall(aName);
		this.setDefined(aName, aVP);
		return cgnode.procedure();
	}
	private CGNode cgnodeForCall(String aName) {
		return this.giveNode(aName);
	}
	public CA caForCall(String aName) {
		return cgnodeForCall(aName).procedure();
	}
	
	// inline everything following the topological sort
	public void inlineAll() {
		
		int maxV = RenameM.inlineDef;
		for (CGNode n : procedures.values()) {
			maxV = Math.max(RenameM.getInlinePrefix(Variable.sa(n.procedure().localUnp())), maxV);
		}
		maxV = Math.max(RenameM.getInlinePrefix(Variable.sa(main.procedure().globalUnp())), maxV);
		
		
		int maxS = RenameM.inlineDef;
		for (CGNode n : procedures.values()) {
			maxS = Math.max(RenameM.getInlinePrefix(CAState.sa(n.procedure().states())), maxS);
		}
		
		maxV++;
		maxS++;
		
		List<BaseNode> l = BaseGraph.topologicalSortBW(main);
		System.out.println(l);
		
		for (BaseNode n : l) {
			
			CGNode nn = (CGNode)n;
			
			// rename variables -- only those of depth 0
			
			// rename zero depth variables
			RenameM renV = RenameM.createForInline(Variable.sa(nn.procedure().localUnpZeroDepth()), maxV);
			renV.inferPrimed();
			nn.procedure().fillVarID(renV);
			
			// rename states
			
			int maxC = 0;
			for (BaseArc a : n.incoming()) {
				maxC = Math.max(((CGArc)a).calls().size(), maxC);
			}
			
			RenameM[] renS = new RenameM[maxC];
			for (int i=1; i<=maxC; i++) {
				renS[i-1] = RenameM.createForInline(CAState.sa(nn.procedure().states()), maxS+i);
			}
			
			inline(nn, renV, renS);
			
			maxV++;
			maxS += maxC;
			
			System.out.println(main.procedure());
		}
		
	}
	
	// inline the automaton passed as the argument
	// implies changes in the structure of the call graph (elimination of the inlined automaton)
	public void inline(CGNode n, RenameM renV, RenameM[] renS) {
		
		// find predecessors
		List<CGNode> calling = new LinkedList<CGNode>();
		for (BaseArc a : n.incoming() ) {
			calling.add((CGNode)((CGArc)a).from());
		}
		
		// preprocess the inlined automaton
		//   -- rename variables
		//   -- rename states for the first inlining
		
		CA aux = CA.rename(n.procedure(),renV);
		
		Collection<Call> callsNew = new LinkedList<Call>();
		
		for (BaseArc a : n.incoming()) {
			
			CGArc aa = (CGArc) a;
			CGNode n2 = (CGNode)aa.from();
			
			callsNew.addAll(n2.procedure().inline(aa.calls(), aux, renS));
		}
		
		// add new calls to the graph
		
		for (Call call : callsNew) {
			this.addCall(call);
		}
		
		// modify call graph
		
		for (BaseArc a : n.incoming()) {
			CGArc aa = (CGArc) a;
			aa.from().removeOutgoing(a);
		}
//		for (AbstractArc a : n.outcoming()) {
//			CGArc aa = (CGArc) a;
//			aa.to().removeIncoming(a);
//		}
		
		for (CGNodePair k : new LinkedList<CGNodePair>(this.calls.keySet()))  {
			if (/*k.first.equals(n.name()) || */k.second.equals(n.name())) {
				this.calls.remove(k);
			}
		}
		//this.procedures.remove(n.name());
	}
	
	public void removeCalls() {
		for (CGNode n : this.procedures.values()) {
			List<BaseArc> aux = new LinkedList<BaseArc>(n.outgoing());
			for (BaseArc a : aux) {
				a.from().removeOutgoing(a);
				a.to().removeIncoming(a);
			}
		}
		this.calls.clear();
	}
	public DisjRel replaceCalls_forRecur(CGNode ncg, Map<String,String> rename, CG cg_recur) {
		
		CA ca = ncg.procedure();
		
		DisjRel ret = new DisjRel();
		
		VariablePool vp = cg_recur.main.procedure().varPool();
		VariablePool vp_orig = ca.varPool();
		{
			Substitution subst = new Substitution();
			for (Map.Entry<String, String> e : rename.entrySet()) {
				Variable v = vp.giveVariable(e.getValue());
				LinearConstr lc = new LinearConstr();
				Variable v_orig = vp_orig.giveVariable(e.getKey());
				lc.addLinTerm(new LinearTerm(v_orig,1));
				subst.put(v, lc);
			}
			
			for (CATransition tt : cg_recur.main.summary().reachEnd()) {
				
				CompositeRel[] rel = tt.rel().substitute(subst);
				ret.addDisj(rel[0]);
			}
		}
		
		// for each calling transition in the automaton
		for (CATransition t : ca.calls()) {
			
			// remove calling transition
			ca.removeTransition(t);
			
			// replace if with summary (end-state and error-state reachability)
			
			Call c = t.call();
			CGNode called = procedures.get(c.called().name());
			if (!called.equals(ncg)) {
				throw new RuntimeException();
			}
			
			Substitution subst2 = called.procedure().renameForCalling(c);
			Substitution subst3 = new Substitution();
			for (Variable v : subst2.getKeys()) {
				Variable v_enc = vp.giveVariable(rename.get(v.name()));
				subst3.put(v_enc, subst2.get(v));
			}
			
			for (CATransition tt : cg_recur.main.summary().reachEnd()) {
				
				CompositeRel[] rel = tt.rel().substitute(subst3);
				
				if (rel.length == 0)
					continue;
				
				// add implicit actions locals unassigned by the call				
				Variable[] implAct = ca.unassignedLocalsAsUnp(c);
				
				Arrays.sort(implAct);
				rel[0].addImplicitActionsForSorted(implAct);
				
				//CATransition ttt = CATransition.plugged_summary(t.from(),t.to(),rel[0],t.name(),ca,t,tt);
				CATransition ttt = new CATransition(t.from(),t.to(),rel[0],t.name(),ca);
				ca.addTransition(ttt);
			}
		}
		
		return ret;
	}

	
	public void replaceCalls(CGNode ncg) {
		
		CA ca = ncg.procedure();
		
		// for each calling transition in the automaton
		for (CATransition t : ca.calls()) {
			
			// remove calling transition
			ca.removeTransition(t);
			
			// replace if with summary (end-state and error-state reachability)
			
			Call c = t.call();
			CGNode called = procedures.get(c.called().name());
			Summary rr = called.summary();
			Substitution subst = called.procedure().renameForCalling(c);
			
			for (CATransition tt : rr.reachEnd()) {
				
				CompositeRel[] rel = tt.rel().substitute(subst);
				
				if (rel.length == 0)
					continue;
				
				// add implicit actions locals unassigned by the call				
				Variable[] implAct = ca.unassignedLocalsAsUnp(c);
				
				Arrays.sort(implAct);
				rel[0].addImplicitActionsForSorted(implAct);
				
				CATransition ttt = CATransition.plugged_summary(t.from(),t.to(),rel[0],t.name(),ca,t,tt);
				ca.addTransition(ttt);
			}
			
			if (!rr.reachError().isEmpty()) {
				CAState error = ca.giveSomeErrorState();
				
				for (CATransition tt : rr.reachError()) {
					CompositeRel[] rel = tt.rel().substitute(subst);
					
					if (rel.length == 0)
						continue;
					
					CATransition ttt = CATransition.plugged_summary(t.from(),error,rel[0],t.name(),ca,t,tt);
					ca.addTransition(ttt);
				}
			}
		}
	}
	
	public void reachability_inline() {
		long start = System.currentTimeMillis();
		
		System.out.println(main.procedure());
		
		this.inlineAll();

		try {
		
			main.procedure().transform4reachability();
			Summary rr = main.procedure().reduce(true);
			main.summary(rr);
			
			if (!rr.successful()) {
				//Main.export(this);
				System.out.println("\nAnalysis was not successful.");
				float time = ((float)(System.currentTimeMillis()-start))/1000;
				outputDONTKNOW(time);
			} else {
				System.out.println("Reduction of the automaton '"+main.name()+"' done.");
			}
		
		} catch (SpuriousCE e) {
			//refine = true;
			//ca.refine();
			
			System.out.println("\nSpurious CE found. Refinement not supported. Analysis stopped.");
			System.exit(1);
			
		} catch (StopReduction e) {
			System.out.println("\n\nError state is reachable. Counter-example trace:");
			if (e.typeTrace()) {
				verimag.flata.Main.processCENodes(main.procedure(), start);
			}
			float time = ((float)(System.currentTimeMillis()-start))/1000;
			outputBUG(time);
		}
		
		System.out.println("No error state reachable.");
		System.out.println("Final-state reachability relation:");
		for (CATransition t: main.summary().reachEnd()) {
			System.out.println(t.rel());
		}
		float time = ((float)(System.currentTimeMillis()-start))/1000;
		System.out.println("running time: "+time+"s");
		outputCORRECT(time);
	}
	
	private Map<CGNode,Summary> summary_cache = new HashMap<CGNode,Summary>();
	public Summary getCached(CGNode cgn) {
		return summary_cache.get(cgn);
	}
	
	public int reachability_summary() {
		return reachability_summary(false);
	}
	public int reachability_summary(boolean skipMain) {
		
		long start = System.currentTimeMillis();
		
		// report recursive programs
		for (List<BaseNode> scc : this.findSccs()) {
			boolean b = false;
			if (scc.size() == 1) {
				String name = ((CGNode)scc.iterator().next()).name();
				CGNodePair pair = new CGNodePair(name, name);
				b = calls.containsKey(pair);
			}
			else if (scc.size() > 1) {
				b = true;
			}
			if (b) {
				System.out.println("Analysis of recursive programs not supported.");
				System.exit(1);
			}
		}
		
		System.out.print("Topological order for the call graph: ");
		List<BaseNode> l = BaseGraph.topologicalSortBW(main);
		System.out.println(l);
		
		CA ca = null;
		
		try {
		
			for (BaseNode nab : l) {
				
				if (nab == main && skipMain) {
					return 0;
				}
				
				// substitute calls with summaries
				CGNode ncg = (CGNode)nab;
				
				if (summary_cache.containsKey(ncg)) {
					continue;
				}
				
				//ncg.procedure().nontermUnreachRemoval();
				replaceCalls(ncg);
				
				// reduce
				ca = ncg.procedure();
				ca.transform4reachability();
				//ca.nontermUnreachRemoval();
//System.out.println(ca.toString());
				Summary rr = ca.reduce(ncg == main);
				ncg.summary(rr);
				summary_cache.put(ncg, rr);
				
if (rr.successful() && rr != null) System.out.println(rr);
				
				
				if (!rr.successful()) {
					//Main.export(this);
					System.out.println("Analysis was not successful.");
					float time = ((float)(System.currentTimeMillis()-start))/1000;
					outputDONTKNOW(time);
					return EXIT_DONTKNOW;
				} else {
					System.out.println("Reduction of the automaton '"+ca.name()+"' done.");
				}
			}
		
		} catch (SpuriousCE e) {
			//refine = true;
			//ca.refine();
			
			System.out.println("\nSpurious CE found. Refinement not supported. Analysis stopped.");
			System.exit(1);
			
		} catch (StopReduction e) {
			
			System.out.println("\n\nError state is reachable. Counter-example trace:");
			if (e.typeTrace()) {
				verimag.flata.Main.processCENodes(main.procedure(), start);
				//if (!Parameters.isOnParameter(Parameters.CE_ALL)) {
				System.out.println("running time (counter-example construction excluded): "+((float)(ca.ceTraceTime()-start))/1000+"s");
			}
			float time = ((float)(System.currentTimeMillis()-start))/1000;
			System.out.println("running time (total): "+time+"s");
			//}
			outputBUG(time);
			return EXIT_BUG;
		}
		
		System.out.println("No error state reachable.");
		System.out.println("Final-state reachability relation:");
		//main.summary().toString();
		System.out.println(main.summary());
		float time = ((float)(System.currentTimeMillis()-start))/1000;
		System.out.println("running time: "+time+"s");
		
//		{
//			System.out.println("Checking equivalence.");
//			DisjRel aux = main.summary().finalSummaryAsRel();
//			DisjRel aux2 = aux.copy();
//			DisjRel aux3 = aux.or_nc(aux2);
//			System.out.println(aux.implies(aux3).and(aux3.implies(aux)));
//		}
		
		outputCORRECT(time);
		return EXIT_CORRECT;
		//return null;
		//return rr.finalSummaryAsRel();
	}

	private boolean check_simple() {
		List<BaseNode> l = BaseGraph.topologicalSortBW(main);
		for (BaseNode n : l) {
			if (n == main) {
				continue;
			}
			CGNode nn = (CGNode) n;
			if (!nn.procedure().allSCCareTrivial()) {
				return false;
			}
		}
		return true;
	}
	
	public void termination2() {
		
		if (this.procedures.size() > 1) {
			if (!check_simple()) {
				System.err.println("Termination analysis of inter-procedural programs not supported");
				System.exit(1);
			} else {
				this.reachability_summary(true);
				replaceCalls(main);
			}
		}
		
		CA ca = this.main.procedure();
		ca.nontermUnreachRemoval();
		
		//System.out.println(ca);
		
		if (ca.initialStates().size() > 1) {
			System.err.println("Automaton must have only one initial state.");
			System.exit(1);
		}
		
		List<CAState> nontriv_states = new LinkedList<CAState>(ca.states());

		// optimization: only control states from non-trivial SCC
		List<List<BaseNode>> sccs = ca.findSccs();
		
		for (List<BaseNode> l : sccs) {
			if (l.size() == 1) {
				CAState aux = ((CAState)l.get(0));
				if (!ca.isInitial(aux) && aux.loops() == 0) {
					nontriv_states.remove(aux);
				}
			}
		}
		
		DisjRel precondition = new DisjRel();
		
		for (CAState s : nontriv_states) {
			CA ca2 = CA.copy(ca);
			CAState s2 = ca2.getStateWithName(s.name());
			Trans4Term tt = ca2.transform4termination(s2);
			Summary sum = ca2.reduce(true);
			
			if (!sum.successful()) {
				System.out.println("-------------------------------------------------------------------------");
				System.out.println("Transition invariant computation failed (some loops are not accelerable).");
				System.exit(0);
			}
			
			CAState sI = tt.mI.get(s2);
			CAState sF = tt.mF.get(s2);
			//List<CompositeRel> reach = ca2.relBetween(init, sF);
			List<CompositeRel> tInv = ca2.relBetween(sI, sF);
			DisjRel reach = new DisjRel();
			reach.addDisj(ca2.relBetween(tt.mI.get(tt.oldInit), sF));
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("Disjuncive non-termination condition for control state '"+s.name()+"':");
			for (CompositeRel r : tInv) {
				System.out.println("Given R <=> ("+r+"),");
				CompositeRel oct = r;
				if (!r.isOctagon()) { 
					oct = r.hull(Relation.RelType.OCTAGON);
					System.out.println("  octagonal hull <=> ("+oct+"),");
				}
				CompositeRel c = oct.weakestNontermCond();
				System.out.println("  wrs(R) <=> ("+(c==null ? "false" : c.toString())+")");
				if (c != null) {
					System.out.println("Propagating wrs to initial control state");
					DisjRel dr = new DisjRel(c);
					for (CompositeRel rr : reach.disjuncts()) {
						DisjRel drr = new DisjRel(rr);
						DisjRel aux = drr.compose(dr);
						DisjRel dom = aux.domain();
						System.out.println(dom);
						precondition.addDisj(dom);
					}
				}
			}
			System.out.println("-------------------------------------------------------------------------");
		}
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("Non-termination precondition:");
		System.out.println(precondition);
		
	}
	public void termination() {
		
		if (this.procedures.size() > 1) {
			System.err.println("Termination analysis of inter-procedural programs not supported");
			System.exit(1);
		}
		
		CA ca = this.main.procedure();
		ca.nontermUnreachRemoval();
		
		//System.out.println(ca);
		
		if (ca.initialStates().size() > 1) {
			System.err.println("Automaton must have only one initial state.");
			System.exit(1);
		}
		
		// perform transformations in order to compute:
		//   -- R+ reachability relation from initial control states to arbitrary control state reachInit(i)
		//   -- Acc - set of accessible states
		//   -- intersect transitions with accessible states (relative to control state)
		//   -- R+ reachability relation from any control state to itself -- reachSelf(i)
		
		
		// reachability analysis hacks at:
		//    MultiloopTransformation.reduceMultiLoopState
		//    CA.replaceMultiLoop
		//    CA.elimAsRegExp
		//    CA.postParsing
		//    CA.eliminateState
		//    CA.reduce
		//    CATransition.compose
		
//		System.out.println("----------------------Reachability-----------------------------");
//		
//		CA.ReachConfig rc = CA.reachableConfigurations4Term(ca);
//		
//		for (Map.Entry<String, DisjRel> e : rc.reach.entrySet()) {
//			System.out.println("Reachable configurations at control state "+e.getKey()+":");
//			System.out.println(e.getValue());
//		}		
//		
//		System.out.println("----------------------Transition invariant-----------------------------");
		
		System.out.println("----------------------Reachability & Transition invariant-----------------------------");
		
		
		CA cp = ca;
		Trans4Term t4t = cp.transform4termination();
		
		cp.reduce(true);
		
		// then, compute (over-approximation of) wrs(reachSelf(i))
		//   check if reachInit(i)(True) /\  wrs(reachSelf(i)) <==> false
		// if forall i . reachInit(i)(True) /\  wrs(reachSelf(i)) <==> false, then the program terminates
		
		//Trans4Term t4t = cp.trans4term;
		CAState init = t4t.mI.get(t4t.oldInit);
		for (CAState s : t4t.mI.keySet()) {
			CAState sI = t4t.mI.get(s);
			CAState sF = t4t.mF.get(s);
			
			List<CompositeRel> reach = cp.relBetween(init, sF);
			DisjRel dreach = new DisjRel(reach);
			List<CompositeRel> tInv = cp.relBetween(sI, sF);
			
			System.out.println("Disjuncive non-termination condition for control state '"+s.name()+"':");
			for (CompositeRel r : tInv) {
				System.out.println("Given R <=> ("+r+"),");
				CompositeRel oct = r;
				if (!r.isOctagon()) { 
					oct = r.hull(Relation.RelType.OCTAGON);
					System.out.println("  octagonal hull <=> ("+oct+"),");
				}
				CompositeRel c = oct.weakestNontermCond();
				System.out.println("  wrs(R) <=> ("+(c==null ? "false" : c.toString())+")");
				
				if (c != null) {
					System.out.println("Propagating wrs");
					DisjRel dwrs = new DisjRel(c);
					System.out.println(dreach.compose(dwrs).domain());
				}
			}
		}
	}
//	public void removeUselessStates() {
//		for (CGNode n : this.procedures()) {
//			n.procedure().nontermUnreachRemoval();
//		}
//	}
	
	public void reduceMin() {
		for (CGNode n : this.procedures()) {
			n.procedure().reduce_min();
		}
	}

	public StringBuffer toStringNTS() {
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, 0);
		
		iw.writeln("nts "+this.name+";");
		iw.writeln();
		
		// declaration
		if (this.mainProc().varPool().globalUnp().length > 0) {
			StringBuffer sb = VariablePool.asString(this.mainProc().varPool().globalUnp());
			sb.append(" : int;");
			iw.writeln(sb);
			iw.writeln();
		}
		
		iw.writeln("instances "+this.mainName()+"[1];");
		iw.writeln();
		
		for (CGNode n : this.procedures()) {
			iw.writeln();
			n.procedure().toStringNTS(iw);
		}
		
		return sw.getBuffer();
	}
	
}
