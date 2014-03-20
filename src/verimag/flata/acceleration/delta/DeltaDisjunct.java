
package verimag.flata.acceleration.delta;

import verimag.flata.presburger.LinearRel;
import verimag.flata.presburger.Relation;

public class DeltaDisjunct {
	public Relation[] rels; // closure R^k after 'n' is eliminated
	public LinearRel periodic_param; // closure R^n with 'k'
	public boolean noK; // no 'k' present 
	
	public DeltaDisjunct(Relation[] aRels, LinearRel aLinRel, boolean aNoK) {
		rels = aRels;
		periodic_param = aLinRel;
		noK = aNoK;
	}
}