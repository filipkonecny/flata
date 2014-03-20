package verimag.flata.presburger;

import java.util.*;

public class Matrix {
	
	private int size;
	private Field[][] mat; 
	
	private FieldStatic fs;
	public FieldStatic fs() { return fs; }
	
	public int size() { return size; }

	public Matrix submatrix(int[] inxs) {
		int newsize = inxs.length;
		
		Matrix ret = new Matrix(newsize, this.fs);
		
		for (int i=0; i<newsize; i++)
			for (int j=0; j<newsize; j++) {
				ret.set(i, j, this.get(inxs[i], inxs[j]));
			}
		
		return ret;
	}
	// matrix 'from' is copied to specified indices in 'inxs'
	public void copyFrom(Matrix from, int[] inxs) {
		int l = inxs.length;
		for (int r=0; r<l; r++) {
			for (int c=0; c<l; c++) {
				Field f = from.get(r, c);
				set(inxs[r], inxs[c], f);
			}
		}
	}
	public void copyFrom2(Matrix from, int[] inxs) {
		int l = inxs.length;
		for (int r=0; r<l; r++) {
			for (int c=0; c<l; c++) {
				Field f = from.get(inxs[r], inxs[c]);
				set(r, c, f);
			}
		}
	}
	
	public Matrix maxEntrywise(Matrix other) {
		if (this.size != other.size)
			throw new RuntimeException("internal error: incompatible matrix size");
		
		Matrix ret = new Matrix(size, this.fs);
		for (int i = 0; i < size; i ++)
			for (int j = 0; j < this.size; j ++)
				ret.set(i, j, this.get(i, j).max(other.get(i, j)));
		
		return ret;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		//sb.append("domain: "+Arrays.toString(domain)+"\n");
		//sb.append("range : "+Arrays.toString(range) +"\n");
		
		for (int i=0; i<size; ++i) {
			sb.append("[");
			for (int j=0; j<size; ++j) {
				sb.append(mat[i][j]);
				if (j!=size-1) sb.append(",");
			}
			sb.append("]\n");
		}
		
		return sb.toString();
	}
	
	/**
	 * Creates matrix of a given size. Elements are not initialized. TODO
	 */
	public Matrix(int height, FieldStatic aFieldStatic) {
		mat = new Field[height][height];
		size = height;
		fs = aFieldStatic;
	}
	
	public Matrix(Matrix m) {
		mat = new Field[m.size][m.size];
		size = m.size;
		
		fs = m.fs;
		
		for (int i=0; i<size; ++i)
			for (int j=0; j<size; ++j)
				mat[i][j] = m.mat[i][j];
	}
	
	public boolean equals(Object other) {

		if (!(other instanceof Matrix))
			return false;
		
		Matrix m2 = (Matrix)other;
		
		if (size != m2.size)
			return false;
		
		for (int i=0; i<size; ++i)
			for (int j=0; j<size; ++j)
				if (!mat[i][j].equals(m2.mat[i][j]))
					return false;
		
		return true;
	}
	// inxsR and inxsC restrict rows and columns of the first operand
	// assumption: first operand after projection via inxsR and inxsC has same dimensions as second operand
	public boolean lessEq(Matrix m2, int[] inxsR, int[] inxsC) {
		int lR = inxsR.length;
		int lC = inxsC.length;
		for (int i=0; i<lR; ++i) {
			for (int j=0; j<lC; ++j) {
				int ii = inxsR[i];
				int jj = inxsC[j];
				if (!mat[ii][jj].lessEq(m2.mat[i][j])) {
					return false;
				}
			}
		}
		return true;
	}
	public boolean lessEq(Matrix m2) {
		
		if (size != m2.size)
			return false;
		
		for (int i=0; i<size; ++i)
			for (int j=0; j<size; ++j)
				if (!mat[i][j].lessEq(m2.mat[i][j])) {
					return false;
				}
		
		return true;
	}
	
	public void init() {
		for (int i=0; i<size; ++i)
			for (int j=0; j<size; ++j)
				mat[i][j] = fs.initVal();
		
		initDBM();
	}
	private void initDBM() {
		for (int i=0; i<size; ++i)
			mat[i][i] = fs.zero();
	}
	
	public void resetRowCol(int x) {
		for (int i=0; i<size; i++) {
			mat[x][i] = fs.initVal();
			mat[i][x] = fs.initVal();
		}
		mat[x][x] = fs.zero();
	}
	
