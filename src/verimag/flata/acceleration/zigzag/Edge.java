package verimag.flata.acceleration.zigzag;

public class Edge {
	public static int count = 0;
	
	private Node source;
	private Node dest;
	private Zigzag zigzag;
	private int length;
	
	public int m_mark = 0;

	static final int CRITICAL = 1;
	
	public Edge(Node s, Node d, Zigzag z) {
		source = s;
		dest = d;
		
		if (z != null) {
			zigzag = z;
			length = 1;
		} else {
			zigzag = new Zigzag(s.getSize());
			length = 0;
		}
		
		count ++;
	}
		
	public Node getSource() { return source; }
	
	public Node getDestination() { return dest; }
	
	public Zigzag getZigzag() { return zigzag; }
	
	public void setZigzag(Zigzag z) { zigzag = z; }
	
	public int getWeight() { return zigzag.weight; }
	
	public int getLength() { return length; }
		
	public void setFlag(int flag) { m_mark = m_mark | flag; }

	public void resetFlag(int flag) { m_mark = m_mark & (~flag); }

	public boolean is(int flag) { return (m_mark & flag) != 0; }
	
	public String toString() { return source + " ==" + zigzag + "==> " + dest; }
}
