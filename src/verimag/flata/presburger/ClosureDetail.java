package verimag.flata.presburger;

import java.util.Map;


public class ClosureDetail {

	/**
	 * c=0 --> relation gets inconsistent at R^b (clearly, in this case b>0, since R^0 is always consistent)
	 *         (e.g. c=0,b=3 -> R^3 is false)
	 * c>0 --> b in {0, ...}
	 * 
	 * note: relation may get inconsistent even if c>0
	 * (but no later than R^(b + c*last_cons_K + 1) if last_cons_K is not infinity)
	 */
	
	public int b; // relation gets periodic at R^b
	public int c; // period

	// relations describing the prefix of the periodic sequence (R^i)_i
	// R^1, ..., R^(b-1)
	public Relation[] prefix; // length = b-1
	
	// relations describing the purely periodic part of the sequence (R^i)_i
	// R^(b+kc), R^(b+1+kc), ..., R^(b+c-1+kc)
	// always c disjunct
	public boolean[] periodic_noK; // is parameter used in the disjunct?
	public LinearRel[] periodic_param; // parametric disjuncts
	public Relation[][] periodic_rel; // disjuncts after elimination of the parameter 'k'
	
	public Variable parameter; // the parameter used in the description of purely periodic portion of the sequence
	// note: parameter is implicitly existentially quantified and interpreted over non-negative integers {0,1,2, ...} 
	
	public Relation[] all;
	
	public DBM dbm;
	public Map<Integer, DBM> powers;
	public int max_power; // largest computed power
	public LinearTerm[] substitution;
	public IntegerInf last_cons_K; // last consistent K: R^(b + c*last_cons_K + 1)) surely inconsistent
	
	public boolean isOmegaConsistent() { 
		return c != 0 && !last_cons_K.isFinite();
	}
	public int inconsistentAt() {
		if (c == 0)
			return b;
		else
			return b + (last_cons_K.toInt() * c) + 1;
	}
	
	public ClosureDetail(int inconsistent, Variable aParameter, DBM adbm, Map<Integer, DBM> apowers, int amax_power, LinearTerm[] asubstitution, IntegerInf alast_cons_K) {
		this(inconsistent, 0, aParameter, adbm, apowers, amax_power, asubstitution, alast_cons_K);
	}
	public ClosureDetail(int ab, int ac, Variable aParameter, DBM adbm, Map<Integer, DBM> apowers, int amax_power, LinearTerm[] asubstitution, IntegerInf alast_cons_K) {
		b = ab;
		c = ac;
		parameter = aParameter;
		prefix = new Relation[Math.max(b-1,0)];
		periodic_noK = new boolean [c];
		periodic_param = new LinearRel[c];
		periodic_rel = new Relation[c][];
		
		dbm = adbm;
		powers = apowers;
		max_power = amax_power;
		substitution = asubstitution;
		last_cons_K = alast_cons_K;
	}
	
//	public static void cd_finish(ClosureDetail cd) {
//		
//		////////////////////////////  prefix //////////////////////////////////
//		
//		int b = cd.b;
//		int c = cd.c;
//		int max = b-1;
//		Map<Integer, DBM> com_hash = cd.powers;
//		LinearTerm[] substitution = cd.substitution;
//		IntegerInf maxK = cd.last_cons_K;
//		boolean isOctagon = cd.dbm.encoding().isOct();
//		Variable v_k = cd.parameter;
//		
//		
//		int tot = 0;
//		
//		for (int i = 1; i <= max; i++) {
//			
//			DBM m_i = com_hash.get(new Integer(i));
//			
//			if (!m_i.isConsistent())
//				throw new RuntimeException();
//			
//			Relation[] tmp = DeltaConvert.mat2modRels_general(m_i, DeltaConvert.MyEnum.OUT_FROM_DBM_CLOSURE, substitution, isOctagon).rels;
//				
//			if (tmp.length != 1) {
//				throw new RuntimeException("closure: internal error");
//			}
//				
//			cd.prefix[i-1] = tmp[0]; // exactly one relation
//			
//			tot++;
//		}
//		
//		
//		////////////////////////////  periodic part //////////////////////////////////
//		
//		// R^(p+kl+r), r \in {0, ..., l-1}  ;  (R^p+k*delta) \circ R^r 
//		
//		for (int r = 0; r < c; ++r) {
//			
//			DBM m_pr = com_hash.get(new Integer(b+r));
//			
//			DBM m_prl = com_hash.get(new Integer(b+r+c));
//			DBM delta = m_prl.minus(m_pr);
//			
//			DBM m_pr_k_delta = DeltaConvert.mat2matParam(m_pr, delta, v_k);
//			
//			if (!isOctagon) {
//				DBM ret = m_pr_k_delta.compact_dbc_without_ckeck();
//				DBM tmp = new DBM(ret);
//				tmp.floydWarshall();
//				
//				if (DeltaClosure.paramBoundsEqual(m_pr_k_delta.mat(),tmp.mat(),maxK)) {
//					m_pr_k_delta = ret;
//				} else {
//					throw new RuntimeException("heuristic for compactness failed");
//				}
//			}
//			
//			DeltaDisjunct cl_disj = DeltaConvert.mat2modRels(m_pr_k_delta, DeltaConvert.MyEnum.OUT_FROM_DBM_CLOSURE, substitution, isOctagon, maxK);
//			
//			cd.periodic_noK[r] = cl_disj.noK;
//			cd.periodic_param[r] = cl_disj.periodic_param;
//			cd.periodic_rel[r] = cl_disj.rels;//tmp.toArray(new Relation[0]);
//			
//			tot += cl_disj.rels.length;
//		}
//		
//		cd.all = new Relation[tot];
//		int l = cd.prefix.length;
//		int i = 0;
//		System.arraycopy(cd.prefix, 0, cd.all, i, l);
//		i += l;
//		for (int r = 0; r < c; ++r) {
//			Relation[] rr = cd.periodic_rel[r];
//			l = rr.length;
//			System.arraycopy(rr, 0, cd.all, i, l);
//			i += l;
//		}
//	}
	
	
	
}
