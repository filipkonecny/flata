package verimag.flata.presburger;

import java.util.List;



public class IntegerInf extends FieldInf implements Comparable<IntegerInf> {
	
	private FieldType type;
	// value can be arbitrary if the object is infinity
	private int val;
	
	public FieldType type() { return type; }
	public int val() { return val; }
	
	public boolean isFinite() {
		return type == FieldType.FINITE;
	}
	public int toInt() {
		if (!isFinite())
			throw new RuntimeException("Attempt to convert infinity to integer.");
		
		return val();
	}
	
	
	public String toString() {
		if (type==FieldType.POS_INF)
			return Field.strPosInf;
		else if (type==FieldType.NEG_INF)
			return Field.strNegInf;
		else
			return ""+val;
	}
	
	public IntegerInf(Integer aVal) {
		this(aVal.intValue());
	}
	public IntegerInf(int aVal) {
		this(FieldType.FINITE,aVal);
	}
	public IntegerInf(FieldType aType) {
		this(aType,0);
	}
	public IntegerInf(FieldType aType, Integer aVal) {
		this(aType, aVal.intValue());
	}
	public IntegerInf(FieldType aType, int aVal) {
		val = aVal;
		type = aType;
	}
	public IntegerInf(IntegerInf other) {
		val = other.val;
		type = other.type;
	}
	
	
	public IntegerInf inverse() {
		if (type == FieldType.FINITE)
			return new IntegerInf(-val);
		else if (type == FieldType.POS_INF)
			return new IntegerInf(FieldType.NEG_INF);
		else 
			return new IntegerInf(FieldType.POS_INF);
	}
	@Override
	public IntegerInf plus(Field aF) {
		
		IntegerInf other = (IntegerInf) aF;
		
		if (type==other.type && type==FieldType.FINITE) {
			return new IntegerInf(val+other.val);
		} else {
			return new IntegerInf(FieldType.plus(type, other.type));
		}
	}
	@Override
	public IntegerInf minus(Field aF) {
		IntegerInf other = (IntegerInf) aF;
		return this.plus(other.inverse());
	}
	@Override
	public IntegerInf times(Field aF) {
		IntegerInf other = (IntegerInf) aF;
		if (type==other.type && type==FieldType.FINITE) {
			return new IntegerInf(val*other.val);
		} else {
			return new IntegerInf(FieldType.plus(type, other.type));
		}
	}
	@Override
	public Field divide(Field aF) {
		throw new RuntimeException("internal error: method not supported");
	}
	
	
	public int compareTo(IntegerInf other) {
		if (type == other.type) {
			if (type != FieldType.FINITE)
				return 0;
			else 
				return val - other.val;
		} else {
			return type.compare(other.type);
		}
	}
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IntegerInf))
			return false;
		
		IntegerInf other = (IntegerInf)obj;
		return this.compareTo(other) == 0;
	}
	public int hashCode() {
		if (this.isFinite()) {
			return this.val;
		} else if (type.isNegInf()) {
			return -100;
		} else {
			return 100;
		}
	}
	@Override
	public boolean lessEq(Field aF) {
		if (!(aF instanceof IntegerInf))
			return false;
		
		IntegerInf other = (IntegerInf)aF;
		return this.compareTo(other) <= 0;
	}
	
	@Override
	public IntegerInf min(Field aF) {
		IntegerInf other = (IntegerInf) aF;
		IntegerInf min = (this.compareTo(other) <= 0)? this : other;
		return new IntegerInf(min);		
	}
	@Override
	public IntegerInf max(Field aF) {
		IntegerInf other = (IntegerInf) aF;
		IntegerInf max = (this.compareTo(other) >= 0)? this : other;
		return new IntegerInf(max);
	}
	
	private IntegerInf abs() {
		if (type != FieldType.FINITE)
			return new IntegerInf(FieldType.POS_INF);
		else
			return new IntegerInf(Math.abs(val));
	}
	@Override
	public boolean absGreater(Field aF) {
		IntegerInf abs = abs();
		IntegerInf absOther = ((IntegerInf)aF).abs();
		return abs.compareTo(absOther) > 0;
	}
	
	@Override
	public void addBound(Field f) {
		throw new RuntimeException("Method not supported");
	}
	@Override
	public boolean fillLinTerms(LinearConstr lc, Variable paramK) {
		lc.addLinTerm(new LinearTerm(null, -val));
		return true;
	}
	@Override
	public boolean consistent(boolean isDiag) {
		return !(type.isNegInf() || ( type.isFinite() && val <0 ));
	}
	
	
	public static boolean hasNegative(List<Field> diagonal) {
		return !hasNonNegative(diagonal);
	}
	public static boolean hasNonNegative(List<Field> diagonal) {
		for (Field f : diagonal) {
			if (((IntegerInf)f).val < 0)
				return false;
		}
		return true;
	}
	
	
	// static methods
	public static IntegerInf zero() { return new IntegerInf(0); }
    public static IntegerInf one() { return new IntegerInf(1); }
    public static IntegerInf giveField(int c) { return new IntegerInf(c); }
    public static IntegerInf posInf() { return new IntegerInf(FieldType.POS_INF); }
	public static IntegerInf negInf() { return new IntegerInf(FieldType.NEG_INF); }
	
	
}
