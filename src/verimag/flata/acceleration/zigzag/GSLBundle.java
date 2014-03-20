package verimag.flata.acceleration.zigzag;

import java.util.*;

public class GSLBundle {
	private Point generator;
	private Vector<Point> bases;
	
	public GSLBundle() { this(null); }
	
	public GSLBundle(Point g) {
		generator = g;
		bases = new Vector<Point>();
	}
	
	public void setBase(Point b) { bases.add(b); }
	
	public void addBase(Point b) {		
		// System.out.println("addBase " + b + " to " + this);
		
		if (b == null)
			return;
				
		if (generator == null) {			
			for (int i = 0; i < bases.size(); i ++) {
				Point c = bases.elementAt(i);
				
				if (c.lessThan(b))
					return;
				
				if (b.lessThan(c)) {
					bases.remove(i);
					bases.add(i, b);
					return;
				}
				
				if (c.getLength() > b.getLength()) {
					bases.add(i, b);					
					return;
				}
			}
			
			bases.add(b);
		} else {
			boolean added = false;
			int i;
			
			for (i = 0; i < bases.size() && !added; i ++) {
				Point c = bases.elementAt(i);
				
				if (generator.smallerBase(c, b))					
					return;
				
				if (!added && c.getLength() > b.getLength()) {
					bases.add(i, b);
					added = true;
				}				
			}
			
			if (!added)
				bases.add(b);
			
			// System.out.println("added " + b + " at " + i);
			
			for (int j = i + 1; j < bases.size(); j ++) {
				Point c = bases.elementAt(j);
				
				if (generator.smallerBase(b, c))
					bases.remove(j --);			
			}
		}
		
		// System.out.println("addBase: " + this);
	}
	
	public void removeBase(int i) {
		if (i < 0 || i >= bases.size())
			throw new RuntimeException("wrong index " + i + "(" + bases.size() + ")");
		
		if (bases.size() == 1) {
			bases.clear();
			generator = null;			
			return;
		}
		
		bases.remove(i);
	}
		
	public Point getPoint(int baseIndex, int index) { 
		return bases.elementAt(baseIndex).sum(generator.times(index));
	}
	
	private void setGenerator(Point g) {
		generator = g;
		
		if (generator == null)
			return;
			
		for (int i = 0; i < bases.size(); i ++) {
			for (int j = i + 1; j < bases.size(); j ++) {
				if (generator.smallerBase(bases.elementAt(i), bases.elementAt(j))) {
					bases.remove(j --);
				}
			}
		}
	}
	
	public void addGenerator(Point g) {
		// System.out.println(this + ": addGenerator " + g);
		
		if (g == null)
			return;
			
		if (generator == null) {
			setGenerator(g);
			return;
		}
		
		Point[] bvec = new Point[bases.size()];
		Point x, y;

		bases.copyInto(bvec);
		
		if (generator.lessRatioThen(g)) { 
			x = generator; 
			y = g;
		} else {
			x = g;
			y = generator;
		}
				
		setGenerator(x);	
				
		for (int i = 0; i < bvec.length; i ++) {
			for (int j = 1; j < GSLSet.lcm(x.getLength(),y.getLength()) / y.getLength(); j ++)
				addBase(bvec[i].sum(y.times(j)));
		}
	}
	
	public Point getGenerator() { return generator; }
	
	public Vector<Point> getBases() { return bases; }

	public Point getBase(int i) { return bases.elementAt(i); }
	
	public void advanceBase(int i, int n) { 
		Point b = bases.elementAt(i);
		bases.set(i, b.sum(generator.times(n)));
		orderBases();
	}
	
	public GSLBundle copy() { 
		GSLBundle ret = new GSLBundle();

		if (generator != null)
			ret.generator = generator.copy();
		
		for (int i = 0; i < bases.size(); i ++)
			ret.bases.add(bases.elementAt(i).copy());
		
		return ret;
	}
	
	public void translate(Point p) {
		// System.out.print("translate " + this + " by " + p + " --> ");
		
		for (int i = 0; i < bases.size(); i ++)
			bases.elementAt(i).translate(p);
		
		// System.out.println(this);
	}
	
	/*
	private void divide(int d) {
		generator.divide(d);
		
		for (int i = 0; i < bases.size(); i ++)
			bases.elementAt(i).divide(d);
	}
	*/
	
	public void tighten() {
		if (!generator.evenWeight()) {
			generator.doubleWeight();
			
			// we need this to keep the bases ordered
			// and to avoid possible duplicates
			Vector<Point> aux = new Vector<Point>();
			for(int i = 0; i < bases.size(); i ++)
				aux.add(bases.elementAt(i).sum(generator));
			
			for (int i = 0; i < aux.size(); i ++)
				addBase(aux.elementAt(i));
		}
		
		for (int i = 0; i < bases.size(); i ++)
			bases.elementAt(i).tightenWeight();
	}
	
