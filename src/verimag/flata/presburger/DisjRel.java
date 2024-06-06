package verimag.flata.presburger;

import java.util.*;

import org.sosy_lab.java_smt.api.BooleanFormula;

import verimag.flata.Closure;
import verimag.flata.common.Answer;
import verimag.flata.common.CR;
import verimag.flata.common.FlataJavaSMT;
import nts.parser.*;

public class DisjRel {

	public boolean isOctagon() {
		if (disjuncts.size() == 0)
			return true;
		return disjuncts.size() == 1 && disjuncts.get(0).isOctagon();
	}
	
	// retrieves the variable pool
	private VariablePool pool() {
		Set<Variable> vars = new HashSet<Variable>();
		for (CompositeRel r : disjuncts) {
			r.refVars(vars);
			if (!vars.isEmpty()) {
				return vars.iterator().next().vp();
			}
		}
		
		return VariablePool.createEmptyPoolNoDeclar();
	}
	
	private List<CompositeRel> disjuncts = new LinkedList<CompositeRel>();
	public List<CompositeRel> disjuncts() { return disjuncts; }
	public String toString() {
		if (disjuncts.size() == 0) {
			return "false";
		}
		Iterator<CompositeRel> iter = disjuncts.iterator();
		boolean b = true;
		String s = "";
		while (iter.hasNext()) {
			if (b) b = false;
			else s += "  ";
			s += "("+iter.next().toString()+")";
			if (iter.hasNext())
				s += " ||\n";
		}
		return s;
	}
	
	public DisjRel domain() {
		return domrange(true);
	}
	public DisjRel range() {
		return domrange(false);
	}
	private DisjRel domrange(boolean dom) {
		DisjRel ret = new DisjRel();
		for (CompositeRel r : disjuncts) {
			List<CompositeRel> aux = new LinkedList<CompositeRel>();
			List<CompositeRel> aux2 = new LinkedList<CompositeRel>();
			aux.add(r);
			Variable[] vars = r.unpPvars();
			int l = vars.length / 2;
			int b = (dom)? l : 0;
			int e = (dom)? l*2 : l;
				
			for (int i=b; i<e; i++) {
				Variable v = vars[i];
				for (CompositeRel rr : aux)
					for (CompositeRel rrr : rr.existElim1(v))
						aux2.add(rrr);
				List<CompositeRel> tmp = aux;
				aux = aux2; aux2=tmp;
				aux2.clear();
			}
			ret.disjuncts.addAll(aux);
		}
		return ret;
	}
	public DisjRel() {
		
	}
	public DisjRel(CompositeRel r) {
		addDisj(r);
//		if (!r.simpleContradiction())
//			disjuncts.add(r);
	}
	public DisjRel(CompositeRel[] rels) {
		for (CompositeRel r : rels) {
			//disjuncts.add(r);
			addDisj(r);
		}
	}
	public DisjRel(Collection<CompositeRel> rels) {
		addDisj(rels);
	}
	
	public void addDisj(DisjRel aOther) {
		addDisj(aOther.disjuncts);
	}
	public void addDisj(Collection<CompositeRel> rels) {
		for (CompositeRel r : rels) {
			addDisj_base(r);
			//disjuncts.add(r);
		}
		//disjuncts.addAll(rels);
	}
	public void addDisj(CompositeRel[] rels) {
		for (CompositeRel r : rels) {
			addDisj_base(r);
			//disjuncts.add(r);
		}
	}
	public void addDisj(CompositeRel rel) {
		addDisj_base(rel);
		//if (!rel.simpleContradiction())
		//	disjuncts.add(rel);
	}
	private void addDisj_base(CompositeRel aRel) {
		if (aRel.simpleContradiction())
			return;
		
		Iterator<CompositeRel> i = disjuncts.iterator();
		while (i.hasNext()) {
			CompositeRel r = i.next();
			if (r.includes(aRel).isTrue())
				return;
			if (aRel.includes(r).isTrue())
				i.remove();
		}
		disjuncts.add(aRel);
	}
	
	
	private boolean count(int[] x1, int[] x2) {
		int i = x1.length-1;
		while (i>=0) {
			if (x1[i] == x2[i]-1) {
				x1[i] = 0;
			} else {
				x1[i]++;
				return true;
			}
			i--;
		}
		return false;
	}
	
	public static DisjRel giveTrue() {
		return new DisjRel(new CompositeRel(DBRel.createTautology()));
	}
	public static DisjRel giveFalse() {
		return new DisjRel();
	}
	
