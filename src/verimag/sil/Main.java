package verimag.sil;

import java.io.*;

import java.util.*;

import nts.parser.*;
import verimag.flata.automata.ca.CA;
import verimag.flata.automata.ca.CAState;
import verimag.flata.automata.ca.CATransition;
import verimag.flata.automata.ca.CENode;
import verimag.flata.automata.ca.CEView;
import verimag.flata.common.*;
import verimag.flata.presburger.CompositeRel;
import verimag.flata.presburger.DisjRel;
import verimag.flata.presburger.LinearRel;
import verimag.flata.presburger.Variable;

public class Main {
	
	private static ResultSIL parse(File inputFile) {
		InputStream is = null;
	    try {
			is = new FileInputStream(inputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	    
	    ParserListener listen = new ParserListener();
	    NTSParser.parseSIL(is, listen);
	    return listen.retriveResultSIL();
	}
	
	public static void main(String[] args) {

		String[] argsext = new String[args.length+2];
		argsext[0] = "-t-fullincl";
		argsext[1] = "-t-merge-prec";
		System.arraycopy(args, 0, argsext, 2, args.length);
		
		File inputFile = CR.processParameters(args);
		verimag.flata.Main.initActions();
		
		long start = System.currentTimeMillis();
		
		ResultSIL res = parse(inputFile);
		boolean checkSAT = res.isCheckSat();
		VarTable vt = res.getVarTable();
		Expr ex = (Expr)res.getExpr();
		vt.semanticChecks();
		ex.semanticChecks(vt);
		
		SymbolTable st = new SymbolTable(vt);
		/*
		 * remove some nested quantifiers if possible
		 */
		SILVisitor_NestedQuant v_quant = new SILVisitor_NestedQuant(st.vt);
		ex.accept(v_quant);
		ex = v_quant.getResult();
		/*
		 * SILVisitor does substitutions p/a[par+const] and 
		 * conjoins forall i. i=par+const => p=a[i] to the resulting formula
		 * This is correct of one checks SATISTIABILITY, but not VALIDITY !!
		 * therefore, one needs to reduce VALIDITY to UNSATISFIABILITY as the very first step !!
		 */
		if (!checkSAT) {
			ex = ASTWithoutToken.exNot(ex);
		}
		
		SILVisitor v = new SILVisitor(st);
		Node root = v.translate(ex);
		
		//System.out.println(root);
		
		boolean b = checkSAT(root,st);
		String s = (checkSAT)? ( b? "SAT" : "UNSAT" ) : (b? "FALSIFIABLE" : "VALID");
		System.out.println("\nFormula is "+s);
		
		System.out.println("running time: "+((float)(System.currentTimeMillis()-start))/1000+"s");
		
		verimag.flata.Main.finalActions();
		System.exit(0);
	}

	private static boolean checkSAT(Node root, SymbolTable st) {
		/*
		for each disjunct of the DNF
		  1. collect Presburger constraints into one conjunctive relation R and create automaton
		       qi --[R && copy(scalars,N)]--> qf
		       qf --[copy(scalars,N)]--> qf
		  2. for each array property, construct one automaton
		  3. if there is no array property in point 2, create an unconstrained array automaton: 
		       qi --[i=0 && i'=i+1 && N=1]--> qf
		       qi --[i=0 && i'=i+1 && N>1]--> q1
		       q1 --[i<N-1 && i'=i+1]--> q1
		       q1 --[i=N-1 && i'=i+1]--> qf
		  4. compute the product of automata obtained by points 1,2,3
		*/
		
		root = root.normalize(st);
		//System.out.print("\n"+n_norm);
		root = root.nnf();
		//System.out.print("\nNNF\n"+root+"\n");
		Node.DnfForm n_dnf = root.dnf();
		//System.out.print("\nDNF\n"+n_dnf+"\n");
		n_dnf.toSB();
		for (Node.Clause clause : n_dnf.clauses()) {

			CA aux = createCA(clause,st);
			
			//long start = System.currentTimeMillis();
			StopReduction ee = null;
			try {
				st.toString();
				System.out.println("reducing");
				aux.reduce(true);
			} catch (StopReduction e) {
				ee = e;
			}
			//reduce_time += ( System.currentTimeMillis() - start );
	
			if (ee != null) {
				
				Collection<CENode> nodes = aux.ce_nodes();
				
				CENode best = CENode.rootWithShortestRealTrace(nodes);
				
				CEView cev = best.prepareTraceView();
				//StringBuffer sbTrace = cev.toSB();
				
				System.out.println("");
				decodeTrace(cev, st);
				
				return true;
			}
		}
		return false;
	}
	
	private static CA createCA(Node.Clause clause, SymbolTable st) {
		DisjRel presb = DisjRel.giveTrue();
		List<Node.Literal> prop = new LinkedList<Node.Literal>();
		for (Node.Literal lit : clause.literals()) {
		//for (Node conjunct : disjunct.collectConjuncts()) {
			Node.NodeLeaf leaf = lit.leaf();
			assert(leaf instanceof Node.NodeForallConjunctive || leaf instanceof Node.NodePresburger);
			if (leaf instanceof Node.NodePresburger) {
				DisjRel aux = ((Node.NodePresburger)leaf).rel();
				if (lit.isNegated())
					aux = aux.not();
				presb = presb.and(aux);
			} else {
				prop.add(lit);
			}
		}
		CA aux = null;
		if (prop.isEmpty()) {
			aux = Node.NodeForallConjunctive.caForallDummy(st);
		} else {
			Iterator<Node.Literal> i = prop.iterator();
			Node.Literal lit = i.next();
			aux = ((Node.NodeForallConjunctive)lit.leaf()).toCA(st,lit.isNegated());
			while (i.hasNext()) {
				lit = i.next();
				CA aux2 = ((Node.NodeForallConjunctive)lit.leaf()).toCA(st,lit.isNegated());
				aux = CA.product(aux,aux2);
			}
		}
		aux = CA.product(aux, caPresb(presb,st));
		return aux;
	}
	private static CA caPresb(DisjRel presb, SymbolTable aST) {
		CA ca = new CA("aux",aST.vp);
		CAState qi = ca.getStateWithName("qi");
		CAState qe = ca.getStateWithName("qf");
		
		ca.setInitial(qi);
		ca.setError(qe);
		
		CompositeRel identity = new CompositeRel(LinearRel.createIdentity(aST.allScalars()));
		// qf --[copy(scalars,N)]--> qf
		ca.addTransitionOnlySat(new CATransition(qe,qe,identity,ca));
		
		// qi --[R && copy(scalars,N)]--> qf
		for (CompositeRel disj : presb.disjuncts()) {
			for (CompositeRel r : identity.intersect(disj)) {
				ca.addTransitionOnlySat(new CATransition(qi,qe,r,ca));
			}
		}
			
		return ca;
	}
	
	public static void decodeTrace(CEView view, SymbolTable st) {
		Map<String,Integer[]> m = view.extractLocalTracesOfMain();
		
		List<Variable> scalars = st.origScalars();
		Collections.sort(scalars);
		
		List<Variable> arrays = st.origArrays();
		Collections.sort(arrays);
		
		//list.addAll(arrays);
		
		System.out.println("Decoding of the CA:");
		
		String indent="  ";
		String ANY = "ANY";
		
		for (Variable ss : scalars) {
			
			String s = ss.name();
			Integer[] vals = m.get(s);
			
			System.out.print(indent+s+"=");
			System.out.println((vals[0] != null)? vals[0].intValue() : ANY);
		}
		for (Variable ss : arrays) {
			
			String s = ss.name();
			Integer[] vals = m.get(s);
			
			System.out.print(indent+s+"=[");
			int l = vals.length;
			for (int j=0; j<l-1; j++) {
				System.out.print((vals[j] != null)? vals[j] : "-");
				if (j!=l-2)
					System.out.print(",");
			}
			System.out.println("]");
		}
	}
	
}
