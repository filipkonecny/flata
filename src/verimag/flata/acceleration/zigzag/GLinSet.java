package verimag.flata.acceleration.zigzag; 

import java.util.Vector;

/**
 * 
 * Class for handling linear sets
 *
 */

public class GLinSet implements LinSet {

	private Point base;

	private Vector<Point> generators;

	public GLinSet() {
		base = new Point(0, 0);
		generators = new Vector<Point>();
	}

	public GLinSet(Point b, Vector<Point> g) {
		if (b == null)
			throw new RuntimeException("null base");
		
		if (g == null)
			throw new RuntimeException("null generator set");
			
		base = b;
		generators = g;
	}

	public Point getBase() { return base; }
	
	public void setBase(Point p) { base = p; }
	
	public void addToBase(Point p) {
		base.setLength(base.getLength() + p.getLength());
		base.setWeight(base.getWeight() + p.getWeight());
	}
	
	public Point getGenerator() { 
		return (generators.size() > 0) ? generators.firstElement() : null; 
	}
		
	public void tightenBase() {
		base.divideWeight(2);
		base.multiplyWeight(2);
	}
	
	//computes the number of steps in which a linear set with 
	//one generator becomes negative
	public int stepsNegative()
	{		
		if (base.getWeight() < 0)
			return base.getLength();
		else if (generators.size() == 0)
		{
			return -1;
		}
		else
		{
			Point gen = generators.get(0);
			if (gen.getWeight() < 0)
			{
				int quotient = base.getWeight() / (- gen.getWeight());
				return base.getLength() + gen.getLength() * (1 + quotient);
			}
			else
				return -1;
		}
	}
		
	//computes the number of steps in which a linear set with 
	//two generators, one in l and one in k, where l <= k,  becomes negative
	public int consistencyCheck()
	{
		if (base.getWeight() < 0)
			return base.getLength();
		else if (generators.size() < 2)
			return -1;
		else
		{
			Point genL = generators.get(0);
			Point genK = generators.get(1);
			
			if (genK.getWeight() < 0) {
				int N = base.getWeight() / (- genK.getWeight());
				return base.getLength() + (N + 1) * genK.getLength();
			} else if (genK.getWeight() + genL.getWeight() < 0) {
				int N = base.getWeight() / (- genK.getWeight() - genL.getWeight());
				return base.getLength() + (N + 1) * (genK.getLength() + genL.getLength());
			}
			else
				return -1;
		}
	}
	
	//computes the maximum number of steps in which the 
	//weight remains positive; -1 if always
	public int maxPosSteps() {
		if (base.getWeight() < 0)
			return base.getLength();
		
		if (generators.size() == 0)
			return -1;
		
		Point gen = generators.get(0);
		
		if (gen.getWeight() >= 0)
			return -1;
		
		return base.getLength() + (base.getWeight()/(-gen.getWeight())) * gen.getLength();
	}
	
	public String toString() {
		String s = base.toString();
		
		for (int i = 0; i < generators.size(); i ++)
			s += " + " + generators.elementAt(i) + "N";

		return s;
	}	
}
