package verimag.flata;

import java.io.*;
import java.util.Collection;

import nts.parser.NTS;
import nts.parser.NTSParser;
import nts.parser.ParserListener;
import verimag.flata.automata.ca.*;
import verimag.flata.automata.cg.CG;
import verimag.flata.common.*;
import verimag.flata.presburger.PartitionsJoin;
import verimag.flata.recur_bounded.SccFinder;

public class Main {

	public static void initActions() {
		// TODO: remove
		CR.launchYices();
		if (Parameters.isOnParameter(Parameters.SOLVER)) {
			CR.initFLataJavaSMT(Parameters.getParameter(Parameters.SOLVER).arguments()[0]);
		} else {
			CR.initFLataJavaSMT();
		}
		
		//if (!CR.RELEASE)
		if (Parameters.isOnParameter(Parameters.ABSTR_OCT)) {
			CR.launchGLPK();
		}
		
		Parameters.initActions();
	}
	private static PrintStream out;
	private static long time_start;
	public static void finalActions() {
		// TODO: remove
		CR.terminateYices();
		
		Parameters.finalActions();
		
		if (Parameters.isOnParameter(Parameters.OUTPUT_TOTAL_TIME)) {
		    System.setOut(out);
		    float time = ((float)(System.currentTimeMillis()-time_start))/1000;
		    System.out.println(time);
		}
	}
	
	private static void checkExport(CG cg) {
		if (!cg.singleProc() || cg.hasCalls() ) {
			System.out.println("Export failed, output only for single procedure systems.");
			System.exit(1);
		}
	}
	public static void export(CG cg) {
		
		File inputFile = cg.inputFile();
		
		String def_out_base = inputFile.getPath();
		if (def_out_base.substring(def_out_base.length()-CR.SUF_CA.length(), def_out_base.length()).equals(CR.SUF_CA))
			def_out_base = def_out_base.substring(0, def_out_base.length()-CR.SUF_CA.length());
		def_out_base += ".out";
		
		
		CA ca = cg.mainProc();
		
		if (Parameters.isOnParameter(Parameters.OUTCA)) {
			checkExport(cg);
			System.out.print("Exporting to .ca ...  ");
			
			String name = def_out_base + CR.SUF_CA;
			
			if (Parameters.isOnParameter(Parameters.OUTCA)) {
				ParameterInfo pi = Parameters.getParameter(Parameters.OUTCA);
				String[] p_args = pi.arguments();
				if (p_args.length != 0)
					name = p_args[0];	
			}
			
			CR.writeToFile(name, ca.toString(true));
			
			System.out.println("done (exported to "+name+")");
		}
		
		if (Parameters.isOnParameter(Parameters.OUTFAST)) {
			checkExport(cg);
			System.out.print("Exporting to .fst ...  ");
			
			if (ca.isFASTCompatible()) {
				String name;
				ParameterInfo pi = Parameters.getParameter(Parameters.OUTFAST);
				String[] p_args = pi.arguments();
				if (p_args.length == 0)
					name = def_out_base + CR.SUF_FAST;
				else
					name = p_args[0];
				
				CR.writeToFile(name, ca.toStringFAST());
				
				System.out.println("done (exported to "+name+")");
			} else {
				System.out.println("unsuccessful (some transitions are incompatible with .fst format)");
			}
		}

		if (Parameters.isOnParameter(Parameters.OUTASPIC)) {
			checkExport(cg);
			System.out.print("Exporting to .aspic ...  ");
			
			if (ca.isFASTCompatible()) {
				String name;
				ParameterInfo pi = Parameters.getParameter(Parameters.OUTASPIC);
				String[] p_args = pi.arguments();
				if (p_args.length == 0)
					name = def_out_base + CR.SUF_ASPIC;
				else
					name = p_args[0];
				
				CR.writeToFile(name, ca.toStringASPIC());
				
				System.out.println("done (exported to "+name+")");
			} else {
				System.out.println("unsuccessful (some transitions are incompatible with .fst/.aspic format)");
			}
		}

		if (Parameters.isOnParameter(Parameters.OUTTREX)) {
			checkExport(cg);
			System.out.print("Exporting to .if ...  ");
			
			if (ca.isTREXCompatible()) {
				String name;
				ParameterInfo pi = Parameters.getParameter(Parameters.OUTTREX);
				String[] p_args = pi.arguments();
				if (p_args.length == 0)
					name = def_out_base + CR.SUF_TREX;
				else
					name = p_args[0];
				
				CR.writeToFile(name, ca.toStringTREX());
				
				System.out.println("done (exported to "+name+")");
			} else {
				System.out.println("unsuccessful (some transitions are incompatible with .if [TReX] format)");
			}
		}	
		
		if (Parameters.isOnParameter(Parameters.OUTARMC)) {
			checkExport(cg);
			System.out.print("Exporting to .armc ...  ");
			
			if (ca.isARMCCompatible()) {
				String name;
				String[] p_args = Parameters.getParameter(Parameters.OUTARMC).arguments();
				if (p_args.length == 0)
					name = def_out_base + CR.SUF_ARMC;
				else
					name = p_args[0];
				
				CR.writeToFile(name, ca.toStringARMC());
				
				System.out.println("done (exported to "+name+")");
			} else {
				System.out.println("unsuccessful (some transitions are incompatible with .armc format)");
			}
		}
		
		if (Parameters.isOnParameter(Parameters.OUTNTS)) {
//			// only for single-procedure systems
//			checkExport(cg); 
			System.out.print("Exporting to .nts ...  ");
			
			//if (ca.isARMCCompatible()) {
				String name;
				String[] p_args = Parameters.getParameter(Parameters.OUTNTS).arguments();
				if (p_args.length == 0)
					name = def_out_base + CR.SUF_NTS;
				else
					name = p_args[0];
				
				CR.writeToFile(name, cg.toStringNTS().toString());
				
				System.out.println("done (exported to "+name+")");
//			} else {
//				System.out.println("unsuccessful (some transitions are incompatible with .armc format)");
//			}
		}
	}
	
//	public static int EXIT_REACH = 100; // reachable
//	public static int EXIT_UNREACH = 101; // unreachable
//	public static int EXIT_DONTKNOW = 102; // unknown
	