	/**
	 * 
	 * @param i row
	 * @param j column
	 * @param f element
	 */
	public void set(int i, int j, Field f) {
		mat[i][j] = f;
	}
	public void setMin(int i, int j, Field f) {
		mat[i][j] = mat[i][j].min(f);
	}
	/**
	 * 
	 * @param i row
	 * @param j column
	 * @return f element
	 */
	public Field get(int i, int j) {
		return mat[i][j];
	}
	
	
	public List<Field> storeDiagonal() {
		List<Field> diagonal = new LinkedList<Field>();
		storeDiagonal(diagonal);
		return diagonal;
	}
	public void storeDiagonal(List<Field> diagonal) {
		for (int i=0; i<this.size; ++i) {
			diagonal.add(this.get(i, i));
		}
	}
	
//	public Matrix eraseRowsCols(BitSet aRows, BitSet aCols) {
//		
//		Matrix m = new Matrix(aRows.cardinality(), aCols.cardinality(), this.fs);
//		
//		int iN = 0;
//		int i=0;
//		while (i<h) {
//			
//			if (!aRows.get(i)) {
//				i ++;
//				continue;
//			}
//			
//			int jN = 0;
//			int j=0;	
//			while (j<w) {
//				
//				if (!aCols.get(j)) {
//					j ++;
//					continue;
//				}
//				
//				m.set(iN, jN, get(i,j));
//				
//				++j; ++jN;
//			}
//			
//			++i; ++iN;
//		}
//		
//		return m;
//	}

	
	// skips rows {rowL, .. ,rowL+rows} and columns {colL, .. ,colL+cols} 
	public Matrix eraseRowsCols(int rowL, int rows, int colL, int cols) {
		Matrix m = new Matrix(size-rows, this.fs);
		
		int iN = 0;
		int i=0;
		while (i<size) {
			
			if (i==rowL) {
				i += rows;
			}
			
			int jN = 0;
			int j=0;	
			while (j<size) {
				
				if (j==colL) {
					j += cols;
				}
				
				m.set(iN, jN, get(i,j));
				
				++j; ++jN;
			}
			
			++i; ++iN;
		}
		return m;
	}
	
	public void copy(int r0, int c0, Matrix m2, int r2, int c2, int h, int w) {
		int i0; int i0Bnd = r0 + h;
		int j0; int j0Bnd = c0 + w;
		int i2;
		int j2;
		
		for (i0 = r0, i2 = r2; i0 < i0Bnd ; ++i0, ++i2) {
			for (j0 = c0, j2 = c2; j0 < j0Bnd ; ++j0, ++j2) {
				this.set(i0, j0, m2.get(i2, j2));
			}
		}
	}
	// fill the submatrix by result of min operation on submatrices of m1 and m2 
	public void fillMin(int r0, int c0, Matrix m1, int r1, int c1, Matrix m2, int r2, int c2, int h, int w) {
		int i0; int i0Bnd = r0 + h;
		int j0; int j0Bnd = c0 + w;
		int i1;
		int j1;
		int i2;
		int j2;
		
		for (i0 = r0, i1 = r1, i2 = r2; i0 < i0Bnd ; ++i0, ++i1, ++i2) {
			for (j0 = c0, j1 = c1, j2 = c2; j0 < j0Bnd ; ++j0, ++j1, ++j2) {
				Field f1 = m1.get(i1, j1);
				Field f2 = m2.get(i2, j2);
				Field min = f1.min(f2);
				this.set(i0, j0, min);
			}
		}
	}
	
	public Matrix times(int c) {
		Matrix ret = new Matrix(size,fs);
		for (int i=0; i<size; ++i)
			for (int j=0; j<size; ++j) {
				ret.set(i, j, get(i,j).times(fs.giveField(c)));
			}
		
		return ret;
	}
	
	public Matrix plus(Matrix m2) {
		if (size != m2.size)
			throw new RuntimeException("Operation on matrices of incompatible sizes");
		
		Matrix ret = new Matrix(size,fs);
		for (int i=0; i<size; ++i)
			for (int j=0; j<size; ++j) {
				ret.set(i, j, get(i,j).plus(m2.get(i,j)));
			}
		
		return ret;
	}
	public Matrix minus(Matrix m2) {
		if (size != m2.size)
			throw new RuntimeException("Operation on matrices of incompatible sizes");
		
		Matrix ret = new Matrix(size,fs);
		for (int i=0; i<size; ++i)
			for (int j=0; j<size; ++j) {
				ret.set(i, j, get(i,j).minus(m2.get(i,j)));
			}
		
		return ret;
	}
	
//	public static class Solve_axb {
//		private boolean solved;
//		private Matrix m;
//		
//		public boolean solved() { return solved; }
//		public Matrix mat() { return m; }
//		
//		
//		private Solve_axb(boolean bb, Matrix mm) {
//			solved = bb;
//			m = mm;
//		}
//		public static Solve_axb noSolution() {
//			return new Solve_axb(false, null);
//		}
//		public static Solve_axb solved(Matrix solved) {
//			return new Solve_axb(true, solved);
//		}
//	}
	public Matrix solve_axb(Matrix m2) {
		Matrix ret = new Matrix(size,fs);
		for (int i=0; i<size; ++i)
			for (int j=0; j<size; ++j) {
				Field f1 = get(i,j);
				Field f2 = m2.get(i,j);
				boolean b1 = f1.isFinite();
				boolean b2 = f2.isFinite();
				if (!b1 || !b2) {
					if (b1 == b2) {
						ret.set(i, j, fs.zero());
					} else {
						return null;
					}
				} else {
					ret.set(i, j, f2.minus(f1));
				}
			}
		return ret;
	}
	
	public float density() {
		int hits = 0;
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++) {
				if (i!=j && get(i,j).isFinite())
					hits++;
			}
		
		return (float)hits / (size*size);
	}
	
}
