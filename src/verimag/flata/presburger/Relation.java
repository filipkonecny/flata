package verimag.flata.presburger;

import java.util.*;

import org.sosy_lab.java_smt.api.BooleanFormula;

import verimag.flata.acceleration.zigzag.flataofpca.ZigzagClosure;
import verimag.flata.acceleration.delta.DeltaClosure;
import verimag.flata.common.Answer;
import verimag.flata.common.FlataJavaSMT;
import verimag.flata.common.IndentedWriter;

public abstract class Relation extends RelationCommon {
	
	protected boolean isIdentity = false;
	public boolean isIdentity() { return isIdentity; }
	
	public static boolean CANONIZE_DB_OCT = true;
	public static boolean CLOSURE_ONLY = false; // don't eliminate n in R^n
	//public static boolean CLOSURE_EXIST_ELIM = true;
	public static AccelerationComp closureComp = AccelerationComp.DELTA;

	public static boolean COMPACTNESS = true;
	public static boolean DEBUG_COMPACTNESS = false;
	
	public abstract Relation weakestNontermCond();
	
	public static enum AccelerationComp {
		DELTA, ZIGZAG;
		
		public static boolean outOfMem = false;
		
		public boolean isZigzag() { return this == ZIGZAG; }
		public boolean isDelta() { return this == DELTA; }
		
		// R^*
		public static ClosureDetail closure_detail(DBM dbm, boolean isOctagon, LinearTerm[] substitution, Variable[] varsOrig) {
			return (new DeltaClosure()).closure_detail(dbm, isOctagon, substitution, varsOrig);
		}
		// R^+
		public static ClosureDetail closurePlus_detail(DBM dbm, boolean isOctagon, LinearTerm[] substitution, Variable[] varsOrig) {
			return (new DeltaClosure()).closurePlus_detail(dbm, isOctagon, substitution, varsOrig);
		}
		
		// Computes transitive closure. Identity included in the computed closure only when prefix=0.
		public static Relation[] closure(DBM dbm, boolean isOctagon, LinearTerm[] substitution, Variable[] varsOrig) {
			if (closureComp.isDelta()) {
				return (new DeltaClosure()).closure(dbm, isOctagon, substitution, varsOrig);
			} else {
				return (new ZigzagClosure()).closure(dbm, isOctagon, substitution, varsOrig);
			}
		}
		public static Relation[] closurePlus(DBM dbm, boolean isOctagon, LinearTerm[] substitution, Variable[] varsOrig) {
			return (new DeltaClosure()).closurePlus(dbm, isOctagon, substitution, varsOrig);
		}
//		public static int accelerationFactor(DBM dbm) {
//			return (new ZigzagClosure()).accelerationFactor(dbm);
//		}
	}
	
//	public boolean equals(Object aObj) {
//		if (!(aObj instanceof Relation))
//			return false;
//		
//		return this.relEquals((Relation)aObj).isTrue();
//	}
	public abstract Answer relEquals(Relation other);
	
	/**
	 * returns true iff this relation R includes the other relation R' (i.e. iff R' => R)
	 */
	public abstract Answer includes(Relation other);
	public Answer isIncludedIn(Relation other) {
		return other.includes(this);
	}
	
	public abstract Answer satisfiable();
	
	// R1 /\ R1 
	// returns array of size 0 if R1 /\ R2 is inconsistent
	// returns array of size 1 if R1 /\ R2 is consistent
	public abstract Relation[] intersect(Relation other);
	public Answer intersects(Relation other) {
		return Answer.createAnswer(this.intersect(other).length != 0);
		//return this.intersect(other).satisfiable();
	}
	
	// Returns relations in their minimal type. No SAT checks are performed. 
	// If the composition is inconsistent, an empty array is returned.
	public abstract Relation[] compose(Relation other);
	
	// contract on both elimination methods below:
	// return only satisfiable disjuncts (including tautology -- 
	// tautologies handled at the level of CompositeRel)
	
	// elimination methods return disjunction of relations
	// eliminates only variable v and NOT the counterpart of v
	public abstract Relation[] existElim1(Variable v);
	// eliminates only variable v and the counterpart of v
	public abstract Relation[] existElim2(Variable v);
	
	public abstract DetUpdateAndGuards deterministicUpdate();
	public abstract FiniteVarIntervals findIntervals();
	
	//protected boolean simpleContradiction = false;
	
	public boolean contradictory() {
		return satisfiable().equals(Answer.FALSE);
	}
	
