package verimag.flata.presburger;

import java.io.StringWriter;
import java.util.*;

import nts.parser.*;

import org.gnu.glpk.*;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

import verimag.flata.common.*;

/**
 * a <tt>linear constraints</tt> class represents a conjunction objects, each of
 * whom is a <tt>linear constraint</tt>.
 * 
 * <p>
 * The class is implemented as a Set of java collection framework.
 * 
 * Therefore, deep copy methods are provided in LinConstraints class -
 * overridden copy constructor and addConstraint and addConstraints methods
 */
public class LinearRel extends Relation {
	
	//public static boolean TOSTRING_ORDERED = true;
	public static boolean INCLUSION_GLPK = false;
	
	public RelType getType() {
		return Relation.RelType.LINEAR;
	}
	
	private List<LinearConstr> linConstraints_inter = new LinkedList<LinearConstr>();

	public Collection<LinearConstr> constraints() { return linConstraints_inter; } 
	
	public List<LinearConstr> toCol() {
		return linConstraints_inter;
	}

	public int size() {
		return linConstraints_inter.size();
	}

	protected boolean simpleContradiction = false;

	public boolean simpleContradiction() {
		return simpleContradiction;
	}

	private void copyFields(LinearRel other) {
		// existentialParams = other.existentialParams;
		simpleContradiction = other.simpleContradiction;
	}

	public LinearRel guards() {
		LinearRel lcs = new LinearRel();
		for (LinearConstr lc : linConstraints_inter)
			if (!lc.isAction())
				lcs.add(lc);
		return lcs;
	}

	public LinearRel actions() {
		LinearRel lcs = new LinearRel();
		for (LinearConstr lc : linConstraints_inter)
			if (lc.isAction())
				lcs.add(lc);
		return lcs;
	}

	public void add(LinearConstr aLC) {
		
		if (CR.NO_POSTPARSING) {
			linConstraints_inter.add(aLC);
			return;
		}
		
		if (this.simpleContradiction)
			return;

		if (aLC.simpleContradiction()) {
			this.simpleContradiction = true;
			return;
		}

		LinearTerm zero = aLC.get(null);
		int size = aLC.size();
		if (size == 0) {
			return;
		} else if (size == 1 && zero != null) {
			if (zero.coeff() > 0)
				this.simpleContradiction = true; // TODO: contradiction
			else
				return; // tautology
		}

		aLC.normalizeByGCD();

		Iterator<LinearConstr> iter = linConstraints_inter.iterator();
		while (iter.hasNext()) {
			LinearConstr c = iter.next();
			LinearConstr.InclContr impl = aLC.implContr(c);
			if (impl.implies()) {
				iter.remove();
				//return;
			} else if (impl.isImplied()) {
				return;
			} else if (impl.isContradictory()) {
				linConstraints_inter.add(aLC);
				this.simpleContradiction = true;
				return;
			}
		}
		linConstraints_inter.add(aLC);
	}

	public void addAll(Collection<LinearConstr> aLCS) {
		if (simpleContradiction)
			return;
		for (LinearConstr lc : aLCS) {
			add(lc);
			// if (lc.simpleContradiction()) {
			// simpleContradiction = true;
			// return;
			// }
			// linConstraints_inter.add(lc);
		}
	}

	public void addAll(LinearRel aLCS) {
		addAll(aLCS.linConstraints_inter);
	}

	/**
	 * adds a copy of the specified linear constraint
	 * 
	 * @param aLinConstr
	 *            a linear constraint to be added
	 */
	public void addConstraint(LinearConstr aLinConstr) {
		LinearConstr aux = new LinearConstr(aLinConstr);

		add(aux);
	}

	public void addConstraints(LinearRel aLCS) {
		this.simpleContradiction = this.simpleContradiction || aLCS.simpleContradiction;
		addConstraints(aLCS.linConstraints_inter);
	}

	/**
	 * adds a copy of the specified linear constraints in the same manner as
	 * method {@link #addConstraint(LinearConstr)} does for a single term
	 * 
	 * @param aLinConstrs
	 *            a collection of linear constraints to be added
	 */
	public void addConstraints(Collection<LinearConstr> aLinConstrs) {
		for (LinearConstr e : aLinConstrs) {
			addConstraint(e);
		}
	}

	public LinearRel() {
	}

	public LinearRel(LinearConstr aLC) {
		this.addConstraint(aLC);
	}
	
	/**
	 * copy constructor; creates a deep copy of the specified
	 * <tt>LinConstraints</tt> object
	 */
	public LinearRel(LinearRel aLinConstrs) {
		copyFields(aLinConstrs);
		addConstraints(aLinConstrs.linConstraints_inter);
	}

	public LinearRel(LinearRel aLinConstrs, Rename aRenameInfo, VariablePool aVarPool) {
		copyFields(aLinConstrs);
		for (LinearConstr constraint : aLinConstrs.linConstraints_inter) {
			addConstraint(new LinearConstr(constraint, aRenameInfo, aVarPool));
		}
	}

//	public static LinearRel deepCopy(LinearRel aLinConstrs) {
//		LinearRel copy = new LinearRel();
//		copy.copyFields(aLinConstrs);
//		for (LinearConstr constraint : aLinConstrs.linConstraints_inter) {
//			copy.addConstraint(LinearConstr.deepCopy(constraint));
//		}
//		return copy;
//	}
	
	private StringBuffer toSB_nice(boolean aEqualities, Collection<LinearConstr> constrs,
			int primePrintType) {
		StringBuffer sb = new StringBuffer();

		LinearConstr constraint;
		for (Iterator<LinearConstr> iter = constrs.iterator(); iter.hasNext();) {
			constraint = iter.next();
			sb.append(constraint.toSB(aEqualities, true, primePrintType));
			if (iter.hasNext())
				sb.append(", ");
		}

		return sb;
	}

	public String toString() {
		// return new String(toSB());
		
//		if (TOSTRING_ORDERED)
//			return toSBOrder().toString();
//		else
			return toSBClever(Variable.ePRINT_prime).toString();
	}

	public StringBuffer toSB() {
		StringBuffer sb = new StringBuffer();
		LinearConstr constraint;
		for (Iterator<LinearConstr> iter = linConstraints_inter.iterator(); iter
				.hasNext();) {
			constraint = iter.next();
			sb.append(constraint.toSB());
			if (iter.hasNext())
				sb.append(", ");
		}
		return sb;
	}

	public static LinearRel removeImplActions(LinearRel aConstrs) {
		LinearRel nia = new LinearRel();
		LinearRel ia = new LinearRel();
		aConstrs.separateImplicitActions(nia, ia);

		return nia;
	}

	
	private static class MyBitSet implements Comparable<MyBitSet> {
		private BitSet bs;
		private LinearConstr lc;
		private Variable[] allvars;
		private Map<Variable,Integer> varinx;
		
		public String toString() { return lc.toString(); }
		
		public MyBitSet(Variable[] aAllvars, Map<Variable,Integer> aVarinx,  LinearConstr aLc) {
			lc = aLc;
			varinx = aVarinx;
			allvars = aAllvars;
			
			bs = new BitSet(varinx.size());
			
			for (Variable v : lc.keySet()) {
				if (v == null)
					continue;
				bs.set(varinx.get(v).intValue());
			}
		}

		public int compareTo(MyBitSet other) {			
			int i1=-1, i2=-1;
			while ( (i1 = this.bs.nextSetBit(i1+1)) >= 0 && (i2 = other.bs.nextSetBit(i2+1)) >= 0) {
				
				if (i1 < i2)
					return -1;
				else if (i1 > i2)
					return 1;
				else {
					int c1 = this.lc.get(allvars[i1]).coeff();
					int c2 = other.lc.get(allvars[i2]).coeff();
					if (c1 != c2)
						return c1-c2;
				}
			}
			
			if (i1 < 0 && i2 < 0)
				return 0;
			else if (i2 < 0)
				return 1;
			else
				return -1;
		}
		
		public StringBuffer toSB(boolean eq) {
			StringBuffer sb = new StringBuffer();
			
			for (int i = bs.nextSetBit(0); i>=0; i = bs.nextSetBit(i+1)) {
				LinearTerm lt = lc.get(allvars[i]);
				Variable v = lt.variable();
				int c = lt.coeff();
				
				if (c>=0)
					sb.append("+");
				else
					sb.append("-");
				
				int ac = Math.abs(c);
				if (ac != 1) {
					sb.append(ac);
				}
				
				sb.append(v);
			}
			
			sb.append(eq? "=" : "<=");
			LinearTerm lt = lc.get(null);
			sb.append((lt == null)? 0 : -lt.coeff());
			
			return sb;
		}

		public void normalizeEq() {
			Variable v = allvars[bs.nextSetBit(0)];
			int c = lc.get(v).coeff();
			if (c<0)
				lc.multiplyWith(-1);
		}
	}
	
	private MyBitSet[] lrOrder_base(Collection<LinearConstr> col, boolean eq) {
		//StringBuffer sb = new StringBuffer();
		
		MyBitSet[] mbss = new MyBitSet[col.size()];
		
		Variable[] allvars = GLPKInclusion.allVars;
		Map<Variable,Integer> varinx = GLPKInclusion.varInx;
		if (allvars == null) {
			allvars = this.refVarsUnpPSorted();
			varinx = new HashMap<Variable,Integer>();
			for (int i = 0; i < allvars.length; i ++) {
				varinx.put(allvars[i], new Integer(i));
			}
		}
		int i=0;
		for (LinearConstr lc : col) {
			mbss[i++] = new MyBitSet(allvars, varinx, lc);
		}
		
		if (eq)
			for (MyBitSet mbs : mbss)
				mbs.normalizeEq();
		
		Arrays.sort(mbss);
		
		//for (MyBitSet mbs : mbss) {
		//	sb.append(mbs.toSB(allvars, eq)+",\t");
		//}
		
		return mbss;
	}
	public static class LROrdered implements Comparable<LROrdered> {
		MyBitSet[] eq;
		MyBitSet[] ineq;
		
		public StringBuffer toSB() {
			StringBuffer sb = new StringBuffer();
			
			for (MyBitSet mbs : eq)
				sb.append(mbs.toSB(true)+",\t");
			for (MyBitSet mbs : ineq)
				sb.append(mbs.toSB(false)+",\t");
			
			return sb;
		}
		
		private static int compare_base(MyBitSet[] m1, MyBitSet[] m2) {
			int i1 = 0, i2 = 0;
			int l1 = m1.length, l2 = m2.length;
			while (i1 < l1 && i2 < l2) {
				int comp = m1[i1].compareTo(m2[i2]);
				if (comp != 0)
					return comp;
				
				i1++; i2++;
			}
			if (i1 >= l1 && i2 >= l2)
				return 0;
			else if (i2 >= l2)
				return 1;
			else
				return -1;
		}
		public int compareTo(LROrdered o) {
			int comp = compare_base(this.eq, o.eq);
			if (comp != 0)
				return comp;
			else
				return compare_base(this.ineq, o.ineq); 
		}
	}
	public LROrdered ordered() {
		LROrdered ret = new LROrdered();
		
		Collection<LinearConstr> ineqs = new LinkedList<LinearConstr>();
		Collection<LinearConstr> eqs = new LinkedList<LinearConstr>();
		separateEqualities(ineqs, eqs);
		
		ret.eq = lrOrder_base(eqs, true);
		ret.ineq = lrOrder_base(ineqs, false);

		return ret; 
	}
//	public StringBuffer toSBOrder() {
//		StringBuffer sb = new StringBuffer();
//		
//		Collection<LinearConstr> ineqs = new LinkedList<LinearConstr>();
//		Collection<LinearConstr> eqs = new LinkedList<LinearConstr>();
//		separateEqualities(ineqs, eqs);
//		
//		sb.append(toSBOrder_base(eqs, true));
//		sb.append(toSBOrder_base(ineqs, false));
//		
//		return sb;
//	}
	
