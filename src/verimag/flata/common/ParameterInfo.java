/**
 * 
 */
package verimag.flata.common;

import org.sosy_lab.java_smt.SolverContextFactory.Solvers;

public class ParameterInfo {
	String help; // help string
	boolean isOn;
	int maxArity;
	String[] arguments;
	boolean userHidden = false;
	
	public String[] arguments() { return arguments; }
	
	
	ParameterInfo(boolean aHide, String aHelp, boolean aIsOn) {
		this(aHide,aHelp,aIsOn,0);
	}
	ParameterInfo(boolean aUserHidden, String aHelp, boolean aIsOn, int aMaxArity) {
		this(aHelp,aIsOn,aMaxArity);
		userHidden = aUserHidden;
	}
	ParameterInfo(String aHelp, boolean aIsOn, int aMaxArity) {
		help = aHelp;
		isOn = aIsOn;
		maxArity = aMaxArity;
	}
	ParameterInfo(String aHelp, boolean aIsOn) {
		this(aHelp, aIsOn, 0);
	}
	ParameterInfo(String aHelp) {
		this(aHelp, false);
	}
	
	public boolean isOn() {
		return isOn;
	}

	
	public boolean checkStrategy(String argName) {
		return argName.equals(SCCStrategy.STR_DFS) || argName.equals(SCCStrategy.STR_BFS) || argName.equals(SCCStrategy.STR_GLOBAL);
	}
	public boolean checkDirection(String argName) {
		return argName.equals(DirStrategy.STR_FW) || argName.equals(DirStrategy.STR_BW);
	}
	public boolean checkSolver(String argName) {
		// Check if the argument is a valid solver from enum solvers
		for (Solvers s : Solvers.values()) {
			if (s.toString().equals(argName.toUpperCase()))
				return true;
		}
		return false;
	}
	public void checkArguments(String argName) {
		if (argName.equals(Parameters.SCCSTRATEGY)) {
			if (arguments.length == 0)
				Parameters.incorrectArgs();
			else {
				if (!checkStrategy(arguments[0]))
					Parameters.incorrectArgs();
			}
		} else if (argName.equals(Parameters.DIRSTRATEGY)) {
			if (arguments.length == 0)
				Parameters.incorrectArgs();
			else {
				if (!checkDirection(arguments[0]))
					Parameters.incorrectArgs();
			}
		} else if (argName.equals(Parameters.SOLVER)) {
			if (arguments.length == 0)
				Parameters.incorrectArgs();
			else {
				if (!checkSolver(arguments[0]))
					Parameters.incorrectArgs();
			}
		}
		
	}
}