	// Returns relations in their minimal type. No SAT checks are performed.
	public abstract Relation[] closureMaybeStar(); // R^*
	public abstract Relation[] closurePlus(); // R^+
	public abstract ClosureDetail closure_detail(); // R^*
	public abstract ClosureDetail closurePlus_detail(); // R^+
	public ClosureDetail closure_detail(boolean onlyPlus) {
		return (onlyPlus)? closurePlus_detail() : closure_detail();
	}
	
	public abstract Relation[] domain();
	public abstract Relation[] range();
	
	public abstract boolean isDBRel();
	public abstract boolean isOctagon();
	public abstract boolean isLinear();
	public abstract boolean isModulo();
	
	public abstract DBRel toDBRel();
	public abstract OctagonRel toOctagonRel();
	public abstract LinearRel toLinearRel();
	public abstract ModuloRel toModuloRel();
//	public abstract ModExistsRel toModExists();
	
// produces a conjunction of constraints
	public abstract BooleanFormula toJSMTAsConj(FlataJavaSMT fjsmt);
	// same as above, just no primes are used:
	//   unprimed variables suffixed with suf_unp
	//   primed variables suffixed with suf_p
	public abstract BooleanFormula toJSMTAsConj(FlataJavaSMT fjsmt, String s_u, String s_p);
	public abstract LinkedList<BooleanFormula> toJSMTList(FlataJavaSMT fjsmt, boolean negate);
	// all referenced variables
	
	public abstract boolean tautology();
	
	public abstract void refVars(Collection<Variable> aCol);
	public abstract Variable[] refVarsUnpPSorted();
	public abstract void refVarsAsUnp(Collection<Variable> aCol);
	public Collection<Variable> refVarsSorted() {
		List<Variable> ret = new LinkedList<Variable>();
		refVars(ret);
		return ret;
	}
	
	public abstract boolean isFASTCompatible();
	public abstract boolean isARMCCompatible();
	public abstract Relation copy();
	public abstract Relation copy(Rename aRenVals, VariablePool aVP);
	
	//public abstract void addImplicitActions(Variable[] allUnprimed); // TODO suppress this method
	public abstract void addImplicitActions(Collection<Variable> aRestriction);
	
	// partitions a relation as much as possible w.r.t. possible techniques
	//   problematic issue: redundant constraints -- e.g. (x<=3 /\ y<=4 /\ z<=5) => (x+y+z<=12)
	//   if a variable is unconstrained in a relation, it is not included in any of the output partitions
	public abstract Relation[] minPartition();
	
	public abstract Relation abstractDBRel();
	public abstract Relation abstractOct();
	public abstract Relation abstractLin();
	public static void abstract2Oct(Relation[] rels) {
		for (int i=0; i<rels.length; i++)
			if (!rels[i].isOctagon())
				rels[i] = rels[i].abstractOct();
	}
	
	public Relation hull(RelType aType) {
		switch (aType) {
		case DBREL:
			return this.abstractDBRel();
		case OCTAGON:
			return this.abstractOct();
		case LINEAR:
			return this.abstractLin();
		default:
			throw new RuntimeException("internal error: "+aType+" hull not supported.");
		}
	}
	
	public Relation joinOct(Relation other) {
		return this.toOctagonRel().hull(other.toOctagonRel());
	}
	
	public abstract Relation asCompact();
	public abstract boolean simpleContradiction();
	
	public abstract ConstProps inConst(); // return unprimed
	public abstract ConstProps outConst(); // return primed
	public abstract void update(ConstProps cps);
	public abstract Collection<Variable> identVars(); // return unprimed
	
	public static enum RelType {
		//RELFALSE, RELTRUE, // not comparable with any other .?.
		DBREL, OCTAGON, LINEAR, MODULO // pairwise comparable
		//MOD_EXISTS
		;
		
		public RelType max(RelType other) {
			//if (this == MOD_EXISTS || other == MOD_EXISTS)
			//	return MOD_EXISTS;
			//else 
			if (this == MODULO || other == MODULO)
				return MODULO;
			else if (this == LINEAR || other == LINEAR)
				return LINEAR;
			else if (this == OCTAGON || other == OCTAGON)
				return OCTAGON;
			else 
				return DBREL;
		}
		
		public boolean hasType(RelType given) { return this == given; }
		//public boolean isTrueOrFalse() { return this == RELFALSE || this == RELTRUE; }
		public boolean isDBM() { return this == DBREL; }
		public boolean isOctagon() { return this == OCTAGON; }
		public boolean isLinear() { return this == LINEAR; }
		public boolean isModulo() { return this == MODULO; }
		//public boolean isModExists() { return this == MOD_EXISTS; }
		
