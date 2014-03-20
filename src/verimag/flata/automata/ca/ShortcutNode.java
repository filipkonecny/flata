package verimag.flata.automata.ca;

import java.util.*;

public class ShortcutNode {

	Collection<ReduceInfo> in;
	Collection<ReduceInfo> closures;
	Collection<ReduceInfo> out;
	
	public ShortcutNode(
			Collection<ReduceInfo> aloops,
			Collection<ReduceInfo> aclosures,
			Collection<ReduceInfo> acomputed) {
		
		in = new LinkedList<ReduceInfo>(aloops);
		closures = new LinkedList<ReduceInfo>(aclosures);
		out = new LinkedList<ReduceInfo>(acomputed);
		
		for (ReduceInfo r : aloops) {
			r.shortcut_up(this);
		}
		for (ReduceInfo r : acomputed) {
			r.shortcut_down(this);
		}
	}
	
	public void destroyBindings() {
		for (ReduceInfo ri : in) {
			ri.shortcut_up(null);
		}
		for (ReduceInfo ri : out) {
			ri.shortcut_down(null);
		}
	}
}
