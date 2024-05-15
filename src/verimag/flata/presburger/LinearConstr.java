package verimag.flata.presburger;

import java.util.*;

import nts.parser.*;

import org.gnu.glpk.*;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

import verimag.flata.common.CR;
import verimag.flata.common.FlataJavaSMT;

/**
 * 
 * a <i>linear constraint</i> is an inequality of the following form
 * 
 * <pre>
 * <tt>a_1*x_1 + a_2*x_2 + ... + a_n*x_n <= 0 (a_i - integer coefficients, x_i - variables)</tt>
 * </pre>
 * 
 * Informally, it is a sum of linear monomials with integer coefficient which is
 * set less or equal to zero)
 * 
 * <p>
 * Implementation: <br>
 * The class extends a Map, which is used for storing linear terms.
 * <tt>variable</tt> objects are used as a keys (and <tt>null</tt> key for a
 * constant term). Map offers only shallow copy methods (such as add, addAll,
 * ...). Following methods which create deep copies are provided: {@link #addLinTerm(LinearTerm)},
 * {@link #addLinTerms(Collection)}, copy constructor
 * {@link #LinConstraint(LinearConstr)}. 
 * If a linear constraint contains no linear term,
 * it is interpreted as a tautology <tt>0 <= 0</tt>.
 */
@SuppressWarnings("serial")
public class LinearConstr extends HashMap<Variable, LinearTerm> implements Constr {

	public boolean isLinear() { return true; }
	public boolean isModulo() {return false; }
	public LinearConstr copy() { return new LinearConstr(this); }
	
	public boolean simpleContradiction() {
		LinearTerm ltzero = get(null);
		if (size() == ((ltzero==null)? 0 : 1)) {
			int zero = (ltzero == null)? 0 : ltzero.coeff();
			if (zero > 0)
				return true;
		}
		return false;
	}
	
	public static final int eTAUTOLOGY = 0x01;
	public static final int eCONTRADICTION = 0x02;

	// pointer to primed variable (there can be either one or none primed
	// variable)
	// to ensure correct value of 'primed' variable, terms may be added only via
	// copy constructor or addLinTerm(s)() method
	private Set<Variable> primedVars = new HashSet<Variable>();
	private Set<Variable> unprimedVars = new HashSet<Variable>();

	public Set<Variable> primedVariables() {
		return primedVars;
	} // no defensive copy

	public boolean containsPrime() {
		return !primedVars.isEmpty();
	}

	public Set<Variable> unprimedVariables() {
		return unprimedVars;
	} // no defensive copy

	public boolean containsUnprime() {
		return !unprimedVars.isEmpty();
	}

	public boolean isAction() {
		return this.containsPrime();
	}

	public Collection<LinearTerm> terms() {
		return this.values();
	}

	public int constTerm() {
		LinearTerm t = this.term(null);
		return (t == null)? 0 : t.coeff();
	}
	
	public LinearTerm term(Variable aVar) {
		return this.get(aVar);
	}

	public int varCount() {
		return unprimedVars.size() + primedVars.size();
	}

	public void variables(Collection<Variable> aVars) {
		
		aVars.addAll(this.keySet());
		aVars.remove(null);
	}

	public Set<Variable> variablesUnpP() {
		Set<Variable> vars = new HashSet<Variable>();
		for (Variable v : this.keySet()) {
			if (v == null)
				continue;
			
			vars.add(v);
			vars.add(v.getCounterpart());
		}
		return vars;
	}
	public Set<Variable> variables() {
		Set<Variable> vars = new HashSet<Variable>();
		variables(vars);
		return vars;
	}

	public LinearConstr unprimeAll() {
		LinearConstr ret = new LinearConstr();
		
		for (LinearTerm lt : this.values()) {
			Variable v = lt.variable();
			if (v != null) {
				v = v.getUnprimedVar();
			}
			ret.addLinTerm(new LinearTerm(v,lt.coeff()));
		}
		
		return ret;
	}
	
	/**
	 * creates an empty constraint (tautology)
	 */
	public LinearConstr() {
		//this(false);
	}
	
	public LinearConstr(LinearTerm t) {
		addLinTerm(t);
	}

//	public LinearConstr(boolean aIsPrecondition) {
//		stLastSymbol = aIsPrecondition;
//	}
	

	public int lcmOfVarCoefs() {
		int lcm = 1;
		for (LinearTerm lt : terms()) {
			if (lt.variable() != null) {
				lcm = LinearConstr.lcm(lcm, Math.abs(lt.coeff()));
			}
		}
		return lcm;
	}

