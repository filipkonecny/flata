package verimag.flata.presburger;

import java.util.*;
import java.util.function.BiFunction;

import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

import verimag.flata.common.CR;
import verimag.flata.common.FlataJavaSMT;
import verimag.flata.common.IndentedWriter;

public class DBM {
	
	private Matrix mat;

	public Matrix mat() {
		return mat;
	}

	private Encoding encoding;

	public Encoding encoding() {
		return encoding;
	}

	public enum Encoding {
		DBC, OCT;

		public boolean isDBC() {
			return this == DBC;
		}

		public boolean isOct() {
			return this == OCT;
		}
	}

	private FieldStaticInf fs = IntegerInfStatic.fs();
	public FieldStaticInf fs() { return fs; }

	// public static Field posInf = new IntegerInf(FieldType.POS_INF);

	public DBM copy() {
		return new DBM(this);
	}
	public DBM(DBM other) {
		mat = new Matrix(other.mat);
		encoding = other.encoding;
		fs = other.fs;
	}
	
	public DBM subDBM(int[] inxs) {
		Matrix submat = this.mat.submatrix(inxs);
		DBM ret = new DBM(this.encoding, submat, this.fs);
		return ret;
	}
	
	public DBM extend(int size, int[] inxs) {
		DBM ret = new DBM(encoding, size, fs);
		Matrix m = ret.mat;
		
		m.init();
		m.copyFrom(mat, inxs);
		
		return ret;
	}


	public boolean equals(Object otherObj) {
		return (otherObj instanceof DBM) && (mat.equals(((DBM) otherObj).mat));
	}

	public String toString() {
		return mat.toString();
	}

	public static Matrix newMatrix(int dim, FieldStatic aFS) {
		return new Matrix(dim, aFS);
	}

	private DBM(Encoding aEncoding, int dim, FieldStaticInf aFS) {
		this(aEncoding, aFS);
		mat = newMatrix(dim, fs);
	}

	public DBM(Encoding aEncoding, Matrix aMat, FieldStaticInf aFS) {
		this(aEncoding, aFS);
		mat = aMat;
	}

	private DBM(Encoding aEncoding, FieldStaticInf aFS) {
		encoding = aEncoding;
		fs = aFS;
	}

	public DBM times(int c) {
		return new DBM(encoding, mat.times(c), fs);
	}
	public DBM plus(DBM other) {
		return new DBM(encoding, mat.plus(other.mat), fs);
	}

	public DBM minus(DBM other) {
		return new DBM(encoding, mat.minus(other.mat), fs);
	}
	
	public static class Solve_axb {
		private boolean solved;
		private DBM dbm;
		
		public boolean solved() { return solved; }
		public DBM dbm() { return dbm; }
		
		
		private Solve_axb(boolean bb, DBM mm) {
			solved = bb;
			dbm = mm;
		}
		public static Solve_axb noSolution() {
			return new Solve_axb(false, null);
		}
		public static Solve_axb solved(DBM solved) {
			return new Solve_axb(true, solved);
		}
	}
	
	public Solve_axb solve_axb(DBM other) {
		Matrix sol = mat.solve_axb(other.mat);
		if (sol != null) {
			return Solve_axb.solved(new DBM(encoding, sol, fs));
		} else {
			return Solve_axb.noSolution();
		}
	}

	
	public DBM identity() {
		DBM ret = new DBM(this.encoding,this.mat.size(),this.fs);
		ret.mat.init();
		
		int n = ret.mat.size()/2;
		
		for (int i=0; i < n; i++) {
			ret.mat.set(i, i+n, fs.giveField(0));
			ret.mat.set(i+n, i, fs.giveField(0));
			ret.mat.set(i, i, fs.giveField(0));
			ret.mat.set(i+n, i+n, fs.giveField(0));
		}
		return ret;
	}
	
//	private boolean isRowUnconstrained(int r) {
//		IntegerInf inf = IntegerInf.posInf();
//		for (int i = 0; i < mat.size(); i++)
//			if (!mat.get(r, i).equals(inf))
//				return false;
//
//		return true;
//	}
//	private boolean isColUnconstrained(int c) {
//		IntegerInf inf = IntegerInf.posInf();
//		for (int i = 0; i < mat.size(); i++)
//			if (!mat.get(i, c).equals(inf))
//				return false;
//
//		return true;
//	}
	
	// 0..(n-1)
	public void removeConstraints(int varInx) {
		if (encoding.isDBC()) {
			int encInx = (varInx >= mat.size()/2)? varInx+2 : varInx+1;
			mat.resetRowCol(encInx);
		} else {
			mat.resetRowCol(2*varInx);
			mat.resetRowCol(2*varInx+1);
		}
	}

	private boolean isColRowUnconstrained(int c) {
		IntegerInf inf = IntegerInf.posInf();
		int l = mat.size();
		for (int i = 0; i < l; i++) {
			if (i == c)
				continue;
			if (!mat.get(i, c).equals(inf) || !mat.get(c, i).equals(inf))
				return false;
		}

		return true;
	}

	// 0..(n-1)
	private Integer[] unconstrainedVars_dbc() {
		int n = this.mat.size() / 2;
		List<Integer> aux = new LinkedList<Integer>();
		for (int i=1; i<n; i++) {
			if (!isColRowUnconstrained(i) || !isColRowUnconstrained(i+n)) {
				aux.add(i-1);
			}
		}
		return aux.toArray(new Integer[0]);
	}
	// 0..(n-1)
	private DBM projectToVars_dbc(Integer[] varsUnp) {
		int nz = this.mat.size() / 2;
		
		int c = varsUnp.length;
		int cz = c+1;
		int inxs[] = new int[cz*2];
		inxs[0] = 0;
		inxs[cz] = nz;
		int i=0;
		for (Integer inx : varsUnp) {
			inxs[1+i] = 1+inx;
			inxs[1+i+cz] = 1+inx+nz;
			i++;
		}
		
		return this.subDBM(inxs);
	}
	// 0..(n-1)
	private Integer[] unconstrainedVars_oct() {
		int n = this.mat.size() / 4;
		List<Integer> aux = new LinkedList<Integer>();
		for (int i=0; i<n; i++) {
			int ip = 2*i;
			int ipp = 2*(i+n);
			if (!isColRowUnconstrained(ip) 
					|| !isColRowUnconstrained(ip+1)
					|| !isColRowUnconstrained(ipp)
					|| !isColRowUnconstrained(ipp+1)
					) {
				aux.add(i);
			}
		}
		return aux.toArray(new Integer[0]);
	}
	// 0..(n-1)
	private DBM projectToVars_oct(Integer[] varsUnp) {
		int n = this.mat.size() / 4;
		
		int n_n = varsUnp.length;
		int inxs[] = new int[n_n*4];
		int n_i=0;
		for (Integer i : varsUnp) {
			int n_ip = 2*n_i;
			int n_ipp = 2*(n_i+n_n);
			int ip = 2*i;
			int ipp = 2*(i+n);
			inxs[n_ip] = ip;
			inxs[n_ip+1] = ip+1;
			inxs[n_ipp] = ipp;
			inxs[n_ipp+1] = ipp+1;
			n_i++;
		}
		
		return this.subDBM(inxs);
	}
	// returns each variable x such that both x and x' are unconstrained
	public Integer[] unconstrainedVars() {
		if (this.encoding.isDBC()) {
			return unconstrainedVars_dbc();
		} else {
			return unconstrainedVars_oct();
		}
	}
	// vars -- array of indices of unprimed variables
	public DBM projectUnconstrained_internal(Integer[] varsUnp) {
		if (this.encoding.isDBC()) {
			return projectToVars_dbc(varsUnp);
		} else {
			return projectToVars_oct(varsUnp);
		}
	}
	
	// Computes collection of indices I such that
	// dom1[i] \in (dom1 \setminus dom2) for all i \in I.
	// Assumption: domains are sorted.
	public Collection<Integer> domainDiff(String[] dom1, String[] dom2) {
		List<Integer> ret = new LinkedList<Integer>();
		int i1 = 0, i2 = 0;
		while (i1 != dom1.length && i2 != dom2.length) {
			String v1 = dom1[i1];
			int comp = v1.compareTo(dom2[i2]);
			if (comp < 0) {
				ret.add(i1);
				i1++;
			} else if (comp > 0) {
				i2++;
			} else {
				i1++;
				i2++;
			}
		}
		return ret;
	}

//	public static String[] domainMergeVar(String[] dom1, String[] dom2) {
//		List<String> ret = new LinkedList<String>();
//		int i1 = 0, i2 = 0, iN = 0;
//		while (i1 != dom1.length || i2 != dom2.length) {
//			String v1 = null, v2 = null;
//			int comp;
//			if (i1 == dom1.length) {
//				v2 = dom2[i2];
//				comp = 1;
//			} else if (i2 == dom2.length) {
//				v1 = dom1[i1];
//				comp = -1;
//			} else {
//				v1 = dom1[i1];
//				v2 = dom2[i2];
//				comp = v1.compareTo(v2);
//			}
//			
//			if (comp < 0) {
//				ret.add(v1);
//				i1++;
//			} else if (comp > 0) {
//				ret.add(v2);
//				i2++;
//			} else {
//				ret.add(v1);
//				i1++;
//				i2++;
//			}
//			iN++;
//		}
//		return ret.toArray(new String[0]);
//	}
	
	
//	// bitset mask_j is set at positions i such that dom_j[i] \in (dom1 \cup
//	// dom2)
//	private static void intersection(String[] dom1, String[] dom2, BitSet dom1_mask, BitSet dom2_mask) {
//
//		int i1 = 0, i2 = 0;
//		while (i1 != dom1.length && i2 != dom2.length) {
//			String v1 = dom1[i1];
//			int comp = v1.compareTo(dom2[i2]);
//			if (comp < 0) {
//				i1++;
//			} else if (comp > 0) {
//				i2++;
//			} else {
//				dom1_mask.set(i1);
//				dom2_mask.set(i2);
//				i1++;
//				i2++;
//			}
//		}
//	}

	// semantic equivalence of the encoding
	public boolean equalsSem(DBM other) {
		
		return mat.equals(other.mat);
	}
	
	public boolean includes(DBM other, int[] inxs) {
		
		return other.mat.lessEq(mat, inxs, inxs);
	}
	
