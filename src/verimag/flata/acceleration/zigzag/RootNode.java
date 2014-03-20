package verimag.flata.acceleration.zigzag;

import java.util.*;

public class RootNode extends Node {
	private boolean _start;
	private boolean _odd;
	private boolean _forw;
	private int index1;
	private int index2;

	private int[] fixIndex;
	private int[] fixValue;
	private int[] variables;
		
	public RootNode(int i, int j, int size) {
		super(size);
		
		index1 = i;
		index2 = j;
	}
		
	public void setSCCEntry() {
		if (_start)
			scc_node.addEntry(this);
		else
			super.setSCCEntry();
	}
	
	public void setSCCExit() {
		if (!_start)
			scc_node.addExit(this);
		else
			super.setSCCExit();
	}
	
	public void initRootNode(boolean odd, boolean forw, boolean start) {		
		_start = start;
		_odd = odd;
		_forw = forw;
		
		if (odd) { 
			fixIndex = new int[1];
			fixValue = new int[1];
			variables = new int[2];

			fixIndex[0] = index1;
			variables[0] = Node.bottom;

			if (forw) { 
				// odd forw
				// start nodes : q_i = r and q_h \in {bot,lr}
				// end node : q_j = r and q_h \in {bot,rl}
				fixValue[0] = Node.right;
				variables[1] = start ? Node.leftright : Node.rightleft;
			} else {
				// odd back
				// start nodes : q_i = l and q_h \in {bot,lr}
				// end node : q_j = l and q_h \in {bot,rl}
				fixValue[0] = Node.left;
				variables[1] = start ? Node.leftright : Node.rightleft;
			} 
		} else { 
			variables = new int[2];			
			if (forw) { 
				if (start) {
					// start even forw
					if (index1 != index2) { 
						fixIndex = new int[2];
						fixValue = new int[2];
					
						// q_i = r, q_j = l and q_h \in {bot,lr}
						fixIndex[0] = index1;
						fixIndex[1] = index2;
						fixValue[0] = Node.right;
						fixValue[1] = Node.left;
					} else { 
						fixIndex = new int[1];
						fixValue = new int[1];
						
						// q_i = q_j = lr and q_h \in {bot,lr}
						fixIndex[0] = index1;
						fixValue[0] = Node.leftright;
					} // index1 != index2 
					
					variables[0] = Node.bottom;
					variables[1] = Node.leftright;
				} else { 
					// end even forw
					fixIndex = new int[0];
					fixValue = new int[0];
					variables = new int[2];
			
					// {bot,rl}^k
					variables[0] = Node.bottom;
					variables[1] = Node.rightleft;
				} 
			} else { 
				if (start) {
					// start even back
					fixIndex = new int[0];
					fixValue = new int[0];
					variables = new int[2];
					
					// {bot,lr}^k
					variables[0] = Node.bottom;
					variables[1] = Node.leftright;
				} else { 
					// end even back
					if (index1 != index2) {
						fixIndex = new int[2];
						fixValue = new int[2];
						
						// q_i = l, q_j = r and q_h \in {bot,rl}
						fixIndex[0] = index1;
						fixIndex[1] = index2;
						fixValue[0] = Node.left;
						fixValue[1] = Node.right;
					} else {
						fixIndex = new int[1];
						fixValue = new int[1];
						
						// q_i = q_j = rl and q_h \in {bot,rl}
						fixIndex[0] = index1;
						fixValue[0] = Node.rightleft;
					} // index1 != index2
					
					variables[0] = Node.bottom;
					variables[1] = Node.rightleft;
				} 
			} 
		} 		
	}
	
	private boolean isBottom(Vector<Integer> tuple) {
		for (int i = 0; i < tuple.size(); i ++) {
			if (tuple.elementAt(i).intValue() != Node.bottom)
				return false;
		}
		return true;
	}
	
