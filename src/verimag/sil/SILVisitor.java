package verimag.sil;

import nts.interf.*;
import nts.interf.base.*;
import nts.interf.expr.*;
import nts.parser.*;

import java.util.*;

import verimag.flata.NTSVisitor;
import verimag.flata.presburger.*;

// translation of the NTS abstract syntax tree to the internal representation
public class SILVisitor implements IVisitor {

	private static void errNoSupport(Object o) {
		System.err.println("'"+o.getClass().getName()+"' not supported.");
		System.exit(1);
	}
	
	private SymbolTable st;
	private Deque<Node> stack = new LinkedList<Node>();
	// substitutions of array accesses a_i[scalar_i+const_i] with fresh scalars p_i
	private Map<AccessBasic,AccessIndexed> subst = new HashMap<AccessBasic,AccessIndexed>();
	
	public SILVisitor(SymbolTable aST) {
		st = aST;
	}
	public Node translate(Expr e) {
		e.accept(this);
		finalActions();
		return getRoot();
	}
	private Node getRoot() {
		return stack.pop();
	}
	private void finalActions() {
		// conjoin the root with performed substitutions: F  --> F /\ a[m+c] = p /\ ...
		
		// given a[term] = p, create forall i . (term <= i <= term) -> a[i] = p
		if (!subst.isEmpty()) {
			
			Node n = stack.pop();
			
			SILVisitor_ArrayPropRhs v4 = new SILVisitor_ArrayPropRhs(st.vp);
			
			for (AccessBasic a : subst.keySet()) {
				
				Node.AccessArray aa = v4.parseIAccessIndexed(subst.get(a));
				n = new Node.NodeAnd(n,Node.NodeForall.create(a.varName(), aa, st));
			}
			
			stack.push(n);
		}
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
		stack.push(stack.pop().not());
	}

	@Override
	public void visit(IExprAnd e) {
		accept(e);
		stack.push(stack.pop().and(stack.pop()));
	}

	@Override
	public void visit(IExprOr e) {
		accept(e);
		stack.push(stack.pop().or(stack.pop()));
	}

	@Override
	public void visit(IExprImpl e) {
		accept(e);
		Node rhs = stack.pop();
		Node lhs = stack.pop();
		stack.push(lhs.not().or(rhs));
	}

	@Override
	public void visit(IExprEquiv e) {
		accept(e);
		Node rhs = stack.pop();
		Node lhs = stack.pop();
		Node aux1 = lhs.not().or(rhs);
		Node aux2 = rhs.not().or(lhs);
		stack.push(aux1.and(aux2));
	}

	
	// quantifiers
	@Override
	public void visit(IExprExists e) {
		
		// all array accesses must be over global-scope variables
		// these can be substituted with fresh variables
		
		// ensure no nested array access
		SILVisitor_NestedAndQuantifiedIndexing v3 = new SILVisitor_NestedAndQuantifiedIndexing();
		e.accept(v3);
		// ensure that no variable in index expressions is quantified 
		v3.enforcePresburger();
		
		// substitute array accesses with fresh scalars 
		SILVisitor_ArrayAccessAsScalar v2 = new SILVisitor_ArrayAccessAsScalar(st);
		e.accept(v2);
		Expr subst = v2.getResult();
		subst.semanticChecks(st.vt);
		this.subst.putAll(v2.getSubst());
		
		// convert the array-access free formula to internal representation for Presburger relations
		NTSVisitor v_nts = new NTSVisitor();
		v_nts.parseSILExprInit(st.vp);
		subst.accept(v_nts);
		DisjRel rel = v_nts.getRelation();
		
		this.stack.push(new Node.NodePresburger(rel));
	}
	