	public DBM intersect_not_canonize(DBM other) {
		DBM ret = new DBM(this);
		Matrix m = ret.mat;
		
		Matrix m2 = other.mat();
		int l = m.size();
		for (int r=0; r<l; r++) {
			for (int c=0; c<l; c++) {
				Field f = m.get(r, c).min(m2.get(r, c));
				m.set(r, c, f);
			}
		}
		
		return ret;
	}
	public DBM intersect(DBM other) {
		
		DBM ret = this.intersect_not_canonize(other);
		
		// keep canonic form
		ret.canonize();

		return ret;
	}
	public DBM intersect(DBM other, DBOct.II ii) {
		
		DBM ret = new DBM(encoding, ii.mergedSize, fs);
		Matrix m = ret.mat;
		
		m.init();
		m.copyFrom(mat, ii.inx1);
		
		Matrix m2 = other.mat();
		int[] inxs2 = ii.inx2;
		int l = inxs2.length;
		for (int r=0; r<l; r++) {
			for (int c=0; c<l; c++) {
				int rr = inxs2[r];
				int cc = inxs2[c];
				Field f = m.get(rr, cc).min(m2.get(r, c));
				m.set(rr, cc, f);
			}
		}
		
		// keep canonic form
		ret.canonize();

		return ret;
	}
	
	
	private DBM hull_sameDomain(DBM other) {
		// max entry-wise
		Matrix mhull = this.mat.maxEntrywise(other.mat);
		return new DBM(encoding, mhull, fs);
	}
	
	public DBM hull(DBM other, DBOct.II ii) {
		
		DBM ret = new DBM(encoding, ii.mergedSize, fs);
		Matrix m = ret.mat;
		
		m.init();
		m.copyFrom2(mat, ii.inx1);
		
		Matrix m2 = other.mat();
		int[] inxs2 = ii.inx2;
		int l = inxs2.length;
		for (int r=0; r<l; r++) {
			for (int c=0; c<l; c++) {
				int rr = inxs2[r];
				int cc = inxs2[c];
				Field f = m.get(r, c).max(m2.get(rr, cc));
				m.set(r, c, f);
			}
		}
		
		// keep canonic form
		ret.canonize();

		return ret;
		
//		Matrix m1 = this.mat;
//		Matrix m2 = other.mat;
//		if (m1.h() != m2.h() || m1.w() != m2.w() ) {
//			throw new RuntimeException();
//		}
//		
//		Matrix mm = m1.maxEntrywise(m2);
//		
//		DBM ret = new DBM(this.encoding, mm, this.fs);
//		return ret;
	}
	
	
	public static void canonize(Matrix mat, Encoding enc) {
		floydWarshall(mat);
		
		if (enc == Encoding.OCT)
			tighten(mat);
	}

	private static TwoMatrix canonize_param_oct(Matrix mat) {
		
		floydWarshall(mat);
		
		return DBM.tightParam(mat);
	}
	
	
	public void canonize() {
		floydWarshall();

		if (encoding == Encoding.OCT)
			tighten();
	}

	
	private static Matrix compose_internal_init(Matrix m1, Matrix m2) {

		int n = m1.size() / 2; // number of variables
		int dim = 3 * n;

		Matrix m = newMatrix(dim, m1.fs());
		m.init();

		m.copy(0, 0, m1, 0, 0, n, n);
		m.copy(0, n, m1, 0, n, n, n);
		m.copy(n, 0, m1, n, 0, n, n);

		m.copy(n, 2 * n, m2, 0, n, n, n);
		m.copy(2 * n, n, m2, n, 0, n, n);
		m.copy(2 * n, 2 * n, m2, n, n, n, n);

		m.fillMin(n, n, m1, n, n, m2, 0, 0, n, n);
		
		return m;
		
	}
	private static Matrix compose_internal(Matrix m1, Matrix m2, List<Field> diagonal, Encoding enc) {
		int n = m1.size() / 2; // number of variables

		Matrix m = compose_internal_init(m1,m2);

		canonize(m, enc);

		// direct negative cycles
		if (diagonal != null)
			m.storeDiagonal(diagonal);

		m = m.eraseRowsCols(n, n, n, n);

		return m;
	}
	
	// assumption: DBM over same domain (and hence same dimensions)
	public DBM compose(DBM other, List<Field> diagonal) {

		Matrix m_c = DBM.compose_internal(this.mat, other.mat, diagonal, encoding);

		return new DBM(encoding, m_c, fs);
	}

	public static class TwoMatrix {
		public Matrix even;
		public Matrix odd;
		
		public TwoMatrix(Matrix m) {
			int dim = m.size();
			
			even = new Matrix(dim, m.fs());
			odd = new Matrix(dim, m.fs());
			
			even.init();
			odd.init();
		}
	}
	
	// assumption: same domain
	public SplitParamEvenOdd composeParamOct(DBM other) {
		
		//DBM m1 = this.extendBy(other.mat.domain());
		//DBM m2 = other.extendBy(this.mat.domain());

		int n = mat.size() / 2; // number of variables

		Matrix m = compose_internal_init(mat,other.mat);

		TwoMatrix mm = canonize_param_oct(m);

		List<Field> diag_even = mm.even.storeDiagonal();
		List<Field> diag_odd = mm.odd.storeDiagonal();

		Matrix even_final = mm.even.eraseRowsCols(n, n, n, n);
		Matrix odd_final = mm.odd.eraseRowsCols(n, n, n, n);

		SplitParamEvenOdd ret = new SplitParamEvenOdd(this, even_final, odd_final, diag_even, diag_odd);
		
		return ret;
	}
	

	public void floydWarshall() {
		floydWarshall(mat);
	}

	public static void floydWarshall(Matrix mat) {

		// if (w != h)
		// throw new RuntimeException("Square matrix expected");

		int size = mat.size();
		for (int k = 0; k < size; ++k) {
			for (int i = 0; i < size; ++i) {
				
				if (i == k)
					continue;
				
				Field ik = mat.get(i, k);
				if (!ik.isFinite())
					continue;
				
				for (int j = 0; j < size; ++j) {
					if (k == j)
						continue;
					// we need to compute diagonal
					//if (i == j)
					//	continue;
					
					Field kj = mat.get(k, j);
					if (!kj.isFinite())
						continue;
					
					Field ij = mat.get(i, j);
					Field min = ij.min(ik.plus(kj));
					mat.set(i, j, min);
				}
			}
			
//			if (mat.get(0, 0) instanceof ParamBounds) {
//				System.out.println("k="+k+"\n"+mat);
//			}
		}
	}

	public void tighten() {
		tighten(mat);
	}

	public static class SplitParamEvenOdd {
		public DBM even;
		public DBM odd;
		
		public List<Field> diagonal_even;
		public List<Field> diagonal_odd;
		
		public SplitParamEvenOdd(DBM dbm) {
			Encoding e = dbm.encoding;
			int l = dbm.mat.size();
			FieldStaticInf fs = dbm.fs;
			
			even = new DBM(e,l,fs);
			odd = new DBM(e,l,fs);
		}
		public SplitParamEvenOdd(DBM dbm, Matrix m_even, Matrix m_odd) {
			this(dbm, m_even, m_odd, null, null);
		}
		public SplitParamEvenOdd(DBM dbm, Matrix m_even, Matrix m_odd, List<Field> diag_even, List<Field> diag_odd) {
			even = new DBM(dbm.encoding, m_even, dbm.fs);
			odd = new DBM(dbm.encoding, m_odd, dbm.fs);
			diagonal_even = diag_even;
			diagonal_odd = diag_odd;
		}
	}
	
	private static void splitEvenOdd_floorDiv2(ParamBounds orig, ParamBounds even, ParamBounds odd) {
		for (ParamBound paramBound : orig.paramBounds()) {
			int a = paramBound.paramCoef();
			int b = paramBound.intVal();
			ParamBound evenBound = new ParamBound(CR.floor(b,2), a);
			ParamBound oddBound = new ParamBound(CR.floor(b+a,2), a); // !!
			even.addBound(evenBound);
			odd.addBound(oddBound);
		}
	}
	private static void splitEvenOdd(ParamBounds orig, ParamBounds even, ParamBounds odd) {
		for (ParamBound paramBound : orig.paramBounds()) {
			int a = paramBound.paramCoef();
			int b = paramBound.intVal();
			
			ParamBound evenBound = new ParamBound(b, 2*a);
			ParamBound oddBound = new ParamBound(b+a, 2*a);  // !!
			
			even.addBound(evenBound);
			odd.addBound(oddBound);
		}
	}
	private static void sum(ParamBounds pbs1, ParamBounds pbs2, ParamBounds res) {
		for (ParamBound pb1 : pbs1.paramBounds())
			for (ParamBound pb2 : pbs2.paramBounds())
				res.addBound(pb1.plus(pb2));
	}
	
	public static SplitParamEvenOdd splitParamDBM(DBM dbm) {
		TwoMatrix mm = splitParamMatEvenOdd(dbm.mat);
		return new SplitParamEvenOdd(dbm, mm.even, mm.odd);
	}
	
	public static TwoMatrix splitParamMatEvenOdd(Matrix mat) {
		
		TwoMatrix ret = new TwoMatrix(mat);
		
		Matrix mat_even = ret.even;
		Matrix mat_odd = ret.odd;
		int nVars = mat.size();
		
		// split existing
		for (int i = 0; i < nVars; i++) {
			for (int j = 2*(i/2); j < nVars; j++) {
				
				ParamBounds ij = (ParamBounds) mat.get(i,j);
				if (!ij.isFinite())
					continue;
				
				ParamBounds evenBounds = ParamBounds.posInf();
				ParamBounds oddBounds = ParamBounds.posInf();
				
				splitEvenOdd(ij, evenBounds, oddBounds);
				
				mat_even.set(i, j, evenBounds);
				mat_odd.set(i, j, oddBounds);
				
				// make coherent
				int bi = OctagonRel.bar(i);
				int bj = OctagonRel.bar(j);
				mat_even.set(bj, bi, evenBounds); // don't copy, fields are not expected to change later
				mat_odd.set(bj, bi, oddBounds);
			}
		}
		
		return ret;
	}
	
