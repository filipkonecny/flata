package verimag.sil;

import java.util.*;

import verimag.flata.automata.ca.CA;
import verimag.flata.automata.ca.CAState;
import verimag.flata.automata.ca.CATransition;
import verimag.flata.presburger.*;

public abstract class Node {
	
	public abstract Node normalize(SymbolTable aST);
	public abstract Node nnf(boolean b);
	public Node nnf() { return nnf(false); }
	public DnfForm dnf() { return dnf_base().ensureMerged(); }
	protected abstract DnfForm dnf_base();
	public abstract List<Node> collectConjuncts();
	
	public abstract StringBuffer toSB();
	public String toString() { return this.toSB().toString(); }
	
	public Node not() { return new NodeNot(this); }
	public Node and(Node that) { return new NodeAnd(this,that); }
	public Node or(Node that) { return new NodeOr(this,that); }
	
	
	
	/*
	 * merging of forall formulas
	 */
	
	////////////// LeafNode ///////////////////
	public static abstract class NodeLeaf extends Node {
		//protected boolean isNegated = false;
		public Node nnf(boolean b) {
			//isNegated = !(isNegated == b);
			return b? this.not() : this;
		}
		protected DnfForm dnf_base() {
			return DnfForm.fromAtom(this);
		}
		public List<Node> collectConjuncts() {
			List<Node> ret = new LinkedList<Node>();
			ret.add(this);
			return ret;
		}
	}
	public static abstract class Access {
		protected int coeff = 1; // 0,1,-1
		public Access(int aCoeff) { coeff = aCoeff; }
		public Access() { this(1); }
		public Access(Access other) { coeff = other.coeff; }
		public abstract Access copy();
		public abstract LinearTerm toCAterm(VariablePool aVP);
		
		public void setCoeff(int aCoeff) { coeff = aCoeff; }
		protected Access unminus() {
			Access ret = this.copy();
			ret.coeff *= -1;
			return ret;
		}
	}
	public static class AccessConst extends Access {
		private int val;
		
		public AccessConst(long aVal) {
			super(1);
			val = (int)aVal;
		}
		public AccessConst(AccessConst that) { 
			super(1);
			val = that.coeff*that.val; 
		}
		public Access copy() { return new AccessConst(this); }
		public String toString() { return coeff+" * "+val; }
		public LinearTerm toCAterm(VariablePool aVP) {
			return LinearTerm.constant(val*coeff);
		}
		
	}
	public static class AccessScalar extends Access {
		protected String var;
		
		public AccessScalar(String aVar) { this(1,aVar); }
		public AccessScalar(int aCoeff, String aVar) {
			super(aCoeff);
			var = aVar;
		}
		public AccessScalar(AccessScalar that) {
			super(that);
			var = that.var;
		}
		public Access copy() { return new AccessScalar(this); }
		public String toString() { return coeff+" * "+var; }
		public LinearTerm toCAterm(VariablePool aVP) {
			return LinearTerm.create(aVP.giveVariable(var), this.coeff);
		}
	}
	public static class AccessArray extends Access {
		protected String var;
		private String index;
		private int offset;
		
		public AccessArray(String aVar, String aInx, int aOffset) {
			this(1,aVar,aInx,aOffset);
		}
		public AccessArray(int aCoeff, String aVar, String aInx, int aOffset) {
			super(aCoeff);
			var = aVar;
			index = aInx;
			offset = aOffset;
		}
		public AccessArray(AccessArray that) {
			super(that);
			var = that.var;
			index = that.index;
			offset = that.offset;
		}
		public Access copy() { return new AccessArray(this); }
		public String toString() { return coeff+" * "+var+"["+index+"+"+offset+"]"; }
		public LinearTerm toCAterm(VariablePool aVP) {
			assert(index != null);
			assert(0<=offset && offset<=1);
			Variable v = aVP.giveVariable(var);
			if (offset==1)
				v = v.getCounterpart();
			return LinearTerm.create(v, this.coeff);
		}
	}
	
	public static class SumTerms {
		// t_1 + t_2 + ... + t_n + c where t1 is of the form +/- a[i+c] or +/- p
		private List<Access> terms = new LinkedList<Access>();
		private int c = 0;
		public void add(int aC) {
			c += aC;
		}
		public void add(Access val) {
			if (val instanceof AccessConst) {
				c += ((AccessConst)val).val;
			} else {
				terms.add(val);
			}
		}
		public SumTerms() {}
		public SumTerms(Access val) { add(val); }
		public SumTerms(int val) { c = val; }
		public SumTerms(SumTerms that) {
			this.terms.addAll(that.terms);
			this.c = that.c;
		}
		
		public int constant() { return c; }
		
		public StringBuffer toSB() {
			Iterator<Access> i = terms.iterator();
			StringBuffer sb = new StringBuffer(i.next().toString());
			while (i.hasNext()) {
				sb.append(" + ").append(i.next().toString());
			}
			return sb.append(" + "+c);
		}
		
		public SumTerms plus(SumTerms that) {
			SumTerms ret = new SumTerms(this);
			ret.terms.addAll(that.terms);
			ret.c += that.c;
			return ret;
		}
		public SumTerms unminus() {
			SumTerms ret = new SumTerms(-1 * this.c);
			for (Access t : terms) {
				ret.terms.add(t.unminus());
			}
			return ret;
		}
		public SumTerms minus(SumTerms that) {
			return this.plus(that.unminus());
		}
		public void multiplyWith(int val) {
			c *= val;
			for (Access a : terms) {
				a.coeff *= val;
			}
		}
		
		protected int countArrayAccesses() {
			int x=0;
			for (Access t : terms)
				if (t instanceof AccessArray)
					x++;
			return x;
		}
		protected boolean hasIndexAccess(String inx) {
			for (Access t : terms)
				if (t instanceof AccessScalar && ((AccessScalar)t).var.equals(inx))
					return true;
			return false;
		}
		protected boolean hasScalarAccess(String inx) {
			for (Access t : terms)
				if (t instanceof AccessScalar && !((AccessScalar)t).var.equals(inx))
					return true;
			return false;
		}
		private boolean noConstTerm() {
			for (Access t : terms) {
				if (t instanceof AccessConst)
					return false;
			}
			return true;
		}
		private boolean hasOctCoef() {
			for (Access t : terms) {
				if (Math.abs(t.coeff) > 1)
					return false;
			}
			return true;
		}
		public boolean check() {
			if (terms.size()>3 || (terms.size() == 3 && noConstTerm()) || !hasOctCoef())
				return false;
			
			return true;
		}
		
