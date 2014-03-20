package verimag.flata_backend;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

import acceleration.*;
import nts.interf.base.*;

import nts.parser.*;
import verimag.flata.NTSVisitor;
import verimag.flata.acceleration.delta.DeltaClosure;
import verimag.flata.automata.ca.*;
import verimag.flata.common.CR;
import verimag.flata.presburger.*;

public class BackEnd implements IAcceleration {

	private NTSVisitor ntsVisitor;
	
	private List<DisjRel> ast2flata(IVarTable aVt, ILoop aLoop) {
		
		List<DisjRel> ret = new LinkedList<DisjRel>();
		ntsVisitor.parseExprInit(aVt);
		
		for (IExpr e : aLoop.expressions()) {
			e.accept(ntsVisitor);
			ret.add(ntsVisitor.getRelation());
		}
		
		return ret;
	}
	
	private static class RelInfo {
		public boolean isOct;
		public DisjRel rel;
		public RelInfo(boolean aIsOct, DisjRel aRel) {
			isOct = aIsOct;
			rel = aRel;
		}
	}
	
	private RelInfo getRel(IVarTable aVt, IAccelerationInput aInput) {
		DisjRel aux = null;
		for (ILoop loop : aInput.loops()) {
			RelInfo ri = getRel(aVt, loop);
			
			if (aux == null) {
				aux = ri.rel;
			} else {
				aux = aux.or_nc(ri.rel);
			}
		}
		return new RelInfo(aux.isOctagon(), aux);
	}
	
	// onlyOct --> RelInfo.rel is null whenever non-octagonal relation detected
	private RelInfo getRel(IVarTable aVt, ILoop aLoop) {
		
		checkZeroLoop(aLoop.expressions().size());
		
		List<DisjRel> l = ast2flata(aVt, aLoop);
		
		// check for false
		for (DisjRel r : l) {
			if (r.disjuncts().size() == 0) {
				return new RelInfo(true, DisjRel.giveFalse());
			}
		}
		
		Iterator<DisjRel> iter = l.iterator();
		DisjRel aux = iter.next();
		while (iter.hasNext()) {
			aux = aux.compose(iter.next());
		}
		return new RelInfo(aux.isOctagon(), aux);
	}
	
	private void checkZeroLoop(int i) {
		if (i == 0) {
			throw new RuntimeException("error: no loop passed in the input for acceleration");
		}
	}
	public boolean isOctagon(IAccelerationInput input) {
		
		ntsVisitor = new NTSVisitor();
		return getRel(input.varTable(), input).isOct;
	}

	// compute Rel* and return
	private Closure prepareOutput(IAccelerationInput input, DisjRel rel) {

		VarTable varT = new VarTable(null);
		for (IVarTableEntry e : input.varTable().visibleUnprimed()) {
			ASTWithoutToken.declareInt(varT, e.name());
		}
		
		if (rel.contradictory()) {
			return new Closure(ASTWithoutToken.litBool(false),varT); 
		}

		VarTable q_varT = new VarTable(varT);
		ASTWithoutToken.declareIntLogical(q_varT, DeltaClosure.v_k.name());

		
		DisjRel closureK2 = rel.closureN(false, null);
		Expr ex = closureK2.toNTS();
		
		//System.out.println(closureK2);
		
		Quantifier q_ex = ASTWithoutToken.exExists(q_varT,ex);
		
		q_ex.semanticChecks(varT);
		
		return new Closure(q_ex,varT);
		
	}
	
	public Closure closure(IAccelerationInput input) {
		int s = input.loops().size();
		checkZeroLoop(s);

		ntsVisitor = new NTSVisitor();
		RelInfo ri = getRel(input.varTable(), input);
		
		if (!ri.isOct) {
			return new Closure();
		}
		
		return prepareOutput(input, ri.rel);
	}

