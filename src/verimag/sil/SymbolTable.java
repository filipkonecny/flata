package verimag.sil;

import java.util.*;

import nts.interf.base.*;
import nts.interf.expr.IAccessBasic;
import nts.parser.*;

import verimag.flata.presburger.*;

public class SymbolTable {
	
	public VarTable vt; // needed for scalars (substitutions scalar/array[index+constant] are done on AST)
	public VariablePool vp;
	
	// original scalars and arrays
	private List<Variable> origScalars = new LinkedList<Variable>();
	private List<Variable> origArrays = new LinkedList<Variable>();
	// auxiliary scalars and arrays
	private List<Variable> auxScalars = new LinkedList<Variable>();
	private List<Variable> auxArrays = new LinkedList<Variable>();
	
	public Variable tick = null;
	public Variable arrLen = null;
	
	public Variable tick() { return tick; }
	public Variable arrLen() { return arrLen; }
	
	public List<Variable> origScalars() { return origScalars; }
	public List<Variable> origArrays() { return origArrays; }
	
	public List<Variable> allScalars() {
		List<Variable> ret = new LinkedList<Variable>();
		ret.addAll(origScalars);
		ret.addAll(auxScalars);
		return ret;
	}
	
	public SymbolTable(IVarTable aVT) {
		vt = (VarTable)aVT;
		List<String> names = checkIntDeclar(vt);
		
		//vp = VariablePool.createGPool(names);
		
		vp = VariablePool.createEmptyPoolNoDeclar();
		for (String s : names) {
			Variable v = vp.giveVariable(s);
			if (vt.get(s).type().isScalar()) {
				origScalars.add(v);
			} else {
				origArrays.add(v);
			}
		}
		tick = vp.giveVariable(tickName());
		arrLen = vp.giveVariable(NTSParser.SIL_COMMON_ARRAY_SIZE);
	}
	
	private String tickName() {
		if (!vp.containsVariable(TICK)) {
			return TICK;
		} else {
			int i=0;
			String name = null;
			do {
				i++;
				name = TICK+"_"+i;
			} while (vp.containsVariable(name));
			return name;
		}
	}
	
	private static void checkModifierSIL(IVarTableEntry e) {
		if (e.modifier() != EModifier.NO) {
			if (e.modifier() != EModifier.PARAM || !e.name().equals(NTSParser.SIL_COMMON_ARRAY_SIZE)) {
				System.err.println("Modifier '"+e.modifier().toString()+"' is not supported.");
				System.exit(1);
			}
		}
	}
	private static void checkTypeSIL(IType e) {
		if (e.basicType() != EBasicType.INT) {
			System.err.println("SIL supports only integer scalars/arrays.");
			System.exit(1);
		}
		if (e.isArray()) {
			if (e.dimOwn() != e.dimTotal() || e.dimOwn() != 1) {
				System.err.println("SIL only supports integer arrays of dimension 1.");
				System.exit(1);
			}
		}
	}
	private static List<String> checkIntDeclar(IVarTable aVT) {
		List<String> ret = new LinkedList<String>();
		for (IVarTableEntry e : aVT.innermost()) {
			if (e.modifier() == EModifier.TID)
				continue;
			if (e.isPrimed())
				continue;
			checkModifierSIL(e);
			checkTypeSIL(e.type());
			if (e.type().isArray()) {
				IExpr size = e.size().get(0);
				if (!(size instanceof IAccessBasic) || !((IAccessBasic)size).var().name().equals(NTSParser.SIL_COMMON_ARRAY_SIZE)) {
					System.err.println("All SIL arrays must be declared of size N, e.g. a[N] : int");
					System.exit(1);
				}
			}
			ret.add(e.name());
		}
		return ret;
	}
		
	private static String pref_scalar = "p_";
	private static String pref_array = "a_";
	private static String pref_inx = "i_";
	private static int cnt = 0;
	
	private static String TICK = "I";
	
	private String fresh(String aPref) {
		String name;
		do {
			cnt++;
			name = aPref+cnt;
		} while (vp.containsVariable(name) || VariablePool.getSpecialPool().containsVariable(name));
		return name;
	}
	
	public String freshScalar() {
		
		String name = fresh(pref_scalar);
		Variable v = vp.giveVariable(name);
		auxScalars.add(v);
		
		ASTWithoutToken.declareInt(vt, name); // needed only for scalars
		
		return name;
	}
	
	public String freshIndex() {
		return fresh(pref_inx);
	}
	public Variable giveIndexVar(String qVarName) {
		 return VariablePool.createSpecial(qVarName);
	}
	
	public String freshArray() {
		String name = fresh(pref_array);
		Variable v = vp.giveVariable(name); // declare
		auxArrays.add(v);
		return name;
	}
	
}
