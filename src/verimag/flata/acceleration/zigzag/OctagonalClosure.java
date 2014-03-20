package verimag.flata.acceleration.zigzag; 

import java.util.*;

/**
 * 
 * Class used for performing tight closure and octagonal 
 * consistency check on a (shortest-paths) closed DBM, 
 * representing the transitive closure of a difference-bound 
 * relation
 */

public class OctagonalClosure {
		//the maximum number of steps in which the relation becomes inconsistent
	private int maxSteps = -1;
		//flag telling whether the relation is octagonal or not
	private boolean octagonal = false;
		//symbol table - to be received from the parent program
	private Vector<String> sym_tab = null; 

	private Graph g = null;

	//DBMs for constraints between primed and unprimed sets of variables
	private SLSet[][] unpUnprimed = null;
	private SLSet[][] pUnprimed = null;
	private SLSet[][] unpPrimed = null;
	private SLSet[][] pPrimed = null;
	
	private int[] barIndexPrimed;
	private int[] barIndexUnprimed;

	private long startTime;
	private long endComputeSets;
	private long endOctagonCheck;
	private long endTime;
	
	public static final int UNP_UNP = 0;
	public static final int UNP_P = 1;
	public static final int P_UNP = 2;
	public static final int P_P = 3;
	
	/**
	 * The relation is not reduced; if it is redundant it will be fed to 
	 * the parser as it is 
	 */
	public static final int LOW = 0;
	/**
	 * The relation is reduced with minimum loss of information
	 */
	public static final int MEDIUM = 1;
	/**
	 * The relation is maximally reduced, but loss of information
	 * might occur if there are zero weight cycles 
	 */
	public static final int HIGH = 2;
	/**
	 * The transitive closure is checked for consistency;
	 * high time complexity (O(n^n))
	 */
	public static final boolean CHECK = true;
	/**
	 * The transitive closure is not checked for consistency;
	 * the computation is much faster
	 */
	public static final boolean NOCHECK = false;
	
	
	public OctagonalClosure(Vector<String> symbols, Graph graph, boolean octag)
	{
		sym_tab = symbols;
		
		unpUnprimed = new SLSet[sym_tab.size()][sym_tab.size()];
		pUnprimed = new SLSet[sym_tab.size()][sym_tab.size()];
		unpPrimed = new SLSet[sym_tab.size()][sym_tab.size()];
		pPrimed = new SLSet[sym_tab.size()][sym_tab.size()];

		g = graph;
		
		octagonal = octag;
	}
	
	private void oddForward(Graph g) {
		for (int m = 0; m < sym_tab.size(); m ++) 							
			g.oddForw(m, this);
	}
		
	private void oddBackward(Graph g) {
		for (int m = 0; m < sym_tab.size(); m ++)				
			g.oddBack(m, this);
	}
	
	private void evenForward(Graph g) {
		for (int m = 0; m < sym_tab.size(); m ++)		
			for (int n = 0; n < sym_tab.size(); n ++) {
				if (n != m) {				
					g.evenForw(m, n, this);

					if (!g.evenForwEnd.m_X.empty()) {
						String x = sym_tab.elementAt(m);
						String y = sym_tab.elementAt(n);
						
						if (octagonal && 
							x.substring(0, x.length()-1).equalsIgnoreCase(y.substring(0, y.length() - 1))) {
							g.evenForwEnd.m_X.tighten();
							barIndexUnprimed[n] = m;
							barIndexUnprimed[m] = n;
						}						
					}
				}
				
			}
	}
	
	private void evenBackward(Graph g) {		
		for (int m = 0; m < sym_tab.size(); m ++) {
			for (int n = 0; n < sym_tab.size(); n ++) {
				if (n != m) {
					g.evenBack(m, n, this);
					
					if (!g.evenBackEnd[m][n].m_X.empty()) {
						String x =  sym_tab.elementAt(m);
						String y =  sym_tab.elementAt(n);
								
						if (octagonal && 
							x.substring(0, x.length()-1).equalsIgnoreCase(y.substring(0, y.length()-1))) {
							g.evenBackEnd[m][n].m_X.tighten();
							barIndexPrimed[n] = m;
							barIndexPrimed[m] = n;
						}
					}
				}
			}
		}
	}
	
