package verimag.flata.acceleration.delta;

import verimag.flata.presburger.*;

public class DeltaConvert {

	public static enum MyEnum {
		ALL, OUT_FROM_DBM_CLOSURE;
		
		public boolean ignoresZero() { return this == OUT_FROM_DBM_CLOSURE; }
	}
	
	//private static Variable zero = VariablePool.createSpecial("$");
	//private static Variable zeroP = VariablePool.createSpecial("$'");
	
	public static DBM mat2matParam(DBM m, DBM paramCoef, Variable paramK) {

		Matrix mat = m.mat();
		
		Matrix paramCoefMat = (paramCoef == null)? null : paramCoef.mat();
		
		if (paramCoefMat != null)
			if (mat.size() != paramCoefMat.size())
				throw new RuntimeException("Operation on matrices of incompatible sizes");
		
		int size = mat.size();
		
		FieldStaticInf fs = DeltaClosure.deltaFS();
		
		Matrix retMat = DBM.newMatrix(size, fs);
		retMat.init();
		
		for (int i=0; i<size; ++i) {
			
			for (int j=0; j<size; ++j) {
				
				Field f1 = mat.get(i,j);
				
				if (!f1.isFinite())
					continue;
				
				int i_f1 = f1.toInt();
				int i_f2 = 0;
				{
					if (paramCoefMat != null) {
						Field f2 = paramCoefMat.get(i,j);
						i_f2 = f2.toInt();
					}
				}
				
				Field f = DeltaClosure.deltaBound(i_f1, i_f2);
				retMat.set(i, j, f);
				
			}
		}
		
		return new DBM(DBM.Encoding.DBC, retMat, fs);
	}

	private static boolean isZero(DBM dbm, int i) {
		int l = dbm.mat().size() / 2;
		return dbm.encoding().isDBC() && (i == 0 || i == l);
	}
	
	
	public static LinearRel mat2LCs(DBM dbm, LinearTerm[] substitution) {
		return mat2LCsWithParam(dbm, null, null, substitution);
	}
	public static LinearRel mat2LCsWithParam(DBM dbm, DBM dbmParamCoef, Variable param, LinearTerm[] substitution) {
		
		Matrix m = dbm.mat();
		Matrix paramCoef = (dbmParamCoef == null)? null : dbmParamCoef.mat();
		
		if (paramCoef != null)
			if (m.size() != paramCoef.size())
				throw new RuntimeException("Operation on matrices of incompatible sizes");
		
		LinearRel lcs = new LinearRel();
		
		int size = m.size();
		
		for (int i=0; i<size; ++i) {
			
			for (int j=0; j<size; ++j) {
				
				if (i == j)
					continue;
				
				//if (!isOctagon && ( (i == size/2 && j == 0) || (j == size/2 && i == 0) ))
				//	continue;
				
				Field f = m.get(i,j);
				
				if (!f.isFinite())
					continue;
				
				if (isZero(dbm,i) && isZero(dbm,j))
					continue;
				
				LinearConstr lc = new LinearConstr();
				
				if (!isZero(dbm,i))
					lc.addLinTerm(new LinearTerm(substitution[i]));
				if (!isZero(dbm,j))
					lc.addLinTerm(new LinearTerm(substitution[j]));
				
				lc.addLinTerm(new LinearTerm( null,  -f.toInt()));
				
				if (paramCoef != null) {
					Field f_paramCoef = paramCoef.get(i,j);
					lc.addLinTerm(new LinearTerm( param, -f_paramCoef.toInt()));
				}
				
				lcs.add(lc);
			}
		}
		
		return lcs;
	}
	
	public static DeltaDisjunct mat2modRels_general(DBM dbm, MyEnum keep, LinearTerm[] substitution, boolean isOctagon) {
		return mat2modRels(dbm,keep,substitution,isOctagon,new IntegerInf(FieldType.POS_INF));
	}
	
