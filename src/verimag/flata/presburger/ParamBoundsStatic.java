package verimag.flata.presburger;


public class ParamBoundsStatic implements FieldStaticInf, FieldStatic.ParametricFS {
	
	public Field giveField(int c) { return ParamBounds.giveField(c); }	
	public Field initVal() { return ParamBounds.init(); }
	public Field one() { return ParamBounds.one(); }
	public Field zero() { return ParamBounds.zero(); }
	
	public FieldInf negInf() { return ParamBounds.negInf(); }
	public FieldInf posInf() { return ParamBounds.posInf(); }
	
	public DBC.BoundType giveType() { return DBC.BoundType.INT_ONE_PARAM; }
	
	private ParamBoundsStatic() {}
	
	private static ParamBoundsStatic fs = new ParamBoundsStatic();
	public static ParamBoundsStatic fs() { return fs; }
}
