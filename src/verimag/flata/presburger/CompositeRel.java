package verimag.flata.presburger;

import java.io.StringWriter;
import java.util.*;

import org.sosy_lab.java_smt.api.BooleanFormula;

import nts.parser.*;
import verimag.flata.acceleration.delta.DeltaClosure;
import verimag.flata.common.*;
import verimag.flata.presburger.Relation.*;


// octagonal abstraction performed only in the constructor CompositeRel(Relation r)

/**
 * Composite relation is an intersection of relations. The purpose is to
 * partition a set of constraints in a way that sets of variables used in the
 * partitions are disjoint. Then, each such partition is represented in a
 * minimal way. E.g. x+y+z=0 /\ v=5 /\ w-v=3 is represented as a union of 1
 * linear relation (x+y+z=0) and 1 DBM relation (v=5 /\ w-v=3). Note: {x,x'} is
 * always in at most one partition for each counter x.
 * 
 */
public class CompositeRel extends RelationCommon implements Label {

	private boolean simpleContradiction = false;

	// free variables of the relation
	// unprimed followed by primed; alphabetically; e.g. x,y,z,x',y',z'
	private Variable[] vars;
	private Map<Variable, Integer> var2inx; // mapping: variable x -> index of x
											// in vars

	// tautology <==> no free variables
	public boolean isTrue() {
		return vars.length == 0;
	}
	
	public static CompositeRel createTautology() {
		return new CompositeRel(DBRel.createTautology());
	}

	private int[] var2part; // partition where variable belongs to; same length
	// note: second half of arrays is a bit duplicit

	private Relation[] rels; // array of partitions
	private Variable[][] rel2vars; // variables used in a partition

	// in case relation is result of closure computation, store information
	// which disjunct it belongs to
	private ClosureDisjunct closure_disjunct = null;

	public ClosureDisjunct closure_disjunct() {
		return closure_disjunct;
	}

	public void closure_disjunct(ClosureDisjunct a_closure_disjunct) {
		closure_disjunct = a_closure_disjunct;
	}

	public String toStringDetail() {
		String t = "rel2vars:\n";
		for (Variable[] arr : this.rel2vars)
			t += "   " + Arrays.toString(arr) + "\n";

		return "vars: " + Arrays.toString(vars) + "\n" + "rels: "
				+ Arrays.toString(rels) + "\n" + "var2part: "
				+ Arrays.toString(var2part) + "\n" + "var2inx: " + var2inx
				+ "\n" + t;
	}

//	public static CompositeRel contradiction() {
//		CompositeRel ret = new CompositeRel();
//		ret.simpleContradiction = true;
//		return ret;
//	}

	private CompositeRel() {
		
	}

	
	private void checkVars() {
		int l = vars.length / 2;
		for (int i = 0; i < l; i++) {
			if (!vars[i].getCounterpart().equals(vars[i + l]))
				throw new RuntimeException("internal error");
			if (i>0) {
				if (vars[i-1].compareTo(vars[i]) >=0 ) {
					throw new RuntimeException("internal error");
				}
			}
		}
	}
	private void checkDisjointPartitions() {
		for (int i1=0; i1<rel2vars.length; i1++) {
			Variable[] v1 = rel2vars[i1];
			for (int i2=i1+1; i2<rel2vars.length; i2++) {
				Variable[] v2 = rel2vars[i2];
				int x1=0, x2=0;
				while (x1<v1.length && x2<v2.length) {
					int c = v1[x1].compareTo(v2[x2]);
					if (c == 0) {
						throw new RuntimeException("internal error (partitions not disjoint)");
					} else if (c<0) {
						x1++;
					} else {
						x2++;
					}
					
				}
			}
		}
	}
	private void checkForTruePartitions() {
		if (this.rels.length == 1) {
			return;
		}
		for (int i=0; i<this.rels.length; i++) {
			if (this.rel2vars[i].length == 0) {
				throw new RuntimeException("internal error (tautologi)");
			}
		}
	}
	private static boolean DEBUG = true;
	private void watchConstruction() {
		if (DEBUG) {
			checkVars();
			checkDisjointPartitions();
			checkForTruePartitions();
		}
	}
	
	private void shallowCopyAllButRel(CompositeRel to) {
		// structures are not modified after construction -> copy them
		to.vars = this.vars;
		to.var2inx = this.var2inx;
		to.var2part = this.var2part;
		to.rels = new Relation[this.rels.length];
		to.rel2vars = this.rel2vars;
	}
	private void deepCopyVars(CompositeRel to) {
		// structures are not modified after construction -> copy them
		to.vars = Arrays.copyOf(this.vars, this.vars.length);
		to.rels = new Relation[this.rels.length];
		to.rel2vars = Arrays.copyOf(this.rel2vars, this.rel2vars.length);
		for (int i=0; i<rel2vars.length; i++) {
			to.rel2vars[i] = Arrays.copyOf(rel2vars[i], rel2vars[i].length);
		}
	}
	// ???
	private CompositeRel shallowCopyButRel() {
		CompositeRel ret = new CompositeRel();
		shallowCopyAllButRel(ret);
		
		ret.watchConstruction();
		return ret;
	}

	private static Map<Variable, Integer> createMapVar2int(Variable[] vararr) {
		Map<Variable, Integer> ret = new HashMap<Variable, Integer>();
		for (int j = 0; j < vararr.length; j++)
			ret.put(vararr[j], new Integer(j));

		return ret;
	}

	public CompositeRel[] substitute(Substitution s) {
		Relation r = this.rels[0];
		for (int i=1; i<this.rels.length; i++)
			r = r.intersect(this.rels[i])[0];
		
		Relation[] ret = r.substitute(s);
		if (ret.length == 0) {
			return new CompositeRel[0];
		} else {
			return new CompositeRel[] { new CompositeRel(Relation.toMinType(ret[0])) };
		}
	}
	
	public CompositeRel(CompositeRel other, Rename aRenVals, VariablePool aVarPool) {
		
		this.rels = new Relation[other.rels.length];
		for (int i = 0; i < other.rels.length; i++) {
			this.rels[i] = other.rels[i].copy(aRenVals, aVarPool);
		}
		
		this.inferAllFromRels();
		
		watchConstruction();
	}

	public CompositeRel(CompositeRel other) {
		other.shallowCopyAllButRel(this);
		
		this.rels = new Relation[other.rels.length];
		for (int i = 0; i < other.rels.length; i++) {
			this.rels[i] = other.rels[i].copy();
		}
		
		watchConstruction();
	}

	// sets a new partition for variables in the partition
	private void setVar2Part(int inx) {
		for (Variable v : rel2vars[inx]) {
			int i = this.var2inx.get(v).intValue();
			this.var2part[i] = inx;
		}
	}

	// some operations may change some partitions, hence a minimality needs to
	// be checked and ensured
	// inx - index of a partition
	private void checkForMinimality(int inx) {
		Relation[] tmp = this.rels[inx].minPartition();
		if (tmp.length == 1) {
			this.rels[inx] = tmp[0];
		} else {
			int l = rels.length;
			this.rels = Arrays.copyOf(this.rels, l + tmp.length - 1);
			this.rel2vars = Arrays.copyOf(this.rel2vars, l + tmp.length - 1);
			
			this.rels[inx] = tmp[0];
			this.rel2vars[inx] = tmp[0].refVarsUnpPSorted();
			setVar2Part(inx);
			for (int i = 1; i < tmp.length; i++) {
				int ii = l + i - 1;
				this.rels[ii] = tmp[i];
				this.rel2vars[ii] = tmp[i].refVarsUnpPSorted();
				setVar2Part(ii);
			}
		}
	}