	public static TwoMatrix tightParam(Matrix mat) {
		
		// split existing
		TwoMatrix ret = splitParamMatEvenOdd(mat);
		
		Matrix mat_even = ret.even;
		Matrix mat_odd = ret.odd;
		int nVars = mat.size();

		// tighten
		for (int i = 0; i < nVars; i++) {
			ParamBounds ibi = (ParamBounds) mat.get(i, OctagonRel.bar(i));
			if (!ibi.isFinite())
				continue;
			
			ParamBounds ibi_even = null;
			ParamBounds ibi_odd = null;
			
			//for (int j = 2*(i/2); j < nVars; j++) {
			for (int j = 0; j < nVars; j++) {
				
				ParamBounds bjj = (ParamBounds) mat.get(OctagonRel.bar(j),j);
				if (!bjj.isFinite())
					continue;
				
				if (ibi_even == null) { 
					ibi_even = ParamBounds.posInf();
					ibi_odd = ParamBounds.posInf();
					
					splitEvenOdd_floorDiv2(ibi, ibi_even, ibi_odd);
				}
				
				ParamBounds bjj_even = ParamBounds.posInf();
				ParamBounds bjj_odd = ParamBounds.posInf();
				
				splitEvenOdd_floorDiv2(bjj, bjj_even, bjj_odd);
				
				
				ParamBounds even_final = ParamBounds.posInf();
				ParamBounds odd_final = ParamBounds.posInf();
				
				sum(ibi_even, bjj_even, even_final);
				sum(ibi_odd, bjj_odd, odd_final);
				
				
				ParamBounds ij_even = (ParamBounds) mat_even.get(i,j);
				ParamBounds ij_even_final = (ParamBounds) ij_even.min(even_final);
				
				ParamBounds ij_odd = (ParamBounds) mat_odd.get(i,j);
				ParamBounds ij_odd_final = (ParamBounds) ij_odd.min(odd_final);
				
				// make coherent
				int bi = OctagonRel.bar(i);
				int bj = OctagonRel.bar(j);
				
				mat_even.set(i, j, ij_even_final);
				mat_odd.set(i, j, ij_odd_final);
				
				mat_even.set(bj, bi, ij_even_final); // don't copy, fields are not expected to change later
				mat_odd.set(bj, bi, ij_odd_final);
			}
		}
		
		return ret;
	}
	
	
	public static void tighten(Matrix mat) {

		int nVars = mat.size();

		FieldStatic fs = mat.fs();

		// tighten i,j: m_{i,j} -> floor(m_{i,b(i)} / 2) + floor(m_{b(j),j} / 2)
		for (int i = 0; i < nVars; i++) {
			Field ibi = mat.get(i, OctagonRel.bar(i));
			if (!ibi.isFinite())
				continue;

			// each cell under "diagonal (2*(i/2))" are, due to coherence,
			// same same as some cell above diagonal
			for (int j = 2*(i/2); j < nVars; j++) {
				
//				if (i==j)
//					continue;
				
				Field bjj = mat.get(OctagonRel.bar(j),j);
				if (!bjj.isFinite())
					continue;
				
				int val_tight = CR.floor(ibi.toInt(),2) + CR.floor(bjj.toInt(),2);
				Field ij = mat.get(i,j);
				int val_new;
				if (ij.isFinite()) {
					int val_old = ij.toInt();
					val_new = Math.min(val_old, val_tight);
				} else {
					val_new = val_tight;
				}
				
				mat.set(i, j, fs.giveField(val_new));
				// coherence under diagonal
				mat.set(OctagonRel.bar(j), OctagonRel.bar(i), fs.giveField(val_new));
			}
		}
	}

	public static boolean isConsistent_diagonal(Matrix mat) {
		int size = mat.size();
		for (int i = 0; i < size; ++i)
			if (!mat.get(i, i).consistent(true))
				return false;
		return true;
	}

	public static boolean isConsistent_tight(Matrix mat) {
		int nVars = mat.size() / 2;
		for (int i = 1; i < nVars; ++i) {
			int r = 2 * i - 2, c = 2 * i - 1;
			Field rc = mat.get(r, c);
			Field cr = mat.get(c, r);
			if (!rc.isFinite() || !cr.isFinite())
				continue;
			if (rc.toInt() / 2 + cr.toInt() / 2 < 0)
				return false;
		}
		return true;
	}

	public boolean isConsistent() {
		if (this.encoding.isDBC()) {
			return isConsistent_diagonal(mat);
		} else {
			if (!isConsistent_diagonal(mat))
				return false;
			return isConsistent_tight(mat);
		}
	}

	// works only for consistent matrix -- TODO
	public OctConstr[] dbMat2OctConstrs(Variable[] varsOrig) {
		Collection<OctConstr> col = new LinkedList<OctConstr>();
		int size = mat.size();
		int nVars = size / 2;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {

				if (i == j)
					continue;
				
				if ((i == 0 && j == nVars) || (j == 0 && i == nVars))
					continue;

				Field f = mat.get(i, j);
				if (!f.isFinite())
					continue;

				int fi = f.toInt();

				LinearTerm lt1;
				LinearTerm lt2;
				int I = (i<nVars)? 1 : 2;
				int J = (j<nVars)? 1 : 2;
				if (((i == 0 || i == nVars) && j != 0 && j != nVars)) {
					lt1 = new LinearTerm(varsOrig[j-J], -1);
					lt2 = lt1;
					fi *= 2;
				} else if (((j == 0 || j == nVars) && i != 0 && i != nVars)) {
					lt1 = new LinearTerm(varsOrig[i-I], 1);
					lt2 = lt1;
					fi *= 2;
				} else {
					lt1 = new LinearTerm(varsOrig[i-I], 1);
					lt2 = new LinearTerm(varsOrig[j-J], -1);
				}
				col.add(new OctConstr(lt1, lt2, fi));
			}
		}
		return col.toArray(new OctConstr[0]);
	}

	public Collection<OctConstrLeqEq> dbMat2OctConstrsLeqEq(Variable[] varsOrig) {
		return dbMat2OctConstrsLeqEq(true, varsOrig);
	}
	public Collection<OctConstrLeqEq> dbMat2OctConstrsLeqEq(boolean useCanonicity, Variable[] varsOrig) {
		Collection<OctConstrLeqEq> col = new LinkedList<OctConstrLeqEq>();
		int size = mat.size();
		int nVars = size / 2;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {

				if (i == j)
					continue;
				
				if (useCanonicity) {
				
					//if (keepZero) {
					//	if ((i == nVars && j != 0) || (i != 0 && j == nVars) ) // canonicity means that both $-x<=c and $'-x<=c are present (similarly x-$<=c and x-$'<=c)
					//		continue;
					//} else {
						if (i == nVars || j == nVars) // canonicity means that both $-x<=c and $'-x<=c are present (similarly x-$<=c and x-$'<=c)
							continue;
					//}
				} else {
					//if (!keepZero)
						if ( (i == nVars && j == 0) || (i == 0 && j == nVars) )
							continue;
				}

				Field f = mat.get(i, j);
				if (!f.isFinite())
					continue;

				int fi = f.toInt();
				Field ff = mat.get(j, i);
				boolean isEq = (ff.isFinite() && ff.toInt() == -fi);

				if (isEq && j > i) // don't add equality twice
					continue;

				LinearTerm lt1;
				LinearTerm lt2;
				int I = (i<nVars)? 1 : 2;
				int J = (j<nVars)? 1 : 2;
				if ((i == 0 || i == nVars) && j != 0 && j != nVars) {
					lt1 = new LinearTerm(varsOrig[j-J], -1);
					lt2 = null;
				} else if ((j == 0 || j == nVars) && i != 0 && i != nVars) {
					lt1 = new LinearTerm(varsOrig[i-I], 1);
					lt2 = null;
				} else {
					lt1 = new LinearTerm(varsOrig[i-I], 1);
					lt2 = new LinearTerm(varsOrig[j-J], -1);
				}

				col.add(new OctConstrLeqEq(lt1, lt2, fi, isEq));
			}
		}
		return col;
	}

/*	public String[] dbConstr_variables_unp() {
		return Arrays.copyOfRange(mat.domain(), 1, mat.h() / 2);
	}
*/
	public boolean octConstr_isDBConstr() {
		int nVars = mat.size() / 2;

		for (int i = 1; i <= nVars; i++) { // x
			for (int j = i+1; j <= nVars; j++) { // y
				int r = 2 * i - 2, c = 2 * j - 1;
	
				Field rc = mat.get(r, c);
				if (rc.isFinite() && !this.implicit_const_oct(r,c,rc)) // x+y
					return false;
				
				Field cr = mat.get(c, r);
				if (cr.isFinite() && !this.implicit_const_oct(c,r,cr)) // -x-y
					return false;
			}
		}

		return true;
	}

	// works only for consistent matrix -- TODO
	// returns those constraints which are difference bound constraints
	// (hence, if it is not equivalent to a DB relation, the returned
	// constraints are an abstraction)
	public DBC[] octMat2DBCs(Variable[] vars) {

		Collection<DBC> col = new LinkedList<DBC>();

		int nVars = mat.size() / 2;

		for (int i = 1; i <= nVars; i++) {
			for (int j = i + 1; j <= nVars; j++) {

				{
					Field a = mat.get(2 * i - 2, 2 * j - 2); // x-y
					if (a.isFinite()) {
						col.add(new DBC(vars[i - 1], vars[j - 1], a));
					}
				}

				{
					Field d = mat.get(2 * i - 1, 2 * j - 1); // y-x
					if (d.isFinite()) {
						col.add(new DBC(vars[j - 1], vars[i - 1], d));
					}
				}
			}

			{
				Field g = mat.get(2 * i - 2, 2 * i - 1);
				if (g.isFinite()) {
					col.add(new DBC(vars[i - 1], null, fs.giveField(g.toInt() / 2)));
				}
			}

			{
				Field h = mat.get(2 * i - 1, 2 * i - 2);
				if (h.isFinite()) {
					col.add(new DBC(null, vars[i - 1], fs.giveField(h.toInt() / 2)));
				}
			}
		}

		return col.toArray(new DBC[0]);
	}

	private LinearConstr createLinearConstr(LinearTerm lt1, int c) {
		LinearConstr lc = new LinearConstr();
		lc.addLinTerm(lt1);
		lc.addLinTerm(new LinearTerm(null, -c));
		return lc;
	}

	private LinearConstr createLinearConstr(LinearTerm lt1, LinearTerm lt2, int c) {
		LinearConstr lc = new LinearConstr();
		lc.addLinTerm(lt1);
		lc.addLinTerm(lt2);
		lc.addLinTerm(new LinearTerm(null, -c));
		return lc;
	}

	// works only for consistent matrix -- TODO
	// assumption: it CAN be represented as an dbc
	public Collection<LinearConstr> octMat2LinConstrs(Variable[] vars) {

		Collection<LinearConstr> col = new LinkedList<LinearConstr>();

		int nVars = mat.size() / 2;

		for (int i = 1; i <= nVars; i++) {
			for (int j = i + 1; j <= nVars; j++) {

				Field a = mat.get(2 * i - 2, 2 * j - 2); // x-y
				if (a.isFinite()) {
					LinearTerm lt1 = new LinearTerm(vars[i - 1], 1);
					LinearTerm lt2 = new LinearTerm(vars[j - 1], -1);

					col.add(createLinearConstr(lt1, lt2, a.toInt()));
				}

				Field b = mat.get(2 * i - 2, 2 * j - 1); // x+y
				if (b.isFinite()) {
					LinearTerm lt1 = new LinearTerm(vars[i - 1], 1);
					LinearTerm lt2 = new LinearTerm(vars[j - 1], 1);

					col.add(createLinearConstr(lt1, lt2, b.toInt()));
				}

				Field c = mat.get(2 * i - 1, 2 * j - 2); // -x-y
				if (c.isFinite()) {
					LinearTerm lt1 = new LinearTerm(vars[i - 1], -1);
					LinearTerm lt2 = new LinearTerm(vars[j - 1], -1);

					col.add(createLinearConstr(lt1, lt2, c.toInt()));
				}

				Field d = mat.get(2 * i - 1, 2 * j - 1); // -x+y
				if (d.isFinite()) {
					LinearTerm lt1 = new LinearTerm(vars[i - 1], -1);
					LinearTerm lt2 = new LinearTerm(vars[j - 1], 1);

					col.add(createLinearConstr(lt1, lt2, d.toInt()));
				}
			}

			Field f = mat.get(2 * i - 2, 2 * i - 1); // 2x
			if (f.isFinite()) {
				LinearTerm lt1 = new LinearTerm(vars[i - 1], 1);

				col.add(createLinearConstr(lt1, f.toInt() / 2));
			}

			Field g = mat.get(2 * i - 1, 2 * i - 2); // -2x
			if (g.isFinite()) {
				LinearTerm lt1 = new LinearTerm(vars[i - 1], -1);

				col.add(createLinearConstr(lt1, g.toInt() / 2));
			}
		}

		return col;
	}

	public Collection<OctConstrLeqEq> octMat2OctConstrsLeqEq(Variable[] vars) {

		Collection<OctConstrLeqEq> col = new LinkedList<OctConstrLeqEq>();

		int nVars = mat.size() / 2;

		for (int i = 1; i <= nVars; i++) {
			for (int j = i + 1; j <= nVars; j++) {

				{
				Field a = mat.get(2 * i - 2, 2 * j - 2); // x-y
				Field d = mat.get(2 * i - 1, 2 * j - 1); // y-x
				if (a.isFinite() || d.isFinite()) {
					if (a.isFinite() && d.isFinite() && a.toInt() == -d.toInt()) {
						LinearTerm lt1 = new LinearTerm(vars[i - 1], 1);
						LinearTerm lt2 = new LinearTerm(vars[j - 1], -1);
	
						col.add(new OctConstrLeqEq(lt1, lt2, a.toInt(), true));
					} else {
						if (a.isFinite()) {
								LinearTerm lt1 = new LinearTerm(vars[i - 1], 1);
								LinearTerm lt2 = new LinearTerm(vars[j - 1], -1);
		
								col.add(new OctConstrLeqEq(lt1, lt2, a.toInt()));
						}
						if (d.isFinite()) {
								LinearTerm lt1 = new LinearTerm(vars[i - 1], -1);
								LinearTerm lt2 = new LinearTerm(vars[j - 1], 1);
		
								col.add(new OctConstrLeqEq(lt1, lt2, d.toInt()));
						}
					}
				}
				}
				
				{
				Field b = mat.get(2 * i - 2, 2 * j - 1); // x+y
				Field c = mat.get(2 * i - 1, 2 * j - 2); // -x-y
				if (b.isFinite() || c.isFinite()) {
					if (b.isFinite() && c.isFinite() && b.toInt() == -c.toInt()) {
						LinearTerm lt1 = new LinearTerm(vars[i - 1], 1);
						LinearTerm lt2 = new LinearTerm(vars[j - 1], 1);
	
						col.add(new OctConstrLeqEq(lt1, lt2, b.toInt(), true));
					} else {
						if (b.isFinite()) {
								LinearTerm lt1 = new LinearTerm(vars[i - 1], 1);
								LinearTerm lt2 = new LinearTerm(vars[j - 1], 1);
		
								col.add(new OctConstrLeqEq(lt1, lt2, b.toInt()));
						}
						if (c.isFinite()) {
								LinearTerm lt1 = new LinearTerm(vars[i - 1], -1);
								LinearTerm lt2 = new LinearTerm(vars[j - 1], -1);
		
								col.add(new OctConstrLeqEq(lt1, lt2, c.toInt()));
						}
					}
				}
				}

			}

			{
			Field f = mat.get(2 * i - 2, 2 * i - 1); // 2x
			Field g = mat.get(2 * i - 1, 2 * i - 2); // -2x
			if (f.isFinite() || g.isFinite()) {
				if (f.isFinite() && g.isFinite() && f.toInt() == -g.toInt()) {
					LinearTerm lt1 = new LinearTerm(vars[i - 1], 1);
	
					col.add(new OctConstrLeqEq(lt1, f.toInt() / 2, true));
				} else {
					if (f.isFinite()) {
							LinearTerm lt1 = new LinearTerm(vars[i - 1], 1);
		
							col.add(new OctConstrLeqEq(lt1, f.toInt() / 2));
					}
					if (g.isFinite()) {
							LinearTerm lt1 = new LinearTerm(vars[i - 1], -1);
		
							col.add(new OctConstrLeqEq(lt1, g.toInt() / 2));
					}
				}
			}
			}
		}

		return col;
	}