		public PropType whichCase (Variable qVar) {
			int x_a = 0;
			int x_p = 0;
			int x_i = 0;
			int x_c = 0;
			for (Access t : terms) {
				if (t instanceof AccessConst) {
					x_c++;
				} else if (t instanceof AccessScalar) {
					AccessScalar a = (AccessScalar)t;
					if (a.var.equals(qVar.name())) {
						x_i++;
					} else {
						x_p++;
					}
				} else if (t instanceof AccessArray) {
					x_a++;
				}
			}
			assert(x_a>=1 && x_a+x_p+x_i<=2 && x_c==0);
			if (x_a==2) return PropType.ARR_ARR;
			else if (x_p==1) return PropType.ARR_SC;
			else if (x_i==1) return PropType.ARR_INX;
			else return PropType.ARR;
		}
		private AccessArray getArr(boolean smaller) {
			AccessArray a1 = null, a2 = null;
			for (Access t : terms) {
				if (t instanceof AccessArray) {
					if (a1==null) a1 = (AccessArray)t;
					else a2 = (AccessArray)t;
				}
			}
			return ((a1.offset<=a2.offset) == smaller)? a1 : a2; 
		}
		public AccessArray getArrSmaller() { return getArr(true); }
		public AccessArray getArrGreater() { return getArr(false); }
		public AccessArray getFirstArr() {
			for (Access t : terms)
				if (t instanceof AccessArray)
					return (AccessArray)t;
			assert(false); return null;
		}
		public AccessScalar getFirstSc() {
			for (Access t : terms)
				if (t instanceof AccessScalar)
					return (AccessScalar)t;
			assert(false); return null;
		}
		public LinearConstr toCAconstr(VariablePool aVP) {
			LinearConstr ret = LinearConstr.createConst(this.c);
			for (Access a : terms) {
				ret.addLinTerm(a.toCAterm(aVP));
			}
			return ret;
		}
	}
	
	public static enum ROP {
		EQ,LEQ,GEQ,LT,GT;
	}
	public static enum PropType {
		ARR_ARR,ARR_INX,ARR_SC,ARR;
	}
	public static class ForallConstr {
		private SumTerms terms = new SumTerms();
		private ROP rop = ROP.LEQ;
		
		public ForallConstr(SumTerms aTerms, ROP aRop) {
			terms = aTerms;
			rop = aRop;
		}
		
		public StringBuffer toSB() {
			return new StringBuffer("(").append(terms.toSB()).append(" "+rop+" 0)");
		}
		public String toString() {
			return toSB().toString();
		}
		
		protected void toLEQorEQ() {
			if (rop != ROP.EQ) {
				switch (rop) {
				case LT:
					// t < 0 IFF t <= -1 IFF t+1 <= 0
					terms.add(1);
					break;
				case GEQ:
					// t >= 0 IFF -t <= 0
					terms.multiplyWith(-1);
					break;
				case GT:
					// t > 0 IFF t >= 1 IFF 1-t <= 0
					terms.multiplyWith(-1);
					terms.add(1);
					break;
				}
				rop = ROP.LEQ;
			}
		}
		
		public LinearRel toCAconstr(VariablePool aVP) {
			assert(rop==ROP.EQ || rop==ROP.LEQ);
			LinearConstr c = terms.toCAconstr(aVP);
			LinearRel ret = new LinearRel(c);
			if (rop==ROP.EQ) {
				ret.addConstraint(LinearConstr.transformBetweenGEQandLEQ(c));
			}
			return ret;
		}
		
		protected int getH(Variable index) {
			if (terms.whichCase(index) == PropType.ARR_ARR) {
				return terms.getArrGreater().offset;
			} else {
				return 0;
			}
		}
	}
	

	
	public static class NodeForall extends NodeLeaf {
		private Variable index;
		private LinearConstr lower,upper;
		private ForallConstr prop;
		
		public NodeForall(Variable aIndex, LinearConstr aLower, LinearConstr aUpper, ForallConstr aConstr) {
			index = aIndex;
			lower = aLower;
			upper = aUpper;
			prop = aConstr;
		}
		public static Node create(Variable aIndex, LinearConstr aLower, LinearConstr aUpper, List<ForallConstr> aList) {
			Iterator<ForallConstr> i = aList.iterator();
			Node ret = new NodeForall(aIndex,aLower,aUpper,i.next());
			while (i.hasNext()) {
				ret = new NodeAnd(ret,new NodeForall(aIndex,aLower,aUpper,i.next()));
			}
			return ret;
		}
		public static Node create(String par, AccessArray arr, SymbolTable st) {
			// p = a[m+c] becomes forall i . (m+c <= i <= m+c) -> a[i] = p
			
			Variable inx = st.giveIndexVar(st.freshIndex());
			
			SumTerms s_terms = new SumTerms(new AccessArray(arr.var, inx.name(), 0) ); // a[i]
			s_terms = s_terms.minus(new Node.SumTerms( new AccessScalar(par) )); // -p
			
			ForallConstr c_forall = new ForallConstr(s_terms, ROP.EQ);
			
			LinearConstr c = new LinearConstr();
			c.addLinTerm(LinearTerm.constant(arr.offset));
			if (arr.index != null) {
				c.addLinTerm(LinearTerm.create(st.vp.giveVariable(arr.index), 1));
			}
			
			return new NodeForall(inx,c,new LinearConstr(c),c_forall);
		}
		
		public StringBuffer toSB() {
			String s_low = (lower==null)? "-" : lower.toStringNoROP().toString();
			String s_up = (upper==null)? "-" : upper.toStringNoROP().toString();
			return new StringBuffer("(forall "+index+" . ("+s_low+" <= "+index+" <= "+s_up+") -> ("+prop.toSB()+"))");
		}
		
		/* 
		 * The ARR_ARR type needs to be further normalized by introducing 
		 * fresh array variables to obtain only indexing [i] or [i+1]
		 * 
		 */
		private Node normalizeArrays(SymbolTable aST) {
			AccessArray a1 = prop.terms.getArrSmaller();
			AccessArray a2 = prop.terms.getArrGreater();
			
			assert(a1.offset == 0);
			if (a2.offset <= 1) {
				return this;
			} else {
				/*
				 * forall i . low <= i <= up => +/- a[i] +/- b[i+d] ROP e  where d>=2 and ROP is non-strict (!!)
				 * becomes a conjunction of (aux_1,...,aux_{d-1} are fresh array variables)
				 *   forall i . low <= i <= up => +/- a[i] - aux1[i+1] ROP 0
				 *   forall i . low+1 <= i <= up+1 => aux1[i] - aux2[i+1] ROP 0
				 *   forall i . low+2 <= i <= up+2 => aux2[i] - aux3[i+1] ROP 0
				 *   ...
				 *   forall i . low+(d-1) <= i <= up+(d-1) => aux_{d-1}[i] +/- b[i+1] <= e
				 */
				
				int d = a2.offset;
				int e = prop.terms.constant();
				
				// create auxiliary array symbols
				String[] fresh = new String[d-1];
				for (int i=0; i<d-1; i++) {
					fresh[i] = aST.freshArray();
				}
				
				Node n = null;
				String inx = a1.var;
				// first forall
				{
					SumTerms forall = new SumTerms(0);
					forall.add(new AccessArray(a1.coeff,a1.var,inx,0));
					forall.add(new AccessArray(-1,fresh[0],inx,1));
					LinearConstr off_c = LinearConstr.createConst(0);
					n = new NodeForall(index, unbPlus(lower,off_c), unbPlus(upper,off_c), new ForallConstr(forall,prop.rop));
				}
				// last forall
				{
					SumTerms forall = new SumTerms(e);
					forall.add(new AccessArray(1,fresh[d-2],inx,0));
					forall.add(new AccessArray(a2.coeff,a2.var,inx,1));
					LinearConstr off_c = LinearConstr.createConst(d-1);
					n = new NodeAnd(n,new NodeForall(index, unbPlus(lower,off_c), unbPlus(upper,off_c), new ForallConstr(forall,prop.rop)));
				}
				// the others
				for (int i=0; i<d-2; i++) {
					SumTerms forall = new SumTerms(0);
					forall.add(new AccessArray(1,fresh[i],inx,0));
					forall.add(new AccessArray(-1,fresh[i+1],inx,1));
					LinearConstr off_c = LinearConstr.createConst(i+1);
					n = new NodeAnd(n,new NodeForall(index, unbPlus(lower,off_c), unbPlus(upper,off_c), new ForallConstr(forall,prop.rop)));
				}
				
				return n;
			}
		}
		private static LinearConstr unbPlus(LinearConstr bnd, LinearConstr c) {
			return (bnd==null)? null : bnd.plus(c);
		}
		