	public CompositeRel hull(Relation.RelType aType) {
		// partition-wise abstraction

		int l = rels.length;
		Relation[][] tmp2 = new Relation[l][];

		//System.out.print("<");
		for (int i = 0; i < l; i++) {
			if (rels[i].hasType(aType)) {
				tmp2[i] = new Relation[] { rels[i] };
			} else {
				tmp2[i] = rels[i].hull(aType).minPartition();
			}
		}
		//System.out.print(">");

		return new CompositeRel(tmp2);
	}
	

	public static boolean onlyLin = false;

	public CompositeRel(Relation aR) {

		Relation r = Relation.toMinType(aR);
		simpleContradiction = r.simpleContradiction();
		if (simpleContradiction)
			return;
		if (r.satisfiable().isFalse()) {
			simpleContradiction = true;
			return;
		}
		
		if (r.tautology()) {
			
			rels = new Relation[0];
			
		} else {
		
			// quick fix due to visualization
			if (onlyLin) {
				rels = new Relation[] { r };
			} else {
				rels = r.minPartition();
			}
		}
		
		inferAllFromRels();
		
		watchConstruction();
	}
	
	// creates a new CompositeRel without splitting into partitions and minimizing the relation types  
	public static CompositeRel createNoMinNoPart(Relation r) {
		CompositeRel ret = new CompositeRel();

		ret.simpleContradiction = r.simpleContradiction();
		if (ret.simpleContradiction)
			throw new RuntimeException(
					"internal error: identified contradiction");

		// quick fix due to visualization
		ret.rels = new Relation[] { r };

		ret.inferAllFromRels();
		
		ret.watchConstruction();
		return ret;
	}
	
	private static CompositeRel[] toCompositeRels(ClosureDetail cd) {
		CompositeRel[] ret = new CompositeRel[cd.all.length];
		int i = 0;
		for (int inx = 0; inx < cd.b - 1; inx++) {
			CompositeRel cr = new CompositeRel(cd.prefix[inx]);
			cr.closure_disjunct = new ClosureDisjunct(cd.b, cd.c, inx + 1);
			ret[i++] = cr;
		}
		for (int inx1 = 0; inx1 < cd.c; inx1++) {
			for (int inx2 = 0; inx2 < cd.periodic_rel[inx1].length; inx2++) {
				CompositeRel cr = new CompositeRel(cd.periodic_rel[inx1][inx2]);
				cr.closure_disjunct = new ClosureDisjunct(cd.b, cd.c, inx1,
						cd.periodic_param[inx1], cd.parameter);
				ret[i++] = cr;
			}
		}
		return ret;
	}
	
	private static CompositeRel[] toCompositeRels(Relation[] rels) {
		CompositeRel[] ret = new CompositeRel[rels.length];
		for (int i = 0; i < rels.length; i++) {
			ret[i] = new CompositeRel(rels[i]);
		}
		return ret;
	}

	// input -- disjunction of relations
	// output -- disjunction of conjunctions of relations
	private static Relation[][] toMinPartition(Relation[] rels) {
		List<Relation[]> aux = new LinkedList<Relation[]>();
		
		for (int i = 0; i < rels.length; i++) {
			if (rels[i].contradictory())
				continue;
			Relation[] aux2 = rels[i].minPartition();
//			if (aux2.length == 1 && aux2[0].tautology()) 
//				continue;
			if (aux2.length == 1 && aux2[0].tautology()) {
				return new Relation[][] { new Relation[] {aux2[0]} }; // return true
			}
			aux.add(aux2);
		}
		Relation[][] ret = new Relation[aux.size()][];
		int i = 0;
		for (Relation[] r : aux) {
			ret[i++] = r;
		}
		return ret;
	}

	private static boolean nextPermutation(int[] inxMax, int[] inxCur) {
		for (int i = inxCur.length - 1; i >= 0; i--) {
			if (inxCur[i] > 0) {
				inxCur[i]--;
				return true;
			} else {
				inxCur[i] = inxMax[i];
			}
		}
		return false;
	}

	private void inferAllFromRels() {
		rel2vars = new Variable[rels.length][];
		int l = 0;
		for (int i = 0; i < rels.length; i++) {
			rel2vars[i] = rels[i].refVarsUnpPSorted();
			l += rel2vars[i].length;
		}
		inferVariables(l);
	}

	private void inferVariables(int varcnt) {
		vars = new Variable[varcnt];
		var2part = new int[varcnt];
		int j = 0;
		for (Variable[] vv : rel2vars) {
			int l = vv.length;
			System.arraycopy(vv, 0, vars, j, l);
			j += l;
		}
		Arrays.sort(vars);

		var2inx = createMapVar2int(vars);

		for (j = 0; j < rel2vars.length; j++) {
			for (Variable v : rel2vars[j])
				var2part[var2inx.get(v).intValue()] = j;
		}
	}

	private static CompositeRel[] toDNF(List<Relation[][]> rrr) {
		Relation[][][] tmp = new Relation[rrr.size()][][];
		int i = 0;
		for (Relation[][] r : rrr) {
			tmp[i++] = r;
		}
		return toDNF(tmp);
	}
	
	// relarr: conjunction of disjunctions of conjunctions
	private static CompositeRel[] toDNF(Relation[][][] relarr) {
		
		if (relarr.length == 0) {
			//return new CompositeRel[0];
			
			// empty conjunction is tautology 
			return new CompositeRel[] { new CompositeRel(DBRel.createTautology()) };
		}
		
		int[] disjInxMax = new int[relarr.length]; // max inx
		int[] disjInxCur = new int[relarr.length]; // current inx (initialized
													// to max inx)
		
		//int disjCnt = 1;
		for (int i = 0; i < relarr.length; i++) {
			int x = relarr[i].length - 1; // conjunct i has disjuncts 0..x
			//disjCnt *= (x + 1);
			disjInxMax[i] = x;
			disjInxCur[i] = x;
		}

		//CompositeRel[] ret = new CompositeRel[disjCnt];
		List<CompositeRel> aux = new LinkedList<CompositeRel>();

		// iterate over all permutaions
		lab1: do {

			CompositeRel rel = new CompositeRel();

			int relsCnt = 0;
			for (int j = 0; j < relarr.length; j++) { // iterate over input
														// conjuncts
				int len = relarr[j][disjInxCur[j]].length;
				if (len == 0) { // false (unsat) relation 
					continue lab1;
				}
				relsCnt += len;
			}

			rel.rels = new Relation[relsCnt];
			rel.rel2vars = new Variable[relsCnt][];

			int varcnt = 0;

			int k = 0;
			for (int j = 0; j < relarr.length; j++) { // iterate over input
														// conjuncts
				for (Relation r : relarr[j][disjInxCur[j]]) {

					rel.rels[k] = r;

					// store variable -> partition info
					rel.rel2vars[k] = r.refVarsUnpPSorted();

					varcnt += rel.rel2vars[k].length;

					k++;
				}
			}

			rel.inferVariables(varcnt);
			
			rel.watchConstruction();
			
			if (!rel.contradictory()) {
				aux.add(rel);
			}

		} while (nextPermutation(disjInxMax, disjInxCur));

		return aux.toArray(new CompositeRel[0]);
	}

