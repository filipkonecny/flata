package verimag.flata.automata.ca;

import java.io.StringWriter;
import java.util.*;

import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

import verimag.flata.common.*;
import verimag.flata.presburger.*;

// counter-example node
@SuppressWarnings("incomplete-switch")
public class CENode {

	// delimiter
	private static String xx = "_";
	
	private static enum Type {
		COMP, DISJ, LEAF, CALL;
	}
	
	Type type;
	ReduceInfo ri;
	
	int g; // number of global variables
	Variable[] vars_unp;
	Integer[] vi, vo;
	
	CENode[] succ; // zero length for: (1) a leaf node or (2) empty composition (zero loop iteration) or (3) spurious hull (disjunctive) node
	Info this_info;

	
	public Variable[] getGlobalVar() {
		return Arrays.copyOf(vars_unp,g);
	}
	public Integer[] getGlobalValIn() { return getGlobalVal(true); }
	public Integer[] getGlobalValOut() { return getGlobalVal(false); }
	public Integer[] getGlobalVal(boolean in) {
		return Arrays.copyOf(in?vi:vo,g);
	}
	public Variable[] getLocalVar() {
		int l = vars_unp.length;
		Variable[] ret = new Variable[l-g];
		System.arraycopy(vars_unp, g, ret, 0, l-g);
		return ret;
	}
	public Integer[] getLocalValIn() { return getLocalVal(true); }
	public Integer[] getLocalValOut() { return getLocalVal(false); }
	public Integer[] getLocalVal(boolean in) {
		int l = vars_unp.length;
		Integer[] ret = new Integer[l-g];
		System.arraycopy(in?vi:vo, g, ret, 0, l-g);
		return ret;
	}
	
	public String toString() {
		return ""+ri.t.ca.name()+":"+ri.t.name();
		//return Arrays.toString(vi)+"\n"+Arrays.toString(vo)+"\n"+ri.t().rel().toString()+"\n############\n"+show_t_hist();
	}
	
	private void copyFields(CENode from) {
		type = from.type;
		ri = from.ri;
		vars_unp = from.vars_unp;
		vi = from.vi;
		vo = from.vo;
		succ = from.succ;
		this_info = from.this_info;
	}

	public ReduceInfo reduce_info() { return ri; }
	private boolean isSpurious() { return (type == Type.DISJ && succ.length == 0); }	
	private void init_succ_fields(int n) { succ = new CENode[n]; }
	
	private static class Info {
		int real_count; // number of real CE branches
		int min_length; // minimal length of a CE (note: equals zero if loop is taken 0-times)
		int min_real_inx = -1; // index of a node with shortest *real* sub-trace (used only for DISJ nodes)
		int spurious_nodes;
		public Info(int a_real_count, int a_min_length, int a_spurious_nodes) {
			this(a_real_count, a_min_length, a_spurious_nodes, -1);
		}
		public Info(int a_real_count, int a_min_length, int a_spurious_nodes, int a_min_real_inx) { 
			real_count = a_real_count;
			min_length = a_min_length;
			spurious_nodes = a_spurious_nodes;
			min_real_inx = a_min_real_inx;
		}
	}
	
	private CENode(ReduceInfo ari, Variable[] avars_unp, int aG) {
		ri = ari;
		g = aG;
		vars_unp = avars_unp;
		vi = new Integer[vars_unp.length];
		vo = new Integer[vars_unp.length];
		getStartingValues(ri.t.rel());
	}
	
	private CENode(ReduceInfo ari, Variable[] avars_unp, int aG, Integer[] avi, Integer[] avo) {
		ri = ari;
		g = aG;
		vars_unp = avars_unp;
		vi = avi;
		vo = avo;
	}

	
	public void toDotLang(StringBuffer sb, int i) {
		
		int iOld = i;
		String label = this.ri.t.name()+"|"+printArray(vi)+"|"+printArray(vo);
		
		sb.append("node [shape = circle, label=\""+label+"\"] s"+iOld+";\n");
		
		int ii=0;
		for (CENode n : this.succ) {
			i++;
			sb.append("s"+iOld+" -> s"+i+" [label=\""+ii+"\""+"]\n");
			n.toDotLang(sb,i);
		}
		
	}
	public void toDotLang() {
		StringBuffer sb = new StringBuffer();

		sb.append("digraph " + "CE" + " {\n");
		
		toDotLang(sb,0);
		
		sb.append("}\n");
		
	}
	
	private static StringBuffer printArray(Integer[] arr) {
		StringBuffer sb = new StringBuffer("[");
		int i = 1;
		int l = arr.length;
		for (Integer x : arr) {
			if (x == null)
				sb.append("-");
			else 
				sb.append(x.intValue());
			if (i!=l)
				sb.append(",");
			i++;
		}	
		sb.append("]");
		return sb;
	}
		