		public Node normalize(SymbolTable aST) {

			// first, normalize to EQ or LEQ
			this.prop.toLEQorEQ();
			
			Node n = null;
			switch (prop.terms.whichCase(index)) {
			/*
			 * forall i . low <= i <= up => +/- a[i+c] <= e  
			 * becomes
			 * forall i . low+c <= i <= up+c => +/- a[i] <= e   
			 */
			case ARR: {
				AccessArray a = prop.terms.getFirstArr();
				int e = prop.terms.constant();
				int off = a.offset;
				SumTerms forall = new SumTerms(e);
				forall.add(new AccessArray(a.coeff,a.var,a.index,0));
				LinearConstr off_c = LinearConstr.createConst(off);
				n = new NodeForall(index, unbPlus(lower,off_c), unbPlus(upper,off_c), new ForallConstr(forall,prop.rop));
				break;
			}
			/* 
			 * forall i . low <= i <= up => +/- a[i+c] +/- b[i+d] <= e  where c<=d
	 		 *   becomes
			 * forall i . low+c <= i <= up+c => +/- a[i] +/- b[i+(d-c)] <= e  where c<=d
			 */
			case ARR_ARR: {
				AccessArray a1 = prop.terms.getArrSmaller();
				AccessArray a2 = prop.terms.getArrGreater();
				int e = prop.terms.constant();
				int diff = a2.offset - a1.offset;
				int off1 = a1.offset;
				SumTerms forall = new SumTerms(e);
				forall.add(new AccessArray(a1.coeff,a1.var,a1.index,0));
				forall.add(new AccessArray(a2.coeff,a2.var,a2.index,diff));
				LinearConstr off_c = LinearConstr.createConst(off1);
				n = new NodeForall(index, unbPlus(lower,off_c), unbPlus(upper,off_c), new ForallConstr(forall,prop.rop)).normalizeArrays(aST);
				break;
			}
			/* 
			 * forall i . low <= i <= up => +/- a[i+c] +/- p <= e  
	 		 * becomes
			 * forall i . low+c <= i <= up+c => +/- a[i] +/- p <= e  
			 */
			case ARR_SC: {
				AccessArray a = prop.terms.getFirstArr();
				AccessScalar s = prop.terms.getFirstSc();
				int e = prop.terms.constant();
				int off = a.offset;
				SumTerms forall = new SumTerms(e);
				forall.add(new AccessArray(a.coeff,a.var,a.index,0));
				forall.add(new AccessScalar(s.coeff,s.var));
				LinearConstr off_c = LinearConstr.createConst(off);
				n = new NodeForall(index, unbPlus(lower,off_c), unbPlus(upper,off_c), new ForallConstr(forall,prop.rop));
				break;
			}
			/*
			 * forall i . low <= i <= up => +/- a[i+c] +/- i <= e
	 		 * becomes
			 * forall i . low+c <= i <= up+c => +/- a[i] +/- (i-c) <= e
			 */
			case ARR_INX: {
				AccessArray a = prop.terms.getFirstArr();
				AccessScalar s = prop.terms.getFirstSc();
				int e = prop.terms.constant();
				int off = a.offset;
				SumTerms forall = new SumTerms(e+off*s.coeff);
				forall.add(new AccessArray(a.coeff,a.var,a.index,0));
				forall.add(new AccessScalar(s.coeff,s.var));
				LinearConstr off_c = LinearConstr.createConst(off);
				n = new NodeForall(index, unbPlus(lower,off_c), unbPlus(upper,off_c), new ForallConstr(forall,prop.rop));
				break;
			}
			default:
				assert(false);
			}
			return n;
		}
	}
	

	
	// class representing normalized array properties more compactly by grouping properties with same guard together
	public static class NodeForallConjunctive extends NodeLeaf {
		//private boolean isNegated;
		private Variable index;
		private LinearConstr lower,upper;
		private List<ForallConstr> prop;
		@Override
		public Node normalize(SymbolTable aST) {
			assert(false); return null;
		}
		@Override
		public StringBuffer toSB() {
			String s_low = (lower==null)? "-" : lower.toStringNoROP().toString();
			String s_up = (upper==null)? "-" : upper.toStringNoROP().toString();
			StringBuffer aux = new StringBuffer();
			Iterator<ForallConstr> i = prop.iterator();
			while (i.hasNext()) {
				aux.append(i.next().toSB());
				if (i.hasNext()) {
					aux.append(" && ");
				}
			}
			return new StringBuffer("(forall "+index+" . ("+s_low+" <= "+index+" <= "+s_up+") -> ("+aux+"))");
		}
		
		private static List<NodeForall> asList(NodeForall aConstr) {
			List<NodeForall> aux = new LinkedList<NodeForall>();
			aux.add(aConstr);
			return aux;
		}
		private NodeForallConjunctive(NodeForall aConstr) {
			this(asList(aConstr));
		}
		private NodeForallConjunctive(List<NodeForall> aConstr) {
			NodeForall aux = aConstr.get(0);
			//isNegated = neg;
			index = aux.index;
			lower = aux.lower;
			upper = aux.upper;
			prop = new LinkedList<ForallConstr>();
			for (NodeForall n : aConstr) {
				prop.add(n.prop);
			}
		}
		
		// TODO: treat conjuncts with h==0 differently from those with h==1
		// for now, computed as max
		private int getH() {
			for (Node.ForallConstr c : prop) {
				if (c.getH(index) == 1) {
					return 1;
				}
			}
			return 0;
		}
		private static LinearRel tick(Variable var) {
			LinearRel ticks = new LinearRel();
			LinearConstr tick = new LinearConstr();
			Variable inxVar = var;
			Variable inxVarP = var.getCounterpart();
			tick.addLinTerm(new LinearTerm(inxVar,1));
			tick.addLinTerm(new LinearTerm(inxVarP,-1));
			tick.addLinTerm(new LinearTerm(null,1));
			ticks.add(tick); // add i-i'<=-c
			tick = new LinearConstr(tick);
			tick.transformBetweenGEQandLEQ(); // transform i-i'<=-c to -i+i'<=c
			ticks.add(tick); // add -i+i'<=c
			return ticks;
		}
		
		public LinearRel toCAconstr(VariablePool aVP) {
			Iterator<Node.ForallConstr> i = prop.iterator();
			LinearRel aux = i.next().toCAconstr(aVP);
			while (i.hasNext()) {
				aux = aux.and(i.next().toCAconstr(aVP));
			}
			return aux;
		}
		public Collection<LinearConstr> toCAconstrNeg(VariablePool aVP) {
			Collection<LinearConstr> col = new LinkedList<LinearConstr>();
			
			LinearRel aux = this.toCAconstr(aVP);
			for (LinearConstr constr : aux.constraints()) {
				LinearConstr aux2 = new LinearConstr(constr);
				aux2.negate();
				col.add(aux2);
			}
			return col;
		}
		
