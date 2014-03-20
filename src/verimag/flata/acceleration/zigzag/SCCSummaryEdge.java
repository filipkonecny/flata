package verimag.flata.acceleration.zigzag;

public class SCCSummaryEdge {
	
	private Node entry;
	private Node exit;
	
	private SLSet transferSet;
	
	public SCCSummaryEdge(Node n, Node x) { this(n, x, null); }
			
	public SCCSummaryEdge(Node n, Node x, SLSet t) {
		entry = n;
		exit = x;
		transferSet = t;
	}
	
	public Node getEntry() { return entry; }
	
	public Node getExit() { return exit; }

	public SLSet getTransferSet() { return transferSet; }
	
	public SLSet getExitSet() { return entry.m_X.sum(transferSet); }

	public String toString() { return entry +  " ##" + transferSet + "##> " + exit; }
}
