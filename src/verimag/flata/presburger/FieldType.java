/**
 * 
 */
package verimag.flata.presburger;

public enum FieldType {
	FINITE, POS_INF, NEG_INF;
	
	public static FieldType plus(FieldType t1, FieldType t2) {
		if (t1 == FINITE && t2 == FINITE)
			return FINITE;
		else {
			if (t1 != FINITE && t2 != FINITE) {
				if (t1 == t2) {
					return t1;
				} else {
					throw new RuntimeException("plus is not defined for operands: "+t1+","+t2);
					// for shortest paths, NEG_INF
				}
			} else if (t1 != FINITE)
				return t1;
			else 
				return t2;
		}
	}
	
	public boolean isFinite() {	return this == FINITE; }
	public boolean isNegInf() {	return this == NEG_INF; }
	public boolean isPosInf() {	return this == POS_INF; }
	
	public static FieldType min(FieldType t1, FieldType t2) {
		if (t1 == NEG_INF || t2 == NEG_INF)
			return NEG_INF;
		if (t1 == POS_INF && t2 == POS_INF)
			return POS_INF;
		else
			return FINITE;
	}
	public static FieldType max(FieldType t1, FieldType t2) {
		if (t1 == NEG_INF && t2 == NEG_INF)
			return NEG_INF;
		if (t1 == POS_INF || t2 == POS_INF)
			return POS_INF;
		else
			return FINITE;
	}
	public int compare(FieldType other) {
		if (this == other)
			return 0;
		else {
			if ((this == NEG_INF) || (this == FINITE && other == POS_INF))
				return -1;
			else 
				return 1;
		}
	}

	public boolean lessEq(FieldType other) {
		return this.compare(other) <= 0;
	}
}