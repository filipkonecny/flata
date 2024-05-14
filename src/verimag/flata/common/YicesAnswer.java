package verimag.flata.common;
// TODO: remove file
/**
 * codes of Yices' answers
 */
public enum YicesAnswer {
	eYicesSAT, eYicesUNSAT, eYicesUknown;
	public boolean isKnown() { return this==eYicesSAT || this==eYicesUNSAT; }
	public boolean isUnknown() { return this == eYicesUknown; }
	public boolean isUnsat() { return this == eYicesUNSAT; }
	public boolean isSat() { return this == eYicesSAT; }
}