		private void createTransitionsForAll(Collection<LinearConstr> aNegUpsilon, CAState aFrom, CAState aTo, CA aCA, LinearRel aConstraints) {
			for (LinearConstr constr: aNegUpsilon) {
				LinearRel new_lc = new LinearRel(aConstraints);
				new_lc.addConstraint(constr);
				aCA.addTransitionOnlySat(new CATransition(aFrom,aTo,new CompositeRel(new_lc),aCA));
			}
		}
		private LinearRel createFalse() {
			LinearConstr c1 = new LinearConstr();
			c1.addLinTerm(LinearTerm.constant(1)); // false
			return new LinearRel(c1);
		}
		/*
	       qi --[i=0 && i'=i+1 && N=1]--> qf
	       qi --[i=0 && i'=i+1 && N>1]--> q1
	       q1 --[i<N-1 && i'=i+1]--> q1
	       q1 --[i=N-1 && i'=i+1]--> qf
	    */
		public static CA caForallDummy(SymbolTable aST) {
			CA ca = new CA("aux",aST.vp);
			CAState qi = ca.getStateWithName("qi");
			CAState q1 = ca.getStateWithName("q1");
			CAState qe = ca.getStateWithName("qf");
			
			ca.setInitial(qi);
			ca.setError(qe);
			
			Variable wn = aST.arrLen();
			Variable vTick = aST.tick();
			
			LinearRel tick = tick(vTick);
						
			LinearRel i_eq_0 = LinearRel.createEq(LinearTerm.create(vTick,1), LinearTerm.constant(0));
			LinearRel N_eq_1 = LinearRel.createEq(LinearTerm.create(wn,1), LinearTerm.constant(1));
			LinearRel N_gt_1 = new LinearRel(LinearConstr.gt(LinearTerm.create(wn,1),LinearTerm.constant(1)));
			
			LinearConstr c_i = new LinearConstr(LinearTerm.create(vTick,1));
			LinearConstr c_Nm1 = LinearConstr.createConst(-1).plus(new LinearConstr(LinearTerm.create(wn,1)));
			LinearRel i_eq_Mn1 = LinearRel.createEq(c_i,c_Nm1);
			LinearRel i_lt_Mn1 = new LinearRel(c_i.lt(c_Nm1));
			
			
			// qi --[i=0 && i'=i+1 && N=1]--> qf
			LinearRel r1 = tick.copy().and(i_eq_0).and(N_eq_1);
			// qi --[i=0 && i'=i+1 && N>1]--> q1
			LinearRel r2 = tick.copy().and(i_eq_0).and(N_gt_1);
			// q1 --[i<N-1 && i'=i+1]--> q1
			LinearRel r3 = tick.copy().and(i_lt_Mn1);
			// q1 --[i=N-1 && i'=i+1]--> qf
			LinearRel r4 = tick.copy().and(i_eq_Mn1);
			
			ca.addTransitionOnlySat(new CATransition(qi,qe,new CompositeRel(r1),ca));
			ca.addTransitionOnlySat(new CATransition(qi,q1,new CompositeRel(r2),ca));
			ca.addTransitionOnlySat(new CATransition(q1,q1,new CompositeRel(r3),ca));
			ca.addTransitionOnlySat(new CATransition(q1,qe,new CompositeRel(r4),ca));
			
			return ca;
		}
		
		
		// translation for several forall properties with the same guard
		// 
		public CA toCA(SymbolTable aST, boolean isNegated) {
			
			
			// TODO: distinguish properties with h=1 from those with h=0
			int h = getH();
			
//			// split the list according to a[i+1] accesses
//			List<Node.NodeForall> list0 = new LinkedList<Node.NodeForall>();
//			List<Node.NodeForall> list1 = new LinkedList<Node.NodeForall>();
//			for (Node.NodeForall n : list) {
//				int h = n.getH();
//				assert(0<=h && h<=1);
//				if (h == 0) {
//					list0.add(n);
//				} else {
//					list1.add(n);
//				}
//			}
			// h's are different in general, in range {0,1}
			// h==1 has an impact on whether a property should be enforced at the transitions leading to the control state qe
			// construct upsilon0 and upsilon1:
			//   upsilon0 -- conjunction of those properties with h==0
			//   upsilon1 -- conjunction of those properties with h==1
			
			CA ca = new CA("aux",aST.vp);
			
			Variable wn = aST.arrLen();
			Variable vTick = aST.tick();
			
			CAState qi = ca.getStateWithName("qi");
			CAState q1 = ca.getStateWithName("q1");
			CAState q2 = ca.getStateWithName("q2");
			CAState q3 = ca.getStateWithName("q3");
			CAState qe = ca.getStateWithName("qf");
			
			/*
			 * forall formula is not negated
			 *   q1 not needed if lower=-inf
			 *   q3 not needed if upper=inf
			 * forall formula is negated
			 *   q1 not needed if lower=-inf
			 *   Note: q3 still needed (!)
			 */			
			
			ca.setInitial(qi);
			ca.setError(qe);
			
			LinearRel tickAll = tick(vTick);
			
			LinearRel upsilon = this.toCAconstr(aST.vp);
			
			LinearRel lessWNminus1;
			LinearRel eqWNminus1;
			{
				LinearConstr c1 = new LinearConstr(); // tau_upsilon-wN+2
				c1.addLinTerm(new LinearTerm(vTick,1));
				c1.addLinTerm(new LinearTerm(wn,-1));
				c1.addLinTerm(new LinearTerm(null,2));
				lessWNminus1 = new LinearRel(c1); // tau_upsilon-wN+2<=0
				
				c1.addLinTerm(new LinearTerm(null, -1)); // tau_upsilon-wN+1
				eqWNminus1 = new LinearRel(c1); // tau_upsilon-wN+1<=0
				LinearRel c2 = LinearRel.getGEQtoLEQ(eqWNminus1);
				eqWNminus1.addAll(c2); // tau_upsilon-wN+1=0
			}
			
			// tau_upsilon = 0
			LinearRel tau_upsilonEQ0;
			{
				LinearConstr c1 = new LinearConstr(); // tau_upsilon <= 0
				c1.addLinTerm(new LinearTerm(vTick,1));
				LinearConstr c2 = LinearConstr.transformBetweenGEQandLEQ(c1); // tau_upsilon >= 0
				tau_upsilonEQ0 = new LinearRel(c1);
				tau_upsilonEQ0.addConstraint(c2);
			}

			// f <= g  (denotes lower <= upper) // true if f=-inf or g=inf
			LinearRel fLEQg;
			{
				fLEQg = new LinearRel(); // true
				if (lower != null && upper != null) {
					LinearConstr c1 = new LinearConstr(upper);
					c1.transformBetweenGEQandLEQ();
					c1.addLinTerms(lower.values());
					fLEQg.addConstraint(c1);
				}
			}

			// tau_upsilon+1=f // false if f=-inf
			LinearRel tau_upsilonPLUS1EQf;
			{
				if (lower==null) {
					tau_upsilonPLUS1EQf = createFalse();
				} else {
					LinearConstr c1 = new LinearConstr(lower);
					c1.transformBetweenGEQandLEQ(); // -f
					c1.addLinTerm(new LinearTerm(null, 1)); // +1
					c1.addLinTerm(new LinearTerm(vTick, 1)); // +tau_upsilon
					LinearRel c2 = new LinearRel(c1); // tau_upsilon+1-f<=0
					LinearRel c3 = LinearRel.getGEQtoLEQ(c2); // -tau_upsilon-1+f<=0
					tau_upsilonPLUS1EQf = c2;
					tau_upsilonPLUS1EQf.addAll(c3);
				}
			}
			
			// tau_upsilon<g // true if g=inf
			LinearRel tau_upsilonLESSg;
			{
				tau_upsilonLESSg = new LinearRel();
				if (upper!=null) {
					// tau_upsilon - g + 1 <= 0
					LinearConstr c1 = new LinearConstr(upper);
					c1.transformBetweenGEQandLEQ(); // -g 
					c1.addLinTerm(new LinearTerm(null, 1)); // +1
					c1.addLinTerm(new LinearTerm(vTick,1)); // tau_upsilon
					tau_upsilonLESSg.addConstraint(c1);
				}
			}

			// g<0 ~ g+1<=0 // false if g=inf
			LinearRel gLESS0;
			{
				if (upper==null) {
					gLESS0 = createFalse();
				} else {
					LinearConstr c1 = new LinearConstr(upper);
					c1.addLinTerm(new LinearTerm(null, 1));
					gLESS0 = new LinearRel(c1);
				}
			}
			
			// f>g ~ g-f+1<=0 // false if f=-inf or g=inf
			LinearRel fGREATERg;
			{
				if (lower==null || upper==null) {
					fGREATERg = createFalse();
				} else {
					LinearConstr c1 = new LinearConstr(lower);
					c1.transformBetweenGEQandLEQ(); // -f
					c1.addLinTerm(new LinearTerm(null, 1)); // +1
					c1.addLinTerms(upper.values());
					fGREATERg = new LinearRel(c1);
				}
			}

			// f<=tau_upsilon // true if f=-inf
			LinearRel fLEQtau_upsilon;
			{
				fLEQtau_upsilon = new LinearRel();
				if (lower!=null) {
					LinearConstr c1 = new LinearConstr(lower);
					c1.addLinTerm(new LinearTerm(vTick,-1));
					fLEQtau_upsilon.addConstraint(c1);
				}
			}
			
			// tau_upsilon<=g // true if g=inf
			LinearRel tau_upsilonLEQg;
			{
				tau_upsilonLEQg = new LinearRel();
				if (upper!=null) {
					LinearConstr c1 = new LinearConstr(upper);
					c1.transformBetweenGEQandLEQ();
					c1.addLinTerm(new LinearTerm(vTick,1));
					tau_upsilonLEQg.addConstraint(c1);
				}
			}

			// tau_upsilon+1<f   ~   tau_upsilon+2-f<=0  // false if f=-inf
			LinearRel tau_upsilonPLUS1LESSf;
			{
				if (lower==null) {
					tau_upsilonPLUS1LESSf = createFalse();
				} else {
					LinearConstr c1 = new LinearConstr(lower);
					c1.transformBetweenGEQandLEQ(); // -f
					c1.addLinTerm(new LinearTerm(null, 2)); // +2
					c1.addLinTerm(new LinearTerm(vTick, 1)); // +tau_upsilon
					tau_upsilonPLUS1LESSf = new LinearRel(c1);
				}
			}
			
			// tau_upsilon=g // false if g=inf
			LinearRel tau_upsilonEQg;
			{
				if (upper==null) {
					tau_upsilonEQg = createFalse();
				} else {
					tau_upsilonEQg = new LinearRel(tau_upsilonLEQg); // tau_upsilon <= g
					LinearRel c1 = LinearRel.getGEQtoLEQ(tau_upsilonLEQg); // tau_upsilon >= g
					tau_upsilonEQg.addAll(c1);
				}
			}
			
			if (lower!=null) {
				
				// q1 tick-loop
				LinearRel lc_q1_loop_tick = new LinearRel(tickAll);
				lc_q1_loop_tick.addConstraints(tau_upsilonPLUS1LESSf);
				lc_q1_loop_tick.addConstraints(lessWNminus1);
				ca.addTransitionOnlySat(new CATransition(q1,q1,new CompositeRel(lc_q1_loop_tick),ca));
				
				// qi-q1
				LinearRel lc_qiq1 = new LinearRel(tickAll);
				lc_qiq1.addConstraints(tau_upsilonEQ0);
				lc_qiq1.addConstraints(tau_upsilonPLUS1LESSf);
				lc_qiq1.addConstraints(fLEQg);
				lc_qiq1.addConstraints(lessWNminus1);
				ca.addTransitionOnlySat(new CATransition(qi,q1,new CompositeRel(lc_qiq1),ca));
				
				// q1-q2
				LinearRel lc_q1q2 = new LinearRel(tickAll);
				lc_q1q2.addConstraints(tau_upsilonPLUS1EQf);
				lc_q1q2.addConstraints(lessWNminus1);
				ca.addTransitionOnlySat(new CATransition(q1,q2,new CompositeRel(lc_q1q2),ca));
			}
			
			// q2 tick-loop
			LinearRel lc_q2_loop_tick = new LinearRel(tickAll);
			lc_q2_loop_tick.addConstraints(tau_upsilonLESSg);
			lc_q2_loop_tick.addConstraints(upsilon);
			lc_q2_loop_tick.addConstraints(lessWNminus1);
			ca.addTransitionOnlySat(new CATransition(q2,q2,new CompositeRel(lc_q2_loop_tick),ca));
			
			// qi-q2 (with overline{upsilon})
			LinearRel lc_qiq2_1 = new LinearRel(tickAll);
			lc_qiq2_1.addConstraints(tau_upsilonEQ0);
			lc_qiq2_1.addConstraints(fLEQtau_upsilon);
			lc_qiq2_1.addConstraints(tau_upsilonLESSg);
			lc_qiq2_1.addConstraints(upsilon);
			lc_qiq2_1.addConstraints(lessWNminus1);
			ca.addTransitionOnlySat(new CATransition(qi,q2,new CompositeRel(lc_qiq2_1),ca));
			
			// qi-q2 (without overline{upsilon})
			LinearRel lc_qiq2_2 = new LinearRel(tickAll);
			{
				// tau_upsilon=0
				lc_qiq2_2.addConstraints(tau_upsilonEQ0);
				// tau_upsilon+1=f
				lc_qiq2_2.addConstraints(tau_upsilonPLUS1EQf);
				// f<=g
				lc_qiq2_2.addConstraints(fLEQg);
				// tau_upsilon<wN-1  ~  tau_upsilon-wN+2<=0
				lc_qiq2_2.addConstraints(lessWNminus1);
			}
			ca.addTransitionOnlySat(new CATransition(qi,q2,new CompositeRel(lc_qiq2_2),ca));
			
			{
				// q3 tick-loop
				LinearRel lc_q3_loop_tick = new LinearRel(tickAll);
				lc_q3_loop_tick.addConstraints(lessWNminus1);
				ca.addTransitionOnlySat(new CATransition(q3,q3,new CompositeRel(lc_q3_loop_tick),ca));
				
				// q3-qf
				LinearRel lc_q3qf = new LinearRel(tickAll);
				lc_q3qf.addConstraints(eqWNminus1);
				ca.addTransitionOnlySat(new CATransition(q3,qe,new CompositeRel(lc_q3qf),ca));
			}
			
			
			if (!isNegated) {
				
				// q2-qf
				LinearRel lc_q2qf = new LinearRel(tickAll);
				if (h==0) lc_q2qf.addConstraints(upsilon);
				lc_q2qf.addConstraints(eqWNminus1);
				ca.addTransitionOnlySat(new CATransition(q2,qe,new CompositeRel(lc_q2qf),ca));
				
				{
					// q1-qf
					LinearRel lc_q1qf = new LinearRel(tickAll);
					lc_q1qf.addConstraints(eqWNminus1);
					ca.addTransitionOnlySat(new CATransition(q1,qe,new CompositeRel(lc_q1qf),ca));
				}
				
				// qi-qf-base
				LinearRel lc_qiqf_base = new LinearRel(tickAll);
				lc_qiqf_base.addConstraints(eqWNminus1);
				lc_qiqf_base.addConstraints(tau_upsilonEQ0);
				
				if (h==0) {
					
					// qi-qf-1
					LinearRel lc_qiqf_1 = new LinearRel(lc_qiqf_base);
					lc_qiqf_1.addConstraints(fLEQg);
					{
						// 0<f ~ -f+1 <= 0 // false of f=-inf
						LinearConstr c1;
						if (lower==null) {
							c1 = LinearConstr.createConst(1); // false
						} else {
							c1 = new LinearConstr(lower);
							c1.transformBetweenGEQandLEQ();
							c1.addLinTerm(new LinearTerm(null, 1));
						}
						lc_qiqf_1.add(c1);
					}
					ca.addTransitionOnlySat(new CATransition(qi,qe,new CompositeRel(lc_qiqf_1),ca));
					
					// qi-qf-2
					LinearRel lc_qiqf_2 = new LinearRel(lc_qiqf_base);
					lc_qiqf_2.addConstraints(fLEQg);
					lc_qiqf_2.addConstraints(gLESS0);
					ca.addTransitionOnlySat(new CATransition(qi,qe,new CompositeRel(lc_qiqf_2),ca));
		
					// qi-qf-3
					LinearRel lc_qiqf_3 = new LinearRel(lc_qiqf_base);
					//lc_qiqf_3.addConstraint(fLEQg);
					lc_qiqf_3.addConstraints(fGREATERg);
					ca.addTransitionOnlySat(new CATransition(qi,qe,new CompositeRel(lc_qiqf_3),ca));
					
					// qi-qf-4
					LinearRel lc_qiqf_4 = new LinearRel(lc_qiqf_base);
					lc_qiqf_4.addConstraints(fLEQtau_upsilon);
					lc_qiqf_4.addConstraints(tau_upsilonLEQg);
					lc_qiqf_4.addConstraints(upsilon);
					ca.addTransitionOnlySat(new CATransition(qi,qe,new CompositeRel(lc_qiqf_4),ca));
					
				} else {
					// just one transition with lc_qiqf_base
					ca.addTransitionOnlySat(new CATransition(qi,qe,new CompositeRel(lc_qiqf_base),ca));
				}
				
				{
					// qi-q3-base
					LinearRel lc_qiq3_base = new LinearRel(tickAll);
					lc_qiq3_base.addConstraints(tau_upsilonEQ0);
					lc_qiq3_base.addConstraints(lessWNminus1);
					
					// qi-q3-1
					LinearRel lc_qiq3_1 = new LinearRel(lc_qiq3_base);
					lc_qiq3_1.addConstraints(upsilon);
					lc_qiq3_1.addConstraints(fLEQtau_upsilon);
					lc_qiq3_1.addConstraints(tau_upsilonEQg);
					ca.addTransitionOnlySat(new CATransition(qi,q3,new CompositeRel(lc_qiq3_1),ca));
					
					// qi-q3-2
					LinearRel lc_qiq3_2 = new LinearRel(lc_qiq3_base);
					lc_qiq3_2.addConstraints(fGREATERg);
					ca.addTransitionOnlySat(new CATransition(qi,q3,new CompositeRel(lc_qiq3_2),ca));
	
					// qi-q3-3
					LinearRel lc_qiq3_3 = new LinearRel(lc_qiq3_base);
					lc_qiq3_3.addConstraints(fLEQg);
					lc_qiq3_3.addConstraints(gLESS0);
					ca.addTransitionOnlySat(new CATransition(qi,q3,new CompositeRel(lc_qiq3_3),ca));
					
					// q2-q3
					LinearRel lc_q2q3 = new LinearRel(tickAll);
					lc_q2q3.addConstraints(lessWNminus1);
					lc_q2q3.addConstraints(upsilon);
					lc_q2q3.addConstraints(tau_upsilonEQg);
					ca.addTransitionOnlySat(new CATransition(q2,q3,new CompositeRel(lc_q2q3),ca));
				}
				
			} else {
				
				//LinearRel negUpsilon_1 = LinearRel.getGEQtoLEQ(upsilon);
				Collection<LinearConstr> negUpsilon = this.toCAconstrNeg(aST.vp);
				
				// q2-q3
				LinearRel lc_q2q3 = new LinearRel(tickAll);
				lc_q2q3.addConstraints(lessWNminus1);
				//lc_q2q3.addConstraints(negUpsilon);
				lc_q2q3.addConstraints(tau_upsilonLEQg);
				createTransitionsForAll(negUpsilon,q2,q3,ca,lc_q2q3);
				//ca.addTransitionOnlySat(new CA_Transition(q2,q3,ca,lc_q2q3));

				// qi-q3
				LinearRel lc_qiq3 = new LinearRel(tickAll);
				lc_qiq3.addConstraints(lessWNminus1);
				lc_qiq3.addConstraints(tau_upsilonEQ0);
				//lc_qiq3.addConstraints(negUpsilon);
				lc_qiq3.addConstraints(tau_upsilonLEQg);
				lc_qiq3.addConstraints(fLEQtau_upsilon);
				createTransitionsForAll(negUpsilon,qi,q3,ca,lc_qiq3);
				//ca.addTransitionOnlySat(new CA_Transition(qi,q3,ca,lc_qiq3));
				
				if (h==0) {
					// qi-qf
					LinearRel lc_qiqf = new LinearRel(tickAll);
					lc_qiqf.addConstraints(eqWNminus1);
					lc_qiqf.addConstraints(tau_upsilonEQ0);
					//lc_qiqf.addConstraints(negUpsilon);
					lc_qiqf.addConstraints(tau_upsilonLEQg);
					lc_qiqf.addConstraints(fLEQtau_upsilon);
					createTransitionsForAll(negUpsilon,qi,qe,ca,lc_qiqf);
					//ca.addTransitionOnlySat(new CA_Transition(qi,qf,ca,lc_qiqf));
					
					// q2-qf
					LinearRel lc_q2qf = new LinearRel(tickAll);
					lc_q2qf.addConstraints(eqWNminus1);
					//lc_q2qf.addConstraints(negUpsilon);
					lc_q2qf.addConstraints(tau_upsilonLEQg);
					createTransitionsForAll(negUpsilon,q2,qe,ca,lc_q2qf);
					//ca.addTransitionOnlySat(new CA_Transition(q2,qf,ca,lc_q2qf));
				}			
			}
			
			ca.nontermUnreachRemoval();
			
			return ca;
		}

	}
	public static class NodePresburger extends NodeLeaf {
		private DisjRel rel;
		public NodePresburger(DisjRel aRel) { rel = aRel; }
		public DisjRel rel() { return rel; }
		public static NodePresburger giveTrue() {
			return new NodePresburger(DisjRel.giveTrue());
		}
		public static NodePresburger giveFalse() {
			return new NodePresburger(DisjRel.giveFalse());
		}
		public Node nnf(boolean b) {
			//if (!(b == isNegated)) {
			if (b) {
				this.rel = this.rel.not();
			}
			//isNegated = false;
			return this;
		}
		public Node and(Node that) {
			if (that instanceof NodePresburger) {
				return new NodePresburger(this.rel().and(((NodePresburger) that).rel()));
			} else {
				return super.and(that);
			}
		}
		public Node or(Node that) {
			if (that instanceof NodePresburger) {
				return new NodePresburger(this.rel().or(((NodePresburger) that).rel()));
			} else {
				return super.or(that);
			}
		}
		public NodePresburger not() {
			return new NodePresburger(this.rel.not());
		}
		
