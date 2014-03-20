package verimag.flata.acceleration.delta;

import java.util.*;

import verimag.flata.presburger.*;

public class DeltaClosure implements verimag.flata.acceleration.Accelerator {
	
	public static int DEBUG_NO = 0;
	public static int DEBUG_LOW = 1;
	public static int DEBUG_MEDIUM = 2;
	public static int DEBUG_FULL = 3;
	public static int DEBUG_LEVEL = DEBUG_NO;

	private static boolean PRINT_DETAILS = false;
	public static boolean delta_parametricFW = true;
	
	public static Variable v_k;
	public static LinearConstr lc_k;

	static {
		setParam("$k");
	}
	
	public static void setParam(String aName) {
		v_k = VariablePool.createSpecial(aName);
		// k >= 0
		lc_k = new LinearConstr();
		lc_k.addLinTerm(new LinearTerm(v_k,-1));
	}
	
	private ClosureDetail cd;
	
	private boolean is_cl_plus = false; // R^+ instead of R^*
	private Variable[] varsOrig;
	private LinearTerm[] subst;
	private boolean isOctagon;
	
	// store for termination analysis
	private DBM m_rhs;
	
	
	static FieldStaticInf deltaFS() {
		return ParamBoundsStatic.fs();
	}
	static Field deltaBound(int i_f1, int i_f2) {
		return new ParamBounds(i_f1, i_f2);
	}
	
