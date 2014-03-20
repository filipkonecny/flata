package verimag.flata.presburger;

import java.util.*;

public class FiniteVarIntervals {
	public List<FiniteVarInterval> intervals = new LinkedList<FiniteVarInterval>();
	
	public static int MAX_SIZE = 5;
	
	public void add(FiniteVarInterval aI) { intervals.add(aI); }
	
	public boolean isEmpty() { return intervals.isEmpty(); }
	
	public FiniteVarInterval getSmallestNonZero() {
		return getSmallestInRange(1,MAX_SIZE); // heuristics
	}
	public FiniteVarInterval getSmallestInRange(int minSize, int maxSize) {
		FiniteVarInterval ret = null;
		for (FiniteVarInterval aux : intervals) {
			int size = aux.bnd_up-aux.bnd_low;
			if (size >= minSize && size <= maxSize) {
				if (ret == null) {
					ret = aux;
				} else if (aux.bnd_up-aux.bnd_low < ret.bnd_up-ret.bnd_low) {
					ret = aux;
				}
			}
			
		}
		
		return ret;
	}
}
