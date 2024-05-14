package verimag.flata.presburger;

import java.util.*;

import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

import verimag.flata.common.FlataJavaSMT;
import verimag.flata.common.IndentedWriter;


public class ModuloConstr implements Constr {

	public boolean isLinear() { return false; }
	public boolean isModulo() {return true; }
	public ModuloConstr copy() { return new ModuloConstr(this); }
	
	public static String param_name = "$k";
	
	private int modulus;
	private LinearConstr constr;
	
	public boolean simpleContradiction() {
		
//		int lcm = 1;
//		for (LinearTerm lt : constr.terms()) {
//			if (lt.variable() != null) {
//				lcm = LinearConstr.lcm(lcm, Math.abs(lt.coeff()));
//			}
//		}
		
		
		int gcd = constr.gcdOfVarCoefs();
		
		// (modulus divides gcd /\ zerocoef != 0) => contradiction
		
		LinearTerm ltzero = constr.get(null);
		int zero = (ltzero == null)? 0 : ltzero.coeff();
		int len = constr.size() - ((ltzero == null)? 0 : 1);
		
		if (len == 0 && zero % modulus != 0 ) {
			return true;
		}
		
		if (modulus != 1 && gcd % modulus == 0 && zero != 0) {
			return true;
		}
		return false;
		
//		LinearTerm ltzero = constr.get(null);
//		if (constr.size() == ((ltzero==null)? 0 : 1)) {
//			int zero = (ltzero == null)? 0 : ltzero.coeff();
//			if (zero % modulus != 0)
//				return true;
//		}
//		return false;
	}
	
	public int modulusOrOne(Variable v) {
		return modulus;
//		if (constr.containsKey(v))
//			return modulus;
//		else
//			return 1;
		
	}
	
	public boolean isTautology() {
		return modulus == 1 || constr.size() == 0; 
	}
	
	public int modulus() { return modulus; }
	public LinearConstr constr() { return constr; }
	
	public boolean equals(Object o) {
		if (!(o instanceof ModuloConstr))
			return false;
		
		ModuloConstr other = (ModuloConstr)o;
		return modulus == other.modulus && constr.equals(other.constr);
	}
	public int hashCode() {
		int h = modulus + constr.hashCode();
		return h;
	}
	
	public ModuloConstr(int aModulus, LinearConstr aConstr) {
		modulus = Math.abs(aModulus);
		constr = aConstr;
		normalize();
	}
	
	public ModuloConstr(ModuloConstr other) {
		modulus = other.modulus;
		constr = new LinearConstr(other.constr);
	}
	
	public ModuloConstr(ModuloConstr other, Rename aRenVals, VariablePool aVarPool) {
		modulus = other.modulus;
		constr = new LinearConstr(other.constr, aRenVals, aVarPool);
	}
	
	// make the constant range in <0,..,modulus-1>
	private void normalizeConstCoeff() {
		LinearTerm lt = constr.remove(null);
		if (lt != null) {
			int c = lt.coeff();
			// first make it positive
			if (c < 0) {
				c = c + ((Math.abs(c) / modulus) + 1) * modulus;
			}
			constr.addLinTerm(LinearTerm.constant(c % modulus));
			//int cc= c % modulus;
			//// java % is remainder after truncation, get constant into interval <0,modulus-1>
			//constr.addLinTerm(new LinearTerm(null, cc-c+((cc<0)?modulus:0)));
		}
	}
	
	public void normalize() {
		Variable minAlpha = null;
		for (Variable v : constr.variables()) {
			if (minAlpha == null) {
				minAlpha = v;
			} else {
				if (v.name.compareTo(minAlpha.name) < 0) {
					minAlpha = v;
				}
			}
		}
		if (minAlpha != null && constr.get(minAlpha).coeff() < 0) {
			constr.multiplyWith(-1);
		}
		
		int gcd = this.constr.getGCD();
		gcd = LinearConstr.gcd(gcd, modulus);
		
		constr.normalizeBy(gcd);
		modulus /= gcd;
		
		normalizeConstCoeff();
	}

	public List<Constr> not() {
		List<Constr> ret = new LinkedList<Constr>();
		
		for (int i=1; i<modulus; i++) {
			LinearConstr cc = new LinearConstr(this.constr);
			cc.addLinTerm(LinearTerm.constant(i));
			ret.add(new ModuloConstr(modulus,cc));
		}
		
		return ret;
	}
	
	public String toString() {
		return ""+modulus+"|"+LinearTerm.toSBtermList(constr.values());
	}

