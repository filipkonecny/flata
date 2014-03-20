/**
 * 
 */
package verimag.flata.presburger;

import java.util.Collection;

import verimag.flata.common.CR;

import nts.parser.*;



/**
 * A linear term is a product of an integer coefficient and a variable. 
 * Note: a constant term has variable==null 
 * note: shallow copy constructor
 */
public class LinearTerm implements java.lang.Comparable<LinearTerm> {

	// (variable==null) => constant term 
	private final Variable variable; // only shallow copies used
	private int coeff;
	
	public Variable variable() { return variable; }
	public int coeff() { return coeff; }
	public void coeff(int aCoeff) { coeff = aCoeff; }

	/**
	 * Constructs a term with the specified variable and, implicitly, with coefficient equal to 1
	 */
	public LinearTerm(Variable aVariable) {
		this(aVariable, 1);
	}
	/**
	 * Constructs a term with the specified variable and coefficient
	 */
	public LinearTerm(Variable aVariable, int aCoeff) {
		super();
		
		variable = aVariable; // reference only
		coeff = aCoeff;
	}
	/**
	 * Constructs a term which is same as the specified term (shallow copies of its members)
	 */
	public LinearTerm(LinearTerm aLinTerm) {
		super();
		variable = aLinTerm.variable; // reference only
		coeff = aLinTerm.coeff;
	}
//	/**
//	 * Creates a linear term in the specified automaton which has same fields as the specified linear term 
//	 */
//	public static LinearTerm deepCopy(LinearTerm aLinTerm) {
//		Variable var = (aLinTerm.variable==null)? null : new Variable(aLinTerm.variable.name());
//		return new LinearTerm(var,aLinTerm.coeff);
//	}
	public static LinearTerm rename(LinearTerm aLinTerm, Rename aRenameInfo, VariablePool aVarPool) {
		Variable var = aLinTerm.variable;
		if (var != null) {
			var = aVarPool.giveVariable(aRenameInfo.getNewNameFor(var.name()));
		}
		return new LinearTerm(var,aLinTerm.coeff);
	}
	
	public boolean equals(Object aObject) {
		if (!(aObject instanceof LinearTerm))
			return false;
		LinearTerm other = (LinearTerm)aObject;
		if (!(((this.variable==null) && (other.variable==null)) ||
			  ((this.variable!=null) && (other.variable!=null))
			 )
		   )
			return false;
		return 
			((variable==null)? other.variable==null : variable.equals(other.variable)) 
					&& coeff == other.coeff;
	}	
	// hash code is a sum of coefficient's hash code and, for non-constant terms, variable's hash code  
	public int hashCode() {
		int hash = coeff;
		if (variable!=null)
			hash += variable.hashCode(); 
		return hash;
	}
	public int compareTo(LinearTerm aOther) {
		if (aOther==null)
			throw new NullPointerException("Attempt to compare an instance of LinTerm with null.");
		
		if (this.variable==null) {
			if (aOther.variable==null) // null -- null
				return this.coeff-aOther.coeff;
			else // null -- non-null
				return 1; // constant term is greatest
		} else if (aOther.variable==null) {  // non-null -- null
			return -1; // constant term is greatest
		} else { // non-null -- non-null
			int ret = this.variable.compareTo(aOther.variable);
			if (ret != 0)
				return ret;
			else {
				return this.coeff-aOther.coeff; 
			}
		}
	}
	
	public LinearTerm times(int c) {
		return new LinearTerm(variable, coeff*c);
	}
	
	public String toString() {
		return toSB(true).toString();
	}
	public StringBuffer toSB(boolean first) {
		return toSB(first, Variable.ePRINT_prime);
	}
	public StringBuffer toSB(boolean first, int primePrintType) {
		String str_coeffAbs = String.valueOf(java.lang.Math.abs(coeff));
		String str_sign;
		// sign
		if (first) {
			str_sign = (coeff < 0) ? "-" : "";
		} else {
			str_sign = (coeff < 0) ? "-" : "+";
		}
		
		StringBuffer sb = new StringBuffer(str_sign);
		
		if (variable!=null) { // add coeff (if not +1 or -1) and variable
			if (!str_coeffAbs.equals("1"))
				sb.append(str_coeffAbs).append("*");
			sb.append(variable.toString(primePrintType));
		} else {  // add coeff
			sb.append(str_coeffAbs);
		}

		return sb;
	}
	// if s_u == null, standard priming is used, otherwise, given suffixes are used
	public StringBuffer toSBYices(String s_u, String s_p) {
		StringBuffer sb = new StringBuffer();
		String xx;
		if (variable == null) {
			//xx = "";
			sb.append(coeff);
			return sb;
		} else if (s_u == null) {
			xx = this.variable.name();
		} else {
			if (variable.isPrimed())
				xx = variable.getUnprimedName()+s_p;
			else
				xx = variable.name()+s_u;
		}
		//String coefStr = (coeff > 0)? ""+coeff : "(- 0 "+Math.abs(coeff)+")";
		String coefStr = ""+coeff;
		if (this.variable!=null) {
			xx = CR.yicesVarName(xx);
			sb.append("(* ").append(coefStr).append(" ").append(xx).append(")");
		} else {
			sb.append("(* ").append(coefStr).append(" 1)");
		}
		return sb;
	}
	
	public static StringBuffer toSBtermList(Collection<LinearTerm> aCol) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (LinearTerm term : aCol) {
			sb.append(term.toSB(first));
			first = false;
		}
		return sb;
	}
	
	public LinearTerm times(LinearTerm other) {
		if (this.variable != null && other.variable != null) {
			//return null;
			throw new verimag.flata.common.NotPresburger();
		} else {
			Variable v = (this.variable != null)? this.variable : other.variable;
			return new LinearTerm(v, this.coeff * other.coeff);
		}
	}
	
	public static LinearTerm constant(int c) {
		return new LinearTerm(null, c);
	}
	public static LinearTerm create(Variable v, int c) {
		return new LinearTerm(v, c);
	}
	
	public Expr toNTS() {
		
		Expr aux = ASTWithoutToken.litInt(Math.abs(coeff));
		if (coeff < 0) {
			aux = ASTWithoutToken.exUnaryMinus(aux);
		}
		
		if (variable != null) {
			aux = ASTWithoutToken.exMult(aux, ASTWithoutToken.accessBasic(variable.name()));
		}
	
		return aux;
	}
}