		public StringBuffer toSB() {
			return new StringBuffer("("+rel.toString()+")");
		}
		public Node normalize(SymbolTable aST) {
			return this;
		}
	}
	
	////////////// InternalNode ///////////////////
	public static abstract class NodeInternal extends Node {
	}
	public static abstract class NodeBin extends NodeInternal {
		protected Node op1,op2;
		public NodeBin(Node aOp1, Node aOp2) {
			op1 = aOp1; op2 = aOp2;
		}
	}
	public static class NodeAnd extends NodeBin {
		public NodeAnd(Node aOp1, Node aOp2) { super(aOp1,aOp2); }
		public Node normalize(SymbolTable aST) {
			return new NodeAnd(op1.normalize(aST),op2.normalize(aST));
		}
		public Node nnf(boolean b) {
			Node n1 = op1.nnf(b);
			Node n2 = op2.nnf(b);
			return (b)? new NodeOr(n1,n2) : new NodeAnd(n1,n2);
		}
		protected DnfForm dnf_base() {
			return op1.dnf_base().and(op2.dnf_base());
		}
		public StringBuffer toSB() {
			return new StringBuffer("(").append(op1.toSB()).append(" && ").append(op2.toSB()).append(")");
		}
		public List<Node> collectConjuncts() {
			List<Node> ret = new LinkedList<Node>();
			ret.addAll(op1.collectConjuncts());
			ret.addAll(op2.collectConjuncts());
			return ret;
		}
	}
	public static class NodeOr extends NodeBin {
		public NodeOr(Node aOp1, Node aOp2) { super(aOp1,aOp2); }
		public Node normalize(SymbolTable aST) {
			return new NodeOr(op1.normalize(aST),op2.normalize(aST));
		}
		public Node nnf(boolean b) {
			Node n1 = op1.nnf(b);
			Node n2 = op2.nnf(b);
			return (!b)? new NodeOr(n1,n2) : new NodeAnd(n1,n2);
		}
		protected DnfForm dnf_base() {
			return op1.dnf_base().or(op2.dnf_base());
		}
		public StringBuffer toSB() {
			return new StringBuffer("(").append(op1.toSB()).append(" || ").append(op2.toSB()).append(")");
		}
		public List<Node> collectConjuncts() {
			assert(false);return null;
		}
	}
	public static class NodeNot extends NodeInternal {
		protected Node op;
		
