package verimag.flata.parsers;

import java.util.*;

import verimag.flata.presburger.*;
import verimag.flata.presburger.Relation.RelType;

// boolean expression node
@SuppressWarnings("incomplete-switch")
public class BENode {

	public static enum ASTConstrType {
		UNDEF, LEQ, GEQ, EQ, NEQ, LESS, GREATER, DIVIDES, NOTDIVIDES;

		public ASTConstrType neg() {
			switch (this) {
			case LEQ:
				return GREATER;
			case GEQ:
				return LESS;
			case EQ:
				return NEQ;
			case NEQ:
				return EQ;
			case LESS:
				return GEQ;
			case GREATER:
				return LEQ;
			case DIVIDES:
				return NOTDIVIDES;
			case NOTDIVIDES:
				return DIVIDES;
			default:
				throw new RuntimeException("internal error: unknown type \""
						+ this + "\"");
			}
		}
	}

	public static class ASTConstr {
		private LinearConstr lhs, rhs;
		private ASTConstrType op;

		public String toString() {
			return lhs + " " + op + " " + rhs;
		}

		public ASTConstr(LinearConstr aLhs, LinearConstr aRhs, ASTConstrType aOp) {
			lhs = aLhs;
			rhs = aRhs;
			op = aOp;
		}

		public ASTConstr(ASTConstr other) {
			lhs = other.lhs;
			rhs = other.rhs;
			op = other.op;
		}

		public ASTConstr copy() {
			return new ASTConstr(this);
		}

		public BENode getConstr(boolean negate, BENode parent) {

			if (negate) {
				op = op.neg();
			}

			// handle modulo constraint
			if (op == ASTConstrType.DIVIDES || op == ASTConstrType.NOTDIVIDES) {
				LinearConstr aux = new LinearConstr(rhs);
				int divisor = lhs.remove(null).coeff();
				switch (op) {
				case DIVIDES:
					return new BENode(parent, new ModuloConstr(divisor, aux));
				case NOTDIVIDES:
					if (divisor == 2) {
						aux.addLinTerm(new LinearTerm(null, 1));
						return new BENode(parent,
								new ModuloConstr(divisor, aux));
					} else {
						BENode ret = new BENode(parent, BENodeType.OR);
						for (int i = 1; i < divisor; i++) {
							LinearConstr cp = new LinearConstr(aux);
							cp.addLinTerm(new LinearTerm(null, i));
							ret.addSucc(new BENode(ret, new ModuloConstr(
									divisor, cp)));
						}
						return ret;
					}
				}
				// handle linear constraint
			} else {

				LinearConstr aux = new LinearConstr(lhs);
				for (LinearTerm lt : rhs.terms())
					aux.addLinTerm(lt.times(-1));

				switch (op) {
				case LEQ: {
					return new BENode(parent, aux);
				}
				case GEQ: {
					aux.transformBetweenGEQandLEQ();
					return new BENode(parent, aux);
				}
				case EQ: {
					// >=
					LinearConstr aux2 = new LinearConstr(aux);
					aux2.transformBetweenGEQandLEQ();

					BENode ret = new BENode(parent, BENodeType.AND);
					ret.addSucc(new BENode(ret, aux)); // <=
					ret.addSucc(new BENode(ret, aux2)); // >=
					return ret;
				}
				case NEQ: {
					LinearConstr aux2 = new LinearConstr(aux);
					// <
					aux.addLinTerm(LinearTerm.constant(1));
					// >
					aux2.transformBetweenGEQandLEQ();
					aux2.addLinTerm(LinearTerm.constant(1));

					BENode ret = new BENode(parent, BENodeType.OR);
					ret.addSucc(new BENode(ret, aux)); // <
					ret.addSucc(new BENode(ret, aux2)); // >
					return ret;
				}
				case LESS: {
					aux.addLinTerm(LinearTerm.constant(1));
					return new BENode(parent, aux);
				}
				case GREATER: {
					aux.transformBetweenGEQandLEQ();
					aux.addLinTerm(LinearTerm.constant(1));
					return new BENode(parent, aux);
				}
				}
			}

			throw new RuntimeException("internal error: unknown type \"" + op
					+ "\"");
		}
	}

