package verimag.flata.acceleration.zigzag; 

import java.util.*;

public class ZigzagMatrix {
	private int m_size;

	private String m[][];

	private boolean s[][];
	
	public static final int STRING_TYPE = 0;
	public static final int BOOLEAN_TYPE = 1;

	public ZigzagMatrix(int size, int type) {

		if (type == STRING_TYPE) {
			m_size = size;
			m = new String[m_size][m_size];
			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					m[i][j] = "inf";
		}
		if (type == BOOLEAN_TYPE) {

			m_size = size;
			s = new boolean[m_size][5];
			for (int i = 0; i < size; i++)
				for (int j = 0; j < 5; j++)
					s[i][j] = false;
		}
	}
	
	public ZigzagMatrix(ZigzagMatrix mat)
	{
		m_size = mat.m_size;
		
		if (mat.m != null)
		{
			m = new String[m_size][m_size];
			for (int i = 0; i < m_size; i++)
				for (int j = 0; j < m_size; j++)
					m[i][j] = mat.m[i][j];
		}
		
		if (mat.s != null)
		{
			s = new boolean[m_size][5];
			for (int i = 0; i < m_size; i++)
				for (int j = 0; j < 5; j++)
					s[i][j] = mat.s[i][j];
		}
	}

	public void writes(int i, int j, String k) {
		m[i][j] = k;
	}

	public void writeb(int i, int j, boolean k) {
		s[i][j] = k;
	}

	public String eles(int i, int j) {
		return m[i][j];
	}

	public boolean eleb(int i, int j) {
		return s[i][j];
	}

	public int getSize() {
		return m_size;
	}
	
	void fw() {
		for (int k = 0; k < m_size; k++)
			for (int i = 0; i < m_size; i++)
				for (int j = 0; j < m_size; j++) {
					if ((!m[i][j].equals("inf"))
							&& (!m[i][k].equals("inf"))
							&& (!m[k][j].equals("inf"))
							&& Integer.parseInt(m[i][k])
									+ Integer.parseInt(m[k][j]) < Integer
									.parseInt(m[i][j]))
						m[i][j] = Integer.toString(Integer.parseInt(m[i][k])
								+ Integer.parseInt(m[k][j]));
					if ((m[i][j].equals("inf")) && (!m[i][k].equals("inf"))
							&& (!m[k][j].equals("inf")))
						m[i][j] = Integer.toString(Integer.parseInt(m[i][k])
								+ Integer.parseInt(m[k][j]));
				}
	}

	void red() {
		boolean[][] b;
		b = new boolean[m_size][m_size];
		for (int i = 0; i < m_size; i++)
			for (int j = 0; j < m_size; j++)
				b[i][j] = false;

		for (int i = 0; i < m_size; i++)
			for (int j = 0; j < m_size; j++)
				for (int k = 0; k < m_size; k++)
					if ((!m[i][j].equals("inf")) && (!m[i][k].equals("inf"))
							&& (!m[k][j].equals("inf")) && (i != k) && (j != k)) {
						if (Integer.parseInt(m[i][j]) == Integer
								.parseInt(m[i][k])
								+ Integer.parseInt(m[k][j]))
						{
							b[i][j] = true;
						}
					}
		for (int i = 0; i < m_size; i++)
			for (int j = 0; j < m_size; j++)
				if (b[i][j] == true)
					m[i][j] = "inf";
	}
	
