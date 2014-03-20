package verimag.flata;

import java.util.*;

import nts.interf.*;
import nts.parser.*;
import verimag.flata.automata.cg.CG;
import verimag.flata.common.Answer;
import verimag.flata.presburger.DisjRel;
import verimag.flata.recur_bounded.*;

public class RecursionKBnd {
	
	public static void run(long start, NTS aNts) {
		//NTS nts_cp = aNts.copy();
		
		RecurToKBounded r2kbnd = RecurToKBounded.start(aNts);
		GenerationInfo genInfo = r2kbnd.getInfo();
		
		INTS nts = genInfo.getNts();
		CG cg;
		
		NTSVisitor v = new NTSVisitor();
		List<String> mainProcedure = new LinkedList<String>();
		List<DisjRel> summary = new LinkedList<DisjRel>();
		
		// k=1
		r2kbnd.generateNewLevel(1);
		nts.accept(v);
	    cg = v.callGraph();
		
		reachability(cg, mainProcedure, summary);
		
		// k=2,3,...
		for (int k=2; ; k++) { // infinite loop
			r2kbnd.generateNewLevel(k);
			List<ISubsystem> latest = genInfo.latestSubsystems();
			v.extendWithNewSubsystems(nts, latest);
			
			reachability(cg, mainProcedure, summary);
	
			
			Answer a = checkFixpoint(summary);
			
//			Answer a;
//			{
//				Map<String,String> m = r2kbnd.giveMapping();
//				CG cg_orig = verimag.flata.Main.nts2cg(nts_cp);
//				DisjRel aux = cg_orig.replaceCalls_forRecur(cg_orig.main(), m, cg);
//				cg_orig.removeCalls();
//				cg_orig.reachability_summary();
//				DisjRel aux2 = cg_orig.main().summary().finalSummaryAsRel();
//				
//				a = aux.implies(aux2).and(aux2.implies(aux)); 
//			}
			
			if (a.isTrue()) {
				float time = ((float)(System.currentTimeMillis()-start))/1000;
				System.out.println("Fixpoint reached for k="+k+". Total time = "+time+" s.");
				System.out.println("Summary relation:");
				System.out.println(summary.get(k-1));
				System.exit(0);
			} else {
				System.out.println("Fixpoint not reached yet (k="+k+","+a+").");
			}
			
		}
	}
	private static void checkSuccess(int code) {
		if (code != CG.EXIT_CORRECT) {
			System.out.println("Reachability analysis failed. Analysis stopped.");
			System.exit(0);
		}
	}
	private static void reachability(CG cg, List<String> mainProcedure, List<DisjRel> summary) {
		int status_code = cg.reachability_summary();
		checkSuccess(status_code);
		
		mainProcedure.add(cg.mainName());
		summary.add(cg.getCached(cg.main()).finalSummaryAsRel());
	}
	private static Answer checkDone(List<DisjRel> summary) {
		return checkFixpoint(summary);
	}
	private static Answer checkFixpoint(List<DisjRel> summary) {
		int k = summary.size();
		DisjRel r1 = summary.get(k-2);
		DisjRel r2 = summary.get(k-1);
		return r1.relEquals(r2);
	}
//	private static Answer checkInductive(List<DisjRel> summary) {
//		
//	}
//	public static void addSubsystemsToCG(NTSVisitor v, CG aCG, INTS aNts, List<ISubsystem> aSubs) {
//		v.extendWithNewSubsystems(aNts, aSubs);
//	}
//	public static CG nts2cg(NTSVisitor v, INTS aNts) {
//	    aNts.accept(v);
//	    return v.callGraph();
//	}
}
