package verimag.sil;

import nts.interf.*;
import nts.interf.base.*;
import nts.interf.expr.*;
import nts.parser.*;

import java.util.*;

import verimag.flata.NTSVisitor;
import verimag.flata.presburger.*;

public class SILVisitor_ArrayPropRhs implements IVisitor {

	private static void errNoSupport(Object o) {
		System.err.println("'"+o.getClass().getName()+"' not supported.");
		System.exit(1);
	}
	private static void errUnexpOp(IExpr o) {
		System.err.println("Unexpected operator: '"+o.getClass().getName()+"' "+Common.at((Expr)o)+".");
		System.exit(1);
	}
	private static void errMultiIndex(IExpr e) {
		System.err.println("Syntax error at: "+Common.at((Expr)e)+".");
		System.exit(1);
	}
	private static void errIndex(IExpr e) {
		System.err.println("Index expression must be of the form 'index_var + constant': "+Common.at((Expr)e)+".");
		System.exit(1);
	}
	private static void errIntexTermWithoutIndexVar(IExpr e) {
		System.err.println("Index expression in an array forall property must contain a quantified index variable: "+Common.at((Expr)e)+".");
		System.exit(1);
	}
	private static void errOctagonal(IExpr e) {
		System.err.println("Array properties must be octagonal: "+Common.at((Expr)e)+".");
		System.exit(1);
	}
	
	
	// if qVar!=null, enforce indexing via qVar only
	private String qVar;
	private VariablePool vp;

	public SILVisitor_ArrayPropRhs(VariablePool aVP) {
		this(aVP,null);
	}
	public SILVisitor_ArrayPropRhs(VariablePool aVP, String aQvar) {
		vp = aVP;
		qVar = aQvar;
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
		errUnexpOp(e);
	}

	@Override
	public void visit(IExprAnd e) {
		accept(e);
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
	
	@Override
	public void visit(IExprExists e) {
		errUnexpOp(e);
	}
	@Override
	public void visit(IExprForall e) {
		errUnexpOp(e);
	}

	// relational operators
	
	private void rop_push(Node.ROP rop) {
		Node.SumTerms v2 = stack.pop();
		Node.SumTerms v1 = stack.pop();
		Node.SumTerms v = v1.minus(v2);
		arrProp.add(new Node.ForallConstr(v,rop));
	}
	
	@Override
	public void visit(IExprEq e) {
		accept(e);
		rop_push(Node.ROP.EQ);
	}

	@Override
	public void visit(IExprNeq e) {
		errUnexpOp(e);
	}

	@Override
	public void visit(IExprLeq e) {
		accept(e);
		rop_push(Node.ROP.LEQ);
	}

	@Override
	public void visit(IExprLt e) {
		accept(e);
		rop_push(Node.ROP.LT);
	}

	@Override
	public void visit(IExprGeq e) {
		accept(e);
		rop_push(Node.ROP.GEQ);
	}

	@Override
	public void visit(IExprGt e) {
		accept(e);
		rop_push(Node.ROP.GT);
	}

	private void accept(IExprUn e) {
		e.operand().accept(this);
	}
	private void accept(IExprBin e) {
		e.operand1().accept(this);
		e.operand2().accept(this);
	}
	
	// arithmetic operators
	
	private Deque<Node.SumTerms> stack = new LinkedList<Node.SumTerms>();
	private List<Node.ForallConstr> arrProp = new LinkedList<Node.ForallConstr>();
	
	public void checkValueConstraints(IExpr e) {
		// check is the constraints are octagonal
		for (Node.SumTerms c : stack) {
			if (!c.check()) {
				errOctagonal(e);
			}
		}
	}
	public List<Node.ForallConstr> getValueConstraints() {
		return arrProp;
	}
	
	@Override
	public void visit(IExprMult e) {
		errUnexpOp(e);
	}
	
	@Override
	public void visit(IExprRemainder e) {
		errUnexpOp(e);
	}

	@Override
	public void visit(IExprDivide e) {
		errUnexpOp(e);
	}

	@Override
	public void visit(IExprPlus e) {
		accept(e);
		Node.SumTerms v2 = stack.pop();
		Node.SumTerms v1 = stack.pop();
		stack.push(v1.plus(v2));
	}

	@Override
	public void visit(IExprMinus e) {
		accept(e);
		Node.SumTerms v2 = stack.pop();
		Node.SumTerms v1 = stack.pop();
		stack.push(v1.minus(v2));
	}

	@Override
	public void visit(IExprUnaryMinus e) {
		accept(e);
		stack.push(stack.pop().unminus());
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
		Node.AccessScalar aa = new Node.AccessScalar(e.var().name());
		stack.push(new Node.SumTerms(aa));
	}

	public Node.AccessArray parseIAccessIndexed(IAccessIndexed e) {
		if (e.indices().size()>1) {
			errMultiIndex(e);
		}
		
		NTSVisitor v = new NTSVisitor();
		v.parseSILExprInit(vp);
		IExpr eIndex = e.indices().get(0);
		eIndex.accept(v);
		LinearConstr lc = v.getArithExp();
		
		LinearTerm offset = LinearTerm.constant(0);
		LinearTerm inx = null;
		for (LinearTerm lt : lc.terms()) {
			if (lt.variable() == null) {
				offset = lt;
			} else {
				if (	inx != null || 
						(qVar != null && !lt.variable().name().equals(qVar)) || 
						lt.coeff() != 1) {
					
					errIndex(eIndex);
				} else {
					inx = lt;
				}
			}
		}
		
		if (qVar != null && inx == null) {
			errIntexTermWithoutIndexVar(eIndex);
		}
		
		String inxVar = null;
		if (qVar!=null) {
			inxVar = qVar;
		} else if (inx != null) {
			inxVar = inx.variable().name();
		}
		return new Node.AccessArray(e.var().name(),inxVar,offset.coeff());
	}
	
	@Override
	public void visit(IAccessIndexed e) {
		Node.AccessArray aa = parseIAccessIndexed(e);
		stack.push(new Node.SumTerms(aa));
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
		Node.AccessConst aa = new Node.AccessConst(e.value());
		stack.push(new Node.SumTerms(aa));
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
