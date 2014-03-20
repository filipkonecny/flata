package verimag.flata.acceleration.zigzag;
/**
 * The output of the acceleration, containing the symbol table used
 * and 12 matrices with the constraints computed (as semilinear sets)
 */

import java.util.*;

public class Output {
	//the maximum number of steps in which the relation becomes inconsistent
	private int maxSteps = -1;

	//public static final String STR_PLUS  = "$p";
	//public static final String STR_MINUS = "$m";
	//public static final int SUF_LEN = 2;
	
	private SLSet[][][] edges = null; 
	
		//holding constraints of the form x<=a, x'<=a,
		//-x<=a and respectively -x'<=a
	private SLSet[][] singulars = null;
	
	private Vector<String> originalSymbols = null; //the initial variables (no '+','-') 
	private Map<String, Integer> backLink = null;

		//x-y<=a
	public static final int UNP_UNP_POTENTIAL = 0;
	public static final int UNP_PRIMED_POTENTIAL = 1;
	public static final int PRIMED_UNP_POTENTIAL = 2;
	public static final int PRIMED_PRIMED_POTENTIAL = 3;
		//x+y<=a
	public static final int UNP_UNP_PLUS= 4;
	public static final int UNP_PRIMED_PLUS = 5;
	public static final int PRIMED_UNP_PLUS = 6;
	public static final int PRIMED_PRIMED_PLUS = 7;
		//-x-y<=a
	public static final int UNP_UNP_MINUS = 8;
	public static final int UNP_PRIMED_MINUS = 9;
	public static final int PRIMED_UNP_MINUS = 10;
	public static final int PRIMED_PRIMED_MINUS = 11;
	
		//for accessing constraints of the form x<=a and respectively -x<=a
	public static final int UNPRIMED_PLUS = 0;
	public static final int PRIMED_PLUS = 1;
	public static final int UNPRIMED_MINUS = 2;
	public static final int PRIMED_MINUS = 3;
	
	public static boolean isSymetric(int i) {
		return 
			(i==UNP_UNP_PLUS)
			|| (i==PRIMED_PRIMED_PLUS)
			|| (i==UNP_UNP_MINUS)
			|| (i==PRIMED_PRIMED_MINUS);
	}
	public static boolean isFirstVarPrimed(int i) {
		return 
			(i==PRIMED_UNP_POTENTIAL)
			|| (i==PRIMED_PRIMED_POTENTIAL)
			|| (i==PRIMED_UNP_PLUS)
			|| (i==PRIMED_PRIMED_PLUS)
			|| (i==PRIMED_UNP_MINUS)
			|| (i==PRIMED_PRIMED_MINUS);
	}
	public static boolean isSecondVarPrimed(int i) {
		return 
			(i==PRIMED_PRIMED_POTENTIAL)
			|| (i==UNP_PRIMED_POTENTIAL)
			|| (i==PRIMED_PRIMED_PLUS)
			|| (i==UNP_PRIMED_PLUS)
			|| (i==PRIMED_PRIMED_MINUS)
			|| (i==UNP_PRIMED_MINUS);
	}
	public static boolean isFirstVarPlus(int i) {
		return 
			(i==UNP_UNP_POTENTIAL)
			|| (i==UNP_PRIMED_POTENTIAL)
			|| (i==PRIMED_UNP_POTENTIAL)
			|| (i==PRIMED_PRIMED_POTENTIAL)
			|| (i==UNPRIMED_PLUS)
			|| (i==PRIMED_PLUS)
			|| (i==PRIMED_UNP_PLUS)
			|| (i==PRIMED_PRIMED_PLUS);
	}

	public static boolean isSecondVarPlus(int i) {
		return 
			(i==UNP_UNP_PLUS)
			|| (i==UNP_PRIMED_PLUS)
			|| (i==PRIMED_UNP_PLUS)
			|| (i==PRIMED_PRIMED_PLUS);
	}
	
