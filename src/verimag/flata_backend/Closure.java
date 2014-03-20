package verimag.flata_backend;

import nts.interf.base.IExpr;
import nts.interf.base.IVarTable;

public class Closure implements acceleration.IClosure {
	
	private boolean succeeded;
	private IExpr expr;
	private IVarTable varTable;
	
	public boolean succeeded() { return succeeded; }
	public IExpr getClosure() { return expr; }
	public IVarTable varTable() { return varTable; }

	public Closure(IExpr aExpr, IVarTable aVarTable) {
		succeeded = true;
		expr = aExpr;
		varTable = aVarTable;
	}
	public Closure() {
		succeeded = false;
	}
}
