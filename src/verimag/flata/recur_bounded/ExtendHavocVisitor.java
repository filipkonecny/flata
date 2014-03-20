package verimag.flata.recur_bounded;

import java.util.*;

import nts.interf.*;
import nts.interf.base.*;
import nts.interf.expr.*;
import nts.parser.*;

public class ExtendHavocVisitor implements IVisitor {

	private boolean found;
	private List<String> list; 
	public static boolean extend(IExpr e, List<String> aList) {
		ExtendHavocVisitor visitor = new ExtendHavocVisitor();
		visitor.list = aList;
		visitor.found = false;
		e.accept(visitor);
		return visitor.found;
	}
	
	@Override
	public void visit(IHavoc e) {
		found = true;
		for (String s : list) {
			((Havoc)e).addHavocToken(s);
		}
	}
	
	private void error() {
		throw new RuntimeException();
	}
	
	@Override
	public void visit(INTS e) { error(); }

	@Override
	public void visit(ISubsystem e) { error(); }

	@Override
	public void visit(IAnnotations e) { error(); }

	@Override
	public void visit(IControlState e) { error(); }

	@Override
	public void visit(ITransition e) { error(); }

	@Override
	public void visit(ICall e) { error(); }

	@Override
	public void visit(IVarTable e) { error(); }

	@Override
	public void visit(IVarTableEntry e) { error(); }

	@Override
	public void visit(IType e) { error(); }

	private void visit_base(IExprUn e) {
		e.operand().accept(this);
	}
	private void visit_base(IExprBin e) {
		e.operand1().accept(this);
		e.operand2().accept(this);
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
	public void visit(IExprExists e) {  }

	@Override
	public void visit(IExprForall e) {  }

	@Override
	public void visit(IExprEq e) {  }

	@Override
	public void visit(IExprNeq e) {  }

	@Override
	public void visit(IExprLeq e) {  }

	@Override
	public void visit(IExprLt e) {  }

	@Override
	public void visit(IExprGeq e) {  }

	@Override
	public void visit(IExprGt e) {  }

	@Override
	public void visit(IExprMult e) {  }

	@Override
	public void visit(IExprRemainder e) {  }

	@Override
	public void visit(IExprDivide e) {  }

	@Override
	public void visit(IExprPlus e) {  }

	@Override
	public void visit(IExprMinus e) {  }

	@Override
	public void visit(IExprUnaryMinus e) {  }

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
	public void visit(IAccessBasic e) {  }

	@Override
	public void visit(IAccessIndexed e) {  }

	@Override
	public void visit(IAccessMulti e) {  }

	@Override
	public void visit(ILitBool e) {  }

	@Override
	public void visit(ILitInt e) {  }

	@Override
	public void visit(ILitReal e) {  }

}