	public Closure closureOverapprox(IAccelerationInput input) {
		int s = input.loops().size();
		checkZeroLoop(s);
		
		ntsVisitor = new NTSVisitor();
		DisjRel aux = null;
		for (ILoop l : input.loops()) {
			DisjRel aux2 = getRel(input.varTable(), l).rel;
			if (aux != null) {
				aux = aux.hullOct(aux2);
			} else {
				aux = aux2.hull(Relation.RelType.OCTAGON);
			}
		}
		
		return prepareOutput(input, aux);
	}
	
	
	private boolean allOct(List<CompositeRel> list) {
		for (CompositeRel r : list) {
			if (!r.isOctagon())
				return false;
		}
		return true;
	}
	
	private ConstProps prefConsts(IAccelerationInput input) {
		if (input.prefix().isEmpty()) {
			return new ConstProps();
		}
		
		DisjRel pref = getRel(input.varTable(), new Loop(input.prefix())).rel;
		
		return pref.outConst();
	}
	
	private DisjRel constLogical(Variable v) {
		return new DisjRel(CompositeRel.createIdentityRelationForSorted(new Variable[] {v}));
	}
	
	private CompositeRel constStrengthen(CompositeRel prefix, CompositeRel r) {
		ConstProps cp = prefix.outConst();
		cp.switchPrimes();
		cp.keepOnly(r.identVars());
		CompositeRel ret = r.copy();
		if (!cp.isEmpty()) {
			ret.update(cp);
		}
		return ret;
	}
	
