package verimag.flata.common;
	
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import org.gnu.glpk.GLPK;

import verimag.flata.presburger.Variable;
import yices.*;

/**
 * Resources shared among various objects. They are implemented as static
 * members.
 */
public class CR {
	public static boolean RELEASE = true;
	
	public static boolean NO_POSTPARSING = false;
	
	public static String TRANS_ID_NAME = "id";
	
	private static boolean yicesLite = false;
	
	public static String yicesCommand;
	
	private static boolean yicesTypeCheck = false;
	
	private static java.io.BufferedWriter yicesUnknownLog;
	private static boolean YICES_LOG_UNKNOWN = false;

	public static String SUF_CA = ".ca";
	public static String SUF_FAST = ".fst";
	public static String SUF_ASPIC = ".aspic";
	public static String SUF_TREX = ".if";
	public static String SUF_ARMC = ".armc";
	public static String SUF_NTS = ".nts";
	
	static {
		if (yicesTypeCheck)
			yicesCommand = "yices -tc";
		else
			yicesCommand = "yices";
		
		if (YICES_LOG_UNKNOWN) {
			try {
				yicesUnknownLog = new java.io.BufferedWriter(new java.io.FileWriter("yicesUnknownLog.txt"));
			} catch (IOException e) {
				System.err.println("IO problems");
				System.exit(-1);
			}
		}
	}
	
	public static boolean NO_OUTPUT = false;
	
	public static int DEBUG_NO = 0;
	public static int DEBUG_LOW = 1;
	public static int DEBUG_MEDIUM = 2;
	public static int DEBUG_HIGH = 3;
	public static int DEBUG = DEBUG_NO;
	
	public static final Properties systemProperties;
	public static final String NEWLINE;
	static {
		systemProperties = new Properties(System.getProperties());
		NEWLINE = systemProperties.getProperty("line.separator");
		
		//paramNames = new ParamNames();
		//clParameters = Parameters.getParameters();
	}
	
	/**
	 * Yices' standard output stream
	 */
	public static java.io.BufferedReader yicesStdoutReader;
	/**
	 * Yices' standard input stream
	 */
	public static java.io.BufferedWriter yicesStdinWriter;

	public static final int eOUT_FLATA=0x01;
	public static final int eOUT_FAST=0x02;
	public static final int eOUT_ARMC=0x04;

	/**
	 * converts the specified code of a Yices' answer to the corresponding string ('sat', 'unsat' or 'unknown')
	 * @param aAnswerCode the code of a Yices' answer
	 * @return corresponding string
	 */
	public static String yicesAnswerToString(YicesAnswer aAnswerCode) {
		switch (aAnswerCode) {
		case eYicesSAT:
			return "SAT";
		case eYicesUNSAT:
			return "UNSAT";
		case eYicesUknown:
			return "UNKNOWN";
		default:
			throw new RuntimeException("Unexpected Yices' answer code.");
		}
	}
	
	/**
	 * reads from Yices' standard output until it reaches 'sat', 'unsat' or 'unknown' answer 
	 * and returns the answer. The text from the standard output which follows that answer 
	 * is added to the specified StringBuffer.
	 * @return Yices' answer
	 * @throws IOException
	 */
	public static YicesAnswer getResultFromYicesStream(StringBuffer aYicesCore) {
		long t1 = System.currentTimeMillis();
		
		YicesAnswer ret;
		if (yicesLite)
			ret = getResultFromYicesStreamN(aYicesCore);
		else
			ret = getResultFromYicesStreamO(aYicesCore);
		
		long t2 = System.currentTimeMillis();
		timeYicesRead += (t2-t1);
		
		return ret;
	}
	private static YicesAnswer getResultFromYicesStreamN(StringBuffer aYicesCore) {
		
		String str=null;
		try {
			
			str = CR.yicesReader.readLine();
			
			while (!str.equals("sat") && !str.equals("unsat") && !str.equals("unknown")) {
				str = CR.yicesReader.readLine();
			}
			
			while (CR.yicesReader.ready())
				aYicesCore.append(CR.yicesReader.readLine()+CR.NEWLINE);
			
		} catch (IOException e) {
			System.err.println("Problems when reading from Yices standard output.");
			e.printStackTrace();
			System.exit(-1);
		}

		if (str==null) {
			throw new RuntimeException("Reading null from Yices");
		}
		
		if (str.equals("sat"))
			return YicesAnswer.eYicesSAT;
		else if (str.equals("unsat"))
			return YicesAnswer.eYicesUNSAT;
		else if (str.equals("unknown"))
			return YicesAnswer.eYicesUknown;
		else
			throw new RuntimeException("Unexpected Yices' answer.");
	}
	
