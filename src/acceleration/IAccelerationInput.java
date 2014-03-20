package acceleration;

import java.util.List;

import nts.interf.base.*;

public interface IAccelerationInput {
	
	// default should be True
	public List<IExpr> prefix();
	public List<ILoop> loops();
	public IVarTable varTable();
}
