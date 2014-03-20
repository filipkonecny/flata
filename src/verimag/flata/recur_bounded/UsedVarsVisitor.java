package verimag.flata.recur_bounded;

import java.util.*;

import nts.interf.*;
import nts.interf.base.*;
import nts.interf.expr.*;

public class UsedVarsVisitor implements IVisitor {

	private Set<IVarTableEntry> s;
	
	public UsedVarsVisitor() {
		s = new HashSet<IVarTableEntry>();
	}
	public Set<IVarTableEntry> getResult() {
		return s;
	}
	
	public static Set<IVarTableEntry> callActParam(ICall aC) {
		UsedVarsVisitor visitor = new UsedVarsVisitor();
		for (IExpr e : aC.actualParameters()) {
			e.accept(visitor);
		}
		return visitor.getResult();
	}
	public static Set<IVarTableEntry> callRetParam(ICall aC) {
		UsedVarsVisitor visitor = new UsedVarsVisitor();
		for (IExpr e : aC.returnVars()) {
			e.accept(visitor);
		}
		return visitor.getResult();
	}
	
	@Override
	public void visit(INTS e) {
		throw new RuntimeException();
	}

	@Override
	public void visit(ISubsystem e) {
		throw new RuntimeException();
	}

	@Override
	public void visit(IAnnotations e) {
		throw new RuntimeException();
	}

	@Override
	public void visit(IControlState e) {
		throw new RuntimeException();
	}

	@Override
	public void visit(ITransition e) {
		throw new RuntimeException();
	}

	@Override
	public void visit(ICall e) {
		for (IExpr ee : e.actualParameters()) {
			ee.accept(this);
		}
		for (IExpr ee : e.returnVars()) {
			ee.accept(this);
		}
	}

	@Override
	public void visit(IVarTable e) {
		throw new RuntimeException();
	}

	@Override
	public void visit(IVarTableEntry e) {
		throw new RuntimeException();
	}

	@Override
	public void visit(IType e) {
		throw new RuntimeException();
	}

	private void visit_base(IExprUn e) {
		e.operand().accept(this);
	}
	private void visit_base(IExprBin e) {
		e.operand1().accept(this);
		e.operand2().accept(this);
	}
	private void visit_base(IExprQ e) {
		e.operand().accept(this);
	}
	
	@Override
	public void visit(IExprNot e) { visit_base(e); }

	@Override
	public void visit(IExprAnd e) { visit_base(e); }

	@Override
	public void visit(IExprOr e) { visit_base(e); }

	@Override
	public void visit(IExprImpl e) { visit_base(e); }

	@Override
	public void visit(IExprEquiv e) { visit_base(e); }

	@Override
	public void visit(IExprExists e) { visit_base(e); }

	@Override
	public void visit(IExprForall e) { visit_base(e); }

	@Override
	public void visit(IExprEq e) { visit_base(e); }

	@Override
	public void visit(IExprNeq e) { visit_base(e); }

	@Override
	public void visit(IExprLeq e) { visit_base(e); }

	@Override
	public void visit(IExprLt e) { visit_base(e); }

	@Override
	public void visit(IExprGeq e) { visit_base(e); }

	@Override
	public void visit(IExprGt e) { visit_base(e); }

	@Override
	public void visit(IExprMult e) { visit_base(e); }

	@Override
	public void visit(IExprRemainder e) { visit_base(e); }

	@Override
	public void visit(IExprDivide e) { visit_base(e); }

	@Override
	public void visit(IExprPlus e) { visit_base(e); }

	@Override
	public void visit(IExprMinus e) { visit_base(e); }

	@Override
	public void visit(IExprUnaryMinus e) { visit_base(e); }

	@Override
	public void visit(IExprArraySize e) {
		e.access().accept(this);
	}

	@Override
	public void visit(IExprList e) {
		for (IExpr ee : e.expressions()) {
			ee.accept(this);
		}
	}

	@Override
	public void visit(IAccessBasic e) {
		s.add(e.var());
	}

	@Override
	public void visit(IAccessIndexed e) {
		s.add(e.var());
		for (IExpr ee : e.indices()) {
			ee.accept(this);
		}
	}

	@Override
	public void visit(IAccessMulti e) {
		s.add(e.var());
		for (IExpr ee : e.singleInxs()) {
			ee.accept(this);
		}
		for (IExpr ee : e.multiInxs()) {
			ee.accept(this);
		}
	}

	@Override
	public void visit(ILitBool e) {
		// noop
	}

	@Override
	public void visit(ILitInt e) {
		// noop
	}

	@Override
	public void visit(ILitReal e) {
		// noop
	}

	@Override
	public void visit(IHavoc e) {
		for (IExpr ee : e.vars()) {
			ee.accept(this);
		}
	}

}
