package verimag.flata.recur_bounded;

import java.io.*;
import java.util.*;

import org.antlr.runtime.Token;

import verimag.flata.common.Parameters;

import nts.interf.*;
import nts.interf.base.*;
import nts.interf.expr.*;
import nts.parser.*;
import client.PrintVisitor;

public class RecurToKBounded {

	/*
	 * Class of programs:
	 *   -- integer recursive programs without parallelism and without arrays 
	 * Normalization:
	 *   -- encode global variables by local variables
	 *   -- make each variable to have a unique name
	 *   -- make each control state to have a unique name
	 *   -- only one initial and one final control state
	 *   -- encode precondition in the main procedure
	 *   -- make all parameters global
	 * TODO:
	 *   -- modules
	 *       -- modules must not conflict in procedure names, global variables names, global parameter names
	 *       -- precondition <=> conjunction of preconditions in modules 
	 */

	private RecurToKBounded(NTS aNts) {
		nts = aNts;
		genInfo = new GenerationInfo(aNts);
	}
	public static RecurToKBounded start(String input) {
		NTS nts = null;
		try {
			nts = parse(new FileInputStream(input));
		} catch (FileNotFoundException e) {
			System.err.println("File not found: "+input);
			System.exit(1);
		}
		return start(nts);
	}
	public static RecurToKBounded start(NTS aNts) {
		RecurToKBounded r = new RecurToKBounded(aNts);
		r.generateTemplates();
		return r;
	}
	private static NTS parse(InputStream istream) {
		ParserListener listen = new ParserListener();
	    NTSParser.parseNTS(istream, listen);
	    return listen.nts();
	}
	
	public static void main(String[] args) throws IOException {
		InputStream istream = null;
	    if (args.length >= 2) {
	      istream = new FileInputStream(args[0]);
	    } else { 
	      istream = System.in;
	    }
	    NTS input_nts = parse(istream);
	    istream.close();
	    
	    RecurToKBounded r = new RecurToKBounded(input_nts);
	    int K = Integer.parseInt(args[1]);
	    
	    if (args.length > 2 && args[2].equals(Parameters.RECUR_SCC)) {
	    	Parameters.setParameter(Parameters.RECUR_SCC);
	    }
	    
		r.generateTemplates();
		r.generateKLevels(K);
		
		Writer w = new BufferedWriter(new OutputStreamWriter(System.out));
		PrintVisitor v = new PrintVisitor(w);
	    r.nts.accept(v);
	    w.flush();
	    w.close();
	    //System.out.println(v.toStringBuffer());
	}
	private void generateKLevels(int K) {
		for (int k=1; k<=K; k++) {
	    	generateOneLevel(k);
	    }
	    nts.semanticChecks();
	}
	public void generateNewLevel(int k) {
		generateOneLevel(k);
		nts.semanticChecks();
	}
	
	public Map<String,String> giveMapping() {
		return renForMain;
	}
	private Map<String,String> renForMain = null;
	
