package verimag.flata;

import nts.interf.*;
import nts.interf.base.*;
import nts.interf.expr.*;

import java.util.*;

import verimag.flata.presburger.*;
import verimag.flata.automata.ca.*;
import verimag.flata.automata.cg.*;
//import verimag.flata.common.*;

public class NTSVisitor implements IVisitor {

	// //////////////////////////////////
	// parsing of individual expressions
	// //////////////////////////////////
	
	public void parseExprInit(IVarTable aVarTable) {
		
		List<String> names = checkIntDeclar(aVarTable);
		currentPool = VariablePool.createGPool(names);
	}

	public DisjRel getRelation() {
		return stack_rel.pop();
	}
	
	public LinearConstr getArithExp() {
		return stack_arith.pop();
	}
	
	public VariablePool getCurrentPool() {
		return currentPool;
	}
	
	// //////////////////////////////////
	// for SIL
	// //////////////////////////////////

	public void parseSILExprInit(VariablePool aVP) {
		currentPool = aVP;
	}

	
	// //////////////////////////////////
	// //////////////////////////////////
	
	private void errNoSupport(Object o) {
		System.err.println("'"+o.getClass().getName()+"' not supported.");
		System.exit(1);
	}
	private void errNotPresburger() {
		System.err.println("Flata supports only Presburger expressions.");
		System.exit(1);
	}
	
	private void errMultiThreaded() {
		System.err.println("Flata supports only single-thread programs.");
		System.exit(1);
	}
	
	private String checkInstances(INTS e) {
		if (e.instances().size() != 1) {
			errMultiThreaded();
			return null;
		} else {
			Map.Entry<String, IExpr> pair = e.instances().entrySet().iterator().next();
			pair.getValue().accept(this);
			LinearConstr c = stack_arith.pop();
			if (c.term(null)==null || c.size()>1) {
				errMultiThreaded();
			}
			return pair.getKey();
		}
	}
	
	private List<String> getParameters(IVarTable aVT) {
		List<String> ret = new LinkedList<String>();
		for (IVarTableEntry e : aVT.innermost()) {
			if (e.modifier() == EModifier.PARAM) {
				ret.add(e.name());
			}
		}
		return ret;
	}
	
	private void checkModifier(IVarTableEntry e) {
		if (e.modifier() != EModifier.NO &&
				e.modifier() != EModifier.IN &&
				e.modifier() != EModifier.OUT &&
				e.modifier() != EModifier.PARAM) {
			System.err.println("Modifier '"+e.modifier().toString()+"' is not currently supported.");
			System.exit(1);
		}
	}
	private void checkType(IType e) {
		if (e.isArray() || e.basicType() != EBasicType.INT) {
			System.err.println("Flata currently supports only scalar integers.");
			System.exit(1);
		}
	}
	private List<String> checkIntDeclar(IVarTable aVT) {
		List<String> ret = new LinkedList<String>();
		for (IVarTableEntry e : aVT.innermost()) {
			if (e.modifier() == EModifier.TID)
				continue;
			if (e.isPrimed())
				continue;
			checkModifier(e);
			checkType(e.type());
			ret.add(e.name());
		}
		return ret;
	}
	private List<String> entry2string(List<IVarTableEntry> entries) {
		return entry2string(entries, false);
	}
	private List<String> entry2stringPrime(List<IVarTableEntry> entries) {
		return entry2string(entries, true);
	}
	private List<String> entry2string(List<IVarTableEntry> entries, boolean addPrime) {
		List<String> ret = new LinkedList<String>();
		String p = addPrime? "'" : "";
		for (IVarTableEntry ee : entries) {
			ret.add(ee.name()+p);
		}
		return ret;
	}
	

	private CG cg = new CG();
	private CA ca = null;
	private VariablePool currentPool = null;
	private VariablePool poolG = null; // global pool
	
	private List<String> globalParameters = null;
	
	public CG callGraph() {
		return cg;
	}

	
	@Override
	public void visit(INTS e) {
		
		// global declarations
		List<String> globals = checkIntDeclar(e.varTable());
		globalParameters = this.getParameters(e.varTable());
		poolG = VariablePool.createGPool(globals);
		
		processNewSubsystems(e, e.subsystems());
		
		plugPrecondition(e);
	}
	
