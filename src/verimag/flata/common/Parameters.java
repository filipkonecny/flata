/**
 * 
 */
package verimag.flata.common;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;


public class Parameters {
		private static Map<String, ParameterInfo> clParameters; // command line parameters <parameter, parameter_info>
		
		public static SCCStrategy getSCCStrategy() {
			ParameterInfo pi = clParameters.get(SCCSTRATEGY);
			if (pi.isOn)
				return SCCStrategy.get(pi.arguments[0]);
			else
				return SCCStrategy.getDefault();
		}
		
		private static Log log_ceOut;
		private static Log log_statNLElim;
		private static Log log_transIncl;
		private static Log log_smerge;
		private static Log log_cond1;
		private static Log log_cond2;
		private static Log log_mergeanalysis;
		private static Log log_abstr;
		private static Log log_disjincl;
		private static Log log_sil_out;
		
		public static void logStatAddTrans(StringBuffer aSB) {
			logStatAddTrans(log_statNLElim,aSB);
		}
		public static void logTransIncl(StringBuffer aSB) {
			logStatAddTrans(log_transIncl,aSB);
		}
		public static void log(String param, StringBuffer aSB) {
			
			Log log;
			
			if (param.equals(CE_OUT))
				log = log_ceOut;
			else if (param.equals(STAT_NLELIM))
				log = log_statNLElim;
			else if (param.equals(STAT_INCLTRANS))
				log = log_transIncl;
			else if (param.equals(STAT_SMERGE))
				log = log_smerge;
//			else if (param.equals(STAT_NLELIM))
//				log = log_nlelim;
			else if (param.equals(STAT_COND1))
				log = log_cond1;
			else if (param.equals(STAT_COND2))
				log = log_cond2;
			else if (param.equals(STAT_MERGE))
				log = log_mergeanalysis;
			else if (param.equals(STAT_ABSTR))
				log = log_abstr;
			else if (param.equals(T_DISJINCL))
				log = log_disjincl;
			else if (param.equals(SIL_EXPORT_ENC))
				log = log_sil_out;
			else 
				throw new RuntimeException("internal error: undefined value");
			
			logStatAddTrans(log,aSB);
		}

		
		private static void logStatAddTrans(Log log, StringBuffer aSB) {
			log.log(aSB);
			log.flush();
		}

		private static void initLog(Log log, String paramname) {
			//log = new Log(paramname, new File(clParameters.get(paramname).arguments()[0]));
			log.initLog();
			log.continueLog();
		}
		public static void initActions() {
			if (isOnParameter(CE_OUT)) {
				String name = CE_OUT;
				log_ceOut = new Log(name, new File(clParameters.get(name).arguments()[0]));
				initLog(log_ceOut, name);
			}
			if (isOnParameter(STAT_NLELIM)) {
				String name = STAT_NLELIM;
				log_statNLElim = new Log(name, new File(clParameters.get(name).arguments()[0]));
				initLog(log_statNLElim, name);
			}
			if (isOnParameter(STAT_INCLTRANS)) {
				String name = STAT_INCLTRANS;
				log_transIncl = new Log(name, new File(clParameters.get(name).arguments()[0]));
				initLog(log_transIncl, name);
			}
			if (isOnParameter(STAT_SMERGE)) {
				String name = STAT_SMERGE;
				log_smerge = new Log(name, new File(clParameters.get(name).arguments()[0]));
				initLog(log_smerge, name);
			}
//			if (isOnParameter(STAT_NLELIM)) {
//				String name = STAT_NLELIM;
//				log_nlelim = new Log(name, new File(clParameters.get(name).arguments()[0]));
//				initLog(log_nlelim, name);
//			}
			if (isOnParameter(STAT_COND1)) {
				String name = STAT_COND1;
				log_cond1 = new Log(name, new File(clParameters.get(name).arguments()[0]));
				initLog(log_cond1, name);
			}
			if (isOnParameter(STAT_COND2)) {
				String name = STAT_COND2;
				log_cond2 = new Log(name, new File(clParameters.get(name).arguments()[0]));
				initLog(log_cond2, name);
			}
			if (isOnParameter(STAT_MERGE)) {
				String name = STAT_MERGE;
				log_mergeanalysis = new Log(name, new File(clParameters.get(name).arguments()[0]));
				initLog(log_mergeanalysis, name);
			}
			if (isOnParameter(STAT_ABSTR)) {
				String name = STAT_ABSTR;
				log_abstr = new Log(name, new File(clParameters.get(name).arguments()[0]));
				initLog(log_abstr, name);
			}
			if (isOnParameter(T_DISJINCL)) {
				String name = T_DISJINCL;
				log_disjincl = new Log(name, new File(clParameters.get(name).arguments()[0]));
				initLog(log_disjincl, name);
			}
			if (isOnParameter(SIL_EXPORT_ENC)) {
				String name = SIL_EXPORT_ENC;
				log_sil_out = new Log(name, new File(clParameters.get(name).arguments()[0]));
				initLog(log_sil_out, name);
			}
		}
		public static void finalActions() {
			if (isOnParameter(CE_OUT)) {
				log_ceOut.endLog();
			}
			if (isOnParameter(STAT_NLELIM)) {
				log_statNLElim.endLog();
			}
			if (isOnParameter(STAT_INCLTRANS)) {
				log_transIncl.endLog();
			}
			if (isOnParameter(STAT_SMERGE)) {
				log_smerge.endLog();
			}
//			if (isOnParameter(STAT_NLELIM)) {
//				log_nlelim.endLog();
//			}
			if (isOnParameter(STAT_COND1)) {
				log_cond1.endLog();
			}
			if (isOnParameter(STAT_COND2)) {
				log_cond2.endLog();
			}
			if (isOnParameter(STAT_MERGE)) {
				log_mergeanalysis.endLog();
			}
			if (isOnParameter(STAT_ABSTR)) {
				log_abstr.endLog();
			}
			if (isOnParameter(T_DISJINCL)) {
				log_disjincl.endLog();
			}
			if (isOnParameter(SIL_EXPORT_ENC)) {
				log_sil_out.endLog();
			}
		}
		