	/**
	 * Constructs a new linear constraint, which contains deep copies of terms
	 * of the specified constraint
	 */
	public LinearConstr(LinearConstr aLinConstr) {
		for (Map.Entry<Variable, LinearTerm> e : aLinConstr.entrySet()) {
			this.put(e.getKey(), new LinearTerm(e.getValue()));
		}
		this.primedVars.addAll(aLinConstr.primedVars);
		this.unprimedVars.addAll(aLinConstr.unprimedVars);
		
//		stLastSymbol = aLinConstr.stLastSymbol;
	}

//	/**
//	 * Creates a linear constraint in the specified automaton which has same
//	 * fields as the specified linear term
//	 */
//	public static LinearConstr deepCopy(LinearConstr aLinConstr) {
//		LinearConstr copy = new LinearConstr();
//		for (LinearTerm term : aLinConstr.values()) {
//			copy.addLinTerm(LinearTerm.deepCopy(term));
//		}
////		copy.stLastSymbol = aLinConstr.stLastSymbol;
//		return copy;
//	}

	public LinearConstr(LinearConstr aOther, Rename aRenVals, VariablePool aVarPool) {
		for (LinearTerm term : aOther.values()) {
			addLinTerm(LinearTerm.rename(term, aRenVals, aVarPool));
		}
//		stLastSymbol = aOther.stLastSymbol;
	}

	public StringBuffer toStringNoROP() {
		StringBuffer sb = new StringBuffer();
		if (this.size() == 0) {
			sb.append("0");
		} else {
			boolean first = true;
			for (LinearTerm term : this.values()) {
				sb.append(term.toSB(first, Variable.ePRINT_prime));
				first = false;
			}
		}
		return sb;
	}
	
	public String toString() {
		return toSB().toString();
	}

	public StringBuffer toSB() {
		return toSB(false, true);
	}

	private LinearTerm giveSomePrimedTermWithCoef1() {
		if (this.primedVars.isEmpty())
			throw new RuntimeException("internal error: no primed variables to provide");
		for (Variable var : this.primedVars) {
			LinearTerm lt = this.get(var);
			if (Math.abs(lt.coeff()) == 1)
				return lt;
		}
		return null;
	}

	public StringBuffer toSB(boolean aIsEquality, boolean aOneTermOnLeft) {
		return toSB(aIsEquality, aOneTermOnLeft, Variable.ePRINT_prime);
	}
	
	public StringBuffer toSB(boolean aIsEquality, boolean aOneTermOnLeft, int primePrintType) {
		if (!aOneTermOnLeft) {
			StringBuffer sb = new StringBuffer();
			// print terms sorted by variables
			// Map<Variable, LinTerm> o_terms = new TreeMap<Variable,
			// LinTerm>(new VariableComparator());
			// o_terms.putAll(this);
			Map<Variable, LinearTerm> o_terms = new HashMap<Variable, LinearTerm>(this);

			Collection<LinearTerm> values = o_terms.values();

			LinearTerm constTerm = this.get(null);
			values.remove(constTerm);
			int c = 0;
			if (constTerm != null)
				c = constTerm.coeff() * -1;

			boolean first = true;
			for (LinearTerm term : values) {
//				if (first && primePrintType == Variable.ePRINT_p_armcPref) {
//					sb.append("0");
//					if (term.coeff()>0)
//						sb.append("+");
//				}
				sb.append(term.toSB(first, primePrintType));
				first = false;
			}

			if (aIsEquality) {
				sb.append("=");
			} else {
				if (primePrintType == Variable.ePRINT_prime)
					sb.append("<=");
				else
					sb.append("=<");
			}

			sb.append(c);

			return sb;
		} else {

			StringBuffer sb = new StringBuffer();
			Map<Variable, LinearTerm> o_terms = new HashMap<Variable, LinearTerm>(this);

			Collection<LinearTerm> values = o_terms.values();

			LinearTerm constTerm = this.get(null);
			int c = 0;
			if (constTerm != null)
				c = constTerm.coeff();// *-1;
			values.remove(constTerm);

			LinearTerm termLeft = null;
			if (!this.containsPrime()) {
				if (!values.isEmpty()) {
					termLeft = new LinearTerm(values.iterator().next());
				} else {
					termLeft = new LinearTerm(null, 0);
				}
			} else {
				LinearTerm tmp = this.giveSomePrimedTermWithCoef1();
				if (tmp == null) {
					// System.out.println("  warning: output automaton is not in the input format (a lin constraint's terms: ["+CR.collectionToString(this.terms(),
					// ",")+"]).");
					tmp = this.get(primedVars.iterator().next());
				}
				termLeft = new LinearTerm(tmp);
			}
			values.remove(termLeft);

			if (termLeft != null) {

				int multConstRight = -1;
				int coeffLeft = termLeft.coeff();
				if (coeffLeft < 0) {
					multConstRight *= -1;
					termLeft.coeff(termLeft.coeff() * -1);
				}

				sb.append(termLeft.toSB(true, primePrintType));

				if (aIsEquality)
					sb.append("=");
				else {
					if (multConstRight == -1) {
						if (primePrintType == Variable.ePRINT_prime)
							sb.append("<=");
						else
							sb.append("=<");
					} else
						sb.append(">=");
				}

				boolean first = true;
				for (LinearTerm lt : values) {
					LinearTerm tmp = new LinearTerm(lt.variable(), lt.coeff() * multConstRight);
					// t.coeff(t.coeff()*multConstRight);

					if (first && primePrintType == Variable.ePRINT_p_armcPref) {
						sb.append("0");
						if (tmp.coeff()>0)
							sb.append("+");
					}
					
					sb.append(tmp.toSB(first, primePrintType));
					first = false;
				}

				if (first || c != 0) {
					LinearTerm constT = new LinearTerm(null, c * multConstRight);
					if (first && constT.coeff()<0)
						sb.append("0");
					sb.append(constT.toSB(first, primePrintType));
				}
			}

			return sb;
		}
	}
	