	public static void processCENodes(CA ca, long start) {
		Collection<CENode> nodes = ca.ce_nodes();
		
		if (CENode.hasRealTrace(nodes)) {
			
			CENode best = CENode.rootWithShortestRealTrace(nodes);
			
//			// old trace printing
//			CENode.CETrace real = best.getShortestRealTrace();
//			real.print();
			
			//ca.outputTHistGraph("ttt.dot");
			
			//CR.writeToFile(, ReduceInfo.toDot(origTransitions, red, blue).toString());
			CEView cev = best.prepareTraceView();
			StringBuffer sbTrace = cev.toSB();
			System.out.println();
			System.out.println(sbTrace);
			
//			System.out.println(best.show_t_hist());
//			real.prune();
//			real.print();
			//System.out.println("------------------------------------");
			//System.out.println(best.show_t_hist());
			
			if (Parameters.isOnParameter(Parameters.CE_OUT)) {
//				Collection<CATransition> col = real.transitions();
//				StringBuffer sb = new StringBuffer();
//				sb.append(ca.toDotLang_mark(ca.name(), CATransition.states(col), col));
//				CR.Parameters.log(CR.Parameters.CE_OUT, sb);
			}
		} else {
			System.out.println("Spurious counter-example. DON'T KNOW.");
			System.exit(1);
		}
		
	}		

		
	
	public static NTS parseNTS(File inputFile) {
		
		InputStream is = null;
	    try {
			is = new FileInputStream(inputFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	    
	    ParserListener listen = new ParserListener();
	    NTSParser.parseNTS(is, listen);
	    return listen.nts();
	}
	public static CG nts2cg(NTS aNts) {
		NTSVisitor v = new NTSVisitor();
	    aNts.accept(v);
	    return v.callGraph();
	}
	
	public static void termination(File inputFile) {
		
		CA.TERM = true;
		//CG cg = verimag.flata.Main.parseCG(inputFile);
		NTS nts = verimag.flata.Main.parseNTS(inputFile);
		CG cg = verimag.flata.Main.nts2cg(nts);
		cg.termination2();
		//cg.termination();
		
		Calculator.finalActions();
	}

	
	public static void main(String[] args) {
		
		long very_start = System.currentTimeMillis();
		
		File inputFile = CR.processParameters(args);
		
		Main.initActions();
		
		if (Parameters.isOnParameter(Parameters.TERMINATION)) {
			termination(inputFile);
			return;
		}
		
		CG cg;
		{
			NTS nts = parseNTS(inputFile);
			// check that all variables are scalar integers
			// TODO
			// check for recursion
			if (SccFinder.isRecursive(nts)) {
				System.out.println("Recursive input. Running k-bounded underapproximation.");
				RecursionKBnd.run(very_start,nts);
				return;
			}
			cg = nts2cg(nts);
		}
		
		out = System.out;
		time_start = System.currentTimeMillis();
		if (Parameters.isOnParameter(Parameters.OUTPUT_TOTAL_TIME)) {
		    System.setOut(new PrintStream(new OutputStream() {
		        public void write(int b) {
		        // NO-OP
		        }
		        }));
		}
		
		cg.inputFile(inputFile);
		
		System.out.println(cg.toString());
		if (Parameters.isOnParameter(Parameters.REDUCE_MIN)) {
			cg.reduceMin();
		} else if (! Parameters.isOnParameter(Parameters.NO_REDUCE)) {
			//cg.reachability_inline();
			cg.reachability_summary();
		}
		
		export(cg);
		
		if (!CR.RELEASE) {
			System.out.println("FW in Join: "+(float)PartitionsJoin.TIME / 1000+" s");
		}
		
		System.out.println(CR.flataJavaSMT.getSolverCalls() + ", " + CR.yices_calls); // TODO: remove

		Main.finalActions();
		System.exit(0);
	}
	
}
