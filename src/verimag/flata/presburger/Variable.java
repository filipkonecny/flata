/**
 * 
 */
package verimag.flata.presburger;

import java.util.*;

/**
 * Class encapsulates the name of a variable. The name, a string, is the member which fully characterizes a variable.
 * Objects of this class are immutable.
 * <p>
 * Class contains information about the type of a variable, which can be unprimed, primed or double-primed. For identification of a type, 
 * static constants are used  
 */
public class Variable implements Comparable<Variable> {
	
	private VariablePool vp;
	public VariablePool vp() { return vp; }
	//public void vp(VariablePool aVP) { vp = aVP; }
	
	public static Variable[] azl = new Variable[0];
	
	public static enum Type {
		ePRIME0, ePRIME2, ePRIME1;
		
		// ePRIME0 < ePRIME2 < ePRIME1
		public int compare(Type other) {
			if (this == other)
				return 0;
			else {
				if (this == ePRIME0 || (this == ePRIME1 && other == ePRIME2)) {
					return -1;
				} else {
					return 1;
				}
			}
		}
		public boolean isPrimed() { return this == ePRIME1; }
		public boolean isDPrimed() { return this == ePRIME2; }
		public boolean isUnprimed() { return this == ePRIME0; }
		
		public Type getCounterpart() {
			switch (this) {
			case ePRIME0:
				return ePRIME1;
			case ePRIME1:
				return ePRIME0;
			default:
				throw new RuntimeException("internal error: attempt to create counterpart for variable type "+this);
			}
		}
	}
	public static final int ePRIME0 = 0x01;
	public static final int ePRIME1 = 0x02;
	public static final int ePRIME2 = 0x04;
	public static String ARMCPRIME = "_p";
	
	
	public boolean isPrimed() { return type.isPrimed(); }
	
	public static final int ePRINT_p = 0x01;
	public static final int ePRINT_prime = 0x02;
	public static final int ePRINT_p_armcPref = 0x04;
	public static final String armc_pref = "V_";
	
	public static final String primeSuf = "'";
	
	final String name;
	private final Type type;
	
	public String name() { return name; }
	public Type type() { return type; }
	
	private Variable(String aName, VariablePool aVP) {
		this(aName, Variable.varType(aName), aVP);
	}
	
	private Variable(String aName, Type aType, VariablePool aVP) {
		name = aName;
		type = aType;
		vp = aVP;
	}
	
	public boolean equals(Object aObject) {
		return (aObject instanceof Variable && name.equals(((Variable)aObject).name));
		//return this==aObject;
	}
	public int hashCode() {
		return name.hashCode();
	}
	public String toString() {
		return name;
	}
	public String toString(int primePrintType) {
		if (primePrintType==Variable.ePRINT_prime) {
			return toString();
		} else {
			String tmp;
			if (this.type.isUnprimed()) {
				tmp = toString();
			} else {
				tmp = new StringBuffer(name).deleteCharAt(name.length()-1).append(Variable.ARMCPRIME).toString();
			}
			
			if (primePrintType==ePRINT_p_armcPref)
				tmp = tmp.substring(0, 1).toUpperCase() + tmp.substring(1, tmp.length());
			return tmp;
		}
	}
	public int compareTo(Variable aOther) {
		if (aOther==null)
			throw new NullPointerException("Attempt to compare an instance of Variable with null.");
		
		int tc = type.compare(aOther.type);
		if (tc != 0) {
			return tc;
		} else {
			return this.getUnprimedName().compareTo(aOther.getUnprimedName());
		}
	}
	
	public Variable getUnprimedVar() {
		return this.vp.getUnprimed(this);
	}
	public Variable getPrimedVar() {
		return this.vp.getPrimed(this);
	}
	public String getUnprimedName() {
		return this.vp.getUnprimed(this).name;
	}
	public String getPrimedName() {
		return this.vp.getPrimed(this).name;
	}
	
	
	public Variable getIntermediate() {
		return this.vp.getIntermediate(this);
	}
	public Variable getCounterpart() {
		return this.vp.getCounterpart(this);
	}
		
	public static Set<Variable> toUnpAndPrimed(Collection<Variable> aCol) {
		Set<Variable> ret = new HashSet<Variable>();
		toUnpAndPrimed(aCol,ret);
		return ret;
	}
	public static Set<Variable> toUnp(Collection<Variable> aCol) {
		Set<Variable> ret = new HashSet<Variable>();
		toUnp(aCol,ret);
		return ret;
	}

	public static void toUnpAndPrimed(Collection<Variable> aCol, Collection<Variable> aColRet) {
		for (Variable var : aCol) {
			aColRet.add(var);
			aColRet.add(var.vp.getCounterpart(var));
		}
	}
	public static void toUnp(Collection<Variable> aCol, Collection<Variable> aColRet) {
		for (Variable var : aCol) {
			aColRet.add(var.vp.getUnprimed(var));
		}
	}
	