	// forall i_1,...,i_n . F 
	@Override
	public void visit(IExprForall e) {
		// array-forall property if
		//   n=1
		//   no quantifiers in F
		//   some array access of the form a[i]
		//   syntactical restrictions: F of the form F1 => F2 or of the form F2 where
		//     F1, F2 are conjunctions of DB constraints
		//     ...
		
		// ensure no nested array access
		SILVisitor_NestedAndQuantifiedIndexing v3 = new SILVisitor_NestedAndQuantifiedIndexing();
		e.accept(v3);
		if (!v3.hasQuantifiedIndexing()) {
			// if no array-index term contains a quantified variable, convert to Presburger formula
			
			// substitute array accesses with fresh scalars 
			SILVisitor_ArrayAccessAsScalar v2 = new SILVisitor_ArrayAccessAsScalar(st);
			e.accept(v2);
			Expr e_subst = v2.getResult();
			e_subst.semanticChecks(st.vt);
			this.subst.putAll(v2.getSubst());
			
			// convert the array-access free formula to internal representation for Presburger relations
			NTSVisitor v_nts = new NTSVisitor();
			v_nts.parseSILExprInit(st.vp);
			e_subst.accept(v_nts);
			DisjRel rel = v_nts.getRelation();
			
			this.stack.push(new Node.NodePresburger(rel));
			
		} else {
			// if some quantified index-term appears, we assume that we have found an array-property formula 
			
			v3.enforceArray(e);
			
			// declare globally
			String qVarName = e.varTable().innermostUnprimed().get(0).name();
			Variable qVar = st.giveIndexVar(qVarName);
			
			LinearConstr low = null;
			LinearConstr up = null;
			IExpr rhs = e.operand();
			if (e.operand() instanceof IExprImpl) { // F has the form F1 => F2
				IExprImpl impl = (IExprImpl)e.operand();
				rhs = impl.operand2();
				
				IExpr lhs = impl.operand1();
				// transform LHS to array-range 
				SILVisitor_ArrayPropLhs v5 = new SILVisitor_ArrayPropLhs();
				v5.parseSILExprInit(st.vp);
				lhs.accept(v5);
				v5.finalizeAndCheck(lhs,qVar);
				low = v5.getLower();
				up = v5.getUpper();
				
			} else { // F has the form F2 
			}
			
			// transform RHS to array-value -- conjunction of Linear constraints
			SILVisitor_ArrayPropRhs v4 = new SILVisitor_ArrayPropRhs(st.vp,qVarName);
			rhs.accept(v4);
			v4.checkValueConstraints(rhs);
			
			List<Node.ForallConstr> valC = v4.getValueConstraints();
			Node n = Node.NodeForall.create(qVar, low, up, valC);
			
			this.stack.push(n);
		}
		
	}

	// relational operators

	private void rop2presb(IExprBin e) {
		SILVisitor_ArrayAccessAsScalar v2 = new SILVisitor_ArrayAccessAsScalar(st);
		e.accept(v2);
		Expr subst = v2.getResult();
		subst.semanticChecks(st.vt);
		this.subst.putAll(v2.getSubst());
		
		NTSVisitor v = new NTSVisitor();
		v.parseSILExprInit(st.vp);
		subst.accept(v);
		this.stack.push(new Node.NodePresburger(v.getRelation()));
	}
	
	@Override
	public void visit(IExprEq e) {
		rop2presb(e);
	}

	@Override
	public void visit(IExprNeq e) {
		rop2presb(e);
	}

	@Override
	public void visit(IExprLeq e) {
		rop2presb(e);
	}

	@Override
	public void visit(IExprLt e) {
		rop2presb(e);
	}

	@Override
	public void visit(IExprGeq e) {
		rop2presb(e);
	}

	@Override
	public void visit(IExprGt e) {
		rop2presb(e);
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
		assert(false);
	}

	@Override
	public void visit(IExprList e) {
		assert(false);
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
		assert(false);
	}

	@Override
	public void visit(ILitBool e) {
		if (e.value()) { // true
			stack.push(Node.NodePresburger.giveTrue());
		} else { // false
			stack.push(Node.NodePresburger.giveFalse());
		}
	}

	@Override
	public void visit(ILitInt e) {
		assert(false);
	}

	@Override
	public void visit(ILitReal e) {
		assert(false);
	}

	@Override
	public void visit(IHavoc e) {
		assert(false);
	}

	
}