		public static DirStrategy getDirStrategy() {
			ParameterInfo pi = clParameters.get(DIRSTRATEGY);
			if (pi.isOn)
				return DirStrategy.get(pi.arguments[0]);
			else
				return DirStrategy.getDefault();
		}
		
		public static void toString(IndentedWriter iw) {
			for(Map.Entry<String, ParameterInfo> entry : clParameters.entrySet()) {
				if (!entry.getValue().userHidden)
					iw.writeln(entry.getKey() + entry.getValue().help);
			}
		}
		
		//public static String INPUT_CA = "-in-ca";
		public static String OUT_STATUS = "-status";
		public static String HELP = "-help";
		public static String IMPLACT = "-ia";
		public static String THIST = "-thist";
		public static String RENAME = "-rename";
		public static String OUTCA = "-ca";
		public static String OUTFAST = "-fst";
		public static String OUTASPIC = "-aspic";
		public static String OUTTREX = "-trex";
		public static String OUTARMC = "-armc";
		public static String OUTNTS = "-nts";
		public static String SCCSTRATEGY = "-strategy-scc";
		public static String DIRSTRATEGY = "-strategy-dir";
		public static String CONSTPROP = "-cp";
		public static String STAT_NLELIM = "-stat-nlelim";
		public static String STAT_INCLTRANS = "-stat-incltrans";
		public static String STAT_SMERGE = "-stat-smerge";
		//public static String STAT_NLELIM = "-stat-nl-elim"; // non-loop elim.
		public static String STAT_COND1 = "-stat-cond1";
		public static String STAT_COND2 = "-stat-cond2";
		
		public static String STAT_ABSTR = "-stat-abstr";
		public static String STAT_MERGE = "-stat-merge";
		
		public static String TERMINATION = "-term";
		
		public static String ABSTR_OCT = "-abstr-oct";
		public static String T_MERGE_PREC = "-t-merge-prec";
		public static String T_FULLINCL = "-t-fullincl";
		public static String ACCELERATE_WITH_OUTGOING = "-acc-outgoing";
		public static String T_OCTINCL = "-t-octincl";
		public static String T_ALWAYSONE = "-t-alwaysone";
		public static String T_MERGE_IMPRECISE = "-t-merge-imprec";
		public static String T_DISJINCL = "-t-disjincl";
		public static String T_SUMMARY_WITH_LOCALS = "-summary-with-locals";
		
		public static String IN_FST = "-in-fst";
		public static String NO_REDUCE = "-no-red";
		public static String REDUCE_MIN = "-reduce-min";
		public static String REDUCE_NO_LOOP = "-no-loop";
		public static String REDUCE_NO_MLOOP = "-no-mloop";
		
		public static String SIL_EXPORT_ENC = "-sil-exp";
		