//	// works only for consistent matrix
//	// works correctly only for compact matrix !!!
//	public Collection<LinearConstr> dbMat2LinConstrs() {
//		return dbMat2LinConstrs(true);
//	}
	
	public Collection<LinearConstr> dbMat2LinConstrs(Variable[] varsOrig) {
		Collection<LinearConstr> col = new LinkedList<LinearConstr>();
		int size = mat.size();
		int nVars = size / 2;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				Field f = mat.get(i, j);
				if (!f.isFinite())
					continue;

				if (i == j)
					continue;
				
				//if (i == nVars || j == nVars)
				//	continue;
				
				if ( (i == 0 && j == nVars) || (j == 0 && i == nVars) )
					continue;
				
				LinearTerm lt1;
				LinearTerm lt2;
				int I = (i<nVars)? 1 : 2;
				int J = (j<nVars)? 1 : 2;
				if (((i == 0 || i == nVars) && j != 0 && j != nVars)) {
					lt1 = new LinearTerm(varsOrig[j-J], -1);
					col.add(createLinearConstr(lt1, f.toInt()));
				} else if (((j == 0 || j == nVars) && i != 0 && i != nVars)) {
					lt1 = new LinearTerm(varsOrig[i-I], 1);
					col.add(createLinearConstr(lt1, f.toInt()));
				} else {
					lt1 = new LinearTerm(varsOrig[i-I], 1);
					lt2 = new LinearTerm(varsOrig[j-J], -1);
					col.add(createLinearConstr(lt1, lt2, f.toInt()));
				}

			}
		}
		return col;
	}

	private static class OctConstrLeqEq extends OctConstr {
		public boolean isEq;

		public OctConstrLeqEq(LinearTerm aLt1, int aBound) {
			this(aLt1, null, aBound);
		}

		public OctConstrLeqEq(LinearTerm aLt1, LinearTerm aLt2, int aBound) {
			this(aLt1, aLt2, aBound, false);
		}

		public OctConstrLeqEq(LinearTerm aLt1, int aBound, boolean aIsEq) {
			this(aLt1, null, aBound, aIsEq);
		}

		public OctConstrLeqEq(LinearTerm aLt1, LinearTerm aLt2, int aBound, boolean aIsEq) {
			super(aLt1, aLt2, aBound);
			isEq = aIsEq;
		}

		public StringBuffer toStringBuf() {
			boolean singleTerm = !(lt1 != null && lt2 != null);
			
			String rop = isEq? "=" : "<=";

			if (bound != 0 || singleTerm) {
				String p1 = (lt1 != null ? lt1.toString() : "");
				String p2 = "";
				if (lt2 != null) {
					if (lt1 != null)
						p2 = ((lt2.coeff()>0)? "+" : "")+lt2;
					else 
						p2 = lt2.toString();
				}
				return new StringBuffer(p1 + p2 + rop + bound);
			} else { // bound == 0 && both terms
				String p2;
				if (lt2.coeff()>0)
					p2 = "-"+lt2.variable();
				else
					p2 = ""+lt2.variable();
				
				return new StringBuffer(lt1.toString() + rop + p2);
			}
		}

		public static StringBuffer toStringBuf(Collection<OctConstrLeqEq> aCol) {
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for (OctConstrLeqEq oc : aCol) {
				if (first) {
					first = false;
				} else {
					sb.append(",");
				}
				sb.append(oc.toStringBuf());
			}
			return sb;
		}

		public BooleanFormula toJSMT(FlataJavaSMT fjsmt, boolean negate, String s_u, String s_p) {
			LinearTerm bndTerm = new LinearTerm(null, bound);
			IntegerFormula bndFormula = bndTerm.toJSMT(fjsmt, s_u, s_p);

			// TODO: check if this is an OK solution
			// TODO: break out into its own function ??
			BiFunction<IntegerFormula, IntegerFormula, BooleanFormula> comparisonFunction;
			if (isEq) {
				if (negate) {
					comparisonFunction = (num1, num2) -> fjsmt.getBfm().not(fjsmt.getIfm().equal(num1, num2));
				} else {
					comparisonFunction = fjsmt.getIfm()::equal;
				}
			} else {
				if (negate) {
					comparisonFunction = fjsmt.getIfm()::greaterThan;
				} else {
					comparisonFunction = fjsmt.getIfm()::lessOrEquals;
				}
			}

			if (lt1 != null && lt2 != null) {
				IntegerFormula lt1Formula = lt1.toJSMT(fjsmt, s_u, s_p);
				IntegerFormula lt2Formula = lt2.toJSMT(fjsmt, s_u, s_p);

				IntegerFormula sum = fjsmt.getIfm().add(lt1Formula, lt2Formula);
				return comparisonFunction.apply(sum, bndFormula);
			} else {
				IntegerFormula formula = (lt1 == null) ? lt2.toJSMT(fjsmt, s_u, s_p) : lt1.toJSMT(fjsmt, s_u, s_p);
				return comparisonFunction.apply(formula, bndFormula);
			}
		}

		// TODO: remove
		public void toStringBufYices(IndentedWriter iw, String s_u, String s_p, boolean negate) {
			
			LinearTerm bndTerm = new LinearTerm(null,bound);
			
			String eq = (negate)? "/=" : "=";
			String leq = (negate)? ">" : "<=";
			
			if (lt1 != null && lt2 != null) {
				iw.writeln("("+(isEq? eq : leq)+" (+ "+lt1.toSBYices(s_u,s_p)+" "+lt2.toSBYices(s_u,s_p)+") "+bndTerm.toSBYices(s_u,s_p)+")");
			} else {
				StringBuffer tmp = (lt1 == null)? lt2.toSBYices(s_u,s_p) : lt1.toSBYices(s_u,s_p);
				iw.writeln("("+(isEq? eq : leq)+" "+tmp+" "+bndTerm.toSBYices(s_u,s_p)+")");
			}
			
		}

		public static LinkedList<BooleanFormula> toJSMTList(FlataJavaSMT fjsmt, boolean negate, String s_u, String s_p, Collection<OctConstrLeqEq> aCol) {
			LinkedList<BooleanFormula> formulas = new LinkedList<>();
			for (OctConstrLeqEq oc : aCol) {
				formulas.add(oc.toJSMT(fjsmt, negate, s_u, s_p));
			}
			return formulas;
		}

		// TODO: remove
		public static void toStringBufYicesList(IndentedWriter iw, Collection<OctConstrLeqEq> aCol, String suf_unp, String suf_p, boolean negate) {
			for (OctConstrLeqEq oc : aCol) {
				oc.toStringBufYices(iw, suf_unp, suf_p, negate);
			}
		}
	}

	public StringBuffer toStringBuf_dbc_compact(Variable[] varsOrig) {
		Collection<OctConstrLeqEq> col;
		col = dbMat2OctConstrsLeqEq(false, varsOrig);
		return OctConstrLeqEq.toStringBuf(col);
	}
	
	public StringBuffer toStringBuf_dbc(Variable[] varsOrig) {
		Collection<OctConstrLeqEq> col;
		col = dbMat2OctConstrsLeqEq(varsOrig);
		return OctConstrLeqEq.toStringBuf(col);
	}

	public StringBuffer toStringBuf_oct(Variable[] vars) {
		Collection<OctConstrLeqEq> col;
		col = octMat2OctConstrsLeqEq(vars);
		return OctConstrLeqEq.toStringBuf(col);
	}

	public LinkedList<BooleanFormula> toJSMTList_dbc(FlataJavaSMT fjsmt, boolean negate, String s_u, String s_p, Variable[] varsOrig) {
		Collection<OctConstrLeqEq> col = dbMat2OctConstrsLeqEq(varsOrig);

		return OctConstrLeqEq.toJSMTList(fjsmt, negate, s_u, s_p, col);
	}

	// TODO: remove
	public void toStringBufYicesList_dbc(IndentedWriter iw, String suf_unp, String suf_p, boolean negate, Variable[] varsOrig) {
		Collection<OctConstrLeqEq> col;
		col = dbMat2OctConstrsLeqEq(varsOrig);
		OctConstrLeqEq.toStringBufYicesList(iw, col, suf_unp, suf_p, negate);
	}

	public LinkedList<BooleanFormula> toJSMTList_oct(FlataJavaSMT fjsmt, boolean negate, String s_u, String s_p, Variable[] varsOrig) {
		Collection<OctConstrLeqEq> col = octMat2OctConstrsLeqEq(varsOrig);

		return OctConstrLeqEq.toJSMTList(fjsmt, negate, s_u, s_p, col);
	}

	// TODO: remove
	public void toStringBufYicesList_oct(IndentedWriter iw, Variable[] vars, String suf_unp, String suf_p, boolean negate) {
		Collection<OctConstrLeqEq> col;
		col = octMat2OctConstrsLeqEq(vars);
		OctConstrLeqEq.toStringBufYicesList(iw, col, suf_unp, suf_p, negate);
	}

	private boolean areInverse(Field f1, Field f2) {
		if (!f1.isFinite() || !f2.isFinite())
			return false;
		
		return f1.toInt() == - f2.toInt();
	}
	private void setInfCoher(int i, int j) {
		mat.set(i, j, fs.posInf());
		mat.set(OctagonRel.bar(j), OctagonRel.bar(i), fs.posInf());
	}
	public DBM determinize_oct() {
		int n2 = mat.size();
		int n1 = n2 / 2;
		int n0 = n1 / 2;
		DBM det = new DBM(this);
		Matrix mdet = det.mat;
		for (int i=n0; i<n1; i++) {
			boolean f = areInverse(mdet.get(2*i+1, 2*i),mdet.get(2*i, 2*i+1));
			for (int j=0; j<n0; j++) {
				if (f) {
					det.setInfCoher(2*i  , 2*j  );
					det.setInfCoher(2*i  , 2*j+1);
					det.setInfCoher(2*i+1, 2*j  );
					det.setInfCoher(2*i+1, 2*j+1);
				} else {
					int ii,jj,kk,ll;
					
					ii = 2*i; jj = 2*j; kk = 2*i+1; ll = 2*j+1;
					if (areInverse(mdet.get(ii, jj),mdet.get(kk, ll))) {
						f = true;
					} else {
						det.setInfCoher(ii, jj);
						det.setInfCoher(kk, ll);
					}
					
					ii = 2*i; jj = 2*j+1; kk = 2*i+1; ll = 2*j;
					if (!f && areInverse(mdet.get(ii, jj),mdet.get(kk, ll))) {
						f = true;
					} else {
						det.setInfCoher(ii, jj);
						det.setInfCoher(kk, ll);
					}
				}
			}
			if (!f) {
				// not deterministic
				return null;
			}
		}

		for (int i=n0; i<n1; i++) {
			for (int j=i+1; j<n1; j++) {
				det.setInfCoher(2*i  , 2*j  );
				det.setInfCoher(2*i  , 2*j+1);
				det.setInfCoher(2*i+1, 2*j  );
				det.setInfCoher(2*i+1, 2*j+1);
			}
		}
		
		DBM tmp = new DBM(det);
		tmp.canonize();
		if (!tmp.equals(this)) {
			// not deterministic
			return null;
		}
		// deterministic
		return det;
	}
	