	public boolean hasSpuriousTrace() {
		return this.this_info.spurious_nodes > 0;
	}
	public boolean hasRealTrace() {
		return (this.this_info.real_count >= 1);
	}
	public static boolean hasRealTrace(Collection<CENode> nodes) {
		for (CENode n : nodes)
			if (n.this_info.real_count > 0) {
				return true;
			}
		return false;
	}
	public static CENode getFirstSpur(Collection<CENode> nodes) {
		for (CENode n : nodes)
			if (n.this_info.real_count == 0)
				return n;
		throw new RuntimeException("internal error: no spurious node found");
	}
	public static CENode rootWithShortestRealTrace(Collection<CENode> nodes) {
		CENode best = null;
		for (CENode n : nodes) {
			if ((best == null || n.this_info.min_length < best.this_info.min_length) && n.this_info.real_count > 0) {
				best = n;
			}
		}
		
		return best;
	}
	
	
	public CEView prepareTraceView() {
		
		if (this.this_info.real_count < 1)
			return null;
		
		Set<CENode> seenCalls = new HashSet<CENode>();
		
		Deque<CENode> stack = new LinkedList<CENode>();
		
		stack.push(this);
		
		Variable[] global = this.getGlobalVar();
		Variable[] local = this.getLocalVar();
		
		
		CEView view = new CEView(global, local, 3);
		
		// process leaves and calls
		
		while (!stack.isEmpty()) {
			
			CENode node = stack.pop();
			
			switch (node.type) {
			case LEAF: {
				CATransition t = node.ri.t();
				String tname = t.name()==null? "_" : t.name();
				StringBuffer[] l1arr = new StringBuffer[] { new StringBuffer(t.ca().name()), new StringBuffer(t.from().name()), new StringBuffer(tname) };
				StringBuffer l2 = new StringBuffer(t.rel().toString());
				view.add(l1arr, l2, node.getGlobalValIn(), node.getLocalValIn());
				
				if (node.getLastProcedureTransition())
					if (!node.getErrorTransition()) { // corresponding transition doesn't lead to an error state
						view.addReturn(node.getGlobalValOut(), node.getLocalValOut());
					} else { // leaf leading to an error state found (last transition of a CE trace)
						StringBuffer[] l1errarr = new StringBuffer[] { new StringBuffer(t.ca().name()), new StringBuffer(t.to().name()), new StringBuffer("error") };
						view.addLast(l1errarr, node.getGlobalValOut(), node.getLocalValOut());
						return view;
					}
				}
				break;
			case COMP:
				for (int i=node.succ.length-1; i>=0; i--) {
					stack.push(node.succ[i]);
				}
				break;
			case DISJ: // no spurious disjunctions can be reached in this search!
				stack.push(node.succ[node.this_info.min_real_inx]);
				break;
			case CALL: {
				
				if (!seenCalls.contains(node)) { // call is seen for the first time
					
					seenCalls.add(node);
					stack.push(node); // process twice
					
					ReduceInfo calling = node.ri.plugged_summary_calling();
					//ReduceInfo called = node.ri.plugged_summary_called();
					Call c = calling.t().call();
					
					CATransition t = node.ri.t();
					String tname = t.name();
					if (tname==null) tname = "";
					StringBuffer[] l1arr = new StringBuffer[] { 
							new StringBuffer(t.ca().name()+"->"+c.called().name()), 
							new StringBuffer(t.from().name()), 
							new StringBuffer(tname) };
					StringBuffer[] l1retarr = new StringBuffer[] { 
							new StringBuffer(t.ca().name()+"<-"+c.called().name()), 
							new StringBuffer(t.to().name()), 
							new StringBuffer("return") };
					StringBuffer l2 = c.getInvocationAssignment();
					StringBuffer l2ret = c.getReturnAssignment();
					
					Variable[] calledLocal = c.called().localUnp();
					
					view.addInvocation(l1arr, l2, node.getGlobalValIn(), node.getLocalValIn(), calledLocal, l1retarr, l2ret);
					
					stack.push(node.succ[0]);
					
				} else { // call seen for the second time (now the view for the called procedure is done)
					
					if (node.getLastProcedureTransition()) {
						view.addReturn(node.getGlobalValOut(), node.getLocalValOut());
					}
					
				}
					
				}
				break;
			}
		}
		
		//trace.prune();
		
		return view;
	}
	
	
	public StringBuffer show_t_hist() {
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, "|  ");
		
		iw.writeln(Arrays.toString(this.vars_unp));
		this.show_t_hist(iw);
		