	public static DeltaDisjunct mat2modRels(DBM dbm, MyEnum keep, LinearTerm[] substitution, boolean isOctagon, IntegerInf maxK) {
		
		Matrix m = dbm.mat();
		
		LinearRel lcs = new LinearRel();
				
		int size = m.size();
		
		boolean onlyZeroK = true; // coefficients of K always 0
		
		for (int i=0; i<size; ++i) {
			
			for (int j=0; j<size; ++j) {
				
				if (i == j)
					continue;
				
				Field f = m.get(i,j);
				
				if (!f.isFinite())
					continue;

				// skip $0-$0'<=0 and $0'-$0<=0
				if (!isOctagon && ( (i == size/2 && j == 0) || (j == size/2 && i == 0) ))
					continue;
				
				LinearConstr lc = new LinearConstr();
				
				lc.addLinTerm(substitution[i].times( 1));
				lc.addLinTerm(substitution[j].times(-1));
				
				boolean zeroK = f.fillLinTerms(lc, DeltaClosure.v_k);
				onlyZeroK &= zeroK;
				
				lcs.add(lc);
			}
		}
		if (!onlyZeroK) {
			if (m.fs() instanceof FieldStatic.ParametricFS)
				lcs.addConstraint(DeltaClosure.lc_k);
			
			if (maxK.isFinite()) {
				lcs.add(DeltaClosure.maxKconstr(maxK.toInt()));
			}
		}
		
		if (DeltaClosure.DEBUG_LEVEL >= DeltaClosure.DEBUG_LOW)
			System.out.println(lcs.toSBClever(Variable.ePRINT_prime));
		
		if (onlyZeroK) {
			Relation r = Relation.toMinType(lcs);
			
			Relation[] l;
			if (!r.contradictory())
				l = new Relation[] {r};
			else
				l = new Relation[0];
			return new DeltaDisjunct(l,lcs, onlyZeroK);
		} else {
			Relation[] tmp = null;
			tmp =lcs.existElim1(DeltaClosure.v_k);
			// else keep null
			return new DeltaDisjunct(tmp,lcs, onlyZeroK);
		}
	}
	
	
	public static void print(DBM dbm, LinearTerm[] substitution, boolean isOctagon, IntegerInf maxK) {
		if (isOctagon)
			printOct(dbm, substitution, maxK);
		else
			printDB(dbm, substitution, maxK);
	}
	private static void printDB(DBM dbm, LinearTerm[] substitution, IntegerInf maxK) {
		StringBuffer sb = new StringBuffer();
		if (maxK!=null) {
			sb.append("k>=0, ");
			if (maxK.isFinite())
				sb.append("k<="+maxK.toInt()+", ");
		}
		
		Matrix m = dbm.mat();
		int size = m.size();
		
		for (int i=0; i<size; ++i) {
			
			for (int j=0; j<size; ++j) {
				
				if (i == j)
					continue;
				
				if ((i == size/2 && j == 0) || (j == size/2 && i == 0) )
					continue;
				
				Field f = m.get(i,j);
				
				if (!f.isFinite())
					continue;
				
				sb.append(""+substitution[i].times( 1).toSB(true)+
						substitution[j].times(-1).toSB(false)+"<="+f.toString()+", ");
			}
		}		
		
		if (DeltaClosure.DEBUG_LEVEL >= DeltaClosure.DEBUG_LOW)
			System.out.println(sb);
	}

	private static void printOct(DBM dbm, LinearTerm[] substitution, IntegerInf maxK) {
		StringBuffer sb = new StringBuffer();
		if (maxK!=null) {
			sb.append("k>=0, ");
			if (maxK.isFinite())
				sb.append("k<="+maxK.toInt()+", ");
		}
		
		Matrix m = dbm.mat();
		int size = m.size();
		
		for (int i=0; i<size; ++i) {
			
			for (int j=2*(i/2); j<size; ++j) {
				
				if (i == j)
					continue;
				
				Field f = m.get(i,j);
				
				if (!f.isFinite())
					continue;
				
				sb.append(""+substitution[i].times( 1).toSB(true)+
						substitution[j].times(-1).toSB(false)+"<="+f.toString()+", ");
			}
		}		
		
		if (DeltaClosure.DEBUG_LEVEL >= DeltaClosure.DEBUG_LOW)
			System.out.println(sb);
	}
}
