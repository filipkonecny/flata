package verimag.flata.common;
	
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.gnu.glpk.GLPK;
import org.sosy_lab.java_smt.api.Model.ValueAssignment;

import com.google.common.collect.ImmutableList;

/**
 * Resources shared among various objects. They are implemented as static
 * members.
 */
public class CR {

	public static FlataJavaSMT flataJavaSMT;

	public static boolean RELEASE = true;
	
	public static boolean NO_POSTPARSING = false;
	
	public static String TRANS_ID_NAME = "id";

	public static String SUF_CA = ".ca";
	public static String SUF_FAST = ".fst";
	public static String SUF_ASPIC = ".aspic";
	public static String SUF_TREX = ".if";
	public static String SUF_ARMC = ".armc";
	public static String SUF_NTS = ".nts";
	
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

	public static final int eOUT_FLATA=0x01;
	public static final int eOUT_FAST=0x02;
	public static final int eOUT_ARMC=0x04;
	
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

	// TODO: check validity
	// TODO: add description saying that it can only be called when sat???
	public static Map<String, String> parseModelAssignments() {
		ImmutableList<ValueAssignment> modelAssignments = CR.flataJavaSMT.getModelAssignments();
		Map<String, String> model = new HashMap<String, String>();
		for (ValueAssignment va : modelAssignments) {
			model.put(va.getKey().toString(), va.getValue().toString());
		}
		return model;
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

	public static void initFLataJavaSMT(String solver) {
		flataJavaSMT = new FlataJavaSMT(solver);
	}
	public static void initFLataJavaSMT() {
		flataJavaSMT = new FlataJavaSMT();
	}
	
	// TODO: remove???
//   private static String configPath = "./config";
// 	public static void configure() {
// 		try {
// 	    BufferedReader reader = new BufferedReader(new FileReader(configPath));

	    
// 	    String line = null;
// 	    while ((line = reader.readLine()) != null) {
// 		    	Scanner scanner = new Scanner(line);
// 				scanner.useDelimiter("(\\s|(\\:))+");
		    
// 				if (scanner.hasNext()) {
// 					String key = scanner.next().toLowerCase();
// 					if (!scanner.hasNext()) {
// 						System.err.println("Key '"+key+"' found, but value is missing.");
// 						System.exit(-1);
// 					} else {
// 						String value = scanner.next();
// 						if (key.equals("yices")) {
// 							CR.yicesCommand = value;
// 						} else {
// 							System.err.println("Unknown key '"+key+"' found.");
// 							System.exit(-1);
// 						}
// 					}
// 				}
// 				scanner.close();
// 	    }
			
// 	    reader.close();
//     } catch (FileNotFoundException e) {
// 	    System.err.println("Configuration file '"+configPath+"' not found");
// 	    System.exit(-1);
//     } catch (IOException e) {
//     	System.err.println("Configuration file '"+configPath+"' could not be closed");
// 	    System.exit(-1);
//     }
// 	}
	
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
}
