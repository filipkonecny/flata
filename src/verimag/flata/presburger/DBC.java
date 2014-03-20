package verimag.flata.presburger;



// Difference bound constraint
public class DBC {
	
	public static enum BoundType {
		INT, INT_ONE_PARAM;//, INT_MORE_PARAM;
		
		public static Field giveInt(FieldType aType, int aWeight) {
			return new IntegerInf(aType, aWeight);
		}
		public static Field giveIntOneParam(Variable paramK, FieldType aType, int aWeight, int aParamCoef) {
			return new ParamBound(aType, aWeight, aParamCoef);
		}
	}
	
	// p - m <= label
	private Variable p;
	private Variable m;
	private Field label;
	
	public Variable plus() { return p; }
	public void plus(Variable aP) { p = aP; }
	
	public Variable minus() { return m; }
	public void minus(Variable aM) { m = aM; }

	public void label(Field aLabel) { label = aLabel; }
	public Field label() { return label; }
	
	public DBC() {
	}
	public DBC(Variable aP, Variable aM, Field aLabel) {
		p = aP;
		m = aM;
		label = aLabel;
	}
	
	private static String rop = "<=";
	public String toString() {
		
		if (p == null)
			return "-"+m+rop+label;
		else if (m == null)
			return p+rop+label;
		else
			return p+"-"+m+rop+label;
	}
}