		//normalization step (effective tight closure and consistency check)
	public void normalize(boolean flag)
	{
			//here the DBMs are filled with the shortest paths between any 
			//two nodes; the shortest paths are assumed already computed in Graph g
		if (sym_tab != null && g != null && unpUnprimed != null &&
			pUnprimed != null && unpPrimed != null && pPrimed != null)
		{
				//if i is an index of a variable in the symbol table, then 
				//i(bar) is assumed to be the index of the same variable, but with 
				//opposite sign; the following two arrays store the corresponding 
				//i(bar) for every i, in both primed and unprimed sets
			barIndexPrimed = new int[sym_tab.size()];
			barIndexUnprimed = new int[sym_tab.size()];

				//initialization step
			for (int m = 0; m < sym_tab.size(); m++) 
				for (int n = 0; n < sym_tab.size(); n++) 
				{
					unpUnprimed[m][n] = null;
					unpPrimed[m][n] = null;
					pUnprimed[m][n] = null;
					pPrimed[m][n] = null;
				}

			for (int m = 0; m < sym_tab.size(); m++) 
			{
				barIndexUnprimed[m] = -1;
				barIndexPrimed[m] = -1;
			}

				//in the following 4 similar loops, corresponding to 
				//forward and backward even and odd automata runs for Graph g, 
				//the shortest paths between x and x', x' and x, x and x and 
				//x' and x' respectively are computed and stored in the corresponding 
				//matrices (where x is the set of variables involved, stored in symb_tab);
			
				//in this step, the constraints between opposite sign forms 
				//of the same variable are tightened (replaced with the greatest 
				//even integer less than the constraint)
			
			startTime = System.currentTimeMillis();
			
			g.initRootNodes();
			
			oddForward(g);
			oddBackward(g);
			evenForward(g);
			evenBackward(g);
									
			endComputeSets = System.currentTimeMillis();
			
			//testing for negative weight cycles
			maxSteps = -1;
			
			for (int i = 0; i < sym_tab.size(); i++) {
				//check for the diagonals in primed-primed
				//and unprimed-unprimed matrices
				if (pPrimed[i][i] != null)
				{
					int crtPP = pPrimed[i][i].maxPosSteps();
					if (maxSteps == -1 || (crtPP >= 0 && maxSteps > crtPP))
						maxSteps = crtPP;
				}
				if (unpUnprimed[i][i] != null)
				{
					int crtUU = unpUnprimed[i][i].maxPosSteps();
					if (maxSteps == -1 || (crtUU >= 0 && maxSteps > crtUU))
						maxSteps = crtUU;
				}
			}
			
			boolean check = (flag) ? octagonal : false;
			
			//testing for m_{i,i(bar)}+m_{i(bar),i}<0,
			//for any (intermediate also!) i, after tightening...
			if (check)
			{
				for (int index = 0; index < sym_tab.size(); index++)
				{
					String value = sym_tab.get(index);
					int barIndex = -1;
					for (int i = 0; i < sym_tab.size(); i++)
					{
						String barred = sym_tab.get(i);
						if (barred.substring(0, barred.length()-1).equalsIgnoreCase(
								value.substring(0, value.length()-1)) &&
								!barred.equalsIgnoreCase(value))
						{
							barIndex = i;
							break;
						}
					}
					if (barIndex < 0)
						continue;
						//find all sizes; all subsets of that size; all orderings; all choices of sense
						//for  both m_{i\bar{i}} and m_{\bar{i}i}
					for (int size = 0; size < sym_tab.size()-1; size++)
					{
							//ordered sets of variables of size 'size'
						int[] chosen = new int[size];
							//chosen indices in one step
						int[] mark = new int[sym_tab.size()]; 
						for (int i = 0; i < sym_tab.size(); i++)
							mark[i] = 0;
							//the ends of the path are fixed
						mark[index] = 1;
						mark[barIndex] = 1;
						
						int k = 0;
						if (size > 0)
							chosen[k] = -1;
						while (k >= 0)
						{
							while (size > 0 && chosen[k]+1 < sym_tab.size() && k < size && mark[chosen[k]+1] == 1)
								chosen[k]++;
							
							if (size == 0 || (size > 0 && chosen[k]+1 < sym_tab.size() && k < size && mark[chosen[k]+1] == 0))
							{
								if (size > 0)
									chosen[k]++;
								else
									k--;
								if (k < size - 1)
								{
									mark[chosen[k]] = 1;
									chosen[++k] = -1;
								}
								else 
								{						
										//choice of sense; repeat for second term; check for condition
									int[] sense = new int[size+1];
									int p = 0;
									sense[p] = -3;
									while (p >= 0)
									{
										if (sense[p]+2 < 2)
										{
											sense[p]+=2;
											if (p < size)
												sense[++p] = -3;
											else 
											{									
													//repeat for second term: choice of size, set and ordering
												for (int size2 = 0; size2 < sym_tab.size()-1; size2++)
												{
													int chosen2[] = new int[size2];
													int[] mark2 = new int[sym_tab.size()];
													for (int i = 0; i < sym_tab.size(); i++)
														mark2[i] = 0;
													mark2[index] = 1;
													mark2[barIndex] = 1;
													
													int k2 = 0;
													if (size2 > 0)
														chosen2[k2] = -1;
													while (k2 >= 0)
													{
														while (size2 > 0 && chosen2[k2]+1 < sym_tab.size() && k2 < size2 && mark2[chosen2[k2]+1] == 1)
															chosen2[k2]++;
														
														if (size2 == 0 || (size2 > 0 && chosen2[k2]+1 < sym_tab.size() && k2 < size2 && mark2[chosen2[k2]+1] == 0))
														{
															if (size2 > 0)
																chosen2[k2]++;
															else
																k2--;
															if (k2 < size2 - 1)
															{
																mark2[chosen2[k2]] = 1;
																chosen2[++k2] = -1;
															}
															else 
															{
																	//choice of sense; check for condition
																int[] sense2 = new int[size2+1];
																int p2 = 0;
																sense2[p2] = -3;
																while (p2 >= 0)
																{
																	if (sense2[p2]+2 < 2)
																	{
																		sense2[p2]+=2;
																		if (p2 < size2)
																			sense2[++p2] = -3;
																		else 
																		{
																				//check for condition: if one term is true,
																				//then the whole disjunction is true and thus
																				//the relation is inconsistent
																			
																			Vector<SLSet> sumTerms1 = new Vector<SLSet>();
																			Vector<SLSet> sumTerms2 = new Vector<SLSet>();
																			
																				//the corresponding parts of path, in either sense, 
																				//for both terms, are stored in the two vectors above
																			if (size > 0)
																			{
																				if (sense[0] > 0)
																				{
																					if (unpUnprimed[index][chosen[0]] != null)
																						sumTerms1.add(unpUnprimed[index][chosen[0]].copy());
																				}
																				else
																					if (pPrimed[index][chosen[0]] != null)
																						sumTerms1.add(pPrimed[index][chosen[0]].copy());
	
																				for (int i = 0; i < size-1; i++)
																				{
																					if (sense[i+1] > 0)
																					{
																						if (unpUnprimed[chosen[i]][chosen[i+1]] != null)
																							sumTerms1.add(unpUnprimed[chosen[i]][chosen[i+1]].copy());
																					}
																					else
																						if (pPrimed[chosen[i]][chosen[i+1]] != null)
																							sumTerms1.add(pPrimed[chosen[i]][chosen[i+1]].copy());
																				}
	
																				if (sense[size] > 0)
																				{
																					if (unpUnprimed[chosen[size-1]][barIndex] != null)
																						sumTerms1.add(unpUnprimed[chosen[size-1]][barIndex].copy());
																				}
																				else
																					if (pPrimed[chosen[size-1]][barIndex] != null)
																						sumTerms1.add(pPrimed[chosen[size-1]][barIndex].copy());
																			}
																			else 
																			{
																				if (sense[0] > 0)
																				{
																					if (unpUnprimed[index][barIndex] != null)
																						sumTerms1.add(unpUnprimed[index][barIndex].copy());
																				}
																				else
																					if (pPrimed[index][barIndex] != null)
																						sumTerms1.add(pPrimed[index][barIndex].copy());
																			}
																			
																			if (size2 > 0)
																			{
																				if (sense2[0] > 0)
																				{
																					if (unpUnprimed[barIndex][chosen2[0]] != null)
																						sumTerms2.add(unpUnprimed[barIndex][chosen2[0]].copy());
																				}
																				else
																					if (pPrimed[barIndex][chosen2[0]] != null)
																						sumTerms2.add(pPrimed[barIndex][chosen2[0]].copy());
	
																				for (int i = 0; i < size2-1; i++)
																				{
																					if (sense2[i+1] > 0)
																					{
																						if (unpUnprimed[chosen2[i]][chosen2[i+1]] != null)
																							sumTerms2.add(unpUnprimed[chosen2[i]][chosen2[i+1]].copy());
																					}
																					else
																						if (pPrimed[chosen2[i]][chosen2[i+1]] != null)
																							sumTerms2.add(pPrimed[chosen2[i]][chosen2[i+1]].copy());
																				}
	
																				if (sense2[size2] > 0)
																				{
																					if (unpUnprimed[chosen2[size2-1]][index] != null)
																						sumTerms2.add(unpUnprimed[chosen2[size2-1]][index].copy());
																				}
																				else
																					if (pPrimed[chosen2[size2-1]][index] != null)
																						sumTerms2.add(pPrimed[chosen2[size2-1]][index].copy());
																			}
																			else 
																			{
																				if (sense2[0] > 0)
																				{
																					if (unpUnprimed[barIndex][index] != null)
																						sumTerms2.add(unpUnprimed[barIndex][index].copy());
																				}
																				else
																					if (pPrimed[barIndex][index] != null)
																						sumTerms2.add(pPrimed[barIndex][index].copy());
																			}

																			//consistency check for the two vectors of terms and the two choices of sense
																			
																			/* ---- temporarily suspended ----
																			int steps = SLSet.inconsistentInNSteps(sumTerms1, sumTerms2, sense, sense2);
																			if (steps > 0)
																			{
																				System.out.println("The relation is inconsistent in "+steps+" steps");
																				System.exit(1);
																			}
																			else if (steps == 0)
																			{
																				System.out.println("The relation is inconsistent");
																				System.exit(1);
																			}
																			-------- */
	
																		}
																	}
																	else
																		p2--;
																}
															}
														}
														else if (k2 >= 0)
														{
															k2--;
															if (k2 >= 0)
																mark2[chosen2[k2]] = 0;
														}
													}
												}
		 
											}
										}
										else
											p--;
									}
								}
							}
							else if (k >= 0)
							{
								k--;
								if (k >= 0)
									mark[chosen[k]] = 0;
							}
						}
					}
				}
				
			}
			
			endOctagonCheck = System.currentTimeMillis();
			
			/*			
			for (int i = 0; i < sym_tab.size(); i++)
				for (int j = 0; j < sym_tab.size(); j++)
				{
					System.out.print(sym_tab.get(i)+"-"+sym_tab.get(j)+"<=");
					if (unpUnprimed[i][j] != null)
						unpUnprimed[i][j].print();
					System.out.println();

					System.out.print(sym_tab.get(i)+"'-"+sym_tab.get(j)+"<=");
					if (pUnprimed[i][j] != null)
						pUnprimed[i][j].print();
					System.out.println();

					System.out.print(sym_tab.get(i)+"-"+sym_tab.get(j)+"'<=");
					if (unpPrimed[i][j] != null)
						unpPrimed[i][j].print();
					System.out.println();

					System.out.print(sym_tab.get(i)+"'-"+sym_tab.get(j)+"'<=");
					if (pPrimed[i][j] != null)
						pPrimed[i][j].print();
					System.out.println();
				}
			*/

				//strong closure step: as best paths, constraint sum 
				//m_{i,i(bar)}/2+m_{j(bar),j}/2 are also accounted for
			for (int i = 0; i < sym_tab.size(); i++)
				for (int j = 0; j < sym_tab.size(); j++)	
				{
					// SLSet closed;

					if (barIndexUnprimed[i] >= 0 && barIndexUnprimed[j] >= 0 
						&& unpUnprimed[i][barIndexUnprimed[i]] != null &&
						unpUnprimed[barIndexUnprimed[j]][j] != null &&
						unpUnprimed[i][j] != null)
					{
						unpUnprimed[i][j].close(unpUnprimed[i][barIndexUnprimed[i]], 
												unpUnprimed[barIndexUnprimed[j]][j]);
						
						/*
						closed = SLSet.close(unpUnprimed[i][barIndexUnprimed[i]],
						unpUnprimed[barIndexUnprimed[j]][j], unpUnprimed[i][j]);
						unpUnprimed[i][j] = closed.copy();
						*/
					}
					if (barIndexPrimed[i] >= 0 && barIndexUnprimed[j] >= 0 
							&& pPrimed[i][barIndexPrimed[i]] != null &&
							unpUnprimed[barIndexUnprimed[j]][j] != null &&
							pUnprimed[i][j] != null)
						{
							pUnprimed[i][j].close(pPrimed[i][barIndexPrimed[i]],
												  unpUnprimed[barIndexUnprimed[j]][j]);
						
							/*
							closed = SLSet.close(pPrimed[i][barIndexPrimed[i]],
								unpUnprimed[barIndexUnprimed[j]][j], pUnprimed[i][j]);
							pUnprimed[i][j] = closed.copy();
							*/
						}

					if (barIndexUnprimed[i] >= 0 && barIndexPrimed[j] >= 0 
							&& unpUnprimed[i][barIndexUnprimed[i]] != null &&
							pPrimed[barIndexPrimed[j]][j] != null &&
							unpPrimed[i][j] != null)
						{
							unpPrimed[i][j].close(unpUnprimed[i][barIndexUnprimed[i]],
												  pPrimed[barIndexPrimed[j]][j]);
						
						/*
							closed = SLSet.close(unpUnprimed[i][barIndexUnprimed[i]],
								pPrimed[barIndexPrimed[j]][j], unpPrimed[i][j]);
							unpPrimed[i][j] = closed.copy();
						*/
						}
					if (barIndexPrimed[i] >= 0 && barIndexPrimed[j] >= 0 
							&& pPrimed[i][barIndexPrimed[i]] != null &&
							pPrimed[barIndexPrimed[j]][j] != null &&
							pPrimed[i][j] != null)
						{
							pPrimed[i][j].close(pPrimed[i][barIndexPrimed[i]],
												pPrimed[barIndexPrimed[j]][j]);
						
						/*
							closed = SLSet.close(pPrimed[i][barIndexPrimed[i]],
									pPrimed[barIndexPrimed[j]][j], pPrimed[i][j]);
							pPrimed[i][j] = closed.copy();
						*/
						}
				}
		}
		
		endTime = System.currentTimeMillis();
	}
	
	
	public long buildTime() { return endComputeSets - startTime; } 
	public long checkTime() { return endOctagonCheck - endComputeSets; }
	public long totalTime() { return endTime - startTime; }
	