	public StringBuffer toSBFAST(boolean aIsAction, boolean aIsEquality) {

		if (!aIsAction) {
			return toSB(aIsEquality, true);
		} else {
			if (primedVars.size() != 1)
				throw new RuntimeException();

			StringBuffer sb = new StringBuffer();

			Variable primedVar = primedVars.iterator().next();
			LinearTerm primedTerm = this.get(primedVar);
			LinearConstr tmp = this;
			tmp.remove(primedVar);
			if (Math.abs(primedTerm.coeff()) != 1)
				throw new RuntimeException("Action is not FAST-compatible (coeff of primed var: (" + primedTerm.coeff() + ").");

			if (primedTerm.coeff() > 0)
				tmp.transformBetweenGEQandLEQ();
			// transformBetweenGEQandLEQ(tmp);

			sb.append(primedVar.toString() + "=");

			Collection<LinearTerm> values = tmp.values();
			boolean first = true;
			for (LinearTerm term : values) {
				sb.append(term.toSB(first));
				first = false;
			}

			return sb;
		}
	}

	public IntegerFormula toJSMT(FlataJavaSMT fjsmt, String s_u, String s_p) {
		Collection<LinearTerm> values = this.values();
		Iterator<LinearTerm> iter = values.iterator();
		int size = values.size();

		if (size == 1) {
			return iter.next().toJSMT(fjsmt, s_u, s_p);
		}

		LinkedList<IntegerFormula> terms = new LinkedList<IntegerFormula>();

		while (iter.hasNext()) {
			LinearTerm t = iter.next();
			terms.add(t.toJSMT(fjsmt, s_u, s_p));
		}

		return fjsmt.getIfm().sum(terms);
	}

	// TODO: remove
	public StringBuffer toSBYices(String s_u, String s_p) {
		StringBuffer sb = new StringBuffer();
		
		Collection<LinearTerm> values = this.values();
		Iterator<LinearTerm> iter = values.iterator();
		int size = values.size();
		
		if (size == 1) {
			return iter.next().toSBYices(s_u, s_p);
		}
		
		sb.append("(+");
		//int i = 1;
		while (iter.hasNext()) {
			LinearTerm t = iter.next();
//			if (i != size) {
//				sb.append(t.toSBYices(s_u,s_p));
//			} else {
				sb.append(" "+t.toSBYices(s_u,s_p));
//			}
//			++i;
		}
		sb.append(")");
//		for (i = 1; i < size; ++i)
//			sb.append(")");

		return sb;
	}

	/**
	 * adds the specified linear term to this constraint. If this linear
	 * constraint contains a term of the same variable as the specified term, it
	 * simply updates its coefficient. Otherwise, it ads a copy of the specified
	 * term to this constraint.
	 */
	// updates reference to constraint's primed variables if necessary
	public void addLinTerm(final LinearTerm aLinTerm) {

		if (aLinTerm.coeff() == 0)
			return; // ignore zero terms

		Variable var = aLinTerm.variable();

		LinearTerm linTerm = this.get(var);
		if (linTerm == null) {
			this.put(var, new LinearTerm(aLinTerm)); // deep copy
			if (var != null) {
				if (var.type().isPrimed())
					primedVars.add(var);
				else if (var.type().isUnprimed())
					unprimedVars.add(var);
			}
		} else {
			int tmp = linTerm.coeff() + aLinTerm.coeff();
			if (tmp == 0) {
				this.remove(var);
				if (var != null) {
					if (var.type().isPrimed())
						primedVars.remove(var);
					else if (var.type().isUnprimed())
						unprimedVars.remove(var);
				}
			} else {
				linTerm.coeff(tmp);
			}
		}
	}

	/**
	 * adds copies of linear terms in the specified collection to the constraint
	 * in the same manner as method {@link #addLinTerm(LinearTerm)} does for a
	 * single term
	 */
	public void addLinTerms(final Collection<LinearTerm> aLinTerms) {
		for (LinearTerm e : aLinTerms) {
			addLinTerm(e);
		}
	}
	
	public void addLinTerms(LinearConstr other) {
		for (LinearTerm e : other.values()) {
			addLinTerm(e);
		}
	}

	public static LinearConstr FM_elimination(final LinearConstr aLess0, final LinearConstr aGreater0) {
		LinearConstr result = new LinearConstr(aLess0);
		for (LinearTerm linTerm : aGreater0.terms()) {
			LinearTerm linTerm_copy = new LinearTerm(linTerm);
			result.addLinTerm(linTerm_copy);
		}
		return result;
	}