	private void generateTemplates() {

		//aNts.checkStates();
		
//		PrintVisitor v = new PrintVisitor();
//	    v = new PrintVisitor();
//	    aNts.accept(v);
//	    System.out.println(v.toStringBuffer());
		
		// transform assignments to global variables g'=F(x) to aux'=f(x); g'=aux /\ havoc(g)
		nts.transformAssignmentsToGlobals("aux", "aux");
		nts.semanticChecks();
	    
	    int n = nts.subsystems().size();

	    // variable prefix
	    String[][] pref = new String[n+1][];
	    String[] prefStates = new String[n];
	    for (int i=1; i<=n; i++) {
	    	pref[i] = new String[4];
	    	pref[i][0] = "p"+(i)+"_i1_";
	    	pref[i][1] = "p"+(i)+"_o1_";
	    	pref[i][2] = "p"+(i)+"_i2_";
	    	pref[i][3] = "p"+(i)+"_o2_";
	    	
	    	prefStates[i-1]   = "p"+(i)+"_";
	    }
	    pref[0] = new String[]{"p0_i1_","p0_o1_","p0_i2_","p0_o2_"};
	    
	    nts.varTable().eraseKey("tid");
	    
	    // global parameters -- rename with prefix "p0_"
	    // global variables -- rename with prefix "p0_i"
	    Map<String,String> renGP = new HashMap<String,String>();
	    
	    
	    // global parameters
	    for (IVarTableEntry e : nts.varTable().innermostUnprimedParam(true)) {
	    	renGP.put(e.name(), pref[0][0]+e.name());
	    }
	    
	    for (String s : renGP.keySet()) {
	    	nts.varTable().renameInnermost(s, renGP.get(s));
	    }
	    
	    // global variables
	    List<String> glob_vars_old = new LinkedList<String>();
	    List<String> glob_vars = new LinkedList<String>();
	    List<String> l_gi = new LinkedList<String>();
	    List<String> l_go = new LinkedList<String>();
	    for (IVarTableEntry iv : nts.varTable().innermostUnprimedParam(false)) {
	    	String gg = iv.name();
	    	
	    	glob_vars_old.add(gg);
	    	
	    	String gi_base = gg+"_i"; // input
	    	String go_base = gg+"_o"; // output
	    	
	    	String gi = pref[0][0]+gi_base;
	    	String go = pref[0][1]+go_base;
	    	
		    // rename global variable names by input variables
	    	renGP.put(gg, gi);
	    	renGP.put(gg+"'", gi+"'");
	    	
		    l_gi.add(gi);
		    l_go.add(go);
		    
		    glob_vars.add(gi_base);
		    glob_vars.add(go_base);
	    }
	    Set<String> globalsEnc = new HashSet<String>(); // unprimed strings used to encode global variables
	    for (IVarTableEntry iv : nts.varTable().innermostUnprimedParam(false)) {
	    	String gg = iv.name();
	    	for (int i=0; i<4; i++) {
	    		globalsEnc.add(pref[0][i]+gg+"_i");
	    		globalsEnc.add(pref[0][i]+gg+"_o");
	    	}
	    }
	    
	    nts.varTable().eraseInnermostNonparam();
	    

	    // strongly connected components of the call graph
	    // identify the procedures with integers
	    s2i = new HashMap<Subsystem,Integer>();
	    i2s = new ArrayList<Subsystem>(n);
	    s2scc = new HashMap<Subsystem,Integer>();
	    SccFinder sccF = new SccFinder(nts);
	    {
	    	if (Parameters.isOnParameter(Parameters.RECUR_SCC)) {
	    		sccF.run();
	    	} else {
	    		sccF.runOneScc();
	    	}
	    	
	    	cnt_scc_all = sccF.trivial.size() + sccF.nontrivial.size();
	    	scc2size = new ArrayList<Integer>(cnt_scc_all);
	    	scc2firstInx = new ArrayList<Integer>(cnt_scc_all);
	    	int i = 1;
	    	for (Subsystem s : sccF.trivial) {
	    		scc2size.add(1);
	    		scc2firstInx.add(i);
	    		s2scc.put(s, scc2size.size()-1);
	    		i2s.add(s);
	    		s2i.put(s, i);
	    		i++;
	    	}
	    	for (List<Subsystem> scc : sccF.nontrivial) {
	    		scc2size.add(scc.size());
	    		scc2firstInx.add(i);
	    		for (Subsystem s : scc) {
	    			s2scc.put(s, scc2size.size()-1);
	    			i2s.add(s);
		    		s2i.put(s, i);
		    		i++;
	    		}
	    	}
	    }

	    
	    
	    String main_name = nts.instances().keySet().iterator().next();
	    main_name_new_base = null;

	    
	    List<List<String>> loc_vars = new ArrayList<List<String>>(n); // without "global" variables
	    List<List<String>> loc_in = new ArrayList<List<String>>(n); // with "global" variables
	    List<List<String>> loc_out = new ArrayList<List<String>>(n); // with "global" variables
	    List<String> states_init = new ArrayList<String>(n);
	    {
		    for (int inx = 1; inx <= n; inx++) {
		    	Subsystem s = i2s.get(inx-1);
		    	
		    	boolean isMain = main_name.equals(s.name());
		    	
		    	
		    	if (isMain) {
			    	renForMain = new HashMap<String,String>();
			    	
			    	for (IVarTableEntry e : s.varIn()) {
			    		renForMain.put(e.name(), pref[inx][0]+e.name());
			    	}
			    	for (IVarTableEntry e : s.varOut()) {
			    		renForMain.put(e.name()+"'", pref[inx][1]+e.name());
			    	}
			    }
		    	
			    
			    // local variables and parameters
		    	List<String> loc_old = new LinkedList<String>();
		    	for (IVarTableEntry iv : s.varTable().innermostUnprimed()) {
		    		loc_old.add(iv.name());
		    	}
		    	
		    	List<String> loc_new = new LinkedList<String>();
			    loc_vars.add(loc_new);
			    List<String> loc_in_new = new LinkedList<String>();
			    loc_in.add(loc_in_new);
			    List<String> loc_out_new = new LinkedList<String>();
			    loc_out.add(loc_out_new);
		    	
		    	// initialize with renaming for global parameters
			    Map<String,String> ren = new HashMap<String,String>(renGP);
			    
			    main_name_new_base = prefStates[inx-1]+s.name();
			    nts.renameSubsystem(s,main_name_new_base);
			    
			    // global variables
			    for (String g : l_gi) {
			    	s.varTable().declareLocalInt(g, EModifier.IN, s);
			    }
			    for (String g : l_go) {
			    	s.varTable().declareLocalInt(g, EModifier.OUT, s);
			    }
			    
			    // local variables and parameters
			    String p = pref[inx][0];
			    Iterator<String> i = loc_old.iterator();
			    while (i.hasNext()) {
			    	String aa = i.next();
			    	String bb = p+aa;
			    	
			    	VarTableEntry e = s.varTable().get(aa);
			    	
			    	if (!e.isParam()) {
					    // rename declarations
				    	s.varTable().renameInnermost(aa,bb);
				    	
				    	loc_new.add(aa);
			    	} else {
			    		i.remove();
			    		// declarations
			    		s.varTable().undeclare(aa);
			    		e.renameToken(bb);
			    		nts.varTable().declare(e);
			    	}
			    	
				    // rename local variables
			    	ren.put(aa, bb);
			    	ren.put(aa+"'", bb+"'");
			    }
			    
			    s.renameVarTokens(ren);
			    
			    s.addInOutForGlobals(l_gi,l_go); // 
			    s.passGlobalsViaCall(l_gi);
			    
			    s.prefixControlStates(prefStates[inx-1]);
			    
			    // unique start and end control states (using Havoc transition)
			    s.uniqueInitialState(prefStates[inx-1]);
			    s.uniqueFinalState(prefStates[inx-1]);
			    s.uniqueErrorState(prefStates[inx-1]);
			    
			    // add g_out'=g_in' to the "final" transition
			    s.setGlobalVarsToOutput(l_gi,l_go);
			    
			    //  plug the precondition in the main procedure
			    if (isMain) {
			    	nts.plugPrecondition(ren,s);
			    }
			    
			    //int len = p.length();
			    for (IVarTableEntry iv : s.varIn()) {
			    	String name = iv.name();
			    	int pref_inx = globalsEnc.contains(name)? 0 : inx;
    				int pref_len = pref[pref_inx][0].length();
			    	loc_in_new.add(name.substring(pref_len));
			    }
			    for (IVarTableEntry iv : s.varOut()) {
			    	String name = iv.name();
			    	int pref_inx = globalsEnc.contains(name)? 0 : inx;
    				int pref_len = pref[pref_inx][0].length();
			    	loc_out_new.add(name.substring(pref_len));
			    }
			    states_init.add(s.marksInit().iterator().next().name());
		    }
	    }
	    
	    //nts.checkStates();
	    nts.expandHavoc();
	    nts.semanticChecks();
	    
//	    v = new PrintVisitor();
//	    aNts.accept(v);
//	    System.out.println(v.toStringBuffer());
	    
	    List<List<String>> loc_vars_withG = new ArrayList<List<String>>(n); // with "global" variables
	    for (int i=0; i<n; i++) {
	    	List<String> aux = new LinkedList<String>(glob_vars);
	    	aux.addAll(loc_vars.get(i));
	    	loc_vars_withG.add(aux);
	    }
	    
	    
	    
	    // prepare input variables and call arguments
	    // in-order call, input variables
	    call_1_in = new String[n][];
	    // out-of-order call, input variables (NOTE: same as actual arguments)
	    call_2_in = new String[n][]; 
	    for (int i=1; i<=n; i++) {
	    	
	    	// in-order call, input variables and actual arguments
	    	int m = loc_in.get(i-1).size() + loc_out.get(i-1).size();
	    	call_1_in[i-1] = new String[m];
	    	int j=0;
	    	for (String s : loc_in.get(i-1)) {
	    		int pref_inx = glob_vars.contains(s)? 0 : i; 
	    		call_1_in[i-1][j] = pref[pref_inx][0] + s;
	    		j++;
	    	}
	    	for (String s : loc_out.get(i-1)) {
	    		int pref_inx = glob_vars.contains(s)? 0 : i;
	    		call_1_in[i-1][j] = pref[pref_inx][1] + s;
	    		j++;
	    	}
	    	
	    	// out-of-order call
	    	m = loc_vars_withG.get(i-1).size();
	    	call_2_in[i-1] = new String[2*m];
	    	j=0;
	    	for (String s : loc_vars_withG.get(i-1)) {
	    		int pref_inx = glob_vars.contains(s)? 0 : i;
	    		call_2_in[i-1][j] = pref[pref_inx][0] + s;
	    		call_2_in[i-1][j+m] = pref[pref_inx][1] + s;
	    		j++;
	    	}
	    }
	    
	    
	    List<List<VarTableEntry>> vvv = new ArrayList<List<VarTableEntry>>(n);
	    callsTo = new ArrayList<Set<ControlState>>(n);
	    
	    s_fin = new String[cnt_scc_all][];
	    
	    // template automaton for each SCC
	    
	    // initialize the lists of calls for each template
	    templates = new Subsystem[cnt_scc_all];
	    for (int i=0; i<cnt_scc_all; i++) {
	    	templateCalls.add(new LinkedList<TCall>());
	    }
	    // generate templates
	    for (int scc = 0; scc < cnt_scc_all; scc++) {

	    	int scc_start = scc2firstInx.get(scc);
	    	int scc_size = scc2size.get(scc);
	    	int scc_end = scc_start + scc_size - 1;
	    	
		    // create "template" automaton
		    // variables (ignore input / output)
		    int call_cnt = 0;
		    VarTable vt = new VarTable(nts.varTable());
		    Subsystem template = new Subsystem(vt, NTSParser.dummyIDN_S("template"+scc));
		    templates[scc] = template;
		    
		    s_fin[scc] = new String[scc_size];
		    
		    for (int inx = scc_start; inx <= scc_end; inx++) { 
		    	Subsystem s = i2s.get(inx-1);
		    	List<String> loc = loc_vars_withG.get(inx-1);
		    	
		    	// declare local variables (local parameters are already removed)
		    	List<VarTableEntry> vv = new ArrayList<VarTableEntry>(4*loc.size());
		    	vvv.add(vv);
		    	for (String name : loc) {
		    		int pref_inx = glob_vars.contains(name)? 0 : inx;
		    		for (int i=0; i<4; i++) {
		    			String p = pref[pref_inx][i];
		    			
		    			VarTableEntry ee;
		    			if (glob_vars.contains(name) && inx != scc_start) {
			    			// declare globals only once
		    				ee = vt.get(p+name);
			    		} else {
			    			ee = vt.declareLocalInt(p+name, EModifier.NO, template);
			    		}
		    			vv.add(ee);
		    		}
		    	}
		    	
		    	// control states
		    	for (IControlState ics : s.controlStates()) {
		    		ControlState cs = (ControlState) ics;
		    		template.getDeclareState(cs.name());
		    	}
		    	
		    	// prepare list of final states
		    	s_fin[scc][inx-scc_start] = s.marksFinal().iterator().next().name();
		    }
		    
		    
		    for (int inx = scc_start; inx <= scc_end; inx++) {
		    	Subsystem s = i2s.get(inx-1);
		    	List<String> loc = loc_vars_withG.get(inx-1);
		    	
		    	// renaming actual parameters for out-of-order call: 1st copy to 2nd copy
		    	Map<String,String> renForOOOCall = new HashMap<String,String>();
		    	for (String s1 : loc) {
		    		int pref_inx = glob_vars.contains(s1)? 0 : inx;
		    		String aa = pref[pref_inx][0]+s1;
		    		String bb = pref[pref_inx][2]+s1;
		    		renForOOOCall.put(aa,bb);
		    	}
		    	
		    	Set<ControlState> callsTo_new = new HashSet<ControlState>();
		    	callsTo.add(callsTo_new);
		    	
		    	// transitions
		    	for (ITransition it : s.transitions()) {
		    		Transition t = (Transition) it;
		    		
		    		if (t.label() instanceof Expr) {
		    			Expr e = (Expr) t.label();
		    			
		    			// identity on X_O1
		    			e = ASTWithoutToken.exAnd(e, this.identity(vvv, inx, 1));
		    			
		    			// binding X_I1 with X_O1
		    			if (s.marksFinal().contains(t.to())) {
		    				for (String name : loc) {
		    					int pref_inx = glob_vars.contains(name)? 0 : inx;
		    					String in  = pref[pref_inx][0] + name + "'";
		    					String out = pref[pref_inx][1] + name + "'";
		    					e = ASTWithoutToken.exAnd(e,
		    							ASTWithoutToken.exEq(
		    								ASTWithoutToken.accessBasic(out),
		    								ASTWithoutToken.accessBasic(in)
		    								)
		    							);
		    				}
			    		}
		    			
		    			Token a1 = t.hasID()? NTSParser.dummyIDN_S(t.id()) : null;
		    			ControlState s_from = template.getState(t.from().name());
		    			ControlState s_to = template.getState(t.to().name());
		    			template.addTransition(new Transition(a1,s_from,s_to,e));
		    			
		    		} else {
		    			Call c = (Call) t.label();
		    			
		    			call_cnt++;
		    			
		    			ControlState s_from = template.getState(t.from().name());
		    			ControlState s_to = template.getState(t.to().name());
		    			int M = 2;
		    			ControlState[] s_aux = new ControlState[M];
		    			for (int i=0; i<M; i++) {
		    				s_aux[i] = template.getDeclareState("aux_"+call_cnt+"_"+i);
		    			}
		    			
		    			int inx_callee = s2i.get(c.callee());
		    			
		    			callsTo_new.add(t.to());
		    			
		    			ControlState callee_init = (ControlState) c.callee().marksInit().iterator().next();
		    			
		    			// bind call + return + havoc
		    			{
			    			Expr ex_2 = ASTWithoutToken.litBool(true);
			    			
			    			// copy variables used in actual parameters terms
			    			for (IVarTableEntry ive : UsedVarsVisitor.callActParam(c)) {
			    				String s1 = ive.name();
			    				int pref_inx = globalsEnc.contains(s1)? 0 : inx;
			    				int pref_len = pref[pref_inx][0].length();
			    				String s2 = pref[pref_inx][2] + ive.name().substring(pref_len);
			    				ex_2 = ASTWithoutToken.exAnd(
	    								ex_2,
	    								ASTWithoutToken.exEq(
	    									ASTWithoutToken.accessBasic(s2+"'"),
	    									ASTWithoutToken.accessBasic(s1))
	    								);
			    			}
			    			// copy variables used in return parameters
			    			List<String> assignedTo = new LinkedList<String>(); // without prime symbol !
			    			for (IVarTableEntry ive : UsedVarsVisitor.callRetParam(c)) {
			    				String aux = ive.counterpart().name();
			    				int pref_inx = globalsEnc.contains(aux)? 0 : inx;
			    				int pref_len = pref[pref_inx][0].length();
			    				String s_base = aux.substring(pref_len);
			    				String s1 = pref[pref_inx][0] + s_base;
			    				String s2 = pref[pref_inx][3] + s_base;
			    				ex_2 = ASTWithoutToken.exAnd(
	    								ex_2,
	    								ASTWithoutToken.exEq(
	    									ASTWithoutToken.accessBasic(s1+"'"),
	    									ASTWithoutToken.accessBasic(s2+"'"))
	    								);
			    				assignedTo.add(s_base);
			    			}
			    			// frame condition
			    			for (String s_base : loc) {
			    				if (!assignedTo.contains(s_base)) {
			    					int pref_inx = glob_vars.contains(s_base)? 0 : inx;
			    					String s1 = pref[pref_inx][0] + s_base;
			    					ex_2 = ASTWithoutToken.exAnd(
		    								ex_2,
		    								ASTWithoutToken.exEq(
		    									ASTWithoutToken.accessBasic(s1+"'"),
		    									ASTWithoutToken.accessBasic(s1))
		    								);
			    				}
			    			}
			    			// identity on X_O1 of the caller
			    			ex_2 = ASTWithoutToken.exAnd(ex_2, identity(vvv, inx, 1));
		    				
			    			Transition t_2 = new Transition(null, s_from, s_aux[0], ex_2);
			    			template.addTransition(t_2);
		    			}
		    			
		    			
		    			List<IExpr> inorder_actual_in = new LinkedList<IExpr>();
		    			for (IExpr ie : c.actualParameters()) {
		    				Expr e = Expr.syn_copy((Expr)ie);
		    				e.renameVarTokens(renForOOOCall);
		    				inorder_actual_in.add(e);
		    			}
		    			List<IAccessBasic> inorder_actual_out = new LinkedList<IAccessBasic>();
		    			
		    			for (IAccessBasic ia : c.returnVars()) {
		    				String unpr = ia.var().counterpart().name();
		    				int pref_inx = globalsEnc.contains(unpr)? 0 : inx;
		    				int pref_len = pref[pref_inx][0].length();
		    				String s1 = pref[pref_inx][3] + unpr.substring(pref_len);
		    				inorder_actual_out.add(ASTWithoutToken.accessBasic(s1));
		    			}
		    			
		    			
		    			// call in order
		    			{
		    				List<IExpr> actParam = new LinkedList<IExpr>(inorder_actual_in);
		    				actParam.addAll(inorder_actual_out);
			    			templateCall(scc,s_aux[0].name(),s_to.name(),s_to.name(),inx_callee,true,
			    					actParam);
		    			}
		    			
		    			// generate only inter-scc out-of-order calls
		    			if (scc_start <= inx_callee && inx_callee <= scc_end) {
		    				
			    			// call out of order
			    			{
				    			templateCall(scc,s_aux[0].name(),s_aux[1].name(),s_to.name(),inx,false,
				    					actualArguments(call_2_in[inx-1]));
			    			}
			    			
			    			// bind2
			    			{
				    			Expr ex_5 = ASTWithoutToken.litBool(true);
				    			
					    		Iterator<IExpr> i1 = inorder_actual_in.iterator();
					    		Iterator<IVarTableEntry> i2 = c.callee().varIn().iterator();
					    		while (i1.hasNext()) {
				    				Expr e_ren = (Expr) i1.next();
				    				VarTableEntry ve = (VarTableEntry) i2.next();
				    				ex_5 = ASTWithoutToken.exAnd(
				    								ex_5,
				    								ASTWithoutToken.exEq(
				    									ASTWithoutToken.accessBasic(ve.name()+"'"),
				    									e_ren)
				    								);
				    			}
				    			Iterator<IAccessBasic> i3 = c.returnVars().iterator();
				    			Iterator<IVarTableEntry> i4 = c.callee().varOut().iterator();
				    			while (i3.hasNext()) {
				    				AccessBasic ret = (AccessBasic) i3.next();
				    				String ret_unpr = ret.var().counterpart().name();
				    				String out_unpr = i4.next().name();
				    				int pref_inx = globalsEnc.contains(ret_unpr)? 0 : inx;
				    				int pref_inx2 = globalsEnc.contains(out_unpr)? 0 : inx_callee;
				    				int pref_len = pref[pref_inx][0].length();
				    				int pref_len2 = pref[pref_inx2][0].length();
				    				String ret2 = pref[pref_inx][3] + ret_unpr.substring(pref_len);
				    				String out2 = pref[pref_inx2][1] + out_unpr.substring(pref_len2);
				    				ex_5 = ASTWithoutToken.exAnd(
				    								ex_5,
				    								ASTWithoutToken.exEq(
				    									ASTWithoutToken.accessBasic(ret2),
				    									ASTWithoutToken.accessBasic(out2+"'"))
				    								);
				    			}
				    			
				    			Transition t_5 = new Transition(null, s_aux[1], callee_init, ex_5);
				    			template.addTransition(t_5);
			    			}
		    			}
		    			
		    			
		    			if (s.marksFinal().contains(t.to())) {
			    			// NOTE: normalization ensures that no call transition leads to final state
		    				//       ==>  skip;
			    		}
		    		}
		    	}
		    }
		    
		    
		    // initial transitions for the out-of-order call
		    for (int inx = scc_start; inx <= scc_end; inx++) {
			    for (ControlState s : callsTo.get(inx-1)) {
					// callee's initial state for out-of-order call
					String name = initStateOOO(s.name());
					ControlState sInit = template.getDeclareState(name);
					
					Expr e = identity(vvv, inx, 0); // identity on X_I1 of the caller
					e = ASTWithoutToken.exAnd(e, identity(vvv, inx, 1)); // identity on X_O1 of the caller
					Transition t_aux = new Transition(null, sInit, s, e);
					template.addTransition(t_aux);
				}
		    }
		    
			// ------------------------------------------------------------------------
//		    nts.addSubsystem(template);
//		    nts.semanticChecks();
//		    v = new PrintVisitor();
//		    nts.accept(v);
//		    System.out.println(v.toStringBuffer());
		    
		}
	    
	    
	    //s_fin = new String[n];
	    s_init = new String[n];
	    for (int inx=1; inx<=n; inx++) { 
	    	Subsystem s = i2s.get(inx-1);
	    	s_init[inx-1] = s.marksInit().iterator().next().name();
	    	//s_fin[inx-1] = s.marksFinal().iterator().next().name();
	    }
	    
	    for (Subsystem s : i2s) {
	    	nts.removeSubsystem(s);
	    }
	    
	    // generated procedures
	    map = new HashMap<String,Subsystem>();
	    
	}
	