	public Closure closureUnderapprox(IAccelerationInput input) {
		int s = input.loops().size();
		checkZeroLoop(s);
		
		ntsVisitor = new NTSVisitor();
		List<CompositeRel> list = new LinkedList<CompositeRel>();
		for (ILoop l : input.loops()) {
			DisjRel aux2 = getRel(input.varTable(), l).rel;
			list.addAll(aux2.disjuncts());
		}
		

		VarTable varT = new VarTable(null);
		for (IVarTableEntry e : input.varTable().visibleUnprimed()) {
			ASTWithoutToken.declareInt(varT, e.name());
		}
		
		VarTable q_varT = new VarTable(varT);
		
		
		if (allOct(list)) {
			
			DisjRel rel = null;
			
			Variable v_k_backup = DeltaClosure.v_k;
			int i = 1;
			for (CompositeRel r : list) {
				
				DeltaClosure.setParam("_k"+(i++));
				ASTWithoutToken.declareIntLogical(q_varT, DeltaClosure.v_k.name());
				
				DisjRel accel = (new DisjRel(r)).closureN(false, null);
				if (rel == null) {
					rel = accel;
				} else {
					rel = rel.and(constLogical(DeltaClosure.v_k)); // prevent modulo constraints
					rel = rel.compose(accel);
				}
			}
			DeltaClosure.setParam(v_k_backup.name());
			
			//System.out.println(rel);
			
			Expr ex = rel.toNTS();
			
			Quantifier q_ex = ASTWithoutToken.exExists(q_varT,ex);
			
			q_ex.semanticChecks(varT);
			
			return new Closure(q_ex,varT);
			
		} else {
			
			CompositeRel id = CompositeRel.createIdentityRelationForSorted(ntsVisitor.getCurrentPool().globalUnp());
			ConstProps cp = prefConsts(input);
			cp.switchPrimes();
			id.update(cp); // identity /\ constants
			
			DisjRel rel = new DisjRel(id);
			
			Variable v_k_backup = DeltaClosure.v_k;
			int i = 1;
			for (CompositeRel r : list) {
				
				DisjRel relAdd = new DisjRel();
				
				DeltaClosure.setParam("_k"+(i++));
				ASTWithoutToken.declareIntLogical(q_varT, DeltaClosure.v_k.name());
				
				if (r.isOctagon()) {
					
					//DeltaClosure.setParam("_k"+(i++));
					//ASTWithoutToken.declareIntLogical(q_varT, DeltaClosure.v_k.name());
					
					DisjRel accel = (new DisjRel(r)).closureN(true, null); // R^+ !!! not R^* !!!
					
					DisjRel relAux = rel.and(constLogical(DeltaClosure.v_k)); // prevent modulo constraints
					relAdd.addDisj(relAux.compose(accel));
					
				} else {
					
					for (CompositeRel r1 : rel.disjuncts()) {
						// strengthen with constants
						CompositeRel rr = constStrengthen(r1,r);
						
						DisjRel accel = null;
						
						if (rr.isOctagon()) {
							
							//DeltaClosure.setParam("_k"+(i++));
							//ASTWithoutToken.declareIntLogical(q_varT, DeltaClosure.v_k.name());
							
							accel = (new DisjRel(rr)).closureN(true, null); // R^+ !!! not R^* !!!
							
						} else {
							
							accel = new DisjRel(rr); // only R, if R cannot be accelerated
							
						}
						
						DisjRel relAux = rel.and(constLogical(DeltaClosure.v_k)); // prevent modulo constraints
						relAdd.addDisj(relAux.compose(accel));
					}
					
				}
				
				rel.addDisj(relAdd);
				
			}
			DeltaClosure.setParam(v_k_backup.name());
			
			//System.out.println(rel);
			
			Expr ex = rel.toNTS();
			
			Quantifier q_ex = ASTWithoutToken.exExists(q_varT,ex);
			
			q_ex.semanticChecks(varT);
			
			return new Closure(q_ex,varT);
		}
	}
	public Closure closureUnderapprox2(IAccelerationInput input) {
		int s = input.loops().size();
		checkZeroLoop(s);
		
		ntsVisitor = new NTSVisitor();
		List<CompositeRel> list = new LinkedList<CompositeRel>();
		for (ILoop l : input.loops()) {
			DisjRel aux2 = getRel(input.varTable(), l).rel;
			list.addAll(aux2.disjuncts());
		}
		
		if (!allOct(list)) {
			
			if (input.prefix().isEmpty()) {
				return new Closure();
			}
			
			DisjRel pref = getRel(input.varTable(), new Loop(input.prefix())).rel;
			
			ConstProps c = prefConsts(input);
			
			if (c.isEmpty()) {
				return new Closure();
			}
			
			CA ca = new CA("constprop",ntsVisitor.getCurrentPool());
			
			CAState s1 = ca.getStateWithName("s"+0);
			CAState s2 = ca.getStateWithName("s"+1);
			for (CompositeRel r : pref.disjuncts()) {
				CATransition t = new CATransition(s1,s2,r,ca);
				ca.addTransition(t);
			}
			int i=1;
			for (CompositeRel r : list) {
				s1 = ca.getStateWithName("s"+i);
				i++;
				s2 = ca.getStateWithName("s"+i);
				
				ca.addTransition(new CATransition(s1,s1,r,ca));
				if (i>1) {
					ca.addTransition(CA.createIdentityTransition(ca, s1, s2));
				}
				
			}
			
			int size = list.size();
			list.clear();
			
			ca.constantPropagation();
			
			for (i=1; i<=size; i++) {
				s1 = ca.getStateWithName("s"+i);
				for (CATransition t : s1.outgoing()) {
					if (t.from().equals(t.to())) {
						CompositeRel r = t.rel();
						if (!r.isOctagon()) {
							return new Closure();
						}
						list.add(r);
					}
				}
			}
		}

		VarTable varT = new VarTable(null);
		for (IVarTableEntry e : input.varTable().visibleUnprimed()) {
			ASTWithoutToken.declareInt(varT, e.name());
		}
		
		VarTable q_varT = new VarTable(varT);
		
		
		DisjRel rel = null;
		
		Variable v_k_backup = DeltaClosure.v_k;
		int i = 1;
		for (CompositeRel r : list) {
			
			DeltaClosure.setParam("_k"+(i++));
			ASTWithoutToken.declareIntLogical(q_varT, DeltaClosure.v_k.name());
			
			DisjRel closureK2 = (new DisjRel(r)).closureN(false, null);
			if (rel == null) {
				rel = closureK2;
			} else {
				rel = rel.and(constLogical(DeltaClosure.v_k)); // prevent modulo constraints
				rel = rel.compose(closureK2);
			}
		}
		DeltaClosure.setParam(v_k_backup.name());
		
		//System.out.println(rel);
		
		Expr ex = rel.toNTS();
		
		Quantifier q_ex = ASTWithoutToken.exExists(q_varT,ex);
		
		q_ex.semanticChecks(varT);
		
		return new Closure(q_ex,varT);
	}
	
	
	public static boolean initActions() {
		try {
			CR.launchYices();
			CR.launchGLPK();
		} catch (UnsatisfiedLinkError e) {
			return false;
		}
		
		return true;
	}
	public static void finalActions() {
		CR.terminateYices();
	}
	