	private CompositeRel(Relation[][] relarr) {
		construct_base(relarr);
		
		watchConstruction();
	}
	
	private Relation[] skipTrue(Relation[][] relarr) {
		List<Relation> aux = new LinkedList<Relation>();
		for (Relation[] rs : relarr) {
			for (Relation r : rs) {
				if (!r.tautology()) {
					aux.add(r);
				}
			}
		}
		return aux.toArray(new Relation[0]);
	}
	
	// relarr -- conjunction of conjunctions
	private void construct_base(Relation[][] aRelarr) {
		
		Relation[] relarr = skipTrue(aRelarr);
		
		int relsCnt = relarr.length;

		rels = new Relation[relsCnt];
		rel2vars = new Variable[relsCnt][];

		int varcnt = 0;
		
		for (int j = 0; j < relarr.length; j++) { // iterate over input
													// conjuncts
			Relation r = relarr[j];

			rels[j] = r;

			// store variable -> partition info
			rel2vars[j] = r.refVarsUnpPSorted();

			varcnt += rel2vars[j].length;
		}

		inferVariables(varcnt);
	}

	private Relation joinPartitions(List<Integer> what) {
		if (what.size() == 0)
			return null;

		Iterator<Integer> iter = what.iterator();

		Relation ret = rels[iter.next().intValue()];
		while (iter.hasNext()) {
			int i = iter.next().intValue();
			if (i < 0) {
				throw new RuntimeException();
			}
			ret = ret.intersect(rels[i])[0];
		}

		return ret;
	}

	// Composition is done partition-class-wise (on a coarser "common partition"
	// -- join operation on partition lattice)
	public CompositeRel[] compose(CompositeRel other) {
		
		Joined joined = this.join(other, false);
		
		// indexing: conjunction of composed partitions, which are themselves
		// disjunctions, each disjunct is again a conjunction of partitions
		
		List<Relation[][]> aux = new LinkedList<Relation[][]>();

		// iterate over new partitions
		// upon seeing contradiction, return immediately false
		// if the loop finishes and the resulting list is empty, then the composition is tautology
		for (JoinedPair jp : joined.collection()) {

			Relation r1 = jp.r1;
			Relation r2 = jp.r2;

			// compose joint relations
			// (note: the result may not be minimally partitioned -- e.g. (x'=y
			// /\ y'=x) \circ (x'=y /\ y'=x) )

			Relation[] comp = null;
			if (r1 != null && r2 != null)
				comp = r1.compose(r2);
			else if (r1 != null)
				comp = r1.domain();
			else if (r2 != null)
				comp = r2.range();

			if (comp.length == 0) // result of composition is false
				return new CompositeRel[0];
			
			Relation[][] aux2 = CompositeRel.toMinPartition(comp);
			if (aux2.length == 0) {
				return new CompositeRel[0];
			}
			if (aux2.length == 1 && aux2[0].length == 1 && aux2[0][0].tautology()) {
				continue;
			} else {
				aux.add(aux2);
			}
		}
		
		return CompositeRel.toDNF(aux);
	}

	public CompositeRel[] range() {
		List<Relation[][]> tmp = new LinkedList<Relation[][]>();
		for (int i=0; i<rels.length; i++) {
			Relation[][] aux2 = CompositeRel.toMinPartition(rels[i].range());
			//tmp.add(aux2);
			if (aux2.length == 0) {
				return new CompositeRel[0];
			}
			if (aux2.length == 1 && aux2[0].length == 1 && aux2[0][0].tautology()) {
				continue;
			} else {
				//aux.add(aux2);
				tmp.add(aux2);
			}
		}
		return CompositeRel.toDNF(tmp);
	}

	private void deriveSecondHalf() {
		int l = vars.length / 2;
		for (int i = 0; i < l; i++) {
			Variable vp = vars[i].getCounterpart();
			vars[i + l] = vp;
			var2part[i + l] = var2part[i];
			var2inx.put(vp, i + l);
		}
	}

	public static CompositeRel createIdentityRelationForSorted(
			Variable[] vars_unp) {
		CompositeRel ret = new CompositeRel();

		int l = vars_unp.length;
		ret.vars = Arrays.copyOf(vars_unp, l * 2);
		ret.var2part = new int[l * 2];
		ret.rels = new Relation[l];
		ret.rel2vars = new Variable[l][];
		ret.var2inx = new HashMap<Variable, Integer>();

		for (int i = 0; i < l; i++) {
			ret.var2part[i] = i;
			Variable[] arr_unp = new Variable[] { vars_unp[i] };
			Variable[] arr_unpP = new Variable[] { vars_unp[i], vars_unp[i].getCounterpart() };
			ret.rel2vars[i] = arr_unpP;
			ret.rels[i] = DBRel.createIdentityRelation(arr_unp);
		}

		ret.deriveSecondHalf();
		ret.var2inx = CompositeRel.createMapVar2int(ret.vars);

		ret.watchConstruction();
		return ret;
	}

	private void createImplActPartitions(List<VariableMerge> vms_unp) {

		int[] var2partOld = this.var2part;

		int l = vms_unp.size();
		this.vars = new Variable[l * 2];
		this.var2part = new int[l * 2];

		int newPartCnt = VariableMerge.newVarCnt(vms_unp);
		int oldPartCnt = this.rels.length;
		this.rels = Arrays.copyOf(this.rels, oldPartCnt + newPartCnt);
		this.rel2vars = Arrays.copyOf(this.rel2vars, oldPartCnt + newPartCnt);

		this.var2inx.clear();

		int k = 0, i = 0;
		for (VariableMerge vm : vms_unp) {

			this.vars[i] = vm.v;
			this.var2inx.put(vm.v, i);

			if (vm.iOld != -1) {
				this.var2part[i] = var2partOld[vm.iOld];
			} else {
				int relinx = oldPartCnt + (k++);
				this.var2part[i] = relinx;
				// Variable[] arr_unp = new Variable[] {vm.v};
				this.rel2vars[relinx] = new Variable[] { vm.v,
						vm.v.getCounterpart() };
				this.rels[relinx] = DBRel
						.createIdentityRelation(new Variable[] { vm.v });
			}
			
			i++;
		}

		deriveSecondHalf();
	}

	// adds implicit in existing relational partitions, and creates extra
	// partitions for variables
	// not appearing in this relation so far
	public void addImplicitActionsForSorted(Variable[] alsoFor_unp) {

		if (this.simpleContradiction)
			return;

		List<VariableMerge> vms = VariableMerge.domainMerge(Arrays.copyOf(
				this.vars, this.vars.length / 2), alsoFor_unp);

		Set<Variable> restriction = new HashSet<Variable>();
		for (Variable v : alsoFor_unp) {
			restriction.add(v);
		}
		
		// add implicit in existing relational partitions
		for (Relation r : this.rels)
			r.addImplicitActions(restriction);

		// create new partitions for variables not appearing do far
		createImplActPartitions(vms);
	}