	/**
	 * Linear terms in this constraint are interpreted as
	 * 
	 * <pre>
	 * <tt>term_1 + ... + term_n <= 0 </tt>
	 * </pre>
	 * <p>
	 * This method takes a specified constraint which contains values as if it
	 * was interpreted as
	 * 
	 * <pre>
	 * <tt>term_1 + ... + term_n >= 0</tt>
	 * </pre>
	 * 
	 * . It transforms that constraint the way it conforms to the standard
	 * interpretation. Therefore the resulting constraint will be of the form
	 * 
	 * <pre>
	 * <tt>(-linTerm_1) + ... + (-linTerm_n) <= 0</tt>
	 * </pre>.
	 * 
	 * @param aLinConstr
	 *            linear constraint to be transformed
	 */
	public static LinearConstr transformBetweenGEQandLEQ(LinearConstr aLinConstr) {
		LinearConstr ret = new LinearConstr(aLinConstr);
		Collection<LinearTerm> values = ret.values();
		for (LinearTerm term : values) {
			term.coeff(term.coeff() * -1);
		}
		return ret;
	}

	public void transformBetweenGEQandLEQ() {
		Collection<LinearTerm> values = this.values();
		for (LinearTerm term : values) {
			term.coeff(term.coeff() * -1);
		}
	}

	public List<Constr> not() {
		// not (term <= 0)   <=>   term > 0   <=>   -term < 0   <=>  -term + 1 <= 0 
		LinearConstr cret = this.times(-1);
		cret.addLinTerm(LinearTerm.constant(1));
		List<Constr> ret = new LinkedList<Constr>();
		ret.add(cret);
		return ret;
	}
	
	public void negate() {
		Collection<LinearTerm> values = this.values();
		for (LinearTerm term : values) {
			term.coeff(term.coeff() * -1);
		}
		this.addLinTerm(new LinearTerm(null, 1));
	}

	/**
	 * 
	 * @param x > 0
	 * @param y > 0
	 * @return greatest common divisor
	 */
	public static int gcd(int x, int y) {
		while (y != 0) {
			int r = x % y;
			x = y;
			y = r;
		}
		return x;
	}

	/**
	 * 
	 * @param x > 0
	 * @param y > 0
	 * @return least common multiple
	 */
	public static int lcm(int x, int y) {
		return (x * y) / gcd(x, y);
	}

	public int gcdOfVarCoefs() {
		Collection<LinearTerm> aux = new LinkedList<LinearTerm>(values());
		aux.remove(term(null));
		
		if (aux.size() == 0)
			return 1; // TODO
		
		Iterator<LinearTerm> iter = aux.iterator();

		int coeff = Math.abs(iter.next().coeff());

		if (aux.size() == 1) {
			return coeff;
		}

		int gcd = coeff;
		LinearTerm term;
		while (iter.hasNext()) {
			term = iter.next();
			coeff = Math.abs(term.coeff());
			gcd = LinearConstr.gcd(gcd, coeff);
		}
		return gcd;
	}
	
	/**
	 * @return <ul>
	 *         <li>-1 if this constraint contains no linear terms (tautology)
	 *         <li>otherwise, <i>greatest common divisor</i> off all
	 *         coefficients of linear terms present in this constraint
	 *         </ul>
	 */
	public int getGCD() {

		Collection<LinearTerm> terms = terms();
		Iterator<LinearTerm> iter = terms().iterator();

		if (terms.size() == 0)
			return -1; // TODO

		int coeff = Math.abs(iter.next().coeff());

		if (terms.size() == 1) {
			return coeff;
		}

		int gcd = coeff;
		LinearTerm term;
		while (iter.hasNext()) {
			term = iter.next();
			coeff = Math.abs(term.coeff());
			gcd = LinearConstr.gcd(gcd, coeff);
		}
		return gcd;
	}

	/**
	 * divides all terms by a value specified as a parameter
	 * 
	 * @param aDivisor
	 */
	private void divideTermsBy(int aDivisor) {
		Iterator<LinearTerm> iter = terms().iterator();
		LinearTerm term;
		while (iter.hasNext()) {
			term = iter.next();
			int coeff = term.coeff();
			//term.coeff(coeff / aDivisor);
			term.coeff(-CR.floor(-coeff,aDivisor));
		}
	}

	/**
	 * performs normalization of this linear constraint. This involves division
	 * of term's coefficients by their <i>greatest common divisor</i>
	 * E.g. 3(x+2y) <= 7 is simplified to x+2y <= 2
	 */
	public void normalizeByGCD() {
		//int gcd = getGCD();
		int gcd = gcdOfVarCoefs();
		normalizeBy(gcd);
	}

	public void normalizeBy(int n) {
		if (n > 1)
			divideTermsBy(n);
	}