//	public DBM determinize_dbm() {
//		int n2 = mat.size();
//		int n1 = n2 / 2;
//		DBM det = new DBM(this);
//		Matrix mdet = det.mat;
//		for (int i=n1+1; i<n2; i++) {
//			boolean f = false;
//			for (int j=0; j<n1; j++) {
//				if (f) {
//					mdet.set(i, j, fs.posInf());
//					mdet.set(j, i, fs.posInf());
//				} else {
//					if (areInverse(mdet.get(i, j),mdet.get(j, i))) {
//						f = true;
//					} else {
//						mdet.set(i, j, fs.posInf());
//						mdet.set(j, i, fs.posInf());
//					}
//				}
//			}
//			if (!f) {
//				// not deterministic
//				return null;
//			}
//		}
//		for (int i=n1; i<n2; i++) {
//			for (int j=i+1; j<n2; j++) {
//				mdet.set(i, j, fs.posInf());
//				mdet.set(j, i, fs.posInf());
//			}
//		}
//		for (int i=1; i<n1; i++) {
//			mdet.set(i, n1, fs.posInf());
//			mdet.set(n1, i, fs.posInf());
//		}
//		DBM tmp = new DBM(det);
//		tmp.canonize();
//		if (!tmp.equals(this)) {
//			// not deterministic
//			return null;
//		}
//		// deterministic
//		return det;
//	}