	// TODO: check if this change is what makes it work, to return list, instead of conjunction (AND)
	public LinkedList<BooleanFormula> toJSMTListPart(FlataJavaSMT fjsmt, boolean negate, String s_u, String s_p) {
		IntegerFormula param = fjsmt.getIfm().makeVariable(param_name);
		IntegerFormula modulusNR = fjsmt.getIfm().makeNumber(modulus);
		IntegerFormula product =  fjsmt.getIfm().multiply(modulusNR, param);


		IntegerFormula constraint = constr.toJSMT(fjsmt, s_u, s_p);
		
		LinkedList<BooleanFormula> formulas = new LinkedList<>();

		// TODO: circumvent use of QFM with modularCongruence
		// ?? How do I do that ??
		// * Manually construct a formula equivalent to exists???
		if (!negate) {
			BooleanFormula formulaMod = fjsmt.getIfm().modularCongruence(constraint, fjsmt.getIfm().makeNumber(0), modulus);
			formulas.add(formulaMod);
			// System.out.println("MODULO1: \n" + formulaMod.toString()); // TODO: remove
		} else {
			for (int i = 1; i < modulus; i++) {
				BooleanFormula formulaMod = fjsmt.getIfm().modularCongruence(constraint, fjsmt.getIfm().makeNumber(i), modulus);
				formulas.add(formulaMod);
				// System.out.println("MODULO2: \n" + formulaMod.toString()); // TODO: remove
			}
		}

		// Old version using quantifiers
		// TODO: remove
		// if (!negate) {
		// 	BooleanFormula equality = fjsmt.getIfm().equal(constraint, product);
		// 	System.out.println("MOD-EQUALITY1 :\n" + equality.toString()); // TODO: remove
		// 	formulas.add(fjsmt.getQfm().exists(param, equality));
		// } else {
		// 	for (int i = 1; i < modulus; i++) {
		// 		IntegerFormula sum = fjsmt.getIfm().add(product, fjsmt.getIfm().makeNumber(i));
		// 		BooleanFormula equality = fjsmt.getIfm().equal(constraint, sum);
		// 		System.out.println("MOD-EQUALITY2 :\n" + equality.toString()); // TODO: remove
		// 		formulas.add(fjsmt.getQfm().exists(param, equality));
		// 	}
		// }

		// System.out.println("MODCONSTR: \n" + formulas.toString()); // TODO: remove

		return formulas;
	}
	
	// TODO: remove
	public void toSBYicesListPart(IndentedWriter iw, boolean negate, String s_u, String s_p) {
		
		if (!negate) {
			iw.writeln("(exists ("+param_name+"::int)");
			iw.indentInc();
			{
				iw.writeln("(="+" "+constr.toSBYices(s_u,s_p)+"(* "+modulus+" "+param_name+")"+")");
			}
			iw.indentDec();
			iw.writeln(")");
		} else {
			for (int i=1; i<modulus; i++) {
				iw.writeln("(exists ("+param_name+"::int)");
				iw.indentInc();
				{
					iw.writeln("(="+" "+constr.toSBYices(s_u,s_p)+"(+ (* "+modulus+" "+param_name+")"+" "+i+"))");
				}
				iw.indentDec();
				iw.writeln(")");
			}
		}
	}
	
	public Set<Variable> variables() {
		return constr.variables();
	}
	public void variables(Collection<Variable> aCol) {
		constr.variables(aCol);
	}

	public ModuloConstr substitute(Substitution s) {
		return new ModuloConstr(modulus, constr.substitute(s));
	}
	public ModuloConstr substitute(Variable aVar, LinearConstr eq) {
		return new ModuloConstr(modulus, constr.giveSubstitute(aVar, eq));
	}
	public void substitute_insitu(Variable aVar, LinearConstr eq) {
		constr.substitute_insitu(aVar, eq);
	}
	public int lcmForCoeffOf(Variable aVar, int aLCM) {
		return constr.lcmForCoeffOf(aVar, aLCM);
	}
	public ModuloConstr normalizeCooper(Variable aVar, int aLCM) {
		LinearTerm lt = constr.term(aVar);
		if (lt == null) {
			return new ModuloConstr(modulus, constr);
		} else {
			int factor = aLCM / Math.abs(lt.coeff());
			return new ModuloConstr(modulus*factor, constr.normalizeCooper(aVar, aLCM));
		}
	}
	public void normalizeCooper_insitu(Variable aVar, int aLCM) {
		LinearTerm lt = constr.term(aVar);
		if (lt != null) {
			int factor = aLCM / Math.abs(lt.coeff());
			modulus = modulus*factor;
			constr.normalizeCooper(aVar, aLCM);
		}
	}
	
	public static ModuloConstr createRenamed(ModuloConstr aConstraint,
			Variable.Type aVarType, HashSet<Variable> aDoublePrimedVars) {
		
		LinearConstr renamed = LinearRel.createRenamedConstraint(aConstraint.constr, aVarType, aDoublePrimedVars);
		return new ModuloConstr(aConstraint.modulus, renamed);
	}
	
	public static ModuloConstr createRenamed_params(ModuloConstr aConstraint,
			Variable.Type aVarType, HashSet<Variable> aDoublePrimedVars, Collection<Variable> params) {
		
		LinearConstr renamed = LinearRel.createRenamedConstraint_params(aConstraint.constr, aVarType, aDoublePrimedVars, params);
		return new ModuloConstr(aConstraint.modulus, renamed);
	}
	
	private void eraseConstTerm() {
		constr.removeTerm(null);
	}
	public ModuloConstr copyWithoutConstTerm() {
		ModuloConstr ret = this.copy();
		ret.eraseConstTerm();
		return ret;
	}
	
	public boolean includesForSure(ModuloConstr other) {
		// m2|t+c2 implies m1|t+c1   IFF   m2 % m1 = 0 /\ |c2-c1| % m1 = 0
		if (other.modulus % this.modulus != 0) {
			return false;
		}
		int c1 = this.constr.constTerm();
		int c2 = other.constr.constTerm();
		if (Math.abs(c2-c1) % this.modulus != 0) {
			return false;
		}
		
		return (this.constr.syntaxEquals(other.constr, true));
	}
	
	public boolean syntaxEquals(ModuloConstr aOther) {
		
		return (this.modulus == aOther.modulus) &&
				this.constr.syntaxEquals(aOther.constr, false);
	}
}
