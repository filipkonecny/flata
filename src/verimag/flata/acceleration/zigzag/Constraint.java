package verimag.flata.acceleration.zigzag; 

public class Constraint {
	private int m_i; // row index

	private int m_j; // target index

	private String m_relcon; // relation constant

	private int prim_mat; // 1=>token1 unprimed, 2=>token1 prime

	Constraint(int i, int j, String weight, int prim) {
		m_i = i; 
		m_j = j;
		m_relcon = weight;
		prim_mat = prim;
	}

	int row_index() {
		return m_i;
	}

	int col_index() {
		return m_j;
	}

	String mat_ele() {
		return m_relcon;
	}

	int matrix_num() {
		return prim_mat;
	}

	void set(String w) {
		m_relcon = w;
	}
	
	public String toString() {
		return "m_i:"+this.m_i+";m_j:"+this.m_j+";m_relcon:"+this.m_relcon+";prim_mat:"+this.prim_mat;
	}
}