	/**
	 * compares a constant term of this constraint and a constant term of the
	 * specified constraint.
	 * 
	 * @return <tt>true</tt> if the constant term of this constraint is higher,
	 *         <tt>false</tt> otherwise
	 */
	public boolean hasHigherConstantTerm(LinearConstr aOther) {
		LinearTerm thisConstTerm = this.get(null);
		LinearTerm otherConstTerm = aOther.get(null);
		if (thisConstTerm == null) {
			if (otherConstTerm == null) {
				return true; // same, don't care which
			} else {
				return otherConstTerm.coeff() < 0;
			}
		} else {
			if (otherConstTerm == null) {
				return thisConstTerm.coeff() >= 0;
			} else {
				return thisConstTerm.coeff() >= otherConstTerm.coeff();
			}
		}
	}

	/**
	 * checks properties (tautology, contradiction) of this constraint which can
	 * be inferred from constant term's value. Such inference is possible only
	 * if this constraint contains the only term - a constant term.
	 * Specifically, a linear constraint of the form
	 * <tt>a <= 0, where a is zero or negative integer</tt> is tautology and
	 * linear constraint of the form
	 * <tt>a <= 0, where a is positive integer</tt> is contradiction.
	 * 
	 * @return bit-coded properties
	 */
	private int constantTermProperties() {
		int prop = 0;
		int size = this.size();
		if (size == 0) {
			return LinearConstr.eTAUTOLOGY;
		} else if (size == 1) {
			LinearTerm constTerm = this.get(null);
			if (constTerm != null) {
				int coeff = constTerm.coeff();
				if (coeff > 0)
					prop |= LinearConstr.eCONTRADICTION;
				else
					prop |= LinearConstr.eTAUTOLOGY;
			}
		}
		return prop;
	}

	/**
	 * on the level of a single constraint, contradiction can be found only when
	 * a constraint contains only constant term which is contradictory - i.e. it
	 * has positive value
	 */
	public boolean isContradiction() {
		if (this.size() != 1)
			return false;
		return verimag.flata.common.CR.isMaskedWith(constantTermProperties(), eCONTRADICTION);
	}

	/**
	 * on the level of a single constraint, tautology can be found only when a
	 * constraint contains only constant term which is tautological - i.e. it
	 * has zero or negative value
	 */
	public boolean isTautology() {
		return verimag.flata.common.CR.isMaskedWith(constantTermProperties(), eTAUTOLOGY);
	}

	
	
	/**
	 * checks if this constraint is a <i>difference bound constraint (DBC)<i>
	 */
	public boolean isDBC() {
		DBC dbc = new DBC();
		return isDBC(dbc);
	}

	public boolean isDBC(DBC aDBC) {
		return isDBC(aDBC, DBC.BoundType.INT, null);
	}

	public boolean isNoVarConstr(ParamBound aParamConstr, Variable aParam) {
		int nrOfParams = (this.get(aParam) == null) ? 0 : 1;
		int nrOfVars = this.primedVars.size() + unprimedVars.size() - nrOfParams;
		
		if (nrOfVars > 0)
			return false;
		
		LinearTerm ltz = this.get(null);
		
		aParamConstr.intVal((ltz == null)? 0 : ltz.coeff());
		if (nrOfParams == 1) {
			aParamConstr.paramCoef(this.get(aParam).coeff());
		}
		
		return true;
	}
	
	public boolean isDBC(DBC aDBC, DBC.BoundType aType, Variable aParam) {

		int nrOfParams = 0;
		if (aParam != null && this.containsKey(aParam))
			nrOfParams = 1;
		
		int nrOfVars = this.primedVars.size() + unprimedVars.size() - nrOfParams;
		if (nrOfVars > 2 || nrOfVars == 0)
			return false;

		//if (nrOfVars + nrOfParams == 0)
		//	throw new RuntimeException("tautology or contradiction is present: " + this);

		LinearTerm ltz = this.get(null);
		int weight = ((ltz == null) ? 0 : -1 * ltz.coeff());

		Field dbcLabel = null;
		switch (aType) {
		case INT:
			if (nrOfParams != 0)
				return false;

			dbcLabel = DBC.BoundType.giveInt(FieldType.FINITE, weight);
			break;
		case INT_ONE_PARAM:
			if (nrOfParams > 1)
				return false;
			
			LinearTerm ltp = (nrOfParams == 0) ? null : this.get(aParam);
			int paramCoef = (ltp == null) ? 0 : -1 * ltp.coeff();
			Variable var_par = (ltp == null) ? null : ltp.variable();
			dbcLabel = DBC.BoundType.giveIntOneParam(var_par, FieldType.FINITE, weight, paramCoef);
			break;
		}

		LinearTerm lt1 = null;
		LinearTerm lt2 = null;
		for (Variable v : variables()) {
			if (v.equals(aParam))
				continue;
			if (lt1 == null)
				lt1 = this.get(v);
			else
				lt2 = this.get(v);
		}
		
		Variable v_plus = null;
		Variable v_minus = null;
		if (nrOfVars == 2) {
			int c1 = lt1.coeff();
			int c2 = lt2.coeff();

			if (c1 * c2 > 0 || Math.abs(c1 * c2) != 1)
				return false;
			
			if (c1 > 0) {
				v_plus = lt1.variable();
				v_minus = lt2.variable();
			} else {
				v_plus = lt2.variable();
				v_minus = lt1.variable();
			}
		} else if (nrOfVars == 1) { // size==maxSize-1
			int c1 = lt1.coeff();

			if (Math.abs(c1) != 1)
				return false;
			
			if (c1 > 0) {
				v_plus = lt1.variable();
			} else {
				v_minus = lt1.variable();
			}
		} else {
			throw new RuntimeException("internal error");
		}
		
//		LinearTerm lt_plus = getPlusMinus(lt1, lt2, true);
//		LinearTerm lt_minus = getPlusMinus(lt1, lt2, false);
//
//		aDBC.plus((lt_plus != null) ? lt_plus.variable() : null);
//		aDBC.minus((lt_minus != null) ? lt_minus.variable() : null);

		aDBC.plus(v_plus);
		aDBC.minus(v_minus);
		
		aDBC.label(dbcLabel);

		return true;
	}
	
