package verimag.sil;

import nts.interf.*;
import nts.interf.base.*;
import nts.interf.expr.*;
import nts.parser.*;

import java.util.*;

public class SILVisitor_NestedAndQuantifiedIndexing implements IVisitor {

	private static void errNoSupport(Object o) {
		System.err.println("'"+o.getClass().getName()+"' not supported.");
		System.exit(1);
	}
	private static void errNestedArrayAccess(Expr e) {
		System.err.println("Nested array access not supported: "+Common.at(e.token())+".");
		System.exit(1);
	}
	private static void errMultiIndex(IExpr e) {
		System.err.println("Syntax error at: "+Common.at((Expr)e)+".");
		System.exit(1);
	}
	public void enforcePresburger() {
		if (arr_access_quantified.size()>0) {
			System.err.println("An array is indexed using a quantified variable: "+Common.at(arr_access_quantified.get(0).token())+".");
			System.exit(1);
		}
	}
	public void enforceArray(IExpr e) {
		// enforce a unique quantifier in the subformula
		if (q_var.size()>1) {
			System.err.println("Array forall property is mixed with Presurger formula: "+Common.at((Expr)e)+".");
			System.exit(1);
		}
		// enforce that all the indexes are quantified
		if (!scalar_access_not_quantified.isEmpty()) {
			System.err.println("Indexing with scalars in array forall properties is currently not supported: "+Common.at((Expr)scalar_access_not_quantified.get(0))+".");
			System.exit(1);
		}
	}
	/*
	 *  This visitor:
	 *    -- checks that no nested array indexing appears
	 *    -- collects the set of array accesses with quantified indexing
	 *  
	 *  visitor works in two modes: 
	 *   (checkAccessBasic == false) -- not inside an index expression (e.g. t in a[t])
	 *   (checkAccessBasic == true) -- inside an index expression
	 *      -- in this mode, check that no nested array access occurs (e.g. a[b[t]])
	 *         and that no variable is quantified
	 */
	private boolean checkIndexTerm = false; 
	
	// top-level array access a[term] where some variable in 'term' is quantified
	private List<AccessIndexed> arr_access_quantified = new LinkedList<AccessIndexed>();
	// top-level array access a[term] where no variable in 'term' is quantified
	private List<AccessIndexed> arr_access_not_quantified = new LinkedList<AccessIndexed>();
	
	// (not)quantified basic accesses in the index expression (used in the second mode)
	private List<AccessBasic> scalar_access_quantified = new LinkedList<AccessBasic>();
	private List<AccessBasic> scalar_access_not_quantified = new LinkedList<AccessBasic>();
	
	/* the set of quantified variables 
	 *  -- stored globally since the NTSVisitor ensures that quantified variables don't shadow the outer scope
	 *       e.g. (forall i . a[j] .. ) && ( forall j . a[i] .. ) cannot happen for this reason
	 */ 
	private Set<IVarTableEntry> q_var = null;
	
	// used internally: upon visiting an array access a[term], 
	//   a new visitor for term is created which checks that no quantified variable appears in term 
	private SILVisitor_NestedAndQuantifiedIndexing(boolean b, Set<IVarTableEntry> aS) {
		checkIndexTerm = b;
		q_var = aS;
	}
	public SILVisitor_NestedAndQuantifiedIndexing() {
		this(false,new HashSet<IVarTableEntry>());
	}
	
	public boolean hasQuantifiedIndexing() {
		return !arr_access_quantified.isEmpty();
	}
	public boolean hasQuantifiedBasicAccess() {
		return !scalar_access_quantified.isEmpty();
	}


	@Override
	public void visit(INTS e) {
		errNoSupport(e);
	}
		
	@Override
	public void visit(ISubsystem e) {
		errNoSupport(e);
	}

	@Override
	public void visit(IAnnotations e) {
		errNoSupport(e);
	}

	@Override
	public void visit(IControlState e) {
		errNoSupport(e);
	}

