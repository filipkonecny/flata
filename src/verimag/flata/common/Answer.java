/**
 * 
 */
package verimag.flata.common;


public enum Answer {
	TRUE, FALSE, DONTKNOW;
	
	public boolean isTrue() { return this == TRUE; }
	public boolean isFalse() { return this == FALSE; }
	public boolean isDontKnow() { return this == DONTKNOW; }
	
	public static Answer createAnswer(boolean b) {
		if (b)
			return TRUE;
		else
			return FALSE;
	}
	public Answer and(Answer other) {
		if (this == TRUE && other == TRUE)
			return TRUE;
		else if (this == FALSE || other == FALSE)
			return FALSE;
		return DONTKNOW;
	}
	public Answer or(Answer other) {
		if (this == TRUE || other == TRUE)
			return TRUE;
		else if (this == FALSE && other == FALSE)
			return FALSE;
		return DONTKNOW;
	}

	// TODO: remove
	public static Answer createFromYicesSat(YicesAnswer satisfiableYices) {
		switch(satisfiableYices) {
		case eYicesSAT: return Answer.TRUE;
		case eYicesUNSAT: return Answer.FALSE;
		default: return Answer.DONTKNOW;
		}
	}
	public static Answer createFromYicesUnsat(YicesAnswer satisfiableYices) {
		switch(satisfiableYices) {
		case eYicesSAT: return Answer.FALSE;
		case eYicesUNSAT: return Answer.TRUE;
		default: return Answer.DONTKNOW;
		}
	}
	public Answer negate() {
		if (this == TRUE)
			return FALSE;
		else if (this == FALSE)
			return TRUE;
		else
			return this;
	}
}