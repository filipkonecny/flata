package verimag.flata.presburger;

public class Distance implements Comparable<Distance> {
	int inf = 0;
	int fin = 0;
	
	public String toString() { 
		return "["+inf+"-"+fin+"]";
	}

	@Override
	public int compareTo(Distance o) {
		int d = inf - o.inf;
		if (d != 0)
			return d;
		return fin - o.fin;
	}
}