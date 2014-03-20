package verimag.flata.automata.ca;

import java.util.*; 

public class BinRel<T> {

	public static class Pair<T> {
		T o1;
		T o2;
		
		public Pair(T ao1, T ao2) {
			o1 = ao1;
			o2 = ao2;
		}
	}
	
	private List<Pair<T>> rel = new LinkedList<Pair<T>>();
	
	public void add(Pair<T> p) {
		rel.add(p);
	}
	public void add(T o1, T o2) {
		add(new Pair<T>(o1, o2));
	}
	
	public Set<T> dom() {
		Set<T> ret = new HashSet<T>();
		for (Pair<T> p : rel) {
			ret.add(p.o1);
			ret.add(p.o2);
		}
		return ret;
	}
	private Map<T,Integer> backLink(List<T> list) {
		Map<T,Integer> ret = new HashMap<T,Integer>();
		int i = 0;
		for (T o : list)
			ret.put(o, i ++);
		return ret;
	}
	
	private boolean[][] m;
	private List<T> link;
	private Map<T,Integer> backlink;
	public void transitiveClosure() {
		int l = rel.size();
		m = new boolean[l][l];
		link = new LinkedList<T>(dom());
		backlink = backLink(link);
		for (Pair<T> p : rel) {
			m[backlink.get(p.o1).intValue()][backlink.get(p.o2).intValue()] = true;
		}
		for (int k=0; k<l; k++)
			for (int i=0; i<l; i++)
				for (int j=0; j<l; j++) {
					m[i][j] = m[i][j] || (m[i][k] && m[k][j]);
				}
	}
	public Set<T> reachPlus(T from) {
		Collection<T> tmp = new LinkedList<T>();
		tmp.add(from);
		return reachPlus(tmp);
	}
	public Set<T> reachPlus(Collection<T> from) {
		Set<T> ret = new HashSet<T>();
		
		transitiveClosure();
		
		for (T s : from) {
			int inx = backlink.get(s).intValue();
			for (int i = 0; i < m.length; i++) {
				if (i == inx)
					continue; // Reach+, not Reach*
				if (m[inx][i])
					ret.add(link.get(i));
			}
		}
		
		return ret;
	}
	
	public Map<T, T> getReach2RootMap(Collection<T> subset) {
		Map<T, T> ret = new HashMap<T, T>();
		
		transitiveClosure();
		
		for (T s : subset) {
			int inx = backlink.get(s).intValue();
			boolean isRoot = true;
			for (T s2 : subset) {
				if (s.equals(s2))
					continue;
				else {
					int inx2 = backlink.get(s2).intValue();
					if (m[inx2][inx]) {
						isRoot = false;
						break;
					}	
				}
			}
			if (isRoot) {
				for (T s2 : subset) {
					int inx2 = backlink.get(s2).intValue();
					if (m[inx][inx2]) {
						ret.put(s2, s);
					}
				}
			}
		}
		
		return ret;
	}
}