	@Override
	public CompositeRel asCompact() {
		CompositeRel ret = this.shallowCopyButRel();
		for (int i = 0; i < rels.length; i++) {
			ret.rels[i] = this.rels[i].asCompact();
		}
		return ret;
	}

	// if condition is False, it returns null
	public CompositeRel weakestNontermCond() {
		int l = rels.length;

		if (l == 0)
			return this.copy();

		Relation joined = rels[0];
		for (int i = 1; i < l; i++) {
			joined = joined.intersect(rels[i])[0];
		}

		Relation nonterm = joined.weakestNontermCond();
		if (nonterm == null)
			return null;
		
		return new CompositeRel(nonterm);
	}

	// Acceleration partition-wise
	// onlyPlus --> returns R^+
	// !onlyPlus --> returns R^+ if R^0 is not entailed by some R^(i+k*c), else returns R^*
	public ClosureDetail closure_detail(boolean onlyPlus) {
		int l = rels.length;

//		if (l == 1) {
//
//			ClosureDetail cd = rels[0].closure_detail(onlyPlus);
//			DeltaClosure.cd_finish(cd);
//			return cd;
//
//		} else {
			
			// create detailed closures for individual partitions
			
			ClosureDetail cdarr[] = new ClosureDetail[l];
			for (int i = 0; i < l; i++) {
				ClosureDetail cd = rels[i].closure_detail(onlyPlus);
				if (cd == null)
					return null;
				cdarr[i] = cd;
			}
			

			return DeltaClosure.joined_closure(cdarr);
//		}
		
	}
	
	public CompositeRel[] closure(boolean plus) {
		return plus? closurePlus() : closureStar();
	}
	// returns R^+
	public CompositeRel[] closurePlus() {
		ClosureDetail cd = closure_detail(true);
		
		if (cd == null) {
			// check for deterministic core
			return deterministicClosure();
			//return null;
		}
		
		return CompositeRel.toCompositeRels(cd);
	}
	// returns R^*
	public CompositeRel[] closureStar() {
		ClosureDetail cd = closure_detail(false);
		
		if (cd == null) {
			// TODO: check for deterministic core
			return null;
		}
		
		CompositeRel[] ret = CompositeRel.toCompositeRels(cd);
		
		if (cd.b != 0) {
			ret = Arrays.copyOf(ret, ret.length+1);
			ret[ret.length-1] = CompositeRel.createIdentityRelationForSorted(Arrays.copyOf(vars, vars.length / 2));
		}
		
		return ret;
	}
	// returns R^* if R^0 covered by some R^(i+k*c), otherwise R^+
	public CompositeRel[] closureMaybeStar() {

		if (this.contradictory())
			throw new RuntimeException(
					"computing closures of contradictory relations not allowed");

		int l = rels.length;

		if (l == 0) {
			// true* = true
			return new CompositeRel[] { this.copy() };
			
		} else {
			
			ClosureDetail cd = closure_detail(false);
			if (cd == null)
				return null;
			
			return CompositeRel.toCompositeRels(cd);
		}
	}
	
	public CompositeRel[] deterministicClosure_old() {

		DetUpdateAndGuards det = this.toModuloRel().deterministicUpdate();
		if (det == null) {
			return null;
		}
		CompositeRel guard_unp = new CompositeRel(det.guard_unp);
		CompositeRel guard_pr = new CompositeRel(det.guard_pr);
		CompositeRel update = new CompositeRel(det.update);

		
		if (!update.isDBRel()) {
			throw new RuntimeException("internal error");
		}
		
		CompositeRel[] ret = DisjRel.deterministicAcceleration(guard_unp, guard_pr, update).disjuncts().toArray(new CompositeRel[0]);
		return ret;
	}
	
	public CompositeRel[] deterministicClosure() {
		
		Variable k = VariablePool.createSpecial("$k");
		
		DisjRel aux = DisjRel.giveTrue();
		
		for (Relation r : rels) {
			
			if (r.isOctagon()) {
				
				CompositeRel[] oct_cl = (new CompositeRel(r)).closureN(true, k);
				assert(oct_cl != null);
				aux = aux.and(new DisjRel(oct_cl));
				
			} else {
				
				DetUpdateAndGuards det = r.deterministicUpdate();
				if (det == null) {
					return null;
				}
				CompositeRel guard_unp = new CompositeRel(det.guard_unp);
				CompositeRel guard_pr = new CompositeRel(det.guard_pr);
				CompositeRel update = new CompositeRel(det.update);

				
				if (!update.isDBRel()) {
					throw new RuntimeException("internal error");
				}
				
				DisjRel det_cl = DisjRel.deterministicAcceleration_k(guard_unp, guard_pr, update, k);
				aux = aux.and(det_cl);
			}
						
		}
		
		return aux.existElim2(k).disjuncts().toArray(new CompositeRel[0]);
	}

	
	// for arbitrary non-octagonal partition, split w.r.t. its smallest interval
	public CompositeRel[] makeMoreAccelerable() {
		// needed only for non-octagonal partitions
		
		FiniteVarInterval interval = null;
		int inx;
		for (inx=0; inx<rels.length; inx++) {
			Relation r = rels[inx];
			if (!r.isOctagon()) {
				//DetUpdateAndGuards det = r.deterministicUpdate();
				//if (det == null || !det.update.isOctagon()) {
					FiniteVarIntervals l = r.findIntervals();
					//interval = l.getSmallestNonZero();
					interval = l.getSmallestInRange(1, 4);
					if (interval != null) {
						break;
					}
				//}
			}
		}
		
		if (interval == null) {
			return null;
		}
		
		Relation[][][] aux = new Relation[rels.length][][];
		for (int i=0; i<rels.length; i++) {
			if (i != inx) {
				aux[i] = new Relation[1][1];
				aux[i][0][0] = rels[i].copy();
			} else {
				int isize = interval.bnd_up - interval.bnd_low + 1;
				Relation[][] aux2 = new Relation[isize][];
				for (int offset=0; offset<isize; offset++) {
					ConstProps cps = new ConstProps();
					cps.add(new ConstProp(interval.var, interval.bnd_low + offset));
					Relation aux3 = rels[i].copy();
					aux3.update(cps);
					
					if (aux3.satisfiable().isFalse()) {
						aux2[offset] = new Relation[0];
					} else {
						aux2[offset] = aux3.minPartition();
					}
					
					//aux2[offset] = aux3.minPartition();
				}
				aux[i] = aux2;
			}
		}
		
		return CompositeRel.toDNF(aux);
	}
	
