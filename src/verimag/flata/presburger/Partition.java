package verimag.flata.presburger;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Partition<T1, T2> {
	List<PartitionMember<T1, T2>> partitions = new LinkedList<PartitionMember<T1, T2>>();
	
	public int size() { return partitions.size(); }
	
	public void merge(Collection<T1> aVars, T2 aLc) {
		PartitionMember<T1, T2> first = null;
		
		PartitionMember<T1, T2> vp = null;
		// iterate over partitions
		Iterator<PartitionMember<T1, T2>> iter = partitions.iterator();
		while (iter.hasNext()) {
			vp = iter.next();
			boolean b = false;
			for (T1 v : vp.vars)
				if (aVars.contains(v)) {
					b = true;
					break;
				}
			
			if (b) {
				if (first == null)
					first = vp;
				else { // merge (variables from aVars appear both in 'first' and 'vp' ) 
					iter.remove();
					first.merge(vp);
				}
			}
		}
		
		if (first == null) { // no variable from aVars is present in current partition
 			first = new PartitionMember<T1, T2>();
			partitions.add(first);
		}
		
		first.add(aVars, aLc); // add variables from aVars to the partition 
	}
}