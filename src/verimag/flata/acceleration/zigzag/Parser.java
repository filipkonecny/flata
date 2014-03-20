package verimag.flata.acceleration.zigzag; 

import java.util.*;
import java.io.*;
/**
 * 
 * 
 * Parser of the input DBM Relation
 *
 */

public class Parser {

	public Vector<String> sym_tab;

	public Vector<Constraint> constraint;
	
	public int size = -1;
	public Map<String, Integer> remainders = new HashMap<String, Integer>();
	public Map<String, Integer> divs = new HashMap<String, Integer>();
	public int[] remaind = null;
	public int[] div = null;
	
	public static final int FORWARD = 1;
	public static final int BACKWARD= 2;
	
	public static final String ZERO = "$$$zero";

	Parser() {
		sym_tab = new Vector<String>();
		sym_tab.add(ZERO);
		constraint = new Vector<Constraint>();
		constraint.add(new Constraint(0, 0, "0", FORWARD));
		constraint.add(new Constraint(0, 0, "0", BACKWARD));
	}

	public void setSize(int m)
	{
		int i;
		size = m;
		remaind = new int[2*m];
		div = new int[2*m];
		
		for (i = 0; i < 2*m; i++)
			remaind[i] = 0;
		for (i = 0; i < 2*m; i++)
			div[i] = 1;
	}
	public void putRemainders(Map<String, Integer> m)
	{
		remainders = m;
	}
	public void putDivs(Map<String, Integer> m)
	{
		divs = m;
	}
	
	public int[] getRem()
	{
		return remaind;
	}
	public int[] getDiv()
	{
		return div;
	}
	public int getRem(int i)
	{
		if (i < remaind.length && i >= 0)
			return remaind[i];
		else 
			return 0;
	}
	public int getDiv(int i)
	{
		if (i < div.length && i >= 0)
			return div[i];
		else 
			return 1;
	}
	
	int find(String token) {
		int ind = -1;
		for (int i = 0; i < sym_tab.size(); i++) {
			String temp = sym_tab.elementAt(i);
			if (temp.equalsIgnoreCase(token)) {
				ind = i;
				break;
			}
		}
		return ind;
	}

	int findOrAdd(String token) {
		int ind = find(token);
		if (ind == -1) {
			ind = sym_tab.size();
			sym_tab.add(token);
		}
		return ind;
	}

	void correctoradd(Constraint nrel) {
		boolean added = false;
		for (int k = 0; k < constraint.size(); k++) {
			Constraint rel = constraint.elementAt(k);
			if (nrel.row_index() == rel.row_index()
					&& nrel.col_index() == rel.col_index()
					&& nrel.matrix_num() == rel.matrix_num()) {
				if ((Integer.parseInt(rel.mat_ele())) > (Integer.parseInt(nrel
						.mat_ele())))
					rel.set(nrel.mat_ele());
				added = true;
			}
		}
		if (!added) {
			constraint.add(nrel);
		}
	}