	private static CompositeRel varEqConst(Variable v, int c) {
		LinearConstr lc = new LinearConstr();
		lc.addLinTerm(new LinearTerm(v,1));
		lc.addLinTerm(new LinearTerm(null,-c));
		DBC[] cc = new DBC[] {new DBC(v,null,new IntegerInf(c)), new DBC(null,v,new IntegerInf(-c))};
		return new CompositeRel(new DBRel(cc, new Variable[] {v,v.getCounterpart()}));
	}
	//  plus --> returns R^n for n>=1
	// !plus --> returns R^n for n>=0
	// n==null -> keep the parameter from closure computation (the one which doesn't express #iterations)
	public CompositeRel[] closureN(boolean plus, Variable n) {
		
		boolean createN = n != null;
		
		if (this.contradictory())
			throw new RuntimeException(
					"internal error: computing closures of contradictory relations");

		int l = rels.length;

		if (l == 0) {
			
			// true U identity <=> true
			return new CompositeRel[] { this.copy() };
			
		} else {
			
			ClosureDetail cd = closure_detail(plus);
			
			if (cd == null) {
				// TODO: check for deterministic core
				return null;
			}
			
			List<CompositeRel> tmp = new LinkedList<CompositeRel>();
			
			int b = cd.b, c = cd.c;

			// conflicts of names are possible
			boolean conflict = createN && cd.parameter.equals(n);
			Variable ren_par = cd.parameter;
			LinearConstr ren_lc = null;
			if (conflict) {
				ren_par = VariablePool.createSpecial("##"+cd.parameter.name+"##");
				ren_lc = new LinearConstr();
				ren_lc.addLinTerm(new LinearTerm(ren_par, 1));
			}
			
			for (int i=0; i<c; i++) {
				LinearRel lr = cd.periodic_param[i];

				if (createN) {
				
					if (conflict) {
						lr.substitute_insitu(cd.parameter, ren_lc);
					}
					
					LinearConstr lc_n = new LinearConstr();
					// n = b+i+c*k <=> n-b-i-c*k <= 0 /\ n-b-i-c*k >= 0
					lc_n.addLinTerm(new LinearTerm(n,1));
					lc_n.addLinTerm(new LinearTerm(null,-b-i));
					lc_n.addLinTerm(new LinearTerm(ren_par,-c));
					
					LinearConstr lc_n2 = LinearConstr.transformBetweenGEQandLEQ(lc_n);
					
					lr.addConstraint(lc_n);
					lr.addConstraint(lc_n2);
					
					for (Relation r : lr.existElim1(ren_par)) {
						tmp.add(new CompositeRel(r));
					}
					
				} else {
					
					tmp.add(new CompositeRel(lr));
					
				}
			}
			
			int ppp = plus?1:0;
			CompositeRel[] ret = new CompositeRel[b-(ppp)+tmp.size()];
			CompositeRel[] pref = toCompositeRels(cd.prefix);
			if (createN) {
				for (int i=0; i<pref.length; i++) {
					pref[i] = pref[i].intersect(varEqConst(n,i+ppp))[0];
				}
			}
			
			int i1=0; int i2=pref.length;
			if (!plus && b!=0) {
				ret[0] = CompositeRel.createIdentityRelationForSorted(Arrays.copyOf(vars, vars.length / 2));
				if (createN) {
					ret[0] = ret[0].intersect(varEqConst(n,0))[0];
				}
				i1++;
				i2++;
			}
			System.arraycopy(pref, 0, ret, i1, pref.length);
			int i=i2;
			for (CompositeRel r : tmp) {
				ret[i++] = r;
			}
			return ret;
		}

	}

	// n used as in b + c*n
	public CompositeRel[] closureVarPeriod(boolean plus, Variable n) {
		
		if (this.contradictory())
			throw new RuntimeException(
					"internal error: computing closures of contradictory relations");

		int l = rels.length;

		if (l == 0) {
			return new CompositeRel[] { this.copy() };
		} else {
			
			ClosureDetail cd = closure_detail(plus);
			
			if (cd == null)
				return null;
			
			List<CompositeRel> tmp = new LinkedList<CompositeRel>();
			
			int b = cd.b, c = cd.c;

			boolean sameVar = cd.parameter.equals(n);
			LinearConstr subst = new LinearConstr();
			subst.addLinTerm(new LinearTerm(n,1));
			
			for (int i=0; i<c; i++) {
				LinearRel lr = cd.periodic_param[i];
				if (!sameVar) {
					lr.substitute_insitu(cd.parameter, subst);
				}
				
				tmp.add(new CompositeRel(lr));
			}
			
			int ppp = plus?1:0;
			CompositeRel[] ret = new CompositeRel[b-(ppp)+tmp.size()];
			CompositeRel[] pref = toCompositeRels(cd.prefix);
			
			int i1=0; int i2=pref.length;
			if (!plus && b!=0) {
				ret[0] = CompositeRel.createIdentityRelationForSorted(Arrays.copyOf(vars, vars.length / 2));
				i1++;
				i2++;
			}
			System.arraycopy(pref, 0, ret, i1, pref.length);
			int i=i2;
			for (CompositeRel r : tmp) {
				ret[i++] = r;
			}
			return ret;
		}

	}
	
	public void splitForDeterministicUpdate() {
		
	}


	@Override
	public CompositeRel copy() {
		return new CompositeRel(this);
	}

	@Override
	public CompositeRel copy(Rename aRenVals, VariablePool aVarPool) {
		return new CompositeRel(this, aRenVals, aVarPool);
	}

	@Override
	public RelType getType() {
		Relation.RelType type = Relation.RelType.DBREL;
		for (int i = 0; i < this.rels.length; i++) {
			type = type.max(this.rels[i].getType());
		}
		return type;
	}

	@Override
	public Collection<Variable> identVars() {
		Set<Variable> ret = new HashSet<Variable>();

		for (Relation r : this.rels)
			ret.addAll(r.identVars());

		return ret;
	}

	public Answer isIncludedIn(CompositeRel other) {
		return other.includes(this);
	}

	private static class JoinedPair {
		Relation r1;
		Relation r2;

		public String toString() {
			return "R1:" + r1 + "\nR2:" + r2 + "\n";
		}

		public JoinedPair(Relation aR1, Relation aR2) {
			r1 = aR1;
			r2 = aR2;
		}
	}

	private static class Joined {
		List<JoinedPair> list = new LinkedList<JoinedPair>();

		public void add(Relation r1, Relation r2) {
			list.add(new JoinedPair(r1, r2));
		}

		public Iterator<JoinedPair> iterator() {
			return list.iterator();
		}

		public Collection<JoinedPair> collection() {
			return list;
		}

		public int size() {
			return list.size();
		}
	}

	private Joined join(CompositeRel other) {
		return join(other, false);
	}

	private Joined join(CompositeRel other, boolean onlyCommon) {
		PartitionsJoin pj = PartitionsJoin.joinParitions(
				this.vars,this.var2part,this.rels.length,
				other.vars, other.var2part,other.rels.length);

		Joined ret = new Joined();
		for (PartitionsJoin.PartitionsJoinElem elem : pj.elements()) {

			// join relations (= intersect them)
			Relation r1 = this.joinPartitions(elem.partitions1());
			Relation r2 = other.joinPartitions(elem.partitions2());

			if (onlyCommon && (r1 == null || r2 == null))
				continue;

			ret.add(r1, r2);
		}

		return ret;
	}

//	public static boolean SIMPLE_INCL = true;
	public Answer includes(CompositeRel other) {
		
//		if (SIMPLE_INCL) {
//			return this.includesSimple(other);
//		}

		// R2 \subseteq R1 (R2=other, R1=this)
		// if
		// Vars(R2) \superseteq Vars(R1) (!! supposing that there are no
		// unconstrained variables !!)

		Joined joined = this.join(other);

		// check first DBM, only then more complex classes...
		Answer ret = Answer.TRUE;
		for (RelType rt : RelType.values()) {
			Answer a = CompositeRel.includes(joined, rt);
			ret = ret.and(a);
			if (ret.isFalse())
				return ret;
		}

		return ret;
	}

