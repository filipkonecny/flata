package verimag.flata.presburger;

import java.io.StringWriter;
import java.util.*;

import verimag.flata.common.Answer;
import verimag.flata.common.CR;
import verimag.flata.common.IndentedWriter;
import verimag.flata.common.YicesAnswer;

public class ModuloRel extends Relation {

	public RelType getType() {
		return Relation.RelType.MODULO;
	}
	
	private LinearRel linConstrs;
	private ModuloConstrs modConstrs;

	public int size() { return linConstrs.size() + modConstrs.size(); }
	
	public List<Constr> constraints() {
		List<Constr> ret = new LinkedList<Constr>();
		ret.addAll(linConstrs.constraints());
		ret.addAll(modConstrs.constraints());
		return ret;
	}
	
	public LinearRel linConstrs() {
		return linConstrs;
	}

	public void linConstrs(LinearRel aLinConstrs) {
		linConstrs = aLinConstrs;
	}

	public ModuloConstrs modConstrs() {
		return modConstrs;
	}

	public void linConstrs(ModuloConstrs aModConstrs) {
		modConstrs = aModConstrs;
	}

//	public boolean equals(Object o) {
//		if (!(o instanceof ModuloRel))
//			return false;
//
//		ModuloRel other = (ModuloRel) o;
//		boolean b1 = linConstrs.relEquals(other.linConstrs).isTrue();
//		boolean b2 = modConstrs.equals(other.modConstrs);
//		return b1 && b2;
//	}
//
//	public int hashCode() {
//		int h = linConstrs.hashCode() + modConstrs.hashCode();
//		return h;
//	}

	public ModuloRel() {
		linConstrs = new LinearRel();
		modConstrs = new ModuloConstrs();
	}

	public ModuloRel(ModuloRel other) {
		this(other.linConstrs, other.modConstrs);
	}

	public ModuloRel(LinearRel aLinConstrs) {
		this(aLinConstrs, new ModuloConstrs());
	}

	public ModuloRel(LinearRel aLinConstrs, ModuloConstrs aModConstrs) {
		this(aLinConstrs, aModConstrs, true);
	}

	public ModuloRel(LinearRel aLinConstrs, ModuloConstrs aModConstrs, boolean copy) {
		if (copy) {
			linConstrs = new LinearRel(aLinConstrs);
			modConstrs = new ModuloConstrs(aModConstrs);
		} else {
			linConstrs = aLinConstrs;
			modConstrs = aModConstrs;
		}
	}

	public ModuloRel(List<Constr> constrs) {
		this();
		for (Constr c : constrs) {
			addConstraint(c);
		}
	}
	
	public ModuloRel asCompact() {
		return new ModuloRel(this.linConstrs.asCompact(), this.modConstrs);
	}
	
	public ModuloRel(ModuloRel aOther, Rename aRenVals, VariablePool aVarPool) {
		linConstrs = new LinearRel(aOther.linConstrs, aRenVals, aVarPool);
		modConstrs = new ModuloConstrs(aOther.modConstrs, aRenVals, aVarPool);
	}

	public void addConstraint(Constr constr) {
		if (constr.isLinear()) {
			this.linConstrs.addConstraint((LinearConstr)constr);
		} else { 
			this.modConstrs.addConstraint((ModuloConstr)constr);
		}
		// aggressive simplification after each change
		// TODO: make it a bit less aggressive
		simplify();
	}
	public void addConstraints(LinearRel other) {
		this.linConstrs.addConstraints(other);
	}

	public void addConstraints(ModuloConstrs other) {
		this.modConstrs.addConstraints(other);
	}

	public void addAll(ModuloRel other) {
		this.linConstrs.addAll(other.linConstrs);
		this.modConstrs.addAll(other.modConstrs);
	}
	public void addConstraints(ModuloRel other) {
		this.addConstraints(other.linConstrs);
		this.addConstraints(other.modConstrs);
	}

	public String toString() {
		return "" + linConstrs.toSBClever(Variable.ePRINT_prime) 
		+ ((modConstrs.size() == 0)? "" : ", ") + modConstrs;
	}
	
	public void toSBYicesAsConj(IndentedWriter iw, String s_u, String s_p) {
		int lsize = linConstrs.size();
		int msize = modConstrs.size();

		if (lsize + msize == 0) {
			iw.writeln("true");
			return;
		}

		iw.writeln("(and");
		iw.indentInc();
		{
			if (lsize > 0)
				linConstrs.toSBYicesList(iw, s_u, s_p);
			if (msize > 0)
				modConstrs.toSBYicesList(iw, s_u, s_p);
		}
		iw.indentDec();
		iw.writeln(")");
	}
	
	public void toSBYicesAsConj(IndentedWriter aIW) {
		toSBYicesAsConj(aIW, null, null);
	}
	
	public void toSBYicesList(IndentedWriter iw, boolean negate) {
		this.linConstrs.toSBYicesList(iw, negate);
		this.modConstrs.toSBYicesList(iw, negate);
	}

	public ModuloRel substitute(Variable aVar, LinearConstr aEQ) {
		ModuloRel ret = new ModuloRel(linConstrs.substitute(aVar, aEQ), modConstrs.substitute(aVar, aEQ), false);
		ret.compact();
		return ret;
	}
	
	private void substitute_insitu(Variable aVar, LinearConstr aEQ) {
		linConstrs.substitute_insitu(aVar, aEQ);
		modConstrs.substitute_insitu(aVar, aEQ);
	}
	private void substitute_insitu(Substitution aS) {
		for (Variable v : aS.getKeys()) {
			this.substitute_insitu(v,aS.get(v));
		}
	}

