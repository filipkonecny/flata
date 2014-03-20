package verimag.flata.automata.ca;

import java.util.*;

import verimag.flata.common.Label;
import verimag.flata.presburger.*;

public class Call implements Label {
	
	private CA calling, called;
	private CATransition callingPoint;
	private List<LinearConstr> args;
	private List<LinearConstr> argsIn = new LinkedList<LinearConstr>();
	private List<Variable> argsOut = new LinkedList<Variable>();
	
	public CA calling() { return calling; }
	public CA called() { return called; }
	public List<LinearConstr> arguments() { return args; }
	public List<LinearConstr> argsIn() { return argsIn; }
	public List<Variable> argsOut() { return argsOut; }
	
	public boolean isCall() { return true; }
	public boolean isRelation() { return false; }
	
	public void setCallingPoint(CATransition t) { this.callingPoint = t; }
	public CATransition getCallingPoint() { return this.callingPoint; }
	
	public void setCalled(CA aCa) { this.called = aCa; }
	
	public StringBuffer getInvocationAssignment() {
		StringBuffer ret = new StringBuffer();
		
		ret.append(this.toString()+" ; ");
		
		Variable[] portIn = called.portIn();
		int i=0;
		for (LinearConstr lc : argsIn) {
			if (i>0) ret.append(", ");
			ret.append(portIn[i]+" <-- "+lc.toStringNoROP());
			i++;
		}
		return ret;
	}
	public StringBuffer getReturnAssignment() {
		StringBuffer ret = new StringBuffer();
		
		Variable[] portOut = called.portOut();
		int i=0;
		for (Variable v : argsOut) {
			if (i>0) ret.append(", ");
			ret.append(v.getCounterpart()+" <-- "+portOut[i].getCounterpart());
			i++;
		}
		return ret;
	}
	public Integer[] evaluateInputs(Map<Variable,Integer> m) {
		Integer[] ret = new Integer[argsIn.size()];
		
		int i=0;
		for (LinearConstr lc : argsIn) {
			ret[i] = lc.evaluate(m);
			i++;
		}
		
		return ret;
	}
	public Integer[] evaluateOutputs(Map<Variable,Integer> m) {
		Integer[] ret = new Integer[argsOut.size()];
		
		int i=0;
		for (Variable v : argsOut) {
			ret[i] = m.get(v);
			i++;
		}
		
		return ret;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(called.name()+"(");
		
		boolean b = true;
		for (LinearConstr v : args) {
			if (b) {
				sb.append(v.toStringNoROP());
				b = false;
			} else {
				sb.append(","+v.toStringNoROP());
			}
		}
			
		sb.append(")");
		
		return sb.toString();
	}
//	private String suggestStringCall(int i1, int i2) {
//		StringBuffer sb = new StringBuffer(called.name()+"(");
//		
//		boolean b = true;
//		for (int ii=0; ii<i1; ii++) {
//			if (b) {
//				b = false;
//			} else {
//				sb.append(",");
//			}
//			sb.append(args.get(ii).unprimeAll());
//		}
//		
//		for (int ii=i1; ii<i1+i2; ii++) {
//			if (b) {
//				b = false;
//			} else {
//				sb.append(",");
//			}
//			sb.append(args.get(ii).getPrimedName());
//		}
//		
//		sb.append(")");
//		
//		return sb.toString();
//	}
	private String callMsg() {
		return "The call "+toString()+" in "+calling.name();
	}
	