		public boolean isInClass(RelType aClass) { return this.ordinal() <= aClass.ordinal(); }
		public boolean isInClassDBRel() { return this.ordinal() <= DBREL.ordinal(); }
		public boolean isInClassOctagon() { return this.ordinal() <= OCTAGON.ordinal(); }
		public boolean isInClassLinear() { return this.ordinal() <= LINEAR.ordinal(); }
		public boolean isInClassModulo() { return this.ordinal() <= MODULO.ordinal(); }
		//public boolean isInClassModExists() { return this.ordinal() <= MOD_EXISTS.ordinal(); }
		
		public String nameShort() {
			switch (this) {
			case DBREL:
				return "D";
			case OCTAGON:
				return "O";
			case LINEAR:
				return "L";
			case MODULO:
				return "M";
			//case MOD_EXISTS:
			//	return "ME";
			default:
				throw new RuntimeException("internal error: unknown type of relation");
			}
		}
	}
	public abstract RelType getType(); // TODO not abstract
	
	public boolean hasType(RelType given) { 
		return getType() == given;
	}
	
	
	private static String unexpectedType = "unexpected type";
	
	public static class Pair {
		public Relation first;
		public Relation second;
		
		public Pair(Relation aFirst, Relation aSecond) {
			first = aFirst;
			second = aSecond;
		}
	}
	
	// if one of the relations is true or false, it is returned as the first element of the pair
	protected static Pair toCommonType(Relation aRel1, Relation aRel2) {
		RelType t1 = aRel1.getType();
		RelType t2 = aRel2.getType();
		
		Relation r1;
		Relation r2;
		
		r1 = aRel1;
		r2 = aRel2;
		
//		if (t1.isTrueOrFalse() || (t1 == t2)) {
//			// keep the order
//		} else if (t2.isTrueOrFalse()) {
//			r1 = aRel2;
//			r2 = aRel1;
//		} else 
		{
			switch (t1.max(t2)) {
			case DBREL:
				r1 = r1.toDBRel();
				r2 = r2.toDBRel();
				break;
			case OCTAGON:
				r1 = r1.toOctagonRel();
				r2 = r2.toOctagonRel();
				break;
			case LINEAR:
				r1 = r1.toLinearRel();
				r2 = r2.toLinearRel();
				break;
			case MODULO:
				r1 = r1.toModuloRel();
				r2 = r2.toModuloRel();
				break;
//			case MOD_EXISTS:
//				r1 = r1.toModExists();
//				r2 = r2.toModExists();
			default:
				throw new RuntimeException(unexpectedType);
			}
		}
		
		return new Pair(r1,r2);
	}
	
	public static Answer equals(Relation aRel1, Relation aRel2) {
		Pair p = toCommonType(aRel1, aRel2);
		return p.first.relEquals(p.second);
	}
	public static Answer includes(Relation aRel1, Relation aRel2) {
		Pair p = toCommonType(aRel1, aRel2);
		return p.first.includes(p.second);
	}
	public static Answer intersects(Relation aRel1, Relation aRel2) {
		Pair p = toCommonType(aRel1, aRel2);
		return p.first.intersects(p.second);
	}
	public static Relation[] intersect(Relation aRel1, Relation aRel2) {
		Pair p = toCommonType(aRel1, aRel2);
		return p.first.intersect(p.second);
	}
	
	public static Relation[] compose(Relation aRel1, Relation aRel2) {

		if (aRel1.isIdentity()) {
			if (Arrays.equals(aRel1.refVarsUnpPSorted(), aRel2.refVarsUnpPSorted()))
				return new Relation[]{aRel2.copy()};
		} else if (aRel2.isIdentity()) {
			if (Arrays.equals(aRel1.refVarsUnpPSorted(), aRel2.refVarsUnpPSorted()))
				return new Relation[]{aRel1.copy()};
		}
		
		Pair p = toCommonType(aRel1, aRel2);
		return p.first.compose(p.second);
	}
	
	public static Relation toMinType(Relation aRel) {
		
		if (aRel.isDBRel()) {
			return aRel.toDBRel();
		}
		if (aRel.isOctagon()) {
			return aRel.toOctagonRel();
		}
		if (aRel.isLinear()) {
			return aRel.toLinearRel();
		}
//		if (aRel.isModulo()) {
//			return aRel.toModuloRel();
//		}
		return aRel;
	}
	
