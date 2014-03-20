package verimag.flata.presburger;

public class DetUpdateAndGuards {
	// this class represents a relation R(x,x') as a conjuction of:
	public Relation guard_unp; // phi(x) -- guard on unprimed variables
	public Relation update;    // R(z,z') -- deterministic update, z is subset of x
	public Relation guard_pr;  // psi(x') -- guard on primed variables
	
	public DetUpdateAndGuards(Relation aGuardUnp, Relation aUpdate, Relation aGuardPrimed) {
		guard_unp = aGuardUnp;
		update = aUpdate;
		guard_pr = aGuardPrimed;
	}
}