	public boolean includes(GSLBundle bundle) {
		if (generator == null) {
			if (bundle.getGenerator() != null)
				return false;
			
			for (int i = 0; i < bundle.getBases().size(); i ++) {
				Point b = bundle.getBase(i);
				boolean included = false;
				
				for (int j = 0; j < bases.size(); j ++) {
					Point c = bases.elementAt(j);
					
					if (c.lessThan(b)) {
						included = true;
						break;
					}
				}
				
				if (!included)
					return false;
			}
		} else {
			Point g = bundle.getGenerator();
			
			if (g != null) {
				if (g.getLength() % generator.getLength() != 0)
					return false;
				
				if (g.getWeight() < (g.getLength() / generator.getLength()) * generator.getWeight())
					return false;		
			}
			
			for (int i = 0; i < bundle.getBases().size(); i ++) {
				Point b = bundle.getBase(i);
				boolean included = false;
				
				for (int j = 0; j < bases.size(); j ++) {
					Point c = bases.elementAt(j);
				
					if (generator.smallerBase(c, b)) {
						included = true;
						break;
					}
				}
			
				if (!included)
					return false;
			}
		}
		
		return true;
	}

	public boolean contains(Point p) {
		if (generator == null) { 
			for (int i = 0; i < bases.size(); i ++) {
				if (bases.elementAt(i).lessThan(p))
					return true;
			}
		} else {
			for (int i = 0; i < bases.size(); i ++) {
				Point b = bases.elementAt(i);
				if (b.getLength() <= p.getLength() && 
					generator.smallerBase(b, p))
					return true;
			}
		}
		
		return false;
	}
	
	public boolean extendsTo(Point p) {		
		if (generator == null)
			return false;
				
		for (int i = 0; i < bases.size(); i ++) {
			Point b = bases.elementAt(i);

			if (b.isEqual(p.sum(generator))) {
				bases.remove(i);
				bases.add(i, p);
				return true;
			}
		}
		
		return false;
	}
	
	private void orderBases() {
		Object[] array = bases.toArray();
		
		bases.clear();
		Arrays.sort(array, new PComparator());
		
		for (int i = 0; i < array.length; i ++)
			bases.add((Point) array[i]);
	}
	
	private boolean compactFrom(int index) {
		Point delta = bases.elementAt(index + 1).diff(bases.elementAt(index));
		
		if (delta.getLength() <= 0)
			throw new RuntimeException("unordered bundle: " + this);
		
		if (generator.getLength() % delta.getLength() != 0)
			return false;
		
		int K = generator.getLength() / delta.getLength();
				
		if (K != bases.size() || delta.getWeight() * K != generator.getWeight())
			return false;
					
		boolean sequence = true;
		
		for (int i = 1; i < bases.size() - 1 && sequence; i ++) {
			Point b1 = bases.elementAt(i);
			Point b2 = bases.elementAt(i+1);
		
			if (b1.getLength() >= b2.getLength())
				throw new RuntimeException("unordered bundle: " + this);
			
			if (!delta.isEqual(b2.diff(b1)))
				sequence = false;			
		}
		
		if (!sequence)
			return false;
		
		generator = generator.div(K);
		
		Vector<Point> newBases = new Vector<Point>();
		
		newBases.add(bases.firstElement());
		bases = newBases;		
		
		return true;
	}
	
	public void compact() { 
		if (generator == null || bases.size() < 2)
			return;
		
		orderBases();
		compactFrom(0);
	}
	
	private int linearMaxPosSteps(Point base, Point generator) {
		if (base.getWeight() < 0)
			return base.getLength();
		
		if (generator == null || generator.getWeight() >= 0)
			return -1;
		
		return base.getLength() + (base.getWeight() / (-generator.getWeight())) * generator.getLength();
	}
	
	public int maxPosSteps() {
		if (bases.size() == 0)
			return -1;
			
		int m = linearMaxPosSteps(bases.firstElement(), generator);
		
		for (int i = 1; i < bases.size(); i ++) {
			int n = linearMaxPosSteps(bases.elementAt(i), generator);
			
			if (m < n)
				m = n;
		}
		
		return m;
	}
	
	public String toString() {
		String s = "{";
		
		for (int i = 0; i < bases.size(); i ++) {
			s += bases.elementAt(i);
			
			if (generator != null)
				s += " + " + generator + "N";
			
			if (i < bases.size() - 1)
				s += ", ";
		}
		
		return s + "}";
	}
}
