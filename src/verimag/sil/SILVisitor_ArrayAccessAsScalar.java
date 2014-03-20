package verimag.sil;

import nts.interf.*;
import nts.interf.base.*;
import nts.interf.expr.*;
import nts.parser.*;

import java.util.*;

/*
 * The visitor replaces array accesses a[scalar+const] with fresh scalars p
 * and stores the substitution in a map. 
 */
public class SILVisitor_ArrayAccessAsScalar implements IVisitor {

	private static void errNoSupport(Object o) {
		System.err.println("'"+o.getClass().getName()+"' not supported.");
		System.exit(1);
	}
	
	private SymbolTable st;
	private Deque<Expr> stack = new LinkedList<Expr>();
	private Map<AccessBasic,AccessIndexed> m = new HashMap<AccessBasic,AccessIndexed>();
	
	public SILVisitor_ArrayAccessAsScalar(SymbolTable aST) {
		st = aST;
	}
	
	public Expr getResult() { return stack.pop(); }
	public Map<AccessBasic,AccessIndexed> getSubst() { return m; }
	
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
		stack.push(ASTWithoutToken.exNot(stack.pop()));
	}

	@Override
	public void visit(IExprAnd e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exAnd(e1,e2));
	}

	@Override
	public void visit(IExprOr e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exOr(e1,e2));
	}

	@Override
	public void visit(IExprImpl e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exImpl(e1,e2));
	}

	@Override
	public void visit(IExprEquiv e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exEquiv(e1,e2));
	}
	
	@Override
	public void visit(IExprExists e) {
		e.operand().accept(this);
		stack.push(ASTWithoutToken.exExists((nts.parser.VarTable)e.varTable(), stack.pop()));
	}
	@Override
	public void visit(IExprForall e) {
		e.operand().accept(this);
		stack.push(ASTWithoutToken.exForall((nts.parser.VarTable)e.varTable(), stack.pop()));
	}

	// relational operators
	
	@Override
	public void visit(IExprEq e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exEq(e1,e2));
	}

	@Override
	public void visit(IExprNeq e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exNeq(e1,e2));
	}

	@Override
	public void visit(IExprLeq e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exLeq(e1,e2));
	}

	@Override
	public void visit(IExprLt e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exLt(e1,e2));
	}

	@Override
	public void visit(IExprGeq e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exGeq(e1,e2));
	}

	@Override
	public void visit(IExprGt e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exGt(e1,e2));
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
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exMult(e1,e2));
	}
	
	@Override
	public void visit(IExprRemainder e) {
		// op1 % op2
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exRemainder(e1,e2));
	}

	@Override
	public void visit(IExprDivide e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exDivide(e1,e2));
	}

	@Override
	public void visit(IExprPlus e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exPlus(e1,e2));
	}

	@Override
	public void visit(IExprMinus e) {
		accept(e);
		Expr e2 = stack.pop();
		Expr e1 = stack.pop();
		stack.push(ASTWithoutToken.exMinus(e1,e2));
	}

	@Override
	public void visit(IExprUnaryMinus e) {
		accept(e);
		stack.push(ASTWithoutToken.exUnaryMinus(stack.pop()));
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
		stack.push((nts.parser.AccessBasic)e);
	}

	@Override
	public void visit(IAccessIndexed e) {
		String fresh = st.freshScalar();
		AccessBasic a = ASTWithoutToken.accessBasic(fresh);
		m.put(a, (AccessIndexed)e);
		stack.push(a);
	}

	@Override
	public void visit(IAccessMulti e) {
		errNoSupport(e);
	}

	@Override
	public void visit(ILitBool e) {
		stack.push((LitBool)e);
	}

	@Override
	public void visit(ILitInt e) {
		stack.push((LitInt)e);
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