	private static Answer includes(Joined joined, RelType onlyType) {
		Iterator<JoinedPair> iter = joined.iterator();
		Answer ret = Answer.TRUE;
		while (iter.hasNext()) {
			JoinedPair jp = iter.next();
			Relation r1 = jp.r1;
			Relation r2 = jp.r2;

			if (r1 == null) // True includes everything
				continue;

			if (r2 == null) // r1 != True and r2 == null
				return Answer.FALSE;
			
			//if (!r1.getType().hasType(onlyType))
			if (!r1.getType().isInClass(onlyType) || !r2.getType().isInClass(onlyType))
				continue;

			Answer a = r1.includes(r2);
			ret = ret.and(a);
			if (ret.isFalse())
				return ret;
		}

		return ret;
	}
	
	private static Answer includesSimple(Joined joined, RelType onlyType) {
		Iterator<JoinedPair> iter = joined.iterator();
		Answer ret = Answer.TRUE;
		while (iter.hasNext()) {
			JoinedPair jp = iter.next();
			Relation r1 = jp.r1;
			Relation r2 = jp.r2;

			if (r1 == null) // True includes everything
				continue;

			if (r2 == null) // r1 != True and r2 == null
				return Answer.FALSE;

			if (!r1.getType().isInClass(onlyType)
					|| !r2.getType().isInClass(onlyType))
				continue;

			Answer a;
			if (onlyType.isModulo()) {
				a = ModuloRel.includesSimple(r1, r2);
			} else {
				a = r1.includes(r2);
			}
			ret = ret.and(a);
			if (ret.isFalse())
				return ret;
		}

		return ret;
	}

	public Answer includesSimple(CompositeRel other) {
		Joined joined = this.join(other);

		// check first DBM, only then more complex classes...
		Answer ret = Answer.TRUE;
		for (RelType rt : RelType.values()) {
			Answer a = CompositeRel.includesSimple(joined, rt);
			ret = ret.and(a);
			if (ret.isFalse())
				return ret;
		}

		return ret;
	}

	public Answer isIncludedSimple(CompositeRel other) {
		return other.includesSimple(this);
	}


	public CompositeRel[] intersect(CompositeRel other) {
		Joined joined = this.join(other, false);

		// indexing: conjunction of composed partitions, which are themselves
		// disjunctions,
		// each disjunct is again a conjunction of partitions
		Relation[][] tmp = new Relation[joined.size()][];

		// iterate over new partitions
		int i = 0;
		for (JoinedPair jp : joined.collection()) {

			Relation r1 = jp.r1;
			Relation r2 = jp.r2;

			// compose joint relations
			// (note: the result may not be minimally partitioned
			// -- e.g. (x'=y /\ y'=x) \circ (x'=x /\ y'=y) )

			Relation[] comp = null;
			if (r1 != null && r2 != null) {
				comp = r1.intersect(r2);
				
				if (comp.length == 0)
					return new CompositeRel[0];

				tmp[i++] = comp[0].minPartition();
				
			} else {
				if (r1 != null) 
					comp = new Relation[] { r1.copy() };
				else if (r2 != null)
					comp = new Relation[] { r2.copy() };
				
				tmp[i++] = comp;
			}
		}

		return new CompositeRel[] { new CompositeRel(tmp) };
	}

	public CompositeRel merge(CompositeRel other) {

		// merge only for octagons and dbms
		if (!this.getType().max(other.getType()).isInClassOctagon())
			return null;

		CompositeRel ret = merge_ifSamePartitions(other);

		if (ret != null)
			return ret;

		Joined joined = this.join(other);

		Relation merged[][] = new Relation[joined.size()][];

		int i = 0;
		boolean b = false; // merge found
		for (JoinedPair jp : joined.collection()) {

			if (jp.r1 == null || jp.r2 == null)
				return null;

			// if (!jp.r1.isDBM() || !jp.r2.isDBM())
			// return null;

			if (b) {
				if (!jp.r1.relEquals(jp.r2).isTrue())
					return null;
				else
					merged[i++] = new Relation[] { jp.r1 };
			} else {

				Relation r = jp.r1.merge(jp.r2);

				if (r == null) {
					return null;
				} else {
					b = true;
					merged[i++] = r.minPartition();
				}
			}
		}

		return new CompositeRel(merged);
	}

	public CompositeRel merge_ifSamePartitions(CompositeRel other) {
		
		if (this.rels.length != other.rels.length) // same number of partitions
			return null;
		if (!Arrays.equals(this.vars, other.vars)) // same variables
			return null;

		// find correspondence of (indices of) partitions
		int[] corr1 = new int[this.rels.length];
		int[] corr2 = new int[this.rels.length];

		int k = 0;
		BitSet bs = new BitSet(vars.length); // use a bitset to skip variables
												// from already processed
												// partitions

		for (int inx = bs.nextClearBit(0); inx >= 0 && inx < vars.length; inx = bs.nextClearBit(inx + 1)) {
			int relInx1 = this.var2part[inx];
			int relInx2 = other.var2part[inx];
			if (!Arrays.equals(this.rel2vars[relInx1], other.rel2vars[relInx2])) // partitions
																					// must
																					// be
																					// same
				return null;

			for (Variable v : this.rel2vars[relInx1])
				// mask variables from current partitioned (will be skipped
				// later on)
				bs.set(this.var2inx.get(v).intValue());

			if (k == rels.length) {
				System.out.println("\n" + this.toStringDetail());
				System.out.println(other.toStringDetail());
				System.out.print("k=" + k + ",\n" + this + "\n" + other + "\n");
				throw new RuntimeException();
			}

			corr1[k] = relInx1;
			corr2[k] = relInx2;
			k++;
		}

		int neqMerges = 0;
		Relation[][] merged = new Relation[this.rels.length][];
		for (int i = 0; i < this.rels.length; i++) {
			Relation r1 = this.rels[corr1[i]];
			Relation r2 = other.rels[corr2[i]];

			if (r1.relEquals(r2).isTrue()) {
				merged[i] = new Relation[] { r1 };
				continue;
			}

			Relation m = r1.merge(r2);
			if (m == null) {
				return null;
			} else {
				neqMerges++;
				if (neqMerges >= 2)
					return null;
				merged[i] = m.minPartition();
			}
		}

		return new CompositeRel(merged);
	}

	public Answer intersects(CompositeRel other) {
		return Answer.createAnswer(this.intersect(other).length != 0);
	}

	@Override
	public boolean isARMCCompatible() {
		for (Relation r : this.rels)
			if (!r.isARMCCompatible())
				return false;

		return true;
	}

	public boolean isFASTCompatible(int v_cnt) {
		
		if (this.vars.length != v_cnt)
			return false;
		for (Relation r : this.rels)
			if (!r.isFASTCompatible()) {
				return false;
			}

		return true;
	}

	public boolean isDBRel() {
		Relation.RelType t = getType();
		return t.isInClassDBRel();
	}

	public boolean isOctagon() {
		Relation.RelType t = getType();
		return t.isInClassOctagon();
	}

	public boolean isLinear() {
		Relation.RelType t = getType();
		return t.isInClassLinear();
	}

	public boolean isModulo() {
		Relation.RelType t = getType();
		return t.isInClassModulo();
	}