	public DisjRel not() {
		
		// test false
		if (this.disjuncts.size() == 0) {
			return DisjRel.giveTrue();
		}
		
		Constr[] aux = new Constr[0];
		Constr[][] l = new Constr[disjuncts.size()][];
		int x1[] = new int[l.length];
		int x2[] = new int[l.length];
		{
			int i=0;
			for (CompositeRel rel : this.disjuncts) {
				List<Constr> aux2 = new LinkedList<Constr>();
				for (Constr cc : rel.toModuloRel().constraints()) {
					aux2.addAll(cc.not());
				}
				l[i] = aux2.toArray(aux);
				x1[i] = 0;
				x2[i] = l[i].length;
				i++;
			}
		}
		
		boolean relTrue = true;
		for (int i=0; i<x2.length; i++) {
			if (x2[i] != 0) {
				relTrue = false;
				break;
			}
		}
		if (relTrue) {
			return DisjRel.giveFalse();
		}
		
		DisjRel ret = new DisjRel();
		do {
			
			ModuloRel mr = new ModuloRel(); 
			for (int ii=0; ii<l.length; ii++) {
				mr.addConstraint(l[ii][x1[ii]]);
			}
			ret.addDisj(new CompositeRel(mr));
		} while (count(x1,x2));
		
		return ret;
	}
	// or operator, no copying of relations
	public DisjRel or_nc(DisjRel aOther) {
		DisjRel ret = new DisjRel();
		ret.addDisj(this);
		ret.addDisj(aOther);
		return ret;
	}
	private void copy_all(DisjRel aOther) {
		for (CompositeRel r : aOther.disjuncts) {
			this.disjuncts.add(r.copy());
		}
	}
	public DisjRel or(DisjRel aOther) {
		DisjRel ret = new DisjRel();
		ret.copy_all(this);
		ret.copy_all(aOther);
		return ret;
	}
	public DisjRel and(DisjRel aOther) {
		DisjRel ret = new DisjRel();
		for (CompositeRel r1 : disjuncts) {
			for (CompositeRel r2 : aOther.disjuncts) {
				ret.addDisj(r1.intersect(r2));
			}
		}
		return ret;
	}
	
	public DisjRel compose(DisjRel aOther) {
		DisjRel ret = new DisjRel();
		for (CompositeRel r1 : disjuncts) {
			for (CompositeRel r2 : aOther.disjuncts) {
				for (CompositeRel r : r1.compose(r2)) {
					ret.disjuncts.add(r);
				}
			}
		}
		return ret;
	}
	
	public DisjRel existElim1(Variable v) {
		DisjRel ret = new DisjRel();
		for (CompositeRel r : disjuncts) {
			ret.addDisj(r.existElim1(v));
		}
		return ret;
	}
	public DisjRel existElim2(Variable v) {
		DisjRel ret = new DisjRel();
		for (CompositeRel r : disjuncts) {
			ret.addDisj(r.existElim2(v));
		}
		return ret;
	}
	
	
	public PrefixPeriod prefixPeriod() {
		switch (disjuncts.size()) {
		case 0:
			return new PrefixPeriod(1,1);
		case 1:
			ClosureDetail cd = disjuncts.get(0).closure_detail(false);
			return new PrefixPeriod(cd.b,cd.c);
		default:
			throw new RuntimeException("Computation of prefix and period of disjunctive relations is not possible.");
		}
	}
	
	public DisjRel closurePlus(VariablePool pool) {
		return closure(pool, true);
	}
	public DisjRel closureStar(VariablePool pool) {
		return closure(pool, false);
	}
	
	private DisjRel closure(VariablePool pool, boolean plus) {
		if (disjuncts.size() == 0) {
			return new DisjRel();
		} else if (disjuncts.size() == 1) {
			CompositeRel[] cl;
			if (plus) {
				cl = disjuncts.get(0).closurePlus();
			} else {
				cl = disjuncts.get(0).closureStar();
			}
			if (cl == null) {
				System.out.println("Acceleration failed: "+this);
				System.exit(0);
			}
			return new DisjRel(cl);
		} else {
			
			if (disjuncts.size() > 1) {
				
				Collection<CompositeRel> cl = Closure.hackClosure(pool, this.disjuncts, plus);
				if (cl == null) {
					System.err.println("Semi-algorithm for discunctive acceleration failed.");
					System.exit(1);
				}
				return new DisjRel(cl);
				
			} else {
				
				return new DisjRel(disjuncts.get(0).closure(plus));
				
			}
		}
	}
	
