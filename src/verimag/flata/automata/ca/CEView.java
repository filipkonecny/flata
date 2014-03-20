package verimag.flata.automata.ca;

import java.util.Map;

import verimag.flata.common.Parameters;
import verimag.flata.presburger.*;

public class CEView {
	
	private CEViewSubtrace mainLocal; // valuation trace of local variables in the main automaton
	private CEViewSubtrace currentLocal; // valuation trace of local variables in the currently processed automaton
	
	private Variable[] varG; // global variables
	private int lenL1;
	
	public CEView(Variable[] aGlobals, Variable[] aMainLocals, int aLenL1) {
		varG = aGlobals;
		mainLocal = new CEViewSubtrace(aMainLocals);
		currentLocal = mainLocal;
		lenL1 = aLenL1;
	}
	
	public void add(StringBuffer[] aL1, StringBuffer aL2, 
			Integer[] aValG, Integer[] aValL) {
		currentLocal.add(aL1, aL2, aValG, aValL);
	}
	public void addInvocation(StringBuffer[] aL1, StringBuffer aL2, 
			Integer[] aValG, Integer[] aValL, 
			Variable[] aNewLocals, StringBuffer[] aL1ret, StringBuffer aL2ret) {
		currentLocal = currentLocal.addInvocation(aL1, aL2, aValG, aValL, aNewLocals, aL1ret, aL2ret);
	}
	public void addReturn(Integer[] aValG, Integer[] aValL) {
		currentLocal.addReturn(aValG, aValL);
		currentLocal = currentLocal.returnFromSubtrace();
	}
	public void addLast(StringBuffer[] aL1err, Integer[] aValG, Integer[] aValL) {
		currentLocal.addLast(aL1err, aValG, aValL);
	}
	
	public StringBuffer toSB() {
		
		// inform the last called procedure that it shouldn't print the
		//lastCalled.setLastCalled();
		
		// calculate the space
		CEViewSubtrace.WidthDataG wd = new CEViewSubtrace.WidthDataG(varG.length, lenL1);
		mainLocal.calculateSpace(wd);
		wd.calculateTotalWidth();
		
		// print
		StringBuffer sb = new StringBuffer();
		
		if (!Parameters.isOnParameter(Parameters.CE_LESS) &&
				varG.length != 0) {
			// declaration of globals
			StringBuffer declareG = CEViewSubtrace.printVarDeclar(wd.widthG, varG);
			declareG.insert(0, "# ");
			declareG.append(" #");
			StringBuffer declarationLine = new StringBuffer();
			int lenL1 = wd.widthL1sum + wd.widthL1.length * CEViewSubtrace.blockSeparatorLen;
			CEViewSubtrace.fillBlank(declarationLine, lenL1 - 2);
			declarationLine.append(declareG);
			
			// print declaration line
			sb.append(declarationLine);
			sb.append("\n");
		}
		
		mainLocal.print(wd, new StringBuffer(), sb);
		
		return sb;
	}
	
	public Map<String,Integer[]> extractLocalTracesOfMain() {
		return this.mainLocal.extractLocalTraces_forMain();
	}
}
