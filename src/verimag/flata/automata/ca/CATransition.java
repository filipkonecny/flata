package verimag.flata.automata.ca;

import java.io.StringWriter;
import java.util.*;

import verimag.flata.automata.*;
import verimag.flata.common.Answer;
import verimag.flata.common.CR;
import verimag.flata.common.IndentedWriter;
import verimag.flata.common.Label;
import verimag.flata.common.Parameters;
import verimag.flata.common.YicesAnswer;
import verimag.flata.presburger.*;

public class CATransition extends BaseArc implements java.lang.Comparable<CATransition> {
	
	public boolean TERM_FLAG = false;

	public static long runtime_closure = 0;
	
	private static int id_gen = 1;
	public static boolean historyInNames = false;
	
	private Integer id = new Integer(id_gen++);
	private String name;
	private String oldName;
	private CAState from;
	private CAState to;
	private Label label;
	// origin of creation 
	private ReduceInfo r_info = null;
	protected CA ca = null; // reverse binding
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	public Integer id() { return id; }
	
	public String name() { /*if (name==null) throw new RuntimeException();*/ return name; }
	public void name(String aName) { name = aName; }
	
	public String oldName() { return oldName; }
	public void oldName(String aName) { oldName = aName; }
	
	public CAState from() { return from; }
	public void from(CAState aFrom) { from = aFrom; }

	public CAState to() { return to; }
	public void to(CAState aTo) { to = aTo; }
	
	// get label
	public boolean calls() { return label.isCall(); }
	public Label label() { return label; }
	public Call call() { return (Call) label; }
	public CompositeRel rel() { return labAsRel(); }
	private CompositeRel labAsRel() { return (CompositeRel) label; }
	// set label
	public void rel(CompositeRel aRel) { label = aRel; }
	 
	public ReduceInfo reduce_info() { return r_info; }
	
	public CA ca() { return ca; }
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	public CATransition(CAState aFrom, CAState aTo, Label aLabel, CA aCA) {
		this(aFrom, aTo, aLabel, 1, null, aCA);
	}
	public CATransition(CAState aFrom, CAState aTo, Label aLabel, String aName, CA aCA) {
		this(aFrom, aTo, aLabel, 1, aName, aCA);
	}
	private CATransition(CAState aFrom, CAState aTo, Label aLabel, int aWeight, String aName, CA aCA) {
		super(aWeight);
		
		ca = aCA;

		from = aFrom;
		to = aTo;
		label = aLabel;
		
		name = aName;

		r_info = ReduceInfo.leaf(this);
	}
	
	// copy constructors
	public CATransition(CATransition aTrans) {
		this(aTrans, true);
	}
	public static Label copyLabel(Label other, CATransition newT) {
		if (other.isRelation())
			return ((CompositeRel)other).copy();
		else 
			return ((Call)other).copy(newT);
	}
	public static Label copyLabel(Rename ren, VariablePool aVarPool, Label other, CATransition newT) {
		if (other.isRelation())
			return ((CompositeRel)other).copy(ren, aVarPool);
		else 
			return ((Call)other).copy(ren, aVarPool, newT);
	}
	
	public CATransition(CATransition other, boolean copyConstraints) {
		super(other);

		ca = other.ca;

		name = other.name;
		from = other.from;
		to = other.to;
		
		if (copyConstraints) {
			label = copyLabel(other.label, this);
		}
		
		//r_info = other.r_info.copy();
	}
	public CATransition(CATransition other, boolean copyConstraints, Rename ren, VariablePool aVarPool) {
		super(other);

		ca = other.ca;

		name = other.name;
		from = other.from;
		to = other.to;
		
		if (copyConstraints) {
			label = copyLabel(ren, aVarPool, other.label, this);
		}

		//r_info = other.r_info.copy();
	}