	private void generateOneLevel(int k) {
		
		String[] empty = new String[0];
		List<ISubsystem> latestSubsystems = new LinkedList<ISubsystem>();
		
    	for (int scc = 0; scc < cnt_scc_all; scc++) {

	    	int scc_start = scc2firstInx.get(scc);
	    	int scc_size = scc2size.get(scc);
	    	int scc_end = scc_start + scc_size - 1;
	    	
	    	// generate procedures for in-order calls
	    	for (int ii=scc_start; ii<=scc_end; ii++) {
	    		int ix = ii-1;
	    		Subsystem s_orig = i2s.get(ix);
	    		String name = procName_inOrder(k,s_orig.name());
	    		Subsystem s_k = templates[scc].copy_notDeep(name, nts.varTable());
	    		//Subsystem s_k = templates[scc].copy(name, nts.varTable());
	    		
	    		map.put(name, s_k);
	    		
	    		// set input variables
	    		s_k.setInOutVars(call_1_in[ix], empty);
	    		
	    		// set calls
	    		if (k > 1) {
	    			generateCalls(k,s_k,scc);
	    		}
	    		
	    		// set initial states
	    		s_k.setInital(s_k.getState(s_init[ix]));
	    		// set final states
	    		for (int jj=0; jj<scc_size; jj++) {
	    			s_k.setFinal(s_k.getState(s_fin[scc][jj]));
	    		}
	    		
	    		latestSubsystems.add(s_k);
	    		nts.addSubsystem(s_k);
	    	}
	    	
	    	// generate procedures for out-of-order calls
	    	//if (k != K) {
    		for (int ii=scc_start; ii<=scc_end; ii++) {
	    		int ix = ii-1;
	    		
	    		for (ControlState s : callsTo.get(ix)) {
	    			
	    			Subsystem s_orig = i2s.get(ix);
		    		String name = procName_outOfOrder(k,s_orig.name(),s.name());
		    		Subsystem s_k = templates[scc].copy_notDeep(name, nts.varTable());
		    		//Subsystem s_k = templates[scc].copy(name, nts.varTable());
		    		
		    		map.put(name, s_k);
		    		
		    		// set input variables
		    		s_k.setInOutVars(call_2_in[ix], empty);
	    			
		    		if (k > 1) {
		    			generateCalls(k,s_k,scc);
		    		}
		    		
		    		// set initial states
		    		ControlState init = s_k.getState(initStateOOO(s.name()));
		    		s_k.setInital(init);
		    		// set final states
		    		for (int jj=0; jj<scc_size; jj++) {
		    			s_k.setFinal(s_k.getState(s_fin[scc][jj]));
		    		}
	    			
		    		latestSubsystems.add(s_k);
		    		nts.addSubsystem(s_k);
	    		}
    		}
	    	//}
    	}
		
    	String latest_main = procName_inOrder(k,main_name_new_base);
    	nts.clearInstances();
	    nts.addInstance(latest_main, ASTWithoutToken.litInt(1));
	    
	    genInfo.setLatestSubsystems(latestSubsystems);
	    //genInfo.setLatestMain(nts.s)
	}