	public static boolean paramBoundsEqual(Matrix m1, Matrix m2, IntegerInf maxK) {
		int size = m1.size();
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				ParamBounds pbs1 = (ParamBounds)m1.get(i, j);
				ParamBounds pbs2 = (ParamBounds)m2.get(i, j);
				
				if (!pbs1.minEquals(pbs2, maxK))
					return false;
			}
		}
		return true;
	}

	static LinearConstr maxKconstr(int maxK) {
		LinearConstr lc = new LinearConstr();
		
		lc.addLinTerm(new LinearTerm(v_k,1));
		lc.addLinTerm(new LinearTerm(null,-maxK));
		
		return lc;
	}
	
	private static int computePowers(DBM m1, int n, Map<Integer, DBM> hash, int max) {
		DBM ret = hash.get(new Integer(n));
		
		if (ret != null)
			return -1;
		
		ret = hash.get(new Integer(max));
		
		for (int i = max+1; i <= n; ++i) {
			
			List<Field> diagonal = new LinkedList<Field>();
			ret = ret.compose(m1, diagonal);
			
			//if (!ret.isConsistent())
			if (IntegerInf.hasNegative(diagonal))
				return i;
			
			hash.put(new Integer(i), ret);
		}
		
		return -1;
	}
	
	// a + bk <= 0
	private static IntegerInf extractParamBounds(Collection<ParamBound> aCol) {
		
		IntegerInf ret = new IntegerInf(FieldType.POS_INF);
		
		for (ParamBound pb : aCol) {
			int a = pb.intVal();
			int b = pb.paramCoef();
			if (b == 0) {
				if (a > 0)
					throw new RuntimeException("internal error: contradiction constraint is present");
				else
					continue;
			} else {
				if (b < 0) {
					if ((a / (-b)) <= 0)
						continue;
					else 
						throw new RuntimeException("internal error: unexpected constraint");
				}
				
				IntegerInf tmp = new IntegerInf((-a) / b);
				if (!ret.isFinite() || tmp.lessEq(ret))
					ret = tmp;
			}
		}
		return ret;
	}
	
	
	private static class Check {
		IntegerInf maxK;
		boolean found;
	}
	
	private Check check(int b, int c, Map<Integer, DBM> com_hash, boolean isOctagon, DBM m) {
		
		Check ret = new Check();
		
		DBM m_p, m_pl, m_pll;
		DBM delta1;
		//DBM delta2;
		
		m_p = com_hash.get(b);
		m_pl = com_hash.get(b+c);
		m_pll = com_hash.get(b+2*c);

		if (comparable(m_p,m_pl,m_pll)) {
			
			DBM.Solve_axb s1 = m_p.solve_axb(m_pl);
			DBM.Solve_axb s2 = m_pl.solve_axb(m_pll);
			
			if (s1.solved() && s2.solved() && s1.dbm().equals(s2.dbm())) {
			
			//delta1 = m_pl.minus(m_p);
			//delta2 = m_pll.minus(m_pl);
			//if (delta1.equals(delta2)) { // induction basis holds
				
				delta1 = s1.dbm();
				
				// (R^p + k*delta) \circ R^l
				DBM A_c = com_hash.get(c);
				DBM A_b = com_hash.get(b);
				
				if (!isOctagon) {
				
					DBM m_lhs;
					
					//Matrix virt_cycles = null;
					
					if (DeltaClosure.delta_parametricFW) { // parametric FW
						DBM m_pkdelta_param = DeltaConvert.mat2matParam(A_b, delta1, v_k);
						DBM m_l_param = DeltaConvert.mat2matParam(A_c, null, v_k);
						List<Field> diagonal = new LinkedList<Field>();
						
						// virtual cycles -- obtain conditions for their negativity 
						// either of conditions holds, the relation terminates
						//Matrix in = m_l_param.mat();
						//virt_cycles = new Matrix(in.size()/2, in.fs());
						
						m_lhs = m_pkdelta_param.compose(m_l_param, diagonal);
						
						ret.maxK = ParamBounds.extractUpperBound(diagonal);
						
					} else { // FM elimination
						LinearRel lcs_tmp1 = DeltaConvert.mat2LCsWithParam(A_b,delta1, v_k, subst);
						LinearRel lcs_tmp2 = DeltaConvert.mat2LCs(A_c, subst);
						LinearRel lcs_lhs = LinearRel.tryComposeFM(lcs_tmp1, lcs_tmp2);
						//System.out.println(lcs_lhs.toSBClever(Variable.ePRINT_prime));
						
						DBCParam aa = lcs_lhs.toDBCParam(v_k);
						ret.maxK = DeltaClosure.extractParamBounds(aa.col_param);

						m_lhs = m.createDBM(aa.col_dbc, deltaFS(), varsOrig);
					}
					
					// R^p + delta + k*delta
					DBM A_pdelta = A_b.plus(delta1);
					m_rhs = DeltaConvert.mat2matParam(A_pdelta, delta1, v_k);

					ret.found = DeltaClosure.paramBoundsEqual(m_rhs.mat(), m_lhs.mat(), ret.maxK);
					
					if (ret.found && PRINT_DETAILS) {
						System.out.println("M^b");
						System.out.println(com_hash.get(new Integer(b)));
						System.out.println("M^(b+c)");
						System.out.println(com_hash.get(new Integer(b+c)));
						System.out.println("M^(b+2c)");
						System.out.println(com_hash.get(new Integer(b+2*c)));
						System.out.println("Lambda");
						System.out.println(delta1);
						System.out.println("Universal query LHS:");
						System.out.println(m_lhs);
						System.out.println("Universal query RHS:");
						System.out.println(m_rhs);
					}
					
				} else {
					
					DBM m_pkdelta_param = DeltaConvert.mat2matParam(A_b, delta1, v_k);
					DBM m_l_param = DeltaConvert.mat2matParam(A_c, null, v_k);
					
					DBM.SplitParamEvenOdd lhs = m_pkdelta_param.composeParamOct(m_l_param);
					
					IntegerInf maxK_even = ParamBounds.extractUpperBound(lhs.diagonal_even);
					IntegerInf maxK_odd = ParamBounds.extractUpperBound(lhs.diagonal_odd);
					
					if (maxK_even.isFinite() && maxK_odd.isFinite()) {
						int i_even  = maxK_even.toInt();
						int i_odd  = maxK_odd.toInt();
						ret.maxK = new IntegerInf(Math.max(i_even*2, i_odd*2-1));
					} else if (maxK_even.isFinite()) {
						int i_even  = maxK_even.toInt();
						ret.maxK = new IntegerInf(i_even*2);
					} else if (maxK_odd.isFinite()){
						int i_odd  = maxK_odd.toInt();
						ret.maxK = new IntegerInf(i_odd*2-1);
					} else {
						ret.maxK = IntegerInf.posInf();
					}
					
					// R^p + delta + k*delta
					DBM A_pdelta = A_b.plus(delta1);
					m_rhs = DeltaConvert.mat2matParam(A_pdelta, delta1, v_k);
					
					DBM.SplitParamEvenOdd rhs = DBM.splitParamDBM(m_rhs);
					
					boolean found_even = DeltaClosure.paramBoundsEqual(rhs.even.mat(), lhs.even.mat(), ret.maxK);
					boolean found_odd = DeltaClosure.paramBoundsEqual(rhs.odd.mat(), lhs.odd.mat(), ret.maxK);
					
					ret.found = found_even && found_odd;
				}
			}
		}
		return ret;
	}

	// computes weakest initial condition for non-termination
	private Matrix weakestInitCond(Matrix paths) {
		
		Matrix nonterm = new Matrix(paths.size(), IntegerInfStatic.fs());
		nonterm.init();
		
		// parameter matrix contains paths between unprimed nodes
		// M[xi,xj]: (xi -> xj), M[xi,zero]: M(xi -> zero), M[zero,xi]: M(zero -> xi)
		
		// virtual cycles:
		// xi_0 - xj_0 + m[xj,xi]
		// xi_0 + m[zero,xi]
		// -xi_0 + m[xi,zero]
		
		// non-termination condition -- 1) - 4) holds, for all i,j:
		//   1) for every cycle: cycle(k) >= 0 for all k  (*-consistency)
		//   2) xi_0 - xj_0 + m[xj,xi] >= 0 
		//   3) xi_0 + m[zero,xi] >= 0      
		//   4) -xi_0 + m[xi,zero] >= 0     
		
		// termination condition -- any of the following, for some i,j:
		//   1> for some cycle: cycle < 0 (*-inconsistency)
		//   2) xi_0 - xj_0 + m[xj,xi] < 0 
		//   3) xi_0 + m[zero,xi] < 0     
		//   4) -xi_0 + m[xi,zero] < 0    

		
		// compute termination condition (weakest ~ max over cycles)
		
		// it may happen that there is a path from xi to xj with strictly decreasing weight in in the parameter k
		// --> then the relation always terminates
		// as a result:
		//   if relation always terminates, the precondition for termination is True and the input space for which there is 
		//       non-terminating computation is False
		//   if the relation doesn't terminate for all inputs, then the matrix m computed below is interpreted as a disjunction
		//       of constraints -- satisfaction of any of them will lead to termination. Hence, the weakest condition for termination
		//       is disjunctive and the input space for which there is non-terminating computation is negation of the weakest condition
		
		int n = paths.size() / 2; // check paths between unprimed variables only
		
		IntegerInf m[][] = new IntegerInf[n][n]; // initialized to null
		// null represents -infinity
		// always_term could be represented as +inf, but we end the cycle analysis immediately instead and return the result
		// diagonal is not used -- already analyzed (if maxK is finite, it tells when the relation gets inconsistent -- at R^(maxK+1))
		
		
		boolean always_term = !cd.isOmegaConsistent(); // is always terminating?
		
		if (!always_term) {
			LL: for (int i=0; i<n; i++) {
			
				for (int j=0; j<n; j++) {
				
					if (i == j) {
						continue;
					}
					
					m[i][j] = null; // false initially
					
					// perform existential quantifier elimination for this special case
					ParamBounds pbs = (ParamBounds)paths.get(j, i);
					for (ParamBound pb : pbs.paramBounds()) {
						int a = pb.paramCoef();
						int b = pb.intVal();
						if (a >= 0) {
							if (m[i][j] == null)
								m[i][j] = IntegerInf.giveField(-b-1);
							else
								m[i][j] = m[i][j].max(IntegerInf.giveField(-b-1));
						} else { // true
							// +inf
							m[i][j] = IntegerInf.posInf();
							always_term = true;
							break LL;
						}
					}
				}
			}
		}
		
		// matrix is a disjunction \/ ti<=ci saying termination condition
		//   (satisfying any of them means termination -> so, if any entry 
		//    contains +inf, non-termination space is false)
		// non-termination is negation of the condition, i.e. /\ -ti<=-c1-1
		// so, just swap indices when printing 

		
		// non-termination condition: negate termination condition
		
		if (always_term) {
			
			return null;
			
		} else {
			
			for (int i=0; i<n; i++) {
				for (int j=0; j<n; j++) {
					if (i==j) {
						nonterm.set(i, j, IntegerInf.giveField(0));
					} else {
						if (m[j][i] == null) {
							nonterm.set(i, j, IntegerInf.posInf());
						} else {
							int tmp = - m[j][i].toInt() - 1; 
							nonterm.set(i, j, IntegerInf.giveField(tmp));
						}
					}
				}
			}
			
			return nonterm;
		}
	}

	// joins closures of partitions into one closure (with common period, ...)
	public static ClosureDetail joined_closure(ClosureDetail[] cdarr) {
		
		if (cdarr.length == 1) { // avoid useless work
			cd_finish(cdarr[0]);
			return cdarr[0];
		}
		
		
		if (DEBUG_LEVEL >= DEBUG_LOW)
			System.out.println("Joining closures of partitions");
		
		
		
		
		// join detailed closures into one for the whole composite relation

		// find common inconsistency (MIN), period (LCM) and prefix (MAX)
		int b = -1;
		int c = -1;
		int inconsistentAt = -1;
		boolean expl = true; // periodicity exploited in all partitions
		for (ClosureDetail cd : cdarr) {

			expl &= cd.c > 0;

			if (!cd.isOmegaConsistent()) {
				if (inconsistentAt < 0)
					inconsistentAt = cd.inconsistentAt();
				else
					inconsistentAt = Math.min(inconsistentAt, cd
							.inconsistentAt());
			}

			// when the hope for exploiting periodicity hasn't vanished yet
			if (expl) {
				
				if (b < 0) {
					b = cd.b;
					c = cd.c;
				} else {
					b = Math.max(b, cd.b);
					c = LinearConstr.lcm(c, cd.c);
				}
			}
		}

		if (expl && inconsistentAt > 0) {
			expl &= b + c < inconsistentAt;
		}

		if (!expl) {
			c = 0;
		}

		
		
		// TODO: redundant line
		expl = (c>0) && (inconsistentAt < 0 || b+c < inconsistentAt); // exploit periodicity
		boolean omega_inconsistent = (c==0) || (c>0 && inconsistentAt>0);
		
		// lambdas for all closures
		DBM[][] lambdas = new DBM[cdarr.length][];
		for (int i=0; i<cdarr.length; i++) {
			ClosureDetail cd = cdarr[i];
			
			// compute all needed powers
			int max_power = expl? (b + cd.c) : inconsistentAt;
			DeltaClosure.computePowers(cd.dbm, max_power, cd.powers, cd.max_power);
			cd.max_power = max_power;
			
			if (expl) {
				// compute all needed lambdas
				lambdas[i] = new DBM[cd.c];
				for (int ii=0; ii<cd.c; ii++) {
					DBM tmp = cd.powers.get(cd.b+ii).solve_axb(cd.powers.get(cd.b+cd.c+ii)).dbm();
					if (tmp == null) {
						throw new RuntimeException("internal error");
					}
					lambdas[i][ii] = tmp;
					//lambdas[i][ii] = cd.powers.get(cd.b+cd.c+ii).minus(cd.powers.get(cd.b+ii));
				}
			}
		}
		
		int pref_len = expl? b : inconsistentAt;
		
		ClosureDetail cdd;
		if (!expl) {
			cdd = new ClosureDetail(pref_len, cdarr[0].parameter, null, null, -1, null, null);
		} else {
			cdd = new ClosureDetail(b, c, cdarr[0].parameter, null, null, -1, null, null);
		}
		
		Collection<Relation> col = new LinkedList<Relation>();
		
		for (int i1=1; i1<pref_len; i1++) {
			Relation rel = null;
			for (int i2=0; i2<cdarr.length; i2++) {
				ClosureDetail cd = cdarr[i2];
				DBM m = cd.powers.get(i1);
				boolean isOct = cd.dbm.encoding().isOct();
				Relation tmp = DeltaConvert.mat2modRels_general(m, DeltaConvert.MyEnum.OUT_FROM_DBM_CLOSURE, cd.substitution, isOct).rels[0];
				if (rel == null) {
					rel = tmp;
				} else {
					rel = rel.intersect(tmp)[0]; // TODO: WHY not to use the fact that relation was partitioned ???? !!!!!!!
				}
			}
			cdd.prefix[i1-1] = rel;
			col.add(rel);
		}
		
		if (expl) {
			
			IntegerInf maxK;
			if (omega_inconsistent) {
				int tmp = (inconsistentAt-b) / c;
				if ((inconsistentAt-b) % c != 1)
					tmp += 1;
				maxK = new IntegerInf(tmp);
			} else {
				maxK = IntegerInf.posInf();
			}
			
			for (int i1=0; i1<c; i1++) {
				
				for (int i2=0; i2<cdarr.length; i2++) {
					ClosureDetail cd = cdarr[i2];
					int dist = b+i1-cd.b;
					int d = dist / cd.c;
					int e = dist % cd.c;
					int f = c / cd.c;
					
					DBM m1 = cd.powers.get(cd.b+e);
					DBM m2 = lambdas[i2][e].times(d);
					DBM m3 = lambdas[i2][e].times(f);
					
					boolean isOct = m1.encoding().isOct();
					DBM m_pr_k_delta = DeltaConvert.mat2matParam(m1.plus(m2), m3, v_k);
					m_pr_k_delta = m_pr_k_delta.compact_dbc_without_ckeck();
					
					int tmp = DeltaClosure.DEBUG_LEVEL;
					DeltaClosure.DEBUG_LEVEL = DeltaClosure.DEBUG_NO;
					DeltaDisjunct cl_disj = DeltaConvert.mat2modRels(m_pr_k_delta, DeltaConvert.MyEnum.OUT_FROM_DBM_CLOSURE, cd.substitution, isOct, maxK);
					DeltaClosure.DEBUG_LEVEL = DeltaClosure.DEBUG_NO;
					DeltaClosure.DEBUG_LEVEL = tmp;
					
					if (cdd.periodic_param[i1] == null) {
						cdd.periodic_noK[i1] = cl_disj.noK;
						cdd.periodic_param[i1] = cl_disj.periodic_param;
					} else {
						cdd.periodic_noK[i1] &= cl_disj.noK;
						cdd.periodic_param[i1] = cdd.periodic_param[i1].intersect(cl_disj.periodic_param)[0].toLinearRel();
					}
					
				}
				
				cdd.periodic_rel[i1] = cdd.periodic_param[i1].existElim1(cdd.parameter);
				for (Relation r : cdd.periodic_rel[i1])
					col.add(r);
				
				if (DEBUG_LEVEL >= DEBUG_LOW) {
					System.out.println("par: "+cdd.periodic_param[i1]);
					System.out.println("nopar: "+Arrays.toString(cdd.periodic_rel[i1]));
				}
			}
		}
		
		cdd.all = col.toArray(new Relation[0]);
		//cd_finish(cdd);
		return cdd;
	}
	
	// computes R+ or R* (R* only if the sequence (R^i)_i is periodic for b=0 and some c 
	// (i.e. the sequence is periodic from the very beginning R^0)
	// m assumed to be canonical
	private void closure(DBM m) {
		
		if (!m.isConsistent()) {
			throw new RuntimeException();
		}
		
		Map<Integer, DBM> com_hash = new HashMap<Integer, DBM>(); // already computed matrices 
		
		DBM identity = m.identity();
		com_hash.put(0, identity);
		
		com_hash.put(1, m);
		int max = 1; // maximal i s.t. R^i is computed in com_hash
		
		Check check = new Check();
		check.found = false;
		
		// initialize with something, so that compiler doesn't protest
		int b = 1;
		int c = 1;
		
		while(!check.found) {
			
			if (DEBUG_LEVEL >= DEBUG_FULL) {
				System.out.println("#################################");
				System.out.println("computation for: p="+b+", l="+c);
			}
			
			int max_next = b+2*c;
			int inconsistent = computePowers(m, max_next, com_hash, max);
			max = (inconsistent < 0) ? max_next : inconsistent-1;
			if (inconsistent >= 0) {
				
				cd = new ClosureDetail(inconsistent, v_k, m, com_hash, max, subst, null);
				return;
			}
			
			check = check(b,c,com_hash,isOctagon,m);
				
			if (!check.found) {
				if (b == c) {
					b ++;
					c = 1;
				} else {
					c ++;
				}
			}
		}
		
		if (b == c) {
			
			int bmin = this.is_cl_plus? 1 : 0;
			for (int bb = b; bb >= bmin; bb--) {
				Check check2 = check(bb,c,com_hash,isOctagon,m);
				if (check2.found) {
					b = bb;
					check = check2;
				} else {
					break;
				}
			}
		}

		check.maxK = check.maxK.plus(new IntegerInf(1));
		
		
		if (DEBUG_LEVEL >= DEBUG_LOW)
			System.out.println("Transitive closure computed (p="+b+", l="+c+"):");
		
		cd = new ClosureDetail(b, c, v_k, m, com_hash, max, subst, check.maxK);
	}
	
	public static void cd_finish(ClosureDetail cd) {
		
		////////////////////////////  prefix //////////////////////////////////
		
		int b = cd.b;
		int c = cd.c;
		int max = b-1;
		Map<Integer, DBM> com_hash = cd.powers;
		LinearTerm[] substitution = cd.substitution;
		IntegerInf maxK = cd.last_cons_K;
		boolean isOctagon = cd.dbm.encoding().isOct();
		
		
		int tot = 0;
		
		for (int i = 1; i <= max; i++) {
			
			DBM m_i = com_hash.get(new Integer(i));
			
			if (!m_i.isConsistent())
				throw new RuntimeException();
			
			Relation[] tmp = DeltaConvert.mat2modRels_general(m_i, DeltaConvert.MyEnum.OUT_FROM_DBM_CLOSURE, substitution, isOctagon).rels;
				
			if (tmp.length != 1) {
				throw new RuntimeException("closure: internal error");
			}
				
			cd.prefix[i-1] = tmp[0]; // exactly one relation
			
			tot++;
		}
		
		
		////////////////////////////  periodic part //////////////////////////////////
		
		// R^(p+kl+r), r \in {0, ..., l-1}  ;  (R^p+k*delta) \circ R^r 
		
		for (int r = 0; r < c; ++r) {
			
			DBM m_pr = com_hash.get(new Integer(b+r));
			
			DBM m_prl = com_hash.get(new Integer(b+r+c));
			
			DBM.Solve_axb s1 = m_pr.solve_axb(m_prl);
			if (!s1.solved())
				throw new RuntimeException("internal error");
			DBM delta = s1.dbm();
			//DBM delta = m_prl.minus(m_pr);
			
			DBM m_pr_k_delta = DeltaConvert.mat2matParam(m_pr, delta, v_k);
			
			if (!isOctagon) {
				DBM ret = m_pr_k_delta.compact_dbc_without_ckeck();
				DBM tmp = new DBM(ret);
				tmp.floydWarshall();
				
				if (DeltaClosure.paramBoundsEqual(m_pr_k_delta.mat(),tmp.mat(),maxK)) {
					m_pr_k_delta = ret;
				} else {
					throw new RuntimeException("heuristic for compactness failed");
				}
			}
			
			DeltaDisjunct cl_disj = DeltaConvert.mat2modRels(m_pr_k_delta, DeltaConvert.MyEnum.OUT_FROM_DBM_CLOSURE, substitution, isOctagon, maxK);
			
			cd.periodic_noK[r] = cl_disj.noK;
			cd.periodic_param[r] = cl_disj.periodic_param;
			cd.periodic_rel[r] = cl_disj.rels;//tmp.toArray(new Relation[0]);
			
			tot += cl_disj.rels.length;
		}
		
		cd.all = new Relation[tot];
		int l = cd.prefix.length;
		int i = 0;
		System.arraycopy(cd.prefix, 0, cd.all, i, l);
		i += l;
		for (int r = 0; r < c; ++r) {
			Relation[] rr = cd.periodic_rel[r];
			l = rr.length;
			System.arraycopy(rr, 0, cd.all, i, l);
			i += l;
		}
	}
	

	// comparable if and only if for all i,j . m1[i,j]=inf <=> m2[i,j]=inf <=> m3[i,j]=inf
	private static boolean comparable(DBM m_p, DBM m_pl, DBM m_pll) {
		Matrix m1 = m_p.mat();
		Matrix m2 = m_pl.mat();
		Matrix m3 = m_pll.mat();
		
		int size = m1.size();
		for (int i=0; i<size; i++) {
			for (int j=0; j<size; j++) {
				Field f1 = m1.get(i, j);
				Field f2 = m2.get(i, j);
				Field f3 = m3.get(i, j);
				
				if (f1.isFinite() != f2.isFinite() || f1.isFinite() != f3.isFinite() )
					return false;
			}
		}
		
		return true;
	}