	public ModuloRel[] substitute(Substitution s) {
		ModuloRel ret = new ModuloRel();
		
		LinearRel[] aux = this.linConstrs.substitute(s);
		
		if (aux.length == 0)
			return new ModuloRel[0];
		
		ret.linConstrs = aux[0];
		ret.modConstrs = this.modConstrs.substitute(s);
		
		if (ret.satisfiable().isFalse()) {
			return new ModuloRel[0];
		} else {
			return new ModuloRel[] { ret };
		}
	}
	
	public int lcmForCoeffOf(Variable aVar) {
		int lcm = 1;
		lcm = linConstrs.lcmForCoeffOf(aVar, lcm);
		lcm = modConstrs.lcmForCoeffOf(aVar, lcm);
		return lcm;
	}

	// multiplies constraints to achieve the same absolute value of the coefficient of the variable v, then replaces each monomial c*v with (c/|c|)*v 
	public ModuloRel normalizeCooper(Variable aVar, int aLCM) {
		return new ModuloRel(linConstrs.normalizeCooper(aVar, aLCM), modConstrs.normalizeCooper(aVar, aLCM), false);
	}
	// multiplies constraints to achieve the same absolute value of the coefficient of the variable v, then replaces each monomial c*v with (c/|c|)*v
	public void normalizeCooper_insitu(Variable aVar, int aLCM) {
		linConstrs.normalizeCooper_insitu(aVar, aLCM);
		modConstrs.normalizeCooper_insitu(aVar, aLCM);
	}

	// public LinConstraintsStatus process(boolean aUseYices) {
	// return linConstrs.process(aUseYices);
	// // TODO
	// }
	public void refVars(Collection<Variable> aCol) {
		linConstrs.refVars(aCol);
		modConstrs.variables(aCol);
	}

	public Set<Variable> refVarsAsUnp() {
		Set<Variable> vars = variables();
		return Variable.toUnp(vars);
	}
	public void refVarsAsUnp(Collection<Variable> aCol) {
		Set<Variable> vars = variables();
		Variable.toUnp(vars, aCol);
	}
	public Variable[] refVarsUnpPSorted() {
		Set<Variable> unp = new HashSet<Variable>();
		this.refVarsAsUnp(unp);
		return Variable.refVarsUnpPSorted(unp);
	}

	public Set<Variable> variables() {
		Set<Variable> vars = new HashSet<Variable>();
		refVars(vars);
		return vars;
	}

	public Set<Variable> allRefVarsAsUnp() {
		Set<Variable> ret = new HashSet<Variable>();
		{
			Set<Variable> vars = new HashSet<Variable>();
			refVars(vars);

			for (Variable var : vars) {
				if (var.isPrimed()) {
					ret.add(var.getUnprimedVar());
				} else {
					ret.add(var);
				}
			}
		}
		return ret;
	}
	
	private void existEliminate(Variable aVar, Collection<ModuloRel> aPresbConstrs) {

		Set<Variable> vars = this.variables();
		if (!vars.contains(aVar)) {
			aPresbConstrs.add(this);
			return;
		}
		
		LinearRel.EqForElim ee = linConstrs.containsEquality(aVar);
		LinearConstr eq = ee.eqWithCoef1();
		if (eq != null) {
			
			ModuloRel subst = this.substitute(aVar, eq);
			Relation.addIfNotContr_mod(aPresbConstrs, subst);
			
		} else {

			// normalize coefficients of aVar
			int lcm = this.lcmForCoeffOf(aVar);
			
			// do Fourier-Moetzkin if it can be done
			if (this.modConstrs.size() == 0 && lcm == 1) {
				boolean done = this.linConstrs.existEliminateTry(aVar);
				
				if (done) {
					aPresbConstrs.add(this.toModuloRel());
					return;
				} else {
					throw new RuntimeException("internal error: fourier moetzkin elim. not possible");
				}
			}

			ModuloRel norm;

			// add modulo constraint lcd | aVar
			if (lcm > 1) {
				norm = this.normalizeCooper(aVar, lcm);
				LinearConstr lc_new = new LinearConstr();
				lc_new.addLinTerm(new LinearTerm(aVar));
				norm.modConstrs.addConstraint(new ModuloConstr(lcm, lc_new));
			} else {
				norm = this;
			}
			
			// if there was an equality c*var = term, substitute in the normalized relation
			if (ee.someEq()) {
				
				LinearRel.EqForElim ee2 = norm.linConstrs.containsEquality(aVar);
				LinearConstr eq2 = ee2.eqWithCoef1();
				
				ModuloRel subst = norm.substitute(aVar, eq2);
				Relation.addIfNotContr_mod(aPresbConstrs, subst);
				
			} else {
			
				// split linear constraints
				LinearRel upperBounds = new LinearRel();
				LinearRel lowerBounds = new LinearRel();
				LinearRel unrelated = new LinearRel();
				LinearRel.splitLeqGeq(norm.linConstrs, aVar, upperBounds, lowerBounds, unrelated);
	
				int mod_lcm = norm.modConstrs.lcmOfMods(aVar);
				
				// simple heuristics for choosing between left and right infinite projection 
				boolean leftProjection = (lowerBounds.size() <= upperBounds.size());
				LinearRel bounds = leftProjection? lowerBounds : upperBounds;
				
				for (int j = 0; j < mod_lcm; ++j) {
					for (LinearConstr bound : bounds.toCol()) {
	
						LinearConstr lb_plus_j = new LinearConstr(bound);
						lb_plus_j.removeTerm(aVar);
						if (!leftProjection)
							lb_plus_j.multiplyWith(-1);
						if (leftProjection)
							lb_plus_j.addLinTerm(new LinearTerm(null, j));
						else
							lb_plus_j.addLinTerm(new LinearTerm(null, -j));
						
						ModuloRel subst = norm.substitute(aVar, lb_plus_j);
						
	
						Relation.addIfNotContr_mod(aPresbConstrs, subst);
						//aPresbConstrs.add(subst);
						// Relation.addAsMinTypeIfSAT(aPresbConstrs, subst);
					}
				}
	
				if (bounds.size() == 0) {
					for (int j = 0; j < mod_lcm; ++j) {
						LinearConstr lc_j = new LinearConstr();
						if (leftProjection)
							lc_j.addLinTerm(new LinearTerm(null, j));
						else
							lc_j.addLinTerm(new LinearTerm(null, -j));
						
						ModuloConstrs renMod = norm.modConstrs.substitute(aVar, lc_j);
	
						ModuloRel leftInfProj = new ModuloRel(new LinearRel(unrelated), renMod);
	
						Relation.addIfNotContr_mod(aPresbConstrs, leftInfProj);
						//aPresbConstrs.add(leftInfProj);
						// Relation.addAsMinTypeIfSAT(aPresbConstrs, leftInfProj);
					}
				}
			}
		}
	}
	
