package verimag.flata.acceleration.zigzag.flataofpca;

import java.util.*;

import verimag.flata.acceleration.zigzag.*;
import verimag.flata.presburger.*;

public class OfpcaOutputElimination {

	public static int DEBUG_NO = 0;
	public static int DEBUG_TINY = 1;
	public static int DEBUG_MEDUIM = 2;
	public static int DEBUG_DETAIL = 3;
	public static int DEBUG = DEBUG_NO;
	
	public static Variable k = VariablePool.createSpecial("$k");

	
	private VariablePool varPool;
	public OfpcaOutputElimination(VariablePool aVarPool) {
		varPool = aVarPool;
	}
	
	private static enum FlataLinSetType {
		POINT, MODULO;
		
		/*public static NPremiseType merge(NPremiseType t1, NPremiseType t2) {
			if (t1==MODULO && t2==MODULO)
				return MODULO;
			else
				return CONST;
		}*/
	}
	
	// assumption: linear sets which it contains do not overlap
	private static class FlataSLSet {
		java.util.Set<FlataLinSet> lin_sets = new TreeSet<FlataLinSet>();
		
		public Collection<FlataLinSet> toCollection() { return  lin_sets; }
		
		public String toString() { return lin_sets.toString(); }
		
		/*public FlataSLSet(Collection<NPremise> aLin_sets) {
			lin_sets = new TreeSet<NPremise>(aLin_sets);
			//Collections.sort(lin_sets);
		}*/
		
		public static FlataSLSet ofpca2flata(SLSet sls_ofpca) {
			FlataSLSet sls_flata = new FlataSLSet();
			
			for (LinSet ls_ofpca : sls_ofpca.getLinearSets())
				sls_flata.add(new FlataLinSet(ls_ofpca));
			
			return sls_flata;
		}
		
		/*public FlataSLSet() {
			lin_sets = new TreeSet<FlataLinSet>();
		}*/
		public void add(FlataLinSet aLS) {
			/*int i=0;
			for (NPremise ls : lin_sets) {
				if (aLS.compareTo(ls)>0)
					break;
			}
			lin_sets.add(i,aLS);*/
			lin_sets.add(aLS);
		}
		public void remove(FlataLinSet aLS) {
			lin_sets.remove(aLS);
		}
		
		public FlataLinSet findLS(int n) {
			for (FlataLinSet ls : lin_sets) {
				if (ls.contains(n))
					return ls;
			}
			
			return null;
		}
		
		public int commonPeriod() {
			int lcm = 1;
			for (FlataLinSet ls : lin_sets) {
				if (ls.isPoint())
					continue;
				
				lcm = LinearConstr.lcm(lcm, ls.genN);
			}
			
			return lcm;
		}
		
		public int maxPointBase() {
			int max = 0;
			for (FlataLinSet ls : lin_sets) {
				if (ls.isPoint())
					if (ls.baseN>max)
						max = ls.baseN;
			}
			return max;
		}
		public int maxModuloBase() {
			int max = 0;
			for (FlataLinSet ls : lin_sets) {
				if (ls.isModulo())
					if (ls.baseN>max)
						max = ls.baseN;
			}
			return max;
		}
		
		private static void removeAndInstantiate(FlataSLSet sls, int n) {
			FlataLinSet ls = sls.findLS(n);
			if (ls != null) {
				sls.remove(ls);
				for (int i = ls.baseN; i < n; i += ls.genN) {
					sls.add(new FlataLinSet(FlataLinSetType.POINT, i));
				}
			}
		}
		