	@Override
	public ConstProps inConst() {
		ConstProps ret = new ConstProps();
		for (Relation r : this.rels)
			ret.addallShallow(r.inConst());

		return ret;
	}

	@Override
	public ConstProps outConst() {
		ConstProps ret = new ConstProps();
		for (Relation r : this.rels)
			ret.addallShallow(r.outConst());

		return ret;
	}

	@Override
	public void refVars(Collection<Variable> aCol) {
		for (Relation r : this.rels)
			r.refVars(aCol);
	}

	@Override
	public void refVarsAsUnp(Collection<Variable> aCol) {
		for (Relation r : this.rels)
			r.refVarsAsUnp(aCol);

	}
	
	public Variable[] unpPvars() {
		return this.vars;
	}
	public Variable[] unpvars() {
		return Arrays.copyOf(this.vars,this.vars.length / 2);
	}

	public Answer relEquals(CompositeRel other) {
		Answer ret = this.includes(other);
		if (ret.isFalse())
			return ret;
		ret = ret.and(other.includes(this));
		return ret;
	}

	private Answer satisfiable(Relation.RelType onlyType) {

		Answer ret = Answer.TRUE;
		for (Relation r : this.rels) {

			if (!r.getType().hasType(onlyType))
				continue;

			Answer a = r.satisfiable();
			ret = ret.and(a);
			if (ret.isFalse())
				return ret;
		}
		return ret;
	}

	@Override
	public Answer satisfiable() {

		if (this.simpleContradiction)
			return Answer.FALSE;

		Answer ret = Answer.TRUE;
		for (RelType rt : RelType.values()) {
			ret = ret.and(satisfiable(rt));
			if (ret.isFalse())
				return ret;
		}
		return ret;
	}

	@Override
	public boolean simpleContradiction() {
		if (simpleContradiction)
			return true;
		for (Relation r : this.rels) {
			if (r.simpleContradiction()) {
				simpleContradiction = true;
				return true;
			}
		}

		return false;
	}

	public BooleanFormula toJSMTAsConj(FlataJavaSMT fjsmt) {
		return toJSMTAsConj(fjsmt, null, null);
	}
	public BooleanFormula toJSMTAsConj(FlataJavaSMT fjsmt, String s_u, String s_p) {
		LinkedList<BooleanFormula> formulas = new LinkedList<>();

		formulas.add(fjsmt.getBfm().makeTrue());
		for (Relation r : this.rels) {
			formulas.add(r.toJSMTAsConj(fjsmt, s_u, s_p));
		}

		return fjsmt.getBfm().and(formulas);
	}
	
	public LinkedList<BooleanFormula> toJSMTList(FlataJavaSMT fjsmt, boolean negate) {
		LinkedList<BooleanFormula> formulas = new LinkedList<>();
		for (Relation r : this.rels) {
			formulas.addAll(r.toJSMTList(fjsmt, negate));
		}
		return formulas;
	}

	private void extendBy(int nVars, int nRels) {
		this.vars = Arrays.copyOf(this.vars, nVars);
		this.rel2vars = Arrays.copyOf(this.rel2vars, nRels);
		this.var2part = Arrays.copyOf(this.var2part, nVars);
		this.rels = Arrays.copyOf(this.rels, nRels);
	}

	private void initWith(int nVars, int nRels) {
		this.vars = new Variable[nVars];
		//this.rel2vars = new Variable[nVars];
		this.var2part = new int[nVars];
		this.rels = new Relation[nRels];

		this.var2inx = new HashMap<Variable, Integer>();
	}

	private void addNewConstsVars(ConstProps toAdd, boolean extend) {

		Set<Variable> newUnp = new HashSet<Variable>();
		for (ConstProp cp : toAdd.getAll()) {
			newUnp.add(cp.v.getUnprimedVar());
		}

		int uvplus = newUnp.size();
		int uvold = (extend) ? vars.length / 2 : 0;
		int uvnew = uvplus + uvold;
		int nrels = (extend) ? rels.length : 0;

		if (extend) {
			extendBy(uvnew * 2, nrels + uvplus);
		} else {
			initWith(uvnew * 2, nrels + uvplus);
		}

		int i = 0;
		for (Variable uv : newUnp) {
			int inx_r = nrels + i;

			List<DBC> tmp = new LinkedList<DBC>();
			ConstProp tmp2;
			tmp2 = toAdd.find(uv);
			if (tmp2 != null) {
				tmp.add(new DBC(uv, null, new IntegerInf(tmp2.c)));
				tmp.add(new DBC(null, uv, new IntegerInf(-tmp2.c)));
			}
			tmp2 = toAdd.find(uv.getCounterpart());
			Variable uvp = uv.getCounterpart();
			if (tmp2 != null) {
				tmp.add(new DBC(uvp, null, new IntegerInf(tmp2.c)));
				tmp.add(new DBC(null, uvp, new IntegerInf(-tmp2.c)));
			}

			rels[inx_r] = new DBRel(tmp.toArray(new DBC[0]), new Variable[] {
					uv, uvp });
			rel2vars[inx_r] = new Variable[2];
			rel2vars[inx_r][0] = uv;
			rel2vars[inx_r][1] = uv.getCounterpart();

			i++;
		}

		inferVariables(vars.length);
		// deriveSecondHalf();
	}

	public CompositeRel(ConstProps cps) {
		addNewConstsVars(cps, false);
		
		watchConstruction();
	}

	@Override
	public void update(ConstProps cps) {
		
		
		// TODO: change logic: first substitute, then add partitions of the form x=x'=const
		
		// updates for free variables of this relation
		for (int i = 0; i < rels.length; i++) {
			ConstProps tmp = cps.onlyForVars(rel2vars[i]);
			if (tmp.size() > 0) {
				rels[i].update(tmp);
				checkForMinimality(i); // e.g. (x=y /\ x=5) <==> (x=5 /\ y=5)
										// (right hand side is minimal partition)
			}
		}

		// updates for variables not present in this relation
		ConstProps toAdd = new ConstProps();
		// Collection<ConstProp> toAdd = new LinkedList<ConstProp>();
		for (ConstProp cp : cps.getAll()) {
			if (var2inx.get(cp.v) == null) {
				toAdd.add(cp);
			}
		}
		addNewConstsVars(toAdd, true);
	}

	// checks identity (relative to used free variables)
	public boolean isIdentity() {
		if (this.isTrue())
			return false;
		for (Relation r : this.rels) {
			if (!r.isIdentity)
				return false;
		}
		return true;
	}

	public OctagonRel toOctagon() {
		Relation r = this.rels[0];
		for (int i = 1; i < rels.length; i++) {
			r = r.intersect(rels[i])[0];
		}
		return r.toOctagonRel();
	}

	public DBRel toDBRel() {
		Relation r = this.rels[0];
		for (int i = 1; i < rels.length; i++) {
			r = r.intersect(rels[i])[0];
		}
		return r.toDBRel();
	}
	
	public LinearRel toLinearRel() {
		LinearRel ret = new LinearRel();
		for (Relation r : this.rels)
			ret.addAll(r.toLinearRel());
		return ret;
	}

	public ModuloRel toModuloRel() {
		ModuloRel ret = new ModuloRel();
		for (Relation r : this.rels)
			ret.addAll(r.toModuloRel());
		return ret;
	}