	/**
	 * checks if this constrain is <i>octagonal<i>
	 */
	public boolean isOctagonal() {
		return isOctagonal(new OctConstr());
	}

	public boolean isOctagonal(OctConstr oc) {

		boolean hasConstTerm = this.containsKey(null);
		int maxSize = (hasConstTerm) ? 3 : 2;
		int size = this.size();

		if (size > maxSize)
			return false;

		if (size == maxSize - 2) {
			//throw new RuntimeException("tautology or contradiction is present: " + this);
			oc.lt1 = LinearTerm.constant(0);
			oc.lt2 = LinearTerm.constant(0);
			oc.bound = - this.constTerm();
			return true;
		}

		Set<Variable> vars = new HashSet<Variable>();
		this.variables(vars);
		Iterator<Variable> iter = vars.iterator();

		if (size == maxSize) {
			oc.lt1 = this.get(iter.next());
			oc.lt2 = this.get(iter.next());
			int c1 = oc.lt1.coeff();
			int c2 = oc.lt2.coeff();
			if (Math.abs(c1) != 1 || Math.abs(c2) != 1)
				return false;

			LinearTerm lt_b = this.get(null);
			oc.bound = (lt_b == null) ? 0 : -lt_b.coeff();

			return true;

		} else { // size==maxSize-1
			LinearTerm lt1 = this.get(iter.next());
			int c1 = lt1.coeff();

			LinearTerm lt0 = this.get(null);
			int c0 = (lt0 == null) ? 0 : lt0.coeff();
			
			int c1_abs = Math.abs(c1);
			int bound = CR.floor(-c0, c1_abs);
			
			int c1_coef = (c1<0)? -1 : 1;
			
			oc.lt1 = new LinearTerm(lt1.variable(), c1_coef);
			oc.lt2 = new LinearTerm(lt1.variable(), c1_coef);
			oc.bound = 2 * bound;

			return true;
		}
	}

	public void setLPRow(glp_prob aLP, int aInx, Variable[] aLPVars) {

		// TODO: zero elements don't have to be set (GLPK ignores them)

		// set row bounds
		LinearTerm zero_term = this.get(null);
		int lower_bound = (zero_term == null) ? 0 : zero_term.coeff();
		GLPK.glp_set_row_bnds(aLP, aInx, GLPKConstants.GLP_LO, (double) lower_bound, 0.);

		int var_count = aLPVars.length;

		// create 2 arrays (index array, value array)
		SWIGTYPE_p_int ind = GLPK.new_intArray(var_count + 1);
		SWIGTYPE_p_double val = GLPK.new_doubleArray(var_count + 1);
		for (int i = 0; i < var_count; i++) {
			LinearTerm lt = this.get(aLPVars[i]);
			GLPK.intArray_setitem(ind, i + 1, i + 1);
			if (lt == null) {
				GLPK.doubleArray_setitem(val, i + 1, 0.0);
			} else {
				GLPK.doubleArray_setitem(val, i + 1, (double) -lt.coeff());
			}
		}

		// set matrix row
		GLPK.glp_set_mat_row(aLP, aInx, var_count, ind, val);
	}

	public void substituteSelf(Variable aVar, LinearConstr eq) {
		
		LinearTerm lt_subst = this.get(aVar);

		if (lt_subst != null) {

			int c = lt_subst.coeff();
			this.removeTerm(aVar);
			for (LinearTerm lt : eq.values()) {
				this.addLinTerm(new LinearTerm(lt.variable(), c * lt.coeff()));
			}
		}
	}
	public LinearConstr giveSubstitute(Variable aVar, LinearConstr eq) {

		LinearConstr ret = new LinearConstr(this);
		LinearTerm lt_subst = this.get(aVar);

		if (lt_subst != null) {

			int c = lt_subst.coeff();
			ret.removeTerm(aVar);
			for (LinearTerm lt : eq.values()) {
				ret.addLinTerm(new LinearTerm(lt.variable(), c * lt.coeff()));
			}
		}
		return ret;
	}
	public void substitute_insitu(Variable aVar, LinearConstr eq) {
		LinearTerm ll = this.term(aVar);
		if (ll == null)
			return;
		int c = ll.coeff();
		this.removeTerm(aVar);
		for (LinearTerm lt : eq.terms()) {
			this.addLinTerm(new LinearTerm(lt.variable(),lt.coeff()*c));
		}
	}
	