		public FlataSLSet complement() {
			FlataSLSet compl = new FlataSLSet();
			
			int P = this.commonPeriod(); // period
			int MP = this.maxPointBase();  // maximal point base
			int MM = this.maxModuloBase(); // maximal modulo base
			
			int B = Math.max(P+MP, MM); // scan boundary
			
			for (int n = 1; n <= B; ++n) {
				FlataLinSet lsThis = this.findLS(n);
				FlataLinSet lsCompl = compl.findLS(n);
				
				if (lsThis == null && lsCompl == null) { // n is neither in the original set nor in the complement set
					compl.add(new FlataLinSet(FlataLinSetType.MODULO,n,P));
				} else if (lsThis != null && lsCompl != null) { // n is both in the original set and in the complement set
					// if n is in complement we are sure that it is contained in some periodic set
					
					int remNr = P / lsCompl.genN; // max nr. of periodic sets to be removed from complement
					for (int i = 0; i < remNr; ++i) {
						int n2 = lsThis.baseN + i*lsThis.genN;
						FlataSLSet.removeAndInstantiate(compl,n2);
					}
					/*compl.remove(lsCompl);
					for (int i = lsCompl.baseN; i < n; i += lsCompl.genN) {
						compl.add(new FlataLinSet(FlataLinSetType.POINT, i));
					}*/
				}
			}
			
			return compl;
		}
	}
	
	private static class FlataLinSet implements Comparable {
		FlataLinSetType type;
		public boolean isModulo() { return type == FlataLinSetType.MODULO; }
		public boolean isPoint() { return !isModulo(); }
		
		// modulo constraint
		int baseN;
		int genN;
		
		public String toString() {
			return (type==FlataLinSetType.MODULO)?
					"[b="+baseN+",g="+genN+"]" :
						"[b="+baseN+"]";
		}
		
		public FlataLinSet(FlataLinSetType aType, int aBaseN) {
			this(aType,aBaseN,0);
		}
		public FlataLinSet(FlataLinSetType aType, int aBaseN, int aGenN) {
			type = aType;
			baseN = aBaseN;
			genN = aGenN;
		}
		public FlataLinSet(LinSet ls) {
			
			// TODO: remove
			//ls = FlataOfpca.unfoldLinSet(ls);
			
			Point base = ls.getBase(); // note on Point: pair (getLength,getWeight)
			Point gen = ls.getGenerator();
			
			if (gen != null) {
				type = FlataLinSetType.MODULO;
				
				baseN = base.getLength();
				genN = gen.getLength();				
				
			} else {
				type = FlataLinSetType.POINT;
				baseN = base.getLength();
			}
		}
		
		public boolean equals(Object o) {
			if (!(o instanceof FlataLinSet))
				return false;
			
			FlataLinSet other = (FlataLinSet) o;
			if (this.type != other.type)
				return false;
			
			if (this.isPoint())
				return (this.baseN == other.baseN);
			else
				return (this.baseN == other.baseN && this.genN == other.genN);
		}
		
		public int hashCode() {
			if (this.isPoint())
				return this.baseN;
			else
				return this.baseN + this.genN;
		}
		
		public boolean contains (int n) {
			if (type==FlataLinSetType.POINT) {
				return n==baseN;
			} else {
				// solve a + c*x = n in positive integers
				return (n >= baseN) && ((n-baseN) % genN == 0);
			}
		}
		
		public static int findBase(int b1, int b2, int gen1, int gen2) {
			int x1 = b1;
			int x2 = b2;
			
			while (x1!=x2) {
				if (x1<x2)
					x1 += gen1;
				else
					x2 += gen2;
			}
			
			return x1;
		}
		
		public static FlataLinSet merge(FlataLinSet ls1, FlataLinSet ls2) {
			
			// TODO: change
			if (ls1.baseN==0 || ls2.baseN==0)
				return null;
			
			if (ls1.type==ls2.type) {
				if (ls1.isPoint()) {
					if (ls1.baseN==ls2.baseN)
						return new FlataLinSet(FlataLinSetType.POINT,ls1.baseN);
					else
						return null;
				} else {
					int gcd = LinearConstr.gcd(ls1.genN, ls2.genN);
					if ((ls1.baseN-ls2.baseN) % gcd == 0) {
						int lcm = (ls1.genN * ls2.genN) / gcd;
						int new_base = findBase(ls1.baseN,ls2.baseN,ls1.genN,ls2.genN);
						return new FlataLinSet(FlataLinSetType.MODULO,new_base,lcm);						
					} else
						return null;
				}
			} else {
				FlataLinSet lsC = ( ls1.isPoint())? ls1 : ls2;   // point linear set
				FlataLinSet lsM = (!ls1.isPoint())? ls1 : ls2;   // modulo linear set
				
				if (lsC.baseN >= lsM.baseN && (lsC.baseN-lsM.baseN) % lsM.genN == 0) {
					return new FlataLinSet(FlataLinSetType.POINT,lsC.baseN);
				} else 
					return null;
			}
		}