	private void plugPrecondition(INTS e) {
		e.precondition().accept(this);
		DisjRel pre = stack_rel.pop();
		if (!pre.isTrue()) {
			cg.mainProc().setPrecondition(pre);
		}
	}
	
	public void extendWithNewSubsystems(INTS aNts, List<ISubsystem> aSubs) {
		processNewSubsystems(aNts, aSubs);
	}
	
	private void processNewSubsystems(INTS aNts, List<ISubsystem> aList) {
		
		String main = checkInstances(aNts);
		
		cg.name(aNts.name());
		
		// first, ignore transitions
		for (ISubsystem s : aList) {
			// declarations
			List<String> locals = checkIntDeclar(s.varTable());
			VariablePool poolL = VariablePool.createGLPool(poolG, locals, s.name());
			// input, output
			poolL.declarePortIn(entry2string(s.varIn()));
			poolL.declarePortOut(entry2stringPrime(s.varOut()));

			//
			ca = new CA(s.name(), poolL);
			currentPool = ca.varPool();
			
			CGNode cgn = new CGNode(ca);
			cg.addProc(cgn);
			
			if (s.name().equals(main)) {
				cg.setMain(cgn);
			}
			
			// control states
			for (IControlState is : s.marksInit()) {
				ca.setInitial(ca.getStateWithName(is.name()));
			}
			for (IControlState is : s.marksFinal()) {
				ca.setFinal(ca.getStateWithName(is.name()));
			}
			for (IControlState is : s.marksError()) {
				ca.setError(ca.getStateWithName(is.name()));
			}
		}
		
		// deal with transitions
		for (ISubsystem s : aList) {
			
			//
			ca = cg.giveNode(s.name()).procedure();
			currentPool = ca.varPool();
			
			// transitions
			for (ITransition t : s.transitions()) {
				t.accept(this);
			}
			
			List<String> parameters = this.getParameters(s.varTable());
			parameters.addAll(globalParameters);
			ca.encodeParameters(parameters);
		}
	}
	
	@Override
	public void visit(ISubsystem e) {
		// dealt from INTS
	}

	@Override
	public void visit(IAnnotations e) {
		// ignore
	}

	@Override
	public void visit(IControlState e) {
		// ignore
	}

	@Override
	public void visit(ITransition e) {
		CAState from = ca.getStateWithName(e.from().name());
		CAState to = ca.getStateWithName(e.to().name());
		String id = e.id();
		
		try{
			e.label().accept(this);
		} catch (verimag.flata.common.NotPresburger ex) {
			if (e.label() instanceof IExpr) {
				nts.parser.Expr aux = (nts.parser.Expr)e.label();
				System.err.println("Non-Presburger expression around token: "+aux.token());
				System.exit(1);
			} else {
				throw ex;
			}
		}
		//Label l = null;
		if (!stack_rel.isEmpty()) { // formula
			DisjRel dr = stack_rel.pop();
			boolean one = dr.disjuncts().size() == 1;
			for (CompositeRel cr : dr.disjuncts()) {
				
				String idU = id;
				if (id != null) {
					idU = one? id : ca.giveNextTransitionLabelWithPrefix(id);
				} 
//				else {
//					idU = ca.giveNextTransitionLabelWithPrefix("t");
//				}
				
				
				CATransition t = new CATransition(from, to, cr, idU, ca);
				ca.addTransition(t);
			}
		} else { // call
			CATransition t = new CATransition(from, to, call, id, ca);
			call.setCallingPoint(t);
			
			ca.addTransition(t);
			call = null;
		}
		
		
		// TODO Auto-generated method stub
		
	}

	private Call call = null;
	