		public NodeNot(Node aOp) { op = aOp; }
		
		public Node normalize(SymbolTable aST) {
			return new NodeNot(op.normalize(aST));
		}
		public Node nnf(boolean b) {
			return op.nnf(!b);
		}
		protected Node.DnfForm dnf_base() {
			return op.dnf_base().negate();
			//throw new RuntimeException();
		}
		public StringBuffer toSB() {
			return new StringBuffer("!(").append(op.toSB()).append(")");
		}
		public List<Node> collectConjuncts() {
			assert(false);return null;
		}
	}
	
	
	public static class Literal {
		private boolean isNegated;
		private NodeLeaf leaf;
		
		public boolean isNegated() { return isNegated; }
		public NodeLeaf leaf() { return leaf; }
		
		public Literal(boolean aIsNegated, NodeLeaf aLeaf) {
			isNegated = aIsNegated;
			leaf = aLeaf;
		}
		public Literal(NodeLeaf aLeaf) {
			this(false, aLeaf);
		}
		public Literal not() {
			return new Literal(!isNegated,leaf);
		}
		public boolean isForall() {
			return leaf instanceof NodeForall;
		}
		public boolean isPresb() {
			return leaf instanceof NodePresburger;
		}
		public StringBuffer toSB() {
			StringBuffer aux = leaf.toSB();
			if (isNegated) {
				aux = new StringBuffer("(!").append(aux).append(")");
			}
			return aux;
		}
	}
	public static enum ClauseType {
		NONE, CONJ, DISJ;
		public ClauseType negate() {
			switch (this) {
			case NONE: return NONE;
			case CONJ: return DISJ;
			case DISJ: return CONJ;
			default: return null;
			}
		}
		public boolean isConjunctive() { return this != DISJ; }
		public boolean isDisjunctive() { return this != CONJ; }
		public String toString() {
			switch (this) {
			case NONE: return "NONE";
			case CONJ: return "&&";
			case DISJ: return "||";
			default: return null;
			}
		}
	}
	public static class Clause {
		protected ClauseType type;
		private List<Literal> literals = new LinkedList<Literal>();
		