		public int compareTo(Object o) {
			if (!(o instanceof FlataLinSet))
				throw new RuntimeException("Attempt to compare NPremise object with "+o.getClass().getName());
			
			FlataLinSet other = (FlataLinSet)o;
			if (this.baseN < other.baseN)
				return -1;
			else if (this.baseN > other.baseN)
				return 1;
			else 
				return new Integer(this.genN).compareTo(new Integer(other.genN));
		}
	}
	
	private static class NConsequence {
		LinearTerm ltFirst;
		LinearTerm ltSecond;
		LinSet linSet;
		
		public String toString() {
			return toSB().toString();
		}
		public StringBuffer toSB() { 
			return ltFirst.toSB(true).
			append((ltSecond!=null)? ltSecond.toSB(false) : "").
			append("<=").
			append(linSet.toString());
		}
		
		public NConsequence(LinearTerm aLtFirst, LinearTerm aLtSecond, LinSet aLinSet) {
			ltFirst = aLtFirst;
			ltSecond = aLtSecond;
			linSet = aLinSet;
		}
		
		public LinearConstr generateTransitionConstrs(int e, int f, Variable k) {
			LinearConstr lc = new LinearConstr();
			
			//if (this.equalsToTrue(f))
			//	return lc;
			
			Point base = linSet.getBase();
			
			int a = base.getLength();
			int b = base.getWeight();
			
			lc.addLinTerm(new LinearTerm(ltFirst.variable(),ltFirst.coeff()));
			if (ltSecond!=null)
				lc.addLinTerm(new LinearTerm(ltSecond.variable(),ltSecond.coeff()));
			
			int c = 0;
			int d = 0;
			
			Point gen = linSet.getGenerator();
			if (gen != null) {
				c = gen.getLength();
				d = gen.getWeight();
			}
			
			// TODO: check and remove
			{
				int constantK = -(d*f);
				int constant  = -(c*b+d*(e-a));
				
				// TODO: can all the coefficients be always divided by 'c' ??
				if (c!=0 && (constantK % c != 0 || constant % c != 0)) 
				//if (constantK % c != 0 || constant % c != 0) // c==0 does not reach this code
					throw new RuntimeException("unexpected values during computation");
				if (c!=0) {
					constantK /= c;
					constant  /= c;
				}
			}
			
			//int constantK = -(d*f)/c;
			//int constant  = -(b + (d*(e-a))/c);
			int constantK = 0;
			int constant  = -b;
			if (c != 0) { // c==0 iff d==0
				constantK = -(d*f)/c;
				constant -= (d*(e-a))/c;
			}
			
			//if (!canIgnoreK(f))
			//if (f != 0)
			lc.addLinTerm(new LinearTerm(k, constantK));
			
			lc.addLinTerm(new LinearTerm(null, constant));
			
			//lc.normalizeByGCD();
			
			return lc;
		}
		
		/**
		 * X <= c + k  equiv. to X<=c \/ X<=c+1 \/ ...   equiv. to X <= inf equiv. to True 
		 *    ----> remove  
		 * X <= c - k  equiv. to X<=c \/ X<=c-1 \/ ...   equiv. to X <= c
		 *    ----> keep just X <= c 
		 */
		/*// (a,b)+(c,d)N      c==1 AND d==1
		public boolean equalsToTrue(int period) {
			Point gen = linSet.getGenerator();
			int incr = (gen == null)? 0 : gen.getWeight();
			return period==1 && incr==1;
		}
		// (a,b)+(c,d)N      c==1 AND d==-1
		private boolean canIgnoreK(int period) {
			Point gen = linSet.getGenerator();
			int incr = (gen == null)? 0 : gen.getWeight();
			return period==0 || (period==1 && incr==-1);
		}*/
	}
	private static class NImplication {
		
		FlataLinSet premise;
		List<NConsequence> consequences = new LinkedList<NConsequence>();
		
