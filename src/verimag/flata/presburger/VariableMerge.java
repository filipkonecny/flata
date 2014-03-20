/**
 * 
 */
package verimag.flata.presburger;

import java.util.*;

class VariableMerge {

	public Variable v;
	public int iNew;
	public int iOld;
	
	public String toString() { return "("+v+","+iNew+","+iOld+")"; }

	public VariableMerge(Variable av, int aiNew, int aiOld) {
		v = av;
		iNew = aiNew;
		iOld = aiOld;
	}

	public static Variable[] variables(VariableMerge[] vm_array) {
		Variable[] ret = new Variable[vm_array.length];
		for (int i = 0; i < vm_array.length; ++i)
			ret[i] = vm_array[i].v;
		return ret;
	}
	
	public static List<VariableMerge> domainMerge(Variable[] dom1, Variable[] dom2) {
		List<VariableMerge> ret = new LinkedList<VariableMerge>();
		int i1 = 0, i2 = 0, iN = 0;
		while (i1 != dom1.length && i2 != dom2.length) {
			Variable v1 = dom1[i1];
			Variable v2 = dom2[i2];
			int comp = v1.compareTo(v2);
			if (comp < 0) {
				ret.add(new VariableMerge(v1, iN, i1));
				i1++;
			} else if (comp > 0) {
				ret.add(new VariableMerge(v2, iN, -1));
				i2++;
			} else {
				ret.add(new VariableMerge(v1, iN, i1));
				i1++;
				i2++;
			}
			iN++;
		}
		
		while (i1 < dom1.length) {
			ret.add(new VariableMerge(dom1[i1], iN, i1));
			i1++;
		}
		
		while (i2 < dom2.length) {
			ret.add(new VariableMerge(dom2[i2], iN, -1));
			i2++;
		}
		
		return ret;
		//return ret.toArray(new VariableMerge[0]);
	}
	
	public static int newVarCnt(List<VariableMerge> vms) {
		int c = 0;
		
		for (VariableMerge vm : vms)
			if (vm.iOld == -1)
				c ++;
		return c;
	}
	
}