	void compact() {
		update(inConst());
		update(outConst());
		
		tighten();
	}
	
	public Relation[] existElim1(Variable aVar) {
		Collection<ModuloRel> col = new LinkedList<ModuloRel>();
		existEliminate(aVar, col);
		return Relation.toMinTypeIfNotContr(col).toArray(new Relation[0]);
	}
	public Relation[] existElim2(Variable aVar) {
		Collection<ModuloRel> c1 = new LinkedList<ModuloRel>();
		existEliminate(aVar, c1);
		
		Collection<ModuloRel> c2 = new LinkedList<ModuloRel>();
		Variable vv = aVar.getCounterpart();
		for (ModuloRel r : c1) {
			r.existEliminate(vv, c2);
		}
		
		return Relation.toMinTypeIfNotContr(c2).toArray(new Relation[0]);
	}
	
	
	public Relation[] compose_param(ModuloRel otherRel, Collection<Variable> params) {
		ModuloRel other = otherRel;

		return compose_param(this, other, params).toArray(new Relation[0]);
	}
	
	public static Collection<Relation> compose_param(ModuloRel aPC1, ModuloRel aPC2, Collection<Variable> params) {

		if (aPC1.modConstrs.size() + aPC2.modConstrs.size() == 0) {

			// TODO: can this be done even if modulo constraints which do not involve var

			LinearRel ret = LinearRel.tryComposeFM(aPC1.linConstrs, aPC2.linConstrs);
			if (ret != null) {
				Collection<Relation> colRet = new LinkedList<Relation>();
				Relation.addAsMinTypeIfNotContr(colRet, ret);
				return colRet;
			}
		}

		HashSet<Variable> doublePrimed = new HashSet<Variable>();
		
		LinearRel renLinConstrs = new LinearRel();
		renLinConstrs.addAll(LinearRel.createRenamed_params(aPC1.linConstrs, Variable.Type.ePRIME1, doublePrimed, params));
		renLinConstrs.addAll(LinearRel.createRenamed_params(aPC2.linConstrs, Variable.Type.ePRIME0, doublePrimed, params));

		ModuloConstrs renModConstrs = new ModuloConstrs();
		renModConstrs.addAll(ModuloConstrs.createRenamed_params(aPC1.modConstrs, Variable.Type.ePRIME1, doublePrimed, params));
		renModConstrs.addAll(ModuloConstrs.createRenamed_params(aPC2.modConstrs, Variable.Type.ePRIME0, doublePrimed, params));

		ModuloRel renPresbConstr = new ModuloRel(renLinConstrs, renModConstrs, false);
		if (renPresbConstr.simpleContradiction())
			return new LinkedList<Relation>();
		
		return eliminateVars(renPresbConstr, doublePrimed, false); // don't minimize
	}
	public static Collection<Relation> compose(ModuloRel aPC1, ModuloRel aPC2) {

		if (aPC1.modConstrs.size() + aPC2.modConstrs.size() == 0) {

			// TODO: can this be done even if modulo constraints which do not involve var

			LinearRel ret = LinearRel.tryComposeFM(aPC1.linConstrs, aPC2.linConstrs);
			if (ret != null) {
				Collection<Relation> colRet = new LinkedList<Relation>();
				Relation.addAsMinTypeIfNotContr(colRet, ret);
				return colRet;
			}
		}

		HashSet<Variable> doublePrimed = new HashSet<Variable>();
		
		LinearRel renLinConstrs = new LinearRel();
		renLinConstrs.addAll(LinearRel.createRenamed(aPC1.linConstrs, Variable.Type.ePRIME1, doublePrimed));
		renLinConstrs.addAll(LinearRel.createRenamed(aPC2.linConstrs, Variable.Type.ePRIME0, doublePrimed));

		ModuloConstrs renModConstrs = new ModuloConstrs();
		renModConstrs.addAll(ModuloConstrs.createRenamed(aPC1.modConstrs, Variable.Type.ePRIME1, doublePrimed));
		renModConstrs.addAll(ModuloConstrs.createRenamed(aPC2.modConstrs, Variable.Type.ePRIME0, doublePrimed));

		ModuloRel renPresbConstr = new ModuloRel(renLinConstrs, renModConstrs, false);
		if (renPresbConstr.simpleContradiction())
			return new LinkedList<Relation>();
		
		return eliminateVars(renPresbConstr, doublePrimed);
	}
	public Collection<Relation> elimVarsDontMinimize(Collection<Variable> vars) {
		return ModuloRel.eliminateVars(this, vars, false);
	}
	public Collection<Relation> elimVars(Collection<Variable> vars) {
		return ModuloRel.eliminateVars(this, vars, true);
	}
	private static Collection<Relation> eliminateVars(ModuloRel aMR, Collection<Variable> vars) {
		return eliminateVars(aMR, vars, true);
	}
	private static Collection<Relation> eliminateVars(ModuloRel aMR, Collection<Variable> vars, boolean toMinType) {
		
		//TODO copy parameter vars
		
		
		vars = new LinkedList<Variable>(vars);
		
		// First, eliminate as many variables from V for which there is constraint v = t, where t is a term.
		// Second, eliminate as many variables from V for which there is constraint c*v = t, where c is integer, and t is a term.
		
		List<LinearConstr> aIneqs = new LinkedList<LinearConstr>();
		List<LinearConstr> aEqs = new LinkedList<LinearConstr>();
		aMR.linConstrs.separateEqualities(aIneqs, aEqs);
		
		// heuristic: substitute first those with x = t where t has minimal number of monomials
		LinearConstr best_lc = null;
		LinearTerm best_lt = null;
		int mode = 0;
		do {
			
			best_lc = null;
			//best_v = null;
			
			for (LinearConstr lc : aEqs) {
				if (best_lc != null && best_lc.size() <= lc.size())
					continue;
				for (LinearTerm lt : lc.terms()) {
					int c = lt.coeff();
					Variable v = lt.variable();
					boolean b = (mode == 0)? (c == -1 || c == 1) : true;
					if (b && (vars.contains(v)) ) {
						best_lc = lc;
						best_lt = lt;
					}
				}
			}
			
			if (best_lc != null) {
				
				best_lc = new LinearConstr(best_lc);
				Variable best_v = best_lt.variable();
				if (best_lt.coeff() == 1)
					best_lc.transformBetweenGEQandLEQ();
				best_lc.removeTerm(best_lt.variable());
				
				if (mode == 1) {
					
					int lcm = aMR.lcmForCoeffOf(best_v);
					
					aMR.normalizeCooper_insitu(best_v, lcm);
					// each coefficient of best_v will be either 1 or -1
					
					// add modulo constraint
					ModuloConstr mc = new ModuloConstr(best_lt.coeff(), best_lc);
					aMR.addConstraint(mc);
				}
				
				aMR.substitute_insitu(best_v, best_lc);
				if (aMR.simpleContradiction()) {
					return new LinkedList<Relation>();
				}
				
				vars.remove(best_v);
			}

			// no more constraints of the form v = t ---> switch to mode that will search for constraints of the form c*v = t
			if (best_lc == null && mode == 0) {
				mode ++;
			}			
		
		} while (best_lc != null || mode == 0);
		
		aMR.compact();
		
		// Apply FM elimination whenever it is possible.
		
		// identify variables which don't appear in modulo constraints
		List<Variable> fmCandidates = new LinkedList<Variable>();
		Set<Variable> moduloVars = aMR.modConstrs.variables();
		for (Variable v : vars) {
			if (!moduloVars.contains(v))
				fmCandidates.add(v);
		}
		Variable[] fmCandidatesArr = fmCandidates.toArray(new Variable[0]);

		// eliminate these variables by applying FM whenever possible 
		int arrSize = fmCandidatesArr.length;
		Variable varElim;
		do {

			varElim = null;

			for (int j = 0; j < arrSize; j++) {

				Variable v = fmCandidatesArr[j];
				if (v == null)
					continue;

				if (aMR.lcmForCoeffOf(v) == 1) {
					fmCandidatesArr[j] = null;
					varElim = v;
				}
			}

			if (varElim != null) {
				
				if (!LinearRel.FM_elimination(aMR.linConstrs, varElim))
					throw new RuntimeException();
				vars.remove(varElim);
				if (aMR.simpleContradiction())
					return new LinkedList<Relation>();
			}

		} while (varElim != null);

		// for the remaining variables, perform general Cooper elimination
		Collection<ModuloRel> col_next;
		Collection<ModuloRel> col = new LinkedList<ModuloRel>();
		col.add(aMR);
		for (Variable var : vars) {

			col_next = new LinkedList<ModuloRel>();

			for (ModuloRel presb : col) {
				presb.existEliminate(var, col_next);
			}

			removeSimpleContrad(col_next);
			col = col_next;
		}

		if (toMinType) {
			return Relation.toMinTypeIfNotContr(col);
		} else {
			Collection<Relation> ret = new LinkedList<Relation>();
			ret.addAll(col);
			return ret;
		}
	}
	public static void removeSimpleContrad(Collection<ModuloRel> col) {
		Iterator<ModuloRel> iter = col.iterator();
		while (iter.hasNext()) {
			if (iter.next().simpleContradiction())
				iter.remove();
		}
	}

