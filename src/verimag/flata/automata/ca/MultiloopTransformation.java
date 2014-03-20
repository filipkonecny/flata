package verimag.flata.automata.ca;

import java.util.*;

import verimag.flata.automata.BaseArc;
import verimag.flata.automata.ca.CA.MergeResult;
import verimag.flata.common.Answer;
import verimag.flata.common.CR;
import verimag.flata.common.HMapWColVal;
import verimag.flata.common.Parameters;
import verimag.flata.presburger.*;
import verimag.flata.presburger.Relation.RelType;

public class MultiloopTransformation {

	public static boolean FIND_PATTERNS = true;
	
	public static boolean INCLUSION_DISJ = false;
	
	public static Relation.RelType abstrType = Relation.RelType.MODULO;//.LINEAR;
	
	public static boolean PRINT_PROGRESS = false;
	
	public static Answer subset(Collection<TransHist> c1, Collection<TransHist> c2,
			Collection<TransHist> Rk_new, Collection<TransHist> Rk_old) {
		return subsetRelational(c1, c2, Rk_new, Rk_old);
	}
	
	private static Answer subsetRelational(Collection<TransHist> c1,
			Collection<TransHist> c2, Collection<TransHist> rkNew,
			Collection<TransHist> rkOld) {
		
		Answer all = Answer.TRUE;
		for (TransHist r1 : c1) {
			
			Answer one = Answer.FALSE;
			for (TransHist r2 : c2) {
				Answer a = r1.t.rel().isIncludedIn(r2.t.rel());
				one = one.or(a);
			}

			if (one.isTrue()) {
				rkOld.add(r1);
			} else {
				rkNew.add(r1);
			}

			
			all = all.and(one);
		}
		
		return all;
	}

	
	private static class TransHist {
		
		private static int INIT_ID = -1; 
		
		private int id = INIT_ID;
		public void name(int aID) { id = aID; }
		public String name() { return "r"+id; }
		
		CATransition t;
		private boolean subsumedLater = false;
		public void setSubsumedLater() { subsumedLater = true; }
		public boolean wasSubsumedLater() { return subsumedLater; }
		
		// relation rel is created either as a closure of a loop relation
		CATransition loop;
		
		// or as a composition of two relations during tree computation
		TransHist left = null;
		TransHist right = null;
		
		TransHist[] hist;
		boolean canTrackExact;
		
		public int last_meta_inx;
		public int todo_inx;
		public int depth;
		
		public TransHist(CATransition aT, CATransition aTLoop) {
			t = aT;
			loop = aTLoop;
			hist = new TransHist[]{this}; // "recursive"
			canTrackExact = true;
		}
		public TransHist(CATransition aT, boolean onlyOne, TransHist aLeft, TransHist aRight) {
			t = aT;
			left = aLeft;
			right = aRight;
			int l1 = aLeft.hist.length,l2 = aRight.hist.length; hist = new TransHist[l1+l2];
			System.arraycopy(aLeft.hist, 0, this.hist, 0, l1);
			System.arraycopy(aRight.hist, 0, this.hist, l1, l2);
			canTrackExact = onlyOne && aLeft.canTrackExact && aRight.canTrackExact;
		}
//		public void copyCommon(TransHist th) {
//			last_meta_inx = th.last_meta_inx;
//			todo_inx = th.todo_inx;
//			depth = th.depth;
//		}
		public void setCommon(int aLast_loop_inx, int aTodo_inx, int aDepth) {
			last_meta_inx = aLast_loop_inx;
			todo_inx = aTodo_inx;
			depth = aDepth;
		}
//		public static CATransition[] toRelArr(Collection<TransHist> aCol) {
//			CATransition[] ret = new CATransition[aCol.size()];
//			int i=0;
//			for (TransHist rh : aCol) {
//				ret[i] = rh.t;
//				i ++;
//			}
//			return ret;
//		}
		public static Collection<CATransition> toRelCol(Collection<TransHist> aCol) {
			Collection<CATransition> ret = new LinkedList<CATransition>();
			for (TransHist rh : aCol) {
				ret.add(rh.t);
			}
			return ret;
		}
		
		public String toString() {
			
			CompositeRel rel = this.t.rel();
			
			String regExp = "  " + t.name() + "\n  ";
			
			String compact = "";
//			if (rel instanceof DBRel) {
//				compact = "\n  compact form:\n  "+DBRel.compact(((DBRel)rel)).toStringBufCompact();
//			}
//			} else if (rel instanceof LinearRel) {
//				compact = "\n  compact form:\n  "+LinearRel.compact((LinearRel)rel);
//			}
			
			if (left == null) {
				return "\n" + name() + "["+rel.typeName()+"]" + "\n  " + regExp + rel + compact
				+ "\n  transitive closure of \n  "+loop;
			} else {
				return "\n" + name() + "["+rel.typeName()+"]" +"["+left.name()+"-"+right.name()+"]" 
				+ "\n  " + regExp + rel + compact;
			}
		}
		