		public List<Literal> literals() { return literals; }
		private Clause(ClauseType aT) {
			type = aT;
		}
		public Clause(ClauseType aT, Literal aLit) {
			type = aT;
			add(aLit);
		}
		public Clause(ClauseType aT, List<Literal> aLits) {
			this(aT);
			addAll(aLits);
		}
		public Clause(Clause that) {
			this.type = that.type;
			addAll(that);
		}
		
		private void add(Literal aLit) {
			literals.add(aLit);
		}
		private void addAll(List<Literal> aLits) {
			for (Literal lit : aLits) {
				add(lit);
			}
		}
		private void addAll(Clause that) {
			addAll(that.literals);
		}
		
		public int size() {
			return literals.size();
		}
		public boolean sameType(Clause that) {
			return this.type == that.type;
		}
		public boolean isConjunctive() {
			return type.isConjunctive();
		}
		public boolean isDisjunctive() {
			return type.isDisjunctive();
		}
		
		public Clause not() {
			Clause ret = new Clause(type.negate());
			for (Literal lit : literals) {
				ret.add(lit.not());
			}
			return ret;
		}
		public Clause and(Clause that) {
			assert(this.isConjunctive() && that.isConjunctive());
			Clause ret = new Clause(this);
			ret.addAll(that);
			return ret;
		}
		
		public StringBuffer toSB() {
			StringBuffer aux = new StringBuffer("(");
			Iterator<Literal> i = literals.iterator();
			while (i.hasNext()) {
				aux.append(i.next().toSB());
				if (i.hasNext()) {
					aux.append(" "+type+" ");
				}
			}
			aux.append(")");
			return aux;
		}
		public String toString() {
			return toSB().toString();
		}
	}
	public static class DnfForm {
		// DNF with merging of forall properties
		private List<Clause> clauses = new LinkedList<Clause>();
		
		public List<Clause> clauses() { return clauses; }
		
		private void add(Clause aC) {
			clauses.add(aC);
		}
		private void add(DnfForm that) {
			clauses.addAll(that.clauses);
		}
		