	// inline
	public CATransition(CATransition other, CAState aFrom, CAState aTo, String aName, CA aCA, Variable[] aLocalOld) {
		super(other);

		ca = aCA;
		name = aName;
		from = aFrom;
		to = aTo;
		
		label = copyLabel(other.label, this);
		
		if (label.isRelation()) {
			((CompositeRel)(label)).addImplicitActionsForSorted(aLocalOld);
		}
		
		//r_info = other.r_info.copy();
	}

	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	public void removeAllConstraints() {
		label = null;
	}

//	/**
//	 * Two transitions are equal if and only if their origins, destinations,
//	 * guards and actions are equal.
//	 */
//	public boolean equals(Object aObj) {
//	if (!(aObj instanceof CATransition))
//		return false;
//	
//	CATransition other = (CATransition)aObj;
//	boolean b = from.equals(other.from()) && to.equals(other.to())
//			&& label.equals(other.label);
//	return b;
//}
//
//public int hashCode() {
//	int h = from.hashCode() + to.hashCode() + label.hashCode();
//	return h;
//}
	public boolean equals(Object aObj) {
		return	aObj instanceof CATransition &&
				id == ((CATransition)aObj).id;
	}

	public int hashCode() {
		return id;
	}

	// method only for debugging (sorted transitions)
	// comparison based on 'from' and 'to' states only
	public int compareTo(CATransition aAnother) {
		return this.id.compareTo(aAnother.id);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	

	public static void unsetMetaReach(Collection<CATransition> loops) {
		
		Collection<ReduceInfo> visited = new LinkedList<ReduceInfo>();
		List<ReduceInfo> todo = new LinkedList<ReduceInfo>();
		for (CATransition t : loops)
			todo.add(t.r_info);
		
		while (!todo.isEmpty()) {
			ReduceInfo ri = todo.remove(0);
			if (!visited.contains(todo)) {
				todo.addAll(ri.succ);
				
				//ri.meta = false;
				ri.useful = false;
				visited.add(ri);
			}
		}
	}
	public static void setMeta(Collection<CATransition> col) {
		for (CATransition t : col) {
			//t.r_info.meta = true;
			t.r_info.useful = true;
		}
	}
	private static boolean searchUseful(ReduceInfo ri, Collection<ReduceInfo> visited) {
		if (visited.contains(ri))
			return ri.useful;
		
		boolean b = ri.useful;
		for (ReduceInfo ri2 : ri.succ) {
			boolean b2 = searchUseful(ri2, visited);
			b = b || b2;
		}
		
		ri.useful = b; //ri.useful = ri.meta || b;
		visited.add(ri);
		return ri.useful;
	}
	public static void pruneUnuseful(Collection<CATransition> loops) {
		
		// TODO: reset flags
		
		// propagate usefulness flag in the tree
		Collection<ReduceInfo> visited = new LinkedList<ReduceInfo>();
		for (CATransition l : loops) {
			//l.r_info.useful = searchUseful(l.r_info, visited);
			searchUseful(l.r_info, visited);
			l.r_info.useful = true; // keep loops as useful
		}
		
		// disconnect all unuseful
		for (ReduceInfo r : visited) {
			if (!r.useful) {
				for (ReduceInfo ri : r.pred) {
					ri.succ.remove(r);
				}
			}
		}
	}
	


	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	
	
	
	public CATransition reconnect(CAState aFrom, CAState aTo) {
		CATransition ret = new CATransition(this, false); // don't copy relation
		ret.label = this.label;
		if (!from.equals(aFrom))
			ret.from = aFrom;
		if (!to.equals(aTo))
			ret.to = aTo;
		ret.r_info = ReduceInfo.reconnect(ret, this.r_info);
		return ret;	
	}
	
	// inclusion holds => merge condition holds
	public CATransition mergeWithIncluded(CATransition other) {
		return this.merge(other, this.labAsRel());
	}
	public CATransition merge(CATransition other) {
		CompositeRel rel = this.labAsRel().merge(other.labAsRel());
		return this.merge(other, rel);
	}
	private CATransition merge(CATransition other, CompositeRel rel) {
		//CompositeRel rel = this.rel.merge(other.rel);
		
		if (rel == null)
			return null;
		
		String s = null;
		if (historyInNames)
			s = "("+this.name+")U("+other.name+")";
		CATransition ret = new CATransition(this.from, this.to, rel, s, this.ca);
		ret.r_info = ReduceInfo.hull(ret, this.r_info, other.r_info);
		return ret;
		//return new CATransition(this.from, this.to, rel, this.ca);
	}
	public CATransition hullOct(CATransition other) {
		CompositeRel relhull = other.labAsRel().hullOct(this.labAsRel());
		CATransition ret = new CATransition(this.from, this.to, relhull, this.ca);
		ret.r_info = ReduceInfo.hull(ret, this.r_info, other.r_info);
		return ret;
	}
	public static void linkNewHull(CATransition someold, CATransition tnew) {
		ReduceInfo.linkNewHull(someold.r_info, tnew.r_info);
	}
	
	public CATransition abstractOct() { // TODO: remove this method
		if (this.labAsRel().getType().isInClass(Relation.RelType.OCTAGON))
			return this;
		CompositeRel relabstr = this.labAsRel().hull(Relation.RelType.OCTAGON);
		CATransition ret = new CATransition(this.from, this.to, relabstr, this.ca);
		ret.r_info = ReduceInfo.abstr(ret, this.r_info, ReduceOp.ABSTROCT);
		return ret;
	}
	public CATransition hull(Relation.RelType aType) {
		if (this.labAsRel().getType().isInClass(aType))
			return this;
		CompositeRel relabstr = this.labAsRel().hull(aType);
		CATransition ret = new CATransition(this.from, this.to, relabstr, this.ca);
		ret.r_info = ReduceInfo.abstr(ret, this.r_info, ReduceOp.reltype2redtype(aType));
		return ret;
	}
	public static Collection<CATransition> abstractOct(Collection<CATransition> aCol) {
		Collection<CATransition> ret = new LinkedList<CATransition>();
		for (CATransition t : aCol) {
			ret.add(t.abstractOct());
		}
		return ret;
	}
	
	public CATransition[] closureStar() {
		return closure(false);
	}
	public CATransition[] closurePlus() {
		return closure(true);
	}
//	public CATransition[] closureVarPeriod(boolean plus, Variable v) {
//		CompositeRel[] cl = this.labAsRel().closureVarPeriod(plus, v);
//		if (cl == null)
//			return null;
//		else
//			return CATransition.cl2Transitions(cl, this);
//	}
	public CATransition[] closure(boolean plus) {
		long s = System.currentTimeMillis();
		
		//System.out.println("Accelerating "+ this.rel);		
		CompositeRel[] cl;
		if (plus) {
			cl = this.labAsRel().closurePlus();
		} else {
			cl = this.labAsRel().closureStar();
		}
		//System.out.println("Result: "+ Arrays.toString(cl));
		
		runtime_closure += (System.currentTimeMillis() - s);
		if (cl == null)
			return null;
		else
			return CATransition.cl2Transitions(cl, this);
	}
	private static CATransition[] cl2Transitions(CompositeRel[] relations, CATransition loop) {

		CATransition[] transitions = new CATransition[relations.length];

		CAState r_label = loop.ca.reduceLabel();
		
		for (int i=0; i<relations.length; i++) {
			CATransition t = new CATransition(loop, false);

			t.rel(relations[i]);
			String s = null;
			if (historyInNames)
				s = "("+loop.name+")*";
			t.name = s;
			t.r_info = ReduceInfo.closure(t, loop.r_info, r_label);

			transitions[i] = t;
		}

		return transitions;
	}
	
	public CATransition[] compose(CATransition other) {
		return CATransition.compose(this, other);
	}
	
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	private StringBuffer toSBufFLATA(int aIndent) {
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, aIndent);
		
		String n = (name == null || name.equals("null"))? "" : this.name + ": ";
		iw.write(n + this.from + " -> " + this.to);
		
		if (Parameters.isOnParameter(Parameters.THIST) && this.oldName != null)
			iw.write("  /* composed as: "+this.from.oldName()+"  -----"+this.oldName+"----->  "+this.to.oldName()+"  */");
		
		//iw.writeln();
		
		//iw.indentInc();
		//ModuloRel r = rel.toModuloRel();

		// iw.writeln(" ["+guards.toSBClever(Variable.ePRINT_prime)+"]");
		// iw.writeln(" {"+actions.toSBClever(Variable.ePRINT_prime)+"}");
		//iw.writeln(" {" + r.toSBClever(Variable.ePRINT_prime) + "}");
		iw.writeln(" {" + label.toString() + "}");
		//iw.indentDec();

		return sw.getBuffer();
	}
	
