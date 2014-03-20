package verimag.flata.acceleration.zigzag;

import java.util.*;

import verimag.flata.acceleration.zigzag.flataofpca.SLSetCompare;

public class GSLSet implements SLSet {
	
	public static boolean PRESTAF = false;
	public static boolean PRESTAF_MINIM = false;
	
	private Vector<GSLBundle> bundles;

	public GSLSet() {
		bundles = new Vector<GSLBundle>();
	}

	public GSLSet copy() {
		GSLSet ret = new GSLSet();

		for (int i = 0; i < bundles.size(); i++)
			ret.bundles.add(bundles.elementAt(i).copy());

		return ret;
	}

	public void clear() {
		bundles.clear();
	}

	public Vector<LinSet> getLinearSets() {
		Vector<LinSet> result = new Vector<LinSet>();

		for (int i = 0; i < bundles.size(); i++) {
			Vector<Point> generators = new Vector<Point>();
			Point g = bundles.elementAt(i).getGenerator();

			if (g != null)
				generators.add(g);

			for (int j = 0; j < bundles.elementAt(i).getBases().size(); j++) {
				Point base = bundles.elementAt(i).getBase(j);
				result.add(new GLinSet(base, generators));
			}
		}

		return result;
	}

	public boolean empty() {
		return bundles.isEmpty();
	}

	public void translate(Point p) {
		GSLSet s = copy();

		for (int i = 0; i < bundles.size(); i++)
			bundles.elementAt(i).translate(p);

		if (Graph.DEBUG >= Graph.MEDIUM_DEBUG && GSLSet.PRESTAF) {
			GLinSet tmp = new GLinSet(); tmp.setBase(p);
			LinSet[] aux = new LinSet[0];
			LinSet[] op1 = s.getLinearSets().toArray(aux);
			LinSet[] op2 = new GLinSet[]{tmp};
			LinSet[] res = this.getLinearSets().toArray(aux);
			
			if (!SLSetCompare.prestaf_checkMinimalSum(op1,op2,res)) {
				System.err.println("op1:"+s+"\nop2:"+op2+"\nres:"+this+"\n");
				throw new RuntimeException("Translate incorrect.");
			}
		}
		
		// System.out.println("\ttranslate " + s + " by " + p + " --> " + this);
	}

	private void addBundle(GSLBundle b) {

		// System.out.println("addBundle " + b + " to " + this);

		if (b.getGenerator() == null) {

			for (int i = 0; i < b.getBases().size(); i++)
				addPoint(b.getBase(i));

		} else {
			for (int i = 0; i < bundles.size(); i++) {
				GSLBundle c = bundles.elementAt(i);

				if (c.getGenerator() != null
						&& c.getGenerator().isEqual(b.getGenerator())) {

					for (int j = 0; j < b.getBases().size(); j++)
						c.addBase(b.getBase(j));

					return;
				}
			}

			bundles.add(b);
		}
	}

	public void union(SLSet set) {
		GSLSet s = copy();

		for (int i = 0; i < ((GSLSet) set).bundles.size(); i++)
			addBundle(((GSLSet) set).bundles.elementAt(i));

		// System.out.println("\t" + s + " U " + set + " --> " + this);
		if (Graph.DEBUG >= Graph.MEDIUM_DEBUG && GSLSet.PRESTAF) {
			LinSet[] aux = new LinSet[0];
			LinSet[] op1 = s.getLinearSets().toArray(aux);
			LinSet[] op2 = set.getLinearSets().toArray(aux);
			LinSet[] res = this.getLinearSets().toArray(aux);
			
			if (!SLSetCompare.prestaf_checkUnion(op1,op2,res)) {
				System.err.println("op1:"+s+"\nop2:"+set+"\nres:"+this+"\n");
				throw new RuntimeException("Union incorrect.");
			}
		}

	}

