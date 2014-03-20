package verimag.flata.presburger;

public interface DBOct {
	
	// used with 2 completely different semantics:
	//  (1) intersection semantics: union of variables, conjunction of all constraints
	//  (2) hull semantics: intersection of variables, max over common constraints
	
	public static class II {
		int mergedSize;
		Variable[] vars;
		int[] inx1, inx2;
		public II(int[] i1, int[] i2, int aMergedSize, Variable[] aVars) {
			inx1 = i1;
			inx2 = i2;
			mergedSize = aMergedSize;
			vars = aVars;
		}
		public static void copyPrimedPart(int ownHalf, int mergedHalf, int[] arr) {
			for (int i=0; i<ownHalf; i++) {
				arr[i+ownHalf] = arr[i] + mergedHalf;
			}
		}
	}
	
	public Variable[] vars();
	public II iii(Variable[] v1, Variable[] v2);
	public DBM dbm();
}