	private GenerationInfo genInfo;
	public GenerationInfo getInfo() { return genInfo; }
	//
	private NTS nts;
	private String main_name_new_base;
	private Subsystem templates[];
	
    // identify the procedures with integers
    private Map<Subsystem,Integer> s2i = null;
    private List<Subsystem> i2s = null;
    
    private Map<Subsystem,Integer> s2scc = null;
    private List<Integer> scc2size = null;
    private List<Integer> scc2firstInx = null;
    private int cnt_scc_all;
	
	private List<List<TCall>> templateCalls = new LinkedList<List<TCall>>();
	private List<Set<ControlState>> callsTo;
	
	String[][] s_fin;
    String[] s_init;
	
    // in-order call, input variables
    private String[][] call_1_in = null;
    // out-of-order call, input variables (NOTE: same as actual arguments)
    private String[][] call_2_in = null;
    
    // generated procedures
    private Map<String,Subsystem> map = null;
	    
	private void templateCall(int aSccInx, String aFrom, String aTo, String aToOrig, int aCalleeInx, boolean aIsInOrder, List<IExpr> aActParam) {
		TCall tc = new TCall(aFrom,aTo,aToOrig,aCalleeInx,aIsInOrder,aActParam);
		templateCalls.get(aSccInx).add(tc);
	}
	