	@Override
	public void visit(ITransition e) {
		errNoSupport(e);
	}
	
	@Override
	public void visit(ICall e) {
		errNoSupport(e);
	}
	
	@Override
	public void visit(IVarTable e) {
		// not used
	}

	@Override
	public void visit(IVarTableEntry e) {
		// not used
	}

	@Override
	public void visit(IType e) {
		// not used
	}

	
	// boolean expressions
	
	@Override
	public void visit(IExprNot e) {
		accept(e);
	}

	@Override
	public void visit(IExprAnd e) {
		accept(e);
	}

	@Override
	public void visit(IExprOr e) {
		accept(e);
	}

	@Override
	public void visit(IExprImpl e) {
		accept(e);
	}

	@Override
	public void visit(IExprEquiv e) {
		accept(e);
	}
	
	@Override
	public void visit(IExprExists e) {
		this.q_var.addAll(e.varTable().innermostUnprimed());
		e.operand().accept(this);
	}
	@Override
	public void visit(IExprForall e) {
		this.q_var.addAll(e.varTable().innermostUnprimed());
		e.operand().accept(this);
	}

	// relational operators
	
	@Override
	public void visit(IExprEq e) {
		accept(e);
	}

	@Override
	public void visit(IExprNeq e) {
		accept(e);
	}

	@Override
	public void visit(IExprLeq e) {
		accept(e);
	}

	@Override
	public void visit(IExprLt e) {
		accept(e);
	}

	@Override
	public void visit(IExprGeq e) {
		accept(e);
	}

	@Override
	public void visit(IExprGt e) {
		accept(e);
	}

	private void accept(IExprUn e) {
		e.operand().accept(this);
	}
	private void accept(IExprBin e) {
		e.operand1().accept(this);
		e.operand2().accept(this);
	}
	
	// arithmetic operators
	
	@Override
	public void visit(IExprMult e) {
		accept(e);
	}
	
	@Override
	public void visit(IExprRemainder e) {
		accept(e);
	}

	@Override
	public void visit(IExprDivide e) {
		accept(e);
	}

	@Override
	public void visit(IExprPlus e) {
		accept(e);
	}

	@Override
	public void visit(IExprMinus e) {
		accept(e);
	}

	@Override
	public void visit(IExprUnaryMinus e) {
		accept(e);
	}

	@Override
	public void visit(IExprArraySize e) {
		errNoSupport(e);
	}

	@Override
	public void visit(IExprList e) {
		errNoSupport(e);
	}
	
	@Override
	public void visit(IAccessBasic e) {
		if (checkIndexTerm) {
			if (q_var.contains(e.var())) {
				scalar_access_quantified.add((AccessBasic)e);
			} else {
				scalar_access_not_quantified.add((AccessBasic)e);
			}
		}
	}

	@Override
	public void visit(IAccessIndexed e) {
		if (checkIndexTerm) {
			errNestedArrayAccess((AccessIndexed)e);
		}
		SILVisitor_NestedAndQuantifiedIndexing v = new SILVisitor_NestedAndQuantifiedIndexing(true,this.q_var);
		if (e.indices().size()>1) {
			errMultiIndex(e);
		}
		e.indices().get(0).accept(v);
		if (v.hasQuantifiedBasicAccess()) {
			scalar_access_quantified.addAll(v.scalar_access_quantified);
			scalar_access_not_quantified.addAll(v.scalar_access_not_quantified);
			arr_access_quantified.add((AccessIndexed)e);
		} else {
			arr_access_not_quantified.add((AccessIndexed)e);
		}
	}

	@Override
	public void visit(IAccessMulti e) {
		errNoSupport(e);
	}

	@Override
	public void visit(ILitBool e) {
		//
	}

	@Override
	public void visit(ILitInt e) {
		//
	}

	@Override
	public void visit(ILitReal e) {
		errNoSupport(e);
	}

	@Override
	public void visit(IHavoc e) {
		errNoSupport(e);
	}
	
}