	public Relation[] closureMaybeStar() {
		return null;
	}
	public Relation[] closurePlus() {
		return null;
	}
	public ClosureDetail closure_detail() {
		return null;
	}
	public ClosureDetail closurePlus_detail() {
		return null;
	}

	public Relation[] compose(Relation otherRel) {
		if (!(otherRel instanceof ModuloRel)) {

			return Relation.compose(this, otherRel);
		} else {
			ModuloRel other = (ModuloRel) otherRel;

			return compose(this, other).toArray(new Relation[0]);
		}
	}

	public Answer relEqualsSimple(ModuloRel other) {
		return this.linConstrs.relEquals(other.linConstrs).and(this.modConstrs.relEqualsSimple(other.modConstrs));
	}
	public Answer relEquals(Relation otherRel) {
		if (!(otherRel instanceof ModuloRel)) {

			// supposing canonical representation of relations is used
			return Answer.FALSE;
		} else {
			ModuloRel other = (ModuloRel) otherRel;

			return this.includes(other).and(other.includes(this));
		}
	}
	
	public static Answer includedSimple(Relation r1, Relation r2) {
		return includesSimple(r2,r1);
	}
	public static Answer includesSimple(Relation aR1, Relation aR2) {
		Relation rr1 = aR1;
		Relation rr2 = aR2;
		
		if (rr2.contradictory()) {
			return Answer.TRUE;
		}
		
		boolean b1 = !rr1.isLinear();
		boolean b2 = !rr2.isLinear();
		
		if (b1 && !b2) {
			ConstProps cp = rr2.inConst();
			cp.addallShallow(rr2.outConst());
			
			if (cp.size() > 0) {
			
				rr1 = rr1.copy();
				rr1.update(cp);
				
				if (rr1.contradictory()) {
					return Answer.FALSE;
				}
				
				b1 = !rr1.isLinear();
				b2 = !rr2.isLinear();
			}
		}
		
		if (!b1 && !b2) {
			return rr1.includes(rr2);
		} else if (b1 && b2) {
			
			ModuloRel m1 = rr1.toModuloRel();
			ModuloRel m2 = rr2.toModuloRel();
			if (m1.modConstrs.includesForSure(m2.modConstrs)
					&& m1.linConstrs.includes(m2.linConstrs).isTrue()) {
				return Answer.TRUE;
			} else {
				return Answer.DONTKNOW;
			}
		} else if (b1) {
			return Answer.DONTKNOW;
		} else {
			Relation lin = rr2.hull(RelType.LINEAR);
			if ( rr1.includes(lin).isTrue()) {
				return Answer.TRUE;
			} else {
				return Answer.DONTKNOW;
			}
		}
	}

