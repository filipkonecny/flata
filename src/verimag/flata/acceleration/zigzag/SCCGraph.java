package verimag.flata.acceleration.zigzag;

import java.util.*;

public class SCCGraph {
	private Vector<SCCNode> nodes;
	// private Vector<SCCEdge> edges;
	
	private Vector<Node> stack; 
	
	private int max_dfs = 0;
	private int scc_id = 0;
	
	public SCCGraph() {
		nodes = new Vector<SCCNode>();
		// edges = new Vector<SCCEdge>();
		
		stack = new Vector<Node>();
	}
	
	private void addSCCNode(Vector<Node> scc) {		
		SCCNode node = new SCCNode(scc);
		
		for (int i = 0; i < nodes.size(); i ++) {
			SCCNode next = nodes.elementAt(i);
			
			if (node.isEqualTo(next)) {
				next.initialize(scc_id ++);
				nodes.removeElementAt(i);				
				nodes.add(next);

				return;
			}
		}
		
		node.initialize(scc_id ++);
		node.howard();
		nodes.add(node);
	}	
	
	private void tarjan(Node n) {
		// one day this should be written using iteration
		n.m_dfs = max_dfs;
		n.m_lowlink = max_dfs;
		
		max_dfs ++;
		
		stack.add(n);
		
		n.setFlag(Node.STACKED);
		n.setFlag(Node.REACHED);
		
		Vector<Node> scc = new Vector<Node>();
		
		for (int k = 0; k < n.outEdges().size(); k ++) {
			Edge outEdge =  n.outEdges().get(k);
			Node target_node = outEdge.getDestination();
			
			if (!target_node.is(Node.REACHED)) {
				tarjan(target_node);
				
				if (n.m_lowlink > target_node.m_lowlink)
					n.m_lowlink = target_node.m_lowlink;
			} else if (target_node.is(Node.STACKED)) {
				if (n.m_lowlink > target_node.m_dfs) {
					n.m_lowlink = target_node.m_dfs;
				}
			}
		}
		
		if (n.m_lowlink == n.m_dfs) {
			for (int s = stack.size() - 1; s >= 0; s --) {
				Node nn = stack.get(s);
				
				stack.remove(s);
				nn.resetFlag(Node.STACKED);
				scc.add(nn);
				
				if (nn == n) {
					addSCCNode(scc);
					break;
				}
			}
		}		
	}
	
	private void iterateReachableSCC(Node entry) {
		LinkedList<Node> worklist = new LinkedList<Node>();
		
		entry.m_X.addPoint( new Point(0,0) );
		
		entry.setFlag(Node.SCC_EVALUATE);
		worklist.addFirst(entry);
		
		while ( !worklist.isEmpty() ) {
			Node nextNode = worklist.removeLast();
			SCCNode nextSCC = nextNode.getSCCNode();
			
			nextNode.resetFlag(Node.SCC_EVALUATE);
			
			nextSCC.addEntry(nextNode);
			nextSCC.initExits();
			
			nextSCC.iterate(nextNode);
			
			for (int i = 0; i < nextSCC.getExits().size(); i ++) {
				Node exit = nextSCC.getExits().elementAt(i);
								
				for (int j = 0; j < exit.outEdges().size(); j ++) {
					Edge edge = exit.outEdges().elementAt(j);
					Node successor = edge.getDestination();
					
					if ( !nextSCC.contains(successor) &&
						 !successor.is(Node.SCC_EVALUATE) ) {
						successor.setFlag(Node.SCC_EVALUATE);
						worklist.addFirst(successor);
					}
					
					if (Thread.interrupted()) {
						throw new RuntimeException(" --- interrupted");
					}
				}
			}
		}
	}
		
	public void buildSemilinearSets(Node root) {
		tarjan(root);
		
		if (Graph.DEBUG >= Graph.HIGH_DEBUG) {
			System.out.println("\n*** begin dump scc ***");
			
			for (int i = 0; i < nodes.size(); i ++)
				System.out.println(nodes.elementAt(i));
			
			System.out.println("*** end dump scc ***\n");
		}
		
		iterateReachableSCC(root);		
	}
}
