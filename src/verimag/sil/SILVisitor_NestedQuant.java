package verimag.sil;

import nts.interf.*;
import nts.interf.base.*;
import nts.interf.expr.*;
import nts.parser.*;

import java.util.*;

/*
 * visitor that implements removal of certain patterns of nested quantification:
 * e.g.
 *   forall i . F1 && (F2 && forall j . F3)
 * becomes
 *   (forall i . F1 && (F2 && true)) && (forall j . F3)
 * under the condition that i is not free in F3
 * 
 * More precisely, the implemented transformation is
 *   pattern
 *     {forall,exists,&&}*.{&&}.{forall/exists j}{F}
 *   describing a path in the AST is transformed to 
 *     ( {forall,exists,&&}*.{&&}.{true} ) && (forall/exists j . F)
 *   and similarly, pattern
 *     {forall,exists,||}*.{||}.{forall/exists j}{F}
 *   is transformed to 
 *     ( {forall,exists,||}*.{||}.{false} ) || (forall/exists j . F)
 *   both under the conditions that no quantified variable from the pattern {forall,exists,&&}* is free in F
 *       
 */
public class SILVisitor_NestedQuant implements IVisitor {

	private static void errNoSupport(Object o) {
		System.err.println("'"+o.getClass().getName()+"' not supported.");
		System.exit(1);
	}
	
	private VarTable vt;
	
	public SILVisitor_NestedQuant(VarTable aVT) {
		vt = aVT;
	}
	
	public Expr getResult() {
		return (Expr)stack.pop();
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
	
	private Deque<IExpr> stack = new LinkedList<IExpr>();
	
	// stack of quantifiers
	private Deque<IExprQ> q_stack = new LinkedList<IExprQ>();
	//
	private Deque<Boolean> stack_and_or = new LinkedList<Boolean>();
	//
	private List<IExpr> move = new LinkedList<IExpr>();
	private boolean moveTypeIsAnd;
		
	private boolean compatible(Boolean b) {
		for (Boolean bb : stack_and_or) {
			if (bb != b)
				return false;
		}
		return true;
	}
	private boolean chainAndOr() {
		if (stack_and_or.isEmpty())
			return true;
		boolean b = stack_and_or.peek();
		assert(compatible(b));
		return b;
	}

	// boolean expressions
	
	@Override
	public void visit(IExprNot e) {
		if (q_stack.isEmpty()) {
			e.operand().accept(this);
			((ExNot)e).operand((Expr)stack.pop());
			stack.push(e);
		} else {
			// nop
			stack.push(e);
		}
	}

	private void visitBin(IExprBin e) {
		e.operand1().accept(this);
		e.operand2().accept(this);
		
		((OpBin)e).operand2((Expr)stack.pop());
		((OpBin)e).operand1((Expr)stack.pop());
		
		stack.push(e);
	}
	
	private void visitAndOr(IExprBin e, boolean isAnd) {
		boolean isQ = q_stack.isEmpty();
		if (!isQ && !compatible(isAnd)) {
			stack.push(e);
		} else {
			moveTypeIsAnd = isAnd;
			stack_and_or.push(isAnd);
			
			visitBin(e);
		}
	}
	@Override
	public void visit(IExprAnd e) {
		visitAndOr(e,true);
	}

	@Override
	public void visit(IExprOr e) {
		visitAndOr(e,false);
	}
	
	@Override
	public void visit(IExprImpl e) {
		if (q_stack.isEmpty()) {
			visitBin(e);
		} else {
			// nop
			stack.push(e);
		}
	}

	@Override
	public void visit(IExprEquiv e) {
		if (q_stack.isEmpty()) {
			visitBin(e);
		} else {
			// nop
			stack.push(e);
		}
	}
		
	private boolean test(IExpr e) {
		if (e instanceof IExprQ) {
			IExprQ q = (IExprQ)e;
			
			SILVisitor_CollectVars v = new SILVisitor_CollectVars();
			if (q instanceof IExprForall) {
				v.visit((IExprForall)e);
			} else {
				v.visit((IExprExists)e);
			}
			
			Set<IVarTableEntry> set = new HashSet<IVarTableEntry>(vt.innermost());
			set.addAll(q.varTable().innermost());
			
			if (set.containsAll(v.getResult())) {
				return true;
			}
		}
		return false;
	}
	private void visitQ(IExprQ e) {
		boolean bTop = q_stack.isEmpty();
		if (!bTop && test(e.operand())) {
			boolean b = chainAndOr();
			stack.push(ASTWithoutToken.litBool(b));
			move.add(e.operand());
		} else {
			q_stack.push(e);
			e.operand().accept(this);
			((Quantifier)e).setExpr((Expr)stack.pop());
			if (!bTop) {
				stack.push(e);
			} else {
				Expr aux = (Expr)e;
				for (IExpr ee : move) {
					if (moveTypeIsAnd) {
						aux = ASTWithoutToken.exAnd(aux, (Expr)ee);
					} else {
						aux = ASTWithoutToken.exOr(aux, (Expr)ee);
					}
				}
				move.clear();
				stack.push(aux);
			}
			q_stack.pop();
		}
	}
	
	@Override
	public void visit(IExprExists e) {
		visitQ(e);
	}
	@Override
	public void visit(IExprForall e) {
		visitQ(e);
	}

	// relational operators
	
	@Override
	public void visit(IExprEq e) {
		stack.push(e);
	}
	
	@Override
	public void visit(IExprNeq e) {
		stack.push(e);
	}

	@Override
	public void visit(IExprLeq e) {
		stack.push(e);
	}

	@Override
	public void visit(IExprLt e) {
		stack.push(e);
	}

	@Override
	public void visit(IExprGeq e) {
		stack.push(e);
	}

	@Override
	public void visit(IExprGt e) {
		stack.push(e);
	}
	
	
	// arithmetic operators
	
	@Override
	public void visit(IExprMult e) {
		assert(false);
	}
	
	@Override
	public void visit(IExprRemainder e) {
		assert(false);
	}

	@Override
	public void visit(IExprDivide e) {
		assert(false);
	}

	@Override
	public void visit(IExprPlus e) {
		assert(false);
	}

	@Override
	public void visit(IExprMinus e) {
		assert(false);
	}

	@Override
	public void visit(IExprUnaryMinus e) {
		assert(false);
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
		assert(false);
	}

	@Override
	public void visit(IAccessIndexed e) {
		assert(false);
	}

	@Override
	public void visit(IAccessMulti e) {
		errNoSupport(e);
	}

	@Override
	public void visit(ILitBool e) {
		assert(false);
	}

	@Override
	public void visit(ILitInt e) {
		assert(false);
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

