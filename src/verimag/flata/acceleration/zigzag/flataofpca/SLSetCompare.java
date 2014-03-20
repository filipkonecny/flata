package verimag.flata.acceleration.zigzag.flataofpca;

import java.io.StringWriter;

import verimag.flata.acceleration.zigzag.*;
import verimag.flata.common.CR;
import verimag.flata.common.IndentedWriter;
import verimag.flata.common.YicesAnswer;

// PresTAF
import application.*;

public class SLSetCompare {

	private static boolean bb = true;
	
	private static void toSBYices(IndentedWriter iw, LinSet ls, String k, String n, String dummy) { 		
		Point base = ls.getBase();
		Point gen = ls.getGenerator();
		
		int a = base.getLength();
		int b = base.getWeight();
		int c = (gen == null)? 0 : gen.getLength();
		int d = (gen == null)? 0 : gen.getWeight();
		
		iw.writeln("(= "+n+" (+ "+a+" (* "+k+" "+c+")))");
		iw.writeln("(<= "+dummy+" (+ "+b+" (* "+k+" "+d+")))");
	}
	private static void toSBYices(IndentedWriter iw, SLSet sls, String n, String dummy) {
		
		if (sls == null || sls.empty()) {
			iw.writeln("true");
			return;
		}
		
		String k = "k";
		
		// ########################################
		iw.writeln("(forall ("+k+"::int)");
		iw.indentInc();
		
		iw.writeln("(and ");
		iw.indentInc();
		
		for (LinSet ls : sls.getLinearSets()) {
			iw.writeln("(=> ");
			iw.indentInc();
			
			toSBYices(iw,ls,k,n,dummy);
			
			iw.indentDec();
			iw.writeln(")"); // =>
		}
		
		iw.indentDec();
		iw.writeln(")"); // and
		
		iw.indentDec();
		iw.writeln(")"); // forall
		
	}
	public static void implicationYices(IndentedWriter iw, SLSet sls1, SLSet sls2, String n, String dummy) {
		iw.writeln("(=> ");
		iw.indentInc();
		
		toSBYices(iw,sls1,n,dummy);
		toSBYices(iw,sls2,n,dummy);
		
		iw.indentDec();
		iw.writeln(")"); // =>
	}
	public static YicesAnswer equalYices(SLSet sls1, SLSet sls2) {
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw);
		
		//String k = "k";
		String n = "n";
		String dummy = "x";
		
		iw.writeln("(reset)\n");
		//iw.writeln("(define "+k+"::int)");
		iw.writeln("(define "+n+"::int)");
		if (bb) iw.writeln("(define "+dummy+"::int)");
		
		iw.writeln("(assert ");
		iw.indentInc();
		
		iw.writeln("(and ");
		iw.indentInc();
		
		iw.writeln("(>= "+n+" 1)");
		
		iw.writeln("(not ");
		iw.indentInc();
		
		iw.writeln("(and ");
		iw.indentInc();
		
		implicationYices(iw,sls1,sls2,n,dummy);
		implicationYices(iw,sls2,sls1,n,dummy);
		
		iw.indentDec();
		iw.writeln(")"); // and
		
		iw.indentDec();
		iw.writeln(")"); // not
		
		iw.indentDec();
		iw.writeln(")"); // and
		
		iw.indentDec();
		iw.writeln(")"); // assert
		
		iw.writeln("(set-evidence! true)");
		iw.writeln("(check)");
		
		StringBuffer sb = sw.getBuffer();
		StringBuffer sb_core = new StringBuffer();
		
		YicesAnswer a = CR.isSatisfiableYices(sb, sb_core);

		if (a == YicesAnswer.eYicesSAT) {
			//System.out.println(sb);
		}
		