	public static enum BENodeType {
		AND, OR, NOT, TRUE, FALSE, ATOM, ATOMFIN,
		/*
		 * types for relational calculator ID -- used for storing values
		 * priority of closure operators is higher than of any boolean operator
		 * priority of composition operator is higher than of disjunction an
		 * lower than of conjunction
		 */
		ID, COMPOSE, EXISTS, CLOSURE_PLUS, CLOSURE_STAR, CLOSURE_PLUS_N, CLOSURE_STAR_N, CLOSURE_EXPR,
		
		ABSTR_D, ABSTR_O, ABSTR_L,
		
		DOMAIN, RANGE;

		public boolean isClosure() {
			switch (this) {
			case CLOSURE_PLUS:
			case CLOSURE_STAR:
			case CLOSURE_PLUS_N:
			case CLOSURE_STAR_N:
			case CLOSURE_EXPR:
				return true;
			default:
				return false;
			} 
		}
		public boolean isAbstr() {
			switch (this) {
			case ABSTR_D:
			case ABSTR_O:
			case ABSTR_L:
				return true;
			default:
				return false;
			}
		}

	}

	private BENodeType type;

	public BENodeType type() {
		return type;
	}

	// for leaves
	private ASTConstr atom; // first, keep a more syntactical representation
	private Constr atomFin; // then (after formula is normalized), translate it
	// to constraint(s)

	private BENode pred;
	private LinkedList<BENode> succ = new LinkedList<BENode>();

	public String toString() {
		String a = "";
		if (atom != null) {
			a = "," + atom.toString();
		} else if (atomFin != null) {
			a = "," + atomFin.toString();
		}
		return "(" + type + a + ")";
	}

	// doesn't create any graph connections
	public BENode(BENode other) {
		type = other.type;
		if (other.atom != null)
			atom = other.atom.copy();
		if (other.atomFin != null)
			atomFin = other.atomFin.copy();
	}

	public void pred(BENode aPred) {
		pred = aPred;
	}

	public void addSucc(BENode n) {
		succ.add(n);
	}

	public void removeSucc(BENode n) {
		succ.remove(n);
	}

	public BENode(BENode aPred, BENodeType aType) {
		pred = aPred;
		type = aType;
	}

	public BENode(BENode aPred, ASTConstr aConstr) {
		pred = aPred;
		type = BENodeType.ATOM;
		atom = aConstr;
	}

	public BENode(BENode aPred, Constr aConstr) {
		pred = aPred;
		type = BENodeType.ATOMFIN;
		atomFin = aConstr;
	}
	
	// for calculator
	private DisjRel calc_rel = null;
	private Integer calc_int = null;
	private String calc_id = null; // id of a stored relation
	private Variable calc_var = null; // parameter of a closure R^n
	private List<Variable> calc_exists = null; // existentially quantified variables
	private LinearConstr closure_expr = null; // R^(b+c-1)
	public DisjRel calc_rel() { return calc_rel; }
	public Integer calc_int() { return calc_int; }
	public String calc_id() { return calc_id; }
	