		return sw.getBuffer();
	}
	public void show_t_hist(IndentedWriter iw) {
		
		String name = this.ri.t.name();
		if (name != null)
			iw.writeln(name);
		iw.writeln(this.ri.t().from().toString()+"->"+this.ri.t().to());
		iw.writeln(this.ri.t().rel().toString());
		iw.writeln(printArray(this.vi));
		iw.writeln(printArray(this.vo));
		
		String marker = "------";
		iw.writeln(marker);
		iw.indentInc();
		
		if (this.type == null)
			return;
		
		switch (this.type) {
		case LEAF:
			break;
		case COMP:
			for (int i=0; i<this.succ.length; i++) {
				if (succ != null)
					this.succ[i].show_t_hist(iw);
			}
			break;
		case DISJ: // no spurious disjunctions can be reached in this search!
			if (this.this_info.min_real_inx >= 0)
				this.succ[this.this_info.min_real_inx].show_t_hist(iw);
			else
				iw.writeln("SPUR");
			break;
		}
		
		iw.indentDec();
		iw.writeln(marker);
	}

	public CENode getFirstSpuriousNode() {
		return this.getSpuriousNodes().get(0);
	}
	// spurious node = hull node where 
	public List<CENode> getSpuriousNodes() {
		if (this.this_info.spurious_nodes < 1)
			return null;
		
		Deque<CENode> stack = new LinkedList<CENode>();
		List<CENode> spur = new LinkedList<CENode>();
		
		stack.push(this);
		while (!stack.isEmpty()) {
			CENode node = stack.pop();
			switch (node.type) {
			case LEAF:
				break;
			case COMP:
				for (int i=node.succ.length-1; i>=0; i--) {
					stack.push(node.succ[i]);
				}
				break;
			case DISJ:
				if (node.isSpurious()) {
					spur.add(node);
				} else {
					for (int i=node.succ.length-1; i>=0; i--) {
						stack.push(node.succ[i]);
					}
				}
			}
		}
		return spur;
	}
	
	private void setInverseBinding() {
		
		List<CENode> todo = new LinkedList<CENode>();
		todo.add(this);
		
		Set<CENode> done = new HashSet<CENode>();
		while (!todo.isEmpty()) {
			CENode n = todo.remove(0);
			if (done.contains(n))
				continue;
			
			done.add(n);
			todo.addAll(Arrays.asList(n.succ));
			
			n.ri.addCENode(n);
		}
	}
	
	private static Variable[] mergeGlobalLocal(Variable[] globalUnp, Variable[] localUnp) {
		int g = globalUnp.length, l = localUnp.length;
		Variable[] allUnp = Arrays.copyOf(globalUnp, g+l);
		System.arraycopy(localUnp, 0, allUnp, g, l);
		return allUnp;
	}
	
	public static CENode buildTree(ReduceInfo ri, Variable[] globalUnp, Variable[] localUnp) {
		
		Variable[] allUnp = mergeGlobalLocal(globalUnp,localUnp);
		
		// construct starting node
		CENode root = new CENode(ri, allUnp, globalUnp.length);
		
		buildTree(root);
		
		root.setInverseBinding();
		
		if (root.succ.length == 0) {
			root.init_succ_fields(0);
		} else {
			root.vi = root.succ[0].vi;
			root.vo = root.succ[root.succ.length-1].vo;
		}
		
		return root;
	}


	private boolean lastProcedureTransition = false;
	private void copyLastProcedureTransition(CENode other) { lastProcedureTransition = other.lastProcedureTransition; }
	private void setLastProcedureTransition() { lastProcedureTransition = true; }
	private boolean getLastProcedureTransition() { return lastProcedureTransition; }
	
	private boolean errorTransition = false; // transition leads to an error state
	private void copyErrorTransition(CENode other) { errorTransition = other.errorTransition; }
	private void setErrorTransition() { errorTransition = true; }
	private boolean getErrorTransition() { return errorTransition; }
	
	private int tb_cnt;
	private Integer[][] tb_vals;
	private List<ReduceInfo> tb_hull_atoms;
	
	private static void buildTree(CENode root) {
		
		root.setLastProcedureTransition();
		root.setErrorTransition();
		
		java.util.Deque<CENode> tb_stack = new java.util.LinkedList<CENode>();
		
		root.tb_cnt = 0;
		tb_stack.push(root);
		
		while (!tb_stack.isEmpty()) {
			
			CENode node = tb_stack.peek();
			
			boolean goon = false;
			
			if (node.tb_cnt == 0) {
				// get valuations for children

				switch (node.ri.op()) {
				case COMPOSE:
					node.processComposition();
					break;
				case CLOSURE:
					node.processClosure();
					break;
				case ABSTROCT:
				case HULL:
					node.processHull();
					break;
				//case PLUGGED_SUMMARY:
				case LEAF:
					node.processLeaf();
					tb_stack.pop();
					goon = true;
					break;
				case RECONNECT:
					node.processReconnect();
					goon = true;
					break;
				case PLUGGED_SUMMARY:
					node.tb_cnt++;
					node.processPluggedSummary();
					tb_stack.push(node.succ[0]);
					goon = true;
					break;
				case IDENTITY:
					node.processId();
					break;
				case SUMMARY: // is skipped
				default:
					System.err.println("\n\n"+node.ri.op);
					throw new RuntimeException("internal error: unexpected type of node");
				}
				
				// reset subtree counter of each child
			}
			
			if (goon)
				continue;

			// PLUGEGD SUMMARY
			if (node.ri.op.isPluggedSummary()) {
				tb_stack.pop();
				node.this_info = node.succ[0].this_info;
			
			// COMPOSITION / CLOSURE
			} else if (node.ri.op.isCompose() || node.ri.op.isClosure()) {
			
				int n = node.succ.length;
				
				// propagate assigned values from previous subtree
				if (node.tb_cnt >= 1) {
					node.tb_vals[node.tb_cnt-1] = node.succ[node.tb_cnt-1].vi;
					node.tb_vals[node.tb_cnt] = node.succ[node.tb_cnt-1].vo;
				}
				
				// based on subtree counter, process corresponding child
				
				if (node.tb_cnt == n) {
					
					// the whole subtree is processed
					
					tb_stack.pop();
					
					// unless 0-iteration of a closure is possible
					
					if (node.succ.length != 0) {
					
						// propagate assigned values from all the subtrees
						node.vi = node.tb_vals[0];
						node.vo = node.tb_vals[n];
						
						int ireal = 1;
						int iminlen = 0;
						int ispur = 0;
						for (CENode asucc : node.succ) {
							Info info = asucc.this_info;
							ireal *= info.real_count;
							iminlen += info.min_length;
							ispur += info.spurious_nodes;
						}
						node.this_info = new Info(ireal,iminlen,ispur);
						
						
					}
					
				} else {
										
					ReduceInfo ri_next = null;
					switch (node.ri.op()) {
					case COMPOSE: {
						ri_next = node.ri.pred.get(node.tb_cnt);
						break;
					}
					case CLOSURE:
						ri_next = node.ri.pred.get(0);
						break;
					}
					
					node.succ[node.tb_cnt] = new CENode(ri_next, node.vars_unp, node.g, node.tb_vals[node.tb_cnt], node.tb_vals[node.tb_cnt+1]);
					node.succ[node.tb_cnt].tb_cnt = 0; // reset
					tb_stack.push(node.succ[node.tb_cnt]);
					
					if (node.tb_cnt == n-1) {
						node.succ[node.tb_cnt].copyLastProcedureTransition(node);
						node.succ[node.tb_cnt].copyErrorTransition(node);
					}
					
					// increase subtree counter
					node.tb_cnt ++;
					
				}
			
			// HULL
			} else if (node.ri.op.isHull() || node.ri.op.isAbstr())  {

				// use the first found real subtree
				
				if (node.succ[0] != null && node.succ[0].this_info.real_count > 0) {
					
					// real subtree found
					
					node.copyFields(node.succ[0]);
					
					tb_stack.pop();
					
				} else {
					
					// TODO: conver, remove
					StringBuffer core = null;
					ReduceInfo ri_next = null;
					
					//int atoms = node.tb_hull_atoms.size();
					Iterator<ReduceInfo> iter = node.tb_hull_atoms.iterator();
					while (iter.hasNext()) {
						
						ReduceInfo tmp = iter.next();
						iter.remove();

						Answer a = node.processHullBase(tmp.t().rel());
						if (a.isDontKnow()) {
							throw new RuntimeException("CE analysis: solver failed - unknown");
						}
						if (a.isTrue()) {
							ri_next = tmp;
							break;
						}
						
						// TODO: convert, remove
						// core = new StringBuffer();
						// YicesAnswer ya = node.processHullBase(tmp.t().rel(), core);
						// if (ya.isUnknown())
						// 	throw new RuntimeException("CE analysis: yices failed - unknown");
						// if (ya.isSat()) {
						// 	ri_next = tmp;
						// 	break;
						// }
					}
					
					if (ri_next == null) {
						
						// no real subtrace
						
						if (node.succ[0] == null)
							node.base_spurious();
						else
							node.copyFields(node.succ[0]); // take the last spurious
						
						tb_stack.pop();
						
					} else {
						node.tb_vals = node.getVals(1, CR.parseModelAssignments());

						// TODO: convert, remove
						// node.tb_vals = node.getVals(1, CR.parseYicesCore(core));
						
						CENode next = new CENode(ri_next, node.vars_unp, node.g, node.tb_vals[0], node.tb_vals[1]);
						next.tb_cnt = 0;
						node.succ[0] = next;
						
						next.copyLastProcedureTransition(node);
						
						tb_stack.push(next);
					
					}
				
				}
			}
		}
		
	}
	
	private Collection<String> getVariableNamesUnpP() {
		Collection<String> ret = new LinkedList<String>();
		for (Variable v : vars_unp) {
			ret.add(v.name());
			ret.add(v.name()+"'");
		}
		return ret;
	}
	private Collection<String> getVariableNames(int n) {
		
		Collection<String> ret = new LinkedList<String>();
		for (Variable v : vars_unp) {
			for (int i=0; i<=n; i++) {
				ret.add(v.toString()+xx+i);
			}
		}
		return ret;
	}
	
	private LinkedList<BooleanFormula> constrainVariablesUnpP(FlataJavaSMT fjsmt, int n) {
		LinkedList<BooleanFormula> ret = new LinkedList<BooleanFormula>();
		for (int i=0; i<vars_unp.length; i++) {
			
			IntegerFormula v = fjsmt.getIfm().makeVariable(vars_unp[i].name());
			IntegerFormula vp = fjsmt.getIfm().makeVariable(vars_unp[i].name() + "'"); // TODO: this little apostrophe messed ME UP!!!!!!!!!!

			if (vi[i] != null) {
				IntegerFormula vi = fjsmt.getIfm().makeNumber(this.vi[i]);
				ret.add(fjsmt.getIfm().equal(v, vi));
			}
			if (vo[i] != null) {
				IntegerFormula vo = fjsmt.getIfm().makeNumber(this.vo[i]);
				ret.add(fjsmt.getIfm().equal(vp, vo));
			}
		}
		return ret;
	}

	// TODO: remove
	private void constrainVariablesUnpP(IndentedWriter iw, int n) {
		for (int i=0; i<vars_unp.length; i++) {
			String v = CR.yicesVarName(vars_unp[i].name());
			if (vi[i] != null)
				iw.writeln("(= "+v+" "+vi[i]+")");
			if (vo[i] != null)
				iw.writeln("(= "+v+"' "+vo[i]+")");
		}
	}

	private LinkedList<BooleanFormula> constrainVariables(FlataJavaSMT fjsmt, int n) {
		LinkedList<BooleanFormula> ret = new LinkedList<BooleanFormula>();
		for (int i=0; i<vars_unp.length; i++) {
			String var = vars_unp[i].name();
			if (vi[i] != null) {
				IntegerFormula v = fjsmt.getIfm().makeVariable(var+xx+0);
				IntegerFormula vi = fjsmt.getIfm().makeNumber(this.vi[i]);
				ret.add(fjsmt.getIfm().equal(v, vi));
			}
			if (vo[i] != null) {
				IntegerFormula v = fjsmt.getIfm().makeVariable(var+xx+n);
				IntegerFormula vo = fjsmt.getIfm().makeNumber(this.vo[i]);
				ret.add(fjsmt.getIfm().equal(v, vo));
			}
		}
		return ret;
	}

	// TODO: remove
	private void constrainVariables(IndentedWriter iw, int n) {
		for (int i=0; i<vars_unp.length; i++) {
			String v = CR.yicesVarName(vars_unp[i].name());
			if (vi[i] != null)
				iw.writeln("(= "+v+xx+0+" "+vi[i]+")");
			if (vo[i] != null)
				iw.writeln("(= "+v+xx+n+" "+vo[i]+")");
		}
	}

	// TODO: remove
	private void assertAnd_begin(IndentedWriter iw) {
		iw.writeln("(assert");
		iw.indentInc();
		
		iw.writeln("(and");
		iw.indentInc();
	}

	// TODO: remove
	private void assertAnd_end(IndentedWriter iw) {
		iw.indentDec();
		iw.writeln(")"); // and
		
		iw.indentDec();
		iw.writeln(")"); // assert		
	}

	private LinkedList<BooleanFormula> begin(FlataJavaSMT fjsmt, Collection<String> varNames, int n) {
		return begin(fjsmt, varNames, n, true, false);
	}
	private LinkedList<BooleanFormula> begin(FlataJavaSMT fjsmt, Collection<String> varNames, int n, boolean constrainVars, boolean usePrime) {
		if (constrainVars) {
			if (usePrime) {
				return constrainVariablesUnpP(fjsmt, n);
			} else {
				return constrainVariables(fjsmt, n);
			}
		}
		return new LinkedList<BooleanFormula>(); // TODO: return null???
	}

	// TODO: remove
	private void begin(IndentedWriter iw, Collection<String> varNames, int n) {
		begin(iw, varNames, n, true, false);
	}
	private void begin(IndentedWriter iw, Collection<String> varNames, int n, boolean constrainVars, boolean usePrime) {
		iw.writeln("(reset)\n");
		iw.writeln("(set-evidence! true)");
		iw.writeln();
		CR.prepareYicesDeclaration(varNames, iw);
		iw.writeln();
		
		assertAnd_begin(iw);
		
		if (constrainVars) {
			if (usePrime)
				constrainVariablesUnpP(iw, n);
			else
				constrainVariables(iw, n);
		}
	}

	private Answer end(FlataJavaSMT fjsmt, LinkedList<BooleanFormula> formulas) {
		return fjsmt.isSatisfiable(fjsmt.getBfm().and(formulas), false, true);
	}
	private Map<String,String> end_get_core(FlataJavaSMT fjsmt, LinkedList<BooleanFormula> formulas) {
		Answer a = end(fjsmt, formulas);

		if (!a.isTrue()) {
			System.err.println(a);
			throw new RuntimeException("CE analysis: internal error");
		}
		
		return CR.parseModelAssignments();
	}
	
	// TODO: remove
	private YicesAnswer end(IndentedWriter iw, StringWriter sw, StringBuffer core) {
		assertAnd_end(iw);
		
		iw.writeln("(check)");
		
		//System.err.print("#");
		return CR.isSatisfiableYices(sw.getBuffer(), core);
	}
	// TODO: remove
	private Map<String,String> end_get_core(IndentedWriter iw, StringWriter sw) {
	
		StringBuffer core = new StringBuffer();
		YicesAnswer ya = end(iw, sw, core);
		if (!ya.isSat()) {
			System.err.println(ya);
			throw new RuntimeException("CE analysis: internal error");
			//System.err.println("[[["+this.ri.origSources.get(0).t.id()+"]]]");
		}
		
		return CR.parseYicesCore(core);
	}
	
	private void getStartingValues(CompositeRel r) {
		getStartingValues(r, false);
	}
	private void getStartingValues(CompositeRel r, boolean useCurrentAssignment) {
		
		int n = 1;
		Collection<String> varNames = getVariableNames(n);
		
		FlataJavaSMT fjsmt = CR.flataJavaSMT;

		// Begin AND
		LinkedList<BooleanFormula> formulasAND = begin(fjsmt, varNames, n, useCurrentAssignment, false);
		formulasAND.add(r.toJSMTAsConj(fjsmt, xx+"0", xx+"1"));

		// End AND
		Map<String,String> mapping = end_get_core(fjsmt, formulasAND);

		// TODO: remove
		// StringWriter sw = new StringWriter();
		// IndentedWriter iw = new IndentedWriter(sw);
		
		// begin(iw, varNames, n, useCurrentAssignment, false);
		
		// r.toSBYicesAsConj(iw, xx+"0", xx+"1");
		
		// Map<String,String> mapping = end_get_core(iw, sw);
		
		for (int i=0; i<vars_unp.length; i++) {
			String vname = vars_unp[i].name();
			String v1 = mapping.get(vname+xx+"0");
			String v2 = mapping.get(vname+xx+"1");
			vi[i] = (v1 == null)? null : Integer.parseInt(v1);
			vo[i] = (v2 == null)? null : Integer.parseInt(v2);
		}
	}

	private Integer[][] getVals(int n, Map<String,String> mapping) {
		int nVars = vars_unp.length;
		Integer[][] vals = new Integer[n+1][];
		
		for (int ii=0; ii<=n; ii++) {
			vals[ii] = new Integer[nVars];
			for (int jj=0; jj<vars_unp.length; jj++) {
				String vname = vars_unp[jj].name();
				String v = mapping.get(vname+xx+ii);
				vals[ii][jj] = (v == null)? null : Integer.parseInt(v);
			}
		}
		
		return vals;
	}
	
	
	// n >= 0
	private void processComposition_base(int n) {
		
		Collection<String> varNames = getVariableNames(n);

		FlataJavaSMT fjsmt = CR.flataJavaSMT;
		
		LinkedList<BooleanFormula> formulasAND = begin(fjsmt, varNames, n);

		// TODO: remove
		// StringWriter sw = new StringWriter();
		// IndentedWriter iw = new IndentedWriter(sw);
		
		// begin(iw, varNames, n);
		
		switch (ri.op()) {
		case COMPOSE: {
			int i=0;
			for (ReduceInfo source : ri.pred) {
				// source.t.rel().toSBYicesAsConj(iw, xx+i, xx+(i+1)); // TODO: remove
				formulasAND.add(source.t.rel().toJSMTAsConj(fjsmt, xx+i, xx+(i+1)));
				i++;
			}
			break;
		}
		case CLOSURE:
			ReduceInfo source = ri.pred.iterator().next();
			// composition of source with itself n-times
			for (int i=0; i<n; i++) {
				// source.t.rel().toSBYicesAsConj(iw, xx+i, xx+(i+1)); // TODO: remove
				formulasAND.add(source.t.rel().toJSMTAsConj(fjsmt, xx+i, xx+(i+1)));
			}
			break;
		default:
			throw new RuntimeException("CE analysis: internal error");
		}
		
		Map<String,String> mapping = end_get_core(fjsmt, formulasAND);

		// Map<String,String> mapping = end_get_core(iw, sw); // TODO: remove
		
		// not precise enough! -- detailed relation may assign some free values
		//vals[0] = vi;
		//vals[n] = vo;
		
		tb_vals = getVals(n, mapping);
		init_succ_fields(n);
	}
	
	private void processComposition() {
		
		if (ri.pred.size() < 2)
			throw new RuntimeException("internal error: CE analysis, composition lacks arguments");
		
		type = Type.COMP;
		
		// n relations, variable suffixes 0-n
		int n = ri.pred.size();
		this.processComposition_base(n);
	}
	
	// TODO: convert, remove
	private int getMinIterations_base(int low, int up) {
		
		int n = 1;
		
		Collection<String> varNames = getVariableNamesUnpP();
		
		ClosureDisjunct cd = ri.t.rel().closure_disjunct();
		String paramName = cd.parameter.name();
		varNames.add(paramName);

		FlataJavaSMT fjsmt = CR.flataJavaSMT;
		
		LinkedList<BooleanFormula> formulasAND = begin(fjsmt, varNames, n, true, true);
		
		formulasAND.add(cd.periodic_param.toJSMTAsConj(fjsmt));
		
		IntegerFormula param = fjsmt.getIfm().makeVariable(paramName);
		if (low >= 0) {
			formulasAND.add(fjsmt.getIfm().greaterOrEquals(param, fjsmt.getIfm().makeNumber(low)));
		}
		if (up >= 0) {
			formulasAND.add(fjsmt.getIfm().lessOrEquals(param, fjsmt.getIfm().makeNumber(up)));
		}
		
		Answer a = end(fjsmt, formulasAND);
		if (a.isDontKnow()) {
			throw new RuntimeException("CE analysis: solver - unknown");
		} else if (a.isFalse()) {
			return -1;
		}
		
		
		Map<String, String> mapping = CR.parseModelAssignments();
		return  Integer.parseInt(mapping.get(paramName));
		
		// TODO: remove	
		// StringWriter sw = new StringWriter();
		// IndentedWriter iw = new IndentedWriter(sw);
		
		// begin(iw, varNames, n, true, true);
		
		// cd.periodic_param.toSBYicesAsConj(iw);
		
		// if (low >= 0)
		// 	iw.writeln("(>= "+CR.yicesVarName(paramName)+" "+low+")");
		// if (up >= 0)
		// 	iw.writeln("(<= "+CR.yicesVarName(paramName)+" "+up+")");

		// StringBuffer core = new StringBuffer();
		// YicesAnswer ya = end(iw, sw, core);
		// if (ya.isUnknown())
		// 	throw new RuntimeException("CE analysis: yices - unknown");
		// else if (ya.isUnsat())
		// 	return -1;
	
		// Map<String,String> mapping = CR.parseYicesCore(core);
			
		// return Integer.parseInt(mapping.get(paramName));
	}
	
	private int getMinIterations() {
		CompositeRel rel = ri.t.rel();
		
		if (rel.isTrue())
			return 1;
			
		ClosureDisjunct cd = rel.closure_disjunct();
		
		if (cd.isPrefix)
			return cd.pref_inx;
		else {
			int low = 0; // k from delta closure ranges {0,1,...}, e.g. R^3+k*Delta
			int n = getMinIterations_base(low, -1);
			
			if (n < low) {
				throw new RuntimeException("internal error");
			}
			
			int up = n;
			while (low != up) {
				int half = (low+up) / 2;
				int nn = getMinIterations_base(low, half);
				if (nn < 0)
					low = half+1;
				else
					up = half;
			}
			// low == up
			return cd.b+cd.c*low+cd.offset;
		}
	}
	
	private void forceIdentity() {
		for (int i=0; i<vars_unp.length; i++) {
			if (vi[i] == null && vo[i] == null) {
				vi[i] = new Integer(0);
				vo[i] = new Integer(0);
			} else if (vi[i] == null) {
				vi[i] = vo[i];
			} else if (vo[i] == null) {
				vo[i] = vi[i];
			}
		}
	}
	private boolean canIdentity() {
		for (int i=0; i<vars_unp.length; i++) {
			if (vi[i] != null && vo[i] != null && vi[i] != vo[i])
				return false;
		}
		return true;
	}
	
	private void processId() {	
		type = Type.COMP;
		forceIdentity();
		this.init_succ_fields(0);
		this_info = new Info(1,0,0); // zero length
	}
	
	private void processClosure() {
		
		type = Type.COMP;
		
//		int n=-1;
//		if (canIdentity()) { // TODO: is this correct?
//			n=0;
//		}
//		if (n!=0) {
//			n = getMinIterations();
//		}
//		if (n==0) {
//			forceIdentity();
//			this.init_succ_fields(0);
//			this_info = new Info(1,0,0); // zero length
//		} else {
//			this.processComposition_base(n);
//		}
	
		if (canIdentity()) { // TODO: is this correct?
			forceIdentity();
			this.init_succ_fields(0);
			this_info = new Info(1,0,0); // zero length
		} else {
			int n = getMinIterations();
			
			// special case: n=0
			if (n == 0) {
				// create R^0
				CATransition aux = this.ri.t.zeroPower();
				// skip closure node
				ri = aux.reduce_info();
			} else {
				if (Parameters.isOnParameter(Parameters.CE_NOLONG) &&
						n > Integer.parseInt(Parameters.getParameter(Parameters.CE_NOLONG).arguments()[0])) {
					throw new StopReduction(StopReduction.StopType.NOLONGTRACE);
				}
				this.processComposition_base(n);
			}
		}
	}

	public Answer processHullBase(CompositeRel r) {
		int n = 1;

		Collection<String> varNames = getVariableNames(n);

		FlataJavaSMT fjsmt = CR.flataJavaSMT;

		LinkedList<BooleanFormula> formulasAND = begin(fjsmt, varNames, n);

		formulasAND.add(r.toJSMTAsConj(fjsmt, xx+"0", xx+"1"));

		return end(fjsmt, formulasAND);
	}

	// TODO: convert, remove
	// public YicesAnswer processHullBase(CompositeRel r) {
	// 	return processHullBase(r, new StringBuffer());
	// }
	// public YicesAnswer processHullBase(CompositeRel r, StringBuffer core) {
		
	// 	int n = 1;
		
	// 	Collection<String> varNames = getVariableNames(n);
		
	// 	StringWriter sw = new StringWriter();
	// 	IndentedWriter iw = new IndentedWriter(sw);
		
	// 	begin(iw, varNames, n);
		
	// 	r.toSBYicesAsConj(iw, xx+"0", xx+"1");

	// 	return end(iw,sw,core);
	// }
	
	private void base_spurious() {
		type = Type.DISJ;
		init_succ_fields(0);
		this_info = new Info(0,1,1);
	}
	

	private void processHull() {
		
		// find all atomic transitions
		List<ReduceInfo> l = new LinkedList<ReduceInfo>();
		tb_hull_atoms = new LinkedList<ReduceInfo>();
		//this.show_t_hist();
		l.add(this.ri);
		while (!l.isEmpty()) {
			ReduceInfo n = l.remove(0);
			if (n.op.isHull()) {
				l.addAll(n.pred);
				l.addAll(n.subsumed);
			} else if (n.op.isAbstr()) {
				l.addAll(n.pred);
			} else {
				tb_hull_atoms.add(n);
			}
		}
		
		this.tb_cnt = -1;
		
		// the first found real subtree will be used
		
		init_succ_fields(1);
		
	}
	
	private void processLeaf() {
		type = Type.LEAF;
		init_succ_fields(0);
		this_info = new Info(1,1,0);
	}
	
	private void processReconnect() {
		// skip reconnect node
		ri = ri.pred.get(0);
	}
	
	private void processPluggedSummary() {
		
		type = Type.CALL;
		
		// prepare valuations for the successor node 
		
		Variable[] global = Arrays.copyOf(vars_unp,g);
		ReduceInfo calling = this.ri.plugged_summary_calling();
		ReduceInfo called = this.ri.plugged_summary_called();
		Call c = calling.t().call();
		Variable[] local = c.called().localUnp();
		
		Map<Variable,Integer> m = new HashMap<Variable,Integer>();
		for (int i=0; i<vars_unp.length; i++) {
			if (vi[i] != null) {
				m.put(vars_unp[i], vi[i]);
			}
			if (vo[i] != null) {
				m.put(vars_unp[i].getCounterpart(), vo[i]);
			}
		}
		
		// get assignments to ports (input and output local counters) of the called procedure
		Integer[] portInVal = c.evaluateInputs(m);
		Integer[] portOutVal = c.evaluateOutputs(m);
		
		int lg = global.length, ll = local.length, l = lg+ll;
		Integer[] inVal = new Integer[l];
		Integer[] outVal = new Integer[l];
		
		// copy valuations of globals
		System.arraycopy(vi, 0, inVal, 0, lg);
		System.arraycopy(vo, 0, outVal, 0, lg);
		
		// copy valuations from ports to locals 
		Variable[] portIn = c.called().portIn(); // unprimed
		Variable[] portOut = c.called().portOut(); // primed
		for (int i=0; i<portIn.length; i++) {
			if (portInVal[i] != null) {
				int inx = Arrays.binarySearch(local, portIn[i]);
				inVal[lg+inx] = portInVal[i];
			}
		}
		for (int i=0; i<portOut.length; i++) {
			if (portOutVal[i] != null) {
				int inx = Arrays.binarySearch(local, portOut[i].getCounterpart());
				outVal[lg+inx] = portOutVal[i];
			}
		}
		
		Variable[] allVarsUnp = CENode.mergeGlobalLocal(global, local);

		
		// set the successor
		
		init_succ_fields(1);
		ReduceInfo redinf = called.summary_pred(); // skips the SUMMARY node !!
		succ[0] = new CENode(redinf, allVarsUnp, lg, inVal, outVal);
		
		// create valuation for remaining locals
		
		succ[0].getStartingValues(redinf.t().rel(), true);
		
		succ[0].setLastProcedureTransition();
		succ[0].copyErrorTransition(this);
	}

	public ConstProps consts() {
		ConstProps ret = new ConstProps();
		for (int i = 0; i < vars_unp.length; i ++) {
			if (vi[i] != null)
				ret.add(new ConstProp(vars_unp[i],vi[i]));
			if (vo[i] != null)
				ret.add(new ConstProp(vars_unp[i].getCounterpart(),vo[i]));
		}
		return ret;
	}
	public CompositeRel createSpurRel() {
		return new CompositeRel(consts());
	}
	
	// returns list of transitions which refine spurs
	// fills spurs with all transitions that have CE valuation as their model
	public List<CATransition> excludeSpurious(Collection<ReduceInfo> spurs) {
		
		List<CATransition> l = new LinkedList<CATransition>();
		// todo list contains spurious transitions (== transitions satisfying spurious assignment)
		List<ReduceInfo> todo = new LinkedList<ReduceInfo>();
		
		todo.add(this.reduce_info());
		while (!todo.isEmpty()) {
			ReduceInfo ri = todo.remove(0);
			spurs.add(ri);
			
			List<ReduceInfo> ops = new LinkedList<ReduceInfo>(ri.pred);
			ops.addAll(ri.subsumed);
			for (ReduceInfo rri : ops) {
				
				if (!rri.op.isHull()) {
					
					l.add(rri.t());
					
				} else {
					
					Answer a = processHullBase(rri.t.rel());

					if (a.isDontKnow()) {
						throw new RuntimeException("CE analysis: solver failed - unknown");
					}
					if (a.isTrue()) {
						todo.add(rri);
					} else {
						l.add(rri.t());
					}

					// TODO: remove
					// YicesAnswer ya = processHullBase(rri.t.rel());
					
					// if (ya.isUnknown())
					// 	throw new RuntimeException("CE analysis: yices failed - unknown");
					// if (ya.isSat())
					// 	todo.add(rri);
					// else {
					// 	l.add(rri.t());
					// }
					
				}
			}
		}
		
		return l;
	}
}
