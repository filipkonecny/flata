package verimag.flata.presburger;

public class DBOctCompatibility<T extends DBOct> {
	
	T r1, r2;
	
	DBOctCompatibility(T ar1, T ar2) {
		r1 = ar1;
		r2 = ar2;
	}
	
	// does not preserve order (domain cardinality has preference)
//	boolean extended;
//	T d1; // DBRel with bigger domain
//	T d2; 
	
	//int[] inxs; // indices of variables of d2 in d1 (includes zero and zero')
	
//	void get() {
//		
//		Variable[] v1 = r1.vars();
//		Variable[] v2 = r2.vars();
//		int l1 = v1.length;
//		int l2 = v2.length;
//		
//		if (l1 >= l2) {
//			d1 = r1; d2 = r2;
//		} else {
//			d1 = r2; d2 = r1;
//		}
//		
//		extended = l1 == l2;
//		inxs = d1.iii(d1.vars(), d2.vars());
//	}

	
	DBOct.II ii;
	DBM first, second;
	
	void extend() {
		first = r1.dbm();
		second = r2.dbm();
		
		ii = r1.iii(r1.vars(), r2.vars());
		
		first = first.extend(ii.mergedSize, ii.inx1);
		second = second.extend(ii.mergedSize, ii.inx2);
	}

}