	public static BENode calc_id(BENode aPred, String aId) {
		BENode ret = new BENode(aPred,BENodeType.ID);
		ret.calc_id = aId;
		return ret;
	}
	public static BENode calc_exists(BENode aPred, List<Variable> aVar) {
		BENode ret = new BENode(aPred,BENodeType.EXISTS);
		ret.calc_exists = aVar;
		return ret;
	}
	public static BENode calc_closure_star(BENode aPred) {
		BENode ret = new BENode(aPred,BENodeType.CLOSURE_STAR);
		return ret;
	}
	public static BENode calc_closure_plus(BENode aPred) {
		BENode ret = new BENode(aPred,BENodeType.CLOSURE_PLUS);
		return ret;
	}
	public static BENode calc_closure_star_n(BENode aPred, Variable aVar) {
		BENode ret = new BENode(aPred,BENodeType.CLOSURE_STAR_N);
		ret.calc_var = aVar;
		return ret;
	}
	public static BENode calc_closure_plus_n(BENode aPred, Variable aVar) {
		BENode ret = new BENode(aPred,BENodeType.CLOSURE_PLUS_N);
		ret.calc_var = aVar;
		return ret;
	}
	
	public static BENode calc_closure_expr(BENode aPred, LinearConstr aC) {
		BENode ret = new BENode(aPred,BENodeType.CLOSURE_EXPR);
		ret.closure_expr = aC;
		return ret;
	}
	
	public static BENode abstr_d(BENode aPred) {
		BENode ret = new BENode(aPred,BENodeType.ABSTR_D);
		return ret;
	}
	public static BENode abstr_o(BENode aPred) {
		BENode ret = new BENode(aPred,BENodeType.ABSTR_O);
		return ret;
	}
	public static BENode abstr_l(BENode aPred) {
		BENode ret = new BENode(aPred,BENodeType.ABSTR_L);
		return ret;
	}

	/*------------------------- normalization of a formula -----------------------------*/
	/*----------- (using simplifications, NNF and DNF transformations ) ----------------*/

	private BENode simplifyAndOr() {

		/*
		 * AND AND / \ ---> / | \ a AND a b c / \ b c
		 */
		if (type == BENodeType.AND || type == BENodeType.OR) {
			List<BENode> aux = new LinkedList<BENode>();
			Iterator<BENode> iter = succ.iterator();
			while (iter.hasNext()) {
				BENode nn = iter.next();
				if (type.equals(nn.type)) {
					for (BENode nnn : nn.succ)
						nnn.pred(this);
					aux.addAll(nn.succ);
					iter.remove();
				}
			}
			succ.addAll(aux);
		}

		// simplify the boolean expression if TRUE or FALSE are present
		if (type == BENodeType.AND) {
			Iterator<BENode> iter = succ.iterator();
			while (iter.hasNext()) {
				BENode nn = iter.next();
				switch (nn.type) {
				case FALSE: // the whole expression gets FALSE
					return new BENode(this.pred, BENodeType.FALSE);
				case TRUE: // remove all TRUEs
					iter.remove();
					break;
				}
			}
			if (succ.isEmpty()) { // empty conjunction is vacuously TRUE
				return new BENode(this.pred, BENodeType.TRUE);
			} else if (succ.size() == 1) { // reduce node with only one
				// successor
				BENode ret = succ.get(0);
				ret.pred = pred;
				return ret;
			}
		} else if (type == BENodeType.OR) {
			Iterator<BENode> iter = succ.iterator();
			while (iter.hasNext()) {
				BENode nn = iter.next();
				switch (nn.type) {
				case FALSE: // remove all FALSEs
					iter.remove();
					break;
				case TRUE: // the whole expression gets TRUE
					return new BENode(this.pred, BENodeType.TRUE);
				}
			}
			if (succ.isEmpty()) { // empty disjunction is vacuously FALSE
				return new BENode(this.pred, BENodeType.FALSE);
			} else if (succ.size() == 1) { // reduce node with only one
				// successor
				BENode ret = succ.get(0);
				ret.pred = pred;
				return ret;
			}
		}

		return this;
	}