	@Override
	public void visit(ICall e) {
		
		List<LinearConstr> argsIn = new LinkedList<LinearConstr>();
		for (IExpr ex : e.actualParameters()) {
			ex.accept(this);
			argsIn.add(stack_arith.pop());
		}
		
		List<Variable> argsOut = new LinkedList<Variable>();
		for (IAccessBasic aa : e.returnVars()) {
			argsOut.add(currentPool.giveVariable(aa.var().name()));
		}
		
		CGNode callee = cg.giveNode(e.callee().name());
		
		call = new Call(ca, callee.procedure(), argsIn, argsOut);
		
		cg.addCall(call);
		
		// TODO
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
		stack_rel.push(stack_rel.pop().not());
	}

	@Override
	public void visit(IExprAnd e) {
		accept(e);
		stack_rel.push(stack_rel.pop().and(stack_rel.pop()));
	}

	@Override
	public void visit(IExprOr e) {
		accept(e);
		stack_rel.push(stack_rel.pop().or_nc(stack_rel.pop()));
	}

	@Override
	public void visit(IExprImpl e) {
		accept(e);
		DisjRel rhs = stack_rel.pop();
		DisjRel lhs = stack_rel.pop();
		stack_rel.push(lhs.not().or_nc(rhs));
	}

	@Override
	public void visit(IExprEquiv e) {
		accept(e);
		DisjRel rhs = stack_rel.pop();
		DisjRel lhs = stack_rel.pop();
		DisjRel aux1 = lhs.not().or_nc(rhs);
		DisjRel aux2 = rhs.not().or_nc(lhs);
		stack_rel.push(aux1.and(aux2));
	}

	// quantifiers
	
	private static DisjRel existElim(DisjRel rel, IExprQ e) {
		DisjRel aux = rel;
		for (IVarTableEntry ve : e.varTable().innermostUnprimed()) {
			Variable v = VariablePool.getSpecialPool().giveVariableInfo(ve.name()).getVariable();
			aux = aux.existElim1(v);
		}
		return aux;
	}
	
	@Override
	public void visit(IExprExists e) {
		for (IVarTableEntry ve : e.varTable().innermostUnprimed()) {
			VariablePool.createSpecial(ve.name());
		}
		e.operand().accept(this);
		stack_rel.push(existElim(stack_rel.pop(),e));
	}

	@Override
	public void visit(IExprForall e) {
		for (IVarTableEntry ve : e.varTable().innermostUnprimed()) {
			VariablePool.createSpecial(ve.name());
		}
		e.operand().accept(this);
		stack_rel.push(existElim(stack_rel.pop().not(),e).not());
	}

	// relational operators
	
