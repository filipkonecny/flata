package verimag.flata.presburger;

import java.util.*;

public class Substitution {

	private Map<Variable,LinearConstr> m = new HashMap<Variable,LinearConstr>();
	
	public List<Variable> getKeys() { return new LinkedList<Variable>(m.keySet()); }
	
	public int size() { return m.size(); }
	
	public void put(Variable v, int aV) {
		LinearConstr lc = new LinearConstr();
		lc.addLinTerm(LinearTerm.constant(aV));
		put(v,lc);
	}
	public void put(Variable v, LinearConstr lc) {
		m.put(v, lc);
	}
	public LinearConstr get(Variable v) {
		return m.get(v);
	}
	
}