	@SuppressWarnings("serial")
	static class MyException extends RuntimeException {
		String message;
		public MyException(String msg) {
			super();
			message = msg;
		}
		public void printAndExit() {
			System.err.println(message);
			System.exit(-1);
		}
	}
	

	private static YicesAnswer getResultFromYicesStreamO(StringBuffer aYicesCore) {
		String str=null;
		try {
			
			str = CR.yicesStdoutReader.readLine();
			
			if (str == null) {
				throw new MyException("internal error: null read from yices");
			}
			
			
			while (!str.equals("sat") && !str.equals("unsat") && !str.equals("unknown")) {
				str = CR.yicesStdoutReader.readLine();
			}
			
			while (CR.yicesStdoutReader.ready()) {
				aYicesCore.append(CR.yicesStdoutReader.readLine()+CR.NEWLINE);
			}
			
		} catch (IOException e) {
			System.err.println("Problems when reading from Yices standard output.");
			e.printStackTrace();
			System.exit(-1);
		}

		if (str==null) {
			throw new RuntimeException("Reading null from Yices");
		}
		
		if (str.equals("sat"))
			return YicesAnswer.eYicesSAT;
		else if (str.equals("unsat"))
			return YicesAnswer.eYicesUNSAT;
		else if (str.equals("unknown"))
			return YicesAnswer.eYicesUknown;
		else
			throw new RuntimeException("Unexpected Yices' answer.");
	}

	
	public static StringBuffer prepareYicesFormula(StringBuffer aDeclarations, StringBuffer aAssertContent, boolean aShowEvidence) {
		return prepareYicesFormula(aDeclarations, aAssertContent, new StringBuffer(), aShowEvidence);
	}
	public static StringBuffer prepareYicesFormula(StringBuffer aDeclarations, StringBuffer aAssertContent, StringBuffer aVarsConstraints, boolean aShowEvidence) {
		StringBuffer formula = new StringBuffer("(reset)\n");
		formula.append(aDeclarations);
		formula.append(aVarsConstraints);
		formula.append("(assert"+CR.NEWLINE)
					.append(aAssertContent)
					.append(")"+CR.NEWLINE);
		if (aShowEvidence)
			formula.append("(set-evidence! true)"+CR.NEWLINE);
		formula.append("(check)"+CR.NEWLINE);
		return formula;
	}
	public static void prepareYicesDeclaration(Collection<String> aVarNames, IndentedWriter iw) {
		for (String varName : aVarNames) {
			iw.writeln("(define "+CR.yicesVarName(varName)+"::int)\n");
		}
	}
	
	/**
	 * Checks with Yices if the specified formula is satisfiable
	 * @param aSB this parameter should contain 'reset', 'define', 'assert' and 'check' commands for Yices
	 * @param aYicesCore the text from the standard output which follows 'sat'/'unsat'/'unknown' answer 
	 * is added to this StringBuffer
	 * @return code of Yices' answer
	 */
	public static long timeYicesWrite = 0;
	public static long timeYicesRead = 0;
	public static long timeIsSatYices = 0;
		