		public static String CE_ALL = "-ce-all"; // all real counter-examples
		public static String CE_OUT = "-ce-out";
		public static String CE_NO = "-ce-no";
		public static String CE_NOLONG = "-ce-bnd";
		
		public static String CE_LESS = "-ce-less"; // print small CEs
		public static String OUTPUT_TOTAL_TIME = "-output-time"; // outputs only total running time
		
		public static String RECUR_SCC = "-recur-scc";
		
		static {
			clParameters = new java.util.TreeMap<String, ParameterInfo>();
			clParameters.put(
					HELP, 
					new ParameterInfo(" - flata help")
					);
			clParameters.put(
					IMPLACT,
					new ParameterInfo(true, " - add implicit actions", false)
					);
			
			clParameters.put(
					CONSTPROP,
					new ParameterInfo(true, " - use constant propagation", false)
					);

			clParameters.put(
					ABSTR_OCT,
					new ParameterInfo(true, " - use octagonal abstraction for loops", false, 0)
					);
			
			clParameters.put(
					TERMINATION,
					new ParameterInfo(true, " - run termination analysis (default: reachability analysis)", false, 0)
					);
			
			boolean hide = CR.RELEASE;
			{
				
				clParameters.put(
						CE_ALL,
						new ParameterInfo(hide, " - find maximum number of counter-examples", false)
						);
				clParameters.put(
						IN_FST,
						new ParameterInfo(hide, " - input is in the FAST format", false)
						);
				clParameters.put(
						SCCSTRATEGY, 
						new ParameterInfo(hide, " ["+SCCStrategy.STR_DFS+"|"+SCCStrategy.STR_BFS+"|"+SCCStrategy.STR_GLOBAL+"] - reduction strategy for SCCs (default "+SCCStrategy.STR_DEFAULT+")", false, 1)
						);
				clParameters.put(
						DIRSTRATEGY, 
						new ParameterInfo(hide, " ["+DirStrategy.STR_FW+"|"+DirStrategy.STR_BW+"] - reduction direction(default "+DirStrategy.STR_DEFAULT+")", false, 1)
						);
				clParameters.put(
						T_ALWAYSONE,
						new ParameterInfo(hide, "", false)
						);
				clParameters.put(
						T_MERGE_IMPRECISE,
						new ParameterInfo(hide, " - use imprecise transition merging", false, 1)
						);
				clParameters.put(
						THIST,
						new ParameterInfo(hide, " - .ca output shows history of transition composition", false)
						);
			}
			
			clParameters.put(
					T_MERGE_PREC,
					new ParameterInfo(hide, " - use precise transition merging", false)
					);
			clParameters.put(
					T_FULLINCL,
					new ParameterInfo(hide, " - use full inclusion checks", false)
					);
			clParameters.put(
					ACCELERATE_WITH_OUTGOING,
					new ParameterInfo(hide, " - use outgoing edges in disjunctive acceleration", false)
					);
			clParameters.put(
					T_SUMMARY_WITH_LOCALS,
					new ParameterInfo(hide, " - keep all local variables of the main function in the summary relation", false)
					);
			clParameters.put(
					RECUR_SCC,
					new ParameterInfo(false, " - scc-wise generation of k-bounded procedures", false)
					);
			
			clParameters.put(
					T_OCTINCL,
					new ParameterInfo(hide, " - use full inclusion checks only for octagons", false)
					);
			clParameters.put(
					T_DISJINCL,
					new ParameterInfo(true, "", false, 1)
					);

			clParameters.put(
					NO_REDUCE,
					new ParameterInfo(false, " - no reduction (use for conversion to other formats)", false)
					);
			clParameters.put(
					REDUCE_NO_LOOP,
					new ParameterInfo(true, " - don't eliminate states with loops", false)
					);
			clParameters.put(
					REDUCE_MIN,
					new ParameterInfo(true, " - reduce only states with at most 3 incident transitions", false)
					);
			clParameters.put(
					REDUCE_NO_MLOOP,
					new ParameterInfo(true, " - don't eliminate states with strictly more than 1 loop", false)
					);

			clParameters.put(
					CE_LESS, 
					new ParameterInfo(true, " - print less information in counter-examples", false)
					);
			
			clParameters.put(
					OUTPUT_TOTAL_TIME,
					new ParameterInfo(true, "", false)
					);

			clParameters.put(
					SIL_EXPORT_ENC, 
					new ParameterInfo(true, " filename - SIL automata output", false, 1)
					);
			
			clParameters.put(
					OUTCA, 
					new ParameterInfo(" filename - export output automaton in the .ca format", false, 1)
					);
			clParameters.put(
					OUTFAST, 
					new ParameterInfo(" filename - export output automaton in the .fst format", false, 1)
					);
			clParameters.put(
					OUTASPIC, 
					new ParameterInfo(hide, " filename - export output automaton in the .aspic format", false, 1)
					);
			clParameters.put(
					OUTTREX, 
					new ParameterInfo(hide, " filename - export output automaton in the .if [TReX] format", false, 1)
					);
			clParameters.put(
					OUTARMC, 
					new ParameterInfo(" filename - export output automaton in the .armc format", false, 1)
					);
			clParameters.put(
					OUTNTS, 
					new ParameterInfo(" filename - export output automaton in the .nts format", false, 1)
					);
			clParameters.put(
					STAT_NLELIM, 
					new ParameterInfo(true, " filename", false, 1)
					);
			clParameters.put(
					STAT_INCLTRANS, 
					new ParameterInfo(true, " filename - transition inclusion statistics", false, 1)
					);
			clParameters.put(
					CE_OUT, 
					new ParameterInfo(true, " filename - output a counter-example trace (.dot format)", false, 1)
					);
			clParameters.put(
					CE_NO, 
					new ParameterInfo(false, " - do not construct a counter-example trace", false, 1)
					);
			clParameters.put(
					CE_NOLONG, 
					new ParameterInfo(true, " bound - bound number of unfoldings of loops in counterexamples", false, 1)
					);
			clParameters.put(
					STAT_SMERGE, 
					new ParameterInfo(true, " filename - syntactic merge output", false, 1)
					);
//			clParameters.put(
//					STAT_NLELIM, 
//					new ParameterInfo(false, " filename - non-loop elim. output", false, 1)
//					);
			clParameters.put(
					STAT_COND1, 
					new ParameterInfo(true, " filename - ", false, 1)
					);
			clParameters.put(
					STAT_COND2, 
					new ParameterInfo(true, " filename - ", false, 1)
					);
			clParameters.put(
					STAT_MERGE, 
					new ParameterInfo(true, " filename - ", false, 1)
					);
			clParameters.put(
					STAT_ABSTR, 
					new ParameterInfo(true, " filename - ", false, 1)
					);
			
			/*clParameters.put(
					INPUT_CA, 
					new ParameterInfo(true, " filename - ", false)
					);*/
			clParameters.put(
					OUT_STATUS, 
					new ParameterInfo(true, " filename - ", false, 1)
					);
		}
		
