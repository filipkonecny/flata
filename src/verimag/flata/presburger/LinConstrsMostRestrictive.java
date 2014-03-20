package verimag.flata.presburger;

import java.util.*;

@SuppressWarnings("serial")
public class LinConstrsMostRestrictive extends ArrayList<LinearConstr> {
	public LinearConstr mostRestrictive;
	public int mostRestrictiveConst;
	
	public LinConstrsMostRestrictive() {
		mostRestrictive = null;
	}
	private void setMostRestrictive(LinearConstr c) {
		mostRestrictive = c;
		LinearTerm term = c.get(null);
		if (term==null)
			mostRestrictiveConst = 0;
		else
			mostRestrictiveConst = term.coeff();
	}
	public boolean add(LinearConstr value) {
		boolean b = super.add(value);
		
		if (mostRestrictive==null) {
			setMostRestrictive(value);
		} else {
			if (!mostRestrictive.hasHigherConstantTerm(value)) {
				setMostRestrictive(value);
			}
		}
		return b;
	}
}
