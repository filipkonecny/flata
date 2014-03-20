package verimag.flata.automata.ca;

import java.util.*;

import verimag.flata.common.Parameters;
import verimag.flata.presburger.*;

public class CEViewSubtrace {
	
	private static class Data {
		StringBuffer[] label1;
		StringBuffer label2;
		Integer[] valG; // valuations of globals
		Integer[] valL; // valuations of locals
		CEViewSubtrace called;
		
		public String toString() {
			return "(L1="+label1+", L2="+label2+", G="+Arrays.toString(valG)+", L="+Arrays.toString(valL)+")";
		}
		
		public Data(StringBuffer[] aL1, StringBuffer aL2, Integer[] aValG, Integer[] aValL) {
			label1 = aL1;
			label2 = aL2;
			valG = aValG;
			valL = aValL;
		}
		public Data(StringBuffer[] aL1, StringBuffer aL2, Integer[] aValG, Integer[] aValL, CEViewSubtrace aCalled) {
			this(aL1,aL2,aValG,aValL);
			called = aCalled;
		}
	}
	
	private Variable[] varsL; // sub-trace variables
	private int[] widthL; // width for each variable
	
	private StringBuffer[] label1ret = null; // labels to print before valuation of counters on return from the sub-trace
	private StringBuffer label2ret = null;   // label  to print after  valuation of counters on return from the sub-trace
	
	private CEViewSubtrace calling = null; // procedure that called this one
	
	private List<Data> data = new LinkedList<Data>();
	
	private boolean lastCalled = false;
	private void setLastCalled() { lastCalled = true; }
	
	public CEViewSubtrace(Variable[] aVars, CEViewSubtrace aCalling, 
			StringBuffer[] aL1ret, StringBuffer aL2ret) {
		this(aVars);
		calling = aCalling;
		label1ret = aL1ret;
		label2ret = aL2ret;
	}
	public CEViewSubtrace(Variable[] aVars) {
		varsL = aVars;
		widthL = new int[varsL.length];
	}
	
	public void add(StringBuffer[] aL1, StringBuffer aL2, 
			Integer[] aValG, Integer[] aValL) {
		data.add(new Data(aL1, aL2, aValG, aValL));
	}
	public CEViewSubtrace addInvocation(StringBuffer[] aL1, StringBuffer aL2, 
			Integer[] aValG, Integer[] aValL, 
			Variable[] aNewLocals, StringBuffer[] aL1ret, StringBuffer aL2ret) {
		CEViewSubtrace newSubtrace = new CEViewSubtrace(aNewLocals, this, aL1ret, aL2ret);
		data.add(new Data(aL1, aL2, aValG, aValL, newSubtrace));
		return newSubtrace;
	}
	public void addReturn(Integer[] aValG, Integer[] aValL) {
		data.add(new Data(label1ret, label2ret, aValG, aValL));
	}
	public void addLast(StringBuffer[] aL1err, Integer[] aValG, Integer[] aValL) {
		this.setLastCalled();
		data.add(new Data(aL1err, new StringBuffer(), aValG, aValL));
	}
	
	public CEViewSubtrace returnFromSubtrace() {
		return calling;
	}
	
	private static void calculateMax(int[] max, Variable[] val) {
		for (int i=0; i<max.length; i++) {
			max[i] = Math.max(max[i], val[i].name().length());
		}
	}
	private static void calculateMax(int[] max, StringBuffer[] val) {
		for (int i=0; i<max.length; i++) {
			max[i] = Math.max(max[i], val[i].length());
		}
	}
	private static void calculateMax(int[] max, Integer[] val) {
		for (int i=0; i<max.length; i++) {
			if (val[i] != null) {
				double w = Math.log10((double)val[i]);
				int wi = (int)w;
				//if (w-((double)wi) > (double)0.00001) {
				if (Math.pow((double)10, (double)wi) < w) {
					wi++;
				}
				wi++;
				max[i] = Math.max(max[i], wi);
			}
		}
	}
	// global width data
	public static class WidthDataG {
		int[] widthG;
		int widthGSum;
		
		// max length over labels
		int[] widthL1;
		int widthL1sum;
		
		int maxL2 = 0;
		
		public WidthDataG(int cntG, int cntL) {
			widthG = new int[cntG];
			Arrays.fill(widthG, 1);
			widthL1 = new int[cntL];
			Arrays.fill(widthL1, 0);
		}
		
		public void calculateTotalWidth() {
			widthGSum = 0;
			for (int i : widthG)
				widthGSum += i;
			widthGSum += indentCounter*widthG.length-1;
			widthL1sum = 0;
			for (int i : widthL1)
				widthL1sum += i;
		}
	}
	public static int indentLoc = 1;
	public static int indentCounter = 1;
	public static String blockSeparator = " | ";
	public static int blockSeparatorLen = blockSeparator.length();
	public void calculateSpace(WidthDataG wd) {
		
		Arrays.fill(widthL, 1);
		calculateMax(widthL,varsL);
		
		for (Data d : data) {
			calculateMax(wd.widthL1,d.label1);
			wd.maxL2 = Math.max(wd.maxL2, d.label2.length());
			
			calculateMax(wd.widthG,d.valG);
			calculateMax(widthL,d.valL);
			
			if (d.called != null) { // global data recursively
				d.called.calculateSpace(wd);
			}
		}
	}
	
