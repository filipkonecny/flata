package verimag.flata.presburger;

import java.util.*;

import org.sosy_lab.java_smt.api.BooleanFormula;

import verimag.flata.common.Answer;
import verimag.flata.common.FlataJavaSMT;
import verimag.flata.common.IndentedWriter;

public class ModuloConstrs {
	
	//private Set<ModuloConstr> modConstrs_inter = new HashSet<ModuloConstr>();
	private List<ModuloConstr> modConstrs_inter = new LinkedList<ModuloConstr>();
	
	public int size() { return modConstrs_inter.size(); }
	
	private boolean simpleContradiction = false;
	public boolean simpleContradiction() { return simpleContradiction; }
	
	public boolean equals(Object o) {
		if (!(o instanceof ModuloConstrs))
			return false;
		
		ModuloConstrs other = (ModuloConstrs)o;
		//if (this.simpleContradiction && other.simpleContradiction)
		//	return true;
		return (new HashSet<ModuloConstr>(modConstrs_inter)).equals((new HashSet<ModuloConstr>(other.modConstrs_inter)));
		//return this.modConstrs_inter.equals(other.modConstrs_inter);
	}
	public int hashCode() {
		return (new HashSet<ModuloConstr>(modConstrs_inter)).hashCode();
		//return modConstrs_inter.hashCode();
	}
	
	public ModuloConstrs() {
	}

	public ModuloConstrs copy() { 
		return new ModuloConstrs(this);
	}
	public ModuloConstrs(ModuloConstrs aModConstrs) {
		addConstraints(aModConstrs);
	}
	
	public ModuloConstrs(ModuloConstrs aOther, Rename aRenVals, VariablePool aVarPool) {
		for (ModuloConstr constraint : aOther.modConstrs_inter) {
			addConstraint(new ModuloConstr(constraint, aRenVals, aVarPool));
		}
	}
	
	public Collection<ModuloConstr> modConstraints() {
		return modConstrs_inter;
	}
	public List<Constr> constraints() {
		return new LinkedList<Constr>(modConstrs_inter);
	}
	
	public StringBuffer toSBFAST() {
		StringBuffer sb = new StringBuffer();
		int size = this.modConstrs_inter.size();
		int i = 0;
		for (ModuloConstr mc : this.modConstrs_inter) {
			i++;
			sb.append("(exists k . (k >= 0 && "+mc.modulus()+"*k = "+mc.constr().toStringNoROP()+"))");
			if (i != size)
				sb.append(" && ");
		}
		return sb;
	}
	
	public String toString() {
		return toString(false);
	}
	public String toString(boolean isFirst) {
		StringBuffer sb = new StringBuffer();
		int size = modConstrs_inter.size();
		int i=1;
		if (i==1 && isFirst)
			sb.append(",");
		for (ModuloConstr mc : modConstrs_inter) {
			sb.append(mc.toString());
			if (i!=size)
				sb.append(",");
		}
		return sb.toString();
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
		LinkedList<BooleanFormula> formulas = new LinkedList<>();
		
		if (this.simpleContradiction) {
			formulas.add(fjsmt.getBfm().makeBoolean(negate));
			return formulas;
		}

		for (ModuloConstr mc : modConstrs_inter) {
			formulas.addAll(mc.toJSMTListPart(fjsmt, negate, s_u, s_p));
		}
		return formulas;
	}

	// TODO: remove
	public void toSBYicesList(IndentedWriter iw, String s_u, String s_p) {
		toSBYicesList(iw,false,s_u,s_p);
	}
	public void toSBYicesList(IndentedWriter iw, boolean negate) {
		toSBYicesList(iw, negate, null, null);
	}
	public void toSBYicesList(IndentedWriter iw) {
		toSBYicesList(iw, false, null, null);
	}
	public void toSBYicesList(IndentedWriter iw, boolean negate, String s_u, String s_p) {
		
		if (this.simpleContradiction) {
			iw.writeln(negate? "true" : "false");
			return;
		}
		
		for (ModuloConstr mc : modConstrs_inter)
			mc.toSBYicesListPart(iw,negate,s_u,s_p);
	}

	public BooleanFormula toJSMTConstrAnd(FlataJavaSMT fjsmt) {
		return toJSMTAsConj(fjsmt, null, null);
	}

