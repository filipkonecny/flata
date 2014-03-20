package verimag.flata.presburger;

import java.util.List;

public class ParamBound extends FieldInf {
	
	private int intVal;
	private int paramCoef;
	
	public int intVal() { return intVal; }
	public int paramCoef() { return paramCoef; }
	
	public void intVal(int aVal) { intVal = aVal; }
	public void paramCoef(int aCoef) { paramCoef = aCoef; }
	
	private FieldType type;
	public FieldType type() { return type; }
	
	// just for printing
	private static String paramK_name = "k";
	
	public String toString() {
		if (type==FieldType.POS_INF)
			return Field.strPosInf;
		else if (type==FieldType.NEG_INF)
			return Field.strNegInf;
		else {
			String v = (intVal == 0)? "" : ""+intVal;
			String s;
			if (paramCoef == 0)
				s = "";
			else
				s = ((paramCoef >= 0)? (intVal == 0? "" : "+") : "-")+Math.abs(paramCoef)+paramK_name;
			return (intVal == 0 && paramCoef == 0)? "0" : v+s;
		}
	}
	
	// constructors
	public ParamBound() { this(FieldType.FINITE,0,0); }
	private ParamBound(int aVal) { this(FieldType.FINITE,aVal,0); }
	private ParamBound(FieldType aType) { this(aType,0,0); }
	public ParamBound(int aVal, int aParamCoef) {
		this(FieldType.FINITE, aVal, aParamCoef);
	}
	public ParamBound(FieldType aType, int aVal, int aParamCoef) {
		type = aType;
		intVal = aVal;
		paramCoef = aParamCoef;
	}
	public ParamBound(ParamBound other) {
		type = other.type;
		intVal = other.intVal;
		paramCoef = other.paramCoef;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ParamBound))
			return false;
		
		ParamBound other = (ParamBound)o;
		if (type != other.type)
			return false;
		else if (type == FieldType.FINITE)
			return intVal == other.intVal && paramCoef == other.paramCoef;
		else
			return true;
	}

	
	// this: a1*k+b1 ; other: a2*k+b2
	// method checks: \forall 0 <= k . a1*k+b1 <= a2*k+b2   (if maxK < 0)
	// method checks: \forall 0 <= k <= maxK . a1*k+b1 <= a2*k+b2   (if maxK < 0)
	public boolean forallLessEqual(ParamBound other, IntegerInf maxK) {
		if (!maxK.isFinite()) {
			return (this.paramCoef <= other.paramCoef) && (this.intVal <= other.intVal);
		} else {
			int maxKi = maxK.toInt();
			return (this.intVal <= other.intVal) && 
				(this.intVal+maxKi*this.paramCoef <= other.intVal+maxKi*other.paramCoef);
		}
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
	
	public Field.Comp compare(ParamBound other) {
		ParamBound min = finiteMin(other);
		if (min == null)
			return Field.Comp.UNKNOWN;
		else if (min.equals(this))
			return Field.Comp.LEQ;
		else
			return Field.Comp.GEQ;
	}
	
	private ParamBound finiteMin(ParamBound other) {
		
		if (this.intVal <= other.intVal && this.paramCoef <= other.paramCoef)
			return new ParamBound(this);
		
		if (this.intVal >= other.intVal && this.paramCoef >= other.paramCoef)
			return new ParamBound(other);
		
		return null;
	}
	
	public Field min(Field f2) {
		ParamBound other = (ParamBound)f2;
		if (type == FieldType.NEG_INF || other.type == FieldType.NEG_INF)
			throw new RuntimeException("Internal error: negative infinity present");
		
		if (type.isFinite() && other.type.isFinite()) {
			
			ParamBound min = finiteMin(other);
			if (min == null)
				throw new RuntimeException("Internal error: min("+this+","+other+") does not exist.");
			else
				return min;
			
		} else if (!type.isFinite() && !other.type.isFinite()) {
			return new ParamBound(type); 
		} else if (type.isFinite()) {
			return new ParamBound(this);
		} else {
			return new ParamBound(other);
		}
	}
	@Override
	public Field minus(Field aF) {
		throw new RuntimeException("Method not supported");
	}
	@Override
	public Field plus(Field aF) {
		ParamBound other = (ParamBound)aF;
		if (type == FieldType.NEG_INF || other.type == FieldType.NEG_INF)
			throw new RuntimeException("Internal error: negative infinity present");
		if (type == FieldType.POS_INF || other.type == FieldType.POS_INF)
			return new ParamBound(FieldType.POS_INF);
		
		// it's OK to add two bounds both of which have parameters
		
		//if (coef != 0 && other.coef != 0)
		//	throw new RuntimeException("Internal error: cannot make addition of 2 parameters");
		return new ParamBound(type, intVal + other.intVal, paramCoef + other.paramCoef);
	}
	@Override
	public Field times(Field aF) {
		throw new RuntimeException("Method not supported");
	}
	@Override
	public int toInt() {
		throw new RuntimeException("Method not supported");
	}
	@Override
	public void addBound(Field f) {
		throw new RuntimeException("Method not supported");
	}
	
	public boolean lessEq(Field other) {
		throw new RuntimeException("Method not supported");
	}
	
	public boolean fillLinTerms(LinearConstr lc, Variable paramK) {
		lc.addLinTerm(new LinearTerm(null, -intVal));
		lc.addLinTerm(new LinearTerm(paramK, -paramCoef));
		return paramCoef == 0;
	}
	// each Field is expected to be ParamBound and is interpreted as x-x = 0 <= a + bk
	public static IntegerInf extractUpperBound(List<Field> diagonal) {
		IntegerInf min = new IntegerInf(FieldType.POS_INF);
		for (Field f : diagonal) {
			ParamBound pb = (ParamBound)f;
			int a = pb.intVal;
			int b = pb.paramCoef;
			if (b < 0) {
				min = min.min(new IntegerInf(a / (-b)));
			} else {
				if ((-a) / b > 0)
					throw new RuntimeException();
			}
		}
		return min;
	}
	
	public boolean consistent(boolean isDiag) {
		if (type == FieldType.POS_INF)
			return true;
		else if (type == FieldType.NEG_INF)
			return false;
		else {
			if (isDiag) {
				return intVal >= 0;
			} else {
				return true;
			}
				
		}
	}
	
	// static methods
	public static ParamBound zero() { return new ParamBound(0); }
    public static ParamBound one() { return new ParamBound(1); }
    public static ParamBound giveField(int c) { return new ParamBound(c); }
    public static ParamBound posInf() { return new ParamBound(FieldType.POS_INF); }
    public static ParamBound negInf() { return new ParamBound(FieldType.NEG_INF); }
    public static ParamBound initValue() { return posInf(); }

}