		public String toString() {
			return "premise: "+premise+"\n"+
			"consequences: "+consequences+"\n";
		}
		
		
		private NImplication() {}
		public NImplication(/*LinTerm lt1, LinTerm lt2,*/ FlataLinSet ls) {
			premise = ls;
			// consequences empty (~ True)
		}
		public NImplication(LinearTerm lt1, LinearTerm lt2, LinSet ls) {
			
			premise = new FlataLinSet(ls);
			consequences.add(new NConsequence(lt1, lt2, ls));
		}
		
		public static NImplication merge(NImplication impl1, NImplication impl2) {
			
			FlataLinSet premise = FlataLinSet.merge(impl1.premise,impl2.premise);
			
			if (premise==null)
				return null;
			
			NImplication impl = new NImplication();
			impl.premise = premise;
			impl.consequences.addAll(impl1.consequences);
			impl.consequences.addAll(impl2.consequences);
			
			return impl;
		}
		
		public Collection<Relation> generateTransitionConstrs() {
			LinearRel lcs = new LinearRel();
			
			int e = premise.baseN;
			int f = premise.genN;
			
			for (NConsequence conseq : consequences) {
				//if (!conseq.equalsToTrue(f))
					lcs.add(conseq.generateTransitionConstrs(e,f, k));
			}
			
			// create $k>=0 constraint (-$k<=0)
			Collection<Relation> col = new LinkedList<Relation>();
			if (f != 0) {
				LinearConstr constr_k = new LinearConstr();
				constr_k.addLinTerm(new LinearTerm(k,-1));
				lcs.add(constr_k);
				
				//ModuloRel mr = new ModuloRel(lcs);
				//col.addAll(mr.existEliminate(k));
				col.addAll(Arrays.asList(Relation.toMinType(lcs).existElim2(k)));
			} else {
				col.add(Relation.toMinType(lcs));
			}
			
			return col;
		}
		
	}
	
	private List<NImplication> implList;
	
	private Vector<String> st; 
	
	/*private boolean isCorrect(LinSet ls) {
		return ls.getBase().getLength()!=0;
	}*/
	// returns true if at least one is correct
	/*private boolean isCorrect(SLSet lset) {
		
		for (LinSet ls : lset.getLinearSets()) {
			if (isCorrect(ls))
				return true;
		}
		
		return false;
	}*/
	
	private List<NImplication> createImplList(LinearTerm lt1, LinearTerm lt2, SLSet sls) {
		List<NImplication> il = new LinkedList<NImplication>();
		
		// create partition on positive integers based on a given semi-linear set in two steps
		
		// 1. add all the linear sets which appear in a given semi-linear set
		for (LinSet ls :sls.getLinearSets()) {
			
			/*NConsequence conseq = new NConsequence(lt1,lt2,ls);
			
			il.add(new NImplication(conseq));*/
						
			il.add(new NImplication(lt1,lt2,ls));
		}
		
		// 2. add all linear sets missing in a given semi-linear set
		FlataSLSet complement = FlataSLSet.ofpca2flata(sls).complement();
		for (FlataLinSet ls : complement.toCollection()) {
			
			//il.add(new NImplication(lt1,lt2,ls));
			il.add(new NImplication(ls));
		}
		
		return il;
	}
	private List<NImplication> mergeImplLists(List<NImplication> l1, List<NImplication> l2) {
		List<NImplication> il = new LinkedList<NImplication>();
		for (NImplication first : l1) {
			for (NImplication second : l2) {
				NImplication impl = NImplication.merge(first,second);
				if (impl!=null)
					il.add(impl);
			}
		}
		return il;
	}
	