	public static int yices_calls = 0;
	public static YicesAnswer isSatisfiableYices(StringBuffer aSB, StringBuffer aYicesCore) {
		yices_calls ++;
		
		YicesAnswer ret;
		
		long t1 = System.currentTimeMillis();
		
		if (yicesLite)
			ret = isSatisfiableYicesN(aSB,aYicesCore);
		else
			ret = isSatisfiableYicesO(aSB,aYicesCore);
		
		long t2 = System.currentTimeMillis();
		timeIsSatYices += (t2-t1);
		
		if (YICES_LOG_UNKNOWN && !ret.isKnown()) {
			try {
				yicesUnknownLog.write(aSB.toString());
				yicesUnknownLog.flush();
			} catch (IOException e) {
				System.err.println("IO problems");
				System.exit(-1);
			}
		}
			
		return ret;
	}
	public static long yltimemkc = 0;
	public static long yltimedelc = 0;
	public static long yltimeread = 0;
	private static YicesAnswer isSatisfiableYicesN(StringBuffer aSB, StringBuffer aYicesCore) {
		
		String str = aSB.toString();
		
		long t1 = System.currentTimeMillis();
				
		yices_ctx = yl.yicesl_mk_context();
		
		long t1write = System.currentTimeMillis();

		yl.yicesl_read(yices_ctx, str);
		long t2write = System.currentTimeMillis();
		
		yl.yicesl_del_context(yices_ctx);
		
		long t2 = System.currentTimeMillis();
		timeYicesWrite += t2-t1;
		yltimeread += (t2write-t1write);
		
		YicesAnswer ret = CR.getResultFromYicesStream(aYicesCore);
		
		return ret;
		
	}
	private static YicesAnswer isSatisfiableYicesO(StringBuffer aSB, StringBuffer aYicesCore) {

		try {
			long t1 = System.currentTimeMillis();
			
			int blockSize = 1000000;
			int blocks = aSB.length()/blockSize
				+ (((aSB.length()%blockSize)==0)? 0 : 1);
			
			for (int i=0; i<blocks; i++) {
				int start = i*blockSize;
				int end = (i==blocks-1)? aSB.length() : (i+1)*blockSize;
				String tmp = aSB.substring(start, end);
				
				CR.yicesStdinWriter.write(tmp);
				CR.yicesStdinWriter.flush();
			}
			
			long t2 = System.currentTimeMillis();
			timeYicesWrite += (t2-t1);
			
		} catch (IOException e) {
			System.err.println("Problems when writing to Yices standard input.");
			e.printStackTrace();
			System.exit(-1);
		}
		
		try {
			YicesAnswer ret = CR.getResultFromYicesStream(aYicesCore);
			return ret;
		} catch (MyException e) {
			//System.out.println(aSB);
			e.printAndExit();
			return null;
		}
	}
	
	public static Map<String, String> parseYicesCore(StringBuffer yicesCore) {
		Map<String, String> coreValues = new HashMap<String, String>();
	
		StringReader sr = new StringReader(new String(yicesCore));
		BufferedReader br = new BufferedReader(sr);
		try {
			while(br.ready()) {
				String line = br.readLine();
				if (line==null)
					break;
				if (line.length()==0)
					continue;
				
				Scanner scanner = new Scanner(line);
				//System.out.println(scanner.delimiter());
				scanner.useDelimiter("(\\s|(\\()|(\\))|(\\=))+");
				//System.out.println(scanner.delimiter());
				
				while (scanner.hasNext()) {
					coreValues.put(CR.yicesVarUnname(scanner.next()), scanner.next());
				}
				scanner.close();
			}
		} catch (IOException e) {
			System.err.print("Unexpected content of yices output.");
			e.printStackTrace();
			System.exit(-1);
		}
		
		return coreValues;
	}

	
	public static String collectionToString(Collection<?> collection, String separator) {
		String str = "";
		Object obj;
		for (Iterator<?> iter = collection.iterator(); iter.hasNext(); ) {
			obj=iter.next();
			str += obj.toString();
			if (iter.hasNext())
				str += separator;
		}
		return str;
	}
	
	public static Collection<String> arrayToStringCollectionUpperCase(Object[] collection) {
		Collection<String> col = new ArrayList<String>();
		for (Object obj : collection) {
			String tmp = obj.toString();
			col.add(tmp.substring(0, 1).toUpperCase() + tmp.substring(1, tmp.length()));
		}
		return col;
	}
	public static Collection<String> collectionToStringCollectionUpperCase(Collection<?> collection) {
		Collection<String> col = new ArrayList<String>();
		for (Object obj : collection) {
			String tmp = obj.toString();
			col.add(tmp.substring(0, 1).toUpperCase() + tmp.substring(1, tmp.length()));
		}
		return col;
	}
	public static Collection<String> collectionToStringCollection(Collection<?> collection) {
		Collection<String> col = new ArrayList<String>();
		for (Object obj : collection) {
			col.add(obj.toString());
		}
		return col;
	}
	
	public static boolean isMaskedWith(int aFlags, int aMask) {
		return (aFlags & aMask) != 0;
	}

