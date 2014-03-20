package verimag.flata.recur_bounded;

import java.util.*;

import nts.interf.*;
import nts.parser.*;

public class SccFinder {

	private INTS nts;
	public List<Subsystem> trivial;
	public List<List<Subsystem>> nontrivial;
	
	public static boolean isRecursive(INTS aNTS) {
		SccFinder sf = new SccFinder(aNTS);
		sf.run();
		return sf.nontrivial.size() > 0;
	}
	
	public SccFinder(INTS aNts) {
		nts = aNts;
	}
	
	public void runOneScc() {
		trivial = new LinkedList<Subsystem>();
		nontrivial = new LinkedList<List<Subsystem>>();
		
		List<Subsystem> scc = new LinkedList<Subsystem>();
		nontrivial.add(scc);
		for (ISubsystem is : nts.subsystems()) {
			scc.add((Subsystem)is);
		}
	}
	
	public void run() {
	
		int n = nts.subsystems().size();
		Map<Subsystem,Integer> s2i = new HashMap<Subsystem,Integer>();
		Subsystem[] i2s = new Subsystem[n];
		List<Integer> i_trivial = new LinkedList<Integer>();
		List<List<Integer>> i_nontrivial = new LinkedList<List<Integer>>();
		
		{
			int i = 0;
			for (ISubsystem is : nts.subsystems()) {
				Subsystem s = (Subsystem) is;
				i2s[i] = s;
				s2i.put(s, i++);
			}
		}
		
		boolean m[][] = new boolean[n][n];
		for (ISubsystem is : nts.subsystems()) {
			Subsystem s = (Subsystem) is;
			for (ITransition t : s.transitions()) {
				if (t.label() instanceof ICall) {
					ICall c = (ICall) t.label();
					Integer from = s2i.get(s);
					Integer to = s2i.get(c.callee());
					m[from][to] = true;
				}
			}
		}
		// Floyd-Warshall
		for (int k=0; k<n; k++) {
			for (int i=0; i<n; i++) {
				for (int j=0; j<n; j++) {
					m[i][j] = m[i][j] || (m[i][k] && m[k][j]);
				}
			}
		}
		
		BitSet bs = new BitSet();
		{
			int i = 0;
			while ( (i = bs.nextClearBit(i)) < n) {
				bs.set(i);
				if (!m[i][i]) {
					i_trivial.add(i);
				} else {
					List<Integer> scc = new LinkedList<Integer>();
					i_nontrivial.add(scc);
					scc.add(i);
					for (int j=i+1; j<n; j++) {
						if (m[i][j] && m[j][i]) {
							scc.add(j);
							bs.set(j);
						}
					}
				}
				i++;
			}
		}
		
		
		trivial = new LinkedList<Subsystem>();
		for (Integer i : i_trivial) {
			trivial.add(i2s[i]);
		}
		
		nontrivial = new LinkedList<List<Subsystem>>();
		for (List<Integer> i_scc : i_nontrivial) {
			List<Subsystem> scc = new LinkedList<Subsystem>();
			for (Integer i : i_scc) {
				scc.add(i2s[i]);
			}
			nontrivial.add(scc);
		}
		
	}
}