	public LinearConstr substitute(Substitution s) {
		LinearConstr ret = new LinearConstr();
		
		for (LinearTerm lt : this.values()) {
			if (lt.variable() == null) {
				ret.addLinTerm(new LinearTerm(lt));
			} else {
				LinearConstr lc = s.get(lt.variable());
				if (lc == null) {
					ret.addLinTerm(new LinearTerm(lt));
				} else {
					int coeff = lt.coeff();
					for (LinearTerm ltsubst : lc.values()) {
						ret.addLinTerm(new LinearTerm(ltsubst.variable(),ltsubst.coeff()*coeff));
					}
				}
			}
		}
		
		return ret;
	}
	
	public LinearTerm removeTerm(Variable var) {
		LinearTerm ret = this.remove(var);
		if (var != null) {
			if (var.type().isPrimed())
				primedVars.remove(var);
			else if (var.type().isUnprimed())
				unprimedVars.remove(var);
		}
		return ret;
	}

	public int lcmForCoeffOf(Variable aVar, int aLCM) {
		LinearTerm lt = get(aVar);
		if (lt == null) {
			return aLCM;
		} else {
			return LinearConstr.lcm(aLCM, Math.abs(lt.coeff()));
		}
	}

	public LinearConstr normalizeCooper(Variable aVar, int aLCM) {

		LinearConstr ret = new LinearConstr(this);

		LinearTerm lt_norm = ret.term(aVar);
		if (lt_norm != null) {
			int factor = aLCM / Math.abs(lt_norm.coeff());
			for (LinearTerm lt : ret.values())
				lt.coeff(lt.coeff() * factor);
			lt_norm.coeff((lt_norm.coeff() > 0) ? 1 : -1);
		}

		return ret;
	}
	public void normalizeCooper_insitu(Variable aVar, int aLCM) {
		LinearTerm lt_norm = term(aVar);
		if (lt_norm != null) {
			int factor = aLCM / Math.abs(lt_norm.coeff());
			for (LinearTerm lt : terms())
				lt.coeff(lt.coeff() * factor);
			lt_norm.coeff((lt_norm.coeff() > 0) ? 1 : -1);
		}
	}
	
	public void multiplyWith(int c) {
		for (LinearTerm t : this.values()) {
			t.coeff(t.coeff()*c);
		}
	}
	public LinearTerm getPosTermInJustOne() {
		return getPosTermInJustOne_coeff(1);
	}
	public LinearTerm geNegTermInJustOne() {
		return getPosTermInJustOne_coeff(-1);
	}
	private LinearTerm getPosTermInJustOne_coeff(int xx) {
		LinearTerm ret = null;
		for (Variable v : this.variables()) {
			LinearTerm t = this.term(v);
			if (xx*t.coeff() > 0) {
				if (ret != null)
					return null;
				else
					ret = t;
			}
		}
		return ret;
	}

	public static enum InclContr {
		IMPLIES, ISIMPLIED, CONTRADICTORY, NEITHER;
		public boolean implies() {
			return this == IMPLIES;
		}
		public boolean isImplied() {
			return this == ISIMPLIED;
		}
		public boolean isContradictory() {
			return this == CONTRADICTORY;
		}
	}

	public InclContr implContr(LinearConstr other) {
		if (this.varCount() != other.varCount())
			return InclContr.NEITHER;

		if (!this.variables().equals(other.variables()))
			return InclContr.NEITHER;
		
		int c1 = 1;
		int c2 = 1;
		
		boolean contradCand = false;
		boolean geqFirst = false;
		int cc = 1;
		
		Iterator<Variable> iter = this.variables().iterator();
		if (iter.hasNext()) {
			Variable v = iter.next();
			LinearTerm first1 = this.get(v);
			LinearTerm first2 = other.get(v);
			if (first2 == null || first1.coeff() * first2.coeff() < 0) {
				//return InclContr.NEITHER;
				contradCand = true;
				cc = -1;
				if (first1.coeff() < 0)
					geqFirst = true;
			}
			int lcm = lcm(Math.abs(first1.coeff()), Math.abs(first2.coeff()));
			c1 = lcm / Math.abs(first1.coeff());
			c2 = lcm / Math.abs(first2.coeff());
			while (iter.hasNext()) {
				v = iter.next();
				LinearTerm lt1 = this.get(v);
				LinearTerm lt2 = other.get(v);
				if (lt2 == null || lt1.coeff() * c1 != lt2.coeff() * c2 * cc)
					return InclContr.NEITHER;
			}
		}

		LinearTerm ltz1 = this.get(null);
		LinearTerm ltz2 = other.get(null);
		int z1 = (ltz1 == null) ? 0 : ltz1.coeff();
		int z2 = (ltz2 == null) ? 0 : ltz2.coeff();

		if (contradCand) {
			if (geqFirst)
				return (z1*c1 <= -z2*c2)? InclContr.NEITHER : InclContr.CONTRADICTORY;
			else
				return (z2*c2 <= -z1*c1)? InclContr.NEITHER : InclContr.CONTRADICTORY;
		} else
			return (c1 * z1 >= c2 * z2) ? InclContr.IMPLIES : InclContr.ISIMPLIED;
	}
	