		return a;
	}

	// ##########################################################################
	
	private static Presburger ls2presb(LinSet ls, String n, String k, String dummy) {
		Point base = ls.getBase();
		Point gen = ls.getGenerator();
		
		int a = base.getLength();
		int b = base.getWeight();
		int c = (gen == null)? 0 : gen.getLength();
		int d = (gen == null)? 0 : gen.getWeight();
		
		Presburger p1 = Presburger.equals(Term.variable(n),Term.integer(a).plus(Term.factor(c,k)));
		Presburger p2 = Presburger.equals(Term.variable(dummy), Term.integer(b).plus(Term.factor(d, k)));
		
		return p1.imply(p2).bracket();
	}
	
	private static Presburger sls2presb(SLSet sls, String n, String k, String dummy) {
		// construct true
		Presburger p = Presburger.equals(Term.variable(n), Term.variable(n));
		
		for (LinSet ls : sls.getLinearSets()) {
			p = p.and(ls2presb(ls, n, k, dummy));
		}
		
		Presburger n1 = Presburger.greaterEquals(Term.variable(n), Term.integer(1));
		return p.bracket().forall(Term.getIndex(k), k).and(n1);
	}
	
	public static boolean equalPrestaf(SLSet sls1, SLSet sls2) {
		String n = "n";
		String k = "k";
		String dummy = "X";
		
		Presburger p1 = sls2presb(sls1,n,k,dummy);
		Presburger p2 = sls2presb(sls2,n,k,dummy);
		
		Presburger p = p1.equiv(p2);
		return p.getNPF().isOne();
	}
	
	// ##########################################################################

	private static Presburger prestaf_ls2presb(LinSet ls, String n, String v, String k) {
		Point base = ls.getBase();
		Point gen = ls.getGenerator();
		
		int a = base.getLength();
		int b = base.getWeight();
		int c = (gen == null)? 0 : gen.getLength();
		int d = (gen == null)? 0 : gen.getWeight();
		
		Presburger p1 = Presburger.equals(Term.variable(n),Term.integer(a).plus(Term.factor(c,k)));
		Presburger p2 = Presburger.equals(Term.variable(v), Term.integer(b).plus(Term.factor(d, k)));
		
		return p1.and(p2).bracket();
	}
	
	private static Presburger prestaf_sls2presb(LinSet[] sls, String n, String v, String k) {
		// construct false !!! BE CAREFUL
		//Presburger p = Presburger.notEquals(Term.variable(n), Term.variable(n));
		
		Presburger p = null;
		
		for (LinSet ls : sls) {
			if (p != null)
				p = p.or(prestaf_ls2presb(ls, n, v, k));
			else
				p = prestaf_ls2presb(ls, n, v, k);
		}
		
		if (p == null) // case when sls does not contain any lin. constraint
			p = Presburger.notEquals(Term.variable(n), Term.variable(n));
		
		Presburger kGEQ0 = Presburger.greaterEquals(Term.variable(k), Term.integer(0));
		return p.bracket().and(kGEQ0).bracket().exists(Term.getIndex(k), k).bracket(); //.forall(Term.getIndex(k), k).and(n1);
	}
	
	public static Presburger n_geq1(String n) {
		return Presburger.greaterEquals(Term.variable(n), Term.integer(1));
	}
	public static Presburger n_geq1(Presburger p, String n) {
		return n_geq1(n)
			.imply(p).bracket();
	}
	public static Presburger forall_n_geq1(Presburger p, String n) {
		return n_geq1(p,n)
			.forall(Term.getIndex(n), n);
	}
	
	// TODO
	public static boolean prestaf_checkMinimalSum(LinSet[] op1, LinSet[] op2, LinSet[] res) {
		String n = "n";
		String n1 = "n1";
		String n2 = "n2";
		String v = "v";
		String v1 = "v1";
		String v2 = "v2";
		String k = "k";
		
		Presburger p_op1 = prestaf_sls2presb(op1,n1,v1,k);
		Presburger p_op2 = prestaf_sls2presb(op2,n2,v2,k);
		Presburger p_res = prestaf_sls2presb(res,n,v,k);
		
		Presburger n_EQ_n1n2 = Presburger.equals(Term.variable(n), Term.variable(n1).plus(Term.variable(n2)));
		Presburger v_EQ_v1v2 = Presburger.equals(Term.variable(v), Term.variable(v1).plus(Term.variable(v2)));
		Presburger v_LEQ_v1v2 = Presburger.lessEquals(Term.variable(v), Term.variable(v1).plus(Term.variable(v2)));
		Presburger n1_GEQ_0 = Presburger.greaterEquals(Term.variable(n1), Term.integer(0));
		Presburger n2_GEQ_0 = Presburger.greaterEquals(Term.variable(n2), Term.integer(0));
		
		Presburger p1 = p_op1.and(p_op2).and(n_EQ_n1n2).and(v_EQ_v1v2).and(n1_GEQ_0).and(n2_GEQ_0).bracket()
		.exists(Term.getIndex(v2), v2).exists(Term.getIndex(v1), v1).exists(Term.getIndex(n2), n2).exists(Term.getIndex(n1), n1);
		
		Presburger p2 = p_res.imply(p1).bracket().forall(Term.getIndex(v), v);
		p2 = forall_n_geq1(p2, n);
		
		/*p_op1 = prestaf_sls2presb(op1,n1,v1,k);
		p_op2 = prestaf_sls2presb(op2,n2,v2,k);
		p_res = prestaf_sls2presb(res,n,v,k);
		
		n_EQ_n1n2 = Presburger.equals(Term.variable(n), Term.variable(n1).plus(Term.variable(n2)));
		v_EQ_v1v2 = Presburger.equals(Term.variable(v), Term.variable(v1).plus(Term.variable(v2)));
		v_LEQ_v1v2 = Presburger.lessEquals(Term.variable(v), Term.variable(v1).plus(Term.variable(v2)));
		n1_GEQ_0 = Presburger.greaterEquals(Term.variable(n1), Term.integer(0));
		n2_GEQ_0 = Presburger.greaterEquals(Term.variable(n2), Term.integer(0));*/
		
		Presburger p3 = p_res.and(v_LEQ_v1v2).bracket().exists(Term.getIndex(v), v);
		Presburger p4 = p_op1.and(p_op2).and(n_EQ_n1n2).and(n1_GEQ_0).and(n2_GEQ_0).bracket();
		Presburger p5 = p4.imply(p3).bracket()
		.forall(Term.getIndex(v2), v2).forall(Term.getIndex(v1), v1).forall(Term.getIndex(n2), n2).forall(Term.getIndex(n1), n1);
		p5 = forall_n_geq1(p5, n);
		
		Presburger p = p2.and(p5);

		return p.getNPF().isOne();
	}
	
	// semi-linear set S is a function from set of lengths to set of weights
	// A.n n<1 \/ A.v1 A.v2 (f_S(n,v1) /\ f_S(n,v2)) -> (v1=v2)
	public static boolean prestaf_isFunction(LinSet[] sls) {
		String n = "n";
		String v1 = "v1";
		String v2 = "v2";
		
		String k = "k";
		
		Presburger p1 = prestaf_sls2presb(sls,n,v1,k);
		Presburger p2 = prestaf_sls2presb(sls,n,v2,k);
		
		Presburger v1EQv2 = Presburger.equals(Term.variable(v1), Term.variable(v2));
		Presburger p = p1.and(p2).bracket().imply(v1EQv2)
			.forall(Term.getIndex(v2), v2).forall(Term.getIndex(v1), v1);
		p = forall_n_geq1(p, n);
		//;p = n_geq1(p, n);
		
		return p.getNPF().isOne();
	}
	// S1 is subset of S2
	// A.n n<1 \/ A.v f_S1(n,v) -> f_S2(n,v)
	public static boolean prestaf_subset(LinSet[] sls1, LinSet[] sls2) {
		String n = "n";
		String v = "v";
		
		String k = "k";
		
		Presburger p1 = prestaf_sls2presb(sls1,n,v,k);
		Presburger p2 = prestaf_sls2presb(sls2,n,v,k);
		
		Presburger p = p1.imply(p2).bracket().forall(Term.getIndex(v), v);
		;p = n_geq1(p, n);
		
		return p.getNPF().isOne();
	}
	// S1 contains all minimal points of S2
	// A.n n<1 \/ A.v2 f_S2(n,v2) -> (E.v1 f_S1(n,v1) /\ v1<v2)
	public static boolean prestaf_containsAllMins(LinSet[] sls1, LinSet[] sls2) {
		String n = "n";
		String v1 = "v1";
		String v2 = "v2";
		
		String k = "k";
		
		Presburger v1LEQv2 = Presburger.lessEquals(Term.variable(v1), Term.variable(v2));
		
		Presburger p1 = prestaf_sls2presb(sls2,n,v2,k);
		Presburger p2 = prestaf_sls2presb(sls1,n,v1,k).and(v1LEQv2).bracket().exists(Term.getIndex(v1), v1);
		
		Presburger p = p1.imply(p2).bracket().forall(Term.getIndex(v2), v2);
		p = forall_n_geq1(p, n);
		
		return p.getNPF().isOne();
	}
	// linear set L1 is disjoint with linear set L2
	// A.n A.v  not f(ls1) and not f(ls2)
	public static boolean prestaf_disjoint(LinSet ls1, LinSet ls2) {

		String n = "n";
		String v = "v";
		
		String k = "k";
		
		Presburger p1 = prestaf_ls2presb(ls1,n,v,k).bracket().exists(Term.getIndex(k), k).bracket().not();
		Presburger p2 = prestaf_ls2presb(ls2,n,v,k).bracket().exists(Term.getIndex(k), k).bracket().not();
		
		Presburger p = p1.or(p2).bracket().forall(Term.getIndex(v), v);
		p = forall_n_geq1(p, n);
		
		return p.getNPF().isOne();
	}

	public static boolean prestaf_disjointLinSets(LinSet[] sls) {
		
		for (int i=0; i<sls.length; ++i) {
			for (int j=i+1; j<sls.length; ++j) {
				if (!prestaf_disjoint(sls[i],sls[j]))
					return false;
			}
		}
		return true;
	}

	// parameter1: original set, parameter2: minimized set
	public static boolean prestaf_checkMinimization(LinSet[] sls1, LinSet[] sls2) {
		
		boolean n1 = prestaf_isFunction(sls2); 
		boolean n2 = prestaf_subset(sls2,sls1); 
		boolean n3 = prestaf_containsAllMins(sls2,sls1); 
		boolean n4 = prestaf_disjointLinSets(sls2);
		return n1 && n2 && n3 && n4;
	}

	/*public static boolean prestaf_checkSum(SLSet op1, LinSet op2, SLSet res) {
		LinSet[] aux = new GLinSet[0];
		return prestaf_checkSum(op1.getLinearSets().toArray(aux), new LinSet[]{op2}, res.getLinearSets().toArray(aux));
	}
	public static boolean prestaf_checkSum(SLSet op1, SLSet op2, SLSet res) {
		LinSet[] aux = new GLinSet[0];
		return prestaf_checkSum(op1.getLinearSets().toArray(aux), op2.getLinearSets().toArray(aux), res.getLinearSets().toArray(aux));
	}*/

	
	public static boolean prestaf_checkUnion(LinSet[] op1, LinSet[] op2, LinSet[] res) {
		String n = "n";
		String v = "v";
		String k = "k";
		Presburger p1 = prestaf_sls2presb(op1,n,v,k);
		Presburger p2 = prestaf_sls2presb(op2,n,v,k);
		Presburger pres = prestaf_sls2presb(res,n,v,k);
		
		return p1.or(p2).bracket().equiv(pres).getNPF().isOne();
	}

}
