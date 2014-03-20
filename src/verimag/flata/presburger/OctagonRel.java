package verimag.flata.presburger;

import java.util.*;

import verimag.flata.acceleration.delta.DeltaClosure;
import verimag.flata.common.Answer;
import verimag.flata.common.CR;
import verimag.flata.common.IndentedWriter;

public class OctagonRel extends Relation implements DBOct {
	
	public RelType getType() {
		return Relation.RelType.OCTAGON;
	}
	
	/**
	 * Let X={x1,...,xn} be a set of variables and X'={x' | x \in X}. A difference bound relation on the set 
	 * of variables X \cup X' (i.e., constraints of the form x-y <= c, x+y <= c, -x-y <= c, x <= c, -x <= c) 
	 * is represented as a DBM (difference bound matrix). 
	 * The encoding uses two variables xp and xm for each x \in X \cup X'.
	 * Constraints of the form x <= c and -x <= c are encoded as x+x <= 2*c and -x-x <= 2*c, respectively. 
	 * Hence, rows and columns correspond to [x1p,x1m,...,xnp,xnm,x1'p,x1'm,...,xn'p,xn'm]
	 */

	public static FieldStaticInf fs = IntegerInfStatic.fs();
	
	private DBM dbm;
	public DBM dbm() { return dbm; }
	
	private Variable[] varsOrig;
	
	
	@Override
	public Variable[] vars() {
		return varsOrig;
	}
	
	@Override
	public DBOct.II iii(Variable[] v1, Variable[] v2) {
		int[] ret1 = new int[v1.length*2];
		int[] ret2 = new int[v2.length*2];
		
		List<Variable> vars = new LinkedList<Variable>();
		
		// treat unprimed first

		int ll1 = v1.length;
		int l1 = ll1 / 2;
		
		int ll2 = v2.length;
		int l2 = ll2 / 2;
		
		int i1 = 0, ii1 = 0;
		int i2 = 0, ii2 = 0;
		int ii = 0;
		while (i1 < l1 && i2 < l2) {
			int c = v1[i1].compareTo(v2[i2]);
			
			if (c <= 0) {
				vars.add(v1[i1]);
			} else {
				vars.add(v2[i2]);
			}
			
			int iplus = 2*ii; 
			if (c <= 0) {
				ret1[ii1++] = iplus; // plus
				ret1[ii1++] = iplus+1; // minus
				i1++;
			}
			if (c >= 0) {
				ret2[ii2++] = iplus; // plus
				ret2[ii2++] = iplus+1; // minus
				i2++;
			}
			ii++;
		}
		while (i1 < l1) {
			vars.add(v1[i1]);
			int iplus = 2*ii;
			ret1[ii1++] = iplus; // plus
			ret1[ii1++] = iplus+1; // minus
			i1++; ii++;
		}
		while (i2 < l2) {
			vars.add(v2[i2]);
			int iplus = 2*ii;
			ret2[ii2++] = iplus; // plus
			ret2[ii2++] = iplus+1; // minus
			i2++; ii++;
		}
		
		// now, ii = number of unprimed variables (including zero) in the merged relation
		
		int mergedHalf = ii * 2;
		DBOct.II.copyPrimedPart(ll1, mergedHalf, ret1);
		DBOct.II.copyPrimedPart(ll2, mergedHalf, ret2);
		
		return new DBOct.II(ret1, ret2, 2 * mergedHalf, Relation.inferCounterparts(vars));
	}
	
	public DBOct.II iiiCommon(Variable[] v1, Variable[] v2) {
		
		Variable[] vars = Variable.intersect(v1, v2);
		
		int[] ret1 = new int[vars.length*2];
		int[] ret2 = new int[vars.length*2];
		
		//List<Variable> vars = new LinkedList<Variable>();
		
		// treat unprimed first

		int ll1 = v1.length;
		int l1 = ll1 / 2;
		
		int ll2 = v2.length;
		int l2 = ll2 / 2;
		
		int i1 = 0;
		int i2 = 0;
		int ii = 0;
		while (i1 < l1 && i2 < l2) {
			int c = v1[i1].compareTo(v2[i2]);
			
			
			
			if (c == 0) {
				int iplus1 = 2*i1;
				int iplus2 = 2*i2;
				
				ret1[ii] = iplus1; // plus
				ret1[ii+1] = iplus1+1; // minus
				
				ret2[ii] = iplus2; // plus
				ret2[ii+1] = iplus2+1; // minus
				
				ii += 2;
			}
			
			if (c <= 0) {
				i1++;
			}
			if (c >= 0) {
				i2++;
			}
			
		}
		
		DBOct.II.copyPrimedPart(vars.length, ll1, ret1);
		DBOct.II.copyPrimedPart(vars.length, ll2, ret2);
		
		return new DBOct.II(ret1, ret2, 2*vars.length, vars);
	}
	