//	public DetRes try_determinize_dbm_old(boolean allDet) {
//		
//		int n2 = mat.size();
//		int n1 = n2 / 2;
//		DBM det = new DBM(this);
//		Matrix mdet = det.mat;
//		// flags for deterministically assigned variables
//		BitSet bs = new BitSet(n2);
//		int uncertainHavoc = 0;
//		for (int i=n1+1; i<n2; i++) {
//			
//			if (areInverse(mdet.get(i, 0),mdet.get(0, i))) {
//				bs.set(i);
//				// x_i'=c
//				// remove redundant constraints by substituting the constant
//				int skip = allDet? 0 : n1; // x_i'=c is stored both in B/\C (x_i'-zero=c) and in D ((x_i'-zero'=c)) 
//				for (int j=0; j<n2; j++) {
//					if (i != j && j != skip) {
//						mdet.set(j, i, fs.posInf());
//						mdet.set(i, j, fs.posInf());
//					}
//				}
//			} else {
//				
//				int inx = -1;
//				for (int j=1; j<n1; j++) {
//					if (areInverse(mdet.get(i, j),mdet.get(j, i))) {
//						inx = j;
//						break;
//					}
//				}
//				if (inx > 0) {
//					bs.set(i);
//					// x_i'=x_inx
//					// we can safely remove constraints from B and C blocks of the DBM
//					// (since they will be present implicitly by transitivity)
//					for (int j=0; j<n1; j++) {
//						if (j != inx) {
//							mdet.set(i, j, fs.posInf());
//							mdet.set(j, i, fs.posInf());
//						}
//					}
//				} else {
//					// optimistically assume that all constraints on x_i in B and C are implied
//					// (justification of this assumption is checked later)
//					boolean b = false;
//					for (int j=0; j<n1; j++) {
//						if (!b && j > 0 && (mdet.get(i,j).isFinite() || mdet.get(j, i).isFinite())) {
//							b = true;
//						}
//						mdet.set(i, j, fs.posInf());
//						mdet.set(j, i, fs.posInf());
//					}
//					if (b) {
//						uncertainHavoc++;
//					}
//				}
//			}
//		}
//		
//		// debug
//		boolean debug = true;
//		
//		if (bs.cardinality() == n1-1 && allDet) {
//			// for sure completely deterministic and the D part is not needed
//			// (A /\ B /\ C) ==> D
//			
//			DBM A = det.preimage();
//			DBM BC = det.projectToUpdate_dbc();
//			
//			if (debug) {
//				DBM aux = A.intersect(BC);
//				aux.canonize();
//				//checkEquiv(this,aux);
//				if (!this.equals(aux)) {
//					//this.try_determinize_dbm(allDet);
//					throw new RuntimeException("internal error");
//				}
//			}
//			if (debug) {
//				DBM aux = new DBM(det);
//				aux.canonize();
//				checkEquiv(this,aux);
//			}
//			
//			return new DetRes(det, A, BC);
//			
//		} else {
//			
//			boolean b = uncertainHavoc == 0;
//			if (!b) {
//				DBM tmp = new DBM(det);
//				tmp.canonize();
//				b = tmp.equals(this); 
//			}
//			
//			if (b) {
//				// bs.set(i) => x_i' is deterministic
//				// not bs.set(i) => x_i' is complete havoc
//				
//				DBM A = det.preimage();
//				DBM BC = det.projectToUpdate_dbc();
//				DBM D = det.postimage();
//				
//				if (debug) {
//					DBM aux = A.intersect(BC).intersect(D);
//					aux.canonize();
//					checkEquiv(this,aux);
//				}
//				
//				return new DetRes(det, A, BC, D);
//			} else {
//				return new DetRes(det);
//			}
//		}
//	}

	public enum DetResType {
		OTHER, EACH_VAR_DET, HAVOC_OR_DET_FROM_CORE;  
	}
	public class DetRes {
		DetResType type;
		DBM det;
		// set only if EACH_VAR_DET or EACH_VAR_DET_OR_HAVOC
		DBM guard;
		DBM update;
		// set only if EACH_VAR_DET_OR_HAVOC
		DBM guard_prime;
		
		public DetRes(DBM aDet) {
			type = DetResType.OTHER;
			det = aDet;
		}
		public DetRes(DBM aDet, DBM aG, DBM aU) {
			type = DetResType.EACH_VAR_DET;
			det = aDet;
			guard = aG;
			update = aU;
		}
		public DetRes(DBM aDet, DBM aG, DBM aU, DBM aGP) {
			type = DetResType.HAVOC_OR_DET_FROM_CORE;
			det = aDet;
			guard = aG;
			update = aU;
			guard_prime = aGP;
		}
		
		public boolean isEachVarDet() { return type == DetResType.EACH_VAR_DET; }
		public boolean isEachVarHavocOrDetFromCore() { return type == DetResType.HAVOC_OR_DET_FROM_CORE; }
	}
	
	public static void checkEquiv(DBM d1, DBM d2) {
		if (!d1.equals(d2)) {
			throw new RuntimeException("internal error");
		}
	}
	
	// map[i] gives predecessors of i
	private BitSet pre(BitSet[] map, BitSet set) {
		int n = map.length;
		BitSet ret = new BitSet(n);
		for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i+1)) {
			ret.or(map[i]);
		}
		return ret;
	}

	// ______   ________
	// | A B | | A' B' |
	// | C D | | C' D' |
	// |_____| |_______|
	//
	// Try to find A' /\ B' /\ C' /\ D'  <==>  A /\ B /\ C /\ D
	// allDet => B' /\ C' is  /\_{x'\in X'} x'=f(X)
	// allDet => D' is empty
	// 
	// not allDet => B' /\ C' 
	//   there is a subset Y' \subseteq X' such that 
	//     Y' is deterministically determined from Y
	//     each variable from X'\Y' is either complete havoc or a function of Y
	//
	// The function returns:
	// {det} if DBM cannot be made deterministic
	// allDet => {det,A', B'/\C'}
	// not allDet => {det,A', B'/\C', D'}
	//
	public DetRes try_determinize_dbm(boolean allDet) {
		// encoding of variables {x,y,z,...}: 0->0, x->1, y->2, z->3, ... 
		BitSet[] detmap = null;
		if (this.encoding.isDBC()) {
			int n2 = mat.size();
			int n1 = n2 / 2;
			detmap = new BitSet[n1];
			// zero'=zero
			detmap[0] = new BitSet();
			detmap[0].set(0);
			//
			for (int i=n1+1; i<n2; i++) { // x,y,z,...
				BitSet bs = new BitSet(n1);
				detmap[i-n1] = bs;
				for (int j=0; j<n1; j++) { // zero,x,y,z,...
					if (areInverse(mat.get(i, j),mat.get(j, i))) {
						bs.set(j);
					}
				}
			}
		} else {
			int n3 = mat.size();
			int n2 = n3 / 2;
			int n1 = n2 / 2;
			int nzero = n1+1;
			detmap = new BitSet[nzero];
			// zero'=zero
			detmap[0] = new BitSet();
			detmap[0].set(0);
			//
			for (int i=n1; i<n2; i++) { // x,y,z,...
				BitSet bs = new BitSet(nzero);
				detmap[i-n1+1] = bs;
				int ip = 2*i;
				int im = 2*i+1;
				for (int j=0; j<n1; j++) { // x,y,z,...
					// xi'=xj  (xip'-xjp=0)
					// xi'=-xj  (xip'-xjm=0)
					int jp = 2*j;
					int jm = 2*j+1;
					if (	areInverse(mat.get(ip, jp),mat.get(jp, ip)) ||
							areInverse(mat.get(ip, jm),mat.get(jm, ip))) {
						bs.set(j+1);
					}
				}
				// zero
				if (areInverse(mat.get(ip, im),mat.get(im, ip))) {
					bs.set(0);
				}
			}
		}
		
		BitSet detOneStep = new BitSet();
		for (int i=0; i<detmap.length; i++) {
			if (!detmap[i].isEmpty()) {
				detOneStep.set(i);
			}
		}
		
		// compute the deterministic core
		BitSet fixpoint = detOneStep;
		int oldsize;
		do {
			oldsize = fixpoint.size();
			fixpoint.and(pre(detmap,fixpoint));
			
		} while(oldsize != fixpoint.size());
		
		// compute all the variables which are uniquely determined:
		//    post-image of the the core (fixpoint) via detmap^-1
		BitSet det_var = new BitSet();
		for (int i = detOneStep.nextSetBit(0); i >= 0; i = detOneStep.nextSetBit(i+1)) {
			BitSet aux = (BitSet) detmap[i].clone();
			aux.and(fixpoint);
			if (!aux.isEmpty()) {
				det_var.set(i);
			}
		}
		
		if (this.encoding.isDBC()) {
			int n2 = mat.size();
			int n1 = n2 / 2;
			
			DBM aux = this.projectUpdatePart_dbc(det_var);
			DBM aux_closed = new DBM(aux);
			aux_closed.canonize();
			boolean b = aux_closed.equals(this);
			
			if (!b || 
					(det_var.size() != n1 && allDet)) {
				
				return new DetRes(aux);
				
			} else {
				
				if (det_var.size() == n1) {
					
					// for completely deterministic update, the D part is not needed
					// since (A /\ B /\ C) ==> D
					
					DBM A = aux.preimage();
					DBM BC = aux.projectToUpdate_dbc();
					
					return new DetRes(aux, A, BC);
					
				} else {
					
					DBM A = aux.preimage();
					DBM BC = aux.projectToUpdate_dbc();
					DBM D = aux.postimage();
					
					// TODO: optimization: if X-det_var are complete havoc, one doesn't need D either
					
					return new DetRes(aux, A, BC, D);
				}
			}
		} else {
			throw new RuntimeException();
		}
	}
	
	public boolean isFASTCompatible_dbm() {
		DetRes aux = try_determinize_dbm(true);
		return aux.type == DetResType.EACH_VAR_DET;
		//return (determinize_dbm() != null);
	}

	public boolean isFASTCompatible_oct() {
		return determinize_oct() != null;
	}

	public FiniteVarIntervals findIntervals_dbc(Variable[] varsOrig) {
		FiniteVarIntervals ret = new FiniteVarIntervals();
		int n = mat.size();
		int n2 = n/2;
		for (int i=1; i<n; i++) {
			if (i == n2) {
				continue;
			}
			Field f_up = mat.get(i,0);
			Field f_low = mat.get(0,i);
			if (f_up.isFinite() && f_low.isFinite()) {
				int ix = (i>n2)? 2 : 1;
				ret.add(new FiniteVarInterval(varsOrig[i-ix],f_up.toInt(),-f_low.toInt()));
			}
		}
		return ret;
	}
	public FiniteVarIntervals findIntervals_oct(Variable[] varsOrig) {
		FiniteVarIntervals ret = new FiniteVarIntervals();
		int n = mat.size();
		for (int i=0; i<n; i++) {
			Field f_up = mat.get(2*i,2*i+1);
			Field f_low = mat.get(2*i+1,2*i);
			if (f_up.isFinite() && f_low.isFinite()) {
				ret.add(new FiniteVarInterval(varsOrig[i],f_up.toInt()/2,-f_low.toInt()/2));
			}
		}
		return ret;
	}
	
	
	// only for DBMs, not octagons
	// a collection contains DBCs over encoding-name variables
	public DBM createDBM(Collection<DBC> aCol, FieldStaticInf aFs, Variable[] varsOrig) { 
		
		int dim = mat.size();
		
		Matrix ret_mat = new Matrix(dim, aFs);
		ret_mat.init();
		
		Map<Variable, Integer> back_link = new HashMap<Variable, Integer>();
		int hl = varsOrig.length / 2;
		for (int i=0; i<hl; ++i) {
			int i1 = i+1, i2 = i+2+hl;
			back_link.put(varsOrig[i1], new Integer(i1));
			back_link.put(varsOrig[i2], new Integer(i2));
		}
		
		// TODO: treat null in DBC
		
		for (DBC dbc : aCol) {
			int r = back_link.get(dbc.plus());
			int c = back_link.get(dbc.minus());
			
			ParamBounds f = (ParamBounds)ret_mat.get(r, c);
			f.addBound(dbc.label());
			ret_mat.set(r, c, f);
		}
		
		if (this.encoding.isDBC()) {
			ret_mat.set(0, dim/2, aFs.zero());
			ret_mat.set(dim/2, 0, aFs.zero());
		}
		
		DBM ret = new DBM(this.encoding, ret_mat, aFs);
		ret.canonize();
		return ret;
	}

	private boolean isConstrained(int inx) {
		for (int i=0; i<mat.size(); i++) {
			
			if (i==inx) // skip diagonal
				continue;
			
			if (mat.get(inx, i).isFinite() || mat.get(i, inx).isFinite())
				return true;
		}
		return false;
	}

	public void addImplicitActions(boolean[] restr) {
		if (this.encoding.isDBC()) {
			
			int encVars = mat.size() / 2;
			
			for (int i=1; i<encVars; i++) {
				if (restr[i-1]) {
					int j = i+encVars;
					this.mat.set(i,j, this.mat.get(i,j).min(fs.giveField(0)));
					this.mat.set(j,i, this.mat.get(j,i).min(fs.giveField(0)));
				}
			}
			
		} else {
			
			int vars = mat.size() / 4;
			
			for (int i=1; i<=vars; i++) {
				if (restr[i-1]) {
					int i1 = 2*i-2;
					int j1 = 2*(i+vars)-2;
					
					if (!this.isConstrained(j1)) {
						
						this.mat.set(i1,j1, this.mat.get(i1,j1).min(fs.giveField(0)));
						this.mat.set(j1,i1, this.mat.get(j1,i1).min(fs.giveField(0)));
						
						int j1b = OctagonRel.bar(j1);
						int i1b = OctagonRel.bar(i1);
						
						this.mat.set(i1b,j1b, this.mat.get(i1b,j1b).min(fs.giveField(0)));
						this.mat.set(j1b,i1b, this.mat.get(j1b,i1b).min(fs.giveField(0)));
					}
				}
			}
			
		}
	}
	public void addImplicitActionsForUnconstrained() {
		
		if (this.encoding.isDBC()) {
	
			int encVars = mat.size() / 2;
			
			for (int i=1; i<encVars; i++) {
				if (!this.isConstrained(i+encVars)) {
					this.mat.set(i, i+encVars, fs.giveField(0));
					this.mat.set(i+encVars, i, fs.giveField(0));
				}
			}
		
		} else {
			
			int vars = mat.size() / 4;
			
			for (int i=1; i<=vars; i++) {
				int i1 = 2*i-2;
				int j1 = 2*(i+vars)-2;
				
				if (!this.isConstrained(j1)) {
					
					this.mat.set(i1,j1, fs.giveField(0));
					this.mat.set(j1,i1, fs.giveField(0));
					
					int j1b = OctagonRel.bar(j1);
					int i1b = OctagonRel.bar(i1);
					
					this.mat.set(i1b,j1b, fs.giveField(0));
					this.mat.set(j1b,i1b, fs.giveField(0));
				}
			}
		}
	}

	
