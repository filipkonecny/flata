/**
 * 
 */
package verimag.flata.automata.ca;

import verimag.flata.presburger.Relation;

public enum ReduceOp {
	// identity relation - auxiliary, not present in the input automaton
	LEAF, COMPOSE, CLOSURE, ABSTROCT, ABSTRLIN, HULL, RECONNECT, SUPERNODECL, PROJECTED, SUBSET
	, IDENTITY
	, SUMMARY,PLUGGED_SUMMARY
	, INLINE_RENAME, INLINE_PLUG, INLINE_CALL, INLINE_RETURN;
	public boolean isLeaf() { return this == LEAF; }
	public boolean isCompose() { return this == COMPOSE; }
	public boolean isClosure() { return this == CLOSURE; }
	public boolean isAbstr() { return this == ABSTROCT || this == ABSTRLIN; }
	public boolean isHull() { return this == HULL; }
	public boolean isReconnect() { return this == RECONNECT; }
	public boolean isSummary() { return this == SUMMARY; }
	public boolean isPluggedSummary() { return this == PLUGGED_SUMMARY; }
	
	public static ReduceOp reltype2redtype(Relation.RelType aType) {
		switch(aType) {
		case OCTAGON:
			return ABSTROCT;
		case LINEAR:
			return ABSTRLIN;
		default:
			throw new RuntimeException("internal error: unexpected type");
		
		}
	}
}