	public StringBuffer toSBufARMC(int aIndent, String data, String datap, String id) {
		return toSBufARMC_renamed(aIndent, name, from.name(), to.name(), data, datap, id);
	}

	public StringBuffer toSBufARMC_renamed(int aIndent, String tName, String fromName, String toName, String data, String datap, String id) {
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, aIndent);

		iw.writeln("r(p(pc(" + fromName + "), " + data + "),");
		iw.indentInc();
		iw.writeln("p(pc(" + toName + "), " + datap + "),");


		LinearRel lr = labAsRel().toLinearRel();

		//Collection<LinearConstr> ineqs = new LinkedList<LinearConstr>();
		//Collection<LinearConstr> eqs = new LinkedList<LinearConstr>();
		//lr.separateEqualities(ineqs, eqs);
		
		
		iw.writeln("[" + LinearRel.toStringARMC_nice(false, lr.guards().constraints()) + "],");
		iw.writeln("[" + LinearRel.toStringARMC_nice(false, lr.actions().constraints()) + "],");
		//iw.writeln("[" + lr.guards().toSBClever(Variable.ePRINT_p_armcPref) + "],");
		//iw.writeln("[" + lr.actions().toSBClever(Variable.ePRINT_p_armcPref) + "],");

		iw.writeln("" + id + ").");