	/**
	 * 
	 * @param filename the name of the output file
	 * @param str string to write in that file
	 */
	public static void writeToFile(String filename, String str) {
		try{
			java.io.FileWriter fstream = new java.io.FileWriter(filename);
			java.io.BufferedWriter out = new java.io.BufferedWriter(fstream);
			out.write(str);
			out.close();
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public static String printArray(Object[] array) {
		StringBuffer sb = new StringBuffer("[");
		for (Object o : array)
			sb.append(o+", ");
		sb.append("]");
		return sb.toString();
	}
	
	public static void launchGLPK() {
		System.loadLibrary("glpk_java");
		GLPK.glp_term_out(GLPK.GLP_OFF);
	}
	
	public static void launchYices() {
		try {
		if (yicesLite)
			launchYicesN();
		else
			launchYicesO();
		} catch (Exception e) {
			System.err.println("problem when launching yices occured");
			System.exit(-1);
		}
	}
	public static void terminateYices() {
		if (yicesLite)
			terminateYicesN();
		else
			terminateYicesO();		
	}
	
	
	//public static String yicesPath = "/home/ikonecny/flata/lib/yices";
	//private static String yicesCommand = "./extTools/yices.exe";
	private static java.lang.Runtime runtime;
	private static java.lang.Process process;
	
	private static void launchYicesO() {
		runtime = java.lang.Runtime.getRuntime();
		try {
			process = runtime.exec(CR.yicesCommand);
			
			/*
			 * note on Yices
			 *   -  'assert' finds only trivial inconsistencies; to perform full analysis, 
			 *      'check' is necessary 
			 *   -  'status' - gives either 'ok' or 'unsat'
			 *   
			 *   format of output of linear constraints:
			 *     - definitions of variables - 'define'
			 *     - list of linear constraints - 'assert'
			 *     - 'check' command
			 *   if any of 'assert' commands finds trivial inconsistency, further 'assert' 
			 *     or 'check' calls cause message "Logical context is inconsistent. Use (reset) to reset."
			 *     These messages are ignored when reading from yices' stdout.
			 */
			
			// naming in java - Input - meaning sth. is input from our perspective 
			// (e.g. output of a process is an input for us) 
			java.io.InputStream stdout = process.getInputStream();
			java.io.OutputStream stdin = process.getOutputStream();
			//java.io.InputStream stderr = process.getErrorStream();

			CR.yicesStdoutReader = new java.io.BufferedReader(new java.io.InputStreamReader(stdout));
			CR.yicesStdinWriter = new java.io.BufferedWriter(new java.io.OutputStreamWriter(stdin));
			//java.io.BufferedReader stderrReader = new java.io.BufferedReader(new java.io.InputStreamReader(stderr));
			
		} catch (IOException e) {
			System.err.println("Problem when launching Yices. "+e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}		
	}
	private static void terminateYicesO() {
		try {
			CR.yicesStdoutReader.close();
			CR.yicesStdinWriter.close();
			//stderrReader.close();
		} catch (IOException e) {
			System.err.println("Problem when terminating Yices. "+e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		
		process.destroy();
	}
	
	private static YicesLite yl;
	public static String yicesOut = "YicesOut.txt";
	public static String yicesLog = "yicesLog.txt";
	public static BufferedReader yicesReader = null;
	private static int yices_ctx = -1;
	private static void launchYicesN() {
		yl = new YicesLite();
		
		//yices_ctx = yl.yicesl_mk_context();
		yl.yicesl_set_output_file(yicesOut);
		yl.yicesl_enable_log_file(yicesLog);
		
		if (yicesTypeCheck)
			yl.yicesl_enable_type_checker((short)1);
		 
		try {
			yicesReader = new BufferedReader(new FileReader(yicesOut));
	    } catch (FileNotFoundException e) {
	    	System.err.println("yices output file could not be opened");
	    	System.exit(-1);
	    }
	}
	private static void terminateYicesN() {
		//yl.yicesl_del_context(yices_ctx);
		
		if (yicesReader!=null) {
			try {
	      yicesReader.close();
      } catch (IOException e) {
      	System.err.println("yices output file could not be closed");
      	System.exit(-1);
      }
		}
	}
	
  private static String configPath = "./config";
	public static void configure() {
		try {
	    BufferedReader reader = new BufferedReader(new FileReader(configPath));

	    
	    String line = null;
	    while ((line = reader.readLine()) != null) {
		    	Scanner scanner = new Scanner(line);
				scanner.useDelimiter("(\\s|(\\:))+");
		    
				if (scanner.hasNext()) {
					String key = scanner.next().toLowerCase();
					if (!scanner.hasNext()) {
						System.err.println("Key '"+key+"' found, but value is missing.");
						System.exit(-1);
					} else {
						String value = scanner.next();
						if (key.equals("yices")) {
							CR.yicesCommand = value;
						} else {
							System.err.println("Unknown key '"+key+"' found.");
							System.exit(-1);
						}
					}
				}
				scanner.close();
	    }
			
	    reader.close();
    } catch (FileNotFoundException e) {
	    System.err.println("Configuration file '"+configPath+"' not found");
	    System.exit(-1);
    } catch (IOException e) {
    	System.err.println("Configuration file '"+configPath+"' could not be closed");
	    System.exit(-1);
    }
	}
	
	public static void yicesDefineVarsS(IndentedWriter iw, Collection<String> vars) {
		for (String v : vars)
			iw.writeln(CR.yicesDefineVar(v));
	}
	public static void yicesDefineVars(IndentedWriter iw, Set<Variable> vars) {
		for (Variable v : vars)
			iw.writeln(CR.yicesDefineVar(v.name()));
	}
	public static void yicesDefineVarNames(IndentedWriter iw, Collection<String> vars) {
		for (String v : vars)
			iw.writeln(CR.yicesDefineVar(v));
	}
	
	private static String yicesVarPref = "V_";
	public static String yicesVarName(String aVarName) {
		return yicesVarPref+aVarName;
	}
	public static String yicesVarUnname(String aVarName) {
		return aVarName.substring(yicesVarPref.length());
	}
	public static StringBuffer yicesDefineVar(String aVarName) {
		return new StringBuffer("(define "+yicesVarName(aVarName)+"::int)");
	}
	
	public static void printMemUsage(String msg, boolean forceGC) {
		System.err.println(msg);
		System.err.println("Max memory: "+Runtime.getRuntime().maxMemory());
		System.err.println("Total memory: "+Runtime.getRuntime().totalMemory());
		System.err.println("Free memory: "+Runtime.getRuntime().freeMemory());
		if (forceGC) {
			System.gc();
			System.err.println(msg+" after GC");
			System.err.println("Max memory: "+Runtime.getRuntime().maxMemory());
			System.err.println("Total memory: "+Runtime.getRuntime().totalMemory());
			System.err.println("Free memory: "+Runtime.getRuntime().freeMemory());
		}
	}
	
	// remainder and division result can be computed from one another
	// a mod b = c /\ a div b = d  ==> d*b + c = a
	public static int mod(int nom, int den) {
		// c = a - b*d 
		return nom - den * floor(nom,den);
	}
	public static int ceil(int nom, int den) {
		int floor = floor(nom,den);
		if (floor*den == nom) {
			return floor;
		} else {
			return floor+1;
		}
	}
	public static int floor(int nom, int den) {
		boolean minus = (nom < 0 && den > 0) || (nom > 0 && den < 0);
		nom = Math.abs(nom);
		den = Math.abs(den);
		int x = nom / den;
		if (minus) {
			x = -1*x;
			if (minus && nom % den != 0)
				x = x - 1;
		}
		
		return x;
	}
	
	public static File processParameters(String[] args) {
		
		// last parameter is the input file
		if (args.length == 0) {
			Parameters.printUsageAndExit();
		}
		
		int maxInx = args.length-2;
		
		Parameters.processParameters(args, 0, maxInx);
		
		File f = new File(args[args.length-1]);
		if (!f.exists()) {
			System.out.println("Input file '"+f.getAbsolutePath()+"' not found.");
			Parameters.printUsageAndExit();
		}
		return f;
	}
	
	public static void col2sb(IndentedWriter iw, Collection<? extends Object> col) {
		if (col != null) {
			for (Object o : col) {
				iw.writeln(o.toString());			
			}
		}
	}
	public static StringBuffer arr2sb(Object[] arr) {
		StringBuffer sb = new StringBuffer();
		if (arr != null) {
			for (int i=0; i<arr.length; i++) {
				sb.append(arr[i]);
				if (i != arr.length-1)
					sb.append(",");
			}
		}
		return sb;
	}
	public static StringBuffer col2sb(Collection<? extends Object> col) {
		StringBuffer sb = new StringBuffer();
		if (col != null) {
			Iterator<? extends Object> iter = col.iterator();
			while (iter.hasNext()) {
				Object o = iter.next();
				sb.append(o.toString());
				if (iter.hasNext())
					sb.append(",");
			}
		}
		return sb;
	}
	
	public static void yicesAndStart(IndentedWriter iw) {
		iw.writeln("(and");
		iw.indentInc();
	}
	public static void yicesAndEnd(IndentedWriter iw) {
		iw.indentDec();
		iw.writeln(")");
	}

}