//	public DBM addImplicitActions(String[] newDomain) {
//		
//		String[] oldDomain = this.mat.domain();
//		List<VariableMerge<String>> V_merge1 = VariableMerge.domainMerge(oldDomain, newDomain);
//
//		DBM ret = extendBy(newDomain);
//		
//		if (this.encoding.isDBC()) {
//	
//			int encVars = newDomain.length / 2;
//			
//			for (int i=1; i<encVars; i++) {
//				if (V_merge1.get(i+encVars).iOld < 0 || !ret.isConstrained(i+encVars)) {
//					ret.mat.set(i, i+encVars, fs.giveField(0));
//					ret.mat.set(i+encVars, i, fs.giveField(0));
//				}
//			}
//		
//		} else {
//			
//			int vars = newDomain.length / 4;
//			
//			for (int i=1; i<=vars; i++) {
//				int i1 = 2*i-2;
//				int j1 = 2*(i+vars)-2;
//				
//				if (V_merge1.get(j1).iOld < 0 || !ret.isConstrained(j1)) {
//					
//					ret.mat.set(i1,j1, fs.giveField(0));
//					ret.mat.set(j1,i1, fs.giveField(0));
//					
//					int j1b = OctagonRel.bar(j1);
//					int i1b = OctagonRel.bar(i1);
//					
//					ret.mat.set(i1b,j1b, fs.giveField(0));
//					ret.mat.set(j1b,i1b, fs.giveField(0));
//				}
//			}
//		}
//		
//		return ret;
//	}
	
	private static boolean[][] initRedundancyMatrix(int size) {
		boolean[][] red = new boolean[size][size];
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				red[i][j] = false;
			}
		}
		return red;
	}
	private Matrix giveCompactMatrix(boolean[][] red) {
		Matrix ret = new Matrix(this.mat);
		
		int size = ret.size();
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				if (red[i][j])
					ret.set(i,j,fs.posInf());
			}
		}
		return ret;
	}
	
	public DBM compact_dbc_with_check() {
		DBM ret = this.compact_dbc();
		DBM tmp = new DBM(ret);
		tmp.floydWarshall();
		if (tmp.equals(this))
			return ret;
		else {
			throw new RuntimeException("heuristic for compactness failed");
			//System.exit(-1);
			//return this;
		}
		
	}
	public DBM compact_dbc_without_ckeck() {
		return this.compact_dbc();
	}
	
	// assumption: dbm is canonical and consistent (really must be canonical ???)
	public DBM compact_dbc() {
		int size = this.mat.size();
		boolean[][] red = initRedundancyMatrix(size);
		
		int nVars = size / 2;
		Field zero = fs.giveField(0);
		
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				
				if (i == j)
					continue;
				
				if ( (i == 0 && j == nVars) || (i == nVars && j == 0) )
					continue;
				
				Field ij = mat.get(i,j), ji = mat.get(j,i);
				
				if (!ij.isFinite())
					continue;
				
				for (int k=0; k<size; k++) {
					
					if (i==k || j==k)
						continue;
					
					Field ik=mat.get(i,k), ki=mat.get(k,i), jk=mat.get(j,k), kj=mat.get(k,j);
					
					if (!ik.isFinite() || !kj.isFinite())
						continue;
					
					if (!ik.plus(kj).equals(ij))
						continue;
					
					if (ij.plus(ji).equals(zero)
							|| ik.plus(ki).equals(zero)
							|| jk.plus(kj).equals(zero)) {
						
						if (red[i][k] || red[k][j])
							continue;
						// proceed only if neither ik nor kj has been marked as reduced
						
						//System.out.println(mat.domain()[i]+" "+mat.domain()[k]+" "+mat.domain()[j]);
						red[i][j] = true;
						break;
						
					} else {
						
						//System.out.println(mat.domain()[i]+" "+mat.domain()[k]+" "+mat.domain()[j]);
						red[i][j] = true;
						break;
						
					}
				}
			}
		}
		
		Matrix m_compact = this.giveCompactMatrix(red);
		
		DBM dbm_compact = new DBM(encoding, m_compact, fs);
		
		return dbm_compact;
	}
	
	public float density () {
		return mat.density();
	}

	public ConstProps inoutConst(Variable[] varsOrig, boolean in) {
		
		Collection<ConstProp> col = new LinkedList<ConstProp>();
		
		if (this.encoding.isDBC()) {
			int nVars = mat.size() / 2;
			int offset = (in)? 0 : nVars;
			int w = (in)? 0 : 1;
			for (int i=1+offset; i<nVars+offset; i++) {
				
				Field f1 = mat.get(i, 0);
				Field f2 = mat.get(0, i);
				
				if (!f1.isFinite() || !f2.isFinite())
					continue;
				
				int v1 = f1.toInt();
				int v2 = f2.toInt();
				
				if (v1*-1 == v2) {
					col.add(new ConstProp(varsOrig[i-1-w],v1));
				}
			}
		} else {
			int nVars = mat.size() / 4;
			int offset = (in)? 0 : nVars;
			for (int i=1+offset; i<=nVars+offset; i++) {
				
				Field f1 = mat.get(2*i-2, 2*i-1);
				Field f2 = mat.get(2*i-1, 2*i-2);
				
				if (!f1.isFinite() || !f2.isFinite())
					continue;
				
				int v1 = f1.toInt();
				int v2 = f2.toInt();
				
				if (v1*-1 == v2) {
					col.add(new ConstProp(varsOrig[i-1],v1/2));
				}
			}
		}
		
		return new ConstProps(col);
	}
	
	public Collection<Variable> identVars(Variable[] origVars) {
		Collection<Variable> col = new LinkedList<Variable>();
		
		Matrix m = this.mat;
		//String[] dom  = m.domain();
		if (this.encoding.isDBC()) {
			int nVars = m.size() / 2;
			for (int i=1; i<nVars; i++) {
				int j = i + nVars;
				if (!m.get(i, j).isFinite() || !m.get(j, i).isFinite())
					continue;
				if (m.get(i, j).toInt() == 0 && m.get(j, i).toInt() == 0)
					col.add(origVars[i-1]);
			}
		} else {
			int l = origVars.length / 2;
			for (int i=1; i<=l; i++) {
				int ii = 2*i-2;
				int jj = 2*(i+l)-2;
				Field f1 = m.get(ii,jj);
				Field f2 = m.get(jj,ii);
				if (!f1.isFinite() || !f2.isFinite())
					continue;
				if (f1.toInt() == 0 && f2.toInt() == 0)
					col.add(origVars[i-1]);
			}
		}
		
		return col;
	}
	
	public boolean checkSetIdentity() {
		if (this.encoding.isDBC()) {
			Matrix m = this.mat;
			int size = m.size();
			int nVars = size / 2;
			for(int i=0; i<size; i++) {
				for(int j=0; j<size; j++) {
					if (i==j) {
						if (!m.get(i, j).isFinite() || m.get(i, j).toInt() != 0)
							return false;
					} else if (Math.abs(i-j) == nVars) {
						if (!m.get(i, j).isFinite() || !m.get(j, i).isFinite())
							return false;
						if (m.get(i, j).toInt() != 0 || m.get(j, i).toInt() != 0)
							return false;
					} else if (m.get(i, j).isFinite()) {
							return false;
					}
				}
			}
			return true;
		}
		return false;
	}
	
	
