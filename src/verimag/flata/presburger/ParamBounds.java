package verimag.flata.presburger;

import java.util.*;

import verimag.flata.common.CR;



public class ParamBounds extends FieldInf {

	public static ParamBounds one() {
		return new ParamBounds(1);
	}
	public static ParamBounds zero() {
		return new ParamBounds(0);
	}
	public static ParamBounds giveField(int c) {
		return new ParamBounds(c);
	}
	public static ParamBounds posInf() {
		return new ParamBounds(FieldType.POS_INF);
	}
	public static ParamBounds negInf() {
		return new ParamBounds(FieldType.NEG_INF);
	}
	public static ParamBounds init() {
		return posInf();
	}
	
	public String toString() {
		if (type==FieldType.POS_INF)
			return Field.strPosInf;
		else if (type==FieldType.NEG_INF)
			return Field.strNegInf;
		else
			return col.toString();
	}
	
	private FieldType type;
	
	// keeps set of parametric bound, but infinite bound is NEVER added to a list !!
	private List<ParamBound> col = new LinkedList<ParamBound>();
	private ParamBound find(ParamBound elem) {
		for (ParamBound e : col)
			if (e.equals(elem))
				return e;
		return null;
	}
	
	public List<ParamBound> paramBounds() {
		return col;
	}
	
	public ParamBounds(ParamBounds other) {
		type = other.type;
		if (type.isFinite()) {
			for (ParamBound b : other.col)
				col.add(new ParamBound(b));
		}
	}
	
	public ParamBounds(int aVal) {
		this(aVal,0);
	}
	public ParamBounds(int aVal, int aParamCoef) {
		type = FieldType.FINITE;
		addBound(new ParamBound(FieldType.FINITE, aVal, aParamCoef));
	}
	private ParamBounds(FieldType aType) {
		type = aType;
	}
//	public ParamBounds(FieldType aType, int aVal, int aParamCoef) {
//		type = aType;
//		addBound(new ParamBound(aType, aVal, aParamCoef));
//	}
	
	
	public boolean minEquals(ParamBounds other) {
		return minEquals(other, new IntegerInf(FieldType.POS_INF));
	}
	
	public boolean minEquals(ParamBounds other, IntegerInf maxK) {
		
		if (type != other.type)
			return false;
		else if (type != FieldType.FINITE)
			return true;
		
		ParamBounds setOneElem;
		ParamBounds set;
		if (col.size() == 1) {
			set = other;
			setOneElem = this;
		} else if (other.col.size() == 1) {
			set = this;
			setOneElem = other;
		} else {
			throw new RuntimeException("minEquals not supported when cardinality of both operands is greater than one.");
		}
		
		ParamBound elem = setOneElem.col.iterator().next();
		
		ParamBound minCandidate = set.find(elem);
		if (minCandidate == null)
			return false;
		
		for (ParamBound e : set.col)
			if (!minCandidate.forallLessEqual(e, maxK))
				return false;
		
		return true;
	}

	public boolean equals(Object other) {
		if (!(other instanceof ParamBounds))
			return false;
		
		return this.minEquals((ParamBounds)other);
	}

	@Override
	public Field max(Field field) {
		throw new RuntimeException("Method not supported");
	}
	@Override
	public boolean absGreater(Field aF) {
		throw new RuntimeException("Method not supported");
	}
	@Override
	public Field divide(Field aF) {
		throw new RuntimeException("Method not supported");
	}
	@Override
	public boolean isFinite() {
		return type.isFinite();
	}
	@Override
	public Field minus(Field aF) {
		throw new RuntimeException("Method not supported");
	}
	@Override
	public Field times(Field aF) {
		throw new RuntimeException("Method not supported");
	}
	@Override
	public int toInt() {
		throw new RuntimeException("Method not supported");
	}
	
