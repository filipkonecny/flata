package verimag.flata.acceleration.zigzag.flataofpca;

/**
 * The output of the acceleration, containing the symbol table used
 * and 12 matrices with the constraints computed (as semilinear sets)
 */

import java.io.Serializable;
import java.util.*;

import verimag.flata.acceleration.zigzag.*;
import verimag.flata.presburger.LinearTerm;
import verimag.flata.presburger.Variable;
import verimag.flata.presburger.VariablePool;

public class OfpcaOutput implements Serializable {
	//the maximum number of steps in which the relation becomes inconsistent
	private int maxSteps = -1;

	//public static final String STR_PLUS  = "$p";
	//public static final String STR_MINUS = "$m";
	//public static final int SUF_LEN = 2;
	
	private SLSet[][][] edges = null; 
	
		//holding constraints of the form x<=a, x'<=a,
		//-x<=a and respectively -x'<=a
	private SLSet[][] singulars = null;

	//LinearTerm[] substitution;
	//private int submatSize;
	private int submatSize;
	private boolean isZeroInx(int i) { return i==0; }
	private boolean isPlusInx(int i) { return (i % 2 == 0); }
	private boolean isMinusInx(int i) { return (i % 2 == 1); }
	
	private Vector<String> origUnpSymbols = null; //the initial variables (no '+','-', 0) 
	//private Map<String, Integer> backLink = null;
	
	public Vector<String> getSymbolTable()
	{
		return origUnpSymbols;
	}

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
	
	public static boolean isVarPlus(int i) {
		return 
			(i==UNPRIMED_PLUS)
			|| (i==PRIMED_PLUS);
	}
	public static boolean isVarPrimed(int i) {
		return 
			(i==PRIMED_PLUS)
			|| (i==PRIMED_MINUS);
	}
	
	/*public String toString() {
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
	}*/
	private void dbmCommon(OctagonalClosure closure, int type) {
		int inx1, inx2, inx3;
		switch (type) {
		case OctagonalClosure.UNP_UNP:
			inx1 = UNP_UNP_POTENTIAL;
			inx2 = UNPRIMED_MINUS;
			inx3 = UNPRIMED_PLUS;
			break;
		case OctagonalClosure.UNP_P:
			inx1 = UNP_PRIMED_POTENTIAL;
			inx2 = PRIMED_MINUS;
			inx3 = UNPRIMED_PLUS;
			break;
		case OctagonalClosure.P_UNP:
			inx1 = PRIMED_UNP_POTENTIAL;
			inx2 = UNPRIMED_MINUS;
			inx3 = PRIMED_PLUS;
			break;
		case OctagonalClosure.P_P:
			inx1 = PRIMED_PRIMED_POTENTIAL;
			inx2 = PRIMED_MINUS;
			inx3 = PRIMED_PLUS;
			break;
		default:
			throw new RuntimeException("unexpected parameter");
		}
		
		for (int m = 0; m < submatSize; m++)
		//for (int m = 0; m < closure.getSymbolTable().size(); m++)
			for (int n = 0; n < submatSize; n++) {
			//for (int n = 0; n < closure.getSymbolTable().size(); n++) {
				SLSet slset = closure.get(m, n, type);
				if (slset != null && 
					!slset.empty())
				{
					//String x = closure.getSymbolTable().elementAt(m);
					//String y = closure.getSymbolTable().elementAt(n);
					
					//if (!closure.isOctagonal() && !x.startsWith(FlataOfpca.AUX_VAR_PREF) &&!y.startsWith(FlataOfpca.AUX_VAR_PREF))

					int i = m-1;
					int j = n-1;
					
					if (!isZeroInx(m) && !isZeroInx(n))
					//if (!x.startsWith(FlataOfpca.ZERO) && !y.startsWith(FlataOfpca.ZERO))
					{
						edges[inx1][i][j] = slset;
					}
					else if (isZeroInx(m) && !isZeroInx(n))
					//else if (x.equals(FlataOfpca.ZERO) && !(y.equals(FlataOfpca.ZERO)))
					{
						singulars[inx2][j] = slset;
					}
					else if (!isZeroInx(m) && isZeroInx(n))
					//else if (y.equals(FlataOfpca.ZERO) && !(x.equals(FlataOfpca.ZERO)))
					{
						singulars[inx3][i] = slset;
					}
				}
			}
	}
	private void dbm(OctagonalClosure closure) {
		
		dbmCommon(closure,OctagonalClosure.UNP_UNP);
		dbmCommon(closure,OctagonalClosure.UNP_P);
		dbmCommon(closure,OctagonalClosure.P_UNP);
		dbmCommon(closure,OctagonalClosure.P_P);		
	}
	