	// negation normal form
	private BENode nnfAndSimplif(boolean prop) {

		switch (type) {
		case TRUE:
			if (prop)
				type = BENodeType.FALSE;
			return this;
		case FALSE:
			if (prop)
				type = BENodeType.TRUE;
			return this;
		case NOT: {
			BENode ret = succ.get(0);
			ret.pred = this.pred;
			return ret.nnfAndSimplif(!prop);
		}
		case ATOM:
			return atom.getConstr(prop, this.pred);
		case ATOMFIN:
			throw new RuntimeException("Internal error: unexpected type "
					+ type);
		case AND:
		case OR: {
			if (prop) {
				if (type == BENodeType.AND) {
					type = BENodeType.OR;
				} else {
					type = BENodeType.AND;
				}
			}

			int l = succ.size();
			for (int i = 0; i < l; i++) {
				succ.set(i, succ.get(i).nnfAndSimplif(prop));
			}

			return this.simplifyAndOr();
		}
		default:
			throw new RuntimeException("Internal error: unexpected type "
					+ type);
		}
	}

	private BENode createConjunction(List<BENode> c1, List<BENode> c2) {
		BENode ret = new BENode(null, BENodeType.AND);
		for (BENode n : c1)
			ret.addSucc(new BENode(n));
		for (BENode n : c2)
			ret.addSucc(new BENode(n));
		return ret;
	}

	// assumption: tree contains only AND, OR nodes and atomic propositions
	private List<BENode> dnfBase() {

		switch (type) {
		case ATOMFIN: {
			List<BENode> ret = new LinkedList<BENode>();
			BENode aux = new BENode(null, BENodeType.AND);
			aux.addSucc(this);
			ret.add(aux);
			return ret;
		}
		case OR: {
			List<BENode> ret = new LinkedList<BENode>();
			for (BENode n : succ) {
				List<BENode> conjunctions = n.dnfBase();
				ret.addAll(conjunctions);
			}
			return ret;
		}
		case AND: {
			Iterator<BENode> iter = succ.iterator();
			List<BENode> cs1 = iter.next().dnfBase(); // conjunctions
			while (iter.hasNext()) {
				List<BENode> cs2 = iter.next().dnfBase(); // conjunctions
				List<BENode> aux = new LinkedList<BENode>();
				for (BENode c1 : cs1) {
					for (BENode c2 : cs2) {
						aux.add(createConjunction(c1.succ, c2.succ));
					}
				}
				cs1 = aux;
			}
			return cs1;
		}
		default: {
			throw new RuntimeException("internal error: unknown type \"" + type
					+ "\"");
		}
		}

	}

	public BENode dnf() {
		List<BENode> conjunctions = dnfBase();

		// balanced tree is obtained --> simplify; and add backward links

		for (int i = 0; i < conjunctions.size(); i++) {
			BENode n = conjunctions.get(i);
			if (n.succ.size() == 1) {
				conjunctions.set(i, n.succ.get(0));
			} else {
				for (BENode nn : n.succ) {
					nn.pred(n);
				}
			}
		}

		if (conjunctions.size() == 1) {
			return conjunctions.get(0);
		} else {
			BENode root = new BENode(null, BENodeType.OR);
			for (BENode c : conjunctions) {
				c.pred(root);
				root.addSucc(c);
			}
			return root;
		}
	}

	
	public static BENode normalize(BENode start) {
		BENode ret = start.nnfAndSimplif(false); // negation normal form and
		// simplification

		if (ret.type == BENodeType.TRUE || ret.type == BENodeType.FALSE) {
			return ret;
		}

		ret = ret.dnf(); // disjunctive normal form
		return ret;
	}

	/*------------------------- conversion from dnf to relations -----------------------------*/
	/*------------------- (list of modulo relations as the most general type) ----------------*/
	private static String errDnf2Rel = "internal error (dnf2rels)";

	private Constr dnf2RelsAtom() {
		if (type == BENodeType.ATOMFIN && atomFin != null) {
			return atomFin;
		} else {
			throw new RuntimeException(errDnf2Rel);
		}
	}

	private ModuloRel dnf2RelsAnd() {
		ModuloRel ret = new ModuloRel();
		if (type == BENodeType.AND) {
			for (BENode n : succ) {
				ret.addConstraint(n.dnf2RelsAtom());
			}
		} else {
			ret.addConstraint(this.dnf2RelsAtom());
		}
		return ret;
	}