	public static int stat = 0;
	private void addFinite_optim(ParamBound other) {
		Iterator<ParamBound> iter = col.iterator();
		while(iter.hasNext()) {
			ParamBound b = iter.next();
			Field.Comp comp = b.compare(other);
			if (comp.isLeq()) {
				return;
			} else if (comp.isGeq()) {
				iter.remove();
				stat++;
			}
		}
		col.add(other);
		return;
	}
	public void addBound(Field f) {
		if (!(f instanceof ParamBound))
			throw new RuntimeException("internal error: imcompatible types for a matrix field");
		
		ParamBound other = (ParamBound) f;
		FieldType otherType = other.type();
		FieldType minType = FieldType.min(type,otherType);
		
		if (minType.isFinite()) {
			if (otherType.isPosInf())
				return;
			else if (type.isPosInf()) {
				type = minType;
				col.add(new ParamBound(other));
			} else {
				addFinite_optim(other);
			}
		} else {
			col.clear();
			type = minType;
		}
	}
	
	private void minimize() {
		
		if (col.size() <= 2)
			return;
		
		Map<Integer,ParamBound> m = new HashMap<Integer,ParamBound>();
		for (ParamBound pb : this.col) {
			if (m.containsKey(pb.paramCoef())) {
				throw new RuntimeException("internal error");
			}
			m.put(pb.paramCoef(), pb);
		}
		int size = m.size();
		ArrayList<Integer> l_aux = new ArrayList<Integer>(m.keySet());
		Collections.sort(l_aux);
		ArrayList<Integer> l = new ArrayList<Integer>(size);
		for (int i=size-1; i>=0; i--) {
			l.add(l_aux.get(i));
		}
		
		BitSet minterms = new BitSet(size);
		int p = size-1;
		minterms.set(p);
		while (true) {
			if (p == 0) {
				break;
			}
			int K[] = new int[p];
			ParamBound pb1 = m.get(l.get(p));
			int a1 = pb1.paramCoef();
			int b1 = pb1.intVal();
			for (int i=0; i<p; i++) {
				ParamBound pb2 = m.get(l.get(i));
				int a2 = pb2.paramCoef();
				int b2 = pb2.intVal();
				K[i] = CR.ceil(b2-b1, a1-a2);
			}
			int M = max(K);
			if (M <= 0) {
				break;
			}
			// candidate terms: those with K[i]=M
			// evaluate terms for M-1
			Set<Integer> val = new HashSet<Integer>();
			for (int i=0; i<p; i++) {
				if (K[i] == M) {
					ParamBound pb2 = m.get(l.get(i));
					val.add(pb2.paramCoef()*(M-1)+pb2.intVal());
				}
			}
			int N = Collections.min(val);
			// find term with smallest parameter coefficient that evaluates to N
			int debug = p;
			for (int i=0; i<p; i++) {
				if (K[i] == M) {
					ParamBound pb2 = m.get(l.get(i));
					if (pb2.paramCoef()*(M-1)+pb2.intVal() == N) {
						p = i;
						break;
					}
				}
			}
			if (p == debug) {
				throw new RuntimeException("internal error");
			}
			minterms.set(p);
		}
		
		int i = -1;
		while ((i = minterms.nextClearBit(i+1)) < size) {
			col.remove(m.get(l.get(i)));
		}
	}
	private int max(int a[]) {
		int m = a[0];
		for (int i=1; i<a.length; i++) {
			if (a[i]>m) {
				m = a[i];
			}
		}
		return m;
	}
	
//	private void finiteOptim() {
//		if (col.size() <= 2)
//			return;
//		// check if all functions ak+b have same solution
//		Iterator<ParamBound> i = col.iterator();
//		ParamBound ref = i.next();
//		int a1 = ref.intVal();
//		int b1 = ref.paramCoef();
//		ParamBound minA = ref;
//		ParamBound minB = ref;
//		double k = -1.0;
//		boolean first = true;
//		
//		while (i.hasNext()) {
//			ParamBound p = i.next();
//			int a2 = p.intVal();
//			int b2 = p.paramCoef();
//			// should never happen due to optimizations:
//			if (b1 == b2 || a1 == a2) {
//				throw new RuntimeException("internal error: param-bounds optimization");
//			}
//			if (!((a1>a2 && b1<b2) || (a1<a2 && b1>b2))) {
//				throw new RuntimeException("internal error: param-bounds optimization");
//			}
//			// invariant: (a1>a2 && b1<b2) || (a1<a1 && b1>b2)
//			double k2 = ((double)(a1-a2))/(b2-b1);
//			if (first) {
//				first = false;
//				k = k2;
//			} else {
//				if (k != k2)
//					return;
//			}
//			if (a2<minA.intVal()) 
//				minA = p;
//			if (b2<minB.paramCoef()) 
//				minB = p;
//			
//		}
//		// if they have same solution, optimize (keep just 2 minimal terms)
//		col.clear();
//		col.add(minA);
//		col.add(minB);
//	}
	public Field min(Field f) {
		if (!(f instanceof ParamBounds))
			throw new RuntimeException("internal error: imcompatible types for a matrix field");
		
		ParamBounds other = (ParamBounds) f;
		FieldType minType = FieldType.min(type,other.type);
		
		if (minType.isFinite()) {
			if (other.type.isPosInf())
				return new ParamBounds(this);
			else if (type.isPosInf()) {
				return new ParamBounds(other);
			} else {
				ParamBounds ret = new ParamBounds(this);
				for (ParamBound b : other.col)
					ret.addFinite_optim(b);
				//ret.finiteOptim();
				ret.minimize();
				return ret;
			}
		} else {
			return new ParamBounds(minType);
		}
	}
	public Field plus(Field f) {
		if (!(f instanceof ParamBounds))
			throw new RuntimeException("internal error: imcompatible types for a matrix field");
		
		ParamBounds other = (ParamBounds) f;
		FieldType maxType = FieldType.max(type,other.type);
		
		if (maxType.isFinite()) {
			if (other.type.isNegInf())
				return new ParamBounds(this);
			else if (type.isNegInf()) {
				return new ParamBounds(other);
			} else {
				ParamBounds ret = new ParamBounds(FieldType.FINITE);
				for (ParamBound b1 : this.col)
					for (ParamBound b2 : other.col) {
						if (Thread.interrupted())
							throw new RuntimeException(" -- interrupt --");
						ret.addFinite_optim((ParamBound)b1.plus(b2));
					}
				return ret;
			}
		} else {
			return new ParamBounds(maxType);
		}
	}	
	