	public DisjRel closureN(boolean plus, Variable n) {
		if (disjuncts.size() == 0) {
			return this.copy();
		} else if (disjuncts.size() == 1) {
			CompositeRel[] cl = disjuncts.get(0).closureN(plus, n);
			if (cl == null) {
				System.err.println("Relation cannot be accelerated. ["+disjuncts+"]");
				System.exit(1);
			}
			return new DisjRel(cl);
		} else {
			System.out.println("Computation of parametric R^n is not supported for disjunctive relations.");
			System.exit(1);
			return null;
		}
	}
	
	
	public DisjRel hullOct(DisjRel other) {
		
		if (this.contradictory() && other.contradictory()) {
			return DisjRel.giveFalse();
		}
		
		List<CompositeRel> l = new LinkedList<CompositeRel>(this.disjuncts);
		l.addAll(other.disjuncts);
		Iterator<CompositeRel> i = l.iterator();
		CompositeRel aux = i.next().hull(Relation.RelType.OCTAGON);
		while (i.hasNext()) {
			CompositeRel aux2 = i.next().hull(Relation.RelType.OCTAGON);
			aux = aux.hullOct(aux2);
		}
		DisjRel ret = new DisjRel();
		ret.addDisj(aux);
		return ret;
	}
	public DisjRel hull(Relation.RelType t) {
		
		if (this.contradictory()) {
			return DisjRel.giveFalse();
		}
		
		CompositeRel aux = null;
		for (CompositeRel r : disjuncts) {
			CompositeRel aux2 = r.hull(t);
			if (aux == null) {
				aux = aux2;
			} else {
				if (t != Relation.RelType.OCTAGON) {
					System.err.println("unsupported operation: linear or dbm hull on disjunctive relations");
					System.exit(1);
				}
				aux = aux.hullOct(aux2);
			}
		}
		
		DisjRel ret = new DisjRel();
		ret.addDisj(aux);
		return ret;
	}

	public DisjRel copy() {
		DisjRel cp = new DisjRel();
		for (CompositeRel r : disjuncts) {
			cp.disjuncts.add(r.copy());
		}
		return cp;
	}

	public DisjRel copy(Rename aRenVals, VariablePool aVP) {
		DisjRel cp = new DisjRel();
		for (CompositeRel r : disjuncts) {
			cp.disjuncts.add(r.copy(aRenVals, aVP));
		}
		return cp;
	}
	
	public void addImplicitActionsForSorted(Variable[] unpsort) {
		for (CompositeRel r : disjuncts) {
			r.addImplicitActionsForSorted(unpsort);
		}
	}
	
	public void terminationAnalysis() {
		if (disjuncts.size() > 1) {
			System.out.println("Termination for disjuntive loops not supported yet.");
			System.exit(1);
		}
		
		CompositeRel cr = disjuncts.get(0);
		if (!cr.isOctagon()) {
			System.out.println("Termination for type "+cr.getType()+" not supported yet.");
			System.exit(1);
		}
		
		CompositeRel nonterm = cr.weakestNontermCond();
		
		String s;
		if (nonterm == null) {
			s = "false";
		} else {
			s = ""+nonterm;
		}
		System.out.println("Nontermination space: "+s);
		
//		DBRel dbr = cr.toDBRel();
//		System.out.println("alwaysTerminates(): "+dbr.alwaysTerminates());
	}
	
	public Expr toNTS() {
		if (disjuncts.size() == 0) {
			return ASTWithoutToken.litBool(false);
		} else {
			
			for (CompositeRel r : disjuncts) {
				if (r.isTrue()) {
					return ASTWithoutToken.litBool(true);
				}
			}
			
			Iterator<CompositeRel> i = disjuncts.iterator();
			Expr aux = i.next().toNTS();
			while (i.hasNext()) {
				aux = ASTWithoutToken.exOr(aux, i.next().toNTS());
			}
			
			return aux;
		}
	}
	
	public Set<Variable> refVarsAsUnp() {
		Set<Variable> ret = new HashSet<Variable>();
		for (CompositeRel r : disjuncts) {
			r.refVarsAsUnp(ret);
		}
		return ret;
	}
	
	public ConstProps outConst() {
		
		if (this.disjuncts.isEmpty()) {
			return new ConstProps();
		}
		Iterator<CompositeRel> i = this.disjuncts.iterator();
		ConstProps ret = i.next().outConst();
		Set<ConstProp> set = new HashSet<ConstProp>(ret.col);
		for (CompositeRel r : this.disjuncts) {
			ConstProps aux = r.outConst();
			set.retainAll(aux.col);
		}
		return new ConstProps(set);
	}
	
	public boolean contradictory() {
		return disjuncts.size() == 0;
	}
	
	public Answer relEquals(DisjRel other) {
		int s1 = this.disjuncts.size();
		int s2 = other.disjuncts.size();
		if (s1 == 0 && s2 == 0) {
			return Answer.TRUE;
		} else {
			return this.implies(other).and(other.implies(this));
		}
	}