	public boolean isTerminal(Vector<Integer> tuple) {				
		for (int i = 0; i < tuple.size(); i ++) {
			int e = tuple.elementAt(i).intValue();
			boolean fixed = false;
			
			for (int j = 0; j < fixIndex.length; j ++) { 
				if (i == fixIndex[j]) {
					if (e != fixValue[j])
						return false;
					else
						fixed = true;
					break;
				}
			}
			
			if (!fixed) {
				boolean found = false;
				for (int j = 0; j < variables.length; j ++) {
					if (e == variables[j]) {
						found = true;
						break;
					}
				}

				if (!found)
					return false;
			}
		}	
		
		return true;
	}
	
	private void append(Integer i, Vector<Integer> s) {
		s.add(i);
	}
	
	private void remove(Vector<Integer> s) {
		s.removeElementAt(s.size() - 1);
	}
	
	private void recGetSuccessors(int level, Vector<Integer> currentSet, 
									Vector<Node> result, Graph graph) {
		
		if (Thread.interrupted())
			throw new RuntimeException(" --- interrupted");
		
		if (level == graph.getNodeSize()) {
			
			if (!isBottom(currentSet)) 
				result.add(graph.findOrAdd(currentSet));
			
			return;
		}
				
		for (int i = 0; i < fixIndex.length; i ++) {
			if (level == fixIndex[i] && graph.reached(level)) {
				append(new Integer(fixValue[i]), currentSet);
				recGetSuccessors(level + 1, currentSet, result, graph);
				remove(currentSet);
				return;
			}
		}
		
		for (int i = 0; i < variables.length; i ++) {
			
			switch (variables[i]) {

			case Node.bottom : 
				append(new Integer(variables[i]), currentSet);
				recGetSuccessors(level + 1, currentSet, result, graph);
				remove(currentSet);
				break;
				
			/*	
			case Node.leftright :
				if (_start && !_odd && !_forw) {
					append(new Integer(variables[i]), currentSet);
					recGetSuccessors(level + 1, currentSet, result, graph);
					remove(currentSet);										
				} else if (graph.existsReachable(level, currentSet)) {
					append(new Integer(variables[i]), currentSet);
					recGetSuccessors(level + 1, currentSet, result, graph);
					remove(currentSet);					
				}
				break;
			*/
				
			default :			
				if (graph.reached(level) ) {
					append(new Integer(variables[i]), currentSet);
					recGetSuccessors(level + 1, currentSet, result, graph);
					remove(currentSet);
				} 
			}			

			
//			switch (variables[i]) {
//
//			case Node.bottom : 
//				append(new Integer(variables[i]), currentSet);
//				recGetSuccessors(level + 1, currentSet, result, graph);
//				remove(currentSet);
//				break;
//				
//			/*	
//			case Node.leftright :
//				if (_start && !_odd && !_forw) {
//					append(new Integer(variables[i]), currentSet);
//					recGetSuccessors(level + 1, currentSet, result, graph);
//					remove(currentSet);										
//				} else if (graph.existsReachable(level, currentSet)) {
//					append(new Integer(variables[i]), currentSet);
//					recGetSuccessors(level + 1, currentSet, result, graph);
//					remove(currentSet);					
//				}
//				break;
//			*/
//				
//			default :			
//				if (graph.reached(level) ) {
//					append(new Integer(variables[i]), currentSet);
//					recGetSuccessors(level + 1, currentSet, result, graph);
//					remove(currentSet);
//				} 
//			}			
		}
	}
	
	public Vector<Node> getSuccessors(Graph graph) {
		Vector<Node> result = new Vector<Node>();
		recGetSuccessors(0, new Vector<Integer>(), result, graph);
		return result;
	}

	public String toString() {
		String s =  "[";
		
		s += _start ? " start" : " end";
		s += _odd ? " odd" : " even";
		s += _forw ? " forw" : " back";
		s += " (" + index1 + " " + index2 + ")]";
		
		return s;
	}
}