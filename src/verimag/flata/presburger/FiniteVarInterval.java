package verimag.flata.presburger;

public class FiniteVarInterval {
	public Variable var;
	int bnd_low;
	int bnd_up;
	
	public FiniteVarInterval(Variable aV, int aL, int aU) {
		var = aV;
		bnd_low = aL;
		bnd_up = aU;
	}
}