	public static void fillBlank(StringBuffer sb, int n) {
		for (int ii=0; ii<n; ii++) {
			sb.append(" ");
		}
	}
	private static StringBuffer printVals(int[] width, Integer[] val) {
		StringBuffer sb = new StringBuffer();
		
		for (int i=0; i<val.length; i++) {
			String aux;
			if (val[i] == null)
				aux = "-";
			else
				aux = ""+val[i];
			
			fillBlank(sb, width[i]-aux.length());
			
			sb.append(aux);
			
			if (i != val.length-1) // counter separator
				fillBlank(sb, indentCounter);
		}
		
		return sb;
	}
	private static StringBuffer printLab(int[] width, StringBuffer[] lab) {
		StringBuffer sb = new StringBuffer();
		
		for (int i=0; i<width.length; i++) {
			fillBlank(sb, width[i]-lab[i].length());
			sb.append(lab[i]);
			if (i != width.length-1)
				sb.append(blockSeparator);
		}
		
		return sb;
	}
	public static StringBuffer printVarDeclar(int[] width, Variable[] lab) {
		StringBuffer sb = new StringBuffer();
		
		for (int i=0; i<width.length; i++) {
			fillBlank(sb, width[i]-lab[i].name().length());
			sb.append(lab[i].name());
			if (i != width.length-1)
				fillBlank(sb, indentCounter);
		}
		
		return sb;
	}
	public void print(WidthDataG wd, StringBuffer localStack, StringBuffer sb) {
		
		if (Parameters.isOnParameter(Parameters.CE_LESS)) {
			for (Data d : data) {
				sb.append(printLab(wd.widthL1, d.label1)+"\n");
				//sb.append("("+wd.widthL1+",  "+d.label1.toString()+")"+"\n");
				
				if (d.called != null) {
					d.called.print(wd, new StringBuffer(), sb);
				}
			}
			//sb.append("\n");
			return;
		}
		
		StringBuffer declareL = printVarDeclar(this.widthL,varsL);
		declareL.insert(0, "# ");
		declareL.append(" #");
		
		StringBuffer declarationLine = new StringBuffer();
		int lenL = (localStack.length() == 0)? 0 : localStack.length()+blockSeparatorLen;
		boolean isG = wd.widthG.length != 0;
		int lenG = isG ? wd.widthGSum+blockSeparatorLen : 0;
		int lenL1 = wd.widthL1sum + wd.widthL1.length * blockSeparatorLen;
		fillBlank(declarationLine, lenL1 + lenG + lenL - 2);
		//declarationLine.append(localStack);
		declarationLine.append(declareL);
		
		// print declaration line
		sb.append(declarationLine);
		sb.append("\n");
		
		for (Data d : data) {
			StringBuffer sbG = printVals(wd.widthG, d.valG);
			StringBuffer sbL = printVals(widthL, d.valL);
			StringBuffer sbL1 = printLab(wd.widthL1, d.label1);
			StringBuffer sbL2 = d.label2; //printLab(wd.maxL2, d.label2);
			
			StringBuffer newLocalStack = new StringBuffer(localStack);
			if (newLocalStack.length() != 0) {
				newLocalStack.append(blockSeparator);
			}
			newLocalStack.append(sbL);
			
			StringBuffer newLocalStackBlanks = new StringBuffer();
			fillBlank(newLocalStackBlanks, newLocalStack.length());
			
			StringBuffer currentPrint = new StringBuffer();
			currentPrint.append(sbL1);
			currentPrint.append(blockSeparator);
			if (isG) {
				currentPrint.append(sbG);
				currentPrint.append(blockSeparator);
			}
			currentPrint.append(newLocalStack);
			currentPrint.append(blockSeparator);
			currentPrint.append(sbL2);
			
			sb.append(currentPrint);
			sb.append("\n");
			
			if (d.called != null) {
				d.called.print(wd, newLocalStackBlanks, sb);
			}
		}
		
	}
	
	public Map<String,Integer[]> extractLocalTraces_forMain() {
		int loc = this.varsL.length;
		Integer[][] traces = new Integer[loc][this.data.size()];
		int i=0;
		for (Data d : data) {
			for (int j=0; j<loc; j++) {
				traces[j][i] = d.valL[j];
			}
			i++;
		}
		Map<String,Integer[]> m = new HashMap<String,Integer[]>();
		for (int j=0; j<loc; j++) {
			m.put(this.varsL[j].name(), traces[j]);
		}
		return m;
	}
}