		return sw.getBuffer();
	}

	public StringBuffer toSBuf(int aType, int aIndent) {
		if (aType == CR.eOUT_FAST)
			return toSBufFAST(aIndent);
		else if (aType == CR.eOUT_FLATA)
			return toSBufFLATA(aIndent);
		else
			throw new RuntimeException();
	}

	public String toString() {
		return toSBuf(CR.eOUT_FLATA, 0).toString(); // indent 0
	}

	public StringBuffer toSBufTREX(int aIndent) {
		
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, aIndent);

		//iw.writeln("transition " + this.name);
		iw.indentInc();
		if (oldName != null)
			iw.writeln("/* composed as: "+this.from.oldName()+"  -----"+this.oldName+"----->  "+this.to.oldName()+"  */");

		iw.writeln("from " + this.from);
		iw.indentInc();
		
		LinearRel lr = labAsRel().toLinearRel();
		
		LinearRel g = lr.guards();
		LinearRel a= lr.actions();
		if (g.size()!=0)
			iw.writeln("if " + g.toSBTREX(true));
		if (a.size()!=0)
			iw.writeln("do " + a.toSBTREX(false));
		
		iw.indentDec();
		iw.writeln("to " + this.to + ";");
		iw.indentDec();
		
		return sw.getBuffer();
	}
	
	private StringBuffer toSBufFAST(int aIndent) {
		
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, aIndent);

		iw.writeln("transition " + this.name + " := {");
		iw.indentInc();
		if (oldName != null)
			iw.writeln("/* composed as: "+this.from.oldName()+"  -----"+this.oldName+"----->  "+this.to.oldName()+"  */");

		iw.writeln("from := " + this.from + " ;");
		iw.writeln("to := " + this.to + " ;");

		