	public Call(CA aCaller, CA aCalled, 
			List<LinearConstr> aArgsIn, List<Variable> aArgsOut) {
		this(aCaller, aCalled, null, null, aArgsIn, aArgsOut);
		args = new LinkedList<LinearConstr>(aArgsIn);
		for (Variable v : aArgsOut) {
			LinearConstr c = new LinearConstr();
			c.addLinTerm(new LinearTerm(v,1));
			args.add(c);
		}
	}
	private Call(CA aCaller, CA aCalled, CATransition aCallingPoint, List<LinearConstr> aArgs, 
			List<LinearConstr> aArgsIn, List<Variable> aArgsOut) {
		calling = aCaller;
		called = aCalled;
		callingPoint = aCallingPoint;
		args = aArgs;
		this.argsIn = aArgsIn;
		this.argsOut = aArgsOut;
	}
	public Call(Call other, CATransition aCallingPoint) {
		calling = other.calling;
		called = other.called;
		callingPoint = aCallingPoint; // !!
		args = new LinkedList<LinearConstr>(other.args);
		argsIn = new LinkedList<LinearConstr>(other.argsIn);
		argsOut = new LinkedList<Variable>(other.argsOut);
	}
	// calling point will be set later
	public Call(CA aCaller, CA aCalled/*, CATransition aCallingPoint*/, List<LinearConstr> aArguments) {
		
		calling = aCaller;
		called = aCalled;
		//callingPoint = aCallingPoint;
		args = aArguments;
	}
	public Call copy(CATransition aCallingPoint) {
		return new Call(this, aCallingPoint);
	}
	public Call copy(Rename aRenVals, VariablePool aVarPool, CATransition aCallingPoint) {
		
		List<LinearConstr> aux = new LinkedList<LinearConstr>();
		for (LinearConstr lc : args) {
			aux.add(new LinearConstr(lc,aRenVals, aVarPool));
		}
		
		List<LinearConstr> auxIn = new LinkedList<LinearConstr>();
		for (LinearConstr lc : argsIn) {
			auxIn.add(new LinearConstr(lc,aRenVals, aVarPool));
		}
		
		List<Variable> auxOut = new LinkedList<Variable>();
		for (Variable v : argsOut) {
			auxOut.add(aVarPool.giveVariable(aRenVals.getNewNameFor(v.name())));
		}
		
		return new Call(calling,called,aCallingPoint,aux,auxIn,auxOut);
	}
	
	public static void check(List<Call> col) {
		
		for (Call c : col) {
			
			// check
			int i = c.called.portInSize();
			int o = c.called.portOutSize();
			
			if (c.args.size() != i+o) {
				System.err.println(c.callMsg()+": the number of parameters doesn't match");
				System.exit(-1);
			}
			
			for (int ii=0; ii<i; ii++) {
				LinearConstr lc = c.args.get(ii);
				for (Variable v : lc.variables()) {
					if (v.isPrimed()) {
						System.err.println(c.callMsg()+", argument \""+c.args.get(ii)+"\": only terms over unprimed counters may be passed as an input [e.g.: "+lc.unprimeAll()+"].");
						System.exit(-1);
					}
				}
				c.argsIn.add(lc);
			}
			
			for (int ii=i; ii<i+o; ii++) {
				LinearConstr lc = c.args.get(ii);
				boolean b = lc.size() == 1;
				LinearTerm lt = lc.terms().iterator().next();
				if (b) {
					b = (lt.variable() != null) && (lt.coeff() == 1);
				}
				if (!b) {
					System.err.println(c.callMsg()+", argument \""+c.args.get(ii)+"\": only primed counters may be passed as an output.");
					System.exit(-1);
				}
				
				c.argsOut.add(lt.variable());
			}
			
			for (int ii=0; ii<c.argsOut.size(); ii++) {
				for (int jj=ii+1; jj<c.argsOut.size(); jj++) {
					if (c.argsOut.get(ii).equals(c.argsOut.get(jj))) {
						System.err.println(c.callMsg()+", multiple use of counter \""+c.argsOut.get(ii)+"\" as output.");
						System.exit(-1);
					}
				}
			}
			
		}
	}
	@Override
	public StringBuffer toSB_NTS() {
		StringBuffer sb = new StringBuffer();
		
		if (this.argsOut.size() > 0) {
			sb.append("(");
			Iterator<Variable> i = argsOut.iterator();
			while(i.hasNext()) {
				sb.append(i.next());
				if (i.hasNext()) {
					sb.append(",");
				}
			}
			sb.append(") = ");
		}
		sb.append(called.name()+"(");
		if (this.argsIn.size() > 0) {
			Iterator<LinearConstr> i = argsIn.iterator();
			while(i.hasNext()) {
				sb.append(i.next().toStringNoROP());
				if (i.hasNext()) {
					sb.append(",");
				}
			}
		}
		sb.append(")");
		
		return sb;
	}
}
