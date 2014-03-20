package verimag.flata.recur_bounded;

import java.util.List;

import nts.interf.base.IExpr;

public class TCall {
	private String from;
	private String to;
	private String toOrig;
	// 1..n, index of corresponding procedure in the original program
	private int calleeInx;
	private boolean isInOrder;
	//private List<String> havoc;
	private List<IExpr> actualParameters;
	
	
	
	public String from() { return from; }
	public String to() { return to; }
	public String toOrig() { return toOrig; }
	public int calleeInx() { return calleeInx; }
	public boolean isInOrder() { return isInOrder; }
	//public List<String> havoc() { return havoc; }
	public List<IExpr> actualParameters() { return actualParameters; }
	
	public TCall(String aFrom, String aTo, String aToOrig, int aCalleeInx, boolean aIsInOrder, List<IExpr> aActParam/*, List<String> aHavoc*/) {
		from = aFrom;
		to = aTo;
		toOrig = aToOrig;
		calleeInx = aCalleeInx;
		isInOrder = aIsInOrder;
		//havoc = aHavoc;
		actualParameters = aActParam;
	}
}
