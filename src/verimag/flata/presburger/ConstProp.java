package verimag.flata.presburger;

public class ConstProp {

	Variable v;
	int c;
	
	public ConstProp(ConstProp other) {
		v = other.v;
		c = other.c;
	}
	public ConstProp(Variable aV, int aC) {
		v = aV;
		c = aC;
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof ConstProp))
			return false;
		ConstProp other = (ConstProp) o;
		return v.equals(other.v) && c == other.c;
	}
	
	public String toString() {
		return "["+v+","+c+"]";
	}
}