	private void octagonalCommon(OctagonalClosure closure, int type) {
		// for types of call -> keep all the indices in the 4-row matrix
		int mtype;
		switch (type) {
		case OctagonalClosure.UNP_UNP: 
			mtype = 0; break;
		case OctagonalClosure.UNP_P: 
			mtype = 1; break;
		case OctagonalClosure.P_UNP: 
			mtype = 2; break;
		case OctagonalClosure.P_P: 
			mtype = 3; break;
		default:
			throw new RuntimeException("unexpected parameter");
		}
		int[][] inx = {
				// unp unp
				{
					UNP_UNP_POTENTIAL,     // xm-ym     ~  y-x
					UNP_UNP_POTENTIAL,     // xp-yp     ~  x-y
					UNP_UNP_MINUS,         // xm-yp (1) ~ -x-y
					UNP_UNP_MINUS,         // xm-yp (2) ~ -y-x
					UNP_UNP_PLUS,          // xp-ym (1) ~  x+y
					UNP_UNP_PLUS          // xp-ym (2) ~  y+x
				},
				// unp primed
				{
					PRIMED_UNP_POTENTIAL,
					UNP_PRIMED_POTENTIAL,
					UNP_PRIMED_MINUS,
					PRIMED_UNP_MINUS,
					UNP_PRIMED_PLUS,
					PRIMED_UNP_PLUS,
				},
				// primed unp
				{
					UNP_PRIMED_POTENTIAL,
					PRIMED_UNP_POTENTIAL,
					PRIMED_UNP_MINUS,
					UNP_PRIMED_MINUS,
					PRIMED_UNP_PLUS,
					UNP_PRIMED_PLUS
				},
				// primed primed
				{
					PRIMED_PRIMED_POTENTIAL,
					PRIMED_PRIMED_POTENTIAL,
					PRIMED_PRIMED_MINUS,
					PRIMED_PRIMED_MINUS,
					PRIMED_PRIMED_PLUS,
					PRIMED_PRIMED_PLUS				
				}
		};
		for (int m = 0; m < submatSize; m++)
		//for (int m = 0; m < closure.getSymbolTable().size(); m++)
			for (int n = 0; n < submatSize; n++) {
			//for (int n = 0; n < closure.getSymbolTable().size(); n++) {
				SLSet slset = closure.get(m, n, type);
				if (slset != null && 
					!slset.empty())
				{					
					int i = m / 2;
					int j = n / 2;
					if (i!=j || type == OctagonalClosure.UNP_P || type == OctagonalClosure.P_UNP) {
						if (isMinusInx(m) && isMinusInx(n))
							edges[inx[mtype][0]][j][i] = slset;
						else if (isPlusInx(m) && isPlusInx(n))
							edges[inx[mtype][1]][i][j] = slset;
						else if (isMinusInx(m) && isPlusInx(n))
						{
							edges[inx[mtype][2]][i][j] = slset;
							edges[inx[mtype][3]][j][i] = slset;
						}
						else if (isPlusInx(m) && isMinusInx(n))
						{
							edges[inx[mtype][4]][i][j] = slset;
							edges[inx[mtype][5]][j][i] = slset;
						}
					} else if (m!=n) {
						
						if (isMinusInx(n)) { // xp - xm == 2x
							if (type == OctagonalClosure.P_P) {
								singulars[PRIMED_PLUS][i] = slset;
							} else if (type == OctagonalClosure.UNP_UNP) {
								singulars[UNPRIMED_PLUS][i] = slset;
							} else {
								throw new RuntimeException("internal error: ofpca - octagonal closure");
							}
						} else {  // xm - xp == -2x
							if (type == OctagonalClosure.P_P) {
								singulars[PRIMED_MINUS][i] = slset;
							} else if (type == OctagonalClosure.UNP_UNP) {
								singulars[UNPRIMED_MINUS][i] = slset;
							} else {
								throw new RuntimeException("internal error: ofpca - octagonal closure");
							}
						}
						
					}
				}
			}
	}
	
	private void octagonal(OctagonalClosure closure) {
		
		octagonalCommon(closure,OctagonalClosure.UNP_UNP);
		octagonalCommon(closure,OctagonalClosure.UNP_P);
		octagonalCommon(closure,OctagonalClosure.P_UNP);
		octagonalCommon(closure,OctagonalClosure.P_P);		
	}
	
	
	
