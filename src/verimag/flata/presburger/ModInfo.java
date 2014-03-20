package verimag.flata.presburger;

import verimag.flata.common.CR;

public class ModInfo {
	
	private ModuloRel mr = null;
	private int mod_offset = -1; // ranges <0..m-1> where m is modulus
	private IntegerInf bnd_low = IntegerInf.negInf();  // t >= bnd_low
	private IntegerInf bnd_up = IntegerInf.posInf();   // t <= bnd_up
	
	public String toString() {
		return "interval=<"+bnd_low+","+bnd_up+">, mod_offset="+mod_offset+", rel <==> "+mr;
	}
	
	// normalize w.r.t. modulus offset
	// i.e. make the interval tight
	public void normalize(int aModulus) {
		if (bnd_up.isFinite()) {
			int aux = tightenUp(bnd_up.toInt(),aModulus,mod_offset);
			bnd_up = IntegerInf.giveField(aux);
		}
		if (bnd_low.isFinite()) {
			// t >= bnd  <==>  not (t <= bnd-1)
			// compute tight upper bound for (bnd_low-1) and add modulus to it
			int aux = tightenUp(bnd_low.toInt()-1,aModulus,mod_offset)+aModulus;
			bnd_low = IntegerInf.giveField(aux);
		}
	}
	private static int tightenUp(int aUpBnd, int aModulus, int aOffset) {
		int nom = aUpBnd+aOffset;
		return CR.floor(nom, aModulus) * aModulus - aOffset;
	}
	
	public void modOffset(int aVal) { mod_offset = aVal; }
	public void bndLow(int aVal) { bnd_low = IntegerInf.giveField(aVal).max(bnd_low); }
	public void bndUp(int aVal) { bnd_up = IntegerInf.giveField(aVal).min(bnd_up); }
	public void rel(ModuloRel aR) { mr = aR; }
	
	public int modOffset() { return mod_offset; }
	public IntegerInf bndLow() { return bnd_low; }
	public IntegerInf bndUp() { return bnd_up; }
	public ModuloRel rel() { return mr; }
}