	public String toString() {
		String str="";
		str += "Variables:\n";
		str += originalSymbols.toString()+"\n";
		str += "Matrices:\n";
		for (int i=0; i<edges.length; ++i) {
			str += "[\n";
			for (int j=0; j<edges[i].length; ++j) {
				str += "  [";
				for (int k=0; k<edges[i][j].length; ++k) {
					SLSet o = edges[i][j][k];
					str += (o==null)?"inf":o;
					if (k!=edges[i][j].length-1)
						str += ",";
				}
				str += "]\n";
			}
			str += "]\n";
		}
		return str;
	}	

	
	public Output(OctagonalClosure closure)
	{
		originalSymbols = new Vector<String>();
		backLink = new HashMap<String, Integer>();

		if (closure == null)
			return;
		
		maxSteps = closure.getMaxPositiveSteps();
		
		originalSymbols = closure.extractOriginals(originalSymbols);
		for (int i = 0; i < originalSymbols.size(); i++)
			backLink.put(originalSymbols.get(i), new Integer(i));
		
		edges = new SLSet[12][originalSymbols.size()][originalSymbols.size()];
		singulars = new SLSet[4][originalSymbols.size()];
		
		for (int m = 0; m < closure.getSymbolTable().size(); m++)
			for (int n = 0; n < closure.getSymbolTable().size(); n++)
				if (closure.getUnprimedUnprimed(m, n) != null && 
					!closure.getUnprimedUnprimed(m, n).empty())
				{
					String x = closure.getSymbolTable().elementAt(m);
					String y = closure.getSymbolTable().elementAt(n);

					String xx = x.substring(0, x.length()-1);
					String yy = y.substring(0, y.length()-1);
					
						//The same variable is saved both in positive form (plus - "p")
						//and negative form (minus - "m"); the following lines recover 
						//a variable's original name and sign
					if (closure.isOctagonal() &&
							(x.endsWith("p") || x.endsWith("m") || x.startsWith(Parser.ZERO)) &&
							(y.endsWith("p") || y.endsWith("m") || y.startsWith(Parser.ZERO)))
					{
						/*
						System.out.print(x+"-"+y+"<=");
						closure.getUnprimedUnprimed(m, n).print();
						System.out.println();
						//*/
						
						if (!x.startsWith(Parser.ZERO) && !y.startsWith(Parser.ZERO))
						{
							int i = backLink.get(xx).intValue();
							int j = backLink.get(yy).intValue();

							if (x.endsWith("m") && y.endsWith("m"))
								edges[UNP_UNP_POTENTIAL][j][i] = closure.getUnprimedUnprimed(m, n);
							else if (x.endsWith("p") && y.endsWith("p"))
								edges[UNP_UNP_POTENTIAL][i][j] = closure.getUnprimedUnprimed(m, n);
							else if (x.endsWith("m") && y.endsWith("p"))
							{
								edges[UNP_UNP_MINUS][i][j] = closure.getUnprimedUnprimed(m, n);
								edges[UNP_UNP_MINUS][j][i] = closure.getUnprimedUnprimed(m, n);
							}
							else if (x.endsWith("p") && y.endsWith("m"))
							{
								edges[UNP_UNP_PLUS][i][j] = closure.getUnprimedUnprimed(m, n);
								edges[UNP_UNP_PLUS][j][i] = closure.getUnprimedUnprimed(m, n);
							}
						}
						else if (x.startsWith(Parser.ZERO) && !(y.startsWith(Parser.ZERO)))
						{
							int j = backLink.get(yy).intValue();
							if (y.endsWith("p"))
								singulars[UNPRIMED_MINUS][j] = closure.getUnprimedUnprimed(m, n);
							else if (y.endsWith("m"))
								singulars[UNPRIMED_PLUS][j] = closure.getUnprimedUnprimed(m, n);
						}
						else if (y.startsWith(Parser.ZERO) && !(x.startsWith(Parser.ZERO)))
						{
							int i = backLink.get(xx).intValue();
							if (x.endsWith("p"))
								singulars[UNPRIMED_PLUS][i] = closure.getUnprimedUnprimed(m, n);
							else if (x.endsWith("m"))
								singulars[UNPRIMED_MINUS][i] = closure.getUnprimedUnprimed(m, n);
						}
					}
					
					if (!closure.isOctagonal() && !x.startsWith("_") &&!y.startsWith("_"))
					{
						if (!x.startsWith(Parser.ZERO) && !y.startsWith(Parser.ZERO))
						{
							int i = backLink.get(x).intValue();
							int j = backLink.get(y).intValue();

							edges[UNP_UNP_POTENTIAL][i][j] = closure.getUnprimedUnprimed(m, n);
						}
						else if (x.startsWith(Parser.ZERO) && !(y.startsWith(Parser.ZERO)))
						{
							int j = backLink.get(y).intValue();
							singulars[UNPRIMED_MINUS][j] = closure.getUnprimedUnprimed(m, n);
						}
						else if (y.startsWith(Parser.ZERO) && !(x.startsWith(Parser.ZERO)))
						{
							int i = backLink.get(x).intValue();
							singulars[UNPRIMED_PLUS][i] = closure.getUnprimedUnprimed(m, n);
						}
					}
				}

		for (int m = 0; m < closure.getSymbolTable().size(); m++)
			for (int n = 0; n < closure.getSymbolTable().size(); n++)
				if (closure.getUnprimedPrimed(m, n) != null && !closure.getUnprimedPrimed(m, n).empty())
				{
					String x = closure.getSymbolTable().elementAt(m);
					String y = closure.getSymbolTable().elementAt(n);

					String xx = x.substring(0, x.length()-1);
					String yy = y.substring(0, y.length()-1);
					
						//The same variable is saved both in positive form (plus - "p")
						//and negative form (minus - "m"); the following lines recover 
						//a variable's original name and sign
					if (closure.isOctagonal() &&
							(x.endsWith("p") || x.endsWith("m") || x.equals(Parser.ZERO)) &&
							(y.endsWith("p") || y.endsWith("m") || y.equals(Parser.ZERO)))
					{
						/*
						System.out.print(x+"-"+y+"'<=");
						closure.getUnprimedPrimed(m, n).print();
						System.out.println();
						*/
						
						if (!x.equals(Parser.ZERO) && !y.equals(Parser.ZERO))
						{
							int i = backLink.get(xx).intValue();
							int j = backLink.get(yy).intValue();

							if (x.endsWith("m") && y.endsWith("m"))
								edges[PRIMED_UNP_POTENTIAL][j][i] = closure.getUnprimedPrimed(m, n);
							else if (x.endsWith("p") && y.endsWith("p"))
								edges[UNP_PRIMED_POTENTIAL][i][j] = closure.getUnprimedPrimed(m, n);
							else if (x.endsWith("m") && y.endsWith("p"))
							{
								edges[UNP_PRIMED_MINUS][i][j] = closure.getUnprimedPrimed(m, n);
								edges[PRIMED_UNP_MINUS][j][i] = closure.getUnprimedPrimed(m, n);
							}
							else if (x.endsWith("p") && y.endsWith("m"))
							{
								edges[UNP_PRIMED_PLUS][i][j] = closure.getUnprimedPrimed(m, n);
								edges[PRIMED_UNP_PLUS][j][i] = closure.getUnprimedPrimed(m, n);
							}
						}
						else if (x.equals(Parser.ZERO) && !(y.equals(Parser.ZERO)))
						{
							int j = backLink.get(yy).intValue();

							if (y.endsWith("p"))
								singulars[PRIMED_MINUS][j] = closure.getUnprimedPrimed(m, n);
							else if (y.endsWith("m"))
								singulars[PRIMED_PLUS][j] = closure.getUnprimedPrimed(m, n);
						}
						else if (y.equals(Parser.ZERO) && !(x.equals(Parser.ZERO)))
						{
							int i = backLink.get(xx).intValue();

							if (x.endsWith("p"))
								singulars[UNPRIMED_PLUS][i] = closure.getUnprimedPrimed(m, n);
							else if (x.endsWith("m"))
								singulars[UNPRIMED_MINUS][i] = closure.getUnprimedPrimed(m, n);
						}
					}
					
					if (!closure.isOctagonal() && !x.startsWith("_") &&!y.startsWith("_"))
					{
						if (!x.equals(Parser.ZERO) && !y.equals(Parser.ZERO))
						{
							int i = backLink.get(x).intValue();
							int j = backLink.get(y).intValue();

							edges[UNP_PRIMED_POTENTIAL][i][j] = closure.getUnprimedPrimed(m, n);
						}
						else if (x.equals(Parser.ZERO) && !(y.equals(Parser.ZERO)))
						{
							int j = backLink.get(y).intValue();

							singulars[PRIMED_MINUS][j] = closure.getUnprimedPrimed(m, n);
						}
						else if (y.equals(Parser.ZERO) && !(x.equals(Parser.ZERO)))
						{
							int i = backLink.get(x).intValue();
							singulars[UNPRIMED_PLUS][i] = closure.getUnprimedPrimed(m, n);
						}
					}
				}

		for (int m = 0; m < closure.getSymbolTable().size(); m++)
			for (int n = 0; n < closure.getSymbolTable().size(); n++)
				if (closure.getPrimedUnprimed(m, n) != null && !closure.getPrimedUnprimed(m, n).empty())
				{
					String x =  closure.getSymbolTable().elementAt(m);
					String y =  closure.getSymbolTable().elementAt(n);

					String xx = x.substring(0, x.length()-1);
					String yy = y.substring(0, y.length()-1);
					
						//The same variable is saved both in positive form (plus - "p")
						//and negative form (minus - "m"); the following lines recover 
						//a variable's original name and sign
					if (closure.isOctagonal() &&
							(x.endsWith("p") || x.endsWith("m") || x.equals(Parser.ZERO)) &&
							(y.endsWith("p") || y.endsWith("m") || y.equals(Parser.ZERO)))
					{
						/*
						System.out.print(x+"'-"+y+"<=");
						closure.getPrimedUnprimed(m, n).print();
						System.out.println();
						*/
						
						if (!x.equals(Parser.ZERO) && !y.equals(Parser.ZERO))
						{
							int i = backLink.get(xx).intValue();
							int j = backLink.get(yy).intValue();

							if (x.endsWith("m") && y.endsWith("m"))
								edges[UNP_PRIMED_POTENTIAL][j][i] = closure.getPrimedUnprimed(m, n);
							else if (x.endsWith("p") && y.endsWith("p"))
								edges[PRIMED_UNP_POTENTIAL][i][j] = closure.getPrimedUnprimed(m, n);
							else if (x.endsWith("m") && y.endsWith("p"))
							{
								edges[PRIMED_UNP_MINUS][i][j] = closure.getPrimedUnprimed(m, n);
								edges[UNP_PRIMED_MINUS][j][i] = closure.getPrimedUnprimed(m, n);
							}
							else if (x.endsWith("p") && y.endsWith("m"))
							{
								edges[PRIMED_UNP_PLUS][i][j] = closure.getPrimedUnprimed(m, n);
								edges[UNP_PRIMED_PLUS][j][i] = closure.getPrimedUnprimed(m, n);
							}
						}
						else if (x.equals(Parser.ZERO) && !(y.equals(Parser.ZERO)))
						{
							int j = backLink.get(yy).intValue();

							if (y.endsWith("p"))
								singulars[UNPRIMED_MINUS][j] = closure.getPrimedUnprimed(m, n);
							else if (y.endsWith("m"))
								singulars[UNPRIMED_PLUS][j] = closure.getPrimedUnprimed(m, n);
						}
						else if (y.equals(Parser.ZERO) && !(x.equals(Parser.ZERO)))
						{
							int i = backLink.get(xx).intValue();

							if (x.endsWith("p"))
								singulars[PRIMED_PLUS][i] = closure.getPrimedUnprimed(m, n);
							else if (x.endsWith("m"))
								singulars[PRIMED_MINUS][i] = closure.getPrimedUnprimed(m, n);
						}
					}
					
					if (!closure.isOctagonal() && !x.startsWith("_") &&!y.startsWith("_"))
					{
						if (!x.equals(Parser.ZERO) && !y.equals(Parser.ZERO))
						{
							int i = backLink.get(x).intValue();
							int j = backLink.get(y).intValue();

							edges[PRIMED_UNP_POTENTIAL][i][j] = closure.getPrimedUnprimed(m, n);
						}
						else if (x.equals(Parser.ZERO) && !(y.equals(Parser.ZERO)))
						{
							int j = backLink.get(y).intValue();
							singulars[UNPRIMED_MINUS][j] = closure.getPrimedUnprimed(m, n);
						}
						else if (y.equals(Parser.ZERO) && !(x.equals(Parser.ZERO)))
						{
							int i = backLink.get(x).intValue();
							singulars[PRIMED_PLUS][i] = closure.getPrimedUnprimed(m, n);
						}
					}
				}

		for (int m = 0; m < closure.getSymbolTable().size(); m++)
			for (int n = 0; n < closure.getSymbolTable().size(); n++) 
				if ((closure.getPrimedPrimed(m, n) != null) && 
						!closure.getPrimedPrimed(m, n).empty()) {
					String x =  closure.getSymbolTable().elementAt(m);
					String y =  closure.getSymbolTable().elementAt(n);

					String xx = x.substring(0, x.length()-1);
					String yy = y.substring(0, y.length()-1);
					
						//The same variable is saved both in positive form (plus - "p")
						//and negative form (minus - "m"); the following lines recover 
						//a variable's original name and sign
					if (closure.isOctagonal() &&
							(x.endsWith("p") || x.endsWith("m") || x.equals(Parser.ZERO)) &&
							(y.endsWith("p") || y.endsWith("m") || y.equals(Parser.ZERO)))
					{
						/*
						System.out.print(x+"'-"+y+"'<=");
						closure.getPrimedPrimed(m, n).print();
						System.out.println();
						*/
						
						if (!x.equals(Parser.ZERO) && !y.equals(Parser.ZERO))
						{
							int i = backLink.get(xx).intValue();
							int j = backLink.get(yy).intValue();

							if (x.endsWith("m") && y.endsWith("m"))
								edges[PRIMED_PRIMED_POTENTIAL][j][i] = closure.getPrimedPrimed(m, n);
							else if (x.endsWith("p") && y.endsWith("p"))
								edges[PRIMED_PRIMED_POTENTIAL][i][j] = closure.getPrimedPrimed(m, n);
							else if (x.endsWith("m") && y.endsWith("p"))
							{
								edges[PRIMED_PRIMED_MINUS][i][j] = closure.getPrimedPrimed(m, n);
								edges[PRIMED_PRIMED_MINUS][j][i] = closure.getPrimedPrimed(m, n);
							}
							else if (x.endsWith("p") && y.endsWith("m"))
							{
								edges[PRIMED_PRIMED_PLUS][i][j] = closure.getPrimedPrimed(m, n);
								edges[PRIMED_PRIMED_PLUS][j][i] = closure.getPrimedPrimed(m, n);
							}
						}
						else if (x.equals(Parser.ZERO) && !(y.equals(Parser.ZERO)))
						{
							int j = backLink.get(yy).intValue();

							if (y.endsWith("p"))
								singulars[PRIMED_MINUS][j] = closure.getPrimedPrimed(m, n);
							else if (y.endsWith("m"))
								singulars[PRIMED_PLUS][j] = closure.getPrimedPrimed(m, n);
						}
						else if (y.equals(Parser.ZERO) && !(x.equals(Parser.ZERO)))
						{
							int i = backLink.get(xx).intValue();

							if (x.endsWith("p"))
								singulars[PRIMED_PLUS][i] = closure.getPrimedPrimed(m, n);
							else if (x.endsWith("m"))
								singulars[PRIMED_MINUS][i] = closure.getPrimedPrimed(m, n);
						}
					}
					
					if (!closure.isOctagonal() && !x.startsWith("_") &&!y.startsWith("_"))
					{
						if (!x.equals(Parser.ZERO) && !y.equals(Parser.ZERO))
						{
							int i = backLink.get(x).intValue();
							int j = backLink.get(y).intValue();

							edges[PRIMED_PRIMED_POTENTIAL][i][j] = closure.getPrimedPrimed(m, n);
						}
						else if (x.equals(Parser.ZERO) && !(y.equals(Parser.ZERO)))
						{
							int j = backLink.get(y).intValue();
							singulars[PRIMED_MINUS][j] = closure.getPrimedPrimed(m, n);
						}
						else if (y.equals(Parser.ZERO) && !(x.equals(Parser.ZERO)))
						{
							int i = backLink.get(x).intValue();
							singulars[PRIMED_PLUS][i] = closure.getPrimedPrimed(m, n);
						}
					}
				}
	}

	

/**
 * 
 * @return a vector of strings, namely the symbols (variables) in the relation
 */
	public Vector<String> getSymbolTable()
	{
		return originalSymbols;
	}
	
/**
 * 
 * @return a hashtable, which maps names of variables (as strings) 
 * to their indices in the symbol table
 */	
	public Map<String, Integer> getBackLink()
	{
		return backLink;
	}
	
/**
 * 
 * @return a vector of 12 matrices, each of which corresponds to a 
 * kind of constraints (x+y<=a,x-y<=a,-x-y<=a, primed and unprimed)
 */	
	public SLSet[][][] getEdges()
	{
		return edges;
	}
	
/**
 * 
 * @param i - the index of a constraint matrix; can be one of the 
 * predefined constants in this class 
 * @return the constraint matrix corresponding to the index 
 */	
	public SLSet[][] getMatrix(int i)
	{
		if (i < 0 || i > 11)
			return null;
		return edges[i];
	}