	public String toString() {

		if (isTrue())
			return "true";

		StringBuffer sb = new StringBuffer();
		if (rels.length != 0) {
			sb.append(rels[0].toString());
			for (int i = 1; i < rels.length; i++) {
				Relation r = rels[i];
				sb.append("," + r.toString());
			}

		}
		return sb.toString();
	}

	// partition wise hull
	// assumes that both relations are octagonal
	public CompositeRel hullOct(CompositeRel other) {
		Joined joined = this.join(other, true);

		Relation[][] tmp = new Relation[joined.size()][];

		int i = 0;
		for (JoinedPair jp : joined.collection()) {

			tmp[i] = jp.r1.joinOct(jp.r2).minPartition();

			i++;
		}

		return new CompositeRel(tmp);
	}

	public CompositeRel[] existElim1(Variable v) {
		return existElimBase(v,false);
	}
	public CompositeRel[] existElim2(Variable v) {
		return existElimBase(v,true);
	}
	public CompositeRel[] existElimBase(Variable v, boolean counterpart) {
		
		if (!var2inx.containsKey(v))
			return new CompositeRel[] {this.copy()};
		
		int inx = var2part[var2inx.get(v)];
		
		Relation[] tmp;
		if (counterpart)
			tmp = rels[inx].existElim2(v);
		else 
			tmp = rels[inx].existElim1(v);
		
		if (tmp.length == 0) { // contradiction
			return new CompositeRel[0];
		}
		
		for (int i=0; i<tmp.length; i++) {
			if (tmp[i].tautology()) { // tautology
				
				CompositeRel ret = new CompositeRel();
				ret.rels = new Relation[rels.length - 1];
				System.arraycopy(rels, 0, ret.rels, 0, inx);
				System.arraycopy(rels, inx+1, ret.rels, inx, rels.length - inx - 1);
				ret.inferAllFromRels();
				
				ret.watchConstruction();
				return new CompositeRel[] {ret};
			}
		}
		
		CompositeRel[] ret = new CompositeRel[tmp.length];
		
		for (int i = 0; i < ret.length; i++) { // iterate over disjunction
			Relation[] part = tmp[i].minPartition();
			
			CompositeRel r = new CompositeRel();
			
			r.rels = new Relation[rels.length+part.length-1];
			System.arraycopy(rels, 0, r.rels, 0, inx);
			System.arraycopy(rels, inx+1, r.rels, inx+1, rels.length - inx - 1);
			
			r.rels[inx] = part[0];
			
			int l=rels.length-1;
			for (int jj=1; jj<part.length; jj++) {
				r.rels[l+jj] = part[jj];
			}
			
			r.inferAllFromRels();
			
			ret[i] = r;
			ret[i].watchConstruction();
		}
		
		return ret;
	}

	public boolean isCall() {
		return false;
	}

	public boolean isRelation() {
		return true;
	}

	public static CompositeRel createAssignment(Variable[] lhs, LinearConstr[] rhs) {
		LinearRel lr = new LinearRel();
		for (int i=0; i<lhs.length; i++) {
			// rhs - lhs <= 0
			LinearConstr lc = new LinearConstr(rhs[i]);
			lc.addLinTerm(new LinearTerm(lhs[i],-1));
			// lhs - rhs <= 0
			LinearConstr lc2 = LinearConstr.transformBetweenGEQandLEQ(lc);
			
			lr.add(lc);
			lr.add(lc2);
		}
		return new CompositeRel(lr);
	}
	public static CompositeRel createAssignment(Variable[] lhs, Variable[] rhs) {
		LinearRel lr = new LinearRel();
		for (int i=0; i<lhs.length; i++) {
			// rhs - lhs <= 0
			LinearConstr lc = new LinearConstr();
			lc.addLinTerm(new LinearTerm(rhs[i],1));
			lc.addLinTerm(new LinearTerm(lhs[i],-1));
			// lhs - rhs <= 0
			LinearConstr lc2 = LinearConstr.transformBetweenGEQandLEQ(lc);
			
			lr.add(lc);
			lr.add(lc2);
		}
		return new CompositeRel(lr);
	}
	
	// checks inclusion 
	// R --> (R1 \/ ... \/ Rn) valid
	// not R \/ R1 \/ ... \/ Rn valid
	// not (not R \/ R1 \/ ... \/ Rn) unsatisfiable
	// R /\ not R1 /\ ... /\ not Rn unsatisfiable
	
	public static Answer subsumed(Collection<String> vars, CompositeRel r, Collection<CompositeRel> rels) {
		
		if (rels.isEmpty()) {
			return Answer.FALSE;
		}

		FlataJavaSMT fjsmt = CR.flataJavaSMT;

		// Begin AND
		LinkedList<BooleanFormula> formulasAND = r.toJSMTList(fjsmt, false);
		
		for (CompositeRel rr : rels) {
			// Begin OR
			LinkedList<BooleanFormula> formulasOR = rr.toJSMTList(fjsmt, true);
			// End OR
			formulasAND.add(fjsmt.getBfm().or(formulasOR));
		}

		BooleanFormula formula = fjsmt.getBfm().and(formulasAND);

		return fjsmt.isSatisfiable(formula, true);		
	}
	
	// assumption: relation is Linear
	public Expr toNTS() {
		
		if (!this.isLinear()) {
			throw new RuntimeException("internal error: linear relation assumed");
		}
		
		LinearRel lin = this.toLinearRel();
		return lin.toNTS();
	}

//	private static void removeUnprimed(Collection<Variable> c) {
//		Iterator<Variable> i = c.iterator();
//		while (i.hasNext()) {
//			Variable v = i.next();
//			if (!v.isPrimed())
//				i.remove();
//		}
//	}
//	private static StringBuffer variables(Collection<Variable> c, boolean counterparts) {
//		Iterator<Variable> i = c.iterator();
//		StringBuffer sb = new StringBuffer();
//		while (i.hasNext()) {
//			Variable v = i.next();
//			if (counterparts)
//				v = v.getCounterpart();
//			sb.append(v.name());
//			if (i.hasNext()) sb.append(",");
//		}
//		return sb;
//	}
	@Override
	public StringBuffer toSB_NTS() {
		
		StringBuffer sb = new StringBuffer();
		
		ModuloRel r = toModuloRel();
		
		// exploit FAST format
		sb.append(r.linConstrs().toSBFAST(true));
		boolean b = r.linConstrs().size() != 0 && r.modConstrs().size() != 0;
		boolean b2 = r.linConstrs().size() != 0 || r.modConstrs().size() != 0;
		if (b) {
			sb.append(" && ");
		}
		if (b2) {
			sb.append(r.modConstrs().toSBFAST());
		} else {
			sb.append("true");
		}
		
//		boolean NTS_HAVOC = false;
//		if (NTS_HAVOC) {
//			// havoc
//			Set<Variable> aux = new HashSet<Variable>(vp.allUnpCol());
//			Set<Variable> aux2 = new HashSet<Variable>();
//			r.refVars(aux2);
//			removeUnprimed(aux2);
//			aux.removeAll(aux2);
//			if (!aux.isEmpty()) {
//				if (b2)
//					sb.append(" && ");
//				sb.append("havoc(");
//				sb.append(variables(aux2,true));
//				sb.append(")");
//			}
//		}
		
		return sb;
	}
	
}