	@SuppressWarnings("unchecked")
	void reduceImproved()
	{
		int[] represented = new int[m_size]; 
		Vector[][] zeroWeightCycles = new Vector[m_size][m_size];
		int cycleNo = 0;
		int i,j,k;
		
		for (i = 0; i < m_size; i++)
			represented[i] = 0;
			
		for (i = 0; i < m_size; i++)
			for (j = 0; j < m_size; j++)
				zeroWeightCycles[i][j] = new Vector();
		
			//First look at which zero weight cycles each arc belongs to, 
			//starting with the zero length cycles and extending them to 
			//larger cycles
		for (i = 0; i < m_size; i++)
			if (!m[i][i].equals("inf") && Integer.parseInt(m[i][i])==0)
				zeroWeightCycles[i][i].add(new Integer(cycleNo++));
		
		for (k = 0; k < m_size; k++)
			for (i = 0; i < m_size; i++)
				for (j = 0; j < m_size; j++)
					if ((!m[i][j].equals("inf")) && (!m[i][k].equals("inf"))
							&& (!m[k][j].equals("inf")) && (i != k) && (j != k) 
							&& ( (Integer.parseInt(m[i][j]) == Integer.parseInt(m[i][k])
							+ Integer.parseInt(m[k][j])) && zeroWeightCycles[i][j].size()>0))
						for (int p = 0; p < zeroWeightCycles[i][j].size(); p++)
						{
							zeroWeightCycles[i][k].add(zeroWeightCycles[i][j].get(p));
							zeroWeightCycles[k][j].add(zeroWeightCycles[i][j].get(p));
						}
		
			//erase the first diagonal
		for (i = 0; i < m_size; i++)
			zeroWeightCycles[i][i] = new Vector();

			//designated arcs for vertex-(zero weight)cycle and 
			//(zero weight)cycle-vertex pairs
		int[][] in = new int[cycleNo][m_size];
		int[][] out = new int[m_size][cycleNo];
			//cycle number for the designated arc
		int[][] cycle = new int[m_size][m_size];
		
		for (i = 0; i < m_size; i++)
			for (j = 0; j < cycleNo; j++)
				{
					in[j][i]=-1;
					out[i][j]=-1;
				}
		
		for (i = 0; i < m_size; i++)
			for (j = 0; j < m_size; j++)
				cycle[i][j]=-1;

			//compute designated arcs
		for (k = 0; k < m_size; k++)
			for (i = 0; i < m_size; i++)
				for (j = 0; j < m_size; j++)
					if ((!m[i][j].equals("inf")) && (!m[i][k].equals("inf"))
							&& (!m[k][j].equals("inf")) && (i != k) && (j != k) && (i != j)
							&& (Integer.parseInt(m[i][j]) == Integer.parseInt(m[i][k])
							+ Integer.parseInt(m[k][j])))
					{
						if (zeroWeightCycles[i][k].size()>0)
						{
							for (int p = 0; p < zeroWeightCycles[i][k].size(); p++)
							{
								int q = ((Integer)zeroWeightCycles[i][k].get(p)).intValue();
								if (in[q][j]<0)
								{
									in[q][j]=i;
									represented[i]++;
									represented[j]++;
								}
							}
						}
						if (zeroWeightCycles[k][j].size()>0)
						{
							for (int p = 0; p < zeroWeightCycles[k][j].size(); p++)
							{
								int q = ((Integer)zeroWeightCycles[k][j].get(p)).intValue();
								if (out[i][q]<0)
								{
									out[i][q]=j;
									represented[i]++;
									represented[j]++;
								}
							}
						}
					}
		
		boolean[][] checked = new boolean[m_size][m_size];
		for (i = 0; i < m_size; i++)
			for (j = 0; j < m_size; j++)
				checked[i][j] = false;
		
		for (k = 0; k < m_size; k++)
			for (i = 0; i < m_size; i++)
				for (j = 0; j < m_size; j++)
					if ((!m[i][j].equals("inf")) && (!m[i][k].equals("inf"))
							&& (!m[k][j].equals("inf")) && (i != k) && (j != k) && (i != j)
							&& (Integer.parseInt(m[i][j]) == Integer.parseInt(m[i][k])
							+ Integer.parseInt(m[k][j])))
						checked[i][j] = true;

		for (i = 0; i < m_size; i++)
			for (j = 0; j < m_size; j++)
				if ((!m[i][j].equals("inf")) && !checked[i][j])
				{
					represented[i]++;
					represented[j]++;
				}

				//some out pairs can make redundant in pairs and vice versa...
			for (i = 0; i < m_size; i++)
				for (j = 0; j < m_size; j++)
					for (int p = 0; p < cycleNo; p++)
						if (in[p][i]>=0 && in[p][i]==in[p][j] &&
								zeroWeightCycles[i][j].size()>0)
						{
							if (represented[j]>3)
							{
								represented[j]--;
								represented[in[p][i]]--;
								in[p][j]=-1;
							}
							else if (represented[i]>3)
							{
								represented[i]--;
								represented[in[p][i]]--;
								in[p][i]=-1;
							}
						}
					
			for (i = 0; i < m_size; i++)
				for (j = 0; j < m_size; j++)
					for (int p = 0; p < cycleNo; p++)
						if (out[i][p]>=0 && out[i][p]==out[j][p] &&
								zeroWeightCycles[i][j].size()>0)
						{
							if (represented[j]>3)
							{
								represented[j]--;
								represented[out[i][p]]--;
								out[j][p]=-1;
							}
							else if (represented[i]>3)
							{
								represented[i]--;
								represented[out[i][p]]--;
								out[i][p]=-1;
							}
						}
		for (i = 0; i < m_size; i++)
			for (j = 0; j < m_size; j++)
				for (k = 0; k < cycleNo; k++)
					if (out[i][k] == j || in[k][j] == i)
						cycle[i][j] = k;
		
			//actual reduce step: an arc can be removed as redundant only if 
			//it is not a designated arc connected to a zero weight cycle
		boolean[][] b = new boolean[m_size][m_size];
		for (i = 0; i < m_size; i++)
			for (j = 0; j < m_size; j++)
				b[i][j] = false;

		for (i = 0; i < m_size; i++)
			for (j = 0; j < m_size; j++)
				for (k = 0; k < m_size; k++)
					if ((!m[i][j].equals("inf")) && (!m[i][k].equals("inf"))
							&& (!m[k][j].equals("inf")) && (i != k) && (j != k)
							&& cycle[i][j] < 0) //not a designated arc 
					{
						if (Integer.parseInt(m[i][j]) == Integer
								.parseInt(m[i][k])
								+ Integer.parseInt(m[k][j]))
						{
							b[i][j] = true;
						}
					}

		for (i = 0; i < m_size; i++)
			for (j = 0; j < m_size; j++)
				if (b[i][j] == true)
					m[i][j] = "inf";
	}
	
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < m_size; i++)
		{
			for (int j = 0; j < m_size; j++)
				result.append(m[i][j]+" ");
			result.append("\r\n");
		}
		
		return result.toString();
	}
	
	public ZigzagMatrix transform(int size, Parser rel, int multiple, int[] p, int[] q)
	{
		int i, j;
		ZigzagMatrix result = new ZigzagMatrix(size, ZigzagMatrix.STRING_TYPE);
		
		if (multiple == 0)
			return result;
		
		for (i = 0; i < size; i++)
			for (j = 0; j < size; j++)
			{
				String crt = m[i][j];
				if (crt.equalsIgnoreCase("inf"))
					result.writes(i, j, crt);
				else
				{
					int alpha = Integer.parseInt(crt);
					int newVal = (int)((alpha + q[j] * rel.getDiv(j) + rel.getRem(j) 
							- p[i] * rel.getDiv(i) - rel.getRem(i)) / multiple);
					result.writes(i, j, new String(newVal+""));
				}
			}
		
		return result;
	}
	
	public boolean containedIn(ZigzagMatrix mat)
	{
		int i,j;
		for (i = 0; i < m_size; i++)
			for (j = 0; j < m_size; j++)
				if (!m[i][j].equals("inf"))
				{
					if (mat.eles(i, j).equals("inf"))
						return false;
					else
					{
						int x = Integer.parseInt(m[i][j]);
						int y = Integer.parseInt(mat.eles(i, j));
						if (x > y)
							return false;
					}
				}
		
		return true;
	}
	
	/**
	 * Tests if there are negative weights on the first diagonal;
	 * if the closure (Floyd-Warshall) has been performed,
	 * this would imply negative weight cycles 
	 */
	public boolean consistent()
	{
		for (int i = 0; i < m_size; i++)
			if (!m[i][i].equals("inf"))
			{
				int val = Integer.parseInt(m[i][i]);
				if (val< 0)
					return false;
			}
		return true;
	}
}