	// setters and getters for each of the four DBMs 
	// (for pairs of primed and unprimed variables)
	public void setPrimedPrimed(int i, int j, SLSet X) {
		if (!X.empty())
			pPrimed[i][j] = X.copy(); 
	}
	
	public SLSet getPrimedPrimed(int i, int j) {
		if (i >= 0 && i < sym_tab.size() && j >= 0 && j < sym_tab.size())
			return pPrimed[i][j];
		
		return null;
	}

	public void setUnprimedPrimed(int i, int j, SLSet X) {
		if (!X.empty())
			unpPrimed[i][j] = X.copy(); 
	}
	
	public SLSet getUnprimedPrimed(int i, int j) {
		if (i >= 0 && i < sym_tab.size() && j >= 0 && j < sym_tab.size())
			return unpPrimed[i][j];
		
		return null;
	}

	public void setPrimedUnprimed(int i, int j, SLSet X) {
		if (!X.empty())
			pUnprimed[i][j] = X.copy(); 
	}
	
	public SLSet getPrimedUnprimed(int i, int j) {
		if (i >= 0 && i < sym_tab.size() && j >= 0 && j < sym_tab.size())
			return pUnprimed[i][j];
		
		return null;
	}

	public void setUnprimedUnprimed(int i, int j, SLSet X) {
		if (!X.empty())
			unpUnprimed[i][j] = X.copy();
	}
	