//		LinearRel lr = rel.toLinearRel();
//
//		LinearRel g = lr.guards();
//		iw.writeln("guard := " + ((g.size()==0)? "true" : g.toSBFAST(true)) + " ;");
//		iw.writeln("action := " + lr.actions().toSBFAST(false) + " ;");
		
		ModuloRel mr = labAsRel().toModuloRel();

		LinearRel mr_g = mr.linConstrs().guards(); int s1 = mr_g.size();
		ModuloConstrs mr_mods = mr.modConstrs(); int s2 = mr_mods.size();
		StringBuffer modulos = new StringBuffer();
		if (s1 != 0 && s2 != 0)
			modulos.append(" && ");
		if (s2 != 0)
			modulos.append(mr.modConstrs().toSBFAST());
		
		iw.writeln("guard := " + ((s1+s2==0)? "true" : mr_g.toSBFAST(true)) + modulos +" ;");
		iw.writeln("action := " + mr.linConstrs().actions().toSBFAST(false) + " ;"); // assumption: primes are only on actions

		iw.indentDec();

		iw.writeln("};");

		return sw.getBuffer();

	}

	public StringBuffer toStringBufferDot() {
		StringBuffer sb = new StringBuffer();

		LinearRel lr = labAsRel().toLinearRel();

		String nn = ((name==null || name.equals("null"))? "" : name+": ");
		LinearRel actions = lr.actions();
		//LinearRel actions = LinearRel.removeImplActions(lr.actions());
		sb.append("\n" + nn + " [" + lr.guards().toSBClever(Variable.ePRINT_prime) + "]" + " {"
				+ actions.toSBClever(Variable.ePRINT_prime) + "}");

		// TODO: add escape sequences
		sb = new StringBuffer(sb.toString().replaceAll("\\n", ""));

		return sb;
	}

	public static void toSBYicesListPart(Collection<CATransition> col, IndentedWriter iw) {

		if (col.size() == 1) {

			col.iterator().next().labAsRel().toSBYicesAsConj(iw);

		} else {

			iw.writeln("(or");
			iw.indentInc();

			for (CATransition t : col) {
				t.labAsRel().toSBYicesAsConj(iw);
			}

			iw.indentDec();
			iw.writeln(")");

		}
	}

	public boolean isFASTCompatible(int v_cnt) {
		return labAsRel().isFASTCompatible(v_cnt);
	}
	public boolean isARMCCompatible() {
		return labAsRel().isARMCCompatible();
	}
	
	public static Collection<CATransition> toCol(CATransition[] ts) {
		
		Collection<CATransition> col = new LinkedList<CATransition>();
		
		for (CATransition t : ts)
			col.add(t);
		
		return col;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////

	
	public static void compose(Collection<CATransition> first, Collection<CATransition> second, Collection<CATransition> up) {
		for (CATransition t1 : first) {
			for (CATransition t2 : second) {
				for (CATransition n : t1.compose(t2)) {
					up.add(n);
				}
			}
		}
	}
	
	public static Collection<CATransition> compose_asCol(CATransition t1, CATransition t2) {
		return CATransition.toCol(CATransition.compose(t1, t2));
	}
	public static CATransition[] compose(CATransition aT1, CATransition aT2) {
		if (aT1.to() != aT2.from())
			throw new RuntimeException("Attempt to compose unconnected transitions.");

		// hack for termination analysis 
		if (aT1.TERM_FLAG && aT2.TERM_FLAG) {
			return new CATransition[0];
		}
		
		
		String name = null;
		if (historyInNames)
			name = aT1.name() + "." + aT2.name();
		
		int weight = aT1.weight() + aT2.weight();
		
		CAState r_label = aT1.ca.reduceLabel();
		
		CompositeRel[] comp = aT1.labAsRel().compose(aT2.labAsRel());
		CATransition[] ret = new CATransition[comp.length];
		for (int i=0; i<comp.length; i++) {

			CompositeRel r = comp[i];
			CATransition t;
			if (comp.length == 1)
				t = new CATransition(aT1.from(), aT2.to(), r, weight, name, aT1.ca);
			else
				t = new CATransition(aT1.from(), aT2.to(), r, weight, name + (i + 1), aT1.ca);
			
			t.r_info = ReduceInfo.compose(t, aT1.r_info, aT2.r_info, r_label);
			
			ret[i] = t;
		}

		return ret;
	}
	
	public List<CATransition> project(Collection<Variable> elim) {
		List<CATransition> ret = new LinkedList<CATransition>();
		
		Collection<Relation> col = this.rel().toModuloRel().elimVars(elim);
		for (Relation r : col) {
			
			CompositeRel rel = new CompositeRel(r);
			CATransition t = new CATransition(this.from, this.to, rel, this.ca);
			t.r_info = ReduceInfo.project(t, this.r_info);
			
			ret.add(t);
		}
		
		return ret;
	}
	
	public List<CATransition> summary(Collection<Variable> elim) {
		List<CATransition> ret = new LinkedList<CATransition>();
		
		Collection<Relation> col = this.rel().toModuloRel().elimVarsDontMinimize(elim);
		for (Relation r : col) {
			
			// create modulo relation (no partitions, no minimizations)
			CompositeRel rel = CompositeRel.createNoMinNoPart(r);
			CATransition t = new CATransition(this.from, this.to, rel, this.ca);
			t.r_info = ReduceInfo.summary(t, this.r_info);
			
			ret.add(t);
		}
		
		return ret;
	}
	public static CATransition subset(CAState aFrom, CAState aTo, Label aLabel, String aName, CA aCA, List<CATransition> aSubsetOf) {
		CATransition ret = new CATransition(aFrom, aTo, aLabel, 1, aName, aCA);
		List<ReduceInfo> aux = new LinkedList<ReduceInfo>();
		for (CATransition t : aSubsetOf) {
			aux.add(t.reduce_info());
		}
		ret.r_info = ReduceInfo.subset(ret, aux);
		return ret;
	}
	public static CATransition split(CAState aFrom, CAState aTo, Label aLabel, String aName, CA aCA, CATransition aSubsetOf) {
		CATransition ret = new CATransition(aFrom, aTo, aLabel, 1, aName, aCA);
		ret.r_info = ReduceInfo.subset(ret, aSubsetOf.reduce_info());
		return ret;
	}
	public static CATransition plugged_summary(CAState aFrom, CAState aTo, Label aLabel, String aName, CA aCA, CATransition aPlugging, CATransition aPlugged) {
		CATransition ret = new CATransition(aFrom, aTo, aLabel, 1, aName, aCA);
		ret.r_info = ReduceInfo.plugged_summary(ret, aPlugging.reduce_info(), aPlugged.reduce_info());
		return ret;
	}
	
	public CATransition zeroPower() {
		// identity on variables *used* in this transition
		CompositeRel id = CompositeRel.createIdentityRelationForSorted(this.rel().unpvars());
		// represent as "LEAF"
		CATransition ret = new CATransition(from, to, id, ca);
		//CATransition ret = CATransition.identity(from, to, id, ca);
		return ret;
	}
	
	public static CATransition identity(CAState aFrom, CAState aTo, Label aLabel, CA aCA) {
		CATransition ret = new CATransition(aFrom, aTo, aLabel, 1, "id", aCA);
		ret.r_info = ReduceInfo.identity(ret);
		return ret;
	}
	
	public static CATransition inline_rename(CATransition other, Rename aRen, VariablePool aVarPool/*, CAState aFrom, CAState aTo, String aName*/, CA aCA) {
		CATransition t = new CATransition(other, true, aRen, aVarPool);
		//t.name = aName;
		//t.from = aFrom;
		//t.to = aTo;
		t.r_info = ReduceInfo.inline_rename(t, other.r_info);
		return t;
	}

	public static CATransition inline_plug(CATransition other, CAState aFrom, CAState aTo, String aName, CA aCA, Variable[] aLocalOld) {
		CATransition t = new CATransition(other, aFrom, aTo, aName, aCA, aLocalOld);
		t.r_info = ReduceInfo.inline_plug(t, other.reduce_info());
		return t;
	}
	public static CATransition inline_call(CATransition calling, CAState aFrom, CAState aTo, Label aLabel, String aName, CA aCA) {
		CATransition t = new CATransition(aFrom, aTo, aLabel, aName, aCA);
		t.r_info = ReduceInfo.inline_call(t, calling.reduce_info());
		return t;
	}
	public static CATransition inline_return(CATransition calling, CAState aFrom, CAState aTo, Label aLabel, String aName, CA aCA) {
		CATransition t = new CATransition(aFrom, aTo, aLabel, aName, aCA);
		t.r_info = ReduceInfo.inline_return(t, calling.reduce_info());
		return t;
	}

	// not contradictory is not the same as satisfiable
	// not satisfiable is not the same as contradictory
	// (since Yices may say unknown)
	public boolean isContradictory() { 
		return labAsRel().contradictory();
	}
	
	public boolean contradictory() {
		return labAsRel().contradictory();
	}
	public String typeName() {
		return labAsRel().typeName();
	}
	
	public static String typeStat(Collection<CATransition> col) {
		int ident = 0, dbm = 0, oct = 0, lin = 0, mod = 0;
		
		for (CATransition t : col) {
			
			CompositeRel r = t.labAsRel();

			if (r.isIdentity()) {
				ident++;
			} else {
				switch (r.getType()) {
				case DBREL:
					dbm ++;
					break;
				case OCTAGON:
					oct ++;
					break;
				case LINEAR:
					lin ++;
					break;
				case MODULO:
					mod ++;
					break;
				}
			}
		}
		//return "Transition types: ident: "+ident+", dbm-"+dbm+", oct-"+oct+", lin-"+lin+", mod-"+mod;
		return "[i:"+ident+",d:"+dbm+",o:"+oct+",l:"+lin+",m:"+mod+"]";
	}
	
	public static boolean addIncomparable(Collection<CATransition> aCol, CATransition aT) {
		Iterator<CATransition> iter = aCol.iterator();
		while (iter.hasNext()) {
			CATransition t = iter.next();
			if (!t.to().equals(aT.to()))
				continue;

			if (t.rel().includes(aT.rel()).isTrue()) {
				return false;
			} else if (t.rel().isIncludedIn(aT.rel()).isTrue()) {
				iter.remove();
				continue;
			}
		}
		
		aCol.add(aT);
		return true;
	}
	
	public void update(ConstProps cps) {
		this.labAsRel().update(cps);
		//this.rel = CompositeRel.toMinType(this.rel);
	}
	
	
	public static List<CATransition> toCATrans(Collection<BaseArc> col) {
		List<CATransition> ret = new LinkedList<CATransition>();
		
		for (BaseArc a : col)
			ret.add((CATransition) a);
		
		return ret;
	}

	public void setClosureIndentInfo(CATransition aloop) {
		labAsRel().closure_disjunct(ClosureDisjunct.closure_identity());
		this.r_info = ReduceInfo.closure(this, aloop.r_info, ca.reduceLabel());
	}
	
	public static Set<CAState> states(Collection<CATransition> col) {
		Set<CAState> ret = new HashSet<CAState>();
		
		for (CATransition t : col) {
			ret.add(t.from);
			ret.add(t.to);
		}
		
		return ret;
	}
	
	public void addOutputConstraints(LinearConstr aCond, LinearRel aIfTrue, LinearRel aIfFalse) {
		// TODO
		throw new RuntimeException("internal error");
	}
	
	public static void shortcutNode(
			Collection<CATransition> loops,
			//Collection<CATransition> closures,
			Collection<CATransition> computed) {
		
		List<ReduceInfo> closures = new LinkedList<ReduceInfo>();
		for (CATransition t : loops) {
			closures.addAll(t.reduce_info().succ);
		}
		new ShortcutNode(trans2rinfo(loops), closures, trans2rinfo(computed));
	}
	public static void shortcutNode(CATransition loop, Collection<CATransition> computed) {
		List<ReduceInfo> tmp = new LinkedList<ReduceInfo>();
		tmp.add(loop.r_info);
		new ShortcutNode(tmp, trans2rinfo(computed), trans2rinfo(computed));
	}

	public static Collection<ReduceInfo> trans2rinfo(Collection<CATransition> arcs) {
		Collection<ReduceInfo> ret = new LinkedList<ReduceInfo>();
		for (CATransition a : arcs) {
			ret.add(a.r_info);
		}
		return ret;
	}
	public static Collection<ReduceInfo> arc2rinfo(Collection<BaseArc> arcs) {
		Collection<ReduceInfo> ret = new LinkedList<ReduceInfo>();
		for (BaseArc a : arcs) {
			ret.add(((CATransition) a).r_info);
		}
		return ret;
	}
	public static Collection<CATransition> arc2trans(Collection<BaseArc> arcs) {
		Collection<CATransition> ret = new LinkedList<CATransition>();
		for (BaseArc a : arcs) {
			ret.add((CATransition) a);
		}
		return ret;
	}

	public void addSubsumed(CATransition aT) {
		this.r_info.addSubsumed(aT.r_info);
	}

	// returns list of transitions removed from the col
	public static List<CATransition> checkInclusion(List<CATransition> col, CATransition aT, boolean addIfNeeded) {
		List<CATransition> ret = new LinkedList<CATransition>();
		// filter by inclusion checks
		if (Parameters.isOnParameter(Parameters.T_FULLINCL) || 
				(Parameters.isOnParameter(Parameters.T_OCTINCL) && aT.rel().isOctagon())) {
		
			Iterator<CATransition> iter = col.iterator();
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
					
					return ret;
					
				} else if (t.rel().isIncludedIn(aT.rel()).isTrue()) {
					ret.add(t);
					//this.removeTransition(t);
					iter.remove();
					
					aT.addSubsumed(t);
					
					// merge
//					aT = aT.mergeWithIncluded(t);
					
					continue;
				}
			}
		}
		
		if (addIfNeeded)
			col.add(aT);
		
		return ret;
	}
	
	
	public static Answer inclusionCheck(Collection<CATransition> tOld, CATransition tNew) {

		if (tOld.size() == 0)
			return Answer.createAnswer(false);
		
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw);

		iw.writeln("(reset)");

		// define
		Set<Variable> vars = new HashSet<Variable>();
		tNew.labAsRel().refVars(vars);
		for (CATransition t : tOld) {
			t.labAsRel().refVars(vars);
		}
		CR.yicesDefineVars(iw, vars);

		iw.writeln("(assert");
		iw.indentInc();
		
		iw.writeln("(and");
		iw.indentInc();
		
		LinearRel lrnew = tNew.labAsRel().toLinearRel();
		lrnew.toSBYicesAsConj(iw);
		
		iw.writeln("(not (or");
		iw.indentInc();
		
		for (CATransition t : tOld) {
			LinearRel lr = t.labAsRel().toLinearRel();
			lr.toSBYicesAsConj(iw);
		}
		
		iw.writeln("))"); // not or
		iw.indentDec();
		
		iw.writeln(")"); // and
		iw.indentDec();
		
		iw.indentDec();
		iw.writeln(")"); // assert

		iw.writeln("(check)");

		StringBuffer yc = new StringBuffer();
		YicesAnswer ya = CR.isSatisfiableYices(sw.getBuffer(), yc);

		return Answer.createFromYicesUnsat(ya);
	}

	// split to more accelerable transitions 
	public CATransition[] makeMoreAccelerable() {
		
		CompositeRel[] split = rel().makeMoreAccelerable();
		if (split == null) {
			return null;
		}
		
		CATransition[] ret = new CATransition[split.length];
		for (int i=0; i<split.length; i++) {
			ret[i] = split(from, to, split[i], null, ca, this);
		}
		return ret;
	}

}