	private List<ModuloRel> dnf2RelsOr() {
		List<ModuloRel> ret = new LinkedList<ModuloRel>();
		if (type == BENodeType.OR) {
			for (BENode n : succ) {
				ret.add(n.dnf2RelsAnd());
			}
		} else {
			ret.add(this.dnf2RelsAnd());
		}
		return ret;
	}

	public List<ModuloRel> dnf2Rels() {

		if (type == BENodeType.TRUE || type == BENodeType.FALSE) {
			List<ModuloRel> ret = new LinkedList<ModuloRel>();
			if (type == BENodeType.TRUE) {
				ret.add(new ModuloRel());
			}
			return ret;
		}

		return dnf2RelsOr();
	}

	
	// disallowed for standard CA input (allowed only for calculator): EXISTS, COMPOSE, CLOSURE
	public static void checkOperators(BENode start) {
		List<BENode> todo = new LinkedList<BENode>();
		todo.add(start);
		while (!todo.isEmpty()) {
			BENode n = todo.remove(0);
			switch (n.type) {
			case EXISTS:
				System.err.println("'Exists' operator is not allowed on labels.");
				System.exit(1);
			case CLOSURE_PLUS:
			case CLOSURE_STAR:
			case CLOSURE_PLUS_N:
			case CLOSURE_STAR_N:
				System.err.println("Closure operator is not allowed on labels.");
				System.exit(1);
			case ID:
				System.err.println("Syntactically invalid atomic relation: "+n.calc_id);
				System.exit(1);
			case COMPOSE:
				if (n.succ.size() == 1) { // reconnect
					BENode s = n.succ.get(0);
					n.pred.removeSucc(n);
					n.pred.addSucc(s);
					s.pred(n.pred);
				} else {
					System.err.println("Composition operator is not allowed on labels.");
					System.exit(1);
				}
			}
			
			todo.addAll(n.succ);
		}
	}
	