	public SLSet getUnprimedUnprimed(int i, int j) {
		if (i >= 0 && i < sym_tab.size() && j >= 0 && j < sym_tab.size())
			return unpUnprimed[i][j];
		
		return null;
	}
	
	public SLSet get(int i, int j, int type) {
		switch (type) {
		
		case UNP_UNP: return getUnprimedUnprimed(i,j);
		
		case UNP_P: return getUnprimedPrimed(i,j);
		
		case P_UNP: return getPrimedUnprimed(i,j);
		
		case P_P: return getPrimedPrimed(i,j);
		
		default: throw new RuntimeException("unexpected parameter");
		}
	}
	
	public Vector<String> getSymbolTable() { return sym_tab; }
	
	public boolean isOctagonal() { return octagonal; }
	
	public Vector<String> extractOriginals(Vector<String> symbols)
	{
		symbols = new Vector<String>();

		for (int i = 0; i < sym_tab.size(); i++)
		{
			String crt = sym_tab.get(i);
			if (octagonal && (crt.endsWith("m")||crt.endsWith("p")))
			{
				String original = crt.substring(0, crt.length()-1);
				if (!symbols.contains(original))
					symbols.add(original);
			}
			if ((!octagonal) && (!crt.startsWith("_")))
				if (!symbols.contains(crt))
					symbols.add(crt);
		}
		
		return symbols;
			
	}
	
	public int getMaxPositiveSteps() { return maxSteps; }
}
