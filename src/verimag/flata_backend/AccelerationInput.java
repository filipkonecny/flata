package verimag.flata_backend;

import java.util.*;

import nts.interf.base.*;

import acceleration.*;

public class AccelerationInput implements IAccelerationInput {

	private List<IExpr> prefix;
	private List<ILoop> loops;
	private IVarTable varTable;
	
	public List<IExpr> prefix() { return prefix; }
	public List<ILoop> loops() { return loops; }
	public IVarTable varTable() { return varTable; }
	
	public AccelerationInput(List<ILoop> aLoops, IVarTable aVarTable) {
		this(new LinkedList<IExpr>(), aLoops, aVarTable);
	}
	public AccelerationInput(List<IExpr> aPref, List<ILoop> aLoops, IVarTable aVarTable) {
		prefix = aPref;
		loops = aLoops;
		varTable = aVarTable;
	}
}

