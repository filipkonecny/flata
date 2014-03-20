package verimag.flata.presburger;

public class PrefixPeriod {
	private int b;
	private int c;
	
	public int b() { return b; }
	public int c() { return c; }
	
	public PrefixPeriod(int ab, int ac) {
		b = ab;
		c = ac;
	}
}
