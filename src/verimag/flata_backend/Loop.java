package verimag.flata_backend;

import java.util.List;

import nts.interf.base.IExpr;

import acceleration.*;

public class Loop implements ILoop {

	public List<IExpr> l;
	public Loop(List<IExpr> aL) { l = aL; }
	public List<IExpr> expressions() { return l; }
}