	public OfpcaOutput(OctagonalClosure closure, Vector<String> aOrigUnpSymbols, int aSubmatSize, boolean aOctagonal)
	{
		submatSize = aSubmatSize;
		
		origUnpSymbols = aOrigUnpSymbols;
		
		if (closure == null)
			return;
		
		maxSteps = closure.getMaxPositiveSteps();
				
		int outputMatSize = origUnpSymbols.size();
		edges = new SLSet[12][outputMatSize][outputMatSize];
		singulars = new SLSet[4][outputMatSize];
		
		// transformation back to octagonal constraints which use only original symbols:
		//   - ignore all relations involving auxiliary variables (ones used when creating new F/B relations)
		//   - in case of octagons, consider all cases which may be represented by Xp-Ym, Xm-Yp, Xp-Yp, Xm-Ym relations 
		//         (X,Y - primed or unprimed variables)
		if (!closure.isOctagonal()) {
			dbm(closure);
		} else {
			octagonal(closure);
		}
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

	
	private static void debug(ZigzagMatrix m, Vector<String> sym_tab) {
		System.out.print("\t");
		for (int i = 0; i < sym_tab.size(); i ++)
			System.out.print(sym_tab.elementAt(i) + "\t");
		System.out.println();
		
		for (int i = 0; i < m.getSize(); i ++) {
			System.out.print(sym_tab.elementAt(i) + "\t");
			for (int j = 0; j < m.getSize(); j ++) {
				System.out.print(m.eles(i, j) + "\t");
			}
			System.out.println();
		}		
	}

	
	public void printEdgesSingulars() {
		
		int size = origUnpSymbols.size();
		for (int i = 0; i < 12; i++) {
			for (int m = 0; m < size; m++) {
				for (int n = 0; n < size; n++) {
					SLSet s = getEdges()[i][m][n];
					if (s!=null)
						System.out.print(s.toString());
					else
						System.out.print("+");
					System.out.print(",");
				}
				System.out.println();
			}
			System.out.println();
		}
		
		for (int i = 0; i < 4; i++) {
			for (int m = 0; m < size; m++) {
				SLSet s = this.getSingular(i,m);
				if (s!=null)
					System.out.print(s.toString());
				else
					System.out.print("+");
				System.out.print(",");
			System.out.println();
			}
			System.out.println();
		}
	
	}
	public void printClosure(ZigzagMatrix fw, ZigzagMatrix bw, Vector<String> sym_tab) {
		
		System.out.println("SYMBOL TABLE:");
		System.out.println(sym_tab);
		
		System.out.println("FORWARD:");
		debug(fw, sym_tab);
		System.out.println("BACKWARD:");
		debug(bw, sym_tab);
		
		//The transitive closure is printed
		for (int i = 0; i < 12; i++) {
			
			if (i == PRIMED_UNP_PLUS || i == PRIMED_UNP_MINUS)
				continue;
			
			for (int m = 0; m < origUnpSymbols.size(); m++)
				for (int n = 0; n < origUnpSymbols.size(); n++)
				{
					
					if (isSymetric(i) && m>n)
						continue;
					
					if (this.getEdges()[i][m][n]!= null && 
						!this.getEdges()[i][m][n].empty())
					{
						
						int j = i / 4;
						
						Variable v1 = VariablePool.createSpecial(this.origUnpSymbols.get(m));
						Variable v2 = VariablePool.createSpecial(this.origUnpSymbols.get(n));
						
						if (i % 4 == 2 || i % 4 == 3) // prime
							v1 = v1.getCounterpart();
						
						if (i % 4 == 1 || i % 4 == 3)
							v2 = v2.getCounterpart();
						
						LinearTerm first = new LinearTerm(v1,1);
						LinearTerm second = new LinearTerm(v2,1);
						if (j == 0 || j == 2) {
							second = second.times(-1);
						}
						if (j == 2) {
							first = first.times(-1);
						}
						
						System.out.print(first.toSB(true));
						System.out.print(second.toSB(false));
						
						System.out.print("<=");
						System.out.println(this.getEdges()[i][m][n]);
						System.out.println();
					}
				}
		}
		
		for (int i = 0; i < 4; i++)
			for (int m = 0; m < origUnpSymbols.size(); m++)
				{
					
					SLSet sls = this.getSingular(i,m);
					if (sls!= null && 
						!sls.empty())
					{
						
						Variable v1 = VariablePool.createSpecial(this.origUnpSymbols.get(m));
						if (i % 2 == 1)
							v1 = v1.getCounterpart();
						
						LinearTerm first = new LinearTerm(v1,1);
						
						if (i >= 2) {
							first = first.times(-1);
						}
						
						System.out.print(first.toSB(true));
						
						System.out.print("<=");
						System.out.println(sls);
						System.out.println();						
					}
				}	
		
		if (this.maxSteps>=0) {
			System.out.println("Maxsteps: "+maxSteps);
		}
	}

}