	private void rename(Rename aRenVals, VariablePool aVP) {
		Variable[] varsOrig_ren = new Variable[varsOrig.length];
		for (int i=0; i<varsOrig.length; i++) {
			Variable vv = aVP.giveVariable(aRenVals.getNewNameFor(varsOrig[i].name()));
			varsOrig_ren[i] = vv;
		}
		varsOrig = varsOrig_ren;
		Arrays.sort(varsOrig);
	}
	
	private OctagonRel(DBM aDBM, Variable[] aVarsOrig) {
		dbm = aDBM;
		varsOrig = aVarsOrig;
	}
	
	public static int bar(int p) {
		if (p % 2 == 0)
			return p+1;
		else
			return p-1;
	}
	
	private static class Indices {
		int r1,c1;
		int r2,c2;
		
		private int getBase(Map<Variable, Integer> back_link, Variable v, int nVars) {
			int base = back_link.get(v.getUnprimedVar());
			if (v.isPrimed())
				base += nVars;
			return base;
		}
		public Indices(Map<Variable, Integer> back_link, OctConstr oc) {
			
			int nVars = back_link.size();
			
			int base1 = getBase(back_link, oc.lt1.variable(), nVars);
			int base2 = getBase(back_link, oc.lt2.variable(), nVars);
			
			// lt1: +/-x, lt2: +/-y
			r1 = 2*base1 - 1 + (oc.lt1.coeff()==1? -1 : 0); // +x -> xp, -x -> xm
			c1 = 2*base2 - 1 + (oc.lt2.coeff()==-1? -1 : 0); // -y -> yp, y -> yp
			
			r2 = bar(c1); // xp -> xm, xm -> xp
			c2 = bar(r1); // yp -> ym, ym -> yp
		}
	}
	
	public static void throwNotOct() {
		throw new RuntimeException("DBC expected");
	}
	
	public OctagonRel(OctConstr[] octConstrs, Variable[] vars_unpP) {
		createOctagonRel(octConstrs, vars_unpP);
	}
	
	public void createOctagonRel(OctConstr[] octConstrs, Variable[] aVarsUnpP) {
		
		int nVar = aVarsUnpP.length / 2; // number of variables
		int dim = 4 * nVar; // dimension of DBM
		
		varsOrig = aVarsUnpP;
		
		// mapping Domain -> {1 .. nVar}
		Map<Variable, Integer> back_link = new HashMap<Variable, Integer>();
		for (int i=1; i <= nVar; i++) {
			Variable v = aVarsUnpP[i-1];
			back_link.put(v, i);
		}
		
		Matrix m = DBM.newMatrix(dim, fs);
		
		m.init();
		
		for (OctConstr oc : octConstrs) {
			
			Indices ind = new Indices(back_link, oc);
			
			Field f = fs.giveField(oc.bound);
			setMin(m,ind.r1,ind.c1,f);
			setMin(m,ind.r2,ind.c2,f);
		}
		
		dbm = new DBM(DBM.Encoding.OCT, m, fs);
		
		if (CANONIZE_DB_OCT)
			dbm.canonize();
	}
	private void setMin(Matrix m, int row, int col, Field fnew) {
		Field f = m.get(row, col);
		
		if (f == null) {
			m.set(row, col, fnew);
		} else {
			m.set(row, col, f.min(fnew));
		}
	}
	