	// TODO: remove
	public void toSBYicesConstrsAnd(IndentedWriter iw) {
		toSBYicesAsConj(iw, null, null);
	}

	public BooleanFormula toJSMTAsConj(FlataJavaSMT fjsmt, String s_u, String s_p) {
		if (modConstrs_inter.size() == 0) {
			return fjsmt.getBfm().makeTrue(); // TODO: check if correct
		}

		return fjsmt.getBfm().and(toJSMTList(fjsmt, s_u, s_p));
	}

	// TODO: remove
	public void toSBYicesAsConj(IndentedWriter iw, String s_u, String s_p) {
		if (modConstrs_inter.size() == 0)
			return;
		
		iw.writeln("(and");
		iw.indentInc();
		
		toSBYicesList(iw, s_u, s_p);
		
		iw.indentDec();
		iw.writeln(")");
	}

	public void variables(Collection<Variable> aVars) {
		for (ModuloConstr mc : modConstrs_inter) {
			mc.variables(aVars);
		}
	}
	public Set<Variable> variables() {
		Set<Variable> ret = new HashSet<Variable>();
		variables(ret);
		return ret;
	}
	
	public void addConstraint(ModuloConstr aModConstr) {
		
		if (simpleContradiction)
			return;
		
		if (aModConstr.simpleContradiction()) {
			
			simpleContradiction = true;
		} else {
			
			aModConstr.normalize();

			if (aModConstr.modulus() == 1) // 1 divides everything
				return;
			
			if (aModConstr.constr().size() == 0) // 0 is divided by everything
				return;
		}
		
		
		//modConstrs_inter.add(new ModuloConstr(aModConstr));
		
		Iterator<ModuloConstr> iter = this.modConstrs_inter.iterator();
		
		boolean add = true;
		while (iter.hasNext()) {
			ModuloConstr old = iter.next();
			if (aModConstr.includesForSure(old)) {
				add = false;
				break;
			} else if (old.includesForSure(aModConstr)) {
				iter.remove();
				continue;
			}

		}
		if (add) {
			modConstrs_inter.add(new ModuloConstr(aModConstr));
		}
	}

	public void addAll(ModuloConstrs aModConstrs) {
		modConstrs_inter.addAll(aModConstrs.modConstrs_inter);
	}
	public void addConstraints(ModuloConstrs aModConstrs) {
		addConstraints(aModConstrs.modConstrs_inter);
	}
	public void addConstraints(Collection<ModuloConstr> aModConstrs) {
		for (ModuloConstr c : aModConstrs) {
			addConstraint(c);
		}
	}

	public ModuloConstrs substitute(Substitution s) {
		ModuloConstrs ret = new ModuloConstrs();
		
		for (ModuloConstr mc : this.modConstrs_inter) {
			ret.addConstraint(mc.substitute(s));
		}
		
		return ret;
	}
	public ModuloConstrs substitute(Variable aVar, LinearConstr eq) {
		ModuloConstrs ret = new ModuloConstrs();
		
		for (ModuloConstr mc : modConstrs_inter) {
			ret.addConstraint(mc.substitute(aVar, eq));
		}
		
		return ret;
	}
	public void substitute_insitu(Variable aVar, LinearConstr eq) {
		Iterator<ModuloConstr> iter = this.modConstrs_inter.iterator();
		List<ModuloConstr> aux = new LinkedList<ModuloConstr>();
		while (iter.hasNext()) {
//			ModuloConstr mc = iter.next();
//			mc.substitute_insitu(aVar, eq);
//			mc.normalize();
//			if (mc.isTautology()) {
//				iter.remove();
//			}
//			if (mc.simpleContradiction()) {
//				this.simpleContradiction = true;
//			}
			ModuloConstr mc = iter.next();
			iter.remove();
			mc.substitute_insitu(aVar, eq);
			mc.normalize();
			if (mc.isTautology()) {
				// do nothing
			} else if (mc.simpleContradiction()) {
				this.simpleContradiction = true;
			} else {
				aux.add(mc);
			}
		}
		if (!this.simpleContradiction) {
			for (ModuloConstr mc : aux) {
				this.addConstraint(mc);
			}
		}
	}
	
	
	public int lcmOfMods(Variable v) {
		int lcm = 1;
		for (ModuloConstr mc : this.modConstrs_inter) {
			lcm = LinearConstr.lcm(lcm, mc.modulusOrOne(v));
		}
		return lcm;
	}

