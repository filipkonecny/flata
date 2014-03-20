package verimag.flata.presburger;

import java.util.*;

public class ConstProps {

	Collection<ConstProp> col = new LinkedList<ConstProp>();
	
	public Collection<ConstProp> getAll() {
		return col;
	}
	
	public ConstProps() {
	}
	
	public ConstProps(Collection<ConstProp> aCol) {
		col = aCol;
	}
	
	public ConstProps(ConstProps cps) {
		for (ConstProp cp : cps.col) {
			col.add(new ConstProp(cp));
		}
	}

	public String toString() {
		return col.toString();
	}
	
	public boolean isEmpty() {
		return col.isEmpty();
	}
	public int size() {
		return col.size();
	}
	public void add(ConstProp c) {
		col.add(c);
	}
	
	public ConstProps setMinus(ConstProps other) {
		
		ConstProps ret = new ConstProps();
		
		for (ConstProp c : col) {
			if (!other.contains(c))
				ret.add(new ConstProp(c));
		}
		
		return ret;
	}

	public boolean contains(ConstProp cp) {
		return col.contains(cp);
	}
	public ConstProp find(Variable var) {
		for (ConstProp cp : col)
			if (cp.v.equals(var))
				return cp;
		return null;
	}
	public boolean containsVar(Variable var) {
		for (ConstProp cp : col)
			if (cp.v.equals(var))
				return true;
		return false;
	}
	
	public Variable[] minusVarWithCounterparts(Variable[] varsorig) {
		Collection<Variable> ret = new LinkedList<Variable>();
		for (ConstProp cp : col) {
			if (Arrays.binarySearch(varsorig, cp.v) < 0) {
				ret.add(cp.v);
				ret.add(cp.v.getCounterpart());
			}
//			boolean f = false;
//			for (int i=0; i<varsorig.length; i++)
//				if (varsorig[i].equals(cp.v)) {
//					f = true;
//				}
//			if (!f)
//				ret.add(cp.v);
		}
		return ret.toArray(new Variable[0]);
	}

	public void switchPrimes() {
		for (ConstProp cp : col) {
			cp.v = cp.v.getCounterpart();
		}
	}

	public void keepOnly(Collection<Variable> commonIdentVars) {
		Iterator<ConstProp> iter = this.col.iterator();
		while (iter.hasNext()) {
			if (!commonIdentVars.contains(iter.next().v))
				iter.remove();
		}
	}

	public ConstProps onlyForVars(Variable[] vars_sorted) {
		ConstProps ret = new ConstProps();
		for (ConstProp cp : this.col)
			if (Arrays.binarySearch(vars_sorted, cp.v) >= 0)
				ret.add(cp);
		return ret;
	}

	public void addallShallow(ConstProps inConst) {
		this.col.addAll(inConst.col);
	}
}