	public StringBuffer toSBClever(int primePrintType) {
		StringBuffer sb = new StringBuffer();

		Collection<LinearConstr> ineqs = new LinkedList<LinearConstr>();
		Collection<LinearConstr> eqs = new LinkedList<LinearConstr>();
		separateEqualities(ineqs, eqs);
		
		sb.append(toSB_nice(true, eqs, primePrintType));
		if (ineqs.size() != 0
				&& eqs.size() != 0)
			sb.append(", ");
		sb.append(toSB_nice(false, ineqs, primePrintType));

		return sb;
	}
	
	public StringBuffer toSBarmc() {
		return LinearRel.toStringARMC_nice(false, this.constraints());
	}
	public static StringBuffer toStringARMC_nice(
			boolean aEqualities, Collection<LinearConstr> constrs) {
		StringBuffer sb = new StringBuffer();

		Iterator<LinearConstr> iter = constrs.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next().toSB(aEqualities, true, Variable.ePRINT_p_armcPref));
			if (iter.hasNext())
				sb.append(" , ");
		}
		return sb;
	}
	
	private StringBuffer toStringFAST_nice(boolean aIsGuards,
			boolean aEqualities, Collection<LinearConstr> constrs) {
		StringBuffer sb = new StringBuffer();

		Iterator<LinearConstr> iter = constrs.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next().toSB(aEqualities, true));
			if (iter.hasNext())
				if (aIsGuards)
					sb.append(" && ");
				else
					sb.append(" , ");
		}
		return sb;
	}

	private StringBuffer toStringTREX_nice(boolean aIsGuards,
			boolean aEqualities, Collection<LinearConstr> constrs) {
		StringBuffer sb = new StringBuffer();

		Iterator<LinearConstr> iter = constrs.iterator();
		while (iter.hasNext()) {
			sb.append(iter.next().toSB(aEqualities, true));
			if (iter.hasNext())
				if (aIsGuards)
					sb.append(" and ");
				else
					sb.append(" , ");
		}
		return sb;
	}
	
	public StringBuffer toSBFAST(boolean aIsGuards) {
		
		Collection<LinearConstr> ineqs = new LinkedList<LinearConstr>();
		Collection<LinearConstr> eqs = new LinkedList<LinearConstr>();
		separateEqualities(ineqs, eqs);
		if ((!aIsGuards) && (ineqs.size() != 0))
			throw new RuntimeException(
					"Attempting to print FAST-incompatible CA to FAST input format.");
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(toStringFAST_nice(aIsGuards, true, eqs));
		if (ineqs.size() != 0
				&& eqs.size() != 0) {
			if (aIsGuards)
				sb.append(" && ");
			else
				sb.append(", ");
		}
		sb.append(toStringFAST_nice(aIsGuards, false, ineqs));
		
		return sb;
	}
	
	public StringBuffer toSBTREX(boolean aIsGuards) {
		
		Collection<LinearConstr> ineqs = new LinkedList<LinearConstr>();
		Collection<LinearConstr> eqs = new LinkedList<LinearConstr>();
		separateEqualities(ineqs, eqs);
		if ((!aIsGuards) && (ineqs.size() != 0))
			throw new RuntimeException(
					"Attempting to print FAST-incompatible CA to FAST input format.");
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(toStringTREX_nice(aIsGuards, true, eqs));
		if (ineqs.size() != 0 && eqs.size() != 0) {
			if (aIsGuards)
				sb.append(" and ");
			else
				sb.append(", ");
		}
		sb.append(toStringTREX_nice(aIsGuards, false, ineqs));
		
		return sb;
	}

	public LinkedList<BooleanFormula> toJSMTList(FlataJavaSMT fjsmt) {
		return toJSMTList(fjsmt, false, null, null);
	}
	public LinkedList<BooleanFormula> toJSMTList(FlataJavaSMT fjsmt, String s_u, String s_p) {
		return toJSMTList(fjsmt, false, s_u, s_p);
	}
	public LinkedList<BooleanFormula> toJSMTList(FlataJavaSMT fjsmt, boolean negate) {
		return toJSMTList(fjsmt, negate, null, null);
	}
	public LinkedList<BooleanFormula> toJSMTList(FlataJavaSMT fjsmt, boolean negate, String s_u, String s_p) {
		LinkedList<BooleanFormula> ret = new LinkedList<BooleanFormula>();
		
		if (this.simpleContradiction) {
			ret.add(fjsmt.getBfm().makeBoolean(false));
			return ret;
		}

		IntegerFormula zero = fjsmt.getIfm().makeNumber(0);
		for (LinearConstr c : linConstraints_inter) {
			if (negate) { // >
				ret.add(fjsmt.getIfm().greaterThan(c.toJSMT(fjsmt, s_u, s_p), zero));
			} else { // <=
				ret.add(fjsmt.getIfm().lessOrEquals(c.toJSMT(fjsmt, s_u, s_p), zero));
			}
		}

		return ret;
	}

	// TODO: remove
	public void toSBYicesList(IndentedWriter iw, String s_u, String s_p) {
		toSBYicesList(iw, false, s_u, s_p);
	}
	public void toSBYicesList(IndentedWriter iw) {
		toSBYicesList(iw, false, null, null);
	}
	public void toSBYicesList(IndentedWriter iw, boolean negate) {
		toSBYicesList(iw, negate, null, null);
	}
	// list constraints without any boolean operator
	public void toSBYicesList(IndentedWriter iw, boolean negate, String s_u, String s_p) {
		
		if (this.simpleContradiction) {
			iw.writeln("false");
			return;
		}
		
		String rel = (negate) ? ">" : "<=";

		for (LinearConstr c : linConstraints_inter) {
			iw.writeln("(" + rel + " " + c.toSBYices(s_u, s_p) + " 0)");
		}
	}

	public BooleanFormula toJSMTFull() {
		return toJSMTAsConj(CR.flataJavaSMT);
	}

	// TODO: remove
	public StringBuffer toSBYicesFull() {
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw);

		iw.writeln("(reset)");

		Set<Variable> vars = new HashSet<Variable>();
		for (LinearConstr c : linConstraints_inter)
			c.variables(vars);

		CR.yicesDefineVars(iw, vars);

		iw.writeln("(assert");
		iw.indentInc();
		{
			toSBYicesAsConj(iw);
		}

		iw.indentDec();
		iw.writeln(")");
		iw.writeln("(check)");

		return sw.getBuffer();
	}
	
	public BooleanFormula toJSMTAsConj(FlataJavaSMT fjsmt) {
		return toJSMTAsConj(fjsmt, null, null);
	}
	public BooleanFormula toJSMTAsConj(FlataJavaSMT fjsmt, String s_u, String s_p) {
		if (this.linConstraints_inter.size() == 0) {
			return fjsmt.getBfm().makeTrue(); // TODO: check if correct
		}
		return fjsmt.getBfm().and(this.toJSMTList(fjsmt, s_u, s_p));
	}


	// TODO: remove
	public void toSBYicesAsConj(IndentedWriter aIW) {
		toSBYicesAsConj(aIW, null, null);
	}
	public void toSBYicesAsConj(IndentedWriter iw, String s_u, String s_p) {
		if (this.linConstraints_inter.size() == 0)
			return;

		iw.writeln("(and");
		iw.indentInc();

		this.toSBYicesList(iw, s_u, s_p);

		iw.indentDec();
		iw.writeln(")");
	}
	
	
	
	public void refVars(Collection<Variable> aCol) {
		for (LinearConstr c : linConstraints_inter)
			c.variables(aCol);
	}

	public Set<Variable> refVarsAsUnp() {
		Set<Variable> vars = variables();
		return Variable.toUnp(vars);
	}
	public Variable[] refVarsUnpPSorted() {
		Set<Variable> unp = this.refVarsAsUnp();
		return Variable.refVarsUnpPSorted(unp);
	}

	public void refVarsAsUnp(Collection<Variable> aCol) {
		Set<Variable> vars = variables();
		Variable.toUnp(vars, aCol);
	}

	public Set<Variable> variables() {
		Set<Variable> vars = new HashSet<Variable>();
		for (LinearConstr c : linConstraints_inter)
			c.variables(vars);
		return vars;
	}

	// TODO: convert ???
	public void exportToYices(String aFilename) {

		try {
			java.io.FileWriter fstream = new java.io.FileWriter(aFilename);
			java.io.BufferedWriter out = new java.io.BufferedWriter(fstream);
			out.write(new String(toSBYicesFull()));
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			System.exit(-1);
		}
	}

	/**
	 * removes a term with the specified variable for every linear constraint
	 * contained in the specified <tt>LinConstraints</tt> object
	 */
	public static void removeTermsWithVar(Collection<LinearConstr> aLinConstrs,
			Variable aVar) {
		for (LinearConstr constraint : aLinConstrs) {
			aLinConstrs.toString();
			constraint.removeTerm(aVar);
			aLinConstrs.toString();
		}
	}

	/**
	 * creates a copy of the specified constraints and renames variables of a
	 * specified type. The methods adds double primed variables which were
	 * created to the specified set.
	 * 
	 * @param aConstraints
	 *            constraints from which a renamed copy will be made
	 * @param aVarType
	 *            a type of variables to rename - either unprimed or primed
	 *            (ePRIME0 or ePRIME1)
	 * @param aDoublePrimedVars
	 *            a set to which all double-primed variables which are created
	 *            in this method are added
	 * @return renamed constraints
	 */
	public static LinearRel createRenamed(LinearRel aConstraints,
			Variable.Type aVarType, HashSet<Variable> aDoublePrimedVars) {

		if (aVarType.isDPrimed())
			throw new RuntimeException(
					"Attempt to rename double-primed variables.");

		LinearRel res_constraints = new LinearRel();

		for (LinearConstr constraint : aConstraints.linConstraints_inter) {
			res_constraints.add(createRenamedConstraint(constraint, aVarType,
					aDoublePrimedVars));
		}

		return res_constraints;
	}
	public static LinearRel createRenamed_params(LinearRel aConstraints,
			Variable.Type aVarType, HashSet<Variable> aDoublePrimedVars, Collection<Variable> params) {

		if (aVarType.isDPrimed())
			throw new RuntimeException(
					"Attempt to rename double-primed variables.");

		LinearRel res_constraints = new LinearRel();

		for (LinearConstr constraint : aConstraints.linConstraints_inter) {
			res_constraints.add(createRenamedConstraint_params(constraint, aVarType,
					aDoublePrimedVars, params));
		}

		return res_constraints;
	}
	
	public static LinearConstr createRenamedConstraint(
			LinearConstr aConstraint, Variable.Type aVarType,
			HashSet<Variable> aDoublePrimedVars) {
		LinearConstr res_constraint = new LinearConstr();

		Variable var;
		for (LinearTerm term : aConstraint.terms()) {
			var = term.variable();

			if (var != null && var.type() == aVarType) {
				Variable res_var = var.getIntermediate();
				aDoublePrimedVars.add(res_var);

				res_constraint
						.addLinTerm(new LinearTerm(res_var, term.coeff()));
			} else {
				res_constraint.addLinTerm(new LinearTerm(var, term.coeff()));
			}
		}

		return res_constraint;
	}
	public static LinearConstr createRenamedConstraint_params(
			LinearConstr aConstraint, Variable.Type aVarType,
			HashSet<Variable> aDoublePrimedVars, Collection<Variable> params) {
		LinearConstr res_constraint = new LinearConstr();

		Variable var;
		for (LinearTerm term : aConstraint.terms()) {
			var = term.variable();

			if (var != null && var.type() == aVarType && !(params.contains(var) || params.contains(var.getCounterpart()))) {
				Variable res_var = var.getIntermediate();
				aDoublePrimedVars.add(res_var);

				res_constraint
						.addLinTerm(new LinearTerm(res_var, term.coeff()));
			} else {
				res_constraint.addLinTerm(new LinearTerm(var, term.coeff()));
			}
		}

		return res_constraint;
	}

	public static LinearRel tryComposeFM_params(LinearRel aLCS1,
			LinearRel aLCS2, Collection<Variable> nonparams) {

		LinearRel renamedConstraints = new LinearRel();

		// int params1 = aLCS2.existParams();
		HashSet<Variable> doublePrimed = new HashSet<Variable>();

		renamedConstraints.addAll(createRenamed_params(aLCS1, Variable.Type.ePRIME1,
				doublePrimed, nonparams));
		// renamedConstraints.addAll(createRenamedConstraints(aLCS2,
		// Variable.ePRIME0, doublePrimed, params1));
		renamedConstraints.addAll(createRenamed_params(aLCS2, Variable.Type.ePRIME0,
				doublePrimed, nonparams));

		// now, tmp contains deep copies of linear constraints (composed of
		// deep-copied linear terms)

		// FM elimination for all double primed variables
		LinearRel fmInput = renamedConstraints;
		for (Variable var : doublePrimed) {
			// LinearRel fmResult = new LinearRel();
			if (!LinearRel.FM_elimination(fmInput, var))
				return null;
			// fmInput = fmResult;
		}

		return fmInput;
	}

	
	public static LinearRel tryComposeFM(LinearRel aLCS1,
			LinearRel aLCS2) {

		LinearRel renamedConstraints = new LinearRel();

		// int params1 = aLCS2.existParams();
		HashSet<Variable> doublePrimed = new HashSet<Variable>();

		renamedConstraints.addAll(createRenamed(aLCS1, Variable.Type.ePRIME1,
				doublePrimed));
		// renamedConstraints.addAll(createRenamedConstraints(aLCS2,
		// Variable.ePRIME0, doublePrimed, params1));
		renamedConstraints.addAll(createRenamed(aLCS2, Variable.Type.ePRIME0,
				doublePrimed));

		// now, tmp contains deep copies of linear constraints (composed of
		// deep-copied linear terms)

		// FM elimination for all double primed variables
		LinearRel fmInput = renamedConstraints;
		for (Variable var : doublePrimed) {
			if (!LinearRel.FM_elimination(fmInput, var))
				return null;
		}

		return fmInput;
	}
	
	public static LinearRel eliminate_p_or_unp(LinearRel aLR, boolean unp) {
		LinearRel ret = aLR.copy();
		for (Variable var : aLR.variables()) {
			boolean isPrimed = var.isPrimed();
			if ((unp && isPrimed) || (!unp && !isPrimed)) {
				continue;
			}
			if (!LinearRel.FM_elimination(ret, var)) {
				return null;
			}
		}
		return ret;
	}
	
	public Relation[] domain() {
		LinearRel ret = eliminate_p_or_unp(this, false);
		if (ret != null)
			return new Relation[] {ret};
		else
			return this.toModuloRel().domain();
	}
	public Relation[] range() {
		LinearRel ret = eliminate_p_or_unp(this, true);
		if (ret != null)
			return new Relation[] {ret};
		else
			return this.toModuloRel().range();
	}

	public Relation[] existElim1(Variable aVar) {
		return this.toModuloRel().existElim1(aVar);
	}
	public Relation[] existElim2(Variable aVar) {
		return this.toModuloRel().existElim2(aVar);
	}
	
	public boolean existEliminateTry(Variable aVar) {
		// LinearRel ret = new LinearRel();
		if (LinearRel.FM_elimination(this, aVar))
			return true;
		else
			return false;
	}

	// TODO: passing of parameters
	// eliminate variable 'var' from 'constraints' and store result in '_result'
	public static boolean FM_elimination(LinearRel aLinConstrs, Variable aVar) {
		
		LinearConstr eq = aLinConstrs.containsEquality(aVar).eqWithCoef1();
		if (eq != null) {

			aLinConstrs.substitute_insitu(aVar, eq);
			return true;
			
		} else {
		
			Collection<LinearConstr> groupGreater0 = new LinkedList<LinearConstr>();
			Collection<LinearConstr> groupLess0 = new LinkedList<LinearConstr>();
	
			if (LinearRel.checkCoefficients(aLinConstrs, aVar, groupGreater0, groupLess0)) {
				// elimination can be performed
	
				// remove double primed
				removeTermsWithVar(groupGreater0, aVar);
				removeTermsWithVar(groupLess0, aVar);
	
				for (LinearConstr constraintLess : groupLess0) {
					for (LinearConstr constraintGreater : groupGreater0) {
	
						if (Thread.interrupted())
							throw new RuntimeException(" -- interrupt --");
	
						aLinConstrs.addConstraint(LinearConstr.FM_elimination(constraintLess, constraintGreater));
					}
				}
	
				return true;
			}
	
			return false;
		
		}
	}

	// check coefficients of all terms with specified variable - it has to be
	// equal to 1 or -1
	// in the meantime, split constraints into three sets
	private static boolean checkCoefficients(LinearRel aLinConstrs,
			Variable aVar, Collection<LinearConstr> aGroupGreater0,
			Collection<LinearConstr> aGroupLess0
	) {

		Iterator<LinearConstr> iter = aLinConstrs.linConstraints_inter.iterator();

		while (iter.hasNext()) {
			LinearConstr constraint = iter.next();
			LinearTerm term = constraint.term(aVar);
			
			if (term != null) {

				iter.remove();
				int coeff = term.coeff();

				if (coeff == 0)
					throw new RuntimeException();
				if (java.lang.Math.abs(coeff) != 1)
					return false;

				if (coeff > 0)
					aGroupGreater0.add(constraint);
				else
					aGroupLess0.add(constraint);
			}
		}
		return true;
	}

	public static void splitLeqGeq(LinearRel aLinConstrs, Variable aVar,
			LinearRel aUpperBound, LinearRel aLowerBounds, LinearRel aUnrelated) {
		for (LinearConstr constraint : aLinConstrs.linConstraints_inter) {
			LinearTerm term = constraint.term(aVar);
			if (term == null) {
				aUnrelated.add(constraint);
			} else {
				int coeff = term.coeff();

				if (coeff == 0) {
					throw new RuntimeException();
				}

				if (coeff > 0)
					aUpperBound.add(constraint);
				else
					aLowerBounds.add(constraint);
			}
		}
	}

	public void removeWeakerConstraints(LinearConstr aRef) {

		LinearConstr ref_core = new LinearConstr(aRef);
		ref_core.removeTerm(null);

		LinearTerm term_b1 = aRef.get(null);
		int b1 = (term_b1 == null) ? 0 : term_b1.coeff();

		Set<Variable> vars = aRef.variables();

		int vars_s = vars.size();

		// removing weaker constraints
		if (vars_s > 1) {
			Iterator<LinearConstr> iter = linConstraints_inter.iterator();
			while (iter.hasNext()) {
				LinearConstr c = iter.next();
				LinearConstr c_core = new LinearConstr(c);
				LinearTerm c_constTerm = c_core.get(null);
				int c_coef = (c_constTerm == null) ? 0 : c_constTerm.coeff();
				c_core.removeTerm(null);
				if (c_core.equals(ref_core)) {
					if (c_coef <= b1)
						iter.remove();
				}
			}
		} else if (vars_s == 1) {
			Variable v_ref = vars.iterator().next();
			LinearTerm term_a1 = aRef.get(v_ref);
			int a1 = term_a1.coeff();

			Iterator<LinearConstr> iter = linConstraints_inter.iterator();
			while (iter.hasNext()) {
				LinearConstr c = iter.next();
				LinearConstr c_core = new LinearConstr(c);
				LinearTerm term_b2 = c_core.get(null);
				int b2 = (term_b2 == null) ? 0 : term_b2.coeff();
				c_core.removeTerm(null);
				if (c_core.size() == 1) {
					LinearTerm term_a2 = c_core.values().iterator().next();
					if (term_a2.variable().equals(v_ref)) {
						int a2 = term_a2.coeff();
						if (a1 * a2 <= 0)
							throw new RuntimeException(
									"internal error: unexpected param coefficients: "
											+ a1 + "," + a2);
						if (a1 > 0) {
							if (a1 * b2 <= a2 * b1)
								iter.remove();
						} else {
							if (Math.abs(a1) * b2 <= Math.abs(a2) * b1)
								iter.remove();
						}
					}
				}
			}
		}
	}

	// /**
	// * performs following actions:
	// * <ul>
	// * <li>elimination of redundancies - redundancy occurs when a group of
	// constraints differs only in the constant term.
	// * Such a group is replaced by the most restrictive constraint in the
	// group.
	// * <li>finds contradictions - this set of linear constraints is
	// contradictory if
	// * <ul>
	// * <li>there is a linear constraint among them which contains only a
	// constant term which is contradictory
	// * <li>or, when there is a pair of constraints of the form
	// * <pre>
	// * <tt>terms > a</tt> and <tt>terms < b</tt>, where <tt>a > b</tt> and
	// <tt>terms</tt> is sum of non-constant terms
	// * </pre>
	// * </ul>
	// * </ul>
	// */
	// // each group of constraints which differs only in the constant term is
	// replaced by the most restrictive constraint in the group
	// private void
	// eliminateRedundanciesAndFindContradictions(LinConstraintsStatus status) {
	//		
	// // structure to gather redundancy information
	// MapWithListValues table = new MapWithListValues();
	//
	// // finding redundancies
	// Iterator<LinearConstr> iter = linConstraints_inter.iterator();
	// while (iter.hasNext()) {
	// LinearConstr constraint = iter.next();
	// LinearConstr tmp = new LinearConstr(constraint);
	// tmp.remove(null);
	// table._add(tmp, constraint);
	// }
	//		
	// // removal of redundancies
	// HashSet<LinearConstr> redundancies = table.getRedundantConstraints();
	//		
	// //logging
	// if (redundancies.size()!=0 && status.logType()!=LogType.NONE) {
	//			
	// }
	//		
	// for (LinearConstr redundancy : redundancies) {
	//
	// if (status.logType()!=LogType.NONE) {
	// status.nrOfConstRedundancies++;
	// status.constRedundancies.add(redundancy.toString());
	// }
	//			
	// linConstraints_inter.remove(redundancy);
	// }
	//		
	// // check trivial contradictions on constant terms
	// for (LinearConstr c : linConstraints_inter)
	// if (c.isContradiction()) {
	//				
	// if (status.logType()!=LogType.NONE) {
	// status.constContradictions.add(c.toString());
	// }
	// status.isContradiction = true;
	//				
	// if (status.logType()!=LogType.FULL)
	// return;
	// }
	//		
	// // finding less trivial contradictions of the form
	// // terms > a and terms < b, where a > b and terms is sum of non-constant
	// terms)
	// Set<LinearConstr> aSet = table.keySet(); // take the set of constraints
	// without constant terms
	// Set<LinearConstr> aSet2 = new HashSet<LinearConstr>();
	//		
	// for (LinearConstr c : aSet) {
	// LinearConstr tmp = new LinearConstr(c);
	// tmp.transformBetweenGEQandLEQ();
	// //LinConstraint.transformBetweenGEQandLEQ(tmp);
	//			
	// if (aSet2.contains(tmp))
	// continue;
	//			
	// if (table.containsKey(tmp)) {
	// int c1 = table.getMostRestrictiveConst(c);
	// int c2 = table.getMostRestrictiveConst(tmp);
	//
	// if (c2>-c1) {
	//					
	// if (status.logType()!=LogType.NONE) {
	// status.constContradictions.add("("+table.getMostRestrictive(c)+", "+table.getMostRestrictive(tmp)+")");
	// }
	// status.isContradiction = true;
	//					
	// aSet2.add(tmp);
	//					
	// if (status.logType()!=LogType.FULL)
	// return;
	// }
	// }
	// }
	//		
	// if (status.useYices == false)
	// return;
	//		
	// // yices contradictions
	// StringBuffer sb = this.toSBYicesFull();
	// CR.YicesAnswer yicesAnswer = CR.isSatisfiableYices(sb, new
	// StringBuffer());
	// status.yicesAnswer = yicesAnswer;
	// if (yicesAnswer == CR.YicesAnswer.eYicesUNSAT) {
	// status.isContradiction = true;
	// }
	//		
	// return;
	// }
	//	
	public enum LogType {
		NONE, // don't report anything
		PARTIAL, // do report, but when the first contradiction is found, don't
					// look for others
		FULL
		// do report, and look for all contradictions
	}

	/**
	 * for a given variable, the method creates an implicit constraint:
	 * <ul>
	 * <li> <tt>x-x'<=0</tt> if the specified variable is unprimed
	 * <li> <tt> 
	 * x'-x<=0</tt> if the specified variable is primed
	 * </ul>
	 */
	public static LinearConstr createImplicitConstraint(Variable aVar) {
		LinearConstr implConstraint = new LinearConstr();
		implConstraint.addLinTerm(new LinearTerm(aVar, 1));
		implConstraint.addLinTerm(new LinearTerm(aVar.getCounterpart(), -1));
		return implConstraint;
	}

	public void addImplicitActionsForUnused(Variable[] allUnprimed) {
		Set<Variable> vars = this.variables();

		for (Variable var : allUnprimed) {
			if (vars.contains(var))
				continue;

			LinearConstr implConstraintLEQ = LinearRel
					.createImplicitConstraint(var);
			LinearConstr implConstraintGEQ = new LinearConstr(implConstraintLEQ);

			implConstraintGEQ.transformBetweenGEQandLEQ();

			this.add(implConstraintLEQ);
			this.add(implConstraintGEQ);
		}
	}

	/**
	 * Adds implicit actions (<tt>x'=x</tt>) for those variables <tt>x</tt> from the
	 * specified set which are contained in no linear constraint of this object.
	 * 
	 * @param allUnprimed
	 *            a set of primed variables
	 */
	
	public void addImplicitAction(Variable v) {
		
		LinearConstr implConstraintLEQ = LinearRel.createImplicitConstraint(v);
		LinearConstr implConstraintGEQ = new LinearConstr(implConstraintLEQ);

		implConstraintGEQ.transformBetweenGEQandLEQ();

		this.add(implConstraintLEQ);
		this.add(implConstraintGEQ);
	}
	
	public void addImplicitActions(Collection<Variable> aRestriction) {
		for (Variable v : this.refVarsAsUnp()) {
			if (aRestriction.contains(v)) {
				addImplicitAction(v);
			}
		}
	}
	
	public void addImplicitActions(Variable[] allUnprimed) {
		Set<Variable> vars = this.variables();
		for (Variable var : allUnprimed) {
			if (vars.contains(var.getCounterpart()))
				continue;

			addImplicitAction(var);
		}
	}

	public boolean isFASTCompatible() {
		for (LinearConstr constr : linConstraints_inter) {
			Set<Variable> primedVars = constr.primedVariables();
			if (primedVars.size() > 1) {
				return false;
			}
			if (primedVars.size() == 1) {
				int coeff = Math.abs(constr.get(primedVars.iterator().next())
						.coeff());
				if (coeff != 1)
					return false;
			}
		}

		Collection<LinearConstr> ineqs = new LinkedList<LinearConstr>();
		Collection<LinearConstr> eqs = new LinkedList<LinearConstr>();
		separateEqualities(ineqs, eqs);

		for (LinearConstr lc : ineqs) {
			for (Variable v : lc.variables()) {
				if (v.isPrimed())
					return false;
			}
		}
		
		Set<Variable> assigned = new HashSet<Variable>();
		for (LinearConstr l : eqs) {
			for (Variable v : l.variables()) {
				if (v.isPrimed()) {
					if (assigned.contains(v))
						return false;
					else 
						assigned.add(v);
				}
			}
		}
		
		return assigned.size() == this.refVarsAsUnp().size();
//		if (ineqs.size() != 0)
//			return false;
//		return true;
	}
	public boolean isARMCCompatible() {
		return true;
	}

	@SuppressWarnings("unchecked")
	private void separateImplicitActions(LinearRel aNIA, LinearRel aIA) {
		// MapWithListValues map = new MapWithListValues();
		Class<HashSet<LinearConstr>> class2 = (Class<HashSet<LinearConstr>>) new HashSet<LinearConstr>()
				.getClass();
		verimag.flata.common.HMapWColVal<LinearConstr, LinearConstr, HashSet<LinearConstr>> map = new verimag.flata.common.HMapWColVal<LinearConstr, LinearConstr, HashSet<LinearConstr>>(
				class2);

		for (LinearConstr constr : linConstraints_inter) {

			LinearConstr tmp = new LinearConstr(constr);
			tmp.transformBetweenGEQandLEQ();
			// LinConstraint.transformBetweenGEQandLEQ(tmp);

			if (map.containsKey(tmp))
				map._add(tmp, constr);
			else
				map._add(constr, constr);
		}
		for (HashSet<LinearConstr> set : map.values()) {
			boolean nia;
			if (set.size() == 2) {
				LinearConstr tmp = set.iterator().next();

				LinearTerm tmpTerm = tmp.get(null);
				if (tmpTerm != null && tmpTerm.coeff() != 0) {
					nia = true;
				} else {

					Set<Variable> tmpSet = new HashSet<Variable>();
					tmp.variables(tmpSet);
					if (tmpSet.size() != 2) {
						nia = true;
					} else {
						Iterator<Variable> iter = tmpSet.iterator();
						Variable var1 = iter.next();
						Variable var2 = iter.next();
						if (var1.getUnprimedVar().equals(var2.getUnprimedVar())) {
							nia = false;
						} else {
							nia = true;
						}
					}
				}

			} else if (set.size() == 1) {
				nia = true;
			} else {
				throw new RuntimeException();
			}

			if (nia)
				aNIA.addAll(set);
			else
				aIA.addAll(set);
		}
	}

	@SuppressWarnings("unchecked")
	public void separateEqualities(Collection<LinearConstr> aIneqs, Collection<LinearConstr> aEqs) {
		// MapWithListValues map = new MapWithListValues();
		Class<HashSet<LinearConstr>> class2 = (Class<HashSet<LinearConstr>>) new HashSet<LinearConstr>()
				.getClass();
		verimag.flata.common.HMapWColVal<LinearConstr, LinearConstr, HashSet<LinearConstr>> map = new verimag.flata.common.HMapWColVal<LinearConstr, LinearConstr, HashSet<LinearConstr>>(
				class2);

		for (LinearConstr constr : linConstraints_inter) {

			LinearConstr tmp = new LinearConstr(constr);
			tmp.transformBetweenGEQandLEQ();
			// LinConstraint.transformBetweenGEQandLEQ(tmp);

			if (map.containsKey(tmp))
				map._add(tmp, constr);
			else
				map._add(constr, constr);
		}
		for (HashSet<LinearConstr> set : map.values()) {
			if (set.size() == 2) {
				aEqs.add(set.iterator().next());
			} else if (set.size() == 1) {
				aIneqs.add(set.iterator().next());
			} else {
				throw new RuntimeException();
			}
		}
	}

	public static LinearRel getGEQtoLEQ(LinearRel aConstrs) {
		LinearRel constrsNew = new LinearRel();
		for (LinearConstr constr : aConstrs.linConstraints_inter) {
			LinearConstr constrNew = new LinearConstr(constr);
			constrNew.transformBetweenGEQandLEQ();
			// LinConstraint.transformBetweenGEQandLEQ(constrNew);
			constrsNew.add(constrNew);
		}
		return constrsNew;
	}

	// private Vector<Variable> lp_variables = null;
	private Variable[] lp_variables = null;
	private Map<Variable, Integer> lp_var_inx = null;

	/**
	 * When the abstraction routines are called, is is assumed that the
	 * LinConstraint object has not been modified since last call of getLP()
	 * method.
	 * 
	 * @return
	 */
	public glp_prob getLP() {
		// create problem
		glp_prob lp = GLPK.glp_create_prob();

		// GLPK.glp_set_prob_name(lp, "myProblem");

		// fill the variable vector
		lp_variables = this.variables().toArray(new Variable[0]);
		int var_count = lp_variables.length;
		// fill the back-link map
		lp_var_inx = new HashMap<Variable, Integer>(lp_variables.length);
		for (int i = 0; i < var_count; ++i) {
			lp_var_inx.put(lp_variables[i], new Integer(i));
		}

		// define columns
		GLPK.glp_add_cols(lp, var_count);
		for (int i = 0; i < var_count; ++i) {
			Variable v = lp_variables[i];
			GLPK.glp_set_col_name(lp, i + 1, v.name());
			GLPK.glp_set_col_kind(lp, i + 1, GLPKConstants.GLP_IV);
			GLPK.glp_set_col_bnds(lp, i + 1, GLPKConstants.GLP_FR, 0., 0.);
		}

		// define rows and create constraints
		setConstraintMatrix(lp);

		return lp;
	}

	private void setConstraintMatrix(glp_prob aLP) {
		int constr_count = linConstraints_inter.size();

		// define rows
		String slack_name_base = "slack";
		GLPK.glp_add_rows(aLP, constr_count);
		int i = 1;
		for (LinearConstr lc : linConstraints_inter) {
			// set row name
			GLPK.glp_set_row_name(aLP, i, slack_name_base + (i));
			// fill the row
			lc.setLPRow(aLP, i, lp_variables);

			i++;
		}
	}

	/**
	 * Three types of octagonal constraints: DEG_0, // x DEG_45, // x-y DEG_135;
	 * // x+y Intended use is for LP (linear programming) or MIP (mixed integer
	 * programming). There, type of objective function (MIN or MAX) is set.
	 * Hence we have 6 types of functions: x, -x x-y, y-x x+y, -x-y
	 */
	public enum OctType {
		DEG_0, // x
		DEG_45, // x-y
		DEG_135; // x+y

		public int coef1(ObjType objType) {
			int ret;
			switch (this) {
			case DEG_0:
				ret = 1;
				break;
			case DEG_45:
				ret = 1;
				break;
			case DEG_135:
				ret = 1;
				break;
			default:
				throw new RuntimeException(
						"unknown type of octagonal constraint");
			}

			return ret;
			// return adjust(ret, objType);
		}

		public int coef2(ObjType objType) {
			int ret;
			switch (this) {
			case DEG_0:
				ret = 0;
				break;
			case DEG_45:
				ret = -1;
				break;
			case DEG_135:
				ret = 1;
				break;
			default:
				throw new RuntimeException(
						"unknown type of octagonal constraint");
			}

			return ret;
			// return adjust(ret, objType);
		}

		/*
		 * private int adjust(int val, ObjType objType) { if
		 * (objType==ObjType.MIN) { return val*-1; } else { return val; } }
		 */

		private boolean doublesEqual(double d1, double d2) {
			double e = 0.000001;

			return (Math.abs(d1 - d2) <= e);
		}

		private int double2int(double d) {
			double f = Math.floor(d);
			double c = Math.ceil(d);

			if (doublesEqual(d, f))
				return (int) f;
			if (doublesEqual(d, c))
				return (int) c;
			throw new RuntimeException("MIP: imprecise integer result");
		}

		// assumption: objective function of the MIP was
		public LinearConstr getHullConstraint(glp_prob aLP, Variable v1, Variable v2, ObjType objType) {
			LinearConstr lc = new LinearConstr();

			// convert objective value to int and set it as a constant in the
			// octagon equation
			double obj_val = GLPK.glp_mip_obj_val(aLP);
			int obj_val_int = double2int(obj_val);

			lc.addLinTerm(new LinearTerm(null, -obj_val_int));

			switch (this) {
			case DEG_0:
				lc.addLinTerm(new LinearTerm(v1, 1));
				break;
			case DEG_45:
				lc.addLinTerm(new LinearTerm(v1, 1));
				lc.addLinTerm(new LinearTerm(v2, -1));
				break;
			case DEG_135:
				lc.addLinTerm(new LinearTerm(v1, 1));
				lc.addLinTerm(new LinearTerm(v2, 1));
				break;
			default:
				throw new RuntimeException(
						"unknown type of octagonal constraint");
			}

			// the above code creates a constraint for MAX. The other case must
			// be handled
			if (objType == ObjType.MIN)
				lc.transformBetweenGEQandLEQ();

			return lc;
		}

	}

	public enum ObjType {
		MAX, MIN
	}

	public void setObjectiveFunction(glp_prob aLP, Variable v1, Variable v2,
			OctType octType, ObjType objType) {
		String objective_name = "obj";

		int coef1 = octType.coef1(objType);
		int coef2 = octType.coef2(objType);

		GLPK.glp_set_obj_name(aLP, objective_name);
		GLPK.glp_set_obj_dir(aLP,
				(objType == ObjType.MAX) ? GLPKConstants.GLP_MAX
						: GLPKConstants.GLP_MIN);

		for (int i = 0; i < lp_variables.length; ++i) {
			Variable v = lp_variables[i];
			if (v.equals(v1))
				GLPK.glp_set_obj_coef(aLP, i + 1, (double) coef1);
			else if (v.equals(v2))
				GLPK.glp_set_obj_coef(aLP, i + 1, (double) coef2);
			else
				GLPK.glp_set_obj_coef(aLP, i + 1, 0.);
		}
	}

	public LinearConstr getOctagonFromHull(glp_prob aLP, Variable v1,
			Variable v2, OctType octType, ObjType objType) {

		this.setObjectiveFunction(aLP, v1, v2, octType, objType);

		//printLP(aLP);

		glp_iocp parm_mip = new glp_iocp();
		GLPK.glp_init_iocp(parm_mip);
		parm_mip.setPresolve(GLPKConstants.GLP_ON);
		int ret = GLPK.glp_intopt(aLP, parm_mip);
		int status = GLPK.glp_mip_status(aLP);

		// Retrieve solution
		if (ret == 0 && (status == GLPK.GLP_OPT || status == GLPK.GLP_FEAS)) {
			// write_mlp_solution(aLP);
			return octType.getHullConstraint(aLP, v1, v2, objType);
		} else {
//			if (ret == GLPKConstants.GLP_EBADB)
//				System.out.println("MIP returned GLP_EBADB(" + ret + ")");
//			else if (ret == GLPKConstants.GLP_ESING)
//				System.out.println("MIP returned GLP_ESING(" + ret + ")");
//			else if (ret == GLPKConstants.GLP_ECOND)
//				System.out.println("MIP returned GLP_ECOND(" + ret + ")");
//			else if (ret == GLPKConstants.GLP_EBOUND)
//				System.out.println("MIP returned GLP_EBOUND(" + ret + ")");
//			else if (ret == GLPKConstants.GLP_EFAIL)
//				System.out.println("MIP returned GLP_EFAIL(" + ret + ")");
//			else if (ret == GLPKConstants.GLP_EOBJLL)
//				System.out.println("MIP returned GLP_EOBJLL(" + ret + ")");
//			else if (ret == GLPKConstants.GLP_EOBJUL)
//				System.out.println("MIP returned GLP_EOBJUL(" + ret + ")");
//			else if (ret == GLPKConstants.GLP_EITLIM)
//				System.out.println("MIP returned GLP_EITLIM(" + ret + ")");
//			else if (ret == GLPKConstants.GLP_ETMLIM)
//				System.out.println("MIP returned GLP_ETMLIM(" + ret + ")");
//			else if (ret == GLPKConstants.GLP_ENOPFS)
//				System.out.println("MIP returned GLP_ENOPFS(" + ret + ")");
//			else if (ret == GLPKConstants.GLP_ENODFS)
//				System.out.println("MIP returned GLP_ENODFS(" + ret + ")");

			if (ret == GLPKConstants.GLP_ENOPFS
					|| ret == GLPKConstants.GLP_ENODFS) {
				return new LinearConstr();
			}

			throw new RuntimeException("A MIP could not be solved");
		}

	}

	public void printLP(glp_prob aLP) {
		int rows = GLPK.glp_get_num_rows(aLP);
		int cols = GLPK.glp_get_num_cols(aLP);

		System.out
				.println("--------------------------------------------------------------");
		// objective function
		System.out.println("OBJECTIVE FUNCTION:");
		System.out.println("dir=" + GLPK.glp_get_obj_dir(aLP));
		System.out.print(GLPK.glp_get_obj_name(aLP) + " = ");
		for (int c = 1; c <= cols; ++c)
			System.out.print(GLPK.glp_get_obj_coef(aLP, c) + "  ");
		System.out.println();

		// constraint matrix
		System.out.println("MATRIX:");
		System.out.print("  ");
		for (int c = 1; c <= cols; ++c)
			System.out.print(GLPK.glp_get_col_name(aLP, c) + "  ");
		System.out.println();

		for (int r = 1; r <= rows; ++r) {
			System.out.print(GLPK.glp_get_row_name(aLP, r) + "  ");

			SWIGTYPE_p_int ind = GLPK.new_intArray(cols + 1);
			SWIGTYPE_p_double val = GLPK.new_doubleArray(cols + 1);
			int cols_nz = GLPK.glp_get_mat_row(aLP, r, ind, val);
			for (int c = 1; c <= cols_nz; ++c) {
				int i = GLPK.intArray_getitem(ind, c);
				double v = GLPK.doubleArray_getitem(val, c);
				System.out.print("[" + i + "," + v + "]" + "  ");
			}
			System.out.println("range=(" + GLPK.glp_get_row_lb(aLP, r) + ","
					+ GLPK.glp_get_row_ub(aLP, r) + ")");
			System.out.println();
		}
		System.out.println("--------------------------------------------------------------");
	}

	
	private LinearRel abstractBase(boolean oct) {
		LinearRel ret = new LinearRel();

		glp_prob lp = this.getLP();

		// iterate over objective functions

		for (int i = 0; i < lp_variables.length; ++i) {
			Variable v = lp_variables[i];

			ret.add(getOctagonFromHull(lp, v, null, OctType.DEG_0, ObjType.MAX));
			ret.add(getOctagonFromHull(lp, v, null, OctType.DEG_0, ObjType.MIN));

			for (int j = i + 1; j < lp_variables.length; ++j) {
				Variable v2 = lp_variables[j];

				ret.add(getOctagonFromHull(lp, v, v2, OctType.DEG_45, ObjType.MAX));
				ret.add(getOctagonFromHull(lp, v, v2, OctType.DEG_45, ObjType.MIN));
				
				if (oct) {
					ret.add(getOctagonFromHull(lp, v, v2, OctType.DEG_135, ObjType.MAX));
					ret.add(getOctagonFromHull(lp, v, v2, OctType.DEG_135, ObjType.MIN));
				}
			}
		}
		
		return ret;
	}
	public Relation abstractDBRel() {
		LinearRel dbr = abstractBase(false);

		Relation ret = Relation.toMinType(new DBRel(dbr.dbConstrs(), refVarsUnpPSorted()));
		
		return ret;
	}
	public Relation abstractOct() {
		
		LinearRel oct = abstractBase(true);

		Relation ret = Relation.toMinType(new OctagonRel(oct.octConstrs(), refVarsUnpPSorted()));
		
		if (Parameters.isOnParameter(Parameters.STAT_ABSTR)) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("Lin  : "+this.toString()+"\n");
			sb.append("Abstr: "+oct.toLinearRel().ordered().toSB()+"\n");
			sb.append("Min  : "+ret.toLinearRel().ordered().toSB()+"\n");
			Relation.toMinType(new OctagonRel(oct.octConstrs(), refVarsUnpPSorted()));
			sb.append("\n");
			
			Parameters.log(Parameters.STAT_ABSTR, sb);
		}

		return ret;
		
		//return octagons.toOctagonRel();
	}
	
	public Relation abstractLin() {
		return this;
	}

	public static LinearRel createIdentity(List<Variable> vars) {
		LinearRel ret = new LinearRel();
		for (Variable v : vars) {
			LinearConstr lc = new LinearConstr();
			lc.addLinTerm(new LinearTerm(v, 1));
			lc.addLinTerm(new LinearTerm(v.getCounterpart(), -1));
			
			ret.addConstraint(lc);
			ret.addConstraint(LinearConstr.transformBetweenGEQandLEQ(lc));
		}
		return ret;
	}

	public DBCParam toDBCParam(Variable aParam) {
		DBCParam ret = new DBCParam();
		for (LinearConstr lc : linConstraints_inter) {
			// System.out.print(lc + "  ");
			DBC dbc = new DBC();
			if (lc.isDBC(dbc, DBC.BoundType.INT_ONE_PARAM, aParam)) {
				ret.col_dbc.add(dbc);
				// System.out.print(dbc);
			} else {
				ParamBound paramConstr = new ParamBound();
				if (lc.isNoVarConstr(paramConstr, aParam))
					ret.col_param.add(paramConstr);
				else
					throw new RuntimeException(
							"internal error: linear relation expected to be parametric");
			}
		}
		return ret;
	}

	// keeps only constraints of the form var = term
	// returns true iff there is a constraint of the form c*var = term
	public static boolean keepOnlyEqualityCandidates(Variable v, Collection<LinearConstr> col) {
		Iterator<LinearConstr> iter = col.iterator();
		boolean b = false;
		while (iter.hasNext()) {
			LinearTerm lt = iter.next().get(v);
			if (lt != null)
				b = true;
			if (lt == null || Math.abs(lt.coeff()) != 1)
				iter.remove();
		}
		return b;
	}
	
	// equality or quantifier elimination
	public static class EqForElim {
		private boolean someEq;
		private LinearConstr eq;
		public boolean someEq() { return someEq; }
		public LinearConstr eqWithCoef1() { return eq; }
		protected EqForElim(boolean b, LinearConstr c) {
			someEq = b;
			eq = c;
		}
	}
	// if constraint var = term is present, it returns term
	public EqForElim containsEquality(Variable aVar) {
		Collection<LinearConstr> ineqs = new LinkedList<LinearConstr>();
		Collection<LinearConstr> eqs = new LinkedList<LinearConstr>();
		
		separateEqualities(ineqs, eqs);
		
		boolean b = keepOnlyEqualityCandidates(aVar, eqs);
		
		LinearConstr best = null;
		for (LinearConstr c : eqs) {
			LinearTerm t = c.term(aVar);
			
			if (Math.abs(t.coeff())!=1)
				throw new RuntimeException();
			
			LinearConstr tmp = new LinearConstr(c);
			if (t.coeff() == 1)
				tmp.transformBetweenGEQandLEQ();
			tmp.removeTerm(aVar);
			
			if (best == null) {
				best = tmp;
			} else {
				int size_best = best.keySet().size();
				if (best.containsKey(null))
					size_best --;
				
				int size_new = tmp.keySet().size();
				if (tmp.containsKey(null))
					size_new --;
				
				if (size_best > size_new)
					best = tmp;
			}
		}

		return new EqForElim(b, best);
	}

	public void substitute_insitu(Variable aVar, LinearConstr eq) {
		
		List<LinearConstr> aux = this.linConstraints_inter;
		this.linConstraints_inter = new LinkedList<LinearConstr>();
		for (LinearConstr lc : aux) {
			lc.substitute_insitu(aVar, eq);
			if (lc.isTautology()) {
				continue;
			}
			this.add(lc);
		}
		
//		Iterator<LinearConstr> iter = this.linConstraints_inter.iterator();
//		while (iter.hasNext()) {
//			LinearConstr lc = iter.next();
//			lc.substitute_insitu(aVar, eq);
//			lc.normalizeByGCD();
//			if (lc.isTautology()) {
//				iter.remove();
//			} else if (lc.isContradiction()) {
//				this.simpleContradiction = true;
//			}
//		}
	}
	// TODO deprecated
	public LinearRel substitute(Variable aVar, LinearConstr eq) {

		LinearRel ret = new LinearRel();
		ret.copyFields(this);

		for (LinearConstr lc : linConstraints_inter) {
			LinearConstr aux = lc.giveSubstitute(aVar, eq);
			if (!aux.isTautology())
				ret.add(aux);
			else if (aux.isContradiction())
				ret.simpleContradiction = true;
		}

		return ret;
	}
	public LinearRel[] substitute(Substitution s) {
		LinearRel ret = new LinearRel();
		ret.copyFields(this);
		
		for (LinearConstr lc : linConstraints_inter) {
			ret.add(lc.substitute(s));
		}
		
		if (ret.satisfiable().isFalse()) {
			
			return new LinearRel[0];
			
		} else {
			
			return new LinearRel[] { ret };
		}
	}

	public int lcmForCoeffOf(Variable aVar, int aLCM) {
		int lcm = aLCM;
		for (LinearConstr lc : linConstraints_inter) {
			lcm = lc.lcmForCoeffOf(aVar, lcm);
		}
		return lcm;
	}

	public LinearRel normalizeCooper(Variable aVar, int aLCM) {
		LinearRel ret = new LinearRel();
		for (LinearConstr lc : linConstraints_inter) {
			ret.add(lc.normalizeCooper(aVar, aLCM));
		}
		return ret;
	}
	public void normalizeCooper_insitu(Variable aVar, int aLCM) {
		for (LinearConstr lc : linConstraints_inter) {
			lc.normalizeCooper_insitu(aVar, aLCM);
		}
	}

	private static String slack_pref = "#";

	// private static boolean isSlack(Variable v) {
	// return v.name().startsWith(slack_pref);
	// }
	private static int slackToConstrInx(Variable v) {
		return Integer.valueOf(v.name().substring(slack_pref.length()));
	}

	// private static void substitute(LinearConstr to,
	// Variable[] subsituents, LinearConstr[] substitutions, int[] coeffs) {
	// for (int i=0; i<subsituents.length; i++) {
	// if (substitutions[i] == null)
	// return;
	//			
	// Variable v = subsituents[i];
	//			
	// if (to.term(v) == null)
	// continue;
	//			
	// int lcm = to.lcmForCoeffOf(v, coeffs[i]);
	// int c = Math.abs(to.term(v).coeff());
	// to.multiplyWith(lcm / c);
	//			
	// LinearConstr eq = new LinearConstr(substitutions[i]);
	// eq.multiplyWith(lcm / coeffs[i]);
	//			
	// to.substitute(v, eq);
	// }
	// }
	private static void substitute(LinearConstr to, Variable subsituent,
			LinearConstr substitution, int coeff) {

		Variable v = subsituent;

		if (to.term(v) == null)
			return;

		int lcm = to.lcmForCoeffOf(v, coeff);
		int c = Math.abs(to.term(v).coeff());
		to.multiplyWith(lcm / c);

		LinearConstr eq = new LinearConstr(substitution);
		eq.multiplyWith(lcm / coeff);

		to.substituteSelf(v, eq);
	}

	// public void compact() {
	// LinearRel com = LinearRel.compact(this);
	// this.linConstraints_inter = com.linConstraints_inter;
	// }
	
	protected void compact() {
		LinearRel tmp = this.asCompact();
		this.copyFields(tmp);
		this.linConstraints_inter = tmp.linConstraints_inter;
	}
	public LinearRel asCompact() {
		return LinearRel.compact(this);
	}
	public DetUpdateAndGuards deterministicUpdate() {
		return this.toModuloRel().deterministicUpdate();
	}
	public FiniteVarIntervals findIntervals() {
		FiniteVarIntervals ret = new FiniteVarIntervals();
		
		Map<Variable,Integer> low = new HashMap<Variable,Integer>();
		Map<Variable,Integer> up = new HashMap<Variable,Integer>();
		
		for (LinearConstr lc : this.constraints()) {
			Set<Variable> aux = lc.variables();
			if (aux.size() == 1) {
				Variable v = aux.iterator().next();
				LinearTerm t = lc.term(v);
				int coef = t.coeff();
				if (Math.abs(coef) > 1) {
					throw new RuntimeException("internal error");
				}
				int c = lc.constTerm();
				if (coef > 0) {
					up.put(v,-c);
				} else {
					low.put(v,c);
				}
			}
		}
		
		for (Variable v : this.variables()) {
			Integer l = low.get(v);
			Integer u = up.get(v);
			if (l != null && u != null) {
				ret.add(new FiniteVarInterval(v,l,u));
			}
		}
		
		return ret;
	}
	
	private ConstProps inoutConst(boolean in) {
		Collection<LinearConstr> nonCons = new LinkedList<LinearConstr>();
		Collection<LinearConstr> cons = new LinkedList<LinearConstr>();
		
		separateConstantConstraints(cons, nonCons);
		
		Collection<ConstProp> col = new LinkedList<ConstProp>();
		
		for (LinearConstr lc : cons) {
			Variable v = null;
			int c = 0;
			int sign = 99;
			for (LinearTerm lt : lc.values()) {
				if (lt.variable() == null)
					c = lt.coeff();
				else {
					v = lt.variable();
					sign = lt.coeff();
				}
			}
			
			if (sign > 0)
				c *= -1;
			
			if ((v.isPrimed() && in) || (!v.isPrimed() && !in))
				continue;
			
			col.add(new ConstProp(v,c));
		}
		
		return new ConstProps(col);
	}
	public ConstProps inConst() {	
		return inoutConst(true);
	}
	public ConstProps outConst() {
		return inoutConst(false);
	}
	
	public void separateConstantConstraints(Collection<LinearConstr> cons, Collection<LinearConstr> nonCons) {
				
		this.separateEqualities(nonCons, cons);
		
		Iterator<LinearConstr> iter = cons.iterator();
		while (iter.hasNext()) {
			LinearConstr lc = iter.next();
			int s = (lc.term(null) == null)? 1 : 2;
			if (lc.size() != s) {
				iter.remove();
				
				nonCons.add(lc);
				nonCons.add(LinearConstr.transformBetweenGEQandLEQ(lc));
			}
		}
	}
	
	public static LinearRel substituteConstants(LinearRel aLR) {
		
		LinearRel ret = aLR;
		
		boolean done = false;
		
		do {
			Collection<LinearConstr> nonCons = new LinkedList<LinearConstr>();
			Collection<LinearConstr> cons = new LinkedList<LinearConstr>();
			
			ret.separateConstantConstraints(cons, nonCons);
			
			if (cons.size() == 0)
				return ret;
			
			Collection<Variable> intersection = LinearConstr.variables2(nonCons);
			intersection.retainAll(LinearConstr.variables2(cons));
			done = intersection.isEmpty();
			
			if (!done) {
				
				LinearRel tmp = new LinearRel();
				tmp.addAll(nonCons);
				
				for (LinearConstr lc : cons) {
					Variable v = lc.variables().iterator().next();
					if (lc.size() == 1) // substitute with zero
						tmp = tmp.substitute(v, new LinearConstr());
					else {
						LinearTerm lt = lc.term(v);
						lc = new LinearConstr(lc);
						lc.removeTerm(v);
						if (lt.coeff() > 0)
							lc.transformBetweenGEQandLEQ();
						
						tmp = tmp.substitute(v, lc);
					}
				}
				
				for (LinearConstr lc : cons) {
					tmp.add(lc);
					LinearConstr lc2 = LinearConstr.transformBetweenGEQandLEQ(lc);
					tmp.add(lc2);
				}
				
				ret = tmp;
			}
		} while (!done);
		
		return ret;
	}
	
	public static LinearRel compact(LinearRel aLR) {
		
//		return aLR;
		
		//if (Relation.DEBUG_COMPACTNESS)
		//	System.out.println("\nCompacting linear relation\n  " + aLR);

		
		LinearRel new_lr = LinearRel.substituteConstants(aLR); // TODO: iteratively
		
		if (aLR == new_lr)
			return new_lr;
		
		new_lr = LinearRel.substituteConstants(aLR); // TODO: iteratively
		
		if (!aLR.relEquals(new_lr).isTrue()) {
			aLR.relEquals(new_lr);
			//System.out.println("LC formula "+aLR.toString()+"\ncompacted to "+new_lr.toString());
			//new_lr = LinearRel.substituteConstants(aLR);
			//new_lr = LinearRel.substituteConstants(aLR);
new_lr = LinearRel.substituteConstants(aLR);
			System.err.println(aLR);
			System.err.println(new_lr);
			throw new RuntimeException("Incorrect compactness.");
		} else
			return new_lr;
		
		
		
//		LinearConstr[] new_lcs = new_lr.linConstraints_inter.toArray(new LinearConstr[0]);
//		LinearConstr[] new_lcs_cp = (new LinearRel(new_lr)).linConstraints_inter.toArray(new LinearConstr[0]);
//		boolean[] redundFlag = new boolean[new_lcs.length];
//		for (int i = 0; i < new_lcs.length; i++) {
//			// new_lcs_cp[i] = new LinearConstr(new_lcs[i]);
//			redundFlag[i] = false;
//		}
//
//		int size = new_lcs.length;
//
//		// prepare substitution structure
//		Variable[] substituents = new_lr.variables().toArray(new Variable[0]);
//		LinearConstr[] substitutions = new LinearConstr[substituents.length];
//		int[] coeffs = new int[substituents.length];
//
//		// create slack variables and add slack terms to linear constraints
//		Variable[] slack = new Variable[size];
//		for (int i = 0; i < size; i++) {
//			slack[i] = new Variable(slack_pref + i);
//			new_lcs[i].addLinTerm(new LinearTerm(slack[i], 1));
//		}
//
//		for (int i = 0; i < substituents.length; i++) {
//			Variable v = substituents[i];
//
//			// find best substituent for variable v
//			LinearConstr min = null;
//			int minInx = -1;
//			int minCoef = -1;
//			for (int j = 0; j < new_lcs.length; j++) {
//				LinearConstr lc = new_lcs[j];
//				if (lc == null)
//					continue;
//
//				LinearTerm lt = lc.get(v);
//				if (lt == null)
//					continue;
//
//				int c = Math.abs(lt.coeff());
//				if (minCoef < 0 || c < minCoef) {
//					min = lc;
//					minCoef = c;
//					minInx = j;
//				}
//			}
//
//			if (minInx < 0) // variable is already not present
//				continue;
//			// 
//			new_lcs[minInx] = null;
//
//			// set substitution coefficient;
//			coeffs[i] = minCoef;
//
//			// set substituent
//			if (min.term(v).coeff() > 0)
//				min.transformBetweenGEQandLEQ();
//			min.removeTerm(v);
//			substitutions[i] = min;
//
//			for (int j = 0; j < new_lcs.length; j++) {
//				if (new_lcs[j] == null)
//					continue;
//
//				substitute(new_lcs[j], substituents[i], substitutions[i],
//						coeffs[i]);
//			}
//		}
//
//		if (Relation.DEBUG_COMPACTNESS)
//			System.out.println("\n  slack system\n  "
//					+ Arrays.toString(new_lcs));
//
//		// find redundancies by analyzing slack constraints
//		for (int j = 0; j < new_lcs.length; j++) {
//			if (new_lcs[j] == null)
//				continue;
//
//			LinearConstr lc = new_lcs[j];
//			LinearTerm pos = lc.getPosTermInJustOne();
//			LinearTerm neg = lc.geNegTermInJustOne();
//
//			int inx = -1;
//			if (pos != null && neg != null) {
//				if (pos.coeff() == 1) {
//					inx = slackToConstrInx(pos.variable());
//				} else if (neg.coeff() == 1)
//					inx = slackToConstrInx(neg.variable());
//			} else if (pos != null && pos.coeff() == 1) {
//				inx = slackToConstrInx(pos.variable());
//			} else if (neg != null && neg.coeff() == -1) {
//				inx = slackToConstrInx(neg.variable());
//			}
//
//			if (inx >= 0) {
//				redundFlag[inx] = true;
//			}
//		}
//
//		LinearRel ret = new LinearRel();
//		for (int i = 0; i < new_lcs_cp.length; i++) {
//			if (!redundFlag[i])
//				ret.add(new_lcs_cp[i]);
//		}
//
//		if (Relation.DEBUG_COMPACTNESS)
//			System.out.println("\n  compacted to\n  " + ret);
//
//		return ret;
	}

	public Relation[] closureMaybeStar() {
		return null;
		
//		Relation[] cl = this.iterationInconsistent();
//
//		if (cl == null) {
//			return null;
//			//System.err.println("Closure cannot be computed:\n  "+this);
//			//throw Relation.notSupportedRE("(LinearRel.closure)");
//		} else
//			return cl;
	}
	public Relation[] closurePlus() {
		// try if R is not an octagon
		Relation aux = this.abstractOct();
		if (this.relEquals(aux).isTrue()) {
			return aux.closurePlus();
		}
		
		return null;
	}
	
	public ClosureDetail closure_detail() {
		return null;
	}
	public ClosureDetail closurePlus_detail() {
		return null;
	}

	public Relation[] compose(Relation otherRel) {
		if (!(otherRel instanceof LinearRel)) {

			return Relation.compose(this, otherRel);
		} else {
			LinearRel other = (LinearRel) otherRel;

			return this.toModuloRel().compose(other.toModuloRel());
		}
	}

	public Answer relEquals(Relation otherRel) {
		if (!(otherRel instanceof LinearRel)) {

			// supposing canonical representation of relations is used
			return Answer.FALSE;
		} else {
			LinearRel other = (LinearRel) otherRel;

			return this.includes(other).and(other.includes(this));
		}
	}

	private static int inclCounter = 0;
	private static int syntCounter = 0;
	public static void resetInclCounter() { inclCounter = 0; }
	public static int inclCounter() { return inclCounter; }
	
	// M(phi) \subseteq M(psi)
	// phi => psi
	// \neg\phi \vee \psi \eqiv True
	// \phi \wedge \neg\psi \eqiv False

	public Relation merge(Relation otherRel) {
		return syntaxMerge(otherRel);
	}
	
	// returns null if merge is impossible
	private Relation syntaxMerge(Relation otherRel) {
		
		if (!(otherRel instanceof LinearRel))
			return null;
		
		LinearRel other = (LinearRel) otherRel;
		
		if (this.size() != other.size())
			return null;
		
		LinearConstr cand1 = null;
		
		List<LinearConstr> l = new LinkedList<LinearConstr>(other.linConstraints_inter);
		
		for (LinearConstr lc : this.linConstraints_inter) {
			if (!l.remove(lc)) {
				if (cand1 == null)
					cand1 = lc;
				else
					return null;
			}
		}
		
		if (cand1 == null) // syntactically same relations
			return this;
		
		LinearConstr cand2 = l.get(0);
		
		LinearConstr tmp1 = new LinearConstr(cand1);
		LinearConstr tmp2 = new LinearConstr(cand2);
		
		LinearTerm lt1 = tmp1.remove(null);
		LinearTerm lt2 = tmp2.remove(null);
		
		tmp2.transformBetweenGEQandLEQ();
		
		if (!tmp1.equals(tmp2))
			return null;
		
		int c1 = lt1 == null? 0 : lt1.coeff();
		int c2 = lt2 == null? 0 : lt2.coeff();
		
		if (-c1 < c2)
			return null;
		
		if (Parameters.isOnParameter(Parameters.STAT_SMERGE)) {
			StringBuffer sb = new StringBuffer("###########################\n");
			sb.append("CAND1: "+cand1+"\n");
			sb.append("CAND2: "+cand2+"\n");
			sb.append("REL1: "+this+"\n");
			sb.append("REL2: "+other+"\n");
			Parameters.log(Parameters.STAT_SMERGE, sb);
		}
		
		LinearRel ret = this.copy();
		ret.linConstraints_inter.remove(cand1);
		return ret;
	}
	
	private boolean syntaxIncludes(LinearRel other) {
		
		if (this.size() > other.size())
			return false;
		
		for (LinearConstr lc : this.linConstraints_inter) {
			if (!other.linConstraints_inter.contains(lc))
				return false;
		}
		
		return true;
	}

	private Answer includesJSMT(LinearRel other) {
		FlataJavaSMT fjsmt = CR.flataJavaSMT;

		// Begin AND
		LinkedList<BooleanFormula> formulasAND = other.toJSMTList(fjsmt, false);

		// Begin and end OR
		BooleanFormula formulaOR = fjsmt.getBfm().or(this.toJSMTList(fjsmt, true));
		
		formulasAND.add(formulaOR);

		// End AND
		BooleanFormula formulaAND = fjsmt.getBfm().and(formulasAND);

		// unsat implies that relation is included
		return fjsmt.isSatisfiable(formulaAND, true);
	}

	// TODO: remove
	private Answer includes_yices(LinearRel other) {

		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw);

		iw.writeln("(reset)");

		// define
		Set<Variable> vars = this.variables();
		other.refVars(vars);
		CR.yicesDefineVars(iw, vars);

		iw.writeln("(assert");
		iw.indentInc();

		// other \subseteq this
		iw.writeln("(and");
		iw.indentInc();
		other.toSBYicesList(iw, false); // not negated

		iw.writeln("(or");
		iw.indentInc();
		this.toSBYicesList(iw, true); // negated

		iw.indentDec();
		iw.writeln(")"); // or
		iw.indentDec();
		iw.writeln(")"); // and

		iw.indentDec();
		iw.writeln(")"); // assert

		iw.writeln("(check)");

		StringBuffer yc = new StringBuffer();
		YicesAnswer ya = CR.isSatisfiableYices(sw.getBuffer(), yc);

		// unsat implies that relation is included
		return Answer.createFromYicesUnsat(ya);
		
	}
	
	private Answer includes_glpk(LinearRel other) {
		boolean b = GLPKInclusion.isIncluded(other, this);
		return Answer.createAnswer(b);
	}
	
	public Answer includes(Relation otherRel) {
		if (!(otherRel instanceof LinearRel)) {

			return Relation.includes(this, otherRel);
		} else {
			inclCounter ++;
			
			LinearRel other = (LinearRel) otherRel;
			
			if (this.syntaxIncludes(other)) {
				return Answer.TRUE;
			}
			
			if (INCLUSION_GLPK)
				return includes_glpk(other);
			else
				return includesJSMT(other);
				//return includes_yices(other); // TODO: remove
		}
	}

	public Relation[] intersect(Relation otherRel) {
		if (!(otherRel instanceof LinearRel)) {

			return Relation.intersect(this, otherRel);
			
		} else {
			LinearRel other = (LinearRel) otherRel;

			LinearRel ret = new LinearRel(this);
			ret.addConstraints(other);
			
			if (ret.satisfiable().isFalse()) {
				
				return new Relation[0];
				
			} else {
				
				return new Relation[] { Relation.toMinType(ret) };
			}
		}
	}

	public boolean tautology() {
		return this.size() == 0;
	}
	
	public Answer satisfiable() {

		if (this.simpleContradiction())
			return Answer.FALSE;

		if (this.linConstraints_inter.size() == 0)
			return Answer.TRUE;
		
		if (Parameters.isOnParameter(Parameters.T_OCTINCL))
			return Answer.DONTKNOW;
		
		if (LinearRel.INCLUSION_GLPK) {
			
			boolean b = GLPKInclusion.isSatisfiable(this);
			return Answer.createAnswer(b);
			
		} else {
			
			// StringBuffer yc = new StringBuffer(); // TODO: remove
			Answer a = CR.flataJavaSMT.isSatisfiable(this.toJSMTFull());
			// Answer a = Answer.createFromYicesSat(CR.isSatisfiableYices(this.toSBYicesFull(), yc)); // TODO: remove
			if (a.isFalse())
				this.simpleContradiction = true;
	
			if (a.isDontKnow())
				throw new RuntimeException();
			return a;
			
		}
	}

	public boolean isDBRel() {
		
		if (this.linConstraints_inter.size() == 0)
			return true;
		

		boolean isdbm = true;
		for (LinearConstr constr : linConstraints_inter) {
			if (!constr.isDBC()) {
				isdbm = false;
				break;
			}
		}
		if (isdbm)
			return true;
		
		if (this.simpleContradiction)
			return true;
		
		return false;
	}

	public boolean isOctagon() {
		for (LinearConstr constr : linConstraints_inter) {
			if (!constr.isOctagonal())
				return false;
		}
		return true;
	}

	public boolean isLinear() {
		return true;
	}

	public boolean isModulo() {
		return true;
	}

	public DBRel toDBRel() {
	
		if (this.simpleContradiction)
			return DBRel.inconsistentRel();
		
		//Set<Variable> tmp = this.refVarsAsUnp();
		//Variable[] vars_unp = tmp.toArray(new Variable[0]);
		DBC[] dbConstrs = dbConstrs();
		
		return new DBRel(dbConstrs, refVarsUnpPSorted());
	}

	private DBC[] dbConstrs() {
		DBC[] ret = new DBC[this.size()];
		
		int i = 0;
		for (LinearConstr constr : this.toCol()) {

			DBC dbc = new DBC();

			if (!constr.isDBC(dbc))
				DBRel.throwNotDBM();

			ret[i] = dbc;
			
			i++;
		}
		return ret;
	}
	private OctConstr[] octConstrs() {
		OctConstr[] octConstrs = new OctConstr[this.size()];

		int i = 0;
		for (LinearConstr constr : this.toCol()) {

			OctConstr oc = new OctConstr();

			if (!constr.isOctagonal(oc))
				OctagonRel.throwNotOct();

			octConstrs[i] = oc;

			i++;
		}
		return octConstrs;
	}
	public OctagonRel toOctagonRel() {

		//Set<Variable> tmp = this.refVarsAsUnp();
		//Variable[] vars_unp = tmp.toArray(new Variable[0]);

		return new OctagonRel(octConstrs(), refVarsUnpPSorted());
	}

	//private boolean compactFlag = false;
	
	public LinearRel toLinearRel() {
		return this.copy();
//		if (Relation.COMPACTNESS) { // && !compactFlag) {
//			//compactFlag = true;
//			LinearRel ret = LinearRel.compact(this);
//						
//			return ret;
//		} else
//			return this.copy();
	}

	public ModuloRel toModuloRel() {
		ModuloRel modRel = new ModuloRel();
		modRel.linConstrs(new LinearRel(this));
		return modRel;
	}

	public LinearRel copy() {
		return new LinearRel(this);
	}

	public Relation copy(Rename aRenVals, VariablePool aVarPool) {
		return new LinearRel(this, aRenVals, aVarPool);
	}

	public void substituteConstant(ConstProp cp) {
		
		LinearConstr lc = new LinearConstr();
		lc.addLinTerm(new LinearTerm(null, cp.c));
		
		this.substitute_insitu(cp.v, lc);
	}
	
	public void update(ConstProps cps) {
		for (ConstProp cp : cps.getAll()) {
			
			substituteConstant(cp);
			
			LinearConstr lc = new LinearConstr();
			lc.addLinTerm(new LinearTerm(cp.v,1));
			lc.addLinTerm(new LinearTerm(null,-cp.c));
			this.add(lc);
			
			lc = LinearConstr.transformBetweenGEQandLEQ(lc);
			this.add(lc);
		}
		
		this.compact();
	}
	
	public Collection<Variable> identVars() {
		// syntactically
		
		List<Variable> ret = new LinkedList<Variable>();
		
		Set<Variable> aux = new HashSet<Variable>();
		
		l0: for (LinearConstr lc : linConstraints_inter) {
			
			if (lc.size() > 2)
				continue l0;
			
			LinearTerm t1 = null;
			LinearTerm t2 = null;
			for (LinearTerm lt : lc.terms()) {
				if (lt.variable() == null)
					continue l0;
				
				if (t1 == null) {
					t1 = lt;
				} else if (t2 != null) {
					continue l0;
				} else {
					t2 = lt;
				}
			}
			if (t1 == null || t1 == null)
				continue l0;
			if (t1.variable().equals(t2.variable().getCounterpart()) && t1.coeff()*t2.coeff() == -1) {
				if (t1.coeff() > 0)
					aux.add(t1.variable());
				else 
					aux.add(t2.variable());
			}
		}
		
		for (Variable v : aux) {
			if (aux.contains(v.getCounterpart()) && !v.isPrimed())
				ret.add(v);
		}
		
		return ret;
	}
	
	
	public Relation[] minPartition() {
		
		// intervals
		Map<Variable, Integer> plusbnd = new HashMap<Variable, Integer>();
		Map<Variable, Integer> minusbnd = new HashMap<Variable, Integer>();
		DBC dbc = new DBC();
		for (LinearConstr lc : this.linConstraints_inter) {
			if (lc.isDBC(dbc)) {
				Variable plus = dbc.plus();
				Variable minus = dbc.minus();
				if (plus == null) {
					minusbnd.put(minus, dbc.label().toInt());
				} else if (minus == null) {
					plusbnd.put(plus, dbc.label().toInt());
				}
			}
		}
		
		Partition<Variable, LinearConstr> vps = new Partition<Variable, LinearConstr>();
		for (LinearConstr lc : this.linConstraints_inter) {
			
			// check with intervals (x<=c1 /\ y<=c2) -> (x+y<=c1+c2)
			int i = 0;
			int c = 0;
			int sum = 0;
			boolean notimplied = false;
			for (LinearTerm lt : lc.values()) {
				Variable v = lt.variable();
				if (v == null)
					c = lt.coeff()*-1;
				else {
					i++;
					int cc = lt.coeff();
					Integer bnd;
					if (cc > 0) {
						bnd = plusbnd.get(v);	
					} else {
						bnd = minusbnd.get(v);
					}
					if (bnd == null) {
						notimplied = true;
						break;
					}
					sum += bnd.intValue() * Math.abs(cc);
				}
			}
			
			if (!notimplied && i >= 2 && sum <= c)
				continue;
			
			vps.merge(lc.variablesUnpP(), lc);
		}
		
		Relation[] ret = new Relation[vps.size()];
		int i = 0;
		for (PartitionMember<Variable, LinearConstr> vp : vps.partitions) {
			LinearRel lr = new LinearRel();
			lr.addAll(vp.constrs);
			ret[i++] = Relation.toMinType(lr);
		}
		
		return ret;
	}
	