	public Answer includes(Relation otherRel) {
		if (!(otherRel instanceof ModuloRel)) {

			return Relation.includes(this, otherRel);
		} else {
			
			ModuloRel other = (ModuloRel) otherRel;
			
			// try to avoid using modulos
			{
				if (this.modConstrs.includesForSure(other.modConstrs)
						&& this.linConstrs.includes(other.linConstrs).isTrue()) {
					return Answer.TRUE;
				}
			}

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
			
			other.linConstrs.toSBYicesList(iw, false); // not negated
			other.modConstrs.toSBYicesList(iw, false); // not negated

			iw.writeln("(or");
			iw.indentInc();
			this.linConstrs.toSBYicesList(iw, true); // negated
			this.modConstrs.toSBYicesList(iw, true); // negated

			iw.indentDec();
			iw.writeln(")"); // or
			iw.indentDec();
			iw.writeln(")"); // and

			iw.indentDec();
			iw.writeln(")"); // assert

			iw.writeln("(check)");

			StringBuffer yc = new StringBuffer();
			YicesAnswer ya = CR.isSatisfiableYices(sw.getBuffer(), yc);

			//if (ya.isKnown())
			//	System.err.println("known answer for modulo: \n"+this+"\n"+otherRel);
			
			return Answer.createFromYicesUnsat(ya);
		}
	}

	public Relation[] intersect(Relation otherRel) {
		if (!(otherRel instanceof ModuloRel)) {

			return Relation.intersect(this, otherRel);
			
		} else {
			ModuloRel other = (ModuloRel) otherRel;

			ModuloRel ret = new ModuloRel(this);
			ret.addConstraints(other);
			
			if (ret.satisfiable().isFalse()) {
				
				return new Relation[0];
				
			} else {
				
				return new Relation[] { Relation.toMinType(ret) };
				
			}
		}
	}

	public static void throwNotLinear() {
		throw new RuntimeException("Linear constraint expected");
	}

	public boolean isDBRel() {
		
		if (this.simpleContradiction())
			return true;
		
		return (this.modConstrs().size() == 0) && this.linConstrs.isDBRel();
	}

	public boolean isOctagon() {
		return (this.modConstrs().size() == 0) && this.linConstrs.isOctagon();
	}

	public boolean isLinear() {
		return (this.modConstrs().size() == 0);
	}

	public boolean isModulo() {
		return true;
	}

	public boolean simpleContradiction() {
		return modConstrs.simpleContradiction() || linConstrs.simpleContradiction();
	}
	
	public boolean tautology() {
		return this.size() == 0;
	}
	
	public Answer satisfiable() {
		
		if (modConstrs.size()+linConstrs.size() == 0)
			return Answer.TRUE;
		
		if (modConstrs.simpleContradiction() || linConstrs.simpleContradiction())
			return Answer.FALSE;
		
		StringBuffer yc = new StringBuffer();
		return Answer.createFromYicesSat(CR.isSatisfiableYices(this.toSBYicesFull(), yc));
	}

	public DBRel toDBRel() {
		
		if (this.simpleContradiction())
			return DBRel.inconsistentRel();
		
		if (this.modConstrs().size() != 0)
			DBRel.throwNotDBM();

		return this.linConstrs().toDBRel();
	}

	public OctagonRel toOctagonRel() {
		if (this.modConstrs().size() != 0)
			OctagonRel.throwNotOct();

		return this.linConstrs().toOctagonRel();
	}

	public LinearRel toLinearRel() {
		if (this.modConstrs().size() != 0)
			throwNotLinear();

		return new LinearRel(this.linConstrs);
	}

	//private boolean compactFlag = false;
	public ModuloRel toModuloRel() {
		return this.copy();
//		if (Relation.COMPACTNESS && !compactFlag) {
//			compactFlag = true;
//			this.linConstrs = LinearRel.compact(this.linConstrs);
//			return this;
//		} else
//			return this.copy();
	}

	public StringBuffer toSBYicesFull() {
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw);

		iw.writeln("(reset)");

		Set<Variable> vars = this.variables();
		CR.yicesDefineVars(iw, vars);

		iw.writeln("(assert");
		iw.indentInc();
		{
			iw.writeln("(and");
			iw.indentInc();

			this.linConstrs.toSBYicesList(iw);

			this.modConstrs.toSBYicesList(iw);

			iw.indentDec();
			iw.writeln(")");
		}

		iw.indentDec();
		iw.writeln(")");
		iw.writeln("(check)");