		public static StringBuffer toStringBuf(Collection<TransHist> aCol) {
			StringBuffer sb = new StringBuffer();
			for (TransHist r : aCol) {				
				sb.append(r.toString()+"\n\n");
			}
			return sb;
		}
		public static void print(Collection<TransHist> aCol) {
			System.out.println(TransHist.toStringBuf(aCol));
		}

		public static int giveNames(Collection<TransHist> aCol, int iStart) {
			int i = iStart;
			for (TransHist r : aCol) {
				r.name((i++));
			}
			return i;
		}
		
		public static void toDotLang(String aGraphName, Collection<TransHist> Rk_h, Collection<TransHist> R_h, int k) {
			StringBuffer sb = new StringBuffer();
			
			String rootNode = "root";
			
			sb.append("digraph " + aGraphName + " {\n");
			
			sb.append("node [shape = circle];\n");
			
			List<String> visited = new LinkedList<String>();
			
			List<TransHist> queue = new LinkedList<TransHist>(Rk_h);
			
			while (!queue.isEmpty()) {
				
				TransHist rh = queue.remove(0);
				String rhID = rh.name()+rh.t.typeName(); 
				if (visited.contains(rhID))
					continue;
				
				visited.add(rhID);
				
				if (rh.left != null) {
					
					String rhLeftID = rh.left.name()+rh.left.t.typeName();
					
					sb.append(rhLeftID + " -> " + rhID
							+ " [ label = \"" + rh.right.name() + "\"];\n");
					
					if (!queue.contains(rh.left))
						queue.add(rh.left);
				} else {
					
					sb.append(rootNode + " -> " + rhID
							+ " [ label = \"" + rh.name() + "\"];\n");
				}
			}
			
			//sb.append("dummy [color=white, label=\"" + RelHist.toStringBuf(R_h) + "\"]\n");
			
			sb.append("}\n");
			
			CR.writeToFile("./out"+k+".dot", sb.toString());
		}
		
		
		public static boolean disjunctiveSubsume( Collection<String> vars,
				Collection<TransHist> incomparableCol, 
				TransHist newCandidate) {
			
			Collection<CompositeRel> tmp = new LinkedList<CompositeRel>();
			for (TransHist th : incomparableCol) {
				if (!th.wasSubsumedLater())
					tmp.add(th.t.rel());
			}
			
			Answer a = CompositeRel.subsumed(vars, newCandidate.t.rel(), tmp);
			
//			if (a.isDontKnow()) {
//				System.err.println("DN");
//			}
			
			return a.isTrue();
		}
		
		public static int subs=0;
		public static int added=0;
		
		private static boolean NO_MODULO_INCLUSION = true;
		private static boolean inclusionGuaranteed(CompositeRel r1, CompositeRel r2) {
			
			boolean b1 = !r1.isLinear();
			boolean b2 = !r2.isLinear();
			if (NO_MODULO_INCLUSION && (b1 || b2)) {
				
				
				if (b1 && !b2) {
					return r1.hull(RelType.LINEAR).isIncludedIn(r2).isTrue();
				} else {
					//return false;
					return r1.isIncludedSimple(r2).isTrue();
				}
			}
			
			return r1.isIncludedIn(r2).isTrue();
		}
		
		// preserves incomparable collections only if newCandidates are already incomparable
		public static boolean addIncomparable( Collection<String> vars,
				Collection<TransHist> incomparableCol, 
				TransHist newCandidate) {
			
			if (INCLUSION_DISJ) {
				boolean b = disjunctiveSubsume(vars, incomparableCol,newCandidate);
				if (!b) {
					incomparableCol.add(newCandidate);
				}
				return !b;
			}
			
			Iterator<TransHist> iter = incomparableCol.iterator();
			
			CompositeRel r_new = newCandidate.t.rel();
			
			boolean add = true;
			while (iter.hasNext()) {
				TransHist old = iter.next();
				CompositeRel r_old = old.t.rel();
				//if (r_new.isIncludedIn(r_old).isTrue()) {
				if (inclusionGuaranteed(r_new,r_old)) {
					// !!! THIS INCLUSION MUST BE CHECKED FIRST (don't recompute when not necessary)
					add = false;
					break;
				//} else if (r_new.includes(r_old).isTrue()) {
				} else if (inclusionGuaranteed(r_old,r_new)) {
					old.setSubsumedLater();
					subs++;
					iter.remove();
					continue;
				}

			}
			if (add) {
				added++;
				incomparableCol.add(newCandidate);
			} else {
				//newCandidate.setSubsumedLater();
			}
			
			return add;
		}
		