	private Deque<DisjRel> stack_rel = new LinkedList<DisjRel>();

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
		stack_rel.push(new DisjRel(new CompositeRel(lr)));
	}
	private void pushEq(LinearConstr op1, LinearConstr op2) {
		LinearRel lr = eq(op1,op2);
		stack_rel.push(new DisjRel(new CompositeRel(lr)));
	}
	private void pushNeq(LinearConstr op1, LinearConstr op2) {
		LinearConstr c = new LinearConstr(op1);
		c = c.minus(op2);
		LinearConstr c2 = new LinearConstr(c);
		
		// op1 < op2   <=>   op1 - op2 + 1 <= 0
		c.addLinTerm(LinearTerm.constant(1));
		// op1 > op2   <=>   op2 < op1   <=>   op2 - op1 + 1 <= 0
		c2 = LinearConstr.transformBetweenGEQandLEQ(c2);
		c2.addLinTerm(LinearTerm.constant(1));
		
		DisjRel dr = new DisjRel();
		dr.addDisj(new CompositeRel(new LinearRel(c)));
		dr.addDisj(new CompositeRel(new LinearRel(c2)));
		
		stack_rel.push(dr);
	}
	
	
	@Override
	public void visit(IExprEq e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		// o1 = o2  <=>  o1 <= o2 /\ o2 <= o1
		pushEq(op1, op2);
		remainder();
	}

	@Override
	public void visit(IExprNeq e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		// o1 = o2  <=>  o1 <= o2 /\ o2 <= o1
		pushNeq(op1, op2);
		remainder();
	}

	@Override
	public void visit(IExprLeq e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		// o1 <= o2
		pushLeq(op1, op2);
		remainder();
	}

	@Override
	public void visit(IExprLt e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		// o1 < o2  <=>  o1 <= o2 - 1
		op2.addLinTerm(LinearTerm.constant(-1));
		pushLeq(op1, op2);
		remainder();
	}

	@Override
	public void visit(IExprGeq e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		// o1 >= o2  <=>  o2 <= o1
		pushLeq(op2, op1);
		remainder();
	}

	@Override
	public void visit(IExprGt e) {
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		// o1 > o2  <=>  o2 <= o1 - 1
		op1.addLinTerm(LinearTerm.constant(-1));
		pushLeq(op2, op1);
		remainder();
	}

	// add constraints used in substitution of remainder expressions op1 % op2
	private void remainder() {
		if (rem_info != null) {
			DisjRel top = stack_rel.pop();
			DisjRel rem_constr = new DisjRel(new CompositeRel(rem_info.constrs));
			
			DisjRel aux = top.and(rem_constr);
			for (Variable v : rem_info.vars_exists) {
				aux = aux.existElim2(v);
			}
			
			stack_rel.push(aux);
			
			rem_info = null;
		}
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

	private int rem_cnt = 1;
	private String rem_pref = "$t$_";
	
	private RemInfo rem_info = null;
	private static class RemInfo {
		public List<Variable> vars_exists = new LinkedList<Variable>();
		public LinearRel constrs = new LinearRel();
	}
	// exists t1,t2 . 0 <= t1 < op2 /\ op1 = t1 + t2*op2
	// returns t1
	private LinearConstr add2RemInfo(LinearConstr op1, LinearConstr op2) {
		
		if (rem_info == null)
			rem_info = new RemInfo();
		
		// create 2 fresh variables
		Variable t1 = VariablePool.createSpecial(rem_pref+(rem_cnt++));
		Variable t2 = VariablePool.createSpecial(rem_pref+(rem_cnt++));
		
		rem_info.vars_exists.add(t1);
		rem_info.vars_exists.add(t2);
		
		LinearConstr c_zero = new LinearConstr();
		LinearConstr c_t1 = new LinearConstr();
		c_t1.addLinTerm(new LinearTerm(t1,1));
		LinearConstr c_t2 = new LinearConstr();
		c_t2.addLinTerm(new LinearTerm(t2,1));
		
		// 0 <= t1
		LinearRel r1 = leq(c_zero,c_t1);
		
		// t1 < op2
		LinearConstr aux2 = new LinearConstr(op2);
		aux2.addLinTerm(LinearTerm.constant(-1));
		LinearRel r2 = leq(c_t1,aux2);
		
		// op1 = t1 + t2*op2
		LinearConstr aux3 = new LinearConstr(op2);
		aux3 = aux3.times(c_t2);
		aux3 = aux3.plus(c_t1);
		LinearRel r3 = eq(op1,aux3);
		
		//
		rem_info.constrs.addAll(r1);
		rem_info.constrs.addAll(r2);
		rem_info.constrs.addAll(r3);
		
		return c_t1;
	}
	
	@Override
	public void visit(IExprRemainder e) {
		// op1 % op2
		accept(e);
		LinearConstr op2 = stack_arith.pop();
		LinearConstr op1 = stack_arith.pop();
		
		LinearConstr subst = add2RemInfo(op1,op2);
		stack_arith.push(subst);
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
		//errNoSupport(e);
		if (e.value()) { // true
			stack_rel.push(DisjRel.giveTrue());
		} else { // false
			stack_rel.push(DisjRel.giveFalse());
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
		
		Set<Variable> vv = new HashSet<Variable>();
		for (Variable v : currentPool.localUnp()) {
			vv.add(v);
		}
		for (Variable v : currentPool.globalUnp()) {
			vv.add(v);
		}
		for (IAccessBasic a : e.vars()) {
			vv.remove(currentPool.giveVariable(a.var().name()));
		}
		
		Variable[] arr = vv.toArray(new Variable[0]);
		Arrays.sort(arr);
		CompositeRel rel = CompositeRel.createIdentityRelationForSorted(arr);
		
		stack_rel.push(new DisjRel(rel));
	}
}
