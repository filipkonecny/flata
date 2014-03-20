package verimag.flata.presburger;

import java.util.*;

/**
 * 
 * @author konecny
 *
 * Class is used for replacement of terms which differ only in constant term 
 * by the most restrictive of them  
 */

@SuppressWarnings("serial")
public class MapWithListValues //extends HashMap<LinConstraint, ListAndMostRestrictive> {
extends verimag.flata.common.HMapWColVal<LinearConstr,LinearConstr,LinConstrsMostRestrictive> {

	@SuppressWarnings("unchecked")
  public MapWithListValues() {
		//super((Class<HashSet<LinConstraint>>) new HashSet<LinConstraint>().getClass());
		super((Class<LinConstrsMostRestrictive>) new LinConstrsMostRestrictive().getClass());
	}

	public HashSet<LinearConstr> getRedundantConstraints() {
		HashSet<LinearConstr> redundancies = new HashSet<LinearConstr>();
		
		for (LinConstrsMostRestrictive value : this.values()) {
			List<LinearConstr> list = value; 
			for (int i=0; i<list.size(); ++i) {
				LinearConstr actual = list.get(i);
				if (value.mostRestrictive != actual)
					redundancies.add(actual);
			}
		}

		return redundancies;
	}
	
	LinearConstr getMostRestrictive(LinearConstr key) {
		return this.get(key).mostRestrictive;
	}
	int getMostRestrictiveConst(LinearConstr key) {
		return this.get(key).mostRestrictiveConst;
	}
}