	public int lcmForCoeffOf(Variable aVar, int aLCM) {
		int lcm = aLCM;
		for (ModuloConstr mc : modConstrs_inter) {
			lcm = mc.lcmForCoeffOf(aVar, lcm);
		}
		return lcm;
	}

	public ModuloConstrs normalizeCooper(Variable aVar, int aLCM) {
		ModuloConstrs ret = new ModuloConstrs();
		for (ModuloConstr mc : modConstrs_inter) {
			ret.addConstraint(mc.normalizeCooper(aVar, aLCM));
		}
		return ret;
	}
	public void normalizeCooper_insitu(Variable aVar, int aLCM) {
		for (ModuloConstr mc : modConstrs_inter) {
			mc.normalizeCooper_insitu(aVar, aLCM);
		}
	}

	
	public static ModuloConstrs createRenamed(ModuloConstrs aModConstrs,
			Variable.Type aVarType, HashSet<Variable> aDoublePrimedVars) {

		ModuloConstrs ret = new ModuloConstrs();
		
		for (ModuloConstr constraint : aModConstrs.modConstrs_inter) {
			ret.addConstraint(ModuloConstr.createRenamed(constraint, aVarType, aDoublePrimedVars));
		}

		return ret;
	}
	public static ModuloConstrs createRenamed_params(ModuloConstrs aModConstrs,
			Variable.Type aVarType, HashSet<Variable> aDoublePrimedVars,
			Collection<Variable> params) {

		ModuloConstrs ret = new ModuloConstrs();
		
		for (ModuloConstr constraint : aModConstrs.modConstrs_inter) {
			ret.addConstraint(ModuloConstr.createRenamed_params(constraint, aVarType, aDoublePrimedVars, params));
		}

		return ret;
	}
	
//	public List<LinearConstr> moduloTerms() {
//		List<LinearConstr> ret = new LinkedList<LinearConstr>();
//		for (ModuloConstr mc : this.modConstrs_inter) {
//			ret.add(mc.constr().copy());
//		}
//		return ret;
//	}
	ModuloConstrs extractModInfo(ModInfo aInfo, ModuloConstr aMc) {
		ModuloConstrs ret = new ModuloConstrs();
		int check = 0;
		for (ModuloConstr mc : this.modConstrs_inter) {
			if (!mc.copyWithoutConstTerm().equals(aMc)) {
				ret.addConstraint(mc);
			} else {
				check++;
				aInfo.modOffset(mc.constr().constTerm());
			}
		}
		if (check > 1) {
			//throw new RuntimeException("internal error");
		}
		return ret;
	}
	
	public boolean syntaxEquals(ModuloConstrs aOther) {
		if (this.modConstrs_inter.size() != aOther.modConstrs_inter.size()) {
			return false;
		}
		
		int s = this.modConstrs_inter.size();
		BitSet matched = new BitSet(s);
		lab1: for (int i=0; i<s; i++) {
			ModuloConstr c1 = this.modConstrs_inter.get(i);
			for (int j = matched.nextClearBit(0); j < s; j = matched.nextClearBit(j+1)) {
				ModuloConstr c2 = aOther.modConstrs_inter.get(j);
				if (c1.syntaxEquals(c2)) {
					matched.set(j);
					continue lab1;
				}
			}
			return false;
		}
		
		return true;
	}
	// simple inclusion checks
	public boolean includesForSure(ModuloConstrs m2) {
		if (size()==0 && m2.size()==0) {
			return true;
		}
		if (this.size() != 1 || m2.size() != 1) {
			//return false;
			
			// check syntactic equivalence
			return this.syntaxEquals(m2);
		}
		return this.modConstrs_inter.get(0).includesForSure(m2.modConstrs_inter.get(0));
	}
	
	public Answer relEqualsSimple(ModuloConstrs other) {
		if (this.includesForSure(other) && other.includesForSure(this)) {
			return Answer.TRUE;
		} else {
			return Answer.DONTKNOW;
		}
	}

	public boolean hasOneTermTotal() {
		if (this.modConstrs_inter.size() != 1) {
			return false;
		} else {
			return this.modConstrs_inter.get(0).variables().size() == 1;
		}
	}
}
