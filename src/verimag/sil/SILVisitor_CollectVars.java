package verimag.sil;

import nts.interf.*;
import nts.interf.base.*;
import nts.interf.expr.*;

import java.util.*;

// collects referenced and quantified variables in the actual expression
public class SILVisitor_CollectVars implements IVisitor {

	private static void errNoSupport(Object o) {
		System.err.println("'"+o.getClass().getName()+"' not supported.");
		System.exit(1);
	}
	
	private Set<IVarTableEntry> vars = new HashSet<IVarTableEntry>();
	public Set<IVarTableEntry> getResult() { return vars; }
	
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
		vars.addAll(e.varTable().innermostUnprimed());
		e.operand().accept(this);
	}
	@Override
	public void visit(IExprForall e) {
		vars.addAll(e.varTable().innermostUnprimed());
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
		vars.add(e.var());
	}

	@Override
	public void visit(IAccessIndexed e) {
		vars.add(e.var());
		for (IExpr ee : e.indices()) {
			ee.accept(this);
		}
	}

	@Override
	public void visit(IAccessMulti e) {
		errNoSupport(e);
	}

	@Override
	public void visit(ILitBool e) {
		// nop
	}

	@Override
	public void visit(ILitInt e) {
		// nop
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