//	private void addBar(boolean oct, List<Integer> l, int i) {
//		l.add(new Integer(i));
//		if (oct)
//			l.add(new Integer(OctagonRel.bar(i)));
//	}
	
	// varcnt does not include zero variable
	private void addWithCounterpart(List<Integer> l,  int i, int varcnt) {
		l.add(new Integer(i));
		if (i >= varcnt)
			l.add(new Integer(i-varcnt));
		else
			l.add(new Integer(i+varcnt));
	}
	// sizehalf includes zero String (for dbc)
	private List<Integer> getinxs(int i, int j, int sizehalf) {
		boolean dbc = this.encoding.isDBC();
		LinkedList<Integer> l = new LinkedList<Integer>();
		if (dbc) {
			if (i != 0 && i != sizehalf) {
				int ii = (i>=sizehalf)? i-2 : i-1;
				addWithCounterpart(l,ii,sizehalf-1);
			}
			if (j != 0 && j != sizehalf) {
				int jj = (j>=sizehalf)? j-2 : j-1;
				addWithCounterpart(l,jj,sizehalf-1);
			}
		} else {
			if (i / 2 == j / 2) {
				addWithCounterpart(l,i/2,sizehalf/2);
			} else {
				addWithCounterpart(l,i/2,sizehalf/2);
				addWithCounterpart(l,j/2,sizehalf/2);
			}
		}
		
		return l;
	}
	private int[] inxsWithZero_oct(Set<Integer> set, int sizehalf) {
		int[] ret = new int[set.size()*2];
		int i=0;
		for (Integer ii : set) {
			int intval = ii.intValue()*2;
			ret[i++] = intval;
			ret[i++] = OctagonRel.bar(intval);
		}
		Arrays.sort(ret);
		return ret;
	}
	private int[] inxsWithZero_dbm(Set<Integer> set, int sizehalf) {
		int[] ret = new int[set.size()+2];
		ret[0] = 0;
		ret[1] = sizehalf;
		int i=2;
		for (Integer ii : set) {
			// transform indices back (so that they include zero variable)
			int inx = ii.intValue() + 1;
			if (inx >= sizehalf)
				inx = inx + 1;
			ret[i++] = inx;
		}
		Arrays.sort(ret);
		return ret;
	}
	private int[] inxsWithZero(boolean dbc, Set<Integer> set, int sizehalf) {
		if (dbc)
			return inxsWithZero_dbm(set, sizehalf);
		else
			return inxsWithZero_oct(set, sizehalf);
	}

	// check if a constraint is implicit -- i.e. if it is of the form
	//     (x<=c1 /\ -y<=c2) -> (x-y<=c1+c2)
	//     (x<=c1 /\ y<=c2) -> (x+y<=c1+c2)
	private boolean implicit_const_dbm(int i, int j, Field f) {
		int sizehalf = this.mat.size();
		if (i!=0 && i!=sizehalf && j!=0 && j!=sizehalf) {
			Field t1 = mat.get(i, 0); // x<=c1 
			Field t2 = mat.get(0, j); // -y<=c2
			// (x-y<=c1+x2) -> (x-y<=c)
			return t1.isFinite() && t2.isFinite() && (t1.toInt()+t2.toInt() <= f.toInt());
		}
		return false;
	}
	private boolean implicit_const_oct(int i, int j, Field f) {
		if (i/2 != j/2) {
			Field t1 = mat.get(i, OctagonRel.bar(i)); 
			Field t2 = mat.get(OctagonRel.bar(j), j);
			return t1.isFinite() && t2.isFinite() && (t1.toInt()/2+t2.toInt()/2 <= f.toInt());
		}
		return false;
	}
	private boolean implicit_const(int i, int j, Field f) {
		if (this.encoding.isDBC())
			return implicit_const_dbm(i,j,f);
		else
			return implicit_const_oct(i,j,f);
	}
	
	// note: method ignores implicit constrains of the form
	//     (x<=c1 /\ -y<=c2) -> (x-y<=c1+c2)
	//     (x<=c1 /\ y<=c2) -> (x+y<=c1+c2)
	public DBM[] minPartition(Partition<Integer, Integer> vps) {
		// second type is dummy (won't be used)
		
		int size = this.mat.size();
		int sizehalf = size / 2;
		boolean dbc = this.encoding.isDBC();
		for (int i = 0; i < size; i ++) {
			
			if (dbc && i == sizehalf) // row sizehalf is same as row 0
				continue;
			
			for (int j = 0; j < size; j ++) {
				
				if (i == j)
					continue;
				
				if (dbc && j == sizehalf) // column sizehalf is same as column 0
					continue;
				
				if (dbc && ((i == 0 && j == sizehalf) || (j == 0 && i == sizehalf)))
					continue;
				
				Field f = mat.get(i, j);
				if (f.isFinite()) {
					
					if (implicit_const(i,j,f))
						continue;
					
					vps.merge(getinxs(i,j,sizehalf), null);
				}
			}
		}
		
		DBM[] ret = new DBM[vps.partitions.size()]; 
		if (vps.partitions.size() == 1) {
			return new DBM[] {this};
		}
		int i = 0;
		for (PartitionMember<Integer, Integer> vp : vps.partitions) {
			ret[i++] = this.subDBM(inxsWithZero(dbc, vp.vars, sizehalf));
		}
		
		return ret;
	}
	
	private int unboundedMeasure_dbc() {
		int size = mat.size();
		int ret = 0;
		for (int i=1; i<size; i++) {
			for (int j=1; j<size; j++) {
				if (!mat.get(i, j).isFinite())
					ret ++;
			}
		}
		return ret;
	}
	
	// TODO: ensure that both operands have the same dimensions and domains
	public Distance distance_dbc(DBM other) {
		
		DBM hull = this.hull_sameDomain(other);
		
		int size = this.mat.size();
		Field two = fs.giveField(2);
		
		Distance ret = new Distance();
		
		for (int i=1; i<size; i++) {
			for (int j=1; j<size; j++) {
				
				if (i == j)
					continue;
				
				if (!this.mat.get(i,j).isFinite())
					continue;
				
				boolean b1 = this.mat.get(i,j).less(other.mat.get(i,j), fs);
				//Field d1 = other.mat.get(i,j).minus(this.mat.get(i,j));
				
				if (!b1)
					continue;
				
				for (int k=1; k<size; k++) {
					for (int l=1; l<size; l++) {
						
						if (i == j)
							continue;
						
						if (!other.mat.get(k,l).isFinite())
							continue;
						
						boolean b2 = other.mat.get(k,l).less(this.mat.get(k,l), fs);
						boolean b3 = this.mat.get(i,j).plus(other.mat.get(k,l)).plus(two).lessEq(hull.mat.get(i,l).plus(hull.mat.get(k,j)));
						
						if (b1 && b2 && b3) {
							
							DBM hullcp = new DBM(hull);
							int cij = this.mat.get(i,j).toInt();
							int ckl = other.mat.get(k,l).toInt();
							hullcp.mat.set(j, i, fs.giveField(-cij-1));
							hullcp.mat.set(l, k, fs.giveField(-ckl-1));
							
							hullcp.canonize();
							if (hullcp.unboundedMeasure_dbc() > 0)
								ret.inf ++;
							else {
								int d1 = Math.abs(hullcp.mat.get(j, i).toInt() + hullcp.mat.get(i, j).toInt() + 1);
								int d2 = Math.abs(hullcp.mat.get(l, k).toInt() + hullcp.mat.get(k, l).toInt() + 1);
								ret.fin += d1 + d2;
							}
						}
					}
				}
			}
		}
		return ret;
		
	}

	// assumption: operands have same domain
	public boolean canPreciseMerge(DBM other) {
		DBM hull = this.hull_sameDomain(other);
		
		if (this.encoding.isOct()) {
			int size = this.mat.size();
			Field two = fs.giveField(2);
			for (int i=1; i<size; i++) {
				for (int j=1; j<size; j++) {
					
					if (i == j)
						continue;
					
					Field eij = fs.giveField((j == OctagonRel.bar(i))? 2 : 1);
					
					if (!this.mat.get(i,j).isFinite())
						continue;
					
					boolean b1a = this.mat.get(i,j).plus(eij).lessEq(other.mat.get(i,j));
					
					if (!b1a) continue;
					
					for (int k=1; k<size; k++) {
						for (int l=1; l<size; l++) {
							
							if (i == j)
								continue;
							
							if (!other.mat.get(k,l).isFinite())
								continue;
							
							Field ekl = fs.giveField((l == OctagonRel.bar(k))? 2 : 1);
							
							int ib = OctagonRel.bar(i);
							int jb = OctagonRel.bar(j);
							int kb = OctagonRel.bar(k);
							int lb = OctagonRel.bar(l);
							
							boolean b1b = other.mat.get(k,l).plus(ekl).lessEq(this.mat.get(k,l));
							if (!b1b) continue;
							boolean b2a = this.mat.get(i,j).plus(other.mat.get(k,l)).plus(eij).plus(ekl).lessEq(hull.mat.get(i,l).plus(hull.mat.get(k,j)));
							if (!b2a) continue;
							boolean b2b = this.mat.get(i,j).plus(other.mat.get(k,l)).plus(eij).plus(ekl).lessEq(hull.mat.get(i,kb).plus(hull.mat.get(lb,j)));
							if (!b2b) continue;
							
							boolean b3a = (two.times(this.mat.get(i,j))).plus(other.mat.get(k,l)).plus(two.times(eij)).plus(ekl).lessEq(hull.mat.get(i,l).plus(hull.mat.get(k,ib)).plus(hull.mat.get(jb, j)));
							if (!b3a) continue;
							boolean b3b = (two.times(this.mat.get(i,j))).plus(other.mat.get(k,l)).plus(two.times(eij)).plus(ekl).lessEq(hull.mat.get(k,j).plus(hull.mat.get(jb,l)).plus(hull.mat.get(i, ib)));
							if (!b3b) continue;
							
							boolean b4a = this.mat.get(i,j).plus(two.times(other.mat.get(k,l))).plus(eij).plus(two.times(ekl)).lessEq(hull.mat.get(k,j).plus(hull.mat.get(i,kb)).plus(hull.mat.get(lb, l)));
							if (!b4a) continue;
							boolean b4b = this.mat.get(i,j).plus(two.times(other.mat.get(k,l))).plus(eij).plus(two.times(ekl)).lessEq(hull.mat.get(i,l).plus(hull.mat.get(lb,j)).plus(hull.mat.get(k, kb)));
							if (!b4b) continue;
							
							return false;
						}
					}
				}
			}
			return true;
		} else {
			int size = this.mat.size();
			Field two = fs.giveField(2);
			for (int i=1; i<size; i++) {
				for (int j=1; j<size; j++) {
					
					if (i == j)
						continue;
					
					if (!this.mat.get(i,j).isFinite())
						continue;
					
					boolean b1 = this.mat.get(i,j).less(other.mat.get(i,j), fs);
					
					if (!b1)
						continue;
					
					for (int k=1; k<size; k++) {
						for (int l=1; l<size; l++) {
							
							if (i == j)
								continue;
							
							if (!other.mat.get(k,l).isFinite())
								continue;
							
							boolean b2 = other.mat.get(k,l).less(this.mat.get(k,l), fs);
							boolean b3 = this.mat.get(i,j).plus(other.mat.get(k,l)).plus(two).lessEq(hull.mat.get(i,l).plus(hull.mat.get(k,j)));
							
							if (b1 && b2 && b3)
								return false;
						}
					}
				}
			}
			return true;
		}
	}
	
	public DBM preORpostimage(boolean pre) { 
		DBM ret = new DBM(this);
		Matrix retmat = ret.mat;
		// diagonal to 0, other entries to infinity
		retmat.init();
		int n = retmat.size()/2;
		
		int s,e;
		s = (pre)? 0 : n;
		e = s + n;
		
		for (int i=s; i<e; i++) {
			for (int j=s; j<e; j++) {
				retmat.set(i, j, this.mat.get(i, j));
			}
		}
		
		if (this.encoding.isDBC()) {
			retmat.set(0, n, fs.giveField(0));
			retmat.set(n, 0, fs.giveField(0));
			if (pre) {
				for (int i=1; i<n; i++) {
					retmat.set(n, i, retmat.get(0, i));
					retmat.set(i, n, retmat.get(i, 0));
				}
			} else {
				for (int i=n+1; i<2*n; i++) {
					retmat.set(0, i, retmat.get(n, i));
					retmat.set(i, 0, retmat.get(i, n));
				}
			}
		}
		return ret;
	}
	// projects the matrix
	// NOTE: DBM is not made canonic prior to projection
	public DBM preimage() {
		return preORpostimage(true);
	}
	public DBM postimage() {
		return preORpostimage(false);
	}
	public DBM projectToUpdate_dbc() {
		int n2 = this.mat.size();
		int n1 = n2 / 2;
		DBM ret = new DBM(this.encoding, n2, this.fs);
		ret.mat.init();
		
		for (int i=1; i<n1; i++) {
			for (int j=1; j<n1; j++) {
				// copy B part (X--X')
				ret.mat.set(i, n1+j, this.mat.get(i, n1+j));
				// copy C (X'--X)
				ret.mat.set(n1+i, j, this.mat.get(n1+i, j));
			}
		}
		
		return ret;
	}
	// keep -- bitset with range 0..n-1
	// clears the relevant parts of B,C part of the matrix (entries for variables that are not masked in 'keep')
	public DBM projectUpdatePart_dbc(BitSet keep) {
		int n2 = this.mat.size();
		int n1 = n2 / 2;
		DBM ret = new DBM(this);
		for (int i = keep.nextClearBit(0); i < n1; i = keep.nextClearBit(i+1)) {
			for (int j=0; j<n1; j++) {
				ret.mat.set(i+n1,j,fs.initVal());
				ret.mat.set(j+n1,i,fs.initVal());
				ret.mat.set(j,i+n1,fs.initVal());
				ret.mat.set(i,j+n1,fs.initVal());
			}
		}
		return ret;
	}

	
	public Substitution equality_subst_oct(Variable[] unp_p) {
		int n3 = this.mat.size();
		int n2 = n3 / 2;
		int n1 = n2 / 2;
		
		Substitution s = new Substitution();
		BitSet done = new BitSet(n1);
		for (int i = done.nextClearBit(0); i < n2; i = done.nextClearBit(i+1)) {
			done.set(i);
			int ip = 2*i;
			int im = 2*i+1;
			for (int j = done.nextClearBit(i); j < n2; j = done.nextClearBit(j+1)) {
				// xj=xi+d  (xjp-xip=d)
				// xj=-xi+d  (xjp-xim=d)
				int jp = 2*j;
				//int jm = 2*j+1;
				LinearConstr c = null;
				if (areInverse(mat.get(jp, ip),mat.get(ip, jp))) {
					c = new LinearConstr();
					c.addLinTerm(LinearTerm.constant(mat.get(jp, ip).toInt()));
					c.addLinTerm(new LinearTerm(unp_p[i],1));
				}
				if (areInverse(mat.get(jp, im),mat.get(im, jp))) {
					c = new LinearConstr();
					c.addLinTerm(LinearTerm.constant(mat.get(jp, im).toInt()));
					c.addLinTerm(new LinearTerm(unp_p[i],-1));
				}
				if (c != null) {
					done.set(j);
					s.put(unp_p[j], c);
				}
			}
		}
		
		return s;
	}
}