		public static Collection<TransHist> notSubsumedLater(Collection<TransHist> aRk_cand) {
			Collection<TransHist> ret = new LinkedList<TransHist>();
			for (TransHist rh : aRk_cand) {
				if (!rh.wasSubsumedLater())
					ret.add(rh);
			}
			return ret;
		}
	}
	
	private static  class TransitionPair {
		public int first;
		public int second;
		
		public TransitionPair(int aFirst, int aSecond) {
			first = aFirst;
			second = aSecond;
		}
		
		public String toString() {
			return "<"+first+","+second+">";
		}
	}
	
	// assumption: input array contains mutually different relations
	private static Collection<TransitionPair> pairsForOrderReduction(TransHist[] ts) {
		
		Collection<TransitionPair> ret = new LinkedList<TransitionPair>(); 
		for (int i=0; i<ts.length; i++) {
			CompositeRel ri = ts[i].t.rel();
			
			for (int j=i+1; j<ts.length; j++) {
				CompositeRel rj = ts[j].t.rel();
				
				CompositeRel[] IJ = ri.compose(rj);
				CompositeRel[] JI = rj.compose(ri);
				
				if (IJ.length==0 && JI.length==0) // both compositions are false
					continue;
				
				boolean reduction = false;
				if (IJ.length==1 && JI.length==1) { // check implication only for conjuctions
					
					if (IJ[0].getType().isModulo() || JI[0].getType().isModulo()) {
						//System.out.println("@@Modulo@@");
					} else 
					if (IJ[0].isIncludedIn(JI[0]).isTrue()) {
						ret.add(new TransitionPair(j,i));
						reduction = true;
					} else if (JI[0].isIncludedIn(IJ[0]).isTrue()) {
						ret.add(new TransitionPair(i,j));
						reduction = true;
					}
				} else if (IJ.length!=0 && JI.length==0) {
					ret.add(new TransitionPair(i,j));
					reduction = true;
				} else if (JI.length!=0 && IJ.length==0) {
					ret.add(new TransitionPair(j,i));
					reduction = true;
				}
				
				if (!reduction) { 
					ret.add(new TransitionPair(i,j));
					ret.add(new TransitionPair(j,i));
				}
			}
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private static Integer[][] searchCandidates(Collection<TransitionPair> orderReduction, int n, boolean forward) {
		
		Collection<Integer>[] tmp = (Collection<Integer>[]) new LinkedList[n];
		for (int i=0; i<tmp.length; i++) {
			tmp[i] = new LinkedList<Integer>();
		}
		
		for (TransitionPair tp : orderReduction) {
			if (forward)
				tmp[tp.first].add(new Integer(tp.second));
			else
				tmp[tp.second].add(new Integer(tp.first));
		}
		
		Integer[][] ret = new Integer[n+1][];
		Integer[] d = new Integer[0];
		for (int i=0; i<n; i ++) {
			ret[i] = tmp[i].toArray(d);
		}
		
		// last entry is for nodes from which all meta-transitions
		ret[n] = new Integer[n];
		for (int i=0; i<n; i++) {
			ret[n][i] = i;
		}
		
		return ret;
	}

	private static boolean gen_pref_suf_contradictory(boolean forward, CATransition[] pref_suf, CATransition t) {
		if (pref_suf.length == 0) {
			return false;
		}
		for (CATransition p_s : pref_suf) {
			CATransition[] comp = (forward)? p_s.compose(t) : t.compose(p_s);
			if (comp.length != 0) {
				return false;
			}
		}
		return true;
	}
	
	private static CATransition abstr(CATransition t) {
		return t.hull(abstrType);
	}
	
	private static Collection<CATransition> gen_compose(boolean forward, CATransition[] pref_suf, CATransition t_hist, CATransition t_loop_closure) {
		
		List<CATransition> tmp;
		if (forward)
			tmp = new LinkedList<CATransition>(Arrays.asList(t_hist.compose(t_loop_closure)));
		else 
			tmp = new LinkedList<CATransition>(Arrays.asList(t_loop_closure.compose(t_hist)));
		
		List<CATransition> ret = new LinkedList<CATransition>();
		for (CATransition t : tmp) {
			t = abstr(t);
			if (!gen_pref_suf_contradictory(forward, pref_suf, t)) {
				ret.add(t);
			}
		}
		return ret;
	}
	private static Collection<CATransition> gen_compose_no_pref_suf(boolean forward, CATransition t_hist, CATransition t_loop_closure) {
		List<CATransition> tmp;
		if (forward)
			tmp = new LinkedList<CATransition>(Arrays.asList(t_hist.compose(t_loop_closure)));
		else 
			tmp = new LinkedList<CATransition>(Arrays.asList(t_loop_closure.compose(t_hist)));
		
		return tmp;
	}
	
	private static CATransition[] gen_pref_suf(boolean forward, CAState s) {
		Collection<CATransition> tmp = (forward)? s.incoming() : s.outgoing();
		Iterator<CATransition> iter = tmp.iterator();
		while (iter.hasNext()) {
			CATransition t = iter.next();
			if (t.from().equals(t.to()))
				iter.remove();
		}
		return tmp.toArray(new CATransition[0]);
	}
	
	private static TransHist gen_new_trans_hist(boolean forward, CATransition trans_new, boolean onlyOne,  TransHist th_list, TransHist th_one) {
		if (forward)
			return new TransHist(trans_new, onlyOne, th_list, th_one);
		else
			return new TransHist(trans_new, onlyOne, th_one, th_list);
	}
	
	private static void gen_add_to_search_todo(boolean bfs, Deque<TransHist> searchTodo, TransHist new_th) {
		if (bfs)
			searchTodo.addLast(new_th);
		else
			searchTodo.addFirst(new_th);
	}
	
	private static CATransition[] findPatterns(boolean forward, TransHist th) {
		TransHist[] ths = th.hist;
		int l = ths.length; // history length
		int pl=2;
		while (2*pl<l) { // pattern length
		//while (pl<=l/2) { // pattern length
			boolean isPattern = true;
			int base = (forward)? (l - 2*pl) : 0;
			for (int i=0; i<pl; i ++) {
				if (ths[base+i].last_meta_inx != ths[base+i+pl].last_meta_inx) {
					isPattern = false;
					break;
				}
			}
			if (!isPattern) {
				pl ++;
				continue;
			} else {
				CATransition[] tmp = new CATransition[pl];
				for (int i=0; i<pl; i++) {
					tmp[i] = ths[base+i].t;
				}
				
				List<CATransition> aux = new LinkedList<CATransition>();
				
				for (int off = 0; off < pl; off++) {
				
					CATransition ret = tmp[off];
					for (int i=1; i<pl; i++) {
						CATransition[] arr = ret.compose(tmp[(off+i)%pl]);
						if (arr.length != 1)
							throw new RuntimeException();
						else
							ret = arr[0];
					}
					aux.add(ret);
				}
				return aux.toArray(new CATransition[0]);
				
			}
		}
		return new CATransition[0];
	}
	
	
	private static boolean commutative(CATransition[] loops) {
		int l = loops.length;
		for (int i=0; i<l; i++) {
			for (int j=i+1; j<l; j++) {
				CompositeRel r1 = loops[i].rel();
				CompositeRel r2 = loops[j].rel();
				
				CompositeRel[] c12 = r1.compose(r2);
				CompositeRel[] c21 = r2.compose(r1);
				
				// analyze only case when the composed relation is not disjuntive
				if (c12.length <= 1 || c12.length != c21.length) {
					return false;
				}
				if (c12.length == 0) { // both compositions are False
					continue;
				}
				if (!c12[0].relEquals(c21[0]).isTrue()) {
					return false;
				}
			}
		}
		return true;
	}
	
	/*
	 * Acceleration of loops that are pair-wise commutative.
	 * 
	 * (R1 \/ ... \/ Rn)^*  <=> R1^* o ... o Rn^*
	 * 
	 * (R1 \/ ... \/ Rn)^+  <=> (R1 \/ ... \/ Rn)^+ o (R1 \/ ... \/ Rn)
	 * 
	 */
//	private static int accelerateCommutative(CATransition[] loops, Collection<CATransition> pcomputed, boolean plus) {
//		
//		int l = loops.length;
//		for (int i=0; i<l; i++) {
//			loops[i]
//		}
//		
//		return l;
//	}
	
	
	
	// process states with more than one loop
	// strategy: state q has loops R1,...,Rl. Let R = (R1*\/...\/Rl*).
	//   find n s.t. R^n \subseteq R
	//   then R+ = R^1 \/ ... \/ R^n
	//   create new state q', remove all loops on q, add disjuncts from R+ on new transitions from q to q', 
	//   and all transitions coming from q are moved so that they come from q'
	// returns tree depth (-1 if unsuccessful)
	public static long runtime_semialg = 0;
	public static HMapWColVal<Float, Integer, LinkedList<Float>> runtime_stat; 
	static {
	@SuppressWarnings("unchecked")
	Class<LinkedList<Float>> class2 = (Class<LinkedList<Float>>) new LinkedList<Float>().getClass();
	runtime_stat = new HMapWColVal<Float, Integer, LinkedList<Float>>(class2);
	}
	
	public static int reduceMultiLoopStatePlus(CA aCA, CAState s, boolean elim_forward, boolean tree_bfs) {
		int ret = reduceMultiLoopState(aCA, s, elim_forward, tree_bfs, null, true);
		return ret;
	}
	
	public static int reduceMultiLoopState(CA aCA, CAState s, boolean elim_forward, boolean tree_bfs) {
		int ret = reduceMultiLoopState(aCA, s, elim_forward, tree_bfs, null);
		return ret;
	}
	private static void finishStats(long start, int loops) {
		long rt_sem = System.currentTimeMillis() - start;
		runtime_semialg += (rt_sem);
		float rat = ((float)rt_sem / loops / 1000);
		//System.out.print( "[,sem:"+((float)rt_sem / 1000)+",loops:"+loops+",ratio:"+rat+"]");
		runtime_stat._add(loops, rat);
		
	}
	// (\/ Ri)^*
	public static int reduceMultiLoopState(CA aCA, CAState s, boolean elim_forward, boolean tree_bfs, Collection<CATransition> pcomputed) {
		return reduceMultiLoopState(aCA, s, elim_forward, tree_bfs, pcomputed, false);
	}
	// (\/ Ri)^+
	public static int reduceMultiLoopStatePlus(CA aCA, CAState s, boolean elim_forward, boolean tree_bfs, Collection<CATransition> pcomputed) {
		return reduceMultiLoopState(aCA, s, elim_forward, tree_bfs, pcomputed, true);
	}
	public static int reduceMultiLoopState(CA aCA, CAState s, boolean elim_forward, boolean tree_bfs, Collection<CATransition> pcomputed, boolean plus) {
		return new MultiloopTransformation(elim_forward, tree_bfs, pcomputed, plus).reduceMultiLoopState_(aCA, s);
	}
	
	
	private static int MAX_UNROLL = 20;
	
	private boolean elim_forward;
	private boolean tree_bfs;
	private Collection<CATransition> pcomputed;
	private boolean plus;
	
	private Set<CATransition> loops;
	//private CATransition[] loops_t;
	private CATransition[] pref_suf;
	private Collection<String> vars;
	private Collection<TransHist> meta_th;
	private Collection<CATransition> meta;
	private CATransition identity;
	
	private MultiloopTransformation(boolean a_elim_forward, boolean a_tree_bfs, Collection<CATransition> a_pcomputed, boolean a_plus) {
		elim_forward = a_elim_forward;
		tree_bfs = a_tree_bfs;
		pcomputed = a_pcomputed;
		plus = a_plus;
	}
	
	private int reduceMultiLoopState_(CA aCA, CAState aS) {
		
		boolean b = Parameters.isOnParameter(Parameters.ACCELERATE_WITH_OUTGOING);
		if (b) { 
			// force backward computation
			elim_forward = false;
		}
		
		loops = new HashSet<CATransition>();
		for (BaseArc a : aS.getLoops()) {
			loops.add((CATransition)a);
		}
		
		// prefix
		pref_suf = gen_pref_suf(elim_forward,aS);
		
		vars = aCA.variableNames();
		
		identity = CA.createIdentityTransition(aCA, aS);
		
		// R^1
		// create R = (R1*\/...\/Rl*)
		meta_th = new LinkedList<TransHist>();
		meta = new LinkedList<CATransition>();
		
		boolean set_identity_origin = false;
		
		for (BaseArc arc : loops) {
			CATransition loop = (CATransition)arc;
			
			if (Parameters.isOnParameter(Parameters.ABSTR_OCT)) {
				loop = loop.abstractOct();
			}
			
			if (!set_identity_origin) {
				identity.setClosureIndentInfo(loop); // this is a bit unclean
				// TODO set reduce info
				set_identity_origin = true;
			}
			
			if (CATransition.addIncomparable(meta, loop)) {
				
				// !!!
				//CATransition[] closure_lc = loop.closure(plus);
				CATransition[] closure_lc = loop.closurePlus();
				
				if (closure_lc == null) {
					System.out.print(" (non-octagonal loop) ");
					loop.closurePlus();
//					for (CATransition t : loops) {
//						System.out.println("  "+t);
//					}
					return -1;
				}
				
				// closures can be comparable: e.g. (x'=x+1)* vs. (x'=x+2)*
				for (CATransition t : closure_lc) {
					
					t = abstr(t);
					
					TransHist th = new TransHist(t,loop);
					TransHist.addIncomparable(vars, meta_th, th);
				}
			}
		}
		
		
		if (!b) {
			int depth = aux(b);
			if (depth < 0) {
				return depth;
			}
			aCA.replaceMultiLoop(aS, computed);
			return depth;
		} else {
			int depth = 0;
			for (CAState s : aS.succ()) {
				if (s.equals(aS))
					continue;
				inOut = aCA.incidentTrans(aS, s);
				int d = aux(b);
				if (d < 0) {
					return d;
				}
				if (d > depth) {
					depth = d;
				}
				for (CATransition t : inOut) {
					aCA.removeTransition(t);
				}
				for (CATransition t : computed) {
					aCA.addTransition(t);
				}
			}
			for (CATransition t : this.loops) {
				aCA.removeTransition(t);
			}
			aCA.elimNonloopState(aS);
			return depth;
		}
	}

	private Collection<CATransition> inOut;
	private Collection<CATransition> computed;
	// all relations, kept minimal s.t. any pair is incomparable
	private Collection<TransHist> computed_h;
	
	private Integer[][] searchCandidates;
	private Deque<TransHist> searchTodo;
	
	private int aux(boolean composeWithInOrOut) {
		
		long start = System.currentTimeMillis();
		
		int depth_for_patterns = 7;
		
		lab1: while (true) {

			if (!CR.RELEASE) {
				System.out.println("Initial steps...");
			}
			
			// meta transitions are kept pairwise incomparable
			TransHist[] acc_meta_ts_arr = meta_th.toArray(new TransHist[0]);
			
			// find reduction based on commutativity
			// A.B => B.A implies that (A,B) is ignored during search and only (B,A) is considered
			Collection<TransitionPair> orderReduction = pairsForOrderReduction(acc_meta_ts_arr);
			searchCandidates = searchCandidates(orderReduction, acc_meta_ts_arr.length, elim_forward);
			
			for (int i=0; i<acc_meta_ts_arr.length; i ++) {
				acc_meta_ts_arr[i].setCommon(i, searchCandidates[i].length-1, 1);
			}
			
			
			computed_h = new LinkedList<TransHist>();
			
			searchTodo = new LinkedList<TransHist>();
			
			int aa;
			
			if (!composeWithInOrOut) {
				
				// R^1
				for (TransHist th : acc_meta_ts_arr) {
					searchTodo.add(th);
					computed_h.add(th);
				}
				
				// R^0
				TransHist th_identity = new TransHist(identity,identity);
				int max_inx = searchCandidates.length-1;
				th_identity.setCommon(max_inx, searchCandidates[max_inx].length-1, 0);
				
				if (!plus)
					TransHist.addIncomparable(vars, computed_h, th_identity);
				
				//searchTodo.add(th_identity);
				
				aa = 2;
				
			} else {
				
				for (CATransition t : inOut) {
					
					TransHist th = new TransHist(t, null);
					int max_inx = searchCandidates.length-1;
					th.setCommon(max_inx, searchCandidates[max_inx].length-1, 0);
					
					if (!plus)
						TransHist.addIncomparable(vars, computed_h, th);
					
					searchTodo.add(th);
				}
				
				aa = 1;
				
			}
			
			if (!CR.RELEASE) {
				System.out.println("Initial steps done.");
			}
			
			int old_depth = -1;
			List<TransHist> dfs_maxLevel = new LinkedList<TransHist>();
			
			while (!searchTodo.isEmpty()) {
				TransHist node = searchTodo.getFirst();
				
				old_depth = node.depth;
				int new_depth = node.depth+1;
				
				if (tree_bfs && new_depth == aa) {
					if (!CR.RELEASE) {
						System.out.println("current depth = "+aa);
					}
					// simplifications for each tree level
					if (Parameters.isOnParameter(Parameters.T_MERGE_PREC)) {
						merging(aa);
					}
					aa++;
					//System.out.println("-----------------------------------------------");
					//System.out.println(this.computed_h);
					
					if (new_depth >= depth_for_patterns) {
						boolean added = patterns();
						// restart
						if (added) {
							depth_for_patterns = new_depth;
							continue lab1;
						}
					}
				}
				
				if (node.todo_inx < 0) {
					searchTodo.removeFirst();
					continue;
				}
				
				if (node.wasSubsumedLater()) {
					searchTodo.removeFirst();
					continue;
				}
				
				int newloop_inx = searchCandidates[node.last_meta_inx][node.todo_inx --];
				CATransition t_append = acc_meta_ts_arr[newloop_inx].t;
				
				// TODO: check pattern -> continue (no pattern) OR add pattern and restart				
				
				Collection<CATransition> ts;
				//ts = gen_compose_no_pref_suf(elim_forward, node.t, t_append);
				if (!composeWithInOrOut) {
					ts = gen_compose(elim_forward, pref_suf, node.t, t_append);
				} else {
					ts = gen_compose_no_pref_suf(elim_forward, node.t, t_append);
				}
				
				if (new_depth >= MAX_UNROLL && tree_bfs) {
					System.out.print(" (tree depth = "+MAX_UNROLL+") ");
					return -1;
				}
				
				boolean nowOnlyOne = ts.size() == 1; // TODO: not necessary (and hence canTrackExact unnecessary too)
				
				for (CATransition t : ts) {
					
					TransHist new_th = gen_new_trans_hist(elim_forward, t, nowOnlyOne, node, acc_meta_ts_arr[newloop_inx]);
					new_th.setCommon(newloop_inx, searchCandidates[newloop_inx].length-1, new_depth);
					
//					if (new_th.canTrackExact && new_depth >= 4 && FIND_PATTERNS) {
//						CATransition meta_new = findPattern(elim_forward, new_th);
//						// pattern found && pattern is accelerable && pattern is not subsumed by other meta transitions
//						CATransition[] cl = (meta_new == null)? null : meta_new.closurePlus();
//						if (meta_new != null && cl != null && CATransition.addIncomparable(meta, meta_new)) {
//
//							boolean added = false;
//							
//							for (CATransition acc_meta : cl) {
//								
//								CATransition acc_meta_abs = abstr(acc_meta);
//								
//								TransHist th = new TransHist(acc_meta_abs, meta_new);
//								added = added || TransHist.addIncomparable(vars, meta_th, th);
//							}
//							
//							// restart
//							if (added) {
//								continue lab1;
//							}
//						}
//					}
					
					if (TransHist.addIncomparable(vars, computed_h, new_th)) {
						
						if (new_depth >= MAX_UNROLL && !tree_bfs) {
							dfs_maxLevel.add(new_th);
						} else {
							gen_add_to_search_todo(tree_bfs, searchTodo, new_th);
							//merging(new_th);
						}
					}
				}
				
			}
			
			for (TransHist th : dfs_maxLevel) {
				if (!th.wasSubsumedLater()) {
					return -1;
				}
			}
			
			if (tree_bfs) {
				// simplifications fo each tree level
				merging(aa);
			}

			finishStats(start, loops.size());
			
			computed = TransHist.toRelCol(computed_h);
			if (pcomputed != null)
				pcomputed.addAll(computed);
			
			//Collection<CATransition> loopst = CATransition.toCATrans(loops);
			CATransition.setMeta(computed);
			
			CATransition.pruneUnuseful(loops);
			
			CATransition.shortcutNode(loops,computed);
			
			//merge_base(old_depth, true);
			
			return (old_depth < 1)? 1 : old_depth; // TODO ??
		}

	}
	
	private void merging(int aDepth) {
		
		{
			// try to simplify modulo relations
			List<TransHist> aux = new LinkedList<TransHist>();
			Iterator<TransHist> i = computed_h.iterator();
			while (i.hasNext()) {
				TransHist th = i.next();
				CATransition t = th.t;
				if (!t.rel().isLinear()) {
					//heuristic
					if (t.rel().toModuloRel().hasOneTermTotal()) {
						continue;
					}
					
					CATransition[] tsplit = th.t.makeMoreAccelerable();
					if (tsplit != null) {
						i.remove();
						th.setSubsumedLater();
						
						for (CATransition t2 : tsplit) {
							TransHist th2 = new TransHist(t2, null); // ignore history !!!
							th2.setCommon(th.last_meta_inx,th.todo_inx,th.depth);
							aux.add(th2);
						}
					}
				}
			}
			for (TransHist th : aux) {
				//System.out.print("|a6|");
				if (TransHist.addIncomparable(vars, computed_h, th)) {
					gen_add_to_search_todo(tree_bfs, searchTodo, th);
				}
			}
		}
		
		// merge modulo relations
		boolean mergeUseful;
		//if (!aLatest.t.rel().isLinear()) {
			
			//List<ModuloRel> inferred = null;
			do {
				
				mergeUseful = merge_base(aDepth, true);
				
			} while (mergeUseful);
		//}
		// merge dbms
		//if (dbm_new > 0) {
			
			// old DBMs
			List<CATransition> aux = new LinkedList<CATransition>();
			for (TransHist th : computed_h) {
				if (!th.wasSubsumedLater() && th.t.rel().isDBRel()) {
					aux.add(th.t);
				}
			}
			
			// some new DBM generated
			MergeResult mr = new MergeResult(aux, 0);
			MergeResult.merge2(mr, 0, aux.size());
			List<CATransition> ts_new = mr.getNew();
			
			// add merged transitions
			for (CATransition tt : ts_new) {
				TransHist th = new TransHist(tt, null); // ignore history !!!
				th.setCommon(searchCandidates.length-1, searchCandidates.length-2, aDepth);
				if (TransHist.addIncomparable(vars, computed_h, th)) {
					gen_add_to_search_todo(tree_bfs, searchTodo, th);
				}
			}
		//}
		//System.out.print("|<-|");
	}
	
	private List<CATransition> genInferred(List<ModuloRel> inferred, List<BitSet> inferred_from_flags, List<TransHist> inferred_from) {
		//System.out.print("|->A|");
		
		List<CATransition> ret = new LinkedList<CATransition>();
		Iterator<ModuloRel> i1 = inferred.iterator();
		Iterator<BitSet> i2 = inferred_from_flags.iterator();
		while (i1.hasNext()) {
			ModuloRel mr = i1.next();
			BitSet bs = i2.next();
			List<CATransition> aux = new LinkedList<CATransition>();
			int i = -1;
			while ((i = bs.nextSetBit(i+1)) >= 0) {
				aux.add(inferred_from.get(i).t);
			}
			CATransition t_aux = aux.get(0);
			CATransition t_new = CATransition.subset(t_aux.from(), t_aux.to(), new CompositeRel(mr), null, t_aux.ca(), aux);
			ret.add(t_new);
		}
		//System.out.print("|<-B|");
		return ret;
	}
	
	private boolean merge_base(int aDepth, boolean ltModulus) {
		//System.out.print("|->C|");
		// find modulo relations
		List<TransHist> aux = new LinkedList<TransHist>();
		for (TransHist th : computed_h) {
			if (!th.wasSubsumedLater() && !th.t.rel().isLinear()) {
			//if (!th.wasSubsumedLater()) {
				aux.add(th);
			}
		}
		
		
		List<ModuloRel> r_aux = new LinkedList<ModuloRel>();
		for (TransHist th : aux) {
			r_aux.add(th.t.rel().toModuloRel());
		}
		
		List<ModuloRel> inferred = new LinkedList<ModuloRel>();
		List<BitSet> inferred_from = new LinkedList<BitSet>();

		ModuloRel.merge(r_aux, inferred, inferred_from,ltModulus);
		
		List<CATransition> ts_new = genInferred(inferred, inferred_from, aux);
		
		// add merged transitions
		boolean mergeUseful = false;
		for (CATransition tt : ts_new) {
			TransHist th = new TransHist(tt, null); // ignore history !!!
			th.setCommon(searchCandidates.length-1, searchCandidates.length-2, aDepth);
			//System.out.print("|a4|");
			if (TransHist.addIncomparable(vars, computed_h, th)) {
				gen_add_to_search_todo(tree_bfs, searchTodo, th);
				mergeUseful = true;
			}
		}
		//System.out.print("|<-C|");
		return mergeUseful;
	}
	
	private boolean patterns() {
		//System.out.print("|->P|");
		boolean added = false;
		
		for (TransHist old_th : this.computed_h) {
			if (old_th.canTrackExact && old_th.depth >= 4 && FIND_PATTERNS) {
				CATransition[] mm = findPatterns(elim_forward, old_th);
				
				for (CATransition meta_new : mm) {
					// pattern found && pattern is accelerable && pattern is not subsumed by other meta transitions
					CATransition[] cl = (meta_new == null)? null : meta_new.closurePlus();
					if (meta_new != null && cl != null && CATransition.addIncomparable(meta, meta_new)) {
						
						for (CATransition acc_meta : cl) {
							
							CATransition acc_meta_abs = abstr(acc_meta);
							
							TransHist th = new TransHist(acc_meta_abs, meta_new);
							boolean b_aux = TransHist.addIncomparable(vars, meta_th, th);
							added = added || b_aux; // !! conditional evaluation
						}
					}
				}
			}
		}
		//System.out.print("|<-P|");
		return added;
	}
	
}
