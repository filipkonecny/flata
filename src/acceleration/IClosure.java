package acceleration;

import nts.interf.base.IExpr;
import nts.interf.base.IVarTable;

public interface IClosure {

	public boolean succeeded();
	public IExpr getClosure();
	public IVarTable varTable();
}