	// main method for testing purposes
	public static void main2(String[] args) {
		
		verimag.flata.Main.initActions();
		
		InputStream is = null;
	    try {
			is = new FileInputStream(args[0]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	    
	    ParserListener listen = new ParserListener();
	    NTSParser.parseExpr(is, listen);
	    
	    List<ILoop> loopList = new LinkedList<ILoop>();
	    loopList.add(new Loop(listen.retriveResultExpr().getExprList()));
	    AccelerationInput ai = new AccelerationInput(loopList, listen.retriveResultExpr().getVarTable());
	    
	    BackEnd backend = new BackEnd();
	    
	    if (backend.isOctagon(ai)) {
	    	@SuppressWarnings("unused")
			Closure cl = backend.closure(ai);
	    } else {
	    	System.out.println("Input is not octagonal.");
	    }
	    
	    verimag.flata.Main.finalActions();
	}
	
	// main method for testing purposes
	public static void main(String[] args) {
		
		BackEnd.initActions();
		
		/*
pref: (-v561=0,-v565=0,x'=0,-v563=0,v562=v558,-v560=-1,-v564=-1,d'=1)
(x=0,x'=1,d'=1)
false
(d'=d, x'=x+d', x>=1, x<=999)
(x=1000,x'=999,d'=-1)
		*/
		Expr e0 = ASTWithoutToken.exAnd(
				ASTWithoutToken.exEq(ASTWithoutToken.accessBasic("d'"), ASTWithoutToken.litInt(1)),
				ASTWithoutToken.exEq(ASTWithoutToken.accessBasic("x'"), ASTWithoutToken.litInt(0))
				);
		Expr e1 = ASTWithoutToken.exAnd(
				ASTWithoutToken.exAnd(
						ASTWithoutToken.exEq(ASTWithoutToken.accessBasic("x"), ASTWithoutToken.litInt(0)),
						ASTWithoutToken.exEq(ASTWithoutToken.accessBasic("x'"), ASTWithoutToken.litInt(1))
				),
				ASTWithoutToken.exEq(ASTWithoutToken.accessBasic("d'"), ASTWithoutToken.litInt(1))
				);
		Expr e2 = ASTWithoutToken.litBool(false);
		Expr e3 = ASTWithoutToken.exAnd(
				ASTWithoutToken.exAnd(
						ASTWithoutToken.exAnd(
								ASTWithoutToken.exGeq(ASTWithoutToken.accessBasic("x"), ASTWithoutToken.litInt(1)),
								ASTWithoutToken.exLeq(ASTWithoutToken.accessBasic("x"), ASTWithoutToken.litInt(999))
						),
						ASTWithoutToken.exEq(ASTWithoutToken.accessBasic("d'"), ASTWithoutToken.accessBasic("d"))
				),
				ASTWithoutToken.exEq(
						ASTWithoutToken.accessBasic("x'"),
						ASTWithoutToken.exPlus(ASTWithoutToken.accessBasic("x"), ASTWithoutToken.accessBasic("d"))))
						//ASTWithoutToken.exPlus(ASTWithoutToken.accessBasic("x"), ASTWithoutToken.litInt(1))))
				;
		Expr e4 = ASTWithoutToken.exAnd(
				ASTWithoutToken.exAnd(
						ASTWithoutToken.exEq(ASTWithoutToken.accessBasic("x"), ASTWithoutToken.litInt(1000)),
						ASTWithoutToken.exEq(ASTWithoutToken.accessBasic("x'"), ASTWithoutToken.litInt(999))
				),
				ASTWithoutToken.exEq(ASTWithoutToken.accessBasic("d'"), ASTWithoutToken.litInt(-1))
				);
		
		/*
		Expr e0 = ASTWithoutToken.exAnd(
				ASTWithoutToken.exEq(
				ASTWithoutToken.litInt(3),
				ASTWithoutToken.accessBasic("y'")),
				ASTWithoutToken.exEq(
						ASTWithoutToken.litInt(5),
						ASTWithoutToken.accessBasic("z'")));
		Expr e1 = ASTWithoutToken.exAnd(
				ASTWithoutToken.exAnd(
					ASTWithoutToken.exEq(
						ASTWithoutToken.exPlus(ASTWithoutToken.accessBasic("x"),
						ASTWithoutToken.exPlus(ASTWithoutToken.accessBasic("z"),ASTWithoutToken.accessBasic("y"))),
						ASTWithoutToken.accessBasic("x'")),
					ASTWithoutToken.exEq(
						ASTWithoutToken.accessBasic("y"),
						ASTWithoutToken.accessBasic("y'"))),
						ASTWithoutToken.exEq(
								ASTWithoutToken.accessBasic("z"),
								ASTWithoutToken.accessBasic("z'")));
		Expr e2 = ASTWithoutToken.exEq(
						ASTWithoutToken.exPlus(ASTWithoutToken.accessBasic("x"),ASTWithoutToken.accessBasic("y")),
						ASTWithoutToken.accessBasic("x'"));
		*/
		
		
		VarTable vt = new VarTable(null);
		ASTWithoutToken.declareInt(vt,"x");
		ASTWithoutToken.declareInt(vt,"d");
		
		e0.semanticChecks(vt);
		e1.semanticChecks(vt);
		e2.semanticChecks(vt);
		e3.semanticChecks(vt);
		e4.semanticChecks(vt);
		
		List<IExpr> l1 = new LinkedList<IExpr>();
		l1.add(e1);
		List<IExpr> l2 = new LinkedList<IExpr>();
		l2.add(e2);
		List<IExpr> l3 = new LinkedList<IExpr>();
		l3.add(e3);
		List<IExpr> l4 = new LinkedList<IExpr>();
		l4.add(e4);
		
		List<IExpr> pref = new LinkedList<IExpr>();
		pref.add(e0);
		
		List<ILoop> loopList = new LinkedList<ILoop>();
	    loopList.add(new Loop(l1));
	    loopList.add(new Loop(l2));
	    loopList.add(new Loop(l3));
	    loopList.add(new Loop(l4));
	    AccelerationInput ai = new AccelerationInput(pref,loopList,vt);
	    
	    BackEnd backend = new BackEnd();
	    
	    if (backend.isOctagon(ai)) {
	    	Closure cl = backend.closure(ai);
	    	
	    	client.PrintVisitor pv = new client.PrintVisitor();
			cl.getClosure().accept(pv);
			System.out.println("Printing output AST:");
			System.out.println(pv.toStringBuffer());
	    } else {
	    	System.out.println("Input is not octagonal.");
	    	
	    	Closure cl = backend.closureOverapprox(ai);
	    	client.PrintVisitor pv = new client.PrintVisitor();
			cl.getClosure().accept(pv);
			System.out.println("Printing output AST:");
			System.out.println(pv.toStringBuffer());
			
			cl = backend.closureUnderapprox(ai);
			if (!cl.succeeded()) {
				System.out.println("Underapproximation failed");
			} else {
				pv = new client.PrintVisitor();
				cl.getClosure().accept(pv);
				System.out.println("Printing output AST:");
				System.out.println(pv.toStringBuffer());
			}
	    }
	    
	    BackEnd.finalActions();
	}
}