	public Answer implies(DisjRel other) {
		if (this.disjuncts.size() == 0 && other.disjuncts.size() == 0) {
			return Answer.FALSE;
		} else if (other.disjuncts.size() > 0 && other.disjuncts.iterator().next().isTrue()) {
			return Answer.TRUE;
		} else {

			FlataJavaSMT fjsmt = CR.flataJavaSMT;

			// Begin NOT Implies
			// Begin OR1
			LinkedList<BooleanFormula> formulasOR1 = new LinkedList<>();
			if (disjuncts.size() == 0) {
				formulasOR1.add(fjsmt.getBfm().makeFalse());
			} else {
				for (CompositeRel r : this.disjuncts) {
					formulasOR1.add(r.toModuloRel().toJSMTAsConj(fjsmt));
				}
			}
			// End OR1
			BooleanFormula formulaOR1 = fjsmt.getBfm().or(formulasOR1);

			// Begin OR2
			LinkedList<BooleanFormula> formulasOR2 = new LinkedList<>();
			if (other.disjuncts.size() == 0) {
				formulasOR2.add(fjsmt.getBfm().makeFalse());
			} else {
				for (CompositeRel r : other.disjuncts) {
					formulasOR2.add(r.toModuloRel().toJSMTAsConj(fjsmt));
				}
			}
			// End OR2
			BooleanFormula formulaOR2 = fjsmt.getBfm().or(formulasOR2);

			// End NOT Implies
			BooleanFormula formula = fjsmt.getBfm().not(fjsmt.getBfm().implication(formulaOR1, formulaOR2));

			return fjsmt.isSatisfiable(formula, true);
		}
	}
	
	public static DisjRel deterministicAcceleration(CompositeRel aGuardUnp, CompositeRel aGuardPr, CompositeRel aUpdate) {
		Variable k = VariablePool.createSpecial("$k");
		DisjRel aux = deterministicAcceleration_k(aGuardUnp, aGuardPr, aUpdate, k);
		return aux.existElim2(k);
	}
	public static DisjRel deterministicAcceleration_k(CompositeRel aGuardUnp, CompositeRel aGuardPr, CompositeRel aUpdate, Variable k) {
		DisjRel guardUnp = new DisjRel(aGuardUnp);
		DisjRel guardPr = new DisjRel(aGuardPr);
		DisjRel update = new DisjRel(aUpdate);
		
		
		Variable l = VariablePool.createSpecial("$l");
		//Variable k = VariablePool.createSpecial("$k");
		DisjRel closure_k = update.closureN(true, k);
		DisjRel closure_l = update.closureN(true, l);
		
		Set<Variable> vars = update.refVarsAsUnp();
		vars.addAll(guardUnp.refVarsAsUnp());
		vars.addAll(guardPr.refVarsAsUnp());
		Variable[] vars_arr = vars.toArray(new Variable[0]);
		Arrays.sort(vars_arr);
		DisjRel id = new DisjRel(CompositeRel.createIdentityRelationForSorted(vars_arr));
		
		// forall l . (1<=l /\ l<k) => exists z . R(l,x,z) /\ phi(z) /\ phi'(z)
		// <==>
		// not exists l . (1<=l /\ l<k) /\ not (exists z . R(l,x,z) /\ phi(z) /\ phi'(z))
		
		// phi: unprimed --> primed
		DisjRel guardUnp_asPr = id.and(guardUnp).range();
		
		DisjRel aux1 = closure_l.and(guardUnp_asPr).and(guardPr);
		DisjRel aux2 = aux1.domain().not();
		
		LinearRel lr = new LinearRel();
		// 1-ell<=0
		{
			LinearConstr lc = new LinearConstr();
			lc.addLinTerm(LinearTerm.constant(1));
			lc.addLinTerm(new LinearTerm(l,-1));
			lr.addConstraint(lc);
		}
		// ell-k+1<=0
		{
			LinearConstr lc = new LinearConstr();
			lc.addLinTerm(new LinearTerm(l,1));
			lc.addLinTerm(new LinearTerm(k,-1));
			lc.addLinTerm(LinearTerm.constant(1));
			lr.addConstraint(lc);
		}
		
		DisjRel aux3 = new DisjRel(new CompositeRel(lr));
		
		DisjRel aux4 = aux3.and(aux2);
		DisjRel aux5 = aux4.existElim2(l).not();

		LinearRel lr2 = new LinearRel();
		// 1-k<=0
		{
			LinearConstr lc = new LinearConstr();
			lc.addLinTerm(LinearTerm.constant(1));
			lc.addLinTerm(new LinearTerm(k,-1));
			lr2.addConstraint(lc);
		}
		
		DisjRel aux6 = new DisjRel(new CompositeRel(lr2));
		
		DisjRel aux7 = aux6.and(closure_k).and(aux5).and(guardUnp).and(guardPr);
		
		//DisjRel aux8 = aux7.existElim2(k);
		//
		//return aux8.disjuncts.toArray(new CompositeRel[0]);
		
		return aux7;
	}
	
	public boolean isTrue() {
		return disjuncts.size() == 1 && disjuncts.get(0).isTrue();
	}
}