	public BENode processAtoms() {
		return processAtoms(pred);
	}
	private BENode processAtoms(BENode aPred) {
		
		if (type == BENodeType.ATOM) {
			return atom.getConstr(false, aPred);
		} else {
			for (int i=0; i<succ.size(); i++) {
				succ.add(i,succ.remove(i).processAtoms(this));
			}
			return this;
		}
		
	}
	
	
	// evaluates expressions following the syntactic tree
	// TODO: group the bottom-most conjunctions to avoid creating a special relations (DisjRel) for each atomic constraint
	// TODO: add n'=n constraint to each relation in the scope of a parameter n 
	//   note: the scope starts
	//  rule: if an operand uses a parameter p, then all the operands should imply p'=p
	//  during evaluation, collect parameters to know that when eliminating \exists n, one should eliminate n' too
	// propagates parameters from nodes upwards
	public Set<Variable> eval(VariablePool pool, VariablePool relvarPool, Map<String, DisjRel> m, Map<Variable, Integer> mI, Map<String,Set<Variable>> params) {
		// closure parameters n from R^n
		Set<Variable> pp = new HashSet<Variable>();
		
		// evaluate successors
		for (BENode n : succ) {
			pp.addAll(n.eval(pool, relvarPool, m, mI, params));
		}
		
		if (succ.size() == 1) {
			calc_int = succ.get(0).calc_int;
		}
		
		// treat parameters
		if (pp.size() != 0) {
			// add n'=n for parameters in the scope
			if (succ.size() != 1) {
				Variable[] unpsort = pp.toArray(new Variable[0]);
				Arrays.sort(unpsort);
				for (BENode n : succ) {
					n.calc_rel.addImplicitActionsForSorted(unpsort);
				}
			} // or else the work has already been done
		}
		
		switch (type) {
		case ATOM:
			throw new RuntimeException();
		case ATOMFIN:
			ModuloRel mr = new ModuloRel();
			mr.addConstraint(atomFin);
			this.calc_rel = new DisjRel(new CompositeRel(Relation.toMinType(mr)));
			break;
		case ID:
			
			// try integer variables
			
			Variable vv = pool.giveVariable(calc_id);
			Integer ii = mI.get(vv);
			if (ii != null) {
				calc_int = ii;
				return pp;
			}
			
			calc_rel = m.get(calc_id);
			if (calc_rel == null) {
				System.err.println("Undefined symbol: "+calc_id);
				System.exit(1);
			}
			pp = params.get(calc_id);
			break;
		case COMPOSE:
			calc_rel = succ.get(0).calc_rel.copy();
			for (int i=1; i<succ.size(); i++) {
				calc_rel = calc_rel.compose(succ.get(i).calc_rel);
			}
			break;
		case OR:
			calc_rel = succ.get(0).calc_rel.copy();
			for (int i=1; i<succ.size(); i++) {
				calc_rel.addDisj(succ.get(i).calc_rel);
			}
			break;
		case AND:
			calc_rel = succ.get(0).calc_rel.copy();
			for (int i=1; i<succ.size(); i++) {
				calc_rel = calc_rel.and(succ.get(i).calc_rel);
			}
			break;
		case EXISTS:
			calc_rel = succ.get(0).calc_rel.copy();
			for (Variable v : calc_exists) {
				calc_rel = calc_rel.existElim1(v);
				if (pp.contains(v)) {
					calc_rel = calc_rel.existElim1(v.getCounterpart());
				}
				pp.remove(v);
			}
			break;
		case CLOSURE_STAR:
			calc_rel = succ.get(0).calc_rel.closureStar(relvarPool);
			break;
		case CLOSURE_PLUS:
			calc_rel = succ.get(0).calc_rel.closurePlus(relvarPool);
			break;
		case CLOSURE_STAR_N:
			calc_rel = succ.get(0).calc_rel.closureN(false, calc_var);
			pp.add(calc_var);
			break;
		case CLOSURE_PLUS_N:
			calc_rel = succ.get(0).calc_rel.closureN(true, calc_var);
			pp.add(calc_var);
			break;
		case CLOSURE_EXPR:
			Variable v = pool.giveVariable("$n$"); 
			calc_rel = succ.get(0).calc_rel.closureN(false, v);
			int power = this.closure_expr.evaluate(mI);
			
			DBC dbc1 = new DBC(v, null, new IntegerInf(power)); // n <= power
			DBC dbc2 = new DBC(null, v, new IntegerInf(-power)); // n >= power  <-->  -n <= -power
			DBC[] dbConstrs = new DBC[] {dbc1, dbc2};
			Variable[] vars_unpP = new Variable[] {v, v.getCounterpart()};
			DBRel force = new DBRel(dbConstrs,vars_unpP);
			
			calc_rel = calc_rel.and(new DisjRel(new CompositeRel(force)));
			calc_rel = calc_rel.existElim1(v);
			
			break;
		
		case ABSTR_D:
			calc_rel = succ.get(0).calc_rel.hull(RelType.DBREL);
			break;
		case ABSTR_O:
			calc_rel = succ.get(0).calc_rel.hull(RelType.OCTAGON);
			break;
		case ABSTR_L:
			calc_rel = succ.get(0).calc_rel.hull(RelType.LINEAR);
			break;
		
		case DOMAIN:
			calc_rel = succ.get(0).calc_rel.domain();
			break;
		case RANGE:
			calc_rel = succ.get(0).calc_rel.range();
			break;
			
		case FALSE:
			calc_rel = new DisjRel(new CompositeRel[0]);
			break;
		case TRUE:
			calc_rel = new DisjRel(new CompositeRel(new LinearRel()));
			break;
		default:
			throw new RuntimeException();
		}
		
		return pp;
	}
	
}