	public void addPoint(Point p) {
		GSLSet s = copy();

		for (int i = 0; i < bundles.size(); i++) {
			if (bundles.elementAt(i).contains(p)) {
				// System.out.println("\taddPoint1 " + p + " to " + s + " --> " + this);
				return;
			}
		}

		GSLBundle bundle;

		if (bundles.size() > 0 && bundles.firstElement().getGenerator() == null)
			bundle = bundles.firstElement();
		else
			bundles.add(0, bundle = new GSLBundle());

		bundle.addBase(p);
		// System.out.println("\taddPoint " + p + " to " + s + " --> " + this);
		if (Graph.DEBUG >= Graph.MEDIUM_DEBUG && GSLSet.PRESTAF) {
			LinSet[] aux = new LinSet[0];
			LinSet[] op1 = s.getLinearSets().toArray(aux);
			Vector<Point> gens = new java.util.Vector<Point>();
			LinSet[] op2 = new LinSet[]{new GLinSet(p, gens)};
			LinSet[] res = this.getLinearSets().toArray(aux);
			
			if (!SLSetCompare.prestaf_checkUnion(op1,op2,res)) {
				System.err.println("op1:"+s+"\nop2:"+op2+"\nres:"+this+"\n");
				throw new RuntimeException("Add point incorrect.");
			}
		}
	}

	public void addGenerator(Point g) {
		GSLSet s = copy();

		for (int i = 0; i < bundles.size(); i++)
			bundles.elementAt(i).addGenerator(g);

		// merge bundles with the same generator
		for (int i = 0; i < bundles.size(); i++) {
			GSLBundle b = bundles.elementAt(i);
			Point bg = b.getGenerator();

			for (int j = i + 1; j < bundles.size(); j++) {
				GSLBundle c = bundles.elementAt(j);
				Point cg = c.getGenerator();

				if ((bg == null && cg == null)
						|| (bg != null && bg.isEqual(cg))) {

					for (int k = 0; k < c.getBases().size(); k++)
						b.addBase(c.getBase(k));

					bundles.remove(j--);
				}
			}
		}

		if (Graph.DEBUG >= Graph.MEDIUM_DEBUG && GSLSet.PRESTAF) {
			Vector<Point> gens = new java.util.Vector<Point>(); gens.add(g);
			LinSet[] aux = new GLinSet[0];
			LinSet[] op1 = s.getLinearSets().toArray(aux);
			LinSet[] op2 = new GLinSet[]{new GLinSet(new Point(0,0), gens)};
			LinSet[] op3 = this.getLinearSets().toArray(aux);
			
			if (!SLSetCompare.prestaf_checkMinimalSum(op1,op2,op3)) {
				System.err.println("op1:"+s+"\nop2:"+g+"\nres:"+this+"\n");
				throw new RuntimeException("Add generator incorrect.");
			}
		}
		
		// System.out.println("\taddGenerator " + g + " to " + s + " --> " + this);
	}

	public SLSet sum(SLSet set) {
		GSLSet result = new GSLSet();
		GSLSet s = copy();

		for (int i = 0; i < bundles.size(); i++) {			
			for (int j = 0; j < ((GSLSet) set).bundles.size(); j++) {
				GSLBundle c = ((GSLSet) set).bundles.elementAt(j);

				for (int k = 0; k < c.getBases().size(); k++) {
					GSLBundle newB = bundles.elementAt(i).copy();
										
					newB.addGenerator(c.getGenerator());
					newB.translate(c.getBase(k));
					result.addBundle(newB);
				}
			}			
		}

		// System.out.println("\t" + s + " (+) " + set + " --> " + result);
		if (Graph.DEBUG >= Graph.MEDIUM_DEBUG && GSLSet.PRESTAF) {
			LinSet[] aux = new LinSet[0];
			LinSet[] op1 = s.getLinearSets().toArray(aux);
			LinSet[] op2 = set.getLinearSets().toArray(aux);
			LinSet[] res = result.getLinearSets().toArray(aux);
			
			if (!SLSetCompare.prestaf_checkMinimalSum(op1,op2,res)) {
				System.err.println("op1:"+s+"\nop2:"+set+"\nres:"+result+"\n");
				throw new RuntimeException("Sum incorrect.");
			}
		}
		
		return result;
	}

	public boolean equals(SLSet set) {
		return set.includes(this) && includes(set);
	}