		private boolean dim1simple() {
			return clauses.size() == 1;
		}
		private boolean dim2simple() {
			for (Clause c : clauses) {
				if (c.size() > 1)
					return false;
			}
			return true;
		}
		private boolean simple() {
			return dim1simple() || dim2simple();
		}
		private static boolean andGivesSimple(DnfForm d1, DnfForm d2) {
			return d1.dim1simple() && d2.dim1simple();
		}
		private static boolean orGivesSimple(DnfForm d1, DnfForm d2) {
			return d1.dim2simple() && d2.dim2simple();
		}
		
		public StringBuffer toSB() {
			StringBuffer aux = new StringBuffer("(");
			Iterator<Clause> i = clauses.iterator();
			while (i.hasNext()) {
				aux.append(i.next().toSB());
				if (i.hasNext()) {
					aux.append(" || ");
				}
			}
			aux.append(")");
			return aux;
		}
		public String toString() {
			return toSB().toString();
		}
		
		private DnfForm() {
			
		}
		public static DnfForm fromAtom(NodeLeaf aLeaf) {
			DnfForm ret = new DnfForm();
			ret.add(new Clause(ClauseType.CONJ,new Literal(aLeaf)));
			return ret;
		}
		public DnfForm negate() {
			assert(clauses.size() == 1 && clauses.get(0).size() == 1);
			DnfForm ret = new DnfForm();
			ret.add(clauses.get(0).not());
			return ret;
		}
		
		private static boolean samePartition(Literal lit1, Literal lit2) {
			assert(!lit1.isNegated && !lit2.isNegated);
//			if (lit1.isNegated || lit2.isNegated) { // negated properties are never merged
//				return false;
//			}
			//if (n1.isNegated != n2.isNegated) {
			//	return false;
			//}
			NodeForall n1 = (NodeForall)lit1.leaf;
			NodeForall n2 = (NodeForall)lit2.leaf;
			if (	((n1.lower == null) != (n2.lower == null)) || 
					((n1.upper == null) != (n2.upper == null)) ) {
				return false;
			}
			if (n1.lower == null && n1.upper == null) {
				return true;
			}
			if (n1.lower == null) {
				return n1.upper.equals(n2.upper);
			} 
			if (n1.upper == null) {
				return n1.lower.equals(n2.lower);
			}
			return n1.lower.equals(n2.lower) && n1.upper.equals(n2.upper);
		}
		// group the properties with same guards together
		// assumption: all literals are positive
		private static List<List<NodeForall>> lit2forall(List<List<Literal>> partitions) {
			List<List<NodeForall>> ret = new LinkedList<List<NodeForall>>();
			for (List<Literal> partition : partitions) {
				List<NodeForall> aux = new LinkedList<NodeForall>();
				ret.add(aux);
				for (Literal lit : partition) {
					aux.add((NodeForall)lit.leaf);
				}
			}
			return ret;
		}
		private static List<List<NodeForall>> partition(Clause clause_forall) {
			List<List<Literal>> partitions = new LinkedList<List<Literal>>();
			Iterator<Literal> i = clause_forall.literals.iterator();
			
			{
				List<Literal> first = new LinkedList<Literal>();
				first.add(i.next());
				partitions.add(first);
			}
			
			l1: while (i.hasNext()) {
				Literal n = i.next();
				for (List<Literal> partition : partitions) {
					Literal repr = partition.get(0);
					if (samePartition(n,repr)) {
						partition.add(n);
						continue l1;
					}
				}
				{
					List<Literal> part_new = new LinkedList<Literal>();
					part_new.add(n);
					partitions.add(part_new);
				}
			}
			return lit2forall(partitions);
		}
		private static NodePresburger getPresburger(boolean negate, Clause conj_presb) {
			DisjRel aux = negate? DisjRel.giveFalse() : DisjRel.giveTrue();
			for (Literal lit : conj_presb.literals) {
				boolean lit_neg = negate != lit.isNegated;
				DisjRel lit_rel = ((NodePresburger)lit.leaf).rel;
				if (lit_neg) {
					lit_rel = lit_rel.not();
				}
				if (negate) {
					aux = aux.or(lit_rel);
				} else {
					aux = aux.and(lit_rel);
				}
			}
			return new NodePresburger(aux);
		}
		private DnfForm merge() {
			assert(simple());
			
			// transform the form a || b || c into not (not a && not b && not c)
			boolean negate = !this.dim1simple();
			Clause conj = null;
			if (!negate) {
				conj = clauses.get(0);
			} else {
				conj = new Clause(ClauseType.CONJ);
				for (Clause c : clauses) {
					conj.add(c.literals().get(0).not());
				}
			}
			
			// split forall and presb. literals
			Clause conj_forall_neg = new Clause(ClauseType.CONJ);
			Clause conj_forall_pos = new Clause(ClauseType.CONJ);
			Clause conj_presb = new Clause(ClauseType.CONJ);
			for (Literal lit : conj.literals) {
				if (lit.isForall()) {
					if (lit.isNegated) {
						conj_forall_neg.add(lit);
					} else {
						conj_forall_pos.add(lit);
					}
				} else {
					conj_presb.add(lit);
				}
			}
			
			List<Literal> list = new LinkedList<Literal>();
			
			// presb. relation
			NodePresburger n_presb = getPresburger(negate,conj_presb);
			list.add(new Literal(false,n_presb));
			
			// negative forall literals
			for (Literal lit : conj_forall_neg.literals) {
				NodeForallConjunctive aux = new NodeForallConjunctive((NodeForall)lit.leaf);
				list.add(new Literal(!negate,aux));
			}
			
			// partition positive forall literals
			if (conj_forall_pos.size() > 0) {
				List<List<NodeForall>> partitions_pos = partition(conj_forall_pos);
				for (List<NodeForall> partition_pos : partitions_pos) {
					NodeForallConjunctive aux = new NodeForallConjunctive(partition_pos);
					list.add(new Literal(negate,aux));
				}
			}
			
			// final step
			if (!negate) {
				DnfForm ret = new DnfForm();
				ret.add(new Clause(ClauseType.CONJ,list));
				return ret;
			} else {
				DnfForm ret = new DnfForm();
				for (Literal lit : list) {
					ret.add(new Clause(ClauseType.CONJ,lit));
				}
				return ret;
			}
			
		}
		private DnfForm and_base(DnfForm that) {
			DnfForm ret = new DnfForm();
			for (Clause c1 : this.clauses) {
				for (Clause c2 : that.clauses) {
					ret.add(c1.and(c2));
				}
			}
			return ret;
		}
		private DnfForm or_base(DnfForm that) {
			DnfForm ret = new DnfForm();
			ret.add(this);
			ret.add(that);
			return ret;
		}
		public DnfForm and(DnfForm that) {
			if (this.simple() && that.simple() && andGivesSimple(this,that)) {
				return this.and_base(that);
			} else { 
				return this.ensureMerged().and_base(that.ensureMerged());
			}
		}
		public DnfForm or(DnfForm that) {
			if (this.simple() && that.simple() && orGivesSimple(this,that)) {
				return this.or_base(that);
			} else {
				return this.ensureMerged().or_base(that.ensureMerged());
			}
		}
		protected DnfForm ensureMerged() {
			return this.simple()? this.merge() : this;
		}
	}

}
