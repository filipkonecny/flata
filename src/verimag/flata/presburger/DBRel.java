package verimag.flata.presburger;

import java.util.*;

import verimag.flata.acceleration.delta.DeltaClosure;
import verimag.flata.common.Answer;
import verimag.flata.common.CR;
import verimag.flata.common.IndentedWriter;
import verimag.flata.presburger.DBM.DetRes;

public class DBRel extends Relation implements DBOct {

	public static boolean TOLIN_COMPACT = true;
	
	public RelType getType() {
		return Relation.RelType.DBREL;
	}

	private boolean inconsistentRel = false;
	public static DBRel inconsistentRel() {
		DBRel r = new DBRel(new Variable[0]);
		r.dbm.mat().set(0, 0, fs.giveField(-99));
		r.inconsistentRel = true;
		return r;
	}
	
	public static boolean CHECK_COMPACTNESS_ALGO = true;
	
	/**
	 * Let X={x1,...,xn} be a set of variables and X'={x' | x \in X}. A difference bound relation on the set 
	 * of variables X \cup X' (i.e., constraints of the form x-y <= c, x <= c, -x <= c) is represented as 
	 * a DBM (difference bound matrix). Constraints of the form x <= c and -x <= c are encoded with the help
	 * of a $zero variable (the constraint $zero'=$zero is always present in the matrix). 
	 * Hence, rows and columns correspond to [$zero,x1,...,xn,$zero',x1',...,xn']
	 */

	public static FieldStaticInf fs = IntegerInfStatic.fs();
	
	//public static Variable zero = VariablePool.createSpecial("$");
	//public static Variable zeroP = VariablePool.createSpecial("$'");
	
	private DBM dbm;
	public DBM dbm() { return dbm; }
	
	private Variable[] varsOrig;

	
	public Variable[] vars() {
		return varsOrig; 
	}
	
	@Override
	// considers zero and zero' too
	public DBOct.II iii(Variable[] v1, Variable[] v2) {
		int[] ret1 = new int[v1.length+2];
		int[] ret2 = new int[v2.length+2];
		
		ret1[0] = 0; // zero
		ret2[0] = 0; // zero
		
		List<Variable> vars = new LinkedList<Variable>();
		
		// treat unprimed first

		int l1 = v1.length / 2;
		int l2 = v2.length / 2;
		
		int i1 = 1;
		int i2 = 1;
		int ii = 1; // merged relation counter
		while (i1 <= l1 && i2 <= l2) {
			int c = v1[i1-1].compareTo(v2[i2-1]);
			
			if (c <= 0) {
				vars.add(v1[i1-1]);
			} else {
				vars.add(v2[i2-1]);
			}
			
			if (c <= 0) {
				ret1[i1++] = ii;
			}
			if (c >= 0) {
				ret2[i2++] = ii;
			}
			ii++;
		}
		while (i1 <= l1) {
			vars.add(v1[i1-1]);
			ret1[i1++] = ii++;
		}
		while (i2 <= l2) {
			vars.add(v2[i2-1]);
			ret2[i2++] = ii++;
		}
		
		// now, ii = number of unprimed variables (including zero) in the merged relation
		
		// next, treat primed
		
		DBOct.II.copyPrimedPart(l1+1, ii, ret1);
		DBOct.II.copyPrimedPart(l2+1, ii, ret2);
		
		return new DBOct.II(ret1, ret2, 2 * ii, Relation.inferCounterparts(vars));
	}	

	
	
	public static void throwNotDBM() {
		throw new RuntimeException("DBC expected");
	}
	
	public static DBRel giveTautology() {
		return new DBRel(new Variable[0]);
	}
	
