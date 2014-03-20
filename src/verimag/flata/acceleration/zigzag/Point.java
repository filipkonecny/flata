package verimag.flata.acceleration.zigzag; 

/**
 * 
 * 2D Point, as a fragment on a path; 
 * the first dimension is the length and the second 
 * is the weight of the path
 *
 */

public class Point {

	private int l; // length
	private int w;	// weight
	
	public Point(int length, int weight) {
		l = length;
		w = weight;
	}

	public Point copy() {
		return new Point(l, w);
	}
		
	public Point sum(Point p) {
		return new Point(l + p.l, w + p.w);
	}
	
	public Point diff(Point p) {
		return new Point(l - p.l, w - p.w);
	}
	
	public Point times(int i) {
		return new Point(l * i, w * i);
	}
	
	public Point div(int n) {
		return new Point(l / n, w / n);
	}
	
	public void divideWeight(int n) { w = w / n; }
		
	public boolean smallerBase(Point b1, Point b2) {
		return b1.l <= b2.l && (b2.l - b1.l) % l == 0 && w * (b2.l - b1.l) <= l * (b2.w - b1.w);
	}
	
	public boolean isEqual(Point p) { return (p != null) && (p.l == l) && (p.w == w); }

	public boolean equals(Object o) { return isEqual((Point) o); }
	
	public boolean lessThan(Point p) { return (p != null) && (p.l == l) && (p.w <= w); }
	
	public boolean lessRatioThen(Point p) {
		// when equal ratio prefers points with smaller length
		return (p != null) && ((w * p.l < p.w * l) || (w * p.l == p.w * l && l < p.l)); 
	}
	
	public void tightenWeight() { w = (w / 2) * 2; }

	public void doubleWeight() { w = w * 2; }
	
	public boolean evenWeight() { return w % 2 == 0; }
	
	// public boolean equals(Object o) { return isEqual((Point) o); }
	
	public void multiplyWeight(int n) { w = w * n; }
	
	public void multiplyPath(int n) { l *= n; w *= n; }
	
	public void translate(Point p) { l += p.l; w += p.w; }
	
	public void addWeight(Point p) { w += p.w; }
		
	//the least common multiple of two lengths of path
	public int lcmPeriod(Point p) {
		if (l == 0 || p.l == 0)
			return 0;
		
		return GSLSet.lcm(l, p.l);
	}
		
	// checks if the path is smaller that that encoded by fragment p
	public boolean smallerPath(Point p) { return l < p.l; }
	
	public int delta(Point otherBase, Point generator) {
		if (generator.l == 0)
			return 0;
		
		return ( l - otherBase.l ) / generator.l ; 
	}
	
	public String toString() { return "("+l+","+w+")"; }
	
	/**
	 * 
	 * @return the first coordinate of the point
	 */
	public int getLength() { return l; }
	
	public void setLength(int length) { l = length; } 
	
	/**
	 * 
	 * @return the second coordinate of the point
	 */
	public int getWeight() { return w; }
	
	public void setWeight(int weight) { w = weight; }
}