	public static Set<Variable> variables2(Collection<LinearConstr> col) {
		Set<Variable> ret = new HashSet<Variable>();
		
		for (LinearConstr lc : col)
			lc.variables(ret);
		
		return ret;
	}
	
	public Integer evaluate (Map<Variable,Integer> m) {
		int ret = 0;
		
		for (LinearTerm lt : this.values()) {
			Variable v = lt.variable();
			int c = lt.coeff();
			if (v == null) {
				ret += c;
			} else {
				Integer val = m.get(v);
				// if a variable 'v' is unassigned, it's value can be arbitrary and hence 
				// the valuation of the linear constraint can be arbitrary
				if (val == null)
					return null;
				ret += c*val;
			}
		}
		
		return ret;
	}
	
	public LinearConstr times(int c) {
		LinearConstr ret = new LinearConstr();
		for (LinearTerm lt1 : this.values()) {
			ret.addLinTerm(lt1.times(c));
		}
		return ret;
	}
	public LinearConstr times(LinearConstr other) {
		LinearConstr ret = new LinearConstr();
		for (LinearTerm lt1 : this.values()) {
			for (LinearTerm lt2 : other.values()) {
				LinearTerm lt = lt1.times(lt2);
				if (lt == null) {
					return null;
				} else {
					ret.addLinTerm(lt);
				}
			}
		}
		return ret;
	}
	public LinearConstr plus(LinearConstr other) {
		LinearConstr ret = new LinearConstr(this);
		ret.addLinTerms(other);
		return ret;
	}
	public LinearConstr minus(LinearConstr other) {
		LinearConstr ret = new LinearConstr(this);
		LinearConstr op2 = other.times(-1);
		ret.addLinTerms(op2);
		return ret;
	}
	public LinearConstr un_minus() {
		return this.times(-1);
	}
	
	public static LinearConstr createConst(int val) {
		LinearConstr ret = new LinearConstr();
		ret.addLinTerm(LinearTerm.constant(val));
		return ret;
	}
	
	public Expr toNTS() {
		if (this.size() == 0) {
			return ASTWithoutToken.litBool(true);
		}
		
		Iterator<LinearTerm> i = this.values().iterator();
		Expr aux = i.next().toNTS();
		while (i.hasNext()) {
			aux = ASTWithoutToken.exPlus(aux, i.next().toNTS());
		}
		
		return ASTWithoutToken.exLeq(aux, ASTWithoutToken.litInt(0));
	}
	private void eraseConstTerm() {
		this.removeTerm(null);
	}
	public LinearConstr copyWithoutConstTerm() {
		LinearConstr ret = this.copy();
		ret.eraseConstTerm();
		return ret;
	}
	
	public boolean syntaxEquals(LinearConstr aOther, boolean ignoreConst) {
		if (!ignoreConst && this.constTerm() != aOther.constTerm()) {
			return false;
		}
		Set<Variable> vars = new HashSet<Variable>(this.keySet());
		vars.addAll(aOther.keySet());
		vars.remove(null);
		for (Variable v : vars) {
			LinearTerm t1 = this.term(v);
			LinearTerm t2 = aOther.term(v);
			if (t1 == null || t2 == null || !t1.equals(t2)) {
				return false;
			}
		}
		return true;
	}
	
	public LinearConstr leq(LinearConstr that) { return this.minus(that); }
	public LinearConstr lt(LinearConstr that) { return this.minus(that).plus(LinearConstr.createConst(1)); }
	public LinearConstr geq(LinearConstr that) { return that.minus(this); }
	public LinearConstr gt(LinearConstr that) { return that.minus(this).plus(LinearConstr.createConst(1)); }
	
	public static LinearConstr leq(LinearTerm t1, LinearTerm t2) {
		return(new LinearConstr(t1)).leq(new LinearConstr(t2));
	}
	public static LinearConstr lt(LinearTerm t1, LinearTerm t2) {
		return(new LinearConstr(t1)).lt(new LinearConstr(t2));
	}
	public static LinearConstr geq(LinearTerm t1, LinearTerm t2) {
		return(new LinearConstr(t1)).geq(new LinearConstr(t2));
	}
	public static LinearConstr gt(LinearTerm t1, LinearTerm t2) {
		return(new LinearConstr(t1)).gt(new LinearConstr(t2));
	}
	
}