	private Expr identity(List<List<VarTableEntry>> vars, int i1, int i2) {
		return identity(vars,i1,i2,null);
	}
	private Expr identity(List<List<VarTableEntry>> vars, int i1, int i2, List<String> exclude) {
		List<VarTableEntry> aux = vars.get(i1-1);
		int size = aux.size();
		Expr ret = ASTWithoutToken.litBool(true);
		for (int i=i2; i<size; i+=4) {
			String name = aux.get(i).name();
			if (exclude != null && exclude.contains(name))
				continue;
			Expr eq = ASTWithoutToken.exEq(
					ASTWithoutToken.accessBasic(name+"'"),
					ASTWithoutToken.accessBasic(name)
					);
//			if (ret == null) {
//				ret = eq;
//			} else {
				ret = ASTWithoutToken.exAnd(ret,eq);
//			}
		}
		return ret;
	}
//	private void addNames(List<String> aL, List<List<VarTableEntry>> vars, int i1, int i2) {
//		List<VarTableEntry> aux = vars.get(i1-1);
//		int size = aux.size();
//		for (int i=i2; i<size; i+=4) {
//			aL.add(aux.get(i).name());
//		}
//	}
	private String procName_inOrder(int k, String sub) {
		return "K_"+k+"_"+sub;
	}
	private String procName_outOfOrder(int k, String sub, String state) {
		return "K_"+k+"_Q_"+state+"_"+sub;
	}
	// initial state for out-of-order calls
	private String initStateOOO(String s) {
		return "INIT_"+s;
	}
	private List<IExpr> actualArguments(String[] a) {
		List<IExpr> ret = new LinkedList<IExpr>();
		for (String s : a) {
			ret.add(ASTWithoutToken.accessBasic(s));
		}
		return ret;
	}
//	private List<IAccessBasic> unprime(List<IAccessBasic> aL) {
//		List<IAccessBasic> ret = new LinkedList<IAccessBasic>();
//		for (IAccessBasic ia : aL) {
//			ASTWithoutToken.accessBasic(ia.var().counterpart().name());
//		}
//		return ret;
//	}
	
	private void generateCalls(int k, Subsystem sub, int template_inx) {
		for (TCall tc : templateCalls.get(template_inx)) {
			ControlState from  = sub.getState(tc.from());
			ControlState to  = sub.getState(tc.to());
			ControlState toOrig  = sub.getState(tc.toOrig());
			Call c = new Call();
			String calleeName;
			int callee_ix = tc.calleeInx()-1;
			String calleeNameOrig = i2s.get(callee_ix).name();
			if (tc.isInOrder()) {
				calleeName = procName_inOrder(k-1,calleeNameOrig);
			} else {
				calleeName = procName_outOfOrder(k-1,calleeNameOrig,toOrig.name());
			}
			c.setCallee(Common.tok_idn(calleeName));
			c.setCallee(map.get(calleeName));
			
			c.addAct(tc.actualParameters()); // not copied !!
			Transition t = new Transition(null, from, to, c);
			sub.addTransition(t);
		}
	}
}