	public boolean includes(SLSet set) {
		for (int i = 0; i < ((GSLSet) set).bundles.size(); i++) {
			GSLBundle bundle = ((GSLSet) set).bundles.elementAt(i);

			boolean is_included = false;
			for (int j = 0; j < bundles.size(); j++) {
				if (bundles.elementAt(j).includes(bundle)) {
					is_included = true;
					break;
				}
			}

			if (!is_included)
				return false;
		}

		return true;
	}

	public static int gcd(int a, int b) {
		while (b != 0) {
			int t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	public static int lcm(int a, int b) {
		return a * b / gcd(a, b);
	}

	private void minimizeBundles() {
		boolean stable = false;
		
		while (!stable) {
			stable = true;
	
			for (int i = 0; i < bundles.size() && stable; i ++)
				for (int j = 0; j < bundles.size() && stable; j ++) {					
					if (i != j && bundles.elementAt(i).includes(bundles.elementAt(j))) {					
						bundles.remove(j);
						stable = false;
					}	
				}
		}
	}
	
	private int firstLinearBundleIndex() {
		for (int i = 0; i < bundles.size(); i++) {
			if (bundles.elementAt(i).getGenerator() != null)
				return i;
		}

		return -1;
	}

	private void unifyPeriods() {
		int index = firstLinearBundleIndex();

		// only points
		if (index == -1)
			return;

		int M = bundles.elementAt(index).getGenerator().getLength();

		for (int i = index + 1; i < bundles.size(); i ++)
			M = lcm(M, bundles.elementAt(i).getGenerator().getLength());

		Vector<GSLBundle> newBundles = new Vector<GSLBundle>();

		// add points
		for (int i = 0; i < index; i++)
			newBundles.add(bundles.elementAt(i));

		// unroll
		for (int i = index; i < bundles.size(); i ++) {
			GSLBundle b = bundles.elementAt(i);
			Point g = b.getGenerator();

			int genL = g.getLength();
			int genW = g.getWeight();

			GSLBundle newB = new GSLBundle(new Point(M, (M / genL) * genW));

			for (int j = 0; j < b.getBases().size(); j ++) {
				Point base = b.getBase(j);

				for (int k = 0; k < M / genL; k ++)
					newB.setBase(base.sum(g.times(k)));
			}

			newBundles.add(newB);
		}
		
		// merge bundles with same generator
		for (int i = index; i < newBundles.size(); i ++) {
			GSLBundle bi = newBundles.elementAt(i);

			for (int j = i + 1; j < newBundles.size(); j ++) {
				GSLBundle bj = newBundles.elementAt(j);

				if (bi.getGenerator().isEqual(bj.getGenerator())) {
					for (int k = 0; k < bj.getBases().size(); k ++)
						bi.addBase(bj.getBase(k));

					newBundles.remove(j --);
				}
			}
		}
		
		bundles = newBundles;
	}

	private void intersectLinSets(int bundleIndex1, int baseIndex1,
			int bundleIndex2, int baseIndex2) {

		GSLBundle b1 = bundles.elementAt(bundleIndex1);
		GSLBundle b2 = bundles.elementAt(bundleIndex2);

		Point gen1 = b1.getGenerator();
		Point gen2 = b2.getGenerator();
		Point base1 = b1.getBase(baseIndex1);
		Point base2 = b2.getBase(baseIndex2);

		if (gen1.getLength() != gen2.getLength())
			throw new RuntimeException("different length generators " + gen1
					+ " " + gen2);

		if (gen1.getWeight() >= gen2.getWeight())
			throw new RuntimeException("wrong weight generators " + gen1 + " "
					+ gen2);

		if (base1.getLength() != base2.getLength())
			throw new RuntimeException("different length bases " + base1 + " "
					+ base2);

		if (base1.getWeight() <= base2.getWeight())
			throw new RuntimeException("wrong weight bases " + base1 + " "
					+ base2);

		int K = (base1.getWeight() - base2.getWeight())
				/ (gen1.getWeight() - gen2.getWeight());

		b1.advanceBase(baseIndex1, K);

		detachPoints(bundleIndex2, baseIndex2, base2.getLength() + K
				* gen2.getWeight());
	}

	private void detachPoints(int bundleIndex, int baseIndex, int startLength) {
		GSLBundle b = bundles.elementAt(bundleIndex);

		while (b.getBase(baseIndex).getLength() <= startLength) {
			Point p = b.getPoint(baseIndex, 0);
			b.advanceBase(baseIndex, 1);
			addPoint(p);
		}
	}

	private void minimizeLinSets() {
		boolean changed = true;

		while (changed) {
			changed = false;

			int index = firstLinearBundleIndex();

			// only points in the set
			if (index == -1)
				return;

			for (int i = index; i < bundles.size() && !changed; i++) {
				GSLBundle bi = bundles.elementAt(i);
				int genW1 = bi.getGenerator().getWeight();

				for (int j = i; j < bundles.size() && !changed; j++) {
					GSLBundle bj = bundles.elementAt(j);
					int genW2 = bj.getGenerator().getWeight();

					if (bi.getGenerator().getLength() != bj.getGenerator().getLength())
						throw new RuntimeException("different length generators");

					for (int k = 0; k < bi.getBases().size() && !changed; k++) {
						Point base1 = bi.getBase(k);
						int length1 = base1.getLength();
						int weight1 = base1.getWeight();

						for (int l = (i == j) ? k + 1 : 0; l < bj.getBases().size() && !changed; l++) {
							Point base2 = bj.getBase(l);
							int length2 = base2.getLength();
							int weight2 = base2.getWeight();

							// System.out.println("{"+bi.getGenerator()+" "+base1+"} {"+
							// bj.getGenerator()+" "+base2+"}");

							if (((length1 - length2) % bi.getGenerator().getLength()) == 0) {
							
								if (length1 == length2) {
									if (genW1 <= genW2 && weight1 <= weight2) {
										bj.removeBase(l);
										
										if (bj.getBases().size() == 0)
											bundles.remove(j);													
									} else if (genW1 >= genW2 && weight1 >= weight2) {
										bi.removeBase(k);
										
										if (bi.getBases().size() == 0)
											bundles.remove(i);										
									} else if (genW1 < genW2 && weight1 > weight2) {
										intersectLinSets(j, l, i, k);
									} else {
										intersectLinSets(i, k, j, l);
									}
								} else if (length1 < length2) {
									// up to but not including length2
									detachPoints(i, k, length2 - 1);
								} else {
									// up to but not including length1
									detachPoints(j, l, length1 - 1);
								}

								changed = true;
							}
						}
					}
				}
			}
		}
	}

	private void minimizePoints() {
		if (bundles.size() < 1)
			return;

		GSLBundle points = bundles.firstElement();

		// no points to minimize
		if (points.getGenerator() != null)
			return;

		boolean changed = true;

		while (changed) {
			changed = false;

			for (int i = 1; i < bundles.size() && !changed; i++) {
				GSLBundle bi = bundles.elementAt(i);
				int genW = bi.getGenerator().getWeight();
				int genL = bi.getGenerator().getLength();

				for (int k = 0; k < points.getBases().size() && !changed; k++) {
					Point base1 = points.getBase(k);
					int length1 = base1.getLength();
					int weight1 = base1.getWeight();

					for (int l = 0; l < bi.getBases().size() && !changed; l++) {
						Point base2 = bi.getBase(l);
						int length2 = base2.getLength();
						int weight2 = base2.getWeight();

						if ((length1 >= length2) && ((length1 - length2) % genL) == 0) {

							if (weight1 >= weight2
									+ ((length1 - length2) / genL) * genW) {
								points.removeBase(k);

								if (points.getBases().size() == 0)
									bundles.remove(0);
							} else {
								// up to including length1
								detachPoints(i, l, length1);
							}

							changed = true;
						}
					}
				}
			}
		}
	}

	private void unrollBases() {
		for (int i = 0; i < bundles.size(); i ++) {
			GSLBundle b = bundles.elementAt(i);

			for (int j = 0; j < b.getBases().size(); j ++) {
				if (b.getBase(j).getLength() == 0) {
					if (b.getGenerator() == null) {
						b.removeBase(j --);

						if (b.getBases().size() == 0)
							bundles.remove(i);
					} else
						b.advanceBase(j, 1);
				}
			}
		}
	}

	private void compactBundles() {
		int index = firstLinearBundleIndex();
		
		// only points in the set
		if (index == -1)
			return;

		// reduce the generators
		for (int i = 0; i < bundles.size(); i++)
			bundles.elementAt(i).compact();
	
		// distribute the points
		if (index > 0) {
			GSLBundle points = bundles.firstElement();
			boolean stop = false;
			
			for (int i = points.getBases().size() - 1; i >= 0 && !stop; i --) {
				Point p = points.getBase(i);

				for (int j = index; j < bundles.size(); j ++) {
					if (bundles.elementAt(j).extendsTo(p)) {
						points.removeBase(i);

						if (points.getBases().size() == 0) {
							bundles.remove(0);
							stop = true;
						}

						break;
					}
				}
			}

			// new attempt to reduce the generators 
			// after dispatching all the points
			for (int i = 0; i < bundles.size(); i++)
				bundles.elementAt(i).compact();			
		}
	}
	
	public void minimize() {
		GSLSet s = copy();
				
		if (empty())
			return;

		// System.out.println("minimize start: " + this);
		
		minimizeBundles();
		
		// System.out.println("minimize unify: " + this);

		unifyPeriods();

		// System.out.println("minimize minlin: " + this);

		minimizeLinSets();

		// System.out.println("minimize minpts: " + this);

		minimizePoints();

		// System.out.println("minimize unroll: " + this);
		
		unrollBases();
		
		// System.out.println("minimize compact: " + this);

		compactBundles();
				
		if (Graph.DEBUG >= Graph.MEDIUM_DEBUG)
			System.out.println("minimize " + s + " --> " + this);
		
		if (Graph.DEBUG >= Graph.MEDIUM_DEBUG && GSLSet.PRESTAF_MINIM) {
			LinSet[] aux = new LinSet[0];
			LinSet[] op = s.getLinearSets().toArray(aux);
			LinSet[] res = this.getLinearSets().toArray(aux);
			
			if (!SLSetCompare.prestaf_checkMinimization(op,res)) {
				System.err.println(s+" \nminimized to\n "+this);
				throw new RuntimeException("Minimization incorrect.");
			}
		}
	}

	public void tighten() {
		for (int i = 0; i < bundles.size(); i++)
			bundles.elementAt(i).tighten();
	}

	private void divideByTwo() {
		for (int i = 0; i < bundles.size(); i++) {
			GSLBundle b = bundles.elementAt(i);

			b.getGenerator().divideWeight(2);
			for (int j = 0; j < b.getBases().size(); j++)
				b.getBases().elementAt(j).divideWeight(2);
		}
	}

	public void close(SLSet s1, SLSet s2) {
		GSLSet s = (GSLSet) s1.sum(s2);
		s.divideByTwo();
		union(s);
		minimize();
	}

	public int maxPosSteps() {
		if (bundles.size() == 0)
			return -1;

		int m = bundles.firstElement().maxPosSteps();

		for (int i = 1; i < bundles.size(); i++) {
			int n = bundles.elementAt(i).maxPosSteps();

			if (m < n)
				m = n;
		}

		return m;
	}

	public String toString() {
		String s = "[";

		for (int i = 0; i < bundles.size(); i++) {
			s += bundles.elementAt(i);

			if (i < bundles.size() - 1)
				s += "; ";
		}

		return s + "]";
	}

	public static void main(String[] args) {
		GSLSet s = new GSLSet();
		GSLSet t = new GSLSet();
		GSLSet v = new GSLSet();
		GSLSet u = new GSLSet();
		// GSLSet w = new GSLSet();

		s.addPoint(new Point(1, -2));
		s.addGenerator(new Point(1, -1));
		
		v.addPoint(new Point(1, -2));
		v.addGenerator(new Point(3, -3));
		
		System.out.println(s + " includes "  + v + " " + s.includes(v));
	}
}