	/**
	 * getter for the constraints of type +-x<=a,+-x'<=a
	 * @param i the desired type of relation: 
	 * UNPRIMED_PLUS, UNPRIMED_MINUS, PRIMED_PLUS or PRIMED_MINUS
	 * @param j the index of the desired variable
	 * @return the desired constraint, as a semilinear set 
	 */
	public SLSet getSingular(int i, int j)
	{
		if (i < 0 || i > 3)
			return null;
		return singulars[i][j];
	}

	/**
	 * 
	 * @return the maximum number of steps in which the relation 
	 * remains consistent
	 * -1 - always consistent
	 * 0 - inconsistent initial relation
	 * N>0 - inconsistent after N steps
	 */
	public int getMaxPositiveSteps()
	{
		return maxSteps;
	}

	public void printClosure(ZigzagMatrix fw, ZigzagMatrix bw, Vector<String> sym_tab) {
		
		System.out.println("SYMBOL TABLE:");
		System.out.println(sym_tab);	
		
		//The transitive closure is printed
		for (int i = 0; i < 12; i++)
			for (int m = 0; m < this.getSymbolTable().size(); m++)
				for (int n = 0; n < this.getSymbolTable().size(); n++)
				{
					String x = this.getSymbolTable().get(m);
					String y = this.getSymbolTable().get(n);
					
					if (this.getEdges()[i][m][n]!= null && 
						!this.getEdges()[i][m][n].empty())
					{
						int j = i / 4;

						if ( j == 2 )
							System.out.print("-");

						System.out.print(x);
						if (i % 4 == 2 || i % 4 == 3)
							System.out.print("'");

						if ( j == 1 )
							System.out.print("+");
						else
							System.out.print("-");

						System.out.print(y);
						if (i % 2 == 1)
							System.out.print("'");
						
						System.out.print("<=");
						System.out.println(this.getEdges()[i][m][n]);
						System.out.println();						
					}
				}
		
		for (int i = 0; i < 4; i++)
			for (int m = 0; m < this.getSymbolTable().size(); m++)
				{
					String x = this.getSymbolTable().get(m);
					
					SLSet sls = this.getSingular(i,m);
					if (sls!= null && 
						!sls.empty())
					{
						if ( i>1 )
							System.out.print("-");

						System.out.print(x);
						if (  i%2 == 1)
							System.out.print("'");
						
						System.out.print("<=");
						System.out.println(sls);
						System.out.println();						
					}
				}		
	}

	
}