	Vector<String> read(Vector<String> br) throws IOException {

		int prim_mat = FORWARD;
		boolean error = false;
		int i = 1, r_ind = -1, c_ind = -1, nr_ind = -1, nc_ind = -1;
		String new_weight = "inf";
		String token1 = null, token2 = null;
		String line = null;
		int lc = 0;
		while (lc < br.size()) {
			line = br.elementAt(lc);
			lc++;
			new_weight = "inf";
			r_ind = -1;
			c_ind = -1;
			nr_ind = -1;
			nc_ind = -1;
			StringTokenizer stp = new StringTokenizer(line, "<= ", false);
			StringTokenizer st = new StringTokenizer(stp.nextToken(), "- ",
					false);
			if (st.countTokens() != 2) {
				error = true;
				break;
			}
			token1 = st.nextToken();
			token2 = st.nextToken();
			String weight = stp.nextToken();
			if (token1.endsWith("'")) {
				token1 = token1.substring(0, token1.length() - 1);
				r_ind = findOrAdd(token1);
				prim_mat = BACKWARD;
				if (token2.endsWith("'")) {
					token2 = token2.substring(0, token2.length() - 1);
					String new_token2 = token2;
					token2 = "_" + i;
					i++;
					new_weight = "0";
					c_ind = findOrAdd(token2);
					nr_ind = c_ind;
					nc_ind = findOrAdd(new_token2);
				} else {
					c_ind = findOrAdd(token2);
				}

			} else {
				r_ind = findOrAdd(token1);
				prim_mat = FORWARD;
				if (token2.endsWith("'")) {
					token2 = token2.substring(0, token2.length() - 1);
					c_ind = findOrAdd(token2);
				} else {
					String new_token2 = token2;
					token2 = "_" + i;
					i++;
					new_weight = "0";
					c_ind = findOrAdd(token2);
					nr_ind = c_ind;
					nc_ind = findOrAdd(new_token2);
				}

			}
			Constraint rel1 = new Constraint(r_ind, c_ind, weight, prim_mat);
			correctoradd(rel1);
			if (new_weight == "0") {
				Constraint rel2 = new Constraint(nr_ind, nc_ind, new_weight,
						3 - prim_mat);
				correctoradd(rel2);
			}

		}

		if (error)
			System.out.println("parse error at line: '" + line + "'");
		return sym_tab;
	}

		//the octagonal relation is read and transformed so as the 
		//resulting constraint graph be bipartite; the relation is 
		//then stored as primed and unprimed DBMs, i.e. DBMs
		//encoding arcs from primed to unprimed and from unprimed to 
		//primed variables respectively;
		//the positive and negative forms for each variable 
		//are saved in the symbol table, with the indices corresponding 
		//to the occurences in the DBM of each such form;
	
		//if the constraints are only potential, there is no need 
		//for positive (plus - "p") and negative (minus - "m")
		//forms, and therefore the variables are stored directly
		//in the symbol table
	
