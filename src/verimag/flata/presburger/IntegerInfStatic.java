package verimag.flata.presburger;


public class IntegerInfStatic implements FieldStaticInf {

	public IntegerInf zero() { return IntegerInf.zero(); }
	public IntegerInf one()  { return IntegerInf.one();  }
	public IntegerInf initVal()  { return IntegerInf.posInf(); }
	
	public IntegerInf posInf() { return IntegerInf.posInf(); }
	public IntegerInf negInf()  { return IntegerInf.negInf(); }
	
	public IntegerInf giveField(int c) { return new IntegerInf(c); }
	public DBC.BoundType giveType() { return DBC.BoundType.INT; }
	
	private IntegerInfStatic() {}
	
	private static IntegerInfStatic fs = new IntegerInfStatic();
	public static IntegerInfStatic fs() { return fs; }
}
