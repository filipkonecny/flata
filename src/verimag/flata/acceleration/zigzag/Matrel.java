package verimag.flata.acceleration.zigzag; 

import java.io.IOException;
import java.util.Vector;

public class Matrel {
	public ZigzagMatrix m1;

	public ZigzagMatrix m2;

	Matrel(int s) {
		m1 = new ZigzagMatrix(s, ZigzagMatrix.STRING_TYPE);	//forward adjacency matrix
		m2 = new ZigzagMatrix(s, ZigzagMatrix.STRING_TYPE); //backward adjacency matrix
	}

	void writemat(Vector<Constraint> c) throws IOException {
		for (int i = 0; i < c.size(); i++) {
			Constraint n = c.elementAt(i);
			if (n.matrix_num() == Parser.FORWARD) {
				m1.writes(n.row_index(), n.col_index(), n.mat_ele());
			} else if (n.matrix_num() == Parser.BACKWARD)
				m2.writes(n.row_index(), n.col_index(), n.mat_ele());
		}
	}
	
	public Matrel transform(Parser rel, int multiple, int[] p, int[] q)
	{
		Matrel result = new Matrel(m1.getSize());
		result.m1 = m1.transform(m1.getSize(), rel, multiple, p, q);	
		result.m2 = m2.transform(m2.getSize(), rel, multiple, q, p);	
		
		return result;
	}
	
	public boolean equals(Object mrl)
	{
		return m1.containedIn(((Matrel)mrl).m1) && m2.containedIn(((Matrel)mrl).m2);
	}
	
	public int getSize()
	{
		return m1.getSize();
	}
	
	public String toString()
	{
		return m1.toString()+"\r\n...\r\n"+m2.toString();
	}
}