	public Answer relEquals(Relation otherRel) {
		if (!(otherRel instanceof OctagonRel)) {
			
			// supposing canonical representation of relations is used
			return Answer.FALSE;
		} else {
			OctagonRel other = (OctagonRel)otherRel;
			
			return Answer.createAnswer(dbm.equalsSem(other.dbm));
		}
	}
	public Answer includes(Relation otherRel) {
		if (!(otherRel instanceof OctagonRel)) {
			return Relation.includes(this, otherRel);
		} else {
			OctagonRel other = (OctagonRel)otherRel;
			
			
			DBOct.II ii = iii(varsOrig, other.varsOrig);
			
			// R1 includes R1  =>  dom(R1) subseteq dom(R2)
			//int l1 = ii.inx1.length;
			int l2 = ii.inx2.length;
			int ll = ii.mergedSize;
			if (!(l2 == ll)) {
			//if (!(l1 <= l2 && l2 == ll)) {
				return Answer.FALSE;
			}
			
			// now, dom(R1) subseteq dom(R2)
			// and we can check inclusion with projected R2
			
			return Answer.createAnswer(this.dbm.includes(other.dbm, ii.inx1));
		}
	}
	public Relation[] intersect(Relation otherRel) {
		if (!(otherRel instanceof OctagonRel)) {
			return Relation.intersect(this, otherRel);
		} else {
			OctagonRel other = (OctagonRel)otherRel;
			
			DBOct.II ii = iii(this.varsOrig, other.varsOrig);
			
			DBM inters = dbm.intersect(other.dbm, ii);
			
			if (inters.isConsistent()) {
				
				OctagonRel rel = new OctagonRel(inters, ii.vars);
				return new Relation[] { Relation.toMinType(rel) };
				
			} else {
				
				return new Relation[0];
			}
		}
	}
	
	private OctagonRel projectUnconstrained() {
		Integer[] inxsUnp = dbm.unconstrainedVars();
		
		if (inxsUnp.length*2 != this.varsOrig.length) {
			DBM dbm_proj = dbm.projectUnconstrained_internal(inxsUnp);
			OctagonRel ret = new OctagonRel(dbm_proj, Variable.projectVars(varsOrig,inxsUnp));
			return ret;
		} else {
			return this;
		}
	}
	
	// set the row and column corresponding to the variable to infinity 
	// (only the diagonal remains equal to 0)
	public Relation[] existElim1(Variable aVar) {
		int inx = Arrays.binarySearch(varsOrig, aVar);
		
		OctagonRel ret = this.copy();
		ret.dbm.removeConstraints(inx);
		
		ret = ret.projectUnconstrained();
		
		return new Relation[] { Relation.toMinType(ret) };
	}
	
	// projection (eliminates the variable and its counterpart)
	public Relation[] existElim2(Variable aVar) {
		int inx1 = Arrays.binarySearch(varsOrig, aVar);
		
		int n = varsOrig.length / 2;
		int inx2;
		if (inx1<n) {
			inx2 = inx1 + n;
		} else {
			inx2 = inx1;
			inx1 = inx2 - n;
		}
		
		OctagonRel ret = this.copy();
		ret.dbm.removeConstraints(inx1);
		ret.dbm.removeConstraints(inx2);
		
//		int len = (varsOrig.length-2)*2;
//		int[] inxs = new int[len];
//		Variable[] varsOrig_proj = new Variable[len];
//		
//		int aux1 = 0, aux2 = 0;
//		for (int i=0; i<varsOrig.length; i++) {
//			if (i == inx1 || i == inx2)
//				continue;
//			
//			varsOrig_proj[aux1++] = varsOrig[i];
//			
//			int plus = (2*(i+1))-2; int minus = (2*(i+1))-1;
//			inxs[aux2++] = plus;
//			inxs[aux2++] = minus;
//		}
//		
//		DBM dbm_proj = this.dbm.subDBM(inxs);
//		
//		Relation ret = Relation.toMinType(new OctagonRel(dbm_proj, varsOrig_proj));
		
		ret = ret.projectUnconstrained();
		
		return new Relation[] { Relation.toMinType(ret) };
	}
	
	public Relation[] compose(Relation otherRel) {
		if (!(otherRel instanceof OctagonRel)) {
			return Relation.compose(this, otherRel);
		} else {
			OctagonRel other = (OctagonRel)otherRel;

			DBOctCompatibility<OctagonRel> c = new DBOctCompatibility<OctagonRel>(this, other);
			c.extend();
			
			List<Field> diagonal = new LinkedList<Field>();
			
			DBM comp = c.first.compose(c.second, diagonal);
			
			if (IntegerInf.hasNonNegative(diagonal)) {
				
				Relation rel = new OctagonRel(comp,c.ii.vars);
				return new Relation[] {rel};
			} else {
				//return new Relation[] { RelFalse.give() };
				return new Relation[0];
			}
		}
	}
	
	
	public boolean isDBRel() {
		return dbm.octConstr_isDBConstr();
	}
	public boolean isOctagon() {return true; }
	public boolean isLinear() {return true; }
	public boolean isModulo() {return true; }
	