	private DBRel(DBM aDBM, Variable[] aVarsOrig) {
		dbm = aDBM;
		varsOrig = aVarsOrig;
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
	
	
	private DBRel(Variable[] vars_unp) {
		Matrix m = createDBRel_base(this,Variable.refVarsUnpPSorted(vars_unp));
		
		int nVars = m.size() / 2;
		for (int i=1; i<=nVars-1; i++) { // range over x_1...x_n (ignore zero)
			int inx1 = i;
			int inx2 = i+nVars;
			m.set(inx1, inx2, fs.giveField(0));
			m.set(inx2, inx1, fs.giveField(0));
		}
		
		dbm = new DBM(verimag.flata.presburger.DBM.Encoding.DBC, m, fs);
	}
	
	public DBRel(DBC[] dbConstrs, Variable[] vars_unpP) {
		createDBRel(this, dbConstrs,vars_unpP);
		
		this.isIdentity = dbm.checkSetIdentity();
	}
	
	public static DBRel createTautology() {
		return new DBRel(new DBC[] {}, new Variable[] {});
	}
	
	private static void createDBRel(DBRel dbRel, DBC[] dbConstrs, Variable[] vars_unpP) {
		
		Matrix m = createDBRel_base(dbRel, vars_unpP);
		
		int nVar = vars_unpP.length / 2 + 1; // including zero
		
		Map<Variable, Integer> back_link = new HashMap<Variable, Integer>();

		int l = vars_unpP.length / 2;
		for (int i=0; i<l; ++i) {
			back_link.put(vars_unpP[i], new Integer(i+1));
			back_link.put(vars_unpP[i+nVar-1], new Integer(i+1+nVar));
		}
		
		for (DBC dbc : dbConstrs) {
			
			int row = (dbc.plus() == null)? (dbc.minus().isPrimed()? 0 : nVar) : back_link.get(dbc.plus()).intValue();
			int col = (dbc.minus() == null)? (dbc.plus().isPrimed()? 0 : nVar) : back_link.get(dbc.minus()).intValue();
			
			Field f = m.get(row, col);
			
			if (f == null) {
				m.set(row, col, dbc.label());
			} else {
				m.set(row, col, f.min(dbc.label()));
			}
			
		}
		
		dbRel.dbm = new DBM(verimag.flata.presburger.DBM.Encoding.DBC, m, fs);
		
		if (CANONIZE_DB_OCT)
			dbRel.dbm.canonize();
	}
	private static Matrix createDBRel_base(DBRel dbRel, Variable[] vars_unpP) {
		
		dbRel.varsOrig = vars_unpP;
		
		int nVar = (vars_unpP.length/2)+1; // variables in the formula plus $zero variable 
		int dim = 2*nVar;
		
		Matrix m = DBM.newMatrix(dim, fs);
		
		m.init();
		
		m.set(0, nVar, fs.zero());
		m.set(nVar, 0, fs.zero());
		
		return m;
	}
	
	public DBRel(int vars, float density, int minval, int maxval) {
		createRandom(this, vars, density, minval, maxval);
	}
	
	public static boolean RAND_IS_CANONIC = true;
	public static boolean RAND_DETERMINISTIC_ACTION = false;
	// returns a relation whose matrix (after canonization) has approximately required density
	public static void createRandom(DBRel dbRel, int vars_unp, double density, int minval, int maxval) {
		
		int nVar = vars_unp+1; // variables in the formula plus $zero variable 
		int dim = 2*nVar;
		
		dbRel.varsOrig = new Variable[vars_unp*2];
		
		for (int i=0; i<vars_unp; ++i) {
			
			int inxUnp = i+1;
			int inxP = inxUnp + nVar;
			
			Variable varUnp = VariablePool.createSpecial("x"+i);
			Variable varP = varUnp.getPrimedVar();
			
			dbRel.varsOrig[inxUnp-1] = varUnp;
			dbRel.varsOrig[inxP-2] = varP;
		}
		
		Matrix m = DBM.newMatrix(dim, fs);
		
		m.init();
		m.set(0, nVar, fs.zero());
		m.set(nVar, 0, fs.zero());
		
		dbRel.dbm = new DBM(DBM.Encoding.DBC, m, fs);
		
		Random rand = new Random();
		boolean ok = false;
		boolean exceeded = false;
		boolean inconsistent = false;
		
		double RAND_TOLER = (density > 0.15)? 0.03 : 0.015;
		
		while (!ok && !exceeded && !inconsistent) {
			//  A B
			//  C D
			int j; // increase probablity that we hit A when constructing deterministic relations
			if (RAND_DETERMINISTIC_ACTION && rand.nextDouble()<0.3)
				j = rand.nextInt(nVar);
			else
				j = rand.nextInt(dim);
			
			int i;
			if (RAND_DETERMINISTIC_ACTION)
				i = rand.nextInt(nVar);
			else
				i = rand.nextInt(dim);
				
			
			if (i==j)
				continue;
			if ((i==0 && j==nVar) || (j==0 && i==nVar))
				continue;
			
			if (m.get(i, j).isFinite())
				continue;					
			
			if (RAND_DETERMINISTIC_ACTION && i>=nVar && j>=nVar)
				continue;
			
			int n = rand.nextInt(maxval-minval) + minval;
			
			if (RAND_DETERMINISTIC_ACTION && ((i<nVar && j>=nVar) || (j<nVar && i>=nVar))) {

				boolean skip = false;
				// prevent e.g. x'=y /\ x'=z
				int k = (i<nVar && j>=nVar)? j : i;
				for (int l=0; l<nVar; l++)
					if (m.get(l, k).isFinite()) {
						skip = true;
						break;
					}
				
				if (skip)
					continue;
				
				m.set(i, j, new IntegerInf(n));
				m.set(j, i, new IntegerInf(-n));
			} else {
				m.set(i, j, new IntegerInf(n));
			}
			
			float dens;
			if (RAND_IS_CANONIC) {
				dbRel.dbm.canonize();
				if (dbRel.contradictory()) {
					inconsistent = true;
					break;
				}
					
				dens = m.density();
			} else {
				DBM copy = new DBM(dbRel.dbm);
				copy.canonize();
				if (!copy.isConsistent()) {
					inconsistent = true;
					break;
				}
				dens = copy.density();
			}
			
			if (density-RAND_TOLER < dens && density+RAND_TOLER > dens) {
				ok = true;
			} else if (density+RAND_TOLER <= dens) {
				exceeded = true;
			}
		}
		
		if (exceeded || inconsistent) {
			m.set(0, 0, IntegerInf.giveField(-1));
		}
				
		if (CANONIZE_DB_OCT)
			dbRel.dbm.canonize();
	}
	
	public Answer relEquals(Relation otherRel) {
		if (!(otherRel instanceof DBRel)) {
			
			// supposing canonical representation of relations is used
			return Answer.FALSE;
		} else {
			DBRel other = (DBRel)otherRel;
			
			return Answer.createAnswer(this.dbm.equalsSem(other.dbm));
		}
	}
	public Answer includes(Relation otherRel) {
		if (!(otherRel instanceof DBRel)) {
			return Relation.includes(this, otherRel);
		} else {
			DBRel other = (DBRel)otherRel;
			
			if (this.isIdentity && other.isIdentity) {
				return Answer.TRUE;
			}
			
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
		if (!(otherRel instanceof DBRel)) {
			return Relation.intersect(this, otherRel);
		} else {
			DBRel other = (DBRel)otherRel;
			
			DBOct.II ii = iii(this.varsOrig, other.varsOrig);
			
			DBM inters = dbm.intersect(other.dbm, ii);
			
			if (inters.isConsistent()) {
				
				DBRel rel = new DBRel(inters, ii.vars);
				rel.isIdentity = this.isIdentity && other.isIdentity;
				return new Relation[] { Relation.toMinType(rel) };
				
			} else {
				
				return new Relation[0];
			}
		}
	}
	

	private DBRel projectUnconstrained() {
		Integer[] inxsUnp = dbm.unconstrainedVars();
		
		if (inxsUnp.length*2 != this.varsOrig.length) {
			DBM dbm_proj = dbm.projectUnconstrained_internal(inxsUnp);
			DBRel ret = new DBRel(dbm_proj, Variable.projectVars(varsOrig,inxsUnp));
			return ret;
		} else {
			return this;
		}
	}
	
	// set the row and column corresponding to the variable to infinity 
	// (only the diagonal remains equal to 0)
	public Relation[] existElim1(Variable aVar) {
		DBRel ret = this.copy();
		
//		int l = varsOrig.length / 2;
		int inx = Arrays.binarySearch(varsOrig, aVar);
		
		ret.dbm.removeConstraints(inx);
		
//		inx += (inx<l)? 1 : 2;
//		Matrix m = ret.dbm.mat();
//		m.resetRowCol(inx);
//		
//		// if the counterpart of v is unconstrained, eliminate both v and v'
//		int n = l+1;
//		int inx2 = (inx < n)? inx+n : inx-n;
//		if (ret.dbm.isColRowUnconstrained(inx2)) {
//			return existElim2(aVar);
//		}
		
		ret = ret.projectUnconstrained();
		
		// some constraint on the counterpart is present, hence the relation does not become tautology
		// !! must not minimize, the output is disjunctive !!
		//return ret.minPartition(); // minimize -- e.g. exists z' .  x-z'<=5 /\ y-z'<=5 <--> true
		//return new Relation[] {Relation.toMinType(ret)}; // minimize -- e.g. exists z' .  x-z'<=5 /\ y-z'<=5 <--> true
		return new Relation[] { ret };
	}
	
	// projection (eliminates the variable and its counterpart)
	public Relation[] existElim2(Variable aVar) {
		DBRel ret = this.copy();
		
		int inx1 = Arrays.binarySearch(varsOrig, aVar);
		
		int n = varsOrig.length / 2;
		int inx2;
		if (inx1<n) {
			inx2 = inx1 + n;
		} else {
			inx2 = inx1;
			inx1 = inx2 - n;
		}
		// inx1 -- unprimed
		// inx2 -- primed
		
		ret.dbm.removeConstraints(inx1);
		ret.dbm.removeConstraints(inx2);
		
		ret = ret.projectUnconstrained();
		
//		int[] inxs = new int[nn]; // projected indices
//		Variable[] vo = new Variable[nn-2]; // projected variables
//		
//		inxs[0] = 0; // zero
//		inxs[n] = n+1; // zero'
//		
//		int aa = 0;
//		for (int i=0; i<n; i++) {
//			if (i != inx1) {
//				vo[aa] = varsOrig[i];
//				inxs[aa+1] = i+1;
//				aa++;
//			}
//		}
//		// now, aa == n-1
//		for (int i=n; i<nn; i++) {
//			if (i != inx2) {
//				vo[aa] = varsOrig[i];
//				inxs[aa+2] = i+2;
//				aa++;
//			}
//		}
//		
//		DBM dbm_proj = this.dbm.subDBM(inxs);
//		
//		DBRel ret = new DBRel(dbm_proj, vo);
		
		// !! must not minimize, the output is disjunctive !!
		//return ret.minPartition();  // minimize -- e.g. exists z' .  x-z'<=5 /\ y-z'<=5 <--> true
		return new Relation[] { ret }; 
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
	
	public Relation[] compose(Relation otherRel) {
		if (!(otherRel instanceof DBRel)) {
			return Relation.compose(this, otherRel);
		} else {
			
			DBRel other = (DBRel)otherRel;
			
			if ( (this.isIdentity() || other.isIdentity()) && Arrays.equals(this.varsOrig, other.varsOrig) ) {
				
				if (this.isIdentity() && other.isIdentity()) {
					Relation r = other.copy();
					r.isIdentity = true;
					return new Relation[]{r};
				} else if (this.isIdentity()) {
					return new Relation[]{other.copy()};
				} else if (other.isIdentity()) {
					return new Relation[]{this.copy()};
				}
			}
			
			DBOctCompatibility<DBRel> c = new DBOctCompatibility<DBRel>(this, other);
			c.extend();
			
			List<Field> diagonal = new LinkedList<Field>();
			
			DBM comp = c.first.compose(c.second, diagonal);
			
			if (IntegerInf.hasNonNegative(diagonal)) {
				
				DBRel rel = new DBRel(comp, c.ii.vars);
				return new Relation[] {rel};
			} else {

				return new Relation[0];
			}
		}
	}
	
	public LinearTerm[] substitution() {
		int nn = varsOrig.length;
		int n = nn / 2;
		LinearTerm[] substitution = new LinearTerm[nn+2];
		
		substitution[0] = new LinearTerm(null,0); // zero 
		substitution[n+1] = new LinearTerm(null,0); // zero'
		
		for (int i=0; i<n; i++) {
			substitution[i+1] = new LinearTerm(varsOrig[i],1);
			substitution[i+n+2] = new LinearTerm(varsOrig[i+n],1);
		}
		
		return substitution;
	}
	
	public Relation[] closureMaybeStar() {
		return Relation.AccelerationComp.closure(dbm, false, substitution(), varsOrig);
	}
	public Relation[] closurePlus() {
		return Relation.AccelerationComp.closurePlus(dbm, false, substitution(), varsOrig);
	}
	public ClosureDetail closure_detail() {
		return Relation.AccelerationComp.closure_detail(dbm, false, substitution(), varsOrig);
	}
	public ClosureDetail closurePlus_detail() {
		return Relation.AccelerationComp.closurePlus_detail(dbm, false, substitution(), varsOrig);
	}
	
	public boolean isDBRel() { return true; }
	public boolean isOctagon() { return true; }
	public boolean isLinear() { return true; }
	public boolean isModulo() { return true; }
	
	public DBRel toDBRel() {
		return this.copy();
	}
	public OctagonRel toOctagonRel() {
		
		return new OctagonRel(dbm.dbMat2OctConstrs(varsOrig), varsOrig);
	}
	
	public LinearRel toLinearRel() {
		LinearRel linRel = new LinearRel();
		if (TOLIN_COMPACT) {
			DBRel compact = DBRel.compact(this);
			linRel.addAll(compact.dbm.dbMat2LinConstrs(varsOrig)); // TODO: problems in LinearRel - variables, ...
		} else {
			linRel.addAll(dbm.dbMat2LinConstrs(varsOrig));
		}
		
		return linRel.asCompact();
		//return linRel;
	}
	public ModuloRel toModuloRel() {
		ModuloRel modRel = new ModuloRel();
		modRel.linConstrs(toLinearRel());
		return modRel;
	}
	
	public StringBuffer toStringBuf() {
		if (this.inconsistentRel) {
			return new StringBuffer("INCONS");
		} else if (isIdentity) {
			StringBuffer sb = new StringBuffer();
			int l = varsOrig.length / 2;
			if (l>0) {
				sb.append(varsOrig[l].toString()+"="+varsOrig[0].toString());
				for (int i = 1; i < l; i ++)
					sb.append(","+varsOrig[i+l].toString()+"="+varsOrig[i].toString());
			}
			return sb;
		} else {
			DBRel compact = DBRel.compact(this);
			return compact.dbm.toStringBuf_dbc_compact(varsOrig);
			//return dbm.toStringBuf_dbc();
		}
	}
//	public String toString_notCompact() {
//		return this.dbm.toStringBuf_dbc(varsOrig).toString();
//	}
	public String toString() {
		return toStringBuf().toString();
	}
	
	public void toSBYicesAsConj(IndentedWriter aIW, String suf_unp, String suf_p) {
		CR.yicesAndStart(aIW);
		dbm.toStringBufYicesList_dbc(aIW, suf_unp, suf_p, false, varsOrig);
		CR.yicesAndEnd(aIW);
	}
	public void toSBYicesAsConj(IndentedWriter aIW) {
		toSBYicesAsConj(aIW, null, null);
	}
	public void toSBYicesList(IndentedWriter iw, boolean negate) {
		dbm.toStringBufYicesList_dbc(iw, null, null, negate, varsOrig);
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
		return dbm.isFASTCompatible_dbm();
	}
	public boolean isARMCCompatible() {
		return true;
	}
	
	public DBRel copy() {
		DBM m_copy = new DBM(this.dbm);
		return new DBRel(m_copy,varsOrig);
	}

	public DBRel copy(Rename aRenVals, VariablePool aVP) {
		DBRel ret = this.copy();
		ret.rename(aRenVals, aVP);
		return ret;
	}

	public static DBRel createIdentityRelation(Variable[] vars_unp) {
		DBRel ret = new DBRel(vars_unp);
		ret.isIdentity = true;
		return ret;
	}
	
	public static Variable[] implActOrigVar(Variable[] allUnprimed) {
		int l = allUnprimed.length;
		Variable[] newvars = new Variable[2*l];
		System.arraycopy(allUnprimed, 0, newvars, 0, l);
		Arrays.sort(newvars, 0, l);
		for (int i=0; i<l; i++)
			newvars[i+l] = newvars[i].getCounterpart();
		return newvars;
	}
	
	public void addImplicitActions(Collection<Variable> aRestriction) {
		
		boolean[] restr = new boolean[varsOrig.length];
		for (int i = 0; i<varsOrig.length; i++) {
			if (aRestriction.contains(varsOrig[i])) {
				restr[i] = true;
			}
		}
		
		this.dbm.addImplicitActions(restr);
		
		if (!RAND_DETERMINISTIC_ACTION) // small hack
			this.dbm.canonize();
	}
	
	public DBRel asCompact() {
		return DBRel.compact(this);
	}
	
	private static DBM compactBase(DBM aDBM) {
		DBM compact = aDBM.copy();
		
		// compact
		compact = compact.compact_dbc();
		
		// check correctness
		if (!CHECK_COMPACTNESS_ALGO) {
			return compact;
		} else {
			DBM compact_cp = new DBM(compact);
			compact_cp.canonize();
			DBM cp_aDBM = aDBM.copy();
			cp_aDBM.canonize();
			if (compact_cp.equals(cp_aDBM)) {
				return compact;
			} else {
				//DBM tmp = aDBM.copy();
				//tmp.compact_dbc();
				//System.err.println("DB formula "+aDBRel.toLinearRel()+"\ncompacted to "+tmp.toLinearRel());
				//System.err.println("DB formula "+aDBRel.toString_notCompact()+"\ncompacted to "+tmp.toString_notCompact());
				throw new RuntimeException();
			}
		}

	}
	public static DBRel compact(DBRel aDBRel) {
		return compact_old(aDBRel);
	}
	public static DBRel compact_with_det(DBRel aDBRel) {
		
		if (!RAND_IS_CANONIC)
			return aDBRel;
		
		DBRel ret = aDBRel.copy();
		
		// try to determinize first
		
		DetRes aux = ret.dbm.try_determinize_dbm(false);
		
		if (aux.isEachVarHavocOrDetFromCore()) {
			DBM det = aux.det;
			
			if (CHECK_COMPACTNESS_ALGO) {
				DBM aux2 = new DBM(det);
				aux2.canonize();
				//DBM.checkEquiv(aux2, ret.dbm);
				if (!aux2.equals(ret.dbm)) {
					ret.dbm.try_determinize_dbm(false);
					throw new RuntimeException("internal error");
				}
			}
			
			DBM compact_guard = DBRel.compactBase(aux.guard);
			DBM compact_guard_prime = DBRel.compactBase(aux.guard_prime);
			
			ret.dbm = compact_guard.intersect_not_canonize(compact_guard_prime).intersect_not_canonize(aux.update);
			
		} else {
			
			ret.dbm = compactBase(aDBRel.dbm);
			
		}
		
		return ret;
		
	}
	public static DBRel compact_old(DBRel aDBRel) {
		
		DBRel ret = aDBRel.copy();
		ret.dbm = aDBRel.dbm.compact_dbc();
		
		// check correctness
		if (!CHECK_COMPACTNESS_ALGO) {
			return ret;
		} else {
			DBM compact_cp = new DBM(ret.dbm);
			compact_cp.canonize();
			if (compact_cp.equals(aDBRel.dbm)) {
				return ret;
			} else {
				throw new RuntimeException();
			}
		}
		
	}
	
	public DetUpdateAndGuards deterministicUpdate() {
		DetRes aux = dbm.try_determinize_dbm(false);
		switch (aux.type) {
		case HAVOC_OR_DET_FROM_CORE:
			return new DetUpdateAndGuards(
					new DBRel(aux.guard, this.varsOrig),
					new DBRel(aux.update, this.varsOrig),
					new DBRel(aux.guard_prime, this.varsOrig));
		case EACH_VAR_DET:
			return new DetUpdateAndGuards(
					new DBRel(aux.guard, this.varsOrig),
					new DBRel(aux.update, this.varsOrig),
					DBRel.createTautology());
		default:
			return null;
		}
	}
	
	public FiniteVarIntervals findIntervals() {
		return dbm.findIntervals_dbc(varsOrig);
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
			int matInx = (cp.v.isPrimed())? origInx+2 : origInx+1;
			Field f1 = fs.giveField(cp.c);
			Field f2 = fs.giveField(-cp.c);
			m.setMin(matInx, 0, f1);
			m.setMin(0, matInx, f2);
		}
		
		if (cps.size() != 0)
			this.isIdentity = false;
		
		if (CANONIZE_DB_OCT)
			dbm.canonize();
	}
	
	public Collection<Variable> identVars() {
		return dbm.identVars(this.varsOrig);
	}
		
	public boolean canPreciseMerge(DBRel other) {
		
		DBOctCompatibility<DBRel> c = new DBOctCompatibility<DBRel>(this, other);
		c.extend();
		
		return c.first.canPreciseMerge(c.second);
	}
	public DBRel hull(DBRel other) {
		
		DBOct.II ii = iii(this.varsOrig, other.varsOrig);
		
		return new DBRel(dbm.hull(other.dbm, ii),ii.vars);
	}
	public Relation merge(Relation otherRel) {
		if (!(otherRel instanceof DBRel))
			return null;
		
		if (!Arrays.equals(this.varsOrig, ((DBRel)otherRel).varsOrig))
			return null;
		
		Relation ret = this.syntaxMerge(otherRel);
		
		if (ret != null)
			return ret;
		
		DBRel other = (DBRel)otherRel;
		if (this.canPreciseMerge(other)) {
			return this.hull(other);
		} else
			return null;
	}

	
	// merge iff
	//   1) DB1: x-y <= c1 /\ y-x <= inf
	//   2) DB2: x-y >= -c2 /\ x-y <= inf
	//   3) -c2 <= c1
	// in other words: (difference of set of constraints in the following)
	//   DB1 \ DB2 is x-y<=c1
	//   DB2 \ DB1 is x-y>=-c2
	private Relation syntaxMerge(Relation otherRel) {
		if (!(otherRel instanceof DBRel))
			return null;
		
		DBRel other = (DBRel) otherRel;
		
		Matrix m1 = this.dbm.mat();
		Matrix m2 = other.dbm.mat();
		
		int ii = -1;
		int jj = -1;
		
		for (int i=0; i<m1.size(); i++) {
			for (int j=0; j<m1.size(); j++) {
				if (i == j)
					continue;
				
				if (i == jj && j == ii)
					continue;
				
				Field f1 = m1.get(i, j);
				Field f2 = m2.get(i, j);
				
				if (f1.equals(f2))
					continue;
				
				if (f1.isFinite() && f2.isFinite()) // not equal and finite
					return null;
				
				// f1.finite xor f2.finite holds now
				
				if (ii>=0)
					return null;
				
				Field ff1 = m1.get(j, i);
				Field ff2 = m2.get(j, i);
				
				boolean b1 = ff1.isFinite();
				boolean b2 = ff2.isFinite();
				
				if ((b1 && b2) || (!b1 && !b2))
					return null;
				
				// ff1.finite xor ff2.finite holds now
				
				int c1;
				int c2;
				
				if (f1.isFinite()) {
					
					if (b1 || !b2)
						return null;
					
					c1 = f1.toInt();
					c2 = ff2.toInt();
				} else {
					
					if (!b1 || b2)
						return null;
					
					c2 = f2.toInt();
					c1 = ff1.toInt();
				}
				
				if (c1 < -c2)
					return null;
				
				ii = i;
				jj = j;
			}
		}

		if (ii < 0)
			return this;
		
		DBRel ret = this.copy();
		Matrix retMat = ret.dbm.mat();
		retMat.set(ii, jj, IntegerInf.posInf());
		retMat.set(jj, ii, IntegerInf.posInf());
		return ret;
	}

	public DBRel subDBRel(DBM subdbm, Set<Integer> varsinxs) {
		Variable[] newvarsorig = Variable.sortedSubset(this.varsOrig, varsinxs);
		return new DBRel(subdbm, newvarsorig);
	}
	
	public Relation[] minPartition() {
		
		if (varsOrig.length <= 1)
			return new Relation[] {this};
		
		Partition<Integer, Integer> vps = new Partition<Integer, Integer>(); // second type is just dummy (won't be used)
		
		DBM[] retdbms = dbm.minPartition(vps);
		
		if (vps.size() == 0) 
			return new Relation[] {DBRel.giveTautology()};
		
		if (retdbms.length == 1)
			return new Relation[] {this};
		
		Relation[] ret = new Relation[retdbms.length];
		int i = 0;
		for (PartitionMember<Integer, Integer> vp : vps.partitions) {
			ret[i] = this.subDBRel(retdbms[i], vp.vars);
			i++;
		}
		
		return ret;
	}
	
	public Relation abstractDBRel() {
		return this;
	}
	public Relation abstractOct() {
		return this;
	}
	public Relation abstractLin() {
		return this;
	}

	public Distance distance(DBRel other) {
		return this.dbm.distance_dbc(other.dbm);
	}
	
	public Relation[] domain() {
		return new Relation[] {new DBRel(dbm.preimage(),this.varsOrig)};
	}
	public Relation[] range() {
		return new Relation[] {new DBRel(dbm.postimage(),this.varsOrig)};		
	}
	
	public boolean alwaysTerminates() {
		return DeltaClosure.alwaysTerminates(this.dbm);
	}

	// returns null iff the weakest nontermination condition is equivalent to false
	public DBRel weakestNontermCond() {
		DBM cond_dbm = DeltaClosure.weakestNontermCond(this.dbm);
		
		if (cond_dbm == null) {
			return null;
		} else {
			cond_dbm.canonize();
			return new DBRel(cond_dbm, this.varsOrig);
		}
	}
	
	public Relation[] substitute(Substitution s) {
		return Relation.substitute(this, s);
	}
}
