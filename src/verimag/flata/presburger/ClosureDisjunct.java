package verimag.flata.presburger;

public class ClosureDisjunct {

	// common fields
	public int b; // relation gets periodic at R^b
	public int c; // period
	
	public boolean isPrefix;

	// prefix disjunct fields
	public int pref_inx;
	
	// periodic disjunct fields (disjuncts are kept with parameter)
	public int offset; // b+c+offset
	public LinearRel periodic_param; // parametric
	public Variable parameter;

	public ClosureDisjunct(int ab, int ac, int a_pref_inx) {
		isPrefix = true;
		b = ab;
		c = ac;
		pref_inx = a_pref_inx;
	}

	public ClosureDisjunct(int ab, int ac, int a_offset, LinearRel a_periodic_param, Variable aParameter) {
		isPrefix = false;
		b = ab;
		c = ac;
		offset = a_offset;
		periodic_param = a_periodic_param;
		parameter = aParameter;
	}

	public static ClosureDisjunct closure_identity() {
		return new ClosureDisjunct(0,0,0);
	}
}