	public DBRel toDBRel() {
		
		if (!dbm.octConstr_isDBConstr())
			throw new RuntimeException("internal error: cannot convert an octagon to a dbm");
		
		return new DBRel(dbm.octMat2DBCs(varsOrig),varsOrig);
		
	}
	public OctagonRel toOctagonRel() {
		return this.copy();
	}
	public LinearRel toLinearRel() {
		LinearRel linRel = new LinearRel();
		
		// try to determinize
		DBM det = dbm.determinize_oct();
		
		if (det != null)
			linRel.addAll(det.octMat2LinConstrs(varsOrig)); // TODO: problems in LinearRel - variables, ...
		else
			linRel.addAll(dbm.octMat2LinConstrs(varsOrig)); // TODO: problems in LinearRel - variables, ...
		return linRel.asCompact();
		//return linRel;
	}
	public ModuloRel toModuloRel() {
		ModuloRel modRel = new ModuloRel();
		modRel.linConstrs(toLinearRel());
		return modRel;
	}
	
	private LinearTerm[] substitution() {
		
		int l = varsOrig.length;
		LinearTerm[] substitution = new LinearTerm[2*l];
		for (int i=1; i<=l; i++) {
			substitution[i*2-2] = new LinearTerm(varsOrig[i-1],1);  // plus
			substitution[i*2-1] = new LinearTerm(varsOrig[i-1],-1); // minus
		}
		
		return substitution;
	}
	
	public Relation[] closureMaybeStar() {
		return Relation.AccelerationComp.closure(dbm, true, substitution(), varsOrig);
	}
	public Relation[] closurePlus() {
		return Relation.AccelerationComp.closurePlus(dbm, true, substitution(), varsOrig);
	}
	public ClosureDetail closure_detail() {
		return Relation.AccelerationComp.closure_detail(dbm, true, substitution(), varsOrig);
	}
	public ClosureDetail closurePlus_detail() {
		return Relation.AccelerationComp.closurePlus_detail(dbm, true, substitution(), varsOrig);
	}
	
	public boolean satisfiable_internal() {
		return this.dbm.isConsistent();
	}
	
	public boolean simpleContradiction() {
		return !this.dbm.isConsistent();
	}
	
	public boolean tautology() {
		return varsOrig.length == 0;
	}
	
	public Answer satisfiable() {
		return Answer.createAnswer(satisfiable_internal());
	}

	public StringBuffer toStringBuf() {
		return dbm.toStringBuf_oct(varsOrig);
	}
	
	public String toString() {
		return toStringBuf().toString();
	}
	
	public void toSBYicesAsConj(IndentedWriter aIW, String s_u, String s_p) {
		CR.yicesAndStart(aIW);
		dbm.toStringBufYicesList_oct(aIW, varsOrig, s_u, s_p, false);
		CR.yicesAndEnd(aIW);
	}
	public void toSBYicesAsConj(IndentedWriter aIW) {
		toSBYicesAsConj(aIW, null, null);
	}
	public void toSBYicesList(IndentedWriter iw, boolean negate) {
		dbm.toStringBufYicesList_oct(iw, varsOrig, null, null, negate);
	}
	
	public void refVars(Collection<Variable> aCol) {
		aCol.addAll(Arrays.asList(varsOrig));
	}
	public void refVarsAsUnp(Collection<Variable> aCol) {
		for (int i=0; i<this.varsOrig.length / 2; i++)
			aCol.add(varsOrig[i]);
	}
	public Variable[] refVarsUnpPSorted() {
		return Arrays.copyOf(this.varsOrig, this.varsOrig.length);
	}
	
	public boolean isFASTCompatible() {
		return dbm.isFASTCompatible_oct();
	}
	public boolean isARMCCompatible() {
		return true;
	}
	
	public OctagonRel copy() {
		DBM m_copy = new DBM(this.dbm);
		return new OctagonRel(m_copy,varsOrig);
	}
	
	public OctagonRel copy(Rename aRenVals, VariablePool aVP) {
		OctagonRel ret = this.copy();
		ret.rename(aRenVals, aVP);
		return ret;
	}
	
	public void addImplicitActions(Collection<Variable> aRestriction) {
		
		boolean[] restr = new boolean[varsOrig.length];
		for (int i = 0; i<varsOrig.length; i++) {
			if (aRestriction.contains(varsOrig[i])) {
				restr[i] = true;
			}
		}
		
		this.dbm.addImplicitActions(restr);
		this.dbm.canonize();
	}
	
	public DBRel asCompact() {
		throw new RuntimeException("internal error: method not supported");
	}
	
	public DetUpdateAndGuards deterministicUpdate() {
		throw new RuntimeException("internal error: method not supported");
	}
	public FiniteVarIntervals findIntervals() {
		return dbm.findIntervals_oct(varsOrig);
	}
	
