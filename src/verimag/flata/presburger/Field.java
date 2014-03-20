package verimag.flata.presburger;




// matrix cell, field
public abstract class Field {
	
	public static enum Comp {
		LEQ, GEQ, UNKNOWN;
		public boolean isUnknown() { return this == UNKNOWN; }
		public boolean isLeq() { return this == LEQ; }
		public boolean isGeq() { return this == GEQ; }
	}
	
	public static String strPosInf = "+";
	public static String strNegInf = "-";
	
	// arithmetic
	public abstract Field plus(Field aF);
	public abstract Field minus(Field aF);
	public abstract Field times(Field aF);
	public abstract Field divide(Field aF);
	
	// min, max
	public abstract Field min(Field aF);
	public abstract Field max(Field aF);
	
	// relational
	public abstract boolean absGreater(Field aF);
	public abstract boolean equals(Object field);
	public abstract boolean lessEq(Field field);
	public boolean less(Field other, FieldStatic fs) {
		return this.plus(fs.one()).lessEq(other);
	}

	public abstract boolean isFinite();
	
	public abstract int toInt();
	
	// for purposes of DBM matrices whose cells may contain more than one bound
	public abstract void addBound(Field f);
	// returns true iff parameter has zero coefficient
	public abstract boolean fillLinTerms(LinearConstr lc, Variable paramK);
	public abstract boolean consistent(boolean isDiag);
	
}
