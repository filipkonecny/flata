package verimag.sil;

import nts.interf.*;
import nts.interf.base.*;
import nts.interf.expr.*;
import nts.parser.*;

import java.util.*;

import verimag.flata.presburger.*;

public class SILVisitor_ArrayPropLhs implements IVisitor {

	// //////////////////////////////////
	// //////////////////////////////////
	private static void errUnexpOp(IExpr o) {
		System.err.println("Unexpected operator: '"+o.getClass().getName()+"' "+Common.at(((Expr)o).token())+".");
		System.exit(1);
	}
	private void errNoSupport(Object o) {
		System.err.println("'"+o.getClass().getName()+"' not supported.");
		System.exit(1);
	}
	private void errNotPresburger() {
		System.err.println("Flata supports only Presburger expressions.");
		System.exit(1);
	}
	private static void errNonOctagonalRange(IExpr e) {
		System.err.println("Forall array property at "+Common.at((Expr)e)+" has non-octagonal guard.");
		System.exit(1);
	}
	private static void errUniqueBound(IExpr e) {
		System.err.println("The guard of the forall array property at "+Common.at((Expr)e)+" must have at most one lower bound and at most one upper bound.");
		System.exit(1);
	}
	
	private VariablePool currentPool = null;

	public void parseSILExprInit(VariablePool aVP) {
		currentPool = aVP;
	}

	private LinearConstr low,up;
	
	// checks that there is at most one lower and at most one upper bound
	// and that the bounds are octagonal
	public void finalizeAndCheck(IExpr root, Variable index) {
		LinearRel rel = stack_rel.pop();
		
		if (!rel.isOctagon()) {
			errNonOctagonalRange(root);
		}
		
		for (LinearConstr c : rel.constraints()) {
			LinearTerm t = c.removeTerm(index);
			if (t == null || (t.coeff()<0 && low!=null) || (t.coeff()>0 && up!=null))
				errUniqueBound(root);
			if (t != null) {
				if (t.coeff()<0) {
					low = c;
				} else {
					up = c;
				}
			}
		}
		if (up != null) {
			up.transformBetweenGEQandLEQ();
		}
	}
	public LinearConstr getLower() { return low; }
	public LinearConstr getUpper() { return up; }

	
	
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
		errUnexpOp(e);
	}

	@Override
	public void visit(IExprAnd e) {
		accept(e);
		stack_rel.push(stack_rel.pop().and(stack_rel.pop()));
	}

	@Override
	public void visit(IExprOr e) {
		errUnexpOp(e);
	}

	@Override
	public void visit(IExprImpl e) {
		errUnexpOp(e);
	}

	@Override
	public void visit(IExprEquiv e) {
		errUnexpOp(e);
	}

	// quantifiers
	
	@Override
	public void visit(IExprExists e) {
		errUnexpOp(e);
	}

	@Override
	public void visit(IExprForall e) {
		errUnexpOp(e);
	}

	// relational operators
	
	private Deque<LinearRel> stack_rel = new LinkedList<LinearRel>();

	// doesn't modify arguments
	private LinearRel leq(LinearConstr op1, LinearConstr op2) {
		LinearConstr c = new LinearConstr(op1);
		c = c.minus(op2);
		return new LinearRel(c);
	}
	private LinearRel eq(LinearConstr op1, LinearConstr op2) {
		LinearConstr c = new LinearConstr(op1);
		c = c.minus(op2);
		LinearRel lr = new LinearRel(c);
		lr.addConstraint(LinearConstr.transformBetweenGEQandLEQ(c));
		return lr;
	}
	// doesn't modify arguments
	private void pushLeq(LinearConstr op1, LinearConstr op2) {
		LinearRel lr = leq(op1,op2);
		stack_rel.push(lr);
	}
	private void pushEq(LinearConstr op1, LinearConstr op2) {
		LinearRel lr = eq(op1,op2);
		stack_rel.push(lr);
	}
	
	
	@Override
	public void visit(IExprEq e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		// o1 = o2  <=>  o1 <= o2 /\ o2 <= o1
		pushEq(op1, op2);
	}

	@Override
	public void visit(IExprNeq e) {
		errUnexpOp(e);
	}

	@Override
	public void visit(IExprLeq e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		// o1 <= o2
		pushLeq(op1, op2);
	}

	@Override
	public void visit(IExprLt e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		// o1 < o2  <=>  o1 <= o2 - 1
		op2.addLinTerm(LinearTerm.constant(-1));
		pushLeq(op1, op2);
	}

	@Override
	public void visit(IExprGeq e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		// o1 >= o2  <=>  o2 <= o1
		pushLeq(op2, op1);
	}

	@Override
	public void visit(IExprGt e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		// o1 > o2  <=>  o2 <= o1 - 1
		op1.addLinTerm(LinearTerm.constant(-1));
		pushLeq(op2, op1);
	}
	
	
	private Deque<LinearConstr> stack_arith = new LinkedList<LinearConstr>();

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
		LinearConstr r = stack_arith.pop().times(stack_arith.pop());
		if (r == null) {
			errNotPresburger();
		}
		stack_arith.push(r);
	}

	@Override
	public void visit(IExprRemainder e) {
		errUnexpOp(e);
	}

	@Override
	public void visit(IExprDivide e) {
		errNoSupport(e);
	}

	@Override
	public void visit(IExprPlus e) {
		accept(e);
		stack_arith.push(stack_arith.pop().plus(stack_arith.pop()));
	}

	@Override
	public void visit(IExprMinus e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		stack_arith.push(op1.minus(op2));
	}

	@Override
	public void visit(IExprUnaryMinus e) {
		accept(e);
		stack_arith.push(stack_arith.pop().un_minus());
	}

	@Override
	public void visit(IExprArraySize e) {
		errNoSupport(e);
	}

	@Override
	public void visit(IExprList e) {
		errNoSupport(e);
	}

	
	// to deal with quantified variables
	private Variable getVar(String aName) {
		if (currentPool.isDeclared(aName)) {
			return currentPool.giveVariable(aName);
		} else {
			if (!VariablePool.getSpecialPool().isDeclared(aName)) {
				throw new RuntimeException("internal error: undeclared variable "+aName);
			} else {
				return VariablePool.createSpecial(aName);
			}
		}
	}
	@Override
	public void visit(IAccessBasic e) {
		
		Variable v = getVar(e.var().name());
		LinearConstr c = new LinearConstr();
		c.addLinTerm(new LinearTerm(v,1));
		stack_arith.push(c);
	}

	@Override
	public void visit(IAccessIndexed e) {
		errNoSupport(e);
	}

	@Override
	public void visit(IAccessMulti e) {
		errNoSupport(e);
	}

	@Override
	public void visit(ILitBool e) {
		if (e.value()) { // true
			stack_rel.push(new LinearRel());
		} else { // false
			errUnexpOp(e);
		}
	}

	@Override
	public void visit(ILitInt e) {
		LinearConstr r = new LinearConstr();
		if (((long)Integer.MAX_VALUE) < e.value() || ((long) Integer.MIN_VALUE) > e.value()) {
			System.out.println("Flata doesn't support literals of type long.");
			System.exit(1);
		}
		int aux = (int)e.value();
		r.addLinTerm(LinearTerm.constant(aux));
		stack_arith.push(r);
	}

	@Override
	public void visit(ILitReal e) {
		errNoSupport(e);
	}

	@Override
	public void visit(IHavoc e) {
		errUnexpOp(e);
	}
}