	public ConstProps inConst() {
		return this.dbm.inoutConst(this.varsOrig, true);
	}
	public ConstProps outConst() {
		return this.dbm.inoutConst(this.varsOrig, false);
	}

	private int origInx(Variable v) {
		int i = Arrays.binarySearch(this.varsOrig, v);
		if (i < 0)
			throw new RuntimeException("Internal error: variable not present");
		return i;
	}
	
	public void update(ConstProps cps) {
		
		Matrix m = this.dbm.mat();
		for (ConstProp cp : cps.getAll()) {
			int origInx = origInx(cp.v);
			origInx++;
			//int i = origInx + 1;
			//int matInx = (cp.v.isPrimed())? origInx+2 : origInx+1;
			Field f1 = fs.giveField(2*cp.c);
			Field f2 = fs.giveField(-2*cp.c);
			m.setMin(2*origInx-2, 2*origInx-1, f1);
			m.setMin(2*origInx-1, 2*origInx-2, f2);
		}
		
		if (CANONIZE_DB_OCT)
			dbm.canonize();
	}
	
	public Collection<Variable> identVars() {
		return dbm.identVars(varsOrig);
	}
	
	public boolean canPreciseMerge(OctagonRel other) {
		
		DBOctCompatibility<OctagonRel> c = new DBOctCompatibility<OctagonRel>(this, other);
		c.extend();
		
		return c.first.canPreciseMerge(c.second);
	}
	public Relation hull(OctagonRel other) {
		
		//DBOct.II ii = iii(this.varsOrig, other.varsOrig);
		DBOct.II ii = iiiCommon(this.varsOrig, other.varsOrig);
		
		DBM d = this.dbm.hull(other.dbm, ii);
		
		//OctagonRel hull = new OctagonRel(d,Variable.intersect(this.varsOrig, other.varsOrig));
		OctagonRel hull = new OctagonRel(d,ii.vars);
		
		return Relation.toMinType(hull);
	}
	public Relation merge(Relation otherRel) {
		if (!(otherRel instanceof OctagonRel))
			return null;
		
		if (!Arrays.equals(this.varsOrig, ((OctagonRel)otherRel).varsOrig))
			return null;
		
		//Relation ret = this.syntaxMerge(otherRel);
		//if (ret != null)
		//	return ret;
		
		OctagonRel other = (OctagonRel)otherRel;
		if (this.canPreciseMerge(other)) {
			return this.hull(other);
		} else
			return null;
	}
	
	public OctagonRel subOctagonRel(DBM subdbm, Set<Integer> varsinxs) {
		Variable[] newvarsorig = Variable.sortedSubset(this.varsOrig, varsinxs);
		return new OctagonRel(subdbm, newvarsorig);
	}
	
	public Relation[] minPartition() {
		
		Partition<Integer, Integer> vps = new Partition<Integer, Integer>(); // second type is just dummy (won't be used)
		
		DBM[] retdbms = dbm.minPartition(vps);
		
		if (vps.size() == 0) 
			return new Relation[] {DBRel.giveTautology()};
		
		if (retdbms.length == 1)
			return new Relation[] {this};
		
		Relation[] ret = new Relation[retdbms.length];
		int i = 0;
		for (PartitionMember<Integer, Integer> vp : vps.partitions) {
			Relation sub = this.subOctagonRel(retdbms[i], vp.vars);
			ret[i] = Relation.toMinType(sub);
			i++;
		}
		
		return ret;
	}
	
	public Relation abstractDBRel() {
		return new DBRel(dbm.octMat2DBCs(varsOrig),varsOrig);
	}
	public Relation abstractOct() {
		return this;
	}
	public Relation abstractLin() {
		return this;
	}
	
	public Relation[] domain() {
		return new Relation[] {new OctagonRel(dbm.preimage(),this.varsOrig)};
	}
	public Relation[] range() {
		return new Relation[] {new OctagonRel(dbm.postimage(),this.varsOrig)};		
	}
	
	// returns null iff the weakest nontermination condition is equivalent to false
	public OctagonRel weakestNontermCond() {
		DBM cond_dbm = DeltaClosure.weakestNontermCond(this.dbm);
		
		if (cond_dbm == null) {
			return null;
		} else {
			cond_dbm.canonize();
			return new OctagonRel(cond_dbm, this.varsOrig);
		}
	}
	
	public Relation[] substitute(Substitution s) {
		return Relation.substitute(this, s);
	}

	public Substitution equality_subst() {
		return dbm.equality_subst_oct(this.varsOrig);
	}
}