	private void processMatrix(OfpcaOutput aOA, int aMatInx) {
		SLSet[][] matrix = aOA.getEdges()[aMatInx];
		
		for (int m = 0; m < st.size(); m++) {
			String x = st.get(m);
			for (int n = 0; n < st.size(); n++) {
				SLSet sls = matrix[m][n];
				
				if (sls==null || sls.empty())
					continue;

				// TODO: change
				//if (!isCorrect(sls))
				//	continue;
				
				if (DEBUG >= DEBUG_DETAIL)
					System.out.println(implList);
				
				String y = st.get(n);
				
				LinearTerm lt1 = new LinearTerm(
						varPool.giveVariable(x + (OfpcaOutput.isFirstVarPrimed(aMatInx)? Variable.primeSuf : "")), 
						1 * (OfpcaOutput.isFirstVarPlus(aMatInx)? 1 : -1)
						);
				LinearTerm lt2 = new LinearTerm(
						varPool.giveVariable(y + (OfpcaOutput.isSecondVarPrimed(aMatInx)? Variable.primeSuf : "")), 
						1 * (OfpcaOutput.isSecondVarPlus(aMatInx)? 1 : -1)
						); 
				
				if (implList == null)
					implList = createImplList(lt1, lt2, sls);
				else
					implList = mergeImplLists(implList,createImplList(lt1, lt2, sls));
			}
		}
	}
	
	private void processVector(OfpcaOutput aOA, int aVecInx) {
		
		for (int m = 0; m < st.size(); m++) {
			String x = st.get(m);
			
			SLSet sls = aOA.getSingular(aVecInx, m);
				
			if (sls==null || sls.empty())
				continue;
			
			LinearTerm lt1 = new LinearTerm(
					varPool.giveVariable(x + (OfpcaOutput.isVarPrimed(aVecInx)? Variable.primeSuf : "")), 
					1 * (OfpcaOutput.isVarPlus(aVecInx)? 1 : -1)
					); 
			
			if (implList.isEmpty())
				implList = createImplList(lt1, null, sls);
			else
				implList = mergeImplLists(implList,createImplList(lt1, null, sls));
		}		
	}
	
	private Collection<Relation> generateTransitionConstrs() {
		
		Collection<Relation> col = new LinkedList<Relation>();
		
		for (NImplication impl : this.implList) {
			col.addAll(impl.generateTransitionConstrs());
		}
		
		return col;
	}
	
	// !! why is aSubstitution not used?
	public Collection<Relation> eliminate(OfpcaOutput aOA, LinearTerm[] aSubstitution) {
//CR.printMemUsage("Entry of eliminate", true);
		
		st = aOA.getSymbolTable();
		
		processMatrix(aOA, Output.UNP_UNP_POTENTIAL);
		processMatrix(aOA, Output.UNP_PRIMED_POTENTIAL);
		processMatrix(aOA, Output.PRIMED_UNP_POTENTIAL);
		processMatrix(aOA, Output.PRIMED_PRIMED_POTENTIAL);
		//x+y<=a
		processMatrix(aOA, Output.UNP_UNP_PLUS); // symmetric matrix
		processMatrix(aOA, Output.UNP_PRIMED_PLUS);
		//processMatrix(aCM, aOfpcaOut, Output.PRIMED_UNP_PLUS); // commutative x+y=y+x for (UNP,PRIMED)-(PRIMED,UNP)
		processMatrix(aOA, Output.PRIMED_PRIMED_PLUS); // symmetric matrix
		//-x-y<=a
		processMatrix(aOA, Output.UNP_UNP_MINUS); // symmetric matrix
		processMatrix(aOA, Output.UNP_PRIMED_MINUS);
		//processMatrix(aCM, aOfpcaOut, Output.PRIMED_UNP_MINUS); // commutative -x-y=-y-x for (UNP,PRIMED)-(PRIMED,UNP)
		processMatrix(aOA, Output.PRIMED_PRIMED_MINUS); // symmetric matrix
		
		processVector(aOA, Output.UNPRIMED_MINUS);
		processVector(aOA, Output.UNPRIMED_PLUS);
		processVector(aOA, Output.PRIMED_MINUS);
		processVector(aOA, Output.PRIMED_PLUS);
		
		if (DEBUG >= DEBUG_TINY)
			System.out.println("####################\nfinal implication list:\n"+implList);
		
		// create transitions
//CR.printMemUsage("Exit of eliminate", true);
		try {
			return generateTransitionConstrs();
		} catch (OutOfMemoryError e) {
			System.out.println("Size of impl.list: "+implList.size());
//CR.printMemUsage("Exit of eliminate", true);
			throw e;
		}
	}
	
}