		return sw.getBuffer();
	}

	public boolean isFASTCompatible() {
		// no modulo constrains on prime variables
		for (Variable v : modConstrs.variables())
			if (v.isPrimed())
				return false;
		return linConstrs.isFASTCompatible();
	}
	public boolean isARMCCompatible() {
		return modConstrs.size() == 0;
	}

	public ModuloRel copy() {
		return new ModuloRel(this);
	}

	public Relation copy(Rename aRenVals, VariablePool aVarPool) {
		return new ModuloRel(this, aRenVals, aVarPool);
	}

	public void addImplicitActions(Collection<Variable> aRestriction) {
		for (Variable v : this.refVarsAsUnp()) {
			if (aRestriction.contains(v)) {
				this.linConstrs.addImplicitAction(v);
			}
		}
	}
	
	public ConstProps inConst() {	
		return this.linConstrs.inConst();
	}
	public ConstProps outConst() {
		return this.linConstrs.outConst();
	}
	public void update(ConstProps cps) {
		this.linConstrs.update(cps);
		for (ConstProp cp : cps.getAll()) {
			this.modConstrs.substitute_insitu(cp.v, LinearConstr.createConst(cp.c));
		}
	}
	public Collection<Variable> identVars() {
		return this.linConstrs.identVars();
	}

	public Relation merge(Relation otherRel) {
		return null;
	}