	public static Type varType(String name) {
		int l = name.length();
		
		if (l>=2 && name.substring(l-2,l).equals("''"))
			throw new RuntimeException("error: use of a variable with two primes: "+name);
		
		if (name.charAt(l - 1) == '\'') {
			if (l == 1)
				throw new RuntimeException("error: no-name variable: "+name);
			return Type.ePRIME1;
		} else {
			return Type.ePRIME0;
		}
	}
	
//	public static Variable createSpecial(String aName) {
//		return new Variable(aName, null);
//	}
	public static Variable createNew(String aName, VariablePool aVP) {
		Variable ret = new Variable(aName, aVP);
		return ret;
	}
	public static Variable createCounterpart(Variable v) {
		StringBuffer cName = new StringBuffer(v.name);
		Variable.Type cType = v.type.getCounterpart();
		switch (v.type) {
		case ePRIME0:
			cName.append('\'');
			break;
		case ePRIME1:
			cName.deleteCharAt(cName.length() - 1);
			break;
		default:
			throw new RuntimeException("internal error: attempt to create counterpart for \""+v+"\"");
		}
		return new Variable(cName.toString(), cType, v.vp);
	}
	
	public static Variable createIntermediate(Variable v) {
		String name = v.name();
		switch (v.type) {
		case ePRIME0:
			return new Variable(name + "''", Variable.Type.ePRIME2, v.vp);
		case ePRIME1:
			return new Variable(name + "'", Variable.Type.ePRIME2, v.vp);
		default:
			throw new RuntimeException("internal error: attempt to create counterpart for \""+v+"\"");
		}
		
	}
	
	public static Variable[] sortedUnpPrimed(Set<Variable> varUnp) {
		int l = varUnp.size();
		Variable[] ret = new Variable[l*2];
		int i=0;
		for (Variable v : varUnp) {
			ret[i++] = v;
		}
		Arrays.sort(ret, 0, l);
		i=0;
		for(; i<l; i++) {
			ret[i+l] = ret[i].getCounterpart();
		}
		return ret;
	}
	
	public static Variable[] sortedSubset(Variable[] vars, Set<Integer> varsinxs) {
			Variable[] newvars = new Variable[varsinxs.size()];
			int i = 0;
			for (Integer ii : varsinxs) {
				 newvars[i++] = vars[ii.intValue()];
			}
			Arrays.sort(newvars);
			return newvars;
	}
	
	public static Variable[] refVarsUnpPSorted(Variable[] unp) {
		int l = unp.length;
		Variable[] ret = new Variable[l*2];
		
		System.arraycopy(unp, 0, ret, 0, l);
		for(int i=0; i<l; i++)
			ret[i+l] = ret[i].getCounterpart();
		
		return ret;
	}
	public static Variable[] refVarsUnpPSorted(Set<Variable> unp) {
		int unpSize = unp.size();
		Variable[] ret = new Variable[unpSize*2];
		int i = 0;
		for (Variable v : unp) {
			ret[i] = v;
			i++;
		}
		Arrays.sort(ret, 0, unpSize);
		for (i = 0; i < unpSize; i++) {
			ret[i+unpSize] = ret[i].getCounterpart();
		}
		return ret;
	}
	public static Variable[] intersect(Variable[] vars1, Variable[] vars2) {
		List<Variable> l = new LinkedList<Variable>();
		int i1=0, l1 = vars1.length, i2=0, l2 = vars2.length;
		while (i1 < l1 && i2 < l2) {
			int c = vars1[i1].compareTo(vars2[i2]);
			if (c == 0) {
				l.add(vars1[i1]);
				i1 ++;
				i2 ++;
			} else if (c < 0) {
				i1 ++;
			} else
				i2 ++;
		}
		return l.toArray(new Variable[0]);
	}

	// variable array -> string array
	public static String[] sa(Variable[] va) {
		String[] ret = new String[va.length];
		for (int i=0; i<va.length; i++)
			ret[i] = va[i].name;
		return ret;
	}
	
	public static Variable[] counterpartArray(Variable[] arr) {
		Variable[] ret = new Variable[arr.length];
		for (int i=0; i<arr.length; i++)
			ret[i] = arr[i].getCounterpart();
		return ret;
	}
	
	public static Variable[] projectVars(Variable[] fromUnpP, Integer[] inxsUnp) {
		int n = fromUnpP.length / 2;
		int n_n = inxsUnp.length;
		Variable[] ret = new Variable[n_n*2];
		for (int i=0; i<inxsUnp.length; i++) {
			ret[i] = fromUnpP[inxsUnp[i]];
			ret[i+n_n] = fromUnpP[inxsUnp[i]+n];
		}
		
		return ret;
	}
}