	Vector<String> readOctagonal(Vector<String> br) throws IOException {

		int prim_mat = FORWARD;
		boolean error = false;
		int i = 1, r_ind = -1, c_ind = -1, nr_ind = -1, nc_ind = -1;
		String new_weight = "inf";
		String token1 = null, token2 = null;
		String line = null;
		int lc = 0;
		while (lc < br.size()) {
			line =  br.elementAt(lc);
			lc++;
			new_weight = "inf";
			r_ind = -1;
			c_ind = -1;
			nr_ind = -1;
			nc_ind = -1;
			StringTokenizer stp = new StringTokenizer(line, "<= ", false);
			StringTokenizer st = new StringTokenizer(stp.nextToken(), "- ",
					false);
			if (st.countTokens() != 2) {
				error = true;
				break;
			}
			token1 = st.nextToken();
			token2 = st.nextToken();
			String weight = stp.nextToken();
			if (token1.endsWith("'p")||token1.endsWith("'m")) {
				token1 = token1.substring(0, token1.length() - 2)+
					token1.substring(token1.length() - 1, token1.length());
				r_ind = findOrAdd(token1);
				prim_mat = BACKWARD;
				if (token2.endsWith("'p")||token2.endsWith("'m")) {
					token2 = token2.substring(0, token2.length() - 2)+
						token2.substring(token2.length() - 1, token2.length());
					String new_token2 = token2;
					token2 = "_" + i;
					i++;
					new_weight = "0";
					c_ind = findOrAdd(token2);
					nr_ind = c_ind;
					nc_ind = findOrAdd(new_token2);
				} else {
					c_ind = findOrAdd(token2);
				}

			} else {
				r_ind = findOrAdd(token1);
				prim_mat = FORWARD;
				if (token2.endsWith("'p")||token2.endsWith("'m")) {
					token2 = token2.substring(0, token2.length() - 2)+
						token2.substring(token2.length() - 1, token2.length());
					c_ind = findOrAdd(token2);
				} else {
					String new_token2 = token2;
					token2 = "_" + i;
					i++;
					new_weight = "0";
					c_ind = findOrAdd(token2);
					nr_ind = c_ind;
					nc_ind = findOrAdd(new_token2);
				}

			}
			Constraint rel1 = new Constraint(r_ind, c_ind, weight, prim_mat);
			correctoradd(rel1);
			if (new_weight == "0") {
				Constraint rel2 = new Constraint(nr_ind, nc_ind, new_weight,
						3 - prim_mat);
				correctoradd(rel2);
			}

		}


		if (error)
			System.out.println("parse error at line: '" + line + "'");
		return sym_tab;
	}
	
	
	Vector<String> readModulo(Vector<String> br) throws IOException {

		int prim_mat = FORWARD;
		boolean error = false;
		int i = 1, r_ind = -1, c_ind = -1, nr_ind = -1, nc_ind = -1;
		String new_weight = "inf";
		String token1 = null, token2 = null;
		String line = null;
		int lc = 0;
		while (lc < br.size()) {
			line = br.elementAt(lc);
			lc++;
			new_weight = "inf";
			r_ind = -1;
			c_ind = -1;
			nr_ind = -1;
			nc_ind = -1;
			StringTokenizer stp = new StringTokenizer(line, "<= ", false);
			StringTokenizer st = new StringTokenizer(stp.nextToken(), "- ",
					false);
			if (st.countTokens() != 2) {
				error = true;
				break;
			}
			token1 = st.nextToken();
			token2 = st.nextToken();
			String weight = stp.nextToken();
			if (token1.endsWith("'")) {
				token1 = token1.substring(0, token1.length() - 1);
				r_ind = findOrAdd(token1);
				Integer rem1 = remainders.get(token1);
				Integer dv1 = divs.get(token1);
				remaind[r_ind] = rem1.intValue();
				div[r_ind] = dv1.intValue();
				
				prim_mat = BACKWARD;
				if (token2.endsWith("'")) {
					token2 = token2.substring(0, token2.length() - 1);
					String new_token2 = token2;
					token2 = "_" + i;
					i++;
					new_weight = "0";
					c_ind = findOrAdd(token2);
					remaind[c_ind] = 0;
					div[c_ind] = 1;

					nr_ind = c_ind;
					nc_ind = findOrAdd(new_token2);
					Integer rem2 = remainders.get(new_token2);
					Integer dv2 = divs.get(new_token2);
					remaind[nc_ind] = rem2.intValue();
					div[nc_ind] = dv2.intValue();
				} else {
					c_ind = findOrAdd(token2);
					Integer rem2 = remainders.get(token2);
					Integer dv2 = divs.get(token2);
					remaind[c_ind] = rem2.intValue();
					div[c_ind] = dv2.intValue();
				}

			} else {
				r_ind = findOrAdd(token1);
				Integer rem1 = remainders.get(token1);
				Integer dv1 = divs.get(token1);
				remaind[r_ind] = rem1.intValue();
				div[r_ind] = dv1.intValue();

				prim_mat = FORWARD;
				if (token2.endsWith("'")) {
					token2 = token2.substring(0, token2.length() - 1);
					c_ind = findOrAdd(token2);

					Integer rem2 = remainders.get(token2);
					Integer dv2 = divs.get(token2);
					remaind[c_ind] = rem2.intValue();
					div[c_ind] = dv2.intValue();
				} else {
					String new_token2 = token2;
					token2 = "_" + i;
					i++;
					new_weight = "0";
					c_ind = findOrAdd(token2);
					remaind[c_ind] = 0;
					div[c_ind] = 1;
					nr_ind = c_ind;

					nc_ind = findOrAdd(new_token2);
					Integer rem2 = remainders.get(new_token2);
					Integer dv2 = divs.get(new_token2);
					remaind[nc_ind] = rem2.intValue();
					div[nc_ind] = dv2.intValue();
				}

			}
			Constraint rel1 = new Constraint(r_ind, c_ind, weight, prim_mat);
			correctoradd(rel1);
			if (new_weight == "0") {
				Constraint rel2 = new Constraint(nr_ind, nc_ind, new_weight,
						3 - prim_mat);
				correctoradd(rel2);
			}

		}

		if (error)
			System.out.println("parse error at line: '" + line + "'");
		return sym_tab;
	}
}