package verimag.flata.presburger;


public class ParamBoundStatic implements FieldStaticInf, FieldStatic.ParametricFS {

	public Field giveField(int c) {	return ParamBound.giveField(c); }

	public Field initVal() { return ParamBound.initValue(); }

	public Field one() { return ParamBound.one(); }

	public Field zero() { return ParamBound.zero(); }
	
	public FieldInf posInf() { return ParamBound.posInf(); }
	public FieldInf negInf() { return ParamBound.negInf(); }
	
	public DBC.BoundType giveType() { return DBC.BoundType.INT_ONE_PARAM; }

	private ParamBoundStatic() {}
	
	private static ParamBoundStatic fs = new ParamBoundStatic();
	public static ParamBoundStatic fs() { return fs; }
	
}
