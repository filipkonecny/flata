/**
 * 
 */
package verimag.flata.presburger;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PartitionMember<T1, T2> {
	Set<T1> vars = new HashSet<T1>();
	List<T2> constrs = new LinkedList<T2>();
	
	public void merge(PartitionMember<T1,T2> other) {
		vars.addAll(other.vars);
		constrs.addAll(other.constrs);
	}
	
	public void add(Collection<T1> aVars, T2 aLc) {
		vars.addAll(aVars);
		if (aLc != null)
			constrs.add(aLc);
	}
}