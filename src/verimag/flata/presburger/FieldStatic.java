package verimag.flata.presburger;


public interface FieldStatic {

	public interface ParametricFS {}
	
	public Field zero();
	public Field one();
	public Field initVal();
	
	public Field giveField(int c);
	public DBC.BoundType giveType();
}