	public static void addAsMinTypeIfNotContr(Collection<Relation> aPresbConstrs, Relation aRel) {
		
		if (!aRel.contradictory()) {
			Relation minType = Relation.toMinType(aRel);
			aPresbConstrs.add(minType);
		}
//		Relation minType = Relation.toMinType(aRel);
//		if (!minType.contradictory()) {
//			aPresbConstrs.add(minType);
//		}
	}
	public static Collection<Relation> toMinTypeIfNotContr(Collection<? extends Relation> col) {
		Collection<Relation> ret = new LinkedList<Relation>();
		for (Relation r : col) {
			Relation.addAsMinTypeIfNotContr(ret,r);
		}
		return ret;
	}
	
	public static void addIfNotContr_mod(Collection<ModuloRel> aPresbConstrs, ModuloRel aRel) {
		// TODO: check
		//if (!aRel.simpleContradiction() && !Relation.toMinType(aRel).contradictory()) {
		//	ModuloRel compact = toMinType(aRel).toModuloRel();
		//	aPresbConstrs.add(compact);
		//}
		
//		Relation tmp = toMinType(aRel);
//		
//		if (!tmp.contradictory())
//			aPresbConstrs.add(aRel);
		
		if (!aRel.contradictory())
			aPresbConstrs.add(aRel);
	}

	public static Collection<Relation> toMinType(Collection<? extends Relation> col) {
		Collection<Relation> ret = new LinkedList<Relation>();
		for (Relation r : col) {
			ret.add(Relation.toMinType(r));
		}
		return ret;
	}
	
	public static BooleanFormula toJSMTAsDisj(Collection<Relation> col, FlataJavaSMT fjsmt) {
		if (col.size() == 1) {
			return col.iterator().next().toJSMTAsConj(fjsmt);
		} else {
			// Begin OR
			LinkedList<BooleanFormula> formulasOR = new LinkedList<BooleanFormula>();

			for (Relation t : col) {
				formulasOR.add(t.toJSMTAsConj(fjsmt));
			}

			return fjsmt.getBfm().or(formulasOR);
		}
	}
	
	public static void checkmintype(Relation rel) {
		Relation min = Relation.toMinType(rel);
		if (!min.equals(rel)) {
			throw new RuntimeException();
		}
		
	}
	
	private static void addMinimal(Collection<Relation> aCol, Collection<Relation> aNew) {
		for (Relation r : aNew)
			addMinimal(aCol, r);
	}
	private static void addMinimal(Collection<Relation> aCol, Relation[] aNew) {
		for (Relation r : aNew)
			addMinimal(aCol, r);
	}
	private static boolean addMinimal(Collection<Relation> aCol, Relation aNew) {
		
		Iterator<Relation> iter = aCol.iterator();
		while (iter.hasNext()) {
			Relation r = iter.next();
			if (r.includes(aNew).isTrue()) {
				return false;
			} else if (r.isIncludedIn(aNew).isTrue()) {
				iter.remove();
				continue;
			}
		}

		aCol.add(aNew);
		return true;
	}
	
	private static final int MAX_ITER = 3;
	
	// if a relation R^i is inconsistent for some i>1, R^* is returned, null otherwise 
	protected Relation[] iterationInconsistent() {
		
		Collection<Relation> closure = new LinkedList<Relation>();
		Collection<Relation> lastlevel = new LinkedList<Relation>();
		closure.add(this);
		lastlevel.add(this);
		
		int depth = 2;
		boolean consistent = true;
		while (consistent && depth <= MAX_ITER) {
			
			Collection<Relation> newlevel = new LinkedList<Relation>();
			for (Relation r : lastlevel)
				Relation.addMinimal(newlevel, r.compose(this));

			Relation.addMinimal(closure, lastlevel);
			
			if (newlevel.isEmpty())
				consistent = false;
			
			depth ++;
		}
		
		if (consistent)
			return null;
		else 
			return closure.toArray(new Relation[0]);
	}
	
	public abstract Relation merge(Relation other);
	//protected abstract Relation syntaxMerge(Relation other);
	
	public abstract Relation[] substitute(Substitution s);
	
	public static Relation[] substitute(Relation r, Substitution s) {
		Relation ret = r;
		
		if (r.isDBRel() || r.isOctagon()) {
			ret = ret.toLinearRel();
		}
		
		return ret.substitute(s);
	}

	public static Variable[] inferCounterparts(List<Variable> vars) {
		int n = vars.size();
		Variable[] ret = new Variable[n*2];
		int ii = 0;
		for (Variable v : vars) {
			ret[ii] = v;
			ret[ii+n] = v.getCounterpart();
			ii++;
		}
		return ret;
	}
	
}