		public static void incorrectArgs() {
			System.out.println("Incorrect arguments");
			printUsageAndExit();
		}
		public static void printUsageAndExit() {
			System.out.print(usage());
			System.exit(1);
		}
		
		public static String usage() {
			IndentedWriter iw = new IndentedWriter(new StringWriter());
			iw.writeln("Program usage:");
			iw.indentInc();
			iw.writeln("java verimag.flata.Main [options] input");
			iw.indentDec();
			iw.writeln("options:");
			iw.indentInc();
			toString(iw);
			iw.indentDec();
			return iw.getWriter().toString();
		}
		public static int processParameter(String[] args, int from, int to) {
			
			//int maxInx = Math.max(args.length-1, to);
			
			String optName = args[from].toLowerCase();
			ParameterInfo pi = clParameters.get(optName);
			
			if (pi==null) {
				System.out.println("Unknown parameter: '"+args[from]+"'");
				printUsageAndExit();
			} else if (args[from].equals(HELP)) {
				printUsageAndExit();
			} else {
				pi.isOn = Boolean.TRUE;
			}
			
			int j=from+1;
			while (j <= from+pi.maxArity && j <= to) {
				if (args[j].startsWith("-"))
					break;
				j++;
			}
			int givenArity = j-from-1;
			pi.arguments = new String[givenArity];
			for (int ii=1; ii<=givenArity; ii++) {
				pi.arguments[ii-1] = args[from+ii];
			}
			pi.checkArguments(optName);
			
			return from+1+givenArity;
		}
		public static void processParameters(String[] args, int from, int to) {
			int i=from;
			while(i<=to) {
				i = processParameter(args,i,to);
			}
		}
		public static ParameterInfo getParameter(String name) {
			return clParameters.get(name);
		}
		public static boolean isOnParameter(String name) {
			return clParameters.get(name).isOn;
		}
		public static void setParameter(String name) {
			clParameters.get(name).isOn = true;
		}
		public static void unsetParameter(String name) {
			clParameters.get(name).isOn = false;
		}
	}