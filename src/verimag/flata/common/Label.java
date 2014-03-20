package verimag.flata.common;

public interface Label {
	//public Label copy();
	//public Label copy(Rename aRenVals);
	public boolean isCall();
	public boolean isRelation();
	public StringBuffer toSB_NTS();
}
