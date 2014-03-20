package verimag.flata.presburger;


public class OctConstr {
	public LinearTerm lt1;
	public LinearTerm lt2;
	public int bound;
	
	public OctConstr() {
	}
	public OctConstr(LinearTerm aLt1, LinearTerm aLt2, int aBound) {
		lt1 = aLt1;
		lt2 = aLt2;
		bound = aBound;
	}
	public String toString() {
		return lt1.toString()+(lt2.coeff()>0 ? "+":"" )+lt2+"<="+bound;
	}
}