//	public Relation[] minPartition_old() {
//		return new Relation[]{this};
//	}
	public Relation[] minPartition() {
		
		// System.out.print("-");
		// initially, each variable has its own bucket of constraints, these are then merged
		Set<Variable> vars = this.refVarsAsUnp();
		int s = vars.size();
		ArrayList<List<Constr>> a = new ArrayList<List<Constr>>(s);
		Map<Variable,Integer> v2id = new HashMap<Variable,Integer>(s);
		ArrayList<Integer> id2inx = new ArrayList<Integer>(s);
		{
			int inx = 0;
			for (Variable v : vars) {
				a.add(new LinkedList<Constr>());
				v2id.put(v, inx);
				id2inx.add(inx);
				inx++;
			}
		}
		
		for (Constr c : this.constraints()) {
			
			// merge constraints
			Iterator<Variable> i = c.variables().iterator();
			Variable first = i.next();
			if (first.isPrimed()) {
				first = first.getCounterpart();
			}
			int inx_first = id2inx.get(v2id.get(first));
			while (i.hasNext()) {
				Variable v = i.next();
				if (v.isPrimed()) {
					v = v.getCounterpart();
				}
				int inx = id2inx.get(v2id.get(v));
				if (inx != inx_first) {
					List<Constr> aux = a.get(inx);
					a.get(inx_first).addAll(aux);
					aux.clear();
					for (int k=0; k<s; k++) {
						if (id2inx.get(k) == inx) {
							id2inx.set(k, inx_first);
						}
					}
				}
			}
			a.get(inx_first).add(c);
			
		}
		
		List<Relation> ret = new LinkedList<Relation>();
		for (List<Constr> l : a) {
			if (!l.isEmpty()) {
				ret.add(Relation.toMinType(new ModuloRel(l)));
			}
		}
		
		Relation[] aux = ret.toArray(new Relation[0]);
		for (int i=0; i<aux.length; i++) {
			Set<Variable> c1 = new HashSet<Variable>();
			aux[i].refVarsAsUnp(c1);
			for (int j=i+1; j<aux.length; j++) {
				Set<Variable> c2 = new HashSet<Variable>();
				aux[j].refVarsAsUnp(c2);
				c2.retainAll(c1);
				if (c2.size() > 0) {
					throw new RuntimeException();
					//this.minPartition();
				}
			}
		}
		
		return aux;
	}

	public Relation abstractDBRel() {
		return this.linConstrs.abstractDBRel();
	}
	public Relation abstractOct() {
		return this.linConstrs.abstractOct();
	}
	public Relation abstractLin() {
		return this.linConstrs.copy();
	}
	
	private Collection<Variable> variables_p() {
		return variables_p_OR_unp(false);
	}
	private Collection<Variable> variables_unp() {
		return variables_p_OR_unp(true);
	}
	private Collection<Variable> variables_p_OR_unp(boolean unp) {
		Collection<Variable> ret = this.variables();
		Iterator<Variable> iter = ret.iterator();
		while (iter.hasNext()) {
			boolean isUnp = !iter.next().isPrimed();
			if ((unp && !isUnp) || (!unp && isUnp))
				iter.remove();
		}
		return ret;
	}
	public Relation[] domain() {
		ModuloRel ret = this.copy();
		return ModuloRel.eliminateVars(ret, this.variables_p()).toArray(new Relation[0]);
	}
	public Relation[] range() {
		ModuloRel ret = this.copy();
		return ModuloRel.eliminateVars(ret, this.variables_unp()).toArray(new Relation[0]);
	}
	
	public DetUpdateAndGuards deterministicUpdate() {
		
		// split into octagonal and non-octagonal constraints
		// each non-octagonal constraints must be either over X or over X (but not over the union X U X') 
		ModuloRel g_unp = new ModuloRel();
		ModuloRel g_pr = new ModuloRel();
		ModuloRel update = new ModuloRel();
		
		for (Constr c : this.constraints()) {
			boolean somepr = false;
			boolean someunp = false;
			Set<Variable> vars = c.variables();
			for (Variable v : vars) {
				if (v.isPrimed()) {
					somepr = true;
				} else {
					someunp = true;
				}
			}
			
			// if c is octagonal constraint, add to update
			if (!c.isModulo() && ((LinearConstr)c).isOctagonal()) {
				update.addConstraint(c);
			}
			// else, if c mixes X and X', we can't accelerate
			else if (somepr && someunp) {
				return null;
			}
			// else, add c either to the guard on X or to the guard on X'
			else if (someunp) {
				g_unp.addConstraint(c);
			} else {
				g_pr.addConstraint(c);
			}
			
//			if (somepr && someunp) {
//				if (vars.size() > 2) {
//					return null;
//				} else {
//					update.addConstraint(c);
//				}
//			} else if (someunp) {
//				g_unp.addConstraint(c);
//			} else {
//				g_pr.addConstraint(c);
//			}
		}
		Relation update_min = Relation.toMinType(update);
		if (!update_min.isDBRel()) {
			return null;
		} else {
			DetUpdateAndGuards det = update_min.toDBRel().deterministicUpdate();
			if (det == null) {
				return null;
			} else {
				Relation aux_g_unp = Relation.toMinType(g_unp).intersect(det.guard_unp)[0];
				Relation aux_g_pr = Relation.toMinType(g_pr).intersect(det.guard_pr)[0];
				return new DetUpdateAndGuards(
						aux_g_unp,
						det.update,
						aux_g_pr);
			}
		}
	}
	public FiniteVarIntervals findIntervals() {
		return this.linConstrs.findIntervals();
	}
	
	public Relation weakestNontermCond() {
		throw new RuntimeException("internal error: method not supported");
	}
	
	private void tighten() {
		for (ModuloConstr c : this.modConstrs.modConstraints()) {
			LinearConstr lc_base = c.constr();
			LinearConstr lc_base_inv = LinearConstr.transformBetweenGEQandLEQ(lc_base);
			ModInfo info = new ModInfo();
			info.modOffset(c.constr().constTerm());
			this.linConstrs.tightenForModConstr(info,lc_base,lc_base_inv);
		}
	}
	private ModInfo extractModInfo(ModuloConstr aMc) {
		ModInfo ret = new ModInfo();
		LinearConstr lc_base = aMc.constr();
		LinearConstr lc_base_inv = LinearConstr.transformBetweenGEQandLEQ(lc_base);
		// retrieve modulo offset and interval bounds
		ModuloRel rel = new ModuloRel();
		ret.rel(rel);
		rel.linConstrs = linConstrs.extractModInfo(ret,lc_base,lc_base_inv);
		rel.modConstrs = modConstrs.extractModInfo(ret,aMc);
		ret.normalize(aMc.modulus());
		return ret;
	}
	
	public static void merge(List<ModuloRel> aL, List<ModuloRel> inferred, List<BitSet> inferred_from, boolean ltModulus) {
		int n = aL.size();
		
		
		Map<ModuloConstr,BitSet> l_mc_base = new HashMap<ModuloConstr,BitSet>();
		int i=-1;
		for (ModuloRel r : aL) {
			i++;
			for (ModuloConstr mc : r.modConstrs.modConstraints()) {
				ModuloConstr mc_base = mc.copyWithoutConstTerm();
				BitSet bs = l_mc_base.get(mc_base);
				if (bs == null) {
					bs = new BitSet(n);
					l_mc_base.put(mc_base, bs);
				}
				bs.set(i);
			}
		}
		for (Map.Entry<ModuloConstr,BitSet> e : l_mc_base.entrySet()) {
			
			
			ModuloConstr mc_base = e.getKey();
			BitSet bs = e.getValue();
			int m = mc_base.modulus();
			
//			// a heuristic
//			if (bs.cardinality() < 2) {
//			//if (bs.cardinality() < m) {
//				continue;
//			}
			
			// extract all needed information, normalize
			ModInfo[] l_info = new ModInfo[n];
			i = -1;
			while ((i = bs.nextSetBit(i+1)) >= 0) {
				l_info[i] = aL.get(i).extractModInfo(mc_base);
			}
			
			// partitioning of relations (logically equivalent relations into same partition)
			List<BitSet> l_part = new LinkedList<BitSet>();
			i = -1;
			l1: while ((i = bs.nextSetBit(i+1)) >= 0) {
				// check for equivalence in existing partitions
				if (!l_part.isEmpty()) {
					ModuloRel r1 = l_info[i].rel();
					for (BitSet bs_part : l_part) {
						int i_part = bs_part.nextSetBit(0); // choose e.g. the first relation
						ModuloRel r2 = l_info[i_part].rel();
						if (r1.relEquals(r2).isTrue()) {
							bs_part.set(i);
							continue l1;
						}
					}
					
				}
				// create new partition
				BitSet bs_new = new BitSet(n);
				bs_new.set(i);
				l_part.add(bs_new);
			}
			
			// process partitions
			for (BitSet bs_part : l_part) {
				
//				// a heuristic
//				if (bs_part.cardinality() < 2) {
//				//if (bs_part.cardinality() < m) {
//					continue;
//				}

				// heuristically choose the target modulus and offset
				BitSet bs_all_mod = new BitSet(m);
				int i_part = -1;
				while ((i_part = bs_part.nextSetBit(i_part+1)) >= 0) {
					int off = l_info[i_part].modOffset();
					bs_all_mod.set(off);
				}
//				// detecting intervals of length at least m, first quick check if this is possible
//				if (bs_all_mod.cardinality() != m) {
//					continue;
//				}
				int max = ltModulus? m/2 : m;
				int target_modulus = -1;
				BitSet offsets = null;
				for (int h=1; h<=max; h++) {
					if (m % h != 0) {
						continue;
					}
					offsets = new BitSet(h);
					lg: for (int g=0; g<h; g++) {
						for (int k=g; k<m; k+=h) {
							if (!bs_all_mod.get(k)) {
								continue lg;
							}
						}
						target_modulus = h;
						offsets.set(g);
					}
					if (offsets.cardinality() > 0) {
						break;
					}
				}
				if (target_modulus == -1) {
					continue;
				}
				
				// finally, detect intervals
				int target_offset = -1;
				while ((target_offset = offsets.nextSetBit(target_offset+1)) >= 0) {
					
					// generate interval boundaries
					Set<IntegerInf> cuts = new HashSet<IntegerInf>();
					i_part = -1;
					while ((i_part = bs_part.nextSetBit(i_part+1)) >= 0) {
						ModInfo info = l_info[i_part];
						IntegerInf bndUp = info.bndUp();
						IntegerInf bndLow = info.bndLow();
						for (int k=0; k<m; k++) {
							cuts.add(bndUp.plus(IntegerInf.giveField(k)));
						}
						for (int k=0; k<m; k++) {
							cuts.add(bndLow.plus(IntegerInf.giveField(-k)));
						}
					}
					List<IntegerInf> l_cuts = new ArrayList<IntegerInf>(cuts);
					Collections.sort(l_cuts);
					
					// check which intervals are OK
					int cnt = l_cuts.size() - 1; // number of intervals to check
					BitSet[] flags1 = new BitSet[m];
					BitSet[] flags2 = new BitSet[n];
					for (int k=0; k<m; k++) {
						flags1[k] = new BitSet(cnt);
					}
					i_part = -1;
					IntegerInf M = IntegerInf.giveField(mc_base.modulus()-1);
					while ((i_part = bs_part.nextSetBit(i_part+1)) >= 0) {
						ModInfo info = l_info[i_part];
						IntegerInf bndUp_weak = info.bndUp().plus(M);
						IntegerInf bndLow_weak = info.bndLow().min(M);
						flags2[i_part] = new BitSet(cnt);
						for (int k=0; k<cnt; k++) {
							if (bndLow_weak.lessEq(l_cuts.get(k)) && l_cuts.get(k+1).lessEq(bndUp_weak)) {
								flags2[i_part].set(k);
							}
						}
						int m_off = info.modOffset();
						flags1[m_off].or(flags2[i_part]);
					}
					
					// detect "continuous" intervals w.r.t. to the target modulus and offset
					BitSet flag = new BitSet(cnt);
					flag.or(flags1[target_offset]);
					for (int k=target_offset+target_modulus; k<m; k+=target_modulus) {
						flag.and(flags1[k]);
					}
					
					List<IntegerInf> l_intervals = new LinkedList<IntegerInf>();
					List<BitSet> lll = new LinkedList<BitSet>();
					{
					int iii = -1;
						while ((iii = flag.nextSetBit(iii+1)) >= 0) {
							BitSet bs_interval = new BitSet();
							lll.add(bs_interval);
							int jjj=iii;
							while (flag.get(jjj)) {
								bs_interval.set(jjj);
								jjj++;
							}
							l_intervals.add(l_cuts.get(iii));
							// (jjj-1) is the last interval, (jjj-1) + 1 is the index for the upper bound of that interval
							l_intervals.add(l_cuts.get(jjj));
							
							iii = jjj;
						}
					}
					
					// infer new relations
					
					ModuloRel r_base = l_info[bs_part.nextSetBit(0)].rel().copy();
					// create modulo constraint for the target offset and modulus
					if (target_modulus > 1) {
						LinearConstr aux = mc_base.constr().copy();
						aux.addLinTerm(LinearTerm.constant(target_offset));
						r_base.addConstraint(new ModuloConstr(target_modulus,aux));
					}
					Iterator<IntegerInf> iter = l_intervals.iterator();
					Iterator<BitSet> iter2 = lll.iterator();
					while(iter.hasNext()) {
						IntegerInf bnd_low = iter.next();
						IntegerInf bnd_up = iter.next();
						BitSet bs_interval = iter2.next();
						
						// a heuristic on minimal length of intervals
						if (bnd_up.isFinite() && bnd_low.isFinite()) {
							if (bnd_up.toInt()-bnd_low.toInt()+1 < m)
								continue;
						}
						
						ModuloRel r_new = r_base.copy();
						
						if (bnd_up.isFinite()) {
							LinearConstr lc_up = mc_base.constr().copy();
							lc_up.addLinTerm(LinearTerm.constant(-bnd_up.toInt()));
							r_new.addConstraint(lc_up);
						}
						if (bnd_low.isFinite()) {
							LinearConstr lc_low = mc_base.constr().copy();
							lc_low.transformBetweenGEQandLEQ();
							lc_low.addLinTerm(LinearTerm.constant(bnd_low.toInt()));
							r_new.addConstraint(lc_low);
						}
						
						BitSet bs_new = new BitSet(n);
						i_part = -1;
						while ((i_part = bs_part.nextSetBit(i_part+1)) >= 0) {
						//for (int ii=0; ii<n; ii++) {
							BitSet aux = new BitSet(cnt);
							aux.or(bs_interval);
							aux.and(flags2[i_part]);
							if (flags2[i_part].cardinality() > 0) {
								bs_new.set(i_part);
							}
						}
						
						inferred.add(r_new);
						inferred_from.add(bs_new);
					}
				}
			}
		}
	}

	public boolean hasOneTermTotal() {
		return this.modConstrs.hasOneTermTotal();
	}
	
	public OctagonRel octagonalSubrel() {
		return this.linConstrs.octagonSubrel();
	}
	// normalize modulo constraints w.r.t. equality classes:
	// e.g. x=y+1 && 2|x && 2|3y+3 becomes x=y+1 && 2|x && 2|3x
	// e.g. x=y+1 && 2|x && 2|y will simplify to x=y+1 && 2|x && 2|x-1 which is false
	private void simplify() {
		if (this.modConstrs.size() == 0) {
			return;
		}
		OctagonRel or = this.octagonalSubrel();
		Substitution s = or.equality_subst();
		if (s.size() == 0) {
			return;
		}
		//System.out.println("\nBEFORE: "+this);
		this.substitute_insitu(s);
		this.linConstrs.addSubstitutionEqualities(s);
		//System.out.println(" AFTER: "+this);
	}
}

