package verimag.flata.automata.ca;

import java.io.StringWriter;
import java.util.List;

import verimag.flata.common.CR;
import verimag.flata.common.IndentedWriter;
import verimag.flata.presburger.DisjRel;

public class Summary {
	
	// note: summaries use modulo relations to avoid representing partially eliminated variables 
	// (e.g. only x, but not x' is eliminated)
	// DBM and octagons always require to encode both x and x' for any x
	
	private boolean successful;
	private List<CATransition> reachEnd;
	private List<CATransition> reachError;

	public boolean successful() { return successful; }
	public List<CATransition> reachEnd() { return reachEnd; }
	public List<CATransition> reachError() { return reachError; }
	
	// -----------------------------printing methods---------------------------------- //
	public String toString() {
		StringWriter sw = new StringWriter();
		IndentedWriter iw = new IndentedWriter(sw, "  ");
		print(iw);
		return sw.toString();
	}
	public void print(IndentedWriter iw) {
		iw.writeln("Reachability relation {");
		iw.indentInc();
		
		iw.writeln("final: ");
		iw.indentInc();
		if (reachEnd.isEmpty())
			iw.writeln("false");
		else
			CR.col2sb(iw, reachEnd);
		iw.indentDec();
		
		iw.writeln("error: ");
		iw.indentInc();
		if (reachError.isEmpty())
			iw.writeln("false");
		else
			CR.col2sb(iw, reachError);
		iw.indentDec();

		iw.indentDec();
		iw.writeln("}");
	}

	
	private Summary() {
		successful = false;
	}
	private Summary(List<CATransition> aReachEnd, List<CATransition> aReachError) {
		successful = true;
		reachEnd = aReachEnd;
		reachError = aReachError;
	}
	
	public static Summary unsuccessful() {
		return new Summary();
	}
	public static Summary successful(List<CATransition> aReachEnd, List<CATransition> aReachError) {
		return new Summary(aReachEnd, aReachError);
	}
	public DisjRel finalSummaryAsRel() {
		DisjRel ret = new DisjRel();
		for (CATransition t : this.reachEnd) {
			ret.addDisj(t.rel());
		}
		return ret;
	}
	
}
