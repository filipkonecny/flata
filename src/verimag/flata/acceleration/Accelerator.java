package verimag.flata.acceleration;

import verimag.flata.presburger.*;

public interface Accelerator {
	
	public static class SubstPair {
		Variable var;
		LinearTerm lt;
	}	
	
	// computes the transitive closure R^+ of a given relation 
	public Relation[] closure(DBM dbm, boolean isOctagon, LinearTerm[] substitution, Variable[] varsOrig);
}