	// each Field is expected to be ParamBound and is interpreted as x-x = 0 <= a + bk
	public static IntegerInf extractUpperBound(List<Field> diagonal) {
		IntegerInf min = new IntegerInf(FieldType.POS_INF);
		
		for (Field f : diagonal) {
			ParamBounds pbs = (ParamBounds)f;
			if (pbs.type.isNegInf())
				return IntegerInf.negInf();
			if (pbs.type.isPosInf())
				continue;
			
			for (ParamBound pb : pbs.col) {
				int a = pb.intVal();
				int b = pb.paramCoef();
				if (b < 0) {
					min = min.min(new IntegerInf(a / (-b)));
				} else if (b > 0) {
					if (-(a / b) > 0)
						throw new RuntimeException();  // k >= c, c>0 should never happen
				} else {
					if (b < 0)
						return IntegerInf.negInf();
				}
			}
		}
		return min;
	}
	
	public boolean lessEq(Field other) {
		throw new RuntimeException("Method not supported");
	}
	
	public boolean fillLinTerms(LinearConstr lc, Variable paramK) {
		if (col.size()!=1)
			throw new RuntimeException("internal error: matrix is supposed to contain one bound in each cell");
		
		return col.get(0).fillLinTerms(lc, paramK);
	}
	public boolean consistent(boolean isDiag) {
		throw new RuntimeException("internal error: method not supported");
	}
	
}