//	public void removeConstrsWithVar(Variable parameterK) {
//		Iterator<LinearConstr> iter = this.linConstraints_inter.iterator();
//		while (iter.hasNext()) {
//			if (iter.next().variables().contains(parameterK))
//				iter.remove();
//		}
//		
//	}
	
	public Relation weakestNontermCond() {
		throw new RuntimeException("internal error: method not supported");
	}
	
	public Expr toNTS() {
		
		if (linConstraints_inter.size() == 0) {
			return ASTWithoutToken.litBool(true);
		}
		
		Iterator<LinearConstr> i = linConstraints_inter.iterator();
		Expr aux = i.next().toNTS();
		while (i.hasNext()) {
			aux = ASTWithoutToken.exAnd(aux, i.next().toNTS());
		}
		return aux;
	}
	

	void tightenForModConstr(ModInfo aInfo, LinearConstr aLc, LinearConstr aLc_inverse) {
		
		extractBoundsForTerm(aInfo, aLc, aLc_inverse);
		
		IntegerInf up = aInfo.bndUp();
		IntegerInf low = aInfo.bndLow();
		if (up.isFinite()) {
			LinearConstr c = aLc.copy();
			c.addLinTerm(LinearTerm.constant(-up.toInt()));
			this.add(c);
		}
		if (low.isFinite()) {
			LinearConstr c = aLc_inverse.copy();
			c.addLinTerm(LinearTerm.constant(low.toInt()));
			this.add(c);
		}
	}
	private void extractBoundsForTerm(ModInfo aInfo, LinearConstr aLc, LinearConstr aLc_inverse) {
		Iterator<LinearConstr> i = this.linConstraints_inter.iterator();
		while (i.hasNext()) {
			LinearConstr lc = i.next();
			LinearConstr lc_base = lc.copyWithoutConstTerm();
			if (lc_base.equals(aLc)) {
				// t + c <= 0  <==>  t <= -c
				aInfo.bndUp(-lc.constTerm());
				i.remove();
			} else if (lc_base.equals(aLc_inverse)) {
				// -t + c <= 0  <==>  t >= c
				aInfo.bndLow(lc.constTerm());
				i.remove();
			}
		}
	}
	LinearRel extractModInfo(ModInfo aInfo, LinearConstr aLc, LinearConstr aLc_inverse) {
		LinearRel ret = new LinearRel();
		for (LinearConstr lc : this.linConstraints_inter) {
			LinearConstr lc_base = lc.copyWithoutConstTerm();
			if (lc_base.equals(aLc)) {
				// t + c <= 0  <==>  t <= -c
				aInfo.bndUp(-lc.constTerm());
			} else if (lc_base.equals(aLc_inverse)) {
				// -t + c <= 0  <==>  t >= c
				aInfo.bndLow(lc.constTerm());
			} else {
				ret.addConstraint(lc);
			}
		}
		return ret;
	}
	
	public OctagonRel octagonSubrel() {
		LinearRel aux = new LinearRel();
		for (LinearConstr c : this.linConstraints_inter) {
			if (c.isOctagonal()) {
				aux.addConstraint(c);
			}
		}
		return aux.toOctagonRel();
	}
	
	public void addEquality(Variable aV, LinearConstr aC) {
		// aC <= aV  IFF aC-aV<=0
		LinearConstr c1 = new LinearConstr(aC);
		c1.addLinTerm(LinearTerm.create(aV, -1));
		
		// aC>=aV IFF -aC+aV<=0
		LinearConstr c2 = c1.copy();
		c2.transformBetweenGEQandLEQ();
		
		this.addConstraint(c1);
		this.addConstraint(c2);
	}
	public void addSubstitutionEqualities(Substitution aS) {
		for (Variable v : aS.getKeys()) {
			this.addEquality(v, aS.get(v));
		}
	}
	public LinearRel and(LinearRel other) {
		LinearRel ret = new LinearRel(this);
		ret.addConstraints(other);
		return ret;
	}
	
	public static LinearRel createEq(LinearTerm t1, LinearTerm t2) {
		LinearConstr c1 = new LinearConstr();
		c1.addLinTerm(t1);
		LinearConstr c2 = new LinearConstr();
		c2.addLinTerm(t2);
		return createEq(c1,c2);
	}
	public static LinearRel createEq(LinearConstr c1, LinearConstr c2) {
		LinearConstr c = c1.minus(c2);
		LinearRel ret = new LinearRel();
		ret.addConstraint(c);
		ret.addConstraint(LinearConstr.transformBetweenGEQandLEQ(c));
		return ret;
	}
}
