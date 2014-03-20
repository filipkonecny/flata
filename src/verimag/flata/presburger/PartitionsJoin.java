package verimag.flata.presburger;

import java.util.*;

import verimag.flata.automata.g.*;

// Class for computing the join of two partitions (partition refinement lattice).
public class PartitionsJoin {

	// Objects contain list of (indices of) partition classes of both partitions that need to be joined, 
	// and variables corresponding to this partition.
	public static class PartitionsJoinElem {
		// partition indices (indices of p1 and p2 are not disjoint !!)
		private List<Integer> p1 = new LinkedList<Integer>(); // classes of the first partition that need to be joined
		private List<Integer> p2 = new LinkedList<Integer>(); // classes of the first partition that need to be joined
		
		public List<Integer> partitions1() { return p1; }
		public List<Integer> partitions2() { return p2; }
		
		public String toString() {
			return "["+p1+p2+"]";
		}
		
		public PartitionsJoinElem(List<Integer> aP1, List<Integer> aP2) {
			p1 = aP1;
			p2 = aP2;
		}
	}
	
	private List<PartitionsJoinElem> l = new LinkedList<PartitionsJoinElem>();
	public List<PartitionsJoinElem> elements() { return l; }
	
	public String toString() {
		return l.toString();
	}
	
	public static long TIME = 0;
	// assumption: variables in vars1 and vars2 are ordered
	public static PartitionsJoin joinParitions_old(Variable[] vars1, int[] vTp1, int n1, Variable[] vars2, int[] vTp2, int n2) {
		
		PartitionsJoin ret = new PartitionsJoin();
		
		int n = n1 + n2;
		
		// incidence matrix of a bipartite graph with
		//    vertices: partitions of the 2 relations
		//    (symmetric) edges: from partition p to partition q iff there is a
		//      common variable v which is in p (in relation1) and in q (in relation2)
		boolean[][] m = new boolean[n][n];
		
		{
		// compute only with unprimed
		int l1 = vTp1.length / 2;
		int l2 = vTp2.length / 2;
		int i1 = 0, i2 = 0;
		while (i1 < l1 && i2 < l2) {
			
			int cmp = vars1[i1].compareTo(vars2[i2]);
			
			if (cmp == 0) {
				int p1 = vTp1[i1];
				int p2 = vTp2[i2] + n1;
				m[p1][p2] = true; // symmetric
				m[p2][p1] = true;
				
				i1++; i2++;
			} else if (cmp < 0) {
				i1++;
			} else {
				i2++;
			}
		}
		}
		
		long start = System.currentTimeMillis();
		// Floyd-Warshall
		for (int kk=0; kk<n; kk++) {
			for (int ii=0; ii<n; ii++) {
				for (int jj=0; jj<n; jj++) {
					m[ii][jj] = m[ii][jj] || (m[ii][kk] && m[kk][jj]);
				}
			}
		}
		TIME += (System.currentTimeMillis() - start);
		
		boolean[] c = new boolean[n]; // skip partitions that have been processed
		
		// find out which partitions have to be joined
		for (int ii=0; ii<n; ii++) {
			
			if (c[ii])
				continue;
			
			List<Integer> p1 = new LinkedList<Integer>();
			List<Integer> p2 = new LinkedList<Integer>();
			
			if (ii < n1) {
				p1.add(ii);
			} else {
				p2.add(ii-n1);
			}
			c[ii] = true;
			
			// incorrect
			//for (int jj=ii+1; jj<n; jj++) { // symmetry --> ignore lower triangle
			for (int jj=0; jj<n; jj++) {
				if (jj == ii) {
					continue;
				}
				
				if (m[ii][jj]) {
					if (jj < n1) {
						p1.add(jj);
					} else {
						p2.add(jj-n1);
					}
					c[jj] = true;
				}
			}
			
			ret.l.add(new PartitionsJoinElem(p1,p2));
		}
		
		return ret;
	}

	public static PartitionsJoin joinParitions(Variable[] vars1, int[] vTp1, int n1, Variable[] vars2, int[] vTp2, int n2) {
		
		PartitionsJoin ret = new PartitionsJoin();
		
		int n = n1 + n2;
		
		// incidence matrix of a bipartite graph with
		//    vertices: partitions of the 2 relations
		//    (symmetric) edges: from partition p to partition q iff there is a
		//      common variable v which is in p (in relation1) and in q (in relation2)
		//boolean[][] m = new boolean[n][n];
		GGraph g = new GGraph();
		
		for (int i=0; i<n; i++) {
			g.addNode_newInst(i);
		}
		
		{
		// compute only with unprimed
		int l1 = vTp1.length / 2;
		int l2 = vTp2.length / 2;
		int i1 = 0, i2 = 0;
		while (i1 < l1 && i2 < l2) {
			
			int cmp = vars1[i1].compareTo(vars2[i2]);
			
			if (cmp == 0) {
				int p1 = vTp1[i1];
				int p2 = vTp2[i2] + n1;
				g.addEge(p1, p2);
				g.addEge(p2, p1); // symmetric
				i1++; i2++;
			} else if (cmp < 0) {
				i1++;
			} else {
				i2++;
			}
		}
		}
		
		long start = System.currentTimeMillis();
		List<List<Integer>> c = g.origComponents();
		TIME += (System.currentTimeMillis() - start);
		
		// find out which partitions have to be joined
		int check = 0;
		for (List<Integer> l : c) {
			check += l.size();
			List<Integer> p1 = new LinkedList<Integer>();
			List<Integer> p2 = new LinkedList<Integer>();
			
			for (Integer i : l) {
				if (i < n1) {
					p1.add(i);
				} else {
					p2.add(i-n1);
				}
			}
			
			ret.l.add(new PartitionsJoinElem(p1,p2));
		}
		
		if (check != n) {
			throw new RuntimeException("internal error (size,expected size)=("+check+","+n+")");
		}
		
		return ret;
	}

}