//	public static LinearTerm[] emptySubst(Variable[] vars) {
//		LinearTerm[] ret = new LinearTerm[vars.length];
//		for (int i=0; i<vars.length; i++)
//			ret[i] = new LinearTerm(vars[i],1);
//		return ret;
//	}

	public ClosureDetail closure_detail(DBM dbm, boolean aIsClosure, LinearTerm[] aSubstitution, Variable[] aVarsOrig) {
		varsOrig = aVarsOrig;
		subst = aSubstitution;
		isOctagon = dbm.encoding().isOct();
		
		closure(dbm);
		return cd;
	}
	public ClosureDetail closurePlus_detail(DBM dbm, boolean aIsClosure, LinearTerm[] substitution, Variable[] varsOrig) {
		is_cl_plus = true;
		return closure_detail(dbm, aIsClosure, substitution, varsOrig);
	}
	
	
	public Relation[] closure(DBM dbm, boolean aIsClosure, LinearTerm[] aSubstitution, Variable[] aVarsOrig) {
		varsOrig = aVarsOrig;
		subst = aSubstitution;
		isOctagon = dbm.encoding().isOct();
		
		closure(dbm);
		if (!Relation.CLOSURE_ONLY) {
			cd_finish(cd);
		}
		return cd.all;
	}
	public Relation[] closurePlus(DBM dbm, boolean aIsClosure, LinearTerm[] substitution, Variable[] varsOrig) {
		is_cl_plus = true;
		return closure(dbm, aIsClosure, substitution, varsOrig);
	}
	
	
	/** 
	 * Implementation of Marius et al algorithm for DBM termination.
	 * <p>
	 * Although it should work for Difference Bounds Constraints (DBRel) and
	 * Octagons (OctagonRel), it currently allows only DBCs to be checked.
	 *
	 * @param dbm the DBM encoding the relation ( 
	 * @return
	 * 	true if the DBM relation always terminates
	 * 	false if infinite traces exist
	 */
	public static boolean alwaysTerminates(DBM dbm)
	{
//		if(!dbm.encoding().isDBC()) {
//			System.out.println("ERROR: DeltaClosure.alwaysTerminates(): termination implemented only for Difference Bounds Constraints");
//			return false;
//		}

		DeltaClosure dc = new DeltaClosure();
		dc.isOctagon = dbm.encoding().isOct();
		dc.closure(dbm);
		
		if (!dc.cd.isOmegaConsistent()) {
			return false;
		}
		// Matrix of R* which represents the transitive closure of the relation encoded by @p dbm
		Matrix m = dc.m_rhs.mat();
		// Check paths between unprimed variables only
		int N = m.size()/2;

		for(int i = 0; i < N; ++i) for(int j = 0; j < N; ++j) {
			for(ParamBound pb: ((ParamBounds)(m.get(i, j))).paramBounds()) {
				if(pb.paramCoef() < 0)
					return true;
			}
		}

		return false;
	}

	// returns null iff the weakest nontermination condition is equivalent to false
	public static DBM weakestNontermCond(DBM dbm) {
		
//		if (dbm.encoding().isOct()) {
//			throw new RuntimeException("internal error: termination precondition not supported for octagons");
//		}
		
		DeltaClosure dc = new DeltaClosure();
		dc.isOctagon = dbm.encoding().isOct();
		dc.closure(dbm);
		
		if (!dc.cd.isOmegaConsistent()) {
			return null;
		}
		Matrix nonterm = dc.weakestInitCond(dc.m_rhs.mat());
		
		if (nonterm == null)
			return null;
		
		if (dbm.encoding().isDBC()) {
			int nVar = nonterm.size() / 2;
			nonterm.set(0, nVar, dbm.fs().zero());
			nonterm.set(nVar, 0, dbm.fs().zero());
		}
		
		DBM ret = new DBM(dbm.encoding(), nonterm, IntegerInfStatic.fs());
		ret.canonize();
		
		return ret;
